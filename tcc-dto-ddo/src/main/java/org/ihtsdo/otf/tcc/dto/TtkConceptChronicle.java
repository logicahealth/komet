package org.ihtsdo.otf.tcc.dto;

//~--- JDK imports ------------------------------------------------------------
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.IntStream.Builder;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.dto.component.TtkChronicleProcessor;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.TtkRevisionProcessorBI;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphMemberChronicle;
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
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.chronicle.ChronicledObjectUniversal;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;

/**
 * Class description
 *
 *
 * @version Enter version here..., 13/03/27
 * @author Enter your name here...
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "concept")
public class TtkConceptChronicle implements ChronicledObjectUniversal {

    /**
     * Field description
     */
    public static final String PADDING = "     ";

    /**
     * Field description
     */
    public static final int dataVersion = 11;

    /**
     * Field description
     */
    public static final long serialVersionUID = 1;

    /**
     * Field description
     */
    @XmlAttribute
    protected boolean annotationStyleRefex = false;

    /**
     * Field description
     */
    @XmlAttribute
    @Deprecated
    protected boolean annotationIndexStyleRefex = false;

    /**
     * Field description
     */
    protected TtkConceptAttributesChronicle conceptAttributes;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "description-collection")
    @XmlElement(name = "description")
    protected List<TtkDescriptionChronicle> descriptions;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "media-collection")
    @XmlElement(name = "media")
    protected List<TtkMediaChronicle> media;

    /**
     * Field description
     */
    @XmlAttribute
    protected UUID primordialUuid;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "refex-member-collection")
    @XmlElement(name = "refex")
    protected List<TtkRefexAbstractMemberChronicle<?>> refsetMembers;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "refex-member-dynamic-collection")
    @XmlElement(name = "refexDynamic")
    protected List<TtkRefexDynamicMemberChronicle> refsetMembersDynamic;

    /**
     * Field description
     */
    @XmlElementWrapper(name = "relationship-collection")
    @XmlElement(name = "relationship")
    protected List<TtkRelationshipChronicle> relationships;

    /**
     * Constructs ...
     *
     */
    public TtkConceptChronicle() {
        super();
    }

    /**
     * Constructs ...
     *
     *
     * @param c
     *
     * @throws IOException
     */
    public TtkConceptChronicle(ConceptChronicleBI c) throws IOException {
        annotationStyleRefex = c.isAnnotationStyleRefex();
        annotationIndexStyleRefex = false;
        conceptAttributes = new TtkConceptAttributesChronicle(c.getConceptAttributes());
        primordialUuid = conceptAttributes.primordialUuid;
        relationships = new ArrayList<>(c.getRelationshipsOutgoing().size());

        for (RelationshipChronicleBI rel : c.getRelationshipsOutgoing()) {
            relationships.add(new TtkRelationshipChronicle(rel));
        }

        descriptions = new ArrayList<>(c.getDescriptions().size());

        for (DescriptionChronicleBI desc : c.getDescriptions()) {
            descriptions.add(new TtkDescriptionChronicle(desc));
        }

        media = new ArrayList<>(c.getMedia().size());

        for (MediaChronicleBI mediaChronicle : c.getMedia()) {
            TtkMediaChronicle tkMedia = new TtkMediaChronicle(mediaChronicle);

            media.add(tkMedia);
        }
//TODO need seperate change set entries for sememes
//        c.getSememeChronicles().forEach((SememeChronology sc) -> {
//            if (refsetMembers == null) {
//                refsetMembers = new ArrayList<>();
//            }
//            switch (sc.getSememeType()) {
//                case LOGIC_GRAPH:
//                    TtkLogicGraphMemberChronicle lgmc = new TtkLogicGraphMemberChronicle((SememeChroniclogyImpl<LogicGraphSememeImpl>) sc);
//                    refsetMembers.add(lgmc);
//                    break;
//                case STRING:
//                    TtkRefexStringMemberChronicle smc = new TtkRefexStringMemberChronicle((SememeChroniclogyImpl<StringSememeImpl>) sc);
//                    refsetMembers.add(smc);
//                    break;
//                default:
//                    throw new UnsupportedOperationException(sc.getSememeType() + " not supported yet.");
//            }
//        });

        if (!c.isAnnotationStyleRefex()) {
            Collection<? extends RefexChronicleBI> members = c.getRefsetMembers();

            if (members != null) {
                if (refsetMembers == null) {
                    refsetMembers = new ArrayList<>(members.size());
                }

                for (RefexChronicleBI<?> m : members) {
                    TtkRefexAbstractMemberChronicle<?> member = convertRefex(m);

                    if (member != null) {
                        refsetMembers.add(member);
                    } else {
                        throw new IOException("Could not convert refset member: " + m + "\nfrom refset: " + c);
                    }
                }
            }
        }
    }

    /**
     * Constructs ...
     *
     *
     * @param in
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public TtkConceptChronicle(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public IntStream getStampSequenceStream() {
        Builder builder = IntStream.builder();
        processComponentChronicles((TtkComponentChronicle<?, ?> cc) -> {
            if (cc != null) {
                builder.accept(cc.getStampSequence());
                if (cc.revisions != null) {
                    cc.revisions.forEach((revision) -> builder.accept(revision.getStampSequence()));
                }
            }
        });
        
        return builder.build();
    }

    public void processComponentChronicles(TtkChronicleProcessor processor) {
        processChronicle(this.conceptAttributes, processor);
        processChronicle(this.descriptions, processor);
        processChronicle(this.relationships, processor);
        processChronicle(this.media, processor);
        processChronicle(this.refsetMembers, processor);
        processChronicle(this.refsetMembersDynamic, processor);
    }

    private void processChronicle(Collection<? extends TtkComponentChronicle> chronicleCollection, TtkChronicleProcessor processor) {
        if (chronicleCollection != null) {
            for (TtkComponentChronicle component : chronicleCollection) {
                processChronicle(component, processor);
            }
        }
    }

    private void processChronicle(TtkComponentChronicle chronicle, TtkChronicleProcessor processor) {
        if (chronicle != null) {
            processor.process(chronicle);
            processChronicle(chronicle.getAnnotations(), processor);
            processChronicle(chronicle.getAnnotationsDynamic(), processor);
        }
    }

    @Override
    public boolean isUncommitted() {
        UncommittedTestProcessor uncommittedTestProcessor = new UncommittedTestProcessor();
        processComponentRevisions(uncommittedTestProcessor);
        return uncommittedTestProcessor.uncommitted;
    }

    @Override
    public CommitStates getCommitState() {
        if (isUncommitted()) {
            return CommitStates.UNCOMMITTED;
        }
        return CommitStates.COMMITTED;
    }
    
    private static class UncommittedTestProcessor implements TtkRevisionProcessorBI {
        boolean uncommitted = false;
        @Override
        public void process(TtkRevision r) {
            if (r.time == Long.MAX_VALUE) {
                uncommitted = true;
            }
        }
        
    }

    public void processComponentRevisions(TtkRevisionProcessorBI processor) {
        processChronicleRevisions(this.conceptAttributes, processor);
        processChronicleRevisions(this.descriptions, processor);
        processChronicleRevisions(this.relationships, processor);
        processChronicleRevisions(this.media, processor);
        processChronicleRevisions(this.refsetMembers, processor);
        processChronicleRevisions(this.refsetMembersDynamic, processor);
    }

    private void processChronicleRevisions(TtkComponentChronicle<?, ?> cc,
            TtkRevisionProcessorBI processor) {
        if (cc != null) {
            processor.process(cc);
            if (cc.revisions != null) {
                cc.revisions.forEach(processor::process);
            }
            if (cc.annotations != null) {
                processChronicleRevisions(cc.annotations, processor);
            }
            if (cc.additionalIds != null) {
                cc.additionalIds.forEach(processor::process);
            }
        }
    }

    private void processChronicleRevisions(List<? extends TtkComponentChronicle<?, ?>> componentList,
            TtkRevisionProcessorBI processor) {
        if (componentList != null) {
            for (TtkComponentChronicle<?, ?> cc : componentList) {
                processChronicleRevisions(cc, processor);
            }
        }
    }

    public static TtkComponentChronicle<?, ?> convertSememeChronicle(SememeChronology<?> sc) {
        switch (sc.getSememeType()) {
            case LOGIC_GRAPH:
            {
                return new TtkLogicGraphMemberChronicle((SememeChronologyImpl<LogicGraphSememeImpl>) sc);
            }
            case DYNAMIC:
                return new TtkRefexDynamicMemberChronicle((SememeChronologyImpl<DynamicSememeImpl>) sc);
            case COMPONENT_NID:
            case LONG:
            case MEMBER:
            default:
                throw new UnsupportedOperationException("Can't handle: " + sc.getSememeType());
        }

    }

    /**
     * Method description
     *
     *
     * @param m
     *
     * @return
     *
     * @throws IOException
     */
    public static TtkRefexAbstractMemberChronicle<?> convertRefex(RefexChronicleBI<?> m) throws IOException {
        if (m.getPrimordialVersion() instanceof RefexNidNidNidVersionBI) {
            return new TtkRefexUuidUuidUuidMemberChronicle((RefexNidNidNidVersionBI) m);
        } else if (m.getPrimordialVersion() instanceof RefexNidNidStringVersionBI) {
            return new TtkRefexUuidUuidStringMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexNidNidVersionBI) {
            return new TtkRefexUuidUuidMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexNidFloatVersionBI) {
            return new TtkRefexUuidFloatMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexNidIntVersionBI) {
            return new TtkRefexUuidIntMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexNidLongVersionBI) {
            return new TtkRefexUuidLongMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexNidStringVersionBI) {
            return new TtkRefexUuidStringMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexNidVersionBI) {
            return new TtkRefexUuidMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexIntVersionBI) {
            return new TtkRefexIntMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexStringVersionBI) {
            return new TtkRefexStringMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexLongVersionBI) {
            return new TtkRefexLongMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexBooleanVersionBI) {
            return new TtkRefexBooleanMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexArrayOfBytearrayVersionBI) {
            return new TtkRefexArrayOfByteArrayMemberChronicle(m);
        } else if (m.getPrimordialVersion() instanceof RefexMemberVersionBI) {
            return new TtkRefexMemberChronicle(m);
        } else {
            throw new UnsupportedOperationException("Cannot handle: " + m);
        }
    }

    /**
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a {@code EConcept}
     * object, and contains the same values, field by field, as this
     * {@code EConcept}.
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

        if (TtkConceptChronicle.class.isAssignableFrom(obj.getClass())) {
            TtkConceptChronicle another = (TtkConceptChronicle) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare ConceptAttributes
            if (this.conceptAttributes == null) {
                if (another.conceptAttributes != null) {
                    return false;
                }
            } else if (!this.conceptAttributes.equals(another.conceptAttributes)) {
                return false;
            }

            // Compare Descriptions
            if (!ListCompareHelper.equals(this.descriptions, another.descriptions)) {
                return false;
            }

            // Compare Relationships
            if (!ListCompareHelper.equals(this.relationships, another.relationships)) {
                return false;
            }

            // Compare Images
            if (!ListCompareHelper.equals(this.media, another.media)) {
                return false;
            }

            // Compare Refset Members
            if (!ListCompareHelper.equals(this.refsetMembers, another.refsetMembers)) {
                return false;
            }

            // Compare Refset Members
            if (!ListCompareHelper.equals(this.refsetMembersDynamic, another.refsetMembersDynamic)) {
                return false;
            }

            // If none of the previous comparisons fail, the objects must be equal
            return true;
        }

        return false;
    }

    /**
     * Returns a hash code for this {@code EConcept}.
     *
     * @return a hash code value for this {@code EConcept}.
     */
    @Override
    public int hashCode() {
        return this.conceptAttributes.getPrimordialComponentUuid().hashCode();
    }

    /**
     * Method description
     *
     *
     * @param in
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public final void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        int readDataVersion = in.readInt();

        if (readDataVersion > dataVersion) {
            throw new IOException("Unsupported dataVersion: " + readDataVersion);
        }

        if (readDataVersion == 1) {
            conceptAttributes = new TtkConceptAttributesChronicle(in, readDataVersion);
            primordialUuid = conceptAttributes.getPrimordialComponentUuid();
        } else {
            primordialUuid = new UUID(in.readLong(), in.readLong());

            int attributeCount = in.readByte();

            if (attributeCount == 1) {
                conceptAttributes = new TtkConceptAttributesChronicle(in, readDataVersion);
            }
        }

        int descCount = in.readInt();

        if (descCount > 0) {
            descriptions = new ArrayList<>(descCount);

            for (int i = 0; i < descCount; i++) {
                descriptions.add(new TtkDescriptionChronicle(in, readDataVersion));
            }
        }

        int relCount = in.readInt();

        if (relCount > 0) {
            relationships = new ArrayList<>(relCount);

            for (int i = 0; i < relCount; i++) {
                relationships.add(new TtkRelationshipChronicle(in, readDataVersion));
            }
        }

        int imgCount = in.readInt();

        if (imgCount > 0) {
            media = new ArrayList<>(imgCount);

            for (int i = 0; i < imgCount; i++) {
                media.add(new TtkMediaChronicle(in, readDataVersion));
            }
        }

        int refsetMemberCount = in.readInt();

        if (refsetMemberCount > 0) {
            refsetMembers = new ArrayList<>(refsetMemberCount);

            for (int i = 0; i < refsetMemberCount; i++) {
                RefexType type = RefexType.readType(in);

                switch (type) {
                    case CID:
                        refsetMembers.add(new TtkRefexUuidMemberChronicle(in, readDataVersion));

                        break;

                    case CID_CID:
                        refsetMembers.add(new TtkRefexUuidUuidMemberChronicle(in, readDataVersion));

                        break;

                    case MEMBER:
                        refsetMembers.add(new TtkRefexMemberChronicle(in, readDataVersion));

                        break;

                    case CID_CID_CID:
                        refsetMembers.add(new TtkRefexUuidUuidUuidMemberChronicle(in, readDataVersion));

                        break;

                    case CID_CID_STR:
                        refsetMembers.add(new TtkRefexUuidUuidStringMemberChronicle(in, readDataVersion));

                        break;

                    case INT:
                        refsetMembers.add(new TtkRefexIntMemberChronicle(in, readDataVersion));

                        break;

                    case STR:
                        refsetMembers.add(new TtkRefexStringMemberChronicle(in, readDataVersion));

                        break;

                    case CID_INT:
                        refsetMembers.add(new TtkRefexUuidIntMemberChronicle(in, readDataVersion));

                        break;

                    case BOOLEAN:
                        refsetMembers.add(new TtkRefexBooleanMemberChronicle(in, readDataVersion));

                        break;

                    case CID_FLOAT:
                        refsetMembers.add(new TtkRefexUuidFloatMemberChronicle(in, readDataVersion));

                        break;

                    case CID_LONG:
                        refsetMembers.add(new TtkRefexUuidLongMemberChronicle(in, readDataVersion));

                        break;

                    case CID_STR:
                        refsetMembers.add(new TtkRefexUuidStringMemberChronicle(in, readDataVersion));

                        break;

                    case LONG:
                        refsetMembers.add(new TtkRefexLongMemberChronicle(in, readDataVersion));

                        break;

                    case ARRAY_BYTEARRAY:
                        refsetMembers.add(new TtkRefexArrayOfByteArrayMemberChronicle(in, readDataVersion));

                        break;

                    case CID_CID_CID_FLOAT:
                        refsetMembers.add(new TtkRefexUuidUuidUuidFloatMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_INT:
                        refsetMembers.add(new TtkRefexUuidUuidUuidIntMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_LONG:
                        refsetMembers.add(new TtkRefexUuidUuidUuidLongMemberChronicle(in, dataVersion));

                        break;

                    case CID_CID_CID_STRING:
                        refsetMembers.add(new TtkRefexUuidUuidUuidStringMemberChronicle(in, dataVersion));

                        break;

                    case CID_BOOLEAN:
                        refsetMembers.add(new TtkRefexUuidBooleanMemberChronicle(in, dataVersion));

                        break;

                    default:
                        throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }
            }
        }

        if (readDataVersion < 4) {
            in.readInt();    // destRelNidTypeNidsCount
            in.readInt();    // refsetUuidMemberUuidForConceptCount
            in.readInt();    // refsetUuidMemberUuidForDescsCount
            in.readInt();    // refsetUuidMemberUuidForRelsCount
            in.readInt();    // refsetUuidMemberUuidForImagesCount
            in.readInt();    // refsetUuidMemberUuidForRefsetMembersCount
        }

        if (readDataVersion >= 5) {
            annotationStyleRefex = in.readBoolean();
        } else {
            annotationStyleRefex = false;
        }

        if (readDataVersion >= 9) {
            annotationIndexStyleRefex = in.readBoolean();
        } else {
            annotationIndexStyleRefex = false;
        }

        if (readDataVersion >= 11) {
            int refsetDynamicMemberCount = in.readInt();

            if (refsetDynamicMemberCount > 0) {
                refsetMembersDynamic = new ArrayList<>(refsetDynamicMemberCount);

                for (int i = 0; i < refsetDynamicMemberCount; i++) {
                    refsetMembersDynamic.add(new TtkRefexDynamicMemberChronicle(in, readDataVersion));
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
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName());
        buff.append(": \n   primordial UUID: ");
        buff.append(TtkRevision.informAboutUuid(this.primordialUuid));
        buff.append(": \n   annotation refex: ");
        buff.append(this.annotationStyleRefex);
        buff.append(": \n   indexed annotation: ");
        buff.append(this.annotationIndexStyleRefex);
        buff.append("\n   ConceptAttributes: \n");
        buff.append(PADDING);

        if (this.conceptAttributes == null) {
            buff.append(PADDING + "none\n");
        } else {
            buff.append(this.conceptAttributes);
            buff.append("\n");
        }

        buff.append("\n   Descriptions: \n");

        if (this.descriptions == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TtkDescriptionChronicle d : this.descriptions) {
                buff.append(PADDING);
                buff.append(d);
                buff.append("\n");
            }
        }

        buff.append("\n   Relationships: \n");

        if (this.relationships == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TtkRelationshipChronicle r : this.relationships) {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            }
        }

        buff.append("\n   RefsetMembers: \n");

        if (this.refsetMembers == null) {
            buff.append(PADDING + "none\n");
        } else {
            buff.append("Count: " + this.refsetMembers.size() + "\n");

            this.refsetMembers.stream().limit(20).forEach(r -> {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            });
        }

        buff.append("\n   RefsetMembersDynamic: \n");

        if (this.refsetMembersDynamic == null) {
            buff.append(PADDING + "none\n");
        } else {
            buff.append("Count: " + this.refsetMembersDynamic.size() + "\n");
            this.refsetMembersDynamic.stream().limit(20).forEach(r -> {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            });
        }

        buff.append("\n   Media: \n");

        if (this.media == null) {
            buff.append(PADDING + "none");
        } else {
            for (TtkMediaChronicle m : this.media) {
                buff.append(PADDING);
                buff.append(m);
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
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(dataVersion);

        if (primordialUuid == null) {
            primordialUuid = conceptAttributes.getPrimordialComponentUuid();
        }

        out.writeLong(primordialUuid.getMostSignificantBits());
        out.writeLong(primordialUuid.getLeastSignificantBits());

        if (conceptAttributes == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            conceptAttributes.writeExternal(out);
        }

        if (descriptions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(descriptions.size());

            for (TtkDescriptionChronicle d : descriptions) {
                d.writeExternal(out);
            }
        }

        if (relationships == null) {
            out.writeInt(0);
        } else {
            out.writeInt(relationships.size());

            for (TtkRelationshipChronicle r : relationships) {
                r.writeExternal(out);
            }
        }

        if (media == null) {
            out.writeInt(0);
        } else {
            out.writeInt(media.size());

            for (TtkMediaChronicle img : media) {
                img.writeExternal(out);
            }
        }

        if (refsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetMembers.size());

            for (TtkRefexAbstractMemberChronicle<?> r : refsetMembers) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }

        out.writeBoolean(annotationStyleRefex);
        out.writeBoolean(annotationIndexStyleRefex);

        if (refsetMembersDynamic == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetMembersDynamic.size());

            for (TtkRefexDynamicMemberChronicle r : refsetMembersDynamic) {
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
    public TtkConceptAttributesChronicle getConceptAttributes() {
        return conceptAttributes;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkDescriptionChronicle> getDescriptions() {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }

        return descriptions;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkMediaChronicle> getMedia() {
        if (media == null) {
            media = new ArrayList<>();
        }

        return media;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public UUID getPrimordialUuid() {
        return primordialUuid;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkRefexAbstractMemberChronicle<?>> getRefsetMembers() {
        if (refsetMembers == null) {
            refsetMembers = new ArrayList<>();
        }

        return refsetMembers;
    }

    /**
     * Will not return null
     */
    public List<TtkRefexDynamicMemberChronicle> getRefsetMembersDynamic() {
        if (refsetMembersDynamic == null) {
            refsetMembersDynamic = new ArrayList<>();
        }

        return refsetMembersDynamic;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<TtkRelationshipChronicle> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<>();
        }

        return relationships;
    }

    /**
     * Method description
     *
     *
     * @return
     * @deprecated no longer supported.
     */
    @Deprecated
    public boolean isAnnotationIndexStyleRefex() {
        return annotationIndexStyleRefex;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public boolean isAnnotationStyleRefex() {
        return annotationStyleRefex;
    }

    /**
     * Method description
     *
     *
     * @param annotationIndexStyleRefex
     * @deprecated No longer used.
     */
    @Deprecated
    public void setAnnotationIndexStyleRefex(boolean annotationIndexStyleRefex) {
        this.annotationIndexStyleRefex = annotationIndexStyleRefex;
    }

    /**
     * Method description
     *
     *
     * @param annotationStyleRefex
     */
    public void setAnnotationStyleRefex(boolean annotationStyleRefex) {
        this.annotationStyleRefex = annotationStyleRefex;
    }

    /**
     * Method description
     *
     *
     * @param conceptAttributes
     */
    public void setConceptAttributes(TtkConceptAttributesChronicle conceptAttributes) {
        this.conceptAttributes = conceptAttributes;
    }

    /**
     * Method description
     *
     *
     * @param descriptions
     */
    public void setDescriptions(List<TtkDescriptionChronicle> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Method description
     *
     *
     * @param images
     */
    public void setImages(List<TtkMediaChronicle> images) {
        this.media = images;
    }

    /**
     * Method description
     *
     *
     * @param primordialUuid
     */
    public void setPrimordialUuid(UUID primordialUuid) {
        this.primordialUuid = primordialUuid;
    }

    /**
     * Method description
     *
     *
     * @param refsetMembers
     */
    public void setRefsetMembers(List<TtkRefexAbstractMemberChronicle<?>> refsetMembers) {
        this.refsetMembers = refsetMembers;
    }

    public void setRefsetDynamicMembers(List<TtkRefexDynamicMemberChronicle> refsetMembersDynamic) {
        this.refsetMembersDynamic = refsetMembersDynamic;
    }

    /**
     * Method description
     *
     *
     * @param relationships
     */
    public void setRelationships(List<TtkRelationshipChronicle> relationships) {
        this.relationships = relationships;
    }

    public String toXml() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString().substring("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>".length());
    }

    @Override
    public List<UUID> getUuidList() {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getUuids();
        }
        return Arrays.asList(new UUID[]{getPrimordialUuid()});
    }


}
