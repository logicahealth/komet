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



package sh.isaac.provider.path;

//~--- JDK imports ------------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * TODO handle versions properly in path provider.
 *
 * @author kec
 */
@Service(name = "Path Provider")
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class VersionManagmentPathProvider
         implements VersionManagmentPathService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant LOCK. */
   private static final Lock LOCK = new ReentrantLock();

   //~--- fields --------------------------------------------------------------

   /** The path map. */
   ConcurrentHashMap<Integer, StampPathImmutable> pathMap;
   ConcurrentHashMap<Integer, ImmutableSet<StampBranchImmutable>> branchMap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new path provider.
    */
   private VersionManagmentPathProvider() {
      //For HK2 only
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Exists.
    *
    * @param pathConceptId the path concept id
    * @return true, if successful
    */
   @Override
   public boolean exists(int pathConceptId) {

      if (this.pathMap.containsKey(pathConceptId)) {
         return true;
      }

      final Optional<StampPathImmutable> stampPath = getFromDisk(pathConceptId);

      return stampPath.isPresent();
   }

   public ImmutableSet<StampBranchImmutable> getBranches(int pathConceptNid) {
      return branchMap.computeIfAbsent(pathConceptNid, (pathNid) -> Sets.immutable.empty());
   }

   /**
    * Setup path map.
    */
   private void setupPathMap() {
      LOCK.lock();
      
      LOG.info("Rebuilding the path map.  Old map size: {}", (this.pathMap == null ? 0 : this.pathMap.size()));
      try {
         ConcurrentHashMap<Integer, StampPathImmutable> newMap = new ConcurrentHashMap<>();
         ConcurrentHashMap<Integer, ImmutableSet<StampBranchImmutable>> newForkMap = new ConcurrentHashMap<>();

         Get.assemblageService()
            .getSemanticChronologyStream(TermAux.PATH_ASSEMBLAGE.getNid())
            .forEach((pathSemantic) -> {
                        final int pathNid = pathSemantic.getReferencedComponentNid();
                        ImmutableSet<StampPositionImmutable> pathOrigins = getOrigins(pathNid);
                        newMap.put(pathNid, StampPathImmutable.make(pathNid, pathOrigins));
                        for (StampPositionImmutable pathOrigin: pathOrigins) {
                           newForkMap.merge(pathOrigin.getPathForPositionNid(),
                                   Sets.immutable.of(StampBranchImmutable.make(pathNid, pathOrigin.getTime())),
                                           (ImmutableSet<StampBranchImmutable> v1, ImmutableSet<StampBranchImmutable> v2) -> {
                                      MutableSet<StampBranchImmutable> forkSet = Sets.mutable.withAll(v1);
                                      forkSet.addAll(v2.castToSet());
                                      return forkSet.toImmutable();
                                   });
                        }
                     });
         
         this.pathMap = newMap;
         this.branchMap = newForkMap;
      } finally {
         LOCK.unlock();
      }
      LOG.info("Finished rebuilding the path map.  New map size: {}", this.pathMap.size());
   }

   /**
    * Traverse origins.
    *
    * @param v1 the v 1
    * @param path the path
    * @return the relative position
    */
   private RelativePosition traverseOrigins(StampedVersion v1, StampPath path) {
      return traverseOrigins(v1.getStampSequence(), path);
   }
   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the from disk.
    *
    * @param stampPathNid the stamp path nid
    * @return the from disk
    */
   private Optional<StampPathImmutable> getFromDisk(int stampPathNid) {
      return Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(stampPathNid, TermAux.PATH_ASSEMBLAGE.getNid()).map((semanticChronicle) -> {
                        int pathId = semanticChronicle.getReferencedComponentNid();
                        assert pathId == stampPathNid:
                               "pathId: " + pathId + " stampPathSequence: " + stampPathNid;

                        final StampPathImmutable stampPath = StampPathImmutable.make(stampPathNid);

                        this.pathMap.put(stampPathNid, stampPath);
                        return stampPath;
                     }).findFirst();
   }

   /**
    * Gets the origins.
    *
    * @param stampPathNid the stamp path nid
    * @return the origins
    */
   @Override
   public ImmutableSet<StampPositionImmutable> getOrigins(int stampPathNid) {
      return getPathOriginsFromDb(stampPathNid);
   }

   /**
    * Gets the path origins from db.
    *
    * @param nid the nid
    * @return the path origins from db
    */
   private ImmutableSet<StampPositionImmutable> getPathOriginsFromDb(int nid) {
      ImmutableSet<StampPositionImmutable> origins = Sets.immutable.fromStream(Get.assemblageService()
              .getSemanticChronologyStreamForComponentFromAssemblage(nid, TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid())
              .map((pathOrigin) -> {
                 Nid1_Long2_Version originSemantic = (Nid1_Long2_Version) pathOrigin.getVersionList().get(0);
                 return StampPositionImmutable.make(originSemantic.getLong2(), originSemantic.getNid1());
              }));

      if (origins.isEmpty() && nid != TermAux.PRIMORDIAL_PATH.getNid()) {
         // A boot strap issue, only the primordial path should have no origins.
         // If terminology not completely loaded, content may not yet be ready.
         if (nid != TermAux.DEVELOPMENT_PATH.getNid() && nid != TermAux.MASTER_PATH.getNid()) {
            throw new IllegalStateException("Path with no origin: " + Get.getTextForComponent(nid));
         }
         return Sets.immutable.with(StampPositionImmutable.make(Long.MAX_VALUE, TermAux.PRIMORDIAL_PATH.getNid()));
      }
      return origins;
   }

   /**
    * Gets the paths.
    *
    * @return the paths
    */
   @Override
   public Collection<? extends StampPathImmutable> getPaths() {
      return Get.assemblageService().getSemanticChronologyStream(TermAux.PATH_ASSEMBLAGE.getNid()).map((semanticChronicle) -> {
                        int pathId = semanticChronicle.getReferencedComponentNid();
                        final StampPathImmutable stampPath = StampPathImmutable.make(pathId);

                        return stampPath;
                     }).collect(Collectors.toList());
   }



   @Override
   public RelativePosition getRelativePosition(int stamp, StampPosition position) {
      StampService stampService = Get.stampService();

      if (stampService.getPathNidForStamp(stamp) == position.getPathForPositionNid()) {
         if (stampService.getTimeForStamp(stamp) < position.getTime()) {
            return RelativePosition.BEFORE;
         }

         if (stampService.getTimeForStamp(stamp) > position.getTime()) {
            return RelativePosition.AFTER;
         }

         return RelativePosition.EQUAL;
      }
      // need to see if after on branched path.

      switch (traverseForks(stamp, position)) {
         case AFTER:
            return RelativePosition.AFTER;
      }

      // before or unreachable.
      return traverseOrigins(stamp, getStampPath(position.getPathForPositionNid()));
   }
   private RelativePosition traverseForks(int stamp, StampPosition position) {
      int stampPathNid = Get.stampService().getPathNidForStamp(stamp);
      if (stampPathNid == position.getPathForPositionNid()) {
         throw new IllegalStateException("You must check for relative position on the same path before calling traverseForks: " +
                 Get.stampService().describeStampSequence(stamp) + " compared to: " + position);
      }

      for (StampBranchImmutable branch: getBranches(position.getPathForPositionNid())) {
         switch (traverseForks(stampPathNid, branch)) {
            case AFTER:
               return RelativePosition.AFTER;
         }
      }
      return RelativePosition.UNREACHABLE;
   }
   private RelativePosition traverseForks(int stampPathNid, StampBranchImmutable stampBranchImmutable) {
      int pathOfBranchNid = stampBranchImmutable.getPathOfBranchNid();
      if (pathOfBranchNid == stampPathNid) {
         return RelativePosition.AFTER;
      }
      for (StampBranchImmutable branch: getBranches(stampBranchImmutable.getPathOfBranchNid())) {
         switch (traverseForks(stampPathNid, branch)) {
            case AFTER:
               return RelativePosition.AFTER;
         }
      }
      return RelativePosition.UNREACHABLE;
   }
   /**
    * Is the stamp reachable from the recursive origins of this path?
    * @param stamp
    * @param path
    * @return RelativePosition.BEFORE if reachable, RelativePosition.UNREACHABLE if not.
    */
   private RelativePosition traverseOrigins(int stamp, StampPath path) {
      StampService stampService = Get.stampService();
      for (final StampPosition origin: path.getPathOrigins()) {
         if (origin.getPathConcept().getNid() == stampService.getPathNidForStamp(stamp)) {
            if (stampService.getTimeForStamp(stamp) <= origin.getTime()) {
               return RelativePosition.BEFORE;
            }
         } else {
            traverseOrigins(stamp, StampPathImmutable.make(origin.getPathConcept().getNid()));
         }
      }
      return RelativePosition.UNREACHABLE;
   }


   @Override
   public RelativePosition getRelativePosition(int stampSequence1, int stampSequence2) {
      if (stampSequence1 == stampSequence2) {
         return RelativePosition.EQUAL;
      }
      StampService stampService = Get.stampService();


      if (stampService.getPathNidForStamp(stampSequence1) == stampService.getPathNidForStamp(stampSequence2)) {
         if (stampService.getTimeForStamp(stampSequence1) < stampService.getTimeForStamp(stampSequence2)) {
            return RelativePosition.BEFORE;
         }

         if (stampService.getTimeForStamp(stampSequence1) > stampService.getTimeForStamp(stampSequence2)) {
            return RelativePosition.AFTER;
         }

         return RelativePosition.EQUAL;
      }

      if (traverseOrigins(stampSequence1, getStampPath(stampService.getPathNidForStamp(stampSequence2))) == RelativePosition.BEFORE) {
         return RelativePosition.BEFORE;
      }

      return traverseOrigins(stampSequence2, getStampPath(stampService.getPathNidForStamp(stampSequence1)));
   }



   @Override
   public RelativePosition getRelativePosition(StampedVersion v1, StampedVersion v2) {
      if (v1.getPathNid() == v2.getPathNid()) {
         if (v1.getTime() < v2.getTime()) {
            return RelativePosition.BEFORE;
         }

         if (v1.getTime() > v2.getTime()) {
            return RelativePosition.AFTER;
         }

         return RelativePosition.EQUAL;
      }
      return getRelativePosition(v1.getStampSequence(),
              StampPositionImmutable.make(v2.getTime(), v2.getPathNid()));
   }

   /**
    * Gets the stamp path.
    *
    * @param stampPathNid the stamp path nid
    * @return the stamp path
    */
   @Override
   public StampPath getStampPath(int stampPathNid) {
      if (exists(stampPathNid)) {
         return this.pathMap.get(stampPathNid);
      }

      final Optional<StampPathImmutable> stampPath = getFromDisk(stampPathNid);

      if (stampPath.isPresent()) {
         return stampPath.get();
      }

      throw new IllegalStateException("No path for: " + stampPathNid + " " +
                                      Get.conceptService().getConceptChronology(stampPathNid).toString());
   }
   
   @Override
   public void rebuildPathMap() {
      setupPathMap();
   }
   
   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting Path provider");
      Get.executor().execute(progressTask);
      try {
         LOG.info("VersionManagementPathProvider starts");
         setupPathMap();
      } finally {
         progressTask.finished();
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("VersionManagementPathProvider stops");
      this.pathMap = null;
   }
}

