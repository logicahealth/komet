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
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;

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
    * A method to remove refs to sememe or concept sequences that never had data stored.
    * This should not be necessary in normal operation.  This supports patterns where objects are
    * being deserialized from an ibdf file (causing refs to be stored here) but then not loaded into the DB.
    */
   void clearUnusedIds();

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
   Optional<LatestVersion<String>> getConceptIdentifierForAuthority(int conceptId,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate);

   /**
    * Gets the concept nid.
    *
    * @param conceptSequence the concept sequence
    * @return the concept nid
    */
   int getConceptNid(int conceptSequence);

   /**
    * Gets the concept nids for concept sequences.
    *
    * @param conceptSequences the concept sequences
    * @return the concept nids for concept sequences
    */
   IntStream getConceptNidsForConceptSequences(IntStream conceptSequences);

   /**
    * NOTE: this method will generate a new concept sequence if one does not already exist.
    * When retrieving concepts using the sequence, use the {@code ConceptService.getOptionalConcept(...)} to safely
    * retrieve concepts without the risk of null pointer exceptions if the concept is not yet written to the store
    * (as would be the case frequently when importing change sets, or loading a database).
    *
    * @param conceptNid the concept nid
    * @return a concept sequence for the provided conceptNid.
    */
   int getConceptSequence(int conceptNid);

   /**
    * Gets the concept sequence for proxy.
    *
    * @param conceptProxy the concept proxy
    * @return the concept sequence for proxy
    */
   int getConceptSequenceForProxy(ConceptSpecification conceptProxy);

   /**
    * Gets the concept sequence for uuids.
    *
    * @param uuids the uuids
    * @return the concept sequence for uuids
    */
   int getConceptSequenceForUuids(Collection<UUID> uuids);

   /**
    * Gets the concept sequence for uuids.
    *
    * @param uuids the uuids
    * @return the concept sequence for uuids
    */
   int getConceptSequenceForUuids(UUID... uuids);

   /**
    * Gets the concept sequence stream.
    *
    * @return the concept sequence stream
    */
   IntStream getConceptSequenceStream();

   /**
    * Gets the concept sequences for concept nids.
    *
    * @param conceptNidArray the concept nid array
    * @return the concept sequences for concept nids
    */
   ConceptSequenceSet getConceptSequencesForConceptNids(int[] conceptNidArray);

   /**
    * Gets the concept sequences for concept nids.
    *
    * @param componentNidSet the component nid set
    * @return the concept sequences for concept nids
    */
   ConceptSequenceSet getConceptSequencesForConceptNids(NidSet componentNidSet);

   /**
    * Gets the identifier for authority.
    *
    * @param nid the nid
    * @param identifierAuthorityUuid the identifier authority uuid
    * @param stampCoordinate the stamp coordinate
    * @return the identifier for authority
    */
   Optional<LatestVersion<String>> getIdentifierForAuthority(int nid,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate);

   /**
    * Gets the max nid.
    *
    * @return the maximum native identifier currently assigned.
    */
   int getMaxNid();

   /**
    * Gets the nid for proxy.
    *
    * @param conceptProxy the concept proxy
    * @return the nid for proxy
    */
   int getNidForProxy(ConceptSpecification conceptProxy);

   /**
    * Gets the nid for uuids.
    *
    * @param uuids the uuids
    * @return the nid for uuids
    */
   int getNidForUuids(Collection<UUID> uuids);

   /**
    * Gets the nid for uuids.
    *
    * @param uuids the uuids
    * @return the nid for uuids
    */
   int getNidForUuids(UUID... uuids);

   /**
    * Gets the parallel concept sequence stream.
    *
    * @return the parallel concept sequence stream
    */
   IntStream getParallelConceptSequenceStream();

   /**
    * Gets the parallel sememe sequence stream.
    *
    * @return the parallel sememe sequence stream
    */
   IntStream getParallelSememeSequenceStream();

   /**
    * Gets the sememe nid.
    *
    * @param sememeId the sememe id
    * @return the sememe nid
    */
   int getSememeNid(int sememeId);

   /**
    * Gets the sememe nids for sememe sequences.
    *
    * @param sememSequences the semem sequences
    * @return the sememe nids for sememe sequences
    */
   IntStream getSememeNidsForSememeSequences(IntStream sememSequences);

   /**
    * NOTE: this method will generate a new sememe sequence if one does not already exist.
    * When retrieving sememes using the sequence, use the {@code SememeService.getOptionalSememe(int sememeSequence)} to safely
    * retrieve sememes without the risk of null pointer exceptions if the sememe is not yet written to the store
    * (as would be the case frequently when importing change sets, or loading a database).
    *
    * @param sememeId the sememe id
    * @return a concept sequence for the provided sememeId.
    */
   int getSememeSequence(int sememeId);

   /**
    * Gets the sememe sequence for uuids.
    *
    * @param uuids the uuids
    * @return the sememe sequence for uuids
    */
   int getSememeSequenceForUuids(Collection<UUID> uuids);

   /**
    * Gets the sememe sequence for uuids.
    *
    * @param uuids the uuids
    * @return the sememe sequence for uuids
    */
   int getSememeSequenceForUuids(UUID... uuids);

   /**
    * Gets the sememe sequence stream.
    *
    * @return the sememe sequence stream
    */
   IntStream getSememeSequenceStream();

   /**
    * Gets the sememe sequences for sememe nids.
    *
    * @param sememeNidArray the sememe nid array
    * @return the sememe sequences for sememe nids
    */
   SememeSequenceSet getSememeSequencesForSememeNids(int[] sememeNidArray);

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
    * Gets the uuid primordial from concept id.
    *
    * @param conceptId the concept id
    * @return the uuid primordial from concept id
    */
   Optional<UUID> getUuidPrimordialFromConceptId(int conceptId);

   /**
    * Gets the uuid primordial from sememe id.
    *
    * @param sememeId the sememe id
    * @return the uuid primordial from sememe id
    */
   Optional<UUID> getUuidPrimordialFromSememeId(int sememeId);

   /**
    * Gets the uuids for nid.
    *
    * @param nid the nid
    * @return the uuids for nid
    */
   List<UUID> getUuidsForNid(int nid);
}

