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



package sh.isaac.model.builder;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.atomic.AtomicReference;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.logic.LogicalExpression;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SememeBuilderProvider.
 *
 * @author kec
 * @param <C> the generic type
 */
@Service
public class SememeBuilderProvider<C extends SememeChronology<? extends SememeVersion<?>>>
         implements SememeBuilderService<C> {
   
   /**
    * Gets the component sememe builder.
    *
    * @param memeComponentNid the meme component nid
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the component sememe builder
    */
   @Override
   public SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.COMPONENT_NID,
                                   new Object[] { memeComponentNid });
   }

   /**
    * Gets the component sememe builder.
    *
    * @param memeComponentNid the meme component nid
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the component sememe builder
    */
   @Override
   public SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.COMPONENT_NID,
                                   new Object[] { memeComponentNid });
   }

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
   @Override
   public SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
           int caseSignificanceConceptSequence,
           int descriptionTypeConceptSequence,
           int languageConceptSequence,
           String text,
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent) {
      return new SememeBuilderImpl(referencedComponent,
                                   TermAux.getDescriptionAssemblageConceptSequence(languageConceptSequence),
                                   SememeType.DESCRIPTION,
                                   new Object[] { caseSignificanceConceptSequence, descriptionTypeConceptSequence,
                                         languageConceptSequence, text });
   }

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
   @Override
   public SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
           int caseSignificanceConceptSequence,
           int languageConceptSequence,
           int descriptionTypeConceptSequence,
           String text,
           int referencedComponentNid) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   TermAux.getDescriptionAssemblageConceptSequence(languageConceptSequence),
                                   SememeType.DESCRIPTION,
                                   new Object[] { caseSignificanceConceptSequence, descriptionTypeConceptSequence,
                                         languageConceptSequence, text });
   }

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the dynamic sememe builder
    */
   @Override
   public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.DYNAMIC);
   }

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the dynamic sememe builder
    */
   @Override
   public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.DYNAMIC);
   }

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param data the data
    * @return the dynamic sememe builder
    */
   @Override
   public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence,
           DynamicSememeData[] data) {
      // Java makes a mess out of passing an array of data into a method that takes the array ... syntax.  If you pass one, it unwraps your array, and passes in the
      // parts individually.  If you pass more than one, it doens't unwrap the parts.  In the first case, it also makes it impossible to cast back from Object[] to
      // the array type we want... so just wrap it in something to stop java from being stupid.
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.DYNAMIC,
                                   new AtomicReference<DynamicSememeData[]>(data));
   }

   /**
    * Gets the dynamic sememe builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param data the data
    * @return the dynamic sememe builder
    */
   @Override
   public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence,
           DynamicSememeData[] data) {
      // Java makes a mess out of passing an array of data into a method that takes the array ... syntax.  If you pass one, it unwraps your array, and passes in the
      // parts individually.  If you pass more than one, it doens't unwrap the parts.  In the first case, it also makes it impossible to cast back from Object[] to
      // the array type we want... so just wrap it in something to stop java from being stupid.
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.DYNAMIC,
                                   new AtomicReference<DynamicSememeData[]>(data));
   }

   /**
    * Gets the logical expression sememe builder.
    *
    * @param expression the expression
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the logical expression sememe builder
    */
   @Override
   public SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.LOGIC_GRAPH,
                                   new Object[] { expression });
   }

   /**
    * Gets the logical expression sememe builder.
    *
    * @param expression the expression
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the logical expression sememe builder
    */
   @Override
   public SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.LOGIC_GRAPH,
                                   new Object[] { expression });
   }

   /**
    * Gets the long sememe builder.
    *
    * @param longValue the long value
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the long sememe builder
    */
   @Override
   public SememeBuilder<C> getLongSememeBuilder(long longValue,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.LONG,
                                   new Object[] { longValue });
   }

   /**
    * Gets the long sememe builder.
    *
    * @param longValue the long value
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the long sememe builder
    */
   @Override
   public SememeBuilder<C> getLongSememeBuilder(long longValue,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.LONG,
                                   new Object[] { longValue });
   }

   /**
    * Gets the membership sememe builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the membership sememe builder
    */
   @Override
   public SememeBuilder<C> getMembershipSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.MEMBER, new Object[] {});
   }

   /**
    * Gets the membership sememe builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the membership sememe builder
    */
   @Override
   public SememeBuilder<C> getMembershipSememeBuilder(int referencedComponentNid, int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.MEMBER,
                                   new Object[] {});
   }

   /**
    * Gets the string sememe builder.
    *
    * @param memeString the meme string
    * @param referencedComponent the referenced component
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the string sememe builder
    */
   @Override
   public SememeBuilder<C> getStringSememeBuilder(String memeString,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.STRING,
                                   new Object[] { memeString });
   }

   /**
    * Gets the string sememe builder.
    *
    * @param memeString the meme string
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the string sememe builder
    */
   @Override
   public SememeBuilder<C> getStringSememeBuilder(String memeString,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.STRING,
                                   new Object[] { memeString });
   }
}

