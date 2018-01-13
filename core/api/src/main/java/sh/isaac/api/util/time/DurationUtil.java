/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.util.time;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author kec
 */
public class DurationUtil {
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

   public static String format(java.time.Duration d) {
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
}
