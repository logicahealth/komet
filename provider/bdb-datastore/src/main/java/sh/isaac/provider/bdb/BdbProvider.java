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



package sh.isaac.provider.bdb;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
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
import javafx.concurrent.Task;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.datastore.SequenceStore;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.collections.SpinedNidNidSetMap;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.taxonomy.TaxonomyRecord;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
/** Align this with {@link DatabaseImplementation#BDB} */
@Service (name="BDB")
@Singleton
@Rank(value=-10)
//However, rather than disabling services, we should likely have a HK2 "name" on each DataStore implementation, and 
//a user preference to select which one we run with.
//TODO [DAN 3] [KEC] All of the fields set up in the class construction need to be properly cleared / purged on a shutdown/startup cycle.
public class BdbProvider
         implements DataStoreSubService, SequenceStore {
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
   private final ConcurrentHashMap<String, SpinedIntIntArrayMap>    spinedTaxonomyMapMap   = new ConcurrentHashMap<>();
   private final DatabaseConfig                                     chronologyDbConfig     = new DatabaseConfig();
   private final DatabaseConfig                                     noDupConfig            = new DatabaseConfig();
   private final ConcurrentSkipListSet<Integer>                     assemblageNids = new ConcurrentSkipListSet<>();
   private final SpinedNidNidSetMap                                 componentToSemanticMap = new SpinedNidNidSetMap();
   private ArrayList<DataWriteListener> writeListeners = new ArrayList<>();

   /**
    * The database validity.
    */
   private DataStoreStartState databaseStartState = DataStoreStartState.NOT_YET_CHECKED;

   private Optional<UUID>      dataStoreId    = Optional.empty();
   private Future<?>       lastSyncFuture = null;
   private final Semaphore syncSemaphore  = new Semaphore(1);
   private final Semaphore pendingSync    = new Semaphore(1);
   private Environment     myDbEnvironment;
   private Database        propertyDatabase;
   private Database        semanticMapDb;
   private SyncTask        lastSyncTask;

   //~--- initializers --------------------------------------------------------

   {
      chronologyDbConfig.setAllowCreate(true);
      chronologyDbConfig.setDeferredWrite(true);
      chronologyDbConfig.setSortedDuplicates(true);
      noDupConfig.setSortedDuplicates(false);
      noDupConfig.setAllowCreate(true);
      noDupConfig.setDeferredWrite(true);
   }

   //~--- methods -------------------------------------------------------------
// TODO abandoned?
//   private void putAssemblageTypeMap(ConcurrentHashMap<Integer, IsaacObjectType> map) {
//      AssemblageObjectTypeMapBinding binding  = new AssemblageObjectTypeMapBinding();
//      Database                       database = getNoDupDatabase(MISC_MAP);
//      DatabaseEntry                  key      = new DatabaseEntry();
//
//      IntegerBinding.intToEntry(ASSEMBLAGE_TYPE_MAP_KEY, key);
//
//      DatabaseEntry value = new DatabaseEntry();
//
//      binding.objectToEntry(map, value);
//
//      OperationStatus result = database.put(null, key, value);
//
//      LOG.info("Put assemblage type map with result of: " + result);
//   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void putChronologyData(ChronologySerializeable chronology) {
      int assemblageNid = chronology.getAssemblageNid();

      assemblageNids.add(assemblageNid);
      
      if (chronology instanceof SemanticChronologyImpl) {
         SemanticChronologyImpl semanticChronology     = (SemanticChronologyImpl) chronology;
         int referencedComponentNid = semanticChronology.getReferencedComponentNid();
         //We could optionally check and see if this chronology is already listed for this nid, but its likely cheaper to just let it merge internally
         componentToSemanticMap.add(referencedComponentNid, semanticChronology.getNid());
      }

      DatabaseEntry key = new DatabaseEntry();

      IntegerBinding.intToEntry(getElementSequenceForNid(chronology.getNid()), key);

      //TODO [KEC] this probably isn't right, see the changes made in commit 7064a7b50cac9666fd93d252605d2d25ad173d86 to FileSystemDataStore, 
      //specifically, the getDataList method.
      //This could probably be redone with the new methods to get the versions distinct from the chronologies
      byte[] data = chronology.getChronologyVersionDataToWrite();
      Database     database = getChronologyDatabase(assemblageNid);

      DatabaseEntry   value  = new DatabaseEntry(data);
      OperationStatus status = database.put(null, key, value);

      if (status != OperationStatus.SUCCESS) {
         throw new RuntimeException("Operation failed: " + status);
      }
      
      for (DataWriteListener dwl : writeListeners) {
          dwl.writeData(chronology);
       }
   }

   private void putSequenceGeneratorMap(ConcurrentMap<Integer, AtomicInteger> assemblageNid_SequenceGenerator_Map) {
      SequenceGeneratorBinding binding  = new SequenceGeneratorBinding();
      Database                 database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry            key      = new DatabaseEntry();

      IntegerBinding.intToEntry(SEQUENCE_GENERATOR_MAP_KEY, key);

      DatabaseEntry value = new DatabaseEntry();

      binding.objectToEntry(assemblageNid_SequenceGenerator_Map, value);
      database.put(null, key, value);
   }

   private void putSpinedIntIntMap(String databaseKey, SpinedIntIntMap map) {
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

   private void putSpinedNidIntMap(String databaseKey, SpinedNidIntMap map) {
      Database                                   mapDatabase  = getNoDupDatabase(databaseKey);
      ConcurrentMap<Integer, AtomicIntegerArray> spineArray   = map.getSpines();
      IntSpineBinding                            spineBinding = new IntSpineBinding();
      DatabaseEntry                              keyEntry     = new DatabaseEntry();
      DatabaseEntry                              valueEntry   = new DatabaseEntry();

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
      if (pendingSync.tryAcquire()) {
         lastSyncTask   = new SyncTask();
         lastSyncFuture = Get.executor()
                             .submit(lastSyncTask);
         return lastSyncFuture;
      }

      return lastSyncFuture;
   }

   static ByteArrayDataBuffer collectByteRecords(DatabaseEntry key,
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
      IdentifierService        idService = ModelGet.identifierService();
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
            int nid             = getAssemblageNid_ElementSequenceToNid_Map(assemblageNid).get(elementSequence);

            origin_DestinationTaxonomyRecord_Map.put(nid, binding.entryToObject(foundData));
         }
      }

      return database;
   }

   private void putNidSet(String key, NidSet nidSet) {
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

   /**
    * {@inheritDoc}
    */
   @Override
   public void startup() {
      try {
         MemoryConfiguration memoryConfiguration = Get.configurationService().getGlobalDatastoreConfiguration().getMemoryConfiguration()
               .orElse(MemoryConfiguration.ALL_CHRONICLES_MANAGED_BY_DB);

         LOG.info("Starting BDB provider. Memory configuration is: " + memoryConfiguration);

         switch (memoryConfiguration) {
         case ALL_CHRONICLES_IN_MEMORY:
         case SELECTED_CHRONICLES_IN_MEMORY:
         case ALL_CHRONICLES_MANAGED_BY_DB:
         default:

         // not used for now
         }

         EnvironmentConfig envConfig = new EnvironmentConfig();
         final Path folderPath = LookupService.getService(ConfigurationService.class)
                                              .getDataStoreFolderPath()
                                              .resolve("object-chronicles")
                                              .resolve("bdb");

         envConfig.setAllowCreate(true);

         File dbEnv = folderPath.toFile();

         if (!dbEnv.exists()) {
            File dataFolderFile = folderPath.getParent()
                                            .getParent()
                                            .getParent()
                                            .toFile();
            File solorDbFolder  = new File(dataFolderFile, "solor-db.data");
            File isaacDbFolder  = new File(dataFolderFile, "isaac.data");

            if (solorDbFolder.exists()) {
               solorDbFolder.renameTo(isaacDbFolder);
            } else {
               this.databaseStartState = DataStoreStartState.NO_DATASTORE;
            }
         }
         else
         {
             this.databaseStartState = DataStoreStartState.EXISTING_DATASTORE;
         }

         dbEnv.mkdirs();
         
         //If the DBID is missing, we better be in NO_DATASTORE state.
         if (!new File(dbEnv, DATASTORE_ID_FILE).isFile())
         {
            if (this.databaseStartState != DataStoreStartState.NO_DATASTORE)
            {
               //This may happen during transition, for a bit, since old DBs don't have them.  
               //But after transition, this should always be created as part of the DB.
               //It also gets written as a semantic on the root concept, so a secondary check will be done 
               //later to make sure we are in sync.
               LOG.warn("The datastore id file was missing on startup!");
            }
            Files.write(dbEnv.toPath().resolve(DATASTORE_ID_FILE), UUID.randomUUID().toString().getBytes());
         }
         dataStoreId = Optional.of(UUID.fromString(new String(Files.readAllBytes(dbEnv.toPath().resolve(DATASTORE_ID_FILE)))));
         
         myDbEnvironment    = new Environment(dbEnv, envConfig);
         propertyDatabase   = myDbEnvironment.openDatabase(null, "property", noDupConfig);
         semanticMapDb      = myDbEnvironment.openDatabase(null, "semantic", noDupConfig);

         populateMapFromBdb(componentToSemanticMap);
  
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
         LOG.info(
             "ENV_LATCH_TIMEOUT: " + myDbEnvironment.getConfig().getConfigParam(EnvironmentConfig.ENV_LATCH_TIMEOUT));
      } catch (Throwable dbe) {
         dbe.printStackTrace();
         throw new RuntimeException(dbe);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown() {
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

            pendingSync.acquire();
            executor.submit(syncTask)
                    .get();
            databases.forEach(
                (key, database) -> {
                   LOG.info("Closing: " + key);
                   database.close();
                });
            LOG.info("property count at close: " + propertyDatabase.count());
            LOG.info("closing property database. ");
            propertyDatabase.close();
            LOG.info("closing semantic index database. ");
            semanticMapDb.close();
            myDbEnvironment.close();
            databaseStartState = DataStoreStartState.NOT_YET_CHECKED;
            dataStoreId = Optional.empty();
            this.writeListeners.clear();
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

   @Override
   public int[] getAssemblageConceptNids() {
      int[] assemblageConceptNids = new int[assemblageNids.size()];
      int   i                     = 0;

      for (Integer assemblageConceptNid: assemblageNids) {
         assemblageConceptNids[i++] = assemblageConceptNid;
      }

      return assemblageConceptNids;
   }

   private ConcurrentHashMap<Integer, IsaacObjectType> getAssemblageObjectTypeMap() {
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
   
   @Override
   public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid) {
       IsaacObjectType result = getAssemblageObjectTypeMap().get(assemblageNid);
       if (result == null) {
          return IsaacObjectType.UNKNOWN;
       }
       return result;
   }

   @Override
   public NidSet getAssemblageNidsForType(IsaacObjectType searchType) {
      final NidSet results = new NidSet();
      getAssemblageObjectTypeMap().forEach((nid, type)-> {
         if (searchType == type) {
            results.add(nid);
         }
      });
      return results;
   }

   @Override
   public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException
   {
      IsaacObjectType oldValue = getAssemblageObjectTypeMap().computeIfAbsent(assemblageNid, (i -> type));
      if (oldValue != type) {
         throw new IllegalStateException("Tried to change the isaac object type of " + assemblageNid + " from " + oldValue + " to " + type);
      }
   }


   @Override
   public Optional<ByteArrayDataBuffer> getChronologyVersionData(int nid) {
      int      assemblageNid   = ModelGet.identifierService()
                                         .getAssemblageNid(nid)
                                         .getAsInt();
      int      elementSequence = getElementSequenceForNid(nid, assemblageNid);
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

   private Database getChronologyDatabase(int assemblageNid) {
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

   File getComponentToSemanticMapDirectory() {
      final Path folderPath = LookupService.getService(ConfigurationService.class)
                                           .getDataStoreFolderPath()
                                           .resolve("object-chronicles")
                                           .resolve("bdb");
      File spinedMapDirectory = new File(folderPath.toFile(), "ComponentToSemanticMap");

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   @Override
   public int[] getSemanticNidsForComponent(int componentNid) {
      return componentToSemanticMap.get(componentNid);
   }

   @Override
   public Optional<UUID> getDataStoreId() {
      return dataStoreId;
   }

   @Override
   public Path getDataStorePath() {
      return this.myDbEnvironment.getHome()
                                 .toPath();
   }

   @Override
   public DataStoreStartState getDataStoreStartState() {
      return this.databaseStartState;
   }

   private int getNidFromKey(String key) {
      int index = key.indexOf('-');

      return Integer.parseInt(key.substring(index));
   }

   private NidSet getNidSet(String key) {
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

   private Database getNoDupDatabase(String databaseKey) {
      Database database = databases.computeIfAbsent(
                              databaseKey,
                                  (dbKey) -> {
                                     Database db = myDbEnvironment.openDatabase(null, dbKey, noDupConfig);

                                     LOG.info("Opening " + dbKey + " count at open: " + db.count());
                                     return db;
                                  });

      return database;
   }

   private ConcurrentMap<Integer, AtomicInteger> getSequenceGeneratorMap() {
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

   private File getSpinedIntIntArrayMapDirectory(String spinedMapName) {
      final Path folderPath = LookupService.getService(ConfigurationService.class)
                                           .getDataStoreFolderPath()
                                           .resolve("object-chronicles")
                                           .resolve("bdb");
      File spinedMapDirectory = new File(folderPath.toFile(), spinedMapName);

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   private SpinedIntIntMap getAssemblageNid_ElementSequenceToNid_Map(int assemblageNid) {
      Database        mapDatabase = getNoDupDatabase("Assemblage" + assemblageNid);
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

   public static final String NID_ASSEMBLAGENID_MAP_KEY = "nid_AssemblageNid_Map";
   public static final String NID_ELEMENT_SEQUENCE_MAP_KEY = "nid_ElementSequence_Map";
   
   private SpinedNidIntMap getNidToAssemblageNidMap() {
      return getSpinedNidIntMap(NID_ASSEMBLAGENID_MAP_KEY);
   }
   
   private SpinedNidIntMap getNidToElementSequenceMap() {
      return getSpinedNidIntMap(NID_ELEMENT_SEQUENCE_MAP_KEY);
   }

   private SpinedNidIntMap getSpinedNidIntMap(String databaseKey) {
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

   private SpinedIntIntArrayMap getTaxonomyMap(int assemblageNid) {
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
   
   @Override
   public int[] getTaxonomyData(int assemblageNid, int conceptNid)
   {
      return getTaxonomyMap(assemblageNid).get(conceptNid);
   }

   @Override
   public int[] accumulateAndGetTaxonomyData(int assemblageId, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction)
   {
      return getTaxonomyMap(assemblageId).accumulateAndGet(conceptNid, newData, accumulatorFunction);
   }
   
   @Override
   public boolean hasChronologyData(int nid, IsaacObjectType expectedType) {
      int assemblageNid = ModelGet.identifierService().getAssemblageNid(nid).getAsInt();
//      if (expectedType != assemblageToObjectType_Map.get(assemblageNid)) {
//          return false;
//       }
      //TODO [KEC] this must validate the expectedType, with info that doesn't appear to be here - but maybe related to the "abandoned" method at the top?
      int elementSequence = getElementSequenceForNid(nid, assemblageNid);
      Database database = getChronologyDatabase(assemblageNid);

      try (Cursor cursor = database.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry key = new DatabaseEntry();

         IntegerBinding.intToEntry(elementSequence, key);

         DatabaseEntry value = new DatabaseEntry();
         OperationStatus status = cursor.getSearchKey(key, value, LockMode.DEFAULT);

         switch (status) {
            case KEYEMPTY:
            case KEYEXIST:
            case NOTFOUND:
               return false;

            case SUCCESS:
               return true;
         }
      }
      return false;
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
         pendingSync.release();
         syncSemaphore.acquireUninterruptibly();

         try {
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
            updateMessage("Writing databases...");
            databases.forEach(
                (key, database) -> {
                   LOG.info("Synchronizing: " + key);
                   database.sync();
                   LOG.info(key + " count: " + database.count());
                });
            completedUnitOfWork();
            updateMessage("Writing property database...");
            LOG.info("Syncronizing property database count: " + propertyDatabase.count());
            propertyDatabase.sync();
            completedUnitOfWork();
            updateMessage("Writing database environment...");
            myDbEnvironment.sync();
            completedUnitOfWork();
            writeListeners.forEach(listener -> listener.sync());
            updateMessage("Write complete");
            return null;
         } finally {
            syncSemaphore.release();
            Get.activeTasks()
               .remove(this);
         }
      }
   }

   @Override
   public int getAssemblageMemoryInUse(int assemblageNid) {
      // TODO [KEC] Auto-generated method stub
      return 0;
   }

   @Override
   public int getAssemblageSizeOnDisk(int assemblageNid) {
      // TODO [KEC] Auto-generated method stub
      return 0;
   }
   
   /** 
    * {@inheritDoc}
    */
   @Override
   public void registerDataWriteListener(DataWriteListener dataWriteListener) {
      writeListeners.add(dataWriteListener);
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public void unregisterDataWriteListener(DataWriteListener dataWriteListener) {
      writeListeners.remove(dataWriteListener);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getElementSequenceForNid(int nid) {
        int elementSequence = getNidToElementSequenceMap().get(nid);
        if (elementSequence != Integer.MAX_VALUE) {
           return elementSequence;
        }
        return getElementSequenceForNid(nid, ModelGet.identifierService().getAssemblageNid(nid).orElseThrow(() -> new RuntimeException("No assemblage nid available for " + nid 
              + " " + Get.identifierService().getUuidPrimordialForNid(nid))));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getElementSequenceForNid(int nid, int assemblageNid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be negative. Found: " + nid);
      }
      if (assemblageNid >= 0) {
         throw new IllegalStateException("assemblageNid must be negative. Found: " + assemblageNid);
      }

      int assemblageForNid = this.getNidToAssemblageNidMap().get(nid);
      if (assemblageForNid == Integer.MAX_VALUE)
      {
         this.getNidToAssemblageNidMap().put(nid, assemblageNid);
      }
      else if (assemblageNid != assemblageForNid)
      {
         throw new IllegalStateException("Assemblage nids do not match: \n" + Get.conceptDescriptionText(assemblageNid) + "(" + assemblageNid + ") and\n"
               + Get.conceptDescriptionText(assemblageForNid) + "(" + assemblageForNid + ")");
      }

      AtomicInteger sequenceGenerator = this.getSequenceGeneratorMap().computeIfAbsent(assemblageNid, (key) -> new AtomicInteger(1));
      int elementSequence = this.getNidToElementSequenceMap().getAndUpdate(nid, (currentValue) -> {
         if (currentValue == Integer.MAX_VALUE)
         {
            return sequenceGenerator.getAndIncrement();
         }
         return currentValue;
      });

      this.getAssemblageNid_ElementSequenceToNid_Map(assemblageNid).put(elementSequence, nid);
      return elementSequence;
   }
   
   @Override
   public int getNidForElementSequence(int assemblageNid, int sequence) {
      return getAssemblageNid_ElementSequenceToNid_Map(assemblageNid).get(sequence);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public OptionalInt getAssemblageOfNid(int nid) {
         int value = getNidToAssemblageNidMap().get(nid);
         if (value != Integer.MAX_VALUE) {
            return OptionalInt.of(value);
         }
         return OptionalInt.empty();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException {
         OptionalInt current = getAssemblageOfNid(nid);
         if (current.isPresent() && current.getAsInt() != assemblage) {
              throw new IllegalArgumentException("The nid " + nid + " is already assigned to assemblage " 
                      + current + " and cannot be reassigned to " + nid);
         }
         else {
            getNidToAssemblageNidMap().put(nid,  assemblage);
         }
   }
   
      public ConcurrentHashMap<Integer, VersionType> getAssemblageVersionTypeMap() {
         // TODO [KEC] Auto-generated method stub
         throw new UnsupportedOperationException();
      }

   /**
    * {@inheritDoc}
    */
   @Override
   public VersionType getVersionTypeForAssemblageNid(int assemblageNid) {
         VersionType result = getAssemblageVersionTypeMap().get(assemblageNid);
         if (result == null) {
            return VersionType.UNKNOWN;
         }
         return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException {
         VersionType oldValue = getAssemblageVersionTypeMap().computeIfAbsent(assemblageNid, (i -> type));
         if (oldValue != type) {
            throw new IllegalStateException("Tried to change the version type of " + assemblageNid + " from " + oldValue + " to " + type);
         }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IntStream getNidsForAssemblage(int assemblageNid) {
         return getAssemblageNid_ElementSequenceToNid_Map(assemblageNid).valueStream();
   }

   @Override
   public boolean implementsSequenceStore() {
      return true;
   }
}
