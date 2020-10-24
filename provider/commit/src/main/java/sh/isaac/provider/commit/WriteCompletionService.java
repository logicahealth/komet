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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//~--- non-JDK imports --------------------------------------------------------
import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- classes ----------------------------------------------------------------
/**
 * This class executes the jobs to write to disk, and also has a single thread
 * running to call .get() on the future for each job submitted, to ensure that
 * any errors are logged / warned (if the caller didn't bother to call get)
 *
 * Previous versions of this class used the ExecuterCompletionService stuff...
 * to handle all of this, but that turns out to be fundamentally broken in 
 * combination with JavaFX Task objects - you end up with a Task and a Future, 
 * and the Task never gets completion notification.
 * 
 * The powers that be at Oracle have been perfectly happy to call this terrible
 * behavior a documentation bug.... 
 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8166449
 *
 * @author kec
 * @author darmbrust
 */
public class WriteCompletionService
        implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * The write concept completion service thread.
     */
    private Thread writeConceptCompletionServiceThread;

    /**
     * The conversion service.
     */
    private ExecutorCompletionService<Void> conversionService;

    /**
     * The worker pool.
     */
    private ExecutorService workerPool;
    
    private final LinkedBlockingQueue<Future<Void>> completionQueue = new LinkedBlockingQueue<>();

    /**
     * Run.
     */
    @Override
    public void run() {
        LOG.info("WriteCompletionService starting");
        
        //need a local ref, to make sure we don't take a null pointer during shutdown
        ExecutorService workerPoolCopy = workerPool;
        ExecutorCompletionService<Void> conversionServiceCopy = conversionService;
        
        while (!workerPoolCopy.isTerminated() || !completionQueue.isEmpty()) {
            try {
               Future<Void> f = conversionServiceCopy.poll(10, TimeUnit.SECONDS);
               if (f != null) {
                  f.get();
               }
            } catch (final InterruptedException ex) {
                if (!workerPoolCopy.isTerminated() && !completionQueue.isEmpty()) {
                    // Only warn if we were not asked to shutdown
                    LOG.warn(ex.getLocalizedMessage(), ex);
                }
            } catch (final ExecutionException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
        this.writeConceptCompletionServiceThread = null;
        LOG.info("Stopped WriteCompletionService writeConceptCompletionServiceThread");
    }

    /**
     * Start.
     */
    public void start() {
        LOG.info("Starting WriteCompletionService");
        this.workerPool = Executors.newFixedThreadPool(4,
                (Runnable r) -> {
                    return new Thread(r, "writeCommitDataPool");
                });
        this.conversionService = new ExecutorCompletionService<>(this.workerPool, completionQueue);
        this.writeConceptCompletionServiceThread = new Thread(this, "writeCompletionService");
        this.writeConceptCompletionServiceThread.start();
    }

    /**
     * Stop.
     */
    public void stop() {
        LOG.info("Stopping WriteCompletionService");
        this.workerPool.shutdown(); //Disable new tasks from being submitted
        this.writeConceptCompletionServiceThread.interrupt();
        boolean terminated = this.workerPool.isTerminated();
        while (!terminated) {
            try {
                terminated = this.workerPool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                // Nothing to do. 
            }
        }
        Thread t = this.writeConceptCompletionServiceThread;
        if (t != null) {
           t.interrupt();
        }
        LOG.info("Stopped WriteCompletionService workerPool");
        this.workerPool = null;
        this.conversionService = null;
        LOG.info("Stopped WriteCompletionService");
    }

    /**
     * Submit.
     *
     * @param task the task
     * @return the future
     */
    protected Future<Void> submit(Task<Void> task) {
        return this.conversionService.submit(task, null);
    }
}
