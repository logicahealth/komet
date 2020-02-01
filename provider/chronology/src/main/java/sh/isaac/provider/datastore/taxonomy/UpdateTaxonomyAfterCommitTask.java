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
package sh.isaac.provider.datastore.taxonomy;

//~--- JDK imports ------------------------------------------------------------
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TestConcept;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

//~--- classes ----------------------------------------------------------------
/**
 * The Class UpdateTaxonomyAfterCommitTask.
 *
 * @author kec
 */
public class UpdateTaxonomyAfterCommitTask
        extends TimedTaskWithProgressTracker<Void> {

    //~--- fields --------------------------------------------------------------
    /**
     * The work done.
     */
    AtomicInteger workDone = new AtomicInteger(0);

    /**
     * The total work.
     */
    AtomicInteger  totalWork = new AtomicInteger(0);

    /**
     * The taxonomy service.
     */
    TaxonomyProvider taxonomyProvider;

    /**
     * The commit record.
     */
    CommitRecord commitRecord;

    /**
     * The semantic identifiers for unhandled changes.
     */
    ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges;

    /**
     * The lock.
     */
    Semaphore permit;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new update taxonomy after commit task.
     *
     * @param taxonomyService the taxonomy service
     * @param commitRecord the commit record
     * @param semanticNidsForUnhandledChanges the semantic sequences for unhandled
     * changes
     * @param permit the lock
     */
    private UpdateTaxonomyAfterCommitTask(TaxonomyProvider taxonomyProvider,
            CommitRecord commitRecord,
            ConcurrentSkipListSet<Integer> semanticNidsForUnhandledChanges,
            Semaphore permit) {
        this.commitRecord = commitRecord;
        this.semanticNidsForUnhandledChanges = semanticNidsForUnhandledChanges;
        this.permit = permit;
        this.taxonomyProvider = taxonomyProvider;
        this.totalWork.set(semanticNidsForUnhandledChanges.size());
        this.updateTitle("Update taxonomy after commit");
        this.addToTotalWork(totalWork.get());
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Call.
     *
     * @return the void
     * @throws Exception the exception
     */
    @Override
    protected Void call()
            throws Exception {

        try {
            final AtomicBoolean atLeastOneFailed = new AtomicBoolean(false);
            final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
            Semaphore updateTaxonomySemaphore = new Semaphore(WRITE_PERMITS);

            this.semanticNidsForUnhandledChanges.stream().parallel().forEach((semanticNid) -> {
                if (TestConcept.WATCH_NID_SET.contains(semanticNid)) {
                    LOG.info("FOUND WATCH IN SET: " + TestConcept.WATCH_NID_SET);
                }
                try {
                    this.workDone.incrementAndGet();
                    this.completedUnitOfWork();

                    if (this.commitRecord.getSemanticNidsInCommit()
                            .contains(semanticNid)) {
                        this.updateMessage("Updating taxonomy for: " + semanticNid);
                        updateTaxonomySemaphore.acquire();
                        Get.executor().execute(() -> {
                            try {
                                this.taxonomyProvider.updateTaxonomy((SemanticChronology) Get.assemblageService()
                                        .getSemanticChronology(semanticNid));
                            } catch (Throwable t) {
                                LOG.error(t);
                                throw t;
                            } finally {
                                updateTaxonomySemaphore.release();
                            }

                        });

                        this.semanticNidsForUnhandledChanges.remove(semanticNid);
                    }
                } catch (final Exception e) {
                    LOG.error("Error handling update taxonomy after commit on semantic " + semanticNid, e);
                    atLeastOneFailed.set(true);
                }
            });

            if (atLeastOneFailed.get()) {
                throw new RuntimeException("There were errors during taxonomy update after commit");
            }
            updateMessage("Waiting for update taxonomy completion...");
            updateTaxonomySemaphore.acquireUninterruptibly(WRITE_PERMITS);

            this.updateMessage("complete");
            return null;
        } finally {
            this.permit.release();
            Get.activeTasks()
                    .remove(this);
            this.taxonomyProvider.getPendingUpdateTasks().remove(this);
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Get an executing task that will update the taxonomy.
     *
     * @param taxonomyService the service to update
     * @param commitRecord the commitRecord to process
     * @param unhandledChanges the changes to look for
     * @param permit permit for the update
     * @return a task, submitted to an executor, and added to the active task
     * set.
     *
     */
    public static UpdateTaxonomyAfterCommitTask get(TaxonomyProvider taxonomyService,
            CommitRecord commitRecord,
            ConcurrentSkipListSet<Integer> unhandledChanges,
            Semaphore permit) {
        final UpdateTaxonomyAfterCommitTask task = new UpdateTaxonomyAfterCommitTask(taxonomyService,
                commitRecord,
                unhandledChanges,
                permit);
        Get.activeTasks().add(task);
        taxonomyService.getPendingUpdateTasks().add(task);
        Get.workExecutors()
                .getExecutor()
                .execute(task);
        return task;
    }
}
