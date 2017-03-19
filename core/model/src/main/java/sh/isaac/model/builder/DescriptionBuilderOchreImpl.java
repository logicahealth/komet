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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DescriptionBuilderOchreImpl.
 *
 * @author kec
 * @param <T> the generic type
 * @param <V> the value type
 */
public class DescriptionBuilderOchreImpl<T extends SememeChronology<V>, V extends DescriptionSememeImpl>
        extends ComponentBuilder<T>
         implements DescriptionBuilder<T, V> {
   /** The preferred in dialect assemblages. */
   private final ArrayList<ConceptSpecification> preferredInDialectAssemblages = new ArrayList<>();

   /** The acceptable in dialect assemblages. */
   private final ArrayList<ConceptSpecification> acceptableInDialectAssemblages = new ArrayList<>();

   /** The concept sequence. */
   private int conceptSequence = Integer.MAX_VALUE;

   /** The description text. */
   private final String descriptionText;

   /** The description type. */
   private final ConceptSpecification descriptionType;

   /** The language for description. */
   private final ConceptSpecification languageForDescription;

   /** The concept builder. */
   private final ConceptBuilder conceptBuilder;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new description builder ochre impl.
    *
    * @param descriptionText the description text
    * @param conceptBuilder the concept builder
    * @param descriptionType the description type
    * @param languageForDescription the language for description
    */
   public DescriptionBuilderOchreImpl(String descriptionText,
                                      ConceptBuilder conceptBuilder,
                                      ConceptSpecification descriptionType,
                                      ConceptSpecification languageForDescription) {
      this.descriptionText        = descriptionText;
      this.descriptionType        = descriptionType;
      this.languageForDescription = languageForDescription;
      this.conceptBuilder         = conceptBuilder;
   }

   /**
    * Instantiates a new description builder ochre impl.
    *
    * @param descriptionText the description text
    * @param conceptSequence the concept sequence
    * @param descriptionType the description type
    * @param languageForDescription the language for description
    */
   public DescriptionBuilderOchreImpl(String descriptionText,
                                      int conceptSequence,
                                      ConceptSpecification descriptionType,
                                      ConceptSpecification languageForDescription) {
      this.descriptionText        = descriptionText;
      this.conceptSequence        = conceptSequence;
      this.descriptionType        = descriptionType;
      this.languageForDescription = languageForDescription;
      this.conceptBuilder         = null;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the acceptable in dialect assemblage.
    *
    * @param dialectAssemblage the dialect assemblage
    * @return the description builder
    */
   @Override
   public DescriptionBuilder addAcceptableInDialectAssemblage(ConceptSpecification dialectAssemblage) {
      this.acceptableInDialectAssemblages.add(dialectAssemblage);
      return this;
   }

   /**
    * Adds the preferred in dialect assemblage.
    *
    * @param dialectAssemblage the dialect assemblage
    * @return the description builder
    */
   @Override
   public DescriptionBuilder addPreferredInDialectAssemblage(ConceptSpecification dialectAssemblage) {
      this.preferredInDialectAssemblages.add(dialectAssemblage);
      return this;
   }

   /**
    * Builds the.
    *
    * @param stampSequence the stamp sequence
    * @param builtObjects the built objects
    * @return the t
    * @throws IllegalStateException the illegal state exception
    */
   @Override
   public T build(int stampSequence,
                  List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      if (this.conceptSequence == Integer.MAX_VALUE) {
         this.conceptSequence = Get.identifierService()
                                   .getConceptSequenceForUuids(this.conceptBuilder.getUuids());
      }

      final SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
      final SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder =
         sememeBuilder.getDescriptionSememeBuilder(TermAux.caseSignificanceToConceptSequence(false),
                                                   this.languageForDescription.getConceptSequence(),
                                                   this.descriptionType.getConceptSequence(),
                                                   this.descriptionText,
                                                   Get.identifierService()
                                                         .getConceptNid(this.conceptSequence));

      descBuilder.setPrimordialUuid(this.getPrimordialUuid());

      final SememeChronologyImpl<DescriptionSememeImpl> newDescription =
         (SememeChronologyImpl<DescriptionSememeImpl>) descBuilder.build(stampSequence,
                                                                         builtObjects);
      final SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);

      this.preferredInDialectAssemblages.forEach((assemblageProxy) -> {
               sememeBuilderService.getComponentSememeBuilder(TermAux.PREFERRED.getNid(),
                     this,
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                                   .build(stampSequence, builtObjects);
            });
      this.acceptableInDialectAssemblages.forEach((assemblageProxy) -> {
               sememeBuilderService.getComponentSememeBuilder(TermAux.ACCEPTABLE.getNid(),
                     this,
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                                   .build(stampSequence, builtObjects);
            });
      this.sememeBuilders.forEach((builder) -> builder.build(stampSequence, builtObjects));
      return (T) newDescription;
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
   public OptionalWaitTask<T> build(EditCoordinate editCoordinate,
                                    ChangeCheckerMode changeCheckerMode,
                                    List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      if (this.conceptSequence == Integer.MAX_VALUE) {
         this.conceptSequence = Get.identifierService()
                                   .getConceptSequenceForUuids(this.conceptBuilder.getUuids());
      }

      final ArrayList<OptionalWaitTask<?>> nestedBuilders = new ArrayList<>();
      final SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
      final SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder =
         sememeBuilder.getDescriptionSememeBuilder(Get.languageCoordinateService()
                                                      .caseSignificanceToConceptSequence(false),
                                                   this.languageForDescription.getConceptSequence(),
                                                   this.descriptionType.getConceptSequence(),
                                                   this.descriptionText,
                                                   Get.identifierService()
                                                         .getConceptNid(this.conceptSequence));

      descBuilder.setPrimordialUuid(this.getPrimordialUuid());

      final OptionalWaitTask<SememeChronologyImpl<DescriptionSememeImpl>> newDescription =
         (OptionalWaitTask<SememeChronologyImpl<DescriptionSememeImpl>>) descBuilder.setState(this.state)
                                                                                    .build(editCoordinate,
                                                                                          changeCheckerMode,
                                                                                          builtObjects);

      nestedBuilders.add(newDescription);

      final SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);

      this.preferredInDialectAssemblages.forEach((assemblageProxy) -> {
               nestedBuilders.add(sememeBuilderService.getComponentSememeBuilder(TermAux.PREFERRED.getNid(),
                     newDescription.getNoWait()
                                   .getNid(),
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                     .build(editCoordinate, changeCheckerMode, builtObjects));
            });
      this.acceptableInDialectAssemblages.forEach((assemblageProxy) -> {
               nestedBuilders.add(sememeBuilderService.getComponentSememeBuilder(TermAux.ACCEPTABLE.getNid(),
                     newDescription.getNoWait()
                                   .getNid(),
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                     .build(editCoordinate, changeCheckerMode, builtObjects));
            });
      this.sememeBuilders.forEach((builder) -> nestedBuilders.add(builder.build(editCoordinate,
            changeCheckerMode,
            builtObjects)));
      return new OptionalWaitTask<>(null, (T) newDescription.getNoWait(), nestedBuilders);
   }
}

