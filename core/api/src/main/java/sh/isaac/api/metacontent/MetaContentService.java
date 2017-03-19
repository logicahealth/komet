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



package sh.isaac.api.metacontent;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ConcurrentMap;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.metacontent.userPrefs.StorableUserPreferences;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link MetaContentService}
 *
 * An interface that allows the storage of information that is not terminology related.
 * Examples include user preferences, but the intent of the API is to be generic.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface MetaContentService {
   /**
    * Call prior to JVM exit for safe shutdown
    */
   public void close();

   /**
    * Open or create a new data store.  The type of the key and value must be specified.
    * Not being consistent with the Key/Value types for a particular store name will result in
    * a runtime class cast exception.  For example, this will fail at runtime:
    *
    * <Long,String>openStore("myStore").put(54l, "fred")
    * ...
    * <Long,Integer>openStore("myStore").get(54l)
    *
    *  Data added to the ConcurrentMap is automatically flushed to disk, and is safe after the flush interval, or as long as {@link #close()} is
    *  called prior to JVM exit.  Note that it is typically not the job of the caller of this method to call close on the overall MetaContentService.
    *
    *  Any object can be utilized for the Key and Value - however, for types outside of the basic types, java serialization will be utilized,
    *  which is quite inefficient.  For storing large objects, it is recommended you make your Value a byte[], and handle the serialization
    *  yourself.
    *
    * @param storeName the name of the store.
    */
   public <K, V> ConcurrentMap<K, V> openStore(String storeName);

   /**
    * @param userId - the nid or sequence of the concept that identifies the user
    * @param userPrefs - user preference data to store
    * @return the old value, or null, if no old value
    */
   public byte[] putUserPrefs(int userId, StorableUserPreferences userPrefs);

   /**
    * Erase the named store
    */
   public void removeStore(String storeName);

   /**
    * Erase any stored user prefs
    */
   public void removeUserPrefs(int userId);

   //~--- get methods ---------------------------------------------------------

   /**
    * @param userId - the nid or sequence of the concept that identifies the user
    * @return the byte[] that stores the user preferences, which was obtained by calling {@link StorableUserPreferences#serialize()}
    * This value should be able to be passed into the concrete implementation constructor of a class that implements {@link StorableUserPreferences}
    */
   public byte[] getUserPrefs(int userId);
}

