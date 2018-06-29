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

import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.coordinate.EditCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableEditCoordinateImpl.
 *
 * @author kec
 */
public class ObservableEditCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableEditCoordinate {
   /** The author nid property. */
   private IntegerProperty authorSequenceProperty = null;

   /** The module nid property. */
   private IntegerProperty moduleSequenceProperty = null;

   /** The path nid property. */
   private IntegerProperty pathSequenceProperty = null;

   /** The edit coordinate. */
   private final EditCoordinateImpl editCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable edit coordinate impl.
    *
    * @param editCoordinate the edit coordinate
    */
   public ObservableEditCoordinateImpl(EditCoordinate editCoordinate) {
      this.editCoordinate = (EditCoordinateImpl) editCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * author nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty authorNidProperty() {
      if (this.authorSequenceProperty == null) {
         this.authorSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.AUTHOR_NID_FOR_EDIT_COORDINATE.toExternalString(),
               getAuthorNid());
         addListenerReference(this.editCoordinate.setAuthorNidProperty(this.authorSequenceProperty));
         this.authorSequenceProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.authorSequenceProperty;
   }

   /**
    * module nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty moduleNidProperty() {
      if (this.moduleSequenceProperty == null) {
         this.moduleSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.MODULE_NID_FOR_EDIT_COORDINATE.toExternalString(),
               getModuleNid());
         addListenerReference(this.editCoordinate.setModuleNidProperty(this.moduleSequenceProperty));
         this.moduleSequenceProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.moduleSequenceProperty;
   }

   /**
    * path nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty pathNidProperty() {
      if (this.pathSequenceProperty == null) {
         this.pathSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_NID_FOR_EDIT_CORDINATE.toExternalString(),
               getPathNid());
         addListenerReference(this.editCoordinate.setPathNidProperty(pathNidProperty()));
         this.pathSequenceProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.pathSequenceProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableEditCoordinateImpl{" + this.editCoordinate + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author nid.
    *
    * @return the author nid
    */
   @Override
   public int getAuthorNid() {
      if (this.authorSequenceProperty != null) {
         return this.authorSequenceProperty.get();
      }

      return this.editCoordinate.getAuthorNid();
   }

   /**
    * Gets the module nid.
    *
    * @return the module nid
    */
   @Override
   public int getModuleNid() {
      if (this.moduleSequenceProperty != null) {
         return this.moduleSequenceProperty.get();
      }

      return this.editCoordinate.getModuleNid();
   }

   /**
    * Gets the path nid.
    *
    * @return the path nid
    */
   @Override
   public int getPathNid() {
      if (this.pathSequenceProperty != null) {
         return this.pathSequenceProperty.get();
      }

      return this.editCoordinate.getPathNid();
   }

   @Override
   public ObservableEditCoordinate deepClone() {
      return new ObservableEditCoordinateImpl(editCoordinate.deepClone());
   }
}

