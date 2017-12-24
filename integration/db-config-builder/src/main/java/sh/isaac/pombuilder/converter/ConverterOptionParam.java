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
   /** The Constant MAVEN_FILE_TYPE. */
   public static final String MAVEN_FILE_TYPE = "options.json";

   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The display name. */
   private String displayName;

   /** The internal name. */
   private String internalName;

   /** The description. */
   private String description;

   /** The allow no selection. */
   private boolean allowNoSelection;

   /** The allow multi select. */
   private boolean allowMultiSelect;

   /** The suggested pick list values. */
   private ConverterOptionParamSuggestedValue[] suggestedPickListValues;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new converter option param.
    */
   @SuppressWarnings("unused")
   private ConverterOptionParam() {
      // for jackson
   }

   /**
    * Instantiates a new converter option param.
    *
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

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
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

      final ConverterOptionParam other = (ConverterOptionParam) obj;

      if (this.allowNoSelection != other.allowNoSelection) {
         return false;
      }

      if (this.allowMultiSelect != other.allowMultiSelect) {
         return false;
      }

      if (this.description == null) {
         if (other.description != null) {
            return false;
         }
      } else if (!this.description.equals(other.description)) {
         return false;
      }

      if (this.displayName == null) {
         if (other.displayName != null) {
            return false;
         }
      } else if (!this.displayName.equals(other.displayName)) {
         return false;
      }

      if (this.internalName == null) {
         if (other.internalName != null) {
            return false;
         }
      } else if (!this.internalName.equals(other.internalName)) {
         return false;
      }

      if (!Arrays.equals(this.suggestedPickListValues, other.suggestedPickListValues)) {
         return false;
      }

      return true;
   }

   /**
    * Read the options specification from a json file found on the provided maven artifact server, with the provided artifact type.
    * May return an empty array, will not return null.
    *
    * @param artifact the artifact
    * @param baseMavenUrl the base maven url
    * @param mavenUsername the maven username
    * @param mavenPassword the maven password
    * @return the converter option param[]
    * @throws Exception the exception
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
         final URL pomURL = ArtifactUtilities.makeFullURL(baseMavenUrl,
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

         final File pomFile = dut.get();

         if (!pomFile.exists()) {
            LOG.debug("Throwing back an exception, as no pom was readable for the specified artifact");
            throw new Exception("Failed to find the pom file for the specified project");
         } else {
            pomFile.delete();
         }

         // Now that we know that the credentials / artifact / version are good - see if there is a config file (there may not be)
         try {
            final URL config = ArtifactUtilities.makeFullURL(baseMavenUrl,
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

            final File                   jsonFile = dut.get();
            final ConverterOptionParam[] temp     = fromFile(jsonFile);

            if (LOG.isDebugEnabled()) {
               LOG.debug("Found options: {}", Arrays.toString(temp));
            } else {
               LOG.info("Read {} options", temp.length);
            }

            jsonFile.delete();
            return temp;
         } catch (final Exception e) {
            // If we successfully downloaded the pom file, but failed here, just assume this file doesn't exist / isn't applicable to this converter.
            LOG.info("No config file found for converter " + artifact.getArtifactId());
            return new ConverterOptionParam[] {};
         }
      } finally {
         try {
            FileUtil.recursiveDelete(tempFolder);
         } catch (final Exception e) {
            LOG.error("Problem cleaning up temp folder {}", tempFolder, e);
         }
      }
   }

   /**
    * Read the options specification from a json file.
    *
    * @param jsonConverterOptionFile the json converter option file
    * @return the converter option param[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static ConverterOptionParam[] fromFile(File jsonConverterOptionFile)
            throws IOException {
      final ObjectMapper mapper = new ObjectMapper();

      return mapper.readValue(jsonConverterOptionFile, ConverterOptionParam[].class);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + (this.allowNoSelection ? 1231
            : 1237);
      result = prime * result + (this.allowMultiSelect ? 1231
            : 1237);
      result = prime * result + ((this.description == null) ? 0
            : this.description.hashCode());
      result = prime * result + ((this.displayName == null) ? 0
            : this.displayName.hashCode());
      result = prime * result + ((this.internalName == null) ? 0
            : this.internalName.hashCode());
      result = prime * result + Arrays.hashCode(this.suggestedPickListValues);
      return result;
   }

   /**
    * Serialize to json.
    *
    * @param options the options
    * @param outputFile the output file
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void serialize(ConverterOptionParam[] options, File outputFile)
            throws IOException {
      final ObjectMapper mapper = new ObjectMapper();

      try {
         mapper.writeValue(outputFile, options);
      } catch (final JsonProcessingException e) {
         throw new RuntimeException("Unexpected error", e);
      }
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ConverterOptionParam [displayName=" + this.displayName + ", internalName=" + this.internalName +
             ", description=" + this.description + ", allowNoSelection=" + this.allowNoSelection +
             ", allowMultiSelect=" + this.allowMultiSelect + ", suggestedPickListValues=" +
             Arrays.toString(this.suggestedPickListValues) + "]";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1.
    *
    * @return true, if allow multi select
    */
   public boolean isAllowMultiSelect() {
      return this.allowMultiSelect;
   }

   /**
    * true if it is valid for the user to select 0 entries from the pick list, false if they must select 1 or more.
    *
    * @return true, if allow no selection
    */
   public boolean isAllowNoSelection() {
      return this.allowNoSelection;
   }

   /**
    * The description of this option suitable to display to the end user, in a GUI.
    *
    * @return the description
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * The displayName of this option - suitable for GUI use to the end user.
    *
    * @return the display name
    */
   public String getDisplayName() {
      return this.displayName;
   }

   /**
    * The internalName of this option - use when creating the pom file.
    *
    * @return the internal name
    */
   public String getInternalName() {
      return this.internalName;
   }

   /**
    * Gets the suggested pick list values.
    *
    * @return the suggested pick list values
    */
   public ConverterOptionParamSuggestedValue[] getSuggestedPickListValues() {
      return this.suggestedPickListValues;
   }
}

