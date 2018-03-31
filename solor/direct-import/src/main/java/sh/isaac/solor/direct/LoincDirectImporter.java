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

import com.opencsv.CSVReader;
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
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.index.IndexBuilderService;

import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 *
 * @author kec
 */
public class LoincDirectImporter extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

    private static final int WRITE_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;

    public static HashSet<String> watchTokens = new HashSet<>();

    //~--- fields --------------------------------------------------------------
    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);

    public LoincDirectImporter() {
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
        updateTitle("Importing LOINC from " + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
    }

    @Override
    protected Void call() throws Exception {
        try {
            File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
            System.out.println("Importing from: " + importDirectory.getAbsolutePath());

            int fileCount = loadDatabase(importDirectory);

            if (fileCount == 0) {
                System.out.println("Import from: " + importDirectory.getAbsolutePath() + " failed.");

                File fallbackDirectory = new File("/Users/kec/isaac/import");

                if (fallbackDirectory.exists()) {
                    System.out.println("Fallback import from: " + fallbackDirectory.getAbsolutePath());
                    updateTitle("Importing from " + fallbackDirectory.getAbsolutePath());
                    loadDatabase(fallbackDirectory);
                }
            }

            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks()
                    .remove(this);
        }
    }

    /**
     * Load database.
     *
     * @param contentDirectory the zip file
     * @throws Exception the exception
     */
    private int loadDatabase(File contentDirectory)
            throws Exception {
        final long time = System.currentTimeMillis();
        int fileCount = 0;
        List<Path> zipFiles = Files.walk(contentDirectory.toPath())
                .filter(p -> (p.toString().toLowerCase().endsWith("_text.zip")
                && p.toFile().getName().toLowerCase().startsWith("loinc")) ||
                (p.toFile().getName().toLowerCase().startsWith("loinc-")
                && p.toString().toLowerCase().endsWith(".zip")))
                .collect(Collectors.toList());
        for (Path zipFilePath : zipFiles) {
            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile(), Charset.forName("UTF-8"))) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName()
                            .toLowerCase();
                    if (entryName.endsWith("loinc.csv")) {
                        try (CSVReader reader
                                = new CSVReader(new BufferedReader(
                                        new InputStreamReader(zipFile.getInputStream(entry),
                                                Charset.forName("UTF-8"))))) {
                            fileCount++;
                            readLoinc(reader, entry);
                        }
                    }
                }
            }
        }
        return fileCount;
    }

    private void readLoinc(CSVReader reader, ZipEntry entry)
            throws IOException {
        long commitTime = System.currentTimeMillis();
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
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
                LoincWriter loincWriter = new LoincWriter(
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing LOINC records from: " + Rf2DirectImporter.trimZipName(
                                entry.getName()),
                        commitTime);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(loincWriter);
            }
        }
        if (empty) {
            LOG.warn("No data in file: " + entry.getName());
        }

        if (empty) {
            LOG.warn("No data in file: " + entry.getName());
        }
        if (!columnsToWrite.isEmpty()) {
            LoincWriter loincWriter = new LoincWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Reading LOINC records from: " + Rf2DirectImporter.trimZipName(
                            entry.getName()), commitTime);

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

}
