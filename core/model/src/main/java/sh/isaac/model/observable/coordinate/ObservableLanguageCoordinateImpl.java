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



package sh.isaac.model.observable.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableLanguageCoordinateImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "observableLanguageCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ObservableLanguageCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableLanguageCoordinate {
   /** The language concept sequence property. */
   @XmlTransient
   IntegerProperty languageConceptSequenceProperty = null;

   /** The dialect assemblage preference list property. */
   @XmlTransient
   ObjectProperty<ObservableIntegerArray> dialectAssemblagePreferenceListProperty = null;

   /** The description type preference list property. */
   @XmlTransient
   ObjectProperty<ObservableIntegerArray> descriptionTypePreferenceListProperty = null;

   /** The language coordinate. */
   private LanguageCoordinate languageCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable language coordinate impl.
    */
   private ObservableLanguageCoordinateImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new observable language coordinate impl.
    *
    * @param languageCoordinate the language coordinate
    */
   public ObservableLanguageCoordinateImpl(LanguageCoordinate languageCoordinate) {
      if (languageCoordinate instanceof ObservableLanguageCoordinateImpl) {
         throw new IllegalStateException("Trying to wrap an observable coordinate in an observable coordinate...");
                 
      }
      this.languageCoordinate = languageCoordinate;
      if (languageCoordinate instanceof ObservableLanguageCoordinateImpl) {
         ObservableLanguageCoordinateImpl observableLanguageCoordinate = (ObservableLanguageCoordinateImpl) languageCoordinate;
         languageConceptSequenceProperty = observableLanguageCoordinate.languageConceptSequenceProperty;
         dialectAssemblagePreferenceListProperty = observableLanguageCoordinate.dialectAssemblagePreferenceListProperty;
         descriptionTypePreferenceListProperty = observableLanguageCoordinate.descriptionTypePreferenceListProperty;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Description type preference list property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<ObservableIntegerArray> descriptionTypePreferenceListProperty() {
      if (this.descriptionTypePreferenceListProperty == null) {
         this.descriptionTypePreferenceListProperty = new SimpleObjectProperty<>(this,
               ObservableFields.DESCRIPTION_TYPE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
               FXCollections.observableIntegerArray(getDescriptionTypePreferenceList()));
         addListenerReference(
             ((LanguageCoordinateImpl) this.languageCoordinate).setDescriptionTypePreferenceListProperty(
                 this.descriptionTypePreferenceListProperty));
      }

      return this.descriptionTypePreferenceListProperty;
   }

   /**
    * Dialect assemblage preference list property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<ObservableIntegerArray> dialectAssemblagePreferenceListProperty() {
      if (this.dialectAssemblagePreferenceListProperty == null) {
         this.dialectAssemblagePreferenceListProperty = new SimpleObjectProperty<>(this,
               ObservableFields.DIALECT_ASSEMBLAGE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
               FXCollections.observableIntegerArray(getDialectAssemblagePreferenceList()));
         
         if (this.languageCoordinate instanceof LanguageCoordinateImpl) {
            addListenerReference(
             ((LanguageCoordinateImpl) this.languageCoordinate).setDialectAssemblagePreferenceListProperty(
                 this.dialectAssemblagePreferenceListProperty));
         } else if (this.languageCoordinate instanceof ObservableLanguageCoordinateImpl) {
            LanguageCoordinateImpl languageCoordinateImpl = 
                    (LanguageCoordinateImpl) ((ObservableLanguageCoordinateImpl) this.languageCoordinate).languageCoordinate;
            languageCoordinateImpl.setDialectAssemblagePreferenceListProperty(
                 this.dialectAssemblagePreferenceListProperty);
         }
         
      }

      return this.dialectAssemblagePreferenceListProperty;
   }

   /**
    * Language concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty languageConceptSequenceProperty() {
      if (this.languageConceptSequenceProperty == null) {
         this.languageConceptSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.LANGUAGE_SEQUENCE_FOR_LANGUAGE_COORDINATE.toExternalString(),
               getLanguageConceptSequence());
         if (this.languageCoordinate instanceof LanguageCoordinateImpl) {
            addListenerReference(
             ((LanguageCoordinateImpl) this.languageCoordinate).setLanguageConceptSequenceProperty(
                 this.languageConceptSequenceProperty));
         } else if (this.languageCoordinate instanceof ObservableLanguageCoordinateImpl) {
            LanguageCoordinateImpl languageCoordinateImpl = 
                    (LanguageCoordinateImpl) ((ObservableLanguageCoordinateImpl) this.languageCoordinate).languageCoordinate;
            languageCoordinateImpl.setLanguageConceptSequenceProperty(
                 this.languageConceptSequenceProperty);
         }
      }
      return this.languageConceptSequenceProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableLanguageCoordinateImpl{" + this.languageCoordinate + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description.
    *
    * @param descriptionList the description list
    * @param stampCoordinate the stamp coordinate
    * @return the description
    */
   @Override
   public LatestVersion<DescriptionVersion> getDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return this.languageCoordinate.getDescription(descriptionList, stampCoordinate);
   }

   /**
    * Gets the description type preference list.
    *
    * @return the description type preference list
    */
   @Override
   public int[] getDescriptionTypePreferenceList() {
      if (this.descriptionTypePreferenceListProperty != null) {
         return this.descriptionTypePreferenceListProperty.get()
               .toArray(new int[2]);
      }

      return this.languageCoordinate.getDescriptionTypePreferenceList();
   }

   /**
    * Gets the dialect assemblage preference list.
    *
    * @return the dialect assemblage preference list
    */
   @Override
   public int[] getDialectAssemblagePreferenceList() {
      if (this.dialectAssemblagePreferenceListProperty != null) {
         return this.dialectAssemblagePreferenceListProperty.get()
               .toArray(new int[2]);
      }

      return this.languageCoordinate.getDialectAssemblagePreferenceList();
   }

   /**
    * Gets the fully specified description.
    *
    * @param descriptionList the description list
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   @Override
   public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return this.languageCoordinate.getFullySpecifiedDescription(descriptionList, stampCoordinate);
   }

   /**
    * Gets the language concept sequence.
    *
    * @return the language concept sequence
    */
   @Override
   public int getLanguageConceptSequence() {
      if (this.languageConceptSequenceProperty != null) {
         return this.languageConceptSequenceProperty.get();
      }

      return this.languageCoordinate.getLanguageConceptSequence();
   }

   /**
    * Gets the preferred description.
    *
    * @param descriptionList the description list
    * @param stampCoordinate the stamp coordinate
    * @return the preferred description
    */
   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return this.languageCoordinate.getPreferredDescription(descriptionList, stampCoordinate);
   }

   @Override
   public ObservableLanguageCoordinateImpl deepClone() {
      return new ObservableLanguageCoordinateImpl(languageCoordinate.deepClone());
   }
   
}

