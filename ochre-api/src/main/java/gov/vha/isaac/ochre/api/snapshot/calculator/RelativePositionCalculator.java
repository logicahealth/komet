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
package gov.vha.isaac.ochre.api.snapshot.calculator;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.OchreCache;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.observable.ObservableChronology;
import gov.vha.isaac.ochre.api.observable.ObservableVersion;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.jvnet.hk2.annotations.Service;
import org.roaringbitmap.RoaringBitmap;

/**
 *
 * @author kec
 */
@Service
@Singleton // Singleton from the perspective of HK2 managed instances
public class RelativePositionCalculator implements OchreCache {

	private static final Logger log = LogManager.getLogger();

	private static final ConcurrentHashMap<StampCoordinate, RelativePositionCalculator> CALCULATOR_CACHE
			  = new ConcurrentHashMap<>();

	public static RelativePositionCalculator getCalculator(StampCoordinate coordinate) {
		RelativePositionCalculator pm = CALCULATOR_CACHE.get(coordinate);

		if (pm != null) {
			return pm;
		}

		pm = new RelativePositionCalculator(coordinate);

		RelativePositionCalculator existing = CALCULATOR_CACHE.putIfAbsent(coordinate, pm);

		if (existing != null) {
			pm = existing;
		}

		return pm;
	}
	StampCoordinate coordinate;
	/**
	 * Mapping from pathNid to each segment for that pathNid. There is one entry
	 * for each path reachable antecedent to the destination position of the
	 * computer.
	 */
	OpenIntObjectHashMap<Segment> pathSequenceSegmentMap;

	public RelativePositionCalculator() {
		// No arg constructor for HK2 managed instance
	}

	public RelativePositionCalculator(StampCoordinate coordinate) {
		this.coordinate = coordinate;
		this.pathSequenceSegmentMap = setupPathSequenceSegmentMap(coordinate.getStampPosition());
	}

	@Override
	public void reset() {
		log.info("Resetting RelativePositionCalculator.");
		CALCULATOR_CACHE.clear();
	}

	private class Segment {

		/**
		 * Each segment gets it's own sequence which gets greater the further
		 * prior to the position of the relative position computer. TODO if we
		 * have a path sequence, may not need segment sequence.
		 */
		int segmentSequence;

		/**
		 * The pathConceptSequence of this segment. Each ancestor path to the
		 * position of the computer gets it's own segment.
		 */
		int pathConceptSequence;
		/**
		 * The end time of the position of the relative position computer. stamps
		 * with times after the end time are not part of the path.
		 */
		long endTime;

		RoaringBitmap precedingSegments;

		private Segment(int segmentSequence, int pathConceptSequence, long endTime, RoaringBitmap precedingSegments) {
			this.segmentSequence = segmentSequence;
			this.pathConceptSequence = pathConceptSequence;
			this.endTime = endTime;
			this.precedingSegments = new RoaringBitmap();
			this.precedingSegments.or(precedingSegments);
		}

