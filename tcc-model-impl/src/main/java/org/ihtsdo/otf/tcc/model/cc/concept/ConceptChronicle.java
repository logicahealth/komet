package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ProcessComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.contradiction.strategy.IdentifyAllConflict;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.LanguageSort;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetPolicy;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.*;
import org.ihtsdo.otf.tcc.model.cc.LanguageSortPrefs.LANGUAGE_SORT_PREF;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributesVersion;
import org.ihtsdo.otf.tcc.model.cc.change.LastChange;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.concept.processor.AdjudicationAnalogCreator;
import org.ihtsdo.otf.tcc.model.cc.concept.processor.VersionFlusher;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionVersion;
import org.ihtsdo.otf.tcc.model.cc.media.Media;
import org.ihtsdo.otf.tcc.model.cc.media.MediaVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipVersion;
import org.ihtsdo.otf.tcc.model.cc.relationship.group.RelGroupChronicle;
import org.ihtsdo.otf.tcc.model.cc.relationship.group.RelGroupVersion;
import org.ihtsdo.otf.tcc.model.cc.termstore.PersistentStoreI;
import org.ihtsdo.otf.tcc.model.jsr166y.ConcurrentReferenceHashMap;

public class ConceptChronicle implements ConceptChronicleBI, Comparable<ConceptChronicle> {

    protected static final Logger logger = Logger.getLogger(ConceptChronicle.class.getName());
    private static int fsXmlDescNid = Integer.MIN_VALUE;
    private static int fsDescNid = Integer.MIN_VALUE;
    public static ConcurrentReferenceHashMap<Integer, Object> componentsCRHM;
    public static ConcurrentReferenceHashMap<Integer, ConceptChronicle> conceptsCRHM;
    private static NidSet rf2LangRefexNidSet;
    private static List<TtkRefexAbstractMemberChronicle<?>> unresolvedAnnotations;
    private static List<TtkRefexDynamicMemberChronicle> unresolvedAnnotationsDynamic;

    //~--- fields --------------------------------------------------------------
    private boolean canceled = false;
    NidSetBI allowedStatus;
    NidSetBI allowedTypes;
    ContradictionManagerBI contradictionManager;
    private I_ManageConceptData data;
    protected int hashCode;
    protected int nid;
    Precedence precedencePolicy;

    //~--- constructors --------------------------------------------------------
//    TODO-AKF: can this be private?
    public ConceptChronicle() {
        lazyInit();
    }

    private static void lazyInit() {
        if (conceptsCRHM == null) {
            init();
        }
    }

    public ConceptChronicle(int nid) throws IOException {
        super();
        lazyInit();
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
        this.nid = nid;
        this.hashCode = Hashcode.compute(nid);
        this.data = PersistentStore.get().getConceptData(nid);
    }

