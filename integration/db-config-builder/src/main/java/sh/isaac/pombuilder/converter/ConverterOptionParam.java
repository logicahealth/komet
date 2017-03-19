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



package sh.isaac.pombuilder.converter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Arrays;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sh.isaac.api.util.ArtifactUtilities;
import sh.isaac.api.util.DownloadUnzipTask;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.artifacts.Converter;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link ConverterOptionParam}
 *
 * The set of options that apply to a particular converter. Converters build this object, and serialize it to json, and publish it to maven.
 * Consumers (the GUI) read the json file, and pass it here, or ask us to read the json file and parse it.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConverterOptionParam {
   public static final String  MAVEN_FILE_TYPE = "options.json";
   private static final Logger LOG             = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private String                               displayName;
   private String                               internalName;
   private String                               description;
   private boolean                              allowNoSelection;
   private boolean                              allowMultiSelect;
   private ConverterOptionParamSuggestedValue[] suggestedPickListValues;

   //~--- constructors --------------------------------------------------------

   @SuppressWarnings("unused")
   private ConverterOptionParam() {
      // for jackson
   }

   /**
    * @param displayName The name of this option
    * @param internalName The name to use when writing the option to a pom file
    * @param description A description suitable for display to end users of the system (in the GUI)
    * @param allowNoSelection true if it is valid for the user to select 0 entries from the pick list, false if they must select 1 or more.
    * @param allowMultiSelect true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1.
    * @param suggestedPickListValues the values to provide the user to select from. This may not be an all-inclusive list of values - the
    * user should still have the option to provide their own value.
    */
   @SafeVarargs
   public ConverterOptionParam(String displayName,
                               String internalName,
                               String description,
                               boolean allowNoSelection,
                               boolean allowMultiSelect,
                               ConverterOptionParamSuggestedValue... suggestedPickListValues) {
      this.displayName             = displayName;
      this.internalName            = internalName;
      this.description             = description;
      this.allowNoSelection        = allowNoSelection;
      this.allowMultiSelect        = allowMultiSelect;
      this.suggestedPickListValues = suggestedPickListValues;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      ConverterOptionParam other = (ConverterOptionParam) obj;

      if (allowNoSelection != other.allowNoSelection) {
         return false;
      }

      if (allowMultiSelect != other.allowMultiSelect) {
         return false;
      }

      if (description == null) {
         if (other.description != null) {
            return false;
         }
      } else if (!description.equals(other.description)) {
         return false;
      }

      if (displayName == null) {
         if (other.displayName != null) {
            return false;
         }
      } else if (!displayName.equals(other.displayName)) {
         return false;
      }

      if (internalName == null) {
         if (other.internalName != null) {
            return false;
         }
      } else if (!internalName.equals(other.internalName)) {
         return false;
      }

      if (!Arrays.equals(suggestedPickListValues, other.suggestedPickListValues)) {
         return false;
      }

      return true;
   }

   /**
    * Read the options specification from a json file found on the provided maven artifact server, with the provided artifact type.
    * May return an empty array, will not return null.
    * @throws Exception
    */
   public static ConverterOptionParam[] fromArtifact(Converter artifact,
         String baseMavenUrl,
         String mavenUsername,
         String mavenPassword)
            throws Exception {
      File tempFolder = null;

      try {
         LOG.debug("Trying to read 'options.json' for {} from '{}'", artifact, baseMavenUrl);
         tempFolder = File.createTempFile("jsonDownload", "");
         tempFolder.delete();
         tempFolder.mkdir();

         // First, try to get the pom file to validate the params they sent us.  If this fails, they sent bad info, and we fail.
         URL pomURL = ArtifactUtilities.makeFullURL(baseMavenUrl,
                                                    mavenUsername,
                                                    mavenPassword,
                                                    artifact.getGroupId(),
                                                    artifact.getArtifactId(),
                                                    artifact.getVersion(),
                                                    artifact.getClassifier(),
                                                    "pom");
         DownloadUnzipTask dut = new DownloadUnzipTask(mavenUsername, mavenPassword, pomURL, false, true, tempFolder);

         WorkExecutors.get()
                      .getExecutor()
                      .execute(dut);

         File pomFile = dut.get();

         if (!pomFile.exists()) {
            LOG.debug("Throwing back an exception, as no pom was readable for the specified artifact");
            throw new Exception("Failed to find the pom file for the specified project");
         } else {
            pomFile.delete();
         }

         // Now that we know that the credentials / artifact / version are good - see if there is a config file (there may not be)
         try {
            URL config = ArtifactUtilities.makeFullURL(baseMavenUrl,
                                                       mavenUsername,
                                                       mavenPassword,
                                                       artifact.getGroupId(),
                                                       artifact.getArtifactId(),
                                                       artifact.getVersion(),
                                                       artifact.getClassifier(),
                                                       MAVEN_FILE_TYPE);

            dut = new DownloadUnzipTask(mavenUsername, mavenPassword, config, false, true, tempFolder);
            WorkExecutors.get()
                         .getExecutor()
                         .execute(dut);

            File                   jsonFile = dut.get();
            ConverterOptionParam[] temp     = fromFile(jsonFile);

            if (LOG.isDebugEnabled()) {
               LOG.debug("Found options: {}", Arrays.toString(temp));
            } else {
               LOG.info("Read {} options", temp.length);
            }

            jsonFile.delete();
            return temp;
         } catch (Exception e) {
            // If we successfully downloaded the pom file, but failed here, just assume this file doesn't exist / isn't applicable to this converter.
            LOG.info("No config file found for converter " + artifact.getArtifactId());
            return new ConverterOptionParam[] {};
         }
      } finally {
         try {
            FileUtil.recursiveDelete(tempFolder);
         } catch (Exception e) {
            LOG.error("Problem cleaning up temp folder " + tempFolder, e);
         }
      }
   }

   /**
    * Read the options specification from a json file.
    */
   public static ConverterOptionParam[] fromFile(File jsonConverterOptionFile)
            throws IOException {
      ObjectMapper mapper = new ObjectMapper();

      return mapper.readValue(jsonConverterOptionFile, ConverterOptionParam[].class);
   }

   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + (allowNoSelection ? 1231
            : 1237);
      result = prime * result + (allowMultiSelect ? 1231
            : 1237);
      result = prime * result + ((description == null) ? 0
            : description.hashCode());
      result = prime * result + ((displayName == null) ? 0
            : displayName.hashCode());
      result = prime * result + ((internalName == null) ? 0
            : internalName.hashCode());
      result = prime * result + Arrays.hashCode(suggestedPickListValues);
      return result;
   }

   /**
    * Serialize to json
    *
    * @throws IOException
    */
   public static void serialize(ConverterOptionParam[] options, File outputFile)
            throws IOException {
      ObjectMapper mapper = new ObjectMapper();

      try {
         mapper.writeValue(outputFile, options);
      } catch (JsonProcessingException e) {
         throw new RuntimeException("Unexpected error", e);
      }
   }

   @Override
   public String toString() {
      return "ConverterOptionParam [displayName=" + displayName + ", internalName=" + internalName + ", description=" +
             description + ", allowNoSelection=" + allowNoSelection + ", allowMultiSelect=" + allowMultiSelect +
             ", suggestedPickListValues=" + Arrays.toString(suggestedPickListValues) + "]";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1.
    */
   public boolean isAllowMultiSelect() {
      return allowMultiSelect;
   }

   /**
    * true if it is valid for the user to select 0 entries from the pick list, false if they must select 1 or more.
    */
   public boolean isAllowNoSelection() {
      return allowNoSelection;
   }

   /**
    * The description of this option suitable to display to the end user, in a GUI.
    */
   public String getDescription() {
      return description;
   }

   /**
    * The displayName of this option - suitable for GUI use to the end user
    */
   public String getDisplayName() {
      return displayName;
   }

   /**
    * The internalName of this option - use when creating the pom file
    */
   public String getInternalName() {
      return internalName;
   }

   /**
    * @param suggestedPickListValues the suggested values to provide the user to select from. This may not be an all-inclusive list of values - the
    * user should still have the option to provide their own value.
    */
   public ConverterOptionParamSuggestedValue[] getSuggestedPickListValues() {
      return suggestedPickListValues;
   }
}

