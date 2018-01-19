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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.model.coordinate.StampPathImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.VersionManagmentPathService;

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
   ConcurrentHashMap<Integer, StampPath> pathMap;

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

      final Optional<StampPath> stampPath = getFromDisk(pathConceptId);

      return stampPath.isPresent();
   }

   /**
    * Setup path map.
    */
   private void setupPathMap() {
      LOCK.lock();
      
      LOG.info("Rebuilding the path map.  Old map size: {}", (this.pathMap == null ? 0 : this.pathMap.size()));
      try {
         ConcurrentHashMap<Integer, StampPath> newMap = new ConcurrentHashMap<>();
         
         Get.assemblageService()
            .getSemanticChronologyStream(TermAux.PATH_ASSEMBLAGE.getNid())
            .forEach((pathSememe) -> {
                        final int pathNid = pathSememe.getReferencedComponentNid();

                        newMap.put(pathNid, new StampPathImpl(pathNid));
                     });
         
         this.pathMap = newMap;
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
      for (final StampPosition origin: path.getPathOrigins()) {
         if (origin.getStampPathNid() == v1.getPathNid()) {
            if (v1.getTime() <= origin.getTime()) {
               return RelativePosition.BEFORE;
            }
         }
      }

      return RelativePosition.UNREACHABLE;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the from disk.
    *
    * @param stampPathNid the stamp path sequence
    * @return the from disk
    */
   private Optional<StampPath> getFromDisk(int stampPathNid) {
      return Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(stampPathNid, TermAux.PATH_ASSEMBLAGE.getNid()).map((sememeChronicle) -> {
                        int pathId = sememeChronicle.getReferencedComponentNid();
                        assert pathId == stampPathNid:
                               "pathId: " + pathId + " stampPathSequence: " + stampPathNid;

                        final StampPath stampPath = new StampPathImpl(stampPathNid);

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
   public Collection<? extends StampPosition> getOrigins(int stampPathNid) {
      return getPathOriginsFromDb(stampPathNid);
   }

   /**
    * Gets the path origins from db.
    *
    * @param nid the nid
    * @return the path origins from db
    */
   private List<StampPosition> getPathOriginsFromDb(int nid) {
      return Get.assemblageService()
                .getSemanticChronologyStreamForComponentFromAssemblage(nid, TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid())
                .map((pathOrigin) -> {
                        final long time = ((LongVersion) pathOrigin.getVersionList()
                                                                  .get(0)).getLongValue();

                        return new StampPositionImpl(time, nid);
                     })
                .collect(Collectors.toList());
   }

   /**
    * Gets the paths.
    *
    * @return the paths
    */
   @Override
   public Collection<? extends StampPath> getPaths() {
      return Get.assemblageService().getSemanticChronologyStream(TermAux.PATH_ASSEMBLAGE.getNid()).map((sememeChronicle) -> {
                        int pathId = sememeChronicle.getReferencedComponentNid();
                       final StampPath stampPath = new StampPathImpl(pathId);

                        return stampPath;
                     }).collect(Collectors.toList());
   }

   /**
    * Gets the relative position.
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @return the relative position
    */
   @Override
   public RelativePosition getRelativePosition(int stampSequence1, int stampSequence2) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the relative position.
    *
    * @param v1 the v 1
    * @param v2 the v 2
    * @return the relative position
    */
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

      if (traverseOrigins(v1, getStampPath(v2.getPathNid())) == RelativePosition.BEFORE) {
         return RelativePosition.BEFORE;
      }

      return traverseOrigins(v2, getStampPath(v1.getPathNid()));
   }

   /**
    * Gets the stamp path.
    *
    * @param stampPathNid the stamp path sequence
    * @return the stamp path
    */
   @Override
   public StampPath getStampPath(int stampPathNid) {
      if (exists(stampPathNid)) {
         return this.pathMap.get(stampPathNid);
      }

      final Optional<StampPath> stampPath = getFromDisk(stampPathNid);

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
      LOG.info("VersionManagementPathProvider starts");
      setupPathMap();
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

