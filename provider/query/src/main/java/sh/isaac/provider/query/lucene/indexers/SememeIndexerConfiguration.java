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



package sh.isaac.provider.query.lucene.indexers;

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
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.MutableDynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.sememe.dataTypes.DynamicSememeArrayImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeIntegerImpl;
import sh.isaac.api.index.IndexStatusListener;

//~--- classes ----------------------------------------------------------------

/**
 * {@link SememeIndexerConfiguration} Holds a cache of the configuration for the sememe indexer (which is read from the DB, and may
 * be changed at any point the user wishes). Keeps track of which assemblage types need to be indexing, and what attributes should be indexed on them.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class SememeIndexerConfiguration {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The what to index sequence to col. */

   // store assemblage sequences that should be indexed - and then - for COLUMN_DATA keys, keep the 0 indexed column order numbers that need to be indexed.
   private HashMap<Integer, Integer[]> whatToIndexSequenceToCol = new HashMap<>();

   /** The read needed. */
   private final AtomicInteger readNeeded =
      new AtomicInteger(1);  // 0 means no readNeeded, anything greater than 0 means it does need a re-read

   /** The read needed block. */
   private final Semaphore readNeededBlock = new Semaphore(1);

   //~--- methods -------------------------------------------------------------

   /**
    * Builds the and configure columns to index.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @param columnsToIndex the columns to index
    * @param skipReindex the skip reindex
    * @return the sememe chronology<? extends dynamic sememe<?>>
    * @throws RuntimeException the runtime exception
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   @SuppressWarnings("unchecked")
   public static SememeChronology<? extends DynamicSememe<?>> buildAndConfigureColumnsToIndex(
           int assemblageNidOrSequence,
           Integer[] columnsToIndex,
           boolean skipReindex)
            throws RuntimeException,
                   InterruptedException,
                   ExecutionException {
      LookupService.get()
                   .getService(SememeIndexerConfiguration.class).readNeeded
                   .incrementAndGet();

      final List<IndexStatusListener> islList = LookupService.get()
                                                               .getAllServices(IndexStatusListener.class);

      for (final IndexStatusListener isl: islList) {
         isl.indexConfigurationChanged(LookupService.get()
               .getService(SememeIndexer.class));
      }

      final ConceptChronology<? extends ConceptVersion<?>> referencedAssemblageConceptC = Get.conceptService()
                                                                                             .getConcept(
                                                                                                assemblageNidOrSequence);

      log.info("Configuring index for dynamic sememe assemblage '" + referencedAssemblageConceptC.toUserString() +
               "' on columns " + Arrays.deepToString(columnsToIndex));

      DynamicSememeData[] data = null;

      if (columnsToIndex != null) {
         final DynamicSememeIntegerImpl[] cols = new DynamicSememeIntegerImpl[columnsToIndex.length];

         for (int i = 0; i < columnsToIndex.length; i++) {
            cols[i] = new DynamicSememeIntegerImpl(columnsToIndex[i]);
         }

         if (cols.length > 0) {
            data = new DynamicSememeData[] { new DynamicSememeArrayImpl<>(cols) };
         }
      } else if (((columnsToIndex == null) || (columnsToIndex.length == 0))) {
         throw new RuntimeException("It doesn't make sense to index a dynamic sememe without indexing any column data");
      }

      final SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> sb = Get.sememeBuilderService()
                                                                                          .getDynamicSememeBuilder(
                                                                                             Get.identifierService()
                                                                                                   .getConceptNid(
                                                                                                      assemblageNidOrSequence),
                                                                                                   DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                                                                                         .getNid(),
                                                                                                   data);

      return sb.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE)
               .get();

      // TODO Dan change indexer
//    Get.commitService().commit("Index Config Change").get();
//    
//    if (!skipReindex)
//    {
//            Get.startIndexTask(new Class[] {SememeIndexer.class});
//    }
   }

   /**
    * for the given assemblage sequence, which columns should be indexed - note - columnsToIndex must be provided
    * it doesn't make any sense to index sememes any longer in ochre without indexing column content.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @param columnsToIndex the columns to index
    * @param skipReindex - if true - does not do a full DB reindex (useful if you are enabling an index on a new sememe that has never been used)
    * otherwise - leave false - so that a full reindex occurs (on this thread) and the index becomes valid.
    * @throws RuntimeException the runtime exception
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   @SuppressWarnings("unchecked")
   public static void configureColumnsToIndex(int assemblageNidOrSequence,
         Integer[] columnsToIndex,
         boolean skipReindex)
            throws RuntimeException,
                   InterruptedException,
                   ExecutionException {
      LookupService.get()
                   .getService(SememeIndexerConfiguration.class).readNeeded
                   .incrementAndGet();

      final List<IndexStatusListener> islList = LookupService.get()
                                                               .getAllServices(IndexStatusListener.class);

      for (final IndexStatusListener isl: islList) {
         isl.indexConfigurationChanged(LookupService.get()
               .getService(SememeIndexer.class));
      }

      final ConceptChronology<? extends ConceptVersion<?>> referencedAssemblageConceptC = Get.conceptService()
                                                                                             .getConcept(
                                                                                                assemblageNidOrSequence);

      log.info("Configuring index for dynamic sememe assemblage '" + referencedAssemblageConceptC.toUserString() +
               "' on columns " + Arrays.deepToString(columnsToIndex));

      DynamicSememeData[] data = null;

      if (columnsToIndex != null) {
         final DynamicSememeIntegerImpl[] cols = new DynamicSememeIntegerImpl[columnsToIndex.length];

         for (int i = 0; i < columnsToIndex.length; i++) {
            cols[i] = new DynamicSememeIntegerImpl(columnsToIndex[i]);
         }

         if (cols.length > 0) {
            data = new DynamicSememeData[] { new DynamicSememeArrayImpl<>(cols) };
         }
      } else if (((columnsToIndex == null) || (columnsToIndex.length == 0))) {
         throw new RuntimeException("It doesn't make sense to index a dynamic sememe without indexing any column data");
      }

      final SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> sb = Get.sememeBuilderService()
                                                                                          .getDynamicSememeBuilder(
                                                                                             Get.identifierService()
                                                                                                   .getConceptNid(
                                                                                                      assemblageNidOrSequence),
                                                                                                   DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                                                                                         .getNid(),
                                                                                                   data);

      sb.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE)
        .get();
      Get.commitService()
         .commit("Index Config Change")
         .get();

      if (!skipReindex) {
         Get.startIndexTask(new Class[] { SememeIndexer.class });
      }
   }

   /**
    * Disable all indexing of the specified refex.  To change the index config, use the {@link #configureColumnsToIndex(int, Integer[]) method.
    *
    * Note that this causes a full DB reindex, on this thread.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @throws RuntimeException the runtime exception
    */
   @SuppressWarnings("unchecked")
   public static void disableIndex(int assemblageConceptSequence)
            throws RuntimeException {
      log.info("Disabling index for dynamic sememe assemblage concept '" + assemblageConceptSequence + "'");

      final DynamicSememe<?> rdv = findCurrentIndexConfigRefex(assemblageConceptSequence);

      if ((rdv != null) && (rdv.getState() == State.ACTIVE)) {
         LookupService.get()
                      .getService(SememeIndexerConfiguration.class).readNeeded
                      .incrementAndGet();

         final List<IndexStatusListener> islList = LookupService.get()
                                                                  .getAllServices(IndexStatusListener.class);

         for (final IndexStatusListener isl: islList) {
            isl.indexConfigurationChanged(LookupService.get()
                  .getService(SememeIndexer.class));
         }

         ((SememeChronology) rdv.getChronology()).createMutableVersion(MutableDynamicSememe.class,
               State.INACTIVE,
               EditCoordinates.getDefaultUserMetadata());
         Get.commitService()
            .addUncommitted(rdv.getChronology());
         Get.commitService()
            .commit("Index Config Change");
         log.info("Index disabled for dynamic sememe assemblage concept '" + assemblageConceptSequence + "'");
         Get.startIndexTask(new Class[] { SememeIndexer.class });
         return;
      } else {
         log.info("No index configuration was found to disable for dynamic sememe assemblage concept '" +
                  assemblageConceptSequence + "'");
      }
   }

   /**
    * Read the indexing configuration for the specified dynamic sememe.
    *
    * Returns null, if the assemblage is not indexed at all.  Returns an empty array, if the assemblage is indexed (but no columns are indexed)
    * Returns an integer array of the column positions of the refex that are indexed, if any.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the integer[]
    * @throws RuntimeException the runtime exception
    */
   public static Integer[] readIndexInfo(int assemblageSequence)
            throws RuntimeException {
      return LookupService.get()
                          .getService(SememeIndexerConfiguration.class)
                          .whatColumnsToIndex(assemblageSequence);
   }

   /**
    * Needs indexing.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return true, if successful
    */
   protected boolean needsIndexing(int assemblageConceptSequence) {
      initCheck();
      return this.whatToIndexSequenceToCol.containsKey(assemblageConceptSequence);
   }

   /**
    * What columns to index.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the integer[]
    */
   protected Integer[] whatColumnsToIndex(int assemblageConceptSequence) {
      initCheck();
      return this.whatToIndexSequenceToCol.get(assemblageConceptSequence);
   }

   /**
    * Find current index config refex.
    *
    * @param indexedSememeId the indexed sememe id
    * @return the dynamic sememe<? extends dynamic sememe<?>>
    * @throws RuntimeException the runtime exception
    */
   private static DynamicSememe<? extends DynamicSememe<?>> findCurrentIndexConfigRefex(int indexedSememeId)
            throws RuntimeException {
      @SuppressWarnings("rawtypes")
      final SememeSnapshotService<DynamicSememe> sss = Get.sememeService()
                                                          .getSnapshot(DynamicSememe.class,
                                                                StampCoordinates.getDevelopmentLatest());
      @SuppressWarnings("rawtypes")
      final Stream<LatestVersion<DynamicSememe>> sememes =
         sss.getLatestSememeVersionsForComponentFromAssemblage(Get.identifierService()
                                                                  .getConceptNid(indexedSememeId),
                                                               DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                                                     .getSequence());
      @SuppressWarnings("rawtypes")
      final Optional<LatestVersion<DynamicSememe>> ds = sememes.findAny();

      if (ds.isPresent()) {
         return ds.get()
                  .value();
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
               log.debug("Reading Dynamic Sememe Index Configuration");

               try {
                  final HashMap<Integer, Integer[]> updatedWhatToIndex = new HashMap<>();
                  final Stream<SememeChronology<? extends SememeVersion<?>>> sememeCs = Get.sememeService()
                                                                                           .getSememesFromAssemblage(
                                                                                              DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                                                                                    .getSequence());

                  sememeCs.forEach(sememeC -> {
                                      if (sememeC.getSememeType() == SememeType.DYNAMIC) {
                                         @SuppressWarnings({ "unchecked", "rawtypes" })
                                         final Optional<LatestVersion<DynamicSememe>> dsv =
                                            ((SememeChronology) sememeC).getLatestVersion(DynamicSememe.class,
                                                                                          StampCoordinates.getDevelopmentLatest());

                                         if (dsv.isPresent() && (dsv.get().value().getState() == State.ACTIVE)) {
                                            final int assemblageToIndex = Get.identifierService()
                                                                             .getConceptSequence(dsv.get()
                                                                                   .value()
                                                                                   .getReferencedComponentNid());
                                            Integer[]                 finalCols = new Integer[] {};
                                            final DynamicSememeData[] data      = dsv.get()
                                                                                     .value()
                                                                                     .getData();

                                            if ((data != null) && (data.length > 0)) {
                                               @SuppressWarnings("unchecked")
                                               final DynamicSememeInteger[] colsToIndex =
                                                  ((DynamicSememeArray<DynamicSememeInteger>) data[0]).getDataArray();

                                               finalCols = new Integer[colsToIndex.length];

                                               for (int i = 0; i < colsToIndex.length; i++) {
                                                  finalCols[i] = colsToIndex[i].getDataInteger();
                                               }
                                            } else {
                                               log.warn(
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
                  log.error(
                      "Unexpected error reading Dynamic Sememe Index Configuration - generated index will be incomplete!",
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
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return true, if assemblage indexed
    */
   public static boolean isAssemblageIndexed(int assemblageConceptSequence) {
      return LookupService.get()
                          .getService(SememeIndexerConfiguration.class)
                          .needsIndexing(assemblageConceptSequence);
   }

   /**
    * Checks if column type indexable.
    *
    * @param dataType the data type
    * @return true, if column type indexable
    */
   public static boolean isColumnTypeIndexable(DynamicSememeDataType dataType) {
      if (dataType == DynamicSememeDataType.BYTEARRAY) {
         return false;
      }

      return true;
   }
}

