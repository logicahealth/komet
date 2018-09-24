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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.preferences.IsaacPreferences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class IsaacPreferencesImpl
        extends AbstractPreferences {
   private final Logger LOG = LogManager.getLogger();
   public static final String DB_PREFERENCES_FOLDER = "preferences";
   
   public static AtomicReference<IsaacPreferencesWrapper> singleton = new AtomicReference<>();
   public static AtomicReference<IsaacPreferencesImpl> coreSingleton = new AtomicReference<>();

   //~--- fields --------------------------------------------------------------

   private Map<String, String> preferencesTree;
   private final File          directory;
   private final File          preferencesFile;
   private final File          temporaryFile;

   //~--- constructors --------------------------------------------------------

   private IsaacPreferencesImpl() {
      //For HK2
      super(null, "");
      this.directory       = Get.configurationService().getDataStoreFolderPath().resolve("preferences").toFile();
      this.preferencesFile = new File(this.directory, "preferences.xml");
      this.temporaryFile   = new File(this.directory, "preferences-tmp.xml");
      init();
   }

   //We only enforce singleton for the root preferences.  Its up to the AbstractPreferences to keep track of 
   //child references properly.
   private IsaacPreferencesImpl(IsaacPreferencesImpl parent, String name) {
      super(parent, name);

      if (!isValidPath(name)) {
         throw new IllegalStateException("Name is not a valid file name or path: " + name);
      }

      this.directory       = new File(parent.directory, name);
      this.preferencesFile = new File(this.directory, "preferences.xml");
      this.temporaryFile   = new File(this.directory, "preferences-tmp.xml");
      init();
   }
   
   @Override
    public String toString() {
        return "Configuration Preference Node: " + this.absolutePath();
    }
   
   /**
    * The public mechanism to get a handle to a preferences store that stores its data inside the datastore folder.
    * @return This class, wrapped by a {@link IsaacPreferencesWrapper}
    */
   public static IsaacPreferences getConfigurationRootPreferences() {
       if (singleton.get() == null) {
           coreSingleton.compareAndSet(null, new IsaacPreferencesImpl());
           singleton.compareAndSet(null, new IsaacPreferencesWrapper(coreSingleton.get()));
       }
       
      return singleton.get();
   }

   public static void reloadConfigurationPreferences() {
        coreSingleton.set(new IsaacPreferencesImpl());
        singleton.get().changeDelegate(coreSingleton.get());
   }
   //~--- methods -------------------------------------------------------------
   
   private void init()
   {
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
   }

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
      return super.isRemoved();
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

   @Override
   protected String getSpi(String key) {
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