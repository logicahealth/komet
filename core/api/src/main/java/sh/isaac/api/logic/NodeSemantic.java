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
   NECESSARY_SET(),
   SUFFICIENT_SET(),
   AND(),
   OR(),
   DISJOINT_WITH(),
   DEFINITION_ROOT(),
   ROLE_ALL(),
   ROLE_SOME(),
   CONCEPT(),
   FEATURE(),
   LITERAL_BOOLEAN(),
   LITERAL_FLOAT(),
   LITERAL_INSTANT(),
   LITERAL_INTEGER(),
   LITERAL_STRING(),
   TEMPLATE(),
   SUBSTITUTION_CONCEPT(),
   SUBSTITUTION_BOOLEAN(),
   SUBSTITUTION_FLOAT(),
   SUBSTITUTION_INSTANT(),
   SUBSTITUTION_INTEGER(),
   SUBSTITUTION_STRING()
   ;

   int  conceptSequence = Integer.MIN_VALUE;
   UUID semanticUuid;

   //~--- constructors --------------------------------------------------------

   private NodeSemantic() {
      this.semanticUuid = UuidT5Generator.get(UUID.fromString("8a834ec8-028d-11e5-a322-1697f925ec7b"),
            this.getClass()
                .getName() + this.name());
   }

   //~--- methods -------------------------------------------------------------

   static void reset() {
      for (final NodeSemantic nodeSemantic: NodeSemantic.values()) {
         nodeSemantic.conceptSequence = Integer.MIN_VALUE;
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getConceptSequence() {
      if (this.conceptSequence == Integer.MIN_VALUE) {
         this.conceptSequence = Get.identifierService()
                                   .getConceptSequenceForUuids(this.semanticUuid);
      }

      return this.conceptSequence;
   }

   public UUID getSemanticUuid() {
      return this.semanticUuid;
   }
}

