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
 *
 * @author kec
 */
public class ObservableEditCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableEditCoordinate {
   private IntegerProperty          authorSequenceProperty = null;
   private IntegerProperty          moduleSequenceProperty = null;
   private IntegerProperty          pathSequenceProperty   = null;
   private final EditCoordinateImpl editCoordinate;

   //~--- constructors --------------------------------------------------------

   public ObservableEditCoordinateImpl(EditCoordinate editCoordinate) {
      this.editCoordinate = (EditCoordinateImpl) editCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty authorSequenceProperty() {
      if (authorSequenceProperty == null) {
         authorSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.AUTHOR_SEQUENCE_FOR_EDIT_COORDINATE.toExternalString(),
               getAuthorSequence());
         addListenerReference(editCoordinate.setAuthorSequenceProperty(authorSequenceProperty));
      }

      return authorSequenceProperty;
   }

   @Override
   public IntegerProperty moduleSequenceProperty() {
      if (moduleSequenceProperty == null) {
         moduleSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.MODULE_SEQUENCE_FOR_EDIT_COORDINATE.toExternalString(),
               getModuleSequence());
         addListenerReference(editCoordinate.setModuleSequenceProperty(moduleSequenceProperty));
      }

      return moduleSequenceProperty;
   }

   @Override
   public IntegerProperty pathSequenceProperty() {
      if (pathSequenceProperty == null) {
         pathSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_SEQUENCE_FOR_EDIT_CORDINATE.toExternalString(),
               getPathSequence());
         addListenerReference(editCoordinate.setPathSequenceProperty(pathSequenceProperty()));
      }

      return pathSequenceProperty;
   }

   @Override
   public String toString() {
      return "ObservableEditCoordinateImpl{" + editCoordinate + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAuthorSequence() {
      if (authorSequenceProperty != null) {
         return authorSequenceProperty.get();
      }

      return editCoordinate.getAuthorSequence();
   }

   @Override
   public int getModuleSequence() {
      if (moduleSequenceProperty != null) {
         return moduleSequenceProperty.get();
      }

      return editCoordinate.getModuleSequence();
   }

   @Override
   public int getPathSequence() {
      if (pathSequenceProperty != null) {
         return pathSequenceProperty.get();
      }

      return editCoordinate.getPathSequence();
   }
}

