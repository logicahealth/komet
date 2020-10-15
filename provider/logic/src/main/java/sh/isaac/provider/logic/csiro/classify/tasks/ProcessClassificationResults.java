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
package sh.isaac.provider.logic.csiro.classify.tasks;


import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import au.csiro.ontology.Node;
import au.csiro.ontology.Ontology;
import javafx.concurrent.Task;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TestConcept;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.task.AggregateTaskInput;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.logic.ClassifierResultsImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierData;

/**
 * The Class ProcessClassificationResults.
 *
 * @author kec
 */
public class ProcessClassificationResults
        extends TimedTaskWithProgressTracker<ClassifierResults> implements AggregateTaskInput {

    ClassifierData inputData;
    Logger log = LogManager.getLogger();

    int classificationDuplicateCount = -1;
    int classificationCountDuplicatesToNote = 10;
    private final ManifoldCoordinate manifoldCoordinate;
    private final Instant effectiveCommitTime;

    /**
     * Instantiates a new process classification results task.
     *
     * @param manifoldCoordinate
     */
    public ProcessClassificationResults(ManifoldCoordinate manifoldCoordinate) {
        if (manifoldCoordinate.getViewStampFilter().getTime() == Long.MAX_VALUE) {
            throw new IllegalStateException("Filter position time must reflect the actual commit time, not 'latest' (Long.MAX_VALUE) ");
        }
        this.manifoldCoordinate = manifoldCoordinate;
        this.effectiveCommitTime = manifoldCoordinate.getViewStampFilter().getTimeAsInstant();
        updateTitle("Retrieve inferred axioms");
    }
    
    /**
     * Must pass in a {@link ClassifierData} prior to executing this task 
     * @see sh.isaac.api.task.AggregateTaskInput#setInput(java.lang.Object)
     */
    @Override
    public void setInput(Object inputData)  {
       if (!(inputData instanceof ClassifierData)) {
          throw new RuntimeException("Input data to LoadAxioms must be " + ClassifierData.class.getName());
       }
       this.inputData = (ClassifierData)inputData;
    }

    @Override
    protected ClassifierResults call()
            throws Exception {
        Get.activeTasks().add(this);
        setStartTime();
        try {
            log.info("Processing classification results");
            if (inputData == null) {
                throw new RuntimeException("Input data to ProcessClassificationResults must be specified by calling setInput prior to executing");
            }
            final Ontology inferredAxioms = this.inputData.getClassifiedOntology();
            Set<Integer> affectedConceptNids = this.inputData.getAffectedConceptNidSet();
            this.addToTotalWork(affectedConceptNids.size());
            Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.INACTIVE);
            WriteCoordinate wc = manifoldCoordinate.getWriteCoordinate(transaction);

            final ClassifierResults classifierResults = collectResults(wc, inferredAxioms, affectedConceptNids);
            transaction.commit("Classifier").get();
            Get.logicService().addClassifierResults(classifierResults);
            log.info("Adding classifier results to logic service...");
            return classifierResults;
        } finally {
            log.info("Notify taxonomy update");
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks().remove(this);
            log.info("Classify results process complete");
        }
    }

    /**
     * Collect results.
     *
     * @param classifiedResult the classified result
     * @param affectedConcepts the affected concepts
     * @return the classifier results
     */
    private ClassifierResults collectResults(WriteCoordinate wc, Ontology classifiedResult, Set<Integer> affectedConcepts) {
        final HashSet<IntArrayList> equivalentSets = new HashSet<>();
        LOG.debug("collect results begins for {} concepts", affectedConcepts.size());
        affectedConcepts.parallelStream().forEach((conceptNid) -> {
            completedUnitOfWork();
            final Node node = classifiedResult.getNode(Integer.toString(conceptNid));

            if (node == null) {
                log.error("Null node for: {} {} {} will be skipped in classifier results collect", conceptNid, 
                    Get.identifierService().getUuidPrimordialStringForNid(conceptNid), Get.conceptDescriptionText(conceptNid));
                // TODO possibly propagate error in gui...
            } else {
                final Set<String> equivalentConcepts = node.getEquivalentConcepts();

                if (equivalentConcepts.size() > 1) {
                    IntArrayList equivalentNids = new IntArrayList(equivalentConcepts.size());
                    for (String equivalentConcept : equivalentConcepts) {
                        int equivalentNid = Integer.parseInt(equivalentConcept);
                        equivalentNids.add(equivalentNid);
                        affectedConcepts.add(equivalentNid);
                    }
                    equivalentNids.sort();
                    equivalentSets.add(equivalentNids);
                } else {
                    for (String equivalentConceptCsiroId : equivalentConcepts) {
                        try {
                            int equivalentNid = Integer.parseInt(equivalentConceptCsiroId);
                            affectedConcepts.add(equivalentNid);
                        } catch (final NumberFormatException numberFormatException) {
                            if (equivalentConceptCsiroId.equals("_BOTTOM_")
                                    || equivalentConceptCsiroId.equals("_TOP_")) {
                                // do nothing.
                            } else {
                                throw numberFormatException;
                            }
                        }
                    }
                }
            }
        });
//        if (!equivalentSets.isEmpty()) {
//            log.info("Equivalent set count: " + equivalentSets.size());
//            int setCount = 1;
//            for (IntArrayList equivalentSet: equivalentSets) {
//                StringBuilder sb = new StringBuilder("Set " + setCount++ + ":\n");
//                for (int nid: equivalentSet.elements()) {
//                    sb.append(Get.conceptDescriptionText(nid)).append("\n");
//                }
//                log.info(sb.toString());
//            }
//        }
        return new ClassifierResultsImpl(affectedConcepts,
                equivalentSets,
                writeBackInferred(wc, classifiedResult, affectedConcepts), manifoldCoordinate);
    }

    /**
     * Test for proper set size.
     *
     * @param inferredSemanticSequences the inferred semantic sequences
     * @param conceptNid the concept nid
     * @param statedSemanticSequences the stated semantic sequences
     * @param semanticService the semantic service
     * @throws IllegalStateException the illegal state exception
     */
    private void testForProperSetSize(ImmutableIntSet inferredSemanticSequences,
            int conceptNid,
                                      ImmutableIntSet statedSemanticSequences,
            AssemblageService semanticService)
            throws IllegalStateException {
        if (inferredSemanticSequences.size() > 1) {
            classificationDuplicateCount++;
            if (classificationDuplicateCount < classificationCountDuplicatesToNote) {
                log.error("Cannot have more than one inferred definition per concept. Found: "
                        + inferredSemanticSequences + "\n\nProcessing concept: " + Get.conceptService().getConceptChronology(conceptNid).toUserString());
            }
        }

        if (statedSemanticSequences.size() != 1) {
            final StringBuilder builder = new StringBuilder();

            builder.append("Must have exactly one stated logic graph per concept. Found: ")
                    .append(statedSemanticSequences)
                    .append("\n");

            if (statedSemanticSequences.isEmpty()) {
                builder.append("No stated definition for concept: ")
                        .append(Get.conceptService()
                                .getConceptChronology(conceptNid)
                                .toUserString())
                        .append("\n");
            } else {
                builder.append("Processing concept: ")
                        .append(Get.conceptService()
                                .getConceptChronology(conceptNid)
                                .toUserString())
                        .append("\n");
                statedSemanticSequences.forEach((semanticSequence) -> {
                    builder.append("Found stated definition: ")
                            .append(semanticService.getSemanticChronology(semanticSequence))
                            .append("\n");
                });
            }
            log.error(builder.toString());
        }
    }

    /**
     * Write back inferred.
     *
     * @param inferredAxioms the inferred axioms
     * @param affectedConcepts the affected concepts
     * @return the optional
     */
    private Optional<CommitRecord> writeBackInferred(WriteCoordinate wc, Ontology inferredAxioms, Set<Integer> affectedConcepts) {
        final AssemblageService assemblageService = Get.assemblageService();
        final AtomicInteger sufficientSets = new AtomicInteger();
        final LogicalExpressionBuilderService logicalExpressionBuilderService = Get.logicalExpressionBuilderService();
        final SemanticBuilderService<? extends SemanticChronology> semanticBuilderService = Get.semanticBuilderService();
        final CommitService commitService = Get.commitService();

        LOG.debug("write back inferred begins with {} axioms", inferredAxioms.getInferredAxioms().size());
        // TODO Dan notes, for reasons not yet understood, this parallelStream call isn't working.  
        // JVisualVM tells me that all of this work is occurring on a single thread.  Need to figure out why...
        ConcurrentHashMap<OptionalWaitTask<?>, Boolean> submitted = new ConcurrentHashMap<>();
        affectedConcepts.parallelStream().forEach((conceptNid) -> {
            try {
                final ImmutableIntSet inferredSemanticNids
                        = assemblageService.getSemanticNidsForComponentFromAssemblage(conceptNid,
                                this.inputData.getLogicCoordinate().getInferredAssemblageNid());
                final ImmutableIntSet statedSemanticNids
                        = assemblageService.getSemanticNidsForComponentFromAssemblage(conceptNid,
                                this.inputData.getLogicCoordinate().getStatedAssemblageNid());

                testForProperSetSize(inferredSemanticNids,
                        conceptNid,
                        statedSemanticNids,
                        assemblageService);

                // SemanticChronology<LogicGraphSemantic> statedChronology = (SemanticChronology<LogicGraphSemantic>) 
                // assemblageService.getSemanticChronology(statedSemanticNids.stream().findFirst().getAsInt());
                if (!statedSemanticNids.isEmpty()) {

                    final SemanticChronology rawStatedChronology
                            = assemblageService.getSemanticChronology(statedSemanticNids.intIterator().next());
                    final LatestVersion<LogicGraphVersion> latestStatedDefinitionOptional
                            = ((SemanticChronology) rawStatedChronology).getLatestVersion(this.inputData.getStampFilter());

                    if (latestStatedDefinitionOptional.isPresent()) {
                        final LogicalExpressionBuilder inferredBuilder
                                = logicalExpressionBuilderService.getLogicalExpressionBuilder();
                        final LatestVersion<LogicGraphVersion> latestStatedDefinition
                                = latestStatedDefinitionOptional;
                        final LogicalExpression statedDefinition = latestStatedDefinition.get()
                                .getLogicalExpression();

                        if (statedDefinition.contains(NodeSemantic.SUFFICIENT_SET)) {
                            sufficientSets.incrementAndGet();

                            // Sufficient sets are copied exactly to the inferred form.
                            statedDefinition.getNodesOfType(NodeSemantic.SUFFICIENT_SET).forEach((sufficientSetNode) -> {
                                inferredBuilder.cloneSubTree(sufficientSetNode);
                            });
                        }

                        // Need to construct the necessary set from classifier results.
                        final Node inferredNode
                                = inferredAxioms.getNode(Integer.toString(conceptNid));
                        final List<ConceptAssertion> parentList = new ArrayList<>();
                        if (inferredNode != null) {
                            inferredNode.getParents().forEach((parent) -> {
                                parent.getEquivalentConcepts().forEach((parentString) -> {
                                    try {
                                        int parentNid = Integer.parseInt(parentString);

                                        parentList.add(
                                                inferredBuilder.conceptAssertion(parentNid));
                                    } catch (final NumberFormatException numberFormatException) {
                                        if (parentString.equals("_BOTTOM_") || parentString.equals("_TOP_")) {
                                            // do nothing.
                                        } else {
                                            throw numberFormatException;
                                        }
                                    }
                                });
                            });
                        }

                        if (!parentList.isEmpty()) {
                            NecessarySet(
                                    And(parentList.toArray(new ConceptAssertion[parentList.size()])));

                            final LogicalExpression inferredExpression = inferredBuilder.build();

                            if (inferredSemanticNids.isEmpty()) {
                                final SemanticBuilder<? extends SemanticChronology> builder
                                        = semanticBuilderService.getLogicalExpressionBuilder(inferredExpression,
                                                conceptNid,
                                                this.inputData.getLogicCoordinate().getInferredAssemblageNid());

                                // get classifier edit coordinate...
                                submitted.put(builder.buildAndWrite(wc), true);
                                
                                if (Get.configurationService().isVerboseDebugEnabled() && TestConcept.CARBOHYDRATE_OBSERVATION.getNid() == conceptNid) {
                                    log.info("ADDING INFERRED NID FOR: " + TestConcept.CARBOHYDRATE_OBSERVATION);
                                    TestConcept.WATCH_NID_SET.add(builder.getNid());
                                }
                            } else {
                                final SemanticChronology inferredChronology
                                        = assemblageService.getSemanticChronology(inferredSemanticNids.intIterator().next());

                                // check to see if changed from old...
                                final LatestVersion<LogicGraphVersion> latestDefinitionOptional
                                        = inferredChronology.getLatestVersion(this.inputData.getStampFilter());

                                if (latestDefinitionOptional.isPresent()) {
                                    if (!latestDefinitionOptional.get()
                                            .getLogicalExpression()
                                            .equals(inferredExpression)) {
                                        final MutableLogicGraphVersion newVersion
                                                = ((SemanticChronology) inferredChronology).createMutableVersion(new WriteCoordinateImpl(wc,Status.ACTIVE));

                                        newVersion.setGraphData(
                                                inferredExpression.getData(DataTarget.INTERNAL));
                                        submitted.put(new OptionalWaitTask<Void>(commitService.addUncommitted(wc.getTransaction().get(), newVersion), null, null), true);
                                    }
                                }
                            }
                        }
                    } else {
                        throw new IllegalStateException(
                                "Empty latest version for stated definition. " + rawStatedChronology);
                    }
                } else {
                    LogManager.getLogger()
                            .error("No statedSemanticNid - skipping concept: " + Get.conceptDescriptionText(conceptNid));
                }

            } catch (final IllegalStateException e) {
                LogManager.getLogger()
                        .error("Error during writeback - skipping concept ", e);
            }
        });
        
        //Wait until all writes are done:
        LOG.debug("Ensuring all writes are complete");
        submitted.forEachKey(50, task -> {
            try {
                task.get();
            }
            catch (InterruptedException | ExecutionException e1) {
                throw new RuntimeException("Failure writing logic graphs for classification", e1);
            }
        });
        LOG.debug("Comitting {} semantics", submitted.size());
        final Task<Optional<CommitRecord>> commitTask = wc.getTransaction().get().commit( "classifier run", this.effectiveCommitTime);

        try {
            final Optional<CommitRecord> commitRecord = commitTask.get();

            if (commitRecord.isPresent()) {
                log.info("Commit record: " + commitRecord.get());
            } else {
                log.info("No commit record.");
            }

            if (classificationDuplicateCount > 0) {
                log.warn("Inferred duplicates found: " + classificationDuplicateCount);
            }
            log.info("Processed " + sufficientSets + " sufficient sets.");
            log.info("stampCoordinate: " + this.inputData.getStampFilter());
            log.info("logicCoordinate: " + this.inputData.getLogicCoordinate());
            return commitRecord;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
