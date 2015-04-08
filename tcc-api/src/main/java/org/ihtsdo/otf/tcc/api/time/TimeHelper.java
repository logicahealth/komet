/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.api.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 *
 * @author kec
 */
public class TimeHelper {
   public static final FastDateFormat localDateFormat = FastDateFormat.getInstance("MM/dd/yy HH:mm:ss");
   public static final FastDateFormat localLongFileFormat = FastDateFormat.getInstance("yyyy-MM-dd-HH.mm.ss");
   public static final FastDateFormat localShortFileFormat = FastDateFormat.getInstance("yyyyMMdd");
 
   //~--- methods -------------------------------------------------------------

   private static String FormatDateForFile(Date date) {
      return localDateFormat.format(date);
   }

   private static String formatDate(Date date) {
      return localDateFormat.format(date);
   }

   public static String formatDate(long time) {
      return formatDate(new Date(time));
   }

   public static String formatDate(Date date, FastDateFormat formatter) {
      if (date.getTime() == Long.MIN_VALUE) {
         return "beginning of time";
      }

      if (date.getTime() == Long.MAX_VALUE) {
         return "end of time";
      }

      return formatter.format(date);
   }

   public static String formatDate(long time, FastDateFormat formatter) {
      return formatDate(new Date(time), formatter);
   }

   public static String formatDateForFile(long time) {
      return FormatDateForFile(new Date(time));
   }

   //~--- get methods ---------------------------------------------------------

   public static FastDateFormat getDateFormat() {
      return localDateFormat;
   }

   public static String getElapsedTimeString(long elapsed) {
      String elapsedStr =
         String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(elapsed),
                       TimeUnit.MILLISECONDS.toSeconds(elapsed)
                       - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)));

      return elapsedStr;
   }

   public static FastDateFormat getFileDateFormat() {
      return localLongFileFormat;
   }

   public static FastDateFormat getFormatter(final String pattern) {
      return FastDateFormat.getInstance(pattern);
   }

   public static String getRemainingTimeString(int completedCount, int totalCount, long elapsed) {
      float  conceptCountFloat = totalCount;
      float  completedFloat    = completedCount;
      float  percentComplete   = completedFloat / conceptCountFloat;
      float  estTotalTime      = elapsed / percentComplete;
      long   remaining         = (long) (estTotalTime - elapsed);
      String remainingStr      = getElapsedTimeString(remaining);

      return remainingStr;
   }

   public static FastDateFormat getShortFileDateFormat() {
      return localShortFileFormat;
   }

   public static long getTimeFromString(String time, SimpleDateFormat formatter) throws ParseException {
      if (time.toLowerCase().equals("latest")) {
         return Long.MAX_VALUE;
      }

      if (time.toLowerCase().equals("bot")) {
         return Long.MIN_VALUE;
      }

      return formatter.parse(time).getTime();
   }
}
