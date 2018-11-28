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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.model.semantic.SemanticChronologyImpl;

//~--- classes ----------------------------------------------------------------
/**
 * The Class ChronologyImpl.
 *
 * @author kec
 */
public abstract class ChronologyImpl
        implements Chronology, ChronologySerializeable {

    protected static final Logger LOG = LogManager.getLogger();

    //~--- fields --------------------------------------------------------------
    /**
     * Position in the data where chronicle data ends, and version data starts.
     */
    private int versionStartPosition = -1;

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

    protected VersionType versionType;

    /**
     * Data that has not yet been persisted. This data will need to be merged
     * with the written data when the chronicle is next serialized.
     */
    private final CopyOnWriteArrayList<Version> uncommittedVersions = new CopyOnWriteArrayList<>();

    /**
     * Data that has already been persisted.
     */
    private final CopyOnWriteArrayList<Version> committedVersions = new CopyOnWriteArrayList<>();

    //~--- constructors --------------------------------------------------------
    /**
     * No argument constructor for reconstituting an object previously
     * serialized together with the readData(ByteArrayDataBuffer data) method.
     *
     */
    protected ChronologyImpl() {
    }
    /**
     * 
     * @return true if uncommitted versions where removed. 
     */
    public boolean removeUncommittedVersions() {
        boolean anyRemoved = false;
        if (this.uncommittedVersions != null) {
            List<Version> toRemove = new ArrayList<>();
            for (Version v: this.uncommittedVersions) {
                if (v.getTime() == Long.MAX_VALUE) {
                    toRemove.add(v);
                    anyRemoved = true;
                }
            };
            this.uncommittedVersions.removeAll(toRemove);
        }
        List<Version> toRemove = new ArrayList<>();
            for (Version v: this.committedVersions) {
                if (v.getTime() == Long.MAX_VALUE) {
                    toRemove.add(v);
                    anyRemoved = true;
                }
            };
        this.committedVersions.removeAll(toRemove);
        return anyRemoved;
    }

    @Override
    public final int getAssemblageNid() {
        return assemblageNid;
    }

    /**
     * For constructing an object for the first time.
     *
     * @param primordialUuid A unique external identifier for this chronicle
     * @param assemblageNid The identifier for the concept that defines what
     * assemblage this element is defined within.
     * @param versionType
     */
    protected ChronologyImpl(UUID primordialUuid, int assemblageNid, VersionType versionType) {
        this.primordialUuidMsb = primordialUuid.getMostSignificantBits();
        this.primordialUuidLsb = primordialUuid.getLeastSignificantBits();
        this.nid = Get.identifierService().assignNid(primordialUuid);
        this.assemblageNid = assemblageNid;
        this.versionType = versionType;
        ModelGet.identifierService().setupNid(this.nid, this.assemblageNid, this.getIsaacObjectType(), versionType);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Adds the additional uuids.
     *
     * @param uuids the uuids
     */
    public void addAdditionalUuids(UUID... uuids) {
        if (uuids == null || uuids.length == 0) {
           return;
        }
        final Set<UUID> temp = new HashSet<>(getUuidList());
        int oldSize = temp.size();

        for (UUID uuid : uuids) {
            if (uuid != null) {
               temp.add(uuid);
            }
        }
        if (temp.size() == oldSize) {
           //didn't add anything, noop.
           return;
        }
        
        //Make sure the primordial isn't in the additional
        temp.remove(getPrimordialUuid());
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

        if ((o == null) || (!Objects.equals(getClass(), o.getClass()))) {
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
     * Write a complete binary representation of this chronicle, including all
     * versions, to the ByteArrayDataBuffer using externally valid identifiers
     * (all nids, sequences, replaced with UUIDs).
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
                    if (Get.stampService()
                            .isNotCanceled(version.getStampSequence())) {
                        writeVersion(out, version);
                    }
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
            Get.assemblageService().getSemanticChronologyStreamForComponent(this.getNid()).forEach((semantic) -> {
                builder.append("ATTACHMENT ").append(attachmentCount.incrementAndGet())
                        .append(":\n  ");
                ((SemanticChronologyImpl) semantic).toString(builder, false);
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
        if (version.isUncommitted()) {
            this.uncommittedVersions.add(version);
        } else {
            this.committedVersions.add(version);
        }
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
     * Put additional chronicle fields.
     *
     * @param out the out
     */
    protected abstract void putAdditionalChronicleFields(ByteArrayDataBuffer out);

    /**
     * Reads data from the ByteArrayDataBuffer. If the data is external, it
     * reads all versions from the ByteArrayDataBuffer. If the data is internal,
     * versions are lazily read.
     *
     * @param data the data
     */
    protected void readData(ByteArrayDataBuffer data) {
        if (data.getObjectDataFormatVersion() != getIsaacObjectType().getDataFormatVersion()) {
            throw new UnsupportedOperationException(
                    "Can't handle data format version: " + data.getObjectDataFormatVersion());
        }

        this.primordialUuidMsb = data.getLong();
        this.primordialUuidLsb = data.getLong();
        getAdditionalUuids(data);

        this.assemblageNid = data.getNid();
        
        this.versionType = VersionType.getFromToken(data.getByte());

        if (data.isExternalData()) {
            List<UUID> allUUIDs = getUuidList();
            if (!Get.identifierService().hasUuid(allUUIDs)) {
                this.nid = Get.identifierService().assignNid(allUUIDs.toArray(new UUID[allUUIDs.size()]));
            } else {
                this.nid = Get.identifierService().getNidForUuids(allUUIDs);
                if (allUUIDs.size() > 1) {  //make sure every UUID is in the identifier service
                    for (UUID additionalUuid : allUUIDs) {
                        Get.identifierService().addUuidForNid(additionalUuid, this.nid);
                    }
                }
            }
            setAdditionalChronicleFieldsFromBuffer(data);

        } else {
            this.nid = data.getNid();
            setAdditionalChronicleFieldsFromBuffer(data);
            constructorEnd(data);
        }
        readVersionList(data);
        if (this.committedVersions.isEmpty() && this.uncommittedVersions.isEmpty()) {
           throw new IllegalStateException();
        }
        ModelGet.identifierService().setupNid(this.nid, this.assemblageNid, this.getIsaacObjectType(), this.getVersionType());

    }
    
    @Override
    public VersionType getVersionType() {
        if (this.versionType == null) {
            List<Version> versionList = getVersionList();
            if (!versionList.isEmpty()) {
                this.versionType = versionList.get(0).getSemanticType();
            }
        }
        return this.versionType;
    }

    /**
     * Skip additional chronicle fields.
     *
     * @param in the in
     */
    protected abstract void skipAdditionalChronicleFields(ByteArrayDataBuffer in);

    /**
     * Write only the chronicle data (not the versions) to the
     * ByteArrayDataBuffer using identifiers determined by the
     * ByteArrayDataBuffer.isExternalData() to determine if the identifiers
     * should be nids and sequences, or if they should be UUIDs.
     *
     * @param data the buffer to write to.
     */
    protected void writeChronicleData(ByteArrayDataBuffer data) {
        IsaacObjectType isaacObjectType = getIsaacObjectType();
        isaacObjectType.writeTypeVersionHeader(data);

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
        
        if (this.versionType == null) {
            throw new IllegalStateException();
        }
        
        data.putByte(this.versionType.getVersionTypeToken());

        if (!data.isExternalData()) {
            data.putInt(this.nid);
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
      if (bb.getUsed() == 0) {
          throw new IllegalStateException();
      }
        int nextPosition = bb.getPosition();

        while (nextPosition < bb.getLimit()) {
            final int versionLength = bb.getInt();
            assert versionLength >= 0 : "Length negative: " + versionLength + " buffer: " + bb;

            if (versionLength > 0) {
                nextPosition = nextPosition + versionLength;

                final int stampSequence = bb.getStampSequence();

                if (stampSequence >= 0) {
                    addVersion(makeVersion(stampSequence, bb));
                } else {
                    LOG.warn("read a version length, but no stamp sequence? versionLength {}, stampSequence {}, {}", versionLength, stampSequence, bb);
                }
            } else {
                nextPosition = Integer.MAX_VALUE;
            }
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
    private <V extends StampedVersion> void writeVersion(ByteArrayDataBuffer db,
            V version) {
        final int startWritePosition = db.getPosition();

        db.putInt(0);  // placeholder for length
        ((VersionImpl) version).writeVersionData(db);

        final int versionLength = db.getPosition() - startWritePosition;

        db.setPosition(startWritePosition);
        db.putInt(versionLength);
        db.setPosition(db.getLimit());
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
    public void setAdditionalUuids(Collection<UUID> uuids) {
        this.additionalUuidParts = new long[uuids.size() * 2];

        int i = 0;
        for (final UUID uuid : uuids) {
            this.additionalUuidParts[2 * i] = uuid.getMostSignificantBits();
            this.additionalUuidParts[2 * i + 1] = uuid.getLeastSignificantBits();
            i++;
        }
        Get.identifierService().assignNid(getUuids());
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
     * {@inheritDoc}
     */
    @Override
    public byte[] getChronologyVersionDataToWrite() {

        if (this.uncommittedVersions == null) {

            // creating a brand new object
            final ByteArrayDataBuffer db = new ByteArrayDataBuffer(40);

            writeChronicleData(db);
            db.putInt(0);  // zero length version record.
            db.trimToSize();
            return db.getData();
        }

        final ByteArrayDataBuffer db = new ByteArrayDataBuffer(512);
        writeChronicleData(db);

        this.versionStartPosition = db.getPosition();
        for (Version version : getVersionList()) {
            if (Get.stampService()
                    .isNotCanceled(version.getStampSequence())) {
                writeVersion(db, version);
            }
        }
        db.putInt(0);  // last data is a zero length version record
        db.trimToSize();
        return db.getData();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getChronologyDataToWrite()
    {
        final ByteArrayDataBuffer db = new ByteArrayDataBuffer(40);
        writeChronicleData(db);
        db.flip();
        db.trimToSize();
        return db.getData();
    }
    
    /**
     * {@inheritDoc}
     * 
     * If one were to combine the bytes from {@link #getChronologyDataToWrite()} with the bytes from each buffer returned by 
     * {@link #getVersionDataToWrite()}, one will have almost (but not quite) the same exact content as {@link #getChronologyVersionDataToWrite()} 
     * produces - they only differ in that you would be missing the '0' int which normally trails the version list.   
     * {@link #readData(ByteArrayDataBuffer)} will handle the data being passed in with this int missing.
     */
    @Override
    public List<byte[]> getVersionDataToWrite()
    {
        List<Version> versionList = getVersionList();
        ArrayList<byte[]> result = new ArrayList<>(versionList.size());
        for (Version version : versionList)
        {
            if (Get.stampService().isNotCanceled(version.getStampSequence())) {
                final ByteArrayDataBuffer db = new ByteArrayDataBuffer(64);
                writeVersion(db, version);
                db.flip();
                db.trimToSize();
                result.add(db.getData());
            }
        }
        return result;
    }
    
    /**
     * When a store is utilizing {@link #getChronologyDataToWrite()}, it may find that it needs to 
     * merge two different copies of the ChronologyData.  Use this method to properly merge the 
     * Chronology data into a new data array that contains the information from both.  Note, this only 
     * merges if necessary, if the inputs are identical, it returns copyOne wrapped in a ByteArrayDataBuffer.
     * 
     * Note that, data stores using this mechanism do NOT need to merge version data from 
     * {@link #getVersionDataToWrite()}, as they simply need to store any versions from the 
     * returned list that they do not yet have stored.
     * 
     * This method should only be passed bytes that came from {@link #getChronologyDataToWrite()}
     * @param copyOne 
     * @param copyTwo 
     * @return The result of merging the two byte arrays
     */
    public static byte[] mergeChronologyData(byte[] copyOne, byte[] copyTwo)
    {
        final ByteArrayDataBuffer db = new ByteArrayDataBuffer(Math.max(copyOne.length, copyTwo.length));
        ByteBuffer copyOnebb = ByteBuffer.wrap(copyOne);
        ByteBuffer copyTwobb = ByteBuffer.wrap(copyTwo);
        if (copyOnebb.equals(copyTwobb))
        {
            //Nothing to merge
            db.put(copyOne);
        }
        else
        {
            //sanity check leading and trailing bytes for equality, merge the UUID list bytes.
            //1 byte for object type token, 1 byte for the version type token, 1 byte for the data source type token
            for (int i = 0; i < 3; i++)
            {
                byte aByte = copyOnebb.get();
                if (aByte != copyTwobb.get())
                {
                    throw new RuntimeException("Unmergeable! Bytes differ at " + i);
                }
                else
                {
                    db.putByte(aByte);
                }
            }
            //The UUID list(s) in these chronologies must be different, need to merge.
            ArrayList<UUID> uuidsFromOne = readUUIDs(copyOnebb);
            ArrayList<UUID> uuidsFromTwo = readUUIDs(copyTwobb);
            
            db.putLong(uuidsFromOne.get(0).getMostSignificantBits());  //write the primordial
            db.putLong(uuidsFromOne.get(0).getLeastSignificantBits());
            
            Set<UUID> mergedExtraUUIDs = new HashSet<>();  //merge the other UUIDs
            mergedExtraUUIDs.addAll(uuidsFromOne);
            mergedExtraUUIDs.addAll(uuidsFromTwo);
            mergedExtraUUIDs.remove(uuidsFromOne.get(0));  //remove the primordial we wrote from the extra list
            
            db.putInt(mergedExtraUUIDs.size() * 2);  //Write the number of UUIDs
            for (UUID uuid : mergedExtraUUIDs)
            {
                db.putLong(uuid.getMostSignificantBits());
                db.putLong(uuid.getLeastSignificantBits());
            }
            
            //All remaining bytes should be identical.
            if (copyOnebb.remaining() != copyTwobb.remaining())
            {
                throw new RuntimeException("Unmergeable! Different number of bytes remaining - " + copyOnebb.remaining() + ", " + copyTwobb.remaining());
            }
            while (copyOnebb.hasRemaining())
            {
                byte aByte = copyOnebb.get();
                if (aByte != copyTwobb.get())
                {
                    throw new RuntimeException("Unmergeable! Bytes differ where they should be the same!.  1) " + Arrays.toString(copyOne)
                    + " 2) " + Arrays.toString(copyTwo));
                }
                db.putByte(aByte);
            }
        }
        db.flip();
        db.trimToSize();
        return db.getData();
    }
    

    /**
     * Read the UUID list portion out of the ByteBuffer
     * @param byteBuffer
     * @return
     */
    private static ArrayList<UUID> readUUIDs(ByteBuffer byteBuffer)
    {
        ArrayList<UUID> result = new ArrayList<>();
        //Read the promordial
        result.add(new UUID(byteBuffer.getLong(), byteBuffer.getLong()));
        
        int remaining = byteBuffer.getInt() / 2;
        for (int i = 0; i < remaining; i++)
        {
            result.add(new UUID(byteBuffer.getLong(), byteBuffer.getLong()));
        }
        return result;
    }

    public int getVersionStartPosition() {
        return versionStartPosition;
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

        final int[] latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

        if (latestStampSequences.length == 0) {
            return new LatestVersion<>();
        }

        return new LatestVersion<>(getVersionsForStamps(latestStampSequences));
    }

    @Override
    public <V extends Version> LatestVersion<V> getLatestCommittedVersion(StampCoordinate coordinate) {
        final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);

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
        final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate.getImmutableAllStateAnalog());
        final int[] latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

        for (int stampSequence : latestStampSequences) {
            if (Get.stampService().getStatusForStamp(stampSequence) == Status.ACTIVE) {
                return true;
            }
        }
        return false;
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
     * Gets the semantic list.
     *
     * @return the semantic list
     */
    @Override
    public <V extends SemanticChronology> List<V> getSemanticChronologyList() {
        return Get.assemblageService()
                .<V>getSemanticChronologyStreamForComponent(this.nid)
                .collect(Collectors.toList());
    }

    /**
     * Gets the semantic list from assemblage.
     *
     * @param assemblageSequence the assemblage sequence
     * @return the semantic list from assemblage
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

        if (this.uncommittedVersions != null) {
            results.addAll((Collection<V>) this.uncommittedVersions);
        }

        return results;
    }

    /**
     * {@inheritDoc}
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

        if (Get.stampService().isUncommitted(stampSequence)) {
            for (Version version : this.uncommittedVersions) {
                if (version.getStampSequence() == stampSequence) {
                    return Optional.of((V) version);
                }
            }
            return Optional.empty();
        }
        for (Version version : this.committedVersions) {
            if (version.getStampSequence() == stampSequence) {
                return Optional.of((V) version);
            }
        }
        for (Version version : this.uncommittedVersions) {
            if (version.getStampSequence() == stampSequence) {
                LOG.warn("Returning committed from uncommitted: " + stampSequence + " in: \n" + this);
                return Optional.of((V) version);
            }
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

        ArrayList<Version> versionList = new ArrayList<>(this.uncommittedVersions.size()
                + this.committedVersions.size());
        versionList.addAll(this.uncommittedVersions);
        versionList.addAll(this.committedVersions);
        return (List<V>) versionList;
    }

    public Map<Integer, Version> getStampVersionMap() {
        Map<Integer, Version> result = new HashMap<>();
        getVersionList().forEach((version) -> result.put(version.getStampSequence(), version));
        return result;
    }

    /**
     * Gets the version stamp sequences.
     *
     * @return a stream of the stampSequences for each version of this
     * chronology.
     */
    @Override
    public int[] getVersionStampSequences() {
        final OpenIntHashSet builder = new OpenIntHashSet();

        for (Version v : this.uncommittedVersions) {
            builder.add(v.getStampSequence());
        }
        for (Version v : this.committedVersions) {
            builder.add(v.getStampSequence());
        }
        return builder.keys().elements();
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Overwrites existing versions. Use to remove duplicates, etc. Deliberately
     * not advertised in standard API, as this call may lose audit data.
     *
     * @param <V>
     * @param versions the new versions
     */
    public <V extends Version> void setVersions(Collection<V> versions) {
        this.uncommittedVersions.clear();
        this.committedVersions.clear();
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
            Optional<V> version = getVersionForStamp(stampSequence);
            if (version.isPresent()) {
                versions.add(version.get());
            } else {
                LOG.error("No version for stamp: " + stampSequence + " in: \n" + this);
            }
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

    @Override
    public NidSet getRecursiveSemanticNids() {
        NidSet sequenceSet = Get.assemblageService().getSemanticNidsForComponent(this.getNid());
        sequenceSet.stream().forEach((semanticSequence) -> addRecursiveSequences(sequenceSet, semanticSequence));

        return sequenceSet;
    }

    private void addRecursiveSequences(IntSet semanticSequenceSet, int semanticNid) {
        IntSet sequenceSet = Get.assemblageService().getSemanticNidsForComponent(semanticNid);
        sequenceSet.stream().forEach((sequence) -> {
            semanticSequenceSet.add(sequence);
            addRecursiveSequences(semanticSequenceSet, sequence);
        });

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
    public static List<byte[]> getDataList(ChronologySerializeable chronology) {

        List<byte[]> dataArray = new ArrayList<>();

        byte[] dataToSplit = chronology.getChronologyVersionDataToWrite();
        int versionStartPosition = ((ChronologyImpl)chronology).getVersionStartPosition();
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
    
}
