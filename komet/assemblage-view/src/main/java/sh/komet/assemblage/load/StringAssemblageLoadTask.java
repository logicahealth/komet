/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.assemblage.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.solor.direct.BrittleRefsetWriter;
import static sh.isaac.solor.direct.DirectImporter.trimZipName;
import sh.isaac.solor.direct.ImportSpecification;
import sh.isaac.solor.direct.ImportStreamType;
import sh.isaac.solor.direct.ImportType;

/**
 * TODO: move to direct import module.
 *
 * @author kec
 */
public class StringAssemblageLoadTask extends TimedTaskWithProgressTracker<Void> {

    private static final int SRF_ID_INDEX = 0;
    private static final int SRF_STATUS_INDEX = 1;
    private static final int SRF_TIME_INDEX = 2;
    private static final int SRF_AUTHOR_INDEX = 3;
    private static final int SRF_MODULE_INDEX = 4;
    private static final int SRF_PATH_INDEX = 5;
    private static final int SRF_ASSEMBLAGE_ID_INDEX = 6;
    private static final int SRF_REFERENCED_COMPONENT_ID_INDEX = 7;
    private static final int SRF_VARIABLE_FIELD_START = 7;

    private final ConceptBuilderService builderService;
    private final LogicalExpressionBuilderService expressionBuilderService;
    private final File fileToImport;
    private final String assemblageName;

    public StringAssemblageLoadTask(File fileToImport, String assemblageName) {
        this.fileToImport = fileToImport;
        this.assemblageName = assemblageName;
        this.builderService = Get.conceptBuilderService();
        this.expressionBuilderService = Get.logicalExpressionBuilderService();
    }

    @Override
    protected Void call() throws Exception {
        ZonedDateTime zonedDateTime = DateTimeUtil.epochToZonedDateTime(System.currentTimeMillis());
        String dateTime = DateTimeUtil.format(zonedDateTime);
        ConceptSpecification assemblageSpec = build(makeBuilder(assemblageName, "SOLOR", MetaData.ASSEMBLAGE____SOLOR), UUID.randomUUID().toString());
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        int writePermits = 4;
        Semaphore writeSemaphore = new Semaphore(writePermits);
        ImportSpecification importSpecification = new ImportSpecification(null, ImportStreamType.STR1_REFSET);
        String rowString;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToImport))) {
            while ((rowString = reader.readLine()) != null) {
                String[] columns = new String[]{
                    UUID.randomUUID().toString(),
                    "1",
                    dateTime,
                    MetaData.USER____SOLOR.getPrimordialUuid().toString(),
                    MetaData.SOLOR_MODULE____SOLOR.getPrimordialUuid().toString(),
                    MetaData.DEVELOPMENT_PATH____SOLOR.getPrimordialUuid().toString(),
                    assemblageSpec.getPrimordialUuid().toString(),
                    assemblageSpec.getPrimordialUuid().toString(),
                    rowString
                };
                columnsToWrite.add(columns);

                if (columnsToWrite.size() == writeSize) {
                    BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, writeSemaphore,
                            "Processing s semantics from: " + fileToImport.getName(),
                            importSpecification, ImportType.ACTIVE_ONLY);
                    columnsToWrite = new ArrayList<>(writeSize);
                    Get.executor()
                            .submit(writer);
                }
            }
            if (!columnsToWrite.isEmpty()) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, writeSemaphore,
                        "Processing s semantics from: " + fileToImport.getName(),
                        importSpecification, ImportType.ACTIVE_ONLY);
                Get.executor()
                        .submit(writer);
            }
            writeSemaphore.acquireUninterruptibly(writePermits);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    LOG.error("problem calling sync on index", e);
                }
            }
            updateMessage("Synchronizing semantic database...");
            Get.assemblageService().sync();
            writeSemaphore.release(writePermits);
        }
        return null;
    }

    private ConceptBuilder makeBuilder(String name, String tag, ConceptSpecification parentSpec) {
        return this.builderService.getDefaultConceptBuilder(name, tag, makeExpression(parentSpec), MetaData.SOLOR_CONCEPT_ASSEMBLAGE____SOLOR.getNid());
    }

    private LogicalExpression makeExpression(ConceptSpecification parentSpec) {
        LogicalExpressionBuilder defBuilder = expressionBuilderService.getLogicalExpressionBuilder();
        NecessarySet(And(ConceptAssertion(parentSpec, defBuilder)));
        return defBuilder.build();
    }

    private ConceptSpecification build(ConceptBuilder builder, String uuidStr) throws IllegalStateException {
        int stampSequence = Get.stampService()
                .getStampSequence(Status.ACTIVE,
                        System.currentTimeMillis(),
                        MetaData.USER____SOLOR.getNid(),
                        MetaData.SOLOR_MODULE____SOLOR.getNid(),
                        MetaData.DEVELOPMENT_PATH____SOLOR.getNid());
        builder.setPrimordialUuid(uuidStr);
        final List<Chronology> builtObjects = new ArrayList<>();
        builder.build(stampSequence, builtObjects);
        builtObjects.forEach((builtObject) -> {
            if (builtObject instanceof ConceptChronology) {
                Get.conceptService().writeConcept(
                        (ConceptChronology) builtObject);
                ConceptChronology restored = Get.conceptService().getConceptChronology(((ConceptChronology) builtObject).getNid());
                if (restored.getAssemblageNid() >= 0) {
                    LOG.error("Bad restore of: " + restored);
                }
            } else if (builtObject instanceof SemanticChronology) {
                SemanticChronology sc = (SemanticChronology) builtObject;
                Get.assemblageService().writeSemanticChronology((SemanticChronology) builtObject);
                if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
                    Get.taxonomyService().updateTaxonomy(sc);
                }

            } else {
                throw new UnsupportedOperationException("Can't handle: " + builtObject);
            }
        });
        return Get.conceptSpecification(Get.identifierService().getNidForUuids(builder.getPrimordialUuid()));
    }

}
