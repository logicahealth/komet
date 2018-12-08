package sh.isaac.solor.rf2.readers.core;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.observable.semantic.version.ObservableLogicGraphVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class RF2TransitiveClosureReader extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2TransitiveClosureReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " batch of size: " + chronologies.size());
        updateMessage("Processing batch of relationships for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {
        ArrayList<String> returnList = new ArrayList<>();

        final AtomicInteger roleGroup = new AtomicInteger(0);
        String isASCTID = rf2ExportHelper.getIdString(Get.concept(TermAux.IS_A));


        try{

            for(Chronology chronology : this.chronologies){

                LogicalExpression logicalExpression = ((LatestVersion<ObservableLogicGraphVersion>)
                        this.rf2ExportHelper.getSnapshotService()
                                .getObservableSemanticVersion(chronology.getNid())).get().getLogicalExpression();

                logicalExpression.processDepthFirst((logicNode, treeNodeVisitData) -> {

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
                            StringBuilder sb1 = new StringBuilder();

                            returnList.add(sb1.append(rf2ExportHelper.getIdString(conceptChronology) + "\t")
                                    .append(rf2ExportHelper.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())) + "\r")
                                    .toString()
                            );

                        } else if (parentNode instanceof RoleNodeAllWithNids) {

                            StringBuilder sb2 = new StringBuilder();

                            returnList.add(sb2.append(rf2ExportHelper.getIdString(conceptChronology) + "\t")
                                    .append(rf2ExportHelper.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())) + "\r")
                                    .toString()
                            );

                        } else if (parentNode instanceof RoleNodeSomeWithNids) {

                            StringBuilder sb3 = new StringBuilder();

                            returnList.add(sb3.append(rf2ExportHelper.getIdString(conceptChronology) + "\t")
                                    .append(rf2ExportHelper.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())) + "\r")
                                    .toString()
                            );
                        }
                    }
                });

                completedUnitOfWork();
            }


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
