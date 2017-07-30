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


//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizableObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableConceptChronologyImpl.
 *
 * @author kec
 */
public class ObservableConceptChronologyImpl
        extends ObservableChronologyImpl
         implements ObservableConceptChronology {
   /** The concept sequence property. */
   private IntegerProperty conceptSequenceProperty;

   /** The description list property. */
   private ListProperty<ObservableSememeChronology> descriptionListProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable concept chronology impl.
    *
    * @param chronicledObjectLocal the chronicled object local
    */
   public ObservableConceptChronologyImpl(ConceptChronology chronicledObjectLocal) {
      super(chronicledObjectLocal);
   }
   
   public ConceptChronology getConceptChronology() {
      return (ConceptChronology) this.chronicledObjectLocal;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Concept description list property.
    *
    * @return the list property
    */
   @Override
   public ListProperty<ObservableSememeChronology> conceptDescriptionListProperty() {
      if (this.descriptionListProperty == null) {
         final ObservableList<ObservableSememeChronology> observableList =
            FXCollections.observableArrayList();

         this.descriptionListProperty =
            new SimpleListProperty<>(this,
                  ObservableFields.DESCRIPTION_LIST_FOR_CONCEPT.toExternalString(),
                  observableList);
         this.getConceptChronology().getConceptDescriptionList().stream().forEach((conceptDescriptionChronicle) -> {
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
      return this.getConceptChronology().containsDescription(descriptionText, stampCoordinate);
   }

   /**
    * Contains description.
    *
    * @param descriptionText the description text
    * @return true, if successful
    */
   @Override
   public boolean containsDescription(String descriptionText) {
      return this.getConceptChronology().containsDescription(descriptionText);
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
   public ObservableList<ObservableSememeChronology> getConceptDescriptionList() {
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

      return this.getConceptChronology().getConceptSequence();
   }

   /**
    * Gets the fully specified description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   @Override
   public LatestVersion<ObservableDescriptionVersion> getFullySpecifiedDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      final LatestVersion<DescriptionVersion> optionalFsn =
         this.getConceptChronology().getFullySpecifiedDescription(languageCoordinate,
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
                                            observableList.add(new ObservableConceptVersionImpl((ConceptVersion) conceptVersion, this));
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
   public LatestVersion<ObservableDescriptionVersion> getPreferredDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      final LatestVersion<DescriptionVersion> optionalPreferred =
         this.getConceptChronology().getPreferredDescription(languageCoordinate,
                                                            stampCoordinate);

      return getSpecifiedDescription(optionalPreferred);
   }

   /**
    * Gets the specified description.
    *
    * @param description the optional specified description
    * @return the specified description
    */
   private LatestVersion<ObservableDescriptionVersion> getSpecifiedDescription(
           LatestVersion<DescriptionVersion> description) {
      if (description.isPresent()) {
         final int specifiedStampSequence = ((DescriptionVersion) description.get()).getStampSequence();
         final ObservableSememeChronology observableSpecified =
            new ObservableSememeChronologyImpl(((DescriptionVersion) description.get()).getChronology());

         

         LatestVersion<ObservableDescriptionVersion> latest = new LatestVersion<>(ObservableDescriptionVersion.class);

         observableSpecified.getVersionList().stream().filter((descVersion) -> (descVersion.getStampSequence() == specifiedStampSequence)).forEachOrdered((descVersion) -> {
            latest.addLatest((ObservableDescriptionVersion) descVersion);
         });

         return latest;
      }

      return new LatestVersion<>(ObservableDescriptionVersion.class);
   }

   @Override
   public <V extends StampedVersion> LatestVersion<V> getLatestVersion(StampCoordinate coordinate) {
      return getConceptChronology().getLatestVersion(coordinate);
   }

   @Override
   public boolean isLatestVersionActive(StampCoordinate coordinate) {
      return getConceptChronology().isLatestVersionActive(coordinate);
   }

   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      getConceptChronology().putExternal(out);
   }

   @Override
   public byte getDataFormatVersion() {
      return getConceptChronology().getDataFormatVersion();
   }

   @Override
   public IsaacExternalizableObjectType getExternalizableObjectType() {
      return getConceptChronology().getExternalizableObjectType();
   }
}