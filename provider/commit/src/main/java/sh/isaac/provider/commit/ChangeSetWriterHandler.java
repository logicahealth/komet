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



package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.commit.ChangeSetListener;
import sh.isaac.api.commit.ChangeSetWriterService;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.MultipleDataWriterService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.util.NamedThreadFactory;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ChangeSetWriterHandler}
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
@Service(name = "Change Set Writer Handler")
@RunLevel(value = 4)
public class ChangeSetWriterHandler
         implements ChangeSetWriterService, ChangeSetListener {
   private static final Logger LOG            = LogManager.getLogger();
   private static final String jsonFileSuffix = "json";
   private static final String ibdfFileSuffix = "ibdf";
   private static final String CHANGESETS     = "changesets";

   //~--- fields --------------------------------------------------------------

   private final UUID        changeSetWriterHandlerUuid = UUID.randomUUID();
   private DataWriterService writer;
   private ExecutorService   changeSetWriteExecutor;
   private boolean           writeEnabled;
   private Boolean           dbBuildMode;
   private Path              changeSetFolder;

   //~--- constructors --------------------------------------------------------

   public ChangeSetWriterHandler()
            throws Exception {
      Optional<Path> databasePath = LookupService.getService(ConfigurationService.class)
                                                 .getDataStoreFolderPath();

      changeSetFolder = databasePath.get()
                                    .resolve(CHANGESETS);
      Files.createDirectories(changeSetFolder);

      if (!changeSetFolder.toFile()
                          .isDirectory()) {
         throw new RuntimeException("Cannot initialize Changeset Store - was unable to create " +
                                    changeSetFolder.toAbsolutePath());
      }

      writer = new MultipleDataWriterService(changeSetFolder,
            "ChangeSet-",
            Optional.of(jsonFileSuffix),
            Optional.of(ibdfFileSuffix));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void disable() {
      writeEnabled = false;
   }

   @Override
   public void enable() {
      writeEnabled = true;
   }

   @Override
   public void handlePostCommit(CommitRecord commitRecord) {
      LOG.info("handle Post Commit");

      if (dbBuildMode == null) {
         dbBuildMode = Get.configurationService()
                          .inDBBuildMode();

         if (dbBuildMode) {
            stopMe();
         }
      }

      if (writeEnabled &&!dbBuildMode) {
         // Do in the backgound
         Runnable r = new Runnable() {
            @Override
            public void run() {
               try {
                  if ((commitRecord.getConceptsInCommit() != null) && (commitRecord.getConceptsInCommit().size() > 0)) {
                     sequenceSetChange(commitRecord.getConceptsInCommit());
                     LOG.debug("handle Post Commit: {} concepts", commitRecord.getConceptsInCommit()
                           .size());
                  }

                  if ((commitRecord.getSememesInCommit() != null) && (commitRecord.getSememesInCommit().size() > 0)) {
                     sequenceSetChange(commitRecord.getSememesInCommit());
                     LOG.debug("handle Post Commit: {} sememes", commitRecord.getSememesInCommit()
                           .size());
                  }
               } catch (Exception e) {
                  LOG.error("Error in Change set writer handler ", e.getMessage());
                  throw new RuntimeException(e);
               }
            }
         };

         changeSetWriteExecutor.execute(r);
      } else {
         LOG.info("ChangeSetWriter ignoring commit");
      }
   }

   @Override
   public void pause()
            throws IOException {
      if (writer != null) {
         writer.pause();
      }
   }

   @Override
   public void resume()
            throws IOException {
      if (writer != null) {
         writer.resume();
      }
   }

   /*
    */
   private void sequenceSetChange(ConceptSequenceSet conceptSequenceSet) {
      conceptSequenceSet.stream().forEach((conceptSequence) -> {
                                    ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService()
                                                                                                .getConcept(
                                                                                                   conceptSequence);

                                    try {
                                       writeToFile(concept);
                                    } catch (IOException e) {
                                       throw new RuntimeException(e);
                                    }
                                 });
   }

   /*
    */
   private void sequenceSetChange(SememeSequenceSet sememeSequenceSet) {
      sememeSequenceSet.stream().forEach((sememeSequence) -> {
                                   SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeService()
                                                                                            .getSememe(sememeSequence);

                                   try {
                                      writeToFile(sememe);
                                   } catch (IOException e) {
                                      throw new RuntimeException(e);
                                   }
                                });
   }

   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting ChangeSetWriterHandler post-construct");
         enable();
         changeSetWriteExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("ISAAC-changeset-write",
               false));
         Get.postCommitService()
            .addChangeSetListener(this);
      } catch (Exception e) {
         LOG.error("Error in ChangeSetWriterHandler post-construct ", e);
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Change Set Writer Handler", e);
         throw new RuntimeException(e);
      }
   }

   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping ChangeSetWriterHandler pre-destroy");
      disable();

      if (changeSetWriteExecutor != null) {
         changeSetWriteExecutor.shutdown();
         changeSetWriteExecutor = null;
      }

      if (writer != null) {
         LOG.debug("Close writer");

         try {
            writer.close();
         } catch (IOException e) {
            LOG.error("Error closing changeset writer!", e);
         } finally {
            writer = null;
         }
      }
   }

   private void writeToFile(OchreExternalizable ochreObject)
            throws IOException {
      writer.put(ochreObject);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getListenerUuid() {
      return changeSetWriterHandlerUuid;
   }

   @Override
   public Path getWriteFolder() {
      return changeSetFolder;
   }

   @Override
   public boolean getWriteStatus() {
      return writeEnabled;
   }
}

