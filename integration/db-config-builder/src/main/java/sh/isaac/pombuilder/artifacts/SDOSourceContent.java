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
 * {@link SDOSourceContent}
 * An artifact that points to a zip file containing native SDO terminology content, such as a set of RF2 files, or the VETs xml file.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SDOSourceContent
        extends Artifact {
   /**
    * Instantiates a new SDO source content.
    *
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    */
   public SDOSourceContent(String groupId, String artifactId, String version) {
      this(groupId, artifactId, version, null);
   }

   /**
    * Instantiates a new SDO source content.
    *
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    * @param classifier the classifier
    */
   public SDOSourceContent(String groupId, String artifactId, String version, String classifier) {
      super(groupId, artifactId, version, classifier);
   }
}

