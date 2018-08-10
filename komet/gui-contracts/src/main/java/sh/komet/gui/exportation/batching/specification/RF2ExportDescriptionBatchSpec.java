package sh.komet.gui.exportation.batching.specification;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.komet.gui.exportation.batching.specification.RF2ExportBatchSpec;
import sh.komet.gui.manifold.Manifold;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * aks8m - 5/22/18
 */
public class RF2ExportDescriptionBatchSpec extends RF2ExportBatchSpec {

    private final Manifold manifold;

    public RF2ExportDescriptionBatchSpec(Manifold manifold) {
        super(manifold);
        this.manifold = manifold;
    }

    @Override
    public List<String> performProcessOnItem(Chronology item) {
        List<String> returnList = new ArrayList<>();

        returnList.add(
                getRF2CommonElements(item) //id, effectiveTime, active, moduleId
                        .append(getIdString(Get.concept(((SemanticChronology) item).getReferencedComponentNid())) + "\t") //conceptId
                        .append(getLanguageCode(item) + "\t") //languageCode
                        .append(getTypeId(item) + "\t") //typeId
                        .append(getTerm(item) + "\t") //term
                        .append(getCaseSignificanceId(item)) //caseSignificanceId
                        .append("\r")
                        .toString());

        return returnList;
    }

    @Override
    public List<Chronology> createItemListToBatch() {
        return Get.conceptService().getConceptChronologyStream()
                .flatMap(conceptChronology -> conceptChronology.getConceptDescriptionList().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void addColumnHeaders(List<String> lines){
        lines.add(0,
                "id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode\ttypeId\tterm\tcaseSignificanceId\r");
    }

    @Override
    public String getReaderUIText() {
        return "Descriptions";
    }

    @Override
    public String getFileName(String rootDirName) {
        return rootDirName + "/Snapshot/Terminology/sct2_Description_Snapshot_" + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt";
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
                super.getSnapshotService().getObservableSemanticVersion(chronology.getNid())).get().getDescriptionTypeConceptNid();

        if(typeNid == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())
            return "900000000000550004";
        else if(typeNid == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000003001";
        else if(typeNid == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000013009";

        return "¯\\_(ツ)_/¯";
    }

    private String getTerm(Chronology chronology){
        return ((LatestVersion<ObservableDescriptionVersion>) super.getSnapshotService().getObservableSemanticVersion(chronology.getNid())).get().getText();
    }

    private String getCaseSignificanceId(Chronology chronology){
        //Entire term case insensitive (core metadata concept)				-	900000000000448009
        //Entire term case sensitive (core metadata concept)				-	900000000000017005
        //Only initial character case insensitive (core metadata concept)	-	900000000000020002

        final int caseSigNid = ((LatestVersion<ObservableDescriptionVersion>)
                super.getSnapshotService().getObservableSemanticVersion(chronology.getNid())).get().getCaseSignificanceConceptNid();


        if( caseSigNid == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid())
            return "900000000000448009";
        else if(caseSigNid == TermAux.DESCRIPTION_CASE_SENSITIVE.getNid())
            return "900000000000017005";
        else if(caseSigNid == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid())
            return "900000000000020002";

        return "¯\\_(ツ)_/¯";
    }
}
