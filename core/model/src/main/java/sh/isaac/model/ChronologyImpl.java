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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------
/**
 * The Class ChronologyImpl.
 *
 * @author kec
 */
public abstract class ChronologyImpl
        implements Chronology, WaitFreeComparable {
  protected static final Logger LOG = LogManager.getLogger();
   /**
    * The Constant STAMPED_LOCKS.
    */
   private static final StampedLock[] STAMPED_LOCKS = new StampedLock[256];

   //~--- static initializers -------------------------------------------------
   static {
      for (int i = 0; i < STAMPED_LOCKS.length; i++) {
         STAMPED_LOCKS[i] = new StampedLock();
      }
   }

   //~--- fields --------------------------------------------------------------
   /**
    * Position in the data where chronicle data ends, and version data starts.
    */
   private int versionStartPosition = -1;

   /**
    * The write sequence is incremented each time data is written, and provides a check to see if this chronicle has had
    * any changes written since the data for this chronicle was read. If the write sequence does not match the write
    * sequences in the persistence storage, the data needs to be merged prior to writing, according to the principles of
    * a {@code WaitFreeComparable} object.
    */
   private int writeSequence;

   /**
    * Primordial uuid most significant bits for this component.
    */
   private long primordialUuidMsb;

   /**
    * Primordial uuid least significant bits for this component.
    */
   private long primordialUuidLsb;

   /**
    * additional uuid most and least significant bits for this component.
    */
   protected long[] additionalUuidParts;

   /**
    * Native identifier of this component.
    */
   private int nid;

   /**
    * Native identifier of the assemblage concept that defines this chronology.
    */
   private int assemblageNid;

   /**
    * Sequence of this chronology within the assemblage that defines it.
    */
   private int elementSequence;

   /**
    * Data previously persisted. Used for lazy instantiation of versions and objects that are part of this chronicle.
    */
   private byte[] writtenData;

   /**
    * Data that has not yet been persisted. This data will need to be merged with the written data when the chronicle is
    * next serialized.
    */
   private ConcurrentSkipListMap<Integer, Version> unwrittenData;

   /**
    * Version data is stored in a soft reference after lazy instantiation, to minimize unnecessary memory utilization.
    */
   private SoftReference<ArrayList<? extends Version>> versionListReference;

   //~--- constructors --------------------------------------------------------
   /**
    * No argument constructor for reconstituting an object previously serialized together with the
    * readData(ByteArrayDataBuffer data) method.
    *
    */
   protected ChronologyImpl() {
   }

   @Override
   public final int getAssemblageNid() {
      return assemblageNid;
   }

   public final int getElementSequence() {
      return elementSequence;
   }

   /**
    * For constructing an object for the first time.
    *
    * @param primordialUuid A unique external identifier for this chronicle
    * @param assemblageNid The identifier for the concept that defines what assemblage this element is defined within.
    */
   protected ChronologyImpl(UUID primordialUuid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) {
      this.writeSequence = Integer.MIN_VALUE;
      this.primordialUuidMsb = primordialUuid.getMostSignificantBits();
      this.primordialUuidLsb = primordialUuid.getLeastSignificantBits();
      this.nid = Get.identifierService().assignNid(primordialUuid);
      this.assemblageNid = assemblageNid;
      ModelGet.identifierService().setupNid(this.nid, this.assemblageNid, objectType, versionType);
      this.elementSequence = ModelGet.identifierService().getElementSequenceForNid(this.nid, this.assemblageNid);
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

      final List<Version> versionList = (List<Version>) getVersionList();

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
      return Long.hashCode(this.nid);
   }

   /**
    * Merge this data, with data from another source to integrate into a single data sequence.
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
    * Write a complete binary representation of this chronicle, including all versions, to the ByteArrayDataBuffer using
    * externally valid identifiers (all nids, sequences, replaced with UUIDs).
    *
    * @param out the buffer to write to.
    */
   @Override
   public final void putExternal(ByteArrayDataBuffer out) {
      out.setExternalData(true);
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
      toString(builder, true);
      builder.append('}');
      return builder.toString();
   }

   public void toString(StringBuilder builder, boolean addAttachments) {
      getUuidList().forEach((uuid) -> builder
              .append(" uuid: ").append(uuid).append("\n"));
      builder.append(" nid: ")
              .append(this.nid)
              .append("\n container: ")
              .append(this.elementSequence)
              .append(",\n versions[");
      getVersionList().forEach(
              (version) -> {
                 builder.append("\n  ");
                 builder.append(version);
                 builder.append(",");
              });

      if (getVersionList() != null) {
         builder.deleteCharAt(builder.length() - 1);
      }
      
      builder.append("\n ]\n}\n");
      
      if (addAttachments) {
         builder.append("\n[[\n");
         AtomicInteger attachmentCount = new AtomicInteger(0);
         Get.assemblageService().getSemanticChronologyStreamForComponent(this.getNid()).forEach((sememe) -> {
            builder.append("ATTACHMENT ").append(attachmentCount.incrementAndGet())
                    .append(":\n  ");
            ((SemanticChronologyImpl) sememe).toString(builder, false);
         });
         builder.append("]]\n");
      }
      builder.append("\n");
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
   public <V extends Version> void addVersion(V version) {
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
    * Stores the location where the chronicle data ends, and the version data starts.
    *
    * @param data the buffer from which to derive the location data.
    */
   protected final void constructorEnd(ByteArrayDataBuffer data) {
      this.versionStartPosition = data.getPosition();
   }

   /**
    * Call to subclass to read data from the data buffer, and create the corresponding version object. The subclass is
    * not responsible to add the version to the version list, that task is performed by the calling method
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

      assert nextPosition >= 0 : bb;

      while (nextPosition < bb.getLimit()) {
         final int versionLength = bb.getInt();

         assert versionLength >= 0 : bb;

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

            if ((!writtenStamps.contains(stampSequenceForVersion))
                    && Get.stampService().isNotCanceled(stampSequenceForVersion)) {
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
    * Put additional chronicle fields.
    *
    * @param out the out
    */
   protected abstract void putAdditionalChronicleFields(ByteArrayDataBuffer out);

   /**
    * Reads data from the ByteArrayDataBuffer. If the data is external, it reads all versions from the
    * ByteArrayDataBuffer. If the data is internal, versions are lazily read.
    *
    * @param data the data
    */
   protected void readData(ByteArrayDataBuffer data) {
      if (data.getObjectDataFormatVersion() != getIsaacObjectType().getDataFormatVersion()) {
         throw new UnsupportedOperationException(
                 "Can't handle data format version: " + data.getObjectDataFormatVersion());
      }

      if (data.isExternalData()) {
         this.writeSequence = Integer.MIN_VALUE;
      } else {
         this.writeSequence = data.getInt();
         this.writtenData = data.getData();
      }

      this.primordialUuidMsb = data.getLong();
      this.primordialUuidLsb = data.getLong();
      getAdditionalUuids(data);

      this.assemblageNid = data.getNid();
      
      if (data.isExternalData()) {
         UUID primordialUUID = new UUID(this.primordialUuidMsb, this.primordialUuidLsb);
         List<UUID> allUUIDs = getUuidList();
         if (!Get.identifierService().hasUuid(primordialUUID)) {
            this.nid = Get.identifierService().assignNid(allUUIDs.toArray(new UUID[allUUIDs.size()]));
         }
         else {
            this.nid = Get.identifierService().getNidForUuids(allUUIDs);
            if (allUUIDs.size() > 1) {
               for (UUID additionalUuid : allUUIDs.subList(1, allUUIDs.size())) {
                  Get.identifierService().addUuidForNid(additionalUuid,this.nid);
               }
            }
         }
         this.elementSequence = ModelGet.identifierService().getElementSequenceForNid(this.nid, getAssemblageNid());
         setAdditionalChronicleFieldsFromBuffer(data);
         readVersionList(data);
      } else {
         this.nid = data.getNid();
         this.elementSequence = data.getInt();
         setAdditionalChronicleFieldsFromBuffer(data);
         constructorEnd(data);
         // find if there are any uncommitted versions in the written data...

         for (int stamp : getVersionStampSequences()) {
            if (Get.stampService().isUncommitted(stamp)) {
               this.unwrittenData = new ConcurrentSkipListMap<>();
               this.versionListReference = null;
               getVersionList().forEach((version) -> {
                  this.unwrittenData.put(version.getStampSequence(), version);
               });
               this.writtenData = null;
               break;
            }
         }
      }
      ModelGet.identifierService().setupNid(this.nid, this.assemblageNid, this.getIsaacObjectType(), this.getVersionType());

   }

   protected void updateStampSequence(int oldStampSequence, int newStampSequence, VersionImpl version) {
      if (this.unwrittenData == null) {
         throw new IllegalStateException("Cannot update since unwritten data is null");
      }
      if (!this.unwrittenData.containsKey(oldStampSequence)) {
         throw new IllegalStateException("No version with the old stamp sequence: " + oldStampSequence + "\n" + this.unwrittenData);
      }
      this.unwrittenData.remove(oldStampSequence);
      if (newStampSequence == -1) {
         this.versionListReference = null;
      } else {
         this.unwrittenData.put(newStampSequence, version);
      }
   }

   /**
    * Skip additional chronicle fields.
    *
    * @param in the in
    */
   protected abstract void skipAdditionalChronicleFields(ByteArrayDataBuffer in);

   /**
    * Write only the chronicle data (not the versions) to the ByteArrayDataBuffer using identifiers determined by the
    * ByteArrayDataBuffer.isExternalData() to determine if the identifiers should be nids and sequences, or if they
    * should be UUIDs.
    *
    * @param data the buffer to write to.
    */
   protected void writeChronicleData(ByteArrayDataBuffer data) {
      IsaacObjectType isaacObjectType = getIsaacObjectType();
      isaacObjectType.writeTypeVersionHeader(data);

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
      
      data.putNid(this.assemblageNid);

      if (!data.isExternalData()) {
         data.putInt(this.nid);
         data.putInt(this.elementSequence);
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
      getIsaacObjectType().readAndValidateHeader(data);
      data.getInt();    // this.writeSequence =
      data.getLong();   // this.primordialUuidMsb =
      data.getLong();   // this.primordialUuidLsb =
      skipAdditionalUuids(data);
      data.getNid();    // this.assemblageNid =
      data.getNid();    // this.nid =
      data.getInt();    // this.elementSequence =
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

         this.additionalUuidParts[2 * i] = uuid.getMostSignificantBits();
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
      for (int stampSequence : getVersionStampSequences()) {
         if (Get.stampService()
                 .isUncommitted(stampSequence)) {
            return CommitStates.UNCOMMITTED;
         }
      }

      return CommitStates.COMMITTED;
   }

   /**
    * Get data to write to datastore, use the writeSequence as it was originally read from the database.
    *
    * @return the data to write
    */
   public byte[] getDataToWrite() {
      return getDataToWrite(this.writeSequence);
   }

   /**
    * Get the data as a list of immutable byte arrays. With an append only data model, these records are safe for
    * concurrent writes without destroying data per the duplicate data model in Berkley DB and Xodus.
    *
    * The chronology record starts with an integer of 0 to differentiate from version records, and then is followed by a
    * byte for the object type, and a byte for the data format version... The object type byte is always > 0, and the
    * version byte is always > 0...
    *
    * Each byte[] for a version starts with an integer length of the version data. The minimum size of a version is 4
    * bytes (an integer stamp sequence).
    *
    * @return
    */
   public List<byte[]> getDataList() {

      List<byte[]> dataArray = new ArrayList<>();

      byte[] dataToSplit = getDataToWrite();
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
            System.out.println("Error versionTo: " + versionTo);
         }
        if (newLength < 0) {
            System.out.println("Error newLength: " + newLength);
        }
         dataArray.add(Arrays.copyOfRange(dataToSplit, versionStart, versionTo));
         versionStart = versionStart + versionSize;
         versionSize = (((dataToSplit[versionStart]) << 24) | ((dataToSplit[versionStart + 1] & 0xff) << 16)
                 | ((dataToSplit[versionStart + 2] & 0xff) << 8) | ((dataToSplit[versionStart + 3] & 0xff)));
      }

      return dataArray;
   }

   /**
    * Get data to write to datastore. Set the write sequence to the specified value
    *
    * @param writeSequence the write sequence to prepend to the data
    * @return the data to write
    */
   public byte[] getDataToWrite(int writeSequence) {
      setWriteSequence(writeSequence);

      if (this.unwrittenData == null) {
         // no changes, so nothing to merge.
         if (this.writtenData != null) {
            return this.writtenData;
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

      this.versionStartPosition = db.getPosition();
      for (Version version: getVersionList()) {
         final int stampSequenceForVersion = version.getStampSequence();
         writeIfNotCanceled(db, version, stampSequenceForVersion);
      }
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
   public <V extends Version> LatestVersion<V> getLatestVersion(StampCoordinate coordinate) {
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);

      if (this.versionListReference != null) {
         final ArrayList<V> versions = (ArrayList<V>) this.versionListReference.get();

         if (versions != null) {
            return calc.getLatestVersion(this);
         }
      }

      final int[] latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

      if (latestStampSequences.length == 0) {
         return new LatestVersion<>();
      }

      return new LatestVersion<>(getVersionsForStamps(latestStampSequences));
   }

   @Override
   public <V extends Version> LatestVersion<V> getLatestCommittedVersion(StampCoordinate coordinate) {
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);

      if (this.versionListReference != null) {
         final ArrayList<V> versions = (ArrayList<V>) this.versionListReference.get();

         if (versions != null) {
            return calc.getLatestVersion(this);
         }
      }

      final int[] latestStampSequences = calc.getLatestCommittedStampSequencesAsSet(this.getVersionStampSequences());

      if (latestStampSequences.length == 0) {
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
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate.makeCoordinateAnalog(Status.ACTIVE,
                      Status.INACTIVE,
                      Status.CANCELED,
                      Status.PRIMORDIAL));
      final int[] latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

      for (int stampSequence : latestStampSequences) {
         if (Get.stampService().getStatusForStamp(stampSequence) == Status.ACTIVE) {
            return true;
         }
      }
      return false;
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
   public <V extends SemanticChronology> List<V> getSemanticChronologyList() {
      return Get.assemblageService()
              .<V>getSemanticChronologyStreamForComponent(this.nid)
              .collect(Collectors.toList());
   }

   /**
    * Gets the sememe list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe list from assemblage
    */
   @Override
   public <V extends SemanticChronology> List<V> getSemanticChronologyListFromAssemblage(int assemblageSequence) {
      return Get.assemblageService()
              .<V>getSemanticChronologyStreamForComponentFromAssemblage(this.nid, assemblageSequence)
              .collect(Collectors.toList());
   }

   /**
    * Gets the unwritten version list.
    *
    * @return a list of all unwritten versions contained in this chronicle.
    */
   @Override
   public <V extends Version> List<V> getUnwrittenVersionList() {
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
    * Used to retrieve a single version, without creating all version objects and storing them in a version list.
    *
    * @param <V>
    * @param stampSequence the stamp sequence that specifies a particular version
    * @return the version with the corresponding stamp sequence
    */
   public <V extends StampedVersion> Optional<V> getVersionForStamp(int stampSequence) {
      if (this.versionListReference != null) {
         final List<V> versions = (List<V>) this.versionListReference.get();

         if (versions != null) {
            for (final V v : versions) {
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
   public <V extends Version> List<Graph<V>> getVersionGraphList() {
      final HashMap<StampPath, TreeSet<V>> versionMap = new HashMap<>();

      getVersionList().<V>forEach(
              (version) -> {
                 final StampPath path = Get.versionManagmentPathService()
                         .getStampPath(version.getPathNid());
                 TreeSet<V> versionSet = versionMap.get(path);

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
         final Graph<V> graph = new Graph<>();

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
   public <V extends Version> List<V> getVersionList() {
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

   public Map<Integer, Version> getStampVersionMap() {
      Map<Integer, Version> result = new HashMap<>();
      getVersionList().forEach((version) -> result.put(version.getStampSequence(), version));
      return result;
   }

   /**
    * Gets the version stamp sequences.
    *
    * @return a stream of the stampSequences for each version of this chronology.
    */
   @Override
   public int[] getVersionStampSequences() {
      final OpenIntHashSet builder = new OpenIntHashSet();
      List<? extends StampedVersion> versions = null;

      if (this.versionListReference != null) {
         versions = this.versionListReference.get();
      }

      if (versions != null) {
         versions.forEach((version) -> builder.add(version.getStampSequence()));
      } else if (this.writtenData != null) {
         final ByteArrayDataBuffer bb = new ByteArrayDataBuffer(this.writtenData);

         getVersionStampSequences(this.versionStartPosition, bb, builder);
      }

      if (this.unwrittenData != null) {
         this.unwrittenData.keySet()
                 .forEach((stamp) -> builder.add(stamp));
      }

      return builder.keys().elements();
   }

   /**
    * Gets the version stamp sequences.
    *
    * @param index the index
    * @param bb the bb
    * @param builder the builder
    */
   protected void getVersionStampSequences(int index, ByteArrayDataBuffer bb, OpenIntHashSet builder) {
      final int limit = bb.getLimit();

      while (index < limit) {
         bb.setPosition(index);

         final int versionLength = bb.getInt();

         if (versionLength > 0) {
            final int stampSequence = bb.getStampSequence();

            builder.add(stampSequence);
            index = index + versionLength;
         } else {
            index = Integer.MAX_VALUE;
         }
      }
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Overwrites existing versions. Use to remove duplicates, etc. Deliberately not advertised in standard API, as this
    * call may lose audit data.
    *
    * @param <V>
    * @param versions the new versions
    */
   public <V extends Version> void setVersions(Collection<V> versions) {
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
   private <V extends StampedVersion> List<V> getVersionsForStamps(int[] stampSequences) {
      final List<V> versions = new ArrayList<>(stampSequences.length);
      for (int stampSequence : stampSequences) {
         versions.add((V) getVersionForStamp(stampSequence).get());
      }
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
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(stampCoordinate);
      final SortedSet<V> sortedLogicGraphs = new TreeSet<>(
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
                       throw new UnsupportedOperationException("o Can't handle: " + relativePosition);
                 }
              });

      sortedLogicGraphs.addAll(getVersionList());
      return sortedLogicGraphs.stream()
              .collect(Collectors.toList());
   }

   /**
    * Gets the write sequence is incremented each time data is written, and provides a check to see if this chronicle
    * has had any changes written since the data for this chronicle was read. If the write sequence does not match the
    * write sequences in the persistence storage, the data needs to be merged prior to writing, according to the
    * principles of a {@code WaitFreeComparable} object.
    *
    * @return the write sequence is incremented each time data is written, and provides a check to see if this chronicle
    * has had any changes written since the data for this chronicle was read
    */
   @Override
   public int getWriteSequence() {
      return this.writeSequence;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set write sequence is incremented each time data is written, and provides a check to see if this chronicle has had
    * any changes written since the data for this chronicle was read. If the write sequence does not match the write
    * sequences in the persistence storage, the data needs to be merged prior to writing, according to the principles of
    * a {@code WaitFreeComparable} object.
    *
    * @param writeSequence the new write sequence is incremented each time data is written, and provides a check to see
    * if this chronicle has had any changes written since the data for this chronicle was read
    */
   @Override
   public void setWriteSequence(int writeSequence) {
      this.writeSequence = writeSequence;
   }

   /**
    * Called after merge and write operations to set the objects data to be the data actually written so that the object
    * in memory has the same value as the object just written to the database.
    *
    * @param writtenData the new data previously persisted
    */
   public void setWrittenData(byte[] writtenData) {
      this.writtenData = writtenData;
      this.unwrittenData = null;
      this.versionListReference = null;
   }

   @Override
   public NidSet getRecursiveSemanticNids() {
      NidSet sequenceSet = Get.assemblageService().getSemanticNidsForComponent(this.getNid());
      sequenceSet.stream().forEach((sememeSequence) -> addRecursiveSequences(sequenceSet, sememeSequence));

      return sequenceSet;
   }

   private void addRecursiveSequences(IntSet sememeSequenceSet, int semanticNid) {
      IntSet sequenceSet = Get.assemblageService().getSemanticNidsForComponent(semanticNid);
      sequenceSet.stream().forEach((sequence) -> {
         sememeSequenceSet.add(sequence);
         addRecursiveSequences(sememeSequenceSet, sequence);
      });

   }

}
