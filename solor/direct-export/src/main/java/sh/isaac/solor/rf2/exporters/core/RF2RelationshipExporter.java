package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.semantic.version.ObservableLogicGraphVersion;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class RF2RelationshipExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2RelationshipExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        final String modifierId = "900000000000451002"; //Existential restriction modifier (core metadata concept)
        final String isASCTID = rf2ExportHelper.getIdString(TermAux.IS_A.getNid());
        final AtomicInteger roleGroup = new AtomicInteger(0);

        try{

            this.intStream
                    .forEach(nid -> {
                        String characteristicTypeId;
                        int semanticChronologyRelAssemblage = Get.assemblageService().getSemanticChronology(nid).getAssemblageNid();

                        if (semanticChronologyRelAssemblage == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
                            characteristicTypeId = "900000000000011006";    //Inferred relationship (core metadata concept)
                        else if (semanticChronologyRelAssemblage == TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())
                            characteristicTypeId = "900000000000010007";    //Stated relationship (core metadata concept)
                        else
                            characteristicTypeId = "¯\\_(ツ)_/¯";

                        LogicalExpression logicalExpression = ((LatestVersion<ObservableLogicGraphVersion>)
                                this.rf2ExportHelper.getSnapshotService()
                                        .getObservableSemanticVersion(nid)).get().getLogicalExpression();

                        logicalExpression.processDepthFirst((logicNode, treeNodeVisitData) -> {

                            if(logicNode.getNodeSemantic() == NodeSemantic.ROLE_ALL
                                    || logicNode.getNodeSemantic() == NodeSemantic.ROLE_SOME){
                                roleGroup.getAndIncrement();
                            }

                            if (logicNode.getNodeSemantic() == NodeSemantic.CONCEPT) {

                                int conceptChronologyNid = logicNode.getNidForConceptBeingDefined();

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

                                    super.writeToFile(
                                            rf2ExportHelper.getRF2CommonElements(nid)
                                                    .append(rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                                    .append(rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()) + "\t")
                                                    .append(roleGroup.get() + "\t")
                                                    .append(isASCTID + "\t")
                                                    .append(characteristicTypeId + "\t")
                                                    .append(modifierId)
                                                    .append("\r")
                                                    .toString()
                                    );

                                } else if (parentNode instanceof RoleNodeAllWithNids) {

                                    super.writeToFile(
                                            rf2ExportHelper.getRF2CommonElements(nid)
                                                    .append(rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                                    .append(rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()) + "\t")
                                                    .append(roleGroup.get() + "\t")
                                                    .append(rf2ExportHelper.getIdString(((RoleNodeAllWithNids) parentNode).getTypeConceptNid()) + "\t")
                                                    .append(characteristicTypeId + "\t")
                                                    .append(modifierId)
                                                    .append("\r")
                                                    .toString()
                                    );

                                } else if (parentNode instanceof RoleNodeSomeWithNids) {

                                    super.writeToFile(
                                            rf2ExportHelper.getRF2CommonElements(nid)
                                                    .append(rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                                    .append(rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()) + "\t")
                                                    .append(roleGroup.get() + "\t")
                                                    .append(rf2ExportHelper.getIdString(((RoleNodeSomeWithNids) parentNode).getTypeConceptNid()) + "\t")
                                                    .append(characteristicTypeId + "\t")
                                                    .append(modifierId)
                                                    .append("\r")
                                                    .toString()
                                    );
                                }
                            }
                        });
                    });

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }

}
