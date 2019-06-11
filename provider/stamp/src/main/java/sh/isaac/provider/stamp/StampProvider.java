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



package sh.isaac.provider.stamp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import javafx.concurrent.Task;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.MetadataService;
import sh.isaac.api.Status;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.commit.UncommittedStamp;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.util.DataToBytesUtils;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@Service(name = "Stamp Provider")
@RunLevel(value = LookupService.SL_L2)
public class StampProvider
         implements StampService {
   private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   /**
    * The Constant STAMP_MANAGER_DATA_FILENAME.
    */
   private static final String STAMP_MANAGER_DATA_FILENAME = "stamp-manager.data";

   /**
    * The Constant DEFAULT_STAMP_MANAGER_FOLDER.
    */
   public static final String DEFAULT_STAMP_MANAGER_FOLDER = "stamp-manager";

   private final AtomicReference<ConcurrentHashMap<UncommittedStamp, Integer>> UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP =
      new AtomicReference<>(
          new ConcurrentHashMap<>());

   //~--- fields --------------------------------------------------------------

   private Optional<UUID> dataStoreId = Optional.empty();

   /**
    * The stamp lock.
    */
   private final ReentrantLock stampLock = new ReentrantLock();

   /**
    * The next stamp sequence.
    */
   private final AtomicInteger nextStampSequence = new AtomicInteger(FIRST_STAMP_SEQUENCE);

   /**
    * Persistent map of stamp sequences to a Stamp object.
    */
   private final ConcurrentHashMap<Stamp, Integer> stampMap = new ConcurrentHashMap<>();

   /**
    * The database validity.
    */
   private DataStoreStartState databaseValidity = DataStoreStartState.NOT_YET_CHECKED;

   /**
    * The stamp sequence path nid map.
    */
   ConcurrentHashMap<Integer, Integer> stampSequence_PathNid_Map = new ConcurrentHashMap<>();

   /**
    * The db folder path.
    */
   private final Path dbFolderPath;

   /**
    * The stamp manager folder.
    */
   private Path stampManagerFolder;

   /**
    * Persistent as a result of reading and writing the stampMap.
    */
   private final ConcurrentHashMap<Integer, Stamp> inverseStampMap;
   
   private ExtendedStore dataStore = null;
   private ExtendedStoreData<Integer, Stamp> sequenceToStamp; 
   private ExtendedStoreData<Integer, UncommittedStamp> sequenceToUncommittedStamp;

   //~--- constructors --------------------------------------------------------

   /*
    * Instantiates a new stamp provider.  For HK2 only
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private StampProvider()
            throws IOException {
      ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
      Path       dataStorePath        = configurationService.getDataStoreFolderPath();

      this.dbFolderPath = dataStorePath.resolve("stamp-provider");
      this.inverseStampMap    = new ConcurrentHashMap<>();
      
      if (Get.dataStore().implementsExtendedStoreAPI()) {
         dataStore = (ExtendedStore)Get.dataStore();
         sequenceToStamp = dataStore.<Integer, byte[], Stamp>getStore("stampProviderSequenceToStamp", 
               (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write), 
               (toDeserialize) ->  {
                  try {
                     return toDeserialize == null ? null : new Stamp(DataToBytesUtils.getDataInput(toDeserialize));
                  }
                  catch (IOException e) {
                     throw new RuntimeException(e);
                  }
               });
         
         sequenceToUncommittedStamp = dataStore.<Integer, byte[], UncommittedStamp>getStore("stampProviderSequenceToUncommittedStamp", 
               (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write), 
               (toDeserialize) ->  {
                  try {
                     return toDeserialize == null ? null : new UncommittedStamp(DataToBytesUtils.getDataInput(toDeserialize));
                  }
                  catch (IOException e) {
                     throw new RuntimeException(e);
                  }
               });

         LOG.info("DataStore implements extended API, will be used for Stamp Manager");
      }
      else {
         Files.createDirectories(this.dbFolderPath);
         this.stampManagerFolder = this.dbFolderPath.resolve(DEFAULT_STAMP_MANAGER_FOLDER);
         LOG.info("DataStore does not implement extended API, local file store will be used for Stamp Manager");
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Set pending stamps for commit.
    *
    * @param pendingStamps the pending stamps
    */
   @Override
   synchronized public void addPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps) {
       for (Map.Entry<UncommittedStamp, Integer> entry: pendingStamps.entrySet()) {
           UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get().remove(entry.getKey());
       }
   }

   /**
    * Adds the stamp.
    *
    * @param stamp the stamp
    * @param stampSequence the stamp sequence
    */
   @Override
   public void addStamp(Stamp stamp, int stampSequence) {
      this.stampMap.put(stamp, stampSequence);
      this.inverseStampMap.put(stampSequence, stamp);
      if (dataStore != null) {
         sequenceToStamp.put(stampSequence, stamp);
      }
      LOG.trace("Added stamp {}", stamp);
   }

   /**
    * Cancel.
    *
    * @param authorNid the author nid
    * @return the task
    */
   @Override
   public synchronized Task<Void> cancel(int authorNid) {
      Map<UncommittedStamp, Integer> map = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get();

      map.forEach(
          (uncommittedStamp, stampSequence) -> {
         // for each uncommitted stamp matching the author, remove the uncommitted stamp
         // and replace with a canceled stamp.
             if (uncommittedStamp.authorNid == authorNid) {
                final Stamp stamp = new Stamp(
                                        uncommittedStamp.status,
                                        Long.MIN_VALUE,
                                        uncommittedStamp.authorNid,
                                        uncommittedStamp.moduleNid,
                                        uncommittedStamp.pathNid);

                addStamp(stamp, stampSequence);
                map.remove(uncommittedStamp);
             }
          });

      // TODO make asynchronous with a actual task.
      final Task<Void> task = new TimedTask<Void>() {
         @Override
         protected Void call()
                  throws Exception {
            Get.activeTasks()
               .remove(this);
            return null;
         }
      };

      Get.activeTasks()
         .add(task);
      Get.workExecutors()
         .getExecutor()
         .execute(task);
      return task;
   }

   /**
    * Describe stamp sequence.
    *
    * @param stampSequence the stamp sequence
    * @return the string
    */
   @Override
   public String describeStampSequence(int stampSequence) {
      if (stampSequence == -1) {
         return "{Stamp≤CANCELED≥}";
      }

      final StringBuilder sb = new StringBuilder();

      sb.append("{Stamp≤");
      sb.append(stampSequence);
      sb.append("::");

      try {
         final Status status = getStatusForStamp(stampSequence);

         sb.append(status);
         sb.append(" ");

         final long time = getTimeForStamp(stampSequence);

         if (time == Long.MAX_VALUE) {
            sb.append("UNCOMMITTED:");
         } else if (time == Long.MIN_VALUE) {
            sb.append("CANCELED:  ");
         } else {
            sb.append(Instant.ofEpochMilli(time));
         }

         sb.append(" a:");
         sb.append(Get.conceptDescriptionText(getAuthorNidForStamp(stampSequence)));
         sb.append(" m:");
         sb.append(Get.conceptDescriptionText(getModuleNidForStamp(stampSequence)));
         sb.append(" p: ");
         sb.append(Get.conceptDescriptionText(getPathNidForStamp(stampSequence)));
      } catch (Exception e) {
         sb.append(e.getMessage());
      }

      sb.append("≥}");
      return sb.toString();
   }

   @Override
   public String describeStampSequenceForTooltip(int stampSequence, ManifoldCoordinate manifoldCoordinate) {
      if (stampSequence == -1) {
         return "CANCELED";
      }

      final StringBuilder sb = new StringBuilder();

      sb.append("S: ");

      final Status status = getStatusForStamp(stampSequence);

      sb.append(status)
        .append("\nT: ");

      final long time = getTimeForStamp(stampSequence);

      // Cannot change to case statement, since case supports int not long...
      if (time == Long.MAX_VALUE) {
         sb.append("UNCOMMITTED");
      } else if (time == Long.MIN_VALUE) {
         sb.append("CANCELED");
      } else {
         ZonedDateTime stampTime = Instant.ofEpochMilli(time)
                                          .atZone(ZoneOffset.UTC);

         sb.append(stampTime.format(FORMATTER));
      }

      LatestVersion<DescriptionVersion> authorDescription = manifoldCoordinate.getPreferredDescription(
                                                                getAuthorNidForStamp(stampSequence));

      if (authorDescription.isPresent()) {
         sb.append("\nA: ")
           .append(authorDescription.get()
                                    .getText());
      } else {
         sb.append("\nA: unretrievable");
      }

      LatestVersion<DescriptionVersion> moduleDescription = manifoldCoordinate.getPreferredDescription(
                                                                getModuleNidForStamp(stampSequence));

      if (moduleDescription.isPresent()) {
         sb.append("\nM: ")
           .append(moduleDescription.get()
                                    .getText());
      } else {
         sb.append("\nM: unretrievable");
      }

      LatestVersion<DescriptionVersion> pathDescription = manifoldCoordinate.getPreferredDescription(
                                                              getPathNidForStamp(stampSequence));

      if (pathDescription.isPresent()) {
         sb.append("\nP: ")
           .append(pathDescription.get()
                                  .getText());
      } else {
         sb.append("\nP: unretrievable");
      }

      Optional<String> optionalComment = Get.commitService()
                                            .getComment(stampSequence);

      if (optionalComment.isPresent()) {
         sb.append("\n\ncomment: ");
         sb.append(optionalComment.get());
      }

      return sb.toString();
   }

   /**
    * Stamp sequences equal except author and time.
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @return true, if successful
    */
   @Override
   public boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2) {
      if (getModuleNidForStamp(stampSequence1) != getModuleNidForStamp(stampSequence2)) {
         return false;
      }

      if (getPathNidForStamp(stampSequence1) != getPathNidForStamp(stampSequence2)) {
         return false;
      }

      return getStatusForStamp(stampSequence1) == getStatusForStamp(stampSequence2);
   }

   @Override
   public Future<?> sync() {
      return Get.executor().submit(() -> {
         writeData();
         return null;
      });
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting Stamp provider");
      Get.executor().execute(progressTask);
      LOG.info("Starting StampProvider post-construct");
      try {
         if (dataStore == null) {
            LOG.debug("Looking for file-based stamp data");
            if (!Files.exists(this.stampManagerFolder) || !Files.isRegularFile(this.stampManagerFolder.resolve(DATASTORE_ID_FILE))) {
               this.databaseValidity = DataStoreStartState.NO_DATASTORE;
            }
            else {
               LOG.info("Reading existing commit manager data. ");
               LOG.info("Reading " + STAMP_MANAGER_DATA_FILENAME);
               
               this.databaseValidity = DataStoreStartState.EXISTING_DATASTORE;
               if (this.stampManagerFolder.resolve(DATASTORE_ID_FILE).toFile().isFile())
               {
                  dataStoreId = Optional.of(UUID.fromString(new String(Files.readAllBytes(this.stampManagerFolder.resolve(DATASTORE_ID_FILE)))));
               }
               else
               {
                  LOG.warn("No datastore ID in the pre-existing commit service {}", this.stampManagerFolder);
               }
            }
            
            Files.createDirectories(this.stampManagerFolder);
   
            if (!this.dataStoreId.isPresent())
            {
               this.dataStoreId = LookupService.get().getService(MetadataService.class).getDataStoreId();
               Files.write(this.stampManagerFolder.resolve(DATASTORE_ID_FILE), this.dataStoreId.get().toString().getBytes());
            }
            
            UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get().clear();
            this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);
            this.stampMap.clear();
            this.stampSequence_PathNid_Map.clear();
            this.inverseStampMap.clear();
            
            if (this.databaseValidity == DataStoreStartState.EXISTING_DATASTORE) {
               try (DataInputStream in = new DataInputStream(
                                             new FileInputStream(
                                                 new File(
                                                     this.stampManagerFolder.toFile(),
                                                     STAMP_MANAGER_DATA_FILENAME)))) {
                  this.nextStampSequence.set(in.readInt());
   
                  final int stampMapSize = in.readInt();
   
                  for (int i = 0; i < stampMapSize; i++) {
                     final int   stampSequence = in.readInt();
                     final Stamp stamp         = new Stamp(in);
   
                     this.stampMap.put(stamp, stampSequence);
                     this.inverseStampMap.put(stampSequence, stamp);
                  }
   
                  final int uncommittedSize = in.readInt();
   
                  for (int i = 0; i < uncommittedSize; i++) {
                     UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                           .put(new UncommittedStamp(in), in.readInt());
                  }
               }
            }
         }
         else
         {
            LOG.debug("Looking for data store based stamp data");
            this.databaseValidity = null;
            this.dataStoreId = null;
            UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get().clear();
            this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);
            this.stampMap.clear();
            this.stampSequence_PathNid_Map.clear();
            this.inverseStampMap.clear();
            //We put the nextStampSequence here in the MAX_VALUE slot.
            OptionalLong oi = dataStore.getSharedStoreLong(DEFAULT_STAMP_MANAGER_FOLDER + "-nextStampSequence");
            if (oi.isPresent()) {
               this.nextStampSequence.set((int)oi.getAsLong());
               sequenceToStamp.getStream().forEach(stampPair ->
               {
                   this.stampMap.put(stampPair.getValue(), stampPair.getKey());
                   this.inverseStampMap.put(stampPair.getKey(), stampPair.getValue());
               });
               
               sequenceToUncommittedStamp.getStream().forEach(stampPair ->
               {
                  this.UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get().put(stampPair.getValue(), stampPair.getKey());
               });
            }
         }
      } catch (final IOException e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Stamp Provider", e);
         throw new RuntimeException(e);
      } finally {
         progressTask.finished();
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping StampProvider pre-destroy. ");

      writeData();
      this.databaseValidity = DataStoreStartState.NOT_YET_CHECKED;
      UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get().clear();
      this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);
      this.stampMap.clear();
      this.stampSequence_PathNid_Map.clear();
      this.inverseStampMap.clear();
      this.dataStoreId = Optional.empty();
   }

   private void writeData() throws RuntimeException {
      if (dataStore == null) {
         //write to the file store
         try (DataOutputStream out = new DataOutputStream(
                 new FileOutputStream(
                         new File(this.stampManagerFolder.toFile(), STAMP_MANAGER_DATA_FILENAME)))) {
            out.writeInt(this.nextStampSequence.get());
            out.writeInt(this.stampMap.size());
            this.stampMap.forEach(
                    (Stamp stamp,
                            Integer stampSequence) -> {
                       try {
                          out.writeInt(stampSequence);
                          stamp.write(out);
                       } catch (final IOException ex) {
                          throw new RuntimeException(ex);
                       }
                    });
            
            final int size = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                    .size();
            
            out.writeInt(size);
   
            for (final Map.Entry<UncommittedStamp, Integer> entry: UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                    .entrySet()) {
               entry.getKey()
                       .write(out);
               out.writeInt(entry.getValue());
            }

         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }
      else
      {
         //Just write the unwritten bits to the data store
         //Should be there already, but make sure we have the latest
         dataStore.putSharedStoreLong(DEFAULT_STAMP_MANAGER_FOLDER + "-nextStampSequence", nextStampSequence.get());
         //stamps are written as they are created
         
         //Write the uncommitted data
         sequenceToUncommittedStamp.clearStore();
         for (Entry<UncommittedStamp, Integer> uc : UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get().entrySet())
         {
            sequenceToUncommittedStamp.put(uc.getValue().intValue(), uc.getKey());
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the activated stamp sequence.
    *
    * @param stampSequence the stamp sequence
    * @return the activated stamp sequence
    */
   @Override
   public int getActivatedStampSequence(int stampSequence) {
      return getStampSequence(
          Status.ACTIVE,
          getTimeForStamp(stampSequence),
          getAuthorNidForStamp(stampSequence),
          getModuleNidForStamp(stampSequence),
          getPathNidForStamp(stampSequence));
   }

   /**
    * Gets the author nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the author nid for stamp
    */
   @Override
   public int getAuthorNidForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.USER.getNid();
      }

      if (this.inverseStampMap.containsKey(stampSequence)) {
         return this.inverseStampMap.get(stampSequence)
                                    .getAuthorNid();
      }

      for (Map.Entry<UncommittedStamp, Integer> entry: UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
            .entrySet()) {
         if (entry.getValue() == stampSequence) {
            return entry.getKey().authorNid;
         }
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   @Override
   public Optional<UUID> getDataStoreId() {
      return dataStore == null ? dataStoreId : dataStore.getDataStoreId();
   }

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDataStorePath() {
      return dataStore == null ? this.stampManagerFolder : dataStore.getDataStorePath();
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DataStoreStartState getDataStoreStartState() {
      return dataStore == null ? this.databaseValidity : dataStore.getDataStoreStartState();
   }

   /**
    * Gets the module nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the module nid for stamp
    */
   @Override
   public int getModuleNidForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.UNSPECIFIED_MODULE.getNid();
      }

      if (this.inverseStampMap.containsKey(stampSequence)) {
         return this.inverseStampMap.get(stampSequence)
                                    .getModuleNid();
      }

      for (Map.Entry<UncommittedStamp, Integer> entry: UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
            .entrySet()) {
         if (entry.getValue() == stampSequence) {
            return entry.getKey().moduleNid;
         }
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Checks if not canceled.
    *
    * @param stamp the stamp
    * @return true, if not canceled
    */
   @Override
   public boolean isNotCanceled(int stamp) {
      if (stamp < 0) {
         return false;
      }

      return getTimeForStamp(stamp) != Long.MIN_VALUE;
   }

   /**
    * Gets the path nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the path nid for stamp
    */
   @Override
   public int getPathNidForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.DEVELOPMENT_PATH.getNid();
      }

      if (this.stampSequence_PathNid_Map.containsKey(stampSequence)) {
         return this.stampSequence_PathNid_Map.get(stampSequence);
      }

      if (this.inverseStampMap.containsKey(stampSequence)) {
         this.stampSequence_PathNid_Map.put(stampSequence, this.inverseStampMap.get(stampSequence)
               .getPathNid());
         return this.stampSequence_PathNid_Map.get(stampSequence);
      }

      for (Map.Entry<UncommittedStamp, Integer> entry: UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
            .entrySet()) {
         if (entry.getValue() == stampSequence) {
            return entry.getKey().pathNid;
         }
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the pending stamps for commit.
    *
    * @return the pending stamps for commit
    */
   @Override
   public ConcurrentHashMap<UncommittedStamp, Integer> getPendingStampsForCommit() {
      ConcurrentHashMap<UncommittedStamp, Integer> pendingStampsForCommit =
         UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get();

      while (!UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.compareAndSet(
            pendingStampsForCommit,
            new ConcurrentHashMap<>())) {
         pendingStampsForCommit = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get();
      }

      return pendingStampsForCommit;
   }

   /**
    * Gets the retired stamp sequence.
    *
    * @param stampSequence the stamp sequence
    * @return the retired stamp sequence
    */
   @Override
   public int getRetiredStampSequence(int stampSequence) {
      return getStampSequence(
          Status.INACTIVE,
          getTimeForStamp(stampSequence),
          getAuthorNidForStamp(stampSequence),
          getModuleNidForStamp(stampSequence),
          getPathNidForStamp(stampSequence));
   }

   /**
    * Gets the stamp sequence.
    *
    * @param status the status
    * @param time the time
    * @param authorSequence the author nid
    * @param moduleSequence the module nid
    * @param pathSequence the path nid
    * @return the stamp sequence
    */
   @Override
   public int getStampSequence(Status status, long time, int authorSequence, int moduleSequence, int pathSequence) {
       if (status == Status.PRIMORDIAL) {
           throw new UnsupportedOperationException(status + " is not an assignable value.");
       }
      final Stamp stampKey = new Stamp(status, time, authorSequence, moduleSequence, pathSequence);

      if (time == Long.MAX_VALUE) {
         final UncommittedStamp usp  = new UncommittedStamp(status, authorSequence, moduleSequence, pathSequence);
         final Integer          temp = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                                                                              .get(usp);

         if (temp != null) {
            return temp;
         } else {
            this.stampLock.lock();

            try {
               if (UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                     .containsKey(usp)) {
                  return UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                        .get(usp);
               }

               final int stampSequence = this.nextStampSequence.getAndIncrement();

               LOG.trace("Putting {}, {} into uncommitted stamp to sequence map",  usp, stampSequence);
               UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
                     .put(usp, stampSequence);
               this.inverseStampMap.put(stampSequence, stampKey);
               if (dataStore != null) {
                  dataStore.putSharedStoreLong(DEFAULT_STAMP_MANAGER_FOLDER + "-nextStampSequence", nextStampSequence.get());
               }
               return stampSequence;
            } finally {
               this.stampLock.unlock();
            }
         }
      }

      if (!this.stampMap.containsKey(stampKey)) {
         // maybe have a few available in an atomic queue, and put back
         // if not used? Maybe in a thread-local?
         // Have different sequences, and have the increments be equal to the
         // number of sequences?
         this.stampLock.lock();

         try {
            if (!this.stampMap.containsKey(stampKey)) {
               OptionalInt stampValue = OptionalInt.of(this.nextStampSequence.getAndIncrement());

               this.inverseStampMap.put(stampValue.getAsInt(), stampKey);
               this.stampMap.put(stampKey, stampValue.getAsInt());
               if (dataStore != null) {
                  dataStore.putSharedStoreLong(DEFAULT_STAMP_MANAGER_FOLDER + "-nextStampSequence", nextStampSequence.get());
                  sequenceToStamp.put(stampValue.getAsInt(), stampKey);
               }
            }
         } finally {
            this.stampLock.unlock();
         }
      }

      return this.stampMap.get(stampKey);
   }

   /**
    * Gets the stamp sequences.
    *
    * @return the stamp sequences
    */
   @Override
   public IntStream getStampSequences() {
      return IntStream.rangeClosed(FIRST_STAMP_SEQUENCE, this.nextStampSequence.get())
                      .filter((stampSequence) -> this.inverseStampMap.containsKey(stampSequence));
   }

   /**
    * Gets the status for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the status for stamp
    */
   @Override
   public Status getStatusForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return Status.CANCELED;
      }

      if (this.inverseStampMap.containsKey(stampSequence)) {
         return this.inverseStampMap.get(stampSequence)
                                    .getStatus();
      }

      for (Map.Entry<UncommittedStamp, Integer> entry: UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
            .entrySet()) {
         if (entry.getValue() == stampSequence) {
            return entry.getKey().status;
         }
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the time for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the time for stamp
    */
   @Override
   public long getTimeForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return Long.MIN_VALUE;
      }

      if (this.inverseStampMap.containsKey(stampSequence)) {
         return this.inverseStampMap.get(stampSequence)
                                    .getTime();
      }

      if (UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
            .containsValue(stampSequence)) {
         return Long.MAX_VALUE;
      }

      throw new NoSuchElementException(
          "No stampSequence found: " + stampSequence + " map size: " + this.stampMap.size() + " inverse map size: " +
          this.inverseStampMap.size());
   }

   @Override
   public Stamp getStamp(int stampSequence)
   {
      if (stampSequence < 0) {
          return new Stamp(Status.CANCELED, Long.MIN_VALUE, TermAux.USER.getNid(), TermAux.UNSPECIFIED_MODULE.getNid(), TermAux.DEVELOPMENT_PATH.getNid());
      }

      if (this.inverseStampMap.containsKey(stampSequence)) {
         return this.inverseStampMap.get(stampSequence);
      }

      for (Map.Entry<UncommittedStamp, Integer> entry: UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get()
           .entrySet()) {
        if (entry.getValue() == stampSequence) {
            UncommittedStamp us = entry.getKey();
            return new Stamp(us.status, Long.MAX_VALUE, us.authorNid, us.moduleNid, us.pathNid);
         }
      }

      throw new NoSuchElementException(
         "No stampSequence found: " + stampSequence + " map size: " + this.stampMap.size() + " inverse map size: " +
         this.inverseStampMap.size());
   }

   /**
    * Checks if uncommitted.
    *
    * @param stampSequence the stamp sequence
    * @return true, if uncommitted
    */
   @Override
   public boolean isUncommitted(int stampSequence) {
      return getTimeForStamp(stampSequence) == Long.MAX_VALUE;
   }
}

