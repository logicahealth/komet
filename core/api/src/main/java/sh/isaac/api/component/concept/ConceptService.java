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



package sh.isaac.api.component.concept;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.DatabaseServices;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ConceptService.
 *
 * @author kec
 */
@Contract
public interface ConceptService
        extends DatabaseServices {
   /**
    * Write a concept to the concept service. Will not overwrite a concept if one already exists, rather it will
    * merge the written concept with the provided concept.
    *
    *
    * The persistence of the concept is dependent on the persistence
    * of the underlying service.
    * @param concept to be written.
    */
   void writeConcept(ConceptChronology<? extends ConceptVersion<?>> concept);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept.
    *
    * @param conceptId either a concept sequence or a concept nid.
    * @return the concept chronology associated with the identifier.
    */
   ConceptChronology<? extends ConceptVersion<?>> getConcept(int conceptId);
   
   /**
    * Gets the list of descriptions for a concept.
    *
    * @param conceptId either a concept sequence or a concept nid.
    * @return the list of descriptions.
    */
   default List<SememeChronology<? extends DescriptionSememe<?>>> getConceptDescriptions(int conceptId) {
      return getConcept(conceptId).getConceptDescriptionList();
   }

   /**
    * Gets the concept.
    *
    * @param conceptUuids a UUID that identifies a concept.
    * @return the concept chronology associated with the identifier.
    */
   ConceptChronology<? extends ConceptVersion<?>> getConcept(UUID... conceptUuids);

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
   Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream();

   /**
    * Gets the concept chronology stream.
    *
    * @param conceptSequences the concept sequences
    * @return the concept chronology stream
    */
   Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream(
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
   Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(int conceptId);

   /**
    * Use in circumstances when not all concepts may have been loaded.
    * @param conceptUuids uuids that identify the concept
    *
    * This implementation should not have a side effect of adding the UUID to any indexes, if the UUID isn't yet present.
    * @return an Optional ConceptChronology.
    */
   Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(UUID... conceptUuids);

   /**
    * Gets the parallel concept chronology stream.
    *
    * @return the parallel concept chronology stream
    */
   Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream();

   /**
    * Gets the parallel concept chronology stream.
    *
    * @param conceptSequences the concept sequences
    * @return the parallel concept chronology stream
    */
   Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream(
           ConceptSequenceSet conceptSequences);

   /**
    * Gets the snapshot.
    *
    * @param stampCoordinate the stamp coordinate
    * @param languageCoordinate the language coordinate
    * @return the snapshot
    */
   ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate);
}

