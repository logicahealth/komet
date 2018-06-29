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
import java.util.Optional;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.ConceptProxy;

import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableVersion.
 *
 * @author kec
 */
public interface ObservableVersion
        extends Version {
   /**
    * Author Nid property.
    *
    * @return the integer property
    */
   IntegerProperty authorNidProperty();

   /**
    * Commit state property.
    *
    * @return the object property
    */
   ObjectProperty<CommitStates> commitStateProperty();

   /**
    * Module Nid property.
    *
    * @return the integer property
    */
   IntegerProperty moduleNidProperty();

   /**
    * Path Nid property.
    *
    * @return the integer property
    */
   IntegerProperty pathNidProperty();

   /**
    * Stamp sequence property.
    *
    * @return the integer property
    */
   ReadOnlyIntegerProperty stampSequenceProperty();

   /**
    * Status property.
    *
    * @return the object property
    */
   ObjectProperty<Status> stateProperty();

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
   List<ReadOnlyProperty<?>> getProperties();
   
   List<Property<?>> getEditableProperties();

   default Map<ConceptSpecification, ReadOnlyProperty<?>> getPropertyMap() {
      Map<ConceptSpecification, ReadOnlyProperty<?>> propertyMap = new HashMap<>();
      getProperties().forEach((property) -> propertyMap.put(new ConceptProxy(property.getName()), property));
      return propertyMap;
   }
   
   /**
    * Method supporting a general purpose ability to associate an api user's objects with versions. 
    * @param <T>
    * @param objectKey
    * @return 
    */
   <T extends Object> Optional<T> getUserObject(String objectKey);
   
   /**
    * Method supporting a general purpose ability to associate an api user's objects with versions. 
    * @param objectKey
    * @param object 
    */
   void putUserObject(String objectKey, Object object);
   
   /**
    * Method supporting a general purpose ability to associate an api user's objects with versions. 
    * @param <T>
    * @param objectKey
    * @return 
    */
   <T extends Object> Optional<T> removeUserObject(String objectKey);
   
   <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec);
   
   /**
    * 
    * @return an independent chronicle that has this 
    * version as a member of it's version list.
    */
   Chronology createIndependentChronicle();

   Chronology createChronologyForCommit(int stampSequence);

}

