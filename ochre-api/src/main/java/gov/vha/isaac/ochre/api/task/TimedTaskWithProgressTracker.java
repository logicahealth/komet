/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api.task;

import static java.lang.invoke.MethodHandles.publicLookup;
import gov.vha.isaac.ochre.api.ProgressTracker;
import gov.vha.isaac.ochre.api.ticker.Ticker;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import javafx.concurrent.Task;

/**
 *
 * @author kec
 * @param <T> Type that the completed task returns. 
 */
public abstract class TimedTaskWithProgressTracker<T> extends TimedTask<T> implements ProgressTracker {
    private final Ticker progressTicker = new Ticker();
    
    static final MethodHandle MH_SET_TOTAL_WORK;
    static final MethodHandle MH_SET_PROGRESS;
    static final MethodHandle MH_SET_WORK_DONE;
    
    static {
        try {
            Method setTotalWork = Task.class.getDeclaredMethod("setTotalWork", double.class);
            setTotalWork.setAccessible(true);
            MH_SET_TOTAL_WORK = publicLookup().unreflect(setTotalWork);

            Method setProgress = Task.class.getDeclaredMethod("setProgress", double.class);
            setProgress.setAccessible(true);
            MH_SET_PROGRESS = publicLookup().unreflect(setProgress);
            
            Method setWorkDone = Task.class.getDeclaredMethod("setWorkDone", double.class);
            setWorkDone.setAccessible(true);
            MH_SET_WORK_DONE = publicLookup().unreflect(setWorkDone);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    } 

    LongAdder completedUnitsOfWork = new LongAdder();
    AtomicLong totalWork = new AtomicLong();
    AtomicLong lastTotalWork = new AtomicLong();
    
    @Override
    public void addToTotalWork(long amountOfWork) {
        totalWork.addAndGet(amountOfWork);
    }

    @Override
    public void completedUnitOfWork() {
        completedUnitsOfWork.increment();
    }
    
    @Override
    public void completedUnitsOfWork(long unitsCompleted) {
        completedUnitsOfWork.add(unitsCompleted);
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
    
    @Override
    protected void running() {
        super.running();
        long currentTotalWork = totalWork.get();
        progressTicker.start(progressUpdateIntervalInSecs, (value) -> {
            try {
                
                if (currentTotalWork > 0) {
                    MH_SET_WORK_DONE.invoke(this, completedUnitsOfWork.doubleValue());
                    MH_SET_PROGRESS.invoke(this, completedUnitsOfWork.doubleValue() / totalWork.doubleValue());
                    MH_SET_TOTAL_WORK.invoke(this, totalWork.doubleValue());
                } else {
                    MH_SET_WORK_DONE.invoke(this, -1d);
                    MH_SET_PROGRESS.invoke(this, -1d);
                    MH_SET_TOTAL_WORK.invoke(this, -1d);
                }
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }
    
   @Override
    protected void done() {
        super.done();
        progressTicker.stop();
    }
    
}
