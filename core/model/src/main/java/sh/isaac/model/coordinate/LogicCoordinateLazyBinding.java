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

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LogicCoordinateLazyBinding.
 *
 * @author kec
 */
@XmlRootElement(name = "logicCoordinateLazyBinding")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogicCoordinateLazyBinding
        extends LogicCoordinateImpl {
   /** The stated assemblage proxy. */
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification statedAssemblageProxy = null;

   /** The inferred assemblage proxy. */
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification inferredAssemblageProxy = null;

   /** The description logic profile proxy. */
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification descriptionLogicProfileProxy = null;

   /** The classifier proxy. */
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification classifierProxy = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new logic coordinate lazy binding.
    */
   private LogicCoordinateLazyBinding() {
      // for jaxb
   }

   /**
    * Instantiates a new logic coordinate lazy binding.
    *
    * @param statedAssemblageProxy the stated assemblage proxy
    * @param inferredAssemblageProxy the inferred assemblage proxy
    * @param descriptionLogicProfileProxy the description logic profile proxy
    * @param classifierProxy the classifier proxy
    */
   public LogicCoordinateLazyBinding(ConceptSpecification statedAssemblageProxy,
                                     ConceptSpecification inferredAssemblageProxy,
                                     ConceptSpecification descriptionLogicProfileProxy,
                                     ConceptSpecification classifierProxy) {
      super(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
      this.statedAssemblageProxy        = statedAssemblageProxy;
      this.inferredAssemblageProxy      = inferredAssemblageProxy;
      this.descriptionLogicProfileProxy = descriptionLogicProfileProxy;
      this.classifierProxy              = classifierProxy;
   }

   //~--- methods -------------------------------------------------------------

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

      // Do not compare object classes, a LogicCoordinateImpl from one impl should be able to be equal to another impl...
      final LogicCoordinate other = (LogicCoordinate) obj;

      if (this.getStatedAssemblageSequence() != other.getStatedAssemblageSequence()) {
         return false;
      }

      if (this.getInferredAssemblageSequence() != other.getInferredAssemblageSequence()) {
         return false;
      }

      if (this.getDescriptionLogicProfileSequence() != other.getDescriptionLogicProfileSequence()) {
         return false;
      }

      return this.getClassifierSequence() == other.getClassifierSequence();
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 5;

      hash = 83 * hash + this.getStatedAssemblageSequence();
      hash = 83 * hash + this.getInferredAssemblageSequence();
      hash = 83 * hash + this.getDescriptionLogicProfileSequence();
      hash = 83 * hash + this.getClassifierSequence();
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "LogicCoordinateLazyBinding{" + "statedAssemblageProxy=" + this.statedAssemblageProxy +
             ", inferredAssemblageProxy=" + this.inferredAssemblageProxy + ", descriptionLogicProfileProxy=" +
             this.descriptionLogicProfileProxy + ", classifierProxy=" + this.classifierProxy + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the classifier sequence.
    *
    * @return the classifier sequence
    */
   @Override
   public int getClassifierSequence() {
      if (this.classifierSequence == Integer.MAX_VALUE) {
         this.classifierSequence = this.classifierProxy.getConceptSequence();
      }

      return this.classifierSequence;
   }

   /**
    * Gets the description logic profile sequence.
    *
    * @return the description logic profile sequence
    */
   @Override
   public int getDescriptionLogicProfileSequence() {
      if (this.descriptionLogicProfileSequence == Integer.MAX_VALUE) {
         this.descriptionLogicProfileSequence = this.descriptionLogicProfileProxy.getConceptSequence();
      }

      return this.descriptionLogicProfileSequence;
   }

   /**
    * Gets the inferred assemblage sequence.
    *
    * @return the inferred assemblage sequence
    */
   @Override
   public int getInferredAssemblageSequence() {
      if (this.inferredAssemblageSequence == Integer.MAX_VALUE) {
         this.inferredAssemblageSequence = this.inferredAssemblageProxy.getConceptSequence();
      }

      return this.inferredAssemblageSequence;
   }

   /**
    * Gets the stated assemblage sequence.
    *
    * @return the stated assemblage sequence
    */
   @Override
   public int getStatedAssemblageSequence() {
      if (this.statedAssemblageSequence == Integer.MAX_VALUE) {
         this.statedAssemblageSequence = this.statedAssemblageProxy.getConceptSequence();
      }

      return this.statedAssemblageSequence;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class ConceptProxyAdapter.
    */
   private static class ConceptProxyAdapter
           extends XmlAdapter<UUID[], ConceptProxy> {
      /**
       * Marshal.
       *
       * @param c the c
       * @return the UUI d[]
       */
      @Override
      public UUID[] marshal(ConceptProxy c) {
         return c.getUuidList()
                 .toArray(new UUID[c.getUuidList().size()]);
      }

      /**
       * Unmarshal.
       *
       * @param v the v
       * @return the concept proxy
       * @throws Exception the exception
       */
      @Override
      public ConceptProxy unmarshal(UUID[] v)
               throws Exception {
         return new ConceptProxy("", v);
      }
   }
}

