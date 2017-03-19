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



package sh.isaac.provider.metacontent;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.file.Path;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.metacontent.userPrefs.StorableUserPreferences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link MVStoreMetaContentProvider}
 *
 * An implementation of a MetaContentService wrapped around the MVStore from the
 * H2 DB project http://www.h2database.com/html/mvstore.html
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "MVStoreMetaContent")
@RunLevel(value = -1)
public class MVStoreMetaContentProvider
         implements MetaContentService {
   /** The Constant USER_PREFS_STORE. */
   private static final String USER_PREFS_STORE = "_userPrefs_";

   //~--- fields --------------------------------------------------------------

   /** The log. */
   private final Logger LOG = LogManager.getLogger();

   /** The store. */
   MVStore store;

   /** The user prefs map. */
   MVMap<Integer, byte[]> userPrefsMap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new MV store meta content provider.
    */
   @SuppressWarnings("unused")
   private MVStoreMetaContentProvider() {
      // For HK2
      this.LOG.info("Constructing MVStoreMetaContent service " + this.hashCode());
   }

   /**
    * Typically, this object should be retrieved from HK2 / the Lookup service
    * - which already had a Service instance created. However, it is allowable
    * to create your own instance outside of the management of HK2 using this
    * method.
    *
    * @param storageFolder
    *            - The folder to utilize for storage.
    * @param storePrefix
    *            - optional - a prefix to utilize on all files/folders created
    *            by the service inside the storageFolder
    * @param wipeExisting
    *            - true to erase preexisting content and start fresh, false to
    *            read existing data.
    */
   public MVStoreMetaContentProvider(File storageFolder, String storePrefix, boolean wipeExisting) {
      this.LOG.info("Starting a user-requested MVStoreMetaContent instance");
      initialize(storageFolder, storePrefix, wipeExisting);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Close.
    *
    * @see sh.isaac.api.metacontent.MetaContentService#close()
    */
   @Override
   @PreDestroy
   public void close() {
      this.LOG.info("Stopping a MVStoreMetaContent service");

      if (this.store != null) {
         this.store.close();
      }
   }

   /**
    * Open store.
    *
    * @param <K> the key type
    * @param <V> the value type
    * @param storeName the store name
    * @return the concurrent map
    * @see sh.isaac.api.metacontent.MetaContentService#openStore(java.lang.String)
    */
   @Override
   public <K, V> ConcurrentMap<K, V> openStore(String storeName) {
      if (storeName.equals(USER_PREFS_STORE)) {
         throw new IllegalArgumentException("reserved store name");
      }

      return this.store.<K, V>openMap(storeName);
   }

   /**
    * Put user prefs.
    *
    * @param userId the user id
    * @param userPrefs the user prefs
    * @return the byte[]
    * @see sh.isaac.api.metacontent.MetaContentService#putUserPrefs(int,
    *      sh.isaac.api.metacontent.userPrefs.StorableUserPreferences)
    */
   @Override
   public byte[] putUserPrefs(int userId, StorableUserPreferences userPrefs) {
      return this.userPrefsMap.put((userId > 0) ? userId
            : Get.identifierService()
                 .getConceptSequence(userId), userPrefs.serialize());
   }

   /**
    * Removes the store.
    *
    * @param storeName the store name
    * @see sh.isaac.api.metacontent.MetaContentService#removeStore(java.lang.String)
    */
   @Override
   public void removeStore(String storeName) {
      if (storeName.equals(USER_PREFS_STORE)) {
         throw new IllegalArgumentException("reserved store name");
      }

      this.store.removeMap(this.store.openMap(storeName));
   }

   /**
    * Removes the user prefs.
    *
    * @param userId the user id
    * @see sh.isaac.api.metacontent.MetaContentService#removeUserPrefs(int)
    */
   @Override
   public void removeUserPrefs(int userId) {
      this.userPrefsMap.remove((userId > 0) ? userId
            : Get.identifierService()
                 .getConceptSequence(userId));
   }

   /**
    * Initialize.
    *
    * @param storageFolder the storage folder
    * @param storePrefix the store prefix
    * @param wipeExisting the wipe existing
    * @return the meta content service
    */
   private MetaContentService initialize(File storageFolder, String storePrefix, boolean wipeExisting) {
      final File dataFile = new File(storageFolder, (StringUtils.isNotBlank(storePrefix) ? storePrefix
            : "") + "MetaContent.mv");

      if (wipeExisting && dataFile.exists()) {
         if (!dataFile.delete()) {
            throw new RuntimeException("wipeExisting was requested, but can't delete " + dataFile.getAbsolutePath());
         }
      }

      this.LOG.info("MVStoreMetaContent store path: " + dataFile.getAbsolutePath());
      this.store = new MVStore.Builder().fileName(dataFile.getAbsolutePath())
                                        .open();

      // store.setVersionsToKeep(0); TODO check group answer
      this.userPrefsMap = this.store.<Integer, byte[]>openMap(USER_PREFS_STORE);
      return this;
   }

   /**
    * Start.
    */
   @PostConstruct
   private void start() {
      this.LOG.info("Starting MVStoreMetaContent service");

      final Optional<Path> path = Get.configurationService()
                                     .getDataStoreFolderPath();

      if (!path.isPresent()) {
         throw new RuntimeException(
             "Unable to start MVStore - no folder path is available in the Configuration Service!");
      }

      final File temp = new File(path.get().toFile(), "metacontent");

      temp.mkdir();

      if (!temp.isDirectory()) {
         throw new RuntimeException("Cannot initialize MetaContent Store - was unable to create " +
                                    temp.getAbsolutePath());
      }

      initialize(temp, "service_", false);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the user prefs.
    *
    * @param userId the user id
    * @return the user prefs
    * @see sh.isaac.api.metacontent.MetaContentService#getUserPrefs(int)
    */
   @Override
   public byte[] getUserPrefs(int userId) {
      return this.userPrefsMap.get((userId > 0) ? userId
            : Get.identifierService()
                 .getConceptSequence(userId));
   }
}

