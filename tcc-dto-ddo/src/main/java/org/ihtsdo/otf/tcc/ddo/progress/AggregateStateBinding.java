/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.ddo.progress;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static javafx.concurrent.Worker.State;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 *
 * @author kec
 */
public class AggregateStateBinding extends ObjectBinding<State> {

   /** Field description */
   ReadOnlyObjectProperty<State>[] items;

   /**
    * Constructs ...
    *
    *
    * @param items
    */
   public AggregateStateBinding(ReadOnlyObjectProperty<State>[] items) {
      this.items = items;
      super.bind(items);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   protected State computeValue() {
      EnumSet<State> states = EnumSet.noneOf(State.class);

      for (ReadOnlyObjectProperty<State> item : items) {
         states.add(item.getValue());
      }

      if (states.contains(State.RUNNING)) {
         return State.RUNNING;
      }
      if (states.contains(State.CANCELLED)) {
         return State.CANCELLED;
      }
      if (states.contains(State.FAILED)) {
         return State.FAILED;
      }

      if (states.contains(State.READY)) {
          if (!states.contains(State.SCHEDULED) 
              && !states.contains(State.SUCCEEDED)) {
             return State.READY;
          }
          return State.RUNNING; 
      }
      if (states.contains(State.SCHEDULED)) {
          if (!!states.contains(State.SUCCEEDED)) {
             return State.SCHEDULED;
          }
          return State.RUNNING; 
      }
     if (states.contains(State.SUCCEEDED)) {
          return State.SUCCEEDED; 
      }
      throw new IllegalStateException("Can't compute aggregate state: " + getDependencies());
      
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public ObservableList<?> getDependencies() {
      return FXCollections.observableArrayList(items);
   }
}
