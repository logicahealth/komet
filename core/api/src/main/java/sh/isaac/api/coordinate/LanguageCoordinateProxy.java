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
import java.util.Optional;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
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
   default LanguageCoordinateImmutable toLanguageCoordinateImmutable() {
      return getLanguageCoordinate().toLanguageCoordinateImmutable();
   }

   @Override
   default ConceptSpecification[] getDescriptionTypeSpecPreferenceList() {
      return getLanguageCoordinate().getDescriptionTypeSpecPreferenceList();
   }

   @Override
   default ConceptSpecification[] getDialectAssemblageSpecPreferenceList() {
      return getLanguageCoordinate().getDialectAssemblageSpecPreferenceList();
   }

   @Override
   default int[] getModulePreferenceListForLanguage() {
      return getLanguageCoordinate().getModulePreferenceListForLanguage();
   }

   @Override
   default ConceptSpecification[] getModuleSpecPreferenceListForLanguage() {
      return getLanguageCoordinate().getModuleSpecPreferenceListForLanguage();
   }

   @Override
   default ConceptSpecification getLanguageConcept() {
      return getLanguageCoordinate().getLanguageConcept();
   }

   @Override
   default LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
      return getLanguageCoordinate().getDefinitionDescription(descriptionList, stampFilter);
   }

   @Override
   default LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
      return getLanguageCoordinate().getDescription(descriptionList, stampFilter);
   }

   @Override
   default int[] getDescriptionTypePreferenceList() {
      return getLanguageCoordinate().getDescriptionTypePreferenceList();
   }

   @Override
   default int[] getDialectAssemblagePreferenceList() {
      return getLanguageCoordinate().getDialectAssemblagePreferenceList();
   }

   @Override
   default LatestVersion<DescriptionVersion> getFullyQualifiedDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
      return getLanguageCoordinate().getFullyQualifiedDescription(descriptionList, stampFilter);
   }

   @Override
   default int getLanguageConceptNid() {
      return getLanguageCoordinate().getLanguageConceptNid();
   }

   @Override
   default LatestVersion<DescriptionVersion> getRegularDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
      return getLanguageCoordinate().getRegularDescription(descriptionList, stampFilter);
   }

   @Override
   default Optional<? extends LanguageCoordinate> getNextPriorityLanguageCoordinate() {
      return getLanguageCoordinate().getNextPriorityLanguageCoordinate();
   }
}
