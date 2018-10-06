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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author kec
 */
public class DateTimeUtil {
    public static final long MS_IN_YEAR =   1000L * 60 * 60 * 24 * 365;
    public static final long MS_IN_MONTH =  1000L * 60 * 60 * 24 * 30;
    public static final long MS_IN_DAY =    1000L * 60 * 60 * 24;
    public static final long MS_IN_HOUR =   1000L * 60 * 60;
    public static final long MS_IN_MINUTE = 1000L * 60;
    public static final long MS_IN_SEC =    1000L;
    
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter SEC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MIN_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter ZONE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  
    public static ZonedDateTime epochToZonedDateTime(long epochMilliSecond) {
        return Instant.ofEpochMilli(epochMilliSecond).atZone(ZoneOffset.UTC);
    }
    public static Instant epochToInstant(long epochMilliSecond) {
        return Instant.ofEpochMilli(epochMilliSecond);
    }
    public static String format(long epochMilliSecond) {
        if (epochMilliSecond == Long.MAX_VALUE) {
            return "Latest";
        }
        if (epochMilliSecond == Long.MIN_VALUE) {
            return "Canceled";
        }
       return FORMATTER.format(Instant.ofEpochMilli(epochMilliSecond).atZone(ZoneOffset.UTC));
    }
    
    public static String format(Instant instant) {
        if (instant.equals(Instant.MAX)) {
            return "Latest";
        }
        if (instant.equals(Instant.MIN)) {
            return "Canceled";
        }
       return FORMATTER.format(instant);
    }
    public static String format(ZonedDateTime zonedDateTime) {
       return zonedDateTime.format(ZONE_FORMATTER);
    }
    public static String format(Instant instant, Double resolution) {
        if (instant.equals(Instant.MAX)) {
            return "Latest";
        }
        if (instant.equals(Instant.MIN)) {
            return "Canceled";
        }
        if (resolution < MS_IN_SEC) {
            return ZONE_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_MINUTE) {
            return SEC_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_HOUR) {
            return MIN_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_DAY) {
            return HOUR_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_MONTH) {
            return DAY_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_YEAR) {
            return MONTH_FORMATTER.format(instant);
        }
       return YEAR_FORMATTER.format(instant);
    }
    public static String format(ZonedDateTime zonedDateTime, Double resolution) {
        if (resolution < MS_IN_SEC) {
            return zonedDateTime.format(ZONE_FORMATTER);
        }
        if (resolution < MS_IN_MINUTE) {
            return zonedDateTime.format(SEC_FORMATTER);
        }
        if (resolution < MS_IN_HOUR) {
            return zonedDateTime.format(MIN_FORMATTER);
        }
        if (resolution < MS_IN_DAY) {
            return zonedDateTime.format(HOUR_FORMATTER);
        }
        if (resolution < MS_IN_MONTH) {
            return zonedDateTime.format(DAY_FORMATTER);
        }
        if (resolution < MS_IN_YEAR) {
            return zonedDateTime.format(MONTH_FORMATTER);
        }
       return zonedDateTime.format(YEAR_FORMATTER);
    }

}
