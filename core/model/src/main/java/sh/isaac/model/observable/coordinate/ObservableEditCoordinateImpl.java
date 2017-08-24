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
   /** The author sequence property. */
   private IntegerProperty authorSequenceProperty = null;

   /** The module sequence property. */
   private IntegerProperty moduleSequenceProperty = null;

   /** The path sequence property. */
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
    * Author sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty authorSequenceProperty() {
      if (this.authorSequenceProperty == null) {
         this.authorSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.AUTHOR_SEQUENCE_FOR_EDIT_COORDINATE.toExternalString(),
               getAuthorSequence());
         addListenerReference(this.editCoordinate.setAuthorSequenceProperty(this.authorSequenceProperty));
      }

      return this.authorSequenceProperty;
   }

   /**
    * Module sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty moduleSequenceProperty() {
      if (this.moduleSequenceProperty == null) {
         this.moduleSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.MODULE_SEQUENCE_FOR_EDIT_COORDINATE.toExternalString(),
               getModuleSequence());
         addListenerReference(this.editCoordinate.setModuleSequenceProperty(this.moduleSequenceProperty));
      }

      return this.moduleSequenceProperty;
   }

   /**
    * Path sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty pathSequenceProperty() {
      if (this.pathSequenceProperty == null) {
         this.pathSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_SEQUENCE_FOR_EDIT_CORDINATE.toExternalString(),
               getPathSequence());
         addListenerReference(this.editCoordinate.setPathSequenceProperty(pathSequenceProperty()));
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
    * Gets the author sequence.
    *
    * @return the author sequence
    */
   @Override
   public int getAuthorSequence() {
      if (this.authorSequenceProperty != null) {
         return this.authorSequenceProperty.get();
      }

      return this.editCoordinate.getAuthorSequence();
   }

   /**
    * Gets the module sequence.
    *
    * @return the module sequence
    */
   @Override
   public int getModuleSequence() {
      if (this.moduleSequenceProperty != null) {
         return this.moduleSequenceProperty.get();
      }

      return this.editCoordinate.getModuleSequence();
   }

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public int getPathSequence() {
      if (this.pathSequenceProperty != null) {
         return this.pathSequenceProperty.get();
      }

      return this.editCoordinate.getPathSequence();
   }

   @Override
   public ObservableEditCoordinate deepClone() {
      return new ObservableEditCoordinateImpl(editCoordinate.deepClone());
   }
   
   
}

