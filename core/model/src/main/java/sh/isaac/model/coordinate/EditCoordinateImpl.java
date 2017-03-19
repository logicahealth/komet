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

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.EditCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class EditCoordinateImpl.
 *
 * @author kec
 */
public class EditCoordinateImpl
         implements EditCoordinate {
   
   /** The author sequence. */
   int authorSequence;
   
   /** The module sequence. */
   int moduleSequence;
   
   /** The path sequence. */
   int pathSequence;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new edits the coordinate impl.
    *
    * @param authorId the author id
    * @param moduleId the module id
    * @param pathId the path id
    */
   public EditCoordinateImpl(int authorId, int moduleId, int pathId) {
      this.authorSequence = Get.identifierService()
                               .getConceptSequence(authorId);
      this.moduleSequence = Get.identifierService()
                               .getConceptSequence(moduleId);
      this.pathSequence   = Get.identifierService()
                               .getConceptSequence(pathId);
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

      if (getClass() != obj.getClass()) {
         return false;
      }

      final EditCoordinateImpl other = (EditCoordinateImpl) obj;

      if (this.authorSequence != other.authorSequence) {
         return false;
      }

      if (this.moduleSequence != other.moduleSequence) {
         return false;
      }

      if (this.pathSequence != other.pathSequence) {
         return false;
      }

      return true;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 97 * hash + this.authorSequence;
      hash = 97 * hash + this.moduleSequence;
      hash = 97 * hash + this.pathSequence;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "EditCoordinate{a: " + Get.conceptDescriptionText(this.authorSequence) + ", m: " +
             Get.conceptDescriptionText(this.moduleSequence) + ", p: " + Get.conceptDescriptionText(this.pathSequence) + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author sequence.
    *
    * @return the author sequence
    */
   @Override
   public int getAuthorSequence() {
      return this.authorSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the author sequence.
    *
    * @param authorId the new author sequence
    */
   public void setAuthorSequence(int authorId) {
      this.authorSequence = Get.identifierService()
                               .getConceptSequence(authorId);
   }

   /**
    * Set author sequence property.
    *
    * @param authorSequenceProperty the author sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setAuthorSequenceProperty(IntegerProperty authorSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.authorSequence = newValue.intValue();
                                        };

      authorSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the module sequence.
    *
    * @return the module sequence
    */
   @Override
   public int getModuleSequence() {
      return this.moduleSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the module sequence.
    *
    * @param moduleId the new module sequence
    */
   public void setModuleSequence(int moduleId) {
      this.moduleSequence = Get.identifierService()
                               .getConceptSequence(moduleId);
   }

   /**
    * Set module sequence property.
    *
    * @param moduleSequenceProperty the module sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setModuleSequenceProperty(IntegerProperty moduleSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.moduleSequence = newValue.intValue();
                                        };

      moduleSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public int getPathSequence() {
      return this.pathSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the path sequence.
    *
    * @param pathId the new path sequence
    */
   public void setPathSequence(int pathId) {
      this.pathSequence = Get.identifierService()
                             .getConceptSequence(pathId);
   }

   /**
    * Set path sequence property.
    *
    * @param pathSequenceProperty the path sequence property
    * @return the change listener
    */
   public ChangeListener<Number> setPathSequenceProperty(IntegerProperty pathSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           this.pathSequence = newValue.intValue();
                                        };

      pathSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
}

