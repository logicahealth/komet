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
   ConcurrentHashMap<Integer, SememeSequenceSet> componentSememeMap                   = new ConcurrentHashMap<>();
   ConcurrentHashMap<Integer, SememeChronology<? extends SememeVersion<?>>> sememeMap = new ConcurrentHashMap<>();

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      // Placeholder as databaseFolderExists always returns true.
   }

   @Override
   public <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType) {
      throw new UnsupportedOperationException();
   }

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

   @Override
   public Stream<Integer> getAssemblageTypes() {
      throw new UnsupportedOperationException();
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

   @Override
   public Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeId) {
      return Optional.ofNullable(getSememe(sememeId));
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream() {
      return this.sememeMap.values()
                      .parallelStream();
   }

   @Override
   public SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId) {
      return this.sememeMap.get(Get.identifierService()
                              .getSememeSequence(sememeId));
   }

   @Override
   public boolean hasSememe(int sememeId) {
      return this.sememeMap.containsKey(Get.identifierService()
                                      .getSememeSequence(sememeId));
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememeChronologyStream() {
      return this.sememeMap.values()
                      .stream();
   }

   @Override
   public int getSememeCount() {
      return this.sememeMap.size();
   }

   @Override
   public IntStream getSememeKeyParallelStream() {
      return this.sememeMap.keySet()
                      .parallelStream()
                      .mapToInt(i -> i);
   }

   @Override
   public IntStream getSememeKeyStream() {
      return this.sememeMap.keySet()
                      .stream()
                      .mapToInt(i -> i);
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageConceptSequence,
         StampPosition position) {
      throw new UnsupportedOperationException();
   }

   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   @Override
   public <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException();
   }
}

