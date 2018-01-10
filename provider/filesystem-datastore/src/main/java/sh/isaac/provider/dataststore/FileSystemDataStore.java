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
package sh.isaac.provider.dataststore;

//~--- JDK imports ------------------------------------------------------------
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.nio.file.Path;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.DatabaseServices;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStore;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedByteArrayArrayMap;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.collections.SpinedNidNidSetMap;
import sh.isaac.model.semantic.SemanticChronologyImpl;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class FileSystemDataStore
        implements DataStore {

   private static final Logger LOG = LogManager.getLogger();
   private static final String DB_UUID_KEY = "FileSystemDataStore.uuid";

   //~--- fields --------------------------------------------------------------
   ConcurrentMap<Integer, AtomicInteger> assemblageNid_SequenceGenerator_Map
           = new ConcurrentHashMap<>();
   private final Properties properties = new Properties();
   private final ConcurrentHashMap<Integer, SpinedIntIntMap> assemblage_ElementToNid_Map
           = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer, SpinedByteArrayArrayMap> spinedChronologyMapMap
           = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer, SpinedIntIntArrayMap> spinedTaxonomyMapMap
           = new ConcurrentHashMap<>();
   private final SpinedNidNidSetMap componentToSemanticNidsMap = new SpinedNidNidSetMap();
   private final ConcurrentHashMap<Integer, IsaacObjectType> assemblageToObjectType_Map
           = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer, VersionType> assemblageToVersionType_Map
           = new ConcurrentHashMap<>();
   private DatabaseServices.DatabaseValidity databaseValidity = DatabaseServices.DatabaseValidity.NOT_SET;
   private SyncTask lastSyncTask = null;
   private Future<?> lastSyncFuture = null;
   private final Semaphore syncSemaphore = new Semaphore(1);
   private final Semaphore pendingSync = new Semaphore(1);
   private final SpinedNidIntMap nidToAssemblageNidMap = new SpinedNidIntMap();
   private final SpinedNidIntMap nidToElementSequenceMap = new SpinedNidIntMap();
   private File isaacDbDirectory;
   private File componentToSemanticMapDirectory;
   private File assemblageNid_ElementSequenceToNid_MapDirectory;
   private File taxonomyMapDirectory;
   private File chronologySpinesDirectory;
   private File assemblageToObjectTypeFile;
   private File assemblageToVersionTypeFile;
   private File sequenceGeneratorMapFile;
   private File propertiesFile;
   private File nidToAssemblageNidMapDirectory;
   private File nidToElementSequenceMapDirectory;

   //~--- methods -------------------------------------------------------------
   @Override
   public void putChronologyData(ChronologyImpl chronology) {
       try {
           int nid = chronology.getNid();
           
           int assemblageNid = chronology.getAssemblageNid();
           if (ModelGet.identifierService()
                   .getAssemblageNidForNid(nid) == Integer.MAX_VALUE) {
               ModelGet.identifierService()
                       .setupNid(chronology.getNid(), assemblageNid, chronology.getIsaacObjectType(),
                               chronology.getVersionType());
               
               if (chronology instanceof SemanticChronologyImpl) {
                   SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) chronology;
                   int referencedComponentNid = semanticChronology.getReferencedComponentNid();
                   
                   componentToSemanticNidsMap.add(referencedComponentNid, semanticChronology.getNid());
               }
           } else if (ModelGet.identifierService()
                   .getAssemblageNidForNid(nid) != assemblageNid) {
               throw new IllegalStateException("Assemblage identifiers do not match: " + ModelGet.identifierService()
                   .getAssemblageNidForNid(nid) + " \n" + chronology);
           }
           
           SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid);
           int elementSequence = ModelGet.identifierService()
                   .getElementSequenceForNid(chronology.getNid(), assemblageNid);
           
           spinedByteArrayArrayMap.put(elementSequence, chronology.getDataList());
           
       } catch (Throwable e) {
           e.printStackTrace();
           throw e;
       }
   }

   @Override
   public Future<?> sync() {
      if (pendingSync.tryAcquire()) {
         lastSyncTask = new SyncTask();
         lastSyncFuture = Get.executor()
                 .submit(lastSyncTask);
         return lastSyncFuture;
      }

      return lastSyncFuture;
   }

   private void readAssemblageToObjectTypeFile()
           throws IOException {
      if (assemblageToObjectTypeFile.exists()) {
         try (DataInputStream dis = new DataInputStream(
                 new BufferedInputStream(new FileInputStream(assemblageToObjectTypeFile)))) {
            int mapSize = dis.readInt();

            for (int i = 0; i < mapSize; i++) {
               assemblageToObjectType_Map.put(dis.readInt(), IsaacObjectType.fromToken(dis.readByte()));
            }
         }
      }
   }

   private void readAssemblageToVersionTypeFile()
           throws IOException {
      if (assemblageToVersionTypeFile.exists()) {
         try (DataInputStream dis = new DataInputStream(
                 new BufferedInputStream(new FileInputStream(assemblageToVersionTypeFile)))) {
            int mapSize = dis.readInt();

            for (int i = 0; i < mapSize; i++) {
               assemblageToVersionType_Map.put(dis.readInt(), VersionType.getFromToken(dis.readByte()));
            }
         }
      }
   }

   private void readSequenceGeneratorMapFile()
           throws IOException {
      if (sequenceGeneratorMapFile.exists()) {
         try (DataInputStream dis = new DataInputStream(
                 new BufferedInputStream(new FileInputStream(sequenceGeneratorMapFile)))) {
            int mapSize = dis.readInt();

            for (int i = 0; i < mapSize; i++) {
               assemblageNid_SequenceGenerator_Map.put(dis.readInt(), new AtomicInteger(dis.readInt()));
            }
         }
      }
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Startings FileSystemDataStore.");

         ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
         Optional<Path> dataStorePath = configurationService.getDataStoreFolderPath();

         if (!dataStorePath.isPresent()) {
            throw new IllegalStateException("dataStorePath is not set");
         }

         Path folderPath = dataStorePath.get();

         this.isaacDbDirectory = folderPath.toFile();
         this.chronologySpinesDirectory = new File(isaacDbDirectory, "chronologies");
         this.propertiesFile = new File(isaacDbDirectory, "properties.txt");
         this.componentToSemanticMapDirectory = new File(isaacDbDirectory, "componentToSemanticMap");
         this.assemblageNid_ElementSequenceToNid_MapDirectory = new File(isaacDbDirectory, "assemblageToSequenceMap");
         this.taxonomyMapDirectory = new File(isaacDbDirectory, "taxonomyMap");
         this.assemblageToObjectTypeFile = new File(isaacDbDirectory, "assemblageToTypeMap");
         this.assemblageToVersionTypeFile = new File(isaacDbDirectory, "assemblageToVersionTypeMap");
         this.nidToAssemblageNidMapDirectory = new File(isaacDbDirectory, "componentToAssemblageMap");
         this.sequenceGeneratorMapFile = new File(isaacDbDirectory, "sequenceGeneratorMap");
         this.nidToElementSequenceMapDirectory = new File(isaacDbDirectory, "componentToAssemblageElementMap");

         if (isaacDbDirectory.exists()) {
            if (this.propertiesFile.exists()) {
               try (Reader reader = new FileReader(propertiesFile)) {
                  this.properties.load(reader);
               }
               if (this.properties.getProperty(DB_UUID_KEY) == null) {
                   this.properties.setProperty(DB_UUID_KEY, UUID.randomUUID()
                       .toString());
               }

               this.databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
            } else {
               this.properties.setProperty(DB_UUID_KEY, UUID.randomUUID()
                       .toString());
               this.componentToSemanticMapDirectory.mkdirs();
               this.databaseValidity = DatabaseValidity.EMPTY_DIRECTORY;
            }
         } else {
            this.properties.setProperty(DB_UUID_KEY, UUID.randomUUID()
                    .toString());
            this.isaacDbDirectory.mkdirs();
            this.componentToSemanticMapDirectory.mkdirs();
            this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }

         readAssemblageToObjectTypeFile();
         readAssemblageToVersionTypeFile();
         readSequenceGeneratorMapFile();
         componentToSemanticNidsMap.read(this.componentToSemanticMapDirectory);

         // spinedChronologyMapMap is lazily loaded
         // spinedTaxonomyMapMap is lazily loaded
         if (nidToAssemblageNidMapDirectory.exists()) {
            nidToAssemblageNidMap.read(nidToAssemblageNidMapDirectory);
         }

         if (nidToElementSequenceMapDirectory.exists()) {
            nidToElementSequenceMap.read(nidToElementSequenceMapDirectory);
         }

         // assemblage_ElementToNid_Map is lazily loaded
      } catch (IOException ex) {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }

   @PreDestroy
   private void stopMe() {
      try {
         LOG.info("Stopping FileSystemDataStore.");

         // The IO non-blocking executor - set core threads equal to max - otherwise, it will never increase the thread count
         // with an unbounded queue.
         ThreadPoolExecutor executor = new ThreadPoolExecutor(
                 4,
                 4,
                 60,
                 TimeUnit.SECONDS,
                 new LinkedBlockingQueue<>(),
                 new NamedThreadFactory("IODataStore-Shutdown-work-thread", true));

         executor.allowCoreThreadTimeOut(true);

         Task<Void> syncTask = new SyncTask();

         pendingSync.acquire();
         executor.submit(syncTask)
                 .get();
      } catch (InterruptedException | ExecutionException ex) {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }

   private void writeAssemblageToObjectTypeFile()
           throws IOException {
      try (DataOutputStream dos = new DataOutputStream(
              new BufferedOutputStream(new FileOutputStream(assemblageToObjectTypeFile)))) {
         dos.writeInt(assemblageToObjectType_Map.size());

         for (Map.Entry<Integer, IsaacObjectType> entry : assemblageToObjectType_Map.entrySet()) {
            dos.writeInt(entry.getKey());
            dos.writeByte(entry.getValue()
                    .getToken());
         }
      }
   }
   private void writeAssemblageToVersionTypeFile()
           throws IOException {
      try (DataOutputStream dos = new DataOutputStream(
              new BufferedOutputStream(new FileOutputStream(assemblageToVersionTypeFile)))) {
         dos.writeInt(assemblageToVersionType_Map.size());

         for (Map.Entry<Integer, VersionType> entry : assemblageToVersionType_Map.entrySet()) {
            dos.writeInt(entry.getKey());
            dos.writeByte(entry.getValue().getVersionTypeToken());
         }
      }
   }


   private void writeSequenceGeneratorMapFile()
           throws IOException {
      try (DataOutputStream dos = new DataOutputStream(
              new BufferedOutputStream(new FileOutputStream(sequenceGeneratorMapFile)))) {
         dos.writeInt(assemblageNid_SequenceGenerator_Map.size());

         for (Map.Entry<Integer, AtomicInteger> entry : assemblageNid_SequenceGenerator_Map.entrySet()) {
            dos.writeInt(entry.getKey());
            dos.writeInt(entry.getValue()
                    .get());
         }
      }
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public int[] getAssemblageConceptNids() {
      int[] assemblageConceptNids = new int[assemblageToObjectType_Map.size()];
      int i = 0;

      for (Integer assemblageConceptNid : assemblageToObjectType_Map.keySet()) {
         assemblageConceptNids[i++] = assemblageConceptNid;
      }

      return assemblageConceptNids;
   }

   @Override
   public SpinedIntIntMap getAssemblageNid_ElementSequenceToNid_Map(int assemblageNid) {
      if (assemblageNid >= 0) {
         throw new IllegalStateException("assemblageNid must be negative. Found: " + assemblageNid);
      }

      return this.assemblage_ElementToNid_Map.computeIfAbsent(
              assemblageNid,
              (key) -> {
                 SpinedIntIntMap map = new SpinedIntIntMap();
                 File directory = getSpineDirectory(
                         assemblageNid_ElementSequenceToNid_MapDirectory,
                         assemblageNid);

                 if (directory.exists()) {
                    int filesRead = map.read(directory);
                    if (filesRead > 0) {
                        LOG.info("Read  " + filesRead + " element to nid files for assemblage: " 
                                + " " + properties.getProperty(Integer.toString(assemblageNid)) 
                                + assemblageNid + " " + Integer.toUnsignedString(assemblageNid));
                    }
                 }

                 return map;
              });
   }

   @Override
   public ConcurrentHashMap<Integer, IsaacObjectType> getAssemblageObjectTypeMap() {
      return assemblageToObjectType_Map;
   }

    @Override
    public ConcurrentHashMap<Integer, VersionType> getAssemblageVersionTypeMap() {
        return assemblageToVersionType_Map;
    }

   @Override
   public Optional<ByteArrayDataBuffer> getChronologyData(int nid) {
      OptionalInt assemblageNidOptional = ModelGet.identifierService()
              .getAssemblageNid(nid);

      if (assemblageNidOptional.isPresent()) {
         int assemblageNid = assemblageNidOptional.getAsInt();
         int elementSequence = ModelGet.identifierService()
                 .getElementSequenceForNid(nid, assemblageNid);
         SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid);
         byte[][] data = spinedByteArrayArrayMap.get(elementSequence);

         if (data == null) {
            return Optional.empty();
         }

         int size = 0;

         for (byte[] dataEntry : data) {
            size = size + dataEntry.length;
         }

         ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(
                 size + 4);  // room for 0 int value at end to indicate last version

         for (byte[] dataEntry : data) {
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
   
   @Override
   public int getAssemblageMemoryInUse(int assemblageNid) {
       return getChronologySpinedMap(assemblageNid).memoryInUse();
   }

    @Override
    public int getAssemblageSizeOnDisk(int assemblageNid) {
        return getChronologySpinedMap(assemblageNid).sizeOnDisk();
    }

   private SpinedByteArrayArrayMap getChronologySpinedMap(int assemblageNid) {
      SpinedByteArrayArrayMap spinedMap = spinedChronologyMapMap.computeIfAbsent(
              assemblageNid,
              (dbKey) -> {
                 SpinedByteArrayArrayMap spinedByteArrayArrayMap = new SpinedByteArrayArrayMap();
                 File spineDirectory = getSpineDirectory(chronologySpinesDirectory, assemblageNid);

                 if (spineDirectory.exists()) {
                    int filesToRead = spinedByteArrayArrayMap.lazyRead(spineDirectory);
                    if (filesToRead > 0) {
                        LOG.info("Lazy open of " + filesToRead + 
                                " chronology files for assemblage: "  
                                + " " + properties.getProperty(Integer.toString(assemblageNid))
                                + assemblageNid + " " + Integer.toUnsignedString(assemblageNid));
                    }
                 }
                 return spinedByteArrayArrayMap;
              });

      return spinedMap;
   }

   @Override
   public SpinedNidNidSetMap getComponentToSemanticNidsMap() {
      return componentToSemanticNidsMap;
   }

   @Override
   public UUID getDataStoreId() {
      String uuidString = this.properties.getProperty(DB_UUID_KEY);

      return UUID.fromString(uuidString);
   }

   @Override
   public Path getDatabaseFolder() {
      return this.isaacDbDirectory.toPath();
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.databaseValidity;
   }

   @Override
   public SpinedNidIntMap getNidToAssemblageNidMap() {
      return nidToAssemblageNidMap;
   }

   @Override
   public SpinedNidIntMap getNidToElementSequenceMap() {
      return nidToElementSequenceMap;
   }

   @Override
   public ConcurrentMap<Integer, AtomicInteger> getSequenceGeneratorMap() {
      return assemblageNid_SequenceGenerator_Map;
   }

   private File getSpineDirectory(File parentDirectory, int assemblageNid) {
      File spinedMapDirectory = new File(parentDirectory, Integer.toUnsignedString(assemblageNid));

      spinedMapDirectory.mkdirs();
      return spinedMapDirectory;
   }

   @Override
   public SpinedIntIntArrayMap getTaxonomyMap(int assemblageNid) {
      SpinedIntIntArrayMap spinedMap = spinedTaxonomyMapMap.computeIfAbsent(
              assemblageNid,
              (dbKey) -> {
                 SpinedIntIntArrayMap spinedIntIntArrayMap = new SpinedIntIntArrayMap();
                 File spineDirectory = getSpineDirectory(taxonomyMapDirectory, assemblageNid);

                 if (spineDirectory.exists()) {
                    int filesRead = spinedIntIntArrayMap.read(spineDirectory);
                    if (filesRead > 0) {
                        LOG.info("Read  " + filesRead + " taxonomy files for assemblage: " 
                                + " " + properties.getProperty(Integer.toString(assemblageNid))
                                + assemblageNid + " " + Integer.toUnsignedString(assemblageNid));
                    }
                 }
                 
                 return spinedIntIntArrayMap;
              });

      return spinedMap;
   }

   //~--- inner classes -------------------------------------------------------
   private class SyncTask
           extends TimedTaskWithProgressTracker<Void> {

      public SyncTask() {
         updateTitle("Writing data to disk");
         addToTotalWork(9);  // TODO figure out total amoutn of work...
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
            updateMessage("Writing sequence generator map...");
            writeSequenceGeneratorMapFile();


            completedUnitOfWork();  // 1
            updateMessage("Writing assemblage nids...");
            writeAssemblageToObjectTypeFile();
            writeAssemblageToVersionTypeFile();
            completedUnitOfWork();  // 2
            updateMessage("Writing component to semantics map...");

            if (componentToSemanticNidsMap.write(componentToSemanticMapDirectory)) {
               LOG.info("Synchronized component to semantics map changes.");
            }

            completedUnitOfWork();  // 3
            updateMessage("Writing chronology spines...");
            spinedChronologyMapMap.forEach(
                    (assemblageNid, spinedMap) -> {
                       File directory = getSpineDirectory(chronologySpinesDirectory, assemblageNid);

                       addInfoFile(directory, assemblageNid);

                       if (spinedMap.write(directory)) {
                          String assemblageDescription = properties.getProperty(Integer.toUnsignedString(assemblageNid));
                          LOG.info("Syncronized chronologies: " + assemblageNid
                          + " " + assemblageDescription);
                       }
                    });
            completedUnitOfWork();  // 4
            updateMessage("Writing taxonomy spines...");
            spinedTaxonomyMapMap.forEach(
                    (assemblageNid, spinedMap) -> {
                       File directory = getSpineDirectory(taxonomyMapDirectory, assemblageNid);

                       addInfoFile(directory, assemblageNid);

                       if (spinedMap.write(directory)) {
                          String assemblageDescription = properties.getProperty(Integer.toUnsignedString(assemblageNid));
                          LOG.info("Syncronizing taxonomies: " + assemblageNid
                          + " " + assemblageDescription);
                       }
                    });
            completedUnitOfWork();  // 5
            updateMessage("Writing component to assemblage map...");
            nidToAssemblageNidMap.write(nidToAssemblageNidMapDirectory);
            completedUnitOfWork();  // 6
            updateMessage("Writing component to assemblage element map...");
            nidToElementSequenceMap.write(nidToElementSequenceMapDirectory);
            completedUnitOfWork();  // 7

            // assemblage_ElementToNid_Map
            updateMessage("Writing assemblage element to component map...");

            for (Map.Entry<Integer, SpinedIntIntMap> entry : assemblage_ElementToNid_Map.entrySet()) {
               File directory = getSpineDirectory(assemblageNid_ElementSequenceToNid_MapDirectory, entry.getKey());

               addInfoFile(directory, entry.getKey());
               entry.getValue()
                       .write(directory);
            }

            completedUnitOfWork();  // 8
            updateMessage("Writing properties...");

            try (FileWriter writer = new FileWriter(propertiesFile)) {
               FileSystemDataStore.this.properties.store(writer, null);
            }
            completedUnitOfWork();  // 9
            updateMessage("Write complete");
            return null;
         } finally {
            syncSemaphore.release();
            Get.activeTasks()
                    .remove(this);
         }
      }

      private void addInfoFile(File directory, Integer assemblageNid) {
         if (LookupService.getCurrentRunLevel() >= LookupService.ISAAC_STARTED_RUNLEVEL) {
            File parentDirectory = directory.getParentFile();
            File[] filesWithPrefix = parentDirectory.listFiles((dir, name) -> name.startsWith(directory.getName()));

            if (filesWithPrefix.length < 2) {
               try {
                  Optional<String> descriptionOptional = Get.concept(assemblageNid)
                          .getPreferedConceptDescriptionText();

                  if (descriptionOptional.isPresent()) {
                     String description = descriptionOptional.get();
                     if (!description.startsWith("No description")) {
                        properties.put(Integer.toString(assemblageNid), description);
                        properties.put(Integer.toUnsignedString(assemblageNid), description);
                        description = description.replace('/', '|');
                         
                        File descriptionFile = new File(
                                parentDirectory,
                                Integer.toUnsignedString(
                                        assemblageNid) + "-" + description);

                        try {
                           descriptionFile.getParentFile().mkdirs();
                           descriptionFile.createNewFile();
                        } catch (IOException ex) {
                           LOG.error(ex);
                        }
                     }
                  }
               } catch (Throwable e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }
}
