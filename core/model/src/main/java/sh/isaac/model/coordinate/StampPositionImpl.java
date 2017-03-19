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



package sh.isaac.model.coordinate;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@XmlRootElement(name = "stampPosition")
@XmlAccessorType(XmlAccessType.FIELD)
public class StampPositionImpl
         implements StampPosition, Comparable<StampPosition> {
   long time;
   int  stampPathSequence;

   //~--- constructors --------------------------------------------------------

   private StampPositionImpl() {
      // for jaxb
   }

   public StampPositionImpl(long time, int stampPathSequence) {
      this.time              = time;
      this.stampPathSequence = Get.identifierService()
                                  .getConceptSequence(stampPathSequence);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(StampPosition o) {
      if (this.stampPathSequence != o.getStampPathSequence()) {
         return Integer.compare(this.stampPathSequence, o.getStampPathSequence());
      }

      return Long.compare(this.time, o.getTime());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final StampPositionImpl other = (StampPositionImpl) obj;

      if (this.time != other.time) {
         return false;
      }

      return this.stampPathSequence == other.stampPathSequence;
   }

   @Override
   public int hashCode() {
      int hash = 7;

      hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
      hash = 83 * hash + this.stampPathSequence;
      return hash;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("StampPosition:{");

      if (this.time == Long.MAX_VALUE) {
         sb.append("latest");
      } else if (this.time == Long.MIN_VALUE) {
         sb.append("CANCELED");
      } else {
         sb.append(getTimeAsInstant());
      }

      sb.append(" on '")
        .append(Get.conceptDescriptionText(this.stampPathSequence))
        .append("' path}");
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public StampPath getStampPath() {
      return new StampPathImpl(this.stampPathSequence);
   }

   @Override
   public int getStampPathSequence() {
      return this.stampPathSequence;
   }

   //~--- set methods ---------------------------------------------------------

   public ChangeListener<Number> setStampPathSequenceProperty(IntegerProperty stampPathSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.stampPathSequence = newValue.intValue();
                                        };

      stampPathSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public long getTime() {
      return this.time;
   }

   //~--- set methods ---------------------------------------------------------

   public ChangeListener<Number> setTimeProperty(LongProperty timeProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.time = newValue.longValue();
                                        };

      timeProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
}

