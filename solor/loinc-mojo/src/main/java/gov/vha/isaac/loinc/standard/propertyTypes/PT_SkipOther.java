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



package gov.vha.isaac.loinc.standard.propertyTypes;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Skip;

//~--- classes ----------------------------------------------------------------

/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public class PT_SkipOther
        extends BPT_Skip {
   /**
    * Instantiates a new p T skip other.
    *
    * @param skipLists the skip lists
    */
   @SafeVarargs
   public PT_SkipOther(List<String>... skipLists) {
      super("Skip Other");
      addSkipListEntries(skipLists);

      // Not Loaded
      addProperty("SOURCE");
      addProperty("FINAL", 0, 1, true);  // deleted in 2.38
   }
}

