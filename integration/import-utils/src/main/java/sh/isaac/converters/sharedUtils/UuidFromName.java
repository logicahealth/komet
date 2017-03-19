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



package sh.isaac.converters.sharedUtils;

//~--- JDK imports ------------------------------------------------------------

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;

//~--- classes ----------------------------------------------------------------

/**
 * The Class UuidFromName.
 */
public class UuidFromName {
   /**
    * This is how to get a UUID that the WB expects from a string in a pom....
    *
    * @param args the arguments
    * @throws NoSuchAlgorithmException the no such algorithm exception
    * @throws UnsupportedEncodingException the unsupported encoding exception
    */
   public static void main(String[] args)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
      System.out.println(UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC,
            "VA JIF Terminology Workbench development path"));
      System.out.println(UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC,
            "VA JIF Terminology Workbench development origin"));
      System.out.println(UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC,
            "VA JIF Terminology Workbench release candidate path"));
      System.out.println(UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC, "Project Refsets"));
      System.out.println(UuidT3Generator.fromSNOMED(900000000000003001l));
      System.out.println(UuidT3Generator.fromSNOMED(900000000000013009l));
      System.out.println(UuidT3Generator.fromSNOMED(900000000000550004l));
   }
}

