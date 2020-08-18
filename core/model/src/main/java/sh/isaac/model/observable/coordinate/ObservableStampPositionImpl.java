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

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;

import java.util.Objects;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPositionImpl.
 *
 * @author kec
 */
public class ObservableStampPositionImpl
        extends ObservableCoordinateImpl<StampPositionImmutable>
         implements ObservableStampPosition {

   /** The time property. */
   LongProperty timeProperty;

   /** The stamp path nid property. */
    ObjectProperty<ConceptSpecification> pathConceptProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable stamp position impl.
    *
    * @param stampPosition the stamp position
    */
   public ObservableStampPositionImpl(StampPositionImmutable stampPosition) {
      super(stampPosition);

      this.pathConceptProperty = new SimpleEqualityBasedObjectProperty(this,
              ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
              stampPosition.getPathForPositionConcept());

      this.timeProperty = new SimpleLongProperty(this,
              ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
              stampPosition.getTime());

      addListeners();
   }

   @Override
   protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampPositionImmutable> observable,
                                                        StampPositionImmutable oldValue, StampPositionImmutable newValue) {
      this.pathConceptProperty.setValue(newValue.getPathForPositionConcept());
      this.timeProperty.set(newValue.getTime());
   }

   @Override
   protected void addListeners() {
      this.pathConceptProperty.addListener(this::pathConceptChanged);
      this.timeProperty.addListener(this::timeChanged);
   }

   @Override
   protected void removeListeners() {
      this.pathConceptProperty.removeListener(this::pathConceptChanged);
      this.timeProperty.removeListener(this::timeChanged);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public StampPositionImmutable getStampPosition() {
      return getValue();
   }

   @Override
   public StampPositionImmutable toStampPositionImmutable() {
      return getValue();
   }

   private void timeChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newTime) {
      this.setValue(StampPositionImmutable.make(newTime.longValue(), getPathForPositionNid()));
   }

   private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                   ConceptSpecification oldPathConcept,
                                   ConceptSpecification newPathConcept) {
      this.setValue(StampPositionImmutable.make(getTime(), newPathConcept.getNid()));
   }
    /**
     * Filter path nid property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<ConceptSpecification> pathConceptProperty() {
         return this.pathConceptProperty;
    }

   /**
    * Time property.
    *
    * @return the long property
    */
   @Override
   public LongProperty timeProperty() {
      return this.timeProperty;
   }


   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableStampPositionImpl{" + this.getValue().toString() + '}';
   }

   //~--- get methods ---------------------------------------------------------


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || !(o instanceof StampPosition)) return false;
      StampPosition that = (StampPosition) o;
      return this.getTime() == that.getTime() &&
              this.getPathForPositionNid() == that.getPathForPositionNid();
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getTime(), this.getPathForPositionNid());
   }
}

