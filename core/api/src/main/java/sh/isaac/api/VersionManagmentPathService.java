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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePosition;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface PathService.
 *
 * @author kec
 */
@Contract
public interface VersionManagmentPathService {
   /**
    * Exists.
    *
    * @param pathConceptId the path concept id
    * @return true, if successful
    */
   boolean exists(int pathConceptId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the origins.
    *
    * @param stampPathNid the stamp path nid
    * @return the origins
    */
   Collection<? extends StampPosition> getOrigins(int stampPathNid);

   /**
    * Gets the paths.
    *
    * @return the paths
    */
   Collection<? extends StampPath> getPaths();

   /**
    * Gets the relative position.
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @return the relative position
    */
   RelativePosition getRelativePosition(int stampSequence1, int stampSequence2);

   /**
    * Gets the relative position.
    *
    * @param v1 the v 1
    * @param v2 the v 2
    * @return the relative position
    */
   RelativePosition getRelativePosition(StampedVersion v1, StampedVersion v2);

   /**
    * Gets the relative position.
    *
    * @param stampSequence1 the v1 stampSequence1
    * @param v2 the v2 coordinate
    * @return the relative position
    */
   RelativePosition getRelativePosition(int stampSequence1, StampCoordinate v2);


   /**
    * Gets the stamp path.
    *
    * @param stampPathSequence the stamp path nid
    * @return the stamp path
    */
   StampPath getStampPath(int stampPathSequence);
   
   /**
    * rebuild internal knowledge of paths (may be necessary after metadata loads, etc)
    * TODO make the versionManagementPathService a commit watcher of sorts, so it can automatically do this when necessary?
    */
   void rebuildPathMap();
}

