package sh.isaac.solor.rf2.utility;



/*
 * aks8m - 9/6/18
 */

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.util.UuidT5Generator;
import sh.komet.gui.manifold.Manifold;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class RF2ExportHelper {

    private Manifold manifold;
    private ObservableSnapshotService snapshotService;

    public RF2ExportHelper(Manifold manifold) {
        this.manifold = manifold;
        snapshotService = Get.observableSnapshotService(manifold);
    }

    public ObservableSnapshotService getSnapshotService() {
        return snapshotService;
    }

    public StringBuilder getRF2CommonElements(int nid){

        int stampNid = 0;
        IsaacObjectType chronologyType = IsaacObjectType.UNKNOWN;
        switch (Get.identifierService().getObjectTypeForComponent(nid)){
            case CONCEPT:
                stampNid = snapshotService.getObservableConceptVersion(nid).getStamps().findFirst().getAsInt();
                chronologyType = IsaacObjectType.CONCEPT;
                break;
            case SEMANTIC:
                stampNid = snapshotService.getObservableSemanticVersion(nid).getStamps().findFirst().getAsInt();
                chronologyType = IsaacObjectType.SEMANTIC;
                break;
        }

        return new StringBuilder()
                .append(getIdString(nid, chronologyType) + "\t")                //id
                .append(getTimeString(stampNid) + "\t")         //time
                .append(getActiveString(stampNid) + "\t")       //active
                .append(getModuleString(stampNid) + "\t");      //moduleId
    }

    public String getIdString(int nid){

        IsaacObjectType chronologyType = IsaacObjectType.UNKNOWN;

        switch (Get.identifierService().getObjectTypeForComponent(nid)){
            case CONCEPT:
                chronologyType = IsaacObjectType.CONCEPT;
                break;
            case SEMANTIC:
                chronologyType = IsaacObjectType.SEMANTIC;
                break;
        }

        return getIdString(nid, chronologyType);
    }

    private String getIdString(int nid, IsaacObjectType chronologyType){

        NidSet nidSet;
        int[] identifierAssemblagesNids = new int[]{
                TermAux.SNOMED_IDENTIFIER.getNid(),
                MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(),
                MetaData.RXNORM_CUI____SOLOR.getNid()
        };

        for(int assemblageNid : identifierAssemblagesNids){
            nidSet = lookupNidSetForAssemblage(nid, assemblageNid);

            if(nidSet.size() > 0){
                String id = lookUpIdentifierStringValueFromSemantic(nidSet.findFirst().getAsInt());

                if(assemblageNid == TermAux.SNOMED_IDENTIFIER.getNid()){
                    return id;
                } else if (assemblageNid == MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid()){
                    return UuidT5Generator.makeSolorIdFromLoincId(id);
                } else if (assemblageNid == MetaData.RXNORM_CUI____SOLOR.getNid()){
                    return UuidT5Generator.makeSolorIdFromRxNormId(id);
                }
            }

        }

        switch (chronologyType){
            case CONCEPT:
                return UuidT5Generator.makeSolorIdFromUuid(Get.concept(nid).getPrimordialUuid());
            case SEMANTIC:
                return UuidT5Generator.makeSolorIdFromUuid(Get.assemblageService().getSemanticChronology(nid).getPrimordialUuid());
        }

        return "00000000000";
    }


    public String getTimeString(int stampNid){
        return new SimpleDateFormat("YYYYMMdd").format(new Date(Get.stampService().getTimeForStamp(stampNid)));
    }

    public String getActiveString(int stampNid){
        return Get.stampService().getStatusForStamp(stampNid).isActive() ? "1" : "0";
    }

    public String getModuleString(int stampNid){
        return getIdString(Get.concept(Get.stampService().getModuleNidForStamp(stampNid)).getNid());
    }

    private NidSet lookupNidSetForAssemblage(int conceptNid, int assemblageNid){
        return Get.assemblageService().getSemanticNidsForComponentFromAssemblage(conceptNid, assemblageNid);
    }

    private String lookUpIdentifierStringValueFromSemantic(int semanticIdentifierNid){

        final StringBuilder stringBuilder = new StringBuilder();

        switch (Get.assemblageService().getSemanticChronology(semanticIdentifierNid).getVersionType()){
            case STRING:
                LatestVersion<ObservableStringVersion> stringVersion = (LatestVersion<ObservableStringVersion>) this.snapshotService.getObservableSemanticVersion(semanticIdentifierNid);
                stringBuilder.append(stringVersion.get().getString());
                break;
            case LONG:
                LatestVersion<ObservableLongVersion> longVersion = (LatestVersion<ObservableLongVersion>) this.snapshotService.getObservableSemanticVersion(semanticIdentifierNid);
                stringBuilder.append(longVersion.get().getLongValue());
                break;
        }

        return stringBuilder.toString();
    }

    public int getIndentifierAssemblageConceptNID(int semanticNid){

        int[] identifierAssemblageNids = Get.taxonomyService().getSnapshot(manifold).getTaxonomyChildConceptNids(TermAux.IDENTIFIER_SOURCE.getNid());

        for(int i=0; i < identifierAssemblageNids.length; i++ ){

            if(Get.assemblageService().getSemanticNidsForComponent(identifierAssemblageNids[i]).contains(semanticNid)){
                return identifierAssemblageNids[i];
            }
        }

        return 0; //should only return if a Metadata
    }

    public String getConceptPrimitiveOrSufficientDefinedSCTID(int conceptNid) {
        //primitive vs sufficiently
        //If the stated definition contains a sufficient set, it is sufficiently defined, if not it is primitive.
        Optional<LogicalExpression> conceptExpression = this.manifold.getLogicalExpression(conceptNid, PremiseType.STATED);

        if (!conceptExpression.isPresent()) {
            return "900000000000074008"; //This seems to happen e.g. SOLOR Concept & SOLOR Concept (SOLOR)
        }else{
            if(conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET)){
                return "900000000000073002"; //sufficiently defined SCTID
            }else{
                return "900000000000074008"; //primitive SCTID
            }
        }
    }

    public String getTypeId(int nid){
        //Definition (core metadata concept)			-	900000000000550004
        //Fully specified name (core metadata concept)	-	900000000000003001
        //Synonym (core metadata concept) 				-	900000000000013009

        final int typeNid = ((LatestVersion<ObservableDescriptionVersion>)
                this.getSnapshotService().getObservableSemanticVersion(nid)).get().getDescriptionTypeConceptNid();

        if(typeNid == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())
            return "900000000000550004";
        else if(typeNid == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000003001";
        else if(typeNid == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000013009";

        return "¯\\_(ツ)_/¯";
    }

    public String getTerm(int nid){
        return ((LatestVersion<ObservableDescriptionVersion>) this.getSnapshotService().getObservableSemanticVersion(nid)).get().getText();
    }

    public String getCaseSignificanceId(int nid){
        //Entire term case insensitive (core metadata concept)				-	900000000000448009
        //Entire term case sensitive (core metadata concept)				-	900000000000017005
        //Only initial character case insensitive (core metadata concept)	-	900000000000020002

        final int caseSigNid = ((LatestVersion<ObservableDescriptionVersion>)
                this.getSnapshotService().getObservableSemanticVersion(nid)).get().getCaseSignificanceConceptNid();


        if( caseSigNid == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid())
            return "900000000000448009";
        else if(caseSigNid == TermAux.DESCRIPTION_CASE_SENSITIVE.getNid())
            return "900000000000017005";
        else if(caseSigNid == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid())
            return "900000000000020002";

        return "¯\\_(ツ)_/¯";
    }

    public String getLanguageCode(int nid){

        int languageNID = Get.assemblageService().getSemanticChronology(nid).getAssemblageNid();

        if(languageNID == TermAux.ENGLISH_LANGUAGE.getNid())
            return "en";
        else
            return "¯\\_(ツ)_/¯";

    }

    public boolean isMetaDataConcept(int conceptNid){
        return Get.taxonomyService().getSnapshot(this.manifold).isKindOf(conceptNid, MetaData.METADATA____SOLOR.getNid());
    }
}
