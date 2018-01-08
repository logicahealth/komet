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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface IdentifierService.
 *
 * @author kec
 */
@Contract
public interface IdentifierService
        extends DatabaseServices {
   /** The Constant FIRST_NID. */
   static final int FIRST_NID = Integer.MIN_VALUE + 1;

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the uuid for nid.
    *
    * @param uuid the uuid
    * @param nid the nid
    */
   void addUuidForNid(UUID uuid, int nid);
   
   /**
    * 
    * @param componentNid
    * @return the OptionalInt containing an assemblageNid within which this component was 
    * created or an empty OptionalInt if the component has not yet been written to the 
    * database. 
    */
   OptionalInt getAssemblageNid(int componentNid);

   /**
    * 
    * @return an array of nids for the concepts that define assemblages. 
    */
   int[] getAssemblageNids();
   
   IntStream getNidStreamOfType(IsaacObjectType objectType);

   IntStream getNidsForAssemblage(int assemblageNid);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the chronology type for nid.
    * 
    * @param nid the nid
    * @return the chronology type for nid
    * @deprecated need to replace with IsaacChronologyType, and eliminate ObjectChronologyType. Use
    * getObjectTypeForComponent instead. 
    */
   @Deprecated
   ObjectChronologyType getOldChronologyTypeForNid(int nid);
   
   /**
    * 
    * @param componentNid the identifier that the object type is requested for. 
    * @return the type of object identified by the componentNid. 
    */
   IsaacObjectType getObjectTypeForComponent(int componentNid);

   /**
    * Gets the concept sequence for proxy.
    *
    * @param conceptProxy the concept proxy
    * @return the concept sequence for proxy
    */
   int getNidForProxy(ConceptSpecification conceptProxy);

   int getCachedNidForProxy(ConceptSpecification conceptProxy);
   /**
    * Gets the concept sequence for uuids.
    *
    * @param uuids the uuids
    * @return the concept sequence for uuids
    */
   int getNidForUuids(Collection<UUID> uuids);

   /**
    * Gets the concept sequence for uuids.
    *
    * @param uuids the uuids
    * @return the concept sequence for uuids
    */
   int getNidForUuids(UUID... uuids);

   /**
    * Gets the identifier for authority.
    *
    * @param nid the nid
    * @param identifierAuthorityUuid the identifier authority uuid
    * @param stampCoordinate the stamp coordinate
    * @return the identifier for authority
    */
   Optional<String> getIdentifierForAuthority(int nid,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate);

   /**
    * Checks for uuid.
    *
    * @param uuids the uuids
    * @return true, if successful
    */
   boolean hasUuid(Collection<UUID> uuids);

   /**
    * Checks for uuid.
    *
    * @param uuids the uuids
    * @return true, if successful
    */
   boolean hasUuid(UUID... uuids);

   /**
    * Gets the uuid array for nid.
    *
    * @param nid the nid
    * @return the uuid array for nid
    */
   default UUID[] getUuidArrayForNid(int nid) {
      final List<UUID> uuids = getUuidsForNid(nid);

      return uuids.toArray(new UUID[uuids.size()]);
   }

   /**
    * Gets the uuid primordial for nid.
    *
    * @param nid the nid
    * @return the uuid primordial for nid
    */
   Optional<UUID> getUuidPrimordialForNid(int nid);

   /**
    * Gets the uuids for nid.
    *
    * @param nid the nid
    * @return the uuids for nid
    */
   List<UUID> getUuidsForNid(int nid);

   /**
    * 
    * @return memory used in bytes
    */
    long getMemoryInUse();
    /**
     * 
     * @return disk space used in bytes
     */
    long getSizeOnDisk();
}

