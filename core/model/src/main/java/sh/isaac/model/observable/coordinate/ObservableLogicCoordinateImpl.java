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

   /** The stated assemblage nid property. */
   @XmlTransient
   IntegerProperty statedAssemblageNidProperty;

   /** The inferred assemblage nid property. */
   @XmlTransient
   IntegerProperty inferredAssemblageNidProperty;

   /** The description logic profile nid property. */
   @XmlTransient
   IntegerProperty descriptionLogicProfileNidProperty;

   /** The classifier nid property. */
   @XmlTransient
   IntegerProperty classifierNidProperty;

   /** The concept assemblage nid property. */
   @XmlTransient
   IntegerProperty conceptAssemblageNidProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable logic coordinate impl.
    */
   @SuppressWarnings("unused")
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
    * Classifier nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty classifierNidProperty() {
      if (this.classifierNidProperty == null) {
         this.classifierNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.CLASSIFIER_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getClassifierNid());
         addListenerReference(this.logicCoordinate.setClassifierNidProperty(this.classifierNidProperty));
         this.classifierNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.classifierNidProperty;
   }

   @Override
   public IntegerProperty conceptAssemblageNidProperty() {
      if (this.conceptAssemblageNidProperty == null) {
         this.conceptAssemblageNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.CLASSIFIER_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getClassifierNid());
         addListenerReference(this.logicCoordinate.setConceptAssemblageNidProperty(this.conceptAssemblageNidProperty));
         this.conceptAssemblageNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.conceptAssemblageNidProperty;
   }

   /**
    * Description logic profile nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty descriptionLogicProfileNidProperty() {
      if (this.descriptionLogicProfileNidProperty == null) {
         this.descriptionLogicProfileNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getDescriptionLogicProfileNid());
         addListenerReference(this.logicCoordinate.setDescriptionLogicProfileNidProperty(this.descriptionLogicProfileNidProperty));
         this.descriptionLogicProfileNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.descriptionLogicProfileNidProperty;
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
    * Inferred assemblage nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty inferredAssemblageNidProperty() {
      if (this.inferredAssemblageNidProperty == null) {
         this.inferredAssemblageNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getInferredAssemblageNid());
         this.inferredAssemblageNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      addListenerReference(this.logicCoordinate.setInferredAssemblageNidProperty(this.inferredAssemblageNidProperty));
      return this.inferredAssemblageNidProperty;
   }

   /**
    * Stated assemblage nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty statedAssemblageNidProperty() {
      if (this.statedAssemblageNidProperty == null) {
         this.statedAssemblageNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getStatedAssemblageNid());
         addListenerReference(this.logicCoordinate.setStatedAssemblageNidProperty(this.statedAssemblageNidProperty));
         this.statedAssemblageNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.statedAssemblageNidProperty;
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
    * Gets the classifier nid.
    *
    * @return the classifier nid
    */
   @Override
   public int getClassifierNid() {
      if (this.classifierNidProperty != null) {
         return this.classifierNidProperty.get();
      }

      return this.logicCoordinate.getClassifierNid();
   }
  /**
    * Gets the concept assemblage nid.
    *
    * @return the classifier nid
    */
   @Override
   public int getConceptAssemblageNid() {
      if (this.conceptAssemblageNidProperty != null) {
         return this.conceptAssemblageNidProperty.get();
      }

      return this.logicCoordinate.getConceptAssemblageNid();
   }

   /**
    * Gets the description logic profile nid.
    *
    * @return the description logic profile nid
    */
   @Override
   public int getDescriptionLogicProfileNid() {
      if (this.descriptionLogicProfileNidProperty != null) {
         return this.descriptionLogicProfileNidProperty.get();
      }

      return this.logicCoordinate.getDescriptionLogicProfileNid();
   }

   /**
    * Gets the inferred assemblage nid.
    *
    * @return the inferred assemblage nid
    */
   @Override
   public int getInferredAssemblageNid() {
      if (this.inferredAssemblageNidProperty != null) {
         return this.inferredAssemblageNidProperty.get();
      }

      return this.logicCoordinate.getInferredAssemblageNid();
   }

   /**
    * Gets the stated assemblage nid.
    *
    * @return the stated assemblage nid
    */
   @Override
   public int getStatedAssemblageNid() {
      if (this.statedAssemblageNidProperty != null) {
         return this.statedAssemblageNidProperty.get();
      }

      return this.logicCoordinate.getStatedAssemblageNid();
   }
   
   
   @Override
   public ObservableLogicCoordinateImpl deepClone() {
      return new ObservableLogicCoordinateImpl(logicCoordinate.deepClone());
   }
}

