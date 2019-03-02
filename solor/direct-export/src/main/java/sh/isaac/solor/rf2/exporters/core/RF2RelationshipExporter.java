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
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class RF2RelationshipExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;
    private RF2Configuration rf2Configuration;

    public RF2RelationshipExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2Configuration = rf2Configuration;
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        final String modifierId = "900000000000451002"; //Existential restriction modifier (core metadata concept)
        final String isASCTID = rf2ExportHelper.getIdString(TermAux.IS_A.getNid());
        final AtomicInteger roleGroup = new AtomicInteger(0);

        try{

            final StringBuilder linesToWrite = new StringBuilder();

            this.intStream
                    .forEach(nid -> {

                        linesToWrite.setLength(0);

                        for(Version version : Get.assemblageService().getSemanticChronology(nid).getVersionList()){

                            String characteristicTypeId;
                            int semanticRelationshipAssemblage = version.getAssemblageNid();

                            if (semanticRelationshipAssemblage == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
                                characteristicTypeId = "900000000000011006";
                            else if (semanticRelationshipAssemblage == TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())
                                characteristicTypeId = "900000000000010007";
                            else
                                characteristicTypeId = "Issue:RF2RelationshipExporter()";

                            LogicalExpression logicalExpression = ((LogicGraphVersion)version).getLogicalExpression();
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

                                        linesToWrite
                                                .append(this.rf2ExportHelper.getIdString(version) + "\t")
                                                .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                                .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()) + "\t")
                                                .append(roleGroup.get() + "\t")
                                                .append(isASCTID + "\t")
                                                .append(characteristicTypeId + "\t")
                                                .append(modifierId)
                                                .append("\r");

                                    } else if (parentNode instanceof RoleNodeAllWithNids) {

                                        linesToWrite
                                                .append(this.rf2ExportHelper.getIdString(version) + "\t")
                                                .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                                .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()) + "\t")
                                                .append(roleGroup.get() + "\t")
                                                .append(this.rf2ExportHelper.getIdString(((RoleNodeAllWithNids) parentNode).getTypeConceptNid()) + "\t")
                                                .append(characteristicTypeId + "\t")
                                                .append(modifierId)
                                                .append("\r");

                                    } else if (parentNode instanceof RoleNodeSomeWithNids) {

                                        linesToWrite
                                                .append(this.rf2ExportHelper.getIdString(version) + "\t")
                                                .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                                .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(conceptChronologyNid) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(((ConceptNodeWithNids) logicNode).getConceptNid()) + "\t")
                                                .append(roleGroup.get() + "\t")
                                                .append(this.rf2ExportHelper.getIdString(((RoleNodeSomeWithNids) parentNode).getTypeConceptNid()) + "\t")
                                                .append(characteristicTypeId + "\t")
                                                .append(modifierId)
                                                .append("\r");

                                    }
                                }
                            });

                        }

                        super.writeStringToFile(linesToWrite.toString());
                    });

            linesToWrite.setLength(0);

            String dateTime = DateTimeFormatter.ofPattern("YYYYMMdd").format(rf2Configuration.getLocalDateTime());
            StringBuilder isoInstantBuilder = new StringBuilder();

            // To IsoInstant: '2011-12-03T00:00:00Z'
            isoInstantBuilder.append(dateTime, 0, 4);
            isoInstantBuilder.append("-");
            isoInstantBuilder.append(dateTime, 4, 6);
            isoInstantBuilder.append("-");
            isoInstantBuilder.append(dateTime, 6, 8);
            isoInstantBuilder.append("T00:00:00Z");
            TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(isoInstantBuilder.toString());

            final long time = accessor.getLong(ChronoField.INSTANT_SECONDS) * 1000;

            ConceptProxy identifierSchemeProxy = new ConceptProxy("Identifier scheme (core metadata concept)",
                    UUID.fromString("72c45251-355f-3f9f-9ee7-5bc803d01654"));
            Arrays.stream(Get.taxonomyService().getSnapshot(this.rf2ExportHelper.getManifold()).getTaxonomyChildConceptNids(TermAux.IDENTIFIER_SOURCE.getNid()))
                    .forEach(nid -> {

                        if(!Get.taxonomyService().getSnapshot(rf2ExportHelper.getManifold()).isKindOf(nid, identifierSchemeProxy.getNid())) {

                            UUID relId = UuidT5Generator.get(
                                    identifierSchemeProxy.getPrimordialUuid().toString() +
                                            Get.concept(nid).getPrimordialUuid().toString());

                            linesToWrite
                                    .append(UuidT5Generator.makeSolorIdFromUuid(relId) + "\t")
                                    .append(new SimpleDateFormat("YYYYMMdd").format(new Date(time)) + "\t")
                                    .append("1" + "\t")
                                    .append(this.rf2ExportHelper.getIdString(TermAux.SOLOR_MODULE.getNid()) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(identifierSchemeProxy.getNid()) + "\t")
                                    .append("0" + "\t")
                                    .append(isASCTID + "\t")
                                    .append("900000000000010007" + "\t")
                                    .append(modifierId)
                                    .append("\r");
                        }
                    });

            super.writeStringToFile(linesToWrite.toString());
            linesToWrite.setLength(0);

            ConceptProxy moduleProxy = new ConceptProxy("Module (core metadata concept)",
                    UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));
            Arrays.stream(Get.taxonomyService().getSnapshot(this.rf2ExportHelper.getManifold()).getTaxonomyChildConceptNids(TermAux.SOLOR_MODULE.getNid()))
                    .forEach(nid -> {

                        if(!Get.taxonomyService().getSnapshot(rf2ExportHelper.getManifold()).isKindOf(nid, moduleProxy.getNid())) {

                            UUID relId = UuidT5Generator.get(
                                    moduleProxy.getPrimordialUuid().toString() +
                                            Get.concept(nid).getPrimordialUuid().toString());

                            linesToWrite
                                    .append(UuidT5Generator.makeSolorIdFromUuid(relId) + "\t")
                                    .append(new SimpleDateFormat("YYYYMMdd").format(new Date(time)) + "\t")
                                    .append("1" + "\t")
                                    .append(this.rf2ExportHelper.getIdString(TermAux.SOLOR_MODULE.getNid()) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(moduleProxy.getNid()) + "\t")
                                    .append("0" + "\t")
                                    .append(isASCTID + "\t")
                                    .append("900000000000010007" + "\t")
                                    .append(modifierId)
                                    .append("\r");
                        }
                    });

            super.writeStringToFile(linesToWrite.toString());
            linesToWrite.setLength(0);

            ConceptProxy attributeTypeProxy = new ConceptProxy("Attribute type (foundation metadata concept)",
                    UUID.fromString("34e794d9-0405-3aa1-adf5-64801950c397"));
            int[] attNidsToWrite = new int[]{MetaData.INTEGER_FIELD____SOLOR.getNid(),
                    MetaData.STRING_FIELD____SOLOR.getNid(), MetaData.LONG_FIELD____SOLOR.getNid()};
            Arrays.stream(attNidsToWrite)
                    .forEach(nid -> {

                        if(!Get.taxonomyService().getSnapshot(rf2ExportHelper.getManifold()).isKindOf(nid, attributeTypeProxy.getNid())) {

                            UUID relId = UuidT5Generator.get(
                                    attributeTypeProxy.getPrimordialUuid().toString() +
                                            Get.concept(nid).getPrimordialUuid().toString());

                            linesToWrite
                                    .append(UuidT5Generator.makeSolorIdFromUuid(relId) + "\t")
                                    .append(new SimpleDateFormat("YYYYMMdd").format(new Date(time)) + "\t")
                                    .append("1" + "\t")
                                    .append(this.rf2ExportHelper.getIdString(TermAux.SOLOR_MODULE.getNid()) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(attributeTypeProxy.getNid()) + "\t")
                                    .append("0" + "\t")
                                    .append(isASCTID + "\t")
                                    .append("900000000000010007" + "\t")
                                    .append(modifierId)
                                    .append("\r");
                        }
                    });

            super.writeStringToFile(linesToWrite.toString());

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
