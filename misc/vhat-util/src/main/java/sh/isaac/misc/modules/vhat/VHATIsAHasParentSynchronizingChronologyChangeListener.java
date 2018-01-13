/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.misc.modules.vhat;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.misc.constants.VHATConstants;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.coordinate.EditCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.utility.Frills;

/**
 * 
 * {@link VHATIsAHasParentSynchronizingChronologyChangeListener}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Service
@RunLevel(value = LookupService.SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class VHATIsAHasParentSynchronizingChronologyChangeListener implements ChronologyChangeListener {
   private static final Logger LOG = LogManager.getLogger(VHATIsAHasParentSynchronizingChronologyChangeListener.class);

   // Cached VHAT module sequences
   private static NidSet VHAT_MODULES = null;

   private static NidSet getVHATModules(StampCoordinate coord) {
      // Initialize VHAT module sequences cache
      if (VHAT_MODULES == null || VHAT_MODULES.size() == 0) { // Should be unnecessary
         VHAT_MODULES = NidSet.of(Frills.getAllChildrenOfConcept(MetaData.VHAT_MODULES____SOLOR.getNid(), true, true, coord));
      }
      return VHAT_MODULES;
   }

   private static StampCoordinate VHAT_STAMP_COORDINATE = null;

   private static StampCoordinate getVHATDevelopmentLatestStampCoordinate() {
      if (VHAT_STAMP_COORDINATE == null) {
         StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, TermAux.DEVELOPMENT_PATH.getNid());
         VHAT_STAMP_COORDINATE = new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, getVHATModules(StampCoordinates.getDevelopmentLatest()),
               Status.ANY_STATUS_SET);
      }

      return VHAT_STAMP_COORDINATE;
   }

   private boolean enabled = true;

   /**
    * Set of nids of component versions created by VHATIsAHasParentSynchronizingChronologyChangeListener which should NOT be processed by
    * VHATIsAHasParentSynchronizingChronologyChangeListener, in order to avoid infinite recursion
    */
   private final Set<Integer> nidsOfGeneratedSememesToIgnore = new ConcurrentSkipListSet<>();
   private final UUID providerUuid = UUID.randomUUID();

   private final ConcurrentSkipListSet<Integer> semanticNidsForUnhandledLogicGraphChanges = new ConcurrentSkipListSet<>();
   private final ConcurrentSkipListSet<Integer> sememeSequencesForUnhandledHasParentAssociationChanges = new ConcurrentSkipListSet<>();

   private ConcurrentLinkedQueue<Future<?>> inProgressJobs = new ConcurrentLinkedQueue<>();
   private ScheduledFuture<?> sf;

   public VHATIsAHasParentSynchronizingChronologyChangeListener() {
   }

   /**
    * Allow clients to exempt and unexempt specified components from handling by the listener, presumably because the client is creating all necessary component
    * changes itself
    * 
    * @param nids
    */
   public void addNidsOfGeneratedSememesToIgnore(int... nids) {
      for (int nid : nids) {
         nidsOfGeneratedSememesToIgnore.add(nid);
      }
   }

   /**
    * Allow clients to exempt and unexempt specified components from handling by the listener, presumably because the client is creating all necessary component
    * changes itself
    * 
    * @param nids
    */
   public void removeNidsOfGeneratedSememesToIgnore(int... nids) {
      for (int nid : nids) {
         nidsOfGeneratedSememesToIgnore.remove(nid);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see gov.vha.isaac.ochre.api.VHATIsAHasParentSynchronizingChronologyChangeListenerI#disable()
    */
   @Override
   public void disable() {
      enabled = false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see gov.vha.isaac.ochre.api.VHATIsAHasParentSynchronizingChronologyChangeListenerI#enable()
    */
   @Override
   public void enable() {
      enabled = true;
   }

   @PostConstruct
   private void startMe() {
      Get.commitService().addChangeListener(this);
      // Prevent a memory leak, by scheduling a thread to periodically empty the job list
      sf = Get.workExecutors().getScheduledThreadPoolExecutor().scheduleAtFixedRate((() -> waitForJobsToComplete()), 5, 5, TimeUnit.MINUTES);
      if (Get.configurationService().inDBBuildMode())
      {
         disable();
      }
   }

   @PreDestroy
   private void stopMe() {
      Get.commitService().removeChangeListener(this);
      sf.cancel(true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#getListenerUuid()
    */
   @Override
   public UUID getListenerUuid() {
      return providerUuid;
   }

   /*
    * (non-Javadoc)
    * 
    * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#handleChange(gov.vha.isaac.ochre.api.component.concept.ConceptChronology)
    */
   @Override
   public void handleChange(ConceptChronology cc) {
      // Only using handleCommit()
      if (!enabled) {
         return;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#handleChange(gov.vha.isaac.ochre.api.component.sememe.SemanticChronology)
    */
   @Override
   public void handleChange(SemanticChronology sc) {
      if (!enabled) {
         LOG.debug("Ignoring, while listener disabled, change to sememe " + sc.getVersionType() + " " + sc.getNid() + " " + sc.getNid());

         return;
      }

      // Determine if this is a sememe generated by this listener
      if (nidsOfGeneratedSememesToIgnore.contains(sc.getNid())) {
         // This is a sememe generated by this listener, so remove it and ignore it
         nidsOfGeneratedSememesToIgnore.remove(sc.getNid());
         LOG.info("Ignoring recursive change to sememe " + sc.getVersionType() + " " + sc.getNid() + " " + sc.getNid());
         return;
      }

      if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
         semanticNidsForUnhandledLogicGraphChanges.add(sc.getNid());
         LOG.info("Adding LogicGraph " + sc.getNid() + " " + sc.getNid() + " to the list of commits to process");
      } else if (sc.getVersionType() == VersionType.DYNAMIC && sc.getAssemblageNid() == VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid()) {
         sememeSequencesForUnhandledHasParentAssociationChanges.add(sc.getNid());
         LOG.info("Adding Association sememe " + sc.getNid() + " " + sc.getNid() + " to the list of commits to process");
      } else {
         // Ignore if not either LOGIC_GRAPH or DYNAMIC has_parent association sememe
         return;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#handleCommit(gov.vha.isaac.ochre.api.commit.CommitRecord)
    */
   @Override
   public void handleCommit(CommitRecord commitRecord) {
      if (!enabled) {
         LOG.debug("Ignoring, while listener disabled, change to commit {} ", commitRecord);

         return;
      }
      // For new and updated VHAT logic graphs, create or retire has_parent associations, as appropriate
      LOG.info("HandleCommit looking for - " + semanticNidsForUnhandledLogicGraphChanges.size() + " logic graphs: " + semanticNidsForUnhandledLogicGraphChanges + " and "
            + sememeSequencesForUnhandledHasParentAssociationChanges.size() + " associations: " + sememeSequencesForUnhandledHasParentAssociationChanges + " the commit contains "
            + commitRecord.getSemanticNidsInCommit());
      for (int logicGraphNid : semanticNidsForUnhandledLogicGraphChanges.toArray(new Integer[semanticNidsForUnhandledLogicGraphChanges.size()])) {
         if (!commitRecord.getSemanticNidsInCommit().contains(logicGraphNid)) {
            LOG.trace("HandleCommit NOT handling logic graph " + logicGraphNid + " which is not contained in the commit record list: "
                  + commitRecord.getSemanticNidsInCommit().toString());
            semanticNidsForUnhandledLogicGraphChanges.remove(logicGraphNid);
            continue;
         }
         semanticNidsForUnhandledLogicGraphChanges.remove(logicGraphNid);
         LOG.debug("HandleCommit handling logic graph " + logicGraphNid + ". " + semanticNidsForUnhandledLogicGraphChanges.size() + " logic graphs remaining");

         SemanticChronology sc = Get.assemblageService().getSemanticChronology(logicGraphNid);
         @SuppressWarnings("unchecked")
         LatestVersion<Version> logicGraph = sc.getLatestVersion(getVHATDevelopmentLatestStampCoordinate());
         if (!logicGraph.isPresent()) {
            // Apparently not a relevant LOGIC_GRAPH sememe
            return;
         }

         LOG.debug("Running VHATIsAHasParentSynchronizingChronologyChangeListener handleChange() on VHAT LOGIC_GRAPH dynamic sememe " + sc.getPrimordialUuid() + " for concept "
               + Get.identifierService().getUuidPrimordialForNid(sc.getReferencedComponentNid()));

         ConceptChronology referencedConcept = Get.conceptService().getConceptChronology(((LogicGraphVersion) logicGraph.get()).getReferencedComponentNid());

         // Handle changes to LOGIC_GRAPH
         // In practice, this should only be for a new concept
         Set<Integer> parentsAccordingToNewLogicGraphVersion = new HashSet<>();
         if (logicGraph.get().getStatus() == Status.INACTIVE) {
            // Retire all has_parent association sememes
         } else {
            parentsAccordingToNewLogicGraphVersion.addAll(Frills.getParentConceptNidsFromLogicGraph((LogicGraphVersion) logicGraph.get()));
            if (parentsAccordingToNewLogicGraphVersion.size() == 0) {
               String msg = "Encountered logic graph for concept NID=" + referencedConcept.getNid() + ", UUID=" + referencedConcept.getPrimordialUuid()
                     + " with no specified parents (concept nodes in necessary set nodes)";
               LOG.warn(msg);
            }
         }

         final EditCoordinate editCoordinate = new EditCoordinateImpl(logicGraph.get().getAuthorNid(), logicGraph.get().getModuleNid(), logicGraph.get().getPathNid());

         final Collection<DynamicVersion<?>> hasParentAssociationDynamicSememes = getActiveHasParentAssociationDynamicSememesAttachedToComponent(
               ((LogicGraphVersion) logicGraph.get()).getReferencedComponentNid());
         Set<Integer> parentsAccordingToHasParentAssociationDynamicSememes = new HashSet<>();
         AtomicReference<Future<?>> f = new AtomicReference<Future<?>>(null);
         // For each active has_parent association
         if (hasParentAssociationDynamicSememes.size() > 0) {
            Runnable runnable = new Runnable() {
               public void run() {
                  List<Chronology> itemsToCommit = new ArrayList<>();
                  for (DynamicVersion<?> hasParentSemantic : hasParentAssociationDynamicSememes) {
                     final DynamicUUID target = (DynamicUUID) hasParentSemantic.getData(0);
                     final int targetSeq = Get.identifierService().getNidForUuids(target.getDataUUID());
                     // Accumulate a list of parents from has_parent sememes
                     parentsAccordingToHasParentAssociationDynamicSememes.add(targetSeq);
                     // If the active has_parent is not represented in an active logic graph, retire it
                     if (!parentsAccordingToNewLogicGraphVersion.contains(targetSeq)) {
                        MutableDynamicVersion<?> mutableVersion = hasParentSemantic.getChronology().createMutableVersion(Status.INACTIVE, editCoordinate);
                        mutableVersion.setData(hasParentSemantic.getData());
                        LOG.info("Putting association sememe " + mutableVersion.getNid() + " " + mutableVersion.getNid() + " into the ignore list prior to commit");

                        // This is a sememe generated by this listener, so add it to list so listener will ignore it
                        nidsOfGeneratedSememesToIgnore.add(hasParentSemantic.getNid());
                        try {
                           Get.commitService().addUncommitted(mutableVersion.getChronology()).get();
                           itemsToCommit.add(mutableVersion.getChronology());
                        } catch (Exception e) {
                           // New version of this sememe failed to be added to commit list, so remove sememe from list so listener won't ignore it
                           nidsOfGeneratedSememesToIgnore.remove(hasParentSemantic.getNid());
                           LOG.error("FAILED calling addUncommitted() to retire VHAT has_parent association sememe " + hasParentSemantic, e);
                        }
                     }
                  }

                  if (itemsToCommit.size() > 0) {
                     for (Chronology c : itemsToCommit) {
                        try {
                           Get.commitService().commit(c, editCoordinate, "Retiring VHAT has_parent sememes").get();
                        } catch (InterruptedException | ExecutionException e) {
                           LOG.error("FAILED commit while retiring " + c + " VHAT has_parent sememes");
                        }
                     }
                  }
               }
            };
            f.set(Get.workExecutors().getExecutor().submit(runnable));
            inProgressJobs.add(f.get());
         }

         // For each parent from an active logic graph

         // If the parent from the active logic graph is not already represented by an active has_parent sememe
         // Create new or unretire existing has_parent sememe
         Runnable runnable = new Runnable() {
            public void run() {
               // Our logic depends on the retirements above, being done...
               if (f.get() != null) {
                  try {
                     f.get().get();
                  } catch (Exception e) {
                     LOG.error("error in required prior thread", e);
                     return;
                  }
               }

               List<Chronology> builtObjects = new ArrayList<>();
               for (int parentAccordingToNewLogicGraphVersion : parentsAccordingToNewLogicGraphVersion) {
                  if (!parentsAccordingToHasParentAssociationDynamicSememes.contains(parentAccordingToNewLogicGraphVersion)) {

                     Optional<UUID> uuidOfParentAccordingToNewLogicGraphVersion = Get.identifierService().getUuidPrimordialForNid(parentAccordingToNewLogicGraphVersion);
                     if (!uuidOfParentAccordingToNewLogicGraphVersion.isPresent()) {
                        LOG.error("FAILED finding UUID for parent seq=" + parentAccordingToNewLogicGraphVersion + " from logic graph for concept NID=" + referencedConcept.getNid()
                              + ", UUID=" + referencedConcept.getPrimordialUuid());
                        continue;
                     }

                     // Check for existence of retired version of has_parent association with this parent target
                     Optional<Version> retiredHasParentSemanticVersion = getInactiveHasParentAssociationDynamicSememeAttachedToComponent(referencedConcept.getNid(),
                           parentAccordingToNewLogicGraphVersion);
                     if (retiredHasParentSemanticVersion.isPresent()) {
                        // If retired version of has_parent association with this parent target exists, then re-activate it
                        MutableDynamicVersion<?> unretiredHasParentSememeVersion = ((SemanticChronology) (retiredHasParentSemanticVersion.get().getChronology()))
                              .createMutableVersion(Status.ACTIVE, editCoordinate);
                        unretiredHasParentSememeVersion.setData(((DynamicVersion<?>) retiredHasParentSemanticVersion.get()).getData());
                        // This is a sememe generated by this listener, so add it to list so listener will ignore it
                        nidsOfGeneratedSememesToIgnore.add(retiredHasParentSemanticVersion.get().getNid());

                        try {
                           Get.commitService().addUncommittedNoChecks(((SemanticChronology) retiredHasParentSemanticVersion.get().getChronology())).get();
                        } catch (Exception e) {
                           // New version of this sememe failed to be added to commit list, so remove sememe from list so listener won't ignore it
                           nidsOfGeneratedSememesToIgnore.remove(retiredHasParentSemanticVersion.get().getNid());
                           LOG.error("FAILED calling addUncommitted() to unretire has_parent association sememe (target UUID=" + uuidOfParentAccordingToNewLogicGraphVersion
                                 + ") of VHAT concept " + referencedConcept, e);
                           return;
                        }
                        try {
                           Get.commitService()
                                 .commit(retiredHasParentSemanticVersion.get().getChronology(), editCoordinate, "Unretiring VHAT has_parent association sememe (target UUID="
                                       + uuidOfParentAccordingToNewLogicGraphVersion + ") for concept (UUID=" + referencedConcept.getPrimordialUuid() + ")")
                                 .get();
                        } catch (Exception e) {
                           // New version of this sememe may have failed to be committed, so remove sememe from list so listener won't ignore it
                           nidsOfGeneratedSememesToIgnore.remove(retiredHasParentSemanticVersion.get().getNid());
                           LOG.error("FAILED calling commit() to unretire has_parent association sememe (target UUID=" + uuidOfParentAccordingToNewLogicGraphVersion
                                 + ") of VHAT concept " + referencedConcept, e);
                           return;
                        }
                        LOG.debug("Unretired has_parent association sememe {} with target {} for concept {}", retiredHasParentSemanticVersion.get().getPrimordialUuid(),
                              uuidOfParentAccordingToNewLogicGraphVersion, referencedConcept.getPrimordialUuid());
                     } else {
                        // If retired version on this has_parent association does not exist, then create a new one

                        DynamicData[] data = new DynamicData[1];
                        data[0] = new DynamicUUIDImpl(uuidOfParentAccordingToNewLogicGraphVersion.get());

                        SemanticBuilder<? extends SemanticChronology> associationSememeBuilder = Get.semanticBuilderService().getDynamicBuilder(referencedConcept.getNid(),
                              VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid(), data);

                        // This is a sememe generated by this listener, so add it to list so listener will ignore it
                        LOG.info("Putting association sememe " + associationSememeBuilder.getNid() + " into the ignore list prior to build");
                        nidsOfGeneratedSememesToIgnore.add(associationSememeBuilder.getNid());

                        builtObjects.add(associationSememeBuilder.build(editCoordinate, ChangeCheckerMode.ACTIVE).getNoThrow());

                        LOG.debug("Built new has_parent association sememe with SOURCE/CHILD={} and TARGET/PARENT={}", referencedConcept.getPrimordialUuid(),
                              uuidOfParentAccordingToNewLogicGraphVersion.get());
                     }
                  }
               }

               if (builtObjects.size() > 0) {
                  for (Chronology c : builtObjects) {
                     try {
                        Get.commitService().commit(c, editCoordinate, "Committing new has_parent association sememes.").get();
                     } catch (InterruptedException | ExecutionException e) {
                        LOG.error("FAILED committing new has_parent association sememes", e);
                        return;
                     }
                  }
               }
            }
         };
         inProgressJobs.add(Get.workExecutors().getExecutor().submit(runnable));
      }

      // For new, updated or retired VHAT has_parent association sememes, update existing logic graph
      for (int hasParentSemanticNid : sememeSequencesForUnhandledHasParentAssociationChanges) {
         if (!commitRecord.getSemanticNidsInCommit().contains(hasParentSemanticNid)) {
            LOG.trace("HandleCommit NOT handling hasParent association " + hasParentSemanticNid + " which is not contained in the commit record list: "
                  + commitRecord.getSemanticNidsInCommit().toString());
            sememeSequencesForUnhandledHasParentAssociationChanges.remove(hasParentSemanticNid);
            continue;
         }
         sememeSequencesForUnhandledHasParentAssociationChanges.remove(hasParentSemanticNid);
         LOG.debug("HandleCommit handling hasParent association " + hasParentSemanticNid + ". " + sememeSequencesForUnhandledHasParentAssociationChanges.size()
               + " hasParent associations remaining");

         SemanticChronology sc = Get.assemblageService().getSemanticChronology(hasParentSemanticNid);
         LatestVersion<Version> hasParentSemantic = sc.getLatestVersion(getVHATDevelopmentLatestStampCoordinate());
         if (!hasParentSemantic.isPresent()) {
            // Apparently not a relevant has_parent association sememe
            return;
         }

         ConceptChronology referencedConcept = Get.conceptService().getConceptChronology(((SemanticVersion) hasParentSemantic.get()).getReferencedComponentNid());

         LOG.debug("Running VHATIsAHasParentSynchronizingChronologyChangeListener handleChange() on VHAT has_parent dynamic sememe {} for concept {}", sc.getPrimordialUuid(),
               referencedConcept.getPrimordialUuid());

         final EditCoordinate editCoordinate = new EditCoordinateImpl(hasParentSemantic.get().getAuthorNid(), hasParentSemantic.get().getModuleNid(),
               hasParentSemantic.get().getPathNid());

         // Handle changes to associations

         // Get active has_parent association dynamic sememes attached to component
         Collection<DynamicVersion<?>> hasParentAssociationDynamicSememes = getActiveHasParentAssociationDynamicSememesAttachedToComponent(referencedConcept.getNid());

         // Create set of parent concept sequences from active has_parent association dynamic sememes attached to component
         Set<Integer> parentSequencesFromHasParentAssociationDynamicSememes = new HashSet<>();
         for (DynamicVersion<?> hasParentAssociationDynamicSememe : hasParentAssociationDynamicSememes) {
            UUID parentUuid = ((DynamicUUIDImpl) hasParentAssociationDynamicSememe.getData()[0]).getDataUUID();
            parentSequencesFromHasParentAssociationDynamicSememes.add(Get.identifierService().getNidForUuids(parentUuid));
         }

         // Get logic graph sememe chronology in order to create new version
         final Optional<SemanticChronology> conceptLogicGraphSemanticChronology = Frills.getLogicGraphChronology(referencedConcept.getNid(), true);
         if (!conceptLogicGraphSemanticChronology.isPresent()) {
            String msg = "No logic graph sememe found for concept (NID=" + referencedConcept.getPrimordialUuid() + ")";
            LOG.error(msg);
            return;
         }

         final Runnable updateExistingLogicGraphRunnable = new Runnable() {
            public void run() {
               try {
                  // This new builtSememeVersion may have resulted in added or retired or changed has_parent association
                  // Need to rebuild logic graph
                  LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();
                  ArrayList<Assertion> assertions = new ArrayList<>();
                  for (int parentConceptSequence : parentSequencesFromHasParentAssociationDynamicSememes) {
                     assertions.add(ConceptAssertion(parentConceptSequence, defBuilder));
                  }

                  NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
                  LogicalExpression parentDef = defBuilder.build();

                  // This code for use when updating an existing logic graph sememe
                  LogicGraphVersionImpl newLogicGraphSememeVersion = ((SemanticChronology) (conceptLogicGraphSemanticChronology.get()))
                        .createMutableVersion((hasParentAssociationDynamicSememes.size() > 0 ? Status.ACTIVE : Status.INACTIVE), editCoordinate);
                  newLogicGraphSememeVersion.setGraphData(parentDef.getData(DataTarget.INTERNAL));
                  // This is a sememe generated by this listener, so add it to list so listener will ignore it
                  LOG.info("Putting logic graph " + newLogicGraphSememeVersion.getNid() + " " + newLogicGraphSememeVersion.getNid() + " into the ignore list prior to commit");
                  nidsOfGeneratedSememesToIgnore.add(conceptLogicGraphSemanticChronology.get().getNid());
                  LOG.debug("Created the logic graph " + newLogicGraphSememeVersion + " due to an association change ");
                  try {
                     Get.commitService().addUncommittedNoChecks(conceptLogicGraphSemanticChronology.get()).get();
                  } catch (Exception e) {
                     // New version of this sememe failed to be added to commit list, so remove sememe from list so listener won't ignore it
                     nidsOfGeneratedSememesToIgnore.remove(conceptLogicGraphSemanticChronology.get().getNid());
                     LOG.error("FAILED calling addUncommitted() on logic graph of VHAT concept " + referencedConcept, e);
                     return;
                  }
                  Get.commitService()
                        .commit(newLogicGraphSememeVersion.getChronology(), editCoordinate,
                              "Committing new version of logic graph sememe " + conceptLogicGraphSemanticChronology.get().getPrimordialUuid() + " with "
                                    + parentSequencesFromHasParentAssociationDynamicSememes.size() + " parent(s) for concept " + referencedConcept.getPrimordialUuid())
                        .get();
               } catch (Exception e) {
                  LOG.error("FAILED committing new version of logic graph sememe " + conceptLogicGraphSemanticChronology.get().getPrimordialUuid() + " with "
                        + parentSequencesFromHasParentAssociationDynamicSememes.size() + " parent(s) for concept " + referencedConcept.getPrimordialUuid(), e);
               }
            }
         };

         // Use either updateExistingLogicGraphRunnable or retireAndCreateLogicGraphRunnable,
         // depending on which (if either) works better with logic graph merge
         final Runnable runnableToUse = updateExistingLogicGraphRunnable;
         inProgressJobs.add(Get.workExecutors().getExecutor().submit(runnableToUse));
      }
   }

   /**
    * Call to ensure that all background processing completed before continuing
    */
   public void waitForJobsToComplete() {
      LOG.info("Waiting for " + inProgressJobs.size());
      Future<?> f = null;
      f = inProgressJobs.peek();
      while (f != null) {
         try {
            // wait for execution of the job to complete
            f.get();
         } catch (Exception e) {
            LOG.error("There was an error in a submitted job!", e);
         }
         inProgressJobs.remove(f);
         f = inProgressJobs.peek();
      }
      LOG.info("Wait complete");
   }

   private static Stream<SemanticChronology> getSememesForComponentFromAssemblagesFilteredBySememeType(int nid) {
      final Set<Integer> selectedAssemblages = new HashSet<>();
      selectedAssemblages.add(VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid());

      final Set<VersionType> sememeTypesToExclude = new HashSet<>();
      for (VersionType type : VersionType.values()) {
         if (type != VersionType.DYNAMIC) {
            sememeTypesToExclude.add(type);
         }
      }

      return Frills.getSemanticForComponentFromAssemblagesFilteredBySememeType(nid, selectedAssemblages, sememeTypesToExclude);
   }

   public static Collection<DynamicVersion<?>> getActiveHasParentAssociationDynamicSememesAttachedToComponent(int nid) {
      final Iterator<SemanticChronology> it = getSememesForComponentFromAssemblagesFilteredBySememeType(nid).iterator();
      final List<DynamicVersion<?>> hasParentAssociationDynamicSememesToReturn = new ArrayList<>();
      while (it.hasNext()) {
         SemanticChronology hasParentAssociationDynamicSememe = (SemanticChronology) it.next();
         // Ensure only working with ACTIVE hasParentAssociationDynamicSememe version
         if (hasParentAssociationDynamicSememe.isLatestVersionActive(getVHATDevelopmentLatestStampCoordinate().makeCoordinateAnalog(Status.ACTIVE_ONLY_SET))) {
            LatestVersion<Version> optionalLatestVersion = hasParentAssociationDynamicSememe.getLatestVersion(getVHATDevelopmentLatestStampCoordinate());
            if (optionalLatestVersion.isPresent()) {
               if (!optionalLatestVersion.contradictions().isEmpty()) {
                  // TODO handle contradictions
               }

               // This check should be redundant
               if (optionalLatestVersion.get().getStatus() == Status.ACTIVE) {
                  hasParentAssociationDynamicSememesToReturn.add((DynamicVersion<?>) optionalLatestVersion.get());
               }
            }
         }
      }

      return Collections.unmodifiableList(hasParentAssociationDynamicSememesToReturn);
   }

   // Get any (first) inactive VHAT has_parent association sememe with specified target parent concept
   public static Optional<Version> getInactiveHasParentAssociationDynamicSememeAttachedToComponent(int nid, int targetParentConceptId) {
      ConceptChronology targetParentConcept = Get.conceptService().getConceptChronology(targetParentConceptId);
      final Iterator<SemanticChronology> it = getSememesForComponentFromAssemblagesFilteredBySememeType(nid).iterator();
      while (it.hasNext()) {
         SemanticChronology hasParentAssociationDynamicSememe = it.next();
         LatestVersion<Version> optionalLatestVersion = hasParentAssociationDynamicSememe.getLatestVersion(getVHATDevelopmentLatestStampCoordinate());
         if (optionalLatestVersion.isPresent()) {
            if (!optionalLatestVersion.contradictions().isEmpty()) {
               // TODO handle contradictions
            }

            if (optionalLatestVersion.get().getStatus() == Status.INACTIVE) {
               UUID inactiveHasParentSememeTargetParentUuid = ((DynamicUUIDImpl) ((DynamicVersion<?>) optionalLatestVersion.get()).getData()[0]).getDataUUID();

               if (targetParentConcept.getPrimordialUuid().equals(inactiveHasParentSememeTargetParentUuid)) {
                  return Optional.of(optionalLatestVersion.get());
               }
            }
         }
      }

      return Optional.empty();
   }
}
