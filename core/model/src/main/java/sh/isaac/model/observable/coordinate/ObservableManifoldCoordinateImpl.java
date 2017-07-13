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

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.State;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.model.coordinate.ManifoldCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableManifoldCoordinateImpl.
 *
 * @author kec
 */
public class ObservableManifoldCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableManifoldCoordinate {
   /** The manifold coordinate. */
   ManifoldCoordinateImpl manifoldCoordinate;

   /** The taxonomy type property. */
   ObjectProperty<PremiseType> taxonomyTypeProperty;

   /** The stamp coordinate property. */
   ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty;

   /** The language coordinate property. */
   ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty;

   /** The logic coordinate property. */
   ObjectProperty<ObservableLogicCoordinate> logicCoordinateProperty;

   /** The uuid property. */
   ObjectProperty<UUID> uuidProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable taxonomy coordinate impl.
    *
    * @param manifoldCoordinate the taxonomy coordinate
    */
   public ObservableManifoldCoordinateImpl(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = (ManifoldCoordinateImpl) manifoldCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Language coordinate property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty() {
      
      
      if (this.languageCoordinateProperty == null) {
         this.languageCoordinateProperty = new SimpleObjectProperty<>(this,
               ObservableFields.LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               new ObservableLanguageCoordinateImpl(this.manifoldCoordinate.getLanguageCoordinate()));
      }

      return this.languageCoordinateProperty;
   }

   /**
    * Logic coordinate property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<ObservableLogicCoordinate> logicCoordinateProperty() {
      if (this.logicCoordinateProperty == null) {
         this.logicCoordinateProperty = new SimpleObjectProperty<>(this,
               ObservableFields.LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               new ObservableLogicCoordinateImpl(this.manifoldCoordinate.getLogicCoordinate()));
      }

      return this.logicCoordinateProperty;
   }

   /**
    * Make analog.
    *
    * @param stampPositionTime the stamp position time
    * @return the observable taxonomy coordinate
    */
   @Override
   public ObservableManifoldCoordinate makeCoordinateAnalog(long stampPositionTime) {
      return new ObservableManifoldCoordinateImpl(this.manifoldCoordinate.makeCoordinateAnalog(stampPositionTime));
   }

   /**
    * Make analog.
    *
    * @param taxonomyType the taxonomy type
    * @return the taxonomy coordinate
    */
   @Override
   public ManifoldCoordinate makeCoordinateAnalog(PremiseType taxonomyType) {
      return new ObservableManifoldCoordinateImpl(this.manifoldCoordinate.makeCoordinateAnalog(taxonomyType));
   }

   /**
    * Make analog.
    *
    * @param state the state
    * @return the observable taxonomy coordinate
    */
   @Override
   public ObservableManifoldCoordinate makeCoordinateAnalog(State... state) {
      return new ObservableManifoldCoordinateImpl(this.manifoldCoordinate.makeCoordinateAnalog(state));
   }

   /**
    * Premise type property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<PremiseType> premiseTypeProperty() {
      if (this.taxonomyTypeProperty == null) {
         this.taxonomyTypeProperty = new SimpleObjectProperty<>(this,
               ObservableFields.PREMISE_TYPE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               this.manifoldCoordinate.getTaxonomyType());
      }

      return this.taxonomyTypeProperty;
   }

   /**
    * Stamp coordinate property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty() {
      if (this.stampCoordinateProperty == null) {
         this.stampCoordinateProperty = new SimpleObjectProperty<>(this,
               ObservableFields.STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               new ObservableStampCoordinateImpl(this.manifoldCoordinate.getStampCoordinate()));
      }

      return this.stampCoordinateProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableManifoldCoordinateImpl{" + this.manifoldCoordinate + '}';
   }

   /**
    * Uuid property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<UUID> uuidProperty() {
      if (this.uuidProperty == null) {
         this.uuidProperty = new SimpleObjectProperty<>(this,
               ObservableFields.UUID_FOR_TAXONOMY_COORDINATE.toExternalString(),
               this.manifoldCoordinate.getCoordinateUuid());
      }

      return this.uuidProperty;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the isa concept sequence.
    *
    * @return the isa concept sequence
    */
   @Override
   public int getIsaConceptSequence() {
      return this.manifoldCoordinate.getIsaConceptSequence();
   }

   /**
    * Gets the language coordinate.
    *
    * @return the language coordinate
    */
   @Override
   public ObservableLanguageCoordinate getLanguageCoordinate() {
      return languageCoordinateProperty().get();
   }

   /**
    * Gets the logic coordinate.
    *
    * @return the logic coordinate
    */
   @Override
   public ObservableLogicCoordinate getLogicCoordinate() {
      return logicCoordinateProperty().get();
   }

   /**
    * Gets the stamp coordinate.
    *
    * @return the stamp coordinate
    */
   @Override
   public ObservableStampCoordinate getStampCoordinate() {
      return stampCoordinateProperty().get();
   }

   /**
    * Gets the taxonomy type.
    *
    * @return the taxonomy type
    */
   @Override
   public PremiseType getTaxonomyType() {
      return premiseTypeProperty().get();
   }

   /**
    * Gets the uuid.
    *
    * @return the uuid
    */
   @Override
   public UUID getCoordinateUuid() {
      return uuidProperty().get();
   }
}

