package org.ihtsdo.otf.tcc.model.cc.component;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.sememe.SememeService;
import gov.vha.isaac.ochre.api.sememe.SememeType;
import gov.vha.isaac.ochre.model.sememe.SememeChronicleImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;
import java.beans.PropertyVetoException;
import java.io.*;
import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ModificationTracker;
import org.ihtsdo.otf.tcc.model.cc.identifier.IdentifierVersion;
import org.ihtsdo.otf.tcc.model.cc.identifier.IdentifierVersionUuid;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexService;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMemberFactory;

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
        Comparable<ConceptComponent> {

    private static RefexService refexService;

    protected static RefexService getRefexService() {
        if (refexService == null) {
            refexService = LookupService.getService(RefexService.class);
        }
        return refexService;
    }

    private static SememeService sememeService;

    protected static SememeService getSememeService() {
        if (sememeService == null) {
            sememeService = LookupService.getService(SememeService.class);
        }
        return sememeService;
    }

    private static CommitService commitManager;

    protected static CommitService getCommitManager() {
        if (commitManager == null) {
            commitManager = Hk2Looker.getService(CommitService.class);
        }
        return commitManager;
    }

    private static IdentifierService sequenceService = null;

    protected static IdentifierService getIdService() {
        if (sequenceService == null) {
            sequenceService = LookupService.getService(IdentifierService.class);
        }
        return sequenceService;
    }

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

    protected long[] additionalUuidParts;

    /**
     * Field description
     */
    @Deprecated
    protected ArrayList<IdentifierVersion> additionalIdVersions;
    /**
     * Field description
     */
    public int enclosingConceptNid;

    /**
     * @param modificationTracker
     */
    public void setModificationTracker(ModificationTracker modificationTracker) {
        this.modificationTracker = modificationTracker;
    }

    public ModificationTracker modificationTracker;
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
     * primordial: first created or developed STAMP = status, time, author,
     * module, path;
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

        if (PersistentStore.get().hasUuid(eComponent.primordialUuid)) {
            this.nid = PersistentStore.get().getNidForUuids(eComponent.primordialUuid);
        } else {
            this.nid = PersistentStore.get().getNidForUuids(eComponent.getUuids());
        }

        assert this.nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
        this.enclosingConceptNid = enclosingConceptNid;

        int cNid = PersistentStore.get().getConceptNidForNid(nid);

        if (cNid == Integer.MAX_VALUE) {
            PersistentStore.get().setConceptNidForNid(this.enclosingConceptNid, this.nid);
        } else if (cNid != this.enclosingConceptNid) {
            PersistentStore.get().resetConceptNidForNid(this.enclosingConceptNid, this.nid);

            if (fixAlert.compareAndSet(true, false)) {
                logger.log(
                        Level.SEVERE, "b. Datafix warning. See log for details.",
                        new Exception(
                                String.format(
                                        "b-Datafix: cNid %s %s incorrect for: %s %s should have been: {4}{5}", cNid,
                                        PersistentStore.get().getUuidsForNid(cNid), this.nid, PersistentStore.get().getUuidsForNid(this.nid),
                                        this.enclosingConceptNid, PersistentStore.get().getUuidsForNid(this.enclosingConceptNid))));
            }
        }

        this.primordialStamp = PersistentStore.get().getStamp(eComponent);
        if (primordialStamp <= 0) {
            System.out.println("### DEBUG: primordial stamp was less than zero for component: " + eComponent + " stamp was : " + primordialStamp);
        }
        assert primordialStamp > 0 : " Processing nid: " + enclosingConceptNid + " stamp: " + primordialStamp;
        this.primordialMsb = eComponent.getPrimordialComponentUuid().getMostSignificantBits();
        this.primordialLsb = eComponent.getPrimordialComponentUuid().getLeastSignificantBits();
        convertId(eComponent.additionalIds);
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;

        if (eComponent.getAnnotations() != null) {

            for (TtkRefexAbstractMemberChronicle<?> eAnnot : eComponent.getAnnotations()) {
                RefexMember<?, ?> annot = RefexMemberFactory.create(eAnnot, enclosingConceptNid);

                addAnnotation(annot);
            }
        }
        if (eComponent.getAnnotationsDynamic() != null) {

            for (TtkRefexDynamicMemberChronicle eAnnot : eComponent.getAnnotationsDynamic()) {
                RefexDynamicMember annot = RefexDynamicMemberFactory.create(eAnnot, enclosingConceptNid);

                addDynamicAnnotation(annot);
            }
        }
    }

    public void setAdditionalUuids(List<UUID> uuids) {
        additionalUuidParts = new long[uuids.size() * 2];
        for (int i = 0; i < uuids.size(); i++) {
            UUID uuid = uuids.get(i);
            additionalUuidParts[2 * i] = uuid.getMostSignificantBits();
            additionalUuidParts[2 * i + 1] = uuid.getLeastSignificantBits();
        }
    }

    public void setEnclosingConceptNid(int enclosingConceptNid) {
        this.enclosingConceptNid = enclosingConceptNid;
    }

    @Override
    public int getStampSequence() {
        return getStamp();
    }

    @Override
    public State getState() {
        return getStatus().getState();
    }

    @Override
    public int getAuthorSequence() {
        return getIdService().getConceptSequence(getAuthorNid());
    }

    @Override
    public int getModuleSequence() {
        return getIdService().getConceptSequence(getModuleNid());
    }

    @Override
    public int getPathSequence() {
        return getIdService().getConceptSequence(getPathNid());
    }

    public IntStream getVersionStampSequences() {
        IntStream.Builder builder = IntStream.builder();
        builder.accept(primordialStamp);
        if (revisions != null) {
            revisions.stream().forEach((r) -> {
                builder.accept(r.getStamp());
            });
        }
        return builder.build();
    }

    public boolean isIndexed() {
        return PersistentStore.get().isIndexed(nid);
    }

    public void setIndexed() {
        if (!isUncommitted()) {
            PersistentStore.get().setIndexed(nid, true);
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
        public static IDENTIFIER_PART_TYPES readType(DataInputStream input) throws IOException {
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
        public void writeType(DataOutput output) throws IOException {
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
    public final boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
       getRefexService().writeRefex((RefexMember<?, ?>) annotation);
       return true;
    }

    @Override
    public final boolean addDynamicAnnotation(RefexDynamicChronicleBI annotation) throws IOException {
        getRefexService().writeDynamicRefex(annotation);
        return true;
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
        getModificationTracker().modified((ComponentChronicleBI) this);

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
                if (PersistentStore.get().getConceptNidForNid(nidToConvert) == nidToConvert) {
                    buf.append("\"");
                    buf.append(PersistentStore.get().getConcept(nidToConvert).toString());
                    buf.append("\" [");
                    buf.append(Integer.toString(nidToConvert));
                    buf.append("]");
                } else {
                    ComponentBI component = PersistentStore.get().getComponent(nidToConvert);
                    if (component != null) {
                        buf.append(component.getClass().getSimpleName());
                        buf.append(" from concept: \"");
                        buf.append(PersistentStore.get().getConceptForNid(nidToConvert).toString());
                        buf.append("\" [");
                        buf.append(Integer.toString(nidToConvert));
                        buf.append("]");
                    } else {
                        buf.append("[" + Integer.toString(nidToConvert)
                                + " is null]");
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

        if (revisions == null) {
            revisions = new RevisionSet(primordialStamp);
            returnValue = revisions.add(r);
        } else {
            returnValue = revisions.add(r);
        }

        r.primordialComponent = (C) this;
        if (modificationTracker != null) {
            modificationTracker.modified(r.getChronicle());
        }
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
            if ((nidToConvert != Integer.MAX_VALUE) && (nidToConvert != 0) && (PersistentStore.get().getConceptNidForNid(nidToConvert) == nidToConvert)) {
                buf.append(PersistentStore.get().getConcept(nidToConvert).toString());
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
     * @param status
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
            return PersistentStore.get().getConcept(enclosingConceptNid).toLongString();
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
                    case STRING:
                    case LONG:
                        UUID strMemberUuid = UuidT5Generator.get(refexSpecNamespace,
                                RefexType.STR.name()
                                + idv.authorityUuid
                                + new UUID(primordialMsb, primordialLsb).toString()
                                + denotation.toString());
                        int sememeNid = PersistentStore.get().getNidForUuids(strMemberUuid);
                        int containerSequence = getIdService().getSememeSequence(sememeNid);
                        int assemblageSequence = getIdService().
                                getConceptSequence(PersistentStore.get().
                                        getNidForUuids(idv.authorityUuid));

                        SememeChronicleImpl<StringSememeImpl> sememeChronicle
                                = new SememeChronicleImpl<>(
                                        SememeType.STRING,
                                        strMemberUuid,
                                        sememeNid,
                                        assemblageSequence,
                                        nid, // referenced component
                                        containerSequence
                                );
                        int stampSequence = getCommitManager().
                                getStamp(State.ACTIVE,
                                        idv.time,
                                        getIdService().getConceptSequenceForUuids(idv.authorUuid),
                                        getIdService().getConceptSequenceForUuids(idv.moduleUuid),
                                        getIdService().getConceptSequenceForUuids(idv.pathUuid));
                        StringSememeImpl stringVersion = sememeChronicle.createMutableStampedVersion(StringSememeImpl.class, stampSequence);
                        stringVersion.setString(denotation.toString());
                        getSememeService().writeSememe(sememeChronicle);
                        break;

                    case UUID:
                        additionalIdVersions.add(new IdentifierVersionUuid((TtkIdentifierUuid) idv));
                        PersistentStore.get().put(((TtkIdentifierUuid) idv).getDenotation(), nid);

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
                if ((cv.getPathNid() != pathNid)
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

        PersistentStore.get().setIndexed(nid, false);
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

        return this;
    }

    /**
     * Call when data has changed, so concept updates it's version.
     */
    protected void modified() {
//TODO-AKF-KEC: should this be implemented?
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
     * @param status
     * @param authorNid
     * @param pathNid
     * @param moduleNid
     */
    public final void resetUncommitted(Status status, int authorNid, int pathNid, int moduleNid) {
        if (getTime() != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Cannot resetUncommitted if time != Long.MIN_VALUE");
        }

        this.primordialStamp = PersistentStore.get().getStamp(status, Long.MAX_VALUE, authorNid, moduleNid, pathNid);
        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
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

        buf.append("{nid:");
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
                long time = getTime();
                if (time == Long.MAX_VALUE) {
                    buf.append("uncommitted");
                } else if (time == Long.MIN_VALUE) {
                    buf.append("canceled");
                } else {
                    buf.append(TimeHelper.formatDate(time));
                }
                buf.append(" ");
                buf.append(getTime());
                buf.append(" a:");
                ConceptComponent.addNidToBuffer(buf, getAuthorNid());
                buf.append(" m:");
                ConceptComponent.addNidToBuffer(buf, getModuleNid());
                buf.append(" p:");
                ConceptComponent.addNidToBuffer(buf, getPathNid());
                buf.append(" ms:");
                buf.append(time);
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
     * @param maxStamp
     */
    private void writeAdditionalIdentifiersToDataOutput(DataOutput output, int maxStamp) throws IOException {
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
    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
        try {
            return getRefexes();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getAuthorNid() {
        return PersistentStore.get().getAuthorNidForStamp(primordialStamp);
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

        HashSet<Integer> stamps = new HashSet<>();

        stamps.addAll(getVersionStamps());
        stamps.addAll(getIdStamps());
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

    @Override
    public int getContainerSequence() {
        return getIdService().getConceptSequence(nid);
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
        Collection<RefexVersionBI<?>> returnValues = new ArrayList<>();

        for (RefexChronicleBI<?> refex : getAnnotations()) {
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

        Collection<T> returnValues = new ArrayList<>();

        for (RefexChronicleBI<?> refex : getAnnotations()) {
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

        for (RefexChronicleBI<?> refex : getAnnotations()) {
            if (refex.getAssemblageNid() == refexNid) {
                for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
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

        for (RefexChronicleBI<?> refex : getAnnotations()) {
            if (refex.getAssemblageNid() == refexNid) {
                for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                    if (cls.isAssignableFrom(version.getClass())) {
                        returnValues.add((T) version);
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
    public ModificationTracker getModificationTracker() {
        return this.modificationTracker;
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
        return PersistentStore.get().getModuleNidForStamp(primordialStamp);
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
        return PersistentStore.get().getPathNidForStamp(primordialStamp);
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
        return new Position(getTime(), PersistentStore.get().getPath(getPathNid()));
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
        return Ts.get().getRefexesForAssemblage(refsetNid);
    }

    /**
     * @return @throws java.io.IOException
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamic()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexesDynamic() throws IOException {
        return Collections.unmodifiableCollection(getRefexService().
                getDynamicRefexesForComponent(nid).collect(Collectors.toList()));
    }

    /**
     * @see
     * org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamicActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
     */
    @Override
    public Collection<? extends RefexDynamicVersionBI<?>> getRefexesDynamicActive(ViewCoordinate viewCoordinate) throws IOException {
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
     * @see
     * org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicAnnotations()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicAnnotations() throws IOException {
        return getRefexesDynamic();
    }

    /**
     * @see
     * org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicMembers()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicMembers() throws IOException {
        return Collections.unmodifiableCollection(getRefexService().
                getDynamicRefexesFromAssemblage(nid).collect(Collectors.toList()));
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
        return Collections.unmodifiableCollection((Ts.get().getRefexesForComponent(nid)));
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
        List<NidPairForRefex> pairs = PersistentStore.get().getRefexPairs(nid);

        if ((pairs == null) || pairs.isEmpty()) {
            return new HashSet<>(0);
        }

        HashSet<Integer> returnValues = new HashSet<>(pairs.size());

        for (NidPairForRefex pair : pairs) {
            ComponentChronicleBI<?> component = PersistentStore.get().getComponent(pair.getMemberNid());
            if (component instanceof RefexChronicleBI<?>) {
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
        List<NidPairForRefex> pairs = PersistentStore.get().getRefexPairs(nid);

        if ((pairs == null) || pairs.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<RefexChronicleBI<?>> returnValues = new ArrayList<>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<>();

        for (NidPairForRefex pair : pairs) {
            ComponentChronicleBI<?> component = PersistentStore.get().getComponent(pair.getMemberNid());
            if (component instanceof RefexChronicleBI<?>) {
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
        return PersistentStore.get().getStatusForStamp(primordialStamp);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public final long getTime() {
        return PersistentStore.get().getTimeForStamp(primordialStamp);
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
        if (additionalUuidParts != null) {
            for (int i = 0; i < additionalUuidParts.length; i = i + 2) {
                returnValues.add(new UUID(additionalUuidParts[i], additionalUuidParts[i + 1]));
            }
        }

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
        IntStream aliases = IntStream.of(getCommitManager().getAliases(primordialStamp));
        aliases.forEach((int alias) -> {
            sapNids.add(alias);
        });

        if (revisions != null) {
            for (R r : revisions) {
                sapNids.add(r.stamp);
                aliases = IntStream.of(getCommitManager().getAliases(r.stamp));
                aliases.forEach((int alias) -> {
                    sapNids.add(alias);
                });
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
     * @param input
     *
     * @return
     */
    public static boolean isCanceled(DataInputStream input) throws IOException {
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
            this.primordialStamp = PersistentStore.get().getStamp(getStatus(), Long.MAX_VALUE, authorNid, getModuleNid(),
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
            this.primordialStamp = PersistentStore.get().getStamp(getStatus(), Long.MAX_VALUE, getAuthorNid(), moduleId,
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
            this.primordialStamp = PersistentStore.get().getStamp(getStatus(), Long.MAX_VALUE, getAuthorNid(), getModuleNid(),
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
     * @param stamp
     */
    public void setSTAMP(int stamp) {
        this.primordialStamp = stamp;
        assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        modified();
    }

    /**
     * Method description
     *
     *
     * @param status
     */
    @Override
    public final void setStatus(Status status) {
        if (getStatus() != null && getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (status != this.getStatus()) {
            this.primordialStamp = PersistentStore.get().getStamp(status, Long.MAX_VALUE, getAuthorNid(), getModuleNid(),
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
            this.primordialStamp = PersistentStore.get().getStamp(getStatus(), time, getAuthorNid(), getModuleNid(),
                    getPathNid());
            assert primordialStamp != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

}
