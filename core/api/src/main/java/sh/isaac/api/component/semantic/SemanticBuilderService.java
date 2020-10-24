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
package sh.isaac.api.component.semantic;

//~--- non-JDK imports --------------------------------------------------------
import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

//~--- interfaces -------------------------------------------------------------
/**
 * The Interface SemanticBuilderService.
 *
 * @author kec
 * @param <C> the generic type
 */
@Contract
public interface SemanticBuilderService<C extends SemanticChronology> {

    /**
     * Gets the component semantic builder.
     *
     * @param componentNid the component nid the semantic references (not the nid of the semantic itself)
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the component sematic builder
     */
    SemanticBuilder<C> getComponentSemanticBuilder(int componentNid,
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the component semantic builder.
     *
     * @param componentNid the component nid the semantic references (not the nid of the semantic itself)
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the component semantic builder
     */
    SemanticBuilder<C> getComponentSemanticBuilder(int componentNid,
            int referencedComponentNid,
            int assemblageConceptNid);

    /**
     * Gets the component semantic builder.
     *
     * @param componentNid the component nid the semantic references (not the nid of the semantic itself)
     * @param intValue the integer value for the semantic
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the component sematic builder
     */
    SemanticBuilder<C> getComponentIntSemanticBuilder(int componentNid, int intValue,
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the component semantic builder.
     *
     * @param componentNid the component nid for the semantic
     * @param intValue the integer value for the semantic
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the component sematic builder
     */
    SemanticBuilder<C> getComponentIntSemanticBuilder(int componentNid, int intValue,
            int referencedComponent,
            int assemblageConceptNid);

    
    /**
     * Gets the description semantic builder.
     *
     * @param caseSignificanceConceptNid the case significance concept nid
     * @param descriptionTypeConceptNid the description type concept nid
     * @param languageConceptNid the language concept nid - also used as the
     * assemblage of the semantic
     * @param text the text
     * @param referencedComponent the referenced component
     * @return the description semantic builder
     */
    SemanticBuilder<? extends SemanticChronology> getDescriptionBuilder(
            int caseSignificanceConceptNid,
            int languageConceptNid,
            int descriptionTypeConceptNid,
            String text,
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent);

    /**
     * Gets the description semantic builder.
     *
     * @param caseSignificanceConceptNid the case significance concept nid
     * @param languageConceptNid the language concept nid
     * @param descriptionTypeConceptNid the description type concept nid - also
     * used as the assemblage of the semantic
     * @param text the text
     * @param referencedComponentNid the referenced component nid
     * @return the description semantic builder
     */
    SemanticBuilder<? extends SemanticChronology> getDescriptionBuilder(
            int caseSignificanceConceptNid,
            int languageConceptNid,
            int descriptionTypeConceptNid,
            String text,
            int referencedComponentNid);

    /**
     * Gets the dynamic semantic builder.
     *
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the dynamic semantic builder
     */
    SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the dynamic semantic builder.
     *
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the dynamic semantic builder
     */
    SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
            int referencedComponentNid,
            int assemblageConceptNid);

    /**
     * Gets the dynamic semantic builder.
     *
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @param data the data
     * @return the dynamic semantic builder
     */
    SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid,
            DynamicData[] data);

    /**
     * Gets the dynamic semantic builder.
     *
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @param data the data
     * @return the dynamic semantic builder
     */
    SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
            int referencedComponentNid,
            int assemblageConceptNid,
            DynamicData[] data);

    /**
     * Gets the logical expression semantic builder.
     *
     * @param expression the expression
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the logical expression semantic builder
     */
    SemanticBuilder<C> getLogicalExpressionBuilder(LogicalExpression expression,
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the logical expression semantic builder.
     *
     * @param expression the expression
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the logical expression semantic builder
     */
    SemanticBuilder<C> getLogicalExpressionBuilder(LogicalExpression expression,
            int referencedComponentNid,
            int assemblageConceptNid);

    /**
     * Gets the long semantic builder.
     *
     * @param longValue the long value
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the long semantic builder
     */
    SemanticBuilder<C> getLongSemanticBuilder(long longValue,
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the long semantic builder.
     *
     * @param componentNid the component nid for the semantic
     * @param longValue the long value for the semantic
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the long semantic builder
     */
    SemanticBuilder<C> getComponentLongSemanticBuilder(int componentNid, long longValue,
                                                       int referencedComponentNid, int assemblageConceptNid);
    SemanticBuilder<C> getComponentLongSemanticBuilder(int componentNid, long longValue,
                                                       IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
                                                       int assemblageConceptNid);
    /**
     * Gets the component long semantic builder.
     *
     * @param longValue the long value
     * @param longValue the long value
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the long semantic builder
     */
    SemanticBuilder<C> getLongSemanticBuilder(long longValue, int referencedComponentNid, int assemblageConceptNid);

    /**
     * Gets the membership semantic builder.
     *
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the membership semantic builder
     */
    SemanticBuilder<C> getMembershipSemanticBuilder(
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the membership semantic builder.
     *
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the membership semantic builder
     */
    SemanticBuilder<C> getMembershipSemanticBuilder(int referencedComponentNid, int assemblageConceptNid);

    /**
     * Gets the string semantic builder.
     *
     * @param semanticString the meme string
     * @param referencedComponent the referenced component
     * @param assemblageConceptNid the assemblage concept nid
     * @return the string semantic builder
     */
    SemanticBuilder<C> getStringSemanticBuilder(String semanticString,
            IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
            int assemblageConceptNid);

    /**
     * Gets the string semantic builder.
     *
     * @param semanticString the meme string
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptNid the assemblage concept nid
     * @return the string semantic builder
     */
    SemanticBuilder<C> getStringSemanticBuilder(String semanticString,
            int referencedComponentNid,
            int assemblageConceptNid);
}
