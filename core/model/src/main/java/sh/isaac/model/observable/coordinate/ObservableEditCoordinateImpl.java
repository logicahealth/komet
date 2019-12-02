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

import sh.isaac.api.observable.coordinate.ObservableCoordinateImpl;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.api.component.concept.ConceptSpecification;

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
   private IntegerProperty authorNidProperty = null;

   /** The module nid property. */
   private IntegerProperty moduleNidProperty = null;

   /** The path nid property. */
   private IntegerProperty pathNidProperty = null;

   /** The edit coordinate. */
   private final EditCoordinateImpl editCoordinate;
   
   private final SimpleListProperty<ConceptSpecification> moduleOptionsList = new SimpleListProperty<>(this, 
           ObservableFields.MODULE_OPTIONS_FOR_EDIT_COORDINATE.toExternalString(), FXCollections.observableArrayList());

   private final SimpleListProperty<ConceptSpecification> pathOptionsList = new SimpleListProperty<>(this, 
           ObservableFields.PATH_OPTIONS_FOR_EDIT_COORDINATE.toExternalString(), FXCollections.observableArrayList());

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable edit coordinate impl.
    *
    * @param editCoordinate the edit coordinate
    */
   public ObservableEditCoordinateImpl(EditCoordinate editCoordinate) {
      this.editCoordinate = (EditCoordinateImpl) editCoordinate;
      this.moduleOptionsList.addAll(editCoordinate.getModuleOptions());
      this.editCoordinate.setModuleOptions(this.moduleOptionsList);
      this.pathOptionsList.addAll(editCoordinate.getPathOptions());
      this.editCoordinate.setPathOptions(pathOptionsList);
   }

   @Override
   public ObservableList<ConceptSpecification> getModuleOptions() {
        return this.moduleOptionsList;
   }

    @Override
    public void setModuleOptions(List<ConceptSpecification> options) {
        this.moduleOptionsList.setAll(options);
    }

    @Override
    public ObservableList<ConceptSpecification> getPathOptions() {
        return this.pathOptionsList;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void setPathOptions(List<ConceptSpecification> options) {
        this.pathOptionsList.setAll(options);
    }

    /**
     * author nid property.
     *
     * @return the integer property
     */
    @Override
    public IntegerProperty authorNidProperty() {
        if (this.authorNidProperty == null) {
            this.authorNidProperty = new SimpleIntegerProperty(this,
                    ObservableFields.AUTHOR_NID_FOR_EDIT_COORDINATE.toExternalString(),
                    getAuthorNid());
            addListenerReference(this.editCoordinate.setAuthorNidProperty(this.authorNidProperty));
            this.authorNidProperty.addListener((invalidation) -> fireValueChangedEvent());
        }
        
        return this.authorNidProperty;
    }

   /**
    * module nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty moduleNidProperty() {
      if (this.moduleNidProperty == null) {
         this.moduleNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.MODULE_NID_FOR_EDIT_COORDINATE.toExternalString(),
               getModuleNid());
         addListenerReference(this.editCoordinate.setModuleNidProperty(this.moduleNidProperty));
         this.moduleNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.moduleNidProperty;
   }

   /**
    * path nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty pathNidProperty() {
      if (this.pathNidProperty == null) {
         this.pathNidProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_NID_FOR_EDIT_CORDINATE.toExternalString(),
               getPathNid());
         addListenerReference(this.editCoordinate.setPathNidProperty(pathNidProperty()));
         this.pathNidProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.pathNidProperty;
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
      if (this.authorNidProperty != null) {
         return this.authorNidProperty.get();
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
      if (this.moduleNidProperty != null) {
         return this.moduleNidProperty.get();
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
      if (this.pathNidProperty != null) {
         return this.pathNidProperty.get();
      }

      return this.editCoordinate.getPathNid();
   }

   @Override
   public ObservableEditCoordinate deepClone() {
      return new ObservableEditCoordinateImpl(editCoordinate.deepClone());
   }

    @Override
    public EditCoordinateImpl getEditCoordinate() {
        return this.editCoordinate;
    }
}

