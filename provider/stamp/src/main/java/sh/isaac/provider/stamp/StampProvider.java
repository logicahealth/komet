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

//~--- JDK imports ------------------------------------------------------------
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

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
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConcurrentObjectIntMap;
import sh.isaac.api.collections.ConcurrentSequenceSerializedObjectMap;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.commit.UncommittedStamp;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.task.TimedTask;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 1/2/16.
 */
@Service(name = "Stamp Provider")
@RunLevel(value = 1)
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

   /**
    * TODO: persist across restarts.
    */
   private static final Map<UncommittedStamp, Integer> UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP
           = new ConcurrentHashMap<>();

   //~--- fields --------------------------------------------------------------
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
   private final ConcurrentObjectIntMap<Stamp> stampMap = new ConcurrentObjectIntMap<>();

   /**
    * The load required.
    */
   private final AtomicBoolean loadRequired = new AtomicBoolean();

   /**
    * The database validity.
    */
   private DatabaseValidity databaseValidity = DatabaseValidity.NOT_SET;

   /**
    * The stamp sequence path sequence map.
    */
   ConcurrentHashMap<Integer, Integer> stampSequencePathSequenceMap = new ConcurrentHashMap();

   /**
    * The db folder path.
    */
   private final Path dbFolderPath;

   /**
    * The stamp manager folder.
    */
   private final Path stampManagerFolder;

   /**
    * Persistent as a result of reading and writing the stampMap.
    */
   private final ConcurrentSequenceSerializedObjectMap<Stamp> inverseStampMap;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new stamp provider.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public StampProvider()
           throws IOException {
      this.dbFolderPath = LookupService.getService(ConfigurationService.class)
              .getChronicleFolderPath()
              .resolve("stamp-provider");
      this.loadRequired.set(Files.exists(this.dbFolderPath));
      Files.createDirectories(this.dbFolderPath);
      this.inverseStampMap = new ConcurrentSequenceSerializedObjectMap<>(new StampSerializer(),
              this.dbFolderPath,
              null,
              null);
      this.stampManagerFolder = this.dbFolderPath.resolve(DEFAULT_STAMP_MANAGER_FOLDER);

      if (!Files.exists(this.stampManagerFolder)) {
         this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
      }

      Files.createDirectories(this.stampManagerFolder);
   }

   //~--- methods -------------------------------------------------------------
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
   }

   /**
    * Cancel.
    *
    * @param authorSequence the author sequence
    * @return the task
    */
   @Override
   public synchronized Task<Void> cancel(int authorSequence) {
      UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.forEach((uncommittedStamp, stampSequence) -> {
         // for each uncommitted stamp matching the author, remove the uncommitted stamp
         // and replace with a canceled stamp.
         if (uncommittedStamp.authorSequence == authorSequence) {
            final Stamp stamp = new Stamp(uncommittedStamp.status,
                    Long.MIN_VALUE,
                    uncommittedStamp.authorSequence,
                    uncommittedStamp.moduleSequence,
                    uncommittedStamp.pathSequence);

            addStamp(stamp, stampSequence);
            UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.remove(uncommittedStamp);
         }
      });

      // TODO make asynchronous with a actual task.
      final Task<Void> task = new TimedTask() {
         @Override
         protected Object call()
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
    * Clear database validity value.
    */
   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      this.databaseValidity = DatabaseValidity.NOT_SET;
   }

   @Override
   public String describeStampSequenceForTooltip(int stampSequence, ManifoldCoordinate manifoldCoordinate) {
      final StringBuilder sb = new StringBuilder();

      sb.append("S: ");
      final State status = getStatusForStamp(stampSequence);

      sb.append(status).append("\nT: ");

      final long time = getTimeForStamp(stampSequence);

      // Cannot change to case statement, since case supports int not long...
      if (time == Long.MAX_VALUE) {
         sb.append("UNCOMMITTED");
      } else if (time == Long.MIN_VALUE) {
         sb.append("CANCELED");
      } else {
         ZonedDateTime stampTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC);
         sb.append(stampTime.format(FORMATTER));
      }
      LatestVersion<DescriptionVersion> authorDescription = manifoldCoordinate.getFullySpecifiedDescription(getAuthorSequenceForStamp(stampSequence));
      if (authorDescription.isPresent()) {
         sb.append("\nA: ").append(authorDescription.get().getText());
      } else {
         sb.append("\nA: unretrievable");
      }
      
      LatestVersion<DescriptionVersion> moduleDescription = manifoldCoordinate.getFullySpecifiedDescription(getModuleSequenceForStamp(stampSequence));
      if (moduleDescription.isPresent()) {
         sb.append("\nM: ").append(moduleDescription.get().getText());
      } else {
         sb.append("\nM: unretrievable");
      }
      
      LatestVersion<DescriptionVersion> pathDescription = manifoldCoordinate.getFullySpecifiedDescription(getPathSequenceForStamp(stampSequence));
      if (pathDescription.isPresent()) {
         sb.append("\nP: ").append(pathDescription.get().getText());
      } else {
         sb.append("\nP: unretrievable");
      }
      return sb.toString();
   }

   /**
    * Describe stamp sequence.
    *
    * @param stampSequence the stamp sequence
    * @return the string
    */
   @Override
   public String describeStampSequence(int stampSequence) {
      final StringBuilder sb = new StringBuilder();

      sb.append("{Stamp≤");
      sb.append(stampSequence);
      sb.append("::");

      final State status = getStatusForStamp(stampSequence);

      sb.append(status);

      if (status == State.ACTIVE) {
         sb.append("  ");
      }

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
      sb.append(Get.conceptDescriptionText(getAuthorSequenceForStamp(stampSequence)));
      sb.append(" <");
      sb.append(getAuthorSequenceForStamp(stampSequence));
      sb.append(">");
      sb.append(" m:");
      sb.append(Get.conceptDescriptionText(getModuleSequenceForStamp(stampSequence)));
      sb.append(" <");
      sb.append(getModuleSequenceForStamp(stampSequence));
      sb.append(">");
      sb.append(" p: ");
      sb.append(Get.conceptDescriptionText(getPathSequenceForStamp(stampSequence)));
      sb.append(" <");
      sb.append(getPathSequenceForStamp(stampSequence));
      sb.append(">≥S}");
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

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting StampProvider post-construct");

         if (this.loadRequired.get()) {
            LOG.info("Reading existing commit manager data. ");
            LOG.info("Reading " + STAMP_MANAGER_DATA_FILENAME);

            try (DataInputStream in = new DataInputStream(new FileInputStream(new File(this.stampManagerFolder.toFile(),
                    STAMP_MANAGER_DATA_FILENAME)))) {
               this.nextStampSequence.set(in.readInt());

               final int stampMapSize = in.readInt();

               for (int i = 0; i < stampMapSize; i++) {
                  final int stampSequence = in.readInt();
                  final Stamp stamp = new Stamp(in);

                  this.stampMap.put(stamp, stampSequence);
                  this.inverseStampMap.put(stampSequence, stamp);
               }

               final int uncommittedSize = in.readInt();

               for (int i = 0; i < uncommittedSize; i++) {
                  UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.put(new UncommittedStamp(in), in.readInt());
               }
            }

            this.databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
         }
      } catch (final Exception e) {
         LookupService.getService(SystemStatusService.class)
                 .notifyServiceConfigurationFailure("Stamp Provider", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping StampProvider pre-destroy. ");

      try (DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(this.stampManagerFolder.toFile(),
              STAMP_MANAGER_DATA_FILENAME)))) {
         out.writeInt(this.nextStampSequence.get());
         out.writeInt(this.stampMap.size());
         this.stampMap.forEachPair((Stamp stamp,
                 int stampSequence) -> {
            try {
               out.writeInt(stampSequence);
               stamp.write(out);
            } catch (final IOException ex) {
               throw new RuntimeException(ex);
            }
         });

         final int size = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.size();

         out.writeInt(size);

         for (final Map.Entry<UncommittedStamp, Integer> entry : UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.entrySet()) {
            entry.getKey()
                    .write(out);
            out.writeInt(entry.getValue());
         }
      } catch (final IOException e) {
         throw new RuntimeException(e);
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
      return getStampSequence(State.ACTIVE,
              getTimeForStamp(stampSequence),
              getAuthorSequenceForStamp(stampSequence),
              getModuleSequenceForStamp(stampSequence),
              getPathSequenceForStamp(stampSequence));
   }

   /**
    * Gets the author nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the author nid for stamp
    */
   public int getAuthorNidForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.USER.getNid();
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return s.get()
                 .getAuthorSequence();
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the author sequence for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the author sequence for stamp
    */
   @Override
   public int getAuthorSequenceForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.USER.getConceptSequence();
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return Get.identifierService()
                 .getConceptSequence(s.get()
                         .getAuthorSequence());
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDatabaseFolder() {
      return this.stampManagerFolder;
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.databaseValidity;
   }

   /**
    * Gets the module nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the module nid for stamp
    */
   private int getModuleNidForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.UNSPECIFIED_MODULE.getNid();
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return s.get()
                 .getModuleSequence();
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the module sequence for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the module sequence for stamp
    */
   @Override
   public int getModuleSequenceForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.UNSPECIFIED_MODULE.getConceptSequence();
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return Get.identifierService()
                 .getConceptSequence(s.get()
                         .getModuleSequence());
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
   private int getPathNidForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.PATH.getNid();
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return s.get()
                 .getPathSequence();
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the path sequence for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the path sequence for stamp
    */
   @Override
   public int getPathSequenceForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return TermAux.DEVELOPMENT_PATH.getConceptSequence();
      }

      if (this.stampSequencePathSequenceMap.containsKey(stampSequence)) {
         return this.stampSequencePathSequenceMap.get(stampSequence);
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         this.stampSequencePathSequenceMap.put(stampSequence,
                 Get.identifierService()
                         .getConceptSequence(s.get()
                                 .getPathSequence()));
         return this.stampSequencePathSequenceMap.get(stampSequence);
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence);
   }

   /**
    * Gets the pending stamps for commit.
    *
    * @return the pending stamps for commit
    */
   @Override
   synchronized public Map<UncommittedStamp, Integer> getPendingStampsForCommit() {
      final Map<UncommittedStamp, Integer> pendingStampsForCommit
              = new HashMap<>(UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP);

      UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.clear();
      return pendingStampsForCommit;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set pending stamps for commit.
    *
    * @param pendingStamps the pending stamps
    */
   @Override
   synchronized public void setPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps) {
      UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.putAll(pendingStamps);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the retired stamp sequence.
    *
    * @param stampSequence the stamp sequence
    * @return the retired stamp sequence
    */
   @Override
   public int getRetiredStampSequence(int stampSequence) {
      return getStampSequence(State.INACTIVE,
              getTimeForStamp(stampSequence),
              getAuthorSequenceForStamp(stampSequence),
              getModuleSequenceForStamp(stampSequence),
              getPathSequenceForStamp(stampSequence));
   }

   /**
    * Gets the stamp sequence.
    *
    * @param status the status
    * @param time the time
    * @param authorSequence the author sequence
    * @param moduleSequence the module sequence
    * @param pathSequence the path sequence
    * @return the stamp sequence
    */
   @Override
   public int getStampSequence(State status, long time, int authorSequence, int moduleSequence, int pathSequence) {
      final Stamp stampKey = new Stamp(status, time, authorSequence, moduleSequence, pathSequence);

      if (time == Long.MAX_VALUE) {
         final UncommittedStamp usp = new UncommittedStamp(status, authorSequence, moduleSequence, pathSequence);
         final Integer temp = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get(usp);

         if (temp != null) {
            return temp.intValue();
         } else {
            this.stampLock.lock();

            try {
               if (UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.containsKey(usp)) {
                  return UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get(usp);
               }

               final int stampSequence = this.nextStampSequence.getAndIncrement();

               UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.put(usp, stampSequence);
               this.inverseStampMap.put(stampSequence, stampKey);
               return stampSequence;
            } finally {
               this.stampLock.unlock();
            }
         }
      }

      OptionalInt stampValue = this.stampMap.get(stampKey);

      if (!stampValue.isPresent()) {
         // maybe have a few available in an atomic queue, and put back
         // if not used? Maybe in a thread-local?
         // Have different sequences, and have the increments be equal to the
         // number of sequences?
         this.stampLock.lock();

         try {
            stampValue = this.stampMap.get(stampKey);

            if (!stampValue.isPresent()) {
               stampValue = OptionalInt.of(this.nextStampSequence.getAndIncrement());
               this.inverseStampMap.put(stampValue.getAsInt(), stampKey);
               this.stampMap.put(stampKey, stampValue.getAsInt());
            }
         } finally {
            this.stampLock.unlock();
         }
      }

      return stampValue.getAsInt();
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
   public State getStatusForStamp(int stampSequence) {
      if (stampSequence < 0) {
         return State.CANCELED;
      }

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return s.get()
                 .getStatus();
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

      final Optional<Stamp> s = this.inverseStampMap.get(stampSequence);

      if (s.isPresent()) {
         return s.get()
                 .getTime();
      }

      throw new NoSuchElementException("No stampSequence found: " + stampSequence + " map size: "
              + this.stampMap.size() + " inverse map size: " + this.inverseStampMap.getSize());
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
