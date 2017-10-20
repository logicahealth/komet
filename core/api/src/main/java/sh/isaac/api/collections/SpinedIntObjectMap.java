/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.collections;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kec
 * @param <E> the generic type for the spined list. 
 */
public class SpinedIntObjectMap<E> {
   private static final int DEFAULT_SPINE_SIZE = 1024;
   private final int spineSize;
   private final List<Object[]> spines;

   public SpinedIntObjectMap() {
      this.spineSize = DEFAULT_SPINE_SIZE;
      this.spines = new ArrayList<>(spineSize);
      this.spines.add(new Object[spineSize]);
   }

   public void put(int index, E element) {
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      while (spineIndex > spines.size() - 1) {
         this.spines.add(new Object[spineSize]);
      }
      this.spines.get(spineIndex)[indexInSpine] = element;
   }

   public E get(int index) {
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return (E) this.spines.get(spineIndex)[indexInSpine];
   }
}
