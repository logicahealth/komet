package sh.komet.gui.exportation;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.util.UuidT5Generator;
import sh.komet.gui.manifold.Manifold;

import java.util.List;
import java.util.Optional;

/*
 * aks8m - 5/19/18
 */
public class ReaderSpecification {

    private final ExportFormatType exportFormatType;
    private final Manifold manifold;
    private final ObservableSnapshotService snapshot;
    private ExportComponentType exportComponentType;
    private final ExportLookUpCache exportLookUpCache;


    public ReaderSpecification(ExportFormatType exportFormatType, Manifold manifold, ExportLookUpCache exportLookUpCache) {
        this.exportFormatType = exportFormatType;
        this.manifold = manifold;
        this.exportLookUpCache = exportLookUpCache;
        this.snapshot = Get.observableSnapshotService(this.manifold);
    }

    public void setExportComponentType(ExportComponentType exportComponentType) {
        this.exportComponentType = exportComponentType;
    }

    public ExportComponentType getExportComponentType() {
        return exportComponentType;
    }

    public String createExportString(Chronology chronology){

        switch (this.getExportComponentType()){
            case CONCEPT:
                return createSharedElements(chronology) //id, effectiveTime, active, moduleId
                        .append(getConceptPrimitiveOrSufficientDefinedSCTID(chronology.getNid()))   //definitionStatusId
                        .toString();
            case DESCRIPTION:
                return createSharedElements(chronology) //id, effectiveTime, active, moduleId
                        .append(getIdString(Get.concept(((SemanticChronology) chronology).getReferencedComponentNid())) + "\t") //conceptId
                        .append(getLanguageCode(chronology) + "\t") //languageCode
                        .append(getTypeId(chronology) + "\t") //typeId
                        .append(getTerm(chronology) + "\t") //term
                        .append(getCaseSignificanceId(chronology)) //caseSignificanceId
                        .toString();
            default:
                return "¯\\_(ツ)_/¯";
        }

    }


    private StringBuilder createSharedElements(Chronology chronology){
        final StringBuilder sb = new StringBuilder();
        int stampNid = 0;

        if(this.exportComponentType == ExportComponentType.CONCEPT)
            stampNid = this.snapshot.getObservableConceptVersion(chronology.getNid()).getStamps().findFirst().getAsInt();
        else if(this.exportComponentType == ExportComponentType.DESCRIPTION)
            stampNid = this.snapshot.getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();

        return sb.append(getIdString(chronology))       //id
                .append(getTimeString(stampNid))        //time
                .append(getActiveString(stampNid))      //active
                .append(getModuleString(stampNid));     //moduleId
    }

