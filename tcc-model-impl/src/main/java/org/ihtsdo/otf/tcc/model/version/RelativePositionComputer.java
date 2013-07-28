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
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI;

/**
 *
 *
 *
 *
 *
 * @author kec
 */
public class RelativePositionComputer implements RelativePositionComputerBI {

    private static ConcurrentHashMap<PositionBI, RelativePositionComputerBI> mapperCache =
            new ConcurrentHashMap<>();

    public static RelativePositionComputerBI getComputer(PositionBI position) {
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
    PositionBI destination;
    HashMap<Integer, Segment> pathNidSegmentMap;

    public RelativePositionComputer(PositionBI destination) {
        this.destination = destination;
        pathNidSegmentMap = setupPathNidSegmentMap(destination);
    }

    private static class Segment {

        int segmentNid;
        int pathNid;
        long endTime;
        BitSet precedingSegments;

        public Segment(int segmentNid, int pathNid, long endTime, BitSet precedingSegments) {
            this.segmentNid = segmentNid;
            this.pathNid = pathNid;
            this.endTime = endTime;
            this.precedingSegments = new BitSet(precedingSegments.size());
            this.precedingSegments.or(precedingSegments);
        }

        public boolean containsPosition(int pathNid, long time) {
            if (this.pathNid == pathNid && time != Long.MIN_VALUE) {
                return time <= endTime;
            }
            return false;
        }
    }

    private static HashMap<Integer, Segment>  setupPathNidSegmentMap(PositionBI destination) {
        HashMap<Integer, Segment> pathNidSegmentMap = new HashMap<>();
        AtomicInteger segmentNidSequence = new AtomicInteger(0);
        BitSet precedingSegments = new BitSet();
        addOriginsToPathNidSegmentMap(destination, pathNidSegmentMap, segmentNidSequence, precedingSegments);

        return pathNidSegmentMap;

    }

    private static void addOriginsToPathNidSegmentMap(PositionBI destination,
            HashMap<Integer, Segment> pathNidRpcNidMap, AtomicInteger segmentNidSequence, BitSet precedingSegments) {
        Segment segment = new Segment(segmentNidSequence.getAndIncrement(), destination.getPath().getConceptNid(),
                destination.getTime(), precedingSegments);
        precedingSegments.set(segment.segmentNid);
        pathNidRpcNidMap.put(destination.getPath().getConceptNid(), segment);
        for (PositionBI origin : destination.getAllOrigins()) {
            addOriginsToPathNidSegmentMap(origin, pathNidRpcNidMap, segmentNidSequence, precedingSegments);
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
        if (seg1.precedingSegments.get(seg2.segmentNid) == true) {
            return RelativePosition.BEFORE;
        }
        if (seg2.precedingSegments.get(seg1.segmentNid) == true) {
            return RelativePosition.AFTER;
        }
        return RelativePosition.CONTRADICTION;
    }

    @Override
    public PositionBI getDestination() {
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
