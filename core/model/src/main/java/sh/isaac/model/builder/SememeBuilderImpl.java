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
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SememeBuilderImpl.
 *
 * @author kec
 * @param <C> the generic type
 */
public class SememeBuilderImpl<C extends SemanticChronology>
        extends ComponentBuilder<C>
         implements SemanticBuilder<C> {
   /** The referenced component nid. */
   int referencedComponentNid = Integer.MAX_VALUE;

   /** The referenced component builder. */
   IdentifiedComponentBuilder referencedComponentBuilder;

   /** The assemblage concept sequence. */
   int assemblageConceptSequence;

   /** The sememe type. */
   VersionType sememeType;

   /** The parameters. */
   Object[] parameters;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe builder impl.
    *
    * @param referencedComponentBuilder the referenced component builder
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param sememeType the sememe type
    * @param paramaters the paramaters
    */
   public SememeBuilderImpl(IdentifiedComponentBuilder referencedComponentBuilder,
                            int assemblageConceptSequence,
                            VersionType sememeType,
                            Object... paramaters) {
      this.referencedComponentBuilder = referencedComponentBuilder;
      this.assemblageConceptSequence  = assemblageConceptSequence;
      this.sememeType                 = sememeType;
      this.parameters                 = paramaters;
   }

   /**
    * Instantiates a new sememe builder impl.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param sememeType the sememe type
    * @param paramaters the paramaters
    */
   public SememeBuilderImpl(int referencedComponentNid,
                            int assemblageConceptSequence,
                            VersionType sememeType,
                            Object... paramaters) {
      this.referencedComponentNid    = referencedComponentNid;
      this.assemblageConceptSequence = assemblageConceptSequence;
      this.sememeType                = sememeType;
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

      SemanticChronologyImpl sememeChronicle;
      final int            sememeNid = Get.identifierService()
                                          .getNidForUuids(this.getUuids());

      if (Get.assemblageService()
             .hasSemantic(sememeNid)) {
         sememeChronicle = (SemanticChronologyImpl) Get.assemblageService()
               .getSemanticChronology(sememeNid);

         if ((sememeChronicle.getVersionType() != this.sememeType) ||
               !sememeChronicle.isIdentifiedBy(getPrimordialUuid()) ||
               (sememeChronicle.getAssemblageSequence() != this.assemblageConceptSequence) ||
               (sememeChronicle.getReferencedComponentNid() != this.referencedComponentNid)) {
            throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing sememe!");
         }
      } else {
         sememeChronicle = new SemanticChronologyImpl(this.sememeType,
               getPrimordialUuid(),
               sememeNid,
               this.assemblageConceptSequence,
               this.referencedComponentNid,
               Get.identifierService().getSemanticSequenceForUuids(this.getUuids()));
      }

      sememeChronicle.setAdditionalUuids(this.additionalUuids);

      switch (this.sememeType) {
      case COMPONENT_NID:
         final ComponentNidVersionImpl cnsi =
            (ComponentNidVersionImpl) sememeChronicle.createMutableVersion(stampSequence);

         cnsi.setComponentNid((Integer) this.parameters[0]);
         break;

      case LONG:
         final LongVersionImpl lsi = (LongVersionImpl) sememeChronicle.createMutableVersion(stampSequence);

         lsi.setLongValue((Long) this.parameters[0]);
         break;

      case LOGIC_GRAPH:
         final LogicGraphVersionImpl lgsi =
            (LogicGraphVersionImpl) sememeChronicle.createMutableVersion(stampSequence);

         lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
         break;

      case MEMBER:
         sememeChronicle.createMutableVersion(stampSequence);
         break;

      case STRING:
         final StringVersionImpl ssi = (StringVersionImpl) sememeChronicle.createMutableVersion(stampSequence);

         ssi.setString((String) this.parameters[0]);
         break;

      case DESCRIPTION: {
         final DescriptionVersionImpl dsi =
            (DescriptionVersionImpl) sememeChronicle.createMutableVersion(stampSequence);

         dsi.setCaseSignificanceConceptSequence((Integer) this.parameters[0]);
         dsi.setDescriptionTypeConceptSequence((Integer) this.parameters[1]);
         dsi.setLanguageConceptSequence((Integer) this.parameters[2]);
         dsi.setText((String) this.parameters[3]);
         break;
      }

      case DYNAMIC: {
         final DynamicImpl dsi = (DynamicImpl) sememeChronicle.createMutableVersion(stampSequence);

         if ((this.parameters != null) && (this.parameters.length > 0)) {
            // See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
            dsi.setData(((AtomicReference<DynamicData[]>) this.parameters[0]).get());
         }

         // TODO Dan this needs to fire the validator!
         break;
      }

      default:
         throw new UnsupportedOperationException("p Can't handle: " + this.sememeType);
      }

      this.sememeBuilders.forEach((builder) -> builder.build(stampSequence, builtObjects));
      builtObjects.add(sememeChronicle);
      return (C) sememeChronicle;
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
      if (this.referencedComponentNid == Integer.MAX_VALUE) {
         this.referencedComponentNid = Get.identifierService()
                                          .getNidForUuids(this.referencedComponentBuilder.getUuids());
      }

      SemanticChronologyImpl sememeChronicle;
      final int            sememeNid = Get.identifierService()
                                          .getNidForUuids(this.getUuids());

      if (Get.assemblageService()
             .hasSemantic(sememeNid)) {
         sememeChronicle = (SemanticChronologyImpl) Get.assemblageService()
               .getSemanticChronology(sememeNid);

         if ((sememeChronicle.getVersionType() != this.sememeType) ||
               !sememeChronicle.isIdentifiedBy(getPrimordialUuid()) ||
               (sememeChronicle.getAssemblageSequence() != this.assemblageConceptSequence) ||
               (sememeChronicle.getReferencedComponentNid() != this.referencedComponentNid)) {
            throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing sememe!");
         }
      } else {
         sememeChronicle = new SemanticChronologyImpl(this.sememeType,
               getPrimordialUuid(),
               sememeNid,
               this.assemblageConceptSequence,
               this.referencedComponentNid,
               Get.identifierService().getSemanticSequenceForUuids(this.getUuids()));
      }

      sememeChronicle.setAdditionalUuids(this.additionalUuids);

      switch (this.sememeType) {
      case COMPONENT_NID:
         final ComponentNidVersionImpl cnsi =
            (ComponentNidVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                          editCoordinate);

         cnsi.setComponentNid((Integer) this.parameters[0]);
         break;

      case LONG:
         final LongVersionImpl lsi = (LongVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                                          editCoordinate);

         lsi.setLongValue((Long) this.parameters[0]);
         break;

      case LOGIC_GRAPH:
         final LogicGraphVersionImpl lgsi =
            (LogicGraphVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                        editCoordinate);

         lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
         break;

      case MEMBER:
         sememeChronicle.createMutableVersion(this.state, editCoordinate);
         break;

      case STRING:
         final StringVersionImpl ssi = (StringVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                                              editCoordinate);

         ssi.setString((String) this.parameters[0]);
         break;

      case DESCRIPTION: {
         final DescriptionVersionImpl dsi =
            (DescriptionVersionImpl) sememeChronicle.createMutableVersion(this.state,
                                                                         editCoordinate);

         dsi.setCaseSignificanceConceptSequence((Integer) this.parameters[0]);
         dsi.setDescriptionTypeConceptSequence((Integer) this.parameters[1]);
         dsi.setLanguageConceptSequence((Integer) this.parameters[2]);
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

         // TODO DAN this needs to fire the validator!
         break;
      }

      default:
         throw new UnsupportedOperationException("q Can't handle: " + this.sememeType);
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

      this.sememeBuilders.forEach((builder) -> nested.add(builder.build(editCoordinate,
            changeCheckerMode,
            builtObjects)));
      builtObjects.add(sememeChronicle);
      return new OptionalWaitTask<>(primaryNested, (C) sememeChronicle, nested);
   }
}

