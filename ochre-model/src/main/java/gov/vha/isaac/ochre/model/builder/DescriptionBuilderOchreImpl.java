/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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


import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kec
 * @param <T>
 * @param <V>
 */
public class DescriptionBuilderOchreImpl<T extends SememeChronology<V>, V extends DescriptionSememeImpl> extends 
            ComponentBuilder<T> 
    implements DescriptionBuilder<T,V> {

    private final ArrayList<ConceptSpecification> preferredInDialectAssemblages = new ArrayList<>();
    private final ArrayList<ConceptSpecification> acceptableInDialectAssemblages = new ArrayList<>();
    
    private final String descriptionText;
    private final ConceptSpecification descriptionType;
    private final ConceptSpecification languageForDescription;
    private final ConceptBuilder conceptBuilder;
    private int conceptSequence = Integer.MAX_VALUE;

    public DescriptionBuilderOchreImpl(String descriptionText, 
            int conceptSequence,
            ConceptSpecification descriptionType, 
            ConceptSpecification languageForDescription) {
        this.descriptionText = descriptionText;
        this.conceptSequence = conceptSequence;
        this.descriptionType = descriptionType;
        this.languageForDescription = languageForDescription;
        this.conceptBuilder = null;
    }
    public DescriptionBuilderOchreImpl(String descriptionText, 
            ConceptBuilder conceptBuilder,
            ConceptSpecification descriptionType, 
            ConceptSpecification languageForDescription) {
        this.descriptionText = descriptionText;
        this.descriptionType = descriptionType;
        this.languageForDescription = languageForDescription;
        this.conceptBuilder = conceptBuilder;
    }

    @Override
    public DescriptionBuilder setPreferredInDialectAssemblage(ConceptSpecification dialectAssemblage) {
        preferredInDialectAssemblages.add(dialectAssemblage);
        return this; 
   }

    @Override
    public DescriptionBuilder setAcceptableInDialectAssemblage(ConceptSpecification dialectAssemblage) {
        acceptableInDialectAssemblages.add(dialectAssemblage);
        return this;
    }

    @Override
    public T build(EditCoordinate editCoordinate, ChangeCheckerMode changeCheckerMode,
            List builtObjects) throws IllegalStateException {
        if (conceptSequence == Integer.MAX_VALUE) {
            conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptBuilder.getUuids());
        }
        SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
        SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder
                = sememeBuilder.getDescriptionSememeBuilder(Get.languageCoordinateService().caseSignificanceToConceptSequence(false),
                        languageForDescription.getConceptSequence(),
                        descriptionType.getConceptSequence(),
                        descriptionText,
                        Get.identifierService().getConceptNid(conceptSequence));
        
        descBuilder.setPrimordialUuid(this.getPrimordialUuid());
        SememeChronologyImpl<DescriptionSememeImpl> newDescription = (SememeChronologyImpl<DescriptionSememeImpl>)
                descBuilder.build(editCoordinate, changeCheckerMode, builtObjects);
        SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);
        preferredInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    TermAux.PREFERRED.getNid(), newDescription.getNid(),
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(editCoordinate, changeCheckerMode, builtObjects);
        });
        acceptableInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    TermAux.ACCEPTABLE.getNid(), 
                    newDescription.getNid(),
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(editCoordinate, changeCheckerMode, builtObjects);
        });
        sememeBuilders.forEach((builder) -> builder.build(editCoordinate, changeCheckerMode, builtObjects));
        return (T) newDescription;
    }

    @Override
    public T build(int stampSequence, List builtObjects) throws IllegalStateException {
        if (conceptSequence == Integer.MAX_VALUE) {
            conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptBuilder.getUuids());
        }
        SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
        
        SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder
                = sememeBuilder.getDescriptionSememeBuilder(
                        TermAux.caseSignificanceToConceptSequence(false),
                        languageForDescription.getConceptSequence(),
                        descriptionType.getConceptSequence(),
                        descriptionText,
                        Get.identifierService().getConceptNid(conceptSequence));
        descBuilder.setPrimordialUuid(this.getPrimordialUuid());
        SememeChronologyImpl<DescriptionSememeImpl> newDescription = (SememeChronologyImpl<DescriptionSememeImpl>)
                descBuilder.build(stampSequence, builtObjects);
        SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);
        preferredInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    TermAux.PREFERRED.getNid(), this,
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(stampSequence, builtObjects);
        });
        acceptableInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    TermAux.ACCEPTABLE.getNid(), this,
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(stampSequence, builtObjects);
        });
        sememeBuilders.forEach((builder) -> builder.build(stampSequence, builtObjects));
        return (T) newDescription;
    }
    
}
