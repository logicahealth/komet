/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.solor.direct;

//~--- JDK imports ------------------------------------------------------------
import com.opencsv.CSVReader;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.solor.ContentProvider;
import sh.isaac.solor.ContentStreamProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//~--- non-JDK imports --------------------------------------------------------


//~--- classes ----------------------------------------------------------------
/**
 * Loader code to convert RF2 format fileCount into the ISAAC format.
 */
public class DirectImporter
        extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

    private static final int WRITE_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;

    public static HashSet<String> watchTokens = new HashSet<>();

    public static Boolean importDynamic = false;

    /**
     * The date format parser.
     */
    protected static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyyMMdd");

    //~--- fields --------------------------------------------------------------
    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);

    protected final ImportType importType;

    protected final List<ContentProvider> entriesToImport;
    protected File importDirectory;
    private HashMap<String, ArrayList<DynamicColumnInfo>> refsetColumnInfo = null;  //refset SCTID to column information from the refset spec
    public static Boolean SRF_IMPORT = false;

    //~--- constructors --------------------------------------------------------
    public DirectImporter(ImportType importType) {
        this.importType = importType;
        this.entriesToImport = null;
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
//        watchTokens.add("89587004"); // Removal of foreign body from abdominal cavity (procedure)
//        watchTokens.add("84971000000100"); // PBCL flag true (attribute)
//        watchTokens.add("123101000000107"); // PBCL flag true: report, request, level, test (qualifier value)

        updateTitle("Importing from from" + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
    }

    public DirectImporter(ImportType importType, List<ContentProvider> entriesToImport) {
        this.importType = importType;
        this.entriesToImport = entriesToImport;
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
//        watchTokens.add("89587004"); // Removal of foreign body from abdominal cavity (procedure)
//        watchTokens.add("84971000000100"); // PBCL flag true (attribute)
//        watchTokens.add("123101000000107"); // PBCL flag true: report, request, level, test (qualifier value)

        updateTitle("Importing from from" + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
    }
    
    public DirectImporter(ImportType importType, File importDirectory) {
        this.importType = importType;
        this.entriesToImport = null;
        this.importDirectory = importDirectory;

        updateTitle("Importing from RF2 from" + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Execute.
     *
     * @throws java.lang.Exception
     */
    @Override
    public Void call()
            throws Exception {
        try {
            final long time = System.currentTimeMillis();

            if (this.entriesToImport != null) {
                ArrayList<ImportSpecification> specificationsToImport = new ArrayList<>();

                SRF_IMPORT = this.entriesToImport.get(0).getStreamSourceName().toLowerCase().startsWith("srf_")
                        ? true : false;

                for (ContentProvider entry : this.entriesToImport) {
                    processEntry(entry, specificationsToImport);
                }
                doImport(specificationsToImport, time);
            } else {
                File importDirectory = this.importDirectory == null ? Get.configurationService().getIBDFImportPath().toFile() : this.importDirectory;

                System.out.println("Importing from: " + importDirectory.getAbsolutePath());

                int fileCount = loadDatabase(importDirectory, time);

                if (fileCount == 0) {
                    System.out.println("Import from: " + importDirectory.getAbsolutePath() + " failed.");

                    File fallbackDirectory = new File("/Users/kec/isaac/import");

                    if (fallbackDirectory.exists()) {
                        System.out.println("Fallback import from: " + fallbackDirectory.getAbsolutePath());
                        updateTitle("Importing from " + fallbackDirectory.getAbsolutePath());
                        loadDatabase(fallbackDirectory, time);
                    }
                }
            }

            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks()
                    .remove(this);
        }
    }

    @Override
    protected void running() {
        super.running();
    }

    /**
     * Load database.
     *
     * @param contentDirectory the zip file
     * @throws Exception the exception
     */
    private int loadDatabase(File contentDirectory, long time)
            throws Exception {
        List<Path> zipFiles = Files.walk(contentDirectory.toPath())
                .filter(
                        p -> p.toString().toLowerCase().endsWith(".zip")
                        && (p.toString().toUpperCase().contains("SNOMEDCT")
                        || p.toString().toLowerCase().contains("sct")))
                .collect(Collectors.toList());
        ArrayList<ImportSpecification> specificationsToImport = new ArrayList<>();
        StringBuilder importPrefixRegex = new StringBuilder();
        importPrefixRegex.append("([a-z/0-9_]*)?(rf2release/)?"); //ignore parent directories
        switch (importType) {
            case FULL:
                importPrefixRegex.append("(full/)"); //prefixes to match
                break;
            case SNAPSHOT:
            case ACTIVE_ONLY:
                importPrefixRegex.append("(snapshot/)"); //prefixes to match
                break;
        }
        importPrefixRegex.append("[a-z/0-9_\\.\\-]*"); //allow all match child directories
        for (Path zipFilePath : zipFiles) {
            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile(), Charset.forName("UTF-8"))) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.matches(importPrefixRegex.toString())) {
                        processEntry(new ContentProvider(zipFilePath.toFile(), entry), specificationsToImport);
                    }
                }
            }
        }

        return doImport(specificationsToImport, time);
    }

    protected int doImport(ArrayList<ImportSpecification> specificationsToImport, final long time) throws ExecutionException, IOException, UnsupportedOperationException, InterruptedException {
        int fileCount = 0;
        Collections.sort(specificationsToImport);
        StringBuilder builder = new StringBuilder();
        builder.append("Importing the following zip entries: \n");
        for (ImportSpecification spec : specificationsToImport) {
            builder.append("     ").append(spec.streamType);
            builder.append(": ").append(spec.contentProvider.getStreamSourceName()).append("\n");
        }

        HashMap<String, UUID> createdColumnConcepts = new HashMap<>();
        ConcurrentHashMap<Integer, Boolean> configuredDynamicSemantics = new ConcurrentHashMap<>();

        LOG.info(builder.toString());

        addToTotalWork(specificationsToImport.size());

        for (ImportSpecification importSpecification : specificationsToImport) {
            String message = "Importing " + trimZipName(importSpecification.contentProvider.getStreamSourceName());
            updateMessage(message);
            LOG.info("\n\n" + message);

            try (ContentStreamProvider csp = importSpecification.contentProvider.get()) {
                try (BufferedReader br = csp.get()) {
                    fileCount++;

                    switch (importSpecification.streamType) {
                        case ALTERNATIVE_IDENTIFIER:
                            readAlternativeIdentifiers(br, importSpecification);
                            break;

                        case CONCEPT:
                            readConcepts(br, importSpecification);
                            break;

                        case DESCRIPTION:
                            readDescriptions(br, importSpecification);
                            break;

                        case DIALECT:
                            readDialect(br, importSpecification);
                            break;

                        case INFERRED_RELATIONSHIP:
                            readInferredRelationships(br, importSpecification);
                            break;

                        case STATED_RELATIONSHIP:
                            readStatedRelationships(br, importSpecification);
                            break;

                        case INT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET:
                            readINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(br, importSpecification);
                            break;
                        case INT1_REFSET:
                            readINT1_REFSET(br, importSpecification);
                            break;
                        case MEMBER_REFSET:
                            readMEMBER_REFSET(br, importSpecification);
                            break;
                        case NID1_INT2_REFSET:
                            readNID1_INT2_REFSET(br, importSpecification);
                            break;
                        case NID1_INT2_STR3_STR4_NID5_NID6_REFSET:
                            readNID1_INT2_STR3_STR4_NID5_NID6_REFSET(br, importSpecification);
                            break;
                        case NID1_NID2_INT3_REFSET:
                            readNID1_NID2_INT3_REFSET(br, importSpecification);
                            break;
                        case NID1_NID2_REFSET:
                            readNID1_NID2_REFSET(br, importSpecification);
                            break;
                        case NID1_NID2_STR3_REFSET:
                            readNID1_NID2_STR3_REFSET(br, importSpecification);
                            break;
                        case NID1_REFSET:
                            readNID1_REFSET(br, importSpecification);
                            break;
                        case NID1_STR2_REFSET:
                            readNID1_STR2_REFSET(br, importSpecification);
                            break;
                        case STR1_REFSET:
                            readSTR1_REFSET(br, importSpecification);
                            break;
                        case STR1_STR2_NID3_NID4_REFSET:
                            readSTR1_STR2_NID3_NID4_REFSET(br, importSpecification);
                            break;
                        case STR1_STR2_REFSET:
                            readSTR1_STR2_REFSET(br, importSpecification);
                            break;
                        case STR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET:
                            readSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(br, importSpecification);
                            break;
                        case STR1_NID2_NID3_NID4_REFSET:
                            readSTR1_NID2_NID3_NID4_REFSET(br, importSpecification);
                            break;
                        case STR1_STR2_NID3_NID4_NID5_REFSET:
                            readSTR1_STR2_NID3_NID4_NID5_REFSET(br, importSpecification);
                            break;
                        case DYNAMIC:
                            read_DYNAMIC_REFSET(br, importSpecification, createdColumnConcepts, configuredDynamicSemantics);
                            break;

                        case RXNORM_CONSO:
                            readRXNORM_CONSO(br, importSpecification);
                            break;

                        case LOINC:
                            readLOINC(br, importSpecification);
                            break;

                        default:
                            throw new UnsupportedOperationException("Can't handle: " + importSpecification.streamType);
                    }
                }
            } catch (Exception e) {
                LOG.error("Unexpected error", e);
            }
            completedUnitOfWork();
        }

        LOG.info("Loaded " + fileCount + " files in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");
        return fileCount;
    }

    protected void processEntry(ContentProvider contentProvider, ArrayList<ImportSpecification> entriesToImport1) {
        String entryName = contentProvider.getStreamSourceName().toLowerCase();
        if (entryName.contains("sct2_concept_") || (entryName.contains("solor_concept"))) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.CONCEPT));
        } else if ((entryName.contains("sct2_description_") || entryName.contains("sct2_textdefinition_"))
                || (entryName.contains("solor_description") || entryName.contains("solor_textdefinition"))) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.DESCRIPTION));
        } else if (entryName.contains("der2_crefset_") && entryName.contains("language")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.DIALECT));
        } else if (entryName.contains("sct2_identifier_") || entryName.contains("solor_identifier")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.ALTERNATIVE_IDENTIFIER));
        } else if (entryName.contains("sct2_relationship_") || entryName.contains("solor_relationship")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.INFERRED_RELATIONSHIP));
        } else if (entryName.contains("sct2_statedrelationship_") || entryName.contains("solor_statedrelationship")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.STATED_RELATIONSHIP));
        } else if (entryName.contains("refset_") || entryName.contains("assemblage_")) {
            if (importDynamic) {
                entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.DYNAMIC, entryName));
            } else {
                if (entryName.contains("_ccirefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_NID2_INT3_REFSET));
                } else if (entryName.contains("_cirefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_INT2_REFSET));
                } else if (entryName.contains("_cissccrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_INT2_STR3_STR4_NID5_NID6_REFSET));
                } else if (entryName.contains("_crefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_REFSET));
                } else if (entryName.contains("_ssccrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.STR1_STR2_NID3_NID4_REFSET));
                } else if (entryName.contains("_ssrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.STR1_STR2_REFSET));
                } else if (entryName.contains("_sssssssrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.STR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET));
                } else if (entryName.contains("_refset") || entryName.contains("assemblage_ ")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.MEMBER_REFSET));
                } else if (entryName.contains("_iisssccrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.INT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET));
                } else if (entryName.contains("_srefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.STR1_REFSET));
                } else if (entryName.contains("_ccrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_NID2_REFSET));
                } else if (entryName.contains("_ccsrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_NID2_STR3_REFSET));
                } else if (entryName.contains("_csrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.NID1_STR2_REFSET));
                } else if (entryName.contains("_irefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.INT1_REFSET));
                } else if (entryName.contains("_scccrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.STR1_NID2_NID3_NID4_REFSET));
                } else if (entryName.contains("_sscccrefset")) {
                    entriesToImport1.add(new ImportSpecification(
                            contentProvider,
                            ImportStreamType.STR1_STR2_NID3_NID4_NID5_REFSET));
                } else {
                    LOG.info("Ignoring: " + contentProvider.getStreamSourceName());
                }
            }
        } else if (entryName.toUpperCase().endsWith("RXNCONSO.RRF")) {
            entriesToImport1.add(new ImportSpecification(
                    contentProvider,
                    ImportStreamType.RXNORM_CONSO));
        } else if (entryName.toUpperCase().endsWith("LOINC.CSV")) {
            entriesToImport1.add(new ImportSpecification(
                    contentProvider,
                    ImportStreamType.LOINC));
        }

    }

    private void readLOINC(BufferedReader br, ImportSpecification importSpecification) throws IOException, InterruptedException, ExecutionException {
        updateMessage("Transforming LOINC expressions...");
        LoincExpressionToConcept expressionToConceptTask = new LoincExpressionToConcept();
        Get.executor().submit(expressionToConceptTask).get();

        updateMessage("Importing LOINC data...");
        long commitTime = System.currentTimeMillis();
        AssemblageService assemblageService = Get.assemblageService();
        boolean empty = true;

        try (CSVReader reader = new CSVReader(br)) {
            reader.readNext();  // discard header row

            final int writeSize = 102400;
            ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
            String[] columns;
            while ((columns = reader.readNext()) != null) {
                empty = false;
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i] == null) {
                        columns[i] = "null";
                    }
                }

                columnsToWrite.add(columns);

                if (columnsToWrite.size() == writeSize) {
                    LoincWriter loincWriter = new LoincWriter(
                            columnsToWrite,
                            this.writeSemaphore,
                            "Processing LOINC records from: " + DirectImporter.trimZipName(
                                    importSpecification.contentProvider.getStreamSourceName()),
                            commitTime);

                    columnsToWrite = new ArrayList<>(writeSize);
                    Get.executor()
                            .submit(loincWriter);
                }
            }
            if (empty) {
                LOG.warn("No data in file: " + 
                                    importSpecification.contentProvider.getStreamSourceName());
            }

            if (empty) {
                LOG.warn("No data in file: " + 
                                    importSpecification.contentProvider.getStreamSourceName());
            }
            if (!columnsToWrite.isEmpty()) {
                LoincWriter loincWriter = new LoincWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Reading LOINC records from: " + DirectImporter.trimZipName(
                                
                                    importSpecification.contentProvider.getStreamSourceName()), commitTime);

                Get.executor()
                        .submit(loincWriter);
            }

            updateMessage("Waiting for LOINC file completion...");
            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    LOG.error("problem calling sync on index", e);
                }
            }
            updateMessage("Synchronizing LOINC records to database...");
            assemblageService.sync();
            this.writeSemaphore.release(WRITE_PERMITS);
        }


        updateMessage("Synchronizing indexes...");
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing LOINC to database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readRXNORM_CONSO(BufferedReader br,
            ImportSpecification importSpecification) throws IOException {
        updateMessage("Importing RxNorm data...");
        long commitTime = System.currentTimeMillis();
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        // RRF has no header row br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                RxNormWriter rxNormWriter = new RxNormWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing RxNorm records from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()), commitTime);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(rxNormWriter);
            }
        }
        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            RxNormWriter rxNormWriter = new RxNormWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Finishing RxNorm records from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()), commitTime);

            Get.executor()
                    .submit(rxNormWriter);
        }

        updateMessage("Waiting for RxNorm file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);

        updateMessage("Synchronizing indexes...");
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing RxNorm to database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing iissscc semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing iissscc semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);

    }

    protected String[] checkWatchTokensAndSplit(String rowString, ImportSpecification importSpecification) {
        String[] columns;
        if (importSpecification.streamType == ImportStreamType.RXNORM_CONSO) {
            columns = rowString.split("\\|");
        } else {

            columns = rowString.split("\t");
            if (!watchTokens.isEmpty()) {
                int watchCount = 0;
                for (String column : columns) {
                    if (watchTokens.contains(column)) {
                        watchCount++;
                    }

                }
                if (watchCount >= 3) {
                    LOG.info("Found watch tokens in: "
                            + importSpecification.contentProvider.getStreamSourceName()
                            + " \n" + rowString);
                }
            }
        }
        return columns;
    }

    private void readINT1_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing i semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing i semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readMEMBER_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_INT2_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing ci semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ci semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_INT2_STR3_STR4_NID5_NID6_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing cisscc semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cisscc semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_NID2_INT3_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing cci semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cci semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_NID2_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing iissscc semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing iissscc semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_NID2_STR3_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing ccs semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ccs semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing c semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing c semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readNID1_STR2_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing cs semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cs semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readSTR1_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing s semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing s semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readSTR1_STR2_NID3_NID4_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing sscc semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sscc semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readSTR1_STR2_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing ss semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ss semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing sssssss semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sssssss semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readSTR1_NID2_NID3_NID4_REFSET(BufferedReader br,
            ImportSpecification importSpecification) throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing sccc semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sccc semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readSTR1_STR2_NID3_NID4_NID5_REFSET(BufferedReader br,
            ImportSpecification importSpecification) throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing ssccc semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ssccc semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);
            Get.executor()
                    .submit(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void read_DYNAMIC_REFSET(BufferedReader br,
            ImportSpecification importSpecification, HashMap<String, UUID> createdColumnConcepts, ConcurrentHashMap<Integer, Boolean> configuredDynamicSemantics)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        br.readLine();  //skip header row
        //String[] headerRow = checkWatchTokensAndSplit(br.readLine(), importSpecification);

        if (refsetColumnInfo == null) {
            /*
             * If things have been sorted properly, the first time this method is called, it will be with the
             * "Reference set descriptor reference set (foundation metadata concept)" (900000000000456007)
             * refset. We must process this file first, to know how to process the rest of the refsets.
             * 
             * the refset descriptor file shouldn't be too huge, so set a mark on the stream, allowing us to back
             * it up and read it again, when we actually process it into the DB below.
             * 
             * (we can't process it on the fly below, because we need to read it first, to know how to process itself, as it is
             * self describing...)
             */
            
            LOG.info("Reading refset descriptors");
            br.mark(100000);  //this should be big enough, if not, we should fail on reset
            
            /*
             * columns we care about are 6, 7 and 8: attributeDescription attributeType attributeOrder
             * attributeDescription is an SCTID column, which provides the concept to use as the column header concept
             * attributeType is an sctid columns, which provides the datatype of the column
             * 
             * attributeOrder is an integer column, which is 0 indexed starting at the referencedComponent columns (example)
             * 
             * id  effectiveTime  active  moduleId  refsetId  referencedComponentId  attributeDescription  attributeType  attributeOrder
             *                                                0                      1                     2              3
             * 00  01             02      03        04        05                     06                    07             08
             * 
             * The DynamicRefsetWriter already has hard-coded logic to handle columns 00 thru 05, as these are present in every refset.
             * So, we only care about 06 on, which is the {@link DynamicRefsetWriter#VARIABLE_FIELD_START} constant, which will match up
             * with the '1' in the attribute order column....
             */
            if(SRF_IMPORT){
                if (!importSpecification.contentProvider.getStreamSourceName().toLowerCase().contains("assemblage/metadata/assemblage_cci snapshot descriptor")) {
                    throw new RuntimeException("assemblage_cci snapshot descriptor is missing or not sorted to the top of the assemblages!");
                }
            }else {
                if (!importSpecification.contentProvider.getStreamSourceName().toLowerCase().contains("refset/metadata/der2_ccirefset_refsetdescriptor")) {
                    throw new RuntimeException("der2_ccirefset_refsetdescriptor is missing or not sorted to the top of the refsets!");
                }
            }
            
            /*
             * Per the RF2 spec:
             * Creation of Reference set descriptor data is mandatory when creating a new reference set in the International
             * Release or in a National Extension .
             * 
             * TODO need to handle ancestor refset spec lookups....
             * 
             * Creation of a Reference set descriptor is optional when creating a reference set in another Extension. If a descriptor
             * is not created, the descriptor of the closest ancestor of the reference set is used when validating reference set
             * member records.
             */
            
            //Configure a hashmap of refsetId -> ArrayList<DynamicColumnInfo>
            refsetColumnInfo = new HashMap<>();
            String rowString;
            while ((rowString = br.readLine()) != null) {
                String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);
                String refsetId = columns[5].trim();  //we actually want the referencedComponentId, not the refsetId, because this is the refset that is being described.
                int adjustedColumnNumber = Integer.parseInt(columns[8]) - 1;
                UUID columnHeaderConcept = UuidT3Generator.fromSNOMED(columns[6]);
                
                ArrayList<DynamicColumnInfo> refsetColumns = refsetColumnInfo.get(refsetId);
                if (refsetColumns == null) {
                   refsetColumns = new ArrayList<>();
                   refsetColumnInfo.put(refsetId, refsetColumns);
                }
                
                if (adjustedColumnNumber < 0) {
                    continue;  //We don't need this one, as it should always be referencedComponentId when processing the refset descriptor file
                }
                
                //TODO I can't figure out if/where the RF2 spec specifies whether columns can be optional or required.... default to optional for now.
                refsetColumns.add(new DynamicColumnInfo(adjustedColumnNumber, columnHeaderConcept, 
                    DynamicDataType.translateSCTIDMetadata(columns[7]), null, false, true)); 
            }
            //At this point, we should have a hash, of how every single refset should be configured.  
           //sort the column info and sanity check....
           for (Entry<String, ArrayList<DynamicColumnInfo>> dci : refsetColumnInfo.entrySet()) {
              Collections.sort(dci.getValue());
              for (int i = 0; i < dci.getValue().size(); i++) {
                 if (dci.getValue().get(i).getColumnOrder() != i) {
                    throw new RuntimeException("Misconfiguration for refset " + dci.getKey() + " no info for column " + i);
                 }
              }
           }
           br.reset();  //back the stream up, and actually process the refset now.
        }
        
        //Process the refset file
        int dataCount = 0;
        String rowString;
        ArrayList<DynamicRefsetWriter> writers = new ArrayList<>();
        while ((rowString = br.readLine()) != null) {
            dataCount++;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);
            if (dataCount == 1) {
               //Another sanity check - the header-row length beyond column 5 should match the column definitions...
               ArrayList<DynamicColumnInfo> dci = refsetColumnInfo.get(columns[DynamicRefsetWriter.ASSEMBLAGE_SCT_ID_INDEX]);
               if (dci != null && dci.size() != columns.length - DynamicRefsetWriter.VARIABLE_FIELD_START) {
                  throw new RuntimeException("Header information in " + importSpecification.contentProvider.getStreamSourceName() 
                       + " does not match specification from the der2_ccirefset_refsetdescriptor file ");
              }
              //dci being null isn't always fatal, if the refset is an extension that adds to an existing refset, for example.
           }
            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                DynamicRefsetWriter writer = new DynamicRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing dynamic semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType, refsetColumnInfo, configuredDynamicSemantics);
                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(writer);
                writers.add(writer);
            }
        }
        if (dataCount == 0) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            DynamicRefsetWriter writer = new DynamicRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing dynamic semantics from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType, refsetColumnInfo, configuredDynamicSemantics);
            Get.executor()
                    .submit(writer);
            writers.add(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        int skipped = 0;
        for (DynamicRefsetWriter writer : writers) {
            try {
                skipped += writer.get();
            } catch (Exception e) {
                LOG.error("Unexpected failure", e);
            }
        }
        LOG.info("Read {} rows of data, and skipped {}", dataCount, skipped);
        updateMessage("Synchronizing semantic database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readAlternativeIdentifiers(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        String rowString;

        br.readLine();  // discard header row
        LOG.warn("Alternative identifiers not yet supported.");
        while ((rowString = br.readLine()) != null) {
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);
        }
    }

    private void readConcepts(BufferedReader br, ImportSpecification importSpecification)
            throws IOException {
        ConceptService conceptService = Get.conceptService();
        final int writeSize = 102400;
        String rowString;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                ConceptWriter conceptWriter = new ConceptWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing concepts from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()), importType);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(conceptWriter);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            ConceptWriter conceptWriter = new ConceptWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Finishing concepts from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()), importType);

            Get.executor()
                    .submit(conceptWriter);
        }

        updateMessage("Waiting for concept file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing concept database...");
        conceptService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readDescriptions(BufferedReader br, ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                DescriptionWriter descriptionWriter = new DescriptionWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing descriptions from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()), importType);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(descriptionWriter);
            }
        }
        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            DescriptionWriter descriptionWriter = new DescriptionWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Finishing descriptions from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()), importType);

            Get.executor()
                    .submit(descriptionWriter);
        }

        updateMessage("Waiting for description file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);

        updateMessage("Synchronizing indexes...");
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing description database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readDialect(BufferedReader br, ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                DialectWriter dialectWriter = new DialectWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing dialect from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()), importType);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(dialectWriter);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            DialectWriter dialectWriter = new DialectWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Finishing dialect from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()), importType);

            Get.executor()
                    .submit(dialectWriter);
        }

        updateMessage("Waiting for dialect file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing indexes...");
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing dialect database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readInferredRelationships(BufferedReader br,
            ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing inferred rels from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(relWriter);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Finishing inferred rels from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);

            Get.executor()
                    .submit(relWriter);
        }

        updateMessage("Waiting for inferred relationship file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing indexes...");
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing relationship database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void readStatedRelationships(BufferedReader br, ImportSpecification importSpecification)
            throws IOException {
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;

        br.readLine();  // discard header row
        boolean empty = true;
        while ((rowString = br.readLine()) != null) {
            empty = false;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing stated rels from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(relWriter);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.contentProvider.getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {
            Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Finishing stated rels from: " + trimZipName(
                            importSpecification.contentProvider.getStreamSourceName()),
                    importSpecification, importType);

            Get.executor()
                    .submit(relWriter);
        }

        updateMessage("Waiting for stated relationship file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        updateMessage("Synchronizing indexes...");
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        updateMessage("Synchronizing relationship database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    public static String trimZipName(String zipName) {
        int index = zipName.lastIndexOf("/");

        return zipName.substring(index + 1);
    }

    //~--- get methods ---------------------------------------------------------
    public static String getIsoInstant(String basicIsoDate) {
        // From basicIsoDate: '20111203'
        StringBuilder isoInstantBuilder = new StringBuilder();

        // To IsoInstant: '2011-12-03T00:00:00Z'
        isoInstantBuilder.append(basicIsoDate.substring(0, 4));
        isoInstantBuilder.append("-");
        isoInstantBuilder.append(basicIsoDate.substring(4, 6));
        isoInstantBuilder.append("-");
        isoInstantBuilder.append(basicIsoDate.substring(6, 8));
        isoInstantBuilder.append("T00:00:00Z");
        return isoInstantBuilder.toString();
    }

}
