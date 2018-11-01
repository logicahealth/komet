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
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SemanticBuilderProvider.
 *
 * @author kec
 * @param <C> the generic type
 */
@Service
public class SemanticBuilderProvider<C extends SemanticChronology>
         implements SemanticBuilderService<C> {
   /**
    * Gets the component semantic builder.
    *
    * @param componentNid the component nid
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @return the component semantic builder
    */
   @Override
   public SemanticBuilder<C> getComponentSemanticBuilder(int componentNid,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.COMPONENT_NID,
                                   new Object[] { componentNid });
   }
   /**
    * Gets the component semantic builder.
    *
    * @param componentNid the component nid
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the component semantic builder
    */
   @Override
   public SemanticBuilder<C> getComponentSemanticBuilder(int componentNid,
         int referencedComponentNid,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponentNid,
                                   assemblageConceptNid,
                                   VersionType.COMPONENT_NID,
                                   new Object[] { componentNid });
   }

    @Override
    public SemanticBuilder<C> getComponentIntSemanticBuilder(int componentNid, int intValue, IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent, int assemblageConceptNid) {
       return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.Nid1_Int2,
                                   new Object[] { componentNid, intValue });
   }

    @Override
    public SemanticBuilder<C> getComponentIntSemanticBuilder(int componentNid, int intValue, int referencedComponent, int assemblageConceptNid) {
       return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.Nid1_Int2,
                                   new Object[] { componentNid, intValue });
    }


   /**
    * {@inheritDoc}
    */
   @Override
   public SemanticBuilder<? extends SemanticChronology> getDescriptionBuilder(
           int caseSignificanceConceptSequence,
           int languageConceptNid,
           int descriptionTypeConceptSequence,
           String text,
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent) {
      return new SemanticBuilderImpl(referencedComponent,
                                   languageConceptNid,
                                   VersionType.DESCRIPTION,
                                   new Object[] { caseSignificanceConceptSequence, descriptionTypeConceptSequence,
                                         languageConceptNid, text });
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SemanticBuilder<? extends SemanticChronology> getDescriptionBuilder(
           int caseSignificanceConceptSequence,
           int languageConceptNid,
           int descriptionTypeConceptSequence,
           String text,
           int referencedComponentNid) {
      return new SemanticBuilderImpl(referencedComponentNid,
                                   languageConceptNid,
                                   VersionType.DESCRIPTION,
                                   new Object[] { caseSignificanceConceptSequence, descriptionTypeConceptSequence,
                                         languageConceptNid, text });
   }

   /**
    * Gets the dynamic builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @return the dynamic builder
    */
   @Override
   public SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponent, assemblageConceptNid, VersionType.DYNAMIC);
   }

   /**
    * Gets the dynamic builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the dynamic builder
    */
   @Override
   public SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           int referencedComponentNid,
           int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponentNid, assemblageConceptNid, VersionType.DYNAMIC);
   }

   /**
    * Gets the dynamic builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @param data the data
    * @return the dynamic builder
    */
   @Override
   public SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptNid,
           DynamicData[] data) {
      // Java makes a mess out of passing an array of data into a method that takes the array ... syntax.  If you pass one, it unwraps your array, and passes in the
      // parts individually.  If you pass more than one, it doens't unwrap the parts.  In the first case, it also makes it impossible to cast back from Object[] to
      // the array type we want... so just wrap it in something to stop java from being stupid.
      return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.DYNAMIC,
                                   new AtomicReference<>(data));
   }

   /**
    * Gets the dynamic builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @param data the data
    * @return the dynamic builder
    */
   @Override
   public SemanticBuilder<? extends SemanticChronology> getDynamicBuilder(
           int referencedComponentNid,
           int assemblageConceptNid,
           DynamicData[] data) {
      // Java makes a mess out of passing an array of data into a method that takes the array ... syntax.  If you pass one, it unwraps your array, and passes in the
      // parts individually.  If you pass more than one, it doens't unwrap the parts.  In the first case, it also makes it impossible to cast back from Object[] to
      // the array type we want... so just wrap it in something to stop java from being stupid.
      return new SemanticBuilderImpl(referencedComponentNid,
                                   assemblageConceptNid,
                                   VersionType.DYNAMIC,
                                   new AtomicReference<>(data));
   }

   /**
    * Gets the logical expression semantic builder.
    *
    * @param expression the expression
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @return the logical expression semantic builder
    */
   @Override
   public SemanticBuilder<C> getLogicalExpressionBuilder(LogicalExpression expression,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.LOGIC_GRAPH,
                                   new Object[] { expression });
   }

   /**
    * Gets the logical expression semantic builder.
    *
    * @param expression the expression
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the logical expression semantic builder
    */
   @Override
   public SemanticBuilder<C> getLogicalExpressionBuilder(LogicalExpression expression,
         int referencedComponentNid,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponentNid,
                                   assemblageConceptNid,
                                   VersionType.LOGIC_GRAPH,
                                   new Object[] { expression });
   }

   /**
    * Gets the long semantic builder.
    *
    * @param longValue the long value
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @return the long semantic builder
    */
   @Override
   public SemanticBuilder<C> getLongSemanticBuilder(long longValue,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.LONG,
                                   new Object[] { longValue });
   }

   /**
    * Gets the long semantic builder.
    *
    * @param longValue the long value
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the long semantic builder
    */
   @Override
   public SemanticBuilder<C> getLongSemanticBuilder(long longValue,
         int referencedComponentNid,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponentNid,
                                   assemblageConceptNid,
                                   VersionType.LONG,
                                   new Object[] { longValue });
   }

   /**
    * Gets the membership semantic builder.
    *
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @return the membership semantic builder
    */
   @Override
   public SemanticBuilder<C> getMembershipSemanticBuilder(
           IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
           int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponent, assemblageConceptNid, VersionType.MEMBER, new Object[] {});
   }

   /**
    * Gets the membership semantic builder.
    *
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the membership semantic builder
    */
   @Override
   public SemanticBuilder<C> getMembershipSemanticBuilder(int referencedComponentNid, int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponentNid,
                                   assemblageConceptNid,
                                   VersionType.MEMBER,
                                   new Object[] {});
   }

   /**
    * Gets the string semantic builder.
    *
    * @param memeString the meme string
    * @param referencedComponent the referenced component
    * @param assemblageConceptNid the assemblage concept nid
    * @return the string semantic builder
    */
   @Override
   public SemanticBuilder<C> getStringSemanticBuilder(String memeString,
         IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponent,
                                   assemblageConceptNid,
                                   VersionType.STRING,
                                   new Object[] { memeString });
   }

   /**
    * Gets the string semantic builder.
    *
    * @param memeString the meme string
    * @param referencedComponentNid the referenced component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the string semantic builder
    */
   @Override
   public SemanticBuilder<C> getStringSemanticBuilder(String memeString,
         int referencedComponentNid,
         int assemblageConceptNid) {
      return new SemanticBuilderImpl(referencedComponentNid,
                                   assemblageConceptNid,
                                   VersionType.STRING,
                                   new Object[] { memeString });
   }
}

