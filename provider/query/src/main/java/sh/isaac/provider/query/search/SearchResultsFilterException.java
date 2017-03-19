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
 * SearchResultsFilterException
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package sh.isaac.provider.query.search;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.function.Function;

//~--- classes ----------------------------------------------------------------

/**
 * SearchResultsFilterException.
 *
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class SearchResultsFilterException
        extends RuntimeException {
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   /** The failed filter. */
   private final Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new search results filter exception.
    *
    * @param failedFilter the failed filter
    */
   public SearchResultsFilterException(Function<List<CompositeSearchResult>,
         List<CompositeSearchResult>> failedFilter) {
      this.failedFilter = failedFilter;
   }

   /**
    * Instantiates a new search results filter exception.
    *
    * @param cause the cause
    */
   public SearchResultsFilterException(SearchResultsFilterException cause) {
      super(cause);
      this.failedFilter = cause.failedFilter;
   }

   /**
    * Instantiates a new search results filter exception.
    *
    * @param failedFilter the failed filter
    * @param message the message
    */
   public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter,
         String message) {
      super(message);
      this.failedFilter = failedFilter;
   }

   /**
    * Instantiates a new search results filter exception.
    *
    * @param failedFilter the failed filter
    * @param cause the cause
    */
   public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter,
         Throwable cause) {
      super(cause);
      this.failedFilter = failedFilter;
   }

   /**
    * Instantiates a new search results filter exception.
    *
    * @param failedFilter the failed filter
    * @param message the message
    * @param cause the cause
    */
   public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter,
         String message,
         Throwable cause) {
      super(message, cause);
      this.failedFilter = failedFilter;
   }

   /**
    * Instantiates a new search results filter exception.
    *
    * @param message the message
    * @param cause the cause
    * @param enableSuppression the enable suppression
    * @param writableStackTrace the writable stack trace
    */
   public SearchResultsFilterException(String message,
         SearchResultsFilterException cause,
         boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
      this.failedFilter = cause.failedFilter;
   }

   /**
    * Instantiates a new search results filter exception.
    *
    * @param failedFilter the failed filter
    * @param message the message
    * @param cause the cause
    * @param enableSuppression the enable suppression
    * @param writableStackTrace the writable stack trace
    */
   public SearchResultsFilterException(Function<List<CompositeSearchResult>, List<CompositeSearchResult>> failedFilter,
         String message,
         Throwable cause,
         boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
      this.failedFilter = failedFilter;
   }
}

