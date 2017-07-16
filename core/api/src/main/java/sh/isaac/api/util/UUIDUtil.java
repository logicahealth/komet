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

import java.util.Optional;
import java.util.UUID;

//~--- classes ----------------------------------------------------------------

/**
 * Various UUID related utilities.
 *
 * @author darmbrust
 * @author kec
 */
public class UUIDUtil {
   /**
    * Convert.
    *
    * @param data the data
    * @return the uuid
    */
   public static UUID convert(long[] data) {
      return new UUID(data[0], data[1]);
   }

   /**
    * Convert.
    *
    * @param id the id
    * @return the long[]
    */
   public static long[] convert(UUID id) {
      final long[] data = new long[2];

      data[0] = id.getMostSignificantBits();
      data[1] = id.getLeastSignificantBits();
      return data;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the uuid.
    *
    * @param string the string
    * @return the uuid
    */
   public static Optional<UUID> getUUID(String string) {
      if (string == null) {
         return Optional.empty();
      }

      if (string.length() != 36) {
         return Optional.empty();
      }

      try {
         return Optional.of(UUID.fromString(string));
      } catch (final IllegalArgumentException e) {
         return Optional.empty();
      }
   }

   /**
    * Checks if uuid.
    *
    * @param string the string
    * @return true, if uuid
    */
   public static boolean isUUID(String string) {
      return (getUUID(string).isPresent());
   }
}

