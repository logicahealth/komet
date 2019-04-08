package sh.isaac.solor.rf2.utility;



/*
 * aks8m - 9/6/18
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.concept.ObservableConceptVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.util.UuidT5Generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class RF2ExportHelper {

    protected static final Logger LOG = LogManager.getLogger();
    private ManifoldCoordinate manifold;
    private static StampCoordinate stampCoordinate;

    private final static int[] identifierNidPriority = new int[]{ //order of priority
            TermAux.SNOMED_IDENTIFIER.getNid(),
            MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(),
            MetaData.RXNORM_CUI____SOLOR.getNid()
    };

    public RF2ExportHelper(ManifoldCoordinate manifold) {
        this.manifold = manifold;
        stampCoordinate = this.manifold.getStampCoordinate();
    }

    public Version getChronologySnapshotVersion(int nid){

        LatestVersion<Version> latestVersion;

        switch (Get.identifierService().getObjectTypeForComponent(nid)){
            case CONCEPT:

                latestVersion = Get.concept(nid).getLatestVersion(stampCoordinate);
                if(latestVersion != null && latestVersion.isPresent()){
                    return latestVersion.get();
                }else {
                    LOG.warn("Can't find latest version for concept chronology " + nid + "\t" + Get.concept(nid).getFullyQualifiedName());
                }

                break;
            case SEMANTIC:

                latestVersion = Get.assemblageService().getSemanticChronology(nid).getLatestVersion(stampCoordinate);
                if(latestVersion != null &&  latestVersion.isPresent()){
                    return latestVersion.get();
                }else {
                    LOG.warn("Can't find latest version for semantic chronology " + nid + "\t" + Get.assemblageService().getSemanticChronology(nid).toUserString());
                }
                break;
        }

        return null;
    }

    public String getIdString(int nid){
        return getIdString(getChronologySnapshotVersion(nid));
    }

    public String getIdString(Version version){

        for(int identifierAssemblageNid : identifierNidPriority){

            NidSet identifierNidsSet = Get.assemblageService()
                    .getSemanticNidsForComponentFromAssemblage(version.getNid(), identifierAssemblageNid);

            if(!identifierNidsSet.isEmpty() && identifierNidsSet.findFirst().isPresent()){

                StringVersion identifierString = (StringVersion) Get.assemblageService()
                        .getSemanticChronology(identifierNidsSet.findFirst().getAsInt()).getVersionList().get(0);

                if(identifierAssemblageNid == TermAux.SNOMED_IDENTIFIER.getNid()){

                    return identifierString.getString();
                }else if(identifierAssemblageNid == MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid()){

                    return UuidT5Generator.makeSolorIdFromLoincId(identifierString.getString());
                }else if(identifierAssemblageNid == MetaData.RXNORM_CUI____SOLOR.getNid()){

                    return UuidT5Generator.makeSolorIdFromRxNormId(identifierString.getString());
                }

                if(identifierNidsSet.size() > 1 || Get.assemblageService()
                        .getSemanticChronology(identifierNidsSet.findFirst().getAsInt()).getVersionList().size() > 1){
                    LOG.warn("Multiple Identifiers from the same Assemblage for Chronology: " + version.getNid());
                }
            }
        }

        return UuidT5Generator.makeSolorIdFromUuid(version.getPrimordialUuid());
    }

    public String getTimeString(int nid){
        return getTimeString(getChronologySnapshotVersion(nid));
    }

    public String getTimeString(Version version){
        return new SimpleDateFormat("YYYYMMdd").format(new Date(version.getTime()));
    }

    public String getActiveString(int nid){
        return getActiveString(getChronologySnapshotVersion(nid));
    }

    public String getActiveString(Version version){
        return version.getStatus().getBoolean() ? "1" : "0";
    }

    public String getConceptPrimitiveOrSufficientDefinedSCTID(int nid){
        return getConceptPrimitiveOrSufficientDefinedSCTID((ObservableConceptVersion) getChronologySnapshotVersion(nid));
    }

    public String getConceptPrimitiveOrSufficientDefinedSCTID(ConceptVersion conceptVersion) {

        Optional<LogicalExpression> conceptExpression = this.manifold.getLogicalExpression(conceptVersion.getNid(), PremiseType.STATED);

        if (!conceptExpression.isPresent()) {
            return "900000000000074008"; //This seems to happen e.g. SOLOR Concept & SOLOR Concept (SOLOR)
        } else {
            if (conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET)) {
                return "900000000000073002"; //sufficiently defined SCTID
            } else {
                return "900000000000074008"; //primitive SCTID
            }
        }
    }

    public String getTypeId(int nid){
        return getTypeId((ObservableDescriptionVersion) getChronologySnapshotVersion(nid));
    }

    public String getTypeId(DescriptionVersion descriptionVersion){


        int typeNid = descriptionVersion.getDescriptionTypeConceptNid();

        if (typeNid == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())
            return "900000000000550004";
        else if (typeNid == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000003001";
        else if (typeNid == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000013009";

        return "Issue::getTypeId(DescriptionVersion descriptionVersion)";
    }

    public String getTerm(int nid){
        return getTerm((ObservableDescriptionVersion) getChronologySnapshotVersion(nid));
    }

    public String getTerm(DescriptionVersion descriptionVersion){
        return descriptionVersion.getText();
    }

    public String getCaseSignificanceId(int nid){
        return getCaseSignificanceId((ObservableDescriptionVersion) getChronologySnapshotVersion(nid));
    }

    public String getCaseSignificanceId(DescriptionVersion descriptionVersion){

        int caseSigNid = descriptionVersion.getCaseSignificanceConceptNid();

        if (caseSigNid == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid())
            return "900000000000448009";
        else if (caseSigNid == TermAux.DESCRIPTION_CASE_SENSITIVE.getNid())
            return "900000000000017005";
        else if (caseSigNid == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid())
            return "900000000000020002";

        return "Issue::getCaseSignificanceId(DescriptionVersion descriptionVersion)";
    }

    public String getLanguageCode(int nid){
        return getLanguageCode(getChronologySnapshotVersion(nid));
    }

    public String getLanguageCode(Version version){

        int languageNID = version.getAssemblageNid();

        if(languageNID == TermAux.ENGLISH_LANGUAGE.getNid())
            return "en";

        return "Issue::getLanguageCode(Version version)";
    }

    public int getModuleNid(int nid){
        return Get.concept(nid).getLatestVersion(this.manifold).get().getModuleNid();
    }

    public String getSemanticStringValue(int nid){
        return ((StringVersion) getChronologySnapshotVersion(nid)).getString();
    }

    public int getSemanticNidValue(int nid){
        return ((ComponentNidVersion) getChronologySnapshotVersion(nid)).getNid();
    }

    public VersionType getSemanticSnapshotVersionType(int nid){
        return getChronologySnapshotVersion(nid).getSemanticType();
    }

    public ManifoldCoordinate getManifold() {
        return manifold;
    }
}
