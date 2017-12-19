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

import java.io.UnsupportedEncodingException;

import java.util.UUID;

//~--- classes ----------------------------------------------------------------

/**
 * The Class UuidT3Generator generates a type 3 UUID object. A type 3 UUID is
 * name based and uses MD5 hashing to create the uuid from the given name. This
 * generator should only be used for SNOMED Ids, all other users should use
 * {@code UuidT5Generator}
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Universally_unique_identifier">http://en.wikipedia.org/wiki/Universally_unique_identifier</a>
 */
public class UuidT3Generator {
   /**
    * The the ENCODING_FOR_UUID_GENERATION string. ISO-8859-1 is (according to the standards at least)
    * the default encoding of documents delivered via HTTP with a MIME type beginning with "text/".
    */
   public static final String ENCODING_FOR_UUID_GENERATION = "8859_1";

   //~--- methods -------------------------------------------------------------

   /**
    * Generates a type 3 UUID from the given SNOMED id.
    *
    * @param id the SNOMED id
    * @return the generated uuid
    */
   public static UUID fromSNOMED(long id) {
      return fromSNOMED(Long.toString(id));
   }

   /**
    * Generates a type 3 UUID from the given SNOMED id.
    *
    * @param id the SNOMED id
    * @return the generated uuid
    */
   public static UUID fromSNOMED(Long id) {
      return fromSNOMED(id.toString());
   }

   /**
    * Generates a type 3 UUID from the given string representing a SNOMED id.
    *
    * @param id a String representation of a SNOMED id
    * @return the generated uuid
    */
   public static UUID fromSNOMED(String id) {
      final String name = "org.snomed." + id;

      try {
         return UUID.nameUUIDFromBytes(name.getBytes(ENCODING_FOR_UUID_GENERATION));
      } catch (final UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Simple utility to generate a UUID for a SNOMED id.
    * @param args not used.
    */
   public static void main(String[] args) {
      String snomedId = "30561011000036101";

      System.out.println("snomedId: " + snomedId + " uuid: " + fromSNOMED(snomedId));
   }
}

