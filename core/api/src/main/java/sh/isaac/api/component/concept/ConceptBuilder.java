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



package sh.isaac.api.component.concept;

import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ConceptBuilder.
 *
 * @author kec
 */
public interface ConceptBuilder
        extends IdentifiedComponentBuilder<ConceptChronology>, ConceptSpecification {
   /**
    * Used to add another arbitrary description type to the concept.
    *
    * @param descriptionBuilder the description builder
    * @return the concept builder
    */
   ConceptBuilder addDescription(DescriptionBuilder<?, ?> descriptionBuilder);

   /**
    * Used to add another acceptable arbitrary description type to the concept, using the default language
    * and dialect information passed into the concept builder upon construction.
    * This does not add a preferred description
    *
    * @param value - the value of the description
    * @param descriptionType - One of {@link TermAux#SYNONYM_DESCRIPTION_TYPE}, {@link TermAux#DEFINITION_DESCRIPTION_TYPE}
    * or {@link TermAux#FULLY_QUALIFIED_DESCRIPTION_TYPE}
    * @return the concept builder
    */
   ConceptBuilder addDescription(String value, ConceptSpecification descriptionType);

   /**
    * Use when adding a secondary definition in a different description logic
    * profile.
    *
    * @param logicalExpression the logical expression
    * @return a ConceptBuilder for use in method chaining/fluent API.
    */
   ConceptBuilder addLogicalExpression(LogicalExpression logicalExpression);

   /**
    * Use when adding a secondary definition in a different description logic
    * profile.
    *
    * @param logicalExpressionBuilder the logical expression builder
    * @return a ConceptBuilder for use in method chaining/fluent API.
    */
   ConceptBuilder addLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder);
   
   /**
    * Sets the logical expression. This method erases any previous logical expressions. 
    *
    * @param logicalExpression the logical expression
    * @return the concept builder
    */
   ConceptBuilder setLogicalExpression(LogicalExpression logicalExpression);

   /**
    * Sets the logical expression builder. This method erases previous logical expression builders. 
    *
    * @param logicalExpressionBuilder the logical expression builder
    * @return the concept builder
    */
   ConceptBuilder setLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder);

   /**
    * Sets the primordial UUID from the given spect, adds any additional UUIDs from the given spec, and
    * adds the description from the spec as an alternate synonym (if it differs from the current preferred term).
    *
    * @param conceptSpec the concept spec
    * @return the concept builder
    */
   ConceptBuilder mergeFromSpec(ConceptSpecification conceptSpec);

   //~--- get methods ---------------------------------------------------------

   /**
    * This may return null, if no description creation information was passed into the Concept Builder.
    *
    * @return the fully specified description builder
    */
   DescriptionBuilder<?, ?> getFullySpecifiedDescriptionBuilder();

   /**
    * This may return null, if no description creation information was passed into the Concept Builder,
    * or if no semantic tag was passed in.
    *
    * @return the synonym preferred description builder
    */
   DescriptionBuilder<?, ?> getPreferredDescriptionBuilder();
   
    /**
     * Gets the stored description builders.  This should include the FullySpecified Description Builder if set, and the SynonymPreferredDescriptionBuilder, if set
     * @return the description builders
     */
    List<DescriptionBuilder<?, ?>> getDescriptionBuilders();
    
    /**
     * Useful for setting up a semantic type concept with the semantic fields, 
     * or an assemblage concept with the field concepts. 
     * @param componentUuid
     * @param fieldIndex
     * @param assemblageUuid
     * @return the ConceptBuilder for a fluent interface. 
     */
    ConceptBuilder addComponentIntSemantic(UUID componentUuid, int fieldIndex, UUID assemblageUuid);
    
    default ConceptBuilder addComponentIntSemantic(ConceptSpecification componentUuid, int fieldIndex, ConceptSpecification assemblage) {
        return addComponentIntSemantic(componentUuid.getPrimordialUuid(), fieldIndex, assemblage.getPrimordialUuid());
    }
    
    ConceptBuilder addComponentSemantic(UUID componentUuid, UUID assemblageUuid);
    
    default ConceptBuilder addComponentSemantic(ConceptSpecification semanticSpecification, ConceptSpecification assemblage) {
        return addComponentSemantic(semanticSpecification.getPrimordialUuid(), assemblage.getPrimordialUuid());
    }
    
    ConceptBuilder addFieldSemanticConcept(String fieldName, int fieldIndex);

    default ConceptBuilder addFieldSemanticConcept(ConceptSpecification fieldSpecification, int fieldIndex) {
        return addFieldSemanticConcept(fieldSpecification.getPrimordialUuid(), fieldIndex);
    }

    ConceptBuilder addFieldSemanticConcept(UUID conceptUuid, int fieldIndex);
}

