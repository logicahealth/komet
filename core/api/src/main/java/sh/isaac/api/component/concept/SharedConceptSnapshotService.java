/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.component.concept;

import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 * TODO: determine if this class is necessary or overlooked. 
 * @author kec
 */
public interface SharedConceptSnapshotService {

   /**
    * Simple method for getting text of the description of a concept.
    * This method will return a description type according to the constraints of
    * the
    * {@code StampCoordinate} and the default
    * {@code LanguageCoordinate}.
    * @param conceptNid of the concept to get the description for
    * @return a description for this concept. If no description can be found,
    * {@code "No desc for: " + conceptNid;} will be returned.
    */
   String conceptDescriptionText(int conceptNid);

   /**
    * Gets the manifold coordinate.
    *
    * @return the {@code ManifoldCoordinate} associated with this snapshot.
    */
   ManifoldCoordinate getManifoldCoordinate();

   //~--- get methods ---------------------------------------------------------
   /**
    * Checks if concept active.
    *
    * @param conceptNid of the concept to determine if it is active
    * according to the {@code StampCoordinate} of this snapshot service
    * @return true, if concept active
    */
   boolean isConceptActive(int conceptNid);
   
}
