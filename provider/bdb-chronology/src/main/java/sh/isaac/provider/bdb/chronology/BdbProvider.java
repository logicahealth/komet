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

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Stream;

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
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.util.concurrent.atomic.AtomicInteger;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.DatabaseServices;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.provider.bdb.binding.AssemblageObjectTypeMapBinding;
import sh.isaac.provider.bdb.binding.IntArrayBinding;
import sh.isaac.provider.bdb.binding.IntSpineBinding;
import sh.isaac.provider.bdb.binding.SequenceGeneratorBinding;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class BdbProvider
         implements DatabaseServices, IdentifiedObjectService {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG             = LogManager.getLogger();
   private static final String ASSEMBLAGE_NIDS = "assemblageNids";
   private static final String MISC_MAP = "miscMap";
   private static final int SEQUENCE_GENERATOR_MAP_KEY = 1;
   private static final int ASSEMBLAGE_TYPE_MAP_KEY = 2;

   //~--- fields --------------------------------------------------------------

   private final ConcurrentHashMap<String, Database> databases          = new ConcurrentHashMap<>();
   private final DatabaseConfig                      chronologyDbConfig = new DatabaseConfig();
   private final DatabaseConfig                      noDupConfig        = new DatabaseConfig();
   private final ConcurrentSkipListSet<Integer>      assemblageNids     = new ConcurrentSkipListSet<>();

   /**
    * The database validity.
    */
   private DatabaseServices.DatabaseValidity databaseValidity = DatabaseServices.DatabaseValidity.NOT_SET;

   // TODO persist dataStoreId.
   private final UUID  dataStoreId = UUID.randomUUID();
   private Environment myDbEnvironment;
   private Database    identifierDatabase;
   private Database    propertyDatabase;

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

   @Override
   public void clearDatabaseValidityValue() {
      this.databaseValidity = DatabaseServices.DatabaseValidity.NOT_SET;
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

   public void writeChronologyData(ChronologyImpl chronology) {
      int             assemblageNid = chronology.getAssemblageNid();
      IsaacObjectType objectType    = chronology.getIsaacObjectType();

      ModelGet.identifierService()
              .setupNid(chronology.getNid(), assemblageNid, objectType);
      assemblageNids.add(assemblageNid);

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

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting BDB provider post-construct");

      try {
         EnvironmentConfig envConfig = new EnvironmentConfig();
         final Path folderPath = LookupService.getService(ConfigurationService.class)
                                              .getChronicleFolderPath()
                                              .resolve("bdb");

         envConfig.setAllowCreate(true);

         File dbEnv = folderPath.toFile();

         if (!dbEnv.exists()) {
            this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }

         dbEnv.mkdirs();
         myDbEnvironment    = new Environment(dbEnv, envConfig);
         propertyDatabase   = myDbEnvironment.openDatabase(null, "property", noDupConfig);
         identifierDatabase = myDbEnvironment.openDatabase(null, "identifier", noDupConfig);
         LOG.info("identifier count at open: " + identifierDatabase.count());

         NidSet assemblageNidsSet = getNidSet(ASSEMBLAGE_NIDS);

         for (int nid: assemblageNidsSet.asArray()) {
            assemblageNids.add(nid);
         }
         
         LOG.info("Off heap cache size: " + myDbEnvironment.getConfig().getOffHeapCacheSize());
         LOG.info("Max disk: " + myDbEnvironment.getConfig().getMaxDisk());
         LOG.info("MAX_MEMORY: " + myDbEnvironment.getConfig().getConfigParam(EnvironmentConfig.MAX_MEMORY));
         LOG.info("MAX_MEMORY_PERCENT: " + myDbEnvironment.getConfig().getConfigParam(EnvironmentConfig.MAX_MEMORY_PERCENT));
         LOG.info("MAX_OFF_HEAP_MEMORY: " + myDbEnvironment.getConfig().getConfigParam(EnvironmentConfig.MAX_OFF_HEAP_MEMORY));
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
            sync();
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
            myDbEnvironment.close();
         }
      } catch (Throwable ex) {
         LOG.error(ex);
         throw ex;
      }
   }

   public void sync() {
      NidSet assemblageNidSet = NidSet.of(assemblageNids);
      putNidSet(ASSEMBLAGE_NIDS, assemblageNidSet);
      databases.forEach(
              (key, database) -> {
                 LOG.info("Syncronizing: " + key + " count: " + database.count());
                 database.sync();
              });
      LOG.info("Syncronizing identifier database count: " + identifierDatabase.count());
      identifierDatabase.sync();
      LOG.info("Syncronizing property database count: " + propertyDatabase.count());
      propertyDatabase.sync();
      myDbEnvironment.sync();
   }

   //~--- get methods ---------------------------------------------------------

   public ConcurrentSkipListSet<Integer> getAssemblageNids() {
      return assemblageNids;
   }

   public Optional<ByteArrayDataBuffer> getChronologyData(int nid)
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
   public DatabaseValidity getDatabaseValidityStatus() {
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

   public SpinedIntIntMap getSpinedIntIntMap(String databaseKey) {
      Database        mapDatabase = getNoDupDatabase(databaseKey);
      SpinedIntIntMap map         = new SpinedIntIntMap();

      try (Cursor cursor = mapDatabase.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry foundKey  = new DatabaseEntry();
         DatabaseEntry foundData = new DatabaseEntry();
         IntSpineBinding  spineBinding = new IntSpineBinding();

         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int spineKey = IntegerBinding.entryToInt(foundKey);
            AtomicIntegerArray spineData = spineBinding.entryToObject(foundData);
            map.getSpines().put(spineKey, spineData);
         }
      }

      return map;
   }

   public SpinedNidIntMap getSpinedNidIntMap(String databaseKey) {
      Database        mapDatabase = getNoDupDatabase(databaseKey);
      SpinedNidIntMap map         = new SpinedNidIntMap();

      try (Cursor cursor = mapDatabase.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry foundKey  = new DatabaseEntry();
         DatabaseEntry foundData = new DatabaseEntry();
         IntSpineBinding  spineBinding = new IntSpineBinding();

         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int spineKey = IntegerBinding.entryToInt(foundKey);
            AtomicIntegerArray spineData = spineBinding.entryToObject(foundData);
            map.getSpines().put(spineKey, spineData);
         }
      }

      return map;
   }

   public static Stream<? extends Chronology> getStream() {
      throw new UnsupportedOperationException();
   }

   public Database getTaxonomyDatabase(int assemblageNid) {
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

   public void putSequenceGeneratorMap(ConcurrentMap<Integer, AtomicInteger> assemblageNid_SequenceGenerator_Map) {
      SequenceGeneratorBinding binding = new SequenceGeneratorBinding();
      Database database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry key = new DatabaseEntry();
      IntegerBinding.intToEntry(SEQUENCE_GENERATOR_MAP_KEY, key);
      DatabaseEntry   value  = new DatabaseEntry();
      binding.objectToEntry(assemblageNid_SequenceGenerator_Map, value);
      database.put(null, key, value);
   }
   
   public ConcurrentMap<Integer, AtomicInteger> getSequenceGeneratorMap() {
      Database database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry key = new DatabaseEntry();
      IntegerBinding.intToEntry(SEQUENCE_GENERATOR_MAP_KEY, key);
      DatabaseEntry   value  = new DatabaseEntry();
      SequenceGeneratorBinding binding = new SequenceGeneratorBinding();
      if (database.get(null, key, value, null) == OperationStatus.SUCCESS) {
         return binding.entryToObject(value);
      }
      return new ConcurrentHashMap<>();
   }
   
   public ConcurrentHashMap<Integer, IsaacObjectType> getAssemblageTypeMap() {
      Database database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry key = new DatabaseEntry();
      IntegerBinding.intToEntry(ASSEMBLAGE_TYPE_MAP_KEY, key);
      DatabaseEntry   value  = new DatabaseEntry();
      AssemblageObjectTypeMapBinding binding = new AssemblageObjectTypeMapBinding();
      if (database.get(null, key, value, null) == OperationStatus.SUCCESS) {
         return binding.entryToObject(value);
      }
      return new ConcurrentHashMap<>();
   }
   public void putAssemblageTypeMap(ConcurrentHashMap<Integer, IsaacObjectType> map) {
      AssemblageObjectTypeMapBinding binding = new AssemblageObjectTypeMapBinding();
      Database database = getNoDupDatabase(MISC_MAP);
      DatabaseEntry key = new DatabaseEntry();
      IntegerBinding.intToEntry(ASSEMBLAGE_TYPE_MAP_KEY, key);
      DatabaseEntry   value  = new DatabaseEntry();
      binding.objectToEntry(map, value);
      OperationStatus result = database.put(null, key, value);
      LOG.info("Put assemblage type map with result of: " + result);
   }
}

