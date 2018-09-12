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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class EditCoordinateImpl.
 *
 * @author kec
 */
public class EditCoordinateImpl
         implements EditCoordinate {
   /** The author nid. */
   int authorNid;

   /** The module nid. */
   int moduleNid;

   /** The path nid. */
   int pathNid;
   
    List<ConceptSpecification> pathOptions = new ArrayList<>();
    List<ConceptSpecification> moduleOptions = new ArrayList<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new edits the coordinate impl.
    *
    * @param authorId the author id
    * @param moduleId the module id
    * @param pathId the path id
    */
   public EditCoordinateImpl(int authorId, int moduleId, int pathId) {
      this.authorNid = authorId;
      this.moduleNid = moduleId;
      this.pathNid   = pathId;
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

      if (this.authorNid != other.authorNid) {
         return false;
      }

      if (this.moduleNid != other.moduleNid) {
         return false;
      }

      if (this.pathNid != other.pathNid) {
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

      hash = 97 * hash + this.authorNid;
      hash = 97 * hash + this.moduleNid;
      hash = 97 * hash + this.pathNid;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "EditCoordinate{a: " + Get.conceptDescriptionText(this.authorNid) + ", m: " +
             Get.conceptDescriptionText(this.moduleNid) + ", p: " +
             Get.conceptDescriptionText(this.pathNid) + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author nid.
    *
    * @return the author nid
    */
   @Override
   public int getAuthorNid() {
      return this.authorNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the author nid.
    *
    * @param authorId the new author nid
    */
   public void setAuthorNid(int authorId) {
      this.authorNid = authorId;
   }

   /**
    * Set author nid property.
    *
    * @param authorSequenceProperty the author nid property
    * @return the change listener
    */
   public ChangeListener<Number> setAuthorNidProperty(IntegerProperty authorSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.authorNid = newValue.intValue();
            };

      authorSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the module nid.
    *
    * @return the module nid
    */
   @Override
   public int getModuleNid() {
      return this.moduleNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the module nid.
    *
    * @param moduleId the new module nid
    */
   public void setModuleNid(int moduleId) {
      this.moduleNid = moduleId;
   }

   /**
    * Set module nid property.
    *
    * @param moduleSequenceProperty the module nid property
    * @return the change listener
    */
   public ChangeListener<Number> setModuleNidProperty(IntegerProperty moduleSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.moduleNid = newValue.intValue();
            };

      moduleSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the path nid.
    *
    * @return the path nid
    */
   @Override
   public int getPathNid() {
      return this.pathNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the path nid.
    *
    * @param pathId the new path nid
    */
   public void setPathNid(int pathId) {
      this.pathNid = pathId;
   }

   /**
    * Set path nid property.
    *
    * @param pathSequenceProperty the path nid property
    * @return the change listener
    */
   public ChangeListener<Number> setPathNidProperty(IntegerProperty pathSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.pathNid = newValue.intValue();
            };

      pathSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   @Override
    public List<ConceptSpecification> getPathOptions() {
        return pathOptions;
    }

   @Override
    public List<ConceptSpecification> getModuleOptions() {
        return moduleOptions;
    }

    @Override
    public void setModuleOptions(List<ConceptSpecification> options) {
        this.moduleOptions = options;
    }

    @Override
    public void setPathOptions(List<ConceptSpecification> options) {
        this.pathOptions = options;
    }

   @Override
   public EditCoordinate deepClone() {
      EditCoordinateImpl newCoordinate = new EditCoordinateImpl(authorNid, moduleNid, pathNid);
      newCoordinate.moduleOptions.addAll(moduleOptions);
      newCoordinate.pathOptions.addAll(pathOptions);
      return newCoordinate;
   }
}

