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

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.model.xml.StampPathAdaptor;
import sh.isaac.model.xml.StampTimeAdaptor;

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampPositionImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "StampPosition")
@XmlAccessorType(XmlAccessType.NONE)
public class StampPositionImpl
         implements StampPosition, Comparable<StampPosition> {
   /** The time. */
   long time;

   /** The stamp path nid. */
   ConceptSpecification stampPathConceptSpecification;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new stamp position impl.
    */
   private StampPositionImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new stamp position impl.
    *
    * @param time the time
    * @param stampPathNid the stamp path nid
    */
   public StampPositionImpl(long time, int stampPathNid) {
      this.time              = time;
      this.stampPathConceptSpecification = Get.conceptSpecification(stampPathNid);
   }

   public StampPositionImpl(long time, ConceptSpecification stampPathConceptSpecification) {
      this.time              = time;
      this.stampPathConceptSpecification = stampPathConceptSpecification;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(StampPosition o) {
       int diff = Integer.compare(this.stampPathConceptSpecification.getNid(), 
               o.getStampPathSpecification().getNid());
      if (diff != 0) {
         return diff;
      }

      return Long.compare(this.time, o.getTime());
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
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

      return this.stampPathConceptSpecification.getNid() == other.stampPathConceptSpecification.getNid();
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 7;

      hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
      hash = 83 * hash + this.stampPathConceptSpecification.getPrimordialUuid().hashCode();
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
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
        .append(Get.conceptDescriptionText(this.stampPathConceptSpecification))
        .append("' path}");
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stamp path.
    *
    * @return the stamp path
    */
    @Override
    public StampPath getStampPath() {
      return new StampPathImpl(this.stampPathConceptSpecification);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   @XmlElement(name = "path")
   public ConceptSpecification getStampPathSpecification() {
      return this.stampPathConceptSpecification;
   }

   public void setStampPathSpecification(ConceptSpecification stampPathConceptSpecification) {
      this.stampPathConceptSpecification = stampPathConceptSpecification;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stamp path nid property.
    *
     * @param stampPathConceptSpecificationProperty
    * @return the change listener
    */
   public ChangeListener<ConceptSpecification> setStampPathConceptSpecificationProperty(
           SimpleObjectProperty<ConceptSpecification> stampPathConceptSpecificationProperty) {
      final ChangeListener<ConceptSpecification> listener = (ObservableValue<? extends ConceptSpecification> observable,
                                               ConceptSpecification oldValue,
                                               ConceptSpecification newValue) -> {
               this.stampPathConceptSpecification = newValue;
            };

      stampPathConceptSpecificationProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public long getTime() {
      return this.time;
   }

   @XmlElement(name = "time")
   @XmlJavaTypeAdapter(StampTimeAdaptor.class)
   public Long getLongTime() {
      return this.time;
   }

   public void setLongTime(Long time) {
      this.time = time;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set time property.
    *
    * @param timeProperty the time property
    * @return the change listener
    */
   public ChangeListener<Number> setTimeProperty(LongProperty timeProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.time = newValue.longValue();
            };

      timeProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   @Override
   public StampPosition deepClone() {
      return new StampPositionImpl(time, stampPathConceptSpecification);
   }
   
   
}

