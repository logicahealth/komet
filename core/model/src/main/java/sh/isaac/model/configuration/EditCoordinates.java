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

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.coordinate.EditCoordinateImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 2/16/15.
 */
public class EditCoordinates {
   /**
    * Gets the classifier solor overlay.
    *
    * @return the classifier solor overlay
    */
   public static EditCoordinate getClassifierSolorOverlay() {
      final EditCoordinate editCoordinate = new EditCoordinateImpl(TermAux.IHTSDO_CLASSIFIER.getNid(),
                                                                   TermAux.SOLOR_OVERLAY_MODULE.getNid(),
                                                                   TermAux.DEVELOPMENT_PATH.getNid());

      return editCoordinate;
   }

   /**
    * Gets the default user metadata.
    *
    * @return the default user metadata
    */
   public static EditCoordinate getDefaultUserMetadata() {
      final EditCoordinate editCoordinate = new EditCoordinateImpl(TermAux.USER.getNid(),
                                                                   TermAux.SOLOR_MODULE.getNid(),
                                                                   TermAux.DEVELOPMENT_PATH.getNid());

      return editCoordinate;
   }

   /**
    * Gets the default user solor overlay.
    *
    * @return the default user solor overlay
    */
   public static EditCoordinate getDefaultUserSolorOverlay() {
      final EditCoordinate editCoordinate = new EditCoordinateImpl(TermAux.USER.getNid(),
                                                                   TermAux.SOLOR_OVERLAY_MODULE.getNid(),
                                                                   TermAux.DEVELOPMENT_PATH.getNid());

      return editCoordinate;
   }

   /**
    * Gets the default user vhat coordinate.
    *
    * @return the default user vhat coordinate
    */
   public static EditCoordinate getDefaultUserVHAT() {
      final EditCoordinate editCoordinate = new EditCoordinateImpl(TermAux.USER.getNid(),
                                                                   TermAux.VHAT_MODULES.getNid(),
                                                                   TermAux.DEVELOPMENT_PATH.getNid());

      return editCoordinate;
   }
}

