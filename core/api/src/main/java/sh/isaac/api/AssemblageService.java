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

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.externalizable.IsaacObjectType;

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
    * Gets the descriptions for component.
    *
    * @param componentNid the component nid
    * @return the descriptions for component
    * @TODO needs to integrate the language at some point...
    */
   List<SemanticChronology> getDescriptionsForComponent(int componentNid);

   /**
    * Gets the optional semantic chronology.
    *
    * @param semanticId sequence or nid for a semantic chronology
    * @return the identified {@code SemanticChronology}
    */
   Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticId);

   /**
    * Gets the SemanticChronology.
    *
    * @param semanticId sequence or nid for a SemanticChronology
    * @return the identified {@code SemanticChronology}
    */
   SemanticChronology getSemanticChronology(int semanticId);

   /**
    * Gets the SemanticChronology stream.
    *
    * @return the SemanticChronology stream
    */
   Stream<SemanticChronology> getSemanticChronologyStream();

   /**
    * Gets the SemanticChronology key stream.
    *
    * @return the SemanticChronology key stream
    */
   IntStream getSemanticNidStream();

   /**
    * 
    * @return count of all the semantic chronologies, active, or inactive. 
    */
   int getSemanticCount();

   /**
    * @param assemblageNid The nid for the assemblage to count elements from
    * @return count of all the semantic chronologies in the assemblage, active, or inactive. 
    */
   int getSemanticCount(int assemblageNid);

   
   /**
    * 
    * @param assemblageNid
    * @return the type of object contained within the assemblage. 
    */
   IsaacObjectType getObjectTypeForAssemblage(int assemblageNid);
   
   VersionType getVersionTypeForAssemblage(int assemblageNid);

   /**
    * Gets the SemanticChronology key stream.
    *
    * @param assemblageNid The nid for the assemblage to select the nids from
    * @return the SemanticChronology key stream
    */
   IntStream getSemanticNidStream(int assemblageNid);

   /**
    * Gets the SemanticChronology nids for component.
    *
    * @param componentNid the component nid
    * @return the SemanticChronology nids for component
    */
   NidSet getSemanticNidsForComponent(int componentNid);

   /**
    * Gets the SemanticChronology nids for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptNid the assemblage nid
    * @return the SemanticChronology nids for component from assemblage
    */
   NidSet getSemanticNidsForComponentFromAssemblage(int componentNid, int assemblageConceptNid);

   /**
    * Gets the SemanticChronology nids from assemblage.
    *
    * @param assemblageConceptNid the assemblage nid
    * @return the SemanticChronology sequences from assemblage
    */
   NidSet getSemanticNidsFromAssemblage(int assemblageConceptNid);

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
      return getReferencedComponentNidStreamFromAssemblage(conceptSpecification.getNid());
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
   
   int[] getAssemblageConceptNids(); 
   
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

   /**
    * 
    * @param assemblageNid
    * @return memory used in bytes
    */
    int getAssemblageMemoryInUse(int assemblageNid);
    /**
     * 
     * @param assemblageNid
     * @return disk space used in bytes
     */
    int getAssemblageSizeOnDisk(int assemblageNid);
}

