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
package sh.isaac.provider.datastore;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import javafx.concurrent.Task;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.SequenceStore;
import sh.isaac.model.collections.SpinedByteArrayArrayMap;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.collections.SpinedNidNidSetMap;
import sh.isaac.model.semantic.SemanticChronologyImpl;

//~--- classes ----------------------------------------------------------------
/**
 * TODO: evaluate how the canceling of changes will impact the array approach
 * for writing versions...
 *
 * @author kec
 */

/** Align this with {@link DatabaseImplementation#FILESYSTEM} */
@Service (name="FILESYSTEM")
@Singleton
@Rank(value=-8)
public class FileSystemDataStore
        implements DataStoreSubService, SequenceStore  {

    private static final Logger LOG = LogManager.getLogger();
    private Optional<UUID> dataStoreId = Optional.empty();

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
    private DataStoreStartState datastoreStartState = DataStoreStartState.NOT_YET_CHECKED;
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
    
    private ArrayList<DataWriteListener> writeListeners = new ArrayList<>();

    private FileSystemDataStore() {
        //Private for HK2 construction only
    }

    //~--- methods -------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public void putChronologyData(ChronologyImpl chronology) {
        try {
            int assemblageNid = chronology.getAssemblageNid();
 
            if (chronology instanceof SemanticChronologyImpl) {
                SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) chronology;
                int referencedComponentNid = semanticChronology.getReferencedComponentNid();

                //We could optionally check and see if this chronology is already listed for this nid, but its likely cheaper to just let it merge internally
                componentToSemanticNidsMap.add(referencedComponentNid, semanticChronology.getNid());
              }

            SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid);
            int elementSequence = getElementSequenceForNid(chronology.getNid(), assemblageNid);

            spinedByteArrayArrayMap.put(elementSequence, getDataList(chronology));
            
            for (DataWriteListener dwl : writeListeners) {
               dwl.writeData(chronology);
            }

        } catch (Throwable e) {
            LOG.error("Unexpected error putting chronology data!", e);
            throw e;
        }
    }

    /**
     * Get the data as a list of immutable byte arrays. With an append only data
     * model, these records are safe for concurrent writes without destroying
     * data per the duplicate data model in Berkley DB and Xodus.
     *
     * The chronology record starts with an integer of 0 to differentiate from
     * version records, and then is followed by a byte for the object type, and
     * a byte for the data format version... The object type byte is always > 0,
     * and the version byte is always > 0...
     *
     * Each byte[] for a version starts with an integer length of the version
     * data. The minimum size of a version is 4 bytes (an integer stamp
     * sequence).
     *
     * @param chronology the chronology to turn into a byte[] list...
     * @return a byte[] list
     */
    private List<byte[]> getDataList(ChronologyImpl chronology) {

        List<byte[]> dataArray = new ArrayList<>();

        byte[] dataToSplit = chronology.getDataToWrite();
        int versionStartPosition = chronology.getVersionStartPosition();
        if (versionStartPosition < 0) {
            throw new IllegalStateException("versionStartPosition is not set");
        }
        byte[] chronicleBytes = new byte[versionStartPosition + 4]; // +4 for the zero integer to start.
        for (int i = 0; i < chronicleBytes.length; i++) {
            if (i < 4) {
                chronicleBytes[i] = 0;
            } else {
                chronicleBytes[i] = dataToSplit[i - 4];
            }
        }
        dataArray.add(chronicleBytes);

        int versionStart = versionStartPosition;
        int versionSize = (((dataToSplit[versionStart]) << 24) | ((dataToSplit[versionStart + 1] & 0xff) << 16)
                | ((dataToSplit[versionStart + 2] & 0xff) << 8) | ((dataToSplit[versionStart + 3] & 0xff)));

        while (versionSize != 0) {
            int versionTo = versionStart + versionSize;
            int newLength = versionTo - versionStart;
            if (versionTo < 0) {
                LOG.error("Error versionTo: " + versionTo);
            }
            if (newLength < 0) {
                LOG.error("Error newLength: " + newLength);
            }
            dataArray.add(Arrays.copyOfRange(dataToSplit, versionStart, versionTo));
            versionStart = versionStart + versionSize;
            versionSize = (((dataToSplit[versionStart]) << 24) | ((dataToSplit[versionStart + 1] & 0xff) << 16)
                    | ((dataToSplit[versionStart + 2] & 0xff) << 8) | ((dataToSplit[versionStart + 3] & 0xff)));
        }

        return dataArray;
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
     * {@inheritDoc}
     */
    @Override
    public void startup() {
        try {
            LOG.info("Startings FileSystemDataStore");

            ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
            Path folderPath = configurationService.getDataStoreFolderPath();

            this.assemblageNid_SequenceGenerator_Map.clear();
            this.properties.clear();
            this.assemblage_ElementToNid_Map.clear();
            this.spinedChronologyMapMap.clear();
            this.spinedTaxonomyMapMap.clear();
            this.componentToSemanticNidsMap.clear();
            this.assemblageToObjectType_Map.clear();
            this.assemblageToVersionType_Map.clear();

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

            if (isaacDbDirectory.exists() && this.propertiesFile.isFile()) {
                try (Reader reader = new FileReader(propertiesFile)) {
                    this.properties.load(reader);
                }
                this.datastoreStartState = DataStoreStartState.EXISTING_DATASTORE;
            } else {
                this.isaacDbDirectory.mkdirs();
                this.componentToSemanticMapDirectory.mkdirs();
                this.datastoreStartState = DataStoreStartState.NO_DATASTORE;
            }

            //If the DBID is missing, we better be in NO_DATASTORE state.
            if (!new File(isaacDbDirectory, DATASTORE_ID_FILE).isFile()) {
                if (this.datastoreStartState != DataStoreStartState.NO_DATASTORE) {
                    //This may happen during transition, for a bit, since old DBs don't have them.  
                    //But after transition, this should always be created as part of the DB.
                    //It also gets written as a semantic on the root concept, so a secondary check will be done 
                    //later to make sure we are in sync.
                    LOG.warn("The datastore id file was missing on startup!");
                }
                Files.write(isaacDbDirectory.toPath().resolve(DATASTORE_ID_FILE), UUID.randomUUID().toString().getBytes());
            }

            dataStoreId = Optional.of(UUID.fromString(new String(Files.readAllBytes(isaacDbDirectory.toPath().resolve(DATASTORE_ID_FILE)))));

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
            LOG.error("Error starting FileSystemDataStore", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
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
            this.datastoreStartState = DataStoreStartState.NOT_YET_CHECKED;
            this.assemblageNid_SequenceGenerator_Map.clear();
            this.properties.clear();
            this.assemblage_ElementToNid_Map.clear();
            this.spinedChronologyMapMap.clear();
            this.spinedTaxonomyMapMap.clear();
            this.componentToSemanticNidsMap.clear();
            this.assemblageToObjectType_Map.clear();
            this.assemblageToVersionType_Map.clear();
            this.nidToAssemblageNidMap.clear();
            this.nidToElementSequenceMap.clear();
            this.lastSyncTask = null;
            this.lastSyncFuture = null;
            this.writeListeners.clear();
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error("Unexpected error in FileSystemDataStore shutdown", ex);
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

    private SpinedIntIntMap getAssemblageNid_ElementSequenceToNid_Map(int assemblageNid) {
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
   public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid) {
       IsaacObjectType result = assemblageToObjectType_Map.get(assemblageNid);
       if (result == null) {
          return IsaacObjectType.UNKNOWN;
       }
       return result;
   }

   @Override
   public NidSet getAssemblageNidsForType(IsaacObjectType searchType) {
      final NidSet results = new NidSet();
      assemblageToObjectType_Map.forEach((nid, type)-> {
         if (searchType == type) {
            results.add(nid);
         }
      });
      return results;
   }

   @Override
   public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException
   {
      IsaacObjectType oldValue = assemblageToObjectType_Map.computeIfAbsent(assemblageNid, (i -> type));
      if (oldValue != type) {
         throw new IllegalStateException("Tried to change the isaac object type of " + assemblageNid + " from " + oldValue + " to " + type);
      }
   }
   
   @Override
   public VersionType getVersionTypeForAssemblageNid(int assemblageNid)
   {
      VersionType result = assemblageToVersionType_Map.get(assemblageNid);
      if (result == null) {
         return VersionType.UNKNOWN;
      }
      return result;
   }

   @Override
   public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException
   {
      VersionType oldValue = assemblageToVersionType_Map.computeIfAbsent(assemblageNid, (i -> type));
      if (oldValue != type) {
         throw new IllegalStateException("Tried to change the version type of " + assemblageNid + " from " + oldValue + " to " + type);
      }
   }

   @Override
    public Optional<ByteArrayDataBuffer> getChronologyData(int nid) {
        OptionalInt assemblageNidOptional = ModelGet.identifierService().getAssemblageNid(nid);
        if (!assemblageNidOptional.isPresent()) {
            return Optional.empty();
        }

        int elementSequence = getElementSequenceForNid(nid, assemblageNidOptional.getAsInt());
        SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNidOptional.getAsInt());
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

        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                // discard the 0 integer at the beginning of the record. 
                // 0 put in to enable the chronicle to sort before the versions. 
                if (data[0][0] != 0 && data[0][1] != 0 && data[0][2] != 0 && data[0][3] != 0) {
                    throw new IllegalStateException("Record does not start with zero...");
                }
                byteBuffer.put(data[0], 4, data[0].length - 4);
            } else {
                byteBuffer.put(data[i]);
            }

        }

        byteBuffer.putInt(0);
        byteBuffer.rewind();

        if (byteBuffer.getUsed() != size) {
            throw new IllegalStateException("Size = " + size + " used = " + byteBuffer.getUsed());
        }
        return Optional.of(byteBuffer);
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
                            LOG.info("Lazy open of " + filesToRead
                                    + " chronology files for assemblage: "
                                    + " " + properties.getProperty(Integer.toString(assemblageNid))
                                    + assemblageNid + " " + Integer.toUnsignedString(assemblageNid));
                        }
                    }
                    return spinedByteArrayArrayMap;
                });

        return spinedMap;
    }

    @Override
    public int[] getSemanticNidsForComponent(int componentNid) {
        return componentToSemanticNidsMap.get(componentNid);
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return dataStoreId;
    }

    @Override
    public Path getDataStorePath() {
        return this.isaacDbDirectory.toPath();
    }

    @Override
    public DataStoreStartState getDataStoreStartState() {
        return this.datastoreStartState;
    }

    private SpinedNidIntMap getNidToAssemblageNidMap() {
        return nidToAssemblageNidMap;
    }

    private ConcurrentMap<Integer, AtomicInteger> getSequenceGeneratorMap() {
        return assemblageNid_SequenceGenerator_Map;
    }

    private File getSpineDirectory(File parentDirectory, int assemblageNid) {
        File spinedMapDirectory = new File(parentDirectory, Integer.toUnsignedString(assemblageNid));

        spinedMapDirectory.mkdirs();
        return spinedMapDirectory;
    }

    private SpinedIntIntArrayMap getTaxonomyMap(int assemblageNid) {
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

   /**
    * {@inheritDoc}
    */
    @Override
    public boolean hasChronologyData(int nid, IsaacObjectType expectedType) {
        OptionalInt assemblageNid = ModelGet.identifierService().getAssemblageNid(nid);
        if (assemblageNid.isPresent()) {
            if (expectedType != assemblageToObjectType_Map.get(assemblageNid.getAsInt())) {
                return false;
            }
            int elementSequence = getElementSequenceForNid(nid, assemblageNid.getAsInt());
            SpinedByteArrayArrayMap spinedByteArrayArrayMap = getChronologySpinedMap(assemblageNid.getAsInt());
            byte[][] data = spinedByteArrayArrayMap.get(elementSequence);

            return data != null;
        } else {
            return false;
        }
    }

    //~--- inner classes -------------------------------------------------------
    private class SyncTask
            extends TimedTaskWithProgressTracker<Void> {

        public SyncTask() {
            updateTitle("Writing data to disk");
        }

        //~--- methods ----------------------------------------------------------
        @Override
        protected Void call()
                throws Exception {
            Get.activeTasks().add(this);
            pendingSync.release();
            syncSemaphore.acquireUninterruptibly();

            try {
                if (Get.configurationService().isInDBBuildMode(BuildMode.IBDF)) {
                    //No reason to write out all the files below (some of which fail anyway) during IBDF Build mode, because the 
                    //purpose of IBDF DBBuildMode is to generate IBDF files, not a valid database.
                    addToTotalWork(1);
                    updateMessage("Bypass writes on shutdown due to DB Build mode");
                    completedUnitOfWork();
                    FileSystemDataStore.LOG.info("Skipping write secondary to BuildMode.IBDF");
                } else {
                    addToTotalWork(9);
                    updateMessage("Writing sequence generator map...");
                    writeSequenceGeneratorMapFile();

                    completedUnitOfWork();  // 1
                    updateMessage("Writing assemblage nids...");
                    writeAssemblageToObjectTypeFile();
                    writeAssemblageToVersionTypeFile();
                    completedUnitOfWork();  // 2
                    updateMessage("Writing component to semantics map...");

                    if (componentToSemanticNidsMap.write(componentToSemanticMapDirectory)) {
                        FileSystemDataStore.LOG.info("Synchronized component to semantics map changes.");
                    }

                    completedUnitOfWork();  // 3
                    updateMessage("Writing chronology spines...");
                    spinedChronologyMapMap.forEach(
                            (assemblageNid, spinedMap) -> {
                                File directory = getSpineDirectory(chronologySpinesDirectory, assemblageNid);

                                addInfoFile(directory, assemblageNid);

                                if (spinedMap.write(directory)) {
                                    String assemblageDescription = properties.getProperty(Integer.toUnsignedString(assemblageNid));
                                    FileSystemDataStore.LOG.debug("Syncronized chronologies: " + assemblageNid
                                            + " " + assemblageDescription + " to " + directory.getAbsolutePath());
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
                                    FileSystemDataStore.LOG.info("Syncronizing taxonomies: " + assemblageNid
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
                    
                    writeListeners.forEach(listener -> listener.sync());
                }
                updateMessage("Write complete");
                FileSystemDataStore.LOG.info("FileSystemDataStore sync complete.");
                return null;
            } finally {
                syncSemaphore.release();
                Get.activeTasks()
                        .remove(this);
            }
        }

        private void addInfoFile(File directory, Integer assemblageNid) {
            if (LookupService.getCurrentRunLevel() >= LookupService.SL_L5_ISAAC_STARTED_RUNLEVEL) {
                File parentDirectory = directory.getParentFile();
                File[] filesWithPrefix = parentDirectory.listFiles((dir, name) -> name.startsWith(directory.getName()));

                if (filesWithPrefix.length < 2 && Get.conceptService().hasConcept(assemblageNid)) {
                    try {
                        Optional<String> descriptionOptional = Get.concept(assemblageNid)
                                .getRegularName();

                        if (descriptionOptional.isPresent()) {
                            String description = descriptionOptional.get();
                            if (!description.startsWith("No description")) {
                                properties.put(Integer.toString(assemblageNid), description);
                                properties.put(Integer.toUnsignedString(assemblageNid), description);
                                description = description.replaceAll("[^a-zA-Z0-9\\-_\\. ]", "_");
                                if (description.length() > 240) {
                                   description = description.substring(0, 240);
                                }

                                File descriptionFile = new File(
                                        parentDirectory,
                                        Integer.toUnsignedString(
                                                assemblageNid) + "-" + description);

                                try {
                                    descriptionFile.getParentFile().mkdirs();
                                    descriptionFile.createNewFile();
                                } catch (IOException ex) {
                                    LOG.warn("Failed to write assemblage description file", ex);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOG.error("Unexpected error while writing on info files for assemblages", e);
                    }
                }
            }
        }
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
         int elementSequence = nidToElementSequenceMap.get(nid);
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
         if (assemblageForNid == Integer.MAX_VALUE) {
            this.getNidToAssemblageNidMap().put(nid, assemblageNid);
         } else if (assemblageNid != assemblageForNid) {
             throw new IllegalStateException("Assemblage nids do not match: \n" +
                     Get.conceptDescriptionText(assemblageNid) + "(" + assemblageNid + ") and\n" +
                     Get.conceptDescriptionText(assemblageForNid) + "(" + assemblageForNid + ")");
         }

         AtomicInteger sequenceGenerator = this.getSequenceGeneratorMap().computeIfAbsent(
                                               assemblageNid,
                                                     (key) -> new AtomicInteger(1));
         int elementSequence = nidToElementSequenceMap.getAndUpdate(
             nid,
                 (currentValue) -> {
                    if (currentValue == Integer.MAX_VALUE) {
                       return sequenceGenerator.getAndIncrement();
                    }
                    return currentValue;
                 });
         
         getAssemblageNid_ElementSequenceToNid_Map(assemblageNid).put(elementSequence, nid);
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
   public IntStream getNidsForAssemblage(int assemblageNid)
   {
      return getAssemblageNid_ElementSequenceToNid_Map(assemblageNid).valueStream();
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public OptionalInt getAssemblageOfNid(int nid)
   {
      int value = nidToAssemblageNidMap.get(nid);
      if (value != Integer.MAX_VALUE) {
         return OptionalInt.of(value);
      }
      return OptionalInt.empty();
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException
   {
      OptionalInt current = getAssemblageOfNid(nid);
      if (current.isPresent() && current.getAsInt() != assemblage) {
           throw new IllegalArgumentException("The nid " + nid + " is already assigned to assemblage " 
                   + current + " and cannot be reassigned to " + nid);
      }
      else {
         nidToAssemblageNidMap.put(nid,  assemblage);
      }
   }
   
   @Override
   public boolean implementsSequenceStore() {
      return true;
   }
}
