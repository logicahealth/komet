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



package sh.isaac.provider.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.AssemblageService;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.stream.VersionStream;
import sh.isaac.api.stream.VersionStreamWrapper;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SememeSnapshotProvider.
 *
 * @author kec
 * @param <V> the value type
 */
public class SememeSnapshotProvider<V extends SememeVersion>
         implements SememeSnapshotService<V> {
   /**
    * The version type.
    */
   Class<V> versionType;

   /**
    * The stamp coordinate.
    */
   StampCoordinate stampCoordinate;

   /**
    * The sememe provider.
    */
   AssemblageService sememeProvider;

   /**
    * The calculator.
    */
   RelativePositionCalculator calculator;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe snapshot provider.
    *
    * @param versionType the version type
    * @param stampCoordinate the stamp coordinate
    * @param sememeProvider the sememe provider
    */
   public SememeSnapshotProvider(Class<V> versionType,
                                 StampCoordinate stampCoordinate,
                                 AssemblageService sememeProvider) {
      this.versionType     = versionType;
      this.stampCoordinate = stampCoordinate;
      this.sememeProvider  = sememeProvider;
      this.calculator      = RelativePositionCalculator.getCalculator(stampCoordinate);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the latest description versions for component.
    *
    * @param componentNid the component nid
    * @return the latest description versions for component
    */
   @Override
   public Stream<LatestVersion<V>> getLatestDescriptionVersionsForComponent(int componentNid) {
      return getLatestSememeVersions(
          this.sememeProvider.getSememeSequencesForComponent(componentNid)
                             .parallelStream()
                             .filter(
                                 sememeSequence -> (this.sememeProvider.getSememe(sememeSequence)
                                       .getSememeType() == SememeType.DESCRIPTION)));
   }

   /**
    * Gets the latest sememe version.
    *
    * @param sememeSequenceOrNid the sememe sequence or nid
    * @return the latest sememe version
    */
   @Override
   public LatestVersion<V> getLatestSememeVersion(int sememeSequenceOrNid) {
      final SememeChronologyImpl sc = (SememeChronologyImpl) this.sememeProvider.getSememe(sememeSequenceOrNid);
      final IntStream               stampSequences  = sc.getVersionStampSequences();
      final StampSequenceSet        latestSequences = this.calculator.getLatestStampSequencesAsSet(stampSequences);

      if (latestSequences.isEmpty()) {
         return new LatestVersion<>();
      }

      final LatestVersion<V> latest = new LatestVersion<>(this.versionType);

      latestSequences.stream()
                     .forEach(
                         (stampSequence) -> {
                            latest.addLatest((V) sc.getVersionForStamp(stampSequence)
                                  .get());
                         });
      return latest;
   }

   /**
    * Gets the latest sememe versions.
    *
    * @param sememeSequenceStream the sememe sequence stream
    * @param progressTrackers the progress trackers
    * @return the latest sememe versions
    */
   private VersionStream<V> getLatestSememeVersions(IntStream sememeSequenceStream,
         ProgressTracker... progressTrackers) {
      return new VersionStreamWrapper<>(getLatestSememeVersionsUnwrapped(sememeSequenceStream, progressTrackers));
   }

   /**
    * Gets the latest sememe versions.
    *
    * @param sememeSequenceSet the sememe sequence set
    * @param progressTrackers the progress trackers
    * @return the latest sememe versions
    */
   private VersionStream<V> getLatestSememeVersions(SememeSequenceSet sememeSequenceSet,
         ProgressTracker... progressTrackers) {
      Arrays.stream(progressTrackers)
            .forEach(
                (tracker) -> {
                   tracker.addToTotalWork(sememeSequenceSet.size());
                });
      return getLatestSememeVersions(sememeSequenceSet.parallelStream(), progressTrackers);
   }

   /**
    * Gets the latest sememe versions for component.
    *
    * @param componentNid the component nid
    * @return the latest sememe versions for component
    */
   @Override
   public VersionStream<V> getLatestSememeVersionsForComponent(int componentNid) {
      return getLatestSememeVersions(this.sememeProvider.getSememeSequencesForComponent(componentNid));
   }

   /**
    * Gets the latest sememe versions for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the latest sememe versions for component from assemblage
    */
   @Override
   public VersionStream<V> getLatestSememeVersionsForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      return getLatestSememeVersions(
          this.sememeProvider.getSememeSequencesForComponentFromAssemblage(componentNid, assemblageConceptSequence));
   }

   /**
    * Gets the latest sememe versions from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param progressTrackers the progress trackers
    * @return the latest sememe versions from assemblage
    */
   @Override
   public VersionStream<V> getLatestSememeVersionsFromAssemblage(int assemblageConceptSequence,
         ProgressTracker... progressTrackers) {
      return getLatestSememeVersions(
          this.sememeProvider.getSememeSequencesFromAssemblage(assemblageConceptSequence),
          progressTrackers);
   }

   private Stream<LatestVersion<V>> getLatestSememeVersionsUnwrapped(IntStream sememeSequenceStream,
         ProgressTracker... progressTrackers) {
      return sememeSequenceStream.mapToObj(
          (int sememeSequence) -> {
             try {
                final SememeChronologyImpl sc = (SememeChronologyImpl) this.sememeProvider.getSememe(
                                                       sememeSequence);
                final IntStream stampSequences = sc.getVersionStampSequences();
                final StampSequenceSet latestStampSequences = this.calculator.getLatestStampSequencesAsSet(
                                                                  stampSequences);

                if (latestStampSequences.isEmpty()) {
                   return Optional.empty();
                }

                final LatestVersion<V> latest = new LatestVersion<>(this.versionType);

                latestStampSequences.stream()
                                    .forEach(
                                        (stampSequence) -> {
                                           latest.addLatest((V) sc.getVersionForStamp(stampSequence)
                                                 .get());
                                        });
                return Optional.of(latest);
             } finally {
                Arrays.stream(progressTrackers)
                      .forEach(
                          (tracker) -> {
                             tracker.completedUnitOfWork();
                          });
             }
          })
                                 .filter(
                                     (optional) -> {
                                        return optional.isPresent();
                                     })
                                 .map((optional) -> (LatestVersion<V>) optional.get());
   }
}

