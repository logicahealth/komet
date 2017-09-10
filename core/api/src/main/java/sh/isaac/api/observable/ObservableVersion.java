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



package sh.isaac.api.observable;

//~--- non-JDK imports --------------------------------------------------------

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import sh.isaac.api.ConceptProxy;

import sh.isaac.api.State;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableVersion.
 *
 * @author kec
 */
public interface ObservableVersion
        extends Version {
   /**
    * Author sequence property.
    *
    * @return the integer property
    */
   IntegerProperty authorSequenceProperty();

   /**
    * Commit state property.
    *
    * @return the object property
    */
   ObjectProperty<CommitStates> commitStateProperty();

   /**
    * Module sequence property.
    *
    * @return the integer property
    */
   IntegerProperty moduleSequenceProperty();

   /**
    * Path sequence property.
    *
    * @return the integer property
    */
   IntegerProperty pathSequenceProperty();

   /**
    * Stamp sequence property.
    *
    * @return the integer property
    */
   IntegerProperty stampSequenceProperty();

   /**
    * State property.
    *
    * @return the object property
    */
   ObjectProperty<State> stateProperty();

   /**
    * Time property.
    *
    * @return the long property
    */
   LongProperty timeProperty();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   ObservableChronology getChronology();
   
   /**
    * 
    * @return a list of properties for this observable version
    */
   List<Property<?>> getProperties();
   
   default Map<ConceptSpecification, Property<?>> getPropertyMap() {
      Map<ConceptSpecification, Property<?>> propertyMap = new HashMap<>();
      getProperties().forEach((property) -> propertyMap.put(new ConceptProxy(property.getName()), property));
      return propertyMap;
   }
}

