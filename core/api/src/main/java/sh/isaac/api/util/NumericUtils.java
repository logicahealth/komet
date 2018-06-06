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

import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicDouble;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicFloat;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicInteger;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicLong;

//~--- classes ----------------------------------------------------------------

/**
 * Various number related utilities.
 *
 * @author darmbrust
 */
public class NumericUtils {
   /**
    * Compare.
    *
    * @param x the x
    * @param y the y
    * @return the int
    */
   public static int compare(final Number x, final Number y) {
      if (isSpecial(x) || isSpecial(y)) {
         return Double.compare(x.doubleValue(), y.doubleValue());
      } else {
         return toBigDecimal(x).compareTo(toBigDecimal(y));
      }
   }

   /**
    * Parses the unknown.
    *
    * @param value the value
    * @return the number
    * @throws NumberFormatException the number format exception
    */
   public static Number parseUnknown(String value)
            throws NumberFormatException {
      if (value == null) {
         throw new NumberFormatException("No value");
      }

      final String temp = value.trim();

      try {
         return Integer.parseInt(temp);
      } catch (final Exception e) {
         // noop
      }

      try {
         return Long.parseLong(temp);
      } catch (final Exception e) {
         // noop
      }

      try {
         return Float.parseFloat(temp);
      } catch (final Exception e) {
         // noop
      }

      return Double.parseDouble(temp);
   }

   /**
    * Read number.
    *
    * @param value the value
    * @return the number
    * @throws NumberFormatException the number format exception
    */
   public static Number readNumber(DynamicData value)
            throws NumberFormatException {
      if (value instanceof DynamicDouble) {
         return ((DynamicDouble) value).getDataDouble();
      } else if (value instanceof DynamicFloat) {
         return ((DynamicFloat) value).getDataFloat();
      } else if (value instanceof DynamicInteger) {
         return ((DynamicInteger) value).getDataInteger();
      } else if (value instanceof DynamicLong) {
         return ((DynamicLong) value).getDataLong();
      } else {
         throw new NumberFormatException("The value passed in to the validator is not a number");
      }
   }

   /**
    * To big decimal.
    *
    * @param number the number
    * @return the big decimal
    * @throws NumberFormatException the number format exception
    */
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

   /**
    * Gets the int.
    *
    * @param string the string
    * @return the int
    */
   public static Optional<Integer> getInt(String string) {
      try {
         return Optional.of(Integer.parseInt(string.trim()));
      } catch (final Exception e) {
         return Optional.empty();
      }
   }

   /**
    * Checks if int.
    *
    * @param string the string
    * @return true, if int
    */
   public static boolean isInt(String string) {
      return (getInt(string).isPresent());
   }

   /**
    * Gets the long.
    *
    * @param string the string
    * @return the long
    */
   public static Optional<Long> getLong(String string) {
      try {
         return Optional.of(Long.parseLong(string.trim()));
      } catch (final Exception e) {
         return Optional.empty();
      }
   }

   /**
    * Checks if long.
    *
    * @param string the string
    * @return true, if long
    */
   public static boolean isLong(String string) {
      return getLong(string).isPresent();
   }
   
   public static boolean isNumber(String string) {
      try {
         parseUnknown(string);
         return true;
      }
      catch (NumberFormatException e) {
         return false;
      }
   }

   /**
    * Same as isInt / getInt - however - only returns a value if the parsed integer is negative.
    *
    * @param string the string
    * @return the nid
    */
   public static Optional<Integer> getNID(String string) {
      final Optional<Integer> possibleInt = getInt(string);

      return (possibleInt.isPresent() && (possibleInt.get() < 0)) ? possibleInt
            : Optional.empty();
   }

   /**
    * Checks if nid.
    *
    * @param string the string
    * @return true, if nid
    */
   public static boolean isNID(String string) {
      return (getNID(string).isPresent());
   }

   /**
    * Checks if special.
    *
    * @param x the x
    * @return true, if special
    */
   private static boolean isSpecial(final Number x) {
      final boolean specialDouble = (x instanceof Double) &&
                                    (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
      final boolean specialFloat = (x instanceof Float) && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));

      return specialDouble || specialFloat;
   }
}

