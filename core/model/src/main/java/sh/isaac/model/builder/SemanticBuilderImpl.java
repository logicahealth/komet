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



package sh.isaac.model.builder;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.LookupService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.SemanticVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuildListenerI;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SemanticBuilderImpl.
 *
 * @author kec
 * @param <C> the generic type
 */
public class SemanticBuilderImpl<C extends SemanticChronology>
        extends ComponentBuilder<C>
         implements SemanticBuilder<C> {
   /** The referenced component nid. */
   int referencedComponentNid = Integer.MAX_VALUE;

   /** The referenced component builder. */
   IdentifiedComponentBuilder referencedComponentBuilder;

   /** The assemblage concept sequence. */
   int assemblageConceptNid;

   /** The sememe type. */
   VersionType semanticType;

   /** The parameters. */
   Object[] parameters;

   private static final Logger LOG = LogManager.getLogger();

   /**
    * Instantiates a new sememe builder impl.
    *
    * @param referencedComponentBuilder the referenced component builder
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param semanticType the sememe type
    * @param paramaters the paramaters
    */
   public SemanticBuilderImpl(IdentifiedComponentBuilder referencedComponentBuilder,
                            int assemblageConceptSequence,
                            VersionType semanticType,
                            Object... paramaters) {
      this.referencedComponentBuilder = referencedComponentBuilder;
      this.assemblageConceptNid  = assemblageConceptSequence;
      this.semanticType                 = semanticType;
      this.parameters                 = paramaters;
   }

   /**
    * Instantiates a new sememe builder impl.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param semanticType the sememe type
    * @param paramaters the paramaters
    */
   public SemanticBuilderImpl(int referencedComponentNid,
                            int assemblageConceptSequence,
                            VersionType semanticType,
                            Object... paramaters) {
      this.referencedComponentNid    = referencedComponentNid;
      this.assemblageConceptNid = assemblageConceptSequence;
      this.semanticType                = semanticType;
      this.parameters                = paramaters;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Builds the.
    *
    * @param stampSequence the stamp sequence
    * @param builtObjects the built objects
    * @return the c
    * @throws IllegalStateException the illegal state exception
    */
   @Override
   public C build(int stampSequence,
                  List<Chronology> builtObjects)
            throws IllegalStateException {
      if (this.referencedComponentNid == Integer.MAX_VALUE) {
         this.referencedComponentNid = Get.identifierService()
                                          .getNidForUuids(this.referencedComponentBuilder.getUuids());
      }
      
      List<SemanticBuildListenerI> sememeBuildListeners = LookupService.get().getAllServices(SemanticBuildListenerI.class);
      for (SemanticBuildListenerI listener : sememeBuildListeners) {
         if (listener != null) {
            if (listener.isEnabled()) {
               // LOG.info("Calling " + listener.getListenerName() + ".applyBefore(...)");
               try {
                  listener.applyBefore(stampSequence, builtObjects);
               } catch (RuntimeException e) {
                  LOG.error("FAILED running " + listener.getListenerName() + ".applyBefore(...): ", e);
               }
            } else {
               LOG.info("NOT calling " + listener.getListenerName() + ".applyBefore(...) because listener has been disabled");
            }
         }
      }
      SemanticVersion version = null;
      SemanticChronologyImpl semanticChronicle;

      final int semanticNid = Get.identifierService().getNidForUuids(this.getUuids());
      if (Get.assemblageService().hasSemantic(semanticNid)) {
         semanticChronicle = (SemanticChronologyImpl) Get.assemblageService()
               .getSemanticChronology(semanticNid);

         if ((semanticChronicle.getVersionType() != this.semanticType) ||
               !semanticChronicle.isIdentifiedBy(getPrimordialUuid()) ||
               (semanticChronicle.getAssemblageNid() != this.assemblageConceptNid) ||
               (semanticChronicle.getReferencedComponentNid() != this.referencedComponentNid)) {
            throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing semantic!");
         }
      } else {
         semanticChronicle = new SemanticChronologyImpl(this.semanticType,
               getPrimordialUuid(),
               this.assemblageConceptNid,
               this.referencedComponentNid);
      }

      semanticChronicle.setAdditionalUuids(this.additionalUuids);

      switch (this.semanticType) {
      case COMPONENT_NID:
         final ComponentNidVersionImpl cnsi =
            (ComponentNidVersionImpl) semanticChronicle.createMutableVersion(stampSequence);

         cnsi.setComponentNid((Integer) this.parameters[0]);
         break;

      case LONG:
         final LongVersionImpl lsi = (LongVersionImpl) semanticChronicle.createMutableVersion(stampSequence);
         version = lsi;
         lsi.setLongValue((Long) this.parameters[0]);
         break;

      case LOGIC_GRAPH:
         final LogicGraphVersionImpl lgsi =
            (LogicGraphVersionImpl) semanticChronicle.createMutableVersion(stampSequence);
         version = lgsi;
         lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
         break;

      case MEMBER:
         SemanticVersionImpl svi = semanticChronicle.createMutableVersion(stampSequence);
         version = svi;
         break;

      case STRING:
         final StringVersionImpl ssi = (StringVersionImpl) semanticChronicle.createMutableVersion(stampSequence);
         version = ssi;
         ssi.setString((String) this.parameters[0]);
         break;

      case DESCRIPTION: {
         final DescriptionVersionImpl dsi =
            (DescriptionVersionImpl) semanticChronicle.createMutableVersion(stampSequence);
         version = dsi;
         dsi.setCaseSignificanceConceptNid((Integer) this.parameters[0]);
         dsi.setDescriptionTypeConceptNid((Integer) this.parameters[1]);
         dsi.setLanguageConceptNid((Integer) this.parameters[2]);
         dsi.setText((String) this.parameters[3]);
         break;
      }

      case DYNAMIC: {
         final DynamicImpl dsi = (DynamicImpl) semanticChronicle.createMutableVersion(stampSequence);

         version = dsi;
         if ((this.parameters != null) && (this.parameters.length > 0)) {
            // See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
            dsi.setData(((AtomicReference<DynamicData[]>) this.parameters[0]).get());
         }

         // TODO [Dan 2] this needs to fire the validator!
         break;
      }

      default:
         throw new UnsupportedOperationException("p Can't handle: " + this.semanticType);
      }

      getSemanticBuilders().forEach((builder) -> builder.build(stampSequence, builtObjects));
      builtObjects.add(semanticChronicle);
      for (SemanticBuildListenerI listener : sememeBuildListeners) {
         if (listener != null) {
            if (listener.isEnabled()) {
               // LOG.info("Calling " + listener.getListenerName() + ".applyAfter(...)");
               try {
                  listener.applyAfter(stampSequence, version, builtObjects);
               } catch (RuntimeException e) {
                  LOG.error("FAILED running " + listener.getListenerName() + ".applyAfter(...): ", e);
               }
            } else {
               LOG.info("NOT calling " + listener.getListenerName() + ".applyAfter(...) because listener has been disabled");
            }
         }
      }
      return (C) semanticChronicle;
   }

   /**
    * Builds the.
    *
    * @param editCoordinate the edit coordinate
    * @param changeCheckerMode the change checker mode
    * @param builtObjects the built objects
    * @return the optional wait task
    * @throws IllegalStateException the illegal state exception
    */
   @Override
   public OptionalWaitTask<C> build(EditCoordinate editCoordinate,
                                    ChangeCheckerMode changeCheckerMode,
                                    List<Chronology> builtObjects)
            throws IllegalStateException {
      
        List<SemanticBuildListenerI> sememeBuildListeners = LookupService.get().getAllServices(SemanticBuildListenerI.class);
        for (SemanticBuildListenerI listener : sememeBuildListeners) {
           if (listener != null) {
            if (listener.isEnabled()) {
               // LOG.info("Calling " + listener.getListenerName() + ".applyBefore(...)");
               try {
                  listener.applyBefore(editCoordinate, changeCheckerMode, builtObjects);
               } catch (RuntimeException e) {
                  LOG.error("FAILED running " + listener.getListenerName() + ".applyBefore(...): ", e);
               }
            } else {
                    LOG.info("NOT calling " + listener.getListenerName() + ".applyBefore(...) because listener has been disabled");
                }
            }
        }

      if (this.referencedComponentNid == Integer.MAX_VALUE) {
         this.referencedComponentNid = Get.identifierService()
                                          .getNidForUuids(this.referencedComponentBuilder.getUuids());
      }

      SemanticVersion version;
      SemanticChronologyImpl sememeChronicle;

      final int semanticNid = Get.identifierService().getNidForUuids(this.getUuids());
      if (Get.assemblageService().hasSemantic(semanticNid)) {
         sememeChronicle = (SemanticChronologyImpl) Get.assemblageService().getSemanticChronology(semanticNid);

         if ((sememeChronicle.getVersionType() != this.semanticType) ||
               !sememeChronicle.isIdentifiedBy(getPrimordialUuid()) ||
               (sememeChronicle.getAssemblageNid() != this.assemblageConceptNid) ||
               (sememeChronicle.getReferencedComponentNid() != this.referencedComponentNid)) {
            throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing sememe!");
         }
      } else {
         sememeChronicle = new SemanticChronologyImpl(this.semanticType,
               getPrimordialUuid(),
               this.assemblageConceptNid,
               this.referencedComponentNid);
      }

      sememeChronicle.setAdditionalUuids(this.additionalUuids);

      switch (this.semanticType) {
      case COMPONENT_NID:
         final ComponentNidVersionImpl cnsi =
            (ComponentNidVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                          editCoordinate);
         version = cnsi;
         cnsi.setComponentNid((Integer) this.parameters[0]);
         break;

      case LONG:
         final LongVersionImpl lsi = (LongVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                                          editCoordinate);
         version = lsi;
         lsi.setLongValue((Long) this.parameters[0]);
         break;

      case LOGIC_GRAPH:
         final LogicGraphVersionImpl lgsi =
            (LogicGraphVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                        editCoordinate);
         version = lgsi;
         lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
         break;

      case MEMBER:
         final SemanticVersionImpl svi = sememeChronicle.createMutableVersion(this.state, editCoordinate);
         version = svi;
         break;

      case STRING:
         final StringVersionImpl ssi = (StringVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                                              editCoordinate);
         version = ssi;
         ssi.setString((String) this.parameters[0]);
         break;

      case DESCRIPTION: {
         final DescriptionVersionImpl dsi =
            (DescriptionVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                         editCoordinate);
         version = dsi;
         dsi.setCaseSignificanceConceptNid((Integer) this.parameters[0]);
         dsi.setDescriptionTypeConceptNid((Integer) this.parameters[1]);
         dsi.setLanguageConceptNid((Integer) this.parameters[2]);
         dsi.setText((String) this.parameters[3]);
         break;
      }

      case DYNAMIC: {
         final DynamicImpl dsi = (DynamicImpl) sememeChronicle.createMutableVersion(this.state,
                                                                                                editCoordinate);

         if ((this.parameters != null) && (this.parameters.length > 0)) {
            // See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
            dsi.setData(((AtomicReference<DynamicData[]>) this.parameters[0]).get());
         }
         version = dsi;
         // TODO [DAN 2] this needs to fire the validator!
         break;
      }

      default:
         throw new UnsupportedOperationException("q Can't handle: " + this.semanticType);
      }

      Task<Void> primaryNested;

      if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
         primaryNested = Get.commitService()
                            .addUncommitted(sememeChronicle);
      } else {
         primaryNested = Get.commitService()
                            .addUncommittedNoChecks(sememeChronicle);
      }

      final ArrayList<OptionalWaitTask<?>> nested = new ArrayList<>();

      getSemanticBuilders().forEach((builder) -> nested.add(builder.build(editCoordinate,
            changeCheckerMode,
            builtObjects)));
      builtObjects.add(sememeChronicle);
      for (SemanticBuildListenerI listener : sememeBuildListeners) {
         if (listener != null) {
            if (listener.isEnabled()) {
               // LOG.info("Calling " + listener.getListenerName() + ".applyAfter(...)");
               listener.applyAfter(editCoordinate, changeCheckerMode, version, builtObjects);
            } else {
               LOG.info("NOT calling " + listener.getListenerName() + ".applyAfter(...) because listener has been disabled");
            }
         }
      }
      return new OptionalWaitTask<>(primaryNested, (C) sememeChronicle, nested);
   }
   
   @Override
   public IdentifiedComponentBuilder<C> setT5Uuid(UUID namespace, BiConsumer<String, UUID> consumer) {
      if (isPrimordialUuidSet() && getPrimordialUuid().version() == 4) {
         throw new RuntimeException("Attempting to set Type 5 UUID where the UUID was previously set to random");
      }

      if (!isPrimordialUuidSet()) {
         UUID assemblageUuid = Get.identifierService().getUuidPrimordialForNid(this.assemblageConceptNid).get();
   
           UUID refCompUuid = null;
           if (referencedComponentBuilder != null) {
               refCompUuid = referencedComponentBuilder.getPrimordialUuid();
           } else {
               refCompUuid = Get.identifierService().getUuidPrimordialForNid(referencedComponentNid).get();
           }
   
           if (semanticType == semanticType.LOGIC_GRAPH) {
              setPrimordialUuid(UuidFactory.getUuidForLogicGraphSememe(namespace, assemblageUuid, refCompUuid, (LogicalExpression) parameters[0], consumer));
           } else if (semanticType == semanticType.MEMBER) {
              setPrimordialUuid(UuidFactory.getUuidForMemberSememe(namespace, assemblageUuid, refCompUuid, consumer));
           } else if (semanticType == semanticType.DYNAMIC) {
              setPrimordialUuid(UuidFactory.getUuidForDynamic(namespace, assemblageUuid, refCompUuid, 
                 (parameters != null && parameters.length > 0 ? ((AtomicReference<DynamicData[]>)parameters[0]).get() : null), consumer));
           } else if (semanticType == semanticType.COMPONENT_NID) {
               UUID componentUuid = Get.identifierService().getUuidPrimordialForNid((Integer)parameters[0]).get();
               setPrimordialUuid(UuidFactory.getUuidForComponentNidSememe(namespace, assemblageUuid, refCompUuid, componentUuid, consumer));
           } else if (semanticType == semanticType.DESCRIPTION) {
              setPrimordialUuid(UuidFactory.getUuidForDescriptionSememe(namespace, refCompUuid, 
                                        Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]).get(),
                                        Get.identifierService().getUuidPrimordialForNid((Integer) parameters[1]).get(),
                                        Get.identifierService().getUuidPrimordialForNid((Integer) parameters[2]).get(),
                                        (String) parameters[3],
                                        consumer));
           } else if (semanticType == semanticType.STRING) {
              setPrimordialUuid(UuidFactory.getUuidForStringSememe(namespace, assemblageUuid, refCompUuid, (String) parameters[0], consumer));
           }
       }
      return this;
   }
}

