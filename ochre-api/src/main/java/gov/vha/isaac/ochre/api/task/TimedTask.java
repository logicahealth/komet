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

import gov.vha.isaac.ochre.api.ticker.Ticker;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kec
 * @param <T>
 */
public abstract class TimedTask<T> extends Task<T>  {
    
    protected static final Logger log = LogManager.getLogger();

    public static int progressUpdateIntervalInSecs = 2;

    private final Ticker updateTicker = new Ticker();

    private Instant startTime;
    private Instant endTime;

    Consumer<TimedTask<T>> completeMessageGenerator;
    Consumer<TimedTask<T>> progressMessageGenerator;

    public Duration getDuration() {
        if (startTime == null) {
            return Duration.ZERO;
        }
        if (endTime == null) {
            return Duration.between(startTime, Instant.now());
        }
        return Duration.between(startTime, endTime);
    }

    public void setProgressMessageGenerator(Consumer<TimedTask<T>> consumer) {
        progressMessageGenerator = consumer;
    }

    public void setCompleteMessageGenerator(Consumer<TimedTask<T>> consumer) {
        completeMessageGenerator = consumer;
    }

    @Override
    protected void done() {
        super.done();
        endTime = Instant.now();
        updateTicker.stop();
        if (completeMessageGenerator == null) {
            setCompleteMessageGenerator((task) -> { 
                updateMessage(getState() + " in " + formatDuration(getDuration()));
            });
        }
        
        Platform.runLater(() -> {
            completeMessageGenerator.accept(this);
            log.info(getTitle() + " " + getMessage());
        });
    }
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

    public String getFormattedDuration() {
        return formatDuration(getDuration());
    }
    
    
    private String formatDuration(Duration d) {
        StringBuilder builder = new StringBuilder();
        long seconds = d.getSeconds();
        if (seconds > 0) {
            long hours = seconds / SECONDS_PER_HOUR;
            int minutes = (int) ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
            int secs = (int) (seconds % SECONDS_PER_MINUTE);
            if (hours != 0) {
                builder.append(hours).append("h ");
            }
            if (minutes != 0) {
                builder.append(minutes).append("m ");
            }
            builder.append(secs).append("s");
            return builder.toString();
        }
        int nanos = d.getNano();
        long milis = TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
        if (milis > 0) {
            return builder.append(milis).append(" ms").toString();
        }
        long micro = TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS);
        if (micro > 0) {
            return builder.append(micro).append(" μs").toString();
        }
        return builder.append(nanos).append(" ns").toString();
    }

    protected void setStartTime() {
       startTime = Instant.now();
    }
    
    @Override
    protected void running() {
        super.running();
        if (startTime == null) {
            startTime = Instant.now();
        }
        
        updateTicker.start(progressUpdateIntervalInSecs, (value) -> {
            if (progressMessageGenerator != null) {
                progressMessageGenerator.accept(this);
            }
        });
    }

}
