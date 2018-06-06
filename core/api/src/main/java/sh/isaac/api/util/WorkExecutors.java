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



package sh.isaac.api.util;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;

//~--- classes ----------------------------------------------------------------

/**
 * {@link WorkExecutors}
 *
 * Generally available thread pools for doing background processing in an ISAAC application.
 *
 * The {@link #getForkJoinPoolExecutor()} that this provides is identical to the @{link {@link ForkJoinPool#commonPool()}
 * with the exception that it will bottom out at 6 processing threads, rather than 1, to help prevent
 * deadlock situations in common ISAAC usage patterns.  This has an unbounded queue depth, and LIFO behavior.
 *
 * The {@link #getPotentiallyBlockingExecutor()} that this provides is a standard thread pool with (up to) the same number of threads
 * as there are cores present on the computer - with a minimum of 6 threads.  This executor has no queue - internally
 * it uses a {@link SynchronousQueue} - so if no thread is available to accept the task being queued, it will block
 * submission of the task until a thread is available to accept the job.
 *
 * The {@link #getExecutor()} that this provides is a standard thread pool with (up to) the same number of threads
 * as there are cores present on the computer - with a minimum of 6 threads.  This executor has an unbounded queue
 * depth, and FIFO behavior.
 *
 * The {@link #getIOExecutor()} that this provides is a standard thread pool with 4 threads.  This executor has an unbounded queue
 * depth, and FIFO behavior.  This executor is good for jobs that tend to block on disk IO, where you don't want many running in parallel.
 *
 * If you wish to use this code outside of an HK2 managed application (or in utility code that may operate in and out
 * of an HK2 environment), please use the {@link #get()} method to get a handle to this class
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@RunLevel(value = LookupService.SL_NEG_1_WORKERS_STARTED_RUNLEVEL)
public class WorkExecutors {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   /** The non HK 2 instance. */
   private volatile static WorkExecutors nonHK2Instance = null;

   //~--- fields --------------------------------------------------------------

   /** The fork join executor. */
   private ForkJoinPool forkJoinExecutor;

   /** The blocking thread pool executor. */
   private ThreadPoolExecutor blockingThreadPoolExecutor;

   /** The thread pool executor. */
   private ThreadPoolExecutor threadPoolExecutor;

   /** The io thread pool executor. */
   private ThreadPoolExecutor ioThreadPoolExecutor;

   /** The scheduled executor. */
   private ScheduledExecutorService scheduledExecutor;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new work executors.
    */
   private WorkExecutors() {
      // For HK2 only
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      log.info("Starting the WorkExecutors thread pools for change to runlevel: " + LookupService.getProceedingToRunLevel());

      if (nonHK2Instance != null) {
         throw new RuntimeException(
             "Two instances of WorkExectors started!  If HK2 will manage, startup HK2 properly first!");
      }

      // The java default ForkJoinPool.commmonPool starts with only 1 thread, on 1 and 2 core systems, which can get us deadlocked pretty easily.
      final int procCount   = Runtime.getRuntime()
                                     .availableProcessors();
      final int parallelism = ((procCount - 1) < 6 ? 6
            : procCount - 1);  // set between 6 and 1 less than proc count (not less than 6)

      this.forkJoinExecutor = new ForkJoinPool(parallelism);

      final int      corePoolSize    = 2;
      final int      maximumPoolSize = parallelism;
      final int      keepAliveTime   = 60;
      final TimeUnit timeUnit        = TimeUnit.SECONDS;

      // The blocking executor
      this.blockingThreadPoolExecutor = new ThreadPoolExecutorFixed(corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            timeUnit,
            new SynchronousQueue<>(),
            new NamedThreadFactory("ISAAC-B-work-thread", true));
      this.blockingThreadPoolExecutor.setRejectedExecutionHandler((runnable, executor) -> {
               try {
                  executor.getQueue()
                          .offer(runnable, Long.MAX_VALUE, TimeUnit.HOURS);
               } catch (final Exception e) {
                  throw new RejectedExecutionException("Interrupted while waiting to enqueue");
               }
            });

      // The non-blocking executor - set core threads equal to max - otherwise, it will never increase the thread count
      // with an unbounded queue.
      this.threadPoolExecutor = new ThreadPoolExecutorFixed(maximumPoolSize,
            maximumPoolSize,
            keepAliveTime,
            timeUnit,
            new LinkedBlockingQueue<>(),
            new NamedThreadFactory("ISAAC-Q-work-thread", true));
      this.threadPoolExecutor.allowCoreThreadTimeOut(true);

      // The IO non-blocking executor - set core threads equal to max - otherwise, it will never increase the thread count
      // with an unbounded queue.
      this.ioThreadPoolExecutor = new ThreadPoolExecutorFixed(6,
            6,
            keepAliveTime,
            timeUnit,
            new LinkedBlockingQueue<>(),
            new NamedThreadFactory("ISAAC-IO-work-thread", true));
      this.ioThreadPoolExecutor.allowCoreThreadTimeOut(true);

      // Execute this once, early on, in a background thread - as randomUUID uses secure random - and the initial
      // init of secure random can block on many systems that don't have enough entropy occuring.  The DB load process
      // should provide enough entropy to get it initialized, so it doesn't pause things later when someone requests a random UUID.
      getExecutor().execute(() -> UUID.randomUUID());
      this.scheduledExecutor = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("ISAAC-Scheduled-Thread", true));
      log.debug("WorkExecutors thread pools ready");
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      log.info("Stopping WorkExecutors thread pools for change to runlevel: " + LookupService.getProceedingToRunLevel());

      if (this.forkJoinExecutor != null) {
         this.forkJoinExecutor.shutdownNow();
         this.forkJoinExecutor = null;
      }

      if (this.blockingThreadPoolExecutor != null) {
         this.blockingThreadPoolExecutor.shutdownNow();
         this.blockingThreadPoolExecutor = null;
      }

      if (this.threadPoolExecutor != null) {
         this.threadPoolExecutor.shutdownNow();
         this.threadPoolExecutor = null;
      }

      if (this.ioThreadPoolExecutor != null) {
         this.ioThreadPoolExecutor.shutdownNow();
         this.ioThreadPoolExecutor = null;
      }

      if (this.scheduledExecutor != null) {
         this.scheduledExecutor.shutdownNow();
         this.scheduledExecutor = null;
      }

      nonHK2Instance = null;
      log.debug("Stopped WorkExecutors thread pools");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the executor.
    *
    * @return The ISAAC common {@link ThreadPoolExecutor} - (behavior described in the class docs).
    * This is backed by an unbounded queue - it won't block / reject submissions because of being full.
    * This executor has processing threads linked to the number of CPUs available.  It is good for compute
    * intensive jobs.
    */
   public ThreadPoolExecutor getExecutor() {
      return this.threadPoolExecutor;
   }

   /**
    * Gets the fork join pool executor.
    *
    * @return the ISAAC common {@link ForkJoinPool} instance - (behavior described in the class docs)
    * This is backed by an unbounded queue - it won't block / reject submissions because of being full.
    */
   public ForkJoinPool getForkJoinPoolExecutor() {
      return this.forkJoinExecutor;
   }

   /**
    * If HK2 has been properly started, this method safely returns the instance setup by HK2.
    * If HK2 has NOT been started, this method will create a static instance, and return a reference to that.
    * A JVM shutdown listener is registered to handle the thread pool shutdown in this case.
    * It is illegal (and will throw a runtime error) to ask for the static instance of this before
    * starting HK2 and then start HK2 - if HK2 is in use in the system, that should manage the lifecycle.
    *
    * This method is the preferred mechanism to get a handle to the WorkExecutors class in an enviornment where
    * code may be executed both in and out of an HK2 managed instance.
    *
    * If your usage is only run inside an HK2 management environment, then you should prefer the HK2 standard mechanisms
    * such as:
    * {@link Get#workExecutors()} or {@link LookupService#getService(WorkExecutors.class)} (however the end result is the same)
    *
    * @return the work executors
    */
   public static WorkExecutors get() {
      log.debug("In WorkExectors.get()");

      if (LookupService.isInitialized() && LookupService.getCurrentRunLevel() >= LookupService.SL_NEG_1_WORKERS_STARTED_RUNLEVEL) {
         log.debug("Handing back the HK2 managed instance");
         return Get.workExecutors();
      } else {
         log.debug("Returning static WorkExecutors instance");

         if (nonHK2Instance == null) {
            synchronized (log) {
               if (nonHK2Instance == null) {
                  log.debug("Setting up static WorkExecutors");

                  // if we aren't relying on the lookup service, we need to make sure the headless toolkit was installed (otherwise, the task APIs end up broken)
                  LookupService.startupFxPlatform();

                  final WorkExecutors temp = new WorkExecutors();

                  temp.startMe();
                  nonHK2Instance = temp;
                  Runtime.getRuntime()
                         .addShutdownHook(new Thread(() -> {
                                                        log.debug("Shutting down static instance of WorkExecutors");
                                                        nonHK2Instance.stopMe();
                                                        log.debug("stopped static instance of WorkExecutors");
                                                     }));
               }
            }
         }

         return nonHK2Instance;
      }
   }

   /**
    * Gets the IO executor.
    *
    * @return The ISAAC common IO {@link ThreadPoolExecutor} - (behavior described in the class docs).
    * This is backed by an unbounded queue - it won't block / reject submissions because of being full.
    * This executor differs from {@link #getExecutor()} by having a much smaller number of threads - good for
    * jobs that tend to block on IO.
    */
   public ThreadPoolExecutor getIOExecutor() {
      return this.ioThreadPoolExecutor;
   }

   /**
    * Gets the potentially blocking executor.
    *
    * @return The ISAAC common {@link ThreadPoolExecutor} - (behavior described in the class docs).
    * This is a synchronous queue - if no thread is available to take a job, it will block until a thread
    * is available to accept the job.
    */
   public ThreadPoolExecutor getPotentiallyBlockingExecutor() {
      return this.blockingThreadPoolExecutor;
   }

   /**
    * Gets the scheduled thread pool executor.
    *
    * @return the ISAAC common {@link ScheduledThreadPoolExecutor} instance - (behavior described in the class docs)
    * This pool only has a single thread - submitted jobs should be fast executing.
    */
   public ScheduledExecutorService getScheduledThreadPoolExecutor() {
      return this.scheduledExecutor;
   }
}

