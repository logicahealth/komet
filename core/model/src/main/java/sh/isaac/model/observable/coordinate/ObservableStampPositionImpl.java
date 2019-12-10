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
import java.time.Instant;
import javafx.beans.InvalidationListener;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.component.concept.ConceptSpecification;

import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPositionImpl.
 *
 * @author kec
 */
public class ObservableStampPositionImpl
        extends ObservableCoordinateImpl
         implements ObservableStampPosition {
   /** The stamp position. */
   StampPositionImpl stampPosition;

   /** The time property. */
   LongProperty timeProperty;

   /** The stamp path nid property. */
    SimpleObjectProperty<ConceptSpecification> stampPathConceptSpecificationProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable stamp position impl.
    *
    * @param stampPosition the stamp position
    */
   public ObservableStampPositionImpl(StampPosition stampPosition) {
      if (stampPosition instanceof ObservableStampPositionImpl) {
         this.stampPosition = ((ObservableStampPositionImpl) stampPosition).stampPosition;
      } else {
         this.stampPosition = (StampPositionImpl) stampPosition;
      }
   }

   //~--- methods -------------------------------------------------------------

   public StampPositionImpl getStampPosition() {
      return stampPosition;
   }

    /**
     * Stamp path nid property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<ConceptSpecification> stampPathConceptSpecificationProperty() {
        if (this.stampPathConceptSpecificationProperty == null) {
            this.stampPathConceptSpecificationProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.PATH_NID_FOR_STAMP_POSITION.toExternalString(),
                    getStampPathSpecification());
            addListenerReference(this.stampPosition.setStampPathConceptSpecificationProperty(this.stampPathConceptSpecificationProperty));
            this.stampPathConceptSpecificationProperty.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
        }
        
        return this.stampPathConceptSpecificationProperty;
    }

   /**
    * Time property.
    *
    * @return the long property
    */
   @Override
   public LongProperty timeProperty() {
      if (this.timeProperty == null) {
         this.timeProperty = new SimpleLongProperty(this,
               ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
               getTime());
         addListenerReference(this.stampPosition.setTimeProperty(this.timeProperty));
         this.timeProperty.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
      }

      return this.timeProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableStampPositionImpl{" + this.stampPosition + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stamp path.
    *
    * @return the stamp path
    */
   @Override
   public StampPath getStampPath() {
      return this.stampPosition.getStampPath();
   }

   /**
    * Gets the stamp path nid.
    *
    * @return the stamp path nid
    */
   @Override
   public ConceptSpecification getStampPathSpecification() {
      if (this.stampPathConceptSpecificationProperty != null) {
         return this.stampPathConceptSpecificationProperty.get();
      }

      return this.stampPosition.getStampPathSpecification();
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public long getTime() {
      if (this.timeProperty != null) {
         return this.timeProperty.get();
      }

      return this.stampPosition.getTime();
   }

   /**
    * Gets the time as instant.
    *
    * @return the time as instant
    */
   @Override
   public Instant getTimeAsInstant() {
      if (this.timeProperty != null) {
         return Instant.ofEpochMilli(this.timeProperty.get());
      }

      return this.stampPosition.getTimeAsInstant();
   }
   
   
   @Override
   public ObservableStampPositionImpl deepClone() {
      return new ObservableStampPositionImpl(stampPosition.deepClone());
   }
   
   @Override
   public int hashCode() {
      return this.stampPosition.hashCode();
   }
   
   @Override
   public boolean equals(Object obj) {
      return this.stampPosition.equals(obj);
   }
}

