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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * Coordinate to manage the retrieval and display of language and dialect information.
 *
 * Created by kec on 2/16/15.
 */
public interface LanguageCoordinate {
   /**
    * Return the latestDescription according to the type and dialect preferences
 of this {@code LanguageCoordinate}.
    *
    * @param descriptionList descriptions to consider
    * @param stampCoordinate the stamp coordinate
    * @return an optional latestDescription best matching the {@code LanguageCoordinate}
    * constraints.
    */
   LatestVersion<DescriptionVersion> getDescription(
           List<SememeChronology<DescriptionVersion>> descriptionList,
           StampCoordinate stampCoordinate);

   /**
    * Gets the latestDescription type preference list.
    *
    * @return the latestDescription type preference list
    */
   int[] getDescriptionTypePreferenceList();

   /**
    * Gets the dialect assemblage preference list.
    *
    * @return the dialect assemblage preference list
    */
   int[] getDialectAssemblagePreferenceList();

   /**
    * Convenience method - returns true if FSN is at the top of the latestDescription list.
    *
    * @return true, if FSN preferred
    */
   public default boolean isFSNPreferred() {
      for (final int descType: getDescriptionTypePreferenceList()) {
         if (descType ==
               Get.identifierService().getConceptSequenceForUuids(
                   TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getPrimordialUuid())) {
            return true;
         }

         break;
      }

      return false;
   }

   /**
    * Gets the fully specified latestDescription.
    *
    * @param descriptionList the latestDescription list
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified latestDescription
    */
   LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
           List<SememeChronology<DescriptionVersion>> descriptionList,
           StampCoordinate stampCoordinate);
   /**
    * Gets the fully specified latestDescription.
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
    * Gets the fully specified latestDescription text.
    *
    * @param conceptId the conceptId to get the fully specified latestDescription for
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified latestDescription
    */
   default String getFullySpecifiedDescriptionText(
           int conceptId,
           StampCoordinate stampCoordinate) {
      LatestVersion<DescriptionVersion> latestDescription = 
              getFullySpecifiedDescription(Get.conceptService().getConceptDescriptions(conceptId), stampCoordinate);
      if (latestDescription.value().isPresent()) {
         return latestDescription.value().get().getText();
      } else {
         return "No description for: " + conceptId;
      }
   }

   /**
    * Gets the language concept sequence.
    *
    * @return the language concept sequence
    */
   int getLanguageConceptSequence();

   /**
    * Gets the preferred latestDescription.
    *
    * @param descriptionList the latestDescription list
    * @param stampCoordinate the stamp coordinate
    * @return the preferred latestDescription
    */
   LatestVersion<DescriptionVersion> getPreferredDescription(
           List<SememeChronology<DescriptionVersion>> descriptionList,
           StampCoordinate stampCoordinate);
   
   /**
    * Gets the preferred description.
    *
    * @param conceptId the conceptId to get the fully specified latestDescription for
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified latestDescription
    */
   default LatestVersion<DescriptionVersion> getPreferredDescription(
           int conceptId,
           StampCoordinate stampCoordinate) {
      return getPreferredDescription(Get.conceptService().getConceptDescriptions(conceptId), stampCoordinate);
   }

   
   /**
    * Gets the preferred description text. 
    *
    * @param conceptId the conceptId to get the fully specified latestDescription for
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified latestDescription
    */
   default String getPreferredDescriptionText(
           int conceptId,
           StampCoordinate stampCoordinate) {
      LatestVersion<DescriptionVersion> latestDescription = 
              getPreferredDescription(Get.conceptService().getConceptDescriptions(conceptId), stampCoordinate);
      if (latestDescription.value().isPresent()) {
         return latestDescription.value().get().getText();
      } else {
         return "No description for: " + conceptId;
      }
   }
}

