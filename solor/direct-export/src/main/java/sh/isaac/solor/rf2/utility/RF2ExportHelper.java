package sh.isaac.solor.rf2.utility;



/*
 * aks8m - 9/6/18
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.concept.ObservableConceptVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.rf2.config.RF2Configuration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class RF2ExportHelper {

    protected static final Logger LOG = LogManager.getLogger();
    private ManifoldCoordinate manifoldCoordinate;
    private ObservableSnapshotService observableSnapshotService;

    private final static int[] identifierNidPriority = new int[]{ //order of priority
            TermAux.SNOMED_IDENTIFIER.getNid(),
            MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(),
            MetaData.RXNORM_CUI____SOLOR.getNid()
    };

    public RF2ExportHelper(ManifoldCoordinate manifold) {
        manifoldCoordinate = manifold;
        observableSnapshotService = Get.observableSnapshotService(manifoldCoordinate.getStampFilter());
    }
    
    public ManifoldCoordinate getManifoldCoordinate() {
        return manifoldCoordinate;
    }

    public Version getObservableSnapshotVersion(int nid){

        switch (Get.identifierService().getObjectTypeForComponent(nid)) {
            case CONCEPT:
                return observableSnapshotService.getObservableConceptVersion(nid).get();
            case SEMANTIC:
                return observableSnapshotService.getObservableSemanticVersion(nid).get();
        }

        return null;
    }

    public String getIdString(int nid){
        return getIdString(getObservableSnapshotVersion(nid));
    }

    //TODO Dan notes, I'm not really sure what purpose this method is safe for, since you have no idea if it will return a nid, loinc id, cui, or 
    //and invented solorid.  You would have no way to properly parse this back in, without having a hint of some sort of what on earth it returned.
    public String getIdString(Version version){

        for(int identifierAssemblageNid : identifierNidPriority){

            ImmutableIntSet identifierNidSet = Get.assemblageService()
                    .getSemanticNidsForComponentFromAssemblage(version.getNid(), identifierAssemblageNid);

            if(!identifierNidSet.isEmpty()){

                StringVersion identifierString = (StringVersion) Get.assemblageService()
                        .getSemanticChronology(identifierNidSet.intIterator().next()).getVersionList().get(0);


                if(identifierAssemblageNid == TermAux.SNOMED_IDENTIFIER.getNid()){

                    return identifierString.getString();
                }else if(identifierAssemblageNid == MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid()){

                    return UuidT5Generator.makeSolorIdFromLoincId(identifierString.getString());
                }else if(identifierAssemblageNid == MetaData.RXNORM_CUI____SOLOR.getNid()){

                    return UuidT5Generator.makeSolorIdFromRxNormId(identifierString.getString());
                }

            }
        }
        
        //Better handling for some cases where brittle types are mapped to stupid, default values.
        if (version.getNid() == DynamicConstants.get().DYNAMIC_DT_NID.getNid() || version.getNid() == DynamicConstants.get().DYNAMIC_DT_UUID.getNid()) {
            return RF2Configuration.REFERENCED_COMPONENT;
        }
        else if (version.getNid() == DynamicConstants.get().DYNAMIC_DT_STRING.getNid()) {
            return RF2Configuration.PARSABLE_STRING;
        }
        else if (version.getNid() == DynamicConstants.get().DYNAMIC_DT_INTEGER.getNid()) {
            return RF2Configuration.SIGNED_INTEGER;
        }

        return UuidT5Generator.makeSolorIdFromUuid(version.getPrimordialUuid());
    }

    public String getTimeString(int nid){
        return getTimeString(getObservableSnapshotVersion(nid));
    }

    public String getTimeString(Version version){
        return new SimpleDateFormat("YYYYMMdd").format(new Date(version.getTime()));
    }

    public String getActiveString(int nid){
        return getActiveString(getObservableSnapshotVersion(nid));
    }

    public String getActiveString(Version version){
        return version.getStatus().getBoolean() ? "1" : "0";
    }

    public String getConceptPrimitiveOrSufficientDefinedSCTID(int nid){
        return getConceptPrimitiveOrSufficientDefinedSCTID((ObservableConceptVersion) getObservableSnapshotVersion(nid));
    }

    public String getConceptPrimitiveOrSufficientDefinedSCTID(ConceptVersion conceptVersion) {

        Optional<LogicalExpression> conceptExpression = manifoldCoordinate.getLogicalExpression(conceptVersion.getNid(), PremiseType.STATED);

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
        return getTypeId((ObservableDescriptionVersion) getObservableSnapshotVersion(nid));
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
        return getTerm((ObservableDescriptionVersion) getObservableSnapshotVersion(nid));
    }

    public String getTerm(DescriptionVersion descriptionVersion){
        return descriptionVersion.getText();
    }

    public String getCaseSignificanceId(int nid){
        return getCaseSignificanceId((ObservableDescriptionVersion) getObservableSnapshotVersion(nid));
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
        return getLanguageCode(getObservableSnapshotVersion(nid));
    }

    public String getLanguageCode(Version version){

        int languageNID = version.getAssemblageNid();

        if(languageNID == TermAux.ENGLISH_LANGUAGE.getNid())
            return "en";

        return "Issue::getLanguageCode(Version version)";
    }

    public int getModuleNid(int nid){
        return getObservableSnapshotVersion(nid).getModuleNid();
    }

    public String getSemanticStringValue(int nid){
        return ((StringVersion) getObservableSnapshotVersion(nid)).getString();
    }

    public int getSemanticComponentNidValue(int nid){
        return ((ComponentNidVersion) getObservableSnapshotVersion(nid)).getComponentNid();
    }

    public VersionType getSemanticSnapshotVersionType(int nid){
        return getObservableSnapshotVersion(nid).getSemanticType();
    }

}
