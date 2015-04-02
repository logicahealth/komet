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
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitManager;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.roaringbitmap.RoaringBitmap;

/**
 *
 * @author kec
 */
public class RelativePositionCalculator {
    
    private static PathService pathService = null;
    
    private static PathService getPathService() {
        if (pathService == null) {
            pathService = LookupService.getService(PathService.class);
        }
        return pathService;
    }

    private static CommitManager commitManager;

    private static CommitManager getCommitManager() {
        if (commitManager == null) {
            commitManager = LookupService.getService(CommitManager.class);
        }
        return commitManager;
    }

    private static final ConcurrentHashMap<StampPosition, RelativePositionCalculator> calculatorCache =
            new ConcurrentHashMap<>();

    public static RelativePositionCalculator getCalculator(StampPosition position) {
        RelativePositionCalculator pm = calculatorCache.get(position);

        if (pm != null) {
            return pm;
        }

        pm = new RelativePositionCalculator(position);

        RelativePositionCalculator existing = calculatorCache.putIfAbsent(position, pm);

        if (existing != null) {
            pm = existing;
        }

        return pm;
    }
    StampPosition destination;
    /**
     * Mapping from pathNid to each segment for that pathNid.
     * There is one entry for each path reachable antecedent to the destination 
     * position of the computer. 
     */
    OpenIntObjectHashMap<Segment> pathNidSegmentMap;

    public RelativePositionCalculator(StampPosition destination) {
        this.destination = destination;
        this.pathNidSegmentMap = setupPathNidSegmentMap(destination);
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
        long ss1Time = getCommitManager().getTimeForStamp(stampSequence1);
        int ss1PathSequence = getCommitManager().getPathSequenceForStamp(stampSequence1);
        long ss2Time = getCommitManager().getTimeForStamp(stampSequence2);
        int ss2PathSequence = getCommitManager().getPathSequenceForStamp(stampSequence2);
        
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
        return destination;
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
            return seg.containsPosition(
                    getCommitManager().getPathSequenceForStamp(stampSequence), 
                    getCommitManager().getTimeForStamp(stampSequence));
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
}

