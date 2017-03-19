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

import sh.isaac.api.DatabaseServices.DatabaseValidity;
import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeConstraints;
import sh.isaac.api.component.sememe.SememeService;
import sh.isaac.api.component.sememe.SememeServiceTyped;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/3/16.
 */
@Service
@Rank(value = -50)
public class MockSememeService
         implements SememeService {
   
   /** The component sememe map. */
   ConcurrentHashMap<Integer, SememeSequenceSet> componentSememeMap                   = new ConcurrentHashMap<>();
   
   /** The sememe map. */
   ConcurrentHashMap<Integer, SememeChronology<? extends SememeVersion<?>>> sememeMap = new ConcurrentHashMap<>();

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
   public <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType) {
      throw new UnsupportedOperationException();
   }

   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    * @param constraints the constraints
    */
   @Override
   public void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints) {
      if (this.componentSememeMap.containsKey(sememeChronicle.getReferencedComponentNid())) {
         this.componentSememeMap.get(sememeChronicle.getReferencedComponentNid())
                           .add(sememeChronicle.getSememeSequence());
      } else {
         final SememeSequenceSet set = SememeSequenceSet.of(sememeChronicle.getSememeSequence());

         this.componentSememeMap.put(sememeChronicle.getReferencedComponentNid(), set);
      }

      this.sememeMap.put(sememeChronicle.getSememeSequence(),
                    (SememeChronology<? extends SememeVersion<?>>) sememeChronicle);
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
   public Stream<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionsForComponent(int componentNid) {
      final SememeSequenceSet                                                set     = this.componentSememeMap.get(componentNid);
      final Stream.Builder<SememeChronology<? extends DescriptionSememe<?>>> builder = Stream.builder();

      if (set != null) {
         set.stream().forEach((sememeSequence) -> {
                        final SememeChronology sememeChronology = this.sememeMap.get(sememeSequence);

                        if (sememeChronology.getSememeType() == SememeType.DESCRIPTION) {
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
   public Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeId) {
      return Optional.ofNullable(getSememe(sememeId));
   }

   /**
    * Gets the parallel sememe stream.
    *
    * @return the parallel sememe stream
    */
   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream() {
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
   public SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId) {
      return this.sememeMap.get(Get.identifierService()
                              .getSememeSequence(sememeId));
   }

   /**
    * Checks for sememe.
    *
    * @param sememeId the sememe id
    * @return true, if successful
    */
   @Override
   public boolean hasSememe(int sememeId) {
      return this.sememeMap.containsKey(Get.identifierService()
                                      .getSememeSequence(sememeId));
   }

   /**
    * Gets the sememe chronology stream.
    *
    * @return the sememe chronology stream
    */
   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememeChronologyStream() {
      return this.sememeMap.values()
                      .stream();
   }

   /**
    * Gets the sememe count.
    *
    * @return the sememe count
    */
   @Override
   public int getSememeCount() {
      return this.sememeMap.size();
   }

   /**
    * Gets the sememe key parallel stream.
    *
    * @return the sememe key parallel stream
    */
   @Override
   public IntStream getSememeKeyParallelStream() {
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
   public IntStream getSememeKeyStream() {
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
   public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
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
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememe sequences for component from assemblages.
    *
    * @param componentNid the component nid
    * @param allowedAssemblageSequences the allowed assemblage sequences
    * @return the sememe sequences for component from assemblages
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememe sequences for components from assemblage.
    *
    * @param componentNidSet the component nid set
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences for components from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememe sequences for components from assemblage modified after position.
    *
    * @param componentNidSet the component nid set
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param position the position
    * @return the sememe sequences for components from assemblage modified after position
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageConceptSequence,
         StampPosition position) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid) {
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
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes for component from assemblages.
    *
    * @param componentNid the component nid
    * @param allowedAssemblageSequences the allowed assemblage sequences
    * @return the sememes for component from assemblages
    */
   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes from assemblage
    */
   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence) {
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
   public <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException();
   }
}

