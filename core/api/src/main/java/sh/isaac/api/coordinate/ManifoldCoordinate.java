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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.util.UUIDUtil;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ManifoldCoordinate.
 *
 * @author kec
 */
public interface ManifoldCoordinate
        extends StampCoordinateProxy, LanguageCoordinateProxy, LogicCoordinateProxy {

    /**
     * 
     * @return a content based uuid, such that identical manifold coordinates
     * will have identical uuids, and that different manifold coordinates will 
     * always have different uuids.
     */
    default UUID getManifoldCoordinateUuid() {
       ArrayList<UUID> uuidList = new ArrayList<>();
       UUIDUtil.addSortedUuids(uuidList, getTaxonomyPremiseType().getPremiseTypeConcept().getNid());
       uuidList.add(getStampCoordinateUuid());
       uuidList.add(getLanguageCoordinateUuid());
       uuidList.add(getLogicCoordinateUuid());
       uuidList.add(getDestinationStampCoordinate().getStampCoordinateUuid());
       return UUID.nameUUIDFromBytes(uuidList.toString().getBytes());
   }
    
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
    * @return The destination stamp coordinate,
    * 
    * In most cases, this coordinate will be the same object that is returned by {@link #getStampCoordinate()}, 
    * But, it may be a different coordinate, depending on the construction - for example, a use case like returning inactive concepts
    * linked by active relationships.
    * 
    * This coordinate is used on the destination concepts in taxonomy operations, while {@link #getStampCoordinate()} is used 
    * on the relationships themselves.
    */
   StampCoordinate getDestinationStampCoordinate();

   /**
    * Gets the uuid.
    *
    * @return a UUID that uniquely identifies this manifold coordinate.
    */
   default UUID getCoordinateUuid() {
       return getManifoldCoordinateUuid();
   }
   
   
   /**
    * Return the best description text according to the type and dialect preferences of this {@code LanguageCoordinate}.
    *
    * @param conceptId the concept id. 
    * @return an optional String best matching the {@link LanguageCoordinate} constraints within this ManifoldCoordinate, or empty
    * if none available that match the {@link ManifoldCoordinate}.
    */
   default Optional<String> getDescription(int conceptId) {
      LatestVersion<DescriptionVersion> temp = getDescription(conceptId, this.getStampCoordinate());
      if (temp.isPresent()) {
         return Optional.of(temp.get().getText());
      }
      return Optional.empty();
   }
   
   /**
    * Return the best description text according to langauge and dialect preferences of this {@code LanguageCoordinate}, 
    * but ignoring the type preferences of the coordinate, rather, using the supplied type preference order
    *
    * @param conceptId the concept id. 
    * @param descriptionTypePreference the order of the description types to try to match, overriding and ignoring the 
    * {@link LanguageCoordinate#getDescriptionTypePreferenceList()} present in this manifold.
    * @return an optional String best matching the {@link LanguageCoordinate} constraints within this ManifoldCoordinate and 
    * the supplied description type preference list, or empty if none available that match the {@link ManifoldCoordinate}.
    */
   default Optional<String> getDescriptionText(int conceptId, int[] descriptionTypePreference) {
      LatestVersion<DescriptionVersion> temp = getLanguageCoordinate().getDescription(conceptId, descriptionTypePreference, this.getStampCoordinate());
      if (temp.isPresent()) {
         return Optional.of(temp.get().getText());
      }
      return Optional.empty();
   }
   
   /**
    * Return the best description text according to language and dialect preferences of this {@code LanguageCoordinate}, 
    * but ignoring the type preferences of the coordinate, rather, using the supplied type preference order
    *
    * @param conceptId the concept id. 
    * @param descriptionTypePreference the order of the description types to try to match, overriding and ignoring the 
    * {@link LanguageCoordinate#getDescriptionTypePreferenceList()} present in this manifold.
    * @return the best matching description to the {@link LanguageCoordinate} constraints within this ManifoldCoordinate and 
    * the supplied description type preference list, or empty if none available that match the {@link ManifoldCoordinate}.
    */
   default LatestVersion<DescriptionVersion> getDescription(int conceptId, int[] descriptionTypePreference) {
      return getLanguageCoordinate().getDescription(conceptId, descriptionTypePreference, getStampCoordinate());
   }
   
   /**
    * Return the description according to the type and dialect preferences of the {@code ManifoldCoordinate}'s {@code LanguageCoordinate}.
    * 
    * If none of the supplied descriptions matches the manifolds {@code LanguageCoordinate}, then empty is returned.
    * 
    *  {@link LanguageCoordinate#getDescription(List, StampCoordinate)}
    *
    * @param descriptionList descriptions to consider
    * @return an optional description best matching the {@code LanguageCoordinate} constraints
    */
   default LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList) {
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
    * DISCOURAGED METHOD!
    * Get the preferred description text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return preferred description text.
    * 
    * Note that this method gives no indication when the preferred text isn't available, instead silently 
    * falling back to a fully specified description, and if that is not present, returns a "no description for {conceptid}"
    * text string
    * 
    * One should really use the method {@link #getDescription(int, StampCoordinate)} instead of this method which will
    * properly use the language coordinate(s) to locate a description, or clearly return an empty.
    */
   default String getPreferredDescriptionText(int conceptId) {
      return getLanguageCoordinate().getRegularName(conceptId, getStampCoordinate())
            .orElse(getLanguageCoordinate().getFullyQualifiedName(conceptId, getStampCoordinate())
               .orElse("no description for " + conceptId));
   }
   
   /**
    * Get the regularName text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return regular name description text, or empty, if not present.
    */
   default Optional<String> getRegularName(int conceptId) {
      return getLanguageCoordinate().getRegularName(conceptId, 
              getStampCoordinate());
   }
   
   /**
    * Calls {@link #getPreferredDescriptionText(int)} with the nid of the specified concept spec.
    * @param conceptSpec  If not provided, this method simply returns "empty"
    * @return see {@link #getPreferredDescriptionText(int)}
    */
   default String getPreferredDescriptionText(ConceptSpecification conceptSpec) {
       if (conceptSpec == null) {
           return "empty";
       }
       return getPreferredDescriptionText(conceptSpec.getNid());
   }
   
   /**
    * Calls {@link #getPreferredDescriptionText(int)} with the nid of the specified concept spec.
    * @param conceptSpec  If not provided, this method simply returns "empty"
    * @param defaultText If an instance is misconfigured, it may be lacking some required concepts. 
    * Rather than throw an exception, the default text option allows a better fallback.
    * @return see {@link #getPreferredDescriptionText(int)}
    */
   
   default String getPreferredDescriptionText(ConceptSpecification conceptSpec, String defaultText) {
       if (conceptSpec == null) {
           return defaultText;
       }
       LatestVersion<DescriptionVersion> latestDescription = getPreferredDescription(conceptSpec);
       if (latestDescription.isPresent()) {
           return latestDescription.get().getText();
       }
       return defaultText;
   }
   
   /**
    * calls {@link #getRegularName(int)}
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return see {@link #getRegularName(int)}
    */
   default Optional<String> getRegularName(ConceptSpecification conceptSpec) {
      return getRegularName(conceptSpec.getNid());
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
    * Calls {@link #getFullySpecifiedDescription(int)}
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return see {@link #getFullySpecifiedDescription(int)}
    */
   default LatestVersion<DescriptionVersion> getFullySpecifiedDescription(ConceptSpecification conceptSpec) {
      return getFullySpecifiedDescription(conceptSpec.getNid());
   }

  /**
    * DISCOURAGED METHOD!
    * Get the fully-specified description text associated with the {@code conceptId}.
    * @param conceptId the conceptId to get the text for.
    * @return fully qualified description text.
    * 
    * Note that this method gives no indication when the fully specified text isn't available, instead silently 
    * falling back to a regular name description, and if that is not present, returns a "no description for {conceptid}"
    * text string
    * 
    * One should really use the method {@link #getDescription(int, StampCoordinate)} instead of this method which will
    * properly use the language coordinate(s) to locate a description, or clearly return an empty.
    */
   default String getFullySpecifiedDescriptionText(int conceptId) {
         return getLanguageCoordinate().getFullyQualifiedName(conceptId, getStampCoordinate())
            .orElse(getLanguageCoordinate().getRegularName(conceptId, getStampCoordinate())
               .orElse("no description for " + conceptId));
   }

  /**
    * calls {@link #getFullySpecifiedDescriptionText(int)}
    * @param conceptSpec the {@code ConceptSpecification} to get the text for.
    * @return see {@link #getFullySpecifiedDescriptionText(int)}
    */
   default String getFullySpecifiedDescriptionText(ConceptSpecification conceptSpec) {
      return getFullySpecifiedDescriptionText(conceptSpec.getNid());
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

   @Override
   default ConceptSpecification getLanguageConcept() {
       return this.getLanguageCoordinate().getLanguageConcept();
   }
   

    @Override
    default Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return getLanguageCoordinate().getNextProrityLanguageCoordinate();
    }

    @Override
    default LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
        return getLanguageCoordinate().getDefinitionDescription(descriptionList, stampCoordinate);
    }

    @Override
    default int[] getModulePreferenceListForLanguage() {
        return getLanguageCoordinate().getModulePreferenceListForLanguage();
    }

    @Override
    default ConceptSpecification[] getDialectAssemblageSpecPreferenceList() {
        return getLanguageCoordinate().getDialectAssemblageSpecPreferenceList();
    }

    @Override
    default ConceptSpecification[] getDescriptionTypeSpecPreferenceList() {
        return getLanguageCoordinate().getDescriptionTypeSpecPreferenceList();
    }

    @Override
    default ConceptSpecification[] getModuleSpecPreferenceListForLanguage() {
        return getLanguageCoordinate().getModuleSpecPreferenceListForLanguage();
    }

    @Override
    default List<ConceptSpecification> getModulePreferenceOrderForVersions() {
        return getStampCoordinate().getModulePreferenceOrderForVersions();
    }

    @Override
    default Set<ConceptSpecification> getModuleSpecifications() {
        return getStampCoordinate().getModuleSpecifications();
    }
    
    /**
     * @return true, if this ManifoldCoordinate has a custom sort for concepts, false otherwise.
     */
    default boolean hasCustomTaxonomySort() {
        return false;
    }
    
    /**
     * Some implementations need to compute unique keys for ManifoldCoordinates, and the sort may need to be a part 
     * of that computation.  This method provides access to the hashcode of the sort implementation.
     * @return the hashcode of the sort implementation
     */
    default int getCustomTaxonomySortHashCode() {
        return 0;
    }
    
    /**
     * if {@link #hasCustomTaxonomySort()} returns true, this method can be called to sort an array of concepts
     * by a custom sort which is part of the {@link ManifoldCoordinate}.  By default, this method throws an 
     * UnsupportedOperationException, or if {@link #hasCustomTaxonomySort()} returns false.
     * @param concepts the array of concept nids to be sorted.
     * @return the sorted set of concept nids.
     */
    default int[] sortConcepts(int[] concepts) {
        throw new UnsupportedOperationException();
    }
}
