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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.provider.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.EnumSet;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.coordinate.TaxonomyCoordinate;

//~--- enums ------------------------------------------------------------------

/**
 * An enum of flags used by taxonomy records to indicate if the specified
 * concept is either a parent of, or child of, or other type of relationship.
 * These flags are designed to support a bit representation within the top 8
 * bits of a 32 bit integer (without interfering with the sign bit), enabling
 * multiple flags to be associated with a STAMP value within a single integer.
 *
 * @author kec
 */
public enum TaxonomyFlags {
   STATED(0x10000000),                 // 0001 0000
   INFERRED(0x20000000),               // 0010 0000
   SEMEME(0x40000000),                 // 0100 0000
   NON_DL_REL(0x08000000),             // 0000 1000
   CONCEPT_STATUS(0x04000000),         // 0000 0100
   RESERVED_FUTURE_USE_1(0x02000000),  // 0000 0010
   RESERVED_FUTURE_USE_2(0x01000000);  // 0000 0001

   public static final int ALL_RELS = 0;

   //~--- fields --------------------------------------------------------------

   public final int bits;

   //~--- constructors --------------------------------------------------------

   TaxonomyFlags(int bits) {
      this.bits = bits;
   }

   //~--- get methods ---------------------------------------------------------

   private static EnumSet<TaxonomyFlags> getFlags(int justFlags) {
      EnumSet<TaxonomyFlags> flagSet = EnumSet.noneOf(TaxonomyFlags.class);

      Arrays.stream(TaxonomyFlags.values()).forEach((flag) -> {
                        if ((justFlags & flag.bits) == flag.bits) {
                           flagSet.add(flag);
                        }
                     });
      return flagSet;
   }

   public static int getFlagsFromTaxonomyCoordinate(TaxonomyCoordinate viewCoordinate) {
      switch (viewCoordinate.getTaxonomyType()) {
      case INFERRED:
         return TaxonomyFlags.INFERRED.bits;

      case STATED:
         return TaxonomyFlags.STATED.bits;

      default:
         throw new UnsupportedOperationException("no support for: " + viewCoordinate.getTaxonomyType());
      }
   }

   public static EnumSet<TaxonomyFlags> getTaxonomyFlags(int stampWithFlags) {
      if (stampWithFlags < 512) {
         stampWithFlags = stampWithFlags << 24;
      }

      return getFlags(stampWithFlags);
   }

   public static int getTaxonomyFlagsAsInt(EnumSet<TaxonomyFlags> flagSet) {
      int flags = 0;

      for (TaxonomyFlags flag: flagSet) {
         flags += flag.bits;
      }

      return flags;
   }
}

