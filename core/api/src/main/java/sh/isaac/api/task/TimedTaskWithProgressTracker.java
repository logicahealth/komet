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



package sh.isaac.api.task;

//~--- JDK imports ------------------------------------------------------------

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import static java.lang.invoke.MethodHandles.publicLookup;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ProgressTracker;
import sh.isaac.api.ticker.Ticker;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TimedTaskWithProgressTracker.
 *
 * @author kec
 * @param <T> Type that the completed task returns.
 */
public abstract class TimedTaskWithProgressTracker<T>
        extends TimedTask<T>
         implements ProgressTracker {
   /** The Constant MH_SET_TOTAL_WORK. */
   static final MethodHandle MH_SET_TOTAL_WORK;

   /** The Constant MH_SET_PROGRESS. */
   static final MethodHandle MH_SET_PROGRESS;

   /** The Constant MH_SET_WORK_DONE. */
   static final MethodHandle MH_SET_WORK_DONE;

   //~--- static initializers -------------------------------------------------

   static {
      try {
         final Method setTotalWork = Task.class.getDeclaredMethod("setTotalWork", double.class);

         setTotalWork.setAccessible(true);
         MH_SET_TOTAL_WORK = publicLookup().unreflect(setTotalWork);

         final Method setProgress = Task.class.getDeclaredMethod("setProgress", double.class);

         setProgress.setAccessible(true);
         MH_SET_PROGRESS = publicLookup().unreflect(setProgress);

         final Method setWorkDone = Task.class.getDeclaredMethod("setWorkDone", double.class);

         setWorkDone.setAccessible(true);
         MH_SET_WORK_DONE = publicLookup().unreflect(setWorkDone);
      } catch (IllegalAccessException | NoSuchMethodException | SecurityException ex) {
         throw new RuntimeException(ex);
      }
   }

   //~--- fields --------------------------------------------------------------

   /** The progress ticker. */
   private final Ticker progressTicker = new Ticker();

   /** The completed units of work. */
   LongAdder completedUnitsOfWork = new LongAdder();

   /** The total work. */
   AtomicLong totalWork = new AtomicLong();

   /** The last total work. */
   AtomicLong lastTotalWork = new AtomicLong();
   //~--- methods -------------------------------------------------------------

   /**
    * Adds the to total work.
    *
    * @param amountOfWork the amount of work
    */
   @Override
   public void addToTotalWork(long amountOfWork) {
      this.totalWork.addAndGet(amountOfWork);
   }

   /**
    * Completed unit of work.
    */
   @Override
   public void completedUnitOfWork() {
      this.completedUnitsOfWork.increment();
   }

   /**
    * Completed units of work.
    *
    * @param unitsCompleted the units completed
    */
   @Override
   public void completedUnitsOfWork(long unitsCompleted) {
      this.completedUnitsOfWork.add(unitsCompleted);
   }

   /**
    * Done.
    */
   @Override
   protected void done() {
      super.done();
      this.progressTicker.stop();
   }

   /**
    * Running.
    */
   @Override
   protected void running() {
      super.running();

      this.progressTicker.start(progressUpdateDuration,
                                (value) -> {
                                   try {
                                      if (this.totalWork.get() > 0) {
                                         MH_SET_WORK_DONE.invoke(this, this.completedUnitsOfWork.doubleValue());
                                         MH_SET_PROGRESS.invoke(this,
                                               this.completedUnitsOfWork.doubleValue() / this.totalWork.doubleValue());
                                         MH_SET_TOTAL_WORK.invoke(this, this.totalWork.doubleValue());
                                      } else {
                                         MH_SET_WORK_DONE.invoke(this, -1d);
                                         MH_SET_PROGRESS.invoke(this, -1d);
                                         MH_SET_TOTAL_WORK.invoke(this, -1d);
                                      }
                                   } catch (final Throwable throwable) {
                                      throw new RuntimeException(throwable);
                                   }
                                });
   }

   /**
    * Will throw an  UnsupportedOperationException("call completedUnitOfWork and addToTotalWork instead. ");
    * Use {@code completedUnitOfWork()} and {@code addToTotalWork(long amountOfWork)} to update progress.
    * @param workDone not used
    * @param max not used
    */
   @Override
   protected void updateProgress(double workDone, double max) {
      throw new UnsupportedOperationException("call completedUnitOfWork() and addToTotalWork instead. ");
   }

   /**
    * Will throw an  UnsupportedOperationException("call completedUnitOfWork and addToTotalWork instead. ");
    * Use {@code completedUnitOfWork()} and {@code addToTotalWork(long amountOfWork)} to update progress.
    * @param workDone not used
    * @param max not used
    */
   @Override
   protected void updateProgress(long workDone, long max) {
      throw new UnsupportedOperationException("call completedUnitOfWork() and addToTotalWork instead. ");
   }
}

