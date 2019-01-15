package sh.isaac.solor.rf2.readers.core;

import org.apache.commons.lang.ArrayUtils;
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
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.isaac.solor.rf2.utility.RF2FileWriter;
import sh.komet.gui.manifold.Manifold;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class RF2VersionedTransitiveClosureReader extends TimedTaskWithProgressTracker<Void> {

    private final RF2ExportHelper rf2ExportHelper;
    private final Stream<? extends Chronology> streamPage;
    private final Semaphore readSemaphore;
    private final Manifold manifold;
    private final RF2Configuration rf2Configuration;
    private final RF2FileWriter rf2FileWriter;

    public RF2VersionedTransitiveClosureReader(Stream streamPage, Semaphore readSemaphore, Manifold manifold,
                                               RF2Configuration rf2Configuration, long pageSize) {
        this.streamPage = streamPage;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.rf2Configuration = rf2Configuration;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);
        this.rf2FileWriter = new RF2FileWriter();

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + this.rf2Configuration.getMessage() + " batch of size: " + pageSize);
        updateMessage("Processing batch of relationships for RF2 Export");
        addToTotalWork(pageSize + 1);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {
        ArrayList<Byte[]> writeBytes = new ArrayList<>();

        final AtomicInteger roleGroup = new AtomicInteger(0);
        String isASCTID = rf2ExportHelper.getIdString(Get.concept(TermAux.IS_A));


        try{

            this.streamPage
                    .forEach(chronology -> {
                        LogicalExpression logicalExpression = ((LatestVersion<ObservableLogicGraphVersion>)
                                this.rf2ExportHelper.getSnapshotService()
                                        .getObservableSemanticVersion(chronology.getNid())).get().getLogicalExpression();

                        int stampNid = rf2ExportHelper.getSnapshotService()
                                .getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();

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
                                    final StringBuilder sb1 = new StringBuilder();

                                    writeBytes.add(ArrayUtils.toObject(sb1.append(rf2ExportHelper.getIdString(conceptChronology) + "\t")
                                            .append(rf2ExportHelper.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())) + "\t")
                                            .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                            .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                            .append("\r")
                                            .toString().getBytes(Charset.forName("UTF-8"))));

                                } else if (parentNode instanceof RoleNodeAllWithNids) {

                                    final StringBuilder sb2 = new StringBuilder();

                                    writeBytes.add(ArrayUtils.toObject(sb2.append(rf2ExportHelper.getIdString(conceptChronology) + "\t")
                                            .append(rf2ExportHelper.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())) + "\t")
                                            .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                            .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                            .append("\r")
                                            .toString().getBytes(Charset.forName("UTF-8"))));

                                } else if (parentNode instanceof RoleNodeSomeWithNids) {

                                    final StringBuilder sb3 = new StringBuilder();

                                    writeBytes.add(ArrayUtils.toObject(sb3.append(rf2ExportHelper.getIdString(conceptChronology) + "\t")
                                            .append(rf2ExportHelper.getIdString(Get.concept(((ConceptNodeWithNids) logicNode).getConceptNid())) + "\t")
                                            .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                            .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                            .append("\r")
                                            .toString().getBytes(Charset.forName("UTF-8"))));

                                }
                            }
                        });

                        completedUnitOfWork();

                    });

            updateTitle("Writing " + rf2Configuration.getMessage() + " RF2 file");
            updateMessage("Writing to " + rf2Configuration.getFilePath());

            rf2FileWriter.writeToFile(writeBytes, this.rf2Configuration);

            completedUnitOfWork();


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
