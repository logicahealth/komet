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

import java.util.EnumSet;
import sh.isaac.api.State;
import sh.isaac.api.collections.ConceptSequenceSet;

/**
 *
 * @author kec
 */
public interface StampCoordinateProxy extends StampCoordinate {
   
   /**
    * Gets the stamp coordinate.
    *
    * @return a StampCoordinate that specifies the retrieval and display of
    * object chronicle versions by indicating the current position on a path, and allowed modules.
    */
   StampCoordinate getStampCoordinate();

   @Override
   public default EnumSet<State> getAllowedStates() {
      return getStampCoordinate().getAllowedStates();
   }

   @Override
   public default ConceptSequenceSet getModuleSequences() {
      return getStampCoordinate().getModuleSequences();
   }

   @Override
   public default StampPosition getStampPosition() {
      return getStampCoordinate().getStampPosition();
   }

   @Override
   public default StampPrecedence getStampPrecedence() {
      return getStampCoordinate().getStampPrecedence();
   }

   @Override
   public default StampCoordinate makeCoordinateAnalog(long stampPositionTime) {
      return getStampCoordinate().makeCoordinateAnalog(stampPositionTime);
   }

   @Override
   public default StampCoordinate makeCoordinateAnalog(State... state) {
      return getStampCoordinate().makeCoordinateAnalog(state);
   }
   
   
}
