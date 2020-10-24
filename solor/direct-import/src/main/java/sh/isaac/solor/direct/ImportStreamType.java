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



package sh.isaac.solor.direct;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.VersionType;

//~--- enums ------------------------------------------------------------------

/**
 *
 * @author kec
 */
public enum ImportStreamType {
   //Dan notes, the import sort order is controlled by this enum order. 
   CONCEPT,
   DESCRIPTION,
   DIALECT,
   STATED_RELATIONSHIP,
   INFERRED_RELATIONSHIP,
   ALTERNATIVE_IDENTIFIER,
   NID1_NID2_INT3_REFSET,
   NID1_INT2_REFSET,
   NID1_LONG2_REFSET,
   NID1_INT2_STR3_STR4_NID5_NID6_REFSET,
   NID1_REFSET,
   STR1_STR2_NID3_NID4_REFSET,
   STR1_STR2_REFSET,
   STR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET,
   MEMBER_REFSET,
   INT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET,
   STR1_REFSET,
   NID1_NID2_REFSET,
   NID1_NID2_STR3_REFSET,
   NID1_STR2_REFSET,
   INT1_REFSET,
   STR1_NID2_NID3_NID4_REFSET,
   STR1_STR2_NID3_NID4_NID5_REFSET,
   DYNAMIC,
   RXNORM_CONSO,
   LOINC,
   CLINVAR,
   CVX,
   LIVD,
   SRF_CONCEPT,
   SRF_DESCRIPTION,
   SRF_STATED_RELATIONSHIP,
   SRF_INFERRED_RELATIONSHIP,
   SRF_NID1_NID2_INT3_ASSEMBLAGE,
   SRF_NID1_INT2_ASSEMBLAGE,
   SRF_NID1_LONG2_ASSEMBLAGE,
   SRF_NID1_INT2_STR3_STR4_NID5_NID6_ASSEMBLAGE,
   SRF_NID1_ASSEMBLAGE,
   SRF_STR1_STR2_NID3_NID4_ASSEMBLAGE,
   SRF_STR1_STR2_ASSEMBLAGE,
   SRF_STR1_STR2_STR3_STR4_STR5_STR6_STR7_ASSEMBLAGE,
   SRF_MEMBER_ASSEMBLAGE,
   SRF_INT1_INT2_STR3_STR4_STR5_NID6_NID7_ASSEMBLAGE,
   SRF_STR1_ASSEMBLAGE,
   SRF_NID1_NID2_ASSEMBLAGE,
   SRF_NID1_NID2_STR3_ASSEMBLAGE,
   SRF_NID1_STR2_ASSEMBLAGE,
   SRF_INT1_ASSEMBLAGE,
   SRF_STR1_NID2_NID3_NID4_ASSEMBLAGE,
   SRF_STR1_STR2_NID3_NID4_NID5_ASSEMBLAGE
   ;

   public VersionType getSemanticVersionType() {
      switch (this) {
         case SRF_NID1_NID2_INT3_ASSEMBLAGE:
         case NID1_NID2_INT3_REFSET:
            return VersionType.Nid1_Nid2_Int3;

         case SRF_NID1_INT2_ASSEMBLAGE:
         case NID1_INT2_REFSET:
            return VersionType.Nid1_Int2;

         case SRF_NID1_LONG2_ASSEMBLAGE:
         case NID1_LONG2_REFSET:
            return VersionType.Nid1_Long2;

         case SRF_NID1_INT2_STR3_STR4_NID5_NID6_ASSEMBLAGE:
         case NID1_INT2_STR3_STR4_NID5_NID6_REFSET:
            return VersionType.Nid1_Int2_Str3_Str4_Nid5_Nid6;

         case SRF_NID1_ASSEMBLAGE:
         case NID1_REFSET:
            return VersionType.COMPONENT_NID;

         case SRF_STR1_STR2_NID3_NID4_ASSEMBLAGE:
         case STR1_STR2_NID3_NID4_REFSET:
            return VersionType.Str1_Str2_Nid3_Nid4;

         case SRF_STR1_STR2_ASSEMBLAGE:
         case STR1_STR2_REFSET:
            return VersionType.Str1_Str2;

         case SRF_STR1_STR2_STR3_STR4_STR5_STR6_STR7_ASSEMBLAGE:
         case STR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET:
            return VersionType.Str1_Str2_Str3_Str4_Str5_Str6_Str7;

         case SRF_MEMBER_ASSEMBLAGE:
         case MEMBER_REFSET:
            return VersionType.MEMBER;

         case SRF_INT1_INT2_STR3_STR4_STR5_NID6_NID7_ASSEMBLAGE:
         case INT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET:
            return VersionType.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7;

         case SRF_STR1_ASSEMBLAGE:
         case STR1_REFSET:
            return VersionType.STRING;

         case SRF_NID1_NID2_ASSEMBLAGE:
         case NID1_NID2_REFSET:
            return VersionType.Nid1_Nid2;

         case SRF_NID1_NID2_STR3_ASSEMBLAGE:
         case NID1_NID2_STR3_REFSET:
            return VersionType.Nid1_Nid2_Str3;

         case SRF_NID1_STR2_ASSEMBLAGE:
         case NID1_STR2_REFSET:
            return VersionType.Nid1_Str2;

         case SRF_INT1_ASSEMBLAGE:
         case INT1_REFSET:
            return VersionType.LONG;

         case SRF_STR1_NID2_NID3_NID4_ASSEMBLAGE:
         case STR1_NID2_NID3_NID4_REFSET:
             return VersionType.Str1_Nid2_Nid3_Nid4;

         case SRF_STR1_STR2_NID3_NID4_NID5_ASSEMBLAGE:
         case STR1_STR2_NID3_NID4_NID5_REFSET:
             return VersionType.Str1_Str2_Nid3_Nid4_Nid5;

         case DYNAMIC:
             return VersionType.DYNAMIC;

         case ALTERNATIVE_IDENTIFIER:
         case CONCEPT:
         case DESCRIPTION:
         case DIALECT:
         case INFERRED_RELATIONSHIP:
         case STATED_RELATIONSHIP:
         default :
            throw new UnsupportedOperationException("No version type for: " + this);
      }
   }
}

