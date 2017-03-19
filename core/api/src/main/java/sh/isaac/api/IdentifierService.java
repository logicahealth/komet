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
 *
 * @author kec
 */
@Contract
public interface IdentifierService
        extends DatabaseServices {
   static final int FIRST_NID = Integer.MIN_VALUE + 1;

   //~--- methods -------------------------------------------------------------

   void addUuidForNid(UUID uuid, int nid);

   /**
    * A method to remove refs to sememe or concept sequences that never had data stored.
    * This should not be necessary in normal operation.  This supports patterns where objects are
    * being deserialized from an ibdf file (causing refs to be stored here) but then not loaded into the DB.
    */
   void clearUnusedIds();

   //~--- get methods ---------------------------------------------------------

   ObjectChronologyType getChronologyTypeForNid(int nid);

   Optional<LatestVersion<String>> getConceptIdentifierForAuthority(int conceptId,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate);

   int getConceptNid(int conceptSequence);

   IntStream getConceptNidsForConceptSequences(IntStream conceptSequences);

   /**
    * NOTE: this method will generate a new concept sequence if one does not already exist.
    * When retrieving concepts using the sequence, use the {@code ConceptService.getOptionalConcept(...)} to safely
    * retrieve concepts without the risk of null pointer exceptions if the concept is not yet written to the store
    * (as would be the case frequently when importing change sets, or loading a database).
    * @param conceptNid
    * @return a concept sequence for the provided conceptNid.
    */
   int getConceptSequence(int conceptNid);

   int getConceptSequenceForProxy(ConceptSpecification conceptProxy);

   int getConceptSequenceForUuids(Collection<UUID> uuids);

   int getConceptSequenceForUuids(UUID... uuids);

   IntStream getConceptSequenceStream();

   ConceptSequenceSet getConceptSequencesForConceptNids(int[] conceptNidArray);

   ConceptSequenceSet getConceptSequencesForConceptNids(NidSet componentNidSet);

   Optional<LatestVersion<String>> getIdentifierForAuthority(int nid,
         UUID identifierAuthorityUuid,
         StampCoordinate stampCoordinate);

   /**
    *
    * @return the maximum native identifier currently assigned.
    */
   int getMaxNid();

   int getNidForProxy(ConceptSpecification conceptProxy);

   int getNidForUuids(Collection<UUID> uuids);

   int getNidForUuids(UUID... uuids);

   IntStream getParallelConceptSequenceStream();

   IntStream getParallelSememeSequenceStream();

   int getSememeNid(int sememeId);

   IntStream getSememeNidsForSememeSequences(IntStream sememSequences);

   /**
    * NOTE: this method will generate a new sememe sequence if one does not already exist.
    * When retrieving sememes using the sequence, use the {@code SememeService.getOptionalSememe(int sememeSequence)} to safely
    * retrieve sememes without the risk of null pointer exceptions if the sememe is not yet written to the store
    * (as would be the case frequently when importing change sets, or loading a database).
    * @param sememeId
    * @return a concept sequence for the provided sememeId.
    */
   int getSememeSequence(int sememeId);

   int getSememeSequenceForUuids(Collection<UUID> uuids);

   int getSememeSequenceForUuids(UUID... uuids);

   IntStream getSememeSequenceStream();

   SememeSequenceSet getSememeSequencesForSememeNids(int[] sememeNidArray);

   boolean hasUuid(Collection<UUID> uuids);

   boolean hasUuid(UUID... uuids);

   default UUID[] getUuidArrayForNid(int nid) {
      List<UUID> uuids = getUuidsForNid(nid);

      return uuids.toArray(new UUID[uuids.size()]);
   }

   Optional<UUID> getUuidPrimordialForNid(int nid);

   Optional<UUID> getUuidPrimordialFromConceptId(int conceptId);

   Optional<UUID> getUuidPrimordialFromSememeId(int sememeId);

   List<UUID> getUuidsForNid(int nid);
}

