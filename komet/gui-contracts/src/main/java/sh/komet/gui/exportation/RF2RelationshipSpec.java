package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.semantic.version.ObservableLogicGraphVersion;
import sh.isaac.api.observable.semantic.version.brittle.ObservableRf2Relationship;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.komet.gui.manifold.Manifold;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/*
 * aks8m - 5/22/18
 */
public class RF2RelationshipSpec extends RF2ReaderSpecification {

    private final Manifold manifold;
    private String isASCTID = super.getIdString(Get.concept(TermAux.IS_A));

    public RF2RelationshipSpec(Manifold manifold) {
        super(manifold);
        this.manifold = manifold;
    }

    @Override
    public void addColumnHeaders(List<String> lines) {
        lines.add(0, ("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId" +
                "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r"));

    }

    @Override
    public List<String> readExportData(Chronology chronology) {
        List<String> returnList = new ArrayList<>();
        final String characteristicTypeId;
        final String modifierId = "900000000000451002"; //Existential restriction modifier (core metadata concept)
        final AtomicInteger roleGroup = new AtomicInteger(0);


        if (chronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
            characteristicTypeId = "900000000000011006";    //Inferred relationship (core metadata concept)
        else if (chronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())
            characteristicTypeId = "900000000000010007";    //Stated relationship (core metadata concept)
        else
            characteristicTypeId = "¯\\_(ツ)_/¯";

        LogicalExpression logicalExpression = ((LatestVersion<ObservableLogicGraphVersion>)
                super.getSnapshotService()
                        .getObservableSemanticVersion(chronology.getNid())).get().getLogicalExpression();

        logicalExpression.processDepthFirst((logicNode, treeNodeVisitData) -> {

            if(logicNode.getNodeSemantic() == NodeSemantic.ROLE_ALL
                    || logicNode.getNodeSemantic() == NodeSemantic.ROLE_SOME){
                roleGroup.getAndIncrement();
            }

            if (logicNode.getNodeSemantic() == NodeSemantic.CONCEPT) {

                ConceptChronology conceptChronology = Get.concept(logicNode.getNidForConceptBeingDefined());

                LogicNode parentNode = null;
                LogicNode tempNode = logicNode;

                do {
                    if (parentNode != null)
                        tempNode = parentNode;

                    int parentIndex = treeNodeVisitData.getPredecessorNid(tempNode.getNodeIndex()).getAsInt();
                    parentNode = logicalExpression.getNode(parentIndex);

                } while (!(parentNode.getNodeSemantic() == NodeSemantic.NECESSARY_SET ||
                        parentNode.getNodeSemantic() == NodeSemantic.SUFFICIENT_SET ||
                        parentNode.getNodeSemantic() == NodeSemantic.ROLE_ALL |
                                parentNode.getNodeSemantic() == NodeSemantic.ROLE_SOME));

                if (parentNode.getNodeSemantic() == NodeSemantic.NECESSARY_SET || parentNode.getNodeSemantic() == NodeSemantic.SUFFICIENT_SET) {

                    returnList.add(compareToRF2RelSemantic(
                            chronology,
                            conceptChronology,
                            super.getIdString(conceptChronology),
                            super.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())),
                            String.valueOf(roleGroup.get()),
                            isASCTID,
                            characteristicTypeId,
                            modifierId));

                } else if (parentNode instanceof RoleNodeAllWithNids) {

                    returnList.add(compareToRF2RelSemantic(
                            chronology,
                            conceptChronology,
                            super.getIdString(conceptChronology),
                            super.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())),
                            String.valueOf(roleGroup.get()),
                            super.getIdString(Get.concept(((RoleNodeAllWithNids) parentNode).getTypeConceptNid())),
                            characteristicTypeId,
                            modifierId));

                } else if (parentNode instanceof RoleNodeSomeWithNids) {

                    returnList.add(compareToRF2RelSemantic(
                            chronology,
                            conceptChronology,
                            super.getIdString(conceptChronology),
                            super.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())),
                            String.valueOf(roleGroup.get()),
                            super.getIdString(Get.concept(((RoleNodeSomeWithNids) parentNode).getTypeConceptNid())),
                            characteristicTypeId,
                            modifierId));

                }
            }
        });

        return returnList;
    }

    private String compareToRF2RelSemantic(Chronology logicGraphChronology,
                                           ConceptChronology conceptChronology,
                                           String solorSourceId,
                                           String solorDestinationId,
                                           String solorRelationhsipGroup,
                                           String solorTypeId,
                                           String solorCharacteristicTypeId,
                                           String solorModifierId){

        //Build out SOLOR relationship strings regardless...
        StringBuilder solorString = new StringBuilder()
                .append(solorSourceId + "\t")
                .append(solorDestinationId + "\t")
                .append(solorRelationhsipGroup + "\t")
                .append(solorTypeId + "\t")
                .append(solorCharacteristicTypeId + "\t")
                .append(solorModifierId)
                .append("\r");

//        Optional<SemanticChronology> optionalRF2RelationshipSemantic = conceptChronology.getSemanticChronologyList().stream()
//                .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.RF2_RELATIONSHIP)
//                .filter(semanticChronology -> semanticChronology.getAssemblageNid() ==
//                        (logicGraphChronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid()?
//                                TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid()
//                                : TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid() ) )
//                .findFirst();
//
//        if(optionalRF2RelationshipSemantic.isPresent()){
//
//            ObservableRf2Relationship observableRf2Relationship = ((LatestVersion<ObservableRf2Relationship>)
//                    getSnapshotService().getObservableSemanticVersion(optionalRF2RelationshipSemantic
//                            .get().getNid())).get();
//
//            final String rf2SourceId = getIdString(conceptChronology);
//            final String rf2DestinationId = String.valueOf(super.getIdString(observableRf2Relationship.destinationNidProperty().get()));
//            final String rf2RelationshipGroup = String.valueOf(observableRf2Relationship.relationshipGroupProperty().get());
//            final String rf2TypeId = String.valueOf(super.getIdString(observableRf2Relationship.typeNidProperty().get()));
//            final String rf2CharacteristicTypeId = String.valueOf(super.getIdString(observableRf2Relationship.characteristicNidProperty().get()));
//            final String rf2ModifierId = String.valueOf(super.getIdString(observableRf2Relationship.modifierNidProperty().get()));
//
//            if( solorSourceId.equals(rf2SourceId)
//                    && solorDestinationId.equals(rf2DestinationId)
//                    && solorRelationhsipGroup.equals(rf2RelationshipGroup)
//                    && solorTypeId.equals(rf2TypeId)
//                    && solorCharacteristicTypeId.equals(rf2CharacteristicTypeId)
//                    && solorModifierId.equals(rf2ModifierId)){
//
//                StringBuilder rf2String = new StringBuilder()
//                        .append(rf2SourceId + "\t")
//                        .append(rf2DestinationId + "\t")
//                        .append(rf2RelationshipGroup + "\t")
//                        .append(rf2TypeId + "\t")
//                        .append(rf2CharacteristicTypeId + "\t")
//                        .append(rf2ModifierId + "\t")
//                        .append("\r");
//
//                return getRF2CommonElements(observableRf2Relationship.getChronology())
//                        .append(rf2String).toString();
//            }
//        }

        return getRF2CommonElements(logicGraphChronology)
                .append(solorString).toString();
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
                .collect(Collectors.toList());
    }

    @Override
    public String getFileName(String rootDirName) {
        return rootDirName + "/Snapshot/Terminology/sct2_Relationship_Snapshot_" + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt";
    }
}
