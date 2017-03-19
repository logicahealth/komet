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
   private final long                  searchStartTime = System.currentTimeMillis();
   private Semaphore                   resultBlock_    = new Semaphore(1);
   private volatile boolean            cancelled       = false;
   private Exception                   error           = null;
   private Integer                     searchID_;
   private List<CompositeSearchResult> result_;

   //~--- constructors --------------------------------------------------------

   SearchHandle(Integer searchID) {
      searchID_ = searchID;
   }

   //~--- methods -------------------------------------------------------------

   public void cancel() {
      this.cancelled = true;
   }

   //~--- get methods ---------------------------------------------------------

   public boolean isCancelled() {
      return cancelled;
   }

   //~--- set methods ---------------------------------------------------------

   protected void setError(Exception e) {
      synchronized (SearchHandle.this) {
         this.error = e;
         SearchHandle.this.notifyAll();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * This is not the same as the size of the result collection, as results may be merged.
    * @return
    * @throws Exception
    */
   public int getHitCount()
            throws Exception {
      int result = 0;

      for (CompositeSearchResult csr: getResults()) {
         result += csr.getMatchingComponents()
                      .size();
      }

      return result;
   }

   /**
    * Blocks until the results are available....
    *
    * @return
    * @throws Exception
    */
   public Collection<CompositeSearchResult> getResults()
            throws Exception {
      if (result_ == null) {
         try {
            resultBlock_.acquireUninterruptibly();

            while ((result_ == null) && (error == null) &&!cancelled) {
               try {
                  SearchHandle.this.wait();
               } catch (InterruptedException e) {
                  // noop
               }
            }
         } finally {
            resultBlock_.release();
         }
      }

      if (error != null) {
         throw error;
      }

      return result_;
   }

   //~--- set methods ---------------------------------------------------------

   protected void setResults(List<CompositeSearchResult> results) {
      synchronized (SearchHandle.this) {
         result_ = results;
         SearchHandle.this.notifyAll();
      }
   }

   //~--- get methods ---------------------------------------------------------

   public long getSearchStartTime() {
      return searchStartTime;
   }

   /**
    * Returns the identifier provided (if any) by the caller when the search was started
    */
   public Integer getTaskId() {
      return searchID_;
   }
}

