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
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.prefs.Preferences;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

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
@RunLevel(value = LookupService.SL_NEG_1_METADATA_STORE_STARTED_RUNLEVEL)
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

         switch (launchEnvironment) {
         case FX_LAUNCHER: {
            System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "data/isaac.data");
            System.setProperty(PREFERENCES_FOLDER_LOCATION, "data/preferences");

            File importPath = new File("data/to-import");

            importPath.mkdirs();

            File afterImportPath = new File("data/completed-import");

            afterImportPath.mkdirs();
            System.setProperty(IMPORT_FOLDER_LOCATION, importPath.getAbsolutePath());
            System.setProperty(AFTER_IMPORT_FOLDER_LOCATION, afterImportPath.getAbsolutePath());

            if (setPropertyIfFileExists(USER_CSS_LOCATION_PROPERTY, Paths.get("data", "user.css"))) {}
            else if (setPropertyIfFileExists(USER_CSS_LOCATION_PROPERTY, Paths.get("user.css"))) {}
            else {
               Path cssPath = Paths.get("data", "user.css");

               try (InputStream is = PreferencesProvider.class.getResourceAsStream("/user.css");
                  BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(cssPath, CREATE, APPEND))) {
                  BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                  String         line   = null;

                  while ((line = reader.readLine()) != null) {
                     writer.write(line);
                     writer.newLine();
                  }
               }
            }

            break;
         }

         case MAVEN: {
            System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/data/isaac.data");
            System.setProperty(PREFERENCES_FOLDER_LOCATION, "target/data/preferences");

            File importPath = new File("target", "to-import");

            importPath.mkdirs();

            File afterImportPath = new File("target", "completed-import");

            afterImportPath.mkdirs();
            System.setProperty(IMPORT_FOLDER_LOCATION, importPath.getAbsolutePath());
            System.setProperty(AFTER_IMPORT_FOLDER_LOCATION, afterImportPath.getAbsolutePath());

            if (setPropertyIfFileExists(
                  USER_CSS_LOCATION_PROPERTY,
                  Paths.get(
                      "/Users",
                      "kec",
                      "isaac",
                      "semiotic-history",
                      "isaac",
                      "komet",
                      "css",
                      "src",
                      "main",
                      "resources",
                      "user.css"))) {}
            else {
               setPropertyIfFileExists(USER_CSS_LOCATION_PROPERTY, Paths.get("target", "data", "user.css"));
            }

            break;
         }

         default:
            throw new UnsupportedOperationException("Can't handle launch environment: " + launchEnvironment);
         }

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
         ex.printStackTrace();
         LOG.error(ex);
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
         // HK2 swallows these exceptions, so I'm trying to make sure they are
         // easy to identify.
         // TODO figure out how to keep hk2 from swallowing these.
         ex.printStackTrace();
         LOG.error(ex);
         throw new RuntimeException(ex);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IsaacPreferences getApplicationPreferences() {
      // MemoryConfiguration memoryConfiguration = MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY;
      return IsaacPreferencesImpl.getApplicationRoot();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    *
    * @return true if the file existed, and the property was set.
    */
   private boolean setPropertyIfFileExists(String property, Path filePath)
            throws MalformedURLException {
      if (Files.exists(filePath)) {
         System.setProperty(property, filePath.toUri()
               .toURL()
               .toString());
         return true;
      }

      return false;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public IsaacPreferences getSystemPreferences() {
      return new PreferencesWrapper(Preferences.systemRoot());
   }

   @Override
   public IsaacPreferences getUserPreferences() {
      return new PreferencesWrapper(Preferences.userRoot());
   }
}

