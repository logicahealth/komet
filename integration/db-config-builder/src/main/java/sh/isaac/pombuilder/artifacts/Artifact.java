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



package sh.isaac.pombuilder.artifacts;

/**
 *
 * {@link Artifact}
 * A base class for providing artifact information to the config builder tool.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class Artifact {
   /** The group id. */
   private final String groupId;

   /** The artifact id. */
   private final String artifactId;

   /** The version. */
   private final String version;

   /** The classifier. */
   private final String classifier;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new artifact.
    *
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    */
   public Artifact(String groupId, String artifactId, String version) {
      this(groupId, artifactId, version, null);
   }

   /**
    * Instantiates a new artifact.
    *
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    * @param classifier the classifier
    */
   public Artifact(String groupId, String artifactId, String version, String classifier) {
      this.groupId    = groupId;
      this.artifactId = artifactId;
      this.version    = version;
      this.classifier = classifier;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "Artifact groupId=" + this.groupId + ", artifactId=" + this.artifactId + ", version=" +
             this.version + ", classifier=" + (this.classifier == null ? "" : this.classifier) + "]";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the artifact id.
    *
    * @return the artifact id
    */
   public String getArtifactId() {
      return this.artifactId;
   }

   /**
    * Gets the classifier.
    *
    * @return the classifier
    */
   public String getClassifier() {
      return this.classifier;
   }

   /**
    * Checks for classifier.
    *
    * @return true, if successful
    */
   public boolean hasClassifier() {
      if ((this.classifier == null) || (this.classifier.trim().length() == 0)) {
         return false;
      }

      return true;
   }

   /**
    * Gets the group id.
    *
    * @return the group id
    */
   public String getGroupId() {
      return this.groupId;
   }

   /**
    * Gets the version.
    *
    * @return the version
    */
   public String getVersion() {
      return this.version;
   }
}

