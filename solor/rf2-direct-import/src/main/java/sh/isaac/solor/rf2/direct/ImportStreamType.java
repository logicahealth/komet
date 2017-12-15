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
package sh.isaac.solor.rf2.direct;

/**
 *
 * @author kec
 */
public enum ImportStreamType {
   CONCEPT, 
   DESCRIPTION, 
   DIALECT,
   STATED_RELATIONSHIP, 
   INFERRED_RELATIONSHIP, 
   ALTERNATIVE_IDENTIFIER, 
   
   NID1_NID2_INT3_REFSET,
   NID1_INT2_REFSET,
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
   ;
}
