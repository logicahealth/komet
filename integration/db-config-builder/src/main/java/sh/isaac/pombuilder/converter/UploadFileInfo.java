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

/**
 * {@link UploadFileInfo}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UploadFileInfo {
   
   /** The suggested source location. */
   private final String  suggestedSourceLocation;
   
   /** The suggested source URL. */
   private final String  suggestedSourceURL;
   
   /** The sample name. */
   private final String  sampleName;
   
   /** The expected naming pattern description. */
   private final String  expectedNamingPatternDescription;
   
   /** The expected naming pattern reg exp pattern. */
   private final String  expectedNamingPatternRegExpPattern;
   
   /** The file is required. */
   private final boolean fileIsRequired;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new upload file info.
    *
    * @param suggestedSourceLocation the suggested source location
    * @param suggestedSourceURL the suggested source URL
    * @param sampleName the sample name
    * @param expectedNamingPatternDescription the expected naming pattern description
    * @param expectedNamingPatternRegExpPattern the expected naming pattern reg exp pattern
    * @param fileIsRequired the file is required
    */
   protected UploadFileInfo(String suggestedSourceLocation,
                            String suggestedSourceURL,
                            String sampleName,
                            String expectedNamingPatternDescription,
                            String expectedNamingPatternRegExpPattern,
                            boolean fileIsRequired) {
      this.suggestedSourceLocation            = suggestedSourceLocation;
      this.suggestedSourceURL                 = suggestedSourceURL;
      this.sampleName                         = sampleName;
      this.expectedNamingPatternDescription   = expectedNamingPatternDescription;
      this.expectedNamingPatternRegExpPattern = expectedNamingPatternRegExpPattern;
      this.fileIsRequired                     = fileIsRequired;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * File is required.
    *
    * @return true, if successful
    */
   public boolean fileIsRequired() {
      return this.fileIsRequired;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the expected naming pattern description.
    *
    * @return the expected naming pattern description
    */
   public String getExpectedNamingPatternDescription() {
      return this.expectedNamingPatternDescription;
   }

   /**
    * Gets the expected naming pattern reg exp pattern.
    *
    * @return the expected naming pattern reg exp pattern
    */
   public String getExpectedNamingPatternRegExpPattern() {
      return this.expectedNamingPatternRegExpPattern;
   }

   /**
    * Gets the sample name.
    *
    * @return the sample name
    */
   public String getSampleName() {
      return this.sampleName;
   }

   /**
    * This is not always populated - it will typically only be populated if {@link #getSuggestedSourceURL()} is NOT populated.
    *
    * @return the suggested source location
    */
   public String getSuggestedSourceLocation() {
      return this.suggestedSourceLocation;
   }

   /**
    * This is not always populated - it will typically only be populated if {@link #getSuggestedSourceLocation()} is NOT populated.
    *
    * @return the suggested source URL
    */
   public String getSuggestedSourceURL() {
      return this.suggestedSourceURL;
   }
}

