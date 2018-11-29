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

import sh.isaac.api.observable.coordinate.ObservableCoordinateImpl;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.collections.FXCollections;
import sh.isaac.api.component.concept.ConceptSpecification;

import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.observable.coordinate.ObservableStampPath;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.coordinate.StampPathImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPathImpl.
 *
 * @author kec
 */
public class ObservableStampPathImpl
        extends ObservableCoordinateImpl
         implements ObservableStampPath {
   /** The stamp path. */
   StampPathImpl stampPath;

   /** The path concept nid property. */
   ReadOnlyIntegerProperty pathConceptSequenceProperty;

   /** The path origins property. */
   ReadOnlyListWrapper<ObservableStampPosition> pathOriginsProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable stamp path impl.
    *
    * @param stampPath the stamp path
    */
   public ObservableStampPathImpl(StampPath stampPath) {
      this.stampPath = (StampPathImpl) stampPath;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(StampPath o) {
      return this.stampPath.compareTo(o);
   }

   /**
    * Path concept nid property.
    *
    * @return the read only integer property
    */
   @Override
   public ReadOnlyIntegerProperty pathConceptNidProperty() {
      if (this.pathConceptSequenceProperty == null) {
         this.pathConceptSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_NID_FOR_STAMP_PATH.toExternalString(),
               getPathConceptNid());
         this.pathConceptSequenceProperty.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
      }

      return this.pathConceptSequenceProperty;
   }

   /**
    * Path origins property.
    *
    * @return the read only list property
    */
   @Override
   public ReadOnlyListProperty<ObservableStampPosition> pathOriginsProperty() {
      if (this.pathOriginsProperty == null) {
         this.pathOriginsProperty = new ReadOnlyListWrapper<>(this,
               ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(),
               FXCollections.<ObservableStampPosition>observableList(getPathOrigins()));
         this.pathOriginsProperty.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
      }

      return this.pathOriginsProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableStampPathImpl{" + this.stampPath + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the path concept nid.
    *
    * @return the path concept nid
    */
   @Override
   public int getPathConceptNid() {
      if (this.pathConceptSequenceProperty != null) {
         return this.pathConceptSequenceProperty.get();
      }

      return this.stampPath.getPathConceptNid();
   }

   /**
    * Gets the path origins.
    *
    * @return the path origins
    */
   @Override
   public List<ObservableStampPosition> getPathOrigins() {
      if (this.pathOriginsProperty != null) {
         return this.pathOriginsProperty.get();
      }

      return this.stampPath.getPathOrigins()
                           .stream()
                           .map((origin) -> new ObservableStampPositionImpl(origin))
                           .collect(Collectors.toList());
   }

    @Override
    public ConceptSpecification getPathConcept() {
        return this.stampPath.getPathConcept();
    }
}

