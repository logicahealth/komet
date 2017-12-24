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



package sh.isaac.pombuilder;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

//~--- classes ----------------------------------------------------------------

/**
 * {@link VersionFinder}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class VersionFinder {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * Note that while this finds the project version from either the metadata embedded in the jar that contains this, 
    * or from the pom.xml file, if we are running in eclipse - it will not return SNAPSHOT versions, rather, it 
    * removes -SNAPSHOT and decrements the versions by 1, to simplify testing in AITC.
    * @return the string
    */
   public static String findProjectVersion() {
      try (InputStream is =
            VersionFinder.class.getResourceAsStream("/META-INF/maven/sh.isaac.integration/db-config-builder/pom.xml");) {
         final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

         // added to avoid XXE injections
         domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

         final DocumentBuilder builder = domFactory.newDocumentBuilder();
         Document              dDoc;

         if (is != null) {
            dDoc = builder.parse(is);
         } else {
            dDoc = builder.parse(new File("pom.xml"));  // running in eclipse, this should work.
         }

         final XPath xPath = XPathFactory.newInstance()
                                         .newXPath();
         final String temp = ((Node) xPath.evaluate("/project/parent/version",
                                                    dDoc,
                                                    XPathConstants.NODE)).getTextContent();

         LOG.debug("VersionFinder finds {} (for the version of this release of the converter library)", temp);

         // Parse 3.42-SNAPSHOT and turn it into '3.41', so we don't write content converters or db builders with SNAPSHOT
         // refs that won't be resolvable in AITC

         try {
            if (temp.endsWith("-SNAPSHOT")) {
               String subString = temp.substring(0, temp.indexOf("-SNAPSHOT"));
               String[] parts = subString.split("\\.");

               int endDigit = Integer.parseInt(parts[parts.length - 1]);
               if (endDigit >= 0) {
                  endDigit--;
               }
               String replacement = "";
               for (int i = 0; i < parts.length - 1; i++) {
                  replacement += parts[i];
                  replacement += ".";
               }
               replacement += endDigit;
               LOG.debug("VersionFinder returns {} (for the version of this release of the converter library) after removing SNAPSHOT", replacement);
               return replacement;
            }
         } catch (Exception e) {
            LOG.error("Unexpected error trying to remove -SNAPSHOT from detected version number - returning found version number", e);
         }

         return temp;
      } catch (final Exception e) {
         throw new RuntimeException(e);
      }
   }
}

