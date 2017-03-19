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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

//~--- classes ----------------------------------------------------------------

/**
 * Handle object to get search results.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author ocarlsen
 */
public class SearchHandle {
   
   /** The search start time. */
   private final long                  searchStartTime = System.currentTimeMillis();
   
   /** The result block. */
   private final Semaphore                   resultBlock_    = new Semaphore(1);
   
   /** The cancelled. */
   private volatile boolean            cancelled       = false;
   
   /** The error. */
   private Exception                   error           = null;
   
   /** The search I D. */
   private final Integer                     searchID_;
   
   /** The result. */
   private List<CompositeSearchResult> result_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new search handle.
    *
    * @param searchID the search ID
    */
   SearchHandle(Integer searchID) {
      this.searchID_ = searchID;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Cancel.
    */
   public void cancel() {
      this.cancelled = true;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if cancelled.
    *
    * @return true, if cancelled
    */
   public boolean isCancelled() {
      return this.cancelled;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the error.
    *
    * @param e the new error
    */
   protected void setError(Exception e) {
      synchronized (SearchHandle.this) {
         this.error = e;
         SearchHandle.this.notifyAll();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * This is not the same as the size of the result collection, as results may be merged.
    *
    * @return the hit count
    * @throws Exception the exception
    */
   public int getHitCount()
            throws Exception {
      int result = 0;

      for (final CompositeSearchResult csr: getResults()) {
         result += csr.getMatchingComponents()
                      .size();
      }

      return result;
   }

   /**
    * Blocks until the results are available....
    *
    * @return the results
    * @throws Exception the exception
    */
   public Collection<CompositeSearchResult> getResults()
            throws Exception {
      if (this.result_ == null) {
         try {
            this.resultBlock_.acquireUninterruptibly();

            while ((this.result_ == null) && (this.error == null) &&!this.cancelled) {
               try {
                  SearchHandle.this.wait();
               } catch (final InterruptedException e) {
                  // noop
               }
            }
         } finally {
            this.resultBlock_.release();
         }
      }

      if (this.error != null) {
         throw this.error;
      }

      return this.result_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the results.
    *
    * @param results the new results
    */
   protected void setResults(List<CompositeSearchResult> results) {
      synchronized (SearchHandle.this) {
         this.result_ = results;
         SearchHandle.this.notifyAll();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the search start time.
    *
    * @return the search start time
    */
   public long getSearchStartTime() {
      return this.searchStartTime;
   }

   /**
    * Returns the identifier provided (if any) by the caller when the search was started.
    *
    * @return the task id
    */
   public Integer getTaskId() {
      return this.searchID_;
   }
}

