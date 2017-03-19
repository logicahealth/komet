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

import java.lang.ref.WeakReference;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.WeakListener;

import javafx.collections.ArrayChangeListener;
import javafx.collections.ObservableIntegerArray;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class WeakArrayChangeListener
         implements WeakListener, ArrayChangeListener<ObservableIntegerArray> {
   private final WeakReference<ArrayChangeListener<ObservableIntegerArray>> ref;

   //~--- constructors --------------------------------------------------------

   public WeakArrayChangeListener(ArrayChangeListener<ObservableIntegerArray> listener) {
      this.ref = new WeakReference<>(listener);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void onChanged(ObservableIntegerArray observableArray, boolean sizeChanged, int from, int to) {
      final ArrayChangeListener<ObservableIntegerArray> listener = this.ref.get();

      if (listener != null) {
         listener.onChanged(observableArray, sizeChanged, from, to);
      } else {
         // The weakly reference listener has been garbage collected,
         // so this WeakListener will now unhook itself from the
         // source bean
         observableArray.removeListener(this);
      }
   }

   @Override
   public boolean wasGarbageCollected() {
      return (this.ref.get() == null);
   }
}

