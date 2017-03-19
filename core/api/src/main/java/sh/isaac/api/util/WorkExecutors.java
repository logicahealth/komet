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
@RunLevel(value = -2)
public class WorkExecutors {
   private static final Logger           log             = LogManager.getLogger();
   private volatile static WorkExecutors nonHK2Instance_ = null;

   //~--- fields --------------------------------------------------------------

   private ForkJoinPool             forkJoinExecutor_;
   private ThreadPoolExecutor       blockingThreadPoolExecutor_;
   private ThreadPoolExecutor       threadPoolExecutor_;
   private ThreadPoolExecutor       ioThreadPoolExecutor_;
   private ScheduledExecutorService scheduledExecutor_;

   //~--- constructors --------------------------------------------------------

   private WorkExecutors() {
      // For HK2 only
   }

   //~--- methods -------------------------------------------------------------

   public static void main(String[] args)
            throws InterruptedException {
      WorkExecutors we = new WorkExecutors();

      we.startMe();

      AtomicInteger counter = new AtomicInteger();

      for (int i = 0; i < 24; i++) {
         System.out.println("submit " + i);
         we.getPotentiallyBlockingExecutor().submit(new Runnable() {
                      @Override
                      public void run() {
                         int id = counter.getAndIncrement();

                         System.out.println(id + " started");

                         try {
                            Thread.sleep(5000);
                         } catch (InterruptedException e) {
                            e.printStackTrace();
                         }

                         System.out.println(id + " finished");
                      }
                   });
      }

      Thread.sleep(7000);
      System.out.println("Blocking test over");

      for (int i = 24; i < 48; i++) {
         System.out.println("submit " + i);
         we.getExecutor().submit(new Runnable() {
                      @Override
                      public void run() {
                         int id = counter.getAndIncrement();

                         System.out.println(id + " started");

                         try {
                            Thread.sleep(5000);
                         } catch (InterruptedException e) {
                            e.printStackTrace();
                         }

                         System.out.println(id + " finished");
                      }
                   });
      }

      while (we.getExecutor()
               .getQueue()
               .size() > 0) {
         Thread.sleep(1000);
      }

      Thread.sleep(7000);
   }

   @PostConstruct
   private void startMe() {
      log.info("Starting the WorkExecutors thread pools");

      if (nonHK2Instance_ != null) {
         throw new RuntimeException(
             "Two instances of WorkExectors started!  If HK2 will manage, startup HK2 properly first!");
      }

      // The java default ForkJoinPool.commmonPool starts with only 1 thread, on 1 and 2 core systems, which can get us deadlocked pretty easily.
      int procCount   = Runtime.getRuntime()
                               .availableProcessors();
      int parallelism = ((procCount - 1) < 6 ? 6
            : procCount - 1);  // set between 6 and 1 less than proc count (not less than 6)

      forkJoinExecutor_ = new ForkJoinPool(parallelism);

      int      corePoolSize    = 2;
      int      maximumPoolSize = parallelism;
      int      keepAliveTime   = 60;
      TimeUnit timeUnit        = TimeUnit.SECONDS;

      // The blocking executor
      blockingThreadPoolExecutor_ = new ThreadPoolExecutor(corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            timeUnit,
            new SynchronousQueue<Runnable>(),
            new NamedThreadFactory("ISAAC-B-work-thread", true));
      blockingThreadPoolExecutor_.setRejectedExecutionHandler((runnable, executor) -> {
               try {
                  executor.getQueue()
                          .offer(runnable, Long.MAX_VALUE, TimeUnit.HOURS);
               } catch (Exception e) {
                  throw new RejectedExecutionException("Interrupted while waiting to enqueue");
               }
            });

      // The non-blocking executor - set core threads equal to max - otherwise, it will never increase the thread count
      // with an unbounded queue.
      threadPoolExecutor_ = new ThreadPoolExecutor(maximumPoolSize,
            maximumPoolSize,
            keepAliveTime,
            timeUnit,
            new LinkedBlockingQueue<Runnable>(),
            new NamedThreadFactory("ISAAC-Q-work-thread", true));
      threadPoolExecutor_.allowCoreThreadTimeOut(true);

      // The IO non-blocking executor - set core threads equal to max - otherwise, it will never increase the thread count
      // with an unbounded queue.
      ioThreadPoolExecutor_ = new ThreadPoolExecutor(4,
            4,
            keepAliveTime,
            timeUnit,
            new LinkedBlockingQueue<Runnable>(),
            new NamedThreadFactory("ISAAC-IO-work-thread", true));
      ioThreadPoolExecutor_.allowCoreThreadTimeOut(true);

      // Execute this once, early on, in a background thread - as randomUUID uses secure random - and the initial
      // init of secure random can block on many systems that don't have enough entropy occuring.  The DB load process
      // should provide enough entropy to get it initialized, so it doesn't pause things later when someone requests a random UUID.
      getExecutor().execute(() -> UUID.randomUUID());
      scheduledExecutor_ = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ISAAC-Scheduled-Thread", true));
      log.debug("WorkExecutors thread pools ready");
   }

