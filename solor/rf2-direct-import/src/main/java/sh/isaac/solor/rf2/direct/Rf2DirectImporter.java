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
package sh.isaac.solor.rf2.direct;

//~--- JDK imports ------------------------------------------------------------
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.ContentProvider;
import sh.isaac.solor.ContentStreamProvider;

//~--- classes ----------------------------------------------------------------
/**
 * Loader code to convert RF2 format fileCount into the ISAAC format.
 */
public class Rf2DirectImporter
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

    //~--- constructors --------------------------------------------------------
    public Rf2DirectImporter(ImportType importType) {
        this.importType = importType;
        this.entriesToImport = null;
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
//        watchTokens.add("89587004"); // Removal of foreign body from abdominal cavity (procedure)
//        watchTokens.add("84971000000100"); // PBCL flag true (attribute)
//        watchTokens.add("123101000000107"); // PBCL flag true: report, request, level, test (qualifier value)

        updateTitle("Importing from RF2 from" + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
    }

    public Rf2DirectImporter(ImportType importType, List<ContentProvider> entriesToImport) {
        this.importType = importType;
        this.entriesToImport = entriesToImport;
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
//        watchTokens.add("89587004"); // Removal of foreign body from abdominal cavity (procedure)
//        watchTokens.add("84971000000100"); // PBCL flag true (attribute)
//        watchTokens.add("123101000000107"); // PBCL flag true: report, request, level, test (qualifier value)

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
                for (ContentProvider entry: this.entriesToImport) {
                    processEntry(entry, specificationsToImport);
                }
                doImport(specificationsToImport, time);
            } else {
                File importDirectory = Get.configurationService().getIBDFImportPath().toFile();

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
        ConcurrentHashMap<Integer,Boolean> configuredDynamicSemantics = new ConcurrentHashMap<>();

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

                        default:
                            throw new UnsupportedOperationException("Can't handle: " + importSpecification.streamType);
                    }
                }
            }
         catch (Exception e) {
            LOG.error("Unexpected error",e );
        }
            completedUnitOfWork();
        }

        updateMessage("Transforming LOINC expressions...");
        LoincExpressionToConcept expressionToConceptTask = new LoincExpressionToConcept();
        Get.executor().submit(expressionToConceptTask).get();

        updateMessage("Importing LOINC records...");
        LoincDirectImporter importTask = new LoincDirectImporter();
        Get.executor().submit(importTask).get();

        LOG.info("Loaded " + fileCount + " files in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");
        return fileCount;
    }

    protected void processEntry(ContentProvider contentProvider, ArrayList<ImportSpecification> entriesToImport1) {
        String entryName = contentProvider.getStreamSourceName().toLowerCase();
        if (entryName.contains("sct2_concept_")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.CONCEPT));
        } else if (entryName.contains("sct2_description_") || entryName.contains("sct2_textdefinition_")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.DESCRIPTION));
        } else if (entryName.contains("der2_crefset_") && entryName.contains("language")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.DIALECT));
        } else if (entryName.contains("sct2_identifier_")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.ALTERNATIVE_IDENTIFIER));
        } else if (entryName.contains("sct2_relationship_")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.INFERRED_RELATIONSHIP));
        } else if (entryName.contains("sct2_statedrelationship_")) {
            entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.STATED_RELATIONSHIP));
        } else if (entryName.contains("refset")) {
            if (importDynamic) {
                entriesToImport1.add(new ImportSpecification(contentProvider, ImportStreamType.DYNAMIC, entryName));
            }
            else {
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
                } else if (entryName.contains("_refset")) {
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
        }

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
        String[] columns = rowString.split("\t");
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
        String[] headerRow = checkWatchTokensAndSplit(br.readLine(), importSpecification);
                
        //Create concepts for each of the data columns
        UUID[] columnDataConcepts = new UUID[importSpecification.refsetBrittleTypes.length];
        EditCoordinate editCoord = Get.configurationService().getGlobalDatastoreConfiguration().getDefaultEditCoordinate();
        boolean createdMetadata = false;
        for (int i = 0; i < columnDataConcepts.length; i++)
        {
            String name = headerRow[DynamicRefsetWriter.VARIABLE_FIELD_START + i];
            
            //Some refsets share the same column names, so don't create duplicate column concepts.
            if (createdColumnConcepts.get(name) == null)
            {
               //TODO need to read real metadata to get the description... the edit coord on this should probably be the same as the one on the metadata.
               ArrayList<Chronology> builtConceptParts = Get.service(DynamicUtility.class)
                       .buildUncommittedNewDynamicSemanticColumnInfoConcept(name, null, editCoord, null);
               createdMetadata = true;
               createdColumnConcepts.put(name, builtConceptParts.get(0).getPrimordialUuid());
            }
            columnDataConcepts[i] = createdColumnConcepts.get(name);
        }
        
        if (createdMetadata)
        {
            //Do a global commit to commit the metadata concepts created here, and just above
            Get.commitService().commit(editCoord, "metadata commit for refset " + trimZipName(importSpecification.contentProvider.getStreamSourceName()));
        }
        
        //Define the column information for the refset(s) specified in this file (yes, their may be more than one, strangely)
        DynamicColumnInfo[] dynamicColumns = new DynamicColumnInfo[importSpecification.refsetBrittleTypes.length];
        for (int i = 0; i < importSpecification.refsetBrittleTypes.length; i++) {
            //TODO is there column required / optional info in the RF2 spec?
            dynamicColumns[i] = new DynamicColumnInfo(i, columnDataConcepts[i], 
                    importSpecification.refsetBrittleTypes[i].getDynamicColumnType(), null, false, true);
        }
        
        int dataCount = 0;
        String rowString;
        ArrayList<DynamicRefsetWriter> writers = new ArrayList<>();
        while ((rowString = br.readLine()) != null) {
            dataCount++;
            String[] columns = checkWatchTokensAndSplit(rowString, importSpecification);
            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                DynamicRefsetWriter writer = new DynamicRefsetWriter(columnsToWrite, this.writeSemaphore,
                        "Processing dynamic semantics from: " + trimZipName(
                                importSpecification.contentProvider.getStreamSourceName()),
                        importSpecification, importType, dynamicColumns, configuredDynamicSemantics);
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
                    importSpecification, importType, dynamicColumns, configuredDynamicSemantics);
            Get.executor()
                    .submit(writer);
            writers.add(writer);
        }

        updateMessage("Waiting for refset file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        int skipped = 0;
        for (DynamicRefsetWriter writer: writers) {
            try {
                skipped += writer.get();
            }
            catch (Exception e) {
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
