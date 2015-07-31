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
package gov.vha.isaac.ochre.model.concept;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.logic.IsomorphicResults;
import gov.vha.isaac.ochre.api.logic.LogicService;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.relationship.RelationshipVersionAdaptor;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronologyImpl;
import gov.vha.isaac.ochre.model.relationship.RelationshipAdaptorChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author kec
 */
public class ConceptChronologyImpl
        extends ObjectChronologyImpl<ConceptVersionImpl>
        implements ConceptChronology<ConceptVersionImpl> {

    private static LogicService logicService;

    private static LogicService getLogicService() {
        if (logicService == null) {
            logicService = LookupService.getService(LogicService.class);
        }
        return logicService;
    }

    public ConceptChronologyImpl(UUID primoridalUuid, int nid, int containerSequence) {
        super(primoridalUuid, nid, containerSequence);
    }

    public ConceptChronologyImpl(DataBuffer data) {
        super(data);
        constructorEnd(data);

    }

    @Override
    public String getConceptDescriptionText() {
        return Get.conceptDescriptionText(getNid());
    }

    @Override
    public void writeChronicleData(DataBuffer data) {
        super.writeChronicleData(data);
    }

    @Override
    public ConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
        int stampSequence = Get.commitService().getStampSequence(state, Long.MAX_VALUE,
                ec.getAuthorSequence(), ec.getModuleSequence(), ec.getPathSequence());
        ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());
        addVersion(newVersion);
        return newVersion;
    }

    @Override
    public ConceptVersionImpl createMutableVersion(int stampSequence) {
        ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());
        addVersion(newVersion);
        return newVersion;
    }

    @Override
    protected ConceptVersionImpl makeVersion(int stampSequence, DataBuffer bb) {
        return new ConceptVersionImpl(this, stampSequence, bb.getShort());
    }

    @Override
    public int getConceptSequence() {
        return getContainerSequence();
    }

    @Override
    public List<? extends SememeChronology<? extends DescriptionSememe>> getConceptDescriptionList() {
        return Get.sememeService().getDescriptionsForComponent(getNid()).collect(Collectors.toList());
    }

    @Override
    public boolean containsDescription(String descriptionText) {
        return Get.sememeService().getDescriptionsForComponent(getNid())
                .anyMatch((desc) -> desc.getVersionList().stream().
                        anyMatch((version) -> version.getText().equals(descriptionText)));
    }

    @Override
    public boolean containsDescription(String descriptionText, StampCoordinate stampCoordinate) {
        return Get.sememeService().getSnapshot(DescriptionSememe.class, stampCoordinate)
                .getLatestDescriptionVersionsForComponent(getNid())
                .anyMatch((latestVersion) -> latestVersion.value().getText().equals(descriptionText));
    }

    @Override
    public String toUserString() {
        List<? extends SememeChronology<? extends DescriptionSememe>> descList = getConceptDescriptionList();
        if (descList.isEmpty()) {
            return "no description for concept: " + getUuidList() + " " + getConceptSequence()
                    + " " + getNid();
        }
        return getConceptDescriptionList().get(0).getVersionList().get(0).getText();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConceptChronologyImpl{");
        builder.append(toUserString());
        builder.append(" ");
        toString(builder);
        return builder.toString();
    }

    @Override
    public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        return languageCoordinate.getFullySpecifiedDescription((List<SememeChronology<DescriptionSememe<?>>>) getConceptDescriptionList(), stampCoordinate);
    }

    @Override
    public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        return languageCoordinate.getPreferredDescription((List<SememeChronology<DescriptionSememe<?>>>) getConceptDescriptionList(), stampCoordinate);
    }

    @Override
    public Optional<LatestVersion<LogicGraphSememe>> getLogicalDefinition(
            StampCoordinate stampCoordinate,
            PremiseType premiseType, LogicCoordinate logicCoordinate) {
        int assemblageSequence;
        if (premiseType == PremiseType.INFERRED) {
            assemblageSequence = logicCoordinate.getInferredAssemblageSequence();
        } else {
            assemblageSequence = logicCoordinate.getStatedAssemblageSequence();
        }
        return Get.sememeService().getSnapshot(LogicGraphSememe.class, stampCoordinate)
                .getLatestSememeVersion(assemblageSequence);
    }
    List<RelationshipAdaptorChronologyImpl> conceptOriginRelationshipList;
    List<RelationshipAdaptorChronologyImpl> conceptOriginRelationshipListDefaltCoordinate;

    @Override
    public List<? extends SememeChronology<? extends RelationshipVersionAdaptor>>
            getRelationshipListOriginatingFromConcept(LogicCoordinate logicCoordinate) {
        if (conceptOriginRelationshipList == null) {
            conceptOriginRelationshipList = new ArrayList<>();
            getLogicService().getRelationshipAdaptorsOriginatingWithConcept(this, logicCoordinate)
                    .forEach((relAdaptor) -> {
                        conceptOriginRelationshipList.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                    });

        }
        return conceptOriginRelationshipList;
    }

    @Override
    public List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> getRelationshipListOriginatingFromConcept() {
        if (conceptOriginRelationshipList == null) {
            conceptOriginRelationshipList = new ArrayList<>();
            getLogicService().getRelationshipAdaptorsOriginatingWithConcept(this)
                    .forEach((relAdaptor) -> {
                        conceptOriginRelationshipList.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                    });

        }
        return conceptOriginRelationshipList;
    }
    List<RelationshipAdaptorChronologyImpl> relationshipListWithConceptAsDestination;
    List<RelationshipAdaptorChronologyImpl> relationshipListWithConceptAsDestinationListDefaltCoordinate;

    @Override
    public List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> getRelationshipListWithConceptAsDestination() {
        if (relationshipListWithConceptAsDestinationListDefaltCoordinate == null) {
            relationshipListWithConceptAsDestinationListDefaltCoordinate = new ArrayList<>();
            getLogicService().getRelationshipAdaptorsWithConceptAsDestination(this)
                    .forEach((relAdaptor) -> {
                        relationshipListWithConceptAsDestinationListDefaltCoordinate.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                    });

        }
        return relationshipListWithConceptAsDestinationListDefaltCoordinate;
    }

    @Override
    public List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> getRelationshipListWithConceptAsDestination(LogicCoordinate logicCoordinate) {
        if (relationshipListWithConceptAsDestination == null) {
            relationshipListWithConceptAsDestination = new ArrayList<>();
            getLogicService().getRelationshipAdaptorsWithConceptAsDestination(this, logicCoordinate)
                    .forEach((relAdaptor) -> {
                        relationshipListWithConceptAsDestination.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                    });

        }
        return relationshipListWithConceptAsDestination;
    }

    @Override
    public String getLogicalDefinitionChronologyReport(StampCoordinate stampCoordinate, PremiseType premiseType, LogicCoordinate logicCoordinate) {
         int assemblageSequence;
        if (premiseType == PremiseType.INFERRED) {
            assemblageSequence = logicCoordinate.getInferredAssemblageSequence();
        } else {
            assemblageSequence = logicCoordinate.getStatedAssemblageSequence();
        }
        Optional<SememeChronology<? extends SememeVersion<?>>> definitionChronologyOptional = 
                Get.sememeService().getSememesForComponentFromAssemblage(getNid(), assemblageSequence).findFirst();
                
        if (definitionChronologyOptional.isPresent()) {

            Collection<LogicGraphSememeImpl> versions = (Collection<LogicGraphSememeImpl>) 
                    definitionChronologyOptional.get().getVisibleOrderedVersionList(stampCoordinate);
            
//            Collection<LogicGraphSememeImpl> versionsList = new ArrayList<>();
//            for (LogicGraphSememeImpl lgs : definitionChronologyOptional.get().getVisibleOrderedVersionList(stampCoordinate)) {
//            	
//            }
            StringBuilder builder = new StringBuilder();
            builder.append("_______________________________________________________________________\n");
            builder.append("  Encountered concept '")
                    .append(Get.conceptDescriptionText(getNid()))
                    .append("' with ").append(versions.size())
                    .append(" definition versions:\n");
            builder.append("￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣\n");
            int version = 0;
            LogicalExpression previousVersion = null;
            for (LogicGraphSememeImpl lgmv : versions) {
                LogicalExpression lg = lgmv.getLogicalExpression();
                builder.append(" Version ")
                        .append(version++)
                        .append("\n")
                        .append(Get.commitService().describeStampSequence(lgmv.getStampSequence()))
                        .append("\n");
                if (previousVersion == null) {
                    builder.append(lg);
                } else {
                    IsomorphicResults solution = lg.findIsomorphisms(previousVersion);
                    builder.append(solution);
                }
                builder.append("_______________________________________________________________________\n");
                previousVersion = lg;
            }
            builder.append("￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣\n");
            return builder.toString();
        }
        return "No definition found. ";

    }

}
