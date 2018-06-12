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

import javax.xml.bind.annotation.adapters.XmlAdapter;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampCoordinateImpl.
 *
 * @author kec
 */
public class StampCoordinateImpl
         implements StampCoordinate {
   /** The stamp precedence. */
   StampPrecedence stampPrecedence;

   /** The stamp position. */
   StampPosition stampPosition;

   /** The module sequences. */
   NidSet moduleNids;

   /** The allowed states. */
   EnumSet<Status> allowedStates;
   
   int[] modulePriorityList;
   
   private StampCoordinateImmutableWrapper stampCoordinateImmutable = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new stamp coordinate impl.
    */
   private StampCoordinateImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new stamp coordinate impl.
    *
    * @param stampPrecedence the stamp precedence
    * @param stampPosition the stamp position
    * @param moduleNids the module nids to include in the version computation
     * @param modulePriorityList empty if no preference, or module nids in the priority
     * order that should be used if a version computation returns two different versions
     * for different modules. 
    * @param allowedStates the allowed states
    */
   public StampCoordinateImpl(StampPrecedence stampPrecedence,
                              StampPosition stampPosition,
                              NidSet moduleNids,
                              int[] modulePriorityList,
                              EnumSet<Status> allowedStates) {
      this.stampPrecedence = stampPrecedence;
      this.stampPosition   = stampPosition;
      this.moduleNids = moduleNids;
      this.modulePriorityList = modulePriorityList;
      this.allowedStates   = allowedStates;

      if (this.moduleNids == null) {
         this.moduleNids = new NidSet();
      }
   }

   /**
    * Instantiates a new stamp coordinate impl.
    *
    * @param stampPrecedence the stamp precedence
    * @param stampPosition the stamp position
    * @param moduleSpecifications the module specifications
    * @param moduleSpecificationPriorities the priority of the modules to use 
    * when contradictions is encountered. 
    * @param allowedStates the allowed states
    */
   public StampCoordinateImpl(StampPrecedence stampPrecedence,
                              StampPosition stampPosition,
                              List<ConceptSpecification> moduleSpecifications,
                              List<ConceptSpecification> moduleSpecificationPriorities,
                              EnumSet<Status> allowedStates) {
      this(stampPrecedence,
           stampPosition,
           NidSet.of(moduleSpecifications.stream()
                 .mapToInt((spec) -> spec.getNid())),
           moduleSpecificationPriorities.stream().mapToInt((spec) -> spec.getNid()).toArray(),
           allowedStates);
   }

   public StampCoordinateImpl(StampPrecedence stampPrecedence,
                              StampPosition stampPosition,
                              List<ConceptSpecification> moduleSpecifications,
                              int[] moduleSpecificationPriorities,
                              EnumSet<Status> allowedStates) {
      this(stampPrecedence,
           stampPosition,
           NidSet.of(moduleSpecifications.stream()
                 .mapToInt((spec) -> spec.getNid())),
           moduleSpecificationPriorities,
           allowedStates);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public StampCoordinate getImmutableAllStateAnalog() {
       StampCoordinateImmutableWrapper coordinate = this.stampCoordinateImmutable;
       if (coordinate != null) {
           return coordinate;
       }
       coordinate = new StampCoordinateImmutableWrapper(this);
       this.stampCoordinateImmutable = coordinate;
       return coordinate;
   }

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
        
        return this.moduleNids.equals(other.moduleNids);
    }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 7;

      hash = 11 * hash + Objects.hashCode(this.stampPrecedence);
      hash = 11 * hash + Objects.hashCode(this.stampPosition);
      hash = 11 * hash + Objects.hashCode(this.moduleNids);
      hash = 11 * hash + Objects.hashCode(this.allowedStates);
      return hash;
   }

   /**
    * Make analog.
    *
    * @param stampPositionTime the stamp position time
    * @return the stamp coordinate impl
    */
   @Override
   public StampCoordinateImpl makeCoordinateAnalog(long stampPositionTime) {
      final StampPosition anotherStampPosition = new StampPositionImpl(stampPositionTime,
                                                                       this.stampPosition.getStampPathNid());

      return new StampCoordinateImpl(this.stampPrecedence,
                                     anotherStampPosition,
                                     this.moduleNids,
                                     this.modulePriorityList,
                                     this.allowedStates);
   }

   /**
    * Make analog.
    *
    * @param states the states
    * @return the stamp coordinate impl
    */
   @Override
   public StampCoordinateImpl makeCoordinateAnalog(Status... states) {
      final EnumSet<Status> newAllowedStates = EnumSet.noneOf(Status.class);

      newAllowedStates.addAll(Arrays.asList(states));
      return new StampCoordinateImpl(this.stampPrecedence, this.stampPosition, this.moduleNids, this.modulePriorityList, newAllowedStates);
   }
   
   @Override
   public StampCoordinate makeCoordinateAnalog(EnumSet<Status> states) {
      return new StampCoordinateImpl(this.stampPrecedence, this.stampPosition, this.moduleNids, this.modulePriorityList, states);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("Stamp Coordinate{")
             .append(this.stampPrecedence)
             .append(", ")
             .append(this.stampPosition)
             .append(", modules: ");

      if (this.moduleNids.isEmpty()) {
         builder.append("all, ");
      } else {
         builder.append(Get.conceptDescriptionTextList(this.moduleNids))
                .append(", ");
      }
      
      builder.append("module priorities: ");
      if (this.modulePriorityList == null || this.modulePriorityList.length == 0) {
         builder.append("none, ");
      } else {
         builder.append(Get.conceptDescriptionTextList(this.modulePriorityList))
                .append(", ");
      }

      builder.append(this.allowedStates)
             .append('}');
      return builder.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the allowed states.
    *
    * @return the allowed states
    */
   @Override
   public EnumSet<Status> getAllowedStates() {
      return this.allowedStates;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set allowed states property.
    *
    * @param allowedStatesProperty the allowed states property
    * @return the set change listener
    */
   public SetChangeListener<Status> setAllowedStatesProperty(SetProperty<Status> allowedStatesProperty) {
      final SetChangeListener<Status> listener = (change) -> {
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

   /**
    * Gets the module sequences.
    *
    * @return the module sequences
    */
   @Override
   public NidSet getModuleNids() {
      return this.moduleNids;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set module sequences property.
    *
    * @param moduleSequencesProperty the module sequences property
    * @return the array change listener
    */
   public ArrayChangeListener<ObservableIntegerArray> setModuleSequencesProperty(
           ObjectProperty<ObservableIntegerArray> moduleSequencesProperty) {
      final ArrayChangeListener<ObservableIntegerArray> listener = (ObservableIntegerArray observableArray,
                                                                    boolean sizeChanged,
                                                                    int from,
                                                                    int to) -> {
               this.moduleNids = NidSet.of(observableArray.toArray(new int[observableArray.size()]));
            };

      moduleSequencesProperty.getValue()
                             .addListener(new WeakArrayChangeListener(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stamp position.
    *
    * @return the stamp position
    */
   @Override
   public StampPosition getStampPosition() {
      return this.stampPosition;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stamp position property.
    *
    * @param stampPositionProperty the stamp position property
    * @return the change listener
    */
   public ChangeListener<ObservableStampPosition> setStampPositionProperty(
           ObjectProperty<ObservableStampPosition> stampPositionProperty) {
      final ChangeListener<ObservableStampPosition> listener = (observable, oldValue, newValue) -> {
               this.stampPosition = newValue;
            };

      stampPositionProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stamp precedence.
    *
    * @return the stamp precedence
    */
   @Override
   public StampPrecedence getStampPrecedence() {
      return this.stampPrecedence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stamp precedence property.
    *
    * @param stampPrecedenceProperty the stamp precedence property
    * @return the change listener
    */
   public ChangeListener<StampPrecedence> setStampPrecedenceProperty(
           ObjectProperty<StampPrecedence> stampPrecedenceProperty) {
      final ChangeListener<StampPrecedence> listener = (observable, oldValue, newValue) -> {
               this.stampPrecedence = newValue;
            };

      stampPrecedenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class ConceptSequenceSetAdapter.
    */
   private static class ConceptSequenceSetAdapter
           extends XmlAdapter<int[], NidSet> {
      /**
       * Marshal.
       *
       * @param c the c
       * @return the int[]
       */
      @Override
      public int[] marshal(NidSet c) {
         return c.asArray();
      }

      /**
       * Unmarshal.
       *
       * @param v the v
       * @return the concept sequence set
       * @throws Exception the exception
       */
      @Override
      public NidSet unmarshal(int[] v)
               throws Exception {
         return NidSet.of(v);
      }
   }


   /**
    * The Class EnumSetAdapter.
    */
   private static class EnumSetAdapter
           extends XmlAdapter<Status[], EnumSet<Status>> {
      /**
       * Marshal.
       *
       * @param c the c
       * @return the state[]
       */
      @Override
      public Status[] marshal(EnumSet<Status> c) {
         return c.toArray(new Status[c.size()]);
      }

      /**
       * Unmarshal.
       *
       * @param v the v
       * @return the enum set
       * @throws Exception the exception
       */
      @Override
      public EnumSet<Status> unmarshal(Status[] v)
               throws Exception {
         final EnumSet<Status> s = EnumSet.noneOf(Status.class);

         s.addAll(Arrays.asList(v));
         return s;
      }
   }
   
   @Override
   public StampCoordinateImpl deepClone() {
      StampCoordinateImpl newCoordinate = new StampCoordinateImpl(stampPrecedence,
                              stampPosition.deepClone(),
                              NidSet.of(moduleNids.stream()),
                              this.modulePriorityList.clone(),
                              EnumSet.copyOf(allowedStates));
      return newCoordinate;
   }

    @Override
    public int[] getModulePreferenceListForVersions() {
        return this.modulePriorityList;
    }
   
    public void setModulePreferenceListForVersions(int[] modulePriorityList) {
        this.modulePriorityList = modulePriorityList;
    }
   
}

