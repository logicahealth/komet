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



package sh.isaac.api.chronicle;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;

import java.util.Locale;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

//~--- enums ------------------------------------------------------------------

/**
 *
 * @author kec
 */
public enum ObjectChronologyType {
   CONCEPT("Concept"),
   SEMEME("Sememe"),
   UNKNOWN_NID("Unknown");

   private String niceName_;

   //~--- constructors --------------------------------------------------------

   private ObjectChronologyType(String niceName) {
      niceName_ = niceName;
   }

   //~--- methods -------------------------------------------------------------

   public static ObjectChronologyType parse(String nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
         return null;
      }

      String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH)
                                 .trim();

      if (StringUtils.isBlank(clean)) {
         return null;
      }

      for (ObjectChronologyType ct: values()) {
         if (ct.name().toLowerCase(Locale.ENGLISH).equals(clean) ||
               ct.niceName_.toLowerCase(Locale.ENGLISH).equals(clean) ||
               (ct.ordinal() + "").equals(clean)) {
            return ct;
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine ObjectChronologyType from " + nameOrEnumId);
      }

      return UNKNOWN_NID;
   }

   /**
    * @see java.lang.Enum#toString()
    */
   @Override
   public String toString() {
      return niceName_;
   }
}

