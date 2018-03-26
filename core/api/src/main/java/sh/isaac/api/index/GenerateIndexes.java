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
 /*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.index;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.progress.PersistTaskResult;

//~--- classes ----------------------------------------------------------------
/**
 * The Class GenerateIndexes.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class GenerateIndexes
        extends TimedTask<Void> implements PersistTaskResult {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------
   /**
    * The processed.
    */
   AtomicLong processed = new AtomicLong(0);

   /**
    * The indexers.
    */
   List<IndexBuilderService> indexers = new ArrayList<>();

   /**
    * The component count.
    */
   long componentCount;

   /**
    * Instantiates a new generate indexes.
    *
    * @param indexersToReindex the indexers to reindex
    */
   public GenerateIndexes(Class<?>... indexersToReindex) {
      updateTitle("Index generation");
      updateProgress(-1, Long.MAX_VALUE); // Indeterminate progress

      if ((indexersToReindex == null) || (indexersToReindex.length == 0)) {
         this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      } else {

         for (final Class<?> clazz : indexersToReindex) {
            if (!IndexBuilderService.class.isAssignableFrom(clazz)) {
               throw new RuntimeException("Invalid Class passed in to the index generator.  Classes must implement IndexBuilderService ");
            }

            final IndexBuilderService temp = (IndexBuilderService) LookupService.get().getService(clazz);

            if (temp != null) {
               this.indexers.add(temp);
            }
         }
      }
   }
   
    /**
     * Used to avoid circular dependencies during a re-index upon startup
     * @param indexersToReindex
     */
   public GenerateIndexes(IndexBuilderService... indexersToReindex) {
      updateTitle("Index generation");
      updateProgress(-1, Long.MAX_VALUE); // Indeterminate progress

      if (indexersToReindex != null) {
         for (IndexBuilderService i : indexersToReindex) {
            indexers.add(i);
         }
      }
   }

   /**
    * Called by a threaded executor to begin the reindex.
    *
    * @return the void
    * @throws Exception the exception
    */
   @Override
   protected Void call() throws Exception {
      Get.activeTasks().add(this);

      try {

         final List<IndexStatusListener> islList = LookupService.get().getAllServices(IndexStatusListener.class);

         for (final IndexBuilderService i : this.indexers) {
            if (islList != null) {
               for (final IndexStatusListener isl : islList) {
                  isl.reindexBegan(i);
               }
            }

            LOG.info("Clearing index for: " + i.getIndexerName());
            i.startBatchReindex();
         }

         // We only need to index semantics now
         // In the future, there may be a need for indexing Concepts from the concept service - for instance, if we wanted to index the concepts
         // by user, or by some other attribute that is attached to the concept. But there simply isn't much on the concept at present, and I have
         // no use case for indexing the concepts. The IndexBuilderService APIs would need enhancement if we allowed indexing things other than semantics.
         final long semanticCount = (int) Get.assemblageService().getSemanticCount();

         LOG.info("Semantic elements to index: " + semanticCount);
         this.componentCount = semanticCount;

         Get.assemblageService().getSemanticChronologyStream().parallel().forEach((SemanticChronology semantic) -> {
            for (final IndexBuilderService i : this.indexers) {
               try {
                  if (semantic == null) {
                     // noop - this error is already logged elsewhere. Just skip.
                  } else {
                     i.index(semantic).get();
                  }
               } catch (final InterruptedException | ExecutionException e) {
                  throw new RuntimeException(e);
               }
            }

            updateProcessedCount();
         });

         for (final IndexBuilderService i : this.indexers) {
            if (islList != null) {
               for (final IndexStatusListener isl : islList) {
                  isl.reindexCompleted(i);
               }
            }

            i.sync().get();
            i.forceMerge();
            LOG.info(i.getIndexerName() + " indexing complete.  Statistics follow:");
            for (final Map.Entry<String, Integer> entry : i.reportIndexedItems().entrySet()) {
               LOG.info(entry.getKey() + ": " + entry.getValue());
            }
         }

         return null;
      } finally {
         Get.activeTasks().remove(this);
         for (final IndexBuilderService i : this.indexers) {
            i.finishBatchReindex();
         }
      }
   }

   /**
    * Update processed count.
    */
   protected void updateProcessedCount() {
      final long processedCount = this.processed.incrementAndGet();

      if (processedCount % 1000 == 0) {
         updateProgress(processedCount, this.componentCount);
         updateMessage(String.format("Indexed %,d components...", processedCount));

         // We were committing too often every 1000 components, it was bad for performance.
         if (processedCount % 100000 == 0) {
            for (final IndexBuilderService i : this.indexers) {
               i.sync();
            }
            LOG.info("Indexed " + processedCount + " semantics");
         }
      }
   }
}
