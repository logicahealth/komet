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
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableTaxonomyCoordinate;
import sh.isaac.model.coordinate.TaxonomyCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableTaxonomyCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableTaxonomyCoordinate {
   TaxonomyCoordinateImpl                       taxonomyCoordinate;
   ObjectProperty<PremiseType>                  taxonomyTypeProperty;
   ObjectProperty<ObservableStampCoordinate>    stampCoordinateProperty;
   ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty;
   ObjectProperty<ObservableLogicCoordinate>    logicCoordinateProperty;
   ObjectProperty<UUID>                         uuidProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableTaxonomyCoordinateImpl(TaxonomyCoordinate taxonomyCoordinate) {
      this.taxonomyCoordinate = (TaxonomyCoordinateImpl) taxonomyCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty() {
      if (languageCoordinateProperty == null) {
         languageCoordinateProperty = new SimpleObjectProperty<>(this,
               ObservableFields.LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               new ObservableLanguageCoordinateImpl(taxonomyCoordinate.getLanguageCoordinate()));
      }

      return languageCoordinateProperty;
   }

   @Override
   public ObjectProperty<ObservableLogicCoordinate> logicCoordinateProperty() {
      if (logicCoordinateProperty == null) {
         logicCoordinateProperty = new SimpleObjectProperty<>(this,
               ObservableFields.LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               new ObservableLogicCoordinateImpl(taxonomyCoordinate.getLogicCoordinate()));
      }

      return logicCoordinateProperty;
   }

   @Override
   public ObservableTaxonomyCoordinate makeAnalog(long stampPositionTime) {
      return new ObservableTaxonomyCoordinateImpl(taxonomyCoordinate.makeAnalog(stampPositionTime));
   }

   @Override
   public TaxonomyCoordinate makeAnalog(PremiseType taxonomyType) {
      return new ObservableTaxonomyCoordinateImpl(taxonomyCoordinate.makeAnalog(taxonomyType));
   }

   @Override
   public ObservableTaxonomyCoordinate makeAnalog(State... state) {
      return new ObservableTaxonomyCoordinateImpl(taxonomyCoordinate.makeAnalog(state));
   }

   @Override
   public ObjectProperty<PremiseType> premiseTypeProperty() {
      if (taxonomyTypeProperty == null) {
         taxonomyTypeProperty = new SimpleObjectProperty<>(this,
               ObservableFields.PREMISE_TYPE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               taxonomyCoordinate.getTaxonomyType());
      }

      return taxonomyTypeProperty;
   }

   @Override
   public ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty() {
      if (stampCoordinateProperty == null) {
         stampCoordinateProperty = new SimpleObjectProperty<>(this,
               ObservableFields.STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE.toExternalString(),
               new ObservableStampCoordinateImpl(taxonomyCoordinate.getStampCoordinate()));
      }

      return stampCoordinateProperty;
   }

   @Override
   public String toString() {
      return "ObservableTaxonomyCoordinateImpl{" + taxonomyCoordinate + '}';
   }

   @Override
   public ObjectProperty<UUID> uuidProperty() {
      if (uuidProperty == null) {
         uuidProperty = new SimpleObjectProperty<>(this,
               ObservableFields.UUID_FOR_TAXONOMY_COORDINATE.toExternalString(),
               taxonomyCoordinate.getUuid());
      }

      return uuidProperty;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getIsaConceptSequence() {
      return taxonomyCoordinate.getIsaConceptSequence();
   }

   @Override
   public ObservableLanguageCoordinate getLanguageCoordinate() {
      return languageCoordinateProperty().get();
   }

   @Override
   public LogicCoordinate getLogicCoordinate() {
      return logicCoordinateProperty().get();
   }

   @Override
   public ObservableStampCoordinate getStampCoordinate() {
      return stampCoordinateProperty().get();
   }

   @Override
   public PremiseType getTaxonomyType() {
      return premiseTypeProperty().get();
   }

   @Override
   public UUID getUuid() {
      return uuidProperty().get();
   }
}

