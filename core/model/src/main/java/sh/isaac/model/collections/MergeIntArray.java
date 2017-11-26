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
package sh.isaac.model.collections;

import java.util.Arrays;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class MergeIntArray {
   
   public static int[] mergeSlow(int[] existing, int[] update) {
      if (existing == null) {
         return update;
      }
      if (update.length == 1) {
         int updateValue = update[0];
         int searchResult = Arrays.binarySearch(existing, updateValue);
         if (searchResult >= 0) {
            return existing; // already there. 
         }
         int[] array2 = new int[existing.length + 1];
         int insertIndex = -searchResult - 1;
         System.arraycopy(existing, 0, array2, 0, insertIndex);
         System.arraycopy(existing, insertIndex, array2, insertIndex + 1, existing.length - insertIndex);
         array2[insertIndex] = updateValue;
         return array2;
      }

      OpenIntHashSet mergedSet = new OpenIntHashSet();

      for (int key: existing) {
         mergedSet.add(key);
      }

      for (int key: update) {
         mergedSet.add(key);
      }
      IntArrayList keys = mergedSet.keys();
      keys.sort();
      return keys.elements();
   }

   public static int[] merge(int[] currentArray, int[] updateArray) {
      if (currentArray == null || currentArray.length == 0) {
         return updateArray;
      }
      if (updateArray == null) {
         throw new IllegalStateException("Update value is null");
      }
      if (updateArray.length == 1) {
         int updateValue = updateArray[0];
         int searchResult = Arrays.binarySearch(currentArray, updateValue);
         if (searchResult >= 0) {
            return currentArray; // already there. 
         }
         int[] array2 = new int[currentArray.length + 1];
         int insertIndex = -searchResult - 1;
         System.arraycopy(currentArray, 0, array2, 0, insertIndex);
         System.arraycopy(currentArray, insertIndex, array2, insertIndex + 1, currentArray.length - insertIndex);
         array2[insertIndex] = updateValue;
         return array2;
      }
      Arrays.sort(updateArray);
      IntArrayList mergedValues = new IntArrayList(currentArray.length + updateArray.length);
      int updateIndex = 0;
      int currentIndex = 0;

      while (updateIndex < updateArray.length || currentIndex < currentArray.length) {
         int compare = Integer.compare(currentArray[currentIndex], updateArray[updateIndex]);
         if (compare == 0) {
            mergedValues.add(currentArray[currentIndex]);
            currentIndex++;
            updateIndex++;
            if (currentIndex == currentArray.length) {
               while (updateIndex < updateArray.length) {
                  mergedValues.add(updateArray[updateIndex++]);
               }
            }
            if (updateIndex == updateArray.length) {
               while (currentIndex < currentArray.length) {
                  mergedValues.add(currentArray[currentIndex++]);
               }
            }
         } else if (compare < 0) {
            mergedValues.add(currentArray[currentIndex]);
            currentIndex++;
            if (currentIndex == currentArray.length) {
               while (updateIndex < updateArray.length) {
                  mergedValues.add(updateArray[updateIndex++]);
               }
            }
         } else {
            mergedValues.add(updateArray[updateIndex]);
            updateIndex++;
            if (updateIndex == updateArray.length) {
               while (currentIndex < currentArray.length) {
                  mergedValues.add(currentArray[currentIndex++]);
               }
            }
         }
      }
      mergedValues.trimToSize();
      return mergedValues.elements();
   }

}
