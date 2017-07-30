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



package sh.isaac.model;

//~--- JDK imports ------------------------------------------------------------

import java.lang.ref.SoftReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ChronologyImpl.
 *
 * @author kec
 */
public abstract class ChronologyImpl
         implements Chronology, WaitFreeComparable {
   /** The Constant STAMPED_LOCKS. */
   private static final StampedLock[] STAMPED_LOCKS = new StampedLock[256];

   //~--- static initializers -------------------------------------------------

   static {
      for (int i = 0; i < STAMPED_LOCKS.length; i++) {
         STAMPED_LOCKS[i] = new StampedLock();
      }
   }

   //~--- fields --------------------------------------------------------------

   /**
    * Counter to give versions unique sequences within the chronicle.
    */
   private short versionSequence = 0;

   /** Position in the data where chronicle data ends, and version data starts. */
   private int versionStartPosition = -1;

   /**
    * The write sequence is incremented each time data is written, and provides
    * a check to see if this chronicle has had any changes written since the
    * data for this chronicle was read. If the write sequence does not match
    * the write sequences in the persistence storage, the data needs to be
    * merged prior to writing, according to the principles of a
    * {@code WaitFreeComparable} object.
    */
   private int writeSequence;

   /** Primordial uuid most significant bits for this component. */
   private long primordialUuidMsb;

   /** Primordial uuid least significant bits for this component. */
   private long primordialUuidLsb;

   /** additional uuid most and least significant bits for this component. */
   protected long[] additionalUuidParts;

   /** Native identifier of this component. */
   private int nid;

   /**
    * Concept sequence if a concept. Sememe sequence otherwise.
    */
   private int containerSequence;

   /**
    * Data previously persisted. Used for lazy instantiation of versions and
    * objects that are part of this chronicle.
    */
   private byte[] writtenData;

   /**
    * Data that has not yet been persisted. This data will need to be merged
    * with the written data when the chronicle is next serialized.
    */
   private ConcurrentSkipListMap<Integer, StampedVersion> unwrittenData;

   /**
    * Version data is stored in a soft reference after lazy instantiation, to
    * minimize unnecessary memory utilization.
    */
   private SoftReference<ArrayList<? extends StampedVersion>> versionListReference;

   //~--- constructors --------------------------------------------------------

   /**
    * No argument constructor for reconstituting an object previously serialized together with the
    * readData(ByteArrayDataBuffer data) method.
    *
    */
   protected ChronologyImpl() {}

   /**
    * For constructing an object for the first time.
    *
    * @param primordialUuid A unique external identifier for this chronicle
    * @param nid A unique internal identifier, that is only valid within this
    * database
    * @param containerSequence Either a concept sequence or a sememe sequence
    * depending on the ofType of the underlying object.
    */
   protected ChronologyImpl(UUID primordialUuid, int nid, int containerSequence) {
      this.writeSequence     = Integer.MIN_VALUE;
      this.primordialUuidMsb = primordialUuid.getMostSignificantBits();
      this.primordialUuidLsb = primordialUuid.getLeastSignificantBits();
      this.nid               = nid;
      this.containerSequence = containerSequence;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the additional uuids.
    *
    * @param uuid the uuid
    */
   public void addAdditionalUuids(UUID uuid) {
      final List<UUID> temp = getUuidList();

      temp.add(uuid);
      setAdditionalUuids(temp);
   }

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      final ChronologyImpl that = (ChronologyImpl) o;

      if (this.nid != that.nid) {
         return false;
      }

      final List<StampedVersion> versionList = (List<StampedVersion>) getVersionList();

      if (versionList.size() != that.getVersionList().size()) {
         return false;
      }

      return StampSequenceSet.of(getVersionStampSequences())
                             .equals(StampSequenceSet.of(that.getVersionStampSequences()));
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.nid;
   }

   /**
    * Merge this data, with data from another source to integrate into a single
    * data sequence.
    *
    * @param writeSequence the write sequence to use for the merged data
    * @param dataToMerge data from another source to integrate with this data
    * @return the merged data
    */
   public byte[] mergeData(int writeSequence, byte[] dataToMerge) {
      setWriteSequence(writeSequence);

      final ByteArrayDataBuffer db = new ByteArrayDataBuffer(512);

      writeChronicleData(db);

      final OpenIntHashSet writtenStamps = new OpenIntHashSet(11);

      if (this.unwrittenData != null) {
         this.unwrittenData.values()
                           .forEach(
                               (version) -> {
                                  final int stampSequenceForVersion = version.getStampSequence();

                                  if (Get.stampService()
                                         .isNotCanceled(stampSequenceForVersion)) {
                                     writtenStamps.add(stampSequenceForVersion);

                                     final int startWritePosition = db.getPosition();

                                     db.putInt(0);  // placeholder for length
                                     ((VersionImpl) version).writeVersionData(db);

                                     final int versionLength = db.getPosition() - startWritePosition;

                                     db.setPosition(startWritePosition);
                                     db.putInt(versionLength);
                                     db.setPosition(db.getLimit());
                                  }
                               });
      }

      if (this.writtenData != null) {
         mergeData(this.writtenData, writtenStamps, db);
      }

      if (dataToMerge != null) {
         mergeData(dataToMerge, writtenStamps, db);
      }

      db.putInt(0);  // last data is a zero length version record
      db.trimToSize();
      return db.getData();
   }

   /**
    * Write a complete binary representation of this chronicle, including all versions, to the
    * ByteArrayDataBuffer using externally valid identifiers (all nids, sequences, replaced with UUIDs).
    * @param out the buffer to write to.
    */
   @Override
   public final void putExternal(ByteArrayDataBuffer out) {
      assert out.isExternalData() == true;
      writeChronicleData(out);

      // add versions...
      getVersionList().forEach(
          (version) -> {
             final int stampSequenceForVersion = version.getStampSequence();

             writeIfNotCanceled(out, version, stampSequenceForVersion);
          });
      out.putInt(0);  // last data is a zero length version record
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();

      builder.append(this.getClass()
                         .getSimpleName());
      builder.append("{");
      toString(builder);
      builder.append('}');
      return builder.toString();
   }

   /**
    * To string.
    *
    * @param builder the builder
    */
   public void toString(StringBuilder builder) {
      builder  // .append("write:").append(writeSequence)
            .append("uuid:")
            .append(new UUID(this.primordialUuidMsb, this.primordialUuidLsb))
            .append(",\n nid:")
            .append(this.nid)
            .append("\n container:")
            .append(this.containerSequence)

      // .append(", versionStartPosition:").append(versionStartPosition)
      .append(",\n versions[");
      getVersionList().forEach(
          (version) -> {
             builder.append("\n");
             builder.append(version);
             builder.append(",");
          });

      if (getVersionList() != null) {
         builder.deleteCharAt(builder.length() - 1);
      }

      builder.append("]");
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public String toUserString() {
      return toString();
   }

   /**
    * Use to add a new version to the chronicle.
    *
    * @param <V>
    * @param version the version to add
    */
   protected <V extends StampedVersion> void addVersion(V version) {
      if (this.unwrittenData == null) {
         final long lockStamp = getLock(this.nid).writeLock();

         try {
            this.unwrittenData = new ConcurrentSkipListMap<>();
         } finally {
            getLock(this.nid).unlockWrite(lockStamp);
         }
      }

      this.unwrittenData.put(version.getStampSequence(), version);

      // invalidate the version reference list, it will be reconstructed with the new version
      // added if requested via a call to versionStream();
      this.versionListReference = null;
   }

   /**
    * Stores the location where the chronicle data ends, and the version data
    * starts.
    *
    * @param data the buffer from which to derive the location data.
    */
   protected final void constructorEnd(ByteArrayDataBuffer data) {
      this.versionStartPosition = data.getPosition();
   }

   /**
    * Call to subclass to read data from the data buffer, and create the
    * corresponding version object. The subclass is not responsible to add the
    * version to the version list, that task is performed by the calling method
    * ({@code maveVersions}).
    *
    * @param <V>
    * @param stampSequence the stamp sequence for this version
    * @param bb the data buffer
    * @return the version object
    */
   protected abstract <V extends StampedVersion> V makeVersion(int stampSequence, ByteArrayDataBuffer bb);

   /**
    * Reconstitutes version objects previously serialized.
    *
    * @param <V>
    * @param bb the byte buffer containing previously written data
    * @param results list of the reconstituted version objects
    */
   protected <V extends StampedVersion> void makeVersions(ByteArrayDataBuffer bb, ArrayList<V> results) {
      int nextPosition = bb.getPosition();

      assert nextPosition >= 0: bb;

      while (nextPosition < bb.getLimit()) {
         final int versionLength = bb.getInt();

         assert versionLength >= 0: bb;

         if (versionLength > 0) {
            nextPosition = nextPosition + versionLength;

            final int stampSequence = bb.getStampSequence();

            if (stampSequence >= 0) {
               results.add(makeVersion(stampSequence, bb));
            }
         } else {
            nextPosition = Integer.MAX_VALUE;
         }
      }
   }

   /**
    * Merge data.
    *
    * @param dataToMerge the data to merge
    * @param writtenStamps the written stamps
    * @param db the db
    */
   protected void mergeData(byte[] dataToMerge, OpenIntHashSet writtenStamps, ByteArrayDataBuffer db) {
      final ByteArrayDataBuffer writtenBuffer = new ByteArrayDataBuffer(dataToMerge);

      goToVersionStart(writtenBuffer);

      int nextPosition = writtenBuffer.getPosition();

      while (nextPosition < writtenBuffer.getLimit()) {
         writtenBuffer.setPosition(nextPosition);

         final int versionLength = writtenBuffer.getInt();

         if (versionLength > 0) {
            final int stampSequenceForVersion = writtenBuffer.getInt();

            if ((!writtenStamps.contains(stampSequenceForVersion)) &&
                  Get.stampService().isNotCanceled(stampSequenceForVersion)) {
               writtenStamps.add(stampSequenceForVersion);
               db.append(writtenBuffer, nextPosition, versionLength);
            }

            nextPosition = nextPosition + versionLength;
         } else {
            nextPosition = writtenBuffer.getLimit();
         }
      }
   }

   /**
    * Next version sequence.
    *
    * @return the short
    */
   protected short nextVersionSequence() {
      return this.versionSequence++;
   }

   /**
    * Put additional chronicle fields.
    *
    * @param out the out
    */
   protected abstract void putAdditionalChronicleFields(ByteArrayDataBuffer out);

   /**
    * Reads data from the ByteArrayDataBuffer. If the data is external, it reads all versions from the ByteArrayDataBuffer.
    * If the data is internal, versions are lazily read.
    *
    * @param data the data
    */
   protected void readData(ByteArrayDataBuffer data) {
      if (data.getObjectDataFormatVersion() != 0) {
         throw new UnsupportedOperationException(
             "Can't handle data format version: " + data.getObjectDataFormatVersion());
      }

      if (data.isExternalData()) {
         this.writeSequence = Integer.MIN_VALUE;
      } else {
         this.writeSequence = data.getInt();
         this.writtenData   = data.getData();
      }

      this.primordialUuidMsb = data.getLong();
      this.primordialUuidLsb = data.getLong();
      getAdditionalUuids(data);

      if (data.isExternalData()) {
         this.nid = Get.identifierService()
                       .getNidForUuids(new UUID(this.primordialUuidMsb, this.primordialUuidLsb));
         getUuidList().forEach(
             (uuid) -> {
                Get.identifierService()
                   .addUuidForNid(uuid, this.nid);
             });

         if (this instanceof ConceptChronologyImpl) {
            this.containerSequence = Get.identifierService()
                                        .getConceptSequence(this.nid);
         } else if (this instanceof SememeChronologyImpl) {
            this.containerSequence = Get.identifierService()
                                        .getSememeSequence(this.nid);
         } else {
            throw new UnsupportedOperationException("Can't handle " + this.getClass().getSimpleName());
         }

         setAdditionalChronicleFieldsFromBuffer(data);
         readVersionList(data);
      } else {
         this.nid               = data.getNid();
         this.containerSequence = data.getInt();
         this.versionSequence   = data.getShort();
         setAdditionalChronicleFieldsFromBuffer(data);
         constructorEnd(data);
      }
   }

   /**
    * Skip additional chronicle fields.
    *
    * @param in the in
    */
   protected abstract void skipAdditionalChronicleFields(ByteArrayDataBuffer in);

   /**
    * Write only the chronicle data (not the versions) to the ByteArrayDataBuffer
    * using identifiers determined by the ByteArrayDataBuffer.isExternalData() to
    * determine if the identifiers should be nids and sequences, or if they should
    * be UUIDs.
    * @param data the buffer to write to.
    */
   protected void writeChronicleData(ByteArrayDataBuffer data) {
      if (!data.isExternalData()) {
         data.putInt(this.writeSequence);
      }

      data.putLong(this.primordialUuidMsb);
      data.putLong(this.primordialUuidLsb);

      if (this.additionalUuidParts == null) {
         data.putInt(0);
      } else {
         data.putInt(this.additionalUuidParts.length);
         LongStream.of(this.additionalUuidParts)
                   .forEach((uuidPart) -> data.putLong(uuidPart));
      }

      if (!data.isExternalData()) {
         data.putInt(this.nid);
         data.putInt(this.containerSequence);
         data.putShort(this.versionSequence);
      }

      putAdditionalChronicleFields(data);
   }

   /**
    * Go to version start.
    *
    * @param data the data
    */
   private void goToVersionStart(ByteArrayDataBuffer data) {
      if (data.isExternalData()) {
         throw new UnsupportedOperationException("Can't handle external data for this method.");
      }

      data.getInt();    // this.writeSequence =
      data.getLong();   // this.primordialUuidMsb =
      data.getLong();   // this.primordialUuidLsb =
      skipAdditionalUuids(data);
      data.getNid();    // this.nid =
      data.getInt();    // this.containerSequence =
      data.getShort();  // this.versionSequence =
      skipAdditionalChronicleFields(data);
   }

   /**
    * Read version list.
    *
    * @param bb the bb
    */
   private void readVersionList(ByteArrayDataBuffer bb) {
      if (bb.isExternalData()) {
         int nextPosition = bb.getPosition();

         while (nextPosition < bb.getLimit()) {
            final int versionLength = bb.getInt();

            if (versionLength > 0) {
               nextPosition = nextPosition + versionLength;

               final int stampSequence = bb.getStampSequence();

               if (stampSequence >= 0) {
                  addVersion(makeVersion(stampSequence, bb));
               }
            } else {
               nextPosition = Integer.MAX_VALUE;
            }
         }
      } else {
         throw new UnsupportedOperationException("This method only supports external data");
      }
   }

   /**
    * Skip additional uuids.
    *
    * @param data the data
    */
   private void skipAdditionalUuids(ByteArrayDataBuffer data) {
      final int additionalUuidPartsSize = data.getInt();

      if (additionalUuidPartsSize > 0) {
         for (int i = 0; i < additionalUuidPartsSize; i++) {
            data.getLong();
         }
      }
   }

   /**
    * Write if not canceled.
    *
    * @param db the db
    * @param version the version
    * @param stampSequenceForVersion the stamp sequence for version
    */
   private <V extends StampedVersion> void writeIfNotCanceled(ByteArrayDataBuffer db,
         V version,
         int stampSequenceForVersion) {
      if (Get.stampService()
             .isNotCanceled(stampSequenceForVersion)) {
         final int startWritePosition = db.getPosition();

         db.putInt(0);  // placeholder for length
         ((VersionImpl) version).writeVersionData(db);

         final int versionLength = db.getPosition() - startWritePosition;

         db.setPosition(startWritePosition);
         db.putInt(versionLength);
         db.setPosition(db.getLimit());
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Gets the additional chronicle fields.
    *
    * @param in the in
    */
   protected abstract void setAdditionalChronicleFieldsFromBuffer(ByteArrayDataBuffer in);

   /**
    * Sets the additional uuids.
    *
    * @param uuids the new additional uuids
    */
   public void setAdditionalUuids(List<UUID> uuids) {
      this.additionalUuidParts = new long[uuids.size() * 2];

      for (int i = 0; i < uuids.size(); i++) {
         final UUID uuid = uuids.get(i);

         this.additionalUuidParts[2 * i]     = uuid.getMostSignificantBits();
         this.additionalUuidParts[2 * i + 1] = uuid.getLeastSignificantBits();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the additional uuids.
    *
    * @param data the data
    * @return the additional uuids
    */
   private void getAdditionalUuids(ByteArrayDataBuffer data) {
      final int additionalUuidPartsSize = data.getInt();

      if (additionalUuidPartsSize > 0) {
         this.additionalUuidParts = new long[additionalUuidPartsSize];

         for (int i = 0; i < additionalUuidPartsSize; i++) {
            this.additionalUuidParts[i] = data.getLong();
         }
      }
   }

   /**
    * Gets the commit state.
    *
    * @return the commit state
    */
   @Override
   public CommitStates getCommitState() {
      if (getVersionStampSequences().anyMatch((stampSequence) -> Get.stampService()
            .isUncommitted(stampSequence))) {
         return CommitStates.UNCOMMITTED;
      }

      return CommitStates.COMMITTED;
   }

   /**
    * Gets the concept sequence if a concept. Sememe sequence otherwise.
    *
    * @return the concept sequence if a concept
    */
   public int getContainerSequence() {
      return this.containerSequence;
   }

   /**
    * Get data to write to datastore, use the writeSequence as it was
    * originally read from the database.
    *
    * @return the data to write
    */
   public byte[] getDataToWrite() {
      return getDataToWrite(this.writeSequence);
   }

   /**
    * Get data to write to datastore. Set the write sequence to the specified
    * value
    *
    * @param writeSequence the write sequence to prepend to the data
    * @return the data to write
    */
   public byte[] getDataToWrite(int writeSequence) {
      setWriteSequence(writeSequence);

      if (this.unwrittenData == null) {
         // no changes, so nothing to merge.
         if (this.writtenData != null) {
            final ByteArrayDataBuffer db = new ByteArrayDataBuffer(this.writtenData);

            return db.getData();
         }

         // creating a brand new object
         final ByteArrayDataBuffer db = new ByteArrayDataBuffer(10);

         writeChronicleData(db);
         db.putInt(0);  // zero length version record.
         db.trimToSize();
         return db.getData();
      }

      final ByteArrayDataBuffer db = new ByteArrayDataBuffer(512);

      writeChronicleData(db);

      if (this.writtenData != null) {
         db.put(
             this.writtenData,
             this.versionStartPosition,
             this.writtenData.length - this.versionStartPosition - 4);  // 4 for the zero length version at the end.
      }

      // add versions..
      this.unwrittenData.values()
                        .forEach(
                            (version) -> {
                               final int stampSequenceForVersion = version.getStampSequence();

                               writeIfNotCanceled(db, version, stampSequenceForVersion);
                            });
      db.putInt(0);  // last data is a zero length version record
      db.trimToSize();
      return db.getData();
   }

   /**
    * Gets the latest version.
    *
    * @param <V>
    * @param coordinate the coordinate
    * @return the latest version
    */
   @Override
   public <V extends StampedVersion> LatestVersion<V> getLatestVersion(StampCoordinate coordinate) {
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);

      if (this.versionListReference != null) {
         final ArrayList<V> versions = (ArrayList<V>) this.versionListReference.get();

         if (versions != null) {
            return calc.getLatestVersion(this);
         }
      }

      final StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

      if (latestStampSequences.isEmpty()) {
         return new LatestVersion<>();
      }

      return new LatestVersion<>(getVersionsForStamps(latestStampSequences));
   }

   /**
    * Checks if latest version active.
    *
    * @param coordinate the coordinate
    * @return true, if latest version active
    */
   @Override
   public boolean isLatestVersionActive(StampCoordinate coordinate) {
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(
                                                  coordinate.makeCoordinateAnalog(
                                                        State.ACTIVE,
                                                              State.INACTIVE,
                                                              State.CANCELED,
                                                              State.PRIMORDIAL));
      final StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

      if (latestStampSequences.isEmpty()) {
         return false;
      }

      return latestStampSequences.stream()
                                 .anyMatch(
                                     stampSequence -> Get.stampService()
                                           .getStatusForStamp(stampSequence) == State.ACTIVE);
   }

   /**
    * Gets the lock.
    *
    * @param key the key
    * @return the lock
    */
   protected static StampedLock getLock(int key) {
      return STAMPED_LOCKS[(((byte) key)) - Byte.MIN_VALUE];
   }

   /**
    * Gets the native identifier of this component.
    *
    * @return the native identifier of this component
    */
   @Override
   public int getNid() {
      return this.nid;
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      return new UUID(this.primordialUuidMsb, this.primordialUuidLsb);
   }

   /**
    * Gets the sememe list.
    *
    * @return the sememe list
    */
   @Override
   public <V extends SememeChronology> List<V> getSememeList() {
      return Get.sememeService()
                .<V>getSememesForComponent(this.nid)
                .collect(Collectors.toList());
   }

   /**
    * Gets the sememe list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe list from assemblage
    */
   @Override
   public <V extends SememeChronology> List<V> getSememeListFromAssemblage(int assemblageSequence) {
      return Get.sememeService()
                .<V>getSememesForComponentFromAssemblage(this.nid, assemblageSequence)
                .collect(Collectors.toList());
   }

   /**
    * Gets the sememe list from assemblage of type.
    *
    * @param assemblageSequence the assemblage sequence
    * @param type the type
    * @return the sememe list from assemblage of type
    */
   @Override
   public <V extends SememeChronology> List<V> getSememeListFromAssemblageOfType(int assemblageSequence,
         Class<? extends SememeVersion> type) {
      final List<V> results = Get.sememeService()
                                 .ofType(type)
                                 .<V>getSememesForComponentFromAssemblage(this.nid, assemblageSequence)
                                 .collect(Collectors.toList());

      return results;
   }

   /**
    * Gets the unwritten version list.
    *
    * @return a list of all unwritten versions contained in this chronicle.
    */
   @Override
   public <V extends StampedVersion> List<V> getUnwrittenVersionList() {
      final ArrayList<V> results = new ArrayList<>();

      if (this.unwrittenData != null) {
         results.addAll((Collection<V>) this.unwrittenData.values());
      }

      return results;
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      final List<UUID> uuids = new ArrayList<>();

      uuids.add(getPrimordialUuid());

      if (this.additionalUuidParts != null) {
         for (int i = 0; i < this.additionalUuidParts.length; i = i + 2) {
            uuids.add(new UUID(this.additionalUuidParts[i], this.additionalUuidParts[i + 1]));
         }
      }

      return uuids;
   }

   /**
    * Used to retrieve a single version, without creating all version objects
    * and storing them in a version list.
    *
    * @param <V>
    * @param stampSequence the stamp sequence that specifies a particular
    * version
    * @return the version with the corresponding stamp sequence
    */
   public <V extends StampedVersion> Optional<V> getVersionForStamp(int stampSequence) {
      if (this.versionListReference != null) {
         final List<V> versions = (List<V>) this.versionListReference.get();

         if (versions != null) {
            for (final V v: versions) {
               if (v.getStampSequence() == stampSequence) {
                  return Optional.of(v);
               }
            }
         }
      }

      if ((this.unwrittenData != null) && this.unwrittenData.containsKey(stampSequence)) {
         return Optional.of((V) this.unwrittenData.get(stampSequence));
      }

      final ByteArrayDataBuffer bb = new ByteArrayDataBuffer(this.writtenData);

      bb.setPosition(this.versionStartPosition);

      int nextPosition = bb.getPosition();

      while (nextPosition < bb.getLimit()) {
         final int versionLength = bb.getInt();

         nextPosition = nextPosition + versionLength;

         final int stampSequenceForVersion = bb.getStampSequence();

         if (stampSequence == stampSequenceForVersion) {
            return Optional.of(makeVersion(stampSequence, bb));
         }

         bb.setPosition(nextPosition);
      }

      return Optional.empty();
   }

   /**
    * Gets the version graph list.
    *
    * @return the version graph list
    */
   @Override
   public <V extends StampedVersion> List<Graph<V>> getVersionGraphList() {
      final HashMap<StampPath, TreeSet<V>> versionMap = new HashMap<>();

      getVersionList().<V>forEach(
          (version) -> {
             final StampPath path = Get.pathService()
                                       .getStampPath(version.getPathSequence());
             TreeSet<V>      versionSet = versionMap.get(path);

             if (versionSet == null) {
                versionSet = new TreeSet<>(
                    (V v1,
                     V v2) -> {
                       final int comparison = Long.compare(v1.getTime(), v2.getTime());

                       if (comparison != 0) {
                          return comparison;
                       }

                       return Integer.compare(v1.getStampSequence(), v2.getStampSequence());
                    });
                versionMap.put(path, versionSet);
             }

             versionSet.add((V) version);
          });

      if (versionMap.size() == 1) {
         // easy case...
         final List<Graph<V>> results = new ArrayList<>();
         final Graph<V>       graph   = new Graph<>();

         results.add(graph);
         versionMap.entrySet()
                   .forEach(
                       (entry) -> {
                          entry.getValue()
                               .forEach(
                                   (version) -> {
                                      if (graph.getRoot() == null) {
                                         graph.createRoot(version);
                                      } else {
                                         graph.getLastAddedNode()
                                              .addChild(version);
                                      }
                                   });
                       });
         return results;
      }

      // TODO support for more than one path...
      throw new UnsupportedOperationException("TODO: Implement version graph for more than one path...");
   }

   /**
    * Gets the version list.
    *
    * @return a list of all versions contained in this chronicle.
    */
   @Override
   public <V extends StampedVersion> List<V> getVersionList() {
      ArrayList<V> results = null;

      if (this.versionListReference != null) {
         results = (ArrayList<V>) this.versionListReference.get();
      }

      while (results == null) {
         results = new ArrayList<>();

         if ((this.writtenData != null) && (this.writtenData.length >= 4)) {
            final ByteArrayDataBuffer bb = new ByteArrayDataBuffer(this.writtenData);

            if (this.versionStartPosition < 0) {
               goToVersionStart(bb);
               this.versionStartPosition = bb.getPosition();
            } else {
               bb.setPosition(this.versionStartPosition);
            }

            makeVersions(bb, results);
         }

         if (this.unwrittenData != null) {
            results.addAll((Collection<V>) this.unwrittenData.values());
         }

         this.versionListReference = new SoftReference<>(results);
      }

      return results;
   }

   /**
    * Gets the version stamp sequences.
    *
    * @return a stream of the stampSequences for each version of this
    * chronology.
    */
   @Override
   public IntStream getVersionStampSequences() {
      final IntStream.Builder builder  = IntStream.builder();
      List<? extends StampedVersion>                 versions = null;

      if (this.versionListReference != null) {
         versions = this.versionListReference.get();
      }

      if (versions != null) {
         versions.forEach((version) -> builder.accept(version.getStampSequence()));
      } else if (this.writtenData != null) {
         final ByteArrayDataBuffer bb = new ByteArrayDataBuffer(this.writtenData);

         getVersionStampSequences(this.versionStartPosition, bb, builder);
      }

      if (this.unwrittenData != null) {
         this.unwrittenData.keySet()
                           .forEach((stamp) -> builder.accept(stamp));
      }

      return builder.build();
   }

   /**
    * Gets the version stamp sequences.
    *
    * @param index the index
    * @param bb the bb
    * @param builder the builder
    */
   protected void getVersionStampSequences(int index, ByteArrayDataBuffer bb, IntStream.Builder builder) {
      final int limit = bb.getLimit();

      while (index < limit) {
         bb.setPosition(index);

         final int versionLength = bb.getInt();

         if (versionLength > 0) {
            final int stampSequence = bb.getStampSequence();

            builder.accept(stampSequence);
            index = index + versionLength;
         } else {
            index = Integer.MAX_VALUE;
         }
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Overwrites existing versions. Use to remove duplicates, etc. Deliberately
    * not advertised in standard API, as this call may lose audit data.
    *
    * @param <V>
    * @param versions the new versions
    */
   public <V extends StampedVersion> void setVersions(Collection<V> versions) {
      if (this.unwrittenData != null) {
         this.unwrittenData.clear();
      }

      // reset written data
      this.writtenData = null;
      versions.forEach((V version) -> addVersion(version));
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the versions for stamps.
    *
    * @param stampSequences the stamp sequences
    * @return the versions for stamps
    */
   private <V extends StampedVersion> List<V> getVersionsForStamps(StampSequenceSet stampSequences) {
      final List<V> versions = new ArrayList<>(stampSequences.size());

      stampSequences.stream()
                    .forEach((stampSequence) -> versions.add((V) getVersionForStamp(stampSequence).get()));
      return versions;
   }

   /**
    * Gets the visible ordered version list.
    *
    * @param stampCoordinate the stamp coordinate
    * @return the visible ordered version list
    */
   @Override
   public <V extends StampedVersion> List<V> getVisibleOrderedVersionList(StampCoordinate stampCoordinate) {
      final RelativePositionCalculator calc              = RelativePositionCalculator.getCalculator(stampCoordinate);
      final SortedSet<V>               sortedLogicGraphs = new TreeSet<>(
                                                               (V graph1,
                                                                     V graph2) -> {
               final RelativePosition relativePosition = calc.fastRelativePosition(
                                                             graph1,
                                                                   graph2,
                                                                   stampCoordinate.getStampPrecedence());

               switch (relativePosition) {
               case BEFORE:
                  return -1;

               case EQUAL:
                  return 0;

               case AFTER:
                  return 1;

               case UNREACHABLE:
               case CONTRADICTION:
               default:
                  throw new UnsupportedOperationException("Can't handle: " + relativePosition);
               }
            });

      sortedLogicGraphs.addAll(getVersionList());
      return sortedLogicGraphs.stream()
                              .collect(Collectors.toList());
   }

   /**
    * Gets the write sequence is incremented each time data is written, and provides a check to see if this chronicle has had any changes written since the data for this chronicle was read. If the write sequence does not match the write sequences in the persistence storage, the data needs to be merged prior to writing, according to the principles of a {@code WaitFreeComparable} object.
    *
    * @return the write sequence is incremented each time data is written, and provides a check to see if this chronicle has had any changes written since the data for this chronicle was read
    */
   @Override
   public int getWriteSequence() {
      return this.writeSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set write sequence is incremented each time data is written, and provides a check to see if this chronicle has had any changes written since the data for this chronicle was read. If the write sequence does not match the write sequences in the persistence storage, the data needs to be merged prior to writing, according to the principles of a {@code WaitFreeComparable} object.
    *
    * @param writeSequence the new write sequence is incremented each time data is written, and provides a check to see if this chronicle has had any changes written since the data for this chronicle was read
    */
   @Override
   public void setWriteSequence(int writeSequence) {
      this.writeSequence = writeSequence;
   }

   /**
    * Called after merge and write operations to set the objects data to be the data
    * actually written so that the object in memory has the same value as the object
    * just written to the database.
    *
    * @param writtenData the new data previously persisted
    */
   public void setWrittenData(byte[] writtenData) {
      this.writtenData          = writtenData;
      this.unwrittenData        = null;
      this.versionListReference = null;
   }
}

