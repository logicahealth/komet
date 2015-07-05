package org.ihtsdo.otf.tcc.api.coordinate;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LanguageCoordinateService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.nid.NidList;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.*;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerPolicy;
import org.ihtsdo.otf.tcc.api.contradiction.strategy.IdentifyAllConflict;
import org.ihtsdo.otf.tcc.api.contradiction.strategy.LastCommitWins;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

@XmlRootElement(name = "viewCoordinate")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"allowedStatusAsString", "classifierSpec", "contradictionManagerPolicy",
    "languageSort", "languageSpec", "languagePreferenceList",
    "name", "precedence", "relationshipAssertionType", "vcUuid",
    "viewPosition"})
public class ViewCoordinate implements StampCoordinate,
        LogicCoordinate, LanguageCoordinate, TaxonomyCoordinate, Externalizable {

    public static final long serialVersionUID = 2;

    private long lastModSequence = Long.MIN_VALUE;
    private EnumSet<Status> allowedStatus;

    private ContradictionManagerBI contradictionManager;
    private LanguageSort langSort;
    private int languageNid = Integer.MAX_VALUE;
    private String name;
    private Position viewPosition;
    private Precedence precedence;
    private RelAssertionType relAssertionType;
    private UUID vcUuid;
    private ViewCoordinate vcWithAllStatusValues;    // transient

    private ConceptSpec classifierSpec;
    private ConceptSpec languageSpec;
    private List<ConceptSpec> langPrefSpecs = new ArrayList<>();

    private static int isaNid = Integer.MAX_VALUE;
    private int classifierNid = Integer.MAX_VALUE;
    private NidListBI langPrefList = new NidList();

    // version 2
    private ConceptSpec statedAssemblageSpec;
    private ConceptSpec inferredAssemblageSpec;
    private ConceptSpec descriptionLogicProfileSpec;
    private int statedAssemblageNid = Integer.MAX_VALUE;
    private int inferredAssemblageNid = Integer.MAX_VALUE;
    private int descriptionLogicProfileNid = Integer.MAX_VALUE;
    private List<ConceptSpec> descriptionTypePrefSpecs = new ArrayList<>();

    private NidListBI descriptionTypePrefList;

    //~--- constructors --------------------------------------------------------
    public ViewCoordinate() throws ValidationException {
        super();
    }

    public ViewCoordinate(SimpleViewCoordinate another) throws ValidationException {
        super();
        this.vcUuid = another.getCoordinateUuid();
        this.name = another.getName();
        this.precedence = another.getPrecedence();
        if (another.getViewPosition() != null) {
            this.viewPosition = new Position(another.getViewPosition());
        }

        if (another.getAllowedStatus() != null) {
            this.allowedStatus = another.getAllowedStatus().clone();
        }
        setContradictionManagerPolicy(another.getContradictionPolicy());
        this.languageSpec = new ConceptSpec(another.getLanguageSpecification());
        this.classifierSpec = new ConceptSpec(another.getClassifierSpecification());
        this.relAssertionType = another.getRelAssertionType();
        another.getLanguagePreferenceOrderList().stream().forEach((langSpec) -> {
            this.langPrefSpecs.add(new ConceptSpec(langSpec));
        });
        this.langSort = another.getLangSort();

    }

    protected ViewCoordinate(ViewCoordinate another) {
        super();
        this.vcUuid = another.vcUuid;
        this.name = another.name;
        this.precedence = another.precedence;

        if (another.viewPosition != null) {
            this.viewPosition = another.viewPosition;
        }

        if (another.allowedStatus != null) {
            this.allowedStatus = another.allowedStatus.clone();
        }

        this.contradictionManager = another.contradictionManager;
        this.languageNid = another.languageNid;
        this.classifierNid = another.classifierNid;
        this.relAssertionType = another.relAssertionType;

        if (another.langPrefList != null) {
            this.langPrefList = new NidList(another.langPrefList.getListArray());
        }

        this.langSort = another.langSort;
        this.lastModSequence = another.lastModSequence;

        classifierSpec = another.classifierSpec;
        languageSpec = another.languageSpec;
        langPrefSpecs.addAll(another.langPrefSpecs);

        this.descriptionLogicProfileNid = another.descriptionLogicProfileNid;
        this.descriptionLogicProfileSpec = another.descriptionLogicProfileSpec;
        this.descriptionTypePrefSpecs = another.descriptionTypePrefSpecs;
        this.statedAssemblageNid = another.statedAssemblageNid;
        this.statedAssemblageSpec = another.statedAssemblageSpec;
        this.inferredAssemblageNid = another.inferredAssemblageNid;
        this.inferredAssemblageSpec = another.inferredAssemblageSpec;
        if (another.descriptionTypePrefList != null) {
            this.descriptionTypePrefList = new NidList(another.descriptionTypePrefList.getListArray());
        }

    }

    public ViewCoordinate(UUID vcUuid, String name, ViewCoordinate another) {
        this(another);
        this.vcUuid = vcUuid;
        this.name = name;
    }

    public ViewCoordinate(UUID vcUuid, String name, Precedence precedence, Position viewPosition,
            EnumSet<Status> allowedStatus,
            ContradictionManagerBI contradictionManager, int languageNid, int classifierNid,
            RelAssertionType relAssertionType, NidListBI langPrefList, LanguageSort langSort) {
        super();
        assert precedence != null;
        assert contradictionManager != null;
        this.vcUuid = vcUuid;
        this.name = name;
        this.precedence = precedence;

        this.viewPosition = viewPosition;

        if (allowedStatus != null) {
            this.allowedStatus = allowedStatus.clone();
        }

        this.contradictionManager = contradictionManager;
        this.languageNid = languageNid;
        this.classifierNid = classifierNid;
        this.relAssertionType = relAssertionType;

        if (langPrefList != null) {
            this.langPrefList = new NidList(langPrefList.getListArray());
        } else {
            this.langPrefList = new NidList(new int[]{languageNid});
        }

        this.langSort = langSort;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof ViewCoordinate) {
            ViewCoordinate another = (ViewCoordinate) o;

            if (!testEquals(precedence, another.precedence)) {
                return false;
            }

            if (!testEquals(viewPosition, another.viewPosition)) {
                return false;
            }

            if (!testEquals(allowedStatus, another.allowedStatus)) {
                return false;
            }

            if (!testEquals(contradictionManager, another.contradictionManager)) {
                return false;
            }

            if (languageNid != Integer.MAX_VALUE && another.languageNid != Integer.MAX_VALUE) {
                if (!testEquals(languageNid, another.languageNid)) {
                    return false;
                }
            } else {
                if (languageNid == Integer.MAX_VALUE && another.languageNid == Integer.MAX_VALUE) {
                    if (!languageSpec.equals(another.languageSpec)) {
                        return false;
                    }
                } else {

                    if (languageNid == Integer.MAX_VALUE) {
                        
                        if (this.languageSpec.getNid() != another.languageNid) {
                            return false;
                        }
                        
                    } else {
                        if (this.languageNid != another.languageSpec.getNid()) {
                            return false;
                        }
                    }
                }
            }

            if (classifierNid != Integer.MAX_VALUE && another.classifierNid != Integer.MAX_VALUE) {
                if (!testEquals(classifierNid, another.classifierNid)) {
                    return false;
                }
            } else {
                if (classifierNid == Integer.MAX_VALUE && another.classifierNid == Integer.MAX_VALUE) {
                    if (!classifierSpec.equals(another.classifierSpec)) {
                        return false;
                    }
                } else {

                    if (classifierNid == Integer.MAX_VALUE) {
                        
                        if (this.classifierSpec.getNid() != another.classifierNid) {
                            return false;
                        }
                        
                    } else {
                        if (this.classifierNid != another.classifierSpec.getNid()) {
                            return false;
                        }
                    }
                }
            }
            if (!testEquals(relAssertionType, another.relAssertionType)) {
                return false;
            }

            if (!testEquals(langSort, another.langSort)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        hashCode = Hashcode.computeLong(hashCode,
                viewPosition.getPath().getPathConceptSequence(), viewPosition.getTime());

        return hashCode;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        lastModSequence = in.readLong();
        allowedStatus = (EnumSet<Status>) in.readObject();
        contradictionManager = (ContradictionManagerBI) in.readObject();
        langSort = (LanguageSort) in.readObject();
        name = (String) in.readObject();
        viewPosition = (Position) in.readObject();
        precedence = (Precedence) in.readObject();
        relAssertionType = (RelAssertionType) in.readObject();
        vcUuid = (UUID) in.readObject();
        classifierSpec = (ConceptSpec) in.readObject();
        languageSpec = (ConceptSpec) in.readObject();
        langPrefSpecs = (ArrayList<ConceptSpec>) in.readObject();
        statedAssemblageSpec = (ConceptSpec) in.readObject();
        inferredAssemblageSpec = (ConceptSpec) in.readObject();
        descriptionLogicProfileSpec = (ConceptSpec) in.readObject();
        descriptionTypePrefSpecs = (ArrayList<ConceptSpec>) in.readObject();

        classifierNid = Integer.MAX_VALUE;
        languageNid = Integer.MAX_VALUE;
        inferredAssemblageNid = Integer.MAX_VALUE;
        statedAssemblageNid = Integer.MAX_VALUE;
        descriptionLogicProfileNid = Integer.MAX_VALUE;
        langPrefList = null;

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeLong(lastModSequence);
        out.writeObject(allowedStatus);
        out.writeObject(contradictionManager);
        out.writeObject(langSort);
        out.writeObject(name);
        out.writeObject(viewPosition);
        out.writeObject(precedence);
        out.writeObject(relAssertionType);
        out.writeObject(vcUuid);

        out.writeObject(classifierSpec);
        out.writeObject(languageSpec);
        out.writeObject(langPrefSpecs);
        out.writeObject(statedAssemblageSpec);
        out.writeObject(inferredAssemblageSpec);
        out.writeObject(descriptionLogicProfileSpec);
        out.writeObject(descriptionTypePrefSpecs);
    }

    private static boolean testEquals(Object o1, Object o2) {
        if ((o1 == null) && (o2 == null)) {
            return true;
        }

        if (o1 == o2) {
            return true;
        }

        if (o1 instanceof NidSetBI) {
            NidSetBI ns1 = (NidSetBI) o1;
            NidSetBI ns2 = (NidSetBI) o2;

            return Arrays.equals(ns1.getSetValues(), ns2.getSetValues());
        }

        if (o1 instanceof NidListBI) {
            NidListBI ns1 = (NidListBI) o1;
            NidListBI ns2 = (NidListBI) o2;

            return Arrays.equals(ns1.getListArray(), ns2.getListArray());
        }

        if (o1.equals(o2)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        TerminologySnapshotDI snap = Ts.get().getSnapshot(this);
        StringBuilder sb = new StringBuilder();

        sb.append("name: ").append(name);
        sb.append("\n     vcUuid: ").append(vcUuid);
        sb.append("\n precedence: ").append(precedence);
        sb.append("\n  positions: ").append(viewPosition);

        String statusStr = "all";

        if (allowedStatus != null) {
            statusStr = allowedStatus.toString();
        }

        sb.append(" \nallowedStatus: ");

        if (statusStr.length() < 50) {
            sb.append(statusStr);
        } else {
            sb.append(statusStr.substring(0, 50)).append("...");
        }
        sb.append(" \ncontradiction: ").append(contradictionManager);
        getConceptText(sb.append(" \nlanguage: "), snap, languageNid);
        getConceptText(sb.append(" \nclassifier: "), snap, classifierNid);
        sb.append(" \nrelAssertionType: ").append(relAssertionType);

        return sb.toString();
    }

    //~--- get methods ---------------------------------------------------------
    @XmlTransient
    public EnumSet<Status> getAllowedStatus() {
        return allowedStatus;
    }

    public void setAllowedStatus(EnumSet<Status> allowedStatus) {
        this.lastModSequence = Ts.get().getSequence();
        this.allowedStatus = allowedStatus;
    }

    public String[] getAllowedStatusAsString() {
        String[] results = new String[allowedStatus.size()];
        int i = 0;
        for (Status status : allowedStatus) {
            results[i++] = status.name();
        }
        return results;
    }

    public void setAllowedStatusAsString(String[] statusStrings) {
        allowedStatus = EnumSet.noneOf(Status.class);
        for (String statusString : statusStrings) {
            allowedStatus.add(Status.valueOf(statusString));
        }

    }

    @XmlTransient
    public int getClassifierNid() {
        if (classifierNid == Integer.MAX_VALUE) {
            try {
                this.classifierNid = classifierSpec.getLenient().getNid();
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return classifierNid;
    }

    public ConceptSpec getClassifierSpec() throws IOException {
        if (classifierSpec != null) {
            return this.classifierSpec;
        }
        return new ConceptSpec(this.classifierNid);
    }

    public void setClassifierSpec(ConceptSpec classifierSpec) throws IOException {
        this.classifierSpec = classifierSpec;
        this.classifierNid = Integer.MAX_VALUE;
    }

    private void getConceptText(StringBuilder sb, TerminologySnapshotDI snap, int nid) {
        if (nid == Integer.MAX_VALUE) {
            sb.append("Integer.MAX_VALUE");

            return;
        }

        if (nid == Integer.MIN_VALUE) {
            sb.append("Integer.MIN_VALUE");

            return;
        }

        try {
            if ((snap.getConceptVersion(nid) != null)
                    && (snap.getConceptVersion(nid).getPreferredDescription() != null)) {
                sb.append(snap.getConceptVersion(nid).getPreferredDescription().getText());
            } else {
                sb.append(Integer.toString(nid));
            }
        } catch (IOException | ContradictionException ex) {
            sb.append(ex.getLocalizedMessage());
        }
    }

    @XmlTransient
    public ContradictionManagerBI getContradictionManager() {
        return contradictionManager;
    }

    public void setContradictionManager(ContradictionManagerBI contradictionManager) {
        this.lastModSequence = Ts.get().getSequence();
        this.contradictionManager = contradictionManager;
    }

    public ContradictionManagerPolicy getContradictionManagerPolicy() {
        return this.contradictionManager.getPolicy();
    }

    public final void setContradictionManagerPolicy(ContradictionManagerPolicy policy) {
        switch (policy) {
            case IDENTIFY_ALL_CONFLICTS:
                this.contradictionManager = new IdentifyAllConflict();
                break;
            case LAST_COMMIT_WINS:
                this.contradictionManager = new LastCommitWins();
                break;
            default:
                throw new UnsupportedOperationException("Can't handle: " + policy);
        }

    }

    public int getIsaNid() {
        if (isaNid == 0 || isaNid == Integer.MAX_VALUE) {
            isaNid = Snomed.IS_A.getNid();
        }
        return isaNid;
    }

    public NidListBI getLangPrefList() {
        if (langPrefList == null || langPrefList.isEmpty()) {
            langPrefList = new NidList();
            if (langPrefSpecs != null) {
                for (ConceptSpec spec : langPrefSpecs) {
                    langPrefList.add(spec.getNid());
                }
            } else {
                if (languageSpec != null) {
                    langPrefList.add(languageSpec.getNid());
                }
            }
        }
        return langPrefList;
    }

    public List<ConceptSpec> getLangPrefSpecs() {
        return langPrefSpecs;
    }

    public LanguagePreferenceList getLanguagePreferenceList() throws IOException {
        if (!langPrefSpecs.isEmpty()) {
            return new LanguagePreferenceList(langPrefSpecs);
        }
        if (langPrefList == null) {
            return new LanguagePreferenceList(new ArrayList<>(0));
        }
        for (int nid : langPrefList.getListArray()) {
            langPrefSpecs.add(new ConceptSpec(nid));
        }

        return new LanguagePreferenceList(langPrefSpecs);
    }

    public void setLanguagePreferenceList(LanguagePreferenceList languagePreferenceList) throws IOException {
        langPrefList.clear();

        if (languagePreferenceList != null) {
            this.langPrefSpecs.clear();
            this.langPrefSpecs.addAll(languagePreferenceList.getPreferenceList());
            for (ConceptSpec conceptSpec : this.langPrefSpecs) {
                langPrefList.add(conceptSpec.getNid());
            }
        }
    }

    public LanguageSort getLanguageSort() {
        return langSort;
    }

    public void setLanguageSort(LanguageSort langSort) {
        this.langSort = langSort;
    }

    @XmlTransient
    public int getLanguageNid() {
        if (languageNid == Integer.MAX_VALUE) {
            this.languageNid = languageSpec.getNid();
        }
        return languageNid;
    }

    public ConceptSpec getLanguageSpec() throws IOException {
        if (languageSpec != null) {
            return languageSpec;
        }
        return new ConceptSpec(this.languageNid);
    }

    public void setLanguageSpec(ConceptSpec languageSpec) throws IOException {
        this.languageSpec = languageSpec;
        this.languageNid = Integer.MAX_VALUE;
    }

    public long getLastModificationSequence() {
        return lastModSequence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getViewPosition() {
        return viewPosition;
    }

    public Precedence getPrecedence() {
        return precedence;
    }

    public RelAssertionType getRelationshipAssertionType() {
        return relAssertionType;
    }

    public UUID getVcUuid() {
        return vcUuid;
    }

    @Override
    public UUID getUuid() {
        return vcUuid;
    }

    public void setVcUuid(UUID vcUuid) {
        this.vcUuid = vcUuid;
    }

    @XmlTransient
    public ViewCoordinate getVcWithAllStatusValues() {
        if (vcWithAllStatusValues == null) {
            vcWithAllStatusValues = new ViewCoordinate(this);
            vcWithAllStatusValues.allowedStatus = null;
        }

        return vcWithAllStatusValues;
    }

    //~--- set methods ---------------------------------------------------------
    public void setClassifierNid(int classifierNid) throws IOException {
        if (Ts.get() != null) {
            this.lastModSequence = Ts.get().getSequence();
        }
        this.vcWithAllStatusValues = null;
        this.classifierNid = classifierNid;
        this.classifierSpec = new ConceptSpec(classifierNid);
    }

    public void setViewPosition(Position viewPosition) {
        if (Ts.get() != null) {
            this.lastModSequence = Ts.get().getSequence();
        }
        this.vcWithAllStatusValues = null;
        this.viewPosition = viewPosition;
    }

    public void setPrecedence(Precedence precedence) {
        this.precedence = precedence;
    }

    public void setRelationshipAssertionType(RelAssertionType relAssertionType) {
        if (Ts.get() != null) {
            this.lastModSequence = Ts.get().getSequence();
        }
        this.vcWithAllStatusValues = null;
        this.relAssertionType = relAssertionType;
    }

    public TerminologySnapshotDI getSnapshot() {
        return Ts.get().getSnapshot(this);
    }

    public TerminologySnapshotDI getCachedSnapshot() {
        return Ts.get().cacheSnapshot(vcUuid, this);
    }

    @Override
    public StampPrecedence getStampPrecedence() {
        return getPrecedence().getStampPrecedence();
    }

    @Override
    public StampPosition getStampPosition() {
        return getViewPosition();
    }

    @Override
    public int getStatedAssemblageSequence() {
        if (this.statedAssemblageNid == Integer.MAX_VALUE) {
            try {
                this.statedAssemblageNid = statedAssemblageSpec.getLenient().getNid();
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            }
        }
        return Get.identifierService().getConceptSequence(this.statedAssemblageNid);
    }

    @Override
    public int getInferredAssemblageSequence() {
        if (this.inferredAssemblageNid == Integer.MAX_VALUE) {
            try {
                this.inferredAssemblageNid = inferredAssemblageSpec.getLenient().getNid();
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } 
        }
        return Get.identifierService().getConceptSequence(this.inferredAssemblageNid);
    }

    @Override
    public int getDescriptionLogicProfileSequence() {
        if (this.descriptionLogicProfileNid == Integer.MAX_VALUE) {
            try {
                this.descriptionLogicProfileNid = descriptionLogicProfileSpec.getLenient().getNid();
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return Get.identifierService().getConceptSequence(this.descriptionLogicProfileNid);
    }

    @Override
    public int getClassifierSequence() {
        return Get.identifierService().getConceptSequence(classifierNid);
    }

    @Override
    public PremiseType getTaxonomyType() {
        switch (getRelationshipAssertionType()) {
            case INFERRED:
            case INFERRED_THEN_STATED:
                return PremiseType.INFERRED;
            case STATED:
                return PremiseType.STATED;
            default:
                throw new UnsupportedOperationException("Can't handle: " + getRelationshipAssertionType());
        }
    }

    public void setStatedAssemblageSpec(ConceptSpec statedAssemblageSpec) {
        this.statedAssemblageSpec = statedAssemblageSpec;
    }

    public void setInferredAssemblageSpec(ConceptSpec inferredAssemblageSpec) {
        this.inferredAssemblageSpec = inferredAssemblageSpec;
    }

    public void setDescriptionLogicProfileSpec(ConceptSpec descriptionLogicProfileSpec) {
        this.descriptionLogicProfileSpec = descriptionLogicProfileSpec;
    }

    @Override
    public int[] getDescriptionTypePreferenceList() {
        if (descriptionTypePrefList == null || descriptionTypePrefList.isEmpty()) {
            descriptionTypePrefList = new NidList();
            if (descriptionTypePrefSpecs != null) {
                for (ConceptSpec spec : descriptionTypePrefSpecs) {
                    descriptionTypePrefList.add(spec.getNid());
                }
            } 
        }
        return descriptionTypePrefList.getListArray();
    }


    public List<ConceptSpec> getDescriptionTypePrefSpecs() {
        return descriptionTypePrefSpecs;
    }

    @Override
    public StampCoordinate getStampCoordinate() {
        return this;
    }

    @Override
    public int getLanugageConceptSequence() {
        return Get.identifierService().getConceptSequence(getLanguageNid());
    }

    @Override
    public int[] getDialectAssemblagePreferenceList() {
        NidListBI prefNidList = getLangPrefList();
        int[] sequences = new int[prefNidList.size()];
        int[] nids = prefNidList.getListArray();
        for (int i = 0; i < sequences.length; i++) {
            sequences[i] = Get.identifierService().getConceptSequence(nids[i]);
        }
        return sequences;
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return this;
    }

    @Override
    public int[] getModuleSequences() {
        return new int[0];
    }
    
    private static LanguageCoordinateService languageCoordinateService;
    private static LanguageCoordinateService getLanguageCoordinateService() {
        if (languageCoordinateService == null) {
            languageCoordinateService = LookupService.getService(LanguageCoordinateService.class);
        }
        return languageCoordinateService;
    }

    @Override
    public Optional<LatestVersion<DescriptionSememe>> getFullySpecifiedDescription(List<SememeChronology<DescriptionSememe>> descriptionList, StampCoordinate stampCoordinate) {
        return getLanguageCoordinateService().getSpecifiedDescription(stampCoordinate, descriptionList, 
                getLanguageCoordinateService().getFullySpecifiedConceptSequence(), this);
   }

    @Override
    public Optional<LatestVersion<DescriptionSememe>> getPreferredDescription(List<SememeChronology<DescriptionSememe>> descriptionList, StampCoordinate stampCoordinate) {
        return getLanguageCoordinateService().getSpecifiedDescription(stampCoordinate, descriptionList, 
                getLanguageCoordinateService().getSynonymConceptSequence(), this);
    }

    @Override
    public EnumSet<State> getAllowedStates() {
        return Status.getStateSet(allowedStatus);
    }

    @Override
    public StampCoordinate makeAnalog(long stampPositionTime) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
