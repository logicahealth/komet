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
package gov.vha.isaac.ochre.api.snapshot.calculator;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.PathService;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.roaringbitmap.RoaringBitmap;

/**
 *
 * @author kec
 */
public class RelativePositionCalculator {
    
    private static final Logger log = LogManager.getLogger();

    private static PathService pathService = null;
    
    private static PathService getPathService() {
        if (pathService == null) {
            pathService = LookupService.getService(PathService.class);
        }
        return pathService;
    }

    private static CommitService commitManager;

    private static CommitService getCommitService() {
        if (commitManager == null) {
            commitManager = LookupService.getService(CommitService.class);
        }
        return commitManager;
    }

    private static final ConcurrentHashMap<StampCoordinate, RelativePositionCalculator> calculatorCache =
            new ConcurrentHashMap<>();

    public static RelativePositionCalculator getCalculator(StampCoordinate coordinate) {
        RelativePositionCalculator pm = calculatorCache.get(coordinate);

        if (pm != null) {
            return pm;
        }

        pm = new RelativePositionCalculator(coordinate);

        RelativePositionCalculator existing = calculatorCache.putIfAbsent(coordinate, pm);

        if (existing != null) {
            pm = existing;
        }

        return pm;
    }
    StampCoordinate coordinate;
    /**
     * Mapping from pathNid to each segment for that pathNid.
     * There is one entry for each path reachable antecedent to the destination 
     * position of the computer. 
     */
    OpenIntObjectHashMap<Segment> pathNidSegmentMap;

    public RelativePositionCalculator(StampCoordinate coordinate) {
        this.coordinate = coordinate;
        this.pathNidSegmentMap = setupPathNidSegmentMap(coordinate.getStampPosition());
    }

    private static class Segment {

        /**
         * Each segment gets it's own sequence which gets greater the further
         * prior to the position of the relative position computer. 
         * TODO if we have a path sequence, may not need segment sequence. 
         */
        int segmentSequence;
        
        /**
         * The pathConceptSequence of this segment. Each ancestor path to the position of the 
         * computer gets it's own segment. 
         */
        int pathConceptSequence;
        /**
         * The end time of the position of the relative position computer. 
         * stamps with times after the end time are not part of the 
         * path. 
         */
        long endTime;
        
        RoaringBitmap precedingSegments;

        public Segment(int segmentSequence, int pathConceptSequence, long endTime, RoaringBitmap precedingSegments) {
            this.segmentSequence = segmentSequence;
            this.pathConceptSequence = pathConceptSequence;
            this.endTime = endTime;
            this.precedingSegments = new RoaringBitmap();
            this.precedingSegments.or(precedingSegments);
        }

        // Could check for modules here...
        public boolean containsPosition(int pathConceptSequence, long time) {
            if (this.pathConceptSequence == pathConceptSequence && time != Long.MIN_VALUE) {
                return time <= endTime;
            }
            return false;
        }
    }

    private static OpenIntObjectHashMap<Segment>  setupPathNidSegmentMap(StampPosition destination) {
        OpenIntObjectHashMap<Segment> pathNidSegmentMap = new OpenIntObjectHashMap<>();
        AtomicInteger segmentSequence = new AtomicInteger(0);
        
        // the sequence of the preceding segments is set in the recursive 
        // call.
        RoaringBitmap precedingSegments = new RoaringBitmap();
        
        // call to recursive method...
        addOriginsToPathNidSegmentMap(destination, pathNidSegmentMap, segmentSequence, precedingSegments);

        return pathNidSegmentMap;

    }

    // recursively called method
    private static void addOriginsToPathNidSegmentMap(StampPosition destination,
            OpenIntObjectHashMap<Segment> pathNidSegmentMap, AtomicInteger segmentSequence, RoaringBitmap precedingSegments) {
        Segment segment = new Segment(segmentSequence.getAndIncrement(), destination.getStampPathSequence(),
                destination.getTime(), precedingSegments);
        // precedingSegments is cumulative, each recursive call adds another
        precedingSegments.add(segment.segmentSequence);
        pathNidSegmentMap.put(destination.getStampPathSequence(), segment);
        getPathService().getStampPath(destination.getStampPathSequence()).
                getPathOrigins().stream().forEach((origin) -> {
            // Recursive call
            addOriginsToPathNidSegmentMap(origin, pathNidSegmentMap, segmentSequence, precedingSegments);
        });
    }

