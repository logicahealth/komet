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



package gov.vha.isaac.rf2.convert.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.util.TreeSet;
import java.util.UUID;

//~--- classes ----------------------------------------------------------------

/**
 * A group of relationships that all have the same ID (different states of the same rel across time)
 * {@link RelBatch}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RelBatch {
   /** The rels. */
   private final TreeSet<Rel> rels = new TreeSet<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new rel batch.
    *
    * @param r the r
    */
   protected RelBatch(Rel r) {
      this.rels.add(r);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "RelBatch{" + this.rels + '}';
   }

   /**
    * Adds the rel.
    *
    * @param rel the rel
    */
   protected void addRel(Rel rel) {
      this.rels.add(rel);

      if (!this.rels.first().sourceId
                     .equals(rel.sourceId)) {
         throw new RuntimeException("oops!");
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if active now.
    *
    * @return true, if active now
    */
   protected boolean isActiveNow() {
      return this.rels.last().isActive;
   }

   /**
    * Gets the batch id.
    *
    * @return the batch id
    */
   protected UUID getBatchId() {
      return this.rels.first().id;
   }

   /**
    * ordered by effectiveTime.
    *
    * @return the rels
    */
   protected TreeSet<Rel> getRels() {
      return this.rels;
   }

   /**
    * Gets the source id.
    *
    * @return the source id
    */
   protected UUID getSourceId() {
      return this.rels.first().sourceId;
   }
}

