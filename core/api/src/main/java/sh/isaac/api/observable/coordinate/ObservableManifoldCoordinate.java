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



package sh.isaac.api.observable.coordinate;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;

import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableManifoldCoordinate.
 *
 * @author kec
 */
public interface ObservableManifoldCoordinate
        extends ManifoldCoordinate, ObservableCoordinate {
   /**
    * Language coordinate property.
    *
    * @return the object property
    */
   ObjectProperty<ObservableLanguageCoordinate> languageCoordinateProperty();

   /**
    * Logic coordinate property.
    *
    * @return the object property
    */
   ObjectProperty<ObservableLogicCoordinate> logicCoordinateProperty();

   /**
    * Premise type property.
    *
    * @return the object property
    */
   ObjectProperty<PremiseType> taxonomyPremiseTypeProperty();

   /**
    * Stamp coordinate property.
    *
    * @return the object property
    */
   ObjectProperty<ObservableStampCoordinate> stampCoordinateProperty();

   /**
    * 
    * @return an observable coordinate, instead of the simple stamp coordinate
    */
   @Override
   public ObservableStampCoordinate getStampCoordinate();
   
   /**
    * @see sh.isaac.api.coordinate.ManifoldCoordinate#getDestinationStampCoordinate()
    */
   @Override
   public ObservableStampCoordinate getDestinationStampCoordinate();
   
   /**
    * @return An observable version of {@link #getDestinationStampCoordinate()}
    */
   public ObjectProperty<ObservableStampCoordinate> destinationStampCoordinateProperty();

   /**
    * 
    * @return an observable coordinate, instead of the simple language coordinate
    */
   @Override
   public ObservableLanguageCoordinate getLanguageCoordinate();

   /**
    * 
    * @return an observable coordinate, instead of the simple logic coordinate
    */
   @Override
   public ObservableLogicCoordinate getLogicCoordinate();
   
   
   @Override
   public ObservableManifoldCoordinate deepClone();
}

