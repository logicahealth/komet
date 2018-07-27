package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.komet.gui.manifold.Manifold;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * aks8m - 5/22/18
 */
public class RF2DescriptionSpec extends RF2ReaderSpecification{

    private final Manifold manifold;

    public RF2DescriptionSpec(Manifold manifold) {
        super(manifold);
        this.manifold = manifold;
    }

    @Override
    public void addColumnHeaders(List<String> lines){
        lines.add(0,
                "id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode\ttypeId\tterm\tcaseSignificanceId\r");
    }

    @Override
    public List<String> readExportData(Chronology chronology) {

        List<String> returnList = new ArrayList<>();

        returnList.add(
                getRF2CommonElements(chronology) //id, effectiveTime, active, moduleId
                .append(getIdString(Get.concept(((SemanticChronology) chronology).getReferencedComponentNid())) + "\t") //conceptId
                .append(getLanguageCode(chronology) + "\t") //languageCode
                .append(getTypeId(chronology) + "\t") //typeId
                .append(getTerm(chronology) + "\t") //term
                .append(getCaseSignificanceId(chronology)) //caseSignificanceId
                .append("\r")
                .toString());

        return returnList;
    }

    @Override
    public String getReaderUIText() {
        return "Descriptions";
    }

    @Override
    public List<Chronology> createChronologyList() {
        return Get.conceptService().getConceptChronologyStream()
                .flatMap(conceptChronology -> conceptChronology.getConceptDescriptionList().stream())
                .collect(Collectors.toList());
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
