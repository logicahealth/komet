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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;

/**
 * UUID
 * nid
 * sequence
 * @author kec
 */
@Contract
public interface IdentifierService2 {
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

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the chronology type for nid.
    *
    * @param nid the nid
    * @return the chronology type for nid
    */
   ObjectChronologyType getChronologyTypeForNid(int nid);

   /**
    * Gets the concept identifier for authority.
    *
    * @param conceptId the concept id
    * @param identifierAuthorityUuid the identifier authority uuid
    * @param stampCoordinate the stamp coordinate
    * @return the concept identifier for authority
    */
   Optional<String> getConceptIdentifierForAuthority(int conceptId,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate);

   /**
    * Gets the concept sequence for proxy.
    *
    * @param conceptProxy the concept proxy
    * @return the concept sequence for proxy
    */
   int getNidForProxy(ConceptSpecification conceptProxy);

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
    * Gets the uuid primordial from semantic id.
    *
    * @param id the semantic id
    * @return the uuid primordial from semantic id
    */
   Optional<UUID> getUuidPrimordialFromId(int id);

   /**
    * Gets the uuids for nid.
    *
    * @param nid the nid
    * @return the uuids for nid
    */
   List<UUID> getUuidsForNid(int nid);
}

