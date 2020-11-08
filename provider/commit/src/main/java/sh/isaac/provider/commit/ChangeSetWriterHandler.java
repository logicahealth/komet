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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeSetListener;
import sh.isaac.api.commit.ChangeSetWriterService;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.MultipleDataWriterService;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;


/**
 * {@link ChangeSetWriterHandler}.
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
@Service(name = "Change Set Writer Handler")
@RunLevel(value = LookupService.SL_L5_ISAAC_STARTED_RUNLEVEL)
public class ChangeSetWriterHandler
         implements ChangeSetWriterService, ChangeSetListener {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant JSON_FILE_SUFFIX. */
   private static final String JSON_FILE_SUFFIX = "json";

   /** The Constant IBDF_FILE_SUFFIX. */
   private static final String IBDF_FILE_SUFFIX = "ibdf";

   /** The Constant CHANGESETS. */
   private static final String CHANGESETS = "changesets";

   private static final int MAX_AVAILABLE = 20;
   private final Semaphore writePermits = new Semaphore(MAX_AVAILABLE);

   /** The change set writer handler uuid. */
   private final UUID changeSetWriterHandlerUuid = UUID.randomUUID();

   /** The writer. */
   private DataWriterService writer;

   /** The change set write executor. */
   private ExecutorService changeSetWriteExecutor;

   /** The write enabled. */
   private boolean writeEnabled;

   /** The change set folder. */
   private Path changeSetFolder;

   /**
   * For HK2
   *
   * @throws Exception the exception
   */
   private ChangeSetWriterHandler()
            throws Exception {
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void disable() {
      this.writeEnabled = false;
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void enable() {
      this.writeEnabled = true;
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void handlePostCommit(CommitRecord commitRecord) {
      LOG.info("handle Post Commit");
      if (this.writeEnabled && !Get.configurationService().isInDBBuildMode()) {
         // Do in the backgound
         writePermits.acquireUninterruptibly();  //prevent incoming commits from getting to far ahead
      final TimedTaskWithProgressTracker<Void> tt = new WriteChangeSetTask(commitRecord);

         Future<?> result = this.changeSetWriteExecutor.submit(tt);
         try {
            result.get();
         } catch (InterruptedException e) {
            LOG.error(e);
         } catch (ExecutionException e) {
            LOG.error(e);
         }
      } else {
         if (Get.configurationService().isInDBBuildMode()) {
            LOG.debug("ChangeSetWriter ignoring commit because in db build mode. ");
         }
         if (!this.writeEnabled) {
            LOG.debug("ChangeSetWriter ignoring commit because write disabled. ");
         }
      }
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void pause()
            throws IOException {
      if (this.writer != null) {
         this.writer.pause();
      }
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void resume()
            throws IOException {
      if (this.writer != null) {
         this.writer.resume();
      }
   }

   /**
   * @param conceptNidSet the concept nid set
   */
   private void conceptNidSetChange(NidSet conceptNidSet, WriteChangeSetTask task) {
      conceptNidSet.stream().forEach((conceptSequence) -> {
         final ConceptChronologyImpl concept = (ConceptChronologyImpl) Get.conceptService().getConceptChronology(conceptSequence);
         concept.removeUncommittedVersions();
         try {
            writeToFile(concept);
         } catch (final IOException e) {
            throw new RuntimeException("Error writing concept " + conceptSequence, e);
         }
         task.completedUnitOfWork();
      });
   }

   /**
   * @param semanticNidSet the semantic sequence set
   */
   private void semanticNidSetChange(NidSet semanticNidSet, WriteChangeSetTask task) {
      semanticNidSet.stream().forEach((semanticSequence) -> {
         final SemanticChronologyImpl semantic = (SemanticChronologyImpl) Get.assemblageService().getSemanticChronology(semanticSequence);
         semantic.removeUncommittedVersions();
         try {
            writeToFile(semantic);
         } catch (final IOException e) {
            throw new RuntimeException("Error writing semantic " + semanticSequence, e);
         }
         task.completedUnitOfWork();
      });
   }

   /**
   * For HK2
   */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting ChangeSetWriterHandler post-construct");
         final Path databasePath = LookupService.getService(ConfigurationService.class).getDataStoreFolderPath();

         this.changeSetFolder = databasePath.resolve(CHANGESETS);
         Files.createDirectories(this.changeSetFolder);
         
         if (!this.changeSetFolder.toFile().isDirectory()) {
            throw new RuntimeException("Cannot initialize Changeset Store - was unable to create " +
            this.changeSetFolder.toAbsolutePath());
         }
         
         this.writer = new MultipleDataWriterService(this.changeSetFolder, "ChangeSet-", Optional.of(JSON_FILE_SUFFIX), Optional.of(IBDF_FILE_SUFFIX));
         enable();
         this.changeSetWriteExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("ISAAC-changeset-write", false));
         Get.postCommitService().addChangeSetListener(this);
      } catch (final IOException | RuntimeException e) {
         LOG.error("Error in ChangeSetWriterHandler post-construct ", e);
         LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Change Set Writer Handler", e);
         throw new RuntimeException(e);
      }
   }

   /**
   * For HK2
   */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping ChangeSetWriterHandler waiting for all writes to complete");
      Get.postCommitService().removeChangeSetListener(this);
      disable();
      writePermits.acquireUninterruptibly(MAX_AVAILABLE);
      writePermits.release(MAX_AVAILABLE);
      LOG.info("Stopping ChangeSetWriterHandler writes complete");

      if (this.changeSetWriteExecutor != null) {
         this.changeSetWriteExecutor.shutdown();
         this.changeSetWriteExecutor = null;
      }

      if (this.writer != null) {
         LOG.debug("Close writer");

         try {
            this.writer.close();
         }
         catch (final IOException e) {
            LOG.error("Error closing changeset writer!", e);
         }
         finally {
            this.writer = null;
         }
      }
      LOG.info("Stopped ChangeSetWriterHandler");
   }

   /**
   * Write to file.
   *
   * @param ochreObject the ochre object
   * @throws IOException Signals that an I/O exception has occurred.
   */
   private void writeToFile(IsaacExternalizable ochreObject)
            throws IOException {
      this.writer.put(ochreObject);
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public UUID getListenerUuid() {
      return this.changeSetWriterHandlerUuid;
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public Path getWriteFolder() {
      return this.changeSetFolder;
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public boolean getWriteStatus() {
      return this.writeEnabled;
   }

   private class WriteChangeSetTask extends TimedTaskWithProgressTracker<Void> {
      CommitRecord commitRecord;
      public WriteChangeSetTask(CommitRecord commitRecord) {
         this.commitRecord = commitRecord;
         updateTitle("Writing Changeset for commit " + commitRecord.getCommitComment());
         Get.activeTasks().add(this);
         addToTotalWork(commitRecord.getConceptsInCommit().size());
         addToTotalWork(commitRecord.getSemanticNidsInCommit().size());
      }

      @Override
      protected Void call() throws Exception {

         try {
            if ((commitRecord.getConceptsInCommit() != null) && (commitRecord.getConceptsInCommit().size() > 0)) {
               conceptNidSetChange(commitRecord.getConceptsInCommit(), this);
               LOG.debug("handle Post Commit: {} concepts", commitRecord.getConceptsInCommit().size());
            }

            if ((commitRecord.getSemanticNidsInCommit() != null) && (commitRecord.getSemanticNidsInCommit().size() > 0)) {
               semanticNidSetChange(commitRecord.getSemanticNidsInCommit(), this);
               LOG.debug("handle Post Commit: {} semantics", commitRecord.getSemanticNidsInCommit().size());
            }
         }
         catch (final Exception e) {
            LOG.error("Error in Change set writer handler ", e.getMessage());
            throw new RuntimeException(e);
         }
         finally {
            writePermits.release();
            Get.activeTasks().remove(this);
         }
         return null;
      }

   }
}