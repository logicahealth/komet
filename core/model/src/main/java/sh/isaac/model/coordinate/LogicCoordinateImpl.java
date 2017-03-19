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
   
   /** The stated assemblage sequence. */
   int statedAssemblageSequence;
   
   /** The inferred assemblage sequence. */
   int inferredAssemblageSequence;
   
   /** The description logic profile sequence. */
   int descriptionLogicProfileSequence;
   
   /** The classifier sequence. */
   int classifierSequence;

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
    * @param statedAssemblageSequence the stated assemblage sequence
    * @param inferredAssemblageSequence the inferred assemblage sequence
    * @param descriptionLogicProfileSequence the description logic profile sequence
    * @param classifierSequence the classifier sequence
    */
   public LogicCoordinateImpl(int statedAssemblageSequence,
                              int inferredAssemblageSequence,
                              int descriptionLogicProfileSequence,
                              int classifierSequence) {
      this.statedAssemblageSequence        = statedAssemblageSequence;
      this.inferredAssemblageSequence      = inferredAssemblageSequence;
      this.descriptionLogicProfileSequence = descriptionLogicProfileSequence;
      this.classifierSequence              = classifierSequence;
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

      if (this.statedAssemblageSequence != other.getStatedAssemblageSequence()) {
         return false;
      }

      if (this.inferredAssemblageSequence != other.getInferredAssemblageSequence()) {
         return false;
      }

      if (this.descriptionLogicProfileSequence != other.getDescriptionLogicProfileSequence()) {
         return false;
      }

      return this.classifierSequence == other.getClassifierSequence();
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 29 * hash + this.statedAssemblageSequence;
      hash = 29 * hash + this.inferredAssemblageSequence;
      hash = 29 * hash + this.descriptionLogicProfileSequence;
      hash = 29 * hash + this.classifierSequence;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "LogicCoordinateImpl{" + Get.conceptDescriptionText(this.statedAssemblageSequence) + "<" +
             this.statedAssemblageSequence + ">,\n" + Get.conceptDescriptionText(this.inferredAssemblageSequence) + "<" +
             this.inferredAssemblageSequence + ">, \n" + Get.conceptDescriptionText(this.descriptionLogicProfileSequence) + "<" +
             this.descriptionLogicProfileSequence + ">, \n" + Get.conceptDescriptionText(this.classifierSequence) + "<" +
             this.classifierSequence + ">}";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the classifier sequence.
    *
    * @return the classifier sequence
    */
   @Override
   public int getClassifierSequence() {
      return this.classifierSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set classifier sequence property.
    *
    * @param classifierSequenceProperty the classifier sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setClassifierSequenceProperty(IntegerProperty classifierSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.classifierSequence = newValue.intValue();
                                        };

      classifierSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description logic profile sequence.
    *
    * @return the description logic profile sequence
    */
   @Override
   public int getDescriptionLogicProfileSequence() {
      return this.descriptionLogicProfileSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set description logic profile sequence property.
    *
    * @param descriptionLogicProfileSequenceProperty the description logic profile sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setDescriptionLogicProfileSequenceProperty(
           IntegerProperty descriptionLogicProfileSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.descriptionLogicProfileSequence = newValue.intValue();
                                        };

      descriptionLogicProfileSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the inferred assemblage sequence.
    *
    * @return the inferred assemblage sequence
    */
   @Override
   public int getInferredAssemblageSequence() {
      return this.inferredAssemblageSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set inferred assemblage sequence property.
    *
    * @param inferredAssemblageSequenceProperty the inferred assemblage sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setInferredAssemblageSequenceProperty(
           IntegerProperty inferredAssemblageSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.inferredAssemblageSequence = newValue.intValue();
                                        };

      inferredAssemblageSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stated assemblage sequence.
    *
    * @return the stated assemblage sequence
    */
   @Override
   public int getStatedAssemblageSequence() {
      return this.statedAssemblageSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stated assemblage sequence property.
    *
    * @param statedAssemblageSequenceProperty the stated assemblage sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setStatedAssemblageSequenceProperty(IntegerProperty statedAssemblageSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.statedAssemblageSequence = newValue.intValue();
                                        };

      statedAssemblageSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
}

