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



package sh.isaac.convert.rf2.mojo;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class VerifyIbdfVersionFormat
         implements EnforcerRule {
   public static final String ARTIFACT_SUFFIX    = "-ibdf";
   public static final String SOURCE_DATA_SUFFIX = "-rf2";

   //~--- methods -------------------------------------------------------------

   @Override
   public void execute(EnforcerRuleHelper helper)
            throws EnforcerRuleException {

      try {
         // get the various expressions out of the helper.
         MavenProject project              = (MavenProject) helper.evaluate("${project}");
         String       artifactId           = project.getArtifactId();
         String       version              = project.getVersion();
         String       sourceDataVersion    = project.getProperties()
                                                    .getProperty("sourceData.version");
         String       sourceDataArtifactId = project.getProperties()
                                                    .getProperty("sourceData.artifactId");
         String       loaderVersion        = project.getProperties()
                                                    .getProperty("loader.version");

         if (!artifactId.endsWith(ARTIFACT_SUFFIX)) {
            throw new EnforcerRuleException(
                "To follow convention, the artifact id must end in: " + ARTIFACT_SUFFIX + " found: " + artifactId);
         }

         if (!sourceDataArtifactId.endsWith(SOURCE_DATA_SUFFIX)) {
            throw new EnforcerRuleException(
                "To follow convention, the source data artifact id must end in: " + SOURCE_DATA_SUFFIX + " found: " +
                sourceDataArtifactId);
         }
         if (!version.startsWith(sourceDataVersion)) {
            throw new EnforcerRuleException(
                "To follow convention, the version must start with the source data version: " + sourceDataVersion + " found: " +
                version);
         }
         if (!version.endsWith(loaderVersion)) {
            throw new EnforcerRuleException(
                "To follow convention, the version must end with the loader version: " + loaderVersion + " found: " +
                version);
         }
         
         String constructedVersionStart = sourceDataVersion + "-loader-" + loaderVersion;
         if (!version.startsWith(constructedVersionStart)) {
            throw new EnforcerRuleException(
                "To follow convention, the version must start with: " + constructedVersionStart + " found: " +
                version);
         }

      } catch (ExpressionEvaluationException e) {
         throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getCacheId() {
      return this.getClass()
                 .getTypeName();
   }

   @Override
   public boolean isCacheable() {
      return false;
   }

   @Override
   public boolean isResultValid(EnforcerRule er) {
      return false;
   }
}

