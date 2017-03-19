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
 *
 * @author kec
 */
public class EditCoordinateImpl
         implements EditCoordinate {
   int authorSequence;
   int moduleSequence;
   int pathSequence;

   //~--- constructors --------------------------------------------------------

   public EditCoordinateImpl(int authorId, int moduleId, int pathId) {
      this.authorSequence = Get.identifierService()
                               .getConceptSequence(authorId);
      this.moduleSequence = Get.identifierService()
                               .getConceptSequence(moduleId);
      this.pathSequence   = Get.identifierService()
                               .getConceptSequence(pathId);
   }

   //~--- methods -------------------------------------------------------------

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

   @Override
   public int hashCode() {
      int hash = 3;

      hash = 97 * hash + this.authorSequence;
      hash = 97 * hash + this.moduleSequence;
      hash = 97 * hash + this.pathSequence;
      return hash;
   }

   @Override
   public String toString() {
      return "EditCoordinate{a: " + Get.conceptDescriptionText(authorSequence) + ", m: " +
             Get.conceptDescriptionText(moduleSequence) + ", p: " + Get.conceptDescriptionText(pathSequence) + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAuthorSequence() {
      return authorSequence;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAuthorSequence(int authorId) {
      this.authorSequence = Get.identifierService()
                               .getConceptSequence(authorId);
   }

   public ChangeListener<Number> setAuthorSequenceProperty(IntegerProperty authorSequenceProperty) {
      ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           authorSequence = newValue.intValue();
                                        };

      authorSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getModuleSequence() {
      return moduleSequence;
   }

   //~--- set methods ---------------------------------------------------------

   public void setModuleSequence(int moduleId) {
      this.moduleSequence = Get.identifierService()
                               .getConceptSequence(moduleId);
   }

   public ChangeListener<Number> setModuleSequenceProperty(IntegerProperty moduleSequenceProperty) {
      ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           moduleSequence = newValue.intValue();
                                        };

      moduleSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getPathSequence() {
      return pathSequence;
   }

   //~--- set methods ---------------------------------------------------------

   public void setPathSequence(int pathId) {
      this.pathSequence = Get.identifierService()
                             .getConceptSequence(pathId);
   }

   public ChangeListener<Number> setPathSequenceProperty(IntegerProperty pathSequenceProperty) {
      ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           pathSequence = newValue.intValue();
                                        };

      pathSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
}

