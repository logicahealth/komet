/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api;

import java.util.Optional;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.DatastoreServices.DataStoreStartState;
import sh.isaac.api.constants.DatabaseInitialization;

/**
 *
 * @author kec
 */
@Contract
public interface MetadataService {
   /**
    * Initialize the database with metadata, IFF the database was started completely blank.  If the database had existing data upon startup, 
    * this call will have no effect.  Calling this method multiple times has no effect beyond the first call.
    * 
    * This is also a no-op if unless {@link GlobalDatastoreConfiguration#getDatabaseInitializationMode()} returns 
    * {@link DatabaseInitialization#LOAD_METADATA}.
    * @throws Exception 
    */
   void importMetadata() throws Exception;

   /**
    * Will reimport metadata into database, even if database is not empty. Used to update the underlying database
    * with updated metadata even if the database is already loaded.
    * @return true if new metadata was encountered.
    * @throws Exception
    */
   boolean reimportMetadata() throws Exception;

   
   /**
    * @return return true, if metadata was imported (via a call to {@link #importMetadata()}) during the startup sequence.  Returns false, if 
    * the importMetadata routine was either not called, or did not need to be executed.
    */
   public boolean wasMetadataImported();
   
   /**
    * Return the UUID that was generated when the datastore was first created.  Note, that this may be empty for a time, 
    * when the DataStoreStartState was {@link DataStoreStartState#NOT_YET_CHECKED} or {@link DataStoreStartState#NO_DATASTORE} 
    * 
    * Note, this is purposefully the same signature pattern as {@link DatastoreServices#getDataStoreId()}, and is added to this 
    * interface as a crutch to help some startup-dependency issues.
    *
    * @return the data store id
    */
   public Optional<UUID> getDataStoreId();
}
