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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;

import static sh.isaac.api.constants.Constants.PREFERENCES_FOLDER_LOCATION;
import sh.isaac.api.preferences.IsaacPreferences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class IsaacPreferencesImpl
        extends AbstractPreferences {
   private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();
   private static IsaacPreferencesWrapper               applicationRoot;
   private static File                                  applicationPreferencesFolder;

   //~--- fields --------------------------------------------------------------

   private Map<String, String> preferencesTree;
   private final File          directory;
   private final File          preferencesFile;
   private final File          temporaryFile;

   //~--- constructors --------------------------------------------------------

   private IsaacPreferencesImpl() {
      super(null, "");
      this.directory       = applicationPreferencesFolder;
      this.preferencesFile = new File(this.directory, "preferences.xml");
      this.temporaryFile   = new File(this.directory, "preferences-tmp.xml");
   }

   private IsaacPreferencesImpl(IsaacPreferencesImpl parent, String name) {
      super(parent, name);

      if (!isValidPath(name)) {
         throw new IllegalStateException("Name is not a valid file name or path: " + name);
      }

      this.directory       = new File(parent.directory, name);
      this.preferencesFile = new File(this.directory, "preferences.xml");
      this.temporaryFile   = new File(this.directory, "preferences-tmp.xml");
   }

   //~--- methods -------------------------------------------------------------

   void exportMap(OutputStream os, Map<String, String> map)
            throws Exception {
      XmlForIsaac.exportMap(os, map);
   }

   void importMap(InputStream is, Map<String, String> map)
            throws Exception {
      XmlForIsaac.importMap(is, map);
   }

   @Override
   protected IsaacPreferencesImpl childSpi(String name) {
      return new IsaacPreferencesImpl(this, name);
   }

   @Override
   public boolean isRemoved() {
      return super.isRemoved(); //To change body of generated methods, choose Tools | Templates.
   }
   
   public Object getLock() {
      return lock;
   }

   @Override
   protected String[] childrenNamesSpi()
            throws BackingStoreException {
      List<String> result      = new ArrayList<>();
      File[]       dirContents = directory.listFiles();

      if (dirContents != null) {
         for (File dirContent: dirContents) {
            if (dirContent.isDirectory()) {
               result.add(dirContent.getName());
            }
         }
      }

      return result.toArray(new String[result.size()]);
   }

   @Override
   protected void flushSpi()
            throws BackingStoreException {
      // nothing to do per the FileSystemPreferences implementation.
   }

   @Override
   protected String[] keysSpi()
            throws BackingStoreException {
      return preferencesTree.keySet()
                            .toArray(new String[preferencesTree.size()]);
   }

   @Override
   protected void putSpi(String key, String value) {
      preferencesTree.put(key, value);
   }

   @Override
   protected void removeNodeSpi()
            throws BackingStoreException {
      if (this.preferencesFile.exists()) {
         this.preferencesFile.delete();
      }

      if (this.temporaryFile.exists()) {
         this.temporaryFile.delete();
      }

      File[] extras = directory.listFiles();

      if (extras.length != 0) {
         LOG.warn("Found extraneous files when removing node: " + Arrays.asList(extras));

         for (File extra: extras) {
            extra.delete();
         }
      }

      if (!directory.delete()) {
         throw new BackingStoreException("Couldn't delete: " + directory);
      }
   }

   @Override
   protected void removeSpi(String key) {
      preferencesTree.remove(key);
   }

   @Override
   protected void syncSpi()
            throws BackingStoreException {
      try {
         if (!directory.exists() &&!directory.mkdirs()) {
            throw new BackingStoreException(directory + " create failed.");
         }

         try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
            exportMap(fos, preferencesTree);
         }
         Files.move(temporaryFile.toPath(), preferencesFile.toPath(), REPLACE_EXISTING);
      } catch (Exception e) {
         if (e instanceof BackingStoreException) {
            throw(BackingStoreException) e;
         }

         throw new BackingStoreException(e);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public static synchronized IsaacPreferences getApplicationRoot() {
      if (applicationRoot == null) {
         applicationPreferencesFolder = new File(System.getProperty(PREFERENCES_FOLDER_LOCATION));
         applicationPreferencesFolder.mkdirs();

         if (!applicationPreferencesFolder.canWrite()) {
            throw new IllegalStateException(
                "Application preferences folder is not writable: " + applicationPreferencesFolder.getAbsolutePath());
         }

         applicationRoot = new IsaacPreferencesWrapper(new IsaacPreferencesImpl());
      }

      return applicationRoot;
   }

   @Override
   protected String getSpi(String key) {
      if (preferencesTree == null) {
         preferencesTree = new TreeMap<>();
         if (preferencesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(preferencesFile)) {
               importMap(fis, preferencesTree);
            } catch (Exception ex) {
               LOG.error(ex);
            }
         }
      }

      return preferencesTree.get(key);
   }

   public static boolean isValidPath(String path) {
      try {
         Paths.get(path);
      } catch (InvalidPathException | NullPointerException ex) {
         return false;
      }

      return true;
   }
}

