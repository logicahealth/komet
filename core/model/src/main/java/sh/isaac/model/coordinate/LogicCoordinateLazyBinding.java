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
 *
 * @author kec
 */
@XmlRootElement(name = "logicCoordinateLazyBinding")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogicCoordinateLazyBinding
        extends LogicCoordinateImpl {
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification statedAssemblageProxy        = null;
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification inferredAssemblageProxy      = null;
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification descriptionLogicProfileProxy = null;
   @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
   private ConceptSpecification classifierProxy              = null;

   //~--- constructors --------------------------------------------------------

   private LogicCoordinateLazyBinding() {
      // for jaxb
   }

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

   @Override
   public int hashCode() {
      int hash = 5;

      hash = 83 * hash + this.getStatedAssemblageSequence();
      hash = 83 * hash + this.getInferredAssemblageSequence();
      hash = 83 * hash + this.getDescriptionLogicProfileSequence();
      hash = 83 * hash + this.getClassifierSequence();
      return hash;
   }

   @Override
   public String toString() {
      return "LogicCoordinateLazyBinding{" + "statedAssemblageProxy=" + this.statedAssemblageProxy +
             ", inferredAssemblageProxy=" + this.inferredAssemblageProxy + ", descriptionLogicProfileProxy=" +
             this.descriptionLogicProfileProxy + ", classifierProxy=" + this.classifierProxy + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getClassifierSequence() {
      if (this.classifierSequence == Integer.MAX_VALUE) {
         this.classifierSequence = this.classifierProxy.getConceptSequence();
      }

      return this.classifierSequence;
   }

   @Override
   public int getDescriptionLogicProfileSequence() {
      if (this.descriptionLogicProfileSequence == Integer.MAX_VALUE) {
         this.descriptionLogicProfileSequence = this.descriptionLogicProfileProxy.getConceptSequence();
      }

      return this.descriptionLogicProfileSequence;
   }

   @Override
   public int getInferredAssemblageSequence() {
      if (this.inferredAssemblageSequence == Integer.MAX_VALUE) {
         this.inferredAssemblageSequence = this.inferredAssemblageProxy.getConceptSequence();
      }

      return this.inferredAssemblageSequence;
   }

   @Override
   public int getStatedAssemblageSequence() {
      if (this.statedAssemblageSequence == Integer.MAX_VALUE) {
         this.statedAssemblageSequence = this.statedAssemblageProxy.getConceptSequence();
      }

      return this.statedAssemblageSequence;
   }

   //~--- inner classes -------------------------------------------------------

   private static class ConceptProxyAdapter
           extends XmlAdapter<UUID[], ConceptProxy> {
      @Override
	public UUID[] marshal(ConceptProxy c) {
         return c.getUuidList()
                 .toArray(new UUID[c.getUuidList().size()]);
      }

      @Override
      public ConceptProxy unmarshal(UUID[] v)
               throws Exception {
         return new ConceptProxy("", v);
      }
   }
}

