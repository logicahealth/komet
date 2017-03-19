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



package sh.isaac.api.util;

/**
 * The Class Hashcode.
 *
 * @author kec
 */
public class Hashcode {
   
   /**
    * Compute.
    *
    * @param parts the parts
    * @return the int
    */
   public static int compute(int... parts) {
      int hash = 0;
      final int len  = parts.length;

      for (int i = 0; i < len; i++) {
         hash <<= 1;

         if (hash < 0) {
            hash |= 1;
         }

         hash ^= parts[i];
      }

      return hash;
   }

   /**
    * Compute.
    *
    * @param parts the parts
    * @return the short
    */
   public static short compute(short... parts) {
      short hash = 0;

      for (int i = 0; i < parts.length; i++) {
         hash <<= 1;

         if (hash < 0) {
            hash |= 1;
         }

         hash ^= parts[i];
      }

      return hash;
   }

   /**
    * Compute long.
    *
    * @param parts the parts
    * @return the int
    */
   public static int computeLong(long... parts) {
      final int[] intParts = new int[parts.length * 2];

      for (int i = 0; i < parts.length; i++) {
         intParts[i * 2]     = (int) parts[i];
         intParts[i * 2 + 1] = (int) (parts[i] >>> 32);
      }

      return compute(intParts);
   }

   /**
    * Int hash to short hash.
    *
    * @param hash the hash
    * @return the short
    */
   public static short intHashToShortHash(int hash) {
      final short[] parts = new short[2];

      parts[0] = (short) hash;          // low order short
      parts[1] = (short) (hash >> 16);  // high order short
      return compute(parts);
   }
}

