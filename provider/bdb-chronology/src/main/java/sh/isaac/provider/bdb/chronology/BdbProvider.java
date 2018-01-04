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



package sh.isaac.provider.bdb.chronology;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.InputStream;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Stream;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DiskOrderedCursor;
import com.sleepycat.je.DiskOrderedCursorConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.util.Map;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.DatabaseServices;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.LookupService;
import sh.isaac.api.MetadataService;
import sh.isaac.api.DatabaseServices.DatabaseValidity;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedByteArrayArrayMap;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.collections.SpinedNidNidSetMap;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.provider.bdb.binding.AssemblageObjectTypeMapBinding;
import sh.isaac.provider.bdb.binding.IntArrayBinding;
import sh.isaac.provider.bdb.binding.IntSpineBinding;
import sh.isaac.provider.bdb.binding.SequenceGeneratorBinding;
import sh.isaac.provider.bdb.taxonomy.TaxonomyRecord;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L0)  //TODO 0, really?
public class BdbProvider
         implements DatabaseServices, IdentifiedObjectService, MetadataService {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG                        = LogManager.getLogger();
   private static final String ASSEMBLAGE_NIDS            = "assemblageNids";
   private static final String MISC_MAP                   = "miscMap";
   private static final int    SEQUENCE_GENERATOR_MAP_KEY = 1;
   private static final int    ASSEMBLAGE_TYPE_MAP_KEY    = 2;

   //~--- fields --------------------------------------------------------------

   private final ConcurrentHashMap<String, Database>                databases              = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<String, SpinedByteArrayArrayMap> spinedChronologyMapMap = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<String, SpinedIntIntArrayMap>    spinedTaxonomyMapMap   = new ConcurrentHashMap<>();
   private final DatabaseConfig                                     chronologyDbConfig     = new DatabaseConfig();
   private final DatabaseConfig                                     noDupConfig            = new DatabaseConfig();
   private final ConcurrentSkipListSet<Integer>                     assemblageNids = new ConcurrentSkipListSet<>();
   private final SpinedNidNidSetMap                                 componentToSemanticMap = new SpinedNidNidSetMap();

   /**
    * The database validity.
    */
   private SimpleObjectProperty<DatabaseValidity> databaseValidity = 
         new SimpleObjectProperty<DatabaseServices.DatabaseValidity>(DatabaseValidity.NOT_YET_CHECKED);

   // TODO persist dataStoreId.
   private final UUID         dataStoreId        = UUID.randomUUID();
   private ChronologyLocation chronologyLocation = ChronologyLocation.SPINE;
   private Environment        myDbEnvironment;
   private Database           identifierDatabase;
   private Database           propertyDatabase;
   private Database           semanticMapDb;

   //~--- initializers --------------------------------------------------------

   {
      chronologyDbConfig.setAllowCreate(true);
      chronologyDbConfig.setDeferredWrite(true);
      chronologyDbConfig.setSortedDuplicates(true);
      noDupConfig.setSortedDuplicates(false);
      noDupConfig.setAllowCreate(true);
      noDupConfig.setDeferredWrite(true);
   }

   //~--- constant enums ------------------------------------------------------

   private enum ChronologyLocation { SPINE,
                                     BDB }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean importMetadata()
            throws Exception {
   	//TODO [DAN] this needs to be reworked - DatabaseValidity needs to go to VALID when the startMe completes, otherwise, 
   	//the startup sequence can't do what its supposed to, which is set aside a corrupt env and pull a new one.
   	//This parameter / feature got used for two different purposes while the development tracks were split... needs to be reconciled.
      if (this.databaseValidity.get() == DatabaseValidity.NO_DATASTORE) {
         Optional<DatabaseInitialization> initializationPreference = Get.applicationPreferences()
                                                                        .getEnum(DatabaseInitialization.class);

         if (initializationPreference.isPresent()) {
            if (initializationPreference.get() == DatabaseInitialization.LOAD_METADATA) {
               loadMetaData();
               return true;
            }
         }
      }

      return false;
   }

   public void putAssemblageTypeMap(ConcurrentHashMap<Integer, IsaacObjectType> map) {
      AssemblageObjectTypeMapBinding binding  = new AssemblageObjectTypeMapBinding();
      Database                       database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry                  key      = new DatabaseEntry();

      IntegerBinding.intToEntry(ASSEMBLAGE_TYPE_MAP_KEY, key);

      DatabaseEntry value = new DatabaseEntry();

      binding.objectToEntry(map, value);

      OperationStatus result = database.put(null, key, value);

      LOG.info("Put assemblage type map with result of: " + result);
   }

   public void putNidSet(String key, NidSet nidSet) {
      DatabaseEntry keyEntry = new DatabaseEntry();

      StringBinding.stringToEntry(key, keyEntry);

      DatabaseEntry   valueEntry = new DatabaseEntry();
      IntArrayBinding binding    = new IntArrayBinding();

      binding.objectToEntry(nidSet.asArray(), valueEntry);

      OperationStatus status = propertyDatabase.put(null, keyEntry, valueEntry);

      if (status != OperationStatus.SUCCESS) {
         throw new RuntimeException("Status = " + status);
      }
   }

   public void putNidToNidSet(int nid, NidSet nidSet) {
      DatabaseEntry keyEntry = new DatabaseEntry();

      IntegerBinding.intToEntry(nid, keyEntry);

      DatabaseEntry   valueEntry = new DatabaseEntry();
      IntArrayBinding binding    = new IntArrayBinding();

      binding.objectToEntry(nidSet.asArray(), valueEntry);

      OperationStatus status = semanticMapDb.put(null, keyEntry, valueEntry);

      if (status != OperationStatus.SUCCESS) {
         throw new RuntimeException("Status = " + status);
      }
   }

   public void putProperty(String property, String value) {
      DatabaseEntry keyEntry = new DatabaseEntry();

      StringBinding.stringToEntry(property, keyEntry);

      DatabaseEntry valueEntry = new DatabaseEntry();

      StringBinding.stringToEntry(value, valueEntry);

      OperationStatus status = propertyDatabase.put(null, keyEntry, valueEntry);

      if (status != OperationStatus.SUCCESS) {
         throw new RuntimeException("Status = " + status);
      }
   }

   public void putSequenceGeneratorMap(ConcurrentMap<Integer, AtomicInteger> assemblageNid_SequenceGenerator_Map) {
      SequenceGeneratorBinding binding  = new SequenceGeneratorBinding();
      Database                 database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry            key      = new DatabaseEntry();

      IntegerBinding.intToEntry(SEQUENCE_GENERATOR_MAP_KEY, key);

      DatabaseEntry value = new DatabaseEntry();

      binding.objectToEntry(assemblageNid_SequenceGenerator_Map, value);
      database.put(null, key, value);
   }

   public void putSpinedIntIntMap(String databaseKey, SpinedIntIntMap map) {
      Database                                   mapDatabase  = getNoDupDatabase(databaseKey);
      ConcurrentMap<Integer, AtomicIntegerArray> spineMap     = map.getSpines();
      IntSpineBinding                            spineBinding = new IntSpineBinding();

      spineMap.forEach(
          (key, spine) -> {
             DatabaseEntry keyEntry = new DatabaseEntry();

             IntegerBinding.intToEntry(key, keyEntry);

             DatabaseEntry valueEntry = new DatabaseEntry();

             spineBinding.objectToEntry(spine, valueEntry);

             OperationStatus status = mapDatabase.put(null, keyEntry, valueEntry);

             if (status != OperationStatus.SUCCESS) {
                throw new RuntimeException("Status = " + status);
             }
          });
   }

   public void putSpinedNidIntMap(String databaseKey, SpinedNidIntMap map) {
      Database             mapDatabase  = getNoDupDatabase(databaseKey);
      ConcurrentMap<Integer, AtomicIntegerArray> spineArray   = map.getSpines();
      IntSpineBinding      spineBinding = new IntSpineBinding();
      DatabaseEntry        keyEntry     = new DatabaseEntry();
      DatabaseEntry        valueEntry   = new DatabaseEntry();

      
      for (Map.Entry<Integer, AtomicIntegerArray> entry: spineArray.entrySet()) {

         IntegerBinding.intToEntry(entry.getKey(), keyEntry);
         spineBinding.objectToEntry(entry.getValue(), valueEntry);

         OperationStatus status = mapDatabase.put(null, keyEntry, valueEntry);

         if (status != OperationStatus.SUCCESS) {
            throw new RuntimeException("Status = " + status);
         }
      }
   }

   @Override
   public Future<?> sync() {
      Task<Void> syncTask = new SyncTask();

      return Get.executor()
                .submit(syncTask);
   }

   public void writeChronologyData(ChronologyImpl chronology) {
      int assemblageNid = chronology.getAssemblageNid();

      assemblageNids.add(assemblageNid);

      IsaacObjectType objectType       = chronology.getIsaacObjectType();
      int             assemblageForNid = ModelGet.identifierService()
                                                 .getAssemblageNidForNid(chronology.getNid());

      if (assemblageForNid == Integer.MAX_VALUE) {
         ModelGet.identifierService()
                 .setupNid(chronology.getNid(), assemblageNid, objectType);

         if (chronology instanceof SemanticChronologyImpl) {
            SemanticChronologyImpl semanticChronology     = (SemanticChronologyImpl) chronology;
            int                    referencedComponentNid = semanticChronology.getReferencedComponentNid();

            componentToSemanticMap.add(referencedComponentNid, semanticChronology.getNid());
         }
      }

      switch (chronologyLocation) {
      case BDB:
         writeChronologyDataToBdb(chronology, assemblageNid);
         break;

      case SPINE:
         writeChronologyDataToSpinedMap(chronology, assemblageNid);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle: " + chronologyLocation);
      }
   }

   public void writeChronologyDataToBdb(ChronologyImpl chronology, int assemblageNid) {
      DatabaseEntry key = new DatabaseEntry();

      IntegerBinding.intToEntry(chronology.getElementSequence(), key);

      List<byte[]> dataList = chronology.getDataList();
      Database     database = getChronologyDatabase(assemblageNid);

      for (byte[] data: dataList) {
         DatabaseEntry   value  = new DatabaseEntry(data);
         OperationStatus status = database.put(null, key, value);

         if (status != OperationStatus.SUCCESS) {
            throw new RuntimeException("Operation failed: " + status);
         }
      }
   }

   public void writeChronologyDataToSpinedMap(ChronologyImpl chronology, int assemblageNid) {
      SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid);
      int elementSequence = ModelGet.identifierService()
                                    .getElementSequenceForNid(chronology.getNid(), assemblageNid);

      spinedByteArrayArrayMap.put(elementSequence, chronology.getDataList());
   }

   protected static ByteArrayDataBuffer collectByteRecords(DatabaseEntry key,
         DatabaseEntry value,
         final Cursor cursor)
            throws IllegalStateException {
      ArrayList<byte[]> dataList = new ArrayList<>();
      byte[]            data     = value.getData();
      int               size     = data.length;

      dataList.add(data);

      while (cursor.getNextDup(key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
         data = value.getData();
         size = size + data.length;

         if ((data[0] == 0) && (data[1] == 0) && (data[2] == 0) && (data[3] == 0)) {
            dataList.add(0, data);
         } else {
            dataList.add(value.getData());
         }
      }

      ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(
                                           size + 4);  // room for 0 int value at end to indicate last version

      for (byte[] dataEntry: dataList) {
         byteBuffer.put(dataEntry);
      }

      byteBuffer.putInt(0);
      byteBuffer.rewind();

      if (byteBuffer.getInt() != 0) {
         throw new IllegalStateException("Record does not start with zero...");
      }

      return byteBuffer;
   }

   private void loadMetaData()
            throws Exception {
      InputStream dataStream = BdbProvider.class.getClassLoader()
                                                .getResourceAsStream("sh/isaac/IsaacMetadataAuxiliary.ibdf");
      final BinaryDataReaderService reader        = Get.binaryDataReader(dataStream);
      final CommitService           commitService = Get.commitService();

      reader.getStream()
            .forEach(
                (object) -> {
                   try {
                      commitService.importNoChecks(object);
                   } catch (Throwable e) {
                      e.printStackTrace();
                      throw e;
                   }
                });
      commitService.postProcessImportNoChecks();
   }

   private void populateMapFromBdb(SpinedNidNidSetMap map)
            throws DatabaseException {
      IntArrayBinding         binding   = new IntArrayBinding();
      DiskOrderedCursorConfig docc      = new DiskOrderedCursorConfig();
      DatabaseEntry           foundKey  = new DatabaseEntry();
      DatabaseEntry           foundData = new DatabaseEntry();

      try (DiskOrderedCursor cursor = semanticMapDb.openCursor(docc)) {
         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            map.put(IntegerBinding.entryToInt(foundKey), binding.entryToObject(foundData));
         }
      }
   }

   private Database populateMapFromBdb(SpinedIntIntArrayMap origin_DestinationTaxonomyRecord_Map,
         int assemblageNid)
            throws DatabaseException {
      ContainerSequenceService idService = ModelGet.identifierService();
      IntArrayBinding          binding   = new IntArrayBinding();
      DiskOrderedCursorConfig  docc      = new DiskOrderedCursorConfig();
      DatabaseEntry            foundKey  = new DatabaseEntry();
      DatabaseEntry            foundData = new DatabaseEntry();

      origin_DestinationTaxonomyRecord_Map.setElementStringConverter(
          (int[] records) -> {
             return new TaxonomyRecord(records).toString();
          });

      Database database = getTaxonomyDatabase(assemblageNid);

      try (DiskOrderedCursor cursor = database.openCursor(docc)) {
         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int elementSequence = IntegerBinding.entryToInt(foundKey);
            int nid             = idService.getNidForElementSequence(elementSequence, assemblageNid);

            origin_DestinationTaxonomyRecord_Map.put(nid, binding.entryToObject(foundData));
         }
      }

      return database;
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting BDB provider post-construct");

      try {
         MemoryConfiguration memoryConfiguration = Get.applicationPreferences()
                                                      .getEnum(MemoryConfiguration.ALL_CHRONICLES_MANAGED_BY_DB);

         switch (memoryConfiguration) {
         case ALL_CHRONICLES_IN_MEMORY:
         case SELECTED_CHRONICLES_IN_MEMORY:
            this.chronologyLocation = ChronologyLocation.SPINE;
            break;

         case ALL_CHRONICLES_MANAGED_BY_DB:
            this.chronologyLocation = ChronologyLocation.BDB;
         }

         EnvironmentConfig envConfig = new EnvironmentConfig();
         final Path folderPath = LookupService.getService(ConfigurationService.class)
                                              .getChronicleFolderPath()
                                              .resolve("bdb");

         envConfig.setAllowCreate(true);

         File dbEnv = folderPath.toFile();

         if (!dbEnv.exists()) {
            File dataFolderFile = folderPath.getParent()
                                            .getParent()
                                            .getParent()
                                            .toFile();
            File solorDbFolder  = new File(dataFolderFile, "solor-db.data");
            File metaDbFolder   = new File(dataFolderFile, "meta-db.data");
            File isaacDbFolder  = new File(dataFolderFile, "isaac.data");

            if (solorDbFolder.exists()) {
               solorDbFolder.renameTo(isaacDbFolder);
            } else if (metaDbFolder.exists()) {
               metaDbFolder.renameTo(isaacDbFolder);
            } else {
               this.databaseValidity.set(DatabaseValidity.NO_DATASTORE);
            }
         }

         dbEnv.mkdirs();
         myDbEnvironment    = new Environment(dbEnv, envConfig);
         propertyDatabase   = myDbEnvironment.openDatabase(null, "property", noDupConfig);
         identifierDatabase = myDbEnvironment.openDatabase(null, "identifier", noDupConfig);
         semanticMapDb      = myDbEnvironment.openDatabase(null, "semantic", noDupConfig);
         LOG.info("identifier count at open: " + identifierDatabase.count());

         switch (this.chronologyLocation) {
         case BDB:
            populateMapFromBdb(componentToSemanticMap);
            break;

         case SPINE:
            componentToSemanticMap.read(getComponentToSemanticMapDirectory());
            break;
         }

         NidSet assemblageNidsSet = getNidSet(ASSEMBLAGE_NIDS);

         for (int nid: assemblageNidsSet.asArray()) {
            assemblageNids.add(nid);
         }

         LOG.info("Off heap cache size: " + myDbEnvironment.getConfig().getOffHeapCacheSize());
         LOG.info("Max disk: " + myDbEnvironment.getConfig().getMaxDisk());
         LOG.info("MAX_MEMORY: " + myDbEnvironment.getConfig().getConfigParam(EnvironmentConfig.MAX_MEMORY));
         LOG.info(
             "MAX_MEMORY_PERCENT: " + myDbEnvironment.getConfig().getConfigParam(EnvironmentConfig.MAX_MEMORY_PERCENT));
         LOG.info(
             "MAX_OFF_HEAP_MEMORY: " + myDbEnvironment.getConfig().getConfigParam(
                 EnvironmentConfig.MAX_OFF_HEAP_MEMORY));
      } catch (Throwable dbe) {
         dbe.printStackTrace();
         throw new RuntimeException(dbe);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping BDB Provider.");

      try {
         if (myDbEnvironment != null) {
            // The IO non-blocking executor - set core threads equal to max - otherwise, it will never increase the thread count
            // with an unbounded queue.
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                                              4,
                                              4,
                                              60,
                                              TimeUnit.SECONDS,
                                              new LinkedBlockingQueue<>(),
                                              new NamedThreadFactory("BDB-Shutdown-work-thread", true));

            executor.allowCoreThreadTimeOut(true);

            Task<Void> syncTask = new SyncTask();

            executor.submit(syncTask)
                    .get();
            databases.forEach(
                (key, database) -> {
                   LOG.info("Closing: " + key);
                   database.close();
                });
            LOG.info("property count at close: " + propertyDatabase.count());
            LOG.info("identifier count at close: " + identifierDatabase.count());
            LOG.info("closing property database. ");
            propertyDatabase.close();
            LOG.info("closing taxonomy database. ");
            identifierDatabase.close();
            LOG.info("closing semantic index database. ");
            semanticMapDb.close();
            myDbEnvironment.close();
         }
      } catch (Throwable ex) {
         ex.printStackTrace();
         LOG.error(ex);
         throw new RuntimeException(ex);
      }
   }

   private void writeMapToBdb(SpinedNidNidSetMap map) {
      IntArrayBinding binding = new IntArrayBinding();
      DatabaseEntry   key     = new DatabaseEntry();
      DatabaseEntry   data    = new DatabaseEntry();

      map.forEach(
          (int nid,
           int[] value) -> {
             IntegerBinding.intToEntry(nid, key);
             binding.objectToEntry(value, data);

             OperationStatus status = semanticMapDb.put(null, key, data);

             if (status != OperationStatus.SUCCESS) {
                String message = "Write to semanticMapDb failed: " + status;

                LOG.error(message);
                throw new RuntimeException(message);
             }
          });
   }

   private Database writeTaxonomyMapToBdb(SpinedIntIntArrayMap taxonomyMap,
         int assemblageNid)
            throws DatabaseException {
      IntArrayBinding binding  = new IntArrayBinding();
      DatabaseEntry   key      = new DatabaseEntry();
      DatabaseEntry   data     = new DatabaseEntry();
      Database        database = getTaxonomyDatabase(assemblageNid);

      taxonomyMap.forEach(
          (int elementSequence,
           int[] value) -> {
             IntegerBinding.intToEntry(elementSequence, key);
             binding.objectToEntry(value, data);

             OperationStatus status = database.put(null, key, data);

             if (status != OperationStatus.SUCCESS) {
                String message = "Write to semanticMapDb failed: " + status;

                LOG.error(message);
                throw new RuntimeException(message);
             }
          });
      return database;
   }

   //~--- get methods ---------------------------------------------------------

   public ConcurrentSkipListSet<Integer> getAssemblageNids() {
      return assemblageNids;
   }

   public ConcurrentHashMap<Integer, IsaacObjectType> getAssemblageTypeMap() {
      Database      database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry key      = new DatabaseEntry();

      IntegerBinding.intToEntry(ASSEMBLAGE_TYPE_MAP_KEY, key);

      DatabaseEntry                  value   = new DatabaseEntry();
      AssemblageObjectTypeMapBinding binding = new AssemblageObjectTypeMapBinding();

      if (database.get(null, key, value, null) == OperationStatus.SUCCESS) {
         return binding.entryToObject(value);
      }

      return new ConcurrentHashMap<>();
   }

   public Optional<ByteArrayDataBuffer> getChronologyData(int nid) {
      switch (chronologyLocation) {
      case BDB:
         return getChronologyDataFromBdb(nid);

      case SPINE:
         return getChronologyDataFromSpine(nid);

      default:
         throw new UnsupportedOperationException("Can't handle: " + chronologyLocation);
      }
   }

   public Optional<ByteArrayDataBuffer> getChronologyDataFromBdb(int nid)
            throws IllegalStateException {
      int      assemblageNid   = ModelGet.identifierService()
                                         .getAssemblageNid(nid)
                                         .getAsInt();
      int      elementSequence = ModelGet.identifierService()
                                         .getElementSequenceForNid(nid, assemblageNid);
      Database database        = getChronologyDatabase(assemblageNid);

      try (Cursor cursor = database.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry key = new DatabaseEntry();

         IntegerBinding.intToEntry(elementSequence, key);

         DatabaseEntry   value  = new DatabaseEntry();
         OperationStatus status = cursor.getSearchKey(key, value, LockMode.DEFAULT);

         switch (status) {
         case KEYEMPTY:
         case KEYEXIST:
         case NOTFOUND:
            return Optional.empty();

         case SUCCESS:
            return Optional.of(collectByteRecords(key, value, cursor));
         }
      }

      return Optional.empty();
   }

   public Optional<ByteArrayDataBuffer> getChronologyDataFromSpine(int nid)
            throws IllegalStateException {
      OptionalInt assemblageNidOptional = ModelGet.identifierService()
                                                  .getAssemblageNid(nid);

      if (assemblageNidOptional.isPresent()) {
         int                     assemblageNid           = assemblageNidOptional.getAsInt();
         int elementSequence = ModelGet.identifierService()
                                       .getElementSequenceForNid(nid, assemblageNid);
         SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid);
         byte[][]                data                    = spinedByteArrayArrayMap.get(elementSequence);

         if (data == null) {
            return Optional.empty();
         }

         int size = 0;

         for (byte[] dataEntry: data) {
            size = size + dataEntry.length;
         }

         ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(
                                              size + 4);  // room for 0 int value at end to indicate last version

         for (byte[] dataEntry: data) {
            byteBuffer.put(dataEntry);
         }

         byteBuffer.putInt(0);
         byteBuffer.rewind();

         if (byteBuffer.getInt() != 0) {
            throw new IllegalStateException("Record does not start with zero...");
         }

         return Optional.of(byteBuffer);
      }
      throw new IllegalStateException("Assemblage nid is not present. ");
   }

   public Database getChronologyDatabase(int assemblageNid) {
      String   databaseKey = "Chronology" + assemblageNid;
      Database database    = databases.computeIfAbsent(
                                 databaseKey,
                                     (dbKey) -> {
                                        Database db = myDbEnvironment.openDatabase(null, dbKey, chronologyDbConfig);

                                        LOG.info("Opening " + dbKey + " count at open: " + db.count());
                                        return db;
                                     });

      return database;
   }

   public SpinedByteArrayArrayMap getChronologySpinedMap(int assemblageNid) {
      String                  spinedMapKey = "ChronologySpinedMap" + assemblageNid;
      SpinedByteArrayArrayMap spinedMap    = spinedChronologyMapMap.computeIfAbsent(
                                                 spinedMapKey,
                                                       (dbKey) -> {
               SpinedByteArrayArrayMap spinedByteArrayArrayMap = new SpinedByteArrayArrayMap();

               spinedByteArrayArrayMap.read(getSpinedByteArrayArrayMapDirectory(dbKey));
               LOG.info("Opening " + dbKey);
               return spinedByteArrayArrayMap;
            });

      return spinedMap;
   }

   File getComponentToSemanticMapDirectory() {
      final Path folderPath = LookupService.getService(ConfigurationService.class)
                                           .getChronicleFolderPath()
                                           .resolve("bdb");
      File spinedMapDirectory = new File(folderPath.toFile(), "ComponentToSemanticMap");

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   public SpinedNidNidSetMap getComponentToSemanticNidsMap() {
      return componentToSemanticMap;
   }

   @Override
   public UUID getDataStoreId() {
      return dataStoreId;
   }

   @Override
   public Path getDatabaseFolder() {
      return this.myDbEnvironment.getHome()
                                 .toPath();
   }

   @Override
   public ObservableObjectValue<DatabaseValidity> getDatabaseValidityStatus() {
      return this.databaseValidity;
   }

   @Override
   public Optional<? extends Chronology> getIdentifiedObjectChronology(int nid) {
      try {
         Optional<ByteArrayDataBuffer> optionalByteBuffer = getChronologyData(nid);

         if (optionalByteBuffer.isPresent()) {
            ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

            // concept or semantic?
            switch (ModelGet.identifierService()
                            .getObjectTypeForComponent(nid)) {
            case CONCEPT:
               IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
               return Optional.of(ConceptChronologyImpl.make(byteBuffer));

            case SEMANTIC:
               IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
               return Optional.of(SemanticChronologyImpl.make(byteBuffer));

            default:
               throw new UnsupportedOperationException(
                   "Can't handle: " + ModelGet.identifierService().getObjectTypeForComponent(nid));
            }
         }
      } catch (NoSuchElementException nse) {
         return Optional.empty();
      }

      return Optional.empty();
   }

   public Database getIdentifierDatabase() {
      return identifierDatabase;
   }

   private int getNidFromKey(String key) {
      int index = key.indexOf('-');

      return Integer.parseInt(key.substring(index));
   }

   public NidSet getNidSet(String key) {
      DatabaseEntry keyEntry = new DatabaseEntry();

      StringBinding.stringToEntry(key, keyEntry);

      DatabaseEntry   valueEntry = new DatabaseEntry();
      OperationStatus status     = propertyDatabase.get(null, keyEntry, valueEntry, null);

      if (status != OperationStatus.SUCCESS) {
         if (status == OperationStatus.NOTFOUND) {
            return new NidSet();
         }

         throw new RuntimeException("Status = " + status);
      }

      IntArrayBinding binding = new IntArrayBinding();

      return NidSet.of(binding.entryToObject(valueEntry));
   }

   public Database getNoDupDatabase(String databaseKey) {
      Database database = databases.computeIfAbsent(
                              databaseKey,
                                  (dbKey) -> {
                                     Database db = myDbEnvironment.openDatabase(null, dbKey, noDupConfig);

                                     LOG.info("Opening " + dbKey + " count at open: " + db.count());
                                     return db;
                                  });

      return database;
   }

   public Optional<String> getProperty(String property) {
      DatabaseEntry key = new DatabaseEntry();

      StringBinding.stringToEntry(property, key);

      DatabaseEntry   value  = new DatabaseEntry();
      OperationStatus status = propertyDatabase.get(null, key, value, null);

      if (status == OperationStatus.SUCCESS) {
         return Optional.of(StringBinding.entryToString(value));
      }

      return Optional.empty();
   }

   public ConcurrentMap<Integer, AtomicInteger> getSequenceGeneratorMap() {
      Database      database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry key      = new DatabaseEntry();

      IntegerBinding.intToEntry(SEQUENCE_GENERATOR_MAP_KEY, key);

      DatabaseEntry            value   = new DatabaseEntry();
      SequenceGeneratorBinding binding = new SequenceGeneratorBinding();

      if (database.get(null, key, value, null) == OperationStatus.SUCCESS) {
         return binding.entryToObject(value);
      }

      return new ConcurrentHashMap<>();
   }

   File getSpinedByteArrayArrayMapDirectory(int assemblageNid) {
      final Path folderPath = LookupService.getService(ConfigurationService.class)
                                           .getChronicleFolderPath()
                                           .resolve("bdb");
      String spinedMapName      = "ChronologySpinedMap" + assemblageNid;
      File   spinedMapDirectory = new File(folderPath.toFile(), spinedMapName);

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   File getSpinedByteArrayArrayMapDirectory(String spinedMapName) {
      final Path folderPath = LookupService.getService(ConfigurationService.class)
                                           .getChronicleFolderPath()
                                           .resolve("bdb");
      File spinedMapDirectory = new File(folderPath.toFile(), spinedMapName);

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   File getSpinedIntIntArrayMapDirectory(String spinedMapName) {
      final Path folderPath = LookupService.getService(ConfigurationService.class)
                                           .getChronicleFolderPath()
                                           .resolve("bdb");
      File spinedMapDirectory = new File(folderPath.toFile(), spinedMapName);

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   public SpinedIntIntMap getSpinedIntIntMap(String databaseKey) {
      Database        mapDatabase = getNoDupDatabase(databaseKey);
      SpinedIntIntMap map         = new SpinedIntIntMap();

      try (Cursor cursor = mapDatabase.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry   foundKey     = new DatabaseEntry();
         DatabaseEntry   foundData    = new DatabaseEntry();
         IntSpineBinding spineBinding = new IntSpineBinding();

         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int                spineKey  = IntegerBinding.entryToInt(foundKey);
            AtomicIntegerArray spineData = spineBinding.entryToObject(foundData);

            map.getSpines()
               .put(spineKey, spineData);
         }
      }

      return map;
   }

   public SpinedNidIntMap getSpinedNidIntMap(String databaseKey) {
      Database        mapDatabase = getNoDupDatabase(databaseKey);
      SpinedNidIntMap map         = new SpinedNidIntMap();

      try (Cursor cursor = mapDatabase.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry   foundKey     = new DatabaseEntry();
         DatabaseEntry   foundData    = new DatabaseEntry();
         IntSpineBinding spineBinding = new IntSpineBinding();

         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int                spineKey  = IntegerBinding.entryToInt(foundKey);
            AtomicIntegerArray spineData = spineBinding.entryToObject(foundData);

            map.addSpine(spineKey, spineData);
         }
      }

      return map;
   }

   public static Stream<? extends Chronology> getStream() {
      throw new UnsupportedOperationException();
   }

   private Database getTaxonomyDatabase(int assemblageNid) {
      String   databaseKey = "Taxonomy" + assemblageNid;
      Database database    = databases.computeIfAbsent(
                                 databaseKey,
                                     (dbKey) -> {
                                        Database db = myDbEnvironment.openDatabase(null, dbKey, chronologyDbConfig);

                                        LOG.info("Opening " + dbKey + " count at open: " + db.count());
                                        return db;
                                     });

      return database;
   }

   public SpinedIntIntArrayMap getTaxonomyMap(int assemblageNid) {
      switch (chronologyLocation) {
      case BDB: {
         String spinedMapKey = "TaxonomySpinedMap" + assemblageNid;

         if (spinedTaxonomyMapMap.containsKey(spinedMapKey)) {
            return spinedTaxonomyMapMap.get(spinedMapKey);
         }

         SpinedIntIntArrayMap origin_DestinationTaxonomyRecord_Map = spinedTaxonomyMapMap.computeIfAbsent(
                                                                         spinedMapKey,
                                                                               (dbKey) -> {
                  SpinedIntIntArrayMap spinedIntIntArrayMap = new SpinedIntIntArrayMap();

                  spinedIntIntArrayMap.read(getSpinedIntIntArrayMapDirectory(dbKey));
                  LOG.info("Opening " + dbKey);
                  return spinedIntIntArrayMap;
               });
         Database database = populateMapFromBdb(origin_DestinationTaxonomyRecord_Map, assemblageNid);

         LOG.info("Taxonomy count at open for " + database.getDatabaseName() + " is " + database.count());
         return origin_DestinationTaxonomyRecord_Map;
      }

      case SPINE:
         return getTaxonomySpinedMap(assemblageNid);

      default:
         throw new UnsupportedOperationException("Can't handle: " + chronologyLocation);
      }
   }

   public SpinedIntIntArrayMap getTaxonomySpinedMap(int assemblageNid) {
      String               spinedMapKey = "TaxonomySpinedMap" + assemblageNid;
      SpinedIntIntArrayMap spinedMap    = spinedTaxonomyMapMap.computeIfAbsent(
                                              spinedMapKey,
                                                    (dbKey) -> {
               SpinedIntIntArrayMap spinedIntIntArrayMap = new SpinedIntIntArrayMap();

               spinedIntIntArrayMap.read(getSpinedIntIntArrayMapDirectory(dbKey));
               LOG.info("Opening " + dbKey);
               return spinedIntIntArrayMap;
            });

      return spinedMap;
   }

   //~--- inner classes -------------------------------------------------------

   private class SyncTask
           extends TimedTaskWithProgressTracker<Void> {
      public SyncTask() {
         updateTitle("Writing data to disk");
         addToTotalWork(8);
         Get.activeTasks()
            .add(this);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      protected Void call()
               throws Exception {
         try {
            switch (chronologyLocation) {
            case BDB: {
               updateMessage("Writing assemblage nids...");

               NidSet assemblageNidSet = NidSet.of(assemblageNids);

               putNidSet(ASSEMBLAGE_NIDS, assemblageNidSet);
               completedUnitOfWork();
               updateMessage("Writing component to semantics map...");
               writeMapToBdb(componentToSemanticMap);
               completedUnitOfWork();
               updateMessage("Writing taxonomy spines...");
               spinedTaxonomyMapMap.forEach(
                   (String key,
                    SpinedIntIntArrayMap spinedMap) -> {
                      LOG.info("Syncronizing: " + key);
                      writeTaxonomyMapToBdb(spinedMap, getNidFromKey(key));
                   });
               completedUnitOfWork();
               break;
            }

            case SPINE: {
               updateMessage("Writing assemblage nids...");

               NidSet assemblageNidSet = NidSet.of(assemblageNids);

               putNidSet(ASSEMBLAGE_NIDS, assemblageNidSet);
               completedUnitOfWork();
               updateMessage("Writing component to semantics map...");
               componentToSemanticMap.write(getComponentToSemanticMapDirectory());
               completedUnitOfWork();
               updateMessage("Writing chronology spines...");
               spinedChronologyMapMap.forEach(
                   (key, spinedMap) -> {
                      LOG.info("Syncronizing: " + key);
                      spinedMap.write(getSpinedByteArrayArrayMapDirectory(key));
                   });
               completedUnitOfWork();
               updateMessage("Writing taxonomy spines...");
               spinedTaxonomyMapMap.forEach(
                   (key, spinedMap) -> {
                      LOG.info("Syncronizing: " + key);
                      spinedMap.write(getSpinedIntIntArrayMapDirectory(key));
                   });
               completedUnitOfWork();
               break;
            }

            default:
               throw new UnsupportedOperationException("Can't handle chronology location: " + chronologyLocation);
            }

            updateMessage("Writing databases...");
            databases.forEach(
                (key, database) -> {
                   LOG.info("Syncronizing: " + key + " count: " + database.count());
                   database.sync();
                });
            completedUnitOfWork();
            updateMessage("Writing identifier database...");
            LOG.info("Syncronizing identifier database count: " + identifierDatabase.count());
            identifierDatabase.sync();
            completedUnitOfWork();
            updateMessage("Writing property database...");
            LOG.info("Syncronizing property database count: " + propertyDatabase.count());
            propertyDatabase.sync();
            completedUnitOfWork();
            updateMessage("Writing database environment...");
            myDbEnvironment.sync();
            completedUnitOfWork();
            updateMessage("Write complete");
            return null;
         } finally {
            Get.activeTasks()
               .remove(this);
         }
      }
   }
}

