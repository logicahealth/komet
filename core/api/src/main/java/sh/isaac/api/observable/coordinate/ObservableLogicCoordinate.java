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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import sh.isaac.api.component.concept.ConceptSpecification;

import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.LogicCoordinateImmutable;
import sh.isaac.api.coordinate.LogicCoordinateProxy;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableLogicCoordinate.
 *
 * @author kec
 */
public interface ObservableLogicCoordinate
        extends LogicCoordinateProxy, ObservableCoordinate<LogicCoordinateImmutable> {

   default Property<?>[] getBaseProperties() {
      return new Property<?>[] {
              classifierProperty(),
              conceptAssemblageProperty(),
              descriptionLogicProfileProperty(),
              inferredAssemblageProperty(),
              statedAssemblageProperty(),
              digraphIdentityProperty()
      };
   }

   default ObservableCoordinate<?>[] getCompositeCoordinates() {
      return new ObservableCoordinate<?>[]{};
   }

    /**
     * 
     * @return the logic coordinate that this observable wraps. 
     */
   LogicCoordinate getLogicCoordinate();

   /**
    * Classifier property.
    *
    * @return the classifier concept property. 
    */
   ObjectProperty<ConceptSpecification> classifierProperty();

   /**
    * Concept assemblage property.
    *
    * @return the assemblage concept property. 
    */
   ObjectProperty<ConceptSpecification> conceptAssemblageProperty();

   /**
    * Description logic profile property.
    *
    * @return the description logic profile concept property. 
    */
   ObjectProperty<ConceptSpecification> descriptionLogicProfileProperty();

   /**
    * Inferred assemblage property.
    *
    * @return the inferred assemblage concept property. 
    */
   ObjectProperty<ConceptSpecification> inferredAssemblageProperty();

   /**
    * Stated assemblage property.
    *
    * @return the stated assemblage concept property. 
    */
   ObjectProperty<ConceptSpecification> statedAssemblageProperty();

   /**
    * digraph identity property.
    *
    * @return the digraph identity property.
    */
   ObjectProperty<ConceptSpecification> digraphIdentityProperty();


}

