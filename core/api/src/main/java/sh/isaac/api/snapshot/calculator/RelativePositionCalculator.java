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

import java.util.*;
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


import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.RoaringIntSet;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.identity.IdentifiedObject;
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
public class RelativePositionCalculator implements StaticIsaacCache {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   private static RelativePositionCalculator lastCalculator = null;

   //~--- fields --------------------------------------------------------------

   /** The error count. */
   private int  errorCount   = 0;
   private StampService stampService;

   /** The coordinate. */
   private StampCoordinate coordinate;
   private EnumSet<Status>  allowedStates;
   private final ConcurrentHashMap<Integer, Boolean> stampOnRoute = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer, Boolean> stampIsAllowedState = new ConcurrentHashMap<>();

   /**
    * Mapping from pathNid to each segment for that pathNid. There is one entry
    * for each path reachable antecedent to the destination position of the
    * computer.
    */
   ConcurrentHashMap<Integer, Segment> pathNidSegmentMap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new relative position calculator.
    */
   private RelativePositionCalculator() {
      // No arg constructor for HK2 managed instance
   }

   /**
    * Instantiates a new relative position calculator.
    *
    * @param coordinate the coordinate
    */
   private RelativePositionCalculator(StampCoordinate coordinate) {
      //For the internal callback to populate the cache
      this.coordinate             = coordinate;
      this.pathNidSegmentMap = setupPathNidSegmentMap(coordinate.getStampPosition());
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
      final int  ss1ModuleNid = getStampService()
                                        .getModuleNidForStamp(stampSequence1);
      final int  ss1PathNid   = getStampService()
                                        .getPathNidForStamp(stampSequence1);
      final long ss2Time           = getStampService()
                                        .getTimeForStamp(stampSequence2);
      final int  ss2ModuleNid = getStampService()
                                        .getModuleNidForStamp(stampSequence2);
      final int  ss2PathNid   = getStampService()
                                        .getPathNidForStamp(stampSequence2);

      if (ss1PathNid == ss2PathNid) {
         final Segment seg = this.pathNidSegmentMap.get(ss1PathNid);

         if (seg.containsPosition(ss1PathNid, ss1ModuleNid, ss1Time) &&
               seg.containsPosition(ss2PathNid, ss2ModuleNid, ss2Time)) {
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

      final Segment seg1 = this.pathNidSegmentMap.get(ss1PathNid);
      final Segment seg2 = this.pathNidSegmentMap.get(ss2PathNid);

      if ((seg1 == null) || (seg2 == null)) {
         return RelativePosition.UNREACHABLE;
      }

      if (!(seg1.containsPosition(ss1PathNid, ss1ModuleNid, ss1Time) &&
            seg2.containsPosition(ss2PathNid, ss2ModuleNid, ss2Time))) {
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
         final Segment seg = this.pathNidSegmentMap.get(v1.getPathNid());

         if (seg == null) {
            final StringBuilder builder = new StringBuilder();

            builder.append("Segment cannot be null.");
            builder.append("\nv1: ")
                   .append(v1);
            builder.append("\nv2: ")
                   .append(v1);
            builder.append("\nno segment in map: ")
                   .append(this.pathNidSegmentMap);
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

      final Segment seg1 = this.pathNidSegmentMap.get(v1.getPathNid());
      final Segment seg2 = this.pathNidSegmentMap.get(v2.getPathNid());

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
      final Segment seg = this.pathNidSegmentMap.get(getStampService()
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
      final Segment seg = this.pathNidSegmentMap.get(version.getPathNid());

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
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "RelativePositionCalculator{" + this.coordinate + '}';
   }

   /**
    * Adds the origins to path nid segment map.
    *
    * @param destination the destination
    * @param pathNidSegmentMap the path nid segment map
    * @param segmentSequence the segment sequence
    * @param precedingSegments the preceding segments
    */

   // recursively called method
   private void addOriginsToPathNidSegmentMap(StampPosition destination,
         ConcurrentHashMap<Integer, Segment> pathNidSegmentMap,
         AtomicInteger segmentSequence,
         ConcurrentSkipListSet<Integer> precedingSegments) {
      final Segment segment = new Segment(
                                  segmentSequence.getAndIncrement(),
                                  destination.getStampPathSpecification().getNid(),
                                  destination.getTime(),
                                  precedingSegments);

      // precedingSegments is cumulative, each recursive call adds another
      precedingSegments.add(segment.segmentSequence);
      pathNidSegmentMap.put(destination.getStampPathSpecification().getNid(), segment);
      destination.getStampPath()
                 .getPathOrigins()
                 .stream()
                 .forEach((origin) -> {
         // Recursive call
                        addOriginsToPathNidSegmentMap(
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
               LOG.warn(
                   "{} should never happen. " + "Data is malformed. StampSequence: {} Part:\n{} \n  Part to test: \n{}\n",
                   new Object[] { RelativePosition.EQUAL, part.getStampSequence(), part, prevPartToTest,  
                       ((IdentifiedObject)part).getPrimordialUuid()});
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
   private void handleStamp(RoaringBitmap stampsForPosition, int stampSequence, boolean allowUncommitted) {
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
      final RoaringBitmap stampsToCompare = stampsForPosition.clone();

      stampsToCompare.forEach((IntConsumer) prevStamp -> {
         switch (fastRelativePosition(stampSequence, prevStamp, coordinate.getStampPrecedence())) {
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
               RelativePositionCalculator.this.errorCount++;

               if (RelativePositionCalculator.this.errorCount < 20) {
                  LOG.warn(
                          "{} should never happen. " + "\n  Data is malformed. \n   stamp: {}  \n   Part to test: {}",
                          new Object[] { RelativePosition.EQUAL,
                                  Get.stampService().describeStampSequence(stampSequence),
                                  Get.stampService().describeStampSequence(prevStamp)});
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
                               RelativePositionCalculator.this.coordinate.getStampPrecedence()));
         }
      });

   }

   /**
    * Setup path nid segment map.
    *
    * @param destination the destination
    * @return the open int object hash map
    */
   private ConcurrentHashMap<Integer, Segment> setupPathNidSegmentMap(StampPosition destination) {
      final ConcurrentHashMap<Integer, Segment> pathNidSegmentMapToSetup = new ConcurrentHashMap<>();
      final AtomicInteger                 segmentSequence               = new AtomicInteger(0);

      // the sequence of the preceding segments is set in the recursive
      // call.
      final ConcurrentSkipListSet<Integer> precedingSegments = new ConcurrentSkipListSet<>();

      // call to recursive method...
      addOriginsToPathNidSegmentMap(destination,
          pathNidSegmentMapToSetup,
          segmentSequence,
          precedingSegments);
      return pathNidSegmentMapToSetup;
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
       RelativePositionCalculator calcToTry = lastCalculator;
       if (calcToTry != null) {
           if (calcToTry.coordinate == coordinate) {
               return calcToTry;
           }
       }

      calcToTry = new RelativePositionCalculator(coordinate);
      lastCalculator = calcToTry;

      return calcToTry;
   }

   /**
    * Gets the calculator.
    *
    * @param coordinate the coordinate
    * @return the calculator
    */
   public RelativePositionCalculator getCalculatorInstance(StampCoordinate coordinate) {
       
       RelativePositionCalculator calcToTry = lastCalculator;
       if (calcToTry != null) {
           if (calcToTry.coordinate == coordinate) {
               return calcToTry;
           }
       }

      calcToTry = new RelativePositionCalculator(coordinate);
      lastCalculator = calcToTry;

      return calcToTry;
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
         if (getStampService().getStatusForStamp(stampSequence) == Status.ACTIVE) {
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
         return new LatestVersion<>();
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
      RoaringBitmap stampsForPosition = new RoaringBitmap();
      for (int stampToCompare: stampSequences) {
         handleStamp(stampsForPosition, stampToCompare, false);
      }
      return getResults(stampsForPosition);
   }

   /**
    * Gets the latest stamp sequences as set. The latest stamp sequences independent of
    * allowed states of the stamp coordinate are identified. Then, if those latest stamp's status values
    * are included in the allowed states, then the stamps are included in the result. If none of the
    * latest stamps are of an allowed state, then an empty set is returned.
    *
    * @param stampSequences the stamp sequence stream
    * @return the latest stamp sequences as an array. Empty array if none of the
    * latest stamps match the allowed states of the stamp coordinate.
    */
   public int[] getLatestStampSequencesAsSet(int[] stampSequences) {

      RoaringBitmap stampsForPosition = new RoaringBitmap();
      for (int stampToCompare: stampSequences) {
         handleStamp(stampsForPosition, stampToCompare, true);
      }

      return getResults(stampsForPosition);
   }

   private int[] getResults(RoaringBitmap stampsForPosition) {
      RoaringBitmap resultList = new RoaringBitmap();

      stampsForPosition.forEach((IntConsumer) stampSequence -> {
         if (isAllowedState(stampSequence)) {
            resultList.add(stampSequence);
         }
      });
      return resultList.toArray();
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

      if (Status.isActiveOnlySet(this.coordinate.getAllowedStates())) {
         final HashSet<V> inactiveVersions = new HashSet<>();

         latestVersionSet.stream()
                         .forEach((version) -> {
                                if (version.getStatus() != Status.ACTIVE) {
                                   inactiveVersions.add(version);
                                }
                             });
         latestVersionSet.removeAll(inactiveVersions);
      }

      final List<V> latestVersionList = new ArrayList<>(latestVersionSet);

      if (latestVersionList.isEmpty()) {
         return new LatestVersion<>();
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
         return new LatestVersion<>();
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
       * TODO if we have a path nid, may not need segment sequence.
       */
      int segmentSequence;

      /**
       * The pathConceptNid of this segment. Each ancestor path to the
       * position of the computer gets it's own segment.
       */
      int pathConceptNid;

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
       * @param pathConceptNid the path concept nid
       * @param endTime the end time
       * @param precedingSegments the preceding segments
       */
      private Segment(int segmentSequence, int pathConceptNid, long endTime, ConcurrentSkipListSet<Integer> precedingSegments) {
         this.segmentSequence     = segmentSequence;
         this.pathConceptNid = pathConceptNid;
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
         return "Segment{" + this.segmentSequence + ", pathConcept=" + Get.conceptDescriptionText(this.pathConceptNid) + "<" + this.pathConceptNid + ">, endTime=" + Instant.ofEpochMilli(
                 this.endTime) + ", precedingSegments=" + this.precedingSegments + '}';
      }

      /**
       * Contains position.
       *
       * @param pathConceptNid the path concept nid
       * @param moduleConceptNid the module concept nid
       * @param time the time
       * @return true, if successful
       */
      private boolean containsPosition(int pathConceptNid, int moduleConceptNid, long time) {
         if (RelativePositionCalculator.this.coordinate.getModuleNids().isEmpty() ||
               RelativePositionCalculator.this.coordinate.getModuleNids().contains(moduleConceptNid)) {
            if ((this.pathConceptNid == pathConceptNid) && (time != Long.MIN_VALUE)) {
               return time <= this.endTime;
            }
         }

         return false;
      }
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public void reset() {
      lastCalculator = null;
   }
}

