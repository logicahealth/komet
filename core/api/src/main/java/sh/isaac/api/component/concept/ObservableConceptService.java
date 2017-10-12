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
package sh.isaac.api.component.concept;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

/**
 *
 * @author kec
 * TODO: determine if this class is necessary or overlooked. 
 */
public interface ObservableConceptService 
        extends SharedConceptService {
   /**
    * Write a concept to the concept service. Will not overwrite a concept if one already exists, rather it will
    * merge the written concept with the provided concept.
    *
    *
    * The persistence of the concept is dependent on the persistence
    * of the underlying service.
    * @param concept to be written.
    */
   void writeConcept(ConceptChronology concept);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept.
    *
    * @param conceptId either a concept sequence or a concept nid.
    * @return the concept chronology associated with the identifier.
    */
   ObservableConceptChronology getConcept(int conceptId);

   /**
    * Gets the concept.
    *
    * @param conceptUuids a UUID that identifies a concept.
    * @return the concept chronology associated with the identifier.
    */
   ObservableConceptChronology getConcept(UUID... conceptUuids);

   /**
    * Use in circumstances when not all concepts may have been loaded to find out if a concept is present,
    * without incurring the overhead of reading back the object.
    * @param conceptId Either a nid or concept sequence
    * @return true if present, false otherwise
    */
   boolean hasConcept(int conceptId);

   /**
    * Checks if concept active.
    *
    * @param conceptSequence the concept sequence
    * @param stampCoordinate the stamp coordinate
    * @return true, if concept active
    */
   boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate);

   /**
    * Gets the concept chronology stream.
    *
    * @return the concept chronology stream
    */
   Stream<ConceptChronology> getConceptChronologyStream();

   /**
    * Gets the concept chronology stream.
    *
    * @param conceptSequences the concept sequences
    * @return the concept chronology stream
    */
   Stream<ConceptChronology> getConceptChronologyStream(
           ConceptSequenceSet conceptSequences);

   /**
    * Gets the concept count.
    *
    * @return the concept count
    */
   int getConceptCount();

   /**
    * Gets the concept key parallel stream.
    *
    * @return the concept key parallel stream
    */
   IntStream getConceptKeyParallelStream();

   /**
    * Gets the concept key stream.
    *
    * @return the concept key stream
    */
   IntStream getConceptKeyStream();

   /**
    * Return the UUID that was generated for this datastore when the concept store was first created.
    *
    * @return the data store id
    */
   public UUID getDataStoreId();

   /**
    * Use in circumstances when not all concepts may have been loaded.
    * @param conceptId Either a nid or concept sequence
    * @return an Optional ConceptChronology.
    */
   Optional<? extends ObservableConceptChronology> getOptionalConcept(int conceptId);

   /**
    * Use in circumstances when not all concepts may have been loaded.
    * @param conceptUuids uuids that identify the concept
    *
    * This implementation should not have a side effect of adding the UUID to any indexes, if the UUID isn't yet present.
    * @return an Optional ConceptChronology.
    */
   Optional<? extends ObservableConceptChronology> getOptionalConcept(UUID... conceptUuids);

   /**
    * Gets the parallel concept chronology stream.
    *
    * @return the parallel concept chronology stream
    */
   Stream<ConceptChronology> getParallelConceptChronologyStream();

   /**
    * Gets the parallel concept chronology stream.
    *
    * @param conceptSequences the concept sequences
    * @return the parallel concept chronology stream
    */
   Stream<ConceptChronology> getParallelConceptChronologyStream(
           ConceptSequenceSet conceptSequences);

   /**
    * Gets the snapshot.
    *
    * @param manifoldCoordinate the stamp coordinate
    * @return the sh.isaac.api.component.concept.ConceptSnapshotService
    */
   ObservableConceptSnapshotService getSnapshot(ManifoldCoordinate manifoldCoordinate);
}

