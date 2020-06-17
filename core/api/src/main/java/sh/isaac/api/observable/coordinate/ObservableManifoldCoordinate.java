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

import javafx.beans.property.Property;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableManifoldCoordinate.
 *
 * @author kec
 */
public interface ObservableManifoldCoordinate
        extends ManifoldCoordinate, ObservableCoordinate<ManifoldCoordinateImmutable> {

   default Property<?>[] getBaseProperties() {
      return new Property<?>[] {};
   }

   default ObservableCoordinate<?>[] getCompositeCoordinates() {
      return new ObservableCoordinate<?>[]{
              getDigraph()
      };
   }

   @Override
   ObservableDigraphCoordinate getDigraph();

   @Override
   default ObservableLogicCoordinate getLogicCoordinate() {
      return getDigraph().getLogicCoordinate();
   }

   @Override
   default ObservableLanguageCoordinate getLanguageCoordinate() {
      return getDigraph().getLanguageCoordinate();
   }

   /**
    *
    * @return the digraph coordinate property.
    */
   ObjectProperty<DigraphCoordinateImmutable> digraphCoordinateImmutableProperty();

   /**
    * In most cases all stamp filters will be the same.
    * @return the digraph vertex stamp filter services as the default stamp filter.
    */
   default ObservableStampFilter getLanguageStampFilter() {
      return getDigraph().getLanguageStampFilter();
   }

   default ObservableStampFilter getVertexStampFilter() {
      return getDigraph().getVertexStampFilter();
   }

   default ObservableStampFilter getEdgeStampFilter() {
      return getDigraph().getEdgeStampFilter();
   }

   default void changeManifoldPath(ConceptSpecification pathConcept) {
      getEdgeStampFilter().pathConceptProperty().set(pathConcept);
      getLanguageStampFilter().pathConceptProperty().set(pathConcept);
      getVertexStampFilter().pathConceptProperty().set(pathConcept);
   }

   /**
    * Will change all contained paths (vertex, edge, and language), to the provided path.
    */
   default void changeManifoldPath(int pathConceptNid) {
      changeManifoldPath(Get.concept(pathConceptNid));
   }

}

