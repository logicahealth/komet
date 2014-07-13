package org.ihtsdo.otf.tcc.model.cc.component;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.AnalogBI;
import org.ihtsdo.otf.tcc.api.AnalogGeneratorBI;
import static org.ihtsdo.otf.tcc.api.blueprint.RefexCAB.refexSpecNamespace;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierLong;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.identifier.IdentifierVersion;
import org.ihtsdo.otf.tcc.model.cc.identifier.IdentifierVersionUuid;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.type_long.LongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicRevision;

/**
 * Class description
 *
 *
 * @param <R>
 * @param <C>
 *
 * @version Enter version here..., 13/03/30
 * @author Enter your name here...
 */
public abstract class ConceptComponent<R extends Revision<R, C>, C extends ConceptComponent<R, C>>
        implements ComponentBI, ComponentVersionBI, IdBI, AnalogBI, AnalogGeneratorBI<R>,
        Comparable<ConceptComponent>{

    /**
     * Field description
     */
    protected static final Logger logger = Logger.getLogger(ConceptComponent.class.getName());
    /**
     * Field description
     */
    private static AtomicBoolean fixAlert = new AtomicBoolean(false);
    /**
     * Field description
     */
    static int authorityNid = Integer.MAX_VALUE;
    /**
     * Field description
     */
    private static AnnotationWriter annotationWriter = new AnnotationWriter();

   /** Field description */
   protected ArrayList<IdentifierVersion> additionalIdVersions;
    /**
     * Field description
     */
    public ConcurrentSkipListSet<RefexMember<?, ?>> annotations;
    public ConcurrentSkipListSet<RefexDynamicMember> annotationsDynamic;
    /**
     * Field description
     */
    public int enclosingConceptNid;
    /**
     * Field description
     */
    public int nid;
    /**
     * Field description
     */
    protected long primordialLsb;
    /**
     * primordial: first created or developed
     *
     */
    protected long primordialMsb;
    /**
     * primordial: first created or developed Sap = status, author, position;
     * position = path, time;
     */
    public int primordialStamp;
    /**
     * Field description
     */
    public Set<R> revisions;   
    /**
     * Constructs ...
     *
     */
    public ConceptComponent() {
        super();
    }

    /**
     * Constructs ...
     *
     *
     * @param enclosingConceptNid
     * @param input
     *
     * @throws IOException
     */
    protected ConceptComponent(int enclosingConceptNid, TupleInput input) throws IOException {
        super();
        this.enclosingConceptNid = enclosingConceptNid;
        readComponentFromBdb(input);

        int cNid = P.s.getConceptNidForNid(nid);

        if (cNid == Integer.MAX_VALUE) {
            P.s.setConceptNidForNid(this.enclosingConceptNid, this.nid);
        } else if (cNid != this.enclosingConceptNid) {
            P.s.resetConceptNidForNid(this.enclosingConceptNid, this.nid);

            if (fixAlert.compareAndSet(true, false)) {
                logger.log(
                        Level.SEVERE, "a. Datafix warning. See log for details.",
                        new Exception(
                        String.format(
                        "a-Datafix: cNid %s %s incorrect for: %s %s should have been: {4}{5}", cNid,
                        P.s.getUuidsForNid(cNid), this.nid, P.s.getUuidsForNid(this.nid),
                        this.enclosingConceptNid, P.s.getUuidsForNid(this.enclosingConceptNid))));
            }
        }

        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
    }

    // TODO move the EComponent constructors to a helper class or factory class...
    // So that the size of this class is kept limited ?
    /**
     * Constructs ...
     *
     *
     * @param eComponent
     * @param enclosingConceptNid
     *
     * @throws IOException
     */
    protected ConceptComponent(TtkComponentChronicle<?> eComponent, int enclosingConceptNid) throws IOException {
        super();
        assert eComponent != null;

        if (P.s.hasUuid(eComponent.primordialUuid)) {
            this.nid = P.s.getNidForUuids(eComponent.primordialUuid);
        } else {
            this.nid = P.s.getNidForUuids(eComponent.getUuids());
        }

        assert this.nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
        this.enclosingConceptNid = enclosingConceptNid;

        int cNid = P.s.getConceptNidForNid(nid);

        if (cNid == Integer.MAX_VALUE) {
            P.s.setConceptNidForNid(this.enclosingConceptNid, this.nid);
        } else if (cNid != this.enclosingConceptNid) {
            P.s.resetConceptNidForNid(this.enclosingConceptNid, this.nid);

            if (fixAlert.compareAndSet(true, false)) {
                logger.log(
                        Level.SEVERE, "b. Datafix warning. See log for details.",
                        new Exception(
                                String.format(
                                        "b-Datafix: cNid %s %s incorrect for: %s %s should have been: {4}{5}", cNid,
                                        P.s.getUuidsForNid(cNid), this.nid, P.s.getUuidsForNid(this.nid),
                                        this.enclosingConceptNid, P.s.getUuidsForNid(this.enclosingConceptNid))));
            }
        }

        this.primordialStamp = P.s.getStamp(eComponent);
        assert primordialStamp > 0 : " Processing nid: " + enclosingConceptNid;
        this.primordialMsb = eComponent.getPrimordialComponentUuid().getMostSignificantBits();
        this.primordialLsb = eComponent.getPrimordialComponentUuid().getLeastSignificantBits();
        convertId(eComponent.additionalIds);
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;

        if (eComponent.getAnnotations() != null) {
            this.annotations = new ConcurrentSkipListSet<>();

            for (TtkRefexAbstractMemberChronicle<?> eAnnot : eComponent.getAnnotations()) {
                RefexMember<?, ?> annot = RefexMemberFactory.create(eAnnot, enclosingConceptNid);

                this.annotations.add(annot);
            }
        }
        if (eComponent.getAnnotationsDynamic() != null) {
            this.annotationsDynamic = new ConcurrentSkipListSet<>();

            for (TtkRefexDynamicMemberChronicle eAnnot : eComponent.getAnnotationsDynamic()) {
                RefexDynamicMember annot = RefexDynamicMemberFactory.create(eAnnot, enclosingConceptNid);

                this.annotationsDynamic.add(annot);
            }
        }
    }
    
    public boolean isIndexed() {
        return P.s.isIndexed(nid);
    }
    
    public void setIndexed() {
        if (!isUncommitted()) {
            P.s.setIndexed(nid, true);
        }
    }
    /**
     * Enum description
     *
     */
    public enum IDENTIFIER_PART_TYPES {

        LONG(1), STRING(2), UUID(3);
        /**
         * Field description
         */
        private int partTypeId;

        /**
         * Constructs ...
         *
         *
         * @param partTypeId
         */
        IDENTIFIER_PART_TYPES(int partTypeId) {
            this.partTypeId = partTypeId;
        }

        /**
         * Method description
         *
         *
         * @param input
         *
         * @return
         */
        public static IDENTIFIER_PART_TYPES readType(TupleInput input) {
            int partTypeId = input.readByte();

            switch (partTypeId) {
                case 1:
                    return LONG;

                case 2:
                    return STRING;

                case 3:
                    return UUID;
            }

            throw new UnsupportedOperationException("partTypeId: " + partTypeId);
        }

        /**
         * Method description
         *
         *
         * @param output
         */
        public void writeType(TupleOutput output) {
            output.writeByte(partTypeId);
        }

        /**
         * Method description
         *
         *
         * @param denotationClass
         *
         * @return
         */
        public static IDENTIFIER_PART_TYPES getType(Class<?> denotationClass) {
            if (UUID.class.isAssignableFrom(denotationClass)) {
                return UUID;
            } else if (Long.class.isAssignableFrom(denotationClass)) {
                return LONG;
            } else if (String.class.isAssignableFrom(denotationClass)) {
                return STRING;
            }

            throw new UnsupportedOperationException(denotationClass.toString());
        }
    }

    /**
     * Method description
     *
     *
     * @param annotation
     *
     * @return
     *
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
        if (annotations == null) {
            annotations = new ConcurrentSkipListSet<>(new Comparator<RefexChronicleBI>() {
                @Override
                public int compare(RefexChronicleBI t, RefexChronicleBI t1) {
                    return t.getNid() - t1.getNid();
                }
            });
        }

        modified();
        return annotations.add((RefexMember<?, ?>) annotation);
    }
    
    @Override
    public boolean addDynamicAnnotation(RefexDynamicChronicleBI annotation) throws IOException {
        if (annotationsDynamic == null) {
            annotationsDynamic = new ConcurrentSkipListSet<>(new Comparator<RefexDynamicChronicleBI>() {
                @Override
                public int compare(RefexDynamicChronicleBI t, RefexDynamicChronicleBI t1) {
                    return t.getNid() - t1.getNid();
                }
            });
        }

        modified();
        return annotationsDynamic.add((RefexDynamicMember) annotation);
    }

    /**
     * Method description
     *
     *
     * @param allNids
     */
    abstract protected void addComponentNids(Set<Integer> allNids);

    /**
     * Method description
     *
     *
     * @param srcId
     *
     * @return
     */
    public boolean addIdVersion(IdentifierVersion srcId) {
        if (additionalIdVersions == null) {
            additionalIdVersions = new ArrayList<>();
        }

        boolean returnValue = additionalIdVersions.add(srcId);
        ConceptChronicle c = getEnclosingConcept();

        c.modified();

        return returnValue;
    }

    /**
     * Method description
     *
     *
     * @param version
     *
     * @return
     */
    public final boolean addMutablePart(R version) {
        return addRevision(version);
    }

    /**
     * Method description
     *
     *
     * @param buf
     * @param nidToConvert
     */
    public static void addNidToBuffer(Appendable buf, int nidToConvert) {
        try {
            if ((nidToConvert != Integer.MAX_VALUE) && (nidToConvert != 0)) {
                if (P.s.getConceptNidForNid(nidToConvert) == nidToConvert) {
                    buf.append("\"");
                    buf.append(P.s.getConcept(nidToConvert).toString());
                    buf.append("\" [");
                    buf.append(Integer.toString(nidToConvert));
                    buf.append("]");
                } else {
                    ComponentBI component = P.s.getComponent(nidToConvert);
                    if (component != null) {
                        buf.append(component.getClass().getSimpleName());
                        buf.append(" from concept: \"");
                        buf.append(P.s.getConceptForNid(nidToConvert).toString());
                        buf.append("\" [");
                        buf.append(Integer.toString(nidToConvert));
                        buf.append("]");  
                    } else {
                        buf.append("[" + Integer.toString(nidToConvert)
                                + "null]");
                    }

                }
            } else {
                buf.append(Integer.toString(nidToConvert));
            }
        } catch (IOException e) {
            try {
                buf.append(e.getLocalizedMessage());
                logger.log(Level.WARNING, e.getLocalizedMessage(), e);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param r
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public final boolean addRevision(R r) {
        assert r != null;

        boolean returnValue;
        ConceptChronicle c = getEnclosingConcept();

        assert c != null : "Can't find concept for: " + r;

        if (revisions == null) {
            revisions = new RevisionSet(primordialStamp);
            returnValue = revisions.add(r);
        } else {
            returnValue = revisions.add(r);
        }

        r.primordialComponent = (C) this;
        c.modified();
        clearVersions();
        return returnValue;
    }

    /**
     * Method description
     *
     *
     * @param r
     *
     * @return
     */
    public final boolean addRevisionNoRedundancyCheck(R r) {
        return addRevision(r);
    }

    /**
     * Method description
     *
     *
     * @param buf
     * @param nidToConvert
     */
    public static void addTextToBuffer(Appendable buf, int nidToConvert) {
        try {
            if ((nidToConvert != Integer.MAX_VALUE) && (nidToConvert != 0) && (P.s.getConceptNidForNid(nidToConvert) == nidToConvert)) {
                buf.append(P.s.getConcept(nidToConvert).toString());
            } else {
                buf.append(Integer.toString(nidToConvert));
            }
        } catch (IOException e) {
            try {
                buf.append(e.getLocalizedMessage());
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param uuidId
     * @param authorityNid
     * @param statusNid
     * @param time
     * @param authorNid
     * @param moduleNid
     * @param pathNid
     *
     * @return
     */
    public boolean addUuidId(UUID uuidId, int authorityNid, Status status, long time, int authorNid,
            int moduleNid, int pathNid) {
        IdentifierVersionUuid v = new IdentifierVersionUuid(status, time, authorNid, moduleNid, pathNid,
                authorityNid, uuidId);

        return addIdVersion(v);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected String assertionString() {
        try {
            return P.s.getConcept(enclosingConceptNid).toLongString();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return toString();
    }

    /**
     * Method description
     *
     */
    public void cancel() {
        clearVersions();

        if (this.getTime() == Long.MAX_VALUE) {
            this.primordialStamp = -1;
        }

        if (additionalIdVersions != null) {
            List<IdentifierVersion> toRemove = new ArrayList<>();

            for (IdentifierVersion idv : additionalIdVersions) {
                if (idv.getTime() == Long.MAX_VALUE) {
                    toRemove.add(idv);
                    idv.setTime(Long.MIN_VALUE);
                    idv.setStamp(-1);
                }
            }

            if (toRemove.size() > 0) {
                for (IdentifierVersion idv : toRemove) {
                    additionalIdVersions.remove(idv);
                }
            }
        }

        if (revisions != null) {
            List<R> toRemove = new ArrayList<>();

            for (R r : revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                    toRemove.add(r);
                }
            }

            if (toRemove.size() > 0) {
                for (R r : toRemove) {
                    revisions.remove(r);
                }
            }
        }

        if (annotations != null) {
            List<Object> toRemove = new ArrayList<>();

            for (RefexMember<?, ?> a : annotations) {
                a.clearVersions();

                if (a.getTime() == Long.MAX_VALUE) {
                    toRemove.add(a);
                } else if (a.revisions != null) {
                    for (RefexRevision rv : a.revisions) {
                        List<RefexRevision> revToRemove = new ArrayList<>();

                        if (rv.getTime() == Long.MAX_VALUE) {
                            revToRemove.add(rv);
                        }

                        a.revisions.removeAll(revToRemove);
                    }
                }
            }

            if (toRemove.size() > 0) {
                for (Object r : toRemove) {
                    annotations.remove((RefexMember<?, ?>) r);
                }
            }
        }
        
        if (annotationsDynamic != null) {
            List<Object> toRemove = new ArrayList<>();

            for (RefexDynamicMember a : annotationsDynamic) {
                a.clearVersions();

                if (a.getTime() == Long.MAX_VALUE) {
                    toRemove.add(a);
                } else if (a.revisions != null) {
                    for (RefexDynamicRevision rv : a.revisions) {
                        List<RefexDynamicRevision> revToRemove = new ArrayList<>();

                        if (rv.getTime() == Long.MAX_VALUE) {
                            revToRemove.add(rv);
                        }

                        a.revisions.removeAll(revToRemove);
                    }
                }
            }

            if (toRemove.size() > 0) {
                for (Object r : toRemove) {
                    annotationsDynamic.remove((RefexMember<?, ?>) r);
                }
            }
        }
    }

    /**
     * Method description
     *
     */
    protected void clearAnnotationVersions() {
        if (annotations != null) {
            for (RefexMember<?, ?> rm : annotations) {
                rm.clearVersions();
            }
        }
        if (annotationsDynamic != null) {
            for (RefexDynamicMember rm : annotationsDynamic) {
                rm.clearVersions();
            }
        }
    }

    /**
     * Method description
     *
     */
    public abstract void clearVersions();

    /**
     * Method description
     *
     *
     * @param o
     *
     * @return
     */
    @Override
    public int compareTo(ConceptComponent o) {
        return this.nid - o.nid;
    }

    /**
     * Method description
     *
     *
     * @param another
     *
     * @return
     */
    public boolean conceptComponentFieldsEqual(ConceptComponent<R, C> another) {
        if (this.nid != another.nid) {
            return false;
        }

        if (this.primordialStamp != another.primordialStamp) {
            return false;
        }

        if (this.primordialLsb != another.primordialLsb) {
            return false;
        }

        if (this.primordialMsb != another.primordialMsb) {
            return false;
        }

        if ((this.additionalIdVersions != null) && (another.additionalIdVersions == null)) {
            return false;
        }

        if ((this.additionalIdVersions == null) && (another.additionalIdVersions != null)) {
            return false;
        }

        if (this.additionalIdVersions != null) {
            if (this.additionalIdVersions.equals(another.additionalIdVersions) == false) {
                return false;
            }
        }

        if ((this.revisions != null) && (another.revisions == null)) {
            return false;
        }

        if ((this.revisions == null) && (another.revisions != null)) {
            return false;
        }

        if (this.revisions != null) {
            if (this.revisions.equals(another.revisions) == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method description
     *
     *
     * @param stamp
     *
     * @return
     */
    public boolean containsStamp(int stamp) {
        if (primordialStamp == stamp) {
            return true;
        }

        if (revisions != null) {
            for (Revision r : revisions) {
                if (r.stamp == stamp) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Method description
     *
     *
     * @param list
     *
     * @throws IOException
     */
    public final void convertId(List<TtkIdentifier> list) throws IOException {
        if ((list == null) || list.isEmpty()) {
            return;
        }

        additionalIdVersions = new ArrayList<>(list.size());

        for (TtkIdentifier idv : list) {
            try {
                Object denotation = idv.getDenotation();
                // Use the algorithm for member content UUIDs. 
                // See method computeMemberContentUuid() in RefexCAB
                switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                    case LONG:
                        TtkIdentifierLong longId = (TtkIdentifierLong) idv;
                        UUID longMemberUuid = UuidT5Generator.get(refexSpecNamespace,
                                RefexType.LONG.name()
                                + idv.authorityUuid
                                + new UUID(primordialMsb, primordialLsb).toString()
                                + longId.toString());

                        LongMember longIdMember = new LongMember();
                        longIdMember.enclosingConceptNid = this.enclosingConceptNid;
                        longIdMember.setPrimordialUuid(longMemberUuid);
                        longIdMember.nid = (P.s.getNidForUuids(longMemberUuid));


                        longIdMember.assemblageNid = P.s.getNidForUuids(idv.authorityUuid);
                        longIdMember.setReferencedComponentNid(nid);
                        longIdMember.setLong1(longId.getDenotation());

                        longIdMember.setSTAMP(
                                P.s.getStamp(idv.getStatus(),
                                idv.getTime(),
                                P.s.getNidForUuids(idv.authorUuid),
                                P.s.getNidForUuids(idv.moduleUuid),
                                P.s.getNidForUuids(idv.pathUuid)));
                        addAnnotation(longIdMember);

                        break;

                    case STRING:
                        if (false) {
                        TtkIdentifierString ids = (TtkIdentifierString) idv;
                        UUID stringMemberUuid = UuidT5Generator.get(refexSpecNamespace,
                                RefexType.LONG.name()
                                + idv.authorityUuid
                                + new UUID(primordialMsb, primordialLsb).toString()
                                + ids.denotation);
                        StringMember stringMember = new StringMember();

                        stringMember.enclosingConceptNid = this.enclosingConceptNid;
                        stringMember.setPrimordialUuid(stringMemberUuid);
                        stringMember.nid = P.s.getNidForUuids(stringMemberUuid);

                        stringMember.assemblageNid = P.s.getNidForUuids(idv.authorityUuid);
                        stringMember.setReferencedComponentNid(nid);
                        stringMember.setString1(ids.getDenotation());

                        stringMember.setSTAMP(P.s.getStamp(idv.getStatus(),
                                idv.getTime(),
                                P.s.getNidForUuids(idv.authorUuid),
                                P.s.getNidForUuids(idv.moduleUuid),
                                P.s.getNidForUuids(idv.pathUuid)));
                        addAnnotation(stringMember);
                        } else {
                            // TODO add back in conversions for string identifiers after 
                            // we have other ways to remove them from SNOMED. 
                        }

                        break;

                    case UUID:
                        additionalIdVersions.add(new IdentifierVersionUuid((TtkIdentifierUuid) idv));
                        P.s.put(((TtkIdentifierUuid) idv).getDenotation(), nid);

                        break;

                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
                throw new IOException(ex);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (ConceptComponent.class.isAssignableFrom(obj.getClass())) {
            ConceptComponent<?, ?> another = (ConceptComponent<?, ?>) obj;

            return this.nid == another.nid;
        }

        return false;
    }

    /**
     * Method description
     *
     *
     * @param another
     *
     * @return
     */
    public abstract boolean fieldsEqual(ConceptComponent<R, C> another);

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid, primordialStamp});
    }

    /**
     * Method description
     *
     *
     * @param ec
     * @param vc
     *
     * @return
     *
     * @throws IOException
     */
    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        boolean changed = false;
        List<? extends Version> versions = this.getVersions(vc.getVcWithAllStatusValues());

        if (ec.getEditPaths().getSetValues().length != 1) {
            throw new IOException("Edit paths != 1: " + ec.getEditPaths().getSetValues().length + " "
                    + Arrays.asList(ec));
        }

        int pathNid = ec.getEditPaths().getSetValues()[0];

        if (versions.size() == 1) {
            for (Version cv : versions) {
                if (!cv.isBaselineGeneration() && (cv.getPathNid() != pathNid)
                        && (cv.getTime() != Long.MAX_VALUE)) {
                    changed = true;
                    cv.makeAnalog(cv.getStatus(), Long.MAX_VALUE, ec.getModuleNid(), ec.getAuthorNid(),
                            pathNid);
                }
            }
        } else if (versions.size() > 1) {
            List<? extends Version> resolution = vc.getContradictionManager().resolveVersions(versions);

            if (versions.size() > 0) {
                for (Version cv : resolution) {
                    cv.makeAnalog(cv.getStatus(), Long.MAX_VALUE, ec.getModuleNid(), ec.getAuthorNid(),
                            pathNid);
                    changed = true;
                }
            }
        }

        // don't adjudicate ids
        // annotations
        if (annotations != null) {
            for (RefexMember<?, ?> a : annotations) {
                boolean annotationChanged = a.makeAdjudicationAnalogs(ec, vc);

                changed = changed || annotationChanged;
            }
        }
        if (annotationsDynamic != null) {
            for (RefexDynamicMember a : annotationsDynamic) {
                boolean annotationChanged = a.makeAdjudicationAnalogs(ec, vc);

                changed = changed || annotationChanged;
            }
        }

        return changed;
    }

    /**
     * Method description
     *
     *
     * @param another
     *
     * @return
     *
     * @throws IOException
     */
    public ConceptComponent<R, C> merge(C another)
            throws IOException {
        Set<Integer> versionSapNids = getVersionStamps();

        P.s.setIndexed(nid, false);
        // merge versions
        for (Version<R, C> v : another.getVersions()) {
            if ((v.getStamp() != -1) && !versionSapNids.contains(v.getStamp())) {
                addRevision((R) v.getRevision());
            }
        }

        Set<Integer> identifierStamps = getIdStamps();

        // merge identifiers
        if (another.additionalIdVersions != null) {
            if (this.additionalIdVersions == null) {
                this.additionalIdVersions = another.additionalIdVersions;
            } else {
                for (IdentifierVersion idv : another.additionalIdVersions) {
                    if ((idv.getStamp() != -1) && !identifierStamps.contains(idv.getStamp())) {
                        this.additionalIdVersions.add(idv);
                    }
                }
            }
        }

        Set<Integer> annotationStamps = getAnnotationStamps();

        // merge annotations

        if (another.annotations != null) {
            if (this.annotations == null) {
                this.annotations = another.annotations;
            } else {
                HashMap<Integer, RefexMember<?, ?>> anotherAnnotationMap = new HashMap<>();

                for (RefexChronicleBI annotation : another.annotations) {
                    anotherAnnotationMap.put(annotation.getNid(), (RefexMember<?, ?>) annotation);
                }

                for (RefexMember annotation : this.annotations) {
                    RefexMember<?, ?> anotherAnnotation = anotherAnnotationMap.remove(annotation.getNid());

                    if (anotherAnnotation != null) {
                        for (RefexMemberVersion annotationVersion : anotherAnnotation.getVersions()) {
                            if ((annotationVersion.getStamp() != -1)
                                    && !annotationStamps.contains(annotationVersion.getStamp())) {
                                annotation.addRevision(annotationVersion.getRevision());
                            }
                        }
                    }
                }

                this.annotations.addAll(anotherAnnotationMap.values());
            }
        }
        
        if (another.annotationsDynamic != null) {
            if (this.annotationsDynamic == null) {
                this.annotationsDynamic = another.annotationsDynamic;
            } else {
                HashMap<Integer, RefexDynamicMember> anotherAnnotationMap = new HashMap<>();

                for (RefexDynamicChronicleBI annotation : another.annotationsDynamic) {
                    anotherAnnotationMap.put(annotation.getNid(), (RefexDynamicMember) annotation);
                }

                for (RefexDynamicMember annotation : this.annotationsDynamic) {
                    RefexDynamicMember anotherAnnotation = anotherAnnotationMap.remove(annotation.getNid());

                    if (anotherAnnotation != null) {
                        for (RefexDynamicMemberVersion annotationVersion : anotherAnnotation.getVersions()) {
                            if ((annotationVersion.getStamp() != -1)
                                    && !annotationStamps.contains(annotationVersion.getStamp())) {
                                annotation.addRevision(annotationVersion.getRevision());
                            }
                        }
                    }
                }

                this.annotationsDynamic.addAll(anotherAnnotationMap.values());
            }
        }

        return this;
    }

    /**
     * Call when data has changed, so concept updates it's version.
     */
    protected void modified() {
//TODO-AKF
//        try {
//            if (enclosingConceptNid != Integer.MIN_VALUE) {
//                if ((P.s != null) && P.s.hasConcept(enclosingConceptNid)) {
//                    P.s.setIndexed(nid, false);
//                    ConceptChronicle c = (ConceptChronicle) P.s.getConcept(enclosingConceptNid);
//
//                    if (c != null) {
//                        c.modified();
//                    }
//                }
//            } else {
//                logger.log(Level.WARNING, "No enclosingConceptNid for: {0}", this);
//            }
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Method description
     *
     *
     * @param input
     */
    private void readAnnotationsFromBdb(TupleInput input) {
        annotations = annotationWriter.entryToObject(input, enclosingConceptNid);
        annotationsDynamic = annotationWriter.entryDynamicToObject(input, enclosingConceptNid);
    }

    /**
     * Method description
     *
     *
     * @param input
     */
    public final void readComponentFromBdb(TupleInput input) {
        this.nid = input.readInt();
        this.primordialMsb = input.readLong();
        this.primordialLsb = input.readLong();
        this.primordialStamp = input.readInt();
        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        readIdentifierFromBdb(input);
        readAnnotationsFromBdb(input);
        readFromBdb(input);
    }

    /**
     * Method description
     *
     *
     * @param input
     */
    public abstract void readFromBdb(TupleInput input);

    /**
     * Method description
     *
     *
     * @param input
     */
    private void readIdentifierFromBdb(TupleInput input) {

        // nid, list size, and conceptNid are read already by the binder...
        int listSize = input.readShort();

        if (listSize != 0) {
            additionalIdVersions = new ArrayList<>(listSize);
        }

        for (int i = 0; i < listSize; i++) {
            switch (IDENTIFIER_PART_TYPES.readType(input)) {
                case LONG:
                    throw new UnsupportedOperationException("Long identifiers must be represented as refexes...");

                case STRING:
                    throw new UnsupportedOperationException("Long identifiers must be represented as refexes...");

                case UUID:
                    IdentifierVersionUuid idvu = new IdentifierVersionUuid(input);

                    if (idvu.getTime() != Long.MIN_VALUE) {
                        additionalIdVersions.add(idvu);
                    }

                    break;

                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public final boolean readyToWrite() {
        assert nid != Integer.MAX_VALUE : assertionString();
        assert nid != 0 : assertionString();
        assert readyToWriteComponent();

        if (revisions != null) {
            for (R r : revisions) {
                assert r.readyToWrite();
            }
        }

        if (annotations != null) {
            for (RefexMember<?, ?> m : annotations) {
                assert m.readyToWrite();
            }
        }
        
        if (annotationsDynamic != null) {
            for (RefexDynamicMember m : annotationsDynamic) {
                assert m.readyToWrite();
            }
        }

        if (additionalIdVersions != null) {
            for (IdentifierVersion idv : additionalIdVersions) {
                assert idv.readyToWrite();
            }
        }

        return true;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public abstract boolean readyToWriteComponent();

    /**
     * Method description
     *
     *
     * @param r
     *
     * @return
     */
    public boolean removeRevision(R r) {
        boolean changed = false;

        if (revisions != null) {
            changed = revisions.remove(r);
            clearVersions();
        }

        return changed;
    }

    /**
     * Method description
     *
     *
     * @param statusNid
     * @param authorNid
     * @param pathNid
     * @param moduleNid
     */
    public final void resetUncommitted(Status status, int authorNid, int pathNid, int moduleNid) {
        if (getTime() != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Cannot resetUncommitted if time != Long.MIN_VALUE");
        }

        this.primordialStamp = P.s.getStamp(status, Long.MAX_VALUE, authorNid, moduleNid, pathNid);
        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        this.getEnclosingConcept().setIsCanceled(false);
        this.clearVersions();
    }

    /**
     * Method description
     *
     *
     * @param min
     * @param max
     *
     * @return
     */
    @Override
    public boolean stampIsInRange(int min, int max) {
        if ((primordialStamp >= min) && (primordialStamp <= max)) {
            return true;
        }

        if (annotations != null) {
            for (RefexChronicleBI<?> a : annotations) {
                for (RefexVersionBI<?> av : a.getVersions()) {
                    if (av.stampIsInRange(min, max)) {
                        return true;
                    }
                }
            }
        }
        
        if (annotationsDynamic != null) {
            for (RefexDynamicChronicleBI<?> a : annotationsDynamic) {
                for (RefexDynamicVersionBI<?> av : a.getVersions()) {
                    if (av.stampIsInRange(min, max)) {
                        return true;
                    }
                }
            }
        }

        if (additionalIdVersions != null) {
            for (IdentifierVersion id : additionalIdVersions) {
                if (id.stampIsInRange(min, max)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("nid:");
        buf.append(nid);
        buf.append(" pUuid:");
        buf.append(new UUID(primordialMsb, primordialLsb));
        buf.append(" stamp: ");

        if (primordialStamp == Integer.MIN_VALUE) {
            buf.append("Integer.MIN_VALUE");
        } else {
            buf.append(primordialStamp);
        }

        if (primordialStamp > 0) {
            try {
                buf.append(" s:");
                buf.append(getStatus());
                buf.append(" t: ");
                buf.append(TimeHelper.formatDate(getTime()));
                buf.append(" ");
                buf.append(getTime());
                buf.append(" a:");
                ConceptComponent.addNidToBuffer(buf, getAuthorNid());
                buf.append(" m:");
                ConceptComponent.addNidToBuffer(buf, getModuleNid());
                buf.append(" p:");
                ConceptComponent.addNidToBuffer(buf, getPathNid());
            } catch (Throwable e) {
                buf.append(" !!! Invalid stamp. Cannot compute status, time, author, module, path. !!! ");
                buf.append(e.getLocalizedMessage());
            }
        } else {
            buf.append(" !!! Invalid stamp. Cannot compute status, time, author, module, path. !!! ");
        }

        buf.append(" extraVersions: ");
        buf.append(revisions);
        buf.append(" xtraIds:");
        buf.append(additionalIdVersions);
        buf.append(" annotations:");
        buf.append(annotations);
        buf.append(" annotations Dynamic:");
        buf.append(annotationsDynamic);
        buf.append("};");

        return buf.toString();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public abstract String toUserString();
    
    /**
     * Method description
     *
     *
     * @param snapshot
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        return toUserString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     *
     * @param another
     * @return either a zero length String, or a String containing a description
     * of the validation failures.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public String validate(ConceptComponent<?, ?> another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();
        String validationResults;

        if (this.nid != another.nid) {
            buf.append("\tConceptComponent.nid not equal: \n" + "\t\tthis.nid = ").append(this.nid).append("\n"
                    + "\t\tanother.nid = ").append(another.nid).append("\n");
        }

        if (this.primordialStamp != another.primordialStamp) {
            buf.append("\tConceptComponent.primordialSapNid not equal: \n"
                    + "\t\tthis.primordialSapNid = ").append(this.primordialStamp).append("\n"
                    + "\t\tanother.primordialSapNid = ").append(another.primordialStamp).append("\n");
        }

        if (this.primordialMsb != another.primordialMsb) {
            buf.append("\tConceptComponent.primordialMsb not equal: \n"
                    + "\t\tthis.primordialMsb = ").append(this.primordialMsb).append("\n"
                    + "\t\tanother.primordialMsb = ").append(another.primordialMsb).append("\n");
        }

        if (this.primordialLsb != another.primordialLsb) {
            buf.append("\tConceptComponent.primordialLsb not equal: \n"
                    + "\t\tthis.primordialLsb = ").append(this.primordialLsb).append("\n"
                    + "\t\tanother.primordialLsb = ").append(another.primordialLsb).append("\n");
        }

        if (this.additionalIdVersions != null) {
            if (this.additionalIdVersions.equals(another.additionalIdVersions) == false) {
                buf.append(
                        "\tConceptComponent.additionalIdentifierParts not equal: \n"
                        + "\t\tthis.additionalIdentifierParts = ").append(this.additionalIdVersions).append(
                        "\n" + "\t\tanother.additionalIdentifierParts = ").append(
                        another.additionalIdVersions).append("\n");
            }
        }

        if (this.revisions != null) {
            if (this.revisions.equals(another.revisions) == false) {
                if (this.revisions.size() != another.revisions.size()) {
                    buf.append("\trevision.size() not equal");
                } else {
                    Iterator<R> thisRevItr = this.revisions.iterator();
                    Iterator<R> anotherRevItr = (Iterator<R>) another.revisions.iterator();

                    while (thisRevItr.hasNext()) {
                        R thisRevision = thisRevItr.next();
                        R anotherRevision = anotherRevItr.next();

                        validationResults = thisRevision.validate(anotherRevision);

                        if (validationResults.length() != 0) {
                            buf.append("\tRevision[").append(thisRevision).append(", ").append(
                                    anotherRevision).append("] not equal: \n");
                            buf.append(validationResults);
                        }
                    }
                }
            }
        }

        if (buf.length() != 0) {

            // Add a sentinal mark to indicate we reach the top of the hierarchy
            buf.append("\t----------------------------\n");
        }

        return buf.toString();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public final int versionCount() {
        if (revisions == null) {
            return 1;
        }

        return revisions.size() + 1;
    }

    /**
     * Method description
     *
     *
     * @param vc1
     * @param vc2
     * @param compareAuthoring
     *
     * @return
     */
    @Override
    public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
        List<? extends Version> versions1 = getVersions(vc1);
        List<? extends Version> versions2 = getVersions(vc2);

        if (versions1.size() != versions2.size()) {
            return false;
        } else if ((versions1.size() == 1) && (versions2.size() == 1)) {
            for (Version v1 : versions1) {
                for (Version v2 : versions2) {
                    if (v1 == v2) {
                        return true;
                    }

                    if (v1.getStatus() != v2.getStatus()) {
                        return false;
                    }

                    if (compareAuthoring) {
                        if (v1.getAuthorNid() != v2.getAuthorNid()) {
                            return false;
                        }

                        if (v1.getPathNid() != v2.getPathNid()) {
                            return false;
                        }
                    }

                    if (v1.getTime() != v2.getTime()) {
                        return false;
                    }

                    if (v1.fieldsEqual(v2)) {
                        return false;
                    }
                }
            }
        } else {
            int foundCount = 0;

            for (Version v1 : versions1) {
                for (Version v2 : versions2) {
                    if (v1 == v2) {
                        foundCount++;
                    } else if (v1.getStatus() != v2.getStatus()) {
                        continue;
                    } else if (v1.getTime() != v2.getTime()) {
                        continue;
                    } else if (compareAuthoring && (v1.getAuthorNid() != v2.getAuthorNid())) {
                        continue;
                    } else if (compareAuthoring && (v1.getPathNid() != v2.getPathNid())) {
                        continue;
                    } else if (v1.fieldsEqual(v2)) {
                        foundCount++;
                    }
                }
            }

            if (foundCount != versions1.size()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method description
     *
     *
     * @param output
     * @param maxReadOnlyStatusAtPositionNid
     */
    private void writeAnnotationsToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        annotationWriter.objectToEntry(annotations, output, maxReadOnlyStatusAtPositionNid);
        annotationWriter.objectDynamicToEntry(annotationsDynamic, output, maxReadOnlyStatusAtPositionNid);
    }

    /**
     * Method description
     *
     *
     * @param output
     * @param maxReadOnlyStatusAtPositionNid
     */
    public final void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        assert nid != 0;
        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        assert primordialStamp != Integer.MAX_VALUE;
        output.writeInt(nid);
        output.writeLong(primordialMsb);
        output.writeLong(primordialLsb);
        output.writeInt(primordialStamp);
        writeIdentifierToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeAnnotationsToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeToBdb(output, maxReadOnlyStatusAtPositionNid);
    }

    /**
     * Method description
     *
     *
     * @param output
     * @param maxStamp
     */
    private void writeIdentifierToBdb(TupleOutput output, int maxStamp) {
        List<IdentifierVersion> partsToWrite = new ArrayList<>();

        if (additionalIdVersions != null) {
            for (IdentifierVersion p : additionalIdVersions) {
                if ((p.getStamp() > maxStamp) && (p.getTime() != Long.MIN_VALUE)) {
                    partsToWrite.add(p);
                }
            }
        }

        // Start writing
        output.writeShort(partsToWrite.size());

        for (IdentifierVersion p : partsToWrite) {
            p.getType().writeType(output);
            p.writeIdPartToBdb(output);
        }
    }

    /**
     * Method description
     *
     *
     * @param output
     * @param maxReadOnlyStatusAtPositionNid
     */
    public abstract void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

    /**
     * Method description
     *
     *
     * @return
     */
    public ArrayList<IdentifierVersion> getAdditionalIdentifierParts() {
        return additionalIdVersions;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Collection<? extends IdBI> getAdditionalIds() {
        return getAdditionalIdentifierParts();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Collection<? extends IdBI> getAllIds() {
        return getIdVersions();
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Set<Integer> getAllNidsForId() throws IOException {
        HashSet<Integer> allNids = new HashSet<>();

        allNids.add(nid);
        allNids.add(getAuthorNid());
        allNids.add(getPathNid());

        return allNids;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Set<Integer> getAllNidsForVersion() throws IOException {
        HashSet<Integer> allNids = new HashSet<>();

        allNids.add(nid);
        allNids.add(getAuthorNid());
        allNids.add(getPathNid());
        addComponentNids(allNids);

        return allNids;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Set<Integer> getAllStamps() throws IOException {
        return getComponentStamps();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Set<Integer> getAnnotationStamps() {
        int size = 0;

        if (annotations != null) {
            size = size + annotations.size();
        }
        if (annotationsDynamic != null) {
            size = size + annotationsDynamic.size();
        }

        HashSet<Integer> sapNids = new HashSet<>(size);

        if (annotations != null) {
            for (RefexChronicleBI<?> annotation : annotations) {
                for (RefexVersionBI<?> av : annotation.getVersions()) {
                    sapNids.add(av.getStamp());
                }
            }
        }
        if (annotationsDynamic != null) {
            for (RefexDynamicChronicleBI<?> annotation : annotationsDynamic) {
                for (RefexDynamicVersionBI<?> av : annotation.getVersions()) {
                    sapNids.add(av.getStamp());
                }
            }
        }

        return sapNids;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
        if (annotations == null) {
            return Collections.unmodifiableCollection(new ArrayList<RefexChronicleBI<?>>());
        }
        return Collections.unmodifiableCollection(annotations);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ConcurrentSkipListSet<? extends RefexChronicleBI<?>> getAnnotationsMod() {
        return annotations;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getAuthorNid() {
        return P.s.getAuthorNidForStamp(primordialStamp);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final int getAuthorityNid() {
        try {
            return TermAux.GENERATED_UUID.getLenient().getConceptNid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public ComponentChronicleBI getChronicle() {
        return (ComponentChronicleBI) this;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Set<Integer> getComponentStamps() throws IOException {
        int size = 1;

        if (revisions != null) {
            size = size + revisions.size();
        }

        if (additionalIdVersions != null) {
            size = size + additionalIdVersions.size();
        }

        if (annotations != null) {
            size = size + annotations.size();
        }
        if (annotationsDynamic != null) {
            size = size + annotationsDynamic.size();
        }

        HashSet<Integer> stamps = new HashSet<>(size);

        stamps.addAll(getVersionStamps());
        stamps.addAll(getIdStamps());
        stamps.addAll(getAnnotationStamps());

        return stamps;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    /**
     * Method description
     *
     *
     * @param xyz
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
            throws IOException {
        if (annotations == null) {
            return Collections.unmodifiableCollection(new ArrayList<RefexVersionBI<?>>());
        }

        Collection<RefexVersionBI<?>> returnValues = new ArrayList<>();

        for (RefexChronicleBI<?> refex : annotations) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param cls
     * @param <T>
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
            Class<T> cls)
            throws IOException {
        if (annotations == null) {
            return Collections.unmodifiableCollection(new ArrayList<T>());
        }

        Collection<T> returnValues = new ArrayList<>();

        for (RefexChronicleBI<?> refex : annotations) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                if (cls.isAssignableFrom(version.getClass())) {
                    returnValues.add((T) version);
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refexNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz,
            int refexNid)
            throws IOException {
        Collection<RefexVersionBI<?>> returnValues = new ArrayList<>();

        if (annotations != null) {
            for (RefexChronicleBI<?> refex : annotations) {
                if (refex.getAssemblageNid() == refexNid) {
                    for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                        returnValues.add(version);
                    }
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refexNid
     * @param cls
     * @param <T>
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
            int refexNid, Class<T> cls)
            throws IOException {
        Collection<T> returnValues = new ArrayList<>();

        if (annotations != null) {
            for (RefexChronicleBI<?> refex : annotations) {
                if (refex.getAssemblageNid() == refexNid) {
                    for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                        if (cls.isAssignableFrom(version.getClass())) {
                            returnValues.add((T) version);
                        }
                    }
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexMembers(refsetNid);
        List<RefexVersionBI<?>> returnValues = new ArrayList<>(refexes.size());

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes();
        List<RefexVersionBI<?>> returnValues = new ArrayList<>(refexes.size());

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final Object getDenotation() {
        return new UUID(primordialMsb, primordialLsb);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ConceptChronicle getEnclosingConcept() {
        try {
//            Need to do this to return different ways depending on datastore implementation
            return (ConceptChronicle) P.s.getConcept(enclosingConceptNid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Set<Integer> getIdStamps() {
        int size = 1;

        if (additionalIdVersions != null) {
            size = size + additionalIdVersions.size();
        }

        HashSet<Integer> stamps = new HashSet<>(size);

        assert primordialStamp != 0;
        stamps.add(primordialStamp);

        if (additionalIdVersions != null) {
            for (IdentifierVersion id : additionalIdVersions) {
                stamps.add(id.getStamp());
            }
        }

        return stamps;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public final List<IdBI> getIdVersions() {
        List<IdBI> returnValues = new ArrayList<>();

        if (additionalIdVersions != null) {
            returnValues.addAll(additionalIdVersions);
        }

        returnValues.add(this);

        return Collections.unmodifiableList(returnValues);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
        Collection<? extends RefexVersionBI<?>> currentRefexes = new HashSet(getRefexMembersActive(xyz));
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes();
        List<RefexVersionBI<?>> returnValues = new ArrayList<>(refexes.size());
        ViewCoordinate allStatus = xyz.getVcWithAllStatusValues();

        allStatus.setAllowedStatus(null);

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(allStatus)) {
                if (!currentRefexes.contains(version)) {
                    returnValues.add(version);
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getModuleNid() {
        return P.s.getModuleNidForStamp(primordialStamp);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public final int getMutablePartCount() {
        return revisions.size();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final int getNid() {
        return nid;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final int getPathNid() {
        return P.s.getPathNidForStamp(primordialStamp);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Position getPosition() throws IOException {
        return new Position(getTime(), P.s.getPath(getPathNid()));
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Set<Position> getPositions() throws IOException {
        List<? extends Version> localVersions = getVersions();
        Set<Position> positions = new HashSet<>(localVersions.size());

        for (Version v : localVersions) {
            positions.add(v.getPosition());
        }

        return positions;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public UUID getPrimordialUuid() {
        return new UUID(primordialMsb, primordialLsb);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected int getPrimordialStatusAtPositionNid() {
        return primordialStamp;
    }

    /**
     * Method description
     *
     *
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        Collection<? extends RefexChronicleBI<?>> r = getRefexes();
        List<RefexChronicleBI<?>> returnValues = new ArrayList<>(r.size());

        for (RefexChronicleBI<?> rcbi : r) {
            if (rcbi.getAssemblageNid() == refsetNid) {
                returnValues.add(rcbi);
            }
        }

        return returnValues;
    }
    
    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamic()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexesDynamic() throws IOException
    {
        List<NidPairForRefex> pairs = P.s.getRefexPairs(nid);
        List<RefexDynamicChronicleBI<?>> returnValues = new ArrayList<>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<>();

        if ((pairs != null) && !pairs.isEmpty()) {
            for (NidPairForRefex pair : pairs) {
                ComponentChronicleBI<?> component = P.s.getComponent(pair.getMemberNid());
                if (component instanceof RefexDynamicChronicleBI<?>)
                {
                    RefexDynamicChronicleBI<?> ext = (RefexDynamicChronicleBI<?>) component;
    
                    if ((ext != null) && !addedMembers.contains(ext.getNid())) {
                        addedMembers.add(ext.getNid());
                        returnValues.add(ext);
                    }
                }
            }
        }

        ComponentBI component = this;

        if (component instanceof ConceptChronicle) {
            component = ((ConceptChronicle) component).getConceptAttributes();
        }

        ComponentChronicleBI<?> cc = (ComponentChronicleBI<?>) component;
        Collection<? extends RefexDynamicChronicleBI<?>> fetchedAnnotations = cc.getRefexDynamicAnnotations();

        if (fetchedAnnotations != null) {
            for (RefexDynamicChronicleBI<?> annotation : fetchedAnnotations) {
                if (addedMembers.contains(annotation.getNid()) == false) {
                    returnValues.add(annotation);
                    addedMembers.add(annotation.getNid());
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamicActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
     */
    @Override
    public Collection<? extends RefexDynamicVersionBI<?>> getRefexesDynamicActive(ViewCoordinate viewCoordinate) throws IOException
    {
        Collection<? extends RefexDynamicChronicleBI<?>> refexes = getRefexesDynamic();
        List<RefexDynamicVersionBI<?>> returnValues = new ArrayList<>(refexes.size());

        for (RefexDynamicChronicleBI<?> refex : refexes) {
            for (RefexDynamicVersionBI<?> version : refex.getVersions(viewCoordinate)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }
    
    

    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicAnnotations()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicAnnotations() throws IOException
    {
        if (annotationsDynamic == null) {
            return Collections.unmodifiableCollection(new ArrayList<RefexDynamicChronicleBI<?>>());
        }

        return Collections.unmodifiableCollection(annotationsDynamic);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicMembers()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicMembers() throws IOException
    {
        List<NidPairForRefex> pairs = P.s.getRefexPairs(nid);
        List<RefexDynamicChronicleBI<?>> returnValues = new ArrayList<>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<>();

        if ((pairs != null) && !pairs.isEmpty()) {
            for (NidPairForRefex pair : pairs) {
                ComponentChronicleBI<?> component = P.s.getComponent(pair.getMemberNid());
                if (component instanceof RefexDynamicChronicleBI<?>)
                {
                    RefexDynamicChronicleBI<?> ext = (RefexDynamicChronicleBI<?>) P.s.getComponent(pair.getMemberNid());
    
                    if ((ext != null) && !addedMembers.contains(ext.getNid())) {
                        addedMembers.add(ext.getNid());
                        returnValues.add(ext);
                    }
                }
            }
        }
        
        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
        List<NidPairForRefex> pairs = P.s.getRefexPairs(nid);
        List<RefexChronicleBI<?>> returnValues = new ArrayList<>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<>();

        if ((pairs != null) && !pairs.isEmpty()) {
            for (NidPairForRefex pair : pairs) {
                ComponentChronicleBI<?> component = P.s.getComponent(pair.getMemberNid());
                if (component instanceof RefexChronicleBI<?>)
                {
                    RefexChronicleBI<?> ext = (RefexChronicleBI<?>) component;
    
                    if ((ext != null) && !addedMembers.contains(ext.getNid())) {
                        addedMembers.add(ext.getNid());
                        returnValues.add(ext);
                    }
                }
            }
        }

        ComponentBI component = this;

        if (component instanceof ConceptChronicle) {
            component = ((ConceptChronicle) component).getConceptAttributes();
        }

        ComponentChronicleBI<?> cc = (ComponentChronicleBI<?>) component;
        Collection<? extends RefexChronicleBI<?>> fetchedAnnotations = cc.getAnnotations();

        if (fetchedAnnotations != null) {
            for (RefexChronicleBI<?> annotation : fetchedAnnotations) {
                if (addedMembers.contains(annotation.getNid()) == false) {
                    returnValues.add(annotation);
                    addedMembers.add(annotation.getNid());
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Set<Integer> getRefsetMemberSapNids() throws IOException {
        List<NidPairForRefex> pairs = P.s.getRefexPairs(nid);

        if ((pairs == null) || pairs.isEmpty()) {
            return new HashSet<>(0);
        }

        HashSet<Integer> returnValues = new HashSet<>(pairs.size());

        for (NidPairForRefex pair : pairs) {
            ComponentChronicleBI<?> component = P.s.getComponent(pair.getMemberNid());
            if (component instanceof RefexChronicleBI<?>)
            {
                RefexChronicleBI<?> ext = (RefexChronicleBI<?>) component;
    
                if (ext != null) {
                    for (RefexVersionBI<?> refexV : ext.getVersions()) {
                        returnValues.add(refexV.getStamp());
                    }
    
                    returnValues.addAll(((ConceptComponent) ext).getRefsetMemberSapNids());
                }
            }
        }

        return returnValues;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException {
        List<NidPairForRefex> pairs = P.s.getRefexPairs(nid);

        if ((pairs == null) || pairs.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<RefexChronicleBI<?>> returnValues = new ArrayList<>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<>();

        for (NidPairForRefex pair : pairs) {
            ComponentChronicleBI<?> component = P.s.getComponent(pair.getMemberNid());
            if (component instanceof RefexChronicleBI<?>)
            {
                RefexChronicleBI<?> ext = (RefexChronicleBI<?>) component;
    
                if ((ext != null) && !addedMembers.contains(ext.getNid())) {
                    addedMembers.add(ext.getNid());
                    returnValues.add(ext);
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getStamp() {
        return primordialStamp;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final Status getStatus() {
        return P.s.getStatusForStamp(primordialStamp);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final long getTime() {
        return P.s.getTimeForStamp(primordialStamp);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final List<UUID> getUUIDs() {
        List<UUID> returnValues = new ArrayList<>();

        returnValues.add(new UUID(primordialMsb, primordialLsb));

        if (additionalIdVersions != null) {
            for (IdentifierVersion idv : additionalIdVersions) {
                if (IdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
                    IdentifierVersionUuid uuidPart = (IdentifierVersionUuid) idv;

                    returnValues.add(uuidPart.getUuid());
                }
            }
        }

        return returnValues;
    }
    
    /**
     * Method description
     *
     *
     * @return
     */
    public HashMap<Integer, Version<R, C>> getVersionSapMap() {
        int size = 1;

        if (revisions != null) {
            size = size + revisions.size();
        }

        HashMap<Integer, Version<R, C>> sapMap = new HashMap<>(size);

        for (Version v : getVersions()) {
            sapMap.put(v.getStamp(), v);
        }

        return sapMap;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Set<Integer> getVersionStamps() {
        int size = 1;

        if (revisions != null) {
            size = size + revisions.size();
        }

        HashSet<Integer> sapNids = new HashSet<>(size);

        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        sapNids.add(primordialStamp);

        if (revisions != null) {
            for (R r : revisions) {
                sapNids.add(r.stamp);
            }
        }

        return sapNids;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public abstract List<? extends Version> getVersions();

    /**
     * Method description
     *
     *
     * @param c
     *
     * @return
     */
    public abstract List<? extends Version> getVersions(ViewCoordinate c);

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        Collection<? extends RefexChronicleBI<?>> members = getAnnotationsActive(xyz, refsetNid);

        for (RefexChronicleBI<?> refex : members) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexMembers(refsetNid);

        if (!refexes.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Method description
     *
     *
     * @param r
     *
     * @return
     */
    public final boolean hasRevision(R r) {
        if (revisions == null) {
            return false;
        }

        return revisions.contains(r);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public boolean isBaselineGeneration() {
        return primordialStamp <= P.s.getMaxReadOnlyStamp();
    }

    /**
     * Method description
     *
     *
     * @param input
     *
     * @return
     */
    public static boolean isCanceled(TupleInput input) {
        int nid = input.readInt();
        int primordialSapNid = input.readInt();

        return primordialSapNid == -1;
    }

    @Override
    public boolean isActive() {
        return getStatus() == Status.ACTIVE;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public boolean isUncommitted() {
        if (this.getTime() == Long.MAX_VALUE) {
            return true;
        }

        if (additionalIdVersions != null) {
            for (IdentifierVersion idv : additionalIdVersions) {
                if (idv.getTime() == Long.MAX_VALUE) {
                    return true;
                }
            }
        }

        if (revisions != null) {
            for (R r : revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                    return true;
                }
            }
        }

        if (annotations != null) {
            for (RefexChronicleBI<?> r : annotations) {
                if (r.isUncommitted()) {
                    return true;
                }
            }
        }
        
        if (annotationsDynamic != null) {
            for (RefexDynamicChronicleBI<?> r : annotationsDynamic) {
                if (r.isUncommitted()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Method description
     *
     *
     * @param authorNid
     */
    @Override
    public void setAuthorNid(int authorNid) {
        if (getAuthorNid() != 0 && getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (authorNid != getAuthorNid()) {
            this.primordialStamp = P.s.getStamp(getStatus(), Long.MAX_VALUE, authorNid, getModuleNid(),
                    getPathNid());
            assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
            modified();
        }
    }

    /**
     * Method description
     *
     *
     * @param moduleId
     */
    @Override
    public final void setModuleNid(int moduleId) {
        if (getModuleNid() != 0 && getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (moduleId != this.getModuleNid()) {
            this.primordialStamp = P.s.getStamp(getStatus(), Long.MAX_VALUE, getAuthorNid(), moduleId,
                    getPathNid());
            assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

    /**
     * Method description
     *
     *
     * @param nid
     *
     * @throws PropertyVetoException
     */
    @Override
    public final void setNid(int nid) throws PropertyVetoException {
        if (this.nid != 0 && (this.getStamp() != Integer.MAX_VALUE) && (this.getTime() != Long.MAX_VALUE) && (this.nid != nid)
                && (this.nid != Integer.MAX_VALUE)) {
            throw new PropertyVetoException("nid", null);
        }

        this.nid = nid;
    }

    /**
     * Method description
     *
     *
     * @param pathId
     */
    @Override
    public final void setPathNid(int pathId) {
        if (getPathNid() != 0 && getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (pathId != getPathNid()) {
            this.primordialStamp = P.s.getStamp(getStatus(), Long.MAX_VALUE, getAuthorNid(), getModuleNid(),
                    pathId);
            assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
            modified();
        }
    }

    /**
     * Method description
     *
     *
     * @param pUuid
     */
    public void setPrimordialUuid(UUID pUuid) {
        this.primordialMsb = pUuid.getMostSignificantBits();
        this.primordialLsb = pUuid.getLeastSignificantBits();
    }

    /**
     * Method description
     *
     *
     * @param sapNid
     */
    public void setSTAMP(int sapNid) {
        this.primordialStamp = sapNid;
        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        modified();
    }

    /**
     * Method description
     *
     *
     * @param statusId
     */
    @Override
    public final void setStatus(Status status) {
        if (getStatus() != null && getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (status != this.getStatus()) {
            this.primordialStamp = P.s.getStamp(status, Long.MAX_VALUE, getAuthorNid(), getModuleNid(),
                    getPathNid());
            assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

    /**
     * Method description
     *
     *
     * @param time
     */
    @Override
    public final void setTime(long time) {
        if (getTime() != 0 && getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (time != getTime()) {
            this.primordialStamp = P.s.getStamp(getStatus(), time, getAuthorNid(), getModuleNid(),
                    getPathNid());
            assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

   
}