		private boolean containsPosition(int pathConceptSequence, int moduleConceptSequence, long time) {
			if (coordinate.getModuleSequences().isEmpty() || coordinate.getModuleSequences().contains(moduleConceptSequence)) {
				if (this.pathConceptSequence == pathConceptSequence
						  && time != Long.MIN_VALUE) {
					return time <= endTime;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return "Segment{" + segmentSequence + ", pathConcept=" + Get.conceptDescriptionText(pathConceptSequence) + "<"
					  + pathConceptSequence
					  + ">, endTime=" + Instant.ofEpochMilli(endTime) + ", precedingSegments=" + precedingSegments + '}';
		}
	}

	private OpenIntObjectHashMap<Segment> setupPathSequenceSegmentMap(StampPosition destination) {
		OpenIntObjectHashMap<Segment> pathSequenceSegmentMapToSetup
				  = new OpenIntObjectHashMap<>();
		AtomicInteger segmentSequence = new AtomicInteger(0);

		// the sequence of the preceding segments is set in the recursive 
		// call.
		RoaringBitmap precedingSegments = new RoaringBitmap();

		// call to recursive method...
		addOriginsToPathSequenceSegmentMap(destination,
				  pathSequenceSegmentMapToSetup, segmentSequence, precedingSegments);

		return pathSequenceSegmentMapToSetup;

	}

	// recursively called method
	private void addOriginsToPathSequenceSegmentMap(StampPosition destination,
			  OpenIntObjectHashMap<Segment> pathNidSegmentMap, AtomicInteger segmentSequence, RoaringBitmap precedingSegments) {
		Segment segment = new Segment(segmentSequence.getAndIncrement(),
				  destination.getStampPathSequence(),
				  destination.getTime(), precedingSegments);
		// precedingSegments is cumulative, each recursive call adds another
		precedingSegments.add(segment.segmentSequence);
		pathNidSegmentMap.put(destination.getStampPathSequence(), segment);
		destination.getStampPath()
				  .getPathOrigins()
				  .stream()
				  .forEach((origin) -> {
					  // Recursive call
					  addOriginsToPathSequenceSegmentMap(origin, pathNidSegmentMap, segmentSequence, precedingSegments);
				  });
	}
	

	public RelativePosition fastRelativePosition(StampedVersion v1, StampedVersion v2, StampPrecedence precedencePolicy) {
		if (v1.getPathSequence() == v2.getPathSequence()) {
			Segment seg = (Segment) pathSequenceSegmentMap.get(v1.getPathSequence());
			if (seg == null) {
				StringBuilder builder = new StringBuilder();
				builder.append("Segment cannot be null.");
				builder.append("\nv1: ").append(v1);
				builder.append("\nv2: ").append(v1);
				builder.append("\nno segment in map: ").append(pathSequenceSegmentMap);
				throw new IllegalStateException(builder.toString());
			}
			if (seg.containsPosition(v1.getPathSequence(), v1.getModuleSequence(), v1.getTime())
					  && seg.containsPosition(v2.getPathSequence(), v2.getModuleSequence(), v2.getTime())) {
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

		Segment seg1 = (Segment) pathSequenceSegmentMap.get(v1.getPathSequence());
		Segment seg2 = (Segment) pathSequenceSegmentMap.get(v2.getPathSequence());
		if (seg1 == null || seg2 == null) {
			return RelativePosition.UNREACHABLE;
		}
		if (!(seg1.containsPosition(v1.getPathSequence(), v1.getModuleSequence(), v1.getTime())
				  && seg2.containsPosition(v2.getPathSequence(), v2.getModuleSequence(), v2.getTime()))) {
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
		long ss1Time = Get.commitService().getTimeForStamp(stampSequence1);
		int ss1ModuleSequence = Get.commitService().getModuleSequenceForStamp(stampSequence1);
		int ss1PathSequence = Get.commitService().getPathSequenceForStamp(stampSequence1);
		long ss2Time = Get.commitService().getTimeForStamp(stampSequence2);
		int ss2ModuleSequence = Get.commitService().getModuleSequenceForStamp(stampSequence2);
		int ss2PathSequence = Get.commitService().getPathSequenceForStamp(stampSequence2);

		if (ss1PathSequence == ss2PathSequence) {
			Segment seg = (Segment) pathSequenceSegmentMap.get(ss1PathSequence);
			if (seg.containsPosition(ss1PathSequence, ss1ModuleSequence, ss1Time)
					  && seg.containsPosition(ss2PathSequence, ss2ModuleSequence, ss2Time)) {
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

		Segment seg1 = (Segment) pathSequenceSegmentMap.get(ss1PathSequence);
		Segment seg2 = (Segment) pathSequenceSegmentMap.get(ss2PathSequence);
		if (seg1 == null || seg2 == null) {
			return RelativePosition.UNREACHABLE;
		}
		if (!(seg1.containsPosition(ss1PathSequence, ss1ModuleSequence, ss1Time)
				  && seg2.containsPosition(ss2PathSequence, ss2ModuleSequence, ss2Time))) {
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
		Segment seg = (Segment) pathSequenceSegmentMap.get(v.getPathSequence());
		if (seg != null) {
			return seg.containsPosition(v.getPathSequence(), v.getModuleSequence(), v.getTime());
		}
		return false;
	}

	public boolean onRoute(int stampSequence) {
		Segment seg = (Segment) pathSequenceSegmentMap.get(Get.commitService().getPathSequenceForStamp(stampSequence));
		if (seg != null) {
			return seg.containsPosition(
					  Get.commitService().getPathSequenceForStamp(stampSequence),
					  Get.commitService().getModuleSequenceForStamp(stampSequence),
					  Get.commitService().getTimeForStamp(stampSequence));
		}
		return false;
	}

	public RelativePosition relativePosition(int stampSequence1, int stampSequence2) {
		if (!(onRoute(stampSequence1) && onRoute(stampSequence2))) {
			return RelativePosition.UNREACHABLE;
		}
		return fastRelativePosition(stampSequence1, stampSequence2, StampPrecedence.PATH);
	}

	public RelativePosition relativePosition(StampedVersion v1, StampedVersion v2) {
		if (!(onRoute(v1) && onRoute(v2))) {
			return RelativePosition.UNREACHABLE;
		}
		return fastRelativePosition(v1, v2, StampPrecedence.PATH);
	}

	public int[] getLatestStampSequencesAsArray(IntStream stampSequenceStream) {
		return getLatestStampSequencesAsSet(stampSequenceStream).asArray();
	}

	public StampSequenceSet getLatestStampSequencesAsSet(IntStream stampSequenceStream) {
		StampSequenceSet result = stampSequenceStream.collect(StampSequenceSet::new,
				  new LatestStampAccumulator(),
				  new LatestStampCombiner());

		return StampSequenceSet.of(result.stream().filter((stampSequence) -> {
			return coordinate.getAllowedStates().contains(Get.commitService().getStatusForStamp(stampSequence));
		}));
	}

	private class LatestStampAccumulator implements ObjIntConsumer<StampSequenceSet> {

		@Override
		public void accept(StampSequenceSet stampsForPosition, int stampToCompare) {
			handleStamp(stampsForPosition, stampToCompare);
		}

	}

	private class LatestStampCombiner implements BiConsumer<StampSequenceSet, StampSequenceSet> {

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

//    private class StampSequenceSetSupplier implements Supplier<StampSequenceSet> {
//        @Override
//        public StampSequenceSet get() {
//            return new StampSequenceSet();
//        }
//    };
	/**
	 *
	 * @param stampSequences A stream of stampSequences from which the latest is
	 * found, and then tested to determine if the latest is active.
	 * @return true if any of the latest stampSequences (may be multiple in the
	 * case of a contradiction) are active.
	 */
	public boolean isLatestActive(IntStream stampSequences) {
		return Arrays.stream(getLatestStampSequencesAsArray(stampSequences)).anyMatch((int stampSequence)
				  -> Get.commitService().getStatusForStamp(stampSequence) == State.ACTIVE);
	}

	public <C extends ObservableChronology<V>, V extends ObservableVersion>
			  Optional<LatestVersion<V>>
			  getLatestVersion(C chronicle) {

		HashSet<V> latestVersionSet = new HashSet<>();

		chronicle.getVersionList().stream().filter((newVersionToTest)
				  -> (newVersionToTest.getTime() != Long.MIN_VALUE)).filter(
				  (newVersionToTest) -> (onRoute(newVersionToTest))).forEach((newVersionToTest) -> {
					  if (latestVersionSet.isEmpty()) {
						  latestVersionSet.add(newVersionToTest);
					  } else {
						  handlePart(latestVersionSet, newVersionToTest);
					  }
				  });
		List<V> latestVersionList = new ArrayList<>(latestVersionSet);
		if (latestVersionList.isEmpty()) {
			return Optional.empty();
		}
		if (latestVersionList.size() == 1) {
			return Optional.of(new LatestVersion<V>(latestVersionList.get(0)));
		}

		return Optional.of(new LatestVersion<V>(latestVersionList.get(0),
				  latestVersionList.subList(1, latestVersionList.size())));
	}

	public <C extends ObjectChronology<V>, V extends StampedVersion>
			  Optional<LatestVersion<V>>
			  getLatestVersion(C chronicle) {

		HashSet<V> latestVersionSet = new HashSet<>();

		chronicle.getVersionList().stream().filter((newVersionToTest)
				  -> (newVersionToTest.getTime() != Long.MIN_VALUE)).filter(
				  (newVersionToTest) -> (onRoute(newVersionToTest))).forEach((newVersionToTest) -> {
					  if (latestVersionSet.isEmpty()) {
						  latestVersionSet.add(newVersionToTest);
					  } else {
						  handlePart(latestVersionSet, newVersionToTest);
					  }
				  });
		if (coordinate.getAllowedStates().equals(State.ACTIVE_ONLY_SET)) {
			HashSet<V> inactiveVersions = new HashSet<>();
			latestVersionSet.stream().forEach((version) -> {
				if (version.getState() != State.ACTIVE) {
					inactiveVersions.add(version);
				}

			});
			latestVersionSet.removeAll(inactiveVersions);
		}
		List<V> latestVersionList = new ArrayList<>(latestVersionSet);
		if (latestVersionList.isEmpty()) {
			return Optional.empty();
		}
		if (latestVersionList.size() == 1) {
			return Optional.of(new LatestVersion<V>(latestVersionList.get(0)));
		}

		return Optional.of(new LatestVersion<V>(latestVersionList.get(0),
				  latestVersionList.subList(1, latestVersionList.size())));
	}

	private int errorCount = 0;

	private <V extends StampedVersion> void handlePart(
			  HashSet<V> partsForPosition, V part) {
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
						log.warn("{} should never happen. "
								  + "Data is malformed. stampSequence: {} Part:\n{} \n  Part to test: \n{}",
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
			  StampSequenceSet stampsForPosition, int stampSequence) {
		if (!onRoute(stampSequence)) {
			return;
		}
		if (stampsForPosition.isEmpty()) {
			stampsForPosition.add(stampSequence);
			return;
		}
		// create a list of values so we don't have any 
		// concurrent modification issues with removing/adding
		// items to the stampsForPosition. 
		StampSequenceSet stampsToCompare = StampSequenceSet.of(stampsForPosition);
		stampsToCompare.stream().forEach((prevStamp) -> {
			switch (fastRelativePosition(stampSequence,
					  prevStamp, coordinate.getStampPrecedence())) {
				case AFTER:
					stampsForPosition.remove(prevStamp);
					stampsForPosition.add(stampSequence);
					break;
				case BEFORE:
					break;
				case CONTRADICTION:
					stampsForPosition.add(stampSequence);
					break;
				case EQUAL:
					// Can only have one stampSequence per time/path
					// combination.
					if (prevStamp == stampSequence) {
						// stampSequence already added from another position.
						// No need to add again.
						break;
					}
					// Duplicate values encountered.
					errorCount++;
					if (errorCount < 5) {
						log.warn("{} should never happen. "
								  + "\n  Data is malformed. stamp: {}  Part to test: {}",
								  new Object[]{RelativePosition.EQUAL,
									  stampSequence,
									  prevStamp});
					}
					break;
				case UNREACHABLE:
					// nothing to do...
					break;
				default:
					throw new UnsupportedOperationException("Can't handle: " + fastRelativePosition(stampSequence,
							  prevStamp, coordinate.getStampPrecedence()));
			}
		});
	}

	@Override
	public String toString() {
		return "RelativePositionCalculator{" + coordinate + '}';
	}
}
