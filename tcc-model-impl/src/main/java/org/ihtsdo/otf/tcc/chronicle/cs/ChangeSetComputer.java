package org.ihtsdo.otf.tcc.chronicle.cs;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.chronicle.cc.description.Description;
import org.ihtsdo.otf.tcc.chronicle.cc.identifier.IdentifierVersion;
import org.ihtsdo.otf.tcc.chronicle.cc.identifier.IdentifierVersionLong;
import org.ihtsdo.otf.tcc.chronicle.cc.identifier.IdentifierVersionString;
import org.ihtsdo.otf.tcc.chronicle.cc.identifier.IdentifierVersionUuid;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.chronicle.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesRevision;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.media.Media;
import org.ihtsdo.otf.tcc.api.id.LongIdBI;
import org.ihtsdo.otf.tcc.api.id.StringIdBI;
import org.ihtsdo.otf.tcc.api.id.UuidIdBI;
import org.ihtsdo.otf.tcc.chronicle.cs.ChangeSetLogger;
import org.ihtsdo.otf.tcc.chronicle.ComputeEConceptForChangeSetI;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionRevision;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierLong;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaRevision;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipRevision;

public class ChangeSetComputer implements ComputeEConceptForChangeSetI {

    private int minSapNid = Integer.MIN_VALUE;
    private int maxSapNid = Integer.MAX_VALUE;
    private int classifier = ReferenceConcepts.SNOROCKET.getNid();
    private NidSetBI commitSapNids;
    private ChangeSetGenerationPolicy policy;

    //~--- constructors --------------------------------------------------------
    public ChangeSetComputer(ChangeSetGenerationPolicy policy, NidSetBI commitSapNids) {
        super();
        this.policy = policy;

        switch (policy) {
            case COMPREHENSIVE:
                maxSapNid = commitSapNids.getMax();

                break;

            case INCREMENTAL:
                if (!commitSapNids.contiguous()) {
                    this.commitSapNids = commitSapNids;
                }

                maxSapNid = commitSapNids.getMax();
                minSapNid = commitSapNids.getMin();

                break;

            case MUTABLE_ONLY:
                maxSapNid = Integer.MAX_VALUE;
                minSapNid = P.s.getMaxReadOnlyStamp() + 1;

                break;

            default:
                throw new RuntimeException("Can't handle policy: " + policy);
        }

        assert minSapNid <= maxSapNid : "Min not <= max; min: " + minSapNid + " max: " + maxSapNid;
    }

    //~--- methods -------------------------------------------------------------
    private TtkConceptAttributesChronicle processConceptAttributes(ConceptChronicle c, AtomicBoolean changed) throws IOException {
        TtkConceptAttributesChronicle eca = null;

        for (ConceptAttributes.Version v : c.getConceptAttributes().getVersions()) {
            if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                    && (v.getTime() != Long.MAX_VALUE)) {
                changed.set(true);

                if ((commitSapNids == null) || commitSapNids.contains(v.getStamp())) {
                    if (eca == null) {
                        eca = new TtkConceptAttributesChronicle();
                        eca.setDefined(v.isDefined());
                        setupFirstVersion(eca, v);
                    } else {
                        TtkConceptAttributesRevision ecv = new TtkConceptAttributesRevision();

                        ecv.setDefined(v.isDefined());
                        setupRevision(eca, v, ecv);
                    }
                }
            }
        }

