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



package sh.isaac.api.constants;

/**
 * Created by kec on 9/11/14.
 *
 * A typical datastore structure would look like this:
 *
 * somePath/data/object-chronicles/cradle/{content}
 * somePath/data/search/lucene/{content}
 *
 * {@link #DATA_STORE_ROOT_LOCATION_PROPERTY} can be utilized to set the absolute path to the location of the 'data' portion of the paths in the
 * example above. {@link #DEFAULT_CHRONICLE_FOLDER} will be automatically appended to the path to create the chronicle path. {@link
 * #DEFAULT_SEARCH_FOLDER} will be automatically appended to the path to create the search path.
 *
 * If you have a data structure where the object-chronicles folder does not share a common parent folder with the search folder, then you may utilize
 * the {@link #CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY} and {@link #SEARCH_ROOT_LOCATION_PROPERTY} to specify the absolute path to the
 * 'object-chronicles' and 'search' components of the above examples, respectively.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Constants {
   /**
    * If set to true, reads HK2 inhabitant files from the jar files to locate HK2 services.  Otherwise, scans the classpath
    * for classes with HK2 annotations - ignoring the inhabitant files.
    */
   public static final String READ_INHABITANT_FILES = "sh.isaac.api.constants.READ_INHABITANT_FILES";

   /**
    * The default HK2 configuration only scans classes in the packages 'gov.vha', 'org.ihtsdo' and 'org.glassfish' when looking for HK2
    * annotated classes.  Specify additional packages to search with this property, separating unique packages with a ';'.
    */
   public static final String EXTRA_PACKAGES_TO_SEARCH = "sh.isaac.api.constants.EXTRA_PACKAGES_TO_SEARCH";

   /** Used to specify the location of the data store folder, which contains subfolders for 'object-chronicles' and 'search'. */
   public static final String DATA_STORE_ROOT_LOCATION_PROPERTY = "sh.isaac.api.constants.data-store-root-location";

   /** Used to specify how the datastore should use memory */
   public static final String DATA_STORE_MEMORY_CONFIG_PROPERTY = "sh.isaac.api.constants.DATA_STORE_MEMORY_CONFIGURATION_PROPERTY";

   /** Appended to the path specified by the {@link #DATA_STORE_ROOT_LOCATION_PROPERTY}. */
   public static final String DEFAULT_CHRONICLE_FOLDER = "object-chronicles";

   /** Appended to the path specified by the {@link #DATA_STORE_ROOT_LOCATION_PROPERTY}. */
   public static final String DEFAULT_SEARCH_FOLDER = "search";
   
   public static final String USER_CSS_LOCATION_PROPERTY = "sh.isaac.api.constants.USER_CSS_LOCATION";

   /**
    * May be optionally used to enable console-level, extremely verbose debug output.
    * This typically turns on and off things that were hacked in with System.Out calls for tracing ugly problems....
    */
   public static final String ISAAC_DEBUG = "sh.isaac.api.constants.isaac-debug";
   
   public static final String IMPORT_FOLDER_LOCATION = "sh.isaac.api.constants.IMPORT_FOLDER_LOCATION";
   public static final String AFTER_IMPORT_FOLDER_LOCATION = "sh.isaac.api.constants.AFTER_IMPORT_FOLDER_LOCATION";

   public static final String PREFERENCES_FOLDER_LOCATION = "sh.isaac.api.constants.PREFERENCES_FILE_LOCATION";
}