    public RelativePosition fastRelativePosition(StampedVersion v1, StampedVersion v2, StampPrecedence precedencePolicy) {
        if (v1.getPathSequence() == v2.getPathSequence()) {
            Segment seg = (Segment) pathNidSegmentMap.get(v1.getPathSequence());
            if (seg.containsPosition(v1.getPathSequence(), v1.getTime())
                    && seg.containsPosition(v2.getPathSequence(), v2.getTime())) {
                if (v1.getTime() < v2.getTime()) {
                    return RelativePosition.BEFORE;
                }
                if (v1.getTime() > v2.getTime()) {
                    return RelativePosition.AFTER;
                }
                if (v1.getTime() == v2.getTime()) {
                    return RelativePosition.EQUAL;
                }
            }
            return RelativePosition.UNREACHABLE;
        }

        Segment seg1 = (Segment) pathNidSegmentMap.get(v1.getPathSequence());
        Segment seg2 = (Segment) pathNidSegmentMap.get(v2.getPathSequence());
        if (seg1 == null || seg2 == null) {
            return RelativePosition.UNREACHABLE;
        }
        if (!(seg1.containsPosition(v1.getPathSequence(), v1.getTime())
                && seg2.containsPosition(v2.getPathSequence(), v2.getTime()))) {
            return RelativePosition.UNREACHABLE;
        }
        if (precedencePolicy == StampPrecedence.TIME) {
            if (v1.getTime() < v2.getTime()) {
                return RelativePosition.BEFORE;
            }
            if (v1.getTime() > v2.getTime()) {
                return RelativePosition.AFTER;
            }
            if (v1.getTime() == v2.getTime()) {
                return RelativePosition.EQUAL;
            }
        }
        if (seg1.precedingSegments.contains(seg2.segmentSequence)) {
            return RelativePosition.BEFORE;
        }
        if (seg2.precedingSegments.contains(seg1.segmentSequence)) {
            return RelativePosition.AFTER;
        }
        return RelativePosition.CONTRADICTION;
    }
    public RelativePosition fastRelativePosition(int stampSequence1, int stampSequence2, StampPrecedence precedencePolicy) {
        long ss1Time = getCommitService().getTimeForStamp(stampSequence1);
        int ss1PathSequence = getCommitService().getPathSequenceForStamp(stampSequence1);
        long ss2Time = getCommitService().getTimeForStamp(stampSequence2);
        int ss2PathSequence = getCommitService().getPathSequenceForStamp(stampSequence2);
        
        if (ss1PathSequence == ss2PathSequence) {
            Segment seg = (Segment) pathNidSegmentMap.get(ss1PathSequence);
            if (seg.containsPosition(ss1PathSequence, ss1Time)
                    && seg.containsPosition(ss2PathSequence, ss2Time)) {
                if (ss1Time < ss2Time) {
                    return RelativePosition.BEFORE;
                }
                if (ss1Time > ss2Time) {
                    return RelativePosition.AFTER;
                }
                if (ss1Time == ss2Time) {
                    return RelativePosition.EQUAL;
                }
            }
            return RelativePosition.UNREACHABLE;
        }

        Segment seg1 = (Segment) pathNidSegmentMap.get(ss1PathSequence);
        Segment seg2 = (Segment) pathNidSegmentMap.get(ss2PathSequence);
        if (seg1 == null || seg2 == null) {
            return RelativePosition.UNREACHABLE;
        }
        if (!(seg1.containsPosition(ss1PathSequence, ss1Time)
                && seg2.containsPosition(ss2PathSequence, ss2Time))) {
            return RelativePosition.UNREACHABLE;
        }
        if (precedencePolicy == StampPrecedence.TIME) {
            if (ss1Time < ss2Time) {
                return RelativePosition.BEFORE;
            }
            if (ss1Time > ss2Time) {
                return RelativePosition.AFTER;
            }
            if (ss1Time == ss2Time) {
                return RelativePosition.EQUAL;
            }
        }
        if (seg1.precedingSegments.contains(seg2.segmentSequence)) {
            return RelativePosition.BEFORE;
        }
        if (seg2.precedingSegments.contains(seg1.segmentSequence)) {
            return RelativePosition.AFTER;
        }
        return RelativePosition.CONTRADICTION;
    }

