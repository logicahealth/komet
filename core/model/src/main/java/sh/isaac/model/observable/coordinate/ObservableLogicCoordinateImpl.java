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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableLogicCoordinateImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "observableLogicCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObservableLogicCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableLogicCoordinate {
   /** The logic coordinate. */
   LogicCoordinateImpl logicCoordinate;

   /** The stated assemblage sequence property. */
   @XmlTransient
   IntegerProperty statedAssemblageSequenceProperty;

   /** The inferred assemblage sequence property. */
   @XmlTransient
   IntegerProperty inferredAssemblageSequenceProperty;

   /** The description logic profile sequence property. */
   @XmlTransient
   IntegerProperty descriptionLogicProfileSequenceProperty;

   /** The classifier sequence property. */
   @XmlTransient
   IntegerProperty classifierSequenceProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable logic coordinate impl.
    */
   private ObservableLogicCoordinateImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new observable logic coordinate impl.
    *
    * @param logicCoordinate the logic coordinate
    */
   public ObservableLogicCoordinateImpl(LogicCoordinate logicCoordinate) {
      if (logicCoordinate instanceof ObservableLogicCoordinateImpl) {
         this.logicCoordinate = ((ObservableLogicCoordinateImpl) logicCoordinate).logicCoordinate;
      } else {
         this.logicCoordinate = (LogicCoordinateImpl) logicCoordinate;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Classifier sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty classifierSequenceProperty() {
      if (this.classifierSequenceProperty == null) {
         this.classifierSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.CLASSIFIER_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(),
               getClassifierSequence());
         addListenerReference(this.logicCoordinate.setClassifierSequenceProperty(this.classifierSequenceProperty));
      }

      return this.classifierSequenceProperty;
   }

   /**
    * Description logic profile sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty descriptionLogicProfileSequenceProperty() {
      if (this.descriptionLogicProfileSequenceProperty == null) {
         this.descriptionLogicProfileSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.DESCRIPTION_LOGIC_PROFILE_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(),
               getDescriptionLogicProfileSequence());
         addListenerReference(
             this.logicCoordinate.setDescriptionLogicProfileSequenceProperty(
                 this.descriptionLogicProfileSequenceProperty));
      }

      return this.descriptionLogicProfileSequenceProperty;
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      return this.logicCoordinate.equals(obj);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.logicCoordinate.hashCode();
   }

   /**
    * Inferred assemblage sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty inferredAssemblageSequenceProperty() {
      if (this.inferredAssemblageSequenceProperty == null) {
         this.inferredAssemblageSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.INFERRED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(),
               getInferredAssemblageSequence());
      }

      addListenerReference(
          this.logicCoordinate.setInferredAssemblageSequenceProperty(this.inferredAssemblageSequenceProperty));
      return this.inferredAssemblageSequenceProperty;
   }

   /**
    * Stated assemblage sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty statedAssemblageSequenceProperty() {
      if (this.statedAssemblageSequenceProperty == null) {
         this.statedAssemblageSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.STATED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(),
               getStatedAssemblageSequence());
         addListenerReference(
             this.logicCoordinate.setStatedAssemblageSequenceProperty(this.statedAssemblageSequenceProperty));
      }

      return this.statedAssemblageSequenceProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableLogicCoordinateImpl{" + this.logicCoordinate.toString() + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the classifier sequence.
    *
    * @return the classifier sequence
    */
   @Override
   public int getClassifierSequence() {
      if (this.classifierSequenceProperty != null) {
         return this.classifierSequenceProperty.get();
      }

      return this.logicCoordinate.getClassifierSequence();
   }

   /**
    * Gets the description logic profile sequence.
    *
    * @return the description logic profile sequence
    */
   @Override
   public int getDescriptionLogicProfileSequence() {
      if (this.descriptionLogicProfileSequenceProperty != null) {
         return this.descriptionLogicProfileSequenceProperty.get();
      }

      return this.logicCoordinate.getDescriptionLogicProfileSequence();
   }

   /**
    * Gets the inferred assemblage sequence.
    *
    * @return the inferred assemblage sequence
    */
   @Override
   public int getInferredAssemblageSequence() {
      if (this.inferredAssemblageSequenceProperty != null) {
         return this.inferredAssemblageSequenceProperty.get();
      }

      return this.logicCoordinate.getInferredAssemblageSequence();
   }

   /**
    * Gets the stated assemblage sequence.
    *
    * @return the stated assemblage sequence
    */
   @Override
   public int getStatedAssemblageSequence() {
      if (this.statedAssemblageSequenceProperty != null) {
         return this.statedAssemblageSequenceProperty.get();
      }

      return this.logicCoordinate.getStatedAssemblageSequence();
   }
   
   
   @Override
   public ObservableLogicCoordinateImpl deepClone() {
      return new ObservableLogicCoordinateImpl(logicCoordinate.deepClone());
   }

}

