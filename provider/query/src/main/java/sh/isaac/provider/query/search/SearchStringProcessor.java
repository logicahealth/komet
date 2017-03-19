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



package sh.isaac.provider.query.search;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link SearchStringProcessor}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

//TODO this class may not even need to exist, I think it was developed out of a mis-understanding of lucene.  Need to reevaulate as part of the search rewrite.
public class SearchStringProcessor {
   public static final String punctuationRegEx       = "!|\"|,|\'s|\'|:|;|\\?|`";
   public static final String symbolsRegEx           = "&|#|\\$|\\%|@|\\\\|_|\\|";
   public static final String operatorsRegEx         = "\\+|\\-|\\*|\\/|<|>|=|\\^|~";
   public static final String parensRegEx            = "\\(|\\)|\\{|\\}|\\[|\\]";
   public static final String escapedCharactersRegEx = "\\+|\\-|&|\\||!|\\(|\\)|\\{|\\}|\\[|\\]|\\^|\"|~|\\*|\\?|:|\\/";

   // Note: \xc2\xa0 is non-breaking space
   public static final String nonPrintableRegEx =
      "\\x00|\\x01|\\x02|\\x03|\\x04|\\x05|\\x06|\\x07|\\x08|\\x09|\\x0a|\\x0b|\\x0c|\\x0d|\\x0e|\\x0f|" +
      "\\x10|\\x11|\\x12|\\x13|\\x14|\\x15|\\x16|\\x17|\\x18|\\x19|\\x1a|\\x1b|\\x1c|\\x1d|\\x1e|\\x1f|" + "\\xc2\\xa0";
   public static final ArrayList<String> stopWords = new ArrayList<String>();

   //~--- static initializers -------------------------------------------------

   static {
      stopWords.add("a");
      stopWords.add("an");
      stopWords.add("and");
      stopWords.add("by");
      stopWords.add("for");
      stopWords.add("in");
      stopWords.add("not");
      stopWords.add("of");
      stopWords.add("on");
      stopWords.add("or");
      stopWords.add("the");
      stopWords.add("to");
      stopWords.add("with");
   }

   //~--- methods -------------------------------------------------------------

   public static String escapeCharacters(String s) {
      return s.replaceAll(escapedCharactersRegEx, "\\\\$0");
   }

   public static String prepareSearchString(String s) {
      String processedString = s;

      processedString = stripNonPrintable(processedString);
      processedString = escapeCharacters(processedString);
      return processedString;
   }

   public static String removeStopWords(String s) {
      String[]      words = s.trim()
                             .toLowerCase()
                             .split("\\s+");
      StringBuilder sb    = new StringBuilder("");

      for (String w: words) {
         w = w.trim();

         if (!stopWords.contains(w)) {
            sb.append(w);
            sb.append(" ");
         }
      }

      return sb.toString()
               .trim();
   }

   public static String stripAll(String s) {
      String allRegEx = punctuationRegEx + "|" + symbolsRegEx + "|" + operatorsRegEx + "|" + parensRegEx + "|" +
                        nonPrintableRegEx;

      return s.replaceAll(allRegEx, " ");
   }

   public static String stripNonPrintable(String s) {
      return s.replaceAll(nonPrintableRegEx, " ");
   }

   public static String stripOperators(String s) {
      return s.replaceAll(operatorsRegEx, " ");
   }

   public static String stripParens(String s) {
      return s.replaceAll(parensRegEx, " ");
   }

   public static String stripPunctuation(String s) {
      return s.replaceAll(punctuationRegEx, " ")
              .trim();
   }

   public static String stripSymbols(String s) {
      return s.replaceAll(symbolsRegEx, " ");
   }
}

