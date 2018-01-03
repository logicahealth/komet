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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Future;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import javafx.beans.value.ObservableObjectValue;

//~--- interfaces -------------------------------------------------------------

/**
 * Contract used to validate that databases & lucene directories uniformly exist and are uniformly populated during startup. If fails, signals that
 * database is corrupted and force a pull of new database. Launched via {@link LookupService}
 *
 * @author Jesse Efron
 */
@Contract
public interface DatabaseServices {
   /**
    * The Enum DatabaseValidity.
    */
   public enum DatabaseValidity {
      /** the starting point. */
      NOT_YET_CHECKED,

      /** The datastore directory is missing or empty */
      NO_DATASTORE,

      /** An existing data store is present */
      EXISTING_DATASTORE,
      
      /** The existing data store has been read and validated */
      VALID_DATASTORE
   }


   //~--- get methods ---------------------------------------------------------

   /**
    * Flag indicating that folder path of the database.
    */
   public Path getDatabaseFolder();

   /**
    * Gets the database validity status.  This should never return null.  Implementations should start by returning {@link DatabaseValidity#NOT_YET_CHECKED}.  
    * When a datastore starts, it should set this appropriately, as it starts up.  When it shuts down, it should return {@link DatabaseValidity#NOT_YET_CHECKED}
    *
    * @return the database validity status
    */
   public ObservableObjectValue<DatabaseValidity> getDatabaseValidityStatus();
   
   /**
    * Return the UUID that was generated when the database was first created.
    *
    * @return the data store id
    */
   public UUID getDataStoreId();
   
   /**
    * Ensures data is written to disk. 
    * @return 
    */
   public Future<?> sync();

}

