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



package sh.isaac.provider.sync.git.gitblit.utils;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

//~--- classes ----------------------------------------------------------------

/**
 * Utility class of string functions.
 *
 *
 */
public class StringUtils {
   /**
    * Compare two repository names for proper group sorting.
    *
    * @param r1 the r 1
    * @param r2 the r 2
    * @return the int
    */
   public static int compareRepositoryNames(String r1, String r2) {
      // sort root repositories first, alphabetically
      // then sort grouped repositories, alphabetically
      r1 = r1.toLowerCase(Locale.ENGLISH);
      r2 = r2.toLowerCase(Locale.ENGLISH);

      final int s1 = r1.indexOf('/');
      final int s2 = r2.indexOf('/');

      if ((s1 == -1) && (s2 == -1)) {
         // neither grouped
         return r1.compareTo(r2);
      } else if ((s1 > -1) && (s2 > -1)) {
         // both grouped
         return r1.compareTo(r2);
      } else if (s1 == -1) {
         return -1;
      } else if (s2 == -1) {
         return 1;
      }

      return 0;
   }

   /**
    * Encodes a url parameter by escaping troublesome characters.
    *
    * @param inStr the in str
    * @return properly escaped url
    */
   public static String encodeURL(String inStr) {
      final StringBuilder retStr = new StringBuilder();
      int                 i      = 0;

      while (i < inStr.length()) {
         if (inStr.charAt(i) == '/') {
            retStr.append("%2F");
         } else if (inStr.charAt(i) == ' ') {
            retStr.append("%20");
         } else if (inStr.charAt(i) == '&') {
            retStr.append("%26");
         } else if (inStr.charAt(i) == '+') {
            retStr.append("%2B");
         } else {
            retStr.append(inStr.charAt(i));
         }

         i++;
      }

      return retStr.toString();
   }

   /**
    * Strips a trailing ".git" from the value.
    *
    * @param value the value
    * @return a stripped value or the original value if .git is not found
    */
   public static String stripDotGit(String value) {
      if (value.toLowerCase(Locale.ENGLISH)
               .endsWith(".git")) {
         return value.substring(0, value.length() - 4);
      }

      return value;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns true if the string is null or empty.
    *
    * @param value the value
    * @return true if string is null or empty
    */
   public static boolean isEmpty(String value) {
      return (value == null) || (value.trim().length() == 0);
   }

   /**
    * Returns the first path element of a path string.  If no path separator is
    * found in the path, an empty string is returned.
    *
    * @param path the path
    * @return the first element in the path
    */
   public static String getFirstPathElement(String path) {
      if (path.indexOf('/') > -1) {
         return path.substring(0, path.indexOf('/'))
                    .trim();
      }

      return "";
   }
}