        return eca;
    }

    private List<TtkDescriptionChronicle> processDescriptions(ConceptChronicle c, AtomicBoolean changed) throws IOException {
        List<TtkDescriptionChronicle> eDescriptions = new ArrayList<>(c.getDescriptions().size());

        for (Description d : c.getDescriptions()) {
            TtkDescriptionChronicle ecd = null;

            for (Description.Version v : d.getVersions()) {
                if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                        && (v.getTime() != Long.MAX_VALUE)) {
                    changed.set(true);

                    if ((commitSapNids == null) || commitSapNids.contains(v.getStamp())) {
                        if (ecd == null) {
                            ecd = new TtkDescriptionChronicle();
                            eDescriptions.add(ecd);
                            ecd.setConceptUuid(P.s.getUuidPrimordialForNid(v.getConceptNid()));
                            ecd.setInitialCaseSignificant(v.isInitialCaseSignificant());
                            ecd.setLang(v.getLang());
                            ecd.setText(v.getText());
                            ecd.setTypeUuid(P.s.getUuidPrimordialForNid(v.getTypeNid()));
                            setupFirstVersion(ecd, v);
                        } else {
                            TtkDescriptionRevision ecv = new TtkDescriptionRevision();

                            ecv.setInitialCaseSignificant(v.isInitialCaseSignificant());
                            ecv.setLang(v.getLang());
                            ecv.setText(v.getText());
                            ecv.setTypeUuid(P.s.getUuidPrimordialForNid(v.getTypeNid()));
                            setupRevision(ecd, v, ecv);
                        }
                    }
                }
            }
        }

        return eDescriptions;
    }

    private List<TtkMediaChronicle> processMedia(ConceptChronicle c, AtomicBoolean changed) throws IOException {
        List<TtkMediaChronicle> eImages = new ArrayList<>();

        for (Media img : c.getImages()) {
            TtkMediaChronicle eImg = null;

            for (Media.Version v : img.getVersions()) {
                if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                        && (v.getTime() != Long.MAX_VALUE)) {
                    if ((commitSapNids == null) || commitSapNids.contains(v.getStamp())) {
                        changed.set(true);

                        if (eImg == null) {
                            eImg = new TtkMediaChronicle();
                            eImages.add(eImg);
                            eImg.setConceptUuid(P.s.getUuidPrimordialForNid(v.getConceptNid()));
                            eImg.setFormat(v.getFormat());
                            eImg.setDataBytes(v.getMedia());
                            eImg.setTextDescription(v.getTextDescription());
                            eImg.setTypeUuid(P.s.getUuidPrimordialForNid(v.getTypeNid()));
                            setupFirstVersion(eImg, v);
                        } else {
                            TtkMediaRevision eImgR = new TtkMediaRevision();

                            eImgR.setTextDescription(v.getTextDescription());
                            eImgR.setTypeUuid(P.s.getUuidPrimordialForNid(v.getTypeNid()));
                            setupRevision(eImg, v, eImgR);
                        }
                    }
                }
            }
        }

        return eImages;
    }

    private List<TtkRefexAbstractMemberChronicle<?>> processRefsetMembers(ConceptChronicle c, AtomicBoolean changed)
            throws IOException {
        List<TtkRefexAbstractMemberChronicle<?>> eRefsetMembers = new ArrayList<>(c.getRefsetMembers().size());
        Collection<RefexMember<?, ?>> membersToRemove = new ArrayList<>();

        for (RefexMember<?, ?> member : c.getRefsetMembers()) {
            TtkRefexAbstractMemberChronicle<?> eMember = null;
            ConceptChronicle concept = (ConceptChronicle) P.s.getConceptForNid(member.getReferencedComponentNid());

            if ((concept != null) && !concept.isCanceled()) {
                for (RefexMember<?, ?>.Version v : member.getVersions()) {
                    if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                            && (v.getTime() != Long.MAX_VALUE)) {
                        if ((commitSapNids == null) || commitSapNids.contains(v.getStamp())) {
                            changed.set(true);

                            if (eMember == null) {
                                try {
                                    eMember = v.getERefsetMember();

                                    if (eMember != null) {
                                        eRefsetMembers.add(eMember);
                                        setupFirstVersion(eMember, v);
                                    }
                                } catch (Exception e) {
                                    ChangeSetLogger.logger.log(Level.WARNING, "Failed in getting Concept for: " + member.toString() + " and " + v.toString(), e);
                                }
                            } else {
                                TtkRevision eRevision = v.getERefsetRevision();

                                setupRevision(eMember, v, eRevision);
                            }
                        }
                    }
                }
            } else {
                member.primordialStamp = -1;
                membersToRemove.add(member);
            }
        }

        if (!membersToRemove.isEmpty()) {
            P.s.addUncommittedNoChecks(c);
        }

        return eRefsetMembers;
    }

    private List<TtkRelationshipChronicle> processRelationships(ConceptChronicle c, AtomicBoolean changed) throws IOException {
        List<TtkRelationshipChronicle> rels = new ArrayList<>(c.getRelationshipsOutgoing().size());

        for (Relationship r : c.getRelationshipsOutgoing()) {
            TtkRelationshipChronicle ecr = null;

            for (Relationship.Version v : r.getVersions()) {
                if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                        && (v.getTime() != Long.MAX_VALUE) && (v.getAuthorNid() != classifier)) {
                    if ((commitSapNids == null) || commitSapNids.contains(v.getStamp())) {
                        try {
                            changed.set(true);

                            if (ecr == null) {
                                ecr = new TtkRelationshipChronicle();
                                rels.add(ecr);
                                ecr.setC1Uuid(P.s.getUuidPrimordialForNid(v.getC1Nid()));
                                ecr.setC2Uuid(P.s.getUuidPrimordialForNid(v.getC2Nid()));
                                ecr.setCharacteristicUuid(P.s.getUuidPrimordialForNid(v.getCharacteristicNid()));
                                ecr.setRefinabilityUuid(P.s.getUuidPrimordialForNid(v.getRefinabilityNid()));
                                ecr.setRelGroup(v.getGroup());
                                ecr.setTypeUuid(P.s.getUuidPrimordialForNid(v.getTypeNid()));
                                setupFirstVersion(ecr, v);
                            } else {
                                TtkRelationshipRevision ecv = new TtkRelationshipRevision();

                                ecv.setCharacteristicUuid(P.s.getUuidPrimordialForNid(v.getCharacteristicNid()));
                                ecv.setRefinabilityUuid(P.s.getUuidPrimordialForNid(v.getRefinabilityNid()));
                                ecv.setRelGroup(v.getGroup());
                                ecv.setTypeUuid(P.s.getUuidPrimordialForNid(v.getTypeNid()));
                                setupRevision(ecr, v, ecv);
                            }
                        } catch (AssertionError e) {
                            ChangeSetLogger.logger.log(Level.SEVERE, e.getLocalizedMessage(), new Exception(e.getLocalizedMessage() + "\n\n"
                                    + c.toLongString(), e));

                            throw e;
                        }
                    }
                }
            }
        }

        return rels;
    }

    @SuppressWarnings("unchecked")
    private void setupFirstVersion(TtkComponentChronicle ec, ConceptComponent<?, ?>.Version v) throws IOException {
        ec.primordialUuid = v.getPrimordialUuid();
        ec.setPathUuid(P.s.getUuidPrimordialForNid(v.getPathNid()));
        ec.setStatus(v.getStatus());
        ec.setAuthorUuid(P.s.getUuidPrimordialForNid(v.getAuthorNid()));
        ec.setTime(v.getTime());

        if (v.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : v.getAdditionalIdentifierParts()) {
                TtkIdentifier eIdv = null;

                if ((idv.getStamp() >= minSapNid) && (idv.getStamp() <= maxSapNid)
                        && (v.getTime() != Long.MIN_VALUE) && (v.getTime() != Long.MAX_VALUE)) {
                    if (IdentifierVersionLong.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new TtkIdentifierLong();
                    } else if (IdentifierVersionString.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new TtkIdentifierString();
                    } else if (IdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new TtkIdentifierUuid();
                    }

                    eIdv.setDenotation(idv.getDenotation());
                    eIdv.setAuthorityUuid(P.s.getUuidPrimordialForNid(idv.getAuthorityNid()));
                    eIdv.setPathUuid(P.s.getUuidPrimordialForNid(idv.getPathNid()));
                    eIdv.setStatus(idv.getStatus());
                    eIdv.setAuthorUuid(P.s.getUuidPrimordialForNid(idv.getAuthorNid()));
                    eIdv.setTime(idv.getTime());
                }
            }
        }

        if (v.getAnnotations() != null) {
            HashMap<UUID, TtkRefexAbstractMemberChronicle<?>> annotationMap = new HashMap<>();

            if (ec.getAnnotations() != null) {
                for (TtkRefexAbstractMemberChronicle<?> member :
                        (Collection<TtkRefexAbstractMemberChronicle<?>>) ec.getAnnotations()) {
                    annotationMap.put(member.getPrimordialComponentUuid(), member);
                }
            }

            for (RefexMember<?, ?> member : (Collection<RefexMember<?, ?>>) v.getAnnotations()) {
                TtkRefexAbstractMemberChronicle<?> eMember = null;
                ConceptChronicle concept =
                        (ConceptChronicle) P.s.getConceptForNid(member.getReferencedComponentNid());

                if ((concept != null) && !concept.isCanceled()) {
                    for (RefexMember<?, ?>.Version mv : member.getVersions()) {
                        if (mv.stampIsInRange(minSapNid, maxSapNid) && (mv.getTime() != Long.MIN_VALUE)
                                && (mv.getTime() != Long.MAX_VALUE)) {
                            if ((commitSapNids == null) || commitSapNids.contains(mv.getStamp())) {
                                if (eMember == null) {
                                    eMember = mv.getERefsetMember();

                                    if (eMember != null) {
                                        if (ec.getAnnotations() == null) {
                                            ec.setAnnotations(new ArrayList());
                                        }

                                        ec.getAnnotations().add(eMember);
                                        setupFirstVersion(eMember, mv);
                                    }
                                } else {
                                    TtkRevision eRevision = mv.getERefsetRevision();

                                    setupRevision(eMember, mv, eRevision);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (v.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : v.getAdditionalIdentifierParts()) {
                if (idv.stampIsInRange(minSapNid, maxSapNid)) {
                    if (ec.getAdditionalIdComponents() == null) {
                        ec.setAdditionalIdComponents(new ArrayList<TtkIdentifier>());
                    }

                    Object denotation = idv.getDenotation();

                    switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                        case LONG:
                            ec.additionalIds.add(new TtkIdentifierLong((LongIdBI) idv));

                            break;

                        case STRING:
                            ec.additionalIds.add(new TtkIdentifierString((StringIdBI) idv));

                            break;

                        case UUID:
                            ec.additionalIds.add(new TtkIdentifierUuid((UuidIdBI) idv));

                            break;

                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setupRevision(TtkComponentChronicle ec, ConceptComponent.Version v, TtkRevision ev) throws IOException {
        if (ec.revisions == null) {
            ec.revisions = new ArrayList();
        }

        ev.setPathUuid(P.s.getUuidPrimordialForNid(v.getPathNid()));
        ev.setStatus(v.getStatus());
        ev.setAuthorUuid(P.s.getUuidPrimordialForNid(v.getAuthorNid()));
        ev.setTime(v.getTime());
        ec.revisions.add(ev);
    }

    @Override
    public String toString() {
        return "EConceptChangeSetComputer: minSapNid: " + minSapNid + " maxSapNid: " + maxSapNid + " policy: "
                + policy;
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc) @see org.ihtsdo.cs.ComputeEConceptForChangeSetI#getEConcept(org.ihtsdo.concept .ConceptChronicle)
     */
    @Override
    public TtkConceptChronicle getEConcept(ConceptChronicle c) throws IOException {
        TtkConceptChronicle ec = new TtkConceptChronicle();
        AtomicBoolean changed = new AtomicBoolean(false);
        ec.setAnnotationIndexStyleRefex(c.isAnnotationIndex());
        ec.setAnnotationStyleRefex(c.isAnnotationStyleRefex());

        ec.setPrimordialUuid(c.getPrimordialUuid());
        ec.setConceptAttributes(processConceptAttributes(c, changed));
        ec.setDescriptions(processDescriptions(c, changed));
        ec.setRelationships(processRelationships(c, changed));
        ec.setImages(processMedia(c, changed));

        if (!c.isAnnotationStyleRefex()) {
            ec.setRefsetMembers(processRefsetMembers(c, changed));
        }

        if (changed.get()) {
            return ec;
        }

        return null;
    }
}
