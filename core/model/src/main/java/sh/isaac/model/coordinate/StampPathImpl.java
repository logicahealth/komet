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



package sh.isaac.model.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class StampPathImpl
         implements StampPath {
   private final int pathConceptSequence;

   //~--- constructors --------------------------------------------------------

   public StampPathImpl(int pathConceptSequence) {
      if (pathConceptSequence < 0) {
         pathConceptSequence = Get.identifierService()
                                  .getConceptSequence(pathConceptSequence);
      }

      this.pathConceptSequence = pathConceptSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(StampPath o) {
      return Integer.compare(this.pathConceptSequence, o.getPathConceptSequence());
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getPathConceptSequence() {
      return this.pathConceptSequence;
   }

   @Override
   public Collection<? extends StampPosition> getPathOrigins() {
      return Get.pathService()
                .getOrigins(this.pathConceptSequence);
   }
}

