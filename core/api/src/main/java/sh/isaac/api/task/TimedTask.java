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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;
//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.ticker.Ticker;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TimedTask.
 *
 * @author kec
 * @param <T> the generic type
 */
public abstract class TimedTask<T>
        extends Task<T> {
   /** The Constant log. */
   protected static final Logger log = LogManager.getLogger();

   /** The progress update interval. */
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
   
   static final AtomicInteger taskSequence = new AtomicInteger();

   //~--- fields --------------------------------------------------------------

   /** The update ticker. */
   private final Ticker updateTicker = new Ticker();

   /** The start time. */
   private Instant startTime;

   /** The end time. */
   private Instant endTime;

   /** The complete message generator. */
   Consumer<TimedTask<T>> completeMessageGenerator;

   /** The progress message generator. */
   Consumer<TimedTask<T>> progressMessageGenerator;
   
   int taskSequenceId = taskSequence.incrementAndGet();

   public TimedTask() {
   }

   public TimedTask(Duration progressUpdateDuration) {
      this.progressUpdateDuration = progressUpdateDuration;
   }

   
   //~--- methods -------------------------------------------------------------

   /**
    * Done.
    */
   @Override
   protected void done() {
      super.done();
      this.endTime = Instant.now();
      this.updateTicker.stop();

      if (this.completeMessageGenerator == null) {
         setCompleteMessageGenerator((task) -> {
                                        updateMessage(getState() + " in " + formatDuration(getDuration()));
                                     });
      }

      Platform.runLater(() -> {
                           this.completeMessageGenerator.accept(this);
                           log.info(getClass().getSimpleName() + " " + taskSequenceId + ": " + getTitle() + " " + getMessage());
                        });
   }

   /**
    * Failed.
    */
   @Override
   protected void failed() {
      log.warn("Timed task failed!", this.getException());
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

      this.updateTicker.start(progressUpdateDuration,
                              (value) -> {
                                 if (this.progressMessageGenerator != null) {
                                    this.progressMessageGenerator.accept(this);
                                 }
                              });
   }

   /**
    * Format duration.
    *
    * @param d the d
    * @return the string
    */
   private String formatDuration(Duration d) {
      final StringBuilder builder = new StringBuilder();
      final long          seconds = d.getSeconds();

      if (seconds > 0) {
         final long hours   = seconds / SECONDS_PER_HOUR;
         final int  minutes = (int) ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
         final int  secs    = (int) (seconds % SECONDS_PER_MINUTE);

         if (hours != 0) {
            builder.append(hours)
                   .append("h ");
         }

         if (minutes != 0) {
            builder.append(minutes)
                   .append("m ");
         }

         builder.append(secs)
                .append("s");
         return builder.toString();
      }

      final int  nanos = d.getNano();
      final long milis = TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);

      if (milis > 0) {
         return builder.append(milis)
                       .append(" ms")
                       .toString();
      }

      final long micro = TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS);

      if (micro > 0) {
         return builder.append(micro)
                       .append(" μs")
                       .toString();
      }

      return builder.append(nanos)
                    .append(" ns")
                    .toString();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the complete message generator.
    *
    * @param consumer the new complete message generator
    */
   public void setCompleteMessageGenerator(Consumer<TimedTask<T>> consumer) {
      this.completeMessageGenerator = consumer;
   }

   //~--- get methods ---------------------------------------------------------

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
      return formatDuration(getDuration());
   }

   //~--- set methods ---------------------------------------------------------

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
}

