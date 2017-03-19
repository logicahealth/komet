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
import java.util.concurrent.atomic.AtomicReference;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.model.sememe.version.ComponentNidSememeImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;
import sh.isaac.model.sememe.version.DynamicSememeImpl;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;
import sh.isaac.model.sememe.version.LongSememeImpl;
import sh.isaac.model.sememe.version.SememeVersionImpl;
import sh.isaac.model.sememe.version.StringSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <C>
 */
public class SememeBuilderImpl<C extends SememeChronology<? extends SememeVersion<?>>>
        extends ComponentBuilder<C>
         implements SememeBuilder<C> {
   int                        referencedComponentNid = Integer.MAX_VALUE;
   IdentifiedComponentBuilder referencedComponentBuilder;
   int                        assemblageConceptSequence;
   SememeType                 sememeType;
   Object[]                   parameters;

   //~--- constructors --------------------------------------------------------

   public SememeBuilderImpl(IdentifiedComponentBuilder referencedComponentBuilder,
                            int assemblageConceptSequence,
                            SememeType sememeType,
                            Object... paramaters) {
      this.referencedComponentBuilder = referencedComponentBuilder;
      this.assemblageConceptSequence  = assemblageConceptSequence;
      this.sememeType                 = sememeType;
      this.parameters                 = paramaters;
   }

   public SememeBuilderImpl(int referencedComponentNid,
                            int assemblageConceptSequence,
                            SememeType sememeType,
                            Object... paramaters) {
      this.referencedComponentNid    = referencedComponentNid;
      this.assemblageConceptSequence = assemblageConceptSequence;
      this.sememeType                = sememeType;
      this.parameters                = paramaters;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public C build(int stampSequence,
                  List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      if (referencedComponentNid == Integer.MAX_VALUE) {
         referencedComponentNid = Get.identifierService()
                                     .getNidForUuids(referencedComponentBuilder.getUuids());
      }

      SememeChronologyImpl sememeChronicle;
      int                  sememeNid = Get.identifierService()
                                          .getNidForUuids(this.getUuids());

      if (Get.sememeService()
             .hasSememe(sememeNid)) {
         sememeChronicle = (SememeChronologyImpl) Get.sememeService()
               .getSememe(sememeNid);

         if ((sememeChronicle.getSememeType() != sememeType) ||
               !sememeChronicle.getPrimordialUuid().equals(getPrimordialUuid()) ||
               (sememeChronicle.getAssemblageSequence() != assemblageConceptSequence) ||
               (sememeChronicle.getReferencedComponentNid() != referencedComponentNid)) {
            throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing sememe!");
         }
      } else {
         sememeChronicle = new SememeChronologyImpl(sememeType,
               getPrimordialUuid(),
               sememeNid,
               assemblageConceptSequence,
               referencedComponentNid,
               Get.identifierService().getSememeSequenceForUuids(this.getUuids()));
      }

      sememeChronicle.setAdditionalUuids(additionalUuids);

      switch (sememeType) {
      case COMPONENT_NID:
         ComponentNidSememeImpl cnsi =
            (ComponentNidSememeImpl) sememeChronicle.createMutableVersion(ComponentNidSememeImpl.class,
                                                                          stampSequence);

         cnsi.setComponentNid((Integer) parameters[0]);
         break;

      case LONG:
         LongSememeImpl lsi = (LongSememeImpl) sememeChronicle.createMutableVersion(LongSememeImpl.class,
                                                                                    stampSequence);

         lsi.setLongValue((Long) parameters[0]);
         break;

      case LOGIC_GRAPH:
         LogicGraphSememeImpl lgsi =
            (LogicGraphSememeImpl) sememeChronicle.createMutableVersion(LogicGraphSememeImpl.class,
                                                                        stampSequence);

         lgsi.setGraphData(((LogicalExpression) parameters[0]).getData(DataTarget.INTERNAL));
         break;

      case MEMBER:
         SememeVersionImpl svi = (SememeVersionImpl) sememeChronicle.createMutableVersion(SememeVersionImpl.class,
                                                                                          stampSequence);

         break;

      case STRING:
         StringSememeImpl ssi = (StringSememeImpl) sememeChronicle.createMutableVersion(StringSememeImpl.class,
                                                                                        stampSequence);

         ssi.setString((String) parameters[0]);
         break;

      case DESCRIPTION: {
         DescriptionSememeImpl dsi =
            (DescriptionSememeImpl) sememeChronicle.createMutableVersion(DescriptionSememeImpl.class,
                                                                         stampSequence);

         dsi.setCaseSignificanceConceptSequence((Integer) parameters[0]);
         dsi.setDescriptionTypeConceptSequence((Integer) parameters[1]);
         dsi.setLanguageConceptSequence((Integer) parameters[2]);
         dsi.setText((String) parameters[3]);
         break;
      }

      case DYNAMIC: {
         DynamicSememeImpl dsi = (DynamicSememeImpl) sememeChronicle.createMutableVersion(DynamicSememeImpl.class,
                                                                                          stampSequence);

         if ((parameters != null) && (parameters.length > 0)) {
            // See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
            dsi.setData(((AtomicReference<DynamicSememeData[]>) parameters[0]).get());
         }

         // TODO Dan this needs to fire the validator!
         break;
      }

      default:
         throw new UnsupportedOperationException("Can't handle: " + sememeType);
      }

      sememeBuilders.forEach((builder) -> builder.build(stampSequence, builtObjects));
      builtObjects.add(sememeChronicle);
      return (C) sememeChronicle;
   }

   @Override
   public OptionalWaitTask<C> build(EditCoordinate editCoordinate,
                                    ChangeCheckerMode changeCheckerMode,
                                    List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      if (referencedComponentNid == Integer.MAX_VALUE) {
         referencedComponentNid = Get.identifierService()
                                     .getNidForUuids(referencedComponentBuilder.getUuids());
      }

      SememeChronologyImpl sememeChronicle;
      int                  sememeNid = Get.identifierService()
                                          .getNidForUuids(this.getUuids());

      if (Get.sememeService()
             .hasSememe(sememeNid)) {
         sememeChronicle = (SememeChronologyImpl) Get.sememeService()
               .getSememe(sememeNid);

         if ((sememeChronicle.getSememeType() != sememeType) ||
               !sememeChronicle.getPrimordialUuid().equals(getPrimordialUuid()) ||
               (sememeChronicle.getAssemblageSequence() != assemblageConceptSequence) ||
               (sememeChronicle.getReferencedComponentNid() != referencedComponentNid)) {
            throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing sememe!");
         }
      } else {
         sememeChronicle = new SememeChronologyImpl(sememeType,
               getPrimordialUuid(),
               sememeNid,
               assemblageConceptSequence,
               referencedComponentNid,
               Get.identifierService().getSememeSequenceForUuids(this.getUuids()));
      }

      sememeChronicle.setAdditionalUuids(additionalUuids);

      switch (sememeType) {
      case COMPONENT_NID:
         ComponentNidSememeImpl cnsi =
            (ComponentNidSememeImpl) sememeChronicle.createMutableVersion(ComponentNidSememeImpl.class,
                                                                          state,
                                                                          editCoordinate);

         cnsi.setComponentNid((Integer) parameters[0]);
         break;

      case LONG:
         LongSememeImpl lsi = (LongSememeImpl) sememeChronicle.createMutableVersion(LongSememeImpl.class,
                                                                                    state,
                                                                                    editCoordinate);

         lsi.setLongValue((Long) parameters[0]);
         break;

      case LOGIC_GRAPH:
         LogicGraphSememeImpl lgsi =
            (LogicGraphSememeImpl) sememeChronicle.createMutableVersion(LogicGraphSememeImpl.class,
                                                                        state,
                                                                        editCoordinate);

         lgsi.setGraphData(((LogicalExpression) parameters[0]).getData(DataTarget.INTERNAL));
         break;

      case MEMBER:
         SememeVersionImpl svi = (SememeVersionImpl) sememeChronicle.createMutableVersion(SememeVersionImpl.class,
                                                                                          state,
                                                                                          editCoordinate);

         break;

      case STRING:
         StringSememeImpl ssi = (StringSememeImpl) sememeChronicle.createMutableVersion(StringSememeImpl.class,
                                                                                        state,
                                                                                        editCoordinate);

         ssi.setString((String) parameters[0]);
         break;

      case DESCRIPTION: {
         DescriptionSememeImpl dsi =
            (DescriptionSememeImpl) sememeChronicle.createMutableVersion(DescriptionSememeImpl.class,
                                                                         state,
                                                                         editCoordinate);

         dsi.setCaseSignificanceConceptSequence((Integer) parameters[0]);
         dsi.setDescriptionTypeConceptSequence((Integer) parameters[1]);
         dsi.setLanguageConceptSequence((Integer) parameters[2]);
         dsi.setText((String) parameters[3]);
         break;
      }

      case DYNAMIC: {
         DynamicSememeImpl dsi = (DynamicSememeImpl) sememeChronicle.createMutableVersion(DynamicSememeImpl.class,
                                                                                          state,
                                                                                          editCoordinate);

         if ((parameters != null) && (parameters.length > 0)) {
            // See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
            dsi.setData(((AtomicReference<DynamicSememeData[]>) parameters[0]).get());
         }

         // TODO DAN this needs to fire the validator!
         break;
      }

      default:
         throw new UnsupportedOperationException("Can't handle: " + sememeType);
      }

      Task<Void> primaryNested;

      if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
         primaryNested = Get.commitService()
                            .addUncommitted(sememeChronicle);
      } else {
         primaryNested = Get.commitService()
                            .addUncommittedNoChecks(sememeChronicle);
      }

      ArrayList<OptionalWaitTask<?>> nested = new ArrayList<>();

      sememeBuilders.forEach((builder) -> nested.add(builder.build(editCoordinate, changeCheckerMode, builtObjects)));
      builtObjects.add(sememeChronicle);
      return new OptionalWaitTask<C>(primaryNested, (C) sememeChronicle, nested);
   }
}

