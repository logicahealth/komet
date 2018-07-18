package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.komet.gui.manifold.Manifold;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * aks8m - 5/22/18
 */
public class RF2ConceptSpec extends RF2ReaderSpecification {


    private final Manifold manifold;

    public RF2ConceptSpec(Manifold manifold, ExportLookUpCache exportLookUpCache) {
        super(manifold, exportLookUpCache);
        this.manifold = manifold;
    }

    @Override
    public void addColumnHeaders(List<String> lines){
        lines.add(0, "id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId\r");
    }

    @Override
    public List<String> readExportData(Chronology chronology) {

        List<String> returnList = new ArrayList<>();

        returnList.add(getRF2CommonElements(chronology) //id, effectiveTime, active, moduleId
                .append(getConceptPrimitiveOrSufficientDefinedSCTID(chronology.getNid()))   //definitionStatusId
                .append("\r")
                .toString());

        return returnList;
    }

    @Override
    public String getReaderUIText() {
        return "Concepts";
    }

    @Override
    public List<Chronology> createChronologyList() {
        return Get.conceptService().getConceptChronologyStream().collect(Collectors.toList());
    }

    @Override
    public String getFileName(String rootDirName) {
        return rootDirName + "/Snapshot/Terminology/sct2_Concept_Snapshot_" + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt";
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
}
