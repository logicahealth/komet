package sh.komet.gui.exportation;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.concept.ObservableConceptVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.komet.gui.manifold.Manifold;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/*
 * aks8m - 5/15/18
 */
public class ConceptExporter extends TimedTaskWithProgressTracker<List<String>> implements PersistTaskResult {


    private final ExportType exportType;
    private final Manifold manifold;

    public ConceptExporter(ExportType exportType, Manifold manifold) {
        this.exportType = exportType;
        this.manifold = manifold;
    }

    //id   effectiveTime   active  moduleId    definitionStatusId
    @Override
    protected List<String> call() throws Exception {

        System.out.println("START================" + LocalDateTime.now());

        ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);
        List<Integer> sctidNids = createListOfConceptNidsFoundInAssemblage(TermAux.SNOMED_IDENTIFIER);
        List<Integer> rxnormNids = createListOfConceptNidsFoundInAssemblage(MetaData.RXNORM_CUI_ASSEMBLAGE____SOLOR);
        List<Integer> loincNids = createListOfConceptNidsFoundInAssemblage(MetaData.CODE____SOLOR);
        List<String> returnList = new ArrayList<>();

        returnList = Get.conceptService().getConceptChronologyStream()
                .map(conceptChronology -> {

                    final StringBuilder sb = new StringBuilder();

                    if (sctidNids.contains(conceptChronology.getNid())) {
                        sb.append(UuidT5Generator.makeLongIdFromUuid(conceptChronology.getPrimordialUuid(), true));  //id
                        sb.append("\t");

                    } else if (loincNids.contains(conceptChronology.getNid())) {
                        final String loincId = getTerminologyIdentifierString(snapshot,MetaData.CODE____SOLOR.getNid(), conceptChronology);
                        sb.append(UuidT5Generator.makeLongIdFromLoincId(loincId));  //id
                        sb.append("\t");

                    } else if (rxnormNids.contains(conceptChronology.getNid())){
                        final String rxnormId = getTerminologyIdentifierString(snapshot,MetaData.RXNORM_CUI_ASSEMBLAGE____SOLOR.getNid(), conceptChronology);
                        sb.append(UuidT5Generator.makeLongIdFromRxNormId(rxnormId));  //id
                        sb.append("\t");

                    }else {
                        sb.append(UuidT5Generator.makeLongIdFromUuid(conceptChronology.getPrimordialUuid(), false));  //id
                        sb.append("\t");

                    }

                    int stampNid = snapshot.getObservableConceptVersion(conceptChronology.getNid()).getStamps().findFirst().getAsInt();

                    sb.append(Get.stampService().getTimeForStamp(stampNid));  //time
                    sb.append("\t");

                    sb.append(Get.stampService().getStatusForStamp(stampNid).isActive() ? "1" : "0");   //active
                    sb.append("\t");

                    ConceptChronology moduleConcept = Get.concept(Get.stampService().getModuleNidForStamp(stampNid));
                    if (sctidNids.contains(moduleConcept.getNid())) {
                        sb.append(UuidT5Generator.makeLongIdFromUuid(moduleConcept.getPrimordialUuid(), true));  //moduleId
                        sb.append("\t");

                    }else {
                        sb.append(UuidT5Generator.makeLongIdFromUuid(moduleConcept.getPrimordialUuid(), false));  //moduleId
                        sb.append("\t");
                    }

                    sb.append(getConceptPrimitiveorSufficientDefinedSCTID(conceptChronology.getNid())); //definitionStatusId

                    return sb.toString();
                })
                .collect(Collectors.toList());

        System.out.println("FINISH===============" + LocalDateTime.now());

        return returnList;
    }

    private List<Integer> createListOfConceptNidsFoundInAssemblage(ConceptSpecification conceptSpecification){
        return Get.assemblageService().getReferencedComponentNidStreamFromAssemblage(conceptSpecification)
                .boxed()
                .collect(Collectors.toList());
    }

    private String getTerminologyIdentifierString(ObservableSnapshotService snapshotService
            , int identifierAssemblageNid, ConceptChronology conceptChronology){

        LatestVersion<ObservableStringVersion> stringVersion = (LatestVersion<ObservableStringVersion>) snapshotService.getObservableSemanticVersion(
                conceptChronology.getSemanticChronologyList().stream()
                        .filter(semanticChronology -> semanticChronology.getAssemblageNid() == identifierAssemblageNid)
                        .findFirst().get().getNid()
        );

        return stringVersion.isPresent() ? stringVersion.get().getString() : "";
    }

    //primitive vs sufficiently
    //If the stated definition contains a sufficient set, it is sufficently defined, if not it is primitive.
    private String getConceptPrimitiveorSufficientDefinedSCTID(int conceptNid) {
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

}
