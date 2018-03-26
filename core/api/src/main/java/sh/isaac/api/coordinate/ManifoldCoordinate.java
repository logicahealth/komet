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
import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicalExpression;

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
    * Gets the taxonomy type.
    *
    * @return PremiseType.STATED if taxonomy operations should be based on stated definitions, or
    * PremiseType.INFERRED if taxonomy operations should be based on inferred definitions.
    */
   PremiseType getTaxonomyPremiseType();

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
           List<SemanticChronology> descriptionList) {
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
   
   default LatestVersion<DescriptionVersion> getPreferredDescription(List<SemanticChronology> descriptionList) {
      return getLanguageCoordinate().getPreferredDescription(descriptionList, getStampCoordinate());
   }

   /**
    * Get the preferred description associated with the {@code conceptId}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description. 
    */
   default LatestVersion<DescriptionVersion> getPreferredDescription(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getPreferredDescription(conceptSpec.getNid(), 
              getStampCoordinate());
   }
   /**
    * Get the preferred description text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description text.
    * 
    *  Note that this method gives no indication when the preferred text isn't available, instead returning 
    *  and "unknown..." type of string.  See {@link #getRegularName(int)} for a method without this behavior.
    */
   default String getPreferredDescriptionText(int conceptId) {
      return getLanguageCoordinate().getPreferredDescriptionText(conceptId, 
              getStampCoordinate());
   }
   
   /**
    * Get the regularName text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description text. 
    */
   default Optional<String> getRegularName(int conceptId) {
      return getLanguageCoordinate().getRegularName(conceptId, 
              getStampCoordinate());
   }
   
   /**
    * Get the preferred description text associated with the {@code ConceptSpecification}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description text. 
    * 
    * Note that this method does not give any indication of text being unavailable, rather, 
    * returning an arbitrary "unknown" string when there is no text avaiable on the coordinate.
    * 
    * See {@link #getRegularName(ConceptSpecification)} for a method without this behavior.
    */
   default String getPreferredDescriptionText(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getPreferredDescriptionText(conceptSpec.getNid(), 
              getStampCoordinate());
   }
   
   /**
    * Get the regular name (Preferred description ) text associated with the {@code ConceptSpecification}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description text. 
    */
   default Optional<String> getRegularName(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getRegularName(conceptSpec.getNid(), 
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
   
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(List<SemanticChronology> descriptionList) {
      return getLanguageCoordinate().getFullySpecifiedDescription(descriptionList, getStampCoordinate());
   }

   /**
    * Get the fully-specified description associated with the {@code conceptId}.
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return preferred description.
    */
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(ConceptSpecification conceptSpec) {
      return getLanguageCoordinate().getFullySpecifiedDescription(conceptSpec.getNid(), getStampCoordinate());
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
      return getLanguageCoordinate().getFullySpecifiedDescriptionText(conceptSpec.getNid(), getStampCoordinate());
   }

   @Override
   public ManifoldCoordinate deepClone();

   @Override
   default int getConceptAssemblageNid() {
      return getLogicCoordinate().getConceptAssemblageNid();
   }
   
   default Optional<LogicalExpression> getStatedLogicalExpression(ConceptSpecification spec) {
       return getStatedLogicalExpression(spec.getNid());
   }

   default Optional<LogicalExpression> getStatedLogicalExpression(int conceptNid) {
       return getLogicalExpression(conceptNid, PremiseType.STATED);
   }
   default Optional<LogicalExpression> getInferredLogicalExpression(ConceptSpecification spec) {
       return getInferredLogicalExpression(spec.getNid());
   }

   default Optional<LogicalExpression> getInferredLogicalExpression(int conceptNid) {
       return getLogicalExpression(conceptNid, PremiseType.INFERRED);
   }
   
   default Optional<LogicalExpression> getLogicalExpression(int conceptNid, PremiseType premiseType) {
       ConceptChronology concept = Get.concept(conceptNid);
       LatestVersion<LogicGraphVersion> logicalDef = concept.getLogicalDefinition(this, premiseType, this);
       if (logicalDef.isPresent()) {
           return Optional.of(logicalDef.get().getLogicalExpression());
       }
       return Optional.empty();
   }
   default LatestVersion<LogicGraphVersion> getStatedLogicGraphVersion(int conceptNid) {
       return getLogicGraphVersion(conceptNid, PremiseType.STATED);
   }

   default LatestVersion<LogicGraphVersion> getInferredLogicGraphVersion(ConceptSpecification conceptSpecification) {
       return getLogicGraphVersion(conceptSpecification.getNid(), PremiseType.INFERRED);
   }

   default LatestVersion<LogicGraphVersion> getStatedLogicGraphVersion(ConceptSpecification conceptSpecification) {
       return getLogicGraphVersion(conceptSpecification.getNid(), PremiseType.STATED);
   }

   default LatestVersion<LogicGraphVersion> getInferredLogicGraphVersion(int conceptNid) {
       return getLogicGraphVersion(conceptNid, PremiseType.INFERRED);
   }

   default LatestVersion<LogicGraphVersion> getLogicGraphVersion(int conceptNid, PremiseType premiseType) {
       ConceptChronology concept = Get.concept(conceptNid);
       return concept.getLogicalDefinition(this, premiseType, this);
   }
}

