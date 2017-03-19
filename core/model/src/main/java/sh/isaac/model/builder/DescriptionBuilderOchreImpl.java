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

import javafx.concurrent.Task;

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
 *
 * @author kec
 * @param <T>
 * @param <V>
 */
public class DescriptionBuilderOchreImpl<T extends SememeChronology<V>, V extends DescriptionSememeImpl>
        extends ComponentBuilder<T>
         implements DescriptionBuilder<T, V> {
   private final ArrayList<ConceptSpecification> preferredInDialectAssemblages  = new ArrayList<>();
   private final ArrayList<ConceptSpecification> acceptableInDialectAssemblages = new ArrayList<>();
   private int                                   conceptSequence                = Integer.MAX_VALUE;
   private final String                          descriptionText;
   private final ConceptSpecification            descriptionType;
   private final ConceptSpecification            languageForDescription;
   private final ConceptBuilder                  conceptBuilder;

   //~--- constructors --------------------------------------------------------

   public DescriptionBuilderOchreImpl(String descriptionText,
                                      ConceptBuilder conceptBuilder,
                                      ConceptSpecification descriptionType,
                                      ConceptSpecification languageForDescription) {
      this.descriptionText        = descriptionText;
      this.descriptionType        = descriptionType;
      this.languageForDescription = languageForDescription;
      this.conceptBuilder         = conceptBuilder;
   }

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

   @Override
   public DescriptionBuilder addAcceptableInDialectAssemblage(ConceptSpecification dialectAssemblage) {
      acceptableInDialectAssemblages.add(dialectAssemblage);
      return this;
   }

   @Override
   public DescriptionBuilder addPreferredInDialectAssemblage(ConceptSpecification dialectAssemblage) {
      preferredInDialectAssemblages.add(dialectAssemblage);
      return this;
   }

   @Override
   public T build(int stampSequence,
                  List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      if (conceptSequence == Integer.MAX_VALUE) {
         conceptSequence = Get.identifierService()
                              .getConceptSequenceForUuids(conceptBuilder.getUuids());
      }

      SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
      SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder =
         sememeBuilder.getDescriptionSememeBuilder(TermAux.caseSignificanceToConceptSequence(false),
                                                   languageForDescription.getConceptSequence(),
                                                   descriptionType.getConceptSequence(),
                                                   descriptionText,
                                                   Get.identifierService()
                                                         .getConceptNid(conceptSequence));

      descBuilder.setPrimordialUuid(this.getPrimordialUuid());

      SememeChronologyImpl<DescriptionSememeImpl> newDescription =
         (SememeChronologyImpl<DescriptionSememeImpl>) descBuilder.build(stampSequence,
                                                                         builtObjects);
      SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);

      preferredInDialectAssemblages.forEach((assemblageProxy) -> {
               sememeBuilderService.getComponentSememeBuilder(TermAux.PREFERRED.getNid(),
                     this,
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                                   .build(stampSequence, builtObjects);
            });
      acceptableInDialectAssemblages.forEach((assemblageProxy) -> {
               sememeBuilderService.getComponentSememeBuilder(TermAux.ACCEPTABLE.getNid(),
                     this,
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                                   .build(stampSequence, builtObjects);
            });
      sememeBuilders.forEach((builder) -> builder.build(stampSequence, builtObjects));
      return (T) newDescription;
   }

   @Override
   public OptionalWaitTask<T> build(EditCoordinate editCoordinate,
                                    ChangeCheckerMode changeCheckerMode,
                                    List<ObjectChronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException {
      if (conceptSequence == Integer.MAX_VALUE) {
         conceptSequence = Get.identifierService()
                              .getConceptSequenceForUuids(conceptBuilder.getUuids());
      }

      ArrayList<OptionalWaitTask<?>> nestedBuilders = new ArrayList<>();
      SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
      SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder =
         sememeBuilder.getDescriptionSememeBuilder(Get.languageCoordinateService()
                                                      .caseSignificanceToConceptSequence(false),
                                                   languageForDescription.getConceptSequence(),
                                                   descriptionType.getConceptSequence(),
                                                   descriptionText,
                                                   Get.identifierService()
                                                         .getConceptNid(conceptSequence));

      descBuilder.setPrimordialUuid(this.getPrimordialUuid());

      OptionalWaitTask<SememeChronologyImpl<DescriptionSememeImpl>> newDescription =
         (OptionalWaitTask<SememeChronologyImpl<DescriptionSememeImpl>>) descBuilder.setState(state)
                                                                                    .build(editCoordinate,
                                                                                          changeCheckerMode,
                                                                                          builtObjects);

      nestedBuilders.add(newDescription);

      SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);

      preferredInDialectAssemblages.forEach((assemblageProxy) -> {
               nestedBuilders.add(sememeBuilderService.getComponentSememeBuilder(TermAux.PREFERRED.getNid(),
                     newDescription.getNoWait()
                                   .getNid(),
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                     .build(editCoordinate, changeCheckerMode, builtObjects));
            });
      acceptableInDialectAssemblages.forEach((assemblageProxy) -> {
               nestedBuilders.add(sememeBuilderService.getComponentSememeBuilder(TermAux.ACCEPTABLE.getNid(),
                     newDescription.getNoWait()
                                   .getNid(),
                     Get.identifierService()
                        .getConceptSequenceForProxy(assemblageProxy))
                     .build(editCoordinate, changeCheckerMode, builtObjects));
            });
      sememeBuilders.forEach((builder) -> nestedBuilders.add(builder.build(editCoordinate,
            changeCheckerMode,
            builtObjects)));
      return new OptionalWaitTask<T>(null, (T) newDescription.getNoWait(), nestedBuilders);
   }
}

