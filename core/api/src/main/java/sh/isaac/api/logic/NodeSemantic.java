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



package sh.isaac.api.logic;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.util.UuidT5Generator;

//~--- enums ------------------------------------------------------------------

/**
 * Created by kec on 12/6/14.
 */
public enum NodeSemantic {
   /** The necessary set. */
   NECESSARY_SET(),

   /** The sufficient set. */
   SUFFICIENT_SET(),

   /** The and. */
   AND(),

   /** The or. */
   OR(),

   /** The disjoint with. */
   DISJOINT_WITH(),

   /** The definition root. */
   DEFINITION_ROOT(),

   /** The role all. */
   ROLE_ALL(),

   /** The role some. */
   ROLE_SOME(),

   /** The concept. */
   CONCEPT(),

   /** The feature. */
   FEATURE(),

   /** The literal boolean. */
   LITERAL_BOOLEAN(),

   /** The literal float. */
   LITERAL_FLOAT(),

   /** The literal instant. */
   LITERAL_INSTANT(),

   /** The literal integer. */
   LITERAL_INTEGER(),

   /** The literal string. */
   LITERAL_STRING(),

   /** The template. */
   TEMPLATE(),

   /** The substitution concept. */
   SUBSTITUTION_CONCEPT(),

   /** The substitution boolean. */
   SUBSTITUTION_BOOLEAN(),

   /** The substitution float. */
   SUBSTITUTION_FLOAT(),

   /** The substitution instant. */
   SUBSTITUTION_INSTANT(),

   /** The substitution integer. */
   SUBSTITUTION_INTEGER(),

   /** The substitution string. */
   SUBSTITUTION_STRING()
   ;

   /** The concept sequence. */
   int conceptNid = Integer.MIN_VALUE;

   /** The semantic uuid. */
   UUID semanticUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new node semantic.
    */
   private NodeSemantic() {
      this.semanticUuid = UuidT5Generator.get(UUID.fromString("8a834ec8-028d-11e5-a322-1697f925ec7b"),
            this.getClass()
                .getName() + this.name());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Reset.
    */
   static void reset() {
      for (final NodeSemantic nodeSemantic: NodeSemantic.values()) {
         nodeSemantic.conceptNid = Integer.MIN_VALUE;
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept sequence.
    *
    * @return the concept sequence
    */
   public int getConceptNid() {
      if (this.conceptNid == Integer.MIN_VALUE) {
         this.conceptNid = Get.identifierService()
                                   .getNidForUuids(this.semanticUuid);
      }

      return this.conceptNid;
   }

   /**
    * Gets the semantic uuid.
    *
    * @return the semantic uuid
    */
   public UUID getSemanticUuid() {
      return this.semanticUuid;
   }
}

