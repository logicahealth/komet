/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.bdb;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.DatabaseServices;
import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.ChronologyImpl;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class BdbProvider implements DatabaseServices {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   static boolean hasKey(Database database, int keyId) {
      DatabaseEntry key = new DatabaseEntry();
      IntegerBinding.intToEntry(keyId, key);
      return getChronologyData(database, keyId).isPresent();
  }
   /**
    * The database validity.
    */
   private DatabaseServices.DatabaseValidity databaseValidity = DatabaseServices.DatabaseValidity.NOT_SET;

   private Environment myDbEnvironment;
   private Database    conceptDatabase;
   
   // TODO persist dataStoreId. 
   private final UUID dataStoreId = UUID.randomUUID();

   public Database getConceptDatabase() {
      return conceptDatabase;
   }

   public Database getSemanticDatabase() {
      return semanticDatabase;
   }
   private Database    semanticDatabase;
   
   private static BdbProvider singleton;
   
   public static BdbProvider get() {
      if (singleton == null) {
         
      }
      return singleton;
   }
   
   @Override
   public UUID getDataStoreId() {
      return dataStoreId;
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting BDB provider post-construct");

      try {
         EnvironmentConfig envConfig = new EnvironmentConfig();

         envConfig.setAllowCreate(true);
         File dbEnv = new File(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY));
         if (!dbEnv.exists()) {
            this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }
         myDbEnvironment = new Environment(dbEnv, envConfig);

         // Open the database. Create it if it does not already exist.
         DatabaseConfig dbConfig = new DatabaseConfig();

         dbConfig.setAllowCreate(true);
         dbConfig.setDeferredWrite(true);
         dbConfig.setSortedDuplicates(true);
         conceptDatabase = myDbEnvironment.openDatabase(null, "concepts", dbConfig);
         semanticDatabase = myDbEnvironment.openDatabase(null, "semantics", dbConfig);
      } catch (DatabaseException dbe) {
         throw new RuntimeException(dbe);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping BDB ConceptProvider.");

      if (myDbEnvironment != null) {
         conceptDatabase.sync();
         conceptDatabase.close();
         semanticDatabase.sync();
         semanticDatabase.close();
         myDbEnvironment.close();
      }
   }

   @Override
   public void clearDatabaseValidityValue() {
      this.databaseValidity = DatabaseServices.DatabaseValidity.NOT_SET;
   }

   @Override
   public Path getDatabaseFolder() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.databaseValidity;
   }
   
   public static void writeChronologyData(Database database, ChronologyImpl chronology) {
      DatabaseEntry key = new DatabaseEntry();

      IntegerBinding.intToEntry(chronology.getContainerSequence(), key);

      List<byte[]> dataList = chronology.getDataList();

      for (byte[] data: dataList) {
         DatabaseEntry value = new DatabaseEntry(data);

         OperationStatus status = database.put(null, key, value);
         if (status != OperationStatus.SUCCESS) {
            throw new RuntimeException("Operation failed: " + status);
         }
      }
      
   }

   public static Optional<ByteArrayDataBuffer> getChronologyData(Database database, int sequenceId) throws IllegalStateException {
      ArrayList<byte[]> dataList = new ArrayList<>();
      ByteArrayDataBuffer byteBuffer = null;
      try (Cursor cursor = database.openCursor(null, CursorConfig.READ_UNCOMMITTED)) {
         DatabaseEntry key = new DatabaseEntry();
         IntegerBinding.intToEntry(sequenceId, key);
         DatabaseEntry value = new DatabaseEntry();
         OperationStatus status = cursor.getSearchKey(key, value, LockMode.DEFAULT);
         switch (status) {
            case KEYEMPTY:
            case KEYEXIST:
            case NOTFOUND:
               return Optional.empty();
               
            case SUCCESS:
               int size = 0;
               byte[] data = value.getData();
               size = data.length;
               dataList.add(data);
               
               while (cursor.getNextDup(key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                  data = value.getData();
                  size = size + data.length;
                  if (data[0] == 0 &&
                          data[1] == 0 &&
                          data[2] == 0 &&
                          data[3] == 0) {
                     dataList.add(0, data);
                  } else {
                     dataList.add(value.getData());
                  }
               }
               
               byteBuffer = new ByteArrayDataBuffer(size + 4); // room for 0 int value at end to indicate last version
               for (byte[] dataEntry: dataList) {
                  byteBuffer.put(dataEntry);
               }
               byteBuffer.putInt(0);
               byteBuffer.rewind();
               if (byteBuffer.getInt() != 0) {
                  throw new IllegalStateException("Record does not start with zero...");
               }
         }
      }
      
      return Optional.of(byteBuffer);
   }
   
}
