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



package sh.isaac.api.component.sememe;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.logic.LogicalExpression;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface SememeBuilderService.
 *
 * @author kec
 * @param <C> the generic type
 */
@Contract
public interface SememeBuilderService<C extends SememeChronology<? extends SememeVersion<?>>> {
   
   /**
    * Gets the component sememe builder.
    *
    * @param memeComponentNid the meme component nid
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the component sememe builder
    */
   SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the component sememe builder.
    *
    * @param memeComponentNid the meme component nid
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the component sememe builder
    */
   SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         int referencedComponentNid,
         int assemblageConceptSequence);

   /**
    * Gets the description sememe builder.
    *
    * @param caseSignificanceConceptSequence the case significance concept sequence
    * @param descriptionTypeConceptSequence the description type concept sequence
    * @param languageConceptSequence the language concept sequence
    * @param text the text
    * @param referencedComponent the referenced component
    * @return the description sememe builder
    */
   SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
           int caseSignificanceConceptSequence,
           int descriptionTypeConceptSequence,
           int languageConceptSequence,
           String text,
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent);

   /**
    * Gets the description sememe builder.
    *
    * @param caseSignificanceConceptSequence the case significance concept sequence
    * @param languageConceptSequence the language concept sequence
    * @param descriptionTypeConceptSequence the description type concept sequence
    * @param text the text
    * @param referencedComponentNid the referenced component nid
    * @return the description sememe builder
    */
   SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
           int caseSignificanceConceptSequence,
           int languageConceptSequence,
           int descriptionTypeConceptSequence,
           String text,
           int referencedComponentNid);

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the dynamic sememe builder
    */
   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence);

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the dynamic sememe builder
    */
   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence);

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param data the data
    * @return the dynamic sememe builder
    */
   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence,
           DynamicSememeData[] data);

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param data the data
    * @return the dynamic sememe builder
    */
   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence,
           DynamicSememeData[] data);

   /**
    * Gets the logical expression sememe builder.
    *
    * @param expression the expression
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the logical expression sememe builder
    */
   SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the logical expression sememe builder.
    *
    * @param expression the expression
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the logical expression sememe builder
    */
   SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         int referencedComponentNid,
         int assemblageConceptSequence);

   /**
    * Gets the long sememe builder.
    *
    * @param longValue the long value
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the long sememe builder
    */
   SememeBuilder<C> getLongSememeBuilder(long longValue,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the long sememe builder.
    *
    * @param longValue the long value
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the long sememe builder
    */
   SememeBuilder<C> getLongSememeBuilder(long longValue, int referencedComponentNid, int assemblageConceptSequence);

   /**
    * Gets the membership sememe builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the membership sememe builder
    */
   SememeBuilder<C> getMembershipSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence);

   /**
    * Gets the membership sememe builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the membership sememe builder
    */
   SememeBuilder<C> getMembershipSememeBuilder(int referencedComponentNid, int assemblageConceptSequence);

   /**
    * Gets the string sememe builder.
    *
    * @param memeString the meme string
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the string sememe builder
    */
   SememeBuilder<C> getStringSememeBuilder(String memeString,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   /**
    * Gets the string sememe builder.
    *
    * @param memeString the meme string
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the string sememe builder
    */
   SememeBuilder<C> getStringSememeBuilder(String memeString,
         int referencedComponentNid,
         int assemblageConceptSequence);
}

