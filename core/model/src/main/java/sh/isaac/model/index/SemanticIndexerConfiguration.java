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
package sh.isaac.model.index;

//~--- JDK imports ------------------------------------------------------------
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.IsaacCache;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.api.index.IndexStatusListener;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicInteger;

//~--- classes ----------------------------------------------------------------
/**
 * {@link SemanticIndexerConfiguration} Holds a cache of the configuration for the indexer (which is read from the DB,
 * and may be changed at any point the user wishes). Keeps track of which assemblage types need to be indexing, and what
 * attributes should be indexed on them.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class SemanticIndexerConfiguration implements IsaacCache {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------
   /**
    * The what to index sequence to col.
    */
   // store assemblage sequences that should be indexed - and then - for COLUMN_DATA keys, keep the 0 indexed column order numbers that need to be indexed.
   private HashMap<Integer, Integer[]> whatToIndexSequenceToCol = new HashMap<>();

   /**
    * The read needed.
    */
   private final AtomicInteger readNeeded
           = new AtomicInteger(1);  // 0 means no readNeeded, anything greater than 0 means it does need a re-read

   /**
    * The read needed block.
    */
   private final Semaphore readNeededBlock = new Semaphore(1);

   //~--- methods -------------------------------------------------------------
   /**
    * Builds the and configure columns to index.
    *
    * @param assemblageNid the assemblage nid or sequence
    * @param columnsToIndex the columns to index
    * @param skipReindex the skip reindex
    * @return the SemanticChronology
    * @throws RuntimeException the runtime exception
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   public static SemanticChronology buildAndConfigureColumnsToIndex(
           int assemblageNid,
           Integer[] columnsToIndex,
           boolean skipReindex)
           throws RuntimeException,
           InterruptedException,
           ExecutionException {
      LookupService.get()
              .getService(SemanticIndexerConfiguration.class).readNeeded
              .incrementAndGet();

      final List<IndexStatusListener> islList = LookupService.get()
              .getAllServices(IndexStatusListener.class);

      islList.forEach((isl) -> {
         isl.indexConfigurationChanged(LookupService.get()
                 .getService(IndexSemanticQueryService.class));
      });

      final ConceptChronology referencedAssemblageConceptC = Get.conceptService()
              .getConceptChronology(assemblageNid);

      LOG.info("Configuring index for dynamic assemblage '" + referencedAssemblageConceptC.toUserString()
              + "' on columns " + Arrays.deepToString(columnsToIndex));

      DynamicData[] data = null;

      if (columnsToIndex != null) {
         final DynamicIntegerImpl[] cols = new DynamicIntegerImpl[columnsToIndex.length];

         for (int i = 0; i < columnsToIndex.length; i++) {
            cols[i] = new DynamicIntegerImpl(columnsToIndex[i]);
         }

         if (cols.length > 0) {
            data = new DynamicData[]{new DynamicArrayImpl<>(cols)};
         }
      } else if (((columnsToIndex == null) || (columnsToIndex.length == 0))) {
         throw new RuntimeException("It doesn't make sense to index a dynamic without indexing any column data");
      }

      Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE);
      final SemanticBuilder<? extends SemanticChronology> sb = Get.semanticBuilderService()
              .getDynamicBuilder(assemblageNid,
                      DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                              .getNid(),
                      data);
      SemanticChronology sc = sb.build(transaction, EditCoordinates.getDefaultUserMetadata()).get();
      transaction.commit();
      return sc;

      // TODO [Dan 3] need to fix the ecosystem of index configuration change indexer
//    Get.commitService().commit("Index Config Change").get();
//    
//    if (!skipReindex)
//    {
//            Get.startIndexTask(new Class[] {SemanticIndexer.class});
//    }
   }

   /**
    * for the given assemblage sequence, which columns should be indexed - note - columnsToIndex must be provided it
    * doesn't make any sense to index semantics any longer in ochre without indexing column content.
    *
    * @param assemblageNid the assemblage nid or sequence
    * @param columnsToIndex the columns to index
    * @param skipReindex - if true - does not do a full DB reindex (useful if you are enabling an index on a new that
    * has never been used) otherwise - leave false - so that a full reindex occurs (on this thread) and the index
    * becomes valid.
    * @throws RuntimeException the runtime exception
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   @SuppressWarnings("unchecked")
   public static void configureColumnsToIndex(int assemblageNid,
           Integer[] columnsToIndex,
           boolean skipReindex)
           throws RuntimeException,
           InterruptedException,
           ExecutionException {
      LookupService.get()
              .getService(SemanticIndexerConfiguration.class).readNeeded
              .incrementAndGet();

      final List<IndexStatusListener> islList = LookupService.get()
              .getAllServices(IndexStatusListener.class);

      for (final IndexStatusListener isl : islList) {
         isl.indexConfigurationChanged(LookupService.get()
                 .getService(IndexSemanticQueryService.class));
      }

      final ConceptChronology referencedAssemblageConceptC = Get.conceptService()
              .getConceptChronology(assemblageNid);

      LOG.info("Configuring index for assemblage '" + referencedAssemblageConceptC.toUserString()
              + "' on columns " + Arrays.deepToString(columnsToIndex));

      DynamicData[] data = null;

      if (columnsToIndex != null) {
         final DynamicIntegerImpl[] cols = new DynamicIntegerImpl[columnsToIndex.length];

         for (int i = 0; i < columnsToIndex.length; i++) {
            cols[i] = new DynamicIntegerImpl(columnsToIndex[i]);
         }

         if (cols.length > 0) {
            data = new DynamicData[]{new DynamicArrayImpl<>(cols)};
         }
      } else if (((columnsToIndex == null) || (columnsToIndex.length == 0))) {
         throw new RuntimeException("It doesn't make sense to index a dynamic without indexing any column data");
      }

      Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE);
      final SemanticBuilder<? extends SemanticChronology> sb = Get.semanticBuilderService()
              .getDynamicBuilder(assemblageNid,
                      DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                              .getNid(),
                      data);

      sb.build(transaction, EditCoordinates.getDefaultUserMetadata())
              .get();
      transaction.commit("Index Config Change").get();
      if (!skipReindex) {
         Get.startIndexTask(new Class[]{IndexSemanticQueryService.class});
      }
   }

   /**
    * Disable all indexing of the specified refex. To change the index config, use the {@link #configureColumnsToIndex(int, Integer[], boolean) method.
    *
    * Note that this causes a full DB reindex, on this thread.
    *
    * @param assemblageConceptNid the assemblage concept nid
    * @throws RuntimeException the runtime exception
    */
   @SuppressWarnings("unchecked")
   public static void disableIndex(int assemblageConceptNid)
           throws RuntimeException {
      LOG.info("Disabling index for dynamic assemblage concept '" + assemblageConceptNid + "'");

      final DynamicVersion<?> rdv = findCurrentIndexConfigRefex(assemblageConceptNid);

      if ((rdv != null) && (rdv.getStatus() == Status.ACTIVE)) {
         LookupService.get()
                 .getService(SemanticIndexerConfiguration.class).readNeeded
                 .incrementAndGet();

         final List<IndexStatusListener> islList = LookupService.get()
                 .getAllServices(IndexStatusListener.class);

         for (final IndexStatusListener isl : islList) {
            isl.indexConfigurationChanged(LookupService.get()
                    .getService(IndexSemanticQueryService.class));
         }

         Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.INACTIVE);

         ((SemanticChronology) rdv.getChronology()).createMutableVersion(transaction, Status.INACTIVE,
                 EditCoordinates.getDefaultUserMetadata());
         Get.commitService().addUncommitted(transaction, rdv);
         transaction.commit("Index Config Change");
         LOG.info("Index disabled for dynamic assemblage concept '" + assemblageConceptNid + "'");
         Get.startIndexTask(new Class[]{IndexSemanticQueryService.class});
      } else {
         LOG.info("No index configuration was found to disable for dynamic assemblage concept '"
                 + assemblageConceptNid + "'");
      }
   }

   /**
    * Read the indexing configuration for the specified dynamic element.
    *
    * Returns null, if the assemblage is not indexed at all. Returns an empty array, if the assemblage is indexed (but
    * no columns are indexed) Returns an integer array of the column positions of the refex that are indexed, if any.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the integer[]
    * @throws RuntimeException the runtime exception
    */
   public static Integer[] readIndexInfo(int assemblageSequence)
           throws RuntimeException {
      return LookupService.get()
              .getService(SemanticIndexerConfiguration.class)
              .whatColumnsToIndex(assemblageSequence);
   }

   /**
    * Needs indexing.
    *
    * @param assemblageConceptNid the assemblage concept nid
    * @return true, if successful
    */
   protected boolean needsIndexing(int assemblageConceptNid) {
      initCheck();
      return this.whatToIndexSequenceToCol.containsKey(assemblageConceptNid);
   }

   /**
    * What columns to index.
    *
    * @param assemblageConceptNid the assemblage concept nid
    * @return the integer[]
    */
   public Integer[] whatColumnsToIndex(int assemblageConceptNid) {
      initCheck();
      return this.whatToIndexSequenceToCol.get(assemblageConceptNid);
   }

   /**
    * Find current index config refex.
    *
    * @param indexedSemanticId the indexed id
    * @return the dynamic element<? extends dynamic element>
    * @throws RuntimeException the runtime exception
    */
   private static DynamicVersion<? extends DynamicVersion> findCurrentIndexConfigRefex(int indexedSemanticId)
           throws RuntimeException {
      @SuppressWarnings("rawtypes")
      final SemanticSnapshotService<DynamicVersion> sss = Get.assemblageService()
              .getSnapshot(DynamicVersion.class,
                      StampCoordinates.getDevelopmentLatest());
      @SuppressWarnings("rawtypes")
      final List<LatestVersion<DynamicVersion>> semantics
              = sss.getLatestSemanticVersionsForComponentFromAssemblage(indexedSemanticId,
                      DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                              .getNid());

      for (LatestVersion<DynamicVersion> ds : semantics) {
         if (ds.isPresent()) {
            return ds.get();
         }
      }
      return null;
   }

   /**
    * Inits the check.
    */
   private void initCheck() {
      if (this.readNeeded.get() > 0) {
         // During bulk index, prevent all threads from doing this at the same time...
         try {
            this.readNeededBlock.acquireUninterruptibly();

            if (this.readNeeded.get() > 0) {
               LOG.debug("Reading dynamic element Index Configuration");

               try {
                  final HashMap<Integer, Integer[]> updatedWhatToIndex = new HashMap<>();
                  final Stream<SemanticChronology> semanticCs = Get.assemblageService()
                          .getSemanticChronologyStream(DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                                  .getNid());

                  semanticCs.forEach(semanticC -> {
                     if (semanticC.getVersionType() == VersionType.DYNAMIC) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        final LatestVersion<DynamicVersion> dsv
                                = ((SemanticChronology) semanticC).getLatestVersion(StampCoordinates.getDevelopmentLatest());

                        if (dsv.isPresent() && (dsv.get().getStatus() == Status.ACTIVE)) {
                           final int assemblageToIndex = dsv.get()
                                   .getReferencedComponentNid();
                           Integer[] finalCols = new Integer[]{};
                           final DynamicData[] data = dsv.get()
                                   .getData();

                           if ((data != null) && (data.length > 0)) {
                              @SuppressWarnings("unchecked")
                              final DynamicInteger[] colsToIndex
                                      = ((DynamicArray<DynamicInteger>) data[0]).getDataArray();

                              finalCols = new Integer[colsToIndex.length];

                              for (int i = 0; i < colsToIndex.length; i++) {
                                 finalCols[i] = colsToIndex[i].getDataInteger();
                              }
                           } else {
                              LOG.warn(
                                      "The assemblage concept {} was entered for indexing without specifying what columns to index.  Nothing to do!",
                                      assemblageToIndex);
                           }

                           updatedWhatToIndex.put(assemblageToIndex, finalCols);
                        }
                     }
                  });
                  this.whatToIndexSequenceToCol = updatedWhatToIndex;
                  this.readNeeded.decrementAndGet();
               } catch (final Exception e) {
                  LOG.error(
                          "Unexpected error reading dynamic element Index Configuration - generated index will be incomplete!",
                          e);
               }
            }
         } finally {
            this.readNeededBlock.release();
         }
      }
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Checks if assemblage indexed.
    *
    * @param assemblageConceptNid the assemblage concept nid
    * @return true, if assemblage indexed
    */
   public static boolean isAssemblageIndexed(int assemblageConceptNid) {
      return LookupService.get()
              .getService(SemanticIndexerConfiguration.class)
              .needsIndexing(assemblageConceptNid);
   }

   /**
    * Checks if column type indexable.
    *
    * @param dataType the data type
    * @return true, if column type indexable
    */
   public static boolean isColumnTypeIndexable(DynamicDataType dataType) {
      if (dataType == DynamicDataType.BYTEARRAY) {
         return false;
      }

      return true;
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public void reset()
   {
      readNeeded.set(1);
      whatToIndexSequenceToCol.clear();
   }
}
