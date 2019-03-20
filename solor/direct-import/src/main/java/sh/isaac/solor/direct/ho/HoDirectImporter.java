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
import java.util.concurrent.ConcurrentHashMap;
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
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.direct.DirectImporter;

/**
 *
 * @author kec
 */
public class HoDirectImporter extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

    public final static ConceptProxy LEGACY_HUMAN_DX_ROOT_CONCEPT = new ConceptProxy("Legacy deprecated Human Dx concept", UUID.fromString("29d825d3-6536-4bb8-8ea6-844dfcb3e8f8"));
    public final static ConceptProxy HUMAN_DX_MODULE = new ConceptProxy("Human Dx module", UUID.fromString("f4904690-b9f7-489b-ab63-f649a001a074"));

    
    public final static ConceptProxy REFID_ASSEMBLAGE = new ConceptProxy("refid", UUID.fromString("c7290eda-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy ALLERGEN_ASSEMBLAGE = new ConceptProxy("Allergen", UUID.fromString("c7291164-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy IS_DIAGNOSIS_ASSEMBLAGE = new ConceptProxy("Diagnosis", UUID.fromString("c729136c-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy IS_CATEGORY_ASSEMBLAGE = new ConceptProxy("Category", UUID.fromString("c72915f6-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy SNOMED_MAP_ASSEMBLAGE = new ConceptProxy("SNOMED ID", UUID.fromString("c7291754-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy INEXACT_SNOMED_ASSEMBLAGE = new ConceptProxy("Inexact SNOMED match", UUID.fromString("c729188a-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy SNOMED_SIB_CHILD_ASSEMBLAGE = new ConceptProxy("SNOMED sibling/child", UUID.fromString("c7291a10-3655-11e9-b210-d663bd873d93"));
    
    public final static ConceptProxy HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE = new ConceptProxy("HDX Solor equivalence", UUID.fromString("11e3eea1-b918-452e-87c1-f903d5ff035f"));

    public final static ConceptProxy HDX_LEGACY_IS_A = new ConceptProxy("HDX legacy is-a", UUID.fromString("80da6f99-74c5-4eb4-8eca-f0fe9893ba01"));

    public final static ConceptProxy HDX_ICD10CM_MAP = 
            new ConceptProxy("HDX ICD-10-CM map", 
                    UUID.fromString("5f6cca2d-4667-466a-8113-ec55d54d0f3b"));

    public final static ConceptProxy HDX_ICD10PCS_MAP = 
            new ConceptProxy("HDX ICD-10-PCS map", 
                    UUID.fromString("794f9e7d-5a90-487b-9f93-8ebbacfd492f"));

    public final static ConceptProxy HDX_ICD9_MAP = 
            new ConceptProxy("HDX ICD-9-CM map", 
                    UUID.fromString("674e91ac-e989-4677-9d28-95bff712490f"));

    public final static ConceptProxy HDX_ICF_MAP = 
            new ConceptProxy("HDX ICF map", 
                    UUID.fromString("3903d7a5-3a69-4f1b-8de1-a7c7844f2953"));

    public final static ConceptProxy HDX_ICPC_MAP = 
            new ConceptProxy("HDX ICPC map", 
                    UUID.fromString("0653274c-f1e5-491d-b754-9eaa2be3e67f"));

    public final static ConceptProxy HDX_LOINC_MAP = 
            new ConceptProxy("HDX LOINC map", 
                    UUID.fromString("5dce327c-951d-4496-a468-ec9e655dab3b"));

    public final static ConceptProxy HDX_MDC_MAP = 
            new ConceptProxy("HDX MDC map", 
                    UUID.fromString("3f687795-8ef7-4a98-a4b2-bb7d5d022f4d"));

    public final static ConceptProxy HDX_MESH_MAP = 
            new ConceptProxy("HDX MESH map", 
                    UUID.fromString("70ead1cb-2f80-4f8e-a0ae-584fd254c714"));

    public final static ConceptProxy HDX_RADLEX_MAP = 
            new ConceptProxy("HDX RADLEX map", 
                    UUID.fromString("7686650f-97e0-4447-affc-e19c9e3e0180"));

    public final static ConceptProxy HDX_RXCUI_MAP = 
            new ConceptProxy("HDX RXCUI map", 
                    UUID.fromString("fdd1f394-180e-4582-a1d0-e104c86f2adc"));

    public final static ConceptProxy HDX_CCS_SINGLE_ICD_MAP = 
            new ConceptProxy("HDX CCS-single category icd 10 map", 
                    UUID.fromString("b3d86f60-6618-47d4-ab78-abe39d9cfa97"));

    public final static ConceptProxy HDX_CCS_MULTI_1_ICD_MAP = 
            new ConceptProxy("HDX CCS-multi level 1 ICD 10 map", 
                    UUID.fromString("e5998d38-ea14-4717-9b29-f6db9d21f607"));

    public final static ConceptProxy HDX_CCS_MULTI_2_ICD_MAP = 
            new ConceptProxy("HDX CCS-multi level 2 ICD 10 map", 
                    UUID.fromString("10cd5e46-e4a0-485d-80b0-aabb74e1ff4d"));

    public final static ConceptProxy DIAGNOSIS_NAV_ASSEMBLAGE = new ConceptProxy("Diagnostic entity", 
            UUID.fromString("1c82bc0e-3160-491f-9454-a45a5ab5556d"));
    public final static ConceptProxy CATEGORY_NAV_ASSEMBLAGE = new ConceptProxy("Category entity", 
            UUID.fromString("c38ec034-4173-49b4-8243-09d314385ec3"));
    public final static ConceptProxy UNCATEGORIZED_NAV_ASSEMBLAGE = new ConceptProxy("Uncategorized entity", 
            UUID.fromString("7169c034-c350-4022-a330-f52f363a4e85"));


    private static final int WRITE_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;

    public static HashSet<String> watchTokens = new HashSet<>();

    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
    private List<IndexBuilderService> indexers;
    private final ConcurrentHashMap<HdxConceptHash, ConceptSpecification> hdxSolorConcepts = new ConcurrentHashMap<>();
    private final TaxonomySnapshot taxonomy;
    
    public HoDirectImporter() {
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
        updateTitle("Importing HO from " + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
        this.taxonomy = Get.taxonomyService().getSnapshot(Get.coordinateFactory().createDefaultStatedManifoldCoordinate());
    }

    public ConcurrentHashMap<HdxConceptHash, ConceptSpecification> getHdxSolorConcepts() {
        return hdxSolorConcepts;
    }

    public TaxonomySnapshot getTaxonomy() {
        return taxonomy;
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
            
            buildConcept(REFID_ASSEMBLAGE.getPrimordialUuid(), 
                    REFID_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(ALLERGEN_ASSEMBLAGE.getPrimordialUuid(), 
                    ALLERGEN_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(IS_DIAGNOSIS_ASSEMBLAGE.getPrimordialUuid(), 
                    IS_DIAGNOSIS_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(IS_CATEGORY_ASSEMBLAGE.getPrimordialUuid(), 
                    IS_CATEGORY_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(SNOMED_MAP_ASSEMBLAGE.getPrimordialUuid(), 
                    SNOMED_MAP_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(INEXACT_SNOMED_ASSEMBLAGE.getPrimordialUuid(), 
                    INEXACT_SNOMED_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(SNOMED_SIB_CHILD_ASSEMBLAGE.getPrimordialUuid(), 
                    SNOMED_SIB_CHILD_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(DIAGNOSIS_NAV_ASSEMBLAGE.getPrimordialUuid(), 
                    DIAGNOSIS_NAV_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(CATEGORY_NAV_ASSEMBLAGE.getPrimordialUuid(), 
                    CATEGORY_NAV_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(UNCATEGORIZED_NAV_ASSEMBLAGE.getPrimordialUuid(), 
                    UNCATEGORIZED_NAV_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getPrimordialUuid(), 
                    HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());           
            
            buildConcept(HDX_LEGACY_IS_A.getPrimordialUuid(), 
                    HDX_LEGACY_IS_A.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_ICD10CM_MAP.getPrimordialUuid(), 
                    HDX_ICD10CM_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_ICD10PCS_MAP.getPrimordialUuid(), 
                    HDX_ICD10PCS_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_ICD9_MAP.getPrimordialUuid(), 
                    HDX_ICD9_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_ICF_MAP.getPrimordialUuid(), 
                    HDX_ICF_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_ICPC_MAP.getPrimordialUuid(), 
                    HDX_ICPC_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_LOINC_MAP.getPrimordialUuid(), 
                    HDX_LOINC_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_MDC_MAP.getPrimordialUuid(), 
                    HDX_MDC_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_MESH_MAP.getPrimordialUuid(), 
                    HDX_MESH_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_RADLEX_MAP.getPrimordialUuid(), 
                    HDX_RADLEX_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_RXCUI_MAP.getPrimordialUuid(), 
                    HDX_RXCUI_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_CCS_SINGLE_ICD_MAP.getPrimordialUuid(), 
                    HDX_CCS_SINGLE_ICD_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_CCS_MULTI_1_ICD_MAP.getPrimordialUuid(), 
                    HDX_CCS_MULTI_1_ICD_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
            buildConcept(HDX_CCS_MULTI_2_ICD_MAP.getPrimordialUuid(), 
                    HDX_CCS_MULTI_2_ICD_MAP.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid()); 
            
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
                        commitTime, this);

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
                        commitTime, this);
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
