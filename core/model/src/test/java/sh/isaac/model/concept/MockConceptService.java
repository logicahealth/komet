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
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.DatabaseServices.DatabaseValidity;
import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/3/16.
 */
@Service
public class MockConceptService
         implements ConceptService {
   ConcurrentHashMap<Integer, ConceptChronology<? extends ConceptVersion<?>>> conceptsMap = new ConcurrentHashMap<>();
   UUID                                                                       dbId        = UUID.randomUUID();

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      // Placeholder as databaseFolderExists always returns true.
   }

   @Override
   public void writeConcept(ConceptChronology<? extends ConceptVersion<?>> concept) {
      this.conceptsMap.put(concept.getConceptSequence(), concept);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConceptChronology<? extends ConceptVersion<?>> getConcept(int conceptId) {
      return this.conceptsMap.get(Get.identifierService()
                                .getConceptSequence(conceptId));
   }

   @Override
   public ConceptChronology<? extends ConceptVersion<?>> getConcept(UUID... conceptUuids) {
      final int conceptNid      = Get.identifierService()
                               .getNidForUuids(conceptUuids);
      final int conceptSequence = Get.identifierService()
                               .getConceptSequence(conceptNid);

      if (this.conceptsMap.containsKey(conceptSequence)) {
         return this.conceptsMap.get(Get.identifierService()
                                   .getConceptSequenceForUuids(conceptUuids));
      }

      final ConceptChronologyImpl concept = new ConceptChronologyImpl(conceptUuids[0], conceptNid, conceptSequence);

      if (conceptUuids.length > 1) {
         concept.setAdditionalUuids(Arrays.asList(Arrays.copyOfRange(conceptUuids, 1, conceptUuids.length)));
      }

      this.conceptsMap.put(conceptSequence, concept);
      return concept;
   }

   @Override
   public boolean hasConcept(int conceptId) {
      return this.conceptsMap.containsKey(Get.identifierService()
                                        .getConceptSequence(conceptId));
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      return false;
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream() {
      return this.conceptsMap.values()
                        .stream();
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream(
           ConceptSequenceSet conceptSequences) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getConceptCount() {
      return this.conceptsMap.size();
   }

   @Override
   public IntStream getConceptKeyParallelStream() {
      return this.conceptsMap.keySet()
                        .parallelStream()
                        .mapToInt(i -> i);
   }

   @Override
   public IntStream getConceptKeyStream() {
      return this.conceptsMap.keySet()
                        .stream()
                        .mapToInt(i -> i);
   }

   @Override
   public UUID getDataStoreId() {
      return this.dbId;
   }

   @Override
   public Path getDatabaseFolder() {
      return null;
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return null;
   }

   @Override
   public Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(int conceptId) {
      return Optional.ofNullable(getConcept(conceptId));
   }

   @Override
   public Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(UUID... conceptUuids) {
      return Optional.ofNullable(getConcept(conceptUuids));
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream() {
      return this.conceptsMap.values()
                        .parallelStream();
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream(
           ConceptSequenceSet conceptSequences) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
      throw new UnsupportedOperationException();
   }
}

