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

import org.apache.commons.lang3.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.api.index.SearchResult;

//~--- classes ----------------------------------------------------------------

/**
 * A {@link Comparator} for {@link SearchResult} objects.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CompositeSearchResultComparator
         implements Comparator<CompositeSearchResult> {
   protected static final Logger LOG = LoggerFactory.getLogger(CompositeSearchResultComparator.class);

   //~--- methods -------------------------------------------------------------

   /**
    * Note, the primary getBestScore() sort is in reverse, so it goes highest to lowest
    *
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(CompositeSearchResult o1, CompositeSearchResult o2) {
      if (o1.getBestScore() < o2.getBestScore()) {
         return 1;
      } else if (o1.getBestScore() > o2.getBestScore()) {
         return -1;
      }

      // Sort off path ones to the bottom
      if (!o1.getContainingConcept().isPresent() ||!o2.getContainingConcept().isPresent()) {
         if (!o1.getContainingConcept().isPresent() && o2.getContainingConcept().isPresent()) {
            return 1;
         } else if (o1.getContainingConcept().isPresent() &&!o2.getContainingConcept().isPresent()) {
            return -1;
         } else {
            return 0;
         }
      }

      // sort on text
      final int textComparison = ObjectUtils.compare(o1.getContainingConcept()
                                                 .get()
                                                 .getConceptDescriptionText(),
                                               o2.getContainingConcept()
                                                     .get()
                                                     .getConceptDescriptionText());

      if (textComparison != 0) {
         return textComparison;
      }

      // else same score and FSN and preferred description - sort on type
      final String comp1String = o1.getMatchingComponents()
                             .iterator()
                             .next()
                             .toUserString();
      final String comp2String = o2.getMatchingComponents()
                             .iterator()
                             .next()
                             .toUserString();

      return ObjectUtils.compare(comp1String, comp2String);
   }
}

