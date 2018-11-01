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



package sh.isaac.model.configuration;

//~--- non-JDK imports --------------------------------------------------------

import java.util.ArrayList;
import java.util.HashSet;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampCoordinates.
 *
 * @author kec
 */
public class StampCoordinates {
   /**
    * Gets the development latest.
    *
    * @return the development latest
    */
   public static StampCoordinate getDevelopmentLatest() {
      final StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE,
                                                                TermAux.DEVELOPMENT_PATH);

      return new StampCoordinateImpl(StampPrecedence.PATH,
                                     stampPosition,
                                     new HashSet<>(),
                                     new ArrayList<>(),
                                     Status.makeAnyStateSet());
   }

   /**
    * Gets the development latest active only.
    *
    * @return the development latest active only
    */
   public static StampCoordinate getDevelopmentLatestActiveOnly() {
      final StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE,
                                                                TermAux.DEVELOPMENT_PATH);

      return new StampCoordinateImpl(StampPrecedence.PATH,
                                     stampPosition,
                                     new HashSet<>(),
                                     new ArrayList<>(),
                                     Status.makeActiveOnlySet());
   }

   /**
    * Gets the master latest.
    *
    * @return the master latest
    */
   public static StampCoordinate getMasterLatest() {
      final StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE,
                                                                TermAux.MASTER_PATH);

      return new StampCoordinateImpl(StampPrecedence.PATH,
                                     stampPosition,
                                     new HashSet<>(),
                                     new ArrayList<>(),
                                     Status.makeAnyStateSet());
   }

   /**
    * Gets the master latest active only.
    *
    * @return the master latest active only
    */
   public static StampCoordinate getMasterLatestActiveOnly() {
      final StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE,
                                                                TermAux.MASTER_PATH);

      return new StampCoordinateImpl(StampPrecedence.PATH,
                                     stampPosition,
                                     new HashSet<>(),
                                     new ArrayList<>(),
                                     Status.makeActiveOnlySet());
   }
}

