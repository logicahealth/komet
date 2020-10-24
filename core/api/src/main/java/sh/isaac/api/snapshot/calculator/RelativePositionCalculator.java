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

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.Status;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

/**
 * The Class RelativePositionCalculator.
 *
 * @author kec
 */
//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
@Service
public class RelativePositionCalculator implements StaticIsaacCache {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   private static final ConcurrentReferenceHashMap<StampFilterImmutable, RelativePositionCalculator> SINGLETONS =
           new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                   ConcurrentReferenceHashMap.ReferenceType.WEAK);

   /** The error count. */
   private int  errorCount   = 0;
   private StampService stampService;

   /** The coordinate. */
   private final StampFilterImmutable filter;
   private final StatusSet allowedStates;
   private final ConcurrentHashMap<Integer, Boolean> stampOnRoute = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Integer, Boolean> stampIsAllowedState = new ConcurrentHashMap<>();

   /**
    * Mapping from pathNid to each segment for that pathNid. There is one entry
    * for each path reachable antecedent to the destination position of the
    * computer.
    */
   ConcurrentHashMap<Integer, Segment> pathNidSegmentMap;

   /**
    * Instantiates a new relative position calculator.
    */
   private RelativePositionCalculator() {
      // No arg constructor for HK2 managed instance
      // This instance just enables reset functionality...
      this.filter = null;
      this.allowedStates = null;
   }

   /**
    * Instantiates a new relative position calculator.
    *
    * @param filter the coordinate
    */
   private RelativePositionCalculator(StampFilterImmutable filter) {
      //For the internal callback to populate the cache
      this.filter = filter;
      this.pathNidSegmentMap = setupPathNidSegmentMap(filter.getStampPosition().toStampPositionImmutable());
      this.allowedStates          = filter.getAllowedStates();
   }

   /**
    * Fast relative position.
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @return the relative position
    */
   public RelativePosition fastRelativePosition(int stampSequence1,
         int stampSequence2) {
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
    * @return the relative position
    */
   public RelativePosition fastRelativePosition(StampedVersion v1,
         StampedVersion v2) {
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

      return fastRelativePosition(stampSequence1, stampSequence2);
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

      return fastRelativePosition(v1, v2);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "RelativePositionCalculator{" + this.filter + '}';
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
   private void addOriginsToPathNidSegmentMap(StampPositionImmutable destination,
                                              ConcurrentHashMap<Integer, Segment> pathNidSegmentMap,
                                              AtomicInteger segmentSequence,
                                              ConcurrentSkipListSet<Integer> precedingSegments) {
      final Segment segment = new Segment(
                                  segmentSequence.getAndIncrement(),
                                  destination.getPathForPositionConcept().getNid(),
                                  destination.getTime(),
                                  precedingSegments);

      // precedingSegments is cumulative, each recursive call adds another
      precedingSegments.add(segment.segmentSequence);
      Segment old = pathNidSegmentMap.put(destination.getPathForPositionNid(), segment);
      if (old != null) {
         LOG.error("Overwrite segment {} with {} for path {}", old, segment, destination.getPathForPositionConcept());
      }
      destination.getPathOrigins()
                 .stream()
                 .forEach((StampPositionImmutable origin) -> {
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
         switch (fastRelativePosition(part, prevPartToTest)) {
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
   private void handleStamp(MutableIntSet stampsForPosition, int stampSequence, boolean allowUncommitted) {
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
      final ImmutableIntSet stampsToCompare = IntSets.immutable.ofAll(stampsForPosition);

      stampsToCompare.forEach(prevStamp -> {
         switch (fastRelativePosition(stampSequence, prevStamp)) {
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

               // Duplicate values encountered.  Likely two stamps at the same time on different modules.
               //TODO this should be using the module preference order to determine which one to put at the top...
               stampsForPosition.add(stampSequence);
               break;

            case UNREACHABLE:

               // nothing to do...
               break;

            default:
               throw new UnsupportedOperationException(
                       "n Can't handle: " + fastRelativePosition(
                               stampSequence,
                               prevStamp));
         }
      });

   }

   /**
    * Setup path nid segment map.
    *
    * @param destination the destination
    * @return the open int object hash map
    */
   private ConcurrentHashMap<Integer, Segment> setupPathNidSegmentMap(StampPositionImmutable destination) {
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
    * @param filter the filter
    * @return the calculator
    */
   public static RelativePositionCalculator getCalculator(StampFilterImmutable filter) {
      return SINGLETONS.computeIfAbsent(filter,
              filterKey -> new RelativePositionCalculator(filter));
   }

   /**
    * Checks if latest active.
    *
    * @param stampSequences A stream of stampSequences from which the latest is
    * found, and then tested to determine if the latest is active.
    * @return true if any of the latest stampSequences (may be multiple in the
    * case of a contradiction) are active.
    */
   public boolean isLatestActive(int[] stampSequences) {
      for (int stampSequence: getLatestStampSequencesAsSet(stampSequences)) {
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
                         (!Get.stampService().isUncommitted(newVersionToTest.getStampSequence()))))
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
    * Gets the latest (committed only) stamp sequences as a sorted set in an array.
    *
    * @param stampSequences the stamp sequence stream
    * @return the latest stamp sequences as a sorted set in an array
    */
   public int[] getLatestCommittedStampSequencesAsSet(int[] stampSequences) {
      MutableIntSet stampsForPosition = IntSets.mutable.empty();
      for (int stampToCompare: stampSequences) {
         handleStamp(stampsForPosition, stampToCompare, false);
      }
      return getResults(stampsForPosition);
   }

   /**
    * Gets the latest stamp sequences as an array, allowing uncommitted stamps.
    * The latest stamp sequences independent of allowed states of the stamp coordinate are identified. 
    * Then, if those latest stamp's status values are included in the allowed states, then the stamps are included in the result. 
    * If none of the latest stamps are of an allowed state, then an empty set is returned.
    *
    * @param stampSequences the input stamp sequences
    * @return the latest stamp sequences as an array. Empty array if none of the
    * latest stamps match the allowed states of the stamp coordinate.
    */
   public int[] getLatestStampSequencesAsSet(int[] stampSequences) {

      MutableIntSet stampsForPosition = IntSets.mutable.empty();
      for (int stampToCompare: stampSequences) {
         handleStamp(stampsForPosition, stampToCompare, true);
      }

      return getResults(stampsForPosition);
   }

   private int[] getResults(MutableIntSet stampsForPosition) {
      MutableIntSet resultList = IntSets.mutable.of();

      stampsForPosition.forEach(stampSequence -> {
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

      if (this.filter.getAllowedStates().isActiveOnly()) {
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
      // TODO this observable method is slightly different than the primitive version. Is that to support temporary observable only versions?
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
         if (allowedStates.contains(latestVersionList.get(0).getStatus())) {
            return new LatestVersion<>(latestVersionList.get(0));
         }
         return new LatestVersion<>();
      }
      if (allowedStates.contains(latestVersionList.get(0).getStatus())) {
         return new LatestVersion<>(latestVersionList.get(0), latestVersionList.subList(1, latestVersionList.size()));
      }
      for (int i = 1; i < latestVersionList.size(); i++) {
         if (allowedStates.contains(latestVersionList.get(i).getStatus())) {
            final List<V> latestVersionSubList = new ArrayList<>(latestVersionSet);
            latestVersionSubList.remove(i);
            return new LatestVersion<>(latestVersionList.get(0), latestVersionSubList);
         }
      }
      return new LatestVersion<>();
   }

   /**
    * The Class Segment.
    */
   private class Segment {
      /**
       * Each segment gets it's own sequence which gets greater the further
       * prior to the position of the relative position computer.
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
         if (RelativePositionCalculator.this.filter.getModuleNids().isEmpty() ||
               RelativePositionCalculator.this.filter.getModuleNids().contains(moduleConceptNid)) {
            if (RelativePositionCalculator.this.filter.getExcludedModuleNids().isEmpty() ||
                    !RelativePositionCalculator.this.filter.getExcludedModuleNids().contains(moduleConceptNid)) {
               if ((this.pathConceptNid == pathConceptNid) && (time != Long.MIN_VALUE)) {
                  return time <= this.endTime;
               }
            }
         }

         return false;
      }
   }

   public static <V extends Version> List<Graph<V>> getVersionGraphList(Collection<V> versionList) {
      VersionManagmentPathService pathService = Get.versionManagmentPathService();
      SortedSet<VersionWithDistance<V>> versionWithDistances = new TreeSet<>();
      versionList.forEach(v -> versionWithDistances.add(new VersionWithDistance<>(v)));

      final List<Graph<V>> results = new ArrayList<>();

      int loopCheck = 0;
      while (!versionWithDistances.isEmpty()) {
         loopCheck++;
         if (loopCheck > 100) {
            throw new IllegalStateException("loopCheck = " + loopCheck);
         }
         Graph<V> graph = new Graph<>();
         results.add(graph);
         Set<Node<V>> leafNodes = new HashSet<>();
         SortedSet<VersionWithDistance<V>> nodesInTree = new TreeSet<>();
         for (VersionWithDistance versionWithDistance: versionWithDistances) {
            if (graph.getRoot() == null) {
               leafNodes.add(graph.createRoot((V) versionWithDistance.version));
               nodesInTree.add(versionWithDistance);
            } else {
               List<Node<V>> leafList = new ArrayList<>(leafNodes);
               for (Node<V> leafNode: leafList) {
                  switch (pathService.getRelativePosition(versionWithDistance.version, leafNode.getData())) {
                     case AFTER:
                        Node<V> newLeaf = leafNode.addChild((V) versionWithDistance.version);
                        nodesInTree.add(versionWithDistance);
                        leafNodes.remove(leafNode);
                        leafNodes.add(newLeaf);
                        break;
                     case EQUAL:
                        // TODO handle different modules... ?
                        throw new IllegalStateException("Version can only be in one module at a time. \n"
                                + leafNode.getData() + "\n" + versionWithDistance.version);
                     case BEFORE:
                        throw new IllegalStateException("Sort order error. \n"
                                + leafNode.getData() + "\n" + versionWithDistance.version);
                     case UNREACHABLE:
                        // if not after by any leaf (unreachable from any leaf), then node will be left in set, and possibly added to next graph.
                        break;
                     default:
                        throw new IllegalStateException("Sort order error. \n"
                                + leafNode.getData() + "\n" + versionWithDistance.version +
                                pathService.getRelativePosition(leafNode.getData(), versionWithDistance.version));
                  }
               }
            }
         }
         versionWithDistances.removeAll(nodesInTree);
      }
      return results;
   }


   private static BigInteger getDistance(StampPosition position) {
      int pathDistanceFromOrigin = pathDistanceFromOrigin(0, position.toStampPositionImmutable());
      return BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(pathDistanceFromOrigin)).add(BigInteger.valueOf(position.getTime()));
   }

   private static int pathDistanceFromOrigin(int cumulativeDistance, StampPositionImmutable positionImmutable) {
      if (positionImmutable.getPathForPositionNid() != TermAux.PRIMORDIAL_PATH.getNid()) {
         int computedDistance = Integer.MAX_VALUE;
         for (StampPositionImmutable origin: positionImmutable.getPathOrigins()) {
            computedDistance = Math.min(computedDistance, pathDistanceFromOrigin(cumulativeDistance + 1, origin));
         }
         return computedDistance;
      }
      return cumulativeDistance;
   }

   private static class VersionWithDistance<V extends Version> implements Comparable<VersionWithDistance> {
      final BigInteger computedDistance;
      final V version;

      public VersionWithDistance(V version) {
         this.version = version;
         this.computedDistance = getDistance(Get.stampService().getStampPosition(version.getStampSequence()));
      }

      @Override
      public int compareTo(VersionWithDistance o) {
         return this.computedDistance.compareTo(o.computedDistance);
      }
   }

   @Override
   public void reset() {
      SINGLETONS.clear();
   }
}

