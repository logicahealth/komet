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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticTags {
   
   private static final Pattern CONTAINS_SEM_TAG_PATTERN = Pattern.compile(".*[ ]\\([^(]+\\)$");
   private static final Pattern SEM_TAG_PATTERN = Pattern.compile("[ ]\\([^(]+\\)$");
   
   
   /**
    * Determine if a string already contains a semantic tag.
    * @param text The string to check
    * @return true, if a semantic tag per the regular expression .*[ ]\(.+\)$ is found in the text.
    */
   public static boolean containsSemanticTag(String text)
   {
      if (StringUtils.isEmpty(text)) {
         return false;
      }
      else {
         return CONTAINS_SEM_TAG_PATTERN.matcher(text).matches();
      }
   }
   
   /**
    * Add the specified semantic tag to the text IFF the specified text does not already contain a semantic tag per {@link #containsSemanticTag(String)}
    * 
    * @param text the text to append the semantic tag to
    * @param semanticTag the semantic tag to add.  The leading space, left paren, and right parent will all be added around this string if necessary.
    * @return The text with a properly formatted semantic tag.
    */
   public static String addSemanticTagIfAbsent(String text, String semanticTag)
   {
      if (containsSemanticTag(text))
      {
         return text;
      }
      
      StringBuilder temp = new StringBuilder(text.length() + semanticTag.length() + 3);
      temp.append(text);
      
      if (text.length() > 0 && text.charAt(text.length() - 1) != ' ')
      {
         temp.append(' ');
      }
      
      String semTagTemp = semanticTag.trim();
      
      if (semTagTemp.charAt(0) != '(')
      {
         temp.append('(');
      }
      
      temp.append(semTagTemp);
      
      if (semTagTemp.charAt(semTagTemp.length() - 1) != ')')
      {
         temp.append(')');
      }
      return temp.toString();
   }
   
   /**
    * Strip the semantic tag, if one is present in the given string
    * @param text the value to remove the semantic tag from
    * @return the initial text minus the semantic tag, or, simply the initial text.
    */
   public static String stripSemanticTagIfPresent(String text)
   {
      if (containsSemanticTag(text))
      {
         Matcher m = SEM_TAG_PATTERN.matcher(text);
         boolean found = m.find();
         if (!found)
         {
            throw new RuntimeException("oops");
         }
         return text.substring(0, m.start());
      }
      return text;
   }
   
   /**
    * Return the semantic tag only, if present, without surrounding parens.
    * @param text the value to find a semantic tag in.
    * @return the semantic tag from the initial text minus surrounding parens.
    */
   public static Optional<String> findSemanticTagIfPresent(String text)
   {
      if (containsSemanticTag(text))
      {
         Matcher m = SEM_TAG_PATTERN.matcher(text);
         boolean found = m.find();
         if (!found)
         {
            throw new RuntimeException("oops");
         }
         String temp = text.substring(m.start(), text.length());
         if (temp.length() < 3)
         {
            throw new RuntimeException("oops");
         }
         return Optional.of(temp.substring(2, temp.length() - 1));
      }
      return Optional.empty();
   }
}