    public StampPosition getDestination() {
        return coordinate.getStampPosition();
    }

    public boolean onRoute(StampedVersion v) {
        Segment seg = (Segment) pathNidSegmentMap.get(v.getPathSequence());
        if (seg != null) {
            return seg.containsPosition(v.getPathSequence(), v.getTime());
        }
        return false;
    }
    public boolean onRoute(int stampSequence) {
        Segment seg = (Segment) pathNidSegmentMap.get(stampSequence);
        if (seg != null) {
            return seg.containsPosition(getCommitService().getPathSequenceForStamp(stampSequence), 
                    getCommitService().getTimeForStamp(stampSequence));
        }
        return false;
    }

    public RelativePosition relativePosition(int v1, int v2) throws IOException {
        if (!(onRoute(v1) && onRoute(v2))) {
            return RelativePosition.UNREACHABLE;
        }
        return fastRelativePosition(v1, v2, StampPrecedence.PATH);
    }    
    
    public RelativePosition relativePosition(StampedVersion v1, StampedVersion v2) throws IOException {
        if (!(onRoute(v1) && onRoute(v2))) {
            return RelativePosition.UNREACHABLE;
        }
        return fastRelativePosition(v1, v2, StampPrecedence.PATH);
    }
    
    
    public StampSequenceSet getLatestStampSequences(IntStream stampSequenceStream) {       
        return stampSequenceStream.collect(StampSequenceSet::new, 
                new LatestStampAccumulator(), 
                new LatestStampCombiner());
    }
    
    private class LatestStampAccumulator implements ObjIntConsumer<StampSequenceSet> {

        @Override
        public void accept(StampSequenceSet stampsForPosition, int stampToCompare) {
            handleStamp(stampsForPosition, stampToCompare);
        }
    
    }
    
    private class LatestStampCombiner implements BiConsumer<StampSequenceSet,StampSequenceSet> {

        @Override
        public void accept(StampSequenceSet t, StampSequenceSet u) {
            u.stream().forEach((stampToTest) -> {
                handleStamp(t, stampToTest);
            });
            u.clear();
            
            // can't find good documentation that specifies behaviour of BiConsumer
            // in this context, so am making sure both sets have the same values. 
            t.or(u);
        }
        
    }

    public <V extends StampedVersion> Optional<LatestVersion<V>>
        getLatestVersion(ObjectChronology<V> chronicle) {

        HashSet<V> latestVersionSet = new HashSet<>();

        chronicle.getVersions().stream().filter((newVersionToTest) -> 
                (newVersionToTest.getTime() != Long.MIN_VALUE)).filter(
                        (newVersionToTest) -> (onRoute(newVersionToTest))).forEach((newVersionToTest) -> {
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
            switch (fastRelativePosition(part,
                    prevPartToTest, coordinate.getStampPrecedence())) {
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
            StampSequenceSet stampsForPosition, int stamp)  {
        // create a list of values so we don't have any 
        // concurrent modification issues with removing/adding
        // items to the stampsForPosition. 
        if (stampsForPosition.isEmpty()) {
            stampsForPosition.add(stamp);
            return;
        }
        StampSequenceSet stampsToCompare = StampSequenceSet.of(stampsForPosition);
        stampsToCompare.stream().forEach((prevStamp) -> {
            switch (fastRelativePosition(stamp,
                    prevStamp, coordinate.getStampPrecedence())) {
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
            }            
        });
    }
}

