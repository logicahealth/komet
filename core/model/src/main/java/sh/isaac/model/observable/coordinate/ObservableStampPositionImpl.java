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

import java.time.Instant;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableStampPositionImpl
        extends ObservableCoordinateImpl
         implements ObservableStampPosition {
   StampPositionImpl stampPosition;
   LongProperty      timeProperty;
   IntegerProperty   stampPathSequenceProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableStampPositionImpl(StampPosition stampPosition) {
      if (stampPosition instanceof ObservableStampPositionImpl) {
         this.stampPosition = ((ObservableStampPositionImpl) stampPosition).stampPosition;
      } else {
         this.stampPosition = (StampPositionImpl) stampPosition;
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty stampPathSequenceProperty() {
      if (stampPathSequenceProperty == null) {
         stampPathSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_SEQUENCE_FOR_STAMP_POSITION.toExternalString(),
               getStampPathSequence());
         addListenerReference(stampPosition.setStampPathSequenceProperty(stampPathSequenceProperty));
      }

      return stampPathSequenceProperty;
   }

   @Override
   public LongProperty timeProperty() {
      if (timeProperty == null) {
         timeProperty = new SimpleLongProperty(this,
               ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
               getTime());
         addListenerReference(stampPosition.setTimeProperty(timeProperty));
      }

      return timeProperty;
   }

   @Override
   public String toString() {
      return "ObservableStampPositionImpl{" + stampPosition + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public StampPath getStampPath() {
      return stampPosition.getStampPath();
   }

   @Override
   public int getStampPathSequence() {
      if (stampPathSequenceProperty != null) {
         return stampPathSequenceProperty.get();
      }

      return stampPosition.getStampPathSequence();
   }

   @Override
   public long getTime() {
      if (timeProperty != null) {
         return timeProperty.get();
      }

      return stampPosition.getTime();
   }

   @Override
   public Instant getTimeAsInstant() {
      if (timeProperty != null) {
         return Instant.ofEpochMilli(timeProperty.get());
      }

      return stampPosition.getTimeAsInstant();
   }
}

