/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.sememe.provider;


import gov.vha.isaac.ochre.api.ProgressTracker;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author kec
 * @param <V>
 */
public class SememeSnapshotProvider<V extends SememeVersion<?>> implements SememeSnapshotService<V> {

    private static int descriptionAssemblageSequence = -1;

    private static int getDescriptionAssemblageSequence() {
        if (descriptionAssemblageSequence == -1) {
            //TODO support descriptions for langauges other than english
            descriptionAssemblageSequence = TermAux.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
        return descriptionAssemblageSequence;
    }

    Class<V> versionType;
    StampCoordinate stampCoordinate;
    SememeService sememeProvider;
    RelativePositionCalculator calculator;

    public SememeSnapshotProvider(Class<V> versionType, StampCoordinate stampCoordinate, SememeService sememeProvider) {
        this.versionType = versionType;
        this.stampCoordinate = stampCoordinate;
        this.sememeProvider = sememeProvider;
        this.calculator = RelativePositionCalculator.getCalculator(stampCoordinate);
    }

    @Override
    public Optional<LatestVersion<V>> getLatestSememeVersion(int sememeSequenceOrNid) {
        SememeChronologyImpl<?> sc = (SememeChronologyImpl<?>) sememeProvider.getSememe(sememeSequenceOrNid);
        IntStream stampSequences = sc.getVersionStampSequences();
        StampSequenceSet latestSequences = calculator.getLatestStampSequencesAsSet(stampSequences);
        if (latestSequences.isEmpty()) {
            return Optional.empty();
        }
        LatestVersion<V> latest = new LatestVersion<>(versionType);
        latestSequences.stream().forEach((stampSequence) -> {
            latest.addLatest((V) sc.getVersionForStamp(stampSequence).get());
        });

        return Optional.of(latest);
    }

    @Override
    public Stream<LatestVersion<V>> getLatestSememeVersionsFromAssemblage(int assemblageConceptSequence, ProgressTracker... progressTrackers) {
        return getLatestSememeVersions(sememeProvider.getSememeSequencesFromAssemblage(assemblageConceptSequence), progressTrackers);
    }

    private Stream<LatestVersion<V>> getLatestSememeVersions(SememeSequenceSet sememeSequenceSet, ProgressTracker... progressTrackers) {
        Arrays.stream(progressTrackers).forEach((tracker) -> {
            tracker.addToTotalWork(sememeSequenceSet.size());
        });
        return sememeSequenceSet.parallelStream()
                .mapToObj((int sememeSequence) -> {
                    try {
                        SememeChronologyImpl<?> sc = (SememeChronologyImpl<?>) sememeProvider.getSememe(sememeSequence);
                        IntStream stampSequences = sc.getVersionStampSequences();
                        StampSequenceSet latestStampSequences = calculator.getLatestStampSequencesAsSet(stampSequences);
                        if (latestStampSequences.isEmpty()) {
                            return Optional.empty();
                        }
                        LatestVersion<V> latest = new LatestVersion<>(versionType);
                        latestStampSequences.stream().forEach((stampSequence) -> {
                            latest.addLatest((V) sc.getVersionForStamp(stampSequence).get());
                        });
                        return Optional.of(latest);
                    } finally {
                        Arrays.stream(progressTrackers).forEach((tracker) -> {
                            tracker.completedUnitOfWork();
                        });
                    }
                }
                ).filter((optional) -> {
                    return optional.isPresent();
                }).map((optional) -> (LatestVersion<V>) optional.get());

    }

    @Override
    public Stream<LatestVersion<V>> getLatestSememeVersionsForComponent(int componentNid) {
        return getLatestSememeVersions(sememeProvider.getSememeSequencesForComponent(componentNid));
    }

    @Override
    public Stream<LatestVersion<V>> getLatestSememeVersionsForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
        return getLatestSememeVersions(sememeProvider.getSememeSequencesForComponentFromAssemblage(componentNid, assemblageConceptSequence));
    }

    @Override
    public Stream<LatestVersion<V>> getLatestDescriptionVersionsForComponent(int componentNid) {
        return getLatestSememeVersions(sememeProvider.getSememeSequencesForComponentFromAssemblage(componentNid, getDescriptionAssemblageSequence()));
    }
}