    public static final int DATA_VERSION = 0;

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
        return getConceptAttributes().addAnnotation(annotation);
    }

    @Override
    public boolean addDynamicAnnotation(RefexDynamicChronicleBI<?> annotation) throws IOException {
        return getConceptAttributes().addDynamicAnnotation(annotation);
    }

    public boolean addMemberNid(int nid) throws IOException {
        Set<Integer> memberNids = data.getMemberNids();

        if (!memberNids.contains(nid)) {
            memberNids.add(nid);
            modified();

            return true;
        }

        return false;
    }

    @Override
    public void cancel() throws IOException {
        LastChange.touchComponents(getConceptNidsAffectedByCommit());
        data.cancel();

        if (PersistentStore.get().forget(getConceptAttributes())) {
            canceled = true;
        }
    }

    private void collectPossibleKindOf(NidSetBI isATypes, NativeIdSetBI possibleKindOfConcepts, int cNid)
            throws IOException {
        for (int cNidForOrigin : PersistentStore.get().getDestRelOriginNids(cNid, isATypes)) {
            if (possibleKindOfConcepts.isMember(cNidForOrigin) == false) {
                possibleKindOfConcepts.setMember(cNidForOrigin);
                collectPossibleKindOf(isATypes, possibleKindOfConcepts, cNidForOrigin);
            }
        }
    }

    @Override
    public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
            throws IOException {
        this.modified();

        return PersistentStore.get().commit(this, ChangeSetPolicy.get(changeSetPolicy),
                ChangeSetWriterThreading.get(changeSetWriterThreading));
    }

    public boolean commit(ChangeSetPolicy changeSetPolicy, ChangeSetWriterThreading changeSetWriterThreading)
            throws IOException {
        return PersistentStore.get().commit(this, changeSetPolicy, changeSetWriterThreading);
    }

    @Override
    public int compareTo(ConceptChronicle o) {
        return getNid() - o.getNid();
    }

    public static void disableComponentsCRHM() {
        componentsCRHM = new ConcurrentReferenceHashMap<Integer, Object>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK) {
                    @Override
                    public Object put(Integer key, Object value) {
                        return null;
                    }

                    @Override
                    public void putAll(Map<? extends Integer, ? extends Object> m) {
                        // nothing to do;
                    }

                    @Override
                    public Object putIfAbsent(Integer key, Object value) {
                        return null;
                    }

                    @Override
                    public boolean replace(Integer key, Object oldValue, Object newValue) {
                        return false;
                    }

                    @Override
                    public Object replace(Integer key, Object value) {
                        return false;
                    }
                };
    }

    public static void enableComponentsCRHM() {
        componentsCRHM = new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (ConceptChronicle.class.isAssignableFrom(obj.getClass())) {
            ConceptChronicle another = (ConceptChronicle) obj;

            return nid == another.nid;
        }

        return false;
    }

    public void flushVersions() throws Exception {
        processComponentChronicles(new VersionFlusher());
    }

    private void formatCollection(StringBuffer buff, Collection<?> list) {
        if ((list != null) && (list.size() > 0)) {
            buff.append("[\n");

            for (Object obj : list) {
                buff.append("   ");
                buff.append(obj);
                buff.append(",\n");
            }

            buff.append("]");
        } else {
            buff.append("[]");
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
    private static Semaphore initPermit = new Semaphore(1);

    private static void init() {
        initPermit.acquireUninterruptibly();
        try {
            if (conceptsCRHM == null) {
                System.out.println("### Starting map initialization.");
                conceptsCRHM = new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                        ConcurrentReferenceHashMap.ReferenceType.WEAK);
                componentsCRHM = new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                        ConcurrentReferenceHashMap.ReferenceType.WEAK);
                unresolvedAnnotations = new ArrayList<>();
                unresolvedAnnotationsDynamic = new ArrayList<>();
                fsXmlDescNid = Integer.MIN_VALUE;
                fsDescNid = Integer.MIN_VALUE;

                rf2LangRefexNidSet = new NidSet();
                rf2LangRefexNidSet.add(ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid());
                rf2LangRefexNidSet.add(ReferenceConcepts.SYNONYM_RF2.getNid());
                System.out.println("### Finished map initialization.");
            }
        } finally {
            initPermit.release();
        }
    }

    @Override
    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
        AdjudicationAnalogCreator aac = new AdjudicationAnalogCreator(ec, vc);

        processComponentChronicles(aac);

        return aac.isComponentChanged();
    }

    public ConceptCB makeBlueprint(ViewCoordinate vc,
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        ConceptCB cab = new ConceptCB(getVersion(vc), idDirective, refexDirective);

        return cab;
    }

    public static ConceptChronicle mergeAndWrite(TtkConceptChronicle eConcept)
            throws IOException {
        int conceptNid = PersistentStore.get().getNidForUuids(eConcept.getPrimordialUuid());

        assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

        ConceptChronicle c = get(conceptNid);

        mergeWithEConcept(eConcept, c);
        PersistentStore.get().addUncommittedNoChecks(c);

        return c;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ConceptChronicle mergeWithEConcept(TtkConceptChronicle eConcept, ConceptChronicle c)
            throws IOException {
        if (c.isAnnotationStyleRefex() == false) {
            c.setAnnotationStyleRefex(eConcept.isAnnotationStyleRefex());
        }
        boolean primordial = c.data.isPrimordial();
        
        TtkConceptAttributesChronicle eAttr = eConcept.getConceptAttributes();

        
        if (eAttr != null) {
            if (primordial || c.getConceptAttributes() == null) {
                setAttributesFromEConcept(c, eAttr);
            }
        }

        if ((eConcept.getDescriptions() != null) && !eConcept.getDescriptions().isEmpty()) {
            if (primordial || (c.getDescriptions() == null) || c.getDescriptions().isEmpty()) {
                System.out.println("### DEBUG: setting descriptions from eConcept");
                setDescriptionsFromEConcept(eConcept, c);
            } else {
                System.out.println("### DEBUG: merging descriptions with eConcept");
                Set<Integer> currentDNids = c.data.getDescNids();

                for (TtkDescriptionChronicle ed : eConcept.getDescriptions()) {
                    System.out.println("### DEBUG: econcept description uuid is: " + ed.primordialUuid);
                    int dNid = PersistentStore.get().getNidForUuids(ed.primordialUuid);
                    System.out.println("### DEBUG: associated nid is: " + dNid);
                    
                    if (currentDNids.contains(dNid)) {
                        System.out.println("###DEBUG: contains nid, merging");
                        Description d = c.getDescription(dNid);

                        d.merge(new Description(ed, c));
                    } else {
                        System.out.println("###DEBUG: creating new description");
                        c.getDescriptions().add(new Description(ed, c));
                    }
                }
            }
        }

        if ((eConcept.getRelationships() != null) && !eConcept.getRelationships().isEmpty()) {
            if (primordial || (c.getNativeSourceRels() == null) || c.getNativeSourceRels().isEmpty()) {
                setRelationshipsFromEConcept(eConcept, c);
            } else {
                Set<Integer> currentSrcRelNids = c.data.getSrcRelNids();

                for (TtkRelationshipChronicle er : eConcept.getRelationships()) {
                    int rNid = PersistentStore.get().getNidForUuids(er.primordialUuid);

                    if (currentSrcRelNids.contains(rNid)) {
                        Relationship r = c.getRelationship(rNid);

                        r.merge(new Relationship(er, c));
                    } else {
                        c.getNativeSourceRels().add(new Relationship(er, c));
                    }
                }
            }
        }
        try{
        if ((eConcept.getMedia() != null) && !eConcept.getMedia().isEmpty()) {
            if (primordial || (c.getImages() == null) || c.getImages().isEmpty()) {
                setImagesFromEConcept(eConcept, c);
            } else {
                Set<Integer> currentImageNids = c.data.getImageNids();

                    for (TtkMediaChronicle eImg : eConcept.getMedia()) {
                        int iNid = PersistentStore.get().getNidForUuids(eImg.primordialUuid);

                        if (currentImageNids.contains(iNid)) {
                            Media img = c.getImage(iNid);

                            img.merge(new Media(eImg, c));
                        } else {
                            c.getImages().add(new Media(eImg, c));
                        }
                    }
                }
            }
        } catch (NullPointerException e) { //TODO-AKF: fix this
            System.out.println("Image not supported yet");
        }
        if (!eConcept.getRefsetMembers().isEmpty()) {
            if (c.isAnnotationStyleRefex()) {
                for (TtkRefexAbstractMemberChronicle<?> er : eConcept.getRefsetMembers()) {
                    ConceptComponent cc;
                    Object referencedComponent = PersistentStore.get().getComponent(er.getComponentUuid());

                    if (referencedComponent != null) {
                        if (referencedComponent instanceof ConceptChronicle) {
                            cc = ((ConceptChronicle) referencedComponent).getConceptAttributes();
                        } else {
                            cc = (ConceptComponent) referencedComponent;
                        }

                        RefexMember r = (RefexMember) PersistentStore.get().getComponent(er.getPrimordialComponentUuid());

                        if (r == null) {
                            cc.addAnnotation(RefexMemberFactory.create(er, PersistentStore.get().getConceptNidForNid(cc.getNid())));
                        } else {
                            r.merge((RefexMember) RefexMemberFactory.create(er,
                                    PersistentStore.get().getConceptNidForNid(cc.getNid())));
                        }
                    } else {
                        unresolvedAnnotations.add(er);
                    }
                }
            } else {
                if (c.getRefsetMembers().isEmpty()) {
                    setRefsetMembersFromEConcept(eConcept, c);
                } else {
                    Set<Integer> currentMemberNids = c.data.getMemberNids();

                    for (TtkRefexAbstractMemberChronicle<?> er : eConcept.getRefsetMembers()) {
                        int rNid = PersistentStore.get().getNidForUuids(er.primordialUuid);
                        RefexMember<?, ?> r = c.getRefsetMember(rNid);

                        if (currentMemberNids.contains(rNid) && (r != null)) {
                            r.merge((RefexMember) RefexMemberFactory.create(er, c.getNid()));
                        } else {
                            c.getRefsetMembers().add(RefexMemberFactory.create(er, c.getNid()));
                        }
                    }
                }
            }
        }

        if (!eConcept.getRefsetMembersDynamic().isEmpty()) {
            if (c.isAnnotationStyleRefex()) {
                for (TtkRefexDynamicMemberChronicle er : eConcept.getRefsetMembersDynamic()) {
                    ConceptComponent cc;
                    Object referencedComponent = PersistentStore.get().getComponent(er.getComponentUuid());

                    if (referencedComponent != null) {
                        if (referencedComponent instanceof ConceptChronicle) {
                            cc = ((ConceptChronicle) referencedComponent).getConceptAttributes();
                        } else {
                            cc = (ConceptComponent) referencedComponent;
                        }

                        RefexDynamicMember r = (RefexDynamicMember) PersistentStore.get().getComponent(er.getPrimordialComponentUuid());

                        if (r == null) {
                            cc.addDynamicAnnotation(RefexDynamicMemberFactory.create(er, PersistentStore.get().getConceptNidForNid(cc.getNid())));
                        } else {
                            r.merge((RefexDynamicMember) RefexDynamicMemberFactory.create(er,
                                    PersistentStore.get().getConceptNidForNid(cc.getNid())));
                        }
                    } else {
                        unresolvedAnnotationsDynamic.add(er);
                    }
                }
            } else {
                if (c.getRefsetDynamicMembers().isEmpty()) {
                    setRefsetMembersDynamicFromEConcept(eConcept, c);
                } else {
                    Set<Integer> currentMemberNids = c.data.getMemberNids();

                    for (TtkRefexDynamicMemberChronicle er : eConcept.getRefsetMembersDynamic()) {
                        int rNid = PersistentStore.get().getNidForUuids(er.primordialUuid);
                        RefexDynamicMember r = c.getRefsetDynamicMember(rNid);

                        if (currentMemberNids.contains(rNid) && (r != null)) {
                            r.merge((RefexDynamicMember) RefexDynamicMemberFactory.create(er, c.getNid()));
                        } else {
                            c.getRefsetDynamicMembers().add(RefexDynamicMemberFactory.create(er, c.getNid()));
                        }
                    }
                }
            }
        }

        return c;
    }

    public void modified() {
        data.modified();
    }

    public void modified(long sequence) {
        data.modified(sequence);
    }

    private static ConceptChronicle populateFromEConcept(TtkConceptChronicle eConcept, ConceptChronicle c) throws IOException {
        if (eConcept.getConceptAttributes() != null) {
            setAttributesFromEConcept(c, eConcept.getConceptAttributes());
        }

        if (eConcept.getDescriptions() != null) {
            setDescriptionsFromEConcept(eConcept, c);
        }

        if (eConcept.getRelationships() != null) {
            setRelationshipsFromEConcept(eConcept, c);
        }

        if (eConcept.getMedia() != null) {
            setImagesFromEConcept(eConcept, c);
        }

        if (eConcept.getRefsetMembers() != null) {
            setRefsetMembersFromEConcept(eConcept, c);
        }

        if (eConcept.getRefsetMembersDynamic() != null) {
            setRefsetMembersDynamicFromEConcept(eConcept, c);
        }

        return c;
    }

    private static class SetIndexedProcessor implements ProcessComponentChronicleBI {

        @Override
        public void process(ComponentChronicleBI cc) throws Exception {
            ((ConceptComponent) cc).setIndexed();
        }

    }

    private static class IsIndexedProcessor implements ProcessComponentChronicleBI {

        boolean indexed = true;

        @Override
        public void process(ComponentChronicleBI cc) throws Exception {
            if (!((ConceptComponent) cc).isIndexed()) {
                indexed = false;
            }
        }

    }

    public boolean isIndexed() {
        try {
            IsIndexedProcessor p = new IsIndexedProcessor();
            processComponentChronicles(p);
            return p.indexed;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setIndexed() {
        try {
            processComponentChronicles(new SetIndexedProcessor());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void processComponentChronicles(ProcessComponentChronicleBI processor) throws Exception {
        if (getConceptAttributes() != null) {
            processComponentChronicles(getConceptAttributes(), processor);
        }

        if (getDescriptions() != null) {
            for (ComponentChronicleBI cc : getDescriptions()) {
                processComponentChronicles(cc, processor);
            }
        }

        if (getNativeSourceRels() != null) {
            for (ComponentChronicleBI cc : getNativeSourceRels()) {
                processComponentChronicles(cc, processor);
            }
        }

        if (getImages() != null) {
            for (ComponentChronicleBI cc : getImages()) {
                processComponentChronicles(cc, processor);
            }
        }

        if (getRefsetMembers() != null) {
            for (ComponentChronicleBI cc : getRefsetMembers()) {
                processComponentChronicles(cc, processor);
            }
        }
    }

    private void processComponentChronicles(ComponentChronicleBI cc,
            ProcessComponentChronicleBI processor) throws Exception {
        processor.process(cc);
        for (RefexChronicleBI refex : cc.getAnnotations()) {
            processComponentChronicles(refex, processor);
        }
    }

    public boolean readyToWrite() {
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
        assert data.readyToWrite() : toLongString();

        return true;
    }

    public static void reset() {
        init();
    }

//    public void resetNidData() {  //TODO-AKF: I think this is just for BDB implementation, ConceptDataSimpleReference has a resetNidData method
//        data.resetNidData();
//    }
    public static void resolveUnresolvedAnnotations(Set<ConceptChronicleBI> indexedAnnotationConcepts) throws IOException {
        List<TtkRefexAbstractMemberChronicle<?>> cantResolve = new ArrayList<>();

        for (TtkRefexAbstractMemberChronicle<?> er : unresolvedAnnotations) {
            ConceptComponent cc;
            Object referencedComponent = PersistentStore.get().getComponent(er.getComponentUuid());

            if (referencedComponent != null) {
                if (referencedComponent instanceof ConceptChronicle) {
                    cc = ((ConceptChronicle) referencedComponent).getConceptAttributes();
                } else {
                    cc = (ConceptComponent) referencedComponent;
                }

                RefexMember r = (RefexMember) PersistentStore.get().getComponent(er.getPrimordialComponentUuid());

                if (r == null) {
                    cc.addAnnotation(RefexMemberFactory.create(er, PersistentStore.get().getConceptNidForNid(cc.getNid())));
                } else {
                    r.merge((RefexMember) RefexMemberFactory.create(er, PersistentStore.get().getConceptNidForNid(cc.getNid())));
                }
            }
        }

        for (TtkRefexDynamicMemberChronicle er : unresolvedAnnotationsDynamic) {
            ConceptComponent cc;
            Object referencedComponent = PersistentStore.get().getComponent(er.getComponentUuid());

            if (referencedComponent != null) {
                if (referencedComponent instanceof ConceptChronicle) {
                    cc = ((ConceptChronicle) referencedComponent).getConceptAttributes();
                } else {
                    cc = (ConceptComponent) referencedComponent;
                }

                RefexDynamicMember r = (RefexDynamicMember) PersistentStore.get().getComponent(er.getPrimordialComponentUuid());

                if (r == null) {
                    cc.addDynamicAnnotation(RefexDynamicMemberFactory.create(er, PersistentStore.get().getConceptNidForNid(cc.getNid())));
                } else {
                    r.merge((RefexDynamicMember) RefexDynamicMemberFactory.create(er, PersistentStore.get().getConceptNidForNid(cc.getNid())));
                }
            }
        }

        if (!cantResolve.isEmpty()) {
            logger.log(Level.SEVERE, "Can't resolve annotations on import",
                    new Exception("Can't resolve some annotations on import: " + cantResolve));
        }
    }

    /**
     * Returns a longer - more complete - string representation of the object.
     *
     * @return
     */
    @Override
    public String toLongString() {
        StringBuffer buff = new StringBuffer();

        try {
            buff.append("\nConcept: \"");
            buff.append(getText());
            buff.append("\" nid: ");
            buff.append(nid);
            buff.append(" annotationRefset: ");
            buff.append(isAnnotationStyleRefex());
            buff.append("\n uncommitted: ");
            buff.append(isUncommitted());
            buff.append("\n unwritten: ");
            buff.append(isUnwritten());
            buff.append("\n attributes: ");
            buff.append(getConceptAttributes());
            buff.append("\n descriptions: ");
            formatCollection(buff, getDescriptions());
            buff.append("\n srcRels: ");
            formatCollection(buff, getNativeSourceRels());
            buff.append("\n images: ");
            formatCollection(buff, getImages());

            if (!isAnnotationStyleRefex()) {
                buff.append("\n refset members: ");
                formatCollection(buff, getExtensions());
                buff.append("\n refset dynamic members: ");
                formatCollection(buff, getExtensionsDynamic());
            }

            buff.append("\n desc nids: ");
            buff.append(data.getDescNids());
            buff.append("\n src rel nids: ");
            buff.append(data.getSrcRelNids());
            buff.append("\n member nids: ");
            buff.append(data.getMemberNids());
            buff.append("\n image nids: ");
            buff.append(data.getImageNids());
            buff.append("\n");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException ", e);
        }

        return buff.toString();
    }

    @Override
    public String toString() {
        try {
            if (!isCanceled()) {
                return getText();
            }

            return "canceled concept";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in toString().", ex);

            return ex.toString();
        }
    }

    @Override
    public String toUserString() {
        try {
            if (!isCanceled()) {
                return getText();
            }

            return "canceled concept";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in toUserSTring()", ex);

            return ex.toString();
        }
    }

    //TODO [REFEX] why are there no calls to this method anywhere?
    public void updateXrefs() throws IOException {
        for (RefexMember<?, ?> m : getRefsetMembers()) {
            NidPairForRefex npr = NidPair.getRefexNidMemberNidPair(m.getAssemblageNid(), m.getNid());

            PersistentStore.get().addXrefPair(m.referencedComponentNid, npr);
        }

        for (RefexDynamicMember m : getRefsetDynamicMembers()) {
            NidPairForRefex npr = NidPair.getRefexNidMemberNidPair(m.getAssemblageNid(), m.getNid());

            PersistentStore.get().addXrefPair(m.referencedComponentNid, npr);
        }
    }

    public static ConceptChronicle get(int nid) throws IOException {
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
        lazyInit();
        ConceptChronicle c = conceptsCRHM.get(nid);

        if (c == null) {
            ConceptChronicle newC = new ConceptChronicle(nid);

            c = conceptsCRHM.putIfAbsent(nid, newC);

            if (c == null) {
                c = newC;
            }
        }

        conceptsCRHM.put(nid, c);

        return c;
    }

    public static ConceptChronicle get(TtkConceptChronicle eConcept)
            throws IOException {
        int conceptNid;
        PersistentStoreI store = Hk2Looker.get().getService(PersistentStoreI.class);
        if (store.hasUuid(eConcept.getPrimordialUuid())) {
            conceptNid = PersistentStore.get().getNidForUuids(eConcept.getPrimordialUuid());
        } else if (eConcept.getConceptAttributes() != null) {
            conceptNid = PersistentStore.get().getNidForUuids(eConcept.getConceptAttributes().getUuids());
        } else {
            conceptNid = PersistentStore.get().getNidForUuids(eConcept.getPrimordialUuid());
        }
        PersistentStore.get().setConceptNidForNid(conceptNid, conceptNid);
        assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

        ConceptChronicle c = get(conceptNid);
        
        System.out.println("### DEBUG: Concept descriptions should be empty. Found " + c.getDescriptions().size() +" descriptions.");
        
        // return populateFromEConcept(eConcept, c);
        try {
            return mergeWithEConcept(eConcept, c);
        } catch (Exception t) {
            System.out.println(t.getLocalizedMessage());
            logger.log(Level.SEVERE, "Cannot merge with eConcept: \n" + eConcept, t);
        }

        return null;
    }

    @Override
    public Collection<? extends IdBI> getAdditionalIds() throws IOException {
        return getConceptAttributes().getAdditionalIds();
    }

    @Override
    public Collection<? extends IdBI> getAllIds() throws IOException {
        return getConceptAttributes().getAllIds();
    }

    public Collection<Integer> getAllNids() throws IOException {
        return data.getAllNids();
    }

    public Collection<? extends RelGroupChronicleBI> getAllRelGroups() throws IOException {
        ArrayList<RelGroupChronicleBI> results = new ArrayList<>();
        Map<Integer, HashSet<RelationshipChronicleBI>> statedGroupMap = new HashMap<>();
        Map<Integer, HashSet<RelationshipChronicleBI>> inferredGroupMap = new HashMap<>();

        for (RelationshipChronicleBI r : getRelationshipsOutgoing()) {

            // Inferred
            for (RelationshipVersionBI rv : r.getVersions()) {
                int group = rv.getGroup();

                if (group > 0) {
                    if (rv.isInferred()) {
                        HashSet<RelationshipChronicleBI> relsInGroup = inferredGroupMap.get(group);

                        if (relsInGroup == null) {
                            relsInGroup = new HashSet<>();
                            inferredGroupMap.put(group, relsInGroup);
                        }

                        relsInGroup.add(r);
                    } else {
                        HashSet<RelationshipChronicleBI> relsInGroup = statedGroupMap.get(group);

                        if (relsInGroup == null) {
                            relsInGroup = new HashSet<>();
                            statedGroupMap.put(group, relsInGroup);
                        }

                        relsInGroup.add(r);
                    }
                }
            }
        }

        for (Entry<Integer, HashSet<RelationshipChronicleBI>> groupEntry : statedGroupMap.entrySet()) {
            results.add(new RelGroupChronicle(this, groupEntry.getKey(), groupEntry.getValue()));
        }

        for (Entry<Integer, HashSet<RelationshipChronicleBI>> groupEntry : inferredGroupMap.entrySet()) {
            results.add(new RelGroupChronicle(this, groupEntry.getKey(), groupEntry.getValue()));
        }

        return results;
    }

    @Override
    public Set<Integer> getAllStamps() throws IOException {
        Set<Integer> sapNids = new HashSet<>();

        if (getConceptAttributes() != null) {
            sapNids.addAll(getConceptAttributes().getComponentStamps());
        }

        if (getDescriptions() != null) {
            for (Description d : getDescriptions()) {
                sapNids.addAll(d.getComponentStamps());
            }
        }

        if (getRelationshipsOutgoing() != null) {
            for (Relationship r : getNativeSourceRels()) {
                sapNids.addAll(r.getComponentStamps());
            }
        }

        if (getImages() != null) {
            for (Media i : getImages()) {
                sapNids.addAll(i.getComponentStamps());
            }
        }

        return sapNids;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
        return getConceptAttributes().getAnnotations();
    }

    @Override
    public ComponentChronicleBI<?> getComponent(int nid) throws IOException {
        return data.getComponent(nid);
    }

    @Override
    public ConceptAttributes getConceptAttributes() throws IOException {
        if (data != null) {
            return data.getConceptAttributes();
        }

        return null;
    }

    public Collection<ConceptAttributesVersion> getConceptAttrVersions(EnumSet<Status> allowedStatus,
            Position viewPosition, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ArrayList<>();
        }

        List<ConceptAttributesVersion> versions = new ArrayList<>(2);

        versions.addAll(getConceptAttributes().getVersions(allowedStatus, viewPosition, precedence,
                contradictionMgr));

        return versions;
    }

    public ArrayList<ConceptAttributes> getConceptAttributesList() throws IOException {
        ArrayList<ConceptAttributes> returnList = new ArrayList<>(1);

        returnList.add(getConceptAttributes());

        return returnList;
    }

    @Override
    public int getConceptNid() {
        return nid;
    }

    public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException {
        return data.getConceptNidsAffectedByCommit();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate vc)
            throws IOException {
        return getConceptAttributes().getAnnotationsActive(vc);
    }

    @Override
    public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
            Class<T> cls)
            throws IOException {
        return getConceptAttributes().getAnnotationsActive(xyz, cls);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz,
            int refexNid)
            throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getAnnotationsActive(xyz, refexNid);
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
            int refexNid, Class<T> cls)
            throws IOException {
        return getConceptAttributes().getAnnotationsActive(xyz, refexNid, cls);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getRefexMembersActive(xyz, refsetNid);
        }

        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getRefexMembersActive(xyz);
        }

        return new ArrayList<>(0);
    }

    @Override
    public RefexVersionBI<?> getCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid)
            throws IOException {
        if (isCanceled()) {
            return null;
        }

        RefexChronicleBI<?> member = getRefsetMemberForComponent(componentNid);

        for (RefexVersionBI version : member.getVersions(vc)) {
            return version;
        }

        return null;
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefsetMembers();
        List<RefexVersionBI<?>> returnValues = new ArrayList<>(refexes.size());

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(vc)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc, Long cuttoffTime)
            throws IOException {
        Collection<RefexMember<?, ?>> refsetMembers = getRefsetMembers();
        List<RefexVersionBI<?>> returnValues = new ArrayList<>(refsetMembers.size());

        for (RefexMember refex : refsetMembers) {
            for (Object o : refex.getVersions(vc, cuttoffTime)) {
                RefexVersionBI version = (RefexVersionBI) o;

                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    public I_ManageConceptData getData() {
        return data;
    }

    public DescriptionVersion getDesc(NidListBI typePrefOrder, NidListBI langPrefOrder,
            EnumSet<Status> allowedStatus, Position viewPosition,
            LANGUAGE_SORT_PREF sortPref, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException {
        DescriptionVersion result;

        switch (sortPref) {
            case TYPE_B4_LANG:
                result = getTypePreferredDesc(getDescriptionVersions(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), viewPosition, precedencePolicy,
                        contradictionManager), typePrefOrder);

                if (result != null) {
                    return result;
                }

                if ((getDescriptions() != null) && (getDescriptions().size() > 0)) {
                    return (DescriptionVersion) getDescriptions().iterator().next().getVersions().iterator().next();
                }

                return null;

            case LANG_REFEX:
                result = getRefexSpecifiedDesc(getDescriptionVersions(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), viewPosition, precedencePolicy,
                        contradictionManager), typePrefOrder, langPrefOrder, allowedStatus, viewPosition);

                if (result != null) {
                    return result;
                }

                return getDesc(typePrefOrder, langPrefOrder, allowedStatus, viewPosition,
                        LANGUAGE_SORT_PREF.TYPE_B4_LANG, precedencePolicy, contradictionManager);

            case RF2_LANG_REFEX:
                result = getRf2RefexSpecifiedDesc(getDescriptionVersions(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), viewPosition, precedencePolicy,
                        contradictionManager), typePrefOrder, langPrefOrder, allowedStatus, viewPosition);

                if (result != null) {
                    return result;
                }

                return getDesc(typePrefOrder, langPrefOrder, allowedStatus, viewPosition,
                        LANGUAGE_SORT_PREF.LANG_REFEX, precedencePolicy, contradictionManager);

            default:
                throw new IOException("Can't handle sort type: " + sortPref);
        }
    }

    public Description getDescription(int nid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        for (Description d : getDescriptions()) {
            if (d.getNid() == nid) {
                return d;
            }
        }

        throw new IOException("No description: " + nid + " " + PersistentStore.get().getUuidsForNid(nid) + " found in\n"
                + toLongString());
    }

    public Set<Integer> getDescriptionNids() throws IOException {
        return data.getDescNids();
    }

    public Collection<DescriptionVersion> getDescriptionVersions(EnumSet<Status> allowedStatus,
            NidSetBI allowedTypes, Position viewPosition, Precedence precedence,
            ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<>(new ComponentComparator());
        }

        Collection<Description> descriptions = getDescriptions();
        List<DescriptionVersion> versions = new ArrayList<>(descriptions.size());

        for (Description d : descriptions) {
            versions.addAll(d.getVersions(allowedStatus, allowedTypes, viewPosition, precedence,
                    contradictionMgr));
        }

        return versions;
    }

    @Override
    public Collection<Description> getDescriptions() throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<>(new ComponentComparator());
        }

        return data.getDescriptions();
    }

    public Collection<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException {
        if (isCanceled()) {
            return new ArrayList<>();
        }

        return data.getDestRels(allowedTypes);
    }

    @Override
    public ConceptChronicle getEnclosingConcept() {
        return this;
    }

    public RefexMember<?, ?> getExtension(int componentNid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        return data.getRefsetMemberForComponent(componentNid);
    }

    public Collection<RefexMember<?, ?>> getExtensions() throws IOException {
        if (isCanceled()) {
            return new ArrayList<>();
        }

        return data.getRefsetMembers();
    }

    public Collection<RefexDynamicMember> getExtensionsDynamic() throws IOException {
        if (isCanceled()) {
            return new ArrayList<>();
        }

        return data.getRefsetDynamicMembers();
    }

    public static ConceptChronicle getIfInMap(int nid) {
        return conceptsCRHM.get(nid);
    }

    public Media getImage(int nid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        for (Media i : data.getMedia()) {
            if (i.getNid() == nid) {
                return i;
            }
        }

        return null;
    }

    public Collection<Media> getImages() throws IOException {
        return data.getMedia();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
        return getConceptAttributes().getRefexMembersInactive(xyz);
    }

    @Override
    public Collection<Media> getMedia() throws IOException {
        return getImages();
    }

    public Collection<MediaVersion> getMediaVersions(EnumSet<Status> allowedStatus, NidSetBI allowedTypes,
            Position viewPosition, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<>(new ComponentComparator());
        }

        Collection<Media> media = getImages();
        List<MediaVersion> versions = new ArrayList<>(media.size());

        for (Media m : media) {
            versions.addAll(m.getVersions(allowedStatus, allowedTypes, viewPosition, precedence,
                    contradictionMgr));
        }

        return versions;
    }

    public Collection<Relationship> getNativeSourceRels() throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<>(new ComponentComparator());
        }

        return data.getSourceRels();
    }

    @Override
    public int getNid() {
        return nid;
    }

    @Override
    public Set<Position> getPositions() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NativeIdSetBI getPossibleKindOfConcepts(NidSetBI isATypes) throws IOException {
        NativeIdSetBI possibleKindOfConcepts = PersistentStore.get().getEmptyNidSet();

        possibleKindOfConcepts.setMember(getNid());
        collectPossibleKindOf(isATypes, possibleKindOfConcepts, nid);

        return possibleKindOfConcepts;
    }

    private DescriptionVersion getPreferredAcceptability(Collection<DescriptionVersion> descriptions,
            int typePrefNid, ViewCoordinate vc, int langRefexNid)
            throws IOException {

        // get FSN
        DescriptionVersion descOfType = null;

        for (DescriptionVersion d : descriptions) {
            if (d.getTypeNid() == typePrefNid) {
                for (RefexVersionBI<?> refex : d.getRefexMembersActive(vc)) {
                    if (refex.getAssemblageNid() == langRefexNid) {
                        RefexNidVersionBI<?> langRefex = (RefexNidVersionBI<?>) refex;

                        if (langRefex.getNid1()
                                == ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF2.getNid()) {
                            return d;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public UUID getPrimordialUuid() {
        try {
            if (getConceptAttributes() != null) {
                return getConceptAttributes().getPrimordialUuid();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return UUID.fromString("00000000-0000-0000-C000-000000000046");
    }

    @Override
    public ConceptVersionBI getPrimordialVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        return getConceptAttributes().getRefexMembers(refsetNid);
    }

    private DescriptionVersion getRefexSpecifiedDesc(Collection<DescriptionVersion> descriptions,
            NidListBI typePrefOrder, NidListBI langRefexOrder, EnumSet<Status> allowedStatus,
            Position viewPosition)
            throws IOException {
        ViewCoordinate vc = new ViewCoordinate(UUID.randomUUID(), "getRefexSpecifiedDesc", Precedence.PATH,
                viewPosition, allowedStatus, new IdentifyAllConflict(),
                Integer.MIN_VALUE, Integer.MIN_VALUE, RelAssertionType.STATED, langRefexOrder,
                LanguageSort.LANG_REFEX);

        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                for (int typePrefNid : typePrefOrder.getListArray()) {
                    if ((langRefexOrder != null) && (langRefexOrder.getListValues() != null)) {
                        for (int langRefexNid : langRefexOrder.getListValues()) {
                            if (typePrefNid == ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid()) {
                                DescriptionVersion answer = getPreferredAcceptability(descriptions, typePrefNid, vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            } else {

                                // get Preferred or other
                                DescriptionVersion answer = getPreferredAcceptability(descriptions,
                                        ReferenceConcepts.SYNONYM_RF2.getNid(), vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
        return getConceptAttributes().getRefexes();
    }

    public RefexMember<?, ?> getRefsetMember(int memberNid) throws IOException {
        return data.getRefsetMember(memberNid);
    }

    public RefexDynamicMember getRefsetDynamicMember(int memberNid) throws IOException {
        return data.getRefsetDynamicMember(memberNid);
    }

    @Override
    public RefexMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        return data.getRefsetMemberForComponent(componentNid);
    }

    @Override
    public Collection<RefexMember<?, ?>> getRefsetMembers() throws IOException {
        return data.getRefsetMembers();
    }

    @Override
    public ConcurrentSkipListSet<RefexDynamicMember> getRefsetDynamicMembers() throws IOException {
        return (ConcurrentSkipListSet<RefexDynamicMember>) data.getRefsetDynamicMembers();
    }

    @Override
    public Collection<? extends RelGroupVersionBI> getRelationshipGroupsActive(ViewCoordinate vc) throws IOException {
        ArrayList<RelGroupVersionBI> results = new ArrayList<>();

        if (vc.getRelationshipAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
            ViewCoordinate tempVc = new ViewCoordinate(UUID.randomUUID(), "getRelGroups", vc);

            tempVc.setRelationshipAssertionType(RelAssertionType.STATED);
            getRelGroups(tempVc, results);
            tempVc.setRelationshipAssertionType(RelAssertionType.INFERRED);
            getRelGroups(tempVc, results);
        } else {
            getRelGroups(vc, results);
        }

        return results;
    }

    private void getRelGroups(ViewCoordinate vc, ArrayList<RelGroupVersionBI> results) throws IOException {
        Map<Integer, HashSet<RelationshipChronicleBI>> groupMap = new HashMap<>();
        ViewCoordinate tempVc = new ViewCoordinate(UUID.randomUUID(), "getRelGroups", vc);

        tempVc.setAllowedStatus(null);

        for (RelationshipChronicleBI r : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI rv : r.getVersions(tempVc)) {
                int group = rv.getGroup();

                if (group > 0) {
                    HashSet<RelationshipChronicleBI> relsInGroup = groupMap.get(group);

                    if (relsInGroup == null) {
                        relsInGroup = new HashSet<>();
                        groupMap.put(group, relsInGroup);
                    }

                    relsInGroup.add(r);
                }
            }
        }

        for (Entry<Integer, HashSet<RelationshipChronicleBI>> groupEntry : groupMap.entrySet()) {
            results.add(new RelGroupVersion(new RelGroupChronicle(this, groupEntry.getKey(),
                    groupEntry.getValue()), vc));
        }
    }

    public Relationship getRelationship(int relNid) throws IOException {
        for (Relationship r : getNativeSourceRels()) {
            if (r.getNid() == relNid) {
                return r;
            }
        }

        return null;
    }

    @Override
    public Collection<Relationship> getRelationshipsIncoming() throws IOException {
        if (isCanceled()) {
            return new ArrayList<>();
        }

        return data.getDestRels();
    }

    @Override
    public Collection<Relationship> getRelationshipsOutgoing() throws IOException {
        return getNativeSourceRels();
    }

    private DescriptionVersion getRf2RefexSpecifiedDesc(Collection<DescriptionVersion> descriptions,
            NidListBI typePrefOrder, NidListBI langRefexOrder, EnumSet<Status> allowedStatus,
            Position viewPosition)
            throws IOException {
        ViewCoordinate vc = new ViewCoordinate(UUID.randomUUID(), "getRf2RefexSpecifiedDesc", Precedence.PATH,
                viewPosition, allowedStatus, new IdentifyAllConflict(),
                Integer.MIN_VALUE, Integer.MIN_VALUE, RelAssertionType.STATED, langRefexOrder,
                LanguageSort.RF2_LANG_REFEX);

        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                if ((langRefexOrder != null) && (langRefexOrder.getListValues() != null)) {
                    for (int langRefexNid : langRefexOrder.getListValues()) {
                        for (int typePrefNid : typePrefOrder.getListArray()) {
                            if (typePrefNid == ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid()) {
                                DescriptionVersion answer = getPreferredAcceptability(descriptions, typePrefNid, vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            } else if (typePrefNid == ReferenceConcepts.SYNONYM_RF2.getNid()) {

                                // get Preferred or other
                                DescriptionVersion answer = getPreferredAcceptability(descriptions,
                                        ReferenceConcepts.SYNONYM_RF2.getNid(), vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            }
                        }
                    }
                }

                if ((langRefexOrder != null) && (langRefexOrder.getListValues() != null)) {
                    for (int langRefexNid : langRefexOrder.getListValues()) {
                        for (int typePrefNid : typePrefOrder.getListArray()) {
                            if (typePrefNid == ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid()) {
                                DescriptionVersion answer = getPreferredAcceptability(descriptions, typePrefNid, vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            } else if (typePrefNid == ReferenceConcepts.SYNONYM_RF2.getNid()) {

                                // get Preferred or other
                                DescriptionVersion answer = getPreferredAcceptability(descriptions,
                                        ReferenceConcepts.SYNONYM_RF2.getNid(), vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public Collection<RelationshipVersion> getSrcRelVersions(EnumSet<Status> allowedStatus, NidSetBI allowedTypes,
            Position viewPosition, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ArrayList<>();
        }

        Collection<Relationship> rels = getNativeSourceRels();
        List<RelationshipVersion> versions = new ArrayList<>(rels.size());

        for (Relationship r : rels) {
            versions.addAll(r.getVersions(allowedStatus, allowedTypes, viewPosition, precedence,
                    contradictionMgr));
        }

        return versions;
    }

    /**
     * This method is for creating temporary concepts for unit testing only...
     *
     * @param eConcept
     * @return
     * @throws IOException
     */
    public static ConceptChronicle getTempConcept(TtkConceptChronicle eConcept) throws IOException {
        int conceptNid = PersistentStore.get().getNidForUuids(eConcept.getConceptAttributes().getPrimordialComponentUuid());

        assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

        return populateFromEConcept(eConcept, new ConceptChronicle(conceptNid));
    }

    public String getText() {
        try {
            if (getDescriptions().size() > 0) {
                return getDescriptions().iterator().next().getText();
            }

            if (fsDescNid == Integer.MIN_VALUE) {
                fsDescNid = SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID();
            }

            if (getDescriptions().size() > 0) {
                Description desc = getDescriptions().iterator().next();

                for (Description d : getDescriptions()) {
                    for (DescriptionVersion part : d.getVersions()) {
                        if ((part.getTypeNid() == fsDescNid) || (part.getTypeNid() == fsXmlDescNid)) {
                            return part.getText();
                        }
                    }
                }

                return desc.getText();
            } else {
                int sequence = nid + Integer.MIN_VALUE;
                String errString = nid + " (" + sequence + ") " + " has no descriptions " + getUUIDs();

                getDescriptions();

                return errString;
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception getting text", ex);

            return ex.getLocalizedMessage();
        }
    }

    /*
     * (non-Javadoc) @see java.lang.Object#toString()
     */
    private DescriptionVersion getTypePreferredDesc(Collection<DescriptionVersion> descriptions,
            NidListBI typePrefOrder)
            throws IOException {
        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                List<DescriptionVersion> matchedList = new ArrayList<>();

                for (int typeId : typePrefOrder.getListValues()) {
                    for (DescriptionVersion d : descriptions) {
                        if (d.getTypeNid() == typeId) {
                            matchedList.add(d);

                            if (matchedList.size() == 2) {
                                break;
                            }
                        }
                    }

                    if (matchedList.size() > 0) {
                        return matchedList.get(0);
                    }
                }

                return descriptions.iterator().next();
            } else {
                return descriptions.iterator().next();
            }
        }

        return null;
    }

    @Override
    public List<UUID> getUUIDs() {
        try {
            if (getConceptAttributes() != null) {
                return getConceptAttributes().getUUIDs();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>();
    }

    public List<UUID> getUidsForComponent(int componentNid) throws IOException {
        if (getComponent(componentNid) != null) {
            return getComponent(componentNid).getUUIDs();
        }

        logger.log(Level.SEVERE, "Null component for concept.",
                new Exception("Null component: " + componentNid + " for concept: " + this.toLongString()));

        return new ArrayList<>();
    }

    public NidListBI getUncommittedNids() {
        return data.getUncommittedNids();
    }

    @Override
    public ConceptVersion getVersion(ViewCoordinate c) {
        return new ConceptVersion(this, c);
    }

    @Override
    public Collection<? extends ConceptVersionBI> getVersions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<ConceptVersion> getVersions(ViewCoordinate c) {
        ArrayList<ConceptVersion> cvList = new ArrayList<>(1);

        cvList.add(new ConceptVersion(this, c));

        return cvList;
    }

    @Override
    public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refexNid) throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().hasCurrentAnnotationMember(xyz, refexNid);
        }

        return false;
    }

    @Override
    public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().hasCurrentRefexMember(xyz, refsetNid);
        }

        return false;
    }

    @Override
    public boolean hasCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid) throws IOException {
        if (isCanceled()) {
            return false;
        }

        RefexMember<?, ?> member = getRefsetMemberForComponent(componentNid);

        if (member != null) {
            for (RefexVersionBI v : member.getVersions(vc)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasExtensionsForComponent(int nid) throws IOException {
        List<NidPairForRefex> refsetPairs = PersistentStore.get().getRefexPairs(nid);

        if ((refsetPairs != null) && (refsetPairs.size() > 0)) {
            return true;
        }

        return false;
    }

    public boolean hasMediaExtensions() throws IOException {
        if ((data.getImageNids() == null) || data.getImageNids().isEmpty()) {
            return false;
        }

        for (int imageNid : data.getImageNids()) {
            if (hasExtensionsForComponent(imageNid)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAnnotationStyleRefex() throws IOException {
        return data.isAnnotationStyleRefex();
    }

    public boolean isCanceled() throws IOException {
        if (!canceled) {
            if ((getConceptAttributes() != null) && (getConceptAttributes().getPrimordialVersion().getTime() == Long.MIN_VALUE)) {
                canceled = true;
            }
        }

        return canceled;
    }

    public boolean isParentOf(ConceptChronicle child, ViewCoordinate vc) throws IOException, ContradictionException {
        return Ts.get().isKindOf(child.nid, nid, vc);
    }

    public boolean isParentOfOrEqualTo(ConceptChronicle child, ViewCoordinate vc)
            throws IOException, ContradictionException {
        if (child == this) {
            return true;
        }

        return isParentOf(child, vc);
    }

    @Override
    public boolean isUncommitted() {
        return data.isUncommitted();
    }

    public boolean isUnwritten() {
        return data.isUnwritten();
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAnnotationStyleRefex(boolean annotationStyleRefset) {
        data.setIsAnnotationStyleRefex(annotationStyleRefset);
    }

    private static void setAttributesFromEConcept(ConceptChronicle c, TtkConceptAttributesChronicle eAttr) throws IOException {
        assert eAttr != null;

        ConceptAttributes attr = new ConceptAttributes(eAttr, c);

        c.data.setConceptAttributes(attr);
    }

    public NidSetBI setCommitTime(long time) {
        return data.setCommitTime(time);
    }

    public void setConceptAttributes(ConceptAttributes attributes) throws IOException {
        assert attributes.nid != 0;
        nid = attributes.nid;
        data.setConceptAttributes(attributes);
    }

    private static void setDescriptionsFromEConcept(TtkConceptChronicle eConcept, ConceptChronicle c) throws IOException {
        if (c.data.isPrimordial()) {
            HashSet<Description> descs = new HashSet<>();
            for (TtkDescriptionChronicle eDesc : eConcept.getDescriptions()) {
                Description desc = new Description(eDesc, c);
                System.out.println("### DEBUG: adding description (primordial). Description is : " + desc.toSimpleString());
                descs.add(desc);
            }
            c.data.setDescriptions(descs);
        } else {
            for (TtkDescriptionChronicle eDesc : eConcept.getDescriptions()) {
                Description desc = new Description(eDesc, c);
                System.out.println("### DEBUG: adding description. Description is : " + desc.toSimpleString());
                c.data.add(desc);
            }
        }
    }

    private static void setImagesFromEConcept(TtkConceptChronicle eConcept, ConceptChronicle c) throws IOException {
        for (TtkMediaChronicle eImage : eConcept.getMedia()) {
            Media img = new Media(eImage, c);

            c.data.add(img);
        }
    }

    public void setIsCanceled(boolean isCanceled) {
        canceled = isCanceled;
    }

    private static void setRefsetMembersFromEConcept(TtkConceptChronicle eConcept, ConceptChronicle c) throws IOException {
        for (TtkRefexAbstractMemberChronicle<?> eRefsetMember : eConcept.getRefsetMembers()) {
            RefexMember<?, ?> refsetMember = RefexMemberFactory.create(eRefsetMember, c.getConceptNid());

            c.data.add(refsetMember);
        }
    }

    private static void setRefsetMembersDynamicFromEConcept(TtkConceptChronicle eConcept, ConceptChronicle c) throws IOException {
        for (TtkRefexDynamicMemberChronicle eRefsetMember : eConcept.getRefsetMembersDynamic()) {
            RefexDynamicMember refsetMember = RefexDynamicMemberFactory.create(eRefsetMember, c.getConceptNid());

            c.data.add(refsetMember);
        }
    }

    private static void setRelationshipsFromEConcept(TtkConceptChronicle eConcept, ConceptChronicle c) throws IOException {
        if(c.data.isPrimordial()){
            HashSet<Relationship> rels = new HashSet<>();
            for (TtkRelationshipChronicle eRel : eConcept.getRelationships()) {
                Relationship rel = new Relationship(eRel, c);
                rels.add(rel);
            }
            c.data.setSourceRels(rels);
        }else {
            for (TtkRelationshipChronicle eRel : eConcept.getRelationships()) {
                Relationship rel = new Relationship(eRel, c);

                c.data.getSourceRels().add(rel);
            }
        }
    }
    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamic()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexesDynamic() throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getRefexesDynamic();
        }
        return new ArrayList<>();
    }

    /**
     * @see
     * org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicAnnotations()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicAnnotations() throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getRefexDynamicAnnotations();
        }
        return new ArrayList<>();
    }

    /**
     * @see
     * org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicMembers()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicMembers() throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getRefexDynamicMembers();
        }
        return new ArrayList<>();
    }

    /**
     * @see
     * org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamicActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
     */
    @Override
    public Collection<? extends RefexDynamicVersionBI<?>> getRefexesDynamicActive(ViewCoordinate viewCoordinate) throws IOException {
        if (getConceptAttributes() != null) {
            return getConceptAttributes().getRefexesDynamicActive(viewCoordinate);
        }
        return new ArrayList<>();
    }
}
