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
import java.io.InputStreamReader;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import static sh.isaac.api.constants.Constants.IMPORT_FOLDER_LOCATION;

//~--- classes ----------------------------------------------------------------
/**
 * Loader code to convert RF2 format fileCount into the ISAAC format.
 */
public class Rf2DirectImporter
        extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

   private static final int WRITE_PERMITS = Runtime.getRuntime()
           .availableProcessors() * 2;
   protected static final Logger LOG = LogManager.getLogger();

   /**
    * The date format parser.
    */
   protected static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyyMMdd");

   //~--- fields --------------------------------------------------------------
   protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);

   //~--- constructors --------------------------------------------------------
   public Rf2DirectImporter() {
      File importDirectory = new File(System.getProperty(IMPORT_FOLDER_LOCATION));

      updateTitle("Importing from " + importDirectory.getAbsolutePath());
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
         File importDirectory = new File(System.getProperty(IMPORT_FOLDER_LOCATION));

         System.out.println("Trying to import from: " + importDirectory.getAbsolutePath());

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
         done();
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
   private int loadDatabase(File contentDirectory)
           throws Exception {
      final long time = System.currentTimeMillis();
      int fileCount = 0;
      List<Path> zipFiles = Files.walk(contentDirectory.toPath())
              .filter(
                      p -> p.toString().endsWith(".zip")
                      && p.toString().toUpperCase().contains("SNOMEDCT"))
              .collect(Collectors.toList());
      ArrayList<ImportSpecification> entriesToImport = new ArrayList<>();

      for (Path zipFilePath : zipFiles) {
         try (ZipFile zipFile = new ZipFile(zipFilePath.toFile(), Charset.forName("UTF-8"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
               ZipEntry entry = entries.nextElement();
               String entryName = entry.getName()
                       .toLowerCase();

               if (entryName.contains("/full/") && !entryName.startsWith("__macosx")) {
                  if (entryName.contains("sct2_concept_")) {
                     entriesToImport.add(
                             new ImportSpecification(zipFilePath.toFile(), ImportStreamType.CONCEPT, entry));
                  } else if (entryName.contains("sct2_description_") || entryName.contains("sct2_textdefinition_")) {
                     entriesToImport.add(
                             new ImportSpecification(zipFilePath.toFile(), ImportStreamType.DESCRIPTION, entry));
                  } else if (entryName.contains("der2_crefset_") && entryName.contains("language")) {
                     entriesToImport.add(
                             new ImportSpecification(zipFilePath.toFile(), ImportStreamType.DIALECT, entry));
                  } else if (entryName.contains("sct2_identifier_")) {
                     entriesToImport.add(
                             new ImportSpecification(zipFilePath.toFile(), ImportStreamType.ALTERNATIVE_IDENTIFIER, entry));
                  } else if (entryName.contains("sct2_relationship_")) {
                     entriesToImport.add(
                             new ImportSpecification(zipFilePath.toFile(), ImportStreamType.INFERRED_RELATIONSHIP, entry));
                  } else if (entryName.contains("sct2_statedrelationship_")) {
                     entriesToImport.add(
                             new ImportSpecification(zipFilePath.toFile(), ImportStreamType.STATED_RELATIONSHIP, entry));
                  } else if (entryName.contains("refset")) {
                     if (entryName.contains("_ccirefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_NID2_INT3_REFSET,
                                        entry));
                     } else if (entryName.contains("_cirefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_INT2_REFSET,
                                        entry));
                     } else if (entryName.contains("_cissccrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_INT2_STR3_STR4_NID5_NID6_REFSET,
                                        entry));
                     } else if (entryName.contains("_crefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_REFSET,
                                        entry));
                     } else if (entryName.contains("_ssccrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.STR1_STR2_NID3_NID4_REFSET,
                                        entry));
                     } else if (entryName.contains("_ssrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.STR1_STR2_REFSET,
                                        entry));
                     } else if (entryName.contains("_sssssssrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.STR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET,
                                        entry));
                     } else if (entryName.contains("_refset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.MEMBER_REFSET,
                                        entry));
                     } else if (entryName.contains("_iisssccrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.INT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET,
                                        entry));
                     } else if (entryName.contains("_srefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.STR1_REFSET,
                                        entry));
                     } else if (entryName.contains("_ccrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_NID2_REFSET,
                                        entry));
                     } else if (entryName.contains("_ccsrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_NID2_STR3_REFSET,
                                        entry));
                     } else if (entryName.contains("_csrefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.NID1_STR2_REFSET,
                                        entry));
                     } else if (entryName.contains("_irefset")) {
                        entriesToImport.add(
                                new ImportSpecification(
                                        zipFilePath.toFile(),
                                        ImportStreamType.INT1_REFSET,
                                        entry));
                     }
                  }
               }
            }
         }
      }

      Collections.sort(entriesToImport);
      addToTotalWork(entriesToImport.size());

      for (ImportSpecification importSpecification : entriesToImport) {
         updateMessage("Importing " + trimZipName(importSpecification.zipEntry.getName()));

         try (ZipFile zipFile = new ZipFile(importSpecification.zipFile, Charset.forName("UTF-8"))) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(zipFile.getInputStream(importSpecification.zipEntry)))) {
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

                  default:
                     throw new UnsupportedOperationException("Can't handle: " + importSpecification.streamType);
               }
            }
         }

         completedUnitOfWork();
      }

      LOG.info("Loaded " + fileCount + " files in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");
      return fileCount;
   }

   private void readINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing iissscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing iissscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);

   }

   private void readINT1_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing i semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing i semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readMEMBER_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readNID1_INT2_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ci semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ci semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readNID1_INT2_STR3_STR4_NID5_NID6_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cisscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cisscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readNID1_NID2_INT3_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cci semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cci semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readNID1_NID2_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing iissscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing iissscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
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
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ccs semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ccs semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
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
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing c semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing c semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readNID1_STR2_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cs semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing cs semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readSTR1_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing s semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing s semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readSTR1_STR2_NID3_NID4_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sscc semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readSTR1_STR2_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ss semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing ss semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      AssemblageService assemblageService = Get.assemblageService();
      final int writeSize = 102400;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
      String rowString;

      br.readLine();  // discard header row
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sssssss semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(writer);
         }
      }

      if (!columnsToWrite.isEmpty()) {
            BrittleRefsetWriter writer = new BrittleRefsetWriter(columnsToWrite, this.writeSemaphore,
                    "Processing sssssss semantics from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    importSpecification.streamType);
            Get.executor()
                    .submit(writer);
      }

      updateMessage("Waiting for refset file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing semantic database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);   }

   private void readAlternativeIdentifiers(BufferedReader br,
           ImportSpecification importSpecification)
           throws IOException {
      String rowString;

      br.readLine();  // discard header row
      LOG.warn("Alternative identifiers not yet supported.");
      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");
      }
   }

   private void readConcepts(BufferedReader br, ImportSpecification importSpecification)
           throws IOException {
      ConceptService conceptService = Get.conceptService();
      final int writeSize = 102400;
      String rowString;
      ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);

      br.readLine();  // discard header row

      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            ConceptWriter conceptWriter = new ConceptWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Processing concepts from: " + trimZipName(
                            importSpecification.zipEntry.getName()));

            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(conceptWriter);
         }
      }

      if (!columnsToWrite.isEmpty()) {
         ConceptWriter conceptWriter = new ConceptWriter(
                 columnsToWrite,
                 this.writeSemaphore,
                 "Finishing concepts from: " + trimZipName(
                         importSpecification.zipEntry.getName()));

         Get.executor()
                 .submit(conceptWriter);
      }

      updateMessage("Waiting for concept file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
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

      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            DescriptionWriter descriptionWriter = new DescriptionWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Processing descriptions from: " + trimZipName(
                            importSpecification.zipEntry.getName()));

            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(descriptionWriter);
         }
      }

      if (!columnsToWrite.isEmpty()) {
         DescriptionWriter descriptionWriter = new DescriptionWriter(
                 columnsToWrite,
                 this.writeSemaphore,
                 "Finishing descriptions from: " + trimZipName(
                         importSpecification.zipEntry.getName()));

         Get.executor()
                 .submit(descriptionWriter);
      }

      updateMessage("Waiting for description file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
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

      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            DialectWriter dialectWriter = new DialectWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Processing dialect from: " + trimZipName(
                            importSpecification.zipEntry.getName()));

            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(dialectWriter);
         }
      }

      if (!columnsToWrite.isEmpty()) {
         DialectWriter descriptionWriter = new DialectWriter(
                 columnsToWrite,
                 this.writeSemaphore,
                 "Finishing dialect from: " + trimZipName(
                         importSpecification.zipEntry.getName()));

         Get.executor()
                 .submit(descriptionWriter);
      }

      updateMessage("Waiting for dialect file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
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

      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Processing inferred rels from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    ImportStreamType.INFERRED_RELATIONSHIP);

            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(relWriter);
         }
      }

      if (!columnsToWrite.isEmpty()) {
         Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                 columnsToWrite,
                 this.writeSemaphore,
                 "Finishing inferred rels from: " + trimZipName(
                         importSpecification.zipEntry.getName()),
                 ImportStreamType.INFERRED_RELATIONSHIP);

         Get.executor()
                 .submit(relWriter);
      }

      updateMessage("Waiting for inferred relationship file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
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

      while ((rowString = br.readLine()) != null) {
         String[] columns = rowString.split("\t");

         columnsToWrite.add(columns);

         if (columnsToWrite.size() == writeSize) {
            Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                    columnsToWrite,
                    this.writeSemaphore,
                    "Processing stated rels from: " + trimZipName(
                            importSpecification.zipEntry.getName()),
                    ImportStreamType.STATED_RELATIONSHIP);

            columnsToWrite = new ArrayList<>(writeSize);
            Get.executor()
                    .submit(relWriter);
         }
      }

      if (!columnsToWrite.isEmpty()) {
         Rf2RelationshipWriter relWriter = new Rf2RelationshipWriter(
                 columnsToWrite,
                 this.writeSemaphore,
                 "Finishing stated rels from: " + trimZipName(
                         importSpecification.zipEntry.getName()),
                 ImportStreamType.STATED_RELATIONSHIP);

         Get.executor()
                 .submit(relWriter);
      }

      updateMessage("Waiting for stated relationship file completion...");
      this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
      updateMessage("Synchronizing relationship database...");
      assemblageService.sync();
      this.writeSemaphore.release(WRITE_PERMITS);
   }

   private static String trimZipName(String zipName) {
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
