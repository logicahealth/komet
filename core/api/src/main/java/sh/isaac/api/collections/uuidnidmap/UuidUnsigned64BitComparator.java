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



package sh.isaac.api.collections.uuidnidmap;

/**
 * The Class UuidUnsigned64BitComparator.
 *
 * @author kec
 */
public class UuidUnsigned64BitComparator
         implements UuidComparatorBI {
   
   /**
    * This algorithm performs unsigned 64 bit comparison of the msb and lsb of 2 uuids. This method is based
    * on the following routine:
    * {@code
    * public static boolean isLessThanUnsigned(long n1, long n2) {
    * return (n1 &lt; n2) ^ ((n1 &lt; 0) != (n2 &lt; 0));
    * }
    * } see: http://www.javamex.com/java_equivalents/unsigned_arithmetic.shtml
    *
    * @param msb1 the msb 1
    * @param lsb1 the lsb 1
    * @param msb2 the msb 2
    * @param lsb2 the lsb 2
    * @return the int
    */
   @Override
   public int compare(long msb1, long lsb1, long msb2, long lsb2) {
      if (msb1 == msb2) {
         if (lsb1 == lsb2) {
            return 0;
         }

         if ((lsb1 < lsb2) ^ ((lsb1 < 0) != (lsb2 < 0))) {
            return -1;
         }

         return 1;
      }

      if ((msb1 < msb2) ^ ((msb1 < 0) != (msb2 < 0))) {
         return -1;
      }

      return 1;
   }
}

