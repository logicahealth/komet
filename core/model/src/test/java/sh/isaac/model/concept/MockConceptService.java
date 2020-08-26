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



package sh.isaac.model.concept;

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Path;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.component.concept.*;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.StampFilterImmutable;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/3/16.
 */
@Service
public class MockConceptService
         implements ConceptService {
   /** The concepts map. */
   ConcurrentHashMap<Integer, ConceptChronology> conceptsMap = new ConcurrentHashMap<>();

   /** The db id. */
   UUID dbId = UUID.randomUUID();

   //~--- methods -------------------------------------------------------------

   /**
    * Write concept.
    *
    * @param concept the concept
    */
   @Override
   public void writeConcept(ConceptChronology concept) {
      this.conceptsMap.put(concept.getNid(), concept);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept.
    *
    * @param conceptId the concept id
    * @return the concept
    */
   @Override
   public ConceptChronology getConceptChronology(int conceptId) {
      return this.conceptsMap.get(conceptId);
   }

   /**
    * Gets the concept.
    *
    * @param conceptUuids the concept uuids
    * @return the concept
    */
   @Override
   public ConceptChronology getConceptChronology(UUID... conceptUuids) {
      final int conceptNid      = Get.identifierService()
                                     .getNidForUuids(conceptUuids);

      if (this.conceptsMap.containsKey(conceptNid)) {
         return this.conceptsMap.get(conceptNid);
      }

      final ConceptChronologyImpl concept = new ConceptChronologyImpl(conceptUuids[0], 0);

      if (conceptUuids.length > 1) {
         concept.setAdditionalUuids(Arrays.asList(Arrays.copyOfRange(conceptUuids, 1, conceptUuids.length)));
      }

      this.conceptsMap.put(conceptNid, concept);
      return concept;
   }

   /**
    * Checks if concept active.
    *
    * @param conceptSequence the concept nid
    * @param stampFilter the stamp coordinate
    * @return true, if concept active
    */
   @Override
   public boolean isConceptActive(int conceptSequence, StampFilterImmutable stampFilter) {
      return false;
   }

   /**
    * Gets the concept chronology stream.
    *
    * @return the concept chronology stream
    */
   @Override
   public Stream<ConceptChronology> getConceptChronologyStream() {
      return this.conceptsMap.values()
                             .stream();
   }

   /**
    * Gets the concept count.
    *
    * @return the concept count
    */
   @Override
   public int getConceptCount() {
      return this.conceptsMap.size();
   }


   /**
    * Gets the concept key stream.
    *
    * @return the concept key stream
    */
   @Override
   public IntStream getConceptNidStream() {
      return this.conceptsMap.keySet()
                             .stream()
                             .mapToInt(i -> i);
   }

   /**
    * Gets the data store id.
    *
    * @return the data store id
    */
   @Override
   public Optional<UUID> getDataStoreId() {
      return Optional.of(this.dbId);
   }

   /**
    * Gets the optional concept.
    *
    * @param conceptId the concept id
    * @return the optional concept
    */
   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(int conceptId) {
      return Optional.ofNullable(getConceptChronology(conceptId));
   }

   /**
    * Gets the optional concept.
    *
    * @param conceptUuids the concept uuids
    * @return the optional concept
    */
   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
      return Optional.ofNullable(getConceptChronology(conceptUuids));
   }

   /**
    * Gets the snapshot.
    *
    * @param manifoldCoordinate the stamp coordinate
    * @return the sh.isaac.api.component.concept.ConceptSnapshotService
    */
   @Override
   public ConceptSnapshotService getSnapshot(ManifoldCoordinateImmutable manifoldCoordinate) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ConceptChronology getConceptChronology(ConceptSpecification conceptSpecification) {
      return getConceptChronology(conceptSpecification.getNid());
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(IntSet conceptNids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(int assemblageNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getConceptCount(int assemblageNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getConceptNidStream(int assemblageNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean hasConcept(int conceptId) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptSnapshot getConceptSnapshot(int conceptNid, ManifoldCoordinate manifoldCoordinate) {
      return null;
   }
}

