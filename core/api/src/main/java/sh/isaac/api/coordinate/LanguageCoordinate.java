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
package sh.isaac.api.coordinate;

//~--- JDK imports ------------------------------------------------------------
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;

//~--- interfaces -------------------------------------------------------------
/**
 * Coordinate to manage the retrieval and display of language and dialect information.
 *
 * Created by kec on 2/16/15.
 */
public interface LanguageCoordinate extends Coordinate {

   final static Logger LOG = LogManager.getLogger();
   /**
    * If the current language coordinate fails to return a requested description, 
    * then the next priority language coordinate will be tried until a description is found, 
    * or until there are no next priority language coordinates left. 
    * 
    * @return 
    */
   Optional<LanguageCoordinate> getNextProrityLanguageCoordinate();
    
   /**
    * Return the latestDescription according to the type and dialect preferences of this {@code LanguageCoordinate}.
    *
    * @param descriptionList descriptions to consider
    * @param stampCoordinate the stamp coordinate
    * @return an optional latestDescription best matching the {@code LanguageCoordinate} constraints.
    */
   LatestVersion<DescriptionVersion> getDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate);

   /**
    * Return the latestDescription according to the type and dialect preferences of this {@code LanguageCoordinate}.
    * or a nested {@code LanguageCoordinate}
    *
    * @param conceptNid the concept nid. 
    * @param stampCoordinate the stamp coordinate
    * @return an optional latestDescription best matching the {@code LanguageCoordinate} constraints.
    */
   default LatestVersion<DescriptionVersion> getDescription(int conceptNid, StampCoordinate stampCoordinate) {
      return getDescription(Get.conceptService().getConceptDescriptions(conceptNid), stampCoordinate);
   }
   
   /**
    * Return the best description text according to language and dialect preferences of this {@code LanguageCoordinate}, 
    * but ignoring the type preferences of the coordinate, rather, using the supplied type preference order
    *
    * @param conceptNid the concept id. 
    * @param descriptionTypePreference the order of the description types to try to match, overriding and ignoring the 
    * {@link LanguageCoordinate#getDescriptionTypePreferenceList()} present in this manifold.
     * @param stampCoordinate
    * @return the best matching description to the {@link LanguageCoordinate} constraints within this ManifoldCoordinate and 
    * the supplied description type preference list, or empty if none available that match the {@link ManifoldCoordinate}.
    */
   public LatestVersion<DescriptionVersion> getDescription(int conceptNid, int[] descriptionTypePreference, StampCoordinate stampCoordinate); 
   
   /**
    * Return the best description text according to language and dialect preferences of this {@code LanguageCoordinate}, 
    * but ignoring the type preferences of the coordinate, rather, using the supplied type preference order
    *
    * @param descriptionList the descriptions to evaluate
    * @param descriptionTypePreference the order of the description types to try to match, overriding and ignoring the 
    * {@link LanguageCoordinate#getDescriptionTypePreferenceList()} present in this manifold.
     * @param stampCoordinate
    * @return the best matching description to the {@link LanguageCoordinate} constraints within this ManifoldCoordinate and 
    * the supplied description type preference list, or empty if none available that match the {@link ManifoldCoordinate}.
    */
   public LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, int[] descriptionTypePreference, StampCoordinate stampCoordinate); 
   
   default OptionalInt getAcceptabilityNid(int descriptionNid, int dialectAssemblageNid, StampCoordinate stampCoordinate) {
       NidSet acceptabilityChronologyNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(descriptionNid, dialectAssemblageNid);
       
       for (int acceptabilityChronologyNid: acceptabilityChronologyNids.asArray()) {
           SemanticChronology acceptabilityChronology = Get.assemblageService().getSemanticChronology(acceptabilityChronologyNid);
               LatestVersion<ComponentNidVersion> latestAcceptability = acceptabilityChronology.getLatestVersion(stampCoordinate);
               if (latestAcceptability.isPresent()) {
                   return OptionalInt.of(latestAcceptability.get().getComponentNid());
               }
       }
       return OptionalInt.empty();
   }

   /**
    * Gets the latestDescription type preference list.
    *
    * @return the latestDescription type preference list
    */
   int[] getDescriptionTypePreferenceList();
   ConceptSpecification[] getDescriptionTypeSpecPreferenceList();

   /**
    * Gets the dialect assemblage preference list.
    *
    * @return the dialect assemblage preference list
    */
   int[] getDialectAssemblagePreferenceList();
   ConceptSpecification[] getDialectAssemblageSpecPreferenceList();
   
   /**
    * Gets the module preference list. Used to adjudicate which component to 
    * return when more than one component is available. For example, if two modules
    * have different preferred names for the component, which one do you prefer to return?
    * @return the module preference list.  If this list is null or empty, the returned preferred
    * name in the multiple case is unspecified.
    */

   int[] getModulePreferenceListForLanguage();
   ConceptSpecification[] getModuleSpecPreferenceListForLanguage();
   /**
    * Convenience method - returns true if FQN is at the top of the latestDescription list.
    *
    * @return true, if FQN preferred
    */
   public default boolean isFQNPreferred() {
      for (final int descType : getDescriptionTypePreferenceList()) {
         if (descType
                 == Get.identifierService().getNidForUuids(
                         TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getPrimordialUuid())) {
            return true;
         }

         break;
      }

      return false;
   }

   /**
    * Gets the latestDescription of type {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}.  Will return empty, if 
    * no matching description type is found in this or any nested language coordinates
    * 
    * @param descriptionList the latestDescription list
    * @param stampCoordinate the stamp coordinate
    * @return the regular name latestDescription, if available
    */
   LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate);

   /**
    * Gets the latestDescription of type {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}.  Will return empty, if 
    * no matching description type is found in this or any nested language coordinates
    *
    * @param conceptId the conceptId to get the fully specified latestDescription for
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified latestDescription
    */
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
           int conceptId,
           StampCoordinate stampCoordinate) {
      return getFullySpecifiedDescription(Get.conceptService().getConceptDescriptions(conceptId), stampCoordinate);
   }

   /**
    * Gets the language concept nid.
    *
    * @return the language concept nid
    */
   int getLanguageConceptNid();

   /**
    * 
    * @return 
    */
    ConceptSpecification getLanguageConcept();

   /**
    * Gets the latestDescription of type {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}.  Will return empty, if 
    * no matching description type is found in this or any nested language coordinates
    * 
    * @param descriptionList the latestDescription list
    * @param stampCoordinate the stamp coordinate
    * @return the regular name latestDescription, if available
    */
   LatestVersion<DescriptionVersion> getPreferredDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate);

   /**
    * Return a description of type definition, or an empty latest version, if none are of type definition in this or any 
    * nested language coordinates
    * @param descriptionList
    * @param stampCoordinate
    * @return
    */
   LatestVersion<DescriptionVersion> getDefinitionDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate);

   /**
    * Gets the latestDescription of type {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}.  Will return empty, if 
    * no matching description type is found in this or any nested language coordinates
    *
    * @param conceptNid the conceptId to get the fully specified latestDescription for
    * @param stampCoordinate the stamp coordinate
    * @return the regular name latestDescription
    */
   default LatestVersion<DescriptionVersion> getPreferredDescription(
           int conceptNid,
           StampCoordinate stampCoordinate) {
       Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptNid);
       if (optionalConcept.isPresent()) {
           return getPreferredDescription(optionalConcept.get().getConceptDescriptionList(), stampCoordinate);
       }
       return LatestVersion.empty();
   }

   /**
    * Gets the text of type {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}.  Will return empty, if 
    * no matching description type is found in this or any nested language coordinates
    *
    * @param componentNid the componentNid to get a regular name for.
    * @param stampCoordinate the stamp coordinate
    * @return the regular name text
    */
   default Optional<String> getRegularName(int componentNid, StampCoordinate stampCoordinate) {
      switch (Get.identifierService().getObjectTypeForComponent(componentNid)) {
         case CONCEPT:
            LatestVersion<DescriptionVersion> latestDescription
               = getPreferredDescription(Get.conceptService().getConceptDescriptions(componentNid), stampCoordinate);
            return latestDescription.isPresent() ? Optional.of(latestDescription.get().getText()) : Optional.empty();
         case SEMANTIC:
            return Optional.of(Get.assemblageService().getSemanticChronology(componentNid).getVersionType().toString());
         case UNKNOWN:
         default:
           return Optional.empty();
      }
   }
   
   /**
    * Gets the text of type {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}.  Will return empty, if 
    * no matching description type is found in this or any nested language coordinates
    *
    * @param componentNid the componentNid to get a regular name for.
    * @param stampCoordinate the stamp coordinate
    * @return the regular name text
    */
   default Optional<String> getFullyQualifiedName(int componentNid, StampCoordinate stampCoordinate) {
      switch (Get.identifierService().getObjectTypeForComponent(componentNid)) {
         case CONCEPT:
            LatestVersion<DescriptionVersion> latestDescription
               = getFullySpecifiedDescription(Get.conceptService().getConceptDescriptions(componentNid), stampCoordinate);
            return latestDescription.isPresent() ? Optional.of(latestDescription.get().getText()) : Optional.empty();
         case SEMANTIC:
            return Optional.of(Get.assemblageService().getSemanticChronology(componentNid).getVersionType().toString());
         case UNKNOWN:
         default:
           return Optional.empty();
      }
   }

   @Override
   public LanguageCoordinate deepClone();
}
