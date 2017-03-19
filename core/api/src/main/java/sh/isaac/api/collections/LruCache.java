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



package sh.isaac.api.collections;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedHashMap;
import java.util.Map;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 5/29/15.
 *
 * @param <K> key type for this cache
 * @param <V> key value for this cache
 */
public class LruCache<K, V>
        extends LinkedHashMap<K, V> {
   /**
    *
    */
   private static final long serialVersionUID = -2584554176457193968L;

   //~--- fields --------------------------------------------------------------

   private final int capacity;  // Maximum number of items in the cache.

   //~--- constructors --------------------------------------------------------

   public LruCache(int capacity) {
      super(capacity + 1, 1.0f, true);  // Pass 'true' for accessOrder.
      this.capacity = capacity;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
      return (size() > this.capacity);
   }
}