    private String getIdString(Chronology chronology){

        if (this.exportLookUpCache.getSctidNids().contains(chronology.getNid())) {
            return lookUpIdentifierFromSemantic(this.snapshot, TermAux.SNOMED_IDENTIFIER.getNid(), chronology) + "\t";
        } else if (this.exportLookUpCache.getLoincNids().contains(chronology.getNid())) {
            final String loincId = lookUpIdentifierFromSemantic(this.snapshot, MetaData.CODE____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeLongIdFromLoincId(loincId) + "\t";
        } else if (this.exportLookUpCache.getRxnormNids().contains(chronology.getNid())) {
            final String rxnormId = lookUpIdentifierFromSemantic(this.snapshot, MetaData.RXNORM_CUI____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeLongIdFromRxNormId(rxnormId) + "\t";
        } else {
            return UuidT5Generator.makeLongIdFromUuid(chronology.getPrimordialUuid()) + "\t";
        }
    }

    private String getTimeString(int stampNid){
        return Long.toString(Get.stampService().getTimeForStamp(stampNid)) + "\t";
    }

    private String getActiveString(int stampNid){
        return Get.stampService().getStatusForStamp(stampNid).isActive() ? "1\t" : "0\t";
    }

    private String getModuleString(int stampNid){
        ConceptChronology moduleConcept = Get.concept(Get.stampService().getModuleNidForStamp(stampNid));
        if (this.exportLookUpCache.getSctidNids().contains(moduleConcept.getNid())) {
            return lookUpIdentifierFromSemantic(this.snapshot, TermAux.SNOMED_IDENTIFIER.getNid(), moduleConcept) + "\t";
        } else {
            return UuidT5Generator.makeLongIdFromUuid(moduleConcept.getPrimordialUuid()) + "\t";
        }
    }

    private String lookUpIdentifierFromSemantic(ObservableSnapshotService snapshotService
            , int identifierAssemblageNid, Chronology chronology){

        LatestVersion<ObservableStringVersion> stringVersion =
                (LatestVersion<ObservableStringVersion>) snapshotService.getObservableSemanticVersion(
                chronology.getSemanticChronologyList().stream()
                        .filter(semanticChronology -> semanticChronology.getAssemblageNid() == identifierAssemblageNid)
                        .findFirst().get().getNid()
        );

        return stringVersion.isPresent() ? stringVersion.get().getString() : "";
    }

    private String getConceptPrimitiveOrSufficientDefinedSCTID(int conceptNid) {
        //primitive vs sufficiently
        //If the stated definition contains a sufficient set, it is sufficently defined, if not it is primitive.
        Optional<LogicalExpression> conceptExpression = this.manifold.getLogicalExpression(conceptNid, PremiseType.STATED);

        if (!conceptExpression.isPresent()) {
            return "";
        }else{
            if(conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET)){
                return "900000000000073002"; //sufficiently defined SCTID
            }else{
                return "900000000000074008"; //primitive SCTID
            }
        }
    }

    private String getLanguageCode(Chronology chronology){

        int languageNID = chronology.getAssemblageNid();

        if(languageNID == TermAux.ENGLISH_LANGUAGE.getNid())
            return "en";
        else
            return "¯\\_(ツ)_/¯";


    }

    private String getTypeId(Chronology chronology){
        //Definition (core metadata concept)			-	900000000000550004
        //Fully specified name (core metadata concept)	-	900000000000003001
        //Synonym (core metadata concept) 				-	900000000000013009

        final int typeNid = ((LatestVersion<ObservableDescriptionVersion>)
                snapshot.getObservableSemanticVersion(chronology.getNid())).get().getDescriptionTypeConceptNid();

        if(typeNid == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())
            return "900000000000550004";
        else if(typeNid == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000003001";
        else if(typeNid == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000013009";

        return "¯\\_(ツ)_/¯";
    }

    private String getTerm(Chronology chronology){
        return ((LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(chronology.getNid())).get().getText();
    }

    private String getCaseSignificanceId(Chronology chronology){
        //Entire term case insensitive (core metadata concept)				-	900000000000448009
        //Entire term case sensitive (core metadata concept)				-	900000000000017005
        //Only initial character case insensitive (core metadata concept)	-	900000000000020002

        final int caseSigNid = ((LatestVersion<ObservableDescriptionVersion>)
                snapshot.getObservableSemanticVersion(chronology.getNid())).get().getCaseSignificanceConceptNid();


        if( caseSigNid == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid())
            return "900000000000448009";
        else if(caseSigNid == TermAux.DESCRIPTION_CASE_SENSITIVE.getNid())
            return "900000000000017005";
        else if(caseSigNid == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid())
            return "900000000000020002";

        return "¯\\_(ツ)_/¯";
    }

    public void addColumnHeaders(List<String> lines){

        if(this.exportFormatType == ExportFormatType.RF2) {
            switch (this.exportComponentType){
                case CONCEPT:
                    lines.add(0, "id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId");
                    break;
                case DESCRIPTION:
                    lines.add(0, "id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode\ttypeId\tterm\tcaseSignificanceId");
                    break;
            }
        }
        else if(this.exportFormatType == ExportFormatType.SRF){

        }
    }
}
