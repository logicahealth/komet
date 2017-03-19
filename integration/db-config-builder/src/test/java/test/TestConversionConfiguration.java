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

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.pombuilder.GitPublish;
import sh.isaac.pombuilder.artifacts.Converter;
import sh.isaac.pombuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.artifacts.SDOSourceContent;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TestConversionConfiguration.
 */
public class TestConversionConfiguration {
   /**
    * The main method.
    *
    * @param args the arguments
    * @throws Exception the exception
    */
   public static void main(String[] args)
            throws Exception {
      final String gitTestURL    = "https://git.isaac.sh/git/";
      final String gitUsername   = "";
      final char[] gitPassword   = "".toCharArray();
      final String nexusUrl      = "https://artifactory.isaac.sh/artifactory/all";
      final String nexusUsername = "";
      final String nexusPassword = "";

      System.setProperty("java.awt.headless", "true");
      System.out.println(GitPublish.readTags(gitTestURL, gitUsername, gitPassword));

      // vhat
//    System.out.println(ContentConverterCreator.createContentConverter(new SDOSourceContent("sh.isaac.terminology.source.vhat", "vhat-src-data", "2016.01.07"), 
//            "4.1-SNAPSHOT", new SDOSourceContent[0], new IBDFFile[0], null, gitTestURL, gitUsername, gitPassword));
      // loinc
//    System.out.println(ContentConverterCreator.createContentConverter(new SDOSourceContent("sh.isaac.terminology.source.loinc", "loinc-src-data", "2.54"), 
//            "5.1-SNAPSHOT", new SDOSourceContent[0], new IBDFFile[0], null, gitTestURL, gitUsername, gitPassword));
      // loinc-tech-preview
//    System.out.println(ContentConverterCreator.createContentConverter(new SDOSourceContent("sh.isaac.terminology.source.loinc", "loinc-src-data-tech-preview", "2015.08.01"), 
//            "5.1-SNAPSHOT", 
//            new SDOSourceContent[] {new SDOSourceContent("sh.isaac.terminology.source.loinc", "loinc-src-data", "2.54")}, 
//            new IBDFFile[] {new IBDFFile("sh.isaac.terminology.converted", "rf2-ibdf-sct", "20150731-loader-3.1-SNAPSHOT", "Snapshot")},
//            null, gitTestURL, gitUsername, gitPassword));
//    //sct
//    System.out.println(ContentConverterCreator.createContentConverter(new SDOSourceContent("sh.isaac.terminology.source.rf2", "rf2-src-data-sct", "20150731"), 
//            "3.1-SNAPSHOT", new SDOSourceContent[0], new IBDFFile[0], null, gitTestURL, gitUsername, gitPassword));
      // sct-us-ext
      final ConverterOptionParam[] optionTypes =
         ContentConverterCreator.getConverterOptions(new Converter("sh.isaac.terminology.converters",
                                                                   "rf2-mojo",
                                                                   "3.3-SNAPSHOT"),
                                                     nexusUrl,
                                                     nexusUsername,
                                                     nexusPassword);
      final HashMap<ConverterOptionParam, Set<String>> options = new HashMap<>();

      for (final ConverterOptionParam x: optionTypes) {
         if (x.getInternalName()
              .equals("moduleUUID")) {
            options.put(x, new HashSet<String>(Arrays.asList(new String[] { "c82efad7-f4bf-5e81-b223-b5b0305f6652" })));
         }
      }

      System.out.println(
          ContentConverterCreator.createContentConverter(new SDOSourceContent("sh.isaac.terminology.source.rf2",
                "rf2-src-data-us-extension",
                "20150301"),
                "3.3-SNAPSHOT",
                new SDOSourceContent[0],
                new IBDFFile[] { new IBDFFile("sh.isaac.terminology.converted",
                      "rf2-ibdf-sct",
                      "20150731-loader-3.3-SNAPSHOT",
                      "Snapshot") },
                options,
                gitTestURL,
                gitUsername,
                gitPassword));

      // rxnorm
//    ConverterOptionParam[] optionTypes = ContentConverterCreator.getConverterOptions(new Converter("sh.isaac.terminology.converters", "rxnorm-mojo", "5.1-SNAPSHOT"), 
//                    nexusUrl, nexusUsername, nexusPassword);
//    
//    HashMap<ConverterOptionParam, Set<String>> options = new HashMap<>();
//    for (ConverterOptionParam x : optionTypes)
//    {
//            if (x.getInternalName().equals("ttyRestriction"))
//            {
//                    options.put(x, new HashSet<String>(Arrays.asList(new String[] {"IN", "SCD"})));
//            }
//            else if (x.getInternalName().equals("sabsToInclude"))
//            {
//                    options.put(x, new HashSet<String>(Arrays.asList(new String[] {"ATC"})));
//            }
//    }
//    
//    System.out.println(ContentConverterCreator.createContentConverter(new SDOSourceContent("sh.isaac.terminology.source.rxnorm", "rxnorm-src-data", "2016.05.02"), 
//    "5.1-SNAPSHOT", 
//    new SDOSourceContent[0], 
//    new IBDFFile[] {new IBDFFile("sh.isaac.terminology.converted", "rf2-ibdf-sct", "20150731-loader-3.1-SNAPSHOT", "Snapshot")},
//            options,
//            gitTestURL, gitUsername, gitPassword));
   }
}

