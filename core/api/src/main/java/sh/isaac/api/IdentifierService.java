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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface IdentifierService.
 *
 * @author kec
 */
@Contract
public interface IdentifierService
        extends DatastoreServices {
   /** The Constant FIRST_NID. */
   static final int FIRST_NID = Integer.MIN_VALUE + 1;

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the uuid for nid.  The nid must already be known to the system.
    *
    * @param uuid the uuid
    * @param nid the nid
    */
   void addUuidForNid(UUID uuid, int nid);
   
   /**
    * 
    * @param componentNid
    * @return the assemblageNid within which this component was created, or an empty OptionalInt if the component 
    * has not yet been setup / written to the database. 
    */
   OptionalInt getAssemblageNid(int componentNid);

   /**
    * 
    * @return an array of nids for the concepts that define assemblages. 
    */
   int[] getAssemblageNids();
   
   /**
    * Map a UUID to a new nid.  
    * @param uuids 1 or more UUIDs that are tied to the object represented by the nid
    * @return the new nid
    * @throws IllegalArgumentException if one or more of the specified uuids is tied more than one distinct nids.
    * This method is a noop, if the specified UUID(s) are already all assigned to the same nid.
    */
   int assignNid(UUID ...uuids) throws IllegalArgumentException;
   
   /**
    * Return the nids that represent all objects in the system of the specified type
    * @param objectType
    * @return
    */
   IntStream getNidStreamOfType(IsaacObjectType objectType);

   /**
    * Return the nids of objects which are members of the specified assemblage
    * @param assemblageNid
    * @return
    */
   IntStream getNidsForAssemblage(int assemblageNid);

   //~--- get methods ---------------------------------------------------------

   /**
    * 
    * @param componentNid the identifier that the object type is requested for. 
    * @return the type of object identified by the componentNid. 
    */
   IsaacObjectType getObjectTypeForComponent(int componentNid);

   /**
    * Gets the nid assigned to the uuids.
    *
    * @param uuids the uuids
    * @return the concept nid for uuids
    * @throws NoSuchElementException if no nid has been assigned
    */
   int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException;

   /**
    * Gets the nid assigned to the uuids.
    *
    * @param uuids the uuids
    * @return the concept nid for uuids
    * @throws NoSuchElementException if no nid has been assigned
    */
   int getNidForUuids(UUID... uuids) throws NoSuchElementException;

   /**
    * Checks for uuid.
    *
    * @param uuids the uuids
    * @return true, if successful
    * @throws IllegalArgumentException if a UUID isn't specified
    */
   boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException;

   /**
    * Checks for uuid.
    *
    * @param uuids the uuids
    * @return true, if successful
    * @throws IllegalArgumentException if a UUID isn't specified
    */
   boolean hasUuid(UUID... uuids) throws IllegalArgumentException;
   
   /**
    * Gets the uuid array for nid.
    *
    * @param nid the nid
    * @return the uuid array for nid
    * @throws NoSuchElementException if the nid is unknown
    */
   default UUID[] getUuidArrayForNid(int nid) throws NoSuchElementException {
      final List<UUID> uuids = getUuidsForNid(nid);

      return uuids.toArray(new UUID[uuids.size()]);
   }

   /**
    * Gets the uuid primordial for nid.
    *
    * @param nid the nid
    * @return the uuid primordial for nid
    * @throws NoSuchElementException if the nid is unknown
    * @Deprecated use the getOptionalUuidPrimoridalForNid as it does not throw an 
    * exception. 
    */
   @Deprecated 
   UUID getUuidPrimordialForNid(int nid) throws NoSuchElementException;
   
   /**
    * Gets the uuid primordial for nid, using an optional rather than throwing a NoSuchElementException.
    *
    * @param nid the nid
    * @return the uuid primordial for nid
    */
   default Optional<UUID> getOptionalUuidPrimoridalForNid(int nid) {
       try {
           return Optional.of(getUuidPrimordialForNid(nid));
       } catch (NoSuchElementException ex) {
           return Optional.empty();
       }
   }
   /**
     * @param nid the nid
    * @return A string representation of the uuid, or new UUID(0,0) 
    * if one has not been assigned. 
    */
   default String getUuidPrimoridalStringForNid(int nid) {
       try {
           return getUuidPrimordialForNid(nid).toString();
       } catch (NoSuchElementException ex) {
           return new UUID(0,0).toString();
       }
   }

   /**
    * Gets the uuids for nid.
    *
    * @param nid the nid
    * @return the uuids for nid
    * @throws NoSuchElementException if the nid is unknown
    */
   List<UUID> getUuidsForNid(int nid) throws NoSuchElementException;

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
    
    /**
     * This object type is normally set when the first object is written to the assemblage, so there 
     * is no need for calling this method directly. 
     * @param nid
     * @param assemblageNid the required assemblageNid for this nid.
     * @param objectType 
     * @param versionType
     * @throws IllegalStateException if the nid was already set up and the previous type(s) don't match, or the nid is unknown.
     */
    void setupNid(int nid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) throws IllegalStateException;
}

