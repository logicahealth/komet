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
package gov.vha.isaac.ochre.logic.csiro.classify.tasks;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;

import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.logic.csiro.classify.ClassifierData;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.classifier.ClassifierResults;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.logic.assertions.ConceptAssertion;
import gov.vha.isaac.ochre.api.task.TimedTask;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import au.csiro.ontology.Node;
import au.csiro.ontology.Ontology;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */public class ProcessClassificationResults extends TimedTask<ClassifierResults> {

    StampCoordinate stampCoordinate;
    LogicCoordinate logicCoordinate;

    public ProcessClassificationResults(StampCoordinate stampCoordinate,
            LogicCoordinate logicCoordinate) {
        this.stampCoordinate = stampCoordinate;
        this.logicCoordinate = logicCoordinate;
        updateTitle("Retrieve inferred axioms");
    }

    @Override
    protected ClassifierResults call() throws Exception {
        ClassifierData cd = ClassifierData.get(stampCoordinate, logicCoordinate);
        Ontology inferredAxioms = cd.getClassifiedOntology();
		  
        ClassifierResults classifierResults = collectResults(inferredAxioms, cd.getAffectedConceptSequenceSet());
        return classifierResults;
    }

    private ClassifierResults collectResults(Ontology classifiedResult, ConceptSequenceSet affectedConcepts) {
        HashSet<ConceptSequenceSet> equivalentSets = new HashSet<>();
        affectedConcepts.parallelStream().forEach((conceptSequence) -> {
            Node node = classifiedResult.getNode(Integer.toString(conceptSequence));
            if (node == null) {
                throw new RuntimeException("Null node for: " + conceptSequence);
            }
            Set<String> equivalentConcepts = node.getEquivalentConcepts();
            if (node.getEquivalentConcepts().size() > 1) {
                ConceptSequenceSet equivalentSet = new ConceptSequenceSet();
                equivalentSets.add(equivalentSet);
                equivalentConcepts.forEach((equivalentConceptSequence) -> {
                    equivalentSet.add(Integer.parseInt(equivalentConceptSequence));
                    affectedConcepts.add(Integer.parseInt(equivalentConceptSequence));
                });
            } else {
                equivalentConcepts.forEach((equivalentConceptSequence) -> {
                    try {
                        affectedConcepts.add(Integer.parseInt(equivalentConceptSequence));

                    } catch (NumberFormatException numberFormatException) {
                        if (equivalentConceptSequence.equals("_BOTTOM_")
                                || equivalentConceptSequence.equals("_TOP_")) {
                            // do nothing. 
                        } else {
                            throw numberFormatException;
                        }
                    }
                });
            }
        });

        return new ClassifierResults(affectedConcepts, equivalentSets, writeBackInferred(classifiedResult, affectedConcepts));
    }

    private Optional<CommitRecord> writeBackInferred(Ontology inferredAxioms, ConceptSequenceSet affectedConcepts) {
        SememeService sememeService = Get.sememeService();
        IdentifierService idService = Get.identifierService();
        AtomicInteger sufficientSets = new AtomicInteger();
        LogicalExpressionBuilderService logicalExpressionBuilderService = Get.logicalExpressionBuilderService();
        SememeBuilderService sememeBuilderService = Get.sememeBuilderService();
        CommitService commitService = Get.commitService();

        affectedConcepts.parallelStream().forEach((conceptSequence) -> {
            SememeSequenceSet inferredSememeSequences
                    = sememeService.getSememeSequencesForComponentFromAssemblage(idService.getConceptNid(conceptSequence), logicCoordinate.getInferredAssemblageSequence());
            SememeSequenceSet statedSememeSequences
                    = sememeService.getSememeSequencesForComponentFromAssemblage(idService.getConceptNid(conceptSequence), logicCoordinate.getStatedAssemblageSequence());
            testForProperSetSize(inferredSememeSequences, conceptSequence, statedSememeSequences, sememeService);

            //SememeChronology<LogicGraphSememe> statedChronology = (SememeChronology<LogicGraphSememe>) sememeService.getSememe(statedSememeSequences.stream().findFirst().getAsInt());
            SememeChronology rawStatedChronology = sememeService.getSememe(statedSememeSequences.stream().findFirst().getAsInt());

            Optional<LatestVersion<LogicGraphSememe>> latestStatedDefinitionOptional = ((SememeChronology<LogicGraphSememe>)rawStatedChronology).getLatestVersion(LogicGraphSememe.class, stampCoordinate);
            if (latestStatedDefinitionOptional.isPresent()) {
                LogicalExpressionBuilder inferredBuilder = logicalExpressionBuilderService.getLogicalExpressionBuilder();

                LatestVersion<LogicGraphSememe> latestStatedDefinition = latestStatedDefinitionOptional.get();
                LogicalExpression statedDefinition = latestStatedDefinition.value().getLogicalExpression();
                if (statedDefinition.contains(NodeSemantic.SUFFICIENT_SET)) {
                    sufficientSets.incrementAndGet();
                    // Sufficient sets are copied exactly to the inferred form. 
                    statedDefinition.getNodesOfType(NodeSemantic.SUFFICIENT_SET).forEach((sufficientSetNode) -> {
                        inferredBuilder.cloneSubTree(sufficientSetNode);
                    });
                }

                // Need to construct the necessary set from classifier results. 
                Node inferredNode = inferredAxioms.getNode(Integer.toString(conceptSequence));

                List<ConceptAssertion> parentList = new ArrayList<>();
                inferredNode.getParents().forEach((parent) -> {
                    parent.getEquivalentConcepts().forEach((parentString) -> {
                        try {
                            parentList.add(inferredBuilder.conceptAssertion(Integer.parseInt(parentString)));
                        } catch (NumberFormatException numberFormatException) {
                            if (parentString.equals("_BOTTOM_")
                                    || parentString.equals("_TOP_")) {
                                // do nothing. 
                            } else {
                                throw numberFormatException;
                            }
                        }
                    });
                });
                if (!parentList.isEmpty()) {
                    NecessarySet(And(parentList.toArray(new ConceptAssertion[parentList.size()])));
                    LogicalExpression inferredExpression = inferredBuilder.build();

                    if (inferredSememeSequences.isEmpty()) {
                        SememeBuilder builder = sememeBuilderService.getLogicalExpressionSememeBuilder(inferredExpression,
                                idService.getConceptNid(conceptSequence),
                                logicCoordinate.getInferredAssemblageSequence());
                        // get classifier edit coordinate...
                        builder.build(
                                EditCoordinates.getClassifierSolorOverlay(),
                                ChangeCheckerMode.INACTIVE);
                    } else {
                        SememeChronology inferredChronology =  sememeService.getSememe(inferredSememeSequences.stream().findFirst().getAsInt());
                        // check to see if changed from old...
                        Optional<LatestVersion<LogicGraphSememe>> latestDefinitionOptional = inferredChronology.getLatestVersion(LogicGraphSememe.class, stampCoordinate);
                        if (latestDefinitionOptional.isPresent()) {
                            if (!latestDefinitionOptional.get().value().getLogicalExpression().equals(inferredExpression)) {
                                MutableLogicGraphSememe newVersion = ((SememeChronology<LogicGraphSememe>)inferredChronology).createMutableVersion(MutableLogicGraphSememe.class, gov.vha.isaac.ochre.api.State.ACTIVE,
                                        EditCoordinates.getClassifierSolorOverlay());
                                newVersion.setGraphData(inferredExpression.getData(DataTarget.INTERNAL));
                                commitService.addUncommittedNoChecks(inferredChronology);
                            }
                        }
                    }
                }

            } else {
                throw new IllegalStateException("Empty latest version for stated definition. " + rawStatedChronology);
            }

        });

        Task<Optional<CommitRecord>> commitTask = commitService.commit("classifier run");
        try {
            Optional<CommitRecord> commitRecord = commitTask.get();
            if (commitRecord.isPresent()) {
                log.info("Commit record: " + commitRecord.get());
            } else {
                log.info("No commit record.");
            }

            log.info("Processed " + sufficientSets + " sufficient sets.");
            log.info("stampCoordinate: " + stampCoordinate);
            log.info("logicCoordinate: " + logicCoordinate);
            return commitRecord;
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }
	 }

    private void testForProperSetSize(SememeSequenceSet inferredSememeSequences, int conceptSequence, SememeSequenceSet statedSememeSequences, SememeService sememeService) throws IllegalStateException {
        if (inferredSememeSequences.size() > 1) {
            log.error("Processing concept: " + Get.conceptService().getConcept(conceptSequence).toUserString());
            throw new IllegalStateException("Cannot have more than one inferred definition per concept. Found: " + inferredSememeSequences);
        }
        if (statedSememeSequences.size() != 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Must have exactly one stated logic graph per concept. Found: ").append(statedSememeSequences).append("\n");
            if (statedSememeSequences.isEmpty()) {
                builder.append("No stated definition for concept: ").append(Get.conceptService().getConcept(conceptSequence).toUserString()).append("\n");
            } else {
                builder.append("Processing concept: ").append(Get.conceptService().getConcept(conceptSequence).toUserString()).append("\n");
                statedSememeSequences.stream().forEach((sememeSequence) -> {
                    builder.append("Found stated definition: ").append(sememeService.getSememe(sememeSequence)).append("\n");

                });
            }
            throw new IllegalStateException(builder.toString());
        }
    }

}
