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

import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.ConceptProxy;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LogicCoordinateImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "logicCoordinate")
@XmlAccessorType(XmlAccessType.NONE)
public class LogicCoordinateImpl
         implements LogicCoordinate {
   /** The stated assemblage nid. */
   protected ConceptSpecification statedAssemblage;

   /** The inferred assemblage nid. */
   protected ConceptSpecification inferredAssemblage;

   /** The description logic profile nid. */
   protected ConceptSpecification descriptionLogicProfile;

   /** The classifier nid. */
   protected ConceptSpecification classifier;

   /** The concept assemblage nid. */
   protected ConceptSpecification conceptAssemblage;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new logic coordinate impl.
    */
   protected LogicCoordinateImpl() {
      // for jaxb and subclass
   }

   /**
    * Instantiates a new logic coordinate impl.
    *
    * @param statedAssemblageNid the stated assemblage nid
    * @param inferredAssemblageNid the inferred assemblage nid
    * @param descriptionLogicProfileNid the description logic profile nid
    * @param classifierNid the classifier nid
    * @param conceptAssemblageNid the nid for the assemblage within which the concepts to be classified are defined within.
    */
   public LogicCoordinateImpl(int statedAssemblageNid,
                              int inferredAssemblageNid,
                              int descriptionLogicProfileNid,
                              int classifierNid,
                              int conceptAssemblageNid) {
      
      this.statedAssemblage        = new ConceptProxy(statedAssemblageNid);
      this.inferredAssemblage      = new ConceptProxy(inferredAssemblageNid);
      this.descriptionLogicProfile = new ConceptProxy(descriptionLogicProfileNid);
      this.classifier              = new ConceptProxy(classifierNid);
      this.conceptAssemblage = new ConceptProxy(conceptAssemblageNid);
   }

   public LogicCoordinateImpl(ConceptSpecification statedAssemblage,
                              ConceptSpecification inferredAssemblage,
                              ConceptSpecification descriptionLogicProfile,
                              ConceptSpecification classifier,
                              ConceptSpecification conceptAssemblage) {
      
      this.statedAssemblage        = statedAssemblage;
      this.inferredAssemblage      = inferredAssemblage;
      this.descriptionLogicProfile = descriptionLogicProfile;
      this.classifier              = classifier;
      this.conceptAssemblage = conceptAssemblage;
   }

   //~--- methods -------------------------------------------------------------
    @Override
    @XmlElement
    public UUID getLogicCoordinateUuid() {
        return LogicCoordinate.super.getLogicCoordinateUuid(); //To change body of generated methods, choose Tools | Templates.
    }

    private void setLogicCoordinateUuid(UUID uuid) {
        // noop for jaxb
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
      
      if (this == obj) {
         return true;
      }
      
      if (!(obj instanceof LogicCoordinate)) {
         return false;
      }

      final LogicCoordinate other = (LogicCoordinate) obj;

      if (this.statedAssemblage.getNid() != other.getStatedAssemblageNid()) {
         return false;
      }

      if (this.inferredAssemblage.getNid() != other.getInferredAssemblageNid()) {
         return false;
      }

      if (this.descriptionLogicProfile.getNid() != other.getDescriptionLogicProfileNid()) {
         return false;
      }
      
      if (this.conceptAssemblage.getNid() != other.getConceptAssemblageNid()) {
         return false;
      }

      return this.classifier.getNid() == other.getClassifierNid();
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 29 * hash + this.statedAssemblage.getNid();
      hash = 29 * hash + this.inferredAssemblage.getNid();
      hash = 29 * hash + this.descriptionLogicProfile.getNid();
      hash = 29 * hash + this.classifier.getNid();
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "LogicCoordinateImpl{" + Get.conceptDescriptionText(this.statedAssemblage) + "<" +
             this.statedAssemblage + ">,\n" + Get.conceptDescriptionText(this.inferredAssemblage) +
             "<" + this.inferredAssemblage + ">, \n" +
             Get.conceptDescriptionText(this.descriptionLogicProfile) + "<" +
             this.descriptionLogicProfile + ">, \n" + Get.conceptDescriptionText(this.classifier) +
             "<" + this.classifier + ">}";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the classifier nid.
    *
    * @return the classifier nid
    */
   @Override
   public int getClassifierNid() {
      return this.classifier.getNid();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set classifier nid property.
    *
    * @param classifierProperty the classifier nid property
    * @return the change listener
    */
   public ChangeListener<ConceptSpecification> setClassifierProperty(ObjectProperty<ConceptSpecification>  classifierProperty) {
      final ChangeListener<ConceptSpecification> listener = (ObservableValue<? extends ConceptSpecification> observable,
                                               ConceptSpecification oldValue,
                                               ConceptSpecification newValue) -> {
               this.classifier = newValue;
            };

      classifierProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description logic profile nid.
    *
    * @return the description logic profile nid
    */
   @Override
   public int getDescriptionLogicProfileNid() {
      return this.descriptionLogicProfile.getNid();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set description logic profile property.
    *
    * @param descriptionLogicProfileProperty the description logic profile property
    * @return the change listener
    */
   public ChangeListener<ConceptSpecification> setDescriptionLogicProfileProperty(
           ObjectProperty<ConceptSpecification>  descriptionLogicProfileProperty) {
      final ChangeListener<ConceptSpecification> listener = (ObservableValue<? extends ConceptSpecification> observable,
                                               ConceptSpecification oldValue,
                                               ConceptSpecification newValue) -> {
               this.descriptionLogicProfile = newValue;
            };

      descriptionLogicProfileProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the inferred assemblage nid.
    *
    * @return the inferred assemblage nid
    */
   @Override
   public int getInferredAssemblageNid() {
      return this.inferredAssemblage.getNid();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set inferred assemblage nid property.
    *
    * @param inferredAssemblageProperty the inferred assemblage nid property
    * @return the change listener
    */
   public ChangeListener<ConceptSpecification> setInferredAssemblageProperty(
           ObjectProperty<ConceptSpecification>  inferredAssemblageProperty) {
      final ChangeListener<ConceptSpecification> listener = (ObservableValue<? extends ConceptSpecification> observable,
                                               ConceptSpecification oldValue,
                                               ConceptSpecification newValue) -> {
               this.inferredAssemblage = newValue;
            };

      inferredAssemblageProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stated assemblage nid.
    *
    * @return the stated assemblage nid
    */
   @Override
   public int getStatedAssemblageNid() {
      return this.statedAssemblage.getNid();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stated assemblage nid property.
    *
    * @param statedAssemblageProperty the stated assemblage nid property
    * @return the change listener
    */
   public ChangeListener<ConceptSpecification> setStatedAssemblageProperty(ObjectProperty<ConceptSpecification>  statedAssemblageProperty) {
      final ChangeListener<ConceptSpecification> listener = (ObservableValue<? extends ConceptSpecification> observable,
                                               ConceptSpecification oldValue,
                                               ConceptSpecification newValue) -> {
               this.statedAssemblage = newValue;
            };

      statedAssemblageProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
   
   @Override
   public LogicCoordinateImpl deepClone() {
      LogicCoordinateImpl newCoordinate = new LogicCoordinateImpl(getStatedAssemblageNid(),
                              getInferredAssemblageNid(),
                              getDescriptionLogicProfileNid(),
                              getClassifierNid(), 
                              getConceptAssemblageNid());
      return newCoordinate;
   }

   @Override
   public int getConceptAssemblageNid() {
      return this.conceptAssemblage.getNid();
   }

   /**
    * Set concept assemblage nid property.
    *
    * @param conceptAssemblageProperty the stated assemblage nid property
    * @return the change listener
    */
   public ChangeListener<ConceptSpecification> setConceptAssemblageProperty(ObjectProperty<ConceptSpecification>  conceptAssemblageProperty) {
      final ChangeListener<ConceptSpecification> listener = (ObservableValue<? extends ConceptSpecification> observable,
                                               ConceptSpecification oldValue,
                                               ConceptSpecification newValue) -> {
               this.conceptAssemblage = newValue;
            };

      conceptAssemblageProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

    @XmlElement(type=ConceptProxy.class)
    public ConceptSpecification getStatedAssemblage() {
        return statedAssemblage;
    }

    public void setStatedAssemblage(ConceptSpecification statedAssemblage) {
        this.statedAssemblage = statedAssemblage;
    }

    @XmlElement(type=ConceptProxy.class)
    public ConceptSpecification getInferredAssemblage() {
        return inferredAssemblage;
    }

    public void setInferredAssemblage(ConceptSpecification inferredAssemblage) {
        this.inferredAssemblage = inferredAssemblage;
    }

    @XmlElement(type=ConceptProxy.class)
    public ConceptSpecification getDescriptionLogicProfile() {
        return descriptionLogicProfile;
    }

    public void setDescriptionLogicProfile(ConceptSpecification descriptionLogicProfile) {
        this.descriptionLogicProfile = descriptionLogicProfile;
    }

    @XmlElement(type=ConceptProxy.class)
    public ConceptSpecification getClassifier() {
        return classifier;
    }

    public void setClassifier(ConceptSpecification classifier) {
        this.classifier = classifier;
    }

    @XmlElement(type=ConceptProxy.class)
    public ConceptSpecification getConceptAssemblage() {
        return conceptAssemblage;
    }

    public void setConceptAssemblage(ConceptSpecification conceptAssemblage) {
        this.conceptAssemblage = conceptAssemblage;
    }
   
}

