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

import java.util.List;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;

/**
 *
 * @author kec
 */
public interface LanguageCoordinateProxy extends LanguageCoordinate {
   /**
    * Gets the language coordinate.
    *
    * @return a LanguageCoordinate that specifies how to manage the retrieval and display of language.
    * and dialect information.
    */
   LanguageCoordinate getLanguageCoordinate();

   @Override
   public default LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
      return getLanguageCoordinate().getDescription(descriptionList, stampCoordinate);
   }

   @Override
   public default int[] getDescriptionTypePreferenceList() {
      return getLanguageCoordinate().getDescriptionTypePreferenceList();
   }

   @Override
   public default int[] getDialectAssemblagePreferenceList() {
      return getLanguageCoordinate().getDialectAssemblagePreferenceList();
   }

   @Override
   public default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
      return getLanguageCoordinate().getFullySpecifiedDescription(descriptionList, stampCoordinate);
   }

   @Override
   public default int getLanguageConceptSequence() {
      return getLanguageCoordinate().getLanguageConceptSequence();
   }

   @Override
   public default LatestVersion<DescriptionVersion> getPreferredDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
      return getLanguageCoordinate().getPreferredDescription(descriptionList, stampCoordinate);
   }

   
}
