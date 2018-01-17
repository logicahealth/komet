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



package sh.isaac.komet.preferences;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

import org.apache.commons.lang3.StringUtils;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.LookupService;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;

import static sh.isaac.api.constants.Constants.AFTER_IMPORT_FOLDER_LOCATION;
import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import static sh.isaac.api.constants.Constants.IMPORT_FOLDER_LOCATION;
import static sh.isaac.api.constants.Constants.PREFERENCES_FOLDER_LOCATION;
import static sh.isaac.api.constants.Constants.USER_CSS_LOCATION_PROPERTY;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service(name = "Preferences Provider")
@RunLevel(value = LookupService.SL_L0_METADATA_STORE_STARTED_RUNLEVEL)
public class PreferencesProvider
         implements PreferencesService {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private LaunchEnvironment launchEnvironment;
   private IsaacPreferences  applicationPreferences;

   //~--- methods -------------------------------------------------------------

   /**
    * Start me.
    */
   @PostConstruct
   protected void startMe() {
      try {
         if (Files.exists(Paths.get("target"))) {
            launchEnvironment = LaunchEnvironment.MAVEN;
         } else {
            launchEnvironment = LaunchEnvironment.FX_LAUNCHER;
         }
         
         checkOrSetFileProperty("DATA_STORE_ROOT_LOCATION_PROPERTY", DATA_STORE_ROOT_LOCATION_PROPERTY, () -> {
            return launchEnvironment == LaunchEnvironment.MAVEN ? 
                  Paths.get("target", "data", "isaac.data").toFile() : 
                     Paths.get("data", "isaac.data").toFile();
         }, true);
         
         
         checkOrSetFileProperty("PREFERENCES_FOLDER_LOCATION", PREFERENCES_FOLDER_LOCATION, () -> {
            //Create it as a sibling folder of wherever the data store is specified
            return Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)).resolveSibling("preferences").toFile();
         }, true);
         
         checkOrSetFileProperty("IMPORT_FOLDER_LOCATION", IMPORT_FOLDER_LOCATION, () -> {
            //Always look under a JVM relative path of data, rather than looking in the target folder.
            return Paths.get("data", "to-import").toFile();
         }, true);
         
         checkOrSetFileProperty("AFTER_IMPORT_FOLDER_LOCATION", AFTER_IMPORT_FOLDER_LOCATION, () -> {
            //Create it as a sibling folder of wherever the data store is specified, this way, it gets
            //removed with a maven clean, along with the rest of the DB.
            return Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)).resolveSibling("completed-import").toFile();
         }, true);
         
         checkOrSetFileProperty("USER_CSS_LOCATION_PROPERTY", USER_CSS_LOCATION_PROPERTY, () -> {
            //db relative paths, followed by jvm relative paths, followed by writing out the default from the classpath.
            try {
               Path[] checkPaths = new Path[] {
                     Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)).resolveSibling("data").resolve("user.css"),
                     Paths.get("data", "user.css"), 
                     Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)).resolveSibling("user.css"),
                     Paths.get("user.css")};
               
               Path p = null;
               for (int i = 0; i < checkPaths.length; i++) {
                  if (Files.isRegularFile(checkPaths[i])) {
                     p = checkPaths[i];
                     break;
                  }
               }
               if (p == null) {
                  // Can't find one, write out the defaults as a sibling of the datastore
                  // When running the build under maven, various tests wants to grab this file, from a project
                  // that hasn't yet been built, so it isn't on the classpath.  Hack the actual path in place here.
                  Path inputSource = Paths.get("komet", "css", "src", "main", "resources", "user.css");

                  p = Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)).resolveSibling("user.css");
                  try (InputStream is = (Files.isRegularFile(inputSource) ? 
                        new FileInputStream(inputSource.toFile()) : 
                           PreferencesProvider.class.getResourceAsStream("/user.css"));
                        BufferedReader reader = (is == null ? null : new BufferedReader(new InputStreamReader(is)));
                        BufferedWriter writer = (reader == null ? null : new BufferedWriter(Files.newBufferedWriter(p, CREATE_NEW)))) {
                     
                     if (is != null) {
                        String line = null;
   
                        while ((line = reader.readLine()) != null) {
                           writer.write(line);
                           writer.newLine();
                        }
                     } else {
                        LOG.info("Unable to locate a user.css file");
                     }
                  }
               }
               return p.toFile();
            } catch (IOException e) {
               LOG.error("Unexpected error trying to locate or write out the user.css file", e);
               throw new RuntimeException(e);
            } 
         }, false);

         this.applicationPreferences = IsaacPreferencesImpl.getApplicationRoot();

         if (!this.applicationPreferences.hasKey(MemoryConfiguration.class)) {
            this.applicationPreferences.putEnum(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);
         }

         this.applicationPreferences.put(
             DATA_STORE_ROOT_LOCATION_PROPERTY,
             System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY));
         this.applicationPreferences.sync();
      } catch (Throwable ex) {
         // HK2 swallows these exceptions, so I'm trying to make sure they are
         // easy to identify.
         // TODO figure out how to keep hk2 from swallowing these.
         LOG.error("Unexpected error stating preferences provider", ex);
         throw new RuntimeException(ex);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   protected void stopMe() {
      try {
         LOG.info("Stopping Preferences Provider.");
         this.applicationPreferences.sync();
      } catch (Throwable ex) {
         // TODO figure out how to keep hk2 from swallowing these.
         LOG.error("Unexpected error stopping prefs provider", ex);
         throw new RuntimeException(ex);
      }
   }
   
   private void checkOrSetFileProperty(String propertyName, String propertyKey, Supplier<File> defaultProvider, boolean makeDir) throws IOException {
      File f;
      if (StringUtils.isBlank(System.getProperty(propertyKey))) {
         f = defaultProvider.get();
         LOG.info("{}:{} is not set, using default of {}", propertyName, propertyKey, f.getCanonicalPath());
         System.setProperty(propertyKey, f.getCanonicalPath());
      }
      else {
         f = new File(System.getProperty(propertyKey)).getCanonicalFile();
         LOG.info("{}:{} was already set to {}", propertyName, propertyKey, f.getCanonicalPath());
      }
      if (makeDir) {
         f.mkdirs();
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IsaacPreferences getApplicationPreferences() {
      // MemoryConfiguration memoryConfiguration = MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY;
      return IsaacPreferencesImpl.getApplicationRoot();
   }

   @Override
   public IsaacPreferences getSystemPreferences() {
      return new PreferencesWrapper(Preferences.systemRoot());
   }

   @Override
   public IsaacPreferences getUserPreferences() {
      return new PreferencesWrapper(Preferences.userRoot());
   }
}

