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



package sh.isaac.api.identity;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface IdentifiedObject.
 *
 * @author kec
 */
public interface IdentifiedObject {
   /**
    * To user string.
    *
    * @return the string
    */
   default String toUserString() {
      return toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   default int getNid() throws NoSuchElementException{
      return Get.identifierService()
                .getNidForUuids(getUuidList());
   }

   /**
    * 
    * @return the nid for the concept specifying which assemblage this object is created within. 
    */
   default int getAssemblageNid() {
      return Get.identifierService().getAssemblageNid(getNid()).getAsInt();
   }


   /**
    * If not yet set via a call to {@link IdentifiedComponentBuilder#setPrimordialUuid(UUID)},  or {@link IdentifiedComponentBuilder#setT5Uuid()}
    * a randomly generated UUID will be created and returned.
    * 
    * Otherwise, return already set value.
    * @return the builder for chaining of operations in a fluent pattern.
    */
   default UUID getPrimordialUuid() {
      return getUuidList().get(0);
   }

   /**
    * Gets a COPY of the uuid list.  Edits to the returned list are not stored.
    *
    * @return the uuid list
    */
   List<UUID> getUuidList();

   /**
    * Gets the uuids.
    *
    * @return the uuids
    */
   default UUID[] getUuids() {
      return getUuidList().toArray(new UUID[getUuidList().size()]);
   }
   
   default boolean isIdentifiedBy(UUID uuid) {
      return getUuidList().contains(uuid);
   }
}

