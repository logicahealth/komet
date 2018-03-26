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



package sh.isaac.provider.query.search;

//~--- JDK imports ------------------------------------------------------------

import java.util.Comparator;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.semantic.version.DescriptionVersion;

//~--- classes ----------------------------------------------------------------

/**
 * A {@link Comparator} for {@link DescriptionVersion} objects that compares the descriptions by their type.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DescriptionSemanticTypeComparator
         implements Comparator<DescriptionVersion> {

   //~--- methods -------------------------------------------------------------

   /**
    * Compare.
    *
    * @param o1 the o 1
    * @param o2 the o 2
    * @return the int
    */
   @Override
   public int compare(DescriptionVersion o1, DescriptionVersion o2) {
      final String o1matchingComponentType = Get.conceptService()
                                                .getOptionalConcept(o1.getDescriptionTypeConceptNid())
                                                .get()
                                                .getFullyQualifiedName();
      final String o2matchingComponentType = Get.conceptService()
                                                .getOptionalConcept(o2.getDescriptionTypeConceptNid())
                                                .get()
                                                .getFullyQualifiedName();

      return o1matchingComponentType.compareTo(o2matchingComponentType);
   }
}

