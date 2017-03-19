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



package sh.isaac.utility;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

//~--- classes ----------------------------------------------------------------

/**
 * {@link SctId} contains validation utilities for SNOMED ID (SCT ID). A unique long
 * Identifier applied to each SNOMED CT component ( Concept, Description, Relationship, Subset, etc.).
 * The SCTID data type is a 64-bit integer, which is subject to the following constraints:
 * - Only positive integer values are permitted.
 * - The minimum permitted value is 100,000 (6 digits)
 * - The maximum permitted value is 999,999,999,999,999,999 (18-digits).
 * 
 * As a result of rules for the partition-identifier and check-digit, many integers within this range are not valid SCTIDs.
 * 
 * Extension SCTID (MSD)Extension Item ID (18-11)Namespace ID(10-4)Partition ID(3-2)Check-digit(1)(LSD)
 * 
 * In java, the SCTID is handled as a long.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @see <a href="http://www.snomed.org/tig?t=trg2main_sctid">IHTSDO Technical Implementation Guide - SCT ID</a>
 */
public class SctId {
   
   /** The Fn F. */
   // parts of the SCTID algorithm
   private static int[][] FnF_       = {
      {
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9
      }, {
         1, 5, 7, 6, 2, 8, 3, 0, 9, 4
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }, {
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0
      }
   };
   
   /** The Dihedral. */
   private static int[][] Dihedral_  = {
      {
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9
      }, {
         1, 2, 3, 4, 0, 6, 7, 8, 9, 5
      }, {
         2, 3, 4, 0, 1, 7, 8, 9, 5, 6
      }, {
         3, 4, 0, 1, 2, 8, 9, 5, 6, 7
      }, {
         4, 0, 1, 2, 3, 9, 5, 6, 7, 8
      }, {
         5, 9, 8, 7, 6, 0, 4, 3, 2, 1
      }, {
         6, 5, 9, 8, 7, 1, 0, 4, 3, 2
      }, {
         7, 6, 5, 9, 8, 2, 1, 0, 4, 3
      }, {
         8, 7, 6, 5, 9, 3, 2, 1, 0, 4
      }, {
         9, 8, 7, 6, 5, 4, 3, 2, 1, 0
      }
   };
   

   //~--- static initializers -------------------------------------------------

   static {
      for (int i = 2; i < 8; i++) {
         for (int j = 0; j < 10; j++) {
            FnF_[i][j] = FnF_[i - 1][FnF_[1][j]];
         }
      }
   }

   //~--- enums ---------------------------------------------------------------

   /**
    * The Enum TYPE listing the possible types of SCT IDs. The second and third
    * digits from the right of the string rendering of the SCTID. The value of
    * the partition-identifier indicates the type of component that the SCTID
    * identifies (e.g. Concept, Description, Relationship, etc) and also
    * indicates whether the SCTID contains a namespace identifier.
    *
    */
   public static enum TYPE {
      /**
       * Identifies the SCT ID as a concept.
       */
      CONCEPT("10"),

      /**
       * Identifies the SCT ID as a description.
       */
      DESCRIPTION("11"),

      /**
       * Identifies the SCT ID as a relationship.
       */
      RELATIONSHIP("12"),

      /**
       * Identifies the SCT ID as a refset.
       */
      SUBSET("13");

