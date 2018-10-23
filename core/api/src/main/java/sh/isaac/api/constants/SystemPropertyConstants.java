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

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;

/**
 * Created by kec on 9/11/14.
 *
 * This class carries the identifiers of properties that may be specified as System Properties, prior to the JVM start.
 * 
 * System properties should only be used where absolutely necessary, during system bootstrap - or for things we need to 
 * conveniently override when doing things like JUnit tests.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SystemPropertyConstants {

   /**
    * The default HK2 configuration only scans classes in a few packages, such as 'sh', 'org.glassfish', and 'org.glassfish' when looking for HK2
    * annotated classes.  Specify additional packages to search with this property, separating unique packages with a ';'.
    * 
    * This is only used internally inside the {@link LookupService} prior to starting HK2.
    */
   public static final String EXTRA_PACKAGES_TO_SEARCH = "EXTRA_PACKAGES_TO_SEARCH";

   /** 
    * Used to specify the location of the data store folder, which contains various datastore subfolders, depending on the 
    * implementation modules of the datastore.  This value should be read via {@link ConfigurationService#getDataStoreFolderPath()}
    */
   public static final String DATA_STORE_ROOT_LOCATION_PROPERTY = "DATA_STORE_ROOT_LOCATION_PROPERTY";

   /** 
    * Used to specify how the datastore should use memory
    * This will override {@link GlobalDatastoreConfiguration#getMemoryConfiguration()}
    */
   public static final String DATA_STORE_MEMORY_CONFIG_PROPERTY = "DATA_STORE_MEMORY_CONFIGURATION_PROPERTY";
   
   /** 
    * Used to specify whether or not the data store will load metadata into itself when it starts.
    * This will override {@link ConfigurationService#getDatabaseInitializationMode()}
    */
   public static final String DATA_STORE_INIT = "DATA_STORE_INIT";
   
   /** 
    * Used to specify which datastore impelementation should be used.
    * This will override {@link ConfigurationService#getDatabaseImplementation()}
    */
   public static final String DATA_STORE_TYPE = "DATA_STORE_TYPE";

   /**
    * May be optionally used to enable console-level, extremely verbose debug output by providing a value of 'true'
    * This typically turns on and off things that were hacked in with System.Out calls for tracing ugly problems....
    * 
    * This is read via {@link ConfigurationService#isVerboseDebugEnabled()}
    */
   public static final String ISAAC_DEBUG = "ISAAC_DEBUG";
   
   /** 
    * Used to specify the location of the ibdf import folder, This value should be read via {@link ConfigurationService#getIBDFImportPath()}
    */
   public static final String IMPORT_FOLDER_LOCATION = "IMPORT_FOLDER_LOCATION";
}

