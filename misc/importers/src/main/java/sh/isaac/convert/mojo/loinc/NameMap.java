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



package sh.isaac.convert.mojo.loinc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads in a file where key and value simply alternate, one per line.
 * Ignores lines starting with "#".
 *
 * Matching is case insensitive.
 *
 * Used to read in the classMappings files.
 *
 * @author Daniel Armbrust
 *
 */
public class NameMap {

   private final Hashtable<String, String> map_ = new Hashtable<String, String>();
   protected Logger log = LogManager.getLogger();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new name map.
    *
    * @param mapFileName the map file name
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public NameMap(String mapFileName)
            throws IOException {
      log.info("Using the class map file " + mapFileName);

      final BufferedReader in = new BufferedReader(new InputStreamReader(NameMap.class.getResourceAsStream("/loinc/" +
                                   mapFileName)));
      String key   = null;
      String value = null;

      for (String str = in.readLine(); str != null; str = in.readLine()) {
         final String temp = str.trim();

         if ((temp.length() > 0) &&!temp.startsWith("#")) {
            if (key == null) {
               key = temp;
            } else {
               value = temp;
            }
         }

         if (value != null) {
            final String old = this.map_.put(key.toLowerCase(), value);

            if ((old != null) &&!old.equals(value)) {
               log.error("Map file " + mapFileName + " has duplicate definition for " + key +
                                        ", but with different values!");
            }

            key   = null;
            value = null;
         }
      }

      in.close();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks for match.
    *
    * @param key the key
    * @return true, if successful
    */
   public boolean hasMatch(String key) {
      return this.map_.containsKey(key.toLowerCase());
   }

   /**
    * Returns the replacement value, or, if none, the value you passed in.
    *
    * @param key the key
    * @return the match value
    */
   public String getMatchValue(String key) {
      final String result = this.map_.get(key.toLowerCase());

      return ((result == null) ? key
                               : result);
   }
}

