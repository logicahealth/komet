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



/**
 * SearchResultsIntersectionFilter
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package sh.isaac.provider.query.search;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- classes ----------------------------------------------------------------

/**
 * SearchResultsIntersectionFilter
 *
 * Just a Utility class to allow chaining multiple filters together for passing them into the SearchHandeler search API calls.
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class SearchResultsIntersectionFilter
         implements Function<List<CompositeSearchResult>, List<CompositeSearchResult>> {
   
   /** The Constant LOG. */
   private static final Logger LOG = LoggerFactory.getLogger(SearchResultsIntersectionFilter.class);

   //~--- fields --------------------------------------------------------------

   /** The filters. */
   List<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> filters = new ArrayList<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new search results intersection filter.
    *
    * @param passedFilters the passed filters
    */
   @SafeVarargs
   public SearchResultsIntersectionFilter(Function<List<CompositeSearchResult>,
         List<CompositeSearchResult>>... passedFilters) {
      if (passedFilters != null) {
         for (final Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter: passedFilters) {
            this.filters.add(filter);
         }
      }
   }

   /**
    * Instantiates a new search results intersection filter.
    *
    * @param passedFilters the passed filters
    */
   public SearchResultsIntersectionFilter(List<Function<List<CompositeSearchResult>,
         List<CompositeSearchResult>>> passedFilters) {
      this.filters.addAll(passedFilters);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Apply.
    *
    * @param results the results
    * @return the list
    * @see java.util.function.Function#apply(java.lang.Object)
    */
   @Override
   public List<CompositeSearchResult> apply(List<CompositeSearchResult> results) {
      List<CompositeSearchResult> filteredResults = results;

      for (final Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter: this.filters) {
         final int numResultsToFilter = filteredResults.size();

         LOG.debug("Applying SearchResultsFilter " + filter + " to " + numResultsToFilter + " search results");
         filteredResults = filter.apply(filteredResults);
         LOG.debug(filteredResults.size() + " results remained after filtering a total of " + numResultsToFilter +
                   " search results");
      }

      return filteredResults;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "SearchResultsIntersectionFilter [filters=" + Arrays.toString(this.filters.toArray()) + "]";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the filters.
    *
    * @return the filters
    */
   public Collection<Function<List<CompositeSearchResult>, List<CompositeSearchResult>>> getFilters() {
      return this.filters;
   }
}

