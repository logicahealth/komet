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



package sh.isaac.provider.ibdf.diff;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.hk2.annotations.Service;

import com.cedarsoftware.util.io.JsonWriter;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.externalizable.BinaryDataDifferService;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.externalizable.json.JsonDataWriterService;

//~--- classes ----------------------------------------------------------------

/**
 * Routines enabling the examination of two ibdf files containing two distinct
 * versions of the same terminology and identifies the new/inactivated/modified
 * content between the two versions.
 *
 * Once identified, a new changeset file may be generated containing these
 * changes. This file can then be imported into an existing database containing
 * the old version of the terminology. This will upgrade it to the new
 * terminology.
 *
 * {@link BinaryDataDifferService}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service(name = "binary data differ")
@Singleton

//TODO there are some serious thread-safety issues in this class
public class BinaryDataDifferProvider
         implements BinaryDataDifferService {
   private final Logger log                = LogManager.getLogger();
   private final String textInputFileName  = "bothVersions.txt";
   private final String jsonOutputFileName = "allChangedComponents.json";
   private final String textOutputFileName = "allChangedComponents.txt";

   // Changeset File Writer
   private DataWriterService               componentCSWriter = null;
   HashSet<Integer>                        skippedItems      = new HashSet<>();
   private BinaryDataDifferProviderUtility diffUtil;

   // Stream hack
   private int conceptCount, sememeCount, itemCount;

   // Analysis File Readers/Writers
   private String analysisFilesOutputDir;
   private String ibdfFileOutputDir;
   private String changesetFileName;

   //~--- constructors --------------------------------------------------------

   public BinaryDataDifferProvider() {
      // For HK2
      log.info("binary data differ constructed");
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void generateDiffedIbdfFile(Map<ChangeType, List<OchreExternalizable>> changedComponents)
            throws IOException {
      componentCSWriter = Get.binaryDataWriter(new File(ibdfFileOutputDir + changesetFileName).toPath());

      for (ChangeType key: changedComponents.keySet()) {
         for (OchreExternalizable c: changedComponents.get(key)) {
            componentCSWriter.put(c);
         }
      }

      componentCSWriter.close();
   }

   @Override
   public Map<ChangeType, List<OchreExternalizable>> identifyVersionChanges(Map<OchreExternalizableObjectType,
              Set<OchreExternalizable>> oldContentMap,
         Map<OchreExternalizableObjectType, Set<OchreExternalizable>> newContentMap) {
      List<OchreExternalizable> addedComponents   = new ArrayList<>();
      List<OchreExternalizable> retiredComponents = new ArrayList<>();
      List<OchreExternalizable> changedComponents = new ArrayList<>();
      CommitService             commitService     = Get.commitService();
      final int                 activeStampSeq    = createStamp(State.ACTIVE);
      final int                 inactiveStampSeq  = createStamp(State.INACTIVE);

      // Find existing
      for (OchreExternalizableObjectType type: OchreExternalizableObjectType.values()) {
         Set<UUID> matchedSet = new HashSet<UUID>();

         if ((type != OchreExternalizableObjectType.CONCEPT) && (type != OchreExternalizableObjectType.SEMEME)) {
            // Given using the OchreExternalizableObjectType.values()
            // collection, ensure only handling the supported types
            continue;
         }

         // Search for modified components
         for (OchreExternalizable oldComp: oldContentMap.get(type)) {
            for (OchreExternalizable newComp: newContentMap.get(type)) {
               ObjectChronology<?> oldCompChron = (ObjectChronology<?>) oldComp;
               ObjectChronology<?> newCompChron = (ObjectChronology<?>) newComp;

               if (oldCompChron.getPrimordialUuid()
                               .equals(newCompChron.getPrimordialUuid())) {
                  matchedSet.add(oldCompChron.getPrimordialUuid());

                  try {
                     OchreExternalizable modifiedComponents = diffUtil.diff(oldCompChron,
                                                                            newCompChron,
                                                                            activeStampSeq,
                                                                            type);

                     if (modifiedComponents != null) {
                        if (type == OchreExternalizableObjectType.CONCEPT) {
                           throw new Exception("Cannot modify Concept in current Object Model");
                        }

                        changedComponents.add(modifiedComponents);
                     }
                  } catch (Exception e) {
                     log.error("Failed ON type: " + type + " on component: " + oldCompChron.getPrimordialUuid());
                     e.printStackTrace();
                  }

                  continue;
               }
            }
         }

         // Add newCons not in newList
         for (OchreExternalizable oldComp: oldContentMap.get(type)) {
            if (!matchedSet.contains(((ObjectChronology<?>) oldComp).getPrimordialUuid())) {
               OchreExternalizable retiredComp = diffUtil.addNewInactiveVersion(oldComp,
                                                                                oldComp.getOchreObjectType(),
                                                                                inactiveStampSeq);

               if (retiredComp != null) {
                  retiredComponents.add(retiredComp);
               }
            }
         }

         // Add newCons not in newList
         for (OchreExternalizable newComp: newContentMap.get(type)) {
            if (!matchedSet.contains(((ObjectChronology<?>) newComp).getPrimordialUuid())) {
               OchreExternalizable addedComp = diffUtil.diff(null, (ObjectChronology<?>) newComp, activeStampSeq, type);

               if (addedComp != null) {
                  addedComponents.add(addedComp);
                  commitService.importNoChecks(addedComp);
               }
            }
         }

         commitService.postProcessImportNoChecks();
      }  // Close Type Loop

      Map<ChangeType, List<OchreExternalizable>> retMap = new HashMap<>();

      retMap.put(ChangeType.NEW_COMPONENTS, addedComponents);
      retMap.put(ChangeType.RETIRED_COMPONENTS, retiredComponents);
      retMap.put(ChangeType.MODIFIED_COMPONENTS, changedComponents);
      return retMap;
   }

   @Override
   public void initialize(String analysisFilesOutputDir,
                          String ibdfFileOutputDir,
                          String changesetFileName,
                          Boolean createAnalysisFiles,
                          boolean diffOnStatus,
                          boolean diffOnTimestamp,
                          boolean diffOnAuthor,
                          boolean diffOnModule,
                          boolean diffOnPath,
                          String importDate) {
      diffUtil = new BinaryDataDifferProviderUtility(diffOnStatus,
            diffOnTimestamp,
            diffOnAuthor,
            diffOnModule,
            diffOnPath);
      diffUtil.setNewImportDate(importDate);
      this.analysisFilesOutputDir = analysisFilesOutputDir;
      this.ibdfFileOutputDir      = ibdfFileOutputDir;
      this.changesetFileName      = changesetFileName;

      if (createAnalysisFiles) {
         File f = new File(analysisFilesOutputDir + "/output");

         f.mkdirs();
         f = new File(analysisFilesOutputDir + "/input");
         f.mkdirs();
      }

      File f = new File(ibdfFileOutputDir);

      f.mkdirs();
   }

   @Override
   public Map<OchreExternalizableObjectType, Set<OchreExternalizable>> processVersion(File versionFile)
            throws Exception {
      BinaryDataReaderService reader = Get.binaryDataReader(versionFile.toPath());

      itemCount    = 0;
      conceptCount = 0;
      sememeCount  = 0;

      Map<OchreExternalizableObjectType, Set<OchreExternalizable>> retMap = new HashMap<>();

      retMap.put(OchreExternalizableObjectType.CONCEPT, new HashSet<OchreExternalizable>());
      retMap.put(OchreExternalizableObjectType.SEMEME, new HashSet<OchreExternalizable>());

      try {
         reader.getStream().forEach((object) -> {
                           if (object != null) {
                              itemCount++;

                              try {
                                 if (object.getOchreObjectType() == OchreExternalizableObjectType.CONCEPT) {
                                    conceptCount++;
                                    retMap.get(object.getOchreObjectType())
                                          .add(object);
                                 } else if (object.getOchreObjectType() == OchreExternalizableObjectType.SEMEME) {
                                    sememeCount++;
                                    retMap.get(object.getOchreObjectType())
                                          .add(object);
                                 } else if (object.getOchreObjectType() == OchreExternalizableObjectType.STAMP_ALIAS) {
                                    throw new RuntimeException("Not setup to handle STAMP ALIASES yet");
                                 } else if (object.getOchreObjectType() ==
                                            OchreExternalizableObjectType.STAMP_COMMENT) {
                                    throw new RuntimeException("Not setup to handle STAMP COMMENTS yet");
                                 } else {
                                    throw new UnsupportedOperationException("Unknown ochre object type: " + object);
                                 }
                              } catch (Exception e) {
                                 log.error("Failure at " + conceptCount + " concepts, " + sememeCount + " sememes, ",
                                           e);

                                 Map<String, Object> args = new HashMap<>();

                                 args.put(JsonWriter.PRETTY_PRINT, true);

                                 ByteArrayOutputStream baos       = new ByteArrayOutputStream();
                                 JsonWriter            json       = new JsonWriter(baos, args);
                                 UUID                  primordial = null;

                                 if (object instanceof ObjectChronology) {
                                    primordial = ((ObjectChronology<?>) object).getPrimordialUuid();
                                 }

                                 json.write(object);
                                 log.error("Failed on " + ((primordial == null) ? ": "
                        : "object with primoridial UUID " + primordial.toString() + ": ") + baos.toString());
                                 json.close();
                              }

                              if (itemCount % 100 == 0) {
                                 log.info("Read " + itemCount + " entries, " + "Loaded " + conceptCount +
                                          " concepts, " + sememeCount + " sememes, ");
                              }
                           }
                        });
      } catch (Exception ex) {
         log.info("Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " +
                  ((skippedItems.size() > 0) ? ", skipped for inactive " + skippedItems.size()
               : ""));
         throw new Exception(ex.getLocalizedMessage(), ex);
      }

      log.info("Processed " + itemCount + " components for Diff Analysis");
      return retMap;
   }

   @Override
   public void writeFilesForAnalysis(Map<OchreExternalizableObjectType, Set<OchreExternalizable>> oldContentMap,
                                     Map<OchreExternalizableObjectType, Set<OchreExternalizable>> newContentMap,
                                     Map<ChangeType, List<OchreExternalizable>> changedComponents,
                                     String ibdfFileOutputDir,
                                     String analysisFilesOutputDir) {
      try {
         if (oldContentMap != null) {
            writeInputFilesForAnalysis(oldContentMap, "OLD", "oldVersion.json");
         } else {
            log.info("oldContentMap empty so not writing json/text Input files for old content");
         }

         if (newContentMap != null) {
            writeInputFilesForAnalysis(newContentMap, "New", "newVersion.json");
         } else {
            log.info("newContentMap empty so not writing json/text Input files for new content");
         }

         if (changedComponents != null) {
            writeChangeSetForAnalysis(changedComponents);
         } else {
            log.info("changedComponents empty so not writing json/text Output files");
         }

         writeChangeSetForVerification();
      } catch (IOException e) {
         log.error("Failed in creating analysis files (not in processing the content written to the analysis files)");
      }
   }

   /**
    * Set up all the boilerplate stuff.
    *
    * Create a stamp in current database... create seq... then when
    * serializing, point it
    *
    * @param state
    *            - state or null (for current)
    * @param time
    *            - time or null (for default)
    */
   private int createStamp(State state) {
      return LookupService.getService(StampService.class)
                          .getStampSequence(state,
                                diffUtil.getNewImportDate(),
                                LookupService.getService(IdentifierService.class)
                                      .getConceptSequenceForUuids(
                                      UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")),  // USER
                                LookupService.getService(IdentifierService.class)
                                      .getConceptSequenceForUuids(
                                      UUID.fromString("8aa5fda8-33e9-5eaf-88e8-dd8a024d2489")),  // VHA

      // MODULE
      LookupService.getService(IdentifierService.class)
                   .getConceptSequenceForUuids(UUID.fromString("1f200ca6-960e-11e5-8994-feff819cdc9f")));  // DEV

      // PATH
   }

   @PostConstruct
   private void startMe() {
      log.info("Starting BinaryDataDifferProvider.");
   }

   @PreDestroy
   private void stopMe() {
      log.info("Stopping BinaryDataDifferProvider.");
   }

   private void writeChangeSetForAnalysis(Map<ChangeType, List<OchreExternalizable>> changedComponents)
            throws IOException {
      int counter = 1;

      try (FileWriter textOutputWriter = new FileWriter(analysisFilesOutputDir + "output/" + textOutputFileName);
         JsonDataWriterService jsonOutputWriter = new JsonDataWriterService(new File(analysisFilesOutputDir +
               "output/" + jsonOutputFileName));) {
         for (ChangeType key: changedComponents.keySet()) {
            FileWriter changeWriter = new FileWriter(analysisFilesOutputDir + "output/" + key + "File.txt");

            try {
               List<OchreExternalizable> components = changedComponents.get(key);

               jsonOutputWriter.put("# **** Modification: " + key.toString() + " ****");
               textOutputWriter.write("\n\n\n\t\t\t**** Modification: " + key.toString() + " ****");

               for (OchreExternalizable c: components) {
                  String componentType;

                  if (c.getOchreObjectType() == OchreExternalizableObjectType.CONCEPT) {
                     componentType = "Concept";
                  } else {
                     componentType = "Sememe";
                  }

                  String componentToWrite = "# ---- " + key.toString() + " " + componentType + " #" + counter++ +
                                            "   " + ((ObjectChronology<?>) c).getPrimordialUuid() + " ----";

                  jsonOutputWriter.put(componentToWrite);
                  textOutputWriter.write("\n\n\t\t\t" + componentToWrite);
                  jsonOutputWriter.put(c);
                  changeWriter.write(c.toString());
                  changeWriter.write("\n\n\n");

                  try {
                     String s = c.toString();

                     textOutputWriter.write(s);
                  } catch (Exception e) {}
               }
            } catch (IOException e) {
               log.error("Failure processing changes of type " + key.toString());
            } finally {
               changeWriter.close();
            }
         }
      }
   }

   private void writeChangeSetForVerification()
            throws FileNotFoundException {
      int ic = 0;
      int cc = 0;
      int sc = 0;
      BinaryDataReaderQueueService reader = Get.binaryDataQueueReader(new File(ibdfFileOutputDir +
                                               changesetFileName).toPath());
      BlockingQueue<OchreExternalizable> queue = reader.getQueue();
      Map<String, Object>                args  = new HashMap<>();

      args.put(JsonWriter.PRETTY_PRINT, true);

      try (FileOutputStream fos = new FileOutputStream(new File(analysisFilesOutputDir + "verificationChanges.json"));
         JsonWriter verificationWriter = new JsonWriter(fos, args);) {
         while (!queue.isEmpty() ||!reader.isFinished()) {
            OchreExternalizable object = queue.poll(500, TimeUnit.MILLISECONDS);

            if (object != null) {
               ic++;

               try {
                  if (object.getOchreObjectType() == OchreExternalizableObjectType.CONCEPT) {
                     cc++;
                     verificationWriter.write(object);
                  } else if (object.getOchreObjectType() == OchreExternalizableObjectType.SEMEME) {
                     sc++;
                     verificationWriter.write(object);
                  } else {
                     throw new UnsupportedOperationException("Unknown ochre object type: " + object);
                  }
               } catch (Exception e) {
                  log.error("Failure at " + ic + " items " + cc + " concepts, " + sc + " sememes, ", e);
                  args = new HashMap<>();
                  args.put(JsonWriter.PRETTY_PRINT, true);

                  ByteArrayOutputStream baos       = new ByteArrayOutputStream();
                  JsonWriter            json       = new JsonWriter(baos, args);
                  UUID                  primordial = null;

                  if (object instanceof ObjectChronology) {
                     primordial = ((ObjectChronology<?>) object).getPrimordialUuid();
                  }

                  json.write(object);
                  log.error("Failed on " + ((primordial == null) ? ": "
                        : "object with primoridial UUID " + primordial.toString() + ": ") + baos.toString());
                  json.close();
               }

               if (ic % 100 == 0) {
                  log.info("Read " + ic + " entries, " + "Loaded " + cc + " concepts, " + sc + " sememes, ");
               }
            }
         }
      } catch (Exception ex) {
         log.info("Loaded " + ic + " items, " + cc + " concepts, " + sc + " sememes, " +
                  ((skippedItems.size() > 0) ? ", skipped for inactive " + skippedItems.size()
               : ""));
      }

      log.info("Finished with " + ic + " items, " + cc + " concepts, " + sc + " sememes, " +
               ((skippedItems.size() > 0) ? ", skipped for inactive " + skippedItems.size()
            : ""));
   }

   private void writeInputFilesForAnalysis(Map<OchreExternalizableObjectType, Set<OchreExternalizable>> contentMap,
         String version,
         String jsonInputFileName)
            throws IOException {
      try (FileWriter textInputWriter = new FileWriter(analysisFilesOutputDir + "input/" + textInputFileName, true);
         JsonDataWriterService jsonInputWriter = new JsonDataWriterService(new File(analysisFilesOutputDir + "input/" +
               jsonInputFileName));) {
         textInputWriter.write("\n\n\n\n\n\n\t\t\t**** " + version + " LIST ****");
         jsonInputWriter.put("# **** " + version + " LIST ****");

         int i = 1;

         for (OchreExternalizable component: contentMap.get(OchreExternalizableObjectType.CONCEPT)) {
            ConceptChronology<?> cc = (ConceptChronology<?>) component;

            jsonInputWriter.put("#---- " + version + " Concept #" + i + "   " + cc.getPrimordialUuid() + " ----");
            jsonInputWriter.put(cc);
            textInputWriter.write("\n\n\t\t\t---- " + version + " Concept #" + i + "   " + cc.getPrimordialUuid() +
                                  " ----");
            textInputWriter.write(cc.toString());
            i++;
         }

         i = 1;

         for (OchreExternalizable component: contentMap.get(OchreExternalizableObjectType.SEMEME)) {
            SememeChronology<?> se = (SememeChronology<?>) component;

            jsonInputWriter.put(se);
            jsonInputWriter.put("# --- " + version + " Sememe #" + i + "   " + se.getPrimordialUuid() + " ----");
            textInputWriter.write("\n\n\t\t\t---- " + version + " Sememe #" + i + "   " + se.getPrimordialUuid() +
                                  " ----");
            textInputWriter.write(se.toString());
            i++;
         }
      } catch (Exception e) {
         log.error("Failure in writing *" + version + "* content to text file for analysis.");
      }
   }
}

