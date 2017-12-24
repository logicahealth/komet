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
package sh.isaac.provider.datastore.chronology;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.AssemblageService;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.stream.VersionStream;
import sh.isaac.api.stream.VersionStreamWrapper;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- classes ----------------------------------------------------------------
/**
 * The Class AssemblageSnapshotProvider.
 *
 * @author kec
 * @param <V> the value type
 */
public class AssemblageSnapshotProvider<V extends SemanticVersion>
        implements SemanticSnapshotService<V> {

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
   AssemblageService semanticProvider;

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
   public AssemblageSnapshotProvider(Class<V> versionType,
           StampCoordinate stampCoordinate,
           AssemblageService sememeProvider) {
      this.versionType = versionType;
      this.stampCoordinate = stampCoordinate;
      this.semanticProvider = sememeProvider;
      this.calculator = RelativePositionCalculator.getCalculator(stampCoordinate);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the latest description versions for component.
    *
    * @param componentNid the component nid
    * @return the latest description versions for component
    */
   @Override
   public List<LatestVersion<V>> getLatestDescriptionVersionsForComponent(int componentNid) {
      List<LatestVersion<V>> results = new ArrayList<>();
      for (int semanticNid : this.semanticProvider.getSemanticNidsForComponent(componentNid).asArray()) {
         SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) this.semanticProvider.getSemanticChronology(semanticNid);
         if (semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
            results.add(this.getLatestSemanticVersion(semanticChronology));
         }
      }
      return results;
   }

   /**
    * Gets the latest sememe version.
    *
    * @param semanticNid the semantic nid
    * @return the latest semantic version
    */
   @Override
   public LatestVersion<V> getLatestSemanticVersion(int semanticNid) {
      final SemanticChronologyImpl sc = (SemanticChronologyImpl) this.semanticProvider.getSemanticChronology(semanticNid);
      return getLatestSemanticVersion(sc);
   }

   private LatestVersion<V> getLatestSemanticVersion(final SemanticChronologyImpl sc) {
      final int[] stampSequences = sc.getVersionStampSequences();
      final int[] latestSequences = this.calculator.getLatestStampSequencesAsSet(stampSequences);

      if (latestSequences.length == 0) {
         return new LatestVersion<>();
      }

      final LatestVersion<V> latest = new LatestVersion<>(this.versionType);

      for (int stampSequence : latestSequences) {
         latest.addLatest((V) sc.getVersionForStamp(stampSequence)
                 .get());
      }
      return latest;
   }

   /**
    * Gets the latest sememe versions.
    *
    * @param sememeSequenceStream the sememe sequence stream
    * @param progressTrackers the progress trackers
    * @return the latest sememe versions
    */
   private VersionStream<V> getLatestSemanticVersionStream(IntStream sememeSequenceStream,
           ProgressTracker... progressTrackers) {
      return new VersionStreamWrapper<>(getLatestSemanticVersionStreamUnwrapped(sememeSequenceStream, progressTrackers));
   }

   private List<LatestVersion<V>> getLatestSemanticVersionList(NidSet semanticNidSet,
           ProgressTracker... progressTrackers) {

      List<LatestVersion<V>> results = new ArrayList<>(semanticNidSet.size());
      for (int semanticNid : semanticNidSet.asArray()) {
         final SemanticChronologyImpl sc = (SemanticChronologyImpl) this.semanticProvider.getSemanticChronology(semanticNid);
         results.add(getLatestSemanticVersion(sc));
         for (ProgressTracker tracker: progressTrackers) {
            tracker.completedUnitOfWork();
         }
      }
      return results;
   }

   /**
    * Gets the latest sememe versions.
    *
    * @param semanticNidSet the sememe sequence set
    * @param progressTrackers the progress trackers
    * @return the latest sememe versions
    */
   private List<LatestVersion<V>> getLatestSemanticVersions(NidSet semanticNidSet,
           ProgressTracker... progressTrackers) {
      Arrays.stream(progressTrackers)
              .forEach((tracker) -> {
                 tracker.addToTotalWork(semanticNidSet.size());
              });
      return AssemblageSnapshotProvider.this.getLatestSemanticVersionList(semanticNidSet, progressTrackers);
   }

   /**
    * Gets the latest sememe versions for component.
    *
    * @param componentNid the component nid
    * @return the latest sememe versions for component
    */
   @Override
   public List<LatestVersion<V>> getLatestSemanticVersionsForComponent(int componentNid) {
      return getLatestSemanticVersions(this.semanticProvider.getSemanticNidsForComponent(componentNid));
   }

   /**
    * Gets the latest sememe versions for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the latest sememe versions for component from assemblage
    */
   @Override
   public List<LatestVersion<V>> getLatestSemanticVersionsForComponentFromAssemblage(int componentNid,
           int assemblageConceptSequence) {
      return getLatestSemanticVersions(this.semanticProvider.getSemanticNidsForComponentFromAssemblage(componentNid, assemblageConceptSequence));
   }

   /**
    * Gets the latest sememe versions from assemblage.
    *
    * @param assemblageConceptNid the assemblage concept sequence
    * @param progressTrackers the progress trackers
    * @return the latest sememe versions from assemblage
    */
   @Override
   public VersionStream<V> getLatestSemanticVersionsFromAssemblage(int assemblageConceptNid,
           ProgressTracker... progressTrackers) {
      return getLatestSemanticVersionStream(this.semanticProvider.getSemanticNidsFromAssemblage(assemblageConceptNid).stream(),
              progressTrackers);
   }

   private Stream<LatestVersion<V>> getLatestSemanticVersionStreamUnwrapped(IntStream semanticNidStream,
           ProgressTracker... progressTrackers) {
      return semanticNidStream.mapToObj((int semanticNid) -> {
         try {
            final SemanticChronologyImpl sc = (SemanticChronologyImpl) this.semanticProvider.getSemanticChronology(semanticNid);
            final int[] stampSequences = sc.getVersionStampSequences();
            final int[] latestStampSequences = this.calculator.getLatestStampSequencesAsSet(
                    stampSequences);

            if (latestStampSequences.length == 0) {
               return Optional.empty();
            }

            final LatestVersion<V> latest = new LatestVersion<>(this.versionType);

            for (int stampSequence : latestStampSequences) {
               latest.addLatest((V) sc.getVersionForStamp(stampSequence).get());
            }
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
