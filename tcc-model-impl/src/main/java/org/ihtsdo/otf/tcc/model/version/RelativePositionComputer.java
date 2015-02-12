/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.model.version;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI;

/**
 * Computes the relative position of two positions with respect to each other. 
 * Used to determine which position is the most current given a time, and a 
 * set of modules and paths to compute the relative position with respect to. 
 * <br/>
 * Each <class>Position</class> is given it's own RelativePositionCOmputer, which 
 * is stored in a cache. 
 * 
 * TODO needs to be a means to clear out the position computer cache, or extend it
 * after a commit. 
 *
 *
 *
 * @author kec
 */
public class RelativePositionComputer implements RelativePositionComputerBI {

    private static ConcurrentHashMap<Position, RelativePositionComputerBI> mapperCache =
            new ConcurrentHashMap<>();

    public static RelativePositionComputerBI getComputer(Position position) {
        RelativePositionComputerBI pm = mapperCache.get(position);

        if (pm != null) {
            return pm;
        }

        pm = new RelativePositionComputer(position);

        RelativePositionComputerBI existing = mapperCache.putIfAbsent(position, pm);

        if (existing != null) {
            pm = existing;
        }

        return pm;
    }
    Position destination;
    /**
     * Mapping from pathNid to each segment for that pathNid.
     * There is one entry for each path reachable antecedent to the destination 
     * position of the computer. 
     */
    HashMap<Integer, Segment> pathNidSegmentMap;

    public RelativePositionComputer(Position destination) {
        this.destination = destination;
        pathNidSegmentMap = setupPathNidSegmentMap(destination);
    }

    private static class Segment {

        /**
         * Each segment gets it's own sequence which gets greater the further
         * prior to the position of the relative position computer. 
         * TODO if we have a path sequence, may not need segment sequence. 
         */
        int segmentSequence;
        
        /**
         * The pathNid of this segment. Each ancestor path to the position of the 
         * computer gets it's own segment. 
         * TODO if we have a path sequence, many not need segment sequence. 
         */
        int pathNid;
        /**
         * The end time of the position of the relative position computer. 
         * stamps with times after the end time are not part of the 
         * path. 
         */
        long endTime;
        
        BitSet precedingSegments;

        public Segment(int segmentSequence, int pathNid, long endTime, BitSet precedingSegments) {
            this.segmentSequence = segmentSequence;
            this.pathNid = pathNid;
            this.endTime = endTime;
            this.precedingSegments = new BitSet(precedingSegments.size());
            this.precedingSegments.or(precedingSegments);
        }

        // Could check for modules here...
        public boolean containsPosition(int pathNid, long time) {
            if (this.pathNid == pathNid && time != Long.MIN_VALUE) {
                return time <= endTime;
            }
            return false;
        }
    }

    private static HashMap<Integer, Segment>  setupPathNidSegmentMap(Position destination) {
        HashMap<Integer, Segment> pathNidSegmentMap = new HashMap<>();
        AtomicInteger segmentSequence = new AtomicInteger(0);
        
        // the sequence of the preceding segments is set in the recursive 
        // call.
        BitSet precedingSegments = new BitSet();
        
        // call to recursive method...
        addOriginsToPathNidSegmentMap(destination, pathNidSegmentMap, segmentSequence, precedingSegments);

        return pathNidSegmentMap;

    }

    // recursively called method
    private static void addOriginsToPathNidSegmentMap(Position destination,
            HashMap<Integer, Segment> pathNidSegmentMap, AtomicInteger segmentSequence, BitSet precedingSegments) {
        Segment segment = new Segment(segmentSequence.getAndIncrement(), destination.getPath().getConceptNid(),
                destination.getTime(), precedingSegments);
        // precedingSegments is cumulative, each recursive call adds another
        precedingSegments.set(segment.segmentSequence);
        pathNidSegmentMap.put(destination.getPath().getConceptNid(), segment);
        for (Position origin : destination.getOrigins()) {
            // Recursive call
            addOriginsToPathNidSegmentMap(origin, pathNidSegmentMap, segmentSequence, precedingSegments);
        }
    }

    @Override
    public RelativePosition fastRelativePosition(VersionPointBI v1, VersionPointBI v2, Precedence precedencePolicy) {
        if (v1.getPathNid() == v2.getPathNid()) {
            Segment seg = (Segment) pathNidSegmentMap.get(v1.getPathNid());
            if (seg.containsPosition(v1.getPathNid(), v1.getTime())
                    && seg.containsPosition(v2.getPathNid(), v2.getTime())) {
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

        Segment seg1 = (Segment) pathNidSegmentMap.get(v1.getPathNid());
        Segment seg2 = (Segment) pathNidSegmentMap.get(v2.getPathNid());
        if (seg1 == null || seg2 == null) {
            return RelativePosition.UNREACHABLE;
        }
        if (!(seg1.containsPosition(v1.getPathNid(), v1.getTime())
                && seg2.containsPosition(v2.getPathNid(), v2.getTime()))) {
            return RelativePosition.UNREACHABLE;
        }
        if (precedencePolicy == Precedence.TIME) {
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
        if (seg1.precedingSegments.get(seg2.segmentSequence) == true) {
            return RelativePosition.BEFORE;
        }
        if (seg2.precedingSegments.get(seg1.segmentSequence) == true) {
            return RelativePosition.AFTER;
        }
        return RelativePosition.CONTRADICTION;
    }

    @Override
    public Position getDestination() {
        return destination;
    }

    @Override
    public boolean onRoute(VersionPointBI v) {
        Segment seg = (Segment) pathNidSegmentMap.get(v.getPathNid());
        if (seg != null) {
            return seg.containsPosition(v.getPathNid(), v.getTime());
        }
        return false;
    }

    @Override
    public RelativePosition relativePosition(VersionPointBI v1, VersionPointBI v2) throws IOException {
        if (!(onRoute(v1) && onRoute(v2))) {
            return RelativePosition.UNREACHABLE;
        }
        return fastRelativePosition(v1, v2, Precedence.PATH);
    }
}
