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



package sh.isaac.converters.sharedUtils.umlsUtils;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

//~--- classes ----------------------------------------------------------------

/**
 * The Class AbbreviationExpansion.
 */
public class AbbreviationExpansion {
   /** The description. */
   String abbreviation, expansion, description;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new abbreviation expansion.
    *
    * @param abbreviation the abbreviation
    * @param expansion the expansion
    * @param description the description
    */
   protected AbbreviationExpansion(String abbreviation, String expansion, String description) {
      this.abbreviation = abbreviation;
      this.expansion    = expansion;
      this.description  = description;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Load.
    *
    * @param is the is
    * @return the hash map
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static HashMap<String, AbbreviationExpansion> load(InputStream is)
            throws IOException {
      final HashMap<String, AbbreviationExpansion> results = new HashMap<>();
      final BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
      String                                       line    = br.readLine();

      while (line != null) {
         if (StringUtils.isBlank(line) || line.startsWith("#")) {
            line = br.readLine();
            continue;
         }

         final String[]              cols = line.split("\t");
         final AbbreviationExpansion ae   = new AbbreviationExpansion(cols[0], cols[1], ((cols.length > 2) ? cols[2]
               : null));

         results.put(ae.getAbbreviation(), ae);
         line = br.readLine();
      }

      return results;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the abbreviation.
    *
    * @return the abbreviation
    */
   public String getAbbreviation() {
      return this.abbreviation;
   }

   /**
    * Gets the description.
    *
    * @return the description
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * Gets the expansion.
    *
    * @return the expansion
    */
   public String getExpansion() {
      return this.expansion;
   }
}

