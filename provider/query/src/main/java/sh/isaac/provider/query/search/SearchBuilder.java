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

/**
 * The Class SearchBuilder.
 */
public class SearchBuilder {
   
   /** The size limit. */
   Integer                                                            sizeLimit             = Integer.MAX_VALUE;
   
   /** The merge results on concept. */
   boolean                                                            mergeResultsOnConcept = false;
   
   /** The filter. */
   Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter                = null;
   
   /** The query. */
   String                                                             query;
   
   /** The task id. */
   Integer                                                            taskId;
   
   /** The prefix search. */
   boolean                                                            prefixSearch;
   
   /** The comparator. */
   Comparator<CompositeSearchResult>                                  comparator;
   
   /** The callback. */
   TaskCompleteCallback                                               callback;

   //~--- methods -------------------------------------------------------------

   /**
    * Concept description search builder.
    *
    * @param query the query
    * @return the search builder
    */
   // Concept search builder factory methods
   public static SearchBuilder conceptDescriptionSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(false);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   /**
    * Concept prefix search builder.
    *
    * @param query the query
    * @return the search builder
    */
   public static SearchBuilder conceptPrefixSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(true);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   /**
    * Description prefix search builder.
    *
    * @param query the query
    * @return the search builder
    */
   public static SearchBuilder descriptionPrefixSearchBuilder(String query) {
      final SearchBuilder search = new SearchBuilder();

      search.setQuery(query);
      search.setPrefixSearch(true);
      search.setComparator(new CompositeSearchResultComparator());
      return search;
   }

   /**
    * Description search builder.
    *
    * @param query the query
    * @return the search builder
    */
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
    * Gets the callback.
    *
    * @return the callback
    */
   public TaskCompleteCallback getCallback() {
      return this.callback;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the callback.
    *
    * @param callback the callback to set
    */
   public void setCallback(TaskCompleteCallback callback) {
      this.callback = callback;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the comparator.
    *
    * @return the comparator
    */
   public Comparator<CompositeSearchResult> getComparator() {
      return this.comparator;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the comparator.
    *
    * @param comparator the comparator to set
    */
   public void setComparator(Comparator<CompositeSearchResult> comparator) {
      this.comparator = comparator;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the filter.
    *
    * @return the filter
    */
   public Function<List<CompositeSearchResult>, List<CompositeSearchResult>> getFilter() {
      return this.filter;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set filter.
    *
    * @param filter the SearchResultsFilter to set
    */
   public void setFilter(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter) {
      this.filter = filter;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the merge results on concept.
    *
    * @return the mergeResultsOnConcept
    */
   public boolean getMergeResultsOnConcept() {
      return this.mergeResultsOnConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the merge results on concept.
    *
    * @param mergeResultsOnConcept the mergeResultsOnConcept to set
    */
   public void setMergeResultsOnConcept(boolean mergeResultsOnConcept) {
      this.mergeResultsOnConcept = mergeResultsOnConcept;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if prefix search.
    *
    * @return the prefixSearch
    */
   public boolean isPrefixSearch() {
      return this.prefixSearch;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the prefix search.
    *
    * @param prefixSearch the prefixSearch to set
    */
   public void setPrefixSearch(boolean prefixSearch) {
      this.prefixSearch = prefixSearch;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the query.
    *
    * @return the query
    */
   public String getQuery() {
      return this.query;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the query.
    *
    * @param query the query to set
    */
   public void setQuery(String query) {
      this.query = query;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the size limit.
    *
    * @return the sizeLimit
    */
   public Integer getSizeLimit() {
      return this.sizeLimit;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the size limit.
    *
    * @param sizeLimit the sizeLimit to set
    */
   public void setSizeLimit(Integer sizeLimit) {
      this.sizeLimit = sizeLimit;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the task id.
    *
    * @return the taskId
    */
   public Integer getTaskId() {
      return this.taskId;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the task id.
    *
    * @param taskId the taskId to set
    */
   public void setTaskId(Integer taskId) {
      this.taskId = taskId;
   }
}

