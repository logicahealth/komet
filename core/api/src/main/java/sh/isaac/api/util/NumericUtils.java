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



package sh.isaac.api.util;

//~--- JDK imports ------------------------------------------------------------

import java.math.BigDecimal;

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDouble;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;

//~--- classes ----------------------------------------------------------------

/**
 * Various number related utilities
 *
 * @author darmbrust
 */
public class NumericUtils {
   public static int compare(final Number x, final Number y) {
      if (isSpecial(x) || isSpecial(y)) {
         return Double.compare(x.doubleValue(), y.doubleValue());
      } else {
         return toBigDecimal(x).compareTo(toBigDecimal(y));
      }
   }

   public static Number parseUnknown(String value)
            throws NumberFormatException {
      if (value == null) {
         throw new NumberFormatException("No value");
      }

      String temp = value.trim();

      try {
         return Integer.parseInt(temp);
      } catch (Exception e) {
         // noop
      }

      try {
         return Long.parseLong(temp);
      } catch (Exception e) {
         // noop
      }

      try {
         return Float.parseFloat(temp);
      } catch (Exception e) {
         // noop
      }

      return Double.parseDouble(temp);
   }

   public static Number readNumber(DynamicSememeData value)
            throws NumberFormatException {
      if (value instanceof DynamicSememeDouble) {
         return Double.valueOf(((DynamicSememeDouble) value).getDataDouble());
      } else if (value instanceof DynamicSememeFloat) {
         return Float.valueOf(((DynamicSememeFloat) value).getDataFloat());
      } else if (value instanceof DynamicSememeInteger) {
         return Integer.valueOf(((DynamicSememeInteger) value).getDataInteger());
      } else if (value instanceof DynamicSememeLong) {
         return Long.valueOf(((DynamicSememeLong) value).getDataLong());
      } else {
         throw new NumberFormatException("The value passed in to the validator is not a number");
      }
   }

   public static BigDecimal toBigDecimal(final Number number)
            throws NumberFormatException {
      if ((number instanceof Integer) || (number instanceof Long)) {
         return new BigDecimal(number.longValue());
      } else if ((number instanceof Float) || (number instanceof Double)) {
         return new BigDecimal(number.doubleValue());
      } else {
         throw new NumberFormatException("Unexpected data type passed in to toBigDecimal (" + number.getClass() + ")");
      }
   }

   //~--- get methods ---------------------------------------------------------

   public static Optional<Integer> getInt(String string) {
      try {
         return Optional.of(Integer.parseInt(string.trim()));
      } catch (Exception e) {
         return Optional.empty();
      }
   }

   public static boolean isInt(String string) {
      return (getInt(string).isPresent());
   }

   public static Optional<Long> getLong(String string) {
      try {
         return Optional.of(Long.parseLong(string.trim()));
      } catch (Exception e) {
         return Optional.empty();
      }
   }

   public static boolean isLong(String string) {
      return getLong(string).isPresent();
   }

   /**
    * Same as isInt / getInt - however - only returns a value if the parsed integer is negative.
    * @param string
    * @return
    */
   public static Optional<Integer> getNID(String string) {
      Optional<Integer> possibleInt = getInt(string);

      return (possibleInt.isPresent() && (possibleInt.get().intValue() < 0)) ? possibleInt
            : Optional.empty();
   }

   public static boolean isNID(String string) {
      return (getNID(string).isPresent());
   }

   private static boolean isSpecial(final Number x) {
      boolean specialDouble = (x instanceof Double) && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
      boolean specialFloat  = (x instanceof Float) && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));

      return specialDouble || specialFloat;
   }
}

