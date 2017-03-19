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
 * SearchBuilder
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package sh.isaac.provider.query.search;

//~--- JDK imports ------------------------------------------------------------

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.util.TaskCompleteCallback;

//~--- classes ----------------------------------------------------------------

public class SearchBuilder {
   Integer                                                            sizeLimit             = Integer.MAX_VALUE;
   boolean                                                            mergeResultsOnConcept = false;
   Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter                = null;
   String                                                             query;
   Integer                                                            taskId;
   boolean                                                            prefixSearch;
   Comparator<CompositeSearchResult>                                  comparator;
   TaskCompleteCallback                                               callback;

   //~--- methods -------------------------------------------------------------

   // Concept search builder factory methods
   public static SearchBuilder conceptDescriptionSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(false);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   public static SearchBuilder conceptPrefixSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(true);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   public static SearchBuilder descriptionPrefixSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(true);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   // Description search builder factory methods
   public static SearchBuilder descriptionSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(false);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the callback
    */
   public TaskCompleteCallback getCallback() {
      return this.callback;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param callback the callback to set
    */
   public void setCallback(TaskCompleteCallback callback) {
      this.callback = callback;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the comparator
    */
   public Comparator<CompositeSearchResult> getComparator() {
      return this.comparator;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param comparator the comparator to set
    */
   public void setComparator(Comparator<CompositeSearchResult> comparator) {
      this.comparator = comparator;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the filter
    */
   public Function<List<CompositeSearchResult>, List<CompositeSearchResult>> getFilter() {
      return this.filter;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param filter the SearchResultsFilter to set
    */
   public void setFilter(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter) {
      this.filter = filter;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the mergeResultsOnConcept
    */
   public boolean getMergeResultsOnConcept() {
      return this.mergeResultsOnConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param mergeResultsOnConcept the mergeResultsOnConcept to set
    */
   public void setMergeResultsOnConcept(boolean mergeResultsOnConcept) {
      this.mergeResultsOnConcept = mergeResultsOnConcept;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the prefixSearch
    */
   public boolean isPrefixSearch() {
      return this.prefixSearch;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param prefixSearch the prefixSearch to set
    */
   public void setPrefixSearch(boolean prefixSearch) {
      this.prefixSearch = prefixSearch;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the query
    */
   public String getQuery() {
      return this.query;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param query the query to set
    */
   public void setQuery(String query) {
      this.query = query;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the sizeLimit
    */
   public Integer getSizeLimit() {
      return this.sizeLimit;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param sizeLimit the sizeLimit to set
    */
   public void setSizeLimit(Integer sizeLimit) {
      this.sizeLimit = sizeLimit;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the taskId
    */
   public Integer getTaskId() {
      return this.taskId;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param taskId the taskId to set
    */
   public void setTaskId(Integer taskId) {
      this.taskId = taskId;
   }
}

