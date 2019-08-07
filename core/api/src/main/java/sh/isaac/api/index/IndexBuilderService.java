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



package sh.isaac.api.index;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.chronicle.Chronology;

/**
 * The contract interface for indexing services that construct or maintain new indexes.
 * <br>
 * {@code IndexBuilderService} implementations must not throw exceptions. 
 * Throwing exceptions could cause the underlying source data to corrupt. 
 * Since indexes can be regenerated, indexes should mark themselves as invalid somehow, 
 * and recreate themselves when necessary.
 * @author aimeefurber
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IndexBuilderService extends DatastoreServices, IndexQueryService {
   /**
    * Call this to begin a batch-reindex.  Underlying indexers shall clear their index, and any statistics, and lock 
    * as necessary so that multiple threads don't try to batch-reindex at the same time.
    * 
    * Callers should ensure they call {@link #finishBatchReindex} in a try/finally block, to ensure that they don't leave a 
    * dangling lock.
    */
   void startBatchReindex();

   /**
    * Call this when a batch-reindex is complete.  
    */
   void finishBatchReindex();

   /**
    * To maximize search performance, you can optionally call forceMerge.
    * forceMerge is a costly operation, so generally call it when the
    * index is relatively static (after finishing a bulk addition of documents)
    */
   void forceMerge();

   /**
    * Index the Chronology in a manner appropriate to the
    * indexer implementation in a background thread. 
    * 
    * This method returns almost immediately.
    * 
    * The implementation is responsible for 
    * determining if the component is appropriate for indexing.
    * 
    * The implementation must not perform lengthy operations on the thread calling the index method, 
    * rather, it should put all long work into the thread that will return the result to the CompleteableFuture.
    *
    * @param chronicle the chronicle
    * @return a {@code CompletableFuture<Long>} for the index generation to which this chronicle is attached.  
    * If this chronicle is not indexed by this indexer, the {@code CompletableFuture<Long>} returns
    * {@code Long.MIN_VALUE}.
    * The generation can be used with searchers to make sure that the component's indexing is complete prior to performing
    * a search where the chronicle's results must be included.
    */
   CompletableFuture<Long> index(Chronology chronicle);
   
   /**
    * Index the Chronology in a maner appropriate to the indexer implementation.  Unlike {@link #index(Chronology)}, this 
    * operation occurs on the calling thread, and does not return until the index operation is complete.
    * 
    * @param chronicle
    * @return the index generation to which this chronicle is attached.  If this chronicle is not indexed by this indexer, 
    * it returns{@code Long.MIN_VALUE}.
    * The generation can be used with searchers to make sure that the component's indexing is complete prior to performing
    * a search where the chronicle's results must be included.
    */
   long indexNow(Chronology chronicle);

   /**
    * Report indexed items.
    *
    * @return name / value pairs that give statistics on the number of things indexed since the last time
    * {@link #startBatchReindex} was called.
    */
   HashMap<String, Integer> reportIndexedItems();

   /**
    * Checks if enabled.
    *
    * @return true if this indexer is enabled.
    */
   boolean isEnabled();

   /**
    * Enables or disables an indexer. A disabled indexer will take
    * no action when the index method is called.
    * @param enabled true if the indexer is enabled, otherwise false.
    */
   void setEnabled(boolean enabled);
   
   int getIndexMemoryInUse();
}

