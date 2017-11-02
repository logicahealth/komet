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



package sh.isaac.api.snapshot.calculator;

//~--- JDK imports ------------------------------------------------------------

import java.time.Instant;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

import org.jvnet.hk2.annotations.Service;


import sh.isaac.api.Get;
import sh.isaac.api.OchreCache;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class RelativePositionCalculator.
 *
 * @author kec
 */
@Service
@Singleton  // Singleton from the perspective of HK2 managed instances
public class RelativePositionCalculator
         implements OchreCache {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   /** The Constant CALCULATOR_CACHE. */
   private static final ConcurrentHashMap<StampCoordinate, RelativePositionCalculator> CALCULATOR_CACHE =
      new ConcurrentHashMap<>();

   //~--- fields --------------------------------------------------------------

   /** The error count. */
   private int  errorCount   = 0;
   private StampService stampService;

   /** The coordinate. */
   private StampCoordinate coordinate;
   private EnumSet<State>  allowedStates;
   private final ConcurrentHashMap<Integer, Boolean> stampOnRoute = new ConcurrentHashMap();
   private final ConcurrentHashMap<Integer, Boolean> stampIsAllowedState = new ConcurrentHashMap();

   /**
    * Mapping from pathNid to each segment for that pathNid. There is one entry
    * for each path reachable antecedent to the destination position of the
    * computer.
    */
   ConcurrentHashMap<Integer, Segment> pathSequenceSegmentMap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new relative position calculator.
    */
   public RelativePositionCalculator() {
      // No arg constructor for HK2 managed instance
   }

   /**
    * Instantiates a new relative position calculator.
    *
    * @param coordinate the coordinate
    */
   public RelativePositionCalculator(StampCoordinate coordinate) {
      this.coordinate             = coordinate;
      this.pathSequenceSegmentMap = setupPathSequenceSegmentMap(coordinate.getStampPosition());
      this.allowedStates          = coordinate.getAllowedStates();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Fast relative position.
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @param precedencePolicy the precedence policy
    * @return the relative position
    */
   public RelativePosition fastRelativePosition(int stampSequence1,
         int stampSequence2,
         StampPrecedence precedencePolicy) {
      final long ss1Time           = getStampService()
                                        .getTimeForStamp(stampSequence1);
      final int  ss1ModuleSequence = getStampService()
                                        .getModuleNidForStamp(stampSequence1);
      final int  ss1PathSequence   = getStampService()
                                        .getPathNidForStamp(stampSequence1);
      final long ss2Time           = getStampService()
                                        .getTimeForStamp(stampSequence2);
      final int  ss2ModuleSequence = getStampService()
                                        .getModuleNidForStamp(stampSequence2);
      final int  ss2PathSequence   = getStampService()
                                        .getPathNidForStamp(stampSequence2);

      if (ss1PathSequence == ss2PathSequence) {
         final Segment seg = this.pathSequenceSegmentMap.get(ss1PathSequence);

         if (seg.containsPosition(ss1PathSequence, ss1ModuleSequence, ss1Time) &&
               seg.containsPosition(ss2PathSequence, ss2ModuleSequence, ss2Time)) {
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

      final Segment seg1 = this.pathSequenceSegmentMap.get(ss1PathSequence);
      final Segment seg2 = this.pathSequenceSegmentMap.get(ss2PathSequence);

      if ((seg1 == null) || (seg2 == null)) {
         return RelativePosition.UNREACHABLE;
      }

      if (!(seg1.containsPosition(ss1PathSequence, ss1ModuleSequence, ss1Time) &&
            seg2.containsPosition(ss2PathSequence, ss2ModuleSequence, ss2Time))) {
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

   /**
    * Fast relative position.
    *
    * @param v1 the v 1
    * @param v2 the v 2
    * @param precedencePolicy the precedence policy
    * @return the relative position
    */
   public RelativePosition fastRelativePosition(StampedVersion v1,
         StampedVersion v2,
         StampPrecedence precedencePolicy) {
      if (v1.getPathNid() == v2.getPathNid()) {
         final Segment seg = this.pathSequenceSegmentMap.get(v1.getPathNid());

         if (seg == null) {
            final StringBuilder builder = new StringBuilder();

            builder.append("Segment cannot be null.");
            builder.append("\nv1: ")
                   .append(v1);
            builder.append("\nv2: ")
                   .append(v1);
            builder.append("\nno segment in map: ")
                   .append(this.pathSequenceSegmentMap);
            throw new IllegalStateException(builder.toString());
         }

         if (seg.containsPosition(v1.getPathNid(), v1.getModuleNid(), v1.getTime()) &&
               seg.containsPosition(v2.getPathNid(), v2.getModuleNid(), v2.getTime())) {
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

      final Segment seg1 = this.pathSequenceSegmentMap.get(v1.getPathNid());
      final Segment seg2 = this.pathSequenceSegmentMap.get(v2.getPathNid());

      if ((seg1 == null) || (seg2 == null)) {
         return RelativePosition.UNREACHABLE;
      }

      if (!(seg1.containsPosition(v1.getPathNid(), v1.getModuleNid(), v1.getTime()) &&
            seg2.containsPosition(v2.getPathNid(), v2.getModuleNid(), v2.getTime()))) {
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
   /**
    * On route.
    *
    * @param stampSequence the stamp sequence
    * @return true, if successful
    */
   public boolean onRoute(int stampSequence) {
      if (stampOnRoute.containsKey(stampSequence)) {
         return stampOnRoute.get(stampSequence);
      }
      final Segment seg = this.pathSequenceSegmentMap.get(getStampService()
                                                             .getPathNidForStamp(stampSequence));
      boolean returnValue = false;
      if (seg != null) {
         returnValue = seg.containsPosition(
             getStampService()
                .getPathNidForStamp(stampSequence),
             getStampService()
                .getModuleNidForStamp(stampSequence),
             getStampService()
                .getTimeForStamp(stampSequence));
      }
      stampOnRoute.put(stampSequence, returnValue);
      return returnValue;
   }

   /**
    * On route.
    *
    * @param <V>
    * @param version the version
    * @return true, if successful
    */
   public <V extends StampedVersion> boolean onRoute(V version) {
      final Segment seg = this.pathSequenceSegmentMap.get(version.getPathNid());

      if (seg != null) {
         return seg.containsPosition(version.getPathNid(), version.getModuleNid(), version.getTime());
      }

      return false;
   }

   /**
    * Relative position.
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @return the relative position
    */
   public RelativePosition relativePosition(int stampSequence1, int stampSequence2) {
      if (!(onRoute(stampSequence1) && onRoute(stampSequence2))) {
         return RelativePosition.UNREACHABLE;
      }

      return fastRelativePosition(stampSequence1, stampSequence2, StampPrecedence.PATH);
   }

   /**
    * Relative position.
    *
    * @param v1 the v 1
    * @param v2 the v 2
    * @return the relative position
    */
   public RelativePosition relativePosition(StampedVersion v1, StampedVersion v2) {
      if (!(onRoute(v1) && onRoute(v2))) {
         return RelativePosition.UNREACHABLE;
      }

      return fastRelativePosition(v1, v2, StampPrecedence.PATH);
   }

   /**
    * Reset.
    */
   @Override
   public void reset() {
      log.info("Resetting RelativePositionCalculator.");
      CALCULATOR_CACHE.clear();
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "RelativePositionCalculator{" + this.coordinate + '}';
   }

   /**
    * Adds the origins to path sequence segment map.
    *
    * @param destination the destination
    * @param pathNidSegmentMap the path nid segment map
    * @param segmentSequence the segment sequence
    * @param precedingSegments the preceding segments
    */

   // recursively called method
   private void addOriginsToPathSequenceSegmentMap(StampPosition destination,
         ConcurrentHashMap<Integer, Segment> pathNidSegmentMap,
         AtomicInteger segmentSequence,
         ConcurrentSkipListSet<Integer> precedingSegments) {
      final Segment segment = new Segment(
                                  segmentSequence.getAndIncrement(),
                                  destination.getStampPathSequence(),
                                  destination.getTime(),
                                  precedingSegments);

      // precedingSegments is cumulative, each recursive call adds another
      precedingSegments.add(segment.segmentSequence);
      pathNidSegmentMap.put(destination.getStampPathSequence(), segment);
      destination.getStampPath()
                 .getPathOrigins()
                 .stream()
                 .forEach(
                     (origin) -> {
         // Recursive call
                        addOriginsToPathSequenceSegmentMap(
                            origin,
                            pathNidSegmentMap,
                            segmentSequence,
                            precedingSegments);
                     });
   }

   /**
    * Handle part.
    *
    * @param <V> the value type
    * @param partsForPosition the parts for position
    * @param part the part
    */
   private <V extends StampedVersion> void handlePart(HashSet<V> partsForPosition, StampedVersion part) {
      // create a list of values so we don't have any
      // concurrent modification issues with removing/adding
      // items to the partsForPosition.
      final List<StampedVersion> partsToCompare = new ArrayList<>(partsForPosition);

      for (final StampedVersion prevPartToTest: partsToCompare) {
         switch (fastRelativePosition(part, prevPartToTest, this.coordinate.getStampPrecedence())) {
         case AFTER:
            partsForPosition.remove((V) prevPartToTest);
            partsForPosition.add((V) part);
            break;

         case BEFORE:
            break;

         case CONTRADICTION:
            partsForPosition.add((V) part);
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
            this.errorCount++;

            if (this.errorCount < 5) {
               log.warn(
                   "{} should never happen. " + "Data is malformed. stampSequence: {} Part:\n{} \n  Part to test: \n{}",
                   new Object[] { RelativePosition.EQUAL, part.getStampSequence(), part, prevPartToTest });
            }

            break;

         case UNREACHABLE:

            // Should have failed mapper.onRoute(part)
            // above.
            throw new RuntimeException(RelativePosition.UNREACHABLE + " should never happen.");
         }
      }
   }

   /**
    * Handle stamp.
    *
    * @param stampsForPosition the stamps for position
    * @param stampSequence the stamp sequence
    */
   private void handleStamp(OpenIntHashSet stampsForPosition, int stampSequence, boolean allowUncommitted) {
      if (!allowUncommitted) {
         if (getStampService()
                .isUncommitted(stampSequence)) {
            return;
         }
      }

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
      final OpenIntHashSet stampsToCompare = (OpenIntHashSet) stampsForPosition.clone();

      stampsToCompare.forEachKey(
          (prevStamp) -> {
             switch (fastRelativePosition(stampSequence, prevStamp, this.coordinate.getStampPrecedence())) {
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
                this.errorCount++;

                if (this.errorCount < 20) {
                   log.warn(
                       "{} should never happen. " + "\n  Data is malformed. stamp: {}  Part to test: {}",
                       new Object[] { RelativePosition.EQUAL, stampSequence, prevStamp });
                }

                break;

             case UNREACHABLE:

                // nothing to do...
                break;

             default:
                throw new UnsupportedOperationException(
                    "n Can't handle: " + fastRelativePosition(
                        stampSequence,
                        prevStamp,
                        this.coordinate.getStampPrecedence()));
             }

             return true;
          });
   }

   /**
    * Setup path sequence segment map.
    *
    * @param destination the destination
    * @return the open int object hash map
    */
   private ConcurrentHashMap<Integer, Segment> setupPathSequenceSegmentMap(StampPosition destination) {
      final ConcurrentHashMap<Integer, Segment> pathSequenceSegmentMapToSetup = new ConcurrentHashMap<>();
      final AtomicInteger                 segmentSequence               = new AtomicInteger(0);

      // the sequence of the preceding segments is set in the recursive
      // call.
      final ConcurrentSkipListSet<Integer> precedingSegments = new ConcurrentSkipListSet();

      // call to recursive method...
      addOriginsToPathSequenceSegmentMap(
          destination,
          pathSequenceSegmentMapToSetup,
          segmentSequence,
          precedingSegments);
      return pathSequenceSegmentMapToSetup;
   }

   //~--- get methods ---------------------------------------------------------
   private StampService getStampService() {
      if (this.stampService == null) {
         this.stampService = Get.stampService();
      }
      return this.stampService;
   }
   private boolean isAllowedState(int stampSequence) {
      if (stampIsAllowedState.containsKey(stampSequence)) {
         return stampIsAllowedState.get(stampSequence);
      }
      boolean allowed = this.allowedStates.contains(getStampService().getStatusForStamp(stampSequence));
      stampIsAllowedState.put(stampSequence, allowed);
      return allowed;
   }

   /**
    * Gets the calculator.
    *
    * @param coordinate the coordinate
    * @return the calculator
    */
   public static RelativePositionCalculator getCalculator(StampCoordinate coordinate) {
      RelativePositionCalculator calculator = CALCULATOR_CACHE.get(coordinate);

      if (calculator != null) {
         return calculator;
      }

      calculator = new RelativePositionCalculator(coordinate);

      final RelativePositionCalculator existing = CALCULATOR_CACHE.putIfAbsent(coordinate, calculator);

      if (existing != null) {
         calculator = existing;
      }

      return calculator;
   }

   /**
    * Gets the destination.
    *
    * @return the destination
    */
   public StampPosition getDestination() {
      return this.coordinate.getStampPosition();
   }

// private class StampSequenceSetSupplier implements Supplier<StampSequenceSet> {
//     @Override
//     public StampSequenceSet get() {
//         return new StampSequenceSet();
//     }
// };

   /**
    * Checks if latest active.
    *
    * @param stampSequences A stream of stampSequences from which the latest is
    * found, and then tested to determine if the latest is active.
    * @return true if any of the latest stampSequences (may be multiple in the
    * case of a contradiction) are active.
    */
   public boolean isLatestActive(int[] stampSequences) {
      for (int stampSequence: getLatestCommittedStampSequencesAsSet(stampSequences)) {
         if (getStampService().getStatusForStamp(stampSequence) == State.ACTIVE) {
            return true;
         }
      }
      return false;
   }

   public <V extends ObservableVersion> LatestVersion<V> getLatestCommittedVersion(ObservableChronology chronicle) {
      final HashSet<V> latestVersionSet = new HashSet<>();

      chronicle.getVersionList()
               .stream()
               .filter(
                   (newVersionToTest) -> ((newVersionToTest.getTime() != Long.MIN_VALUE) &&
                         (newVersionToTest.getTime() != Long.MAX_VALUE)))
               .filter((newVersionToTest) -> (onRoute(newVersionToTest)))
               .forEach(
                   (newVersionToTest) -> {
                      if (latestVersionSet.isEmpty()) {
                         latestVersionSet.add((V) newVersionToTest);
                      } else {
                         handlePart(latestVersionSet, newVersionToTest);
                      }
                   });

      final List<V> latestVersionList = new ArrayList<>(latestVersionSet);

      if (latestVersionList.isEmpty()) {
         return new LatestVersion();
      }

      if (latestVersionList.size() == 1) {
         return new LatestVersion<>(latestVersionList.get(0));
      }

      return new LatestVersion<>(latestVersionList.get(0), latestVersionList.subList(1, latestVersionList.size()));
   }

   /**
    * Gets the latest stamp sequences as a sorted set in an array.
    *
    * @param stampSequences the stamp sequence stream
    * @return the latest stamp sequences as a sorted set in an array
    */
   public int[] getLatestCommittedStampSequencesAsSet(int[] stampSequences) {
      OpenIntHashSet stampsForPosition = new OpenIntHashSet();
      for (int stampToCompare: stampSequences) {
         handleStamp(stampsForPosition, stampToCompare, false);
      }
      OpenIntHashSet resultSet = new OpenIntHashSet();

      for (int stampSequence: stampsForPosition.keys().elements()) {
         if (isAllowedState(stampSequence)) {
            resultSet.add(stampSequence);
         }
      }

      IntArrayList resultList = resultSet.keys();

      resultList.sort();
      return resultList.elements();
   }

   /**
    * Gets the latest stamp sequences as set.
    *
    * @param stampSequences the stamp sequence stream
    * @return the latest stamp sequences as set
    */
   public int[] getLatestStampSequencesAsSet(int[] stampSequences) {
      
      OpenIntHashSet stampsForPosition = new OpenIntHashSet();
      for (int stampToCompare: stampSequences) {
         handleStamp(stampsForPosition, stampToCompare, true);
      }
      
      OpenIntHashSet resultSet = new OpenIntHashSet();

      for (int stampSequence: stampsForPosition.keys().elements()) {
         if (isAllowedState(stampSequence)) {
            resultSet.add(stampSequence);
         }
      }

      IntArrayList resultList = resultSet.keys();

      resultList.sort();
      return resultList.elements();
   }

   /**
    * Gets the latest version.
    *
    * @param <C> the generic type
    * @param <V> the value type
    * @param chronicle the chronicle
    * @return the latest version
    */
   public <C extends Chronology, V extends StampedVersion> LatestVersion<V> getLatestVersion(C chronicle) {
      final HashSet<V> latestVersionSet = new HashSet<>();

      chronicle.getVersionList()
               .stream()
               .filter((newVersionToTest) -> (newVersionToTest.getTime() != Long.MIN_VALUE))
               .filter((newVersionToTest) -> (onRoute(newVersionToTest)))
               .forEach(
                   (newVersionToTest) -> {
                      if (latestVersionSet.isEmpty()) {
                         latestVersionSet.add((V) newVersionToTest);
                      } else {
                         handlePart(latestVersionSet, newVersionToTest);
                      }
                   });

      if (State.isActiveOnlySet(this.coordinate.getAllowedStates())) {
         final HashSet<V> inactiveVersions = new HashSet<>();

         latestVersionSet.stream()
                         .forEach(
                             (version) -> {
                                if (version.getState() != State.ACTIVE) {
                                   inactiveVersions.add(version);
                                }
                             });
         latestVersionSet.removeAll(inactiveVersions);
      }

      final List<V> latestVersionList = new ArrayList<>(latestVersionSet);

      if (latestVersionList.isEmpty()) {
         return new LatestVersion();
      }

      if (latestVersionList.size() == 1) {
         return new LatestVersion<>(latestVersionList.get(0));
      }

      return new LatestVersion<>(latestVersionList.get(0), latestVersionList.subList(1, latestVersionList.size()));
   }

   /**
    * Gets the latest version.
    *
    * @param <V> the value type
    * @param chronicle the chronicle
    * @return the latest version
    */
   public <V extends ObservableVersion> LatestVersion<V> getLatestVersion(ObservableChronology chronicle) {
      final HashSet<V> latestVersionSet = new HashSet<>();

      chronicle.getVersionList()
               .stream()
               .filter((newVersionToTest) -> (newVersionToTest.getTime() != Long.MIN_VALUE))
               .filter((newVersionToTest) -> (onRoute(newVersionToTest)))
               .forEach(
                   (newVersionToTest) -> {
                      if (latestVersionSet.isEmpty()) {
                         latestVersionSet.add((V) newVersionToTest);
                      } else {
                         handlePart(latestVersionSet, newVersionToTest);
                      }
                   });

      final List<V> latestVersionList = new ArrayList<>(latestVersionSet);

      if (latestVersionList.isEmpty()) {
         return new LatestVersion();
      }

      if (latestVersionList.size() == 1) {
         return new LatestVersion<>(latestVersionList.get(0));
      }

      return new LatestVersion<>(latestVersionList.get(0), latestVersionList.subList(1, latestVersionList.size()));
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class Segment.
    */
   private class Segment {
      /**
       * Each segment gets it's own sequence which gets greater the further
       * prior to the position of the relative position computer.
       * TODO if we have a path sequence, may not need segment sequence.
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

      /** The preceding segments. */
      ConcurrentSkipListSet<Integer> precedingSegments;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new segment.
       *
       * @param segmentSequence the segment sequence
       * @param pathConceptSequence the path concept sequence
       * @param endTime the end time
       * @param precedingSegments the preceding segments
       */
      private Segment(int segmentSequence, int pathConceptSequence, long endTime, ConcurrentSkipListSet<Integer> precedingSegments) {
         this.segmentSequence     = segmentSequence;
         this.pathConceptSequence = pathConceptSequence;
         this.endTime             = endTime;
         this.precedingSegments   = precedingSegments.clone();
      }

      //~--- methods ----------------------------------------------------------

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return "Segment{" + this.segmentSequence + ", pathConcept=" + Get.conceptDescriptionText(
             this.pathConceptSequence) + "<" + this.pathConceptSequence + ">, endTime=" + Instant.ofEpochMilli(
                 this.endTime) + ", precedingSegments=" + this.precedingSegments + '}';
      }

      /**
       * Contains position.
       *
       * @param pathConceptSequence the path concept sequence
       * @param moduleConceptSequence the module concept sequence
       * @param time the time
       * @return true, if successful
       */
      private boolean containsPosition(int pathConceptSequence, int moduleConceptSequence, long time) {
         if (RelativePositionCalculator.this.coordinate.getModuleNids().isEmpty() ||
               RelativePositionCalculator.this.coordinate.getModuleNids().contains(moduleConceptSequence)) {
            if ((this.pathConceptSequence == pathConceptSequence) && (time != Long.MIN_VALUE)) {
               return time <= this.endTime;
            }
         }

         return false;
      }
   }
}

