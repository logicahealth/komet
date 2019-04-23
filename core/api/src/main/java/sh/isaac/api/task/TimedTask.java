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
import java.time.Duration;
import java.time.Instant;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;
//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.concurrent.Task;

import javafx.concurrent.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.util.FxTimer;

import sh.isaac.api.util.time.DurationUtil;

import static javafx.concurrent.Worker.State.*;

//~--- classes ----------------------------------------------------------------
/**
 * The Class TimedTask.
 *
 * @author kec
 * @param <T> the generic type
 */
public abstract class TimedTask<T>
        extends Task<T> {

    /**
     * The Constant log.
     */
    protected static final Logger LOG = LogManager.getLogger();

    /**
     * The progress update interval.
     */
    public Duration progressUpdateDuration = Duration.ofMillis(10);

    /**
     * Seconds per minute.
     */
    static final int SECONDS_PER_MINUTE = 60;

    /**
     * Minutes per hour.
     */
    static final int MINUTES_PER_HOUR = 60;

    /**
     * Seconds per hour.
     */
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

    static final AtomicInteger TASK_SEQUENCE = new AtomicInteger();

    //~--- fields --------------------------------------------------------------
    /**
     * The update ticker.
     */
    private final FxTimer updateTimer = FxTimer.createPeriodic(progressUpdateDuration, this::generateProgressMessage);

    /**
     * The start time.
     */
    private Instant startTime;

    /**
     * The end time.
     */
    private Instant endTime;

    /**
     * The complete message generator.
     */
    Consumer<TimedTask<T>> completeMessageGenerator;

    /**
     * The progress message generator.
     */
    Consumer<TimedTask<T>> progressMessageGenerator;

    protected final int taskSequenceId = TASK_SEQUENCE.incrementAndGet();

    private boolean canCancel = false;

    String simpleTitle = this.getClass().getSimpleName();
    {
        titleProperty().addListener((observable, oldValue, newValue) ->  simpleTitle = newValue);
    }

    public TimedTask() {
    }

    public TimedTask(Duration progressUpdateDuration) {
        this.progressUpdateDuration = progressUpdateDuration;
    }

    /**
     * Done.
     */
    @Override
    protected void done() {
        super.done();
        this.endTime = Instant.now();
        this.updateTimer.stop();

        if (this.completeMessageGenerator == null) {
            setCompleteMessageGenerator((task) -> {
                updateMessage(getSimpleName() + " in " + DurationUtil.format(getDuration()));
            });
        }
        LOG.info(getSimpleName() + " " + taskSequenceId + " completed in: " + DurationUtil.format(getDuration()));

        Platform.runLater(() -> {
            if (exceptionProperty().get() != null) {
                Throwable ex = exceptionProperty().get();
                if (ex instanceof InterruptedException) {
                    LOG.trace(ex.getLocalizedMessage(), ex);
                } else {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
            this.completeMessageGenerator.accept(this);
        });
    }

    protected String getSimpleName() {
        return getClass().getSimpleName();
    }

    /**
     * Failed.
     */
    @Override
    protected void failed() {
        LOG.warn("Timed task failed!", this.getException());
    }

    protected void generateProgressMessage() {
        if (this.progressMessageGenerator != null) {
            this.progressMessageGenerator.accept(this);
        }
    }

    /**
     * Running.
     */
    @Override
    protected void running() {
        super.running();

        if (this.startTime == null) {
            this.startTime = Instant.now();
        }
        updateTimer.start();
    }

    /**
     * Sets the complete message generator.
     *
     * @param consumer the new complete message generator
     */
    public void setCompleteMessageGenerator(Consumer<TimedTask<T>> consumer) {
        this.completeMessageGenerator = consumer;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public Duration getDuration() {
        if (this.startTime == null) {
            return Duration.ZERO;
        }

        if (this.endTime == null) {
            return Duration.between(this.startTime, Instant.now());
        }

        return Duration.between(this.startTime, this.endTime);
    }

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    public String getFormattedDuration() {
        return DurationUtil.format(getDuration());
    }

    /**
     * Sets the progress message generator.
     *
     * @param consumer the new progress message generator
     */
    public void setProgressMessageGenerator(Consumer<TimedTask<T>> consumer) {
        this.progressMessageGenerator = consumer;
    }

    /**
     * Set start time.
     */
    protected void setStartTime() {
        this.startTime = Instant.now();
    }

    public Instant getStartTime() {
        return startTime;
    }
    
    /**
     * @return a unique ID (within this JVM) for the task being executed.
     */
    public int getTaskId() {
        return taskSequenceId;
    }

    public String toString() {

        return simpleTitle + " " + taskSequenceId + " " + getState();
    }

    public boolean canCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }
}