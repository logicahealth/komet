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



package sh.isaac.api.coordinate;

//~--- JDK imports ------------------------------------------------------------

import sh.isaac.api.collections.NidSet;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

//~--- interfaces -------------------------------------------------------------

/**
 * Analog: A structural derivative that often differs by a single element.
 * @author kec
 * @param <T> The type of object to make a time-based analog from.
 */
public interface TimeBasedAnalogMaker<T> {
   /**
    * Analog: A structural derivative that often differs by a single element.
    * @param dateTimeText the text to parse such as "2007-12-03T10:15:30", which is specified by the ISO-8601 extended offset date-time format.
    *  @return a new {@code <T>}  with the specified stamp position time.
    */
   default T makeAnalog(CharSequence dateTimeText) {
      return makeCoordinateAnalog(LocalDateTime.parse(dateTimeText)
                                     .toEpochSecond(ZoneOffset.UTC) * 1000);
   }

   /**
    * Make analog.
    *
    * @param stampPositionTime the time of the stamp position for the analog
    * @return a new {@code <T>}  with the specified stamp position time.
    */
   T makeCoordinateAnalog(long stampPositionTime);

   /**
    * Analog: A structural derivative that often differs by a single element.
    * @param temporalSpecification temporal - the temporal object to specify the time for the resulting {@code <T>}
    * @return a new {@code <T>}  with the specified stamp position time.
    */
   default T makeAnalog(TemporalAccessor temporalSpecification) {
      return makeCoordinateAnalog(LocalDateTime.from(temporalSpecification)
                                     .toEpochSecond(ZoneOffset.UTC) * 1000);
   }

   /**
    * Analog: A structural derivative that often differs by a single element.
    * @param year the year to represent, from MIN_YEAR to MAX_YEAR
    * @param month the month-of-year to represent, from 1 (January) to 12 (December)
    * @param dayOfMonth the day-of-month to represent, from 1 to 31
    * @param hour the hour-of-day to represent, from 0 to 23
    * @param minute the minute-of-hour to represent, from 0 to 59
    * @param second the second-of-minute to represent, from 0 to 59
    * @return a new {@code <T>}  with the specified stamp position time.
    */
   default T makeAnalog(int year, int month, int dayOfMonth, int hour, int minute, int second) {
      return makeCoordinateAnalog(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second)
                                     .toEpochSecond(ZoneOffset.UTC) * 1000);
   }
}

