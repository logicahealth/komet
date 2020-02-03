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
package sh.isaac.solor.direct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.util.CloseIgnoringInputStream;
import com.opencsv.CSVReader;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.solor.ContentProvider;

/**
 *
 * @author kec
 */
public class LoincDirectImporter extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

    private static final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;

    public static HashSet<String> watchTokens = new HashSet<>();
    
    private boolean foundLoinc = false;
    private ArrayList<ContentProvider> contentProviders = new ArrayList<>();
    // TODO consider replacing readSemaphore with TaskCountManager
    private final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);

    private final Transaction transaction;

    public LoincDirectImporter(Transaction transaction) {
        this.transaction = transaction;
        Path importDirectory = Get.configurationService().getIBDFImportPath();
        updateTitle("Importing LOINC from " + importDirectory.toAbsolutePath());
        
        try {
            Files.walk(importDirectory).filter(p -> 
                (p.toString().toLowerCase().endsWith("_text.zip") && p.toFile().getName().toLowerCase().startsWith("loinc")) 
                  || (p.toFile().getName().toLowerCase().startsWith("loinc-") && p.toString().toLowerCase().endsWith(".zip")))
                    .forEach(f -> contentProviders.add(new ContentProvider(f)));
        }
        catch (IOException e) {
            throw new RuntimeException("Specified IBDF Import folder unreadable: " + e);
        }
        
        LOG.info("Importing LOINC from {}, found {} potential file", importDirectory.toAbsolutePath(), contentProviders.size());
    }
    
    /**
     * For importing from the provided items.
     * @param contentItems
     */
    public LoincDirectImporter(Transaction transaction, ArrayList<ContentProvider> contentItems) {
        this.transaction = transaction;
        updateTitle("Importing LOINC from provided item list");
        LOG.info("Importing LOINC from provided item list");
        this.contentProviders = contentItems;
    }

    @Override
    protected Void call() throws Exception {
        try {
            Get.activeTasks().add(this);

            int fileCount = 0;
            for (ContentProvider cp : contentProviders) {
                fileCount += readContent(cp);
            }

            if (fileCount == 0) {
            	LOG.error("Loinc loader failed to load any files!");
            }

            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks().remove(this);
        }
    }

    /**
     * Load database.
     *
     * @param cp the content provider
     * @throws Exception the exception
     */
    private int readContent(ContentProvider cp) throws Exception {
        int fileCount = 0;

        LOG.trace("Scanning {}", cp.getStreamSourceName());
        if (cp.getStreamSourceName().toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zis = new ZipInputStream(cp.get().get(), Charset.forName("UTF-8"))) {
                ZipEntry nestedEntry = zis.getNextEntry();
                while (nestedEntry != null)  {
                    if (!nestedEntry.isDirectory())  {
                        fileCount += readContent(new ContentProvider(cp.getStreamSourceName() + ":" + nestedEntry.getName(), () ->  {
                            try {
                                return IOUtils.toByteArray(zis);
                            }
                            catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }));
                    }
                    nestedEntry = zis.getNextEntry();
                }
            }
        }
        else if (cp.getStreamSourceName().toLowerCase().endsWith("loinc.csv")) {
            foundLoinc = true;
            LOG.info("Processing {}", cp.getStreamSourceName());
            // Their new format includes the (optional) UTF-8 BOM, which chokes java for stupid legacy reasons.
            try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(
                    new CloseIgnoringInputStream(new BOMInputStream(cp.get().get())), Charset.forName("UTF-8"))))) {
                fileCount++;
                readLoinc(reader, cp.getStreamSourceName());
            }
        }
        else {
            LOG.debug("Ignoring {}", cp.getStreamSourceName());
        }
        return fileCount;
    }

    private void readLoinc(CSVReader reader, String readingFrom)
            throws IOException {
        long commitTime = System.currentTimeMillis();
        AssemblageService assemblageService = Get.assemblageService();
        AxiomsFromLoincRecord loincAxiomMaker = new AxiomsFromLoincRecord();

        final int writeSize = 1024;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String[] columns;

        reader.readNext();  // discard header row
        boolean empty = true;
        while ((columns = reader.readNext()) != null) {
            empty = false;
            for (int i = 0; i < columns.length; i++) {
                if (columns[i] == null) {
                    columns[i] = "null";
                }
            }

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                LoincWriter loincWriter = new LoincWriter(transaction,
                        columnsToWrite, loincAxiomMaker,
                        this.writeSemaphore,
                        "Processing LOINC records from: " + DirectImporter.trimZipName(
                                readingFrom),
                        commitTime);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor().submit(loincWriter);
            }
        }

        if (empty) {
            LOG.warn("No data in file: " + readingFrom);
        }
        if (!columnsToWrite.isEmpty()) {
            LoincWriter loincWriter = new LoincWriter(transaction,
                    columnsToWrite, loincAxiomMaker,
                    this.writeSemaphore,
                    "Reading LOINC records from: " + DirectImporter.trimZipName(
                            readingFrom), commitTime);

            Get.executor().submit(loincWriter);
        }

        updateMessage("Waiting for LOINC file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        loincAxiomMaker.listMethods();

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

    protected String[] checkWatchTokensAndSplit(String rowString, ZipEntry entry) {
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
                        + entry.getName() + " entry: " + entry.getName()
                        + " \n" + rowString);
            }
        }
        for (int i = 0; i < columns.length; i++) {
            // for LOINC files. 
            if (columns[i].charAt(0) == '"') {
                columns[i] = columns[i].substring(1, columns[i].length() - 1);
            }
        }
        return columns;
    }

    public boolean foundLoinc() {
        return foundLoinc;
    }

}
