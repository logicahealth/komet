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



package sh.isaac.model.observable;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionSememe;
import sh.isaac.model.concept.ConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableDescriptionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableConceptChronologyImpl
        extends ObservableChronologyImpl<ObservableConceptVersionImpl, ConceptChronology<ConceptVersionImpl>>
         implements ObservableConceptChronology<ObservableConceptVersionImpl> {
   private IntegerProperty                                                          conceptSequenceProperty;
   private ListProperty<ObservableSememeChronology<ObservableDescriptionSememe<?>>> descriptionListProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableConceptChronologyImpl(ConceptChronology<ConceptVersionImpl> chronicledObjectLocal) {
      super(chronicledObjectLocal);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public ListProperty<ObservableSememeChronology<ObservableDescriptionSememe<?>>> conceptDescriptionListProperty() {
      if (descriptionListProperty == null) {
         ObservableList<ObservableSememeChronology<ObservableDescriptionSememe<?>>> observableList =
            FXCollections.observableArrayList();

         descriptionListProperty =
            new SimpleListProperty<ObservableSememeChronology<ObservableDescriptionSememe<?>>>(this,
                  ObservableFields.DESCRIPTION_LIST_FOR_CONCEPT.toExternalString(),
                  observableList);
         chronicledObjectLocal.getConceptDescriptionList().stream().forEach((conceptDescriptionChronicle) -> {
                                          ObservableSememeChronologyImpl observableConceptDescriptionChronicle =
                                             new ObservableSememeChronologyImpl(
                                                (SememeChronology) conceptDescriptionChronicle);

                                          observableList.add(observableConceptDescriptionChronicle);
                                       });
      }

      return descriptionListProperty;
   }

   @Override
   public IntegerProperty conceptSequenceProperty() {
      if (conceptSequenceProperty == null) {
         conceptSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.CONCEPT_SEQUENCE_FOR_CHRONICLE.toExternalString(),
               getConceptSequence());
      }

      return conceptSequenceProperty;
   }

   @Override
   public boolean containsActiveDescription(String descriptionText, StampCoordinate stampCoordinate) {
      return chronicledObjectLocal.containsDescription(descriptionText, stampCoordinate);
   }

   @Override
   public boolean containsDescription(String descriptionText) {
      return chronicledObjectLocal.containsDescription(descriptionText);
   }

   @Override
   public ObservableConceptVersionImpl createMutableVersion(int stampSequence) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ObservableConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ObservableList<ObservableSememeChronology<ObservableDescriptionSememe<?>>> getConceptDescriptionList() {
      return conceptDescriptionListProperty().get();
   }

   @Override
   public int getConceptSequence() {
      if (conceptSequenceProperty != null) {
         return conceptSequenceProperty.get();
      }

      return chronicledObjectLocal.getConceptSequence();
   }

   @Override
   public Optional<LatestVersion<ObservableDescriptionSememe<?>>> getFullySpecifiedDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      Optional<LatestVersion<DescriptionSememe<?>>> optionalFsn =
         chronicledObjectLocal.getFullySpecifiedDescription(languageCoordinate,
                                                            stampCoordinate);

      return getSpecifiedDescription(optionalFsn);
   }

   @Override
   protected ObservableList<ObservableConceptVersionImpl> getObservableVersionList() {
      ObservableList<ObservableConceptVersionImpl> observableList = FXCollections.observableArrayList();

      chronicledObjectLocal.getVersionList().stream().forEach((conceptVersion) -> {
                                       observableList.add(new ObservableConceptVersionImpl(conceptVersion, this));
                                    });
      return observableList;
   }

   @Override
   public Optional<LatestVersion<ObservableDescriptionSememe<?>>> getPreferredDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      Optional<LatestVersion<DescriptionSememe<?>>> optionalPreferred =
         chronicledObjectLocal.getPreferredDescription(languageCoordinate,
                                                       stampCoordinate);

      return getSpecifiedDescription(optionalPreferred);
   }

   private Optional<LatestVersion<ObservableDescriptionSememe<?>>> getSpecifiedDescription(
           Optional<LatestVersion<DescriptionSememe<?>>> optionalSpecifiedDescription) {
      if (optionalSpecifiedDescription.isPresent()) {
         LatestVersion<DescriptionSememe<?>> latestPreferred = optionalSpecifiedDescription.get();
         int latestStampSequence = ((DescriptionSememe) latestPreferred.value()).getStampSequence();
         ObservableSememeChronologyImpl<ObservableDescriptionImpl, SememeChronology<DescriptionSememe>> observableSpecified =
            new ObservableSememeChronologyImpl(((DescriptionSememe) latestPreferred.value()).getChronology());
         LatestVersion<ObservableDescriptionSememe> rawLatest = new LatestVersion<>(ObservableDescriptionSememe.class);
         LatestVersion<ObservableDescriptionSememe<?>> latest = null;

         for (ObservableDescriptionSememe<?> descVersion: observableSpecified.getVersionList()) {
            if (descVersion.getStampSequence() == latestStampSequence) {
               if (latest == null) {
                  latest = new LatestVersion<>(descVersion);
               } else {
                  latest.addLatest(descVersion);
               }
            }
         }

         return Optional.of(latest);
      }

      return Optional.empty();
   }
}

