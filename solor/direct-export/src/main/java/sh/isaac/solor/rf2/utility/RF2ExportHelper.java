package sh.isaac.solor.rf2.utility;



/*
 * aks8m - 9/6/18
 */

import mifschema.Concept;
import org.apache.lucene.index.Term;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.util.UuidT5Generator;
import sh.komet.gui.manifold.Manifold;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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

    public StringBuilder getRF2CommonElements(Chronology chronology){

        int stampNid = 0;

        if(chronology instanceof ConceptChronology)
            stampNid = snapshotService.getObservableConceptVersion(chronology.getNid()).getStamps().findFirst().getAsInt();
        else if(chronology instanceof SemanticChronology)
            stampNid = snapshotService.getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();


        return new StringBuilder()
                .append(getIdString(chronology) + "\t")       //id
                .append(getTimeString(stampNid) + "\t")        //time
                .append(getActiveString(stampNid) + "\t")      //active
                .append(getModuleString(stampNid) + "\t");     //moduleId
    }

    public StringBuilder getRF2CommonElements(Version version){

        return new StringBuilder()
                .append(getIdString(version) + "\t")        //id
                .append(getTimeString(version) + "\t")      //time
                .append(getActiveString(version) + "\t")    //active
                .append(getModuleString(version) + "\t");   //moduleId
    }

    public String getIdString(Chronology chronology){

        if (RF2ExportLookUpCache.isSCTID(chronology)) {
            return lookUpIdentifierFromSemantic(snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), chronology);
        } else if (RF2ExportLookUpCache.isLoinc(chronology)) {
            final String loincId = lookUpIdentifierFromSemantic(snapshotService, MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromLoincId(loincId);
        } else if (RF2ExportLookUpCache.isRxNorm(chronology)) {
            final String rxnormId = lookUpIdentifierFromSemantic(snapshotService, MetaData.RXNORM_CUI____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromRxNormId(rxnormId);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(chronology.getPrimordialUuid());
        }
    }



    public String getIdString(int nID){

        Chronology chronology = null;
        switch (Get.identifierService().getObjectTypeForComponent(nID)){
            case CONCEPT:
                chronology = Get.concept(nID);
                break;
            case SEMANTIC:
                chronology = Get.assemblageService().getSemanticChronology(nID);
                break;
        }

        return chronology != null? getIdString(chronology) : "null_chronology";
    }

    public String getTimeString(int stampNid){
        return new SimpleDateFormat("YYYYMMdd").format(new Date(Get.stampService().getTimeForStamp(stampNid)));
    }

    public String getActiveString(int stampNid){
        return Get.stampService().getStatusForStamp(stampNid).isActive() ? "1" : "0";
    }

    public String getModuleString(int stampNid){
        ConceptChronology moduleConcept = Get.concept(Get.stampService().getModuleNidForStamp(stampNid));
        if (RF2ExportLookUpCache.isSCTID(moduleConcept)) {
            return lookUpIdentifierFromSemantic(snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), moduleConcept);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(moduleConcept.getPrimordialUuid());
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

    public String getIdString(Version version){

        Chronology chronology = version.getChronology();

        if (RF2ExportLookUpCache.isSCTID(chronology)) {
            return lookUpIdentifierFromSemantic(TermAux.SNOMED_IDENTIFIER, chronology);
        } else if (RF2ExportLookUpCache.isLoinc(chronology)) {
            return UuidT5Generator.makeSolorIdFromLoincId(
                    lookUpIdentifierFromSemantic(MetaData.LOINC_ID_ASSEMBLAGE____SOLOR, chronology)
            );
        } else if (RF2ExportLookUpCache.isRxNorm(chronology)) {
            return UuidT5Generator.makeSolorIdFromRxNormId(
                    lookUpIdentifierFromSemantic(MetaData.RXNORM_CUI____SOLOR, chronology)
            );
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(chronology.getPrimordialUuid());
        }
    }

    public String getTimeString(Version version){
        return new SimpleDateFormat("YYYYMMdd").format(new Date(version.getTime()));
    }

    public String getActiveString(Version version){
        return version.isActive() ? "1" : "0";
    }

    public String getModuleString(Version version){

        ConceptChronology moduleConcept = Get.concept(version.getModuleNid());
        if (RF2ExportLookUpCache.isSCTID(moduleConcept)) {
            return lookUpIdentifierFromSemantic(TermAux.SNOMED_IDENTIFIER, moduleConcept);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(moduleConcept.getPrimordialUuid());
        }
    }

    private String lookUpIdentifierFromSemantic(ConceptSpecification assemblageConceptSpec, Chronology chronology){

        if(

        chronology.getSemanticChronologyList().stream()
                .filter(semanticChronology -> semanticChronology.getAssemblageNid() == assemblageConceptSpec.getNid())
                .findFirst()
                .get()
                .getVersionList()
                .size() > 1
        )
            System.out.println("More than one versioned ID");

        return "0000000";
    }
}