   @PreDestroy
   private void stopMe() {
      log.info("Stopping WorkExecutors thread pools");

      if (forkJoinExecutor_ != null) {
         forkJoinExecutor_.shutdownNow();
         forkJoinExecutor_ = null;
      }

      if (blockingThreadPoolExecutor_ != null) {
         blockingThreadPoolExecutor_.shutdownNow();
         blockingThreadPoolExecutor_ = null;
      }

      if (threadPoolExecutor_ != null) {
         threadPoolExecutor_.shutdownNow();
         threadPoolExecutor_ = null;
      }

      if (ioThreadPoolExecutor_ != null) {
         ioThreadPoolExecutor_.shutdownNow();
         ioThreadPoolExecutor_ = null;
      }

      if (scheduledExecutor_ != null) {
         scheduledExecutor_.shutdownNow();
         scheduledExecutor_ = null;
      }

      nonHK2Instance_ = null;
      log.debug("Stopped WorkExecutors thread pools");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return The ISAAC common {@link ThreadPoolExecutor} - (behavior described in the class docs).
    * This is backed by an unbounded queue - it won't block / reject submissions because of being full.
    * This executor has processing threads linkes to the number of CPUs available.  It is good for compute
    * intensive jobs.
    */
   public ThreadPoolExecutor getExecutor() {
      return threadPoolExecutor_;
   }

   /**
    * @return the ISAAC common {@link ForkJoinPool} instance - (behavior described in the class docs)
    * This is backed by an unbounded queue - it won't block / reject submissions because of being full.
    */
   public ForkJoinPool getForkJoinPoolExecutor() {
      return forkJoinExecutor_;
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
    * @return
    */
   public static WorkExecutors get() {
      log.debug("In WorkExectors.get()");

      if (LookupService.isInitialized() &&
            (LookupService.getCurrentRunLevel() >= LookupService.WORKERS_STARTED_RUNLEVEL)) {
         log.debug("Handing back the HK2 managed instance");
         return Get.workExecutors();
      } else {
         log.debug("Returning static WorkExecutors instance");

         if (nonHK2Instance_ == null) {
            synchronized (log) {
               if (nonHK2Instance_ == null) {
                  log.debug("Setting up static WorkExecutors");

                  // if we aren't relying on the lookup service, we need to make sure the headless toolkit was installed (otherwise, the task APIs end up broken)
                  LookupService.startupFxPlatform();

                  WorkExecutors temp = new WorkExecutors();

                  temp.startMe();
                  nonHK2Instance_ = temp;
                  Runtime.getRuntime()
                         .addShutdownHook(new Thread(() -> {
                                                        log.debug("Shutting down static instance of WorkExecutors");
                                                        nonHK2Instance_.stopMe();
                                                        log.debug("stopped static instance of WorkExecutors");
                                                     }));
               }
            }
         }

         return nonHK2Instance_;
      }
   }

   /**
    * @return The ISAAC common IO {@link ThreadPoolExecutor} - (behavior described in the class docs).
    * This is backed by an unbounded queue - it won't block / reject submissions because of being full.
    * This executor differs from {@link #getExecutor()} by having a much smaller number of threads - good for
    * jobs that tend to block on IO.
    */
   public ThreadPoolExecutor getIOExecutor() {
      return ioThreadPoolExecutor_;
   }

   /**
    * @return The ISAAC common {@link ThreadPoolExecutor} - (behavior described in the class docs).
    * This is a synchronous queue - if no thread is available to take a job, it will block until a thread
    * is available to accept the job.
    */
   public ThreadPoolExecutor getPotentiallyBlockingExecutor() {
      return blockingThreadPoolExecutor_;
   }

   /**
    * @return the ISAAC common {@link ScheduledThreadPoolExecutor} instance - (behavior described in the class docs)
    * This pool only has a single thread - submitted jobs should be fast executing.
    */
   public ScheduledExecutorService getScheduledThreadPoolExecutor() {
      return scheduledExecutor_;
   }
}

