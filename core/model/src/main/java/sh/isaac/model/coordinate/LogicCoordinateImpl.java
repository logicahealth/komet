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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.LogicCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LogicCoordinateImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "logicCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogicCoordinateImpl
         implements LogicCoordinate {
   /** The stated assemblage nid. */
   protected int statedAssemblageNid;

   /** The inferred assemblage nid. */
   protected int inferredAssemblageNid;

   /** The description logic profile nid. */
   protected int descriptionLogicProfileNid;

   /** The classifier nid. */
   protected int classifierNid;

   /** The concept assemblage nid. */
   protected int conceptAssemblageNid;

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
      
      this.statedAssemblageNid        = statedAssemblageNid;
      this.inferredAssemblageNid      = inferredAssemblageNid;
      this.descriptionLogicProfileNid = descriptionLogicProfileNid;
      this.classifierNid              = classifierNid;
      this.conceptAssemblageNid = conceptAssemblageNid;
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
      if (obj instanceof LogicCoordinate) {
         return false;
      }
      // Do not compare object classes, a LogicCoordinateImpl from one impl should be able to be equal to another impl...
      final LogicCoordinate other = (LogicCoordinate) obj;

      if (this.statedAssemblageNid != other.getStatedAssemblageNid()) {
         return false;
      }

      if (this.inferredAssemblageNid != other.getInferredAssemblageNid()) {
         return false;
      }

      if (this.descriptionLogicProfileNid != other.getDescriptionLogicProfileNid()) {
         return false;
      }

      return this.classifierNid == other.getClassifierNid();
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 29 * hash + this.statedAssemblageNid;
      hash = 29 * hash + this.inferredAssemblageNid;
      hash = 29 * hash + this.descriptionLogicProfileNid;
      hash = 29 * hash + this.classifierNid;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "LogicCoordinateImpl{" + Get.conceptDescriptionText(this.statedAssemblageNid) + "<" +
             this.statedAssemblageNid + ">,\n" + Get.conceptDescriptionText(this.inferredAssemblageNid) +
             "<" + this.inferredAssemblageNid + ">, \n" +
             Get.conceptDescriptionText(this.descriptionLogicProfileNid) + "<" +
             this.descriptionLogicProfileNid + ">, \n" + Get.conceptDescriptionText(this.classifierNid) +
             "<" + this.classifierNid + ">}";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the classifier nid.
    *
    * @return the classifier nid
    */
   @Override
   public int getClassifierNid() {
      return this.classifierNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set classifier nid property.
    *
    * @param classifierNidProperty the classifier nid property
    * @return the change listener
    */
   public ChangeListener<Number> setClassifierNidProperty(IntegerProperty classifierNidProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.classifierNid = newValue.intValue();
            };

      classifierNidProperty.addListener(new WeakChangeListener<>(listener));
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
      return this.descriptionLogicProfileNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set description logic profile nid property.
    *
    * @param descriptionLogicProfileNidProperty the description logic profile nid property
    * @return the change listener
    */
   public ChangeListener<Number> setDescriptionLogicProfileNidProperty(
           IntegerProperty descriptionLogicProfileNidProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.descriptionLogicProfileNid = newValue.intValue();
            };

      descriptionLogicProfileNidProperty.addListener(new WeakChangeListener<>(listener));
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
      return this.inferredAssemblageNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set inferred assemblage nid property.
    *
    * @param inferredAssemblageNidProperty the inferred assemblage nid property
    * @return the change listener
    */
   public ChangeListener<Number> setInferredAssemblageNidProperty(
           IntegerProperty inferredAssemblageNidProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.inferredAssemblageNid = newValue.intValue();
            };

      inferredAssemblageNidProperty.addListener(new WeakChangeListener<>(listener));
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
      return this.statedAssemblageNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stated assemblage nid property.
    *
    * @param statedAssemblageNidProperty the stated assemblage nid property
    * @return the change listener
    */
   public ChangeListener<Number> setStatedAssemblageNidProperty(IntegerProperty statedAssemblageNidProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.statedAssemblageNid = newValue.intValue();
            };

      statedAssemblageNidProperty.addListener(new WeakChangeListener<>(listener));
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
      return this.conceptAssemblageNid;
   }

   /**
    * Set concept assemblage nid property.
    *
    * @param conceptAssemblageNidProperty the stated assemblage nid property
    * @return the change listener
    */
   public ChangeListener<Number> setConceptAssemblageNidProperty(IntegerProperty conceptAssemblageNidProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.conceptAssemblageNid = newValue.intValue();
            };

      conceptAssemblageNidProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
   
}

