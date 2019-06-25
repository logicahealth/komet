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
package sh.isaac.api.coordinate;

import java.util.UUID;

/**
 *
 * @author kec
 */
public interface ManifoldCoordinateProxy extends ManifoldCoordinate {

   /**
    * Gets the manifold coordinate.
    *
    * @return a ManifoldCoordinate that specifies how to manage the retrieval and display of taxonomy information.
    */
   public ManifoldCoordinate getManifoldCoordinate();

   @Override
   public default ManifoldCoordinate makeCoordinateAnalog(PremiseType taxonomyType) {
      return getManifoldCoordinate().makeCoordinateAnalog(taxonomyType);
   }

   @Override
   public default PremiseType getTaxonomyPremiseType() {
      return getManifoldCoordinate().getTaxonomyPremiseType();
   }

   @Override
   public default UUID getCoordinateUuid() {
      return getManifoldCoordinate().getCoordinateUuid();
   }
   
   @Override
   public default StampCoordinate getDestinationStampCoordinate() {
      return getManifoldCoordinate().getDestinationStampCoordinate();
   }
   
   @Override
   public default LogicCoordinate getLogicCoordinate() {
      return getManifoldCoordinate().getLogicCoordinate();
   }
}
