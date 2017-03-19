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
 *
 * {@link UploadFileInfo}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UploadFileInfo {
   private final String  suggestedSourceLocation;
   private final String  suggestedSourceURL;
   private final String  sampleName;
   private final String  expectedNamingPatternDescription;
   private final String  expectedNamingPatternRegExpPattern;
   private final boolean fileIsRequired;

   //~--- constructors --------------------------------------------------------

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

   public boolean fileIsRequired() {
      return this.fileIsRequired;
   }

   //~--- get methods ---------------------------------------------------------

   public String getExpectedNamingPatternDescription() {
      return this.expectedNamingPatternDescription;
   }

   public String getExpectedNamingPatternRegExpPattern() {
      return this.expectedNamingPatternRegExpPattern;
   }

   public String getSampleName() {
      return this.sampleName;
   }

   /**
    * This is not always populated - it will typically only be populated if {@link #getSuggestedSourceURL()} is NOT populated.
    */
   public String getSuggestedSourceLocation() {
      return this.suggestedSourceLocation;
   }

   /**
    * This is not always populated - it will typically only be populated if {@link #getSuggestedSourceLocation()} is NOT populated.
    */
   public String getSuggestedSourceURL() {
      return this.suggestedSourceURL;
   }
}

