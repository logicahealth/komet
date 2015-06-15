package org.ihtsdo.otf.tcc.dto.component;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import gov.vha.isaac.ochre.model.ObjectChronologyImpl;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierLong;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;

//~--- JDK imports ------------------------------------------------------------
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import java.util.stream.IntStream;

import javax.xml.bind.annotation.*;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.otf.tcc.dto.ListCompareHelper;

/**
 * Class description
 *
 *
 * @param <R>
 * @param <V>
 *
 * @version Enter version here..., 13/03/27
 * @author Enter your name here...
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TtkComponentChronicle<R extends TtkRevision, V extends StampedVersion> 
    extends TtkRevision
    implements ObjectChronology<V>,  StampedVersion {

    /**
     * Field description
     */
    private static final long serialVersionUID = 1;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "additional-ids")
    @XmlElement(name = "id")
    public List<TtkIdentifier> additionalIds;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "refex")
    public List<TtkRefexAbstractMemberChronicle<?>> annotations;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "annotationsDynamic")
    @XmlElement(name = "refexDynamic")
    public List<TtkRefexDynamicMemberChronicle> annotationsDynamic;

    /**
     * Field description
     */
    @XmlAttribute
    public UUID primordialUuid;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "revisions")
    @XmlElement(name = "revision")
    public List<R> revisions;

    /**
     * Constructs ...
     *
     */
    public TtkComponentChronicle() {
        super();
    }

    /**
     * Constructs ...
     *
     *
     * @param another
     *
     * @throws IOException
     */
    public TtkComponentChronicle(ComponentVersionBI another) throws IOException {
        super(another);

        Collection<? extends IdBI> anotherAdditionalIds = another.getAdditionalIds();

        if (anotherAdditionalIds != null) {
            this.additionalIds = new ArrayList<>(anotherAdditionalIds.size());
            for (IdBI id : anotherAdditionalIds) {
                this.additionalIds.add((TtkIdentifier) TtkIdentifier.convertId(id));
            }
        }

        processAnnotations(another.getAnnotations());
        processDynamicAnnotations(another.getRefexDynamicAnnotations());
        this.primordialUuid = another.getPrimordialUuid();
    }

    public TtkComponentChronicle(ObjectChronologyImpl<?> another) {
        super(another.getVersionList().get(0));
        try {
            List<UUID> allUuids = another.getUuidList();
            if (allUuids.size() > 1) {
                this.additionalIds = new ArrayList<>(allUuids.size() - 1);
                for (int i = 1; i < allUuids.size(); i++) {
                    this.additionalIds.add(new TtkIdentifierUuid(allUuids.get(i)));
                }
            }
            
            processAnnotations(null);
            processDynamicAnnotations(null);
            this.primordialUuid = another.getPrimordialUuid();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isLatestVersionActive(StampCoordinate coordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);
        StampSequenceSet latestStampSequences = calc.getLatestStampSequences(this.getVersionStampSequences());
        return !latestStampSequences.isEmpty();
    }

    /**
     * Constructs ...
     *
     *
     * @param in
     * @param dataVersion
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public TtkComponentChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public void addStamps(Collection<TtkStamp> stamps) {
        stamps.add(this.getStamp());
        if (revisions != null) {
            for (TtkRevision revision : revisions) {
                stamps.add(revision.getStamp());
            }
        }
    }

    /**
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a {@code EComponent}
     * object, and contains the same values, field by field, as this
     * {@code EComponent}.
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

        if (TtkComponentChronicle.class.isAssignableFrom(obj.getClass())) {
            TtkComponentChronicle<?, ?> another = (TtkComponentChronicle<?, ?>) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare primordialComponentUuid
            if (!this.primordialUuid.equals(another.primordialUuid)) {
                return false;
            }

            // Compare additionalIdComponents
            // TODO have this altenative list use the annotations for comparison. 
            if (!testIdLists(this, another)) {
                return false;
            }

            // Compare extraVersions
            if (!ListCompareHelper.equals(this.revisions, another.revisions)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    private boolean testIdLists(TtkComponentChronicle<?,?> thisComponent, TtkComponentChronicle<?,?> anotherComponent) {
        if (thisComponent.additionalIds == null) {
            thisComponent.additionalIds = new ArrayList<>();
        }

        if (anotherComponent.additionalIds == null) {
            anotherComponent.additionalIds = new ArrayList<>();
        }
        List<TtkIdentifier> thisAlternateList = removeNonUuidIdentifiers(thisComponent.additionalIds);
        List<TtkIdentifier> anotherAlternateList = removeNonUuidIdentifiers(anotherComponent.additionalIds);
        return ListCompareHelper.equals(thisAlternateList, anotherAlternateList);
    }

    private List<TtkIdentifier> removeNonUuidIdentifiers(List<TtkIdentifier> additionalIds) {
        List<TtkIdentifier> thisAlternateList = new ArrayList<>(additionalIds);
        for (Iterator<TtkIdentifier> iterator = thisAlternateList.iterator(); iterator.hasNext();) {
            TtkIdentifier ttkId = iterator.next();
            if (ttkId.getIdType() != IDENTIFIER_PART_TYPES.UUID) {
                iterator.remove();
            }
        }
        return thisAlternateList;
    }

    /**
     * Returns a hash code for this {@code EComponent}.
     *
     * @return a hash code value for this {@code EComponent}.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{getPrimordialComponentUuid().hashCode(), status.hashCode(),
            pathUuid.hashCode(), (int) time, (int) (time >>> 32)});
    }

    /**
     * Method description
     *
     *
     * @param annotations
     *
     * @throws IOException
     */
    private void processAnnotations(Collection<? extends RefexChronicleBI<?>> annotations) throws IOException {
        if ((annotations != null) && !annotations.isEmpty()) {
            this.annotations = new ArrayList<>(annotations.size());

            for (RefexChronicleBI<?> r : annotations) {
                this.annotations.add(TtkConceptChronicle.convertRefex(r));
            }
        }
    }

    private void processDynamicAnnotations(Collection<? extends RefexDynamicChronicleBI<?>> annotationsDynamic) throws IOException {
        if ((annotationsDynamic != null) && !annotationsDynamic.isEmpty()) {
            this.annotationsDynamic = new ArrayList<>(annotationsDynamic.size());

            for (RefexDynamicChronicleBI<?> r : annotationsDynamic) {
                this.annotationsDynamic.add(TtkConceptChronicle.convertRefex(r));
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param in
     * @param dataVersion
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        primordialUuid = new UUID(in.readLong(), in.readLong());

        short idVersionCount = in.readShort();

        assert idVersionCount < 500 : "idVersionCount is: " + idVersionCount;

        if (idVersionCount > 0) {
            additionalIds = new ArrayList<>(idVersionCount);

            for (int i = 0; i < idVersionCount; i++) {
                switch (IDENTIFIER_PART_TYPES.readType(in)) {
                    case LONG:
                        additionalIds.add(new TtkIdentifierLong(in, dataVersion));

                        break;

                    case STRING:
                        additionalIds.add(new TtkIdentifierString(in, dataVersion));

                        break;

                    case UUID:
                        additionalIds.add(new TtkIdentifierUuid(in, dataVersion));

                        break;

                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }

        short annotationCount = in.readShort();

        assert annotationCount < 5000 : "annotation count is: " + annotationCount;

        if (annotationCount > 0) {
            annotations = new ArrayList<>(annotationCount);

            for (int i = 0; i < annotationCount; i++) {
                RefexType type = RefexType.readType(in);

                switch (type) {
                    case CID:
                        annotations.add(new TtkRefexUuidMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID:
                        annotations.add(new TtkRefexUuidUuidMemberChronicle(in, dataVersion));

                        break;

                    case MEMBER:
                        annotations.add(new TtkRefexMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID:
                        annotations.add(new TtkRefexUuidUuidUuidMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_STR:
                        annotations.add(new TtkRefexUuidUuidStringMemberChronicle(in, dataVersion));

                        break;

                    case INT:
                        annotations.add(new TtkRefexIntMemberChronicle(in, dataVersion));

                        break;

                    case STR:
                        annotations.add(new TtkRefexStringMemberChronicle(in, dataVersion));

                        break;

                    case CID_INT:
                        annotations.add(new TtkRefexUuidIntMemberChronicle(in, dataVersion));

                        break;

                    case BOOLEAN:
                        annotations.add(new TtkRefexBooleanMemberChronicle(in, dataVersion));

                        break;

                    case CID_FLOAT:
                        annotations.add(new TtkRefexUuidFloatMemberChronicle(in, dataVersion));

                        break;

                    case CID_LONG:
                        annotations.add(new TtkRefexUuidLongMemberChronicle(in, dataVersion));

                        break;

                    case CID_STR:
                        annotations.add(new TtkRefexUuidStringMemberChronicle(in, dataVersion));

                        break;

                    case LONG:
                        annotations.add(new TtkRefexLongMemberChronicle(in, dataVersion));

                        break;

                    case ARRAY_BYTEARRAY:
                        annotations.add(new TtkRefexArrayOfByteArrayMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_FLOAT:
                        annotations.add(new TtkRefexUuidUuidUuidFloatMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_INT:
                        annotations.add(new TtkRefexUuidUuidUuidIntMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_LONG:
                        annotations.add(new TtkRefexUuidUuidUuidLongMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_STRING:
                        annotations.add(new TtkRefexUuidUuidUuidStringMemberChronicle(in, dataVersion));

                        break;

                    case CID_BOOLEAN:
                        annotations.add(new TtkRefexUuidBooleanMemberChronicle(in, dataVersion));

                        break;

                    default:
                        throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }

            }
        }

        if (dataVersion >= 11) {
            short annotationsDynamicCount = in.readShort();
            if (annotationsDynamicCount > 0) {
                annotationsDynamic = new ArrayList<>(annotationsDynamicCount);

                for (int i = 0; i < annotationsDynamicCount; i++) {
                    annotationsDynamic.add(new TtkRefexDynamicMemberChronicle(in, dataVersion));
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return
     */
    @Override
    public String toString() {
        int depth = 1;

        if (this instanceof TtkRefexAbstractMemberChronicle) {
            depth = 2;
        }

        StringBuilder buff = new StringBuilder();

        buff.append(" primordial:");
        buff.append(this.primordialUuid);
        buff.append(" xtraIds:");
        buff.append(this.additionalIds);
        buff.append(super.toString());

        if ((annotations != null) && (annotations.size() > 0)) {
            buff.append("\n" + TtkConceptChronicle.PADDING);

            for (int i = 0; i < depth; i++) {
                buff.append(TtkConceptChronicle.PADDING);
            }

            buff.append("annotations:\n");

            for (TtkRefexAbstractMemberChronicle<?> m : this.annotations) {
                buff.append(TtkConceptChronicle.PADDING);
                buff.append(TtkConceptChronicle.PADDING);

                for (int i = 0; i < depth; i++) {
                    buff.append(TtkConceptChronicle.PADDING);
                }

                buff.append(m);
                buff.append("\n");
            }
        }
        if ((annotationsDynamic != null) && (annotationsDynamic.size() > 0)) {
            buff.append("\n" + TtkConceptChronicle.PADDING);

            for (int i = 0; i < depth; i++) {
                buff.append(TtkConceptChronicle.PADDING);
            }
            buff.append("annotations dynamic:\n");

            for (TtkRefexDynamicMemberChronicle m : this.annotationsDynamic) {
                buff.append(TtkConceptChronicle.PADDING);
                buff.append(TtkConceptChronicle.PADDING);

                for (int i = 0; i < depth; i++) {
                    buff.append(TtkConceptChronicle.PADDING);
                }

                buff.append(m);
                buff.append("\n");
            }
        }

        if ((revisions != null) && (revisions.size() > 0)) {
            buff.append("\n" + TtkConceptChronicle.PADDING + "revisions:\n");

            for (TtkRevision r : this.revisions) {
                buff.append(TtkConceptChronicle.PADDING);
                buff.append(TtkConceptChronicle.PADDING);

                for (int i = 0; i < depth; i++) {
                    buff.append(TtkConceptChronicle.PADDING);
                }

                buff.append(r);
                buff.append("\n");
            }
        }

        return buff.toString();
    }

    /**
     * Method description
     *
     *
     * @param out
     *
     * @throws IOException
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(primordialUuid.getMostSignificantBits());
        out.writeLong(primordialUuid.getLeastSignificantBits());

        if (additionalIds == null) {
            out.writeShort(0);
        } else {
            assert additionalIds.size() < 500 : "additionalIds is: " + additionalIds.size();
            out.writeShort(additionalIds.size());

            for (TtkIdentifier idv : additionalIds) {
                idv.getIdType().writeType(out);
                idv.writeExternal(out);
            }
        }

        if (annotations == null) {
            out.writeShort(0);
        } else {
            assert annotations.size() < 500 : "annotation count is: " + annotations.size();
            out.writeShort(annotations.size());

            for (TtkRefexAbstractMemberChronicle<?> r : annotations) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }
        if (annotationsDynamic == null) {
            out.writeShort(0);
        } else {
            out.writeShort(annotationsDynamic.size());

            for (TtkRefexDynamicMemberChronicle r : annotationsDynamic) {
                r.writeExternal(out);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkIdentifier> getAdditionalIdComponents() {
        if (additionalIds == null) {
            additionalIds = new ArrayList<>();
        }
        return additionalIds;
    }

    @Override
    public Collection<UUID> getUuidReferences() {
        HashSet<UUID> uuidReferences = new HashSet<>();
        uuidReferences.add(this.authorUuid);
        uuidReferences.add(this.moduleUuid);
        uuidReferences.add(this.pathUuid);
        addUuidReferencesForRevision(uuidReferences);
        if (revisions != null) {
            for (TtkRevision r : revisions) {
                r.addUuidReferencesForRevision(uuidReferences);
            }
        }
        return uuidReferences;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkRefexAbstractMemberChronicle<?>> getAnnotations() {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        return annotations;
    }

    public List<TtkRefexDynamicMemberChronicle> getAnnotationsDynamic() {
        return annotationsDynamic;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkIdentifier> getEIdentifiers() {
        List<TtkIdentifier> ids;

        if (additionalIds != null) {
            ids = new ArrayList<>(additionalIds.size() + 1);
            ids.addAll(additionalIds);
        } else {
            ids = new ArrayList<>(1);
        }

        ids.add(new TtkIdentifierUuid(this.primordialUuid));

        return ids;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public int getIdComponentCount() {
        if (additionalIds == null) {
            return 1;
        }

        return additionalIds.size() + 1;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public UUID getPrimordialComponentUuid() {
        return primordialUuid;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public abstract List<? extends TtkRevision> getRevisionList();

    /**
     * Method description
     *
     *
     * @return
     */
    public List<R> getRevisions() {
        return revisions;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<UUID> getUuids() {
        List<UUID> uuids;

        if (additionalIds != null) {
            uuids = new ArrayList<>(additionalIds.size() + 1);
        } else {
            uuids = new ArrayList<>(1);
        }

        uuids.add(primordialUuid);

        if (additionalIds != null) {
            for (TtkIdentifier idv : additionalIds) {
                if (TtkIdentifierUuid.class.isAssignableFrom(idv.getClass())) {
                    uuids.add((UUID) idv.getDenotation());
                }
            }
        }

        return uuids;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public int getVersionCount() {
        List<? extends TtkRevision> extraVersions = getRevisionList();

        if (extraVersions == null) {
            return 1;
        }

        return extraVersions.size() + 1;
    }

    /**
     * Method description
     *
     *
     * @param additionalIdComponents
     */
    public void setAdditionalIdComponents(List<TtkIdentifier> additionalIdComponents) {
        this.additionalIds = additionalIdComponents;
    }

    /**
     * Method description
     *
     *
     * @param annotations
     */
    public void setAnnotations(List<TtkRefexAbstractMemberChronicle<?>> annotations) {
        this.annotations = annotations;
    }

    public void setAnnotationsDynamic(List<TtkRefexDynamicMemberChronicle> annotationsDynamic) {
        this.annotationsDynamic = annotationsDynamic;
    }

    /**
     * Method description
     *
     *
     * @param primordialComponentUuid
     */
    public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
        this.primordialUuid = primordialComponentUuid;
    }

    /**
     * Method description
     *
     *
     * @param revisions
     */
    public void setRevisions(List<R> revisions) {
        this.revisions = revisions;
    }

    @Override
    public Optional<LatestVersion<V>> getLatestVersion(Class<V> type, StampCoordinate coordinate) {
        return RelativePositionCalculator.getCalculator(coordinate).getLatestVersion(this);
    }

    @Override
    public List<? extends V> getVersionList() {
        ArrayList versionList = new ArrayList();
        versionList.add(this);
        if (revisions != null) {
            revisions.stream().forEach((revision) -> versionList.add(revision));
        }
        return versionList;
    }

    @Override
    public IntStream getVersionStampSequences() {
        IntStream.Builder builder = IntStream.builder();
        builder.accept(getStampSequence());
        if (revisions != null) {
            revisions.stream().forEach((revision)-> builder.accept(revision.getStampSequence()));
        }
        return builder.build();
    }

    @Override
    public List<? extends SememeChronology<? extends SememeVersion>> getSememeList() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public List<? extends SememeChronology<? extends SememeVersion>> getSememeListFromAssemblage(int assemblageSequence) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <SV extends SememeVersion> List<? extends SememeChronology<SV>> getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public int getNid() {
        return getIdService().getNidForUuids(getPrimordialComponentUuid());
    }

    @Override
    public UUID getPrimordialUuid() {
        return primordialUuid;
    }

    @Override
    public List<UUID> getUuidList() {
        return getUuids();
    }

    @Override
    public CommitStates getCommitState() {
        if (getVersionList().stream().anyMatch((version) -> version.getTime() == Long.MAX_VALUE)) {
            return CommitStates.UNCOMMITTED;
        }
        return CommitStates.COMMITTED;
    }
    
    
}
