/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.misc.constants.VHATConstants;
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
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class VHATIsAHasParentSynchronizingChronologyChangeListener implements ChronologyChangeListener
{
	private static final Logger LOG = LogManager.getLogger(VHATIsAHasParentSynchronizingChronologyChangeListener.class);

	// Cached VHAT module nids
	private static NidSet VHAT_MODULES = null;

	private static StampFilter VHAT_STAMP_COORDINATE = null;

	private boolean enabled = true;

	/**
	 * Set of nids of component versions created by VHATIsAHasParentSynchronizingChronologyChangeListener which should NOT be processed by
	 * VHATIsAHasParentSynchronizingChronologyChangeListener, in order to avoid infinite recursion
	 */
	private final ConcurrentLinkedQueue<Integer> nidsOfGeneratedSemanticsToIgnore = new ConcurrentLinkedQueue<>();
	private final UUID providerUuid = UUID.randomUUID();

	private final ConcurrentLinkedQueue<Integer> semanticNidsForUnhandledLogicGraphChanges = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<Integer> semanticNidsForUnhandledHasParentAssociationChanges = new ConcurrentLinkedQueue<>();

	private ConcurrentLinkedQueue<Future<?>> inProgressJobs = new ConcurrentLinkedQueue<>();
	private ScheduledFuture<?> sf;

	private VHATIsAHasParentSynchronizingChronologyChangeListener()
	{
		//For HK2 to construct
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.isaac.ochre.api.VHATIsAHasParentSynchronizingChronologyChangeListenerI#disable()
	 */
	@Override
	public void disable()
	{
		LOG.info("Disabling");
		enabled = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.isaac.ochre.api.VHATIsAHasParentSynchronizingChronologyChangeListenerI#enable()
	 */
	@Override
	public void enable()
	{
		LOG.info("Enabling");
		enabled = true;
	}

	@PostConstruct
	private void startMe()
	{
		if (Get.configurationService().isInDBBuildMode())
		{
			LOG.info("Disabling due to DBBuildMode - will not receive events even if enabled.");
			disable();
		}
		else
		{
			Get.commitService().addChangeListener(this);
			// Prevent a memory leak, by scheduling a thread to periodically empty the job list
			sf = Get.workExecutors().getScheduledThreadPoolExecutor().scheduleAtFixedRate((() -> waitForJobsToComplete()), 5, 5, TimeUnit.MINUTES);
		}

		if (!Get.conceptService().hasConcept(VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid()))
		{
			//If this concept doesn't yet exist, there is no VHAT, and no reason to use this.  Disable it.
			disable();
		}

		Get.configurationService().getDBBuildMode().addListener((change) -> {
			if (Get.configurationService().isInDBBuildMode())
			{
				disable();
			}
		});
	}

	@PreDestroy
	private void stopMe()
	{
		Get.commitService().removeChangeListener(this);
		if (sf != null)
		{
			sf.cancel(true);
		}
		sf = null;
		VHAT_MODULES = null;
		VHAT_STAMP_COORDINATE = null;
		enabled = true;
		nidsOfGeneratedSemanticsToIgnore.clear();
		semanticNidsForUnhandledLogicGraphChanges.clear();
		semanticNidsForUnhandledHasParentAssociationChanges.clear();
		inProgressJobs.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#getListenerUuid()
	 */
	@Override
	public UUID getListenerUuid()
	{
		return providerUuid;
	}

	private static ImmutableIntSet getVHATModules(StampFilter stampFilter)
	{
		// Initialize VHAT module nids cache
		if (VHAT_MODULES == null || VHAT_MODULES.size() == 0)
		{ // Should be unnecessary
			VHAT_MODULES = NidSet.of(Frills.getAllChildrenOfConcept(MetaData.VHAT_MODULES____SOLOR.getNid(), true, true, stampFilter).toArray(new Integer[0]));
		}
		return IntSets.immutable.of(VHAT_MODULES.asArray());
	}

	private static StampFilter getVHATDevelopmentLatestStampFilter()
	{
		if (VHAT_STAMP_COORDINATE == null)
		{
			StampPosition stampPosition = StampPositionImmutable.make(Long.MAX_VALUE, TermAux.DEVELOPMENT_PATH.getNid());

			VHAT_STAMP_COORDINATE = StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE, stampPosition,
					getVHATModules(Coordinates.Filter.DevelopmentLatest()), IntLists.immutable.empty());
		}

		return VHAT_STAMP_COORDINATE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#handleChange(gov.vha.isaac.ochre.api.component.concept.ConceptChronology)
	 */
	@Override
	public void handleChange(ConceptChronology cc)
	{
		// Only using handleCommit()
		if (!enabled)
		{
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#handleChange(gov.vha.isaac.ochre.api.component.semantic.SemanticChronology)
	 */
	@Override
	public void handleChange(SemanticChronology sc)
	{
		if (!enabled)
		{
			LOG.trace("Ignoring, while listener disabled, change to semantic " + sc.getVersionType() + " " + sc.getNid() + " " + sc.getNid());
			return;
		}

		// Determine if this is a semantic generated by this listener
		if (nidsOfGeneratedSemanticsToIgnore.contains(sc.getNid()))
		{
			// This is a semantic generated by this listener, so remove it and ignore it
			nidsOfGeneratedSemanticsToIgnore.remove(sc.getNid());
			LOG.trace("Ignoring recursive change to semantic " + sc.getVersionType() + " " + sc.getNid() + " " + sc.getNid());
			return;
		}

		if (sc.getVersionType() == VersionType.LOGIC_GRAPH && sc.getAssemblageNid() == MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid())
		{
			semanticNidsForUnhandledLogicGraphChanges.add(sc.getNid());
			LOG.trace("Adding LogicGraph " + sc.getNid() + " " + sc.getNid() + " to the list of commits to process");
		}
		else if (sc.getVersionType() == VersionType.DYNAMIC && sc.getAssemblageNid() == VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid())
		{
			semanticNidsForUnhandledHasParentAssociationChanges.add(sc.getNid());
			LOG.trace("Adding Association semantic " + sc.getNid() + " " + sc.getNid() + " to the list of commits to process");
		}
		else
		{
			// Ignore if not either LOGIC_GRAPH or DYNAMIC has_parent association semantic
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.isaac.ochre.api.commit.ChronologyChangeListener#handleCommit(gov.vha.isaac.ochre.api.commit.CommitRecord)
	 */
	@Override
	public void handleCommit(CommitRecord commitRecord)
	{
		if (!enabled)
		{
			LOG.trace("Ignoring, while listener disabled, change to commit {} ", commitRecord);

			return;
		}
		// For new and updated VHAT logic graphs, create or retire has_parent associations, as appropriate
		LOG.trace("HandleCommit looking for - " + semanticNidsForUnhandledLogicGraphChanges.size() + " logic graphs: "
				+ semanticNidsForUnhandledLogicGraphChanges + " and " + semanticNidsForUnhandledHasParentAssociationChanges.size() + " associations: "
				+ semanticNidsForUnhandledHasParentAssociationChanges + " the commit contains " + commitRecord.getSemanticNidsInCommit());

		//Cleanup previously submitted jobs, to prevent a leak
		Iterator<Future<?>> it = inProgressJobs.iterator();
		while (it.hasNext())
		{
			if (it.next().isDone())
			{
				it.remove();
			}
		}

		for (int logicGraphNid : semanticNidsForUnhandledLogicGraphChanges.toArray(new Integer[semanticNidsForUnhandledLogicGraphChanges.size()]))
		{
			if (!commitRecord.getSemanticNidsInCommit().contains(logicGraphNid))
			{
				LOG.trace("HandleCommit NOT handling logic graph at this time " + logicGraphNid + " which is not contained in the commit record list: "
						+ commitRecord.getSemanticNidsInCommit().toString());
				continue;
			}
			semanticNidsForUnhandledLogicGraphChanges.remove(logicGraphNid);
			LOG.trace(
					"HandleCommit handling logic graph " + logicGraphNid + ". " + semanticNidsForUnhandledLogicGraphChanges.size() + " logic graphs remaining");

			SemanticChronology sc = Get.assemblageService().getSemanticChronology(logicGraphNid);
			LatestVersion<Version> logicGraph = sc.getLatestVersion(getVHATDevelopmentLatestStampFilter());
			if (!logicGraph.isPresent())
			{
				// Apparently not a relevant LOGIC_GRAPH semantic
				return;
			}

			LOG.trace("Running VHATIsAHasParentSynchronizingChronologyChangeListener handleChange() on VHAT LOGIC_GRAPH dynamic semantic "
					+ sc.getPrimordialUuid() + " for concept " + Get.identifierService().getUuidPrimordialForNid(sc.getReferencedComponentNid()));

			ConceptChronology referencedConcept = Get.conceptService().getConceptChronology(((LogicGraphVersion) logicGraph.get()).getReferencedComponentNid());

			// Handle changes to LOGIC_GRAPH
			// In practice, this should only be for a new concept
			Set<Integer> parentsAccordingToNewLogicGraphVersion = new HashSet<>();
			if (logicGraph.get().getStatus() == Status.INACTIVE)
			{
				// Retire all has_parent association semantics
			}
			else
			{
				parentsAccordingToNewLogicGraphVersion
						.addAll(Frills.getParentConceptNidsFromLogicGraph(((LogicGraphVersion) logicGraph.get()).getLogicalExpression()));
				if (parentsAccordingToNewLogicGraphVersion.isEmpty())
				{
					String msg = "Encountered logic graph for concept NID=" + referencedConcept.getNid() + ", UUID=" + referencedConcept.getPrimordialUuid()
							+ " with no specified parents (concept nodes in necessary set nodes)";
					LOG.warn(msg);
				}
			}

			final Collection<DynamicVersion> hasParentAssociationDynamicSemantics = getActiveHasParentAssociationDynamicSemanticsAttachedToComponent(
					((LogicGraphVersion) logicGraph.get()).getReferencedComponentNid());
			Set<Integer> parentsAccordingToHasParentAssociationDynamicSemantics = new HashSet<>();
			AtomicReference<Future<?>> f = new AtomicReference<Future<?>>(null);
			// For each active has_parent association
			if (hasParentAssociationDynamicSemantics.size() > 0)
			{
				Runnable runnable = new Runnable()
				{
					public void run()
					{
						List<Chronology> itemsToCommit = new ArrayList<>();
						final WriteCoordinate writeCoordinate = new WriteCoordinateImpl(logicGraph.get().getAuthorNid(), logicGraph.get().getModuleNid(),
								logicGraph.get().getPathNid(), Get.commitService().newTransaction("has parent sync 1"));
						for (DynamicVersion hasParentSemantic : hasParentAssociationDynamicSemantics)
						{
							final DynamicUUID target = (DynamicUUID) hasParentSemantic.getData(0);
							final int targetSeq = Get.identifierService().getNidForUuids(target.getDataUUID());
							// Accumulate a list of parents from has_parent semantics
							parentsAccordingToHasParentAssociationDynamicSemantics.add(targetSeq);
							// If the active has_parent is not represented in an active logic graph, retire it
							if (!parentsAccordingToNewLogicGraphVersion.contains(targetSeq))
							{
								MutableDynamicVersion mutableVersion = hasParentSemantic.getChronology()
										.createMutableVersion(new WriteCoordinateImpl(writeCoordinate, Status.INACTIVE));
								mutableVersion.setData(hasParentSemantic.getData());
								LOG.trace("Putting association semantic " + mutableVersion.getNid() + " " + mutableVersion.getNid()
										+ " into the ignore list prior to commit");

								// This is a semantic generated by this listener, so add it to list so listener will ignore it
								nidsOfGeneratedSemanticsToIgnore.add(hasParentSemantic.getNid());
								try
								{
									Get.commitService().addUncommitted(writeCoordinate.getTransaction().get(), mutableVersion.getChronology()).get();
									itemsToCommit.add(mutableVersion.getChronology());
								}
								catch (Exception e)
								{
									// New version of this semantic failed to be added to commit list, so remove semantic from list so listener won't ignore it
									nidsOfGeneratedSemanticsToIgnore.remove(hasParentSemantic.getNid());
									LOG.error("FAILED calling addUncommitted() to retire VHAT has_parent association semantic " + hasParentSemantic, e);
								}
							}
						}

						if (itemsToCommit.size() > 0)
						{
							try
							{
								LOG.trace("Doing commit");
								Frills.commitCheck(writeCoordinate.getTransaction().get().commit("Retiring VHAT has_parent semantics"));
							}
							catch (RuntimeException e)
							{
								LOG.error("FAILED commit while retiring {} VHAT has_parent semantics", itemsToCommit.size());
							}
						}
						else
						{
							writeCoordinate.getTransaction().get().cancel().getNoThrow();
						}
					}
				};
				f.set(Get.workExecutors().getExecutor().submit(runnable));
				inProgressJobs.add(f.get());
			}

			// For each parent from an active logic graph

			// If the parent from the active logic graph is not already represented by an active has_parent semantic
			// Create new or unretire existing has_parent semantic
			Runnable runnable = new Runnable()
			{
				public void run()
				{
					// Our logic depends on the retirements above, being done...
					if (f.get() != null)
					{
						try
						{
							Future<?> future = f.get();
							future.get();  //Wait for it to finish
							inProgressJobs.remove(future);
						}
						catch (Exception e)
						{
							LOG.error("error in required prior thread", e);
							return;
						}
					}

					List<Chronology> builtObjects = new ArrayList<>();

					final WriteCoordinate writeCoordinate = new WriteCoordinateImpl(logicGraph.get().getAuthorNid(), logicGraph.get().getModuleNid(),
							logicGraph.get().getPathNid(), Get.commitService().newTransaction("has parent sync 2"));

					for (int parentAccordingToNewLogicGraphVersion : parentsAccordingToNewLogicGraphVersion)
					{
						if (!parentsAccordingToHasParentAssociationDynamicSemantics.contains(parentAccordingToNewLogicGraphVersion))
						{

							UUID uuidOfParentAccordingToNewLogicGraphVersion = Get.identifierService()
									.getUuidPrimordialForNid(parentAccordingToNewLogicGraphVersion);

							// Check for existence of retired version of has_parent association with this parent target
							Optional<Version> retiredHasParentSemanticVersion = getInactiveHasParentAssociationDynamicSemanticAttachedToComponent(
									referencedConcept.getNid(), parentAccordingToNewLogicGraphVersion);
							if (retiredHasParentSemanticVersion.isPresent())
							{
								// If retired version of has_parent association with this parent target exists, then re-activate it
								MutableDynamicVersion unretiredHasParentSemanticVersion = ((SemanticChronology) (retiredHasParentSemanticVersion.get()
										.getChronology())).createMutableVersion(new WriteCoordinateImpl(writeCoordinate, Status.ACTIVE));
								unretiredHasParentSemanticVersion.setData(((DynamicVersion) retiredHasParentSemanticVersion.get()).getData());
								// This is a semantic generated by this listener, so add it to list so listener will ignore it
								nidsOfGeneratedSemanticsToIgnore.add(retiredHasParentSemanticVersion.get().getNid());

								try
								{
									Get.commitService().addUncommitted(writeCoordinate.getTransaction().get(),
											((SemanticChronology) retiredHasParentSemanticVersion.get().getChronology())).get();
								}
								catch (Exception e)
								{
									// New version of this semantic failed to be added to commit list, so remove semantic from list so listener won't ignore it
									nidsOfGeneratedSemanticsToIgnore.remove(retiredHasParentSemanticVersion.get().getNid());
									LOG.error("FAILED calling addUncommitted() to unretire has_parent association semantic (target UUID="
											+ uuidOfParentAccordingToNewLogicGraphVersion + ") of VHAT concept " + referencedConcept, e);
									return;
								}
								try
								{
									LOG.trace("Doing commit");
									Frills.commitCheck(writeCoordinate.getTransaction().get()
											.commit("Unretiring VHAT has_parent association semantic (target UUID="
													+ uuidOfParentAccordingToNewLogicGraphVersion + ") for concept (UUID="
													+ referencedConcept.getPrimordialUuid() + ")"));
								}
								catch (RuntimeException e)
								{
									// New version of this semantic may have failed to be committed, so remove semantic from list so listener won't ignore it
									nidsOfGeneratedSemanticsToIgnore.remove(retiredHasParentSemanticVersion.get().getNid());
									LOG.error("FAILED calling commit() to unretire has_parent association semantic (target UUID="
											+ uuidOfParentAccordingToNewLogicGraphVersion + ") of VHAT concept " + referencedConcept, e);
									return;
								}
								LOG.trace("Unretired has_parent association semantic {} with target {} for concept {}",
										retiredHasParentSemanticVersion.get().getPrimordialUuid(), uuidOfParentAccordingToNewLogicGraphVersion,
										referencedConcept.getPrimordialUuid());
							}
							else
							{
								// If retired version on this has_parent association does not exist, then create a new one

								DynamicData[] data = new DynamicData[1];
								data[0] = new DynamicUUIDImpl(uuidOfParentAccordingToNewLogicGraphVersion);

								SemanticBuilder<? extends SemanticChronology> associationSemanticBuilder = Get.semanticBuilderService()
										.getDynamicBuilder(referencedConcept.getNid(), VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid(), data);

								// This is a semantic generated by this listener, so add it to list so listener will ignore it
								LOG.trace("Putting association semantic " + associationSemanticBuilder.getNid() + " into the ignore list prior to build");
								nidsOfGeneratedSemanticsToIgnore.add(associationSemanticBuilder.getNid());

								builtObjects.add(associationSemanticBuilder.buildAndWrite(writeCoordinate).getNoThrow());

								LOG.trace("Built new has_parent association semantic with SOURCE/CHILD={} and TARGET/PARENT={}",
										referencedConcept.getPrimordialUuid(), uuidOfParentAccordingToNewLogicGraphVersion);
							}
						}
					}

					if (builtObjects.size() > 0)
					{
						try
						{
							LOG.trace("Doing commit");
							Frills.commitCheck(writeCoordinate.getTransaction().get().commit("Committing new has_parent association semantics."));
						}
						catch (RuntimeException e)
						{
							LOG.error("FAILED committing new has_parent association semantics", e);
							return;
						}
					}
					else
					{
						try
						{
							writeCoordinate.getTransaction().get().cancel().get();
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					}
				}
			};
			inProgressJobs.add(Get.workExecutors().getExecutor().submit(runnable));
		}

		// For new, updated or retired VHAT has_parent association semantics, update existing logic graph
		for (int hasParentSemanticNid : semanticNidsForUnhandledHasParentAssociationChanges)
		{
			if (!commitRecord.getSemanticNidsInCommit().contains(hasParentSemanticNid))
			{
				LOG.trace("HandleCommit NOT handling hasParent association at this time" + hasParentSemanticNid
						+ " which is not contained in the commit record list: " + commitRecord.getSemanticNidsInCommit().toString());
				continue;
			}
			semanticNidsForUnhandledHasParentAssociationChanges.remove(hasParentSemanticNid);
			LOG.trace("HandleCommit handling hasParent association " + hasParentSemanticNid + ". " + semanticNidsForUnhandledHasParentAssociationChanges.size()
					+ " hasParent associations remaining");

			SemanticChronology sc = Get.assemblageService().getSemanticChronology(hasParentSemanticNid);
			LatestVersion<Version> hasParentSemantic = sc.getLatestVersion(getVHATDevelopmentLatestStampFilter());
			if (!hasParentSemantic.isPresent())
			{
				// Apparently not a relevant has_parent association semantic
				return;
			}

			ConceptChronology referencedConcept = Get.conceptService()
					.getConceptChronology(((SemanticVersion) hasParentSemantic.get()).getReferencedComponentNid());

			LOG.trace("Running VHATIsAHasParentSynchronizingChronologyChangeListener handleChange() on VHAT has_parent dynamic semantic {} for concept {}",
					sc.getPrimordialUuid(), referencedConcept.getPrimordialUuid());

			// Handle changes to associations

			// Get active has_parent association dynamic semantics attached to component
			Collection<DynamicVersion> hasParentAssociationDynamicSemantics = getActiveHasParentAssociationDynamicSemanticsAttachedToComponent(
					referencedConcept.getNid());

			// Create set of parent concept nids from active has_parent association dynamic semantics attached to component
			Set<Integer> parentSequencesFromHasParentAssociationDynamicSemantics = new HashSet<>();
			for (DynamicVersion hasParentAssociationDynamicSemantic : hasParentAssociationDynamicSemantics)
			{
				UUID parentUuid = ((DynamicUUIDImpl) hasParentAssociationDynamicSemantic.getData()[0]).getDataUUID();
				parentSequencesFromHasParentAssociationDynamicSemantics.add(Get.identifierService().getNidForUuids(parentUuid));
			}

			// Get logic graph semantic chronology in order to create new version
			final Optional<SemanticChronology> conceptLogicGraphSemanticChronology = Frills.getLogicGraphChronology(referencedConcept.getNid(), true);
			if (!conceptLogicGraphSemanticChronology.isPresent())
			{
				String msg = "No logic graph semantic found for concept (NID=" + referencedConcept.getPrimordialUuid() + ")";
				LOG.error(msg);
				return;
			}

			final Runnable updateExistingLogicGraphRunnable = new Runnable()
			{
				public void run()
				{
					try
					{
						final WriteCoordinate writeCoordinate = new WriteCoordinateImpl(hasParentSemantic.get().getAuthorNid(), hasParentSemantic.get().getModuleNid(),
								hasParentSemantic.get().getPathNid(), Get.commitService().newTransaction("vhat sync 3"));

						// This new builtSemanticVersion may have resulted in added or retired or changed has_parent association
						// Need to rebuild logic graph
						LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();
						ArrayList<Assertion> assertions = new ArrayList<>();
						for (int parentConceptSequence : parentSequencesFromHasParentAssociationDynamicSemantics)
						{
							assertions.add(ConceptAssertion(parentConceptSequence, defBuilder));
						}

						NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
						LogicalExpression parentDef = defBuilder.build();

						// This code for use when updating an existing logic graph semantic
						LogicGraphVersionImpl newLogicGraphSemanticVersion = ((SemanticChronology) (conceptLogicGraphSemanticChronology.get()))
								.createMutableVersion(new WriteCoordinateImpl(writeCoordinate, 
										(hasParentAssociationDynamicSemantics.size() > 0 ? Status.ACTIVE : Status.INACTIVE)));
						newLogicGraphSemanticVersion.setGraphData(parentDef.getData(DataTarget.INTERNAL));
						// This is a semantic generated by this listener, so add it to list so listener will ignore it
						LOG.trace("Putting logic graph " + newLogicGraphSemanticVersion.getNid() + " " + newLogicGraphSemanticVersion.getNid()
								+ " into the ignore list prior to commit");
						nidsOfGeneratedSemanticsToIgnore.add(conceptLogicGraphSemanticChronology.get().getNid());
						LOG.trace("Created the logic graph " + newLogicGraphSemanticVersion + " due to an association change ");
						try
						{
							Get.commitService().addUncommitted(writeCoordinate.getTransaction().get(), conceptLogicGraphSemanticChronology.get()).get();
						}
						catch (Exception e)
						{
							// New version of this semantic failed to be added to commit list, so remove semantic from list so listener won't ignore it
							nidsOfGeneratedSemanticsToIgnore.remove(conceptLogicGraphSemanticChronology.get().getNid());
							LOG.error("FAILED calling addUncommitted() on logic graph of VHAT concept " + referencedConcept, e);
							return;
						}
						LOG.trace("Doing commit");
						Frills.commitCheck(writeCoordinate.getTransaction().get()
								.commit("Committing new version of logic graph semantic " + conceptLogicGraphSemanticChronology.get().getPrimordialUuid()
										+ " with " + parentSequencesFromHasParentAssociationDynamicSemantics.size() + " parent(s) for concept "
										+ referencedConcept.getPrimordialUuid()));
					}
					catch (Exception e)
					{
						LOG.error("FAILED committing new version of logic graph semantic " + conceptLogicGraphSemanticChronology.get().getPrimordialUuid()
								+ " with " + parentSequencesFromHasParentAssociationDynamicSemantics.size() + " parent(s) for concept "
								+ referencedConcept.getPrimordialUuid(), e);
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
	public void waitForJobsToComplete()
	{
		Future<?> f = null;
		f = inProgressJobs.peek();
		while (f != null)
		{
			try
			{
				// wait for execution of the job to complete
				LOG.trace("Waiting for " + inProgressJobs.size());
				f.get();
			}
			catch (Exception e)
			{
				LOG.error("There was an error in a submitted job!", e);
			}
			inProgressJobs.remove(f);
			f = inProgressJobs.peek();
		}
		LOG.trace("Wait complete");
	}

	private static Stream<SemanticChronology> getSemanticsForComponentFromAssemblagesFilteredBySemanticType(int nid)
	{
		final Set<Integer> selectedAssemblages = new HashSet<>();
		selectedAssemblages.add(VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getNid());

		final Set<VersionType> semanticTypesToExclude = new HashSet<>();
		for (VersionType type : VersionType.values())
		{
			if (type != VersionType.DYNAMIC)
			{
				semanticTypesToExclude.add(type);
			}
		}

		return Frills.getSemanticForComponentFromAssemblagesFilteredBySemanticType(nid, selectedAssemblages, semanticTypesToExclude);
	}

	public static Collection<DynamicVersion> getActiveHasParentAssociationDynamicSemanticsAttachedToComponent(int nid)
	{
		final Iterator<SemanticChronology> it = getSemanticsForComponentFromAssemblagesFilteredBySemanticType(nid).iterator();
		final List<DynamicVersion> hasParentAssociationDynamicSemanticsToReturn = new ArrayList<>();
		while (it.hasNext())
		{
			SemanticChronology hasParentAssociationDynamicSemantic = (SemanticChronology) it.next();
			// Ensure only working with ACTIVE hasParentAssociationDynamicSemantic version
			if (hasParentAssociationDynamicSemantic.isLatestVersionActive(getVHATDevelopmentLatestStampFilter()))
			{
				LatestVersion<Version> optionalLatestVersion = hasParentAssociationDynamicSemantic.getLatestVersion(getVHATDevelopmentLatestStampFilter());
				if (optionalLatestVersion.isPresent())
				{
					if (!optionalLatestVersion.contradictions().isEmpty())
					{
						// TODO handle contradictions
					}

					// This check should be redundant
					if (optionalLatestVersion.get().getStatus() == Status.ACTIVE)
					{
						hasParentAssociationDynamicSemanticsToReturn.add((DynamicVersion) optionalLatestVersion.get());
					}
				}
			}
		}

		return Collections.unmodifiableList(hasParentAssociationDynamicSemanticsToReturn);
	}

	// Get any (first) inactive VHAT has_parent association semantic with specified target parent concept
	public static Optional<Version> getInactiveHasParentAssociationDynamicSemanticAttachedToComponent(int nid, int targetParentConceptId)
	{
		ConceptChronology targetParentConcept = Get.conceptService().getConceptChronology(targetParentConceptId);
		final Iterator<SemanticChronology> it = getSemanticsForComponentFromAssemblagesFilteredBySemanticType(nid).iterator();
		while (it.hasNext())
		{
			SemanticChronology hasParentAssociationDynamicSemantic = it.next();
			LatestVersion<Version> optionalLatestVersion = hasParentAssociationDynamicSemantic.getLatestVersion(getVHATDevelopmentLatestStampFilter());
			if (optionalLatestVersion.isPresent())
			{
				if (!optionalLatestVersion.contradictions().isEmpty())
				{
					// TODO handle contradictions
				}

				if (optionalLatestVersion.get().getStatus() == Status.INACTIVE)
				{
					UUID inactiveHasParentSemanticTargetParentUuid = ((DynamicUUIDImpl) ((DynamicVersion) optionalLatestVersion.get()).getData()[0])
							.getDataUUID();

					if (targetParentConcept.getPrimordialUuid().equals(inactiveHasParentSemanticTargetParentUuid))
					{
						return Optional.of(optionalLatestVersion.get());
					}
				}
			}
		}

		return Optional.empty();
	}
}
