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



package sh.isaac.api.util.metainf;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MavenArtifactInfo}
 *
 * This class carries Maven dependency information.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
public class MavenArtifactInfo {
   /** Database Licenses. */
   public List<MavenLicenseInfo> dbLicenses = new ArrayList<>();

   /**
    * The source content that was built into the underlying database.
    */
   public List<MavenArtifactInfo> dbDependencies = new ArrayList<>();

   /** Maven Dependency Group ID. */
   public String groupId;

   /** Maven Dependency Artifact ID. */
   public String artifactId;

   /** Maven Dependency Version. */
   public String version;

   /** Maven Dependency Classifier. */
   public String classifier;

   /** Maven Dependency Type. */
   public String type;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new maven artifact info.
    */
   public MavenArtifactInfo() {}

   /**
    * Instantiates a new maven artifact info.
    *
    * @param groupId Maven Dependency Group ID
    * @param artifactId Maven Dependency Artifact ID
    * @param version Maven Dependency Version
    * @param classifier Maven Dependency Classifier
    * @param type Maven Dependency Type
    */
   public MavenArtifactInfo(String groupId, String artifactId, String version, String classifier, String type) {
      super();
      setValues(groupId, artifactId, version, classifier, type);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("MavenArtifactInfo\r\n");
      sb.append("  groupId=" + this.groupId + "\r\n");
      sb.append("  artifactId=" + this.artifactId + "\r\n");
      sb.append("  version=" + this.version + "\r\n");
      sb.append("  classifier=" + this.classifier + "\r\n");
      sb.append("  type=" + this.type + "\r\n");
      sb.append("\r\n");
      sb.append("Licenses\r\n");
      this.dbLicenses.forEach(license -> sb.append("  " + license.toString() + "\r\n"));
      sb.append("\r\n");
      sb.append("Database Dependencies\r\n");
      this.dbDependencies.forEach(dbDependendy -> sb.append("  " + dbDependendy.toString() + "\r\n"));
      return sb.toString();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set values.
    *
    * @param groupId Maven Dependency Group ID
    * @param artifactId Maven Dependency Artifact ID
    * @param version Maven Dependency Version
    * @param classifier Maven Dependency Classifier
    * @param type Maven Dependency Type
    */
   public void setValues(String groupId, String artifactId, String version, String classifier, String type) {
      this.groupId    = groupId;
      this.artifactId = artifactId;
      this.version    = version;
      this.classifier = classifier;
      this.type       = type;
   }
}

