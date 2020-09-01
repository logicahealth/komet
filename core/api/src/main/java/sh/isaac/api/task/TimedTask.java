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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

    private long suppressionForTasksShorterThan = 5;
    private static final int SUPPRESSION_TIME = 30;
    private static final int SUPPRESSION_AFTER_X_IN_SUPRESSION_TIME = 10;

    //Note that these timed expirations are not done on a strict schedule, it may take further activity to trigger them
    private static Cache<String, AtomicInteger> suppressCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(SUPPRESSION_TIME))
            .removalListener((key, value, cause) -> {
                if (((AtomicInteger)value).get() > SUPPRESSION_AFTER_X_IN_SUPRESSION_TIME) {
                    LOG.debug("Suppressed {} task completion logs from {}, next will be logged normally.", ((AtomicInteger)value).get() - SUPPRESSION_AFTER_X_IN_SUPRESSION_TIME, 
                    key, SUPPRESSION_TIME);
                }
            }).build(); 

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

    /**
     * Tasks that execute more quickly than this will be suppressed from the logs, if too many occur in a short time.
     * @return
     */
    public long getSuppressionForTasksShorterThanSeconds() {
        return suppressionForTasksShorterThan;
    }

    /**
     * Tasks longer than this value always have their end times logs.  Shorter than this, may be suppressed from the logs, if they 
     * occur in too short of a window.  Set to max value, to disable suppression 
     * @param suppressionTimeDurationInSeconds
     */
    public void setSuppressionForTasksShorterThanSeconds(long suppressionTimeDurationInSeconds) {
        this.suppressionForTasksShorterThan = suppressionTimeDurationInSeconds;
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

        if (getDuration().getSeconds() > suppressionForTasksShorterThan) {
            LOG.debug("{} {} completed in: {}", (() -> getSimpleName()),  (() -> taskId), (() -> DurationUtil.format(getDuration())));
        } else {
            AtomicInteger hitCount = suppressCache.get(getSimpleName(), (nameAgain -> new AtomicInteger(0)));
            int total = hitCount.getAndIncrement();
            if (total >= SUPPRESSION_AFTER_X_IN_SUPRESSION_TIME) { //More than SUPRESSION_AFTER_X_IN_SUPRESSION_TIME, in SUPRESSION_TIME seconds, stop printing them.
                if (total == SUPPRESSION_AFTER_X_IN_SUPRESSION_TIME) {
                    LOG.debug(" Tasks of type {} rapidly executing, will suppress tasks of this type for up to {} seconds.", getSimpleName(), SUPPRESSION_TIME);
                }
            }
            else {
                LOG.debug("{} {} completed in: {}", (() -> getSimpleName()),  (() -> taskId), (() -> DurationUtil.format(getDuration())));
            }
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