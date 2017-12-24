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
    * @param memeComponentNid the meme component nid
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the component sematic builder
    */
   SemanticBuilder<C> getComponentSemanticBuilder(int memeComponentNid,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the component sememe builder.
    *
    * @param semanticNid the meme component nid
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the component sememe builder
    */
   SemanticBuilder<C> getComponentSemanticBuilder(int semanticNid,
         int referencedComponentNid,
         int assemblageConceptSequence);

   /**
    * Gets the description semantic builder.
    *
    * @param caseSignificanceConceptSequence the case significance concept sequence
    * @param descriptionTypeConceptSequence the description type concept sequence
    * @param languageConceptSequence the language concept sequence
    * @param text the text
    * @param referencedComponent the referenced component
    * @return the description semantic builder
    */
   SemanticBuilder<? extends SemanticChronology> getDescriptionBuilder(
           int caseSignificanceConceptSequence,
           int descriptionTypeConceptSequence,
           int languageConceptSequence,
           String text,
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent);

   /**
    * Gets the description semantic builder.
    *
    * @param caseSignificanceConceptSequence the case significance concept sequence
    * @param languageConceptSequence the language concept sequence
    * @param descriptionTypeConceptSequence the description type concept sequence
    * @param text the text
    * @param referencedComponentNid the referenced component nid
    * @return the description semantic builder
    */
   SemanticBuilder<? extends SemanticChronology> getDescriptionBuilder(
           int caseSignificanceConceptSequence,
           int languageConceptSequence,
           int descriptionTypeConceptSequence,
           String text,
           int referencedComponentNid);

   /**
    * Gets the dynamic semantic builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the dynamic semantic builder
    */
   SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence);

   /**
    * Gets the dynamic semantic builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the dynamic semantic builder
    */
   SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence);

   /**
    * Gets the dynamic semantic builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param data the data
    * @return the dynamic semantic builder
    */
   SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence,
           DynamicData[] data);

   /**
    * Gets the dynamic semantic builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param data the data
    * @return the dynamic semantic builder
    */
   SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence,
           DynamicData[] data);

   /**
    * Gets the logical expression semantic builder.
    *
    * @param expression the expression
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the logical expression semantic builder
    */
   SemanticBuilder<C> getLogicalExpressionBuilder(LogicalExpression expression,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the logical expression semantic builder.
    *
    * @param expression the expression
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the logical expression semantic builder
    */
   SemanticBuilder<C> getLogicalExpressionBuilder(LogicalExpression expression,
         int referencedComponentNid,
         int assemblageConceptSequence);

   /**
    * Gets the long semantic builder.
    *
    * @param longValue the long value
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the long semantic builder
    */
   SemanticBuilder<C> getLongSemanticBuilder(long longValue,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the long semantic builder.
    *
    * @param longValue the long value
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the long semantic builder
    */
   SemanticBuilder<C> getLongSemanticBuilder(long longValue, int referencedComponentNid, int assemblageConceptSequence);

   /**
    * Gets the membership semantic builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept sequence
    * @return the membership semantic builder
    */
   SemanticBuilder<C> getMembershipSemanticBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptNid);

   /**
    * Gets the membership semantic builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the membership semantic builder
    */
   SemanticBuilder<C> getMembershipSemanticBuilder(int referencedComponentNid, int assemblageConceptSequence);

   /**
    * Gets the string semantic builder.
    *
    * @param semanticString the meme string
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the string semantic builder
    */
   SemanticBuilder<C> getStringSemanticBuilder(String semanticString,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the string semantic builder.
    *
    * @param semanticString the meme string
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the string semantic builder
    */
   SemanticBuilder<C> getStringSemanticBuilder(String semanticString,
         int referencedComponentNid,
         int assemblageConceptSequence);
}

