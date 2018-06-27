package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.semantic.version.ObservableLogicGraphVersion;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
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
public class RF2RelationshipSpec extends RF2ReaderSpecification {

    private final Manifold manifold;

    public RF2RelationshipSpec(Manifold manifold, ExportLookUpCache exportLookUpCache) {
        super(manifold, exportLookUpCache);
        this.manifold = manifold;
    }

    @Override
    public void addColumnHeaders(List<byte[]> byteList) throws UnsupportedEncodingException {
        byteList.add(0, ("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId" +
                "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r").getBytes("UTF-8"));

    }

    @Override
    public List<byte[]> readExportData(Chronology chronology) throws UnsupportedEncodingException {

        return getComputedComponents(
                getRF2CommonElements(chronology).toString(),
                chronology);
    }

    private List<byte[]> getComputedComponents(String baseRF2Elements, Chronology chronology){

        List<byte[]> byteList = new ArrayList<>();
        final String modifierId = "900000000000451002"; //Existential restriction modifier (core metadata concept)
        final String roleGroup = "0";
        final String characteristicTypeId;

        if(chronology.getAssemblageNid() == TermAux. EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
            characteristicTypeId = "900000000000011006";    //Inferred relationship (core metadata concept)
        else if(chronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())
            characteristicTypeId = "900000000000010007";    //Stated relationship (core metadata concept)
        else
            characteristicTypeId = "¯\\_(ツ)_/¯";


        LogicalExpression logicalExpression = ((LatestVersion<ObservableLogicGraphVersion>)
                super.getSnapshotService()
                        .getObservableSemanticVersion(chronology.getNid())).get().getLogicalExpression();

        logicalExpression.processDepthFirst((logicNode, treeNodeVisitData) -> {

            if(logicNode.getNodeSemantic() == NodeSemantic.CONCEPT){

                LogicNode parentNode = null;
                LogicNode tempNode = logicNode;

                do{
                    if(parentNode != null)
                        tempNode = parentNode;

                    int parentIndex = treeNodeVisitData.getPredecessorSequence(tempNode.getNodeIndex()).getAsInt();
                    parentNode = logicalExpression.getNode(parentIndex);

                }while(!(parentNode.getNodeSemantic() == NodeSemantic.NECESSARY_SET ||
                        parentNode.getNodeSemantic() == NodeSemantic.SUFFICIENT_SET ||
                        parentNode.getNodeSemantic() == NodeSemantic.ROLE_ALL |
                                parentNode.getNodeSemantic() == NodeSemantic.ROLE_SOME));

                if(parentNode.getNodeSemantic() == NodeSemantic.NECESSARY_SET || parentNode.getNodeSemantic() == NodeSemantic.SUFFICIENT_SET){

                    StringBuilder sb = new StringBuilder();
                    byteList.add(convertStringToUTF8Array(
                            sb.append(baseRF2Elements)
                                    .append(getIdString(Get.concept(logicNode.getNidForConceptBeingDefined())) + "\t") //source
                                    .append(getIdString(Get.concept(((ConceptNodeWithNids)logicNode).getConceptNid())) + "\t") //destination
                                    .append(roleGroup + "\t") //rolegroup
                                    .append(getIdString(Get.concept(TermAux.IS_A)) + "\t") //type
                                    .append(characteristicTypeId + "\t") //charType
                                    .append(modifierId + "\t")  //modifier
                                    .toString()
                                    + "\r"));
                }else if(parentNode instanceof RoleNodeAllWithNids){

                    StringBuilder sb = new StringBuilder();
                    byteList.add(convertStringToUTF8Array(
                            sb.append(baseRF2Elements)
                                    .append(getIdString(Get.concept(logicNode.getNidForConceptBeingDefined())) + "\t") //source
                                    .append(getIdString(Get.concept(((ConceptNodeWithNids)logicNode).getConceptNid())) + "\t") //destination
                                    .append(roleGroup + "\t") //rolegroup
                                    .append(getIdString(Get.concept (((RoleNodeAllWithNids)parentNode).getTypeConceptNid())) + "\t") //type
                                    .append(characteristicTypeId + "\t") //charType
                                    .append(modifierId + "\t")  //modifier
                                    .toString()
                                    + "\r"));
                }else if(parentNode instanceof RoleNodeSomeWithNids){

                    StringBuilder sb = new StringBuilder();
                    byteList.add(convertStringToUTF8Array(
                            sb.append(baseRF2Elements)
                                    .append(getIdString(Get.concept(logicNode.getNidForConceptBeingDefined())) + "\t") //source
                                    .append(getIdString(Get.concept(((ConceptNodeWithNids)logicNode).getConceptNid())) + "\t") //destination
                                    .append(roleGroup + "\t") //rolegroup
                                    .append(getIdString(Get.concept (((RoleNodeSomeWithNids)parentNode).getTypeConceptNid())) + "\t") //type
                                    .append(characteristicTypeId + "\t") //charType
                                    .append(modifierId + "\t")  //modifier
                                    .toString()
                                    + "\r"));
                }
            }
        });

        return byteList;
    }

    private byte[] convertStringToUTF8Array(String string){
        byte[] bytes = new byte[1025];

        try {
            bytes = string.getBytes("UTF-8");
        }catch (UnsupportedEncodingException uueE){
            uueE.printStackTrace();
        }

        return bytes;
    }



    @Override
    public String getReaderUIText() {
        return "Relationships";
    }

    @Override
    public List<Chronology> createChronologyList() {
        return Get.conceptService().getConceptChronologyStream()
                .flatMap(conceptChronology -> conceptChronology.getSemanticChronologyList().stream())
                .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH)
                .limit(2000)
                .collect(Collectors.toList());
    }

    @Override
    public String getFileName(String rootDirName) {
        return rootDirName + "/Snapshot/Terminology/sct2_Relationship_Snapshot_" + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt";
    }
}
