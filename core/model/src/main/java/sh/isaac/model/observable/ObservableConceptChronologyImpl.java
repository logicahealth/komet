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
 * The Class ObservableConceptChronologyImpl.
 *
 * @author kec
 */
public class ObservableConceptChronologyImpl
        extends ObservableChronologyImpl<ObservableConceptVersionImpl, ConceptChronology<ConceptVersionImpl>>
         implements ObservableConceptChronology<ObservableConceptVersionImpl> {
   /** The concept sequence property. */
   private IntegerProperty conceptSequenceProperty;

   /** The description list property. */
   private ListProperty<ObservableSememeChronology<ObservableDescriptionSememe<?>>> descriptionListProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable concept chronology impl.
    *
    * @param chronicledObjectLocal the chronicled object local
    */
   public ObservableConceptChronologyImpl(ConceptChronology<ConceptVersionImpl> chronicledObjectLocal) {
      super(chronicledObjectLocal);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Concept description list property.
    *
    * @return the list property
    */
   @Override
   public ListProperty<ObservableSememeChronology<ObservableDescriptionSememe<?>>> conceptDescriptionListProperty() {
      if (this.descriptionListProperty == null) {
         final ObservableList<ObservableSememeChronology<ObservableDescriptionSememe<?>>> observableList =
            FXCollections.observableArrayList();

         this.descriptionListProperty =
            new SimpleListProperty<>(this,
                  ObservableFields.DESCRIPTION_LIST_FOR_CONCEPT.toExternalString(),
                  observableList);
         this.chronicledObjectLocal.getConceptDescriptionList().stream().forEach((conceptDescriptionChronicle) -> {
                                               final ObservableSememeChronologyImpl observableConceptDescriptionChronicle =
                                                  new ObservableSememeChronologyImpl(conceptDescriptionChronicle);

                                               observableList.add(observableConceptDescriptionChronicle);
                                            });
      }

      return this.descriptionListProperty;
   }

   /**
    * Concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty conceptSequenceProperty() {
      if (this.conceptSequenceProperty == null) {
         this.conceptSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.CONCEPT_SEQUENCE_FOR_CHRONICLE.toExternalString(),
               getConceptSequence());
      }

      return this.conceptSequenceProperty;
   }

   /**
    * Contains active description.
    *
    * @param descriptionText the description text
    * @param stampCoordinate the stamp coordinate
    * @return true, if successful
    */
   @Override
   public boolean containsActiveDescription(String descriptionText, StampCoordinate stampCoordinate) {
      return this.chronicledObjectLocal.containsDescription(descriptionText, stampCoordinate);
   }

   /**
    * Contains description.
    *
    * @param descriptionText the description text
    * @return true, if successful
    */
   @Override
   public boolean containsDescription(String descriptionText) {
      return this.chronicledObjectLocal.containsDescription(descriptionText);
   }

   /**
    * Creates the mutable version.
    *
    * @param stampSequence the stamp sequence
    * @return the observable concept version impl
    */
   @Override
   public ObservableConceptVersionImpl createMutableVersion(int stampSequence) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Creates the mutable version.
    *
    * @param state the state
    * @param ec the ec
    * @return the observable concept version impl
    */
   @Override
   public ObservableConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept description list.
    *
    * @return the concept description list
    */
   @Override
   public ObservableList<ObservableSememeChronology<ObservableDescriptionSememe<?>>> getConceptDescriptionList() {
      return conceptDescriptionListProperty().get();
   }

   /**
    * Gets the concept sequence.
    *
    * @return the concept sequence
    */
   @Override
   public int getConceptSequence() {
      if (this.conceptSequenceProperty != null) {
         return this.conceptSequenceProperty.get();
      }

      return this.chronicledObjectLocal.getConceptSequence();
   }

   /**
    * Gets the fully specified description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   @Override
   public Optional<LatestVersion<ObservableDescriptionSememe<?>>> getFullySpecifiedDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      final Optional<LatestVersion<DescriptionSememe<?>>> optionalFsn =
         this.chronicledObjectLocal.getFullySpecifiedDescription(languageCoordinate,
                                                                 stampCoordinate);

      return getSpecifiedDescription(optionalFsn);
   }

   /**
    * Gets the observable version list.
    *
    * @return the observable version list
    */
   @Override
   protected ObservableList<ObservableConceptVersionImpl> getObservableVersionList() {
      final ObservableList<ObservableConceptVersionImpl> observableList = FXCollections.observableArrayList();

      this.chronicledObjectLocal.getVersionList().stream().forEach((conceptVersion) -> {
                                            observableList.add(new ObservableConceptVersionImpl(conceptVersion, this));
                                         });
      return observableList;
   }

   /**
    * Gets the preferred description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the preferred description
    */
   @Override
   public Optional<LatestVersion<ObservableDescriptionSememe<?>>> getPreferredDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      final Optional<LatestVersion<DescriptionSememe<?>>> optionalPreferred =
         this.chronicledObjectLocal.getPreferredDescription(languageCoordinate,
                                                            stampCoordinate);

      return getSpecifiedDescription(optionalPreferred);
   }

   /**
    * Gets the specified description.
    *
    * @param optionalSpecifiedDescription the optional specified description
    * @return the specified description
    */
   private Optional<LatestVersion<ObservableDescriptionSememe<?>>> getSpecifiedDescription(
           Optional<LatestVersion<DescriptionSememe<?>>> optionalSpecifiedDescription) {
      if (optionalSpecifiedDescription.isPresent() && optionalSpecifiedDescription.get().value().isPresent()) {
         final LatestVersion<DescriptionSememe<?>> latestPreferred = optionalSpecifiedDescription.get();
         final int latestStampSequence = ((DescriptionSememe) latestPreferred.value().get()).getStampSequence();
         final ObservableSememeChronologyImpl<ObservableDescriptionImpl, SememeChronology<DescriptionSememe>> observableSpecified =
            new ObservableSememeChronologyImpl(((DescriptionSememe) latestPreferred.value().get()).getChronology());

         new LatestVersion<>(ObservableDescriptionSememe.class);

         LatestVersion<ObservableDescriptionSememe<?>> latest = null;

         for (final ObservableDescriptionSememe<?> descVersion: observableSpecified.getVersionList()) {
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

