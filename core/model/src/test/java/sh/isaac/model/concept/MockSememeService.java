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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.Rank;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.component.semantic.SemanticConstraints;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticServiceTyped;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/3/16.
 */
@Service
@Rank(value = -50)
public class MockSememeService
         implements AssemblageService {
   /** The component sememe map. */
   ConcurrentHashMap<Integer, SemanticSequenceSet> componentSememeMap = new ConcurrentHashMap<>();

   /** The sememe map. */
   ConcurrentHashMap<Integer, SemanticChronology> sememeMap = new ConcurrentHashMap<>();

   //~--- methods -------------------------------------------------------------

   /**
    * Clear database validity value.
    */
   @Override
   public void clearDatabaseValidityValue() {
      // Placeholder as databaseFolderExists always returns true.
   }

   /**
    * Of type.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @return the sememe service typed
    */
   @Override
   public <V extends SemanticVersion> SemanticServiceTyped ofType(VersionType versionType) {
      throw new UnsupportedOperationException();
   }

   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    */
   @Override
   public void writeSemanticChronology(SemanticChronology sememeChronicle) {
      if (this.componentSememeMap.containsKey(sememeChronicle.getReferencedComponentNid())) {
         this.componentSememeMap.get(sememeChronicle.getReferencedComponentNid())
                                .add(sememeChronicle.getSemanticSequence());
      } else {
         final SemanticSequenceSet set = SemanticSequenceSet.of(sememeChronicle.getSemanticSequence());

         this.componentSememeMap.put(sememeChronicle.getReferencedComponentNid(), set);
      }

      this.sememeMap.put(sememeChronicle.getSemanticSequence(),
                         (SemanticChronology) sememeChronicle);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage types.
    *
    * @return the assemblage types
    */
   @Override
   public Stream<Integer> getAssemblageTypes() {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDatabaseFolder() {
      return null;
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return null;
   }

   /**
    * Gets the descriptions for component.
    *
    * @param componentNid the component nid
    * @return the descriptions for component
    */
   @Override
   public Stream<SemanticChronology> getDescriptionsForComponent(int componentNid) {
      final SemanticSequenceSet set = this.componentSememeMap.get(componentNid);
      final Stream.Builder<SemanticChronology> builder = Stream.builder();

      if (set != null) {
         set.stream().forEach((sememeSequence) -> {
                        final SemanticChronology sememeChronology = this.sememeMap.get(sememeSequence);

                        if (sememeChronology.getVersionType() == VersionType.DESCRIPTION) {
                           builder.accept(sememeChronology);
                        }
                     });
      }

      return builder.build();
   }

   /**
    * Gets the optional sememe.
    *
    * @param sememeId the sememe id
    * @return the optional sememe
    */
   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int sememeId) {
      return Optional.ofNullable(getSemanticChronology(sememeId));
   }

   /**
    * Gets the parallel sememe stream.
    *
    * @return the parallel sememe stream
    */
   @Override
   public Stream<SemanticChronology> getParallelSemanticChronologyStream() {
      return this.sememeMap.values()
                           .parallelStream();
   }

   /**
    * Gets the sememe.
    *
    * @param sememeId the sememe id
    * @return the sememe
    */
   @Override
   public SemanticChronology getSemanticChronology(int sememeId) {
      return this.sememeMap.get(Get.identifierService()
                                   .getSemanticSequence(sememeId));
   }

   /**
    * Checks for sememe.
    *
    * @param sememeId the sememe id
    * @return true, if successful
    */
   @Override
   public boolean hasSemanticChronology(int sememeId) {
      return this.sememeMap.containsKey(Get.identifierService()
            .getSemanticSequence(sememeId));
   }

   /**
    * Gets the sememe chronology stream.
    *
    * @return the sememe chronology stream
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return this.sememeMap.values()
                           .stream();
   }

   /**
    * Gets the sememe count.
    *
    * @return the sememe count
    */
   @Override
   public int getSemanticChronologyCount() {
      return this.sememeMap.size();
   }

   /**
    * Gets the sememe key parallel stream.
    *
    * @return the sememe key parallel stream
    */
   @Override
   public IntStream getSemanticChronologyKeyParallelStream() {
      return this.sememeMap.keySet()
                           .parallelStream()
                           .mapToInt(i -> i);
   }

   /**
    * Gets the sememe key stream.
    *
    * @return the sememe key stream
    */
   @Override
   public IntStream getSemanticChronologyKeyStream() {
      return this.sememeMap.keySet()
                           .stream()
                           .mapToInt(i -> i);
   }

   /**
    * Gets the sememe sequences for component.
    *
    * @param componentNid the component nid
    * @return the sememe sequences for component
    */
   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememe sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences for component from assemblage
    */
   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }


   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences from assemblage
    */
   @Override
   public SemanticSequenceSet getSemanticChronologySequencesFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes for component from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes from assemblage
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the snapshot.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @param stampCoordinate the stamp coordinate
    * @return the snapshot
    */
   @Override
   public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException();
   }
}

