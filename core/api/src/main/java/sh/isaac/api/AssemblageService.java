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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticServiceTyped;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface AssemblageService.
 *
 * @author kec
 */
@Contract
public interface AssemblageService
        extends DatabaseServices {
   /**
    * Of type.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @return the assemblage service typed
    */
   <V extends SemanticVersion> SemanticServiceTyped ofType(VersionType versionType);

   /**
    * Write a SemanticChronology to the assemblage service. Will not overwrite a SemanticChronology if one already exists, rather it will
    * merge the written SemanticChronology with the provided semantic.
    *
    *
    * The persistence of the concept is dependent on the persistence
    * of the underlying service.
    *
    * @param semanticChronicle the SemanticChronology 
    */
   void writeSemanticChronology(SemanticChronology semanticChronicle);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage types.
    *
    * @return the sequence identifiers of all assemblage concepts that are actually in use by a semantic
    */
   Stream<Integer> getAssemblageTypes();

   /**
    * Gets the descriptions for component.
    *
    * @param componentNid the component nid
    * @return the descriptions for component
    */
   Stream<SemanticChronology> getDescriptionsForComponent(int componentNid);

   /**
    * Gets the optional semantic chronology.
    *
    * @param semanticId sequence or nid for a semantic chronology
    * @return the identified {@code SemanticChronology}
    */
   Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticId);

   /**
    * Gets the parallel semantic stream.
    *
    * @return the parallel semantic  stream
    */
   Stream<SemanticChronology> getParallelSemanticChronologyStream();

   /**
    * Gets the SemanticChronology.
    *
    * @param semanticId sequence or nid for a SemanticChronology
    * @return the identified {@code SemanticChronology}
    */
   SemanticChronology getSemanticChronology(int semanticId);

   /**
    * Use in circumstances when not all SemanticChronologys may have been loaded to find out if a SemanticChronology is present,
    * without incurring the overhead of reading back the object.
    * @param semanticId Either a nid or SemanticChronology sequence
    * @return true if present, false otherwise
    */
   boolean hasSemantic(int semanticId);

   /**
    * Gets the SemanticChronology stream.
    *
    * @return the SemanticChronology stream
    */
   Stream<SemanticChronology> getSemanticChronologyStream();

   /**
    * Gets the SemanticChronology count.
    *
    * @return the SemanticChronology count
    */
   int getSemanticChronologyCount();

   /**
    * Gets the SemanticChronology key parallel stream.
    *
    * @return the SemanticChronology key parallel stream
    */
   IntStream getSemanticChronologyKeyParallelStream();

   /**
    * Gets the SemanticChronology key stream.
    *
    * @return the SemanticChronology key stream
    */
   IntStream getSemanticChronologyKeyStream();

   /**
    * Gets the SemanticChronology sequences for component.
    *
    * @param componentNid the component nid
    * @return the SemanticChronology sequences for component
    */
   SemanticSequenceSet getSemanticChronologySequencesForComponent(int componentNid);

   /**
    * Gets the SemanticChronology sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the SemanticChronology sequences for component from assemblage
    */
   SemanticSequenceSet getSemanticChronologySequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence);

   /**
    * Gets the SemanticChronology sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the SemanticChronology sequences from assemblage
    */
   SemanticSequenceSet getSemanticChronologySequencesFromAssemblage(int assemblageConceptSequence);

   /**
    * Gets the SemanticChronology for component.
    *
    * @param <C>
    * @param componentNid the component nid
    * @return the SemanticChronology for component
    */
   <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponent(int componentNid);

   /**
    * Gets the SemanticChronology for component from assemblage.
    *
    * @param <C>
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the SemanticChronologies for component from assemblage
    */
   <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence);

   /**
    * Gets the SemanticChronologies from assemblage.
    *
    * @param <C>
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the SemanticChronologies from assemblage
    */
   <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamFromAssemblage(int assemblageConceptSequence);

   /**
    * Gets the referenced component nids from assemblage.
    *
    * @param conceptSpecification the assemblage concept specification
    * @return the referenced component nids as an IntStream
    */
   default IntStream getReferencedComponentNidStreamFromAssemblage(ConceptSpecification conceptSpecification) {
      return getReferencedComponentNidStreamFromAssemblage(conceptSpecification.getConceptSequence());
   }
   
   /**
    * Gets the referenced component nids from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the referenced component nids as an IntStream
    */
   default IntStream getReferencedComponentNidStreamFromAssemblage(int assemblageConceptSequence) {
      return getSemanticChronologyStreamFromAssemblage(assemblageConceptSequence).mapToInt((semantic) -> semantic.getReferencedComponentNid());
   }
   
   
   /**
    * Gets the snapshot.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @param stampCoordinate the stamp coordinate
    * @return the snapshot
    */
   <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate);
}

