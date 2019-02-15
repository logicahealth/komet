/*
 * Copyright 2019 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.solor.direct.ho;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.DirectImporter;

/**
 *
 * @author kec
 */
public class HoDirectImporter extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

    public final static ConceptProxy LEGACY_HUMAN_DX_ROOT_CONCEPT = new ConceptProxy("Legacy deprecated Human Dx concept", UUID.fromString("29d825d3-6536-4bb8-8ea6-844dfcb3e8f8"));
    public final static ConceptProxy HUMAN_DX_MODULE = new ConceptProxy("Human Dx module", UUID.fromString("f4904690-b9f7-489b-ab63-f649a001a074"));

    private static final int WRITE_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;

    public static HashSet<String> watchTokens = new HashSet<>();

    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
    private List<IndexBuilderService> indexers;
    
    public HoDirectImporter() {
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
        updateTitle("Importing HO from " + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
    }

    @Override
    protected Void call() throws Exception {
        try {
            this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);
             StampService stampService = Get.stampService();
            int authorNid = TermAux.USER.getNid();
            int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
            int moduleNid = HUMAN_DX_MODULE.getNid();
            int stamp = stampService.getStampSequence(Status.ACTIVE, System.currentTimeMillis(), authorNid, moduleNid, pathNid);
            
            buildConcept(LEGACY_HUMAN_DX_ROOT_CONCEPT.getPrimordialUuid(), 
                    LEGACY_HUMAN_DX_ROOT_CONCEPT.getFullyQualifiedName(), stamp, MetaData.SOLOR_CONCEPT____SOLOR.getNid());           
            
            buildConcept(HUMAN_DX_MODULE.getPrimordialUuid(), 
                    HUMAN_DX_MODULE.getFullyQualifiedName(), stamp, MetaData.MODULE____SOLOR.getNid());           
            
            
            File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
            System.out.println("Importing from: " + importDirectory.getAbsolutePath());

            int fileCount = loadDatabase(importDirectory);
            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks()
                    .remove(this);
        }
    }

    private int loadDatabase(File contentDirectory)
            throws Exception {
        int fileCount = 0;
        List<Path> zipFiles = Files.walk(contentDirectory.toPath())
                .filter(p -> (p.toString().toLowerCase().endsWith(".zip")
                && p.toFile().getName().toLowerCase().startsWith("ho-ho-ho")))
                .collect(Collectors.toList());
        for (Path zipFilePath : zipFiles) {
            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile(), Charset.forName("UTF-8"))) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName()
                            .toLowerCase();
                    if (entryName.endsWith(".tsv") &! entryName.startsWith("__MACOSX".toLowerCase())) {
                        CSVReader reader
                                = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry),
                                        Charset.forName("UTF-8"))))
                                        .withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
                        fileCount++;

                        readHo(reader, entry);
                    }
                }
            }
        }
        return fileCount;
    }

    private void readHo(CSVReader reader, ZipEntry entry)
            throws IOException {
        long commitTime = System.currentTimeMillis();
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String[] columns;
        int deprecated = 0;

        reader.readNext();  // discard header row
        boolean empty = true;
        while ((columns = reader.readNext()) != null) {
            empty = false;
            if (columns[HoWriter.DEPRECATED].equals("True")) {
                deprecated++;
                continue;
            }

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                HoWriter hoWriter = new HoWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing HO records from: " + DirectImporter.trimZipName(
                                entry.getName()),
                        commitTime);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(hoWriter);
            }
        }
        if (empty) {
            LOG.warn("No data in file: " + entry.getName());
        }

        if (empty) {
            LOG.warn("No data in file: " + entry.getName());
        }
        if (!columnsToWrite.isEmpty()) {
                HoWriter hoWriter = new HoWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing HO records from: " + DirectImporter.trimZipName(
                                entry.getName()),
                        commitTime);
                Get.executor()
                        .submit(hoWriter);
        }

        updateMessage("Waiting for HO file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
           try {
               indexer.sync().get();
           } catch (Exception e) {
              LOG.error("problem calling sync on index", e);
           }
        }
        LOG.info("Deprecated entities: " + deprecated);
        updateMessage("Synchronizing HO records to database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }
    
    private void buildConcept(String refId, String conceptName, int stamp, int parentConceptNid) throws IllegalStateException, NoSuchElementException {
        buildConcept(UuidT5Generator.get(UUID.fromString("d96cb408-b9ae-473d-a08d-ece06dbcedf9"), refId), conceptName, stamp, parentConceptNid);
    }
    
    protected void buildConcept(UUID conceptUuid, String conceptName, int stamp, int parentConceptNid) throws IllegalStateException, NoSuchElementException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.necessarySet(eb.and(eb.conceptAssertion(parentConceptNid)));
        ConceptBuilderService builderService = Get.conceptBuilderService();
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "HO",
                eb.build(),
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(conceptUuid);
        List<Chronology> builtObjects = new ArrayList<>();
        builder.build(stamp, builtObjects);
        for (Chronology chronology : builtObjects) {
            Get.identifiedObjectService().putChronologyData(chronology);
            index(chronology);
        }
    }

    private void index(Chronology chronicle) {
        if (chronicle instanceof SemanticChronology) {
            if (chronicle.getVersionType() == VersionType.LOGIC_GRAPH) {
                Get.taxonomyService().updateTaxonomy((SemanticChronology) chronicle);
            }
        }
        for (IndexBuilderService indexer : indexers) {
            indexer.indexNow(chronicle);
        }
    }
    
}
