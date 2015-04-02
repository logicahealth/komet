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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.snapshot;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.TaxonomyService;
import gov.vha.isaac.ochre.api.TaxonomySnapshotService;
import gov.vha.isaac.ochre.api.chronicle.ChronicledObjectLocal;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.sememe.SememeService;
import gov.vha.isaac.ochre.api.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePosition;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.set.AbstractIntSet;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class Snapshot {
    
    private static final Logger log = LogManager.getLogger();

    LanguageCoordinate languageCoordinate;
    LogicCoordinate logicCoordinate;
    StampCoordinate stampCoordinate;
    TaxonomyCoordinate taxonomyCoordinate;
    RelativePositionCalculator positionCalculator;

    public Snapshot(LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate, StampCoordinate stampCoordinate, TaxonomyCoordinate taxonomyCoordinate) {
        this.languageCoordinate = languageCoordinate;
        this.logicCoordinate = logicCoordinate;
        this.stampCoordinate = stampCoordinate;
        this.taxonomyCoordinate = taxonomyCoordinate;
        this.positionCalculator = RelativePositionCalculator.getCalculator(stampCoordinate.getStampPosition());
    }

    public <V extends SememeVersion> SememeSnapshotService<V> getSememeSnapshotService(Class<V> type) {
        return LookupService.getService(SememeService.class).getSnapshot(type, stampCoordinate);
    }

    public TaxonomySnapshotService getTaxonomySnapshotService() {
        return LookupService.getService(TaxonomyService.class).getSnapshot(taxonomyCoordinate);
    }

    public <V extends StampedVersion> Stream<? extends V> getVisible(ChronicledObjectLocal<V> chronicle) {
        return chronicle.getVersions().stream().filter((V version) -> positionCalculator.onRoute(version));
    }
    
    public OpenIntHashSet getLatestStampSequences(IntStream stampSequenceStream) {       
        return stampSequenceStream.collect(OpenIntHashSet::new, 
                new LatestStampAccumulator(), 
                new LatestStampCombiner());
    }
    
    private class LatestStampAccumulator implements ObjIntConsumer<OpenIntHashSet> {

        @Override
        public void accept(OpenIntHashSet stampsForPosition, int stampToCompare) {
            handleStamp(stampsForPosition, stampToCompare);
        }
    
    }
    
    private class LatestStampCombiner implements BiConsumer<OpenIntHashSet,OpenIntHashSet> {

        @Override
        public void accept(OpenIntHashSet t, OpenIntHashSet u) {
            u.forEachKey((stampToTest) -> {
                handleStamp(t, stampToTest);
                return true;
            });
            u.clear();
            
            // can't find good documentation that specifies behaviour of BiConsumer
            // in this context, so am making sure both sets have the same values. 
            t.forEachKey((stamp) -> {
                u.add(stamp);
                return true;
            });
        }
        
    }

    public <V extends StampedVersion> Optional<LatestVersion<V>>
        getLatestVersion(ChronicledObjectLocal<V> chronicle) {

        HashSet<V> latestVersionSet = new HashSet<>();

        chronicle.getVersions().stream().filter((newVersionToTest) -> (newVersionToTest.getTime() != Long.MIN_VALUE)).filter((newVersionToTest) -> (positionCalculator.onRoute(newVersionToTest))).forEach((newVersionToTest) -> {
            if (latestVersionSet.isEmpty()) {
                latestVersionSet.add(newVersionToTest);
            } else {
                handlePart(latestVersionSet, newVersionToTest);
            }
        });
        List<V> latestVersionList =  new ArrayList<>(latestVersionSet);
        if (latestVersionList.isEmpty()) {
            return Optional.empty();
        } 
        if (latestVersionList.size() == 1) {
            return Optional.of(new LatestVersion(latestVersionList.get(1)));
        }
        
        return Optional.of(new LatestVersion(latestVersionList.get(1),
            latestVersionList.subList(2, latestVersionList.size())));
    }

    private int errorCount = 0;
    
    private <V extends StampedVersion> void handlePart(
            HashSet<V> partsForPosition, V part)  {
        // create a list of values so we don't have any 
        // concurrent modification issues with removing/adding
        // items to the partsForPosition. 
        List<V> partsToCompare = new ArrayList<>(partsForPosition);
        for (V prevPartToTest : partsToCompare) {
            switch (positionCalculator.fastRelativePosition(part,
                    prevPartToTest, stampCoordinate.getStampPrecedence())) {
                case AFTER:
                    partsForPosition.remove(prevPartToTest);
                    partsForPosition.add(part);
                    break;
                case BEFORE:
                    break;
                case CONTRADICTION:
                    partsForPosition.add(part);
                    break;
                case EQUAL:
                    // Can only have one part per time/path
                    // combination.
                    if (prevPartToTest.equals(part)) {
                        // part already added from another position.
                        // No need to add again.
                        break;
                    }
                    // Duplicate values encountered.
                    errorCount++;
                    if (errorCount < 5) {
                        log.warn("{0} should never happen. "
                                + "Data is malformed. sap: {1} Part:\n{2} \n  Part to test: \n{3}",
                                new Object[]{RelativePosition.EQUAL,
                                    part.getStampSequence(),
                                    part,
                                    prevPartToTest});
                    }
                    break;
                case UNREACHABLE:
                    // Should have failed mapper.onRoute(part)
                    // above.
                    throw new RuntimeException(
                            RelativePosition.UNREACHABLE
                            + " should never happen.");
            }
        }
    }

    private void handleStamp(
            OpenIntHashSet stampsForPosition, int stamp)  {
        // create a list of values so we don't have any 
        // concurrent modification issues with removing/adding
        // items to the stampsForPosition. 
        AbstractIntSet stampsToCompare = stampsForPosition.copy();
        stampsToCompare.forEachKey((prevStamp) -> {
            switch (positionCalculator.fastRelativePosition(stamp,
                    prevStamp, stampCoordinate.getStampPrecedence())) {
                case AFTER:
                    stampsForPosition.remove(prevStamp);
                    stampsForPosition.add(stamp);
                    break;
                case BEFORE:
                    break;
                case CONTRADICTION:
                    stampsForPosition.add(stamp);
                    break;
                case EQUAL:
                    // Can only have one stamp per time/path
                    // combination.
                    if (prevStamp == stamp) {
                        // stamp already added from another position.
                        // No need to add again.
                        break;
                    }
                    // Duplicate values encountered.
                    errorCount++;
                    if (errorCount < 5) {
                        log.warn("{0} should never happen. "
                                + "Data is malformed. stamp: {1} \n  Part to test: \n{2}",
                                new Object[]{RelativePosition.EQUAL,
                                    stamp,
                                    prevStamp});
                    }
                    break;
                case UNREACHABLE:
                    // Should have failed mapper.onRoute(stamp)
                    // above.
                    throw new RuntimeException(
                            RelativePosition.UNREACHABLE
                            + " should never happen.");
            }            return true;
        });

    }
}
