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
 *
 * @author kec
 * @param <C>
 */
@Contract
public interface SememeBuilderService<C extends SememeChronology<? extends SememeVersion<?>>> {
   SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
         int referencedComponentNid,
         int assemblageConceptSequence);

   SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
           int caseSignificanceConceptSequence,
           int descriptionTypeConceptSequence,
           int languageConceptSequence,
           String text,
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent);

   SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
           int caseSignificanceConceptSequence,
           int languageConceptSequence,
           int descriptionTypeConceptSequence,
           String text,
           int referencedComponentNid);

   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence);

   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence);

   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence,
           DynamicSememeData[] data);

   SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDynamicSememeBuilder(
           int referencedComponentNid,
           int assemblageConceptSequence,
           DynamicSememeData[] data);

   SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression,
         int referencedComponentNid,
         int assemblageConceptSequence);

   SememeBuilder<C> getLongSememeBuilder(long longValue,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   SememeBuilder<C> getLongSememeBuilder(long longValue, int referencedComponentNid, int assemblageConceptSequence);

   SememeBuilder<C> getMembershipSememeBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptSequence);

   SememeBuilder<C> getMembershipSememeBuilder(int referencedComponentNid, int assemblageConceptSequence);

   SememeBuilder<C> getStringSememeBuilder(String memeString,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptSequence);

   SememeBuilder<C> getStringSememeBuilder(String memeString,
         int referencedComponentNid,
         int assemblageConceptSequence);
}

