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

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

import javafx.collections.ArrayChangeListener;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.SetChangeListener;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@XmlRootElement(name = "stampCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class StampCoordinateImpl
         implements StampCoordinate {
   StampPrecedence    stampPrecedence;
   @XmlElement(type = StampPositionImpl.class)
   StampPosition      stampPosition;
   @XmlJavaTypeAdapter(ConceptSequenceSetAdapter.class)
   ConceptSequenceSet moduleSequences;
   @XmlJavaTypeAdapter(EnumSetAdapter.class)
   EnumSet<State>     allowedStates;

   //~--- constructors --------------------------------------------------------

   private StampCoordinateImpl() {
      // for jaxb
   }

   public StampCoordinateImpl(StampPrecedence stampPrecedence,
                              StampPosition stampPosition,
                              ConceptSequenceSet moduleSequences,
                              EnumSet<State> allowedStates) {
      this.stampPrecedence = stampPrecedence;
      this.stampPosition   = stampPosition;
      this.moduleSequences = moduleSequences;
      this.allowedStates   = allowedStates;

      if (this.moduleSequences == null) {
         this.moduleSequences = new ConceptSequenceSet();
      }
   }

   public StampCoordinateImpl(StampPrecedence stampPrecedence,
                              StampPosition stampPosition,
                              List<ConceptSpecification> moduleSpecifications,
                              EnumSet<State> allowedStates) {
      this(stampPrecedence,
           stampPosition,
           ConceptSequenceSet.of(moduleSpecifications.stream()
                 .mapToInt((spec) -> spec.getConceptSequence())),
           allowedStates);
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

      final StampCoordinateImpl other = (StampCoordinateImpl) obj;

      if (this.stampPrecedence != other.stampPrecedence) {
         return false;
      }

      if (!Objects.equals(this.stampPosition, other.stampPosition)) {
         return false;
      }

      if (!this.allowedStates.equals(other.allowedStates)) {
         return false;
      }

      return this.moduleSequences.equals(other.moduleSequences);
   }

   @Override
   public int hashCode() {
      int hash = 7;

      hash = 11 * hash + Objects.hashCode(this.stampPrecedence);
      hash = 11 * hash + Objects.hashCode(this.stampPosition);
      hash = 11 * hash + Objects.hashCode(this.moduleSequences);
      hash = 11 * hash + Objects.hashCode(this.allowedStates);
      return hash;
   }

   @Override
   public StampCoordinateImpl makeAnalog(long stampPositionTime) {
      final StampPosition anotherStampPosition = new StampPositionImpl(stampPositionTime,
                                                                 this.stampPosition.getStampPathSequence());

      return new StampCoordinateImpl(this.stampPrecedence, anotherStampPosition, this.moduleSequences, this.allowedStates);
   }

   @Override
   public StampCoordinateImpl makeAnalog(State... states) {
      final EnumSet<State> newAllowedStates = EnumSet.noneOf(State.class);

      newAllowedStates.addAll(Arrays.asList(states));
      return new StampCoordinateImpl(this.stampPrecedence, this.stampPosition, this.moduleSequences, newAllowedStates);
   }

   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("Stamp Coordinate{")
             .append(this.stampPrecedence)
             .append(", ")
             .append(this.stampPosition)
             .append(", modules: ");

      if (this.moduleSequences.isEmpty()) {
         builder.append("all, ");
      } else {
         builder.append(Get.conceptDescriptionTextList(this.moduleSequences))
                .append(", ");
      }

      builder.append(this.allowedStates)
             .append('}');
      return builder.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public EnumSet<State> getAllowedStates() {
      return this.allowedStates;
   }

   //~--- set methods ---------------------------------------------------------

   public SetChangeListener<State> setAllowedStatesProperty(SetProperty<State> allowedStatesProperty) {
      final SetChangeListener<State> listener = (change) -> {
               if (change.wasAdded()) {
                  this.allowedStates.add(change.getElementAdded());
               } else {
                  this.allowedStates.remove(change.getElementRemoved());
               }
            };

      allowedStatesProperty.addListener(new WeakSetChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConceptSequenceSet getModuleSequences() {
      return this.moduleSequences;
   }

   //~--- set methods ---------------------------------------------------------

   public ArrayChangeListener<ObservableIntegerArray> setModuleSequencesProperty(
           ObjectProperty<ObservableIntegerArray> moduleSequencesProperty) {
      final ArrayChangeListener<ObservableIntegerArray> listener = (ObservableIntegerArray observableArray,
                                                              boolean sizeChanged,
                                                              int from,
                                                              int to) -> {
               this.moduleSequences = ConceptSequenceSet.of(observableArray.toArray(new int[observableArray.size()]));
            };

      moduleSequencesProperty.getValue()
                             .addListener(new WeakArrayChangeListener(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public StampPosition getStampPosition() {
      return this.stampPosition;
   }

   //~--- set methods ---------------------------------------------------------

   public ChangeListener<ObservableStampPosition> setStampPositionProperty(
           ObjectProperty<ObservableStampPosition> stampPositionProperty) {
      final ChangeListener<ObservableStampPosition> listener = (observable, oldValue, newValue) -> {
               this.stampPosition = newValue;
            };

      stampPositionProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public StampPrecedence getStampPrecedence() {
      return this.stampPrecedence;
   }

   //~--- set methods ---------------------------------------------------------

   public ChangeListener<StampPrecedence> setStampPrecedenceProperty(
           ObjectProperty<StampPrecedence> stampPrecedenceProperty) {
      final ChangeListener<StampPrecedence> listener = (observable, oldValue, newValue) -> {
               this.stampPrecedence = newValue;
            };

      stampPrecedenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- inner classes -------------------------------------------------------

   private static class ConceptSequenceSetAdapter
           extends XmlAdapter<int[], ConceptSequenceSet> {
      @Override
	public int[] marshal(ConceptSequenceSet c) {
         return c.asArray();
      }

      @Override
      public ConceptSequenceSet unmarshal(int[] v)
               throws Exception {
         return ConceptSequenceSet.of(v);
      }
   }


   private static class EnumSetAdapter
           extends XmlAdapter<State[], EnumSet<State>> {
      @Override
	public State[] marshal(EnumSet<State> c) {
         return c.toArray(new State[c.size()]);
      }

      @Override
      public EnumSet<State> unmarshal(State[] v)
               throws Exception {
         final EnumSet<State> s = EnumSet.noneOf(State.class);

         s.addAll(Arrays.asList(v));
         return s;
      }
   }
}

