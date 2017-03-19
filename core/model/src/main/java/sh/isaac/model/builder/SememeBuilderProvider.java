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
 * @author kec
 */
@Service
public class SememeBuilderProvider<C extends SememeChronology<? extends SememeVersion<?>>>
         implements SememeBuilderService<C> {
   @Override
   public SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.COMPONENT_NID,
                                   new Object[] { memeComponentNid });
   }

   @Override
   public SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.COMPONENT_NID,
                                   new Object[] { memeComponentNid });
   }

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

   @Override
   public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.DYNAMIC);
   }

   @Override
   public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.DYNAMIC);
   }

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

   @Override
   public SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.LOGIC_GRAPH,
                                   new Object[] { expression });
   }

   @Override
   public SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.LOGIC_GRAPH,
                                   new Object[] { expression });
   }

   @Override
   public SememeBuilder<C> getLongSememeBuilder(long longValue,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.LONG,
                                   new Object[] { longValue });
   }

   @Override
   public SememeBuilder<C> getLongSememeBuilder(long longValue,
         int referencedComponentNid,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.LONG,
                                   new Object[] { longValue });
   }

   @Override
   public SememeBuilder<C> getMembershipSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.MEMBER, new Object[] {});
   }

   @Override
   public SememeBuilder<C> getMembershipSememeBuilder(int referencedComponentNid, int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponentNid,
                                   assemblageConceptSequence,
                                   SememeType.MEMBER,
                                   new Object[] {});
   }

   @Override
   public SememeBuilder<C> getStringSememeBuilder(String memeString,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence) {
      return new SememeBuilderImpl(referencedComponent,
                                   assemblageConceptSequence,
                                   SememeType.STRING,
                                   new Object[] { memeString });
   }

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