      /** The digits. */
      private final String digits_;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new SCT ID type based on the <code>digits</code>.
       *
       * @param digits the digits specifying the SCT ID type
       */
      private TYPE(String digits) {
         this.digits_ = digits;
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Gets the digits specifying the SCT ID type.
       *
       * @return the digits specifying the SCT ID type
       */
      public String getDigits() {
         return this.digits_;
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * see {@link #isValidSctId(String)}.
    *
    * @param sctid the sctid
    * @return true, if valid SCTID
    */
   public static boolean isValidSCTID(int sctid) {
      return isValidSctId(Integer.toString(sctid));
   }

   /**
    * see {@link #isValidSctId(String)}.
    *
    * @param sctid the sctid
    * @return true, if valid sct id
    */
   public static boolean isValidSctId(long sctid) {
      return isValidSctId(Long.toString(sctid));
   }

   // TODO validate this code / make clean APIs when it is needed.
// /**
//  * Generates an SCT ID based on the given <code>sequence</code>, <code>projectId</code>, <code>namespaceId</code>, and <code>type</code>.
//  *
//  * @param sequence the sequence to use for the item identifier
//  * @param projectId the id of the mapping project
//  * @param namespaceId the <code>int</code> representation of the namespace to use
//  * @param type the SCT ID type
//  * @return a string representation of the generated SCT ID
//  */
// public static String generate(long sequence, int projectId, int namespaceId, TYPE type)
// {
//
//         if (sequence <= 0)
//         {
//                 throw new RuntimeException("sequence must be > 0");
//         }
//
//         String mergedid = Long.toString(sequence) + projectId + namespaceId + type.digits_;
//
//         return mergedid + verhoeffCompute(mergedid);
// }
//
// /**
//  * Generates an SCT ID based on the given <code>sequence</code>, <code>namespaceString</code>, and <code>type</code>.
//  *
//  * @param sequence the sequence to use for the item identifier
//  * @param namespaceString the <code>String</code> representation of the namespace to use
//  * @param type the SCT ID type
//  * @return a string representation of the generated SCT ID
//  */
// public static String generate(long sequence, String namespaceString, TYPE type)
// {
//         if (sequence <= 0)
//         {
//                 throw new RuntimeException("sequence must be > 0");
//         }
//         String mergedid = Long.toString(sequence) + namespaceString + type.digits_;
//         return mergedid + verhoeffCompute(mergedid);
// }
//
// /**
//  * Generates an SCT ID based on the given <code>sequence</code>, <code>namespaceId</code>, and <code>type</code>.
//  *
//  * @param sequence the sequence to use for the item identifier
//  * @param namespaceId the <code>int</code> representation of the namespace to use
//  * @param type the SCT ID type
//  * @return a string representation of the generated SCT ID
//  */
// public static String generate(long sequence, int namespaceId, TYPE type)
// {
//
//         if (sequence <= 0)
//         {
//                 throw new RuntimeException("sequence must be > 0");
//         }
//
//         String mergedid = Long.toString(sequence) + namespaceId + type.digits_;
//
//         return mergedid + verhoeffCompute(mergedid);
// }
// /**
//  * Computes the check digit. The SCTID (See Component features - Identifiers) includes a check-digit, which is generated using Verhoeff's
//  * dihedral check.
//  *
//  * @param idAsString a String representation of the SCT ID
//  * @return the generated SCT ID
//  * @see <a href="http://www.snomed.org/tig?t=trg_app_check_digit">IHTSDO Technical Implementation Guide - Verhoeff</a>
//  */
// public static long verhoeffCompute(String idAsString)
// {
//         int check = 0;
//         for (int i = idAsString.length() - 1; i >= 0; i--)
//         {
//                 check = Dihedral_[check][FnF_[((idAsString.length() - i) % 8)][new Integer(new String(new char[] { idAsString.charAt(i) }))]];
//
//         }
//         return InverseD5_[check];
// }

   /**
    * Verifies the check digit of an SCT identifier.
    *
    * @param idAsString a String representation of the SCT ID
    * @return <code>true</code>, if the checksum in the string is correct for an SCTID.
    * @see <a href="http://www.snomed.org/tig?t=trg_app_check_digit">IHTSDO Technical Implementation Guide - Verhoeff</a>
    */
   public static boolean isValidSctId(String idAsString) {
      if (StringUtils.isBlank(idAsString)) {
         return false;
      }

      try {
         final long l = Long.parseLong(idAsString);

         if ((l < 100000) || (l > 999999999999999999l)) {
            return false;
         }
      } catch (final NumberFormatException e) {
         return false;
      }

      int check = 0;

      for (int i = idAsString.length() - 1; i >= 0; i--) {
         check =
            Dihedral_[check][FnF_[(idAsString.length() - i - 1) % 8][new Integer(new String(new char[] { idAsString.charAt(i) }))]];
      }

      if (check != 0) {
         return false;
      } else {
         return true;
      }
   }
}

