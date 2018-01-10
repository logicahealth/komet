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

package sh.isaac.convert.mojo.rf2;

import java.util.TreeSet;
import java.util.UUID;

/**
 * A group of relationships that all have the same ID (different states of the same rel across time)
 * {@link RelBatch}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RelBatch
{
   private TreeSet<Rel> rels_ = new TreeSet<>();
   
   protected RelBatch(Rel r)
   {
      rels_.add(r);
   }
   
   protected UUID getBatchId()
   {
      return rels_.first().id;
   }
   
   protected UUID getSourceId()
   {
      return rels_.first().sourceId;
   }
   
   protected boolean isActiveNow()
   {
      return rels_.last().isActive;
   }
   
   protected void addRel(Rel rel)
   {
      rels_.add(rel);
      if (!rels_.first().sourceId.equals(rel.sourceId))
      {
         throw new RuntimeException("oops!");
      }
   }
   
   /**
    * ordered by effectiveTime
    * @return
    */
   protected TreeSet<Rel> getRels()
   {
      return rels_;
   }
}
