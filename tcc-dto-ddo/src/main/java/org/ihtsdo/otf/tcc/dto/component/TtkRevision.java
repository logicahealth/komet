package org.ihtsdo.otf.tcc.dto.component;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.IdentifiedObjectService;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptServiceManagerI;
import gov.vha.isaac.ochre.collections.LruCache;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ExternalStampBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;

//~--- JDK imports ------------------------------------------------------------
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TtkRevision implements ExternalStampBI, StampedVersion {

    private static final long serialVersionUID = 1;

    private static IdentifierService idService = null;

    protected static IdentifierService getIdService() {
        if (idService == null) {
            idService = LookupService.getService(IdentifierService.class);
        }
        return idService;
    }

    private static CommitService commitService;

    protected static CommitService getCommitService() {
        if (commitService == null) {
            commitService = LookupService.getService(CommitService.class);
        }
        return commitService;
    }

    private static ConceptService conceptService;

    protected static ConceptService getConceptService() {
        if (conceptService == null) {
            conceptService = LookupService.getService(ConceptServiceManagerI.class).get();
        }
        return conceptService;
    }

    private static IdentifiedObjectService identifiedObjectService;

    protected static IdentifiedObjectService getIdentifiedObjectService() {
        if (identifiedObjectService == null) {
            identifiedObjectService = LookupService.getService(IdentifiedObjectService.class);
        }
        return identifiedObjectService;
    }

    @XmlAttribute
    public long time = Long.MIN_VALUE;
    @XmlAttribute
    public UUID authorUuid;
    @XmlAttribute
    public UUID pathUuid;
    @XmlAttribute
    public Status status;
    @XmlAttribute
    public UUID moduleUuid;

    public TtkRevision() {
        super();
    }

    public TtkRevision(StampedVersion another) {
        super();
        this.status = Status.getStatusFromState(another.getState());
        this.authorUuid = getIdService().getUuidPrimordialFromConceptSequence(another.getAuthorSequence()).get();
        this.pathUuid = getIdService().getUuidPrimordialFromConceptSequence(another.getPathSequence()).get();
        this.moduleUuid = getIdService().getUuidPrimordialFromConceptSequence(another.getModuleSequence()).get();
        assert pathUuid != null : another;
        assert authorUuid != null : another;
        assert status != null : another;
        assert moduleUuid != null : another;
        this.time = another.getTime();
    }

    public TtkRevision(ComponentVersionBI another) throws IOException {
        super();
        this.status = another.getStatus();
        this.authorUuid = Ts.get().getUuidPrimordialForNid(another.getAuthorNid());
        this.pathUuid = Ts.get().getUuidPrimordialForNid(another.getPathNid());
        this.moduleUuid = Ts.get().getUuidPrimordialForNid(another.getModuleNid());
        assert pathUuid != null : another;
        assert authorUuid != null : another;
        assert status != null : another;
        assert moduleUuid != null : another;
        this.time = another.getTime();
    }

    public TtkRevision(IdBI id) throws IOException {
        super();
        this.authorUuid = Ts.get().getComponent(id.getAuthorNid()).getPrimordialUuid();
        this.pathUuid = Ts.get().getComponent(id.getPathNid()).getPrimordialUuid();
        this.status = id.getStatus();
        this.moduleUuid = Ts.get().getComponent(id.getModuleNid()).getPrimordialUuid();
        this.time = id.getTime();
        assert pathUuid != null : id;
        assert authorUuid != null : id;
        assert status != null : id;
        assert moduleUuid != null : id;
    }

    public TtkRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
        assert pathUuid != null : this;
        assert authorUuid != null : this;
        assert status != null : this;
        assert moduleUuid != null : this;
    }

    public TtkStamp getStamp() {
        return new TtkStamp(status, time, authorUuid, moduleUuid, pathUuid);
    }

    @Override
    public int getStampSequence() {
        return getCommitService().getStampSequence(getState(), time, getAuthorSequence(),
                getModuleSequence(), getPathSequence());
    }

    @Override
    public State getState() {
        return status.getState();
    }

    @Override
    public int getAuthorSequence() {
        return getIdService().getConceptSequenceForUuids(authorUuid);
    }

    private static ThreadLocal<LinkedHashMap<UUID, Integer>> moduleSequenceCache =
            new ThreadLocal() {
                @Override
                protected LruCache<UUID, Integer> initialValue() {
                    return new LruCache<>(13);
                }
            };

    @Override
    public int getModuleSequence() {
        Integer pathSequence = moduleSequenceCache.get().get(moduleUuid);
        if (pathSequence != null) {
            return pathSequence;
        }

        int intPathSequence = getIdService().getConceptSequenceForUuids(moduleUuid);
        moduleSequenceCache.get().put(moduleUuid, intPathSequence);
        return intPathSequence;
    }

    private static ThreadLocal<LinkedHashMap<UUID, Integer>> pathSequenceCache =
            new ThreadLocal() {
                @Override
                protected LruCache<UUID, Integer> initialValue() {
                    return new LruCache<>(7);
                }
            };

    @Override
    public int getPathSequence() {
        Integer pathSequence = pathSequenceCache.get().get(pathUuid);
        if (pathSequence != null) {
            return pathSequence;
        }

        int intPathSequence = getIdService().getConceptSequenceForUuids(pathUuid);
        pathSequenceCache.get().put(pathUuid, intPathSequence);
        return intPathSequence;
    }

    public Collection<UUID> getUuidReferences() {
        HashSet<UUID> uuidReferences = new HashSet<>();
        addUuidReferencesForRevision(uuidReferences);
        return uuidReferences;
    }

    protected final void addUuidReferencesForRevision(Collection<UUID> references) {
        references.add(this.authorUuid);
        references.add(this.pathUuid);
        references.add(this.moduleUuid);
        addUuidReferencesForRevisionComponent(references);
    }

    protected abstract void addUuidReferencesForRevisionComponent(Collection<UUID> references);

    /**
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a {@code EVersion}
     * object, and contains the same values, field by field, as this
     * {@code EVersion}.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are the same; {@code false}
     * otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TtkRevision.class.isAssignableFrom(obj.getClass())) {
            TtkRevision another = (TtkRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            if (this.status != another.status) {
                return false;
            }

            if ((this.authorUuid != null) && (another.authorUuid != null)) {
                if (!this.authorUuid.equals(another.authorUuid)) {
                    return false;
                }
            } else if (!((this.authorUuid == null) && (another.authorUuid == null))) {
                return false;
            }

            if (!this.pathUuid.equals(another.pathUuid)) {
                return false;
            }

            if ((this.moduleUuid != null) && (another.moduleUuid != null)) {
                if (!this.moduleUuid.equals(another.moduleUuid)) {
                    return false;
                }
            } else if (!((this.moduleUuid == null) && (another.moduleUuid == null))) {
                return false;
            }

            if (this.time != another.time) {
                return false;
            }

            // Objects are equal! (Don't climb any higher in the hierarchy)
            return true;
        }

        return false;
    }

    /**
     * Returns a hash code for this {@code EVersion}.
     *
     * @return a hash code value for this {@code EVersion}.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{status.hashCode(), pathUuid.hashCode(), (int) time,
            (int) (time >>> 32)});
    }

    public static CharSequence informAboutUuid(UUID uuid) {
        if (uuid == null) {
            return "NULL UUID";
        }
        if (getConceptService() == null) {
            return uuid.toString();
        }

        StringBuilder sb = new StringBuilder();

        if (getIdService().hasUuid(uuid)) {
            int nid = getIdService().getNidForUuids(uuid);
            int conceptSequence = getIdService().getConceptSequenceForComponentNid(nid);
            if (conceptSequence != Integer.MAX_VALUE) {
                if (getIdService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
                    ConceptChronology<? extends StampedVersion> cc
                            = getConceptService().getConcept(conceptSequence);

                    sb.append("'");
                    sb.append(cc.toUserString());
                    sb.append("' ");
                    sb.append(conceptSequence);
                    sb.append(" ");
                } else {
                    Optional<? extends ObjectChronology<? extends StampedVersion>> component = 
                            getIdentifiedObjectService().getIdentifiedObjectChronology(nid);

                    if (component.isPresent()) {
                        sb.append("' ");
                        sb.append(component.get().toUserString());
                    } else {
                        sb.append("'null");
                    }

                    sb.append("' ");
                    sb.append(nid);
                    sb.append(" ");
                }
            } else {
                sb.append(uuid.toString());
                sb.append(" nid: ");
                sb.append(nid);
                sb.append(" conceptSequence: ");
                sb.append(conceptSequence);
                sb.append(" ");
            }
        }

        sb.append(uuid.toString());

        return sb;
    }

    private static final String ACTIVE = "d12702ee-c37f-385f-a070-61d56d4d0f1f";
    private static final String INACTIVE = "a5daba09-7feb-37f0-8d6d-c3cadfc7f724";

    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        pathUuid = new UUID(in.readLong(), in.readLong());
        if (dataVersion > 9) {
            boolean active = in.readBoolean();
            if (active) {
                status = Status.ACTIVE;
            } else {
                status = Status.INACTIVE;
            }

        } else {
            UUID statusUuid = new UUID(in.readLong(), in.readLong());
            switch (statusUuid.toString()) {
                case ACTIVE:
                    status = Status.ACTIVE;
                    break;
                case INACTIVE:
                    status = Status.INACTIVE;
                    break;
                default:
                    throw new UnsupportedOperationException("DataVersion: " + dataVersion
                            + " status uuid:" + statusUuid);
            }

        }

        if (dataVersion >= 3) {
            authorUuid = new UUID(in.readLong(), in.readLong());
        } else {
            throw new UnsupportedOperationException();
        }

        if (dataVersion >= 8) {
            moduleUuid = new UUID(in.readLong(), in.readLong());
        } else {
            throw new UnsupportedOperationException();
        }

        time = in.readLong();

        if (time == Long.MAX_VALUE) {
            time = Long.MIN_VALUE;
        }
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(" s:");
        buff.append(this.status);
        buff.append(" t: ");
        buff.append(new Date(this.time)).append(" ").append(this.time);
        buff.append(" a:");
        buff.append(informAboutUuid(this.authorUuid));
        buff.append(" m:");
        buff.append(informAboutUuid(this.moduleUuid));
        buff.append(" p:");
        buff.append(informAboutUuid(this.pathUuid));

        return buff.toString();
    }

    public void writeExternal(DataOutput out) throws IOException {
        if (time == Long.MAX_VALUE) {
            time = Long.MIN_VALUE;
        }

        assert pathUuid != null : this;
        assert authorUuid != null : this;
        assert status != null : this;
        assert moduleUuid != null : this;
        out.writeLong(pathUuid.getMostSignificantBits());
        out.writeLong(pathUuid.getLeastSignificantBits());
        out.writeBoolean(status == Status.ACTIVE);

        out.writeLong(authorUuid.getMostSignificantBits());
        out.writeLong(authorUuid.getLeastSignificantBits());

        out.writeLong(moduleUuid.getMostSignificantBits());
        out.writeLong(moduleUuid.getLeastSignificantBits());
        out.writeLong(time);
    }

    @Override
    public UUID getAuthorUuid() {
        return authorUuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_VersionExternal#getPathUuid()
     */
    @Override
    public UUID getPathUuid() {
        return pathUuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_VersionExternal#getStatusUuid()
     */
    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public UUID getModuleUuid() {
        return moduleUuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_VersionExternal#getTime()
     */
    @Override
    public long getTime() {
        return time;
    }

    public void setAuthorUuid(UUID authorUuid) {
        this.authorUuid = authorUuid;
    }

    public void setPathUuid(UUID pathUuid) {
        this.pathUuid = pathUuid;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setModuleUuid(UUID moduleUuid) {
        this.moduleUuid = moduleUuid;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
