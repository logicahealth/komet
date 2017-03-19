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

/*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
 */
import java.util.Comparator;

//~--- classes ----------------------------------------------------------------

/**
 * The Alphanum Algorithm is an improved sorting algorithm for strings containing numbers.
 *
 * Instead of sorting numbers in ASCII order like a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The inspiration for this implementation came from http://www.DaveKoelle.com However, his implementation did not handle leading 0's properly, nor
 * did it handle nulls or case sensitivity.
 *
 * I fixed all of those issues, and also added convenience methods.
 *
 * @author <A HREF="mailto:daniel.armbrust@gmail.com">Dan Armbrust</A>
 *
 * See http://armbrust.dyndns.org/programs/index.php?page=3
 */
public class AlphanumComparator
         implements Comparator<String> {
   /** The case sensitive instance. */
   private static AlphanumComparator caseSensitiveInstance_;

   /** The case insensitive instance. */
   private static AlphanumComparator caseInsensitiveInstance_;

   //~--- fields --------------------------------------------------------------

   /** The ignore case. */
   private final boolean ignoreCase_;

   //~--- constructors --------------------------------------------------------

   /**
    * Create a new instance of an AlphanumComparator.
    *
    * @param ignoreCase the ignore case
    */
   public AlphanumComparator(boolean ignoreCase) {
      this.ignoreCase_ = ignoreCase;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare.
    *
    * @param s1 the s 1
    * @param s2 the s 2
    * @return the int
    */
   @Override
   public int compare(String s1, String s2) {
      if (s1 == null) {
         return -1;
      }

      if (s2 == null) {
         return 1;
      }

      int       thisMarker = 0;
      int       thatMarker = 0;
      final int s1Length   = s1.length();
      final int s2Length   = s2.length();

      while ((thisMarker < s1Length) && (thatMarker < s2Length)) {
         final String thisChunk = getChunk(s1, s1Length, thisMarker);

         thisMarker += thisChunk.length();

         final String thatChunk = getChunk(s2, s2Length, thatMarker);

         thatMarker += thatChunk.length();

         // If both chunks contain numeric characters, sort them numerically
         int result = 0;

         if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
            int[] thisChunkInt = subChunkNumeric(thisChunk);
            int[] thatChunkInt = subChunkNumeric(thatChunk);

            // 0 pad the shorter array, so that they have the same length.
            if (thisChunkInt.length > thatChunkInt.length) {
               final int[] temp         = new int[thisChunkInt.length];
               int         insertOffset = thisChunkInt.length - thatChunkInt.length;

               for (int i = 0; i < thatChunkInt.length; i++) {
                  temp[insertOffset++] = thatChunkInt[i];
               }

               thatChunkInt = temp;
            } else {
               if (thisChunkInt.length < thatChunkInt.length) {
                  final int[] temp         = new int[thatChunkInt.length];
                  int         insertOffset = thatChunkInt.length - thisChunkInt.length;

                  for (int i = 0; i < thisChunkInt.length; i++) {
                     temp[insertOffset++] = thisChunkInt[i];
                  }

                  thisChunkInt = temp;
               }
            }

            for (int i = 0; i < thisChunkInt.length; i++) {
               if (thisChunkInt[i] > thatChunkInt[i]) {
                  result = 1;
                  break;
               } else {
                  if (thisChunkInt[i] < thatChunkInt[i]) {
                     result = -1;
                     break;
                  }
               }
            }
         } else {
            if (this.ignoreCase_) {
               result = thisChunk.compareToIgnoreCase(thatChunk);
            } else {
               result = thisChunk.compareTo(thatChunk);
            }
         }

         if (result != 0) {
            return result;
         }
      }

      return s1Length - s2Length;
   }

   /**
    * Compare.
    *
    * @param left the left
    * @param right the right
    * @param ignoreCase the ignore case
    * @return the int
    */
   public static int compare(String left, String right, boolean ignoreCase) {
      return getCachedInstance(ignoreCase).compare(left, right);
   }

   /**
    * Sub chunk numeric.
    *
    * @param numericChunk the numeric chunk
    * @return the int[]
    */

   /*
    * Take in string (which we assume will pass Integer.ParseInt) and return an array of integers.
    * An array is returned so we don't exceed the limits of int.
    *
    * For example, 45600000000524566874861567 would be returned as : [456000000,005245668,74861567]
    */
   private int[] subChunkNumeric(String numericChunk) {
      final int[] result = new int[(int) Math.ceil(numericChunk.length() / 9.0)];
      int         s      = 0;
      int         e      = ((9 > numericChunk.length()) ? numericChunk.length()
            : 9);

      for (int i = 0; i < result.length; i++) {
         result[i] = Integer.parseInt(numericChunk.substring(s, e));
         s         = e;
         e         = ((e + 9 > numericChunk.length()) ? numericChunk.length()
               : e + 9);
      }

      return result;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Get a reference to a cached, shared instance. Good for reuse, but would have multithreading issues if many threads are trying to sort at the
    * same time.
    *
    * @param ignoreCase the ignore case
    * @return the cached instance
    */
   public static synchronized AlphanumComparator getCachedInstance(boolean ignoreCase) {
      if (ignoreCase) {
         if (caseSensitiveInstance_ == null) {
            caseSensitiveInstance_ = new AlphanumComparator(true);
         }

         return caseSensitiveInstance_;
      } else {
         if (caseInsensitiveInstance_ == null) {
            caseInsensitiveInstance_ = new AlphanumComparator(false);
         }

         return caseInsensitiveInstance_;
      }
   }

   /**
    * Length of string is passed in for improved efficiency (only need to calculate it once).
    *
    * @param s the s
    * @param slength the slength
    * @param marker the marker
    * @return the chunk
    */
   private String getChunk(String s, int slength, int marker) {
      final StringBuilder chunk = new StringBuilder();
      char                c     = s.charAt(marker);

      chunk.append(c);
      marker++;

      if (isDigit(c)) {
         while (marker < slength) {
            c = s.charAt(marker);

            if (!isDigit(c)) {
               break;
            }

            chunk.append(c);
            marker++;
         }
      } else {
         while (marker < slength) {
            c = s.charAt(marker);

            if (isDigit(c)) {
               break;
            }

            chunk.append(c);
            marker++;
         }
      }

      return chunk.toString();
   }

   /**
    * Checks if digit.
    *
    * @param ch the ch
    * @return true, if digit
    */
   private boolean isDigit(char ch) {
      return (ch >= 48) && (ch <= 57);
   }
}

