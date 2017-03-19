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

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ArtifactUtilities.
 */
public class ArtifactUtilities {
   /**
    * The main method.
    *
    * @param args the arguments
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void main(String[] args)
            throws InterruptedException, ExecutionException, IOException {
      try {
         final String username = "foo";
         final String userpd   = "foo";

         LookupService.startupWorkExecutors();

         final URL release = new URL("http://artifactory.isaac.sh/artifactory/libs-release-local" +
                                     makeMavenRelativePath("aopalliance",
                                           "aopalliance",
                                           "1.0",
                                           null,
                                           "jar"));
         Task<File> task = new DownloadUnzipTask(null, null, release, false, true, null);

         Get.workExecutors()
            .getExecutor()
            .submit(task);

         File foo = task.get();

         System.out.println(foo.getCanonicalPath());
         foo.delete();
         foo.getParentFile()
            .delete();

         final File where = new File("").getAbsoluteFile();
         URL snapshot = new URL("http://artifactory.isaac.sh/artifactory/libs-snapshot" +
                                makeMavenRelativePath("http://artifactory.isaac.sh/artifactory/libs-snapshot",
                                      username,
                                      userpd,
                                      "sh.isaac.db",
                                      "vhat",
                                      "2016.01.07-1.0-SNAPSHOT",
                                      "all",
                                      "cradle.zip"));

         task = new DownloadUnzipTask(username, userpd, snapshot, true, true, where);
         Get.workExecutors()
            .getExecutor()
            .submit(task);
         foo = task.get();
         snapshot = new URL("http://artifactory.isaac.sh/artifactory/libs-snapshot" +
                            makeMavenRelativePath("http://artifactory.isaac.sh/artifactory/libs-snapshot",
                                  username,
                                  userpd,
                                  "sh.isaac.db",
                                  "vhat",
                                  "2016.01.07-1.0-SNAPSHOT",
                                  "all",
                                  "lucene.zip"));
         task = new DownloadUnzipTask(username, userpd, snapshot, true, true, where);
         Get.workExecutors()
            .getExecutor()
            .submit(task);
         foo = task.get();
         System.out.println(foo.getCanonicalPath());
      } catch (final Exception e) {
         e.printStackTrace();
      }

      Platform.exit();
   }

   /**
    * Make full URL.
    *
    * @param baseMavenURL the base maven URL
    * @param mavenUsername - optional - only used for a SNAPSHOT dependency
    * @param mavenPassword - optional - only used for a SNAPSHOT dependency
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    * @param classifier - optional
    * @param type the type
    * @return the url
    * @throws Exception the exception
    */
   public static URL makeFullURL(String baseMavenURL,
                                 String mavenUsername,
                                 String mavenPassword,
                                 String groupId,
                                 String artifactId,
                                 String version,
                                 String classifier,
                                 String type)
            throws Exception {
      return new URL(baseMavenURL + (baseMavenURL.endsWith("/") ? ""
            : "/") + makeMavenRelativePath(baseMavenURL,
                  mavenUsername,
                  mavenPassword,
                  groupId,
                  artifactId,
                  version,
                  classifier,
                  type));
   }

   /**
    * Make maven relative path.
    *
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    * @param classifier the classifier
    * @param type the type
    * @return the string
    */
   public static String makeMavenRelativePath(String groupId,
         String artifactId,
         String version,
         String classifier,
         String type) {
      if (version.endsWith("-SNAPSHOT")) {
         throw new RuntimeException(
             "Cannot create a valid path to a -SNAPSHOT url without downloading the corresponding maven-metadata.xml file.");
      }

      try {
         return makeMavenRelativePath(null, null, null, groupId, artifactId, version, classifier, type);
      } catch (final Exception e) {
         throw new RuntimeException("Unexpected", e);
      }
   }

   /**
    * Make maven relative path.
    *
    * @param baseMavenURL - optional - but required if you are downloading a SNAPSHOT dependency, as this method will need to download the metadata file
    * from the repository server in order to determine the proper version component for the SNAPSHOT.
    * @param mavenUsername - optional - only used for a SNAPSHOT dependency
    * @param mavenPassword - optional - only used for a SNAPSHOT dependency
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    * @param classifier - optional
    * @param type the type
    * @return the string
    * @throws Exception the exception
    */
   public static String makeMavenRelativePath(String baseMavenURL,
         String mavenUsername,
         String mavenPassword,
         String groupId,
         String artifactId,
         String version,
         String classifier,
         String type)
            throws Exception {
      final String temp                   = groupId.replaceAll("\\.", "/");
      String       snapshotVersion        = "";
      String       versionWithoutSnapshot = version;

      if (version.endsWith("-SNAPSHOT")) {
         versionWithoutSnapshot = version.substring(0, version.lastIndexOf("-SNAPSHOT"));

         final URL metadataUrl = new URL(baseMavenURL + (baseMavenURL.endsWith("/") ? ""
               : "/") + temp + "/" + artifactId + "/" + version + "/maven-metadata.xml");

         // Need to download the maven-metadata.xml file
         final Task<File> task = new DownloadUnzipTask(mavenUsername, mavenPassword, metadataUrl, false, false, null);

         WorkExecutors.get()
                      .getExecutor()
                      .execute(task);

         final File                   metadataFile = task.get();
         final DocumentBuilderFactory domFactory   = DocumentBuilderFactory.newInstance();

         // added to avoid XXE injections
         domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

         DocumentBuilder builder;
         Document        dDoc  = null;
         final XPath     xPath = XPathFactory.newInstance()
                                             .newXPath();

         builder = domFactory.newDocumentBuilder();
         dDoc    = builder.parse(metadataFile);

         final String timestamp = ((Node) xPath.evaluate("/metadata/versioning/snapshot/timestamp",
                                                         dDoc,
                                                         XPathConstants.NODE)).getTextContent();
         final String buildNumber = ((Node) xPath.evaluate("/metadata/versioning/snapshot/buildNumber",
                                                           dDoc,
                                                           XPathConstants.NODE)).getTextContent();

         snapshotVersion = "-" + timestamp + "-" + buildNumber;
         metadataFile.delete();

         // The download task makes a subfolder in temp for this, delete that too
         metadataFile.getParentFile()
                     .delete();
      }

      return temp + "/" + artifactId + "/" + version + "/" + artifactId + "-" + versionWithoutSnapshot +
             snapshotVersion + (StringUtils.isNotBlank(classifier) ? "-" + classifier
            : "") + "." + type;
   }
}

