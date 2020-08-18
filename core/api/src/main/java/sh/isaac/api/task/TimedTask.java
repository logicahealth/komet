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

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import sh.isaac.api.util.FxTimer;
import sh.isaac.api.util.time.DurationUtil;

/**
 * The Class TimedTask.
 *
 * @author kec
 * @param <T> the generic type
 */
public abstract class TimedTask<T>
        extends Task<T> {

    protected static final Logger LOG = LogManager.getLogger();

    public Duration progressUpdateDuration = Duration.ofMillis(10);

    static final int SECONDS_PER_MINUTE = 60;
    static final int MINUTES_PER_HOUR = 60;
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

    private final FxTimer updateTimer = FxTimer.createPeriodic(progressUpdateDuration, this::generateProgressMessage);

    private Instant startTime;
    private Instant endTime;

    private boolean suppressCompletionLogIfLessThanSpecifiedDuration = true;
    private long suppressionTimeDurationInSeconds = 5;
    private static int suppressionCount = 0;
    private static final int suppressionStartCount = 11;

    private Consumer<TimedTask<T>> completeMessageGenerator;
    private Consumer<TimedTask<T>> progressMessageGenerator;

    protected final UUID taskId = UUID.randomUUID();

    private boolean canCancel = false;

    protected String simpleTitle = this.getClass().getSimpleName();
    {
        titleProperty().addListener((observable, oldValue, newValue) ->  simpleTitle = newValue);
    }

    public TimedTask() {
    }

    public TimedTask(Duration progressUpdateDuration) {
        this.progressUpdateDuration = progressUpdateDuration;
    }

    public boolean getSuppressCompletionLogIfLessThanSpecifiedDuration() {
        return suppressCompletionLogIfLessThanSpecifiedDuration;
    }

    public void setSuppressCompletionLogIfLessThanSpecifiedDuration(boolean suppressCompletionLogIfLessThanSpecifiedDuration) {
        this.suppressCompletionLogIfLessThanSpecifiedDuration = suppressCompletionLogIfLessThanSpecifiedDuration;
    }

    public long getSuppressionTimeDurationInSeconds() {
        return suppressionTimeDurationInSeconds;
    }

    public void setSuppressionTimeDurationInSeconds(long suppressionTimeDurationInSeconds) {
        this.suppressionTimeDurationInSeconds = suppressionTimeDurationInSeconds;
    }

    @Override
    protected void done() {
        super.done();
        this.endTime = Instant.now();
        this.updateTimer.stop();

        if (this.completeMessageGenerator == null) {
            setCompleteMessageGenerator((task) -> {
                updateMessage(getSimpleName() + " completed in " + DurationUtil.format(getDuration()));
            });
        }
        
        //TODO DAN[1] notes, this is odd.  Typically, if you are suppressing logs, you only suppress identical logs in a certain time frame.
        //Not ALL logs after X.... Not to mention most of the performance issues caused by this logging could be properly handled with TRACE
        //And using the proper logging pattern, so it isn't calculating values when they aren't logged, as I have updated it below.
        if (suppressCompletionLogIfLessThanSpecifiedDuration) {
            if (getDuration().getSeconds() > suppressionTimeDurationInSeconds) {
                LOG.trace("{} {} completed in: {}", (() -> getSimpleName()),  (() -> taskId), (() -> DurationUtil.format(getDuration())));
            } else {
                if (suppressionCount < suppressionStartCount) {
                    suppressionCount++;
                    LOG.trace("{} {} completed in: {}", (() -> getSimpleName()),  (() -> taskId), (() -> DurationUtil.format(getDuration())));
                    LOG.trace("Suppression of task completion logging for tasks shorter than " + suppressionTimeDurationInSeconds +
                            " seconds will occur after "+ (suppressionStartCount - suppressionCount) + " more completions.");
                }
            }
        } else {
            LOG.trace("{} {} completed in: {}", (() -> getSimpleName()),  (() -> taskId), (() -> DurationUtil.format(getDuration())));
        }


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

    @Override
    protected void failed() {
        Throwable throwable = this.getException();
        if (throwable instanceof CancellationException) {
            LOG.info("Timed task " + taskId + " " + this.getSimpleName() + " canceled");
        } else {
            LOG.warn("Timed task " + taskId + " failed!", throwable);
        }
     }

    protected void generateProgressMessage() {
        if (this.progressMessageGenerator != null) {
            this.progressMessageGenerator.accept(this);
        }
    }

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
     * @return a unique ID for the task being executed.
     */
    public UUID getTaskId() {
        return taskId;
    }

    @Override
	public String toString() {

        return simpleTitle + " " + taskId + " " + getState();
    }

    public boolean canCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }
}