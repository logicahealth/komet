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

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampCoordinateImpl.
 *
 * @author kec
 */
public class ObservableStampCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableStampCoordinate {
   /** The stamp coordinate. */
   StampCoordinateImpl stampCoordinate;

   /** The stamp precedence property. */
   ObjectProperty<StampPrecedence> stampPrecedenceProperty;

   /** The stamp position property. */
   ObjectProperty<ObservableStampPosition> stampPositionProperty;

   /** The module nids property. */
   SimpleSetProperty<ConceptSpecification> moduleSpecificationsProperty;

   /** The allowed states. */
   SetProperty<Status> allowedStates;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable stamp coordinate impl.
    *
    * @param stampCoordinate the stamp coordinate
    */
   public ObservableStampCoordinateImpl(StampCoordinate stampCoordinate) {
      if (stampCoordinate instanceof ObservableStampCoordinateImpl) {
         this.stampCoordinate = ((ObservableStampCoordinateImpl) stampCoordinate).stampCoordinate;
      } else {
         this.stampCoordinate = (StampCoordinateImpl) stampCoordinate;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Allowed states property.
    *
    * @return the set property
    */
   @Override
   public SetProperty<Status> allowedStatesProperty() {
      if (this.allowedStates == null) {
         this.allowedStates = new SimpleSetProperty<>(this,
               ObservableFields.ALLOWED_STATES_FOR_STAMP_COORDINATE.toExternalString(),
               FXCollections.observableSet(this.stampCoordinate.getAllowedStates()));
         this.stampCoordinate.setAllowedStatesProperty(this.allowedStates);
         this.allowedStates.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
      }

      return this.allowedStates;
   }

   /**
    * Make analog.
    *
    * @param stampPositionTime the stamp position time
    * @return the observable stamp coordinate impl
    */
   @Override
   public ObservableStampCoordinateImpl makeCoordinateAnalog(long stampPositionTime) {
      final StampCoordinate analog = this.stampCoordinate.makeCoordinateAnalog(stampPositionTime);

      return new ObservableStampCoordinateImpl(analog);
   }

   /**
    * Make analog.
    *
    * @param state the state
    * @return the observable stamp coordinate
    */
   @Override
   public ObservableStampCoordinate makeCoordinateAnalog(Status... state) {
      final StampCoordinate analog = this.stampCoordinate.makeCoordinateAnalog(state);

      return new ObservableStampCoordinateImpl(analog);
   }
   
   @Override
   public ObservableStampCoordinate makeCoordinateAnalog(EnumSet<Status> states) {
      StampCoordinate analog = stampCoordinate.makeCoordinateAnalog(states);
      return new ObservableStampCoordinateImpl(analog);
   }
   
   @Override
   public ObservableStampCoordinate makeModuleAnalog(Collection<ConceptSpecification> modules, boolean add) {
      return new ObservableStampCoordinateImpl(this.stampCoordinate.makeModuleAnalog(modules, add));
   }

   
   @Override
   public SetProperty<ConceptSpecification> moduleSpecificationsProperty() {
       if (this.moduleSpecificationsProperty == null) {
           this.moduleSpecificationsProperty = new SimpleSetProperty(this,
           ObservableFields.MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                   FXCollections.observableSet(this.stampCoordinate.getModuleSpecifications()));
           this.stampCoordinate.setModuleSpecifications(moduleSpecificationsProperty.get());
       }
       return this.moduleSpecificationsProperty;
   }

   /**
    * Stamp position property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<ObservableStampPosition> stampPositionProperty() {
      if (this.stampPositionProperty == null) {
         this.stampPositionProperty = new SimpleObjectProperty<>(this,
               ObservableFields.STAMP_POSITION_FOR_STAMP_COORDINATE.toExternalString(),
               new ObservableStampPositionImpl(this.stampCoordinate.getStampPosition()));
         addListenerReference(this.stampCoordinate.setStampPositionProperty(this.stampPositionProperty));
         this.stampPositionProperty.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
      }

      return this.stampPositionProperty;
   }

   /**
    * Stamp precedence property.
    *
    * @return the object property
    */
   @Override
   public ObjectProperty<StampPrecedence> stampPrecedenceProperty() {
      if (this.stampPrecedenceProperty == null) {
         this.stampPrecedenceProperty = new SimpleObjectProperty<>(this,
               ObservableFields.STAMP_PRECEDENCE_FOR_STAMP_COORDINATE.toExternalString(),
               getStampPrecedence());
         addListenerReference(this.stampCoordinate.setStampPrecedenceProperty(this.stampPrecedenceProperty));
         this.stampPrecedenceProperty.addListener((InvalidationListener)(invalidation) -> fireValueChangedEvent());
      }

      return this.stampPrecedenceProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableStampCoordinateImpl{" + this.stampCoordinate + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the allowed states.
    *
    * @return the allowed states
    */
   @Override
   public EnumSet<Status> getAllowedStates() {
      return this.stampCoordinate.getAllowedStates();
   }

   /**
    * Gets the module nids.
    *
    * @return the module nids
    */
   @Override
   public NidSet getModuleNids() {
      return this.stampCoordinate.getModuleNids();
   }

   /**
    * Gets the stamp position.
    *
    * @return the stamp position
    */
   @Override
   public ObservableStampPosition getStampPosition() {
      return stampPositionProperty().get();
   }

   /**
    * Gets the stamp precedence.
    *
    * @return the stamp precedence
    */
   @Override
   public StampPrecedence getStampPrecedence() {
      if (this.stampPrecedenceProperty != null) {
         return this.stampPrecedenceProperty.get();
      }

      return this.stampCoordinate.getStampPrecedence();
   }

   @Override
   public int hashCode() {
      return this.stampCoordinate.hashCode();
   }

   
   @Override
   public ObservableStampCoordinateImpl deepClone() {
      return new ObservableStampCoordinateImpl(stampCoordinate.deepClone());
   }

    @Override
    public StampCoordinate getImmutableAllStateAnalog() {
        return this.stampCoordinate.getImmutableAllStateAnalog();
    }

    @Override
    public ObservableList<ConceptSpecification> getModulePreferenceOrderForVersions() {
        return this.modulePreferenceListForVersionsProperty().get();
    }
    /**
     * The dialect assemblage preference list property.
     */
    SimpleListProperty<ConceptSpecification> modulePreferenceOrderForVersionsProperty = null;

    @Override
    public ListProperty<ConceptSpecification> modulePreferenceListForVersionsProperty() {
        if (this.modulePreferenceOrderForVersionsProperty == null) {
            ObservableList<ConceptSpecification> preferenceList = FXCollections.observableArrayList(this.stampCoordinate.getModulePreferenceOrderForVersions());
            this.modulePreferenceOrderForVersionsProperty = new SimpleListProperty(this,
                    ObservableFields.MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE.toExternalString(),
                    preferenceList);
            this.stampCoordinate.setModulePreferenceListForVersions(preferenceList);
        }

        return this.modulePreferenceOrderForVersionsProperty;
    }

    @Override
    public Set<ConceptSpecification> getModuleSpecifications() {
        return this.stampCoordinate.getModuleSpecifications();
    }


    
}

