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



package gov.va.rxnorm.solor;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URISyntaxException;

import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DoseFormMapping.
 */
public class DoseFormMapping {
   /**
    * Read dose form mapping.
    *
    * @return the stream
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws URISyntaxException the URI syntax exception
    */
   public static Stream<DoseForm> readDoseFormMapping()
            throws IOException, URISyntaxException {
      return new BufferedReader(new InputStreamReader(DoseFormMapping.class.getResourceAsStream("/10a1 Dose Form Mapping.txt"))).lines().map(line -> {
                    return line.split("\t");
                 }).filter(items -> {
                       return (((items.length >= 4) && StringUtils.isNumeric(items[3])) ? true
               : false);
                    }).map(items -> {
                    final DoseForm df = new DoseForm();

                    df.tty            = items[0];
                    df.rxcui          = items[1];
                    df.doseFormString = items[2];
                    df.sctid          = items[3];

                    if (items.length > 4) {
                       df.snomedString = items[4];
                    }

                    if (items.length > 5) {
                       df.comment = items[5];
                    }

                    return df;
                 });
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class DoseForm.
    */
   protected static class DoseForm {
      /** The comment. */
      String sctid, tty, rxcui, doseFormString, snomedString, comment;
   }
}

