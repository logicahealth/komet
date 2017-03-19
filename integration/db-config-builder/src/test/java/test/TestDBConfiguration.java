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



package test;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.pombuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.dbbuilder.DBConfigurationCreator;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TestDBConfiguration.
 */
public class TestDBConfiguration {
   /**
    * The main method.
    *
    * @param args the arguments
    * @throws Exception the exception
    */
   public static void main(String[] args)
            throws Exception {
      final String testURL  = "https://git.isaac.sh/git/r/dantest.git";
      final String username = "";
      final char[] password = "".toCharArray();

      System.setProperty("java.awt.headless", "true");

      // VHAT
      System.out.println(DBConfigurationCreator.createDBConfiguration("vhat-test",
            "2.0",
            "a test database",
            "all",
            true,
            new IBDFFile[] { new IBDFFile("sh.isaac.terminology.converted",
                  "vhat-ibdf",
                  "2016.01.07-loader-4.1-SNAPSHOT") },
            "3.03-SNAPSHOT",
            testURL,
            username,
            password));

      // VETS
      System.out.println(DBConfigurationCreator.createDBConfiguration("vets-test",
            "2.0",
            "a test database",
            "all",
            true,
            new IBDFFile[] { new IBDFFile("sh.isaac.terminology.converted",
                  "vhat-ibdf",
                  "2016.01.07-loader-4.1-SNAPSHOT"), new IBDFFile("sh.isaac.terminology.converted",
                        "loinc-ibdf",
                        "2.54-loader-5.1-SNAPSHOT"), new IBDFFile("sh.isaac.terminology.converted",
                              "rf2-ibdf-sct",
                              "20150731-loader-3.1-SNAPSHOT",
                              "Snapshot") },
            "3.03-SNAPSHOT",
            testURL,
            username,
            password));
   }
}

