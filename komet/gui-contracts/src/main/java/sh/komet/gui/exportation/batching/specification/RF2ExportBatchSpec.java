package sh.komet.gui.exportation.batching.specification;

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
import sh.komet.gui.exportation.ExportLookUpCache;
import sh.komet.gui.manifold.Manifold;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
 * aks8m - 5/22/18
 */
public abstract class RF2ExportBatchSpec implements BatchSpecification<Chronology, String> {

    private final Manifold manifold;
    private static ObservableSnapshotService snapshotService;

    public RF2ExportBatchSpec(Manifold manifold) {
        this.manifold = manifold;
        createSnapshotInstance(this.manifold);
    }

    abstract void addColumnHeaders(List<String> batchResultList);

    private static void createSnapshotInstance(Manifold manifold){
        snapshotService = Get.observableSnapshotService(manifold);
    }

    public ObservableSnapshotService getSnapshotService() {
        return this.snapshotService;
    }

    StringBuilder getRF2CommonElements(Chronology chronology){

        int stampNid = 0;

        if(chronology instanceof ConceptChronology)
            stampNid = getSnapshotService().getObservableConceptVersion(chronology.getNid()).getStamps().findFirst().getAsInt();
        else if(chronology instanceof SemanticChronology)
            stampNid = getSnapshotService().getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();


        return new StringBuilder()
                .append(getIdString(chronology) + "\t")       //id
                .append(getTimeString(stampNid) + "\t")        //time
                .append(getActiveString(stampNid) + "\t")      //active
                .append(getModuleString(stampNid) + "\t");     //moduleId
    }

    String getIdString(Chronology chronology){

        if (ExportLookUpCache.isSCTID(chronology)) {
            return lookUpIdentifierFromSemantic(this.snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), chronology);
        } else if (ExportLookUpCache.isLoinc(chronology)) {
            final String loincId = lookUpIdentifierFromSemantic(this.snapshotService, MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromLoincId(loincId);
        } else if (ExportLookUpCache.isRxNorm(chronology)) {
            final String rxnormId = lookUpIdentifierFromSemantic(this.snapshotService, MetaData.RXNORM_CUI____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromRxNormId(rxnormId);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(chronology.getPrimordialUuid());
        }

    }
    String getIdString(int chronologyNid){
        return getIdString(Get.concept(chronologyNid));
    }


    String getTimeString(int stampNid){
        return new SimpleDateFormat("YYYYMMd").format(new Date(Get.stampService().getTimeForStamp(stampNid)));
    }

    String getActiveString(int stampNid){
        return Get.stampService().getStatusForStamp(stampNid).isActive() ? "1" : "0";
    }

    String getModuleString(int stampNid){
        ConceptChronology moduleConcept = Get.concept(Get.stampService().getModuleNidForStamp(stampNid));
        if (ExportLookUpCache.isSCTID(moduleConcept)) {
            return lookUpIdentifierFromSemantic(this.snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), moduleConcept);
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
