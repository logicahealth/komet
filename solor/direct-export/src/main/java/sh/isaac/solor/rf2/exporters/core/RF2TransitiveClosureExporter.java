package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2TransitiveClosureExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2TransitiveClosureExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        try{

            this.intStream
                    .forEach(nid -> {

                        super.clearLineOutput();
                        super.incrementProgressCount();

                        for(Version version : Get.assemblageService().getSemanticChronology(nid).getVersionList()){

                            LogicalExpression logicalExpression = ((LogicGraphVersion)version).getLogicalExpression();
                            logicalExpression.processDepthFirst((logicNode, treeNodeVisitData) -> {

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

                                    super.outputToWrite
                                            .append(this.rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()))
                                            .append("\r\n");

                                }
                            });
                        }

                        super.writeToFile();
                        super.tryAndUpdateProgressTracker();
                    });

            super.clearLineOutput();
            ConceptProxy identifierSchemeProxy = new ConceptProxy("Identifier scheme (core metadata concept)",
                    UUID.fromString("72c45251-355f-3f9f-9ee7-5bc803d01654"));
            Arrays.stream(Get.taxonomyService().getSnapshot(this.rf2ExportHelper.getManifold()).getTaxonomyChildConceptNids(TermAux.IDENTIFIER_SOURCE.getNid()))
                    .forEach(nid -> {

                        if(!Get.taxonomyService().getSnapshot(rf2ExportHelper.getManifold()).isKindOf(nid, identifierSchemeProxy.getNid())) {

                            super.outputToWrite
                                    .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(identifierSchemeProxy.getNid()))
                                    .append("\r\n");
                        }
                    });
            super.writeToFile();

            super.clearLineOutput();
            ConceptProxy moduleProxy = new ConceptProxy("Module (core metadata concept)",
                    UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));
            Arrays.stream(Get.taxonomyService().getSnapshot(this.rf2ExportHelper.getManifold()).getTaxonomyChildConceptNids(TermAux.SOLOR_MODULE.getNid()))
                    .forEach(nid -> {

                        if(!Get.taxonomyService().getSnapshot(rf2ExportHelper.getManifold()).isKindOf(nid, moduleProxy.getNid())) {

                            super.outputToWrite
                                    .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(moduleProxy.getNid()))
                                    .append("\r\n");
                        }
                    });
            super.writeToFile();

            super.clearLineOutput();
            ConceptProxy attributeTypeProxy = new ConceptProxy("Attribute type (foundation metadata concept)",
                    UUID.fromString("34e794d9-0405-3aa1-adf5-64801950c397"));
            int[] attNidsToWrite = new int[]{MetaData.INTEGER_FIELD____SOLOR.getNid(),
                    MetaData.STRING_FIELD____SOLOR.getNid(), MetaData.LONG_FIELD____SOLOR.getNid()};
            Arrays.stream(attNidsToWrite)
                    .forEach(nid -> {

                        if(!Get.taxonomyService().getSnapshot(rf2ExportHelper.getManifold()).isKindOf(nid, attributeTypeProxy.getNid())) {

                            super.outputToWrite
                                    .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(attributeTypeProxy.getNid()))
                                    .append("\r\n");
                        }
                    });
            super.writeToFile();



        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
