package sh.isaac.solor.rf2;



/*
 * aks8m - 9/6/18
 */

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.ExportLookUpCache;
import sh.komet.gui.manifold.Manifold;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RF2ExportHelper {

    private Manifold manifold;
    private static ObservableSnapshotService snapshotService;

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

    String getIdString(Chronology chronology){

        if (ExportLookUpCache.isSCTID(chronology)) {
            return lookUpIdentifierFromSemantic(snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), chronology);
        } else if (ExportLookUpCache.isLoinc(chronology)) {
            final String loincId = lookUpIdentifierFromSemantic(snapshotService, MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromLoincId(loincId);
        } else if (ExportLookUpCache.isRxNorm(chronology)) {
            final String rxnormId = lookUpIdentifierFromSemantic(snapshotService, MetaData.RXNORM_CUI____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromRxNormId(rxnormId);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(chronology.getPrimordialUuid());
        }
    }

    private String getTimeString(int stampNid){
        return new SimpleDateFormat("YYYYMMdd").format(new Date(Get.stampService().getTimeForStamp(stampNid)));
    }

    private String getActiveString(int stampNid){
        return Get.stampService().getStatusForStamp(stampNid).isActive() ? "1" : "0";
    }

    private String getModuleString(int stampNid){
        ConceptChronology moduleConcept = Get.concept(Get.stampService().getModuleNidForStamp(stampNid));
        if (ExportLookUpCache.isSCTID(moduleConcept)) {
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


}
