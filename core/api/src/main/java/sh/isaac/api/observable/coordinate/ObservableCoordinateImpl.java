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

package sh.isaac.api.observable.coordinate;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValueBase;

/**
 * The implementation code that actually makes all of these "Observable" coordinates observable as a whole, 
 * instead of just a wrapper of a bunch of other observables.
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public abstract class ObservableCoordinateImpl extends ObservableValueBase<ObservableCoordinate> implements ObservableCoordinate {
   private final List<Object> listenerReferences = new ArrayList<>();

   /**
    * Convenience method for holding listener references to prevent garbage collection.
    *
    * @param listener the listener
    */
   protected void addListenerReference(Object listener) {
      this.listenerReferences.add(listener);
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public ObservableCoordinate getValue() {
      return this;
   }
}
