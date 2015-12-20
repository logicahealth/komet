/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.builder;

import java.util.concurrent.atomic.AtomicReference;

import gov.vha.isaac.ochre.api.IdentifiedComponentBuilder;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.commit.CommittableComponent;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import org.jvnet.hk2.annotations.Service;

/**
 * @author kec
 */
@Service
public class SememeBuilderProvider<C extends SememeChronology<? extends SememeVersion<?>>> implements SememeBuilderService<C> {

    @Override
    public SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
                                                   IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
                                                   int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.COMPONENT_NID, new Object[]{memeComponentNid});
    }

    @Override
    public SememeBuilder<C> getComponentSememeBuilder(int memeComponentNid,
                                                   int referencedComponentNid,
                                                   int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.COMPONENT_NID, new Object[]{memeComponentNid});
    }

    @Override
    public SememeBuilder<C> getLongSememeBuilder(long longValue,
                                              IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent,
                                              int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.LONG, new Object[]{longValue});
    }

    @Override
    public SememeBuilder<C> getLongSememeBuilder(long longValue, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.LONG, new Object[]{longValue});
    }

    @Override
    public SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression, IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.LOGIC_GRAPH, new Object[]{expression});
    }

    @Override
    public SememeBuilder<C> getLogicalExpressionSememeBuilder(LogicalExpression expression, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.LOGIC_GRAPH, new Object[]{expression});
    }

    @Override
    public SememeBuilder<C> getMembershipSememeBuilder(IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.MEMBER, new Object[]{});
    }

    @Override
    public SememeBuilder<C> getMembershipSememeBuilder(int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.MEMBER, new Object[]{});
    }

    @Override
    public SememeBuilder<C> getStringSememeBuilder(String memeString, IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.STRING, new Object[]{memeString});
    }

    @Override
    public SememeBuilder<C> getStringSememeBuilder(String memeString, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.STRING, new Object[]{memeString});
    }

    @Override
    public SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(int caseSignificanceConceptSequence,
                                                                                                              int descriptionTypeConceptSequence,
                                                                                                              int languageConceptSequence,
                                                                                                              String text,
                                                                                                              IdentifiedComponentBuilder<? extends CommittableComponent> referencedComponent) {
        return new SememeBuilderImpl(referencedComponent, TermAux.getDescriptionAssemblageConceptSequence(languageConceptSequence),
                SememeType.DESCRIPTION, new Object[]{caseSignificanceConceptSequence,
                descriptionTypeConceptSequence, languageConceptSequence, text});
    }

    @Override
    public SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> getDescriptionSememeBuilder(
            int caseSignificanceConceptSequence,
            int languageConceptSequence,
            int descriptionTypeConceptSequence,
            String text,
            int referencedComponentNid) {
        return new SememeBuilderImpl(referencedComponentNid, TermAux.getDescriptionAssemblageConceptSequence(languageConceptSequence),
                SememeType.DESCRIPTION, new Object[]{caseSignificanceConceptSequence,
                descriptionTypeConceptSequence, languageConceptSequence, text});
    }

    @Override
    public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDyanmicSememeBuilder(int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.DYNAMIC);
    }

    @Override
    public SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> getDyanmicSememeBuilder(int referencedComponentNid, int assemblageConceptSequence, DynamicSememeDataBI[] data) {
        //Java makes a mess out of passing an array of data into a method that takes the array ... syntax.  If you pass one, it unwraps your array, and passes in the 
        //parts individually.  If you pass more than one, it doens't unwrap the parts.  In the first case, it also makes it impossible to cast back from Object[] to 
        //the array type we want... so just wrap it in something to stop java from being stupid. 
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.DYNAMIC, new AtomicReference<DynamicSememeDataBI[]>(data));
    }
}