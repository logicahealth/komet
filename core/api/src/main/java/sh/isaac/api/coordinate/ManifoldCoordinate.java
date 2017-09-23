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
import java.util.UUID;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ManifoldCoordinate.
 *
 * @author kec
 */
public interface ManifoldCoordinate
        extends StampCoordinateProxy, LanguageCoordinateProxy, LogicCoordinateProxy {
   /**
    * Make analog.
    *
    * @param taxonomyType the {@code PremiseType} for the analog
    * @return a new manifoldCoordinate with the specified taxonomy type.
    */
   ManifoldCoordinate makeCoordinateAnalog(PremiseType taxonomyType);

   //~--- get methods ---------------------------------------------------------

   /**
    * Convenience method, buffers concept sequence in a cache-sensitive manner.
    * @return the concept sequence that defines the is-a relationship type.
    */
   int getIsaConceptSequence();

   /**
    * Gets the taxonomy type.
    *
    * @return PremiseType.STATED if taxonomy operations should be based on stated definitions, or
    * PremiseType.INFERRED if taxonomy operations should be based on inferred definitions.
    */
   PremiseType getTaxonomyType();

   /**
    * Gets the uuid.
    *
    * @return a UUID that uniquely identifies this manifold coordinate.
    */
   UUID getCoordinateUuid();
   
   /**
    * Return the description according to the type and dialect preferences
    * of the {@code ManifoldCoordinate}'s {@code LanguageCoordinate}.
    *
    * @param descriptionList descriptions to consider
    * @return an optional description best matching the {@code LanguageCoordinate}
    * constraints.
    */
   default LatestVersion<DescriptionVersion> getDescription(
           List<SememeChronology> descriptionList) {
      return getLanguageCoordinate().getDescription(descriptionList, getStampCoordinate());
   };
   
      
   /**
    * Get the preferred description associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description. 
    */
   default LatestVersion<DescriptionVersion> getPreferredDescription(int conceptId) {
      return getLanguageCoordinate().getPreferredDescription(conceptId, 
              getStampCoordinate());
   }
   
   default LatestVersion<DescriptionVersion> getPreferredDescription(List<SememeChronology> descriptionList) {
      return getLanguageCoordinate().getPreferredDescription(descriptionList, getStampCoordinate());
   }

   /**
    * Get the preferred description associated with the {@code conceptId}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description. 
    */
   default LatestVersion<DescriptionVersion> getPreferredDescription(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getPreferredDescription(conceptSpec.getConceptSequence(), 
              getStampCoordinate());
   }
   /**
    * Get the preferred description text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description text. 
    */
   default String getPreferredDescriptionText(int conceptId) {
      return getLanguageCoordinate().getPreferredDescriptionText(conceptId, 
              getStampCoordinate());
   }
   
   /**
    * Get the preferred description text associated with the {@code ConceptSpecification}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description text. 
    */
   default String getPreferredDescriptionText(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getPreferredDescriptionText(conceptSpec.getConceptSequence(), 
              getStampCoordinate());
   }
   
   /**
    * Get the fully-specified description associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description.
    */
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(int conceptId) {
      return getLanguageCoordinate().getFullySpecifiedDescription(conceptId, getStampCoordinate());
   }
   
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(List<SememeChronology> descriptionList) {
      return getLanguageCoordinate().getFullySpecifiedDescription(descriptionList, getStampCoordinate());
   }

   /**
    * Get the fully-specified description associated with the {@code conceptId}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description.
    */
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getFullySpecifiedDescription(conceptSpec.getConceptSequence(), getStampCoordinate());
   }

  /**
    * Get the fully-specified description text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description text.
    */
   default String getFullySpecifiedDescriptionText(int conceptId) {
      return getLanguageCoordinate().getFullySpecifiedDescriptionText(conceptId, getStampCoordinate());
   }


  /**
    * Get the fully-specified description text associated with the {@code conceptId}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description text.
    */
   default String getFullySpecifiedDescriptionText(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getFullySpecifiedDescriptionText(conceptSpec.getConceptSequence(), getStampCoordinate());
   }

   @Override
   public ManifoldCoordinate deepClone();
   
   

}

