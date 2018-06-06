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
package sh.isaac.model.logic;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.Get;
import sh.isaac.api.collections.SequenceSet;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.tree.TreeNodeVisitDataImpl;

//~--- classes ----------------------------------------------------------------
/**
 * The Class IsomorphicResultsBottomUp.
 *
 * @author kec
 */
public class IsomorphicResultsBottomUp
        implements IsomorphicResults {

    private static final String CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE = "ConceptsReferencedAtNodeOrAbove";
    /**
     * Nodes that are relationship roots in the referenceExpression.
     */
    private final Map<RelationshipKey, Integer> referenceRelationshipNodesMap = new TreeMap<>();

    /**
     * Nodes that are relationship roots in the comparisonExpression.
     */
    private final Map<RelationshipKey, Integer> comparisonRelationshipNodesMap = new TreeMap<>();

    /**
     * The comparison deletion roots.
     */
    SequenceSet<?> comparisonDeletionRoots = new SequenceSet<>();

    /**
     * The reference addition roots.
     */
    SequenceSet<?> referenceAdditionRoots = new SequenceSet<>();

    /**
     * The comparison expression.
     */
    LogicalExpressionImpl comparisonExpression;

    /**
     * The reference expression.
     */
    LogicalExpressionImpl referenceExpression;

    /**
     * The isomorphic expression.
     */
    LogicalExpressionImpl isomorphicExpression;

    /**
     * The merged expression.
     */
    LogicalExpressionImpl mergedExpression;

    /**
     * The isomorphic solution.
     */

    /*
    * localIsomorphicSolution is a mapping from logicNodes in the referenceExpression to logicNodes
    * in the comparisonExpression. The index of the localIsomorphicSolution is the nodeId
    * in the referenceExpression, the value of the array at that index is the
    * nodeId in the comparisonExpression: localIsomorphicSolution[nodeIdInReference] == nodeIdInComparison
    * If the nodeIdInComparison == -1, then there is no corresponding node in the
    * comparisonExpression as part of the localIsomorphicSolution.
     */
    Optional<IsomorphicSolution> optionalIsomorphicSolution;

    /**
     * The reference visit data.
     */
    TreeNodeVisitData referenceVisitData;

    /**
     * The comparison visit data.
     */
    TreeNodeVisitData comparisonVisitData;

    /**
     * The reference expression to merged node id map.
     */
    int[] referenceExpressionToMergedNodeIdMap;

    /**
     * The comparison expression to reference node id map.
     */
    int[] comparisonExpressionToReferenceNodeIdMap;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new isomorphic results bottom up.
     *
     * @param referenceExpression the reference expression
     * @param comparisonExpression the comparison expression
     */
    public IsomorphicResultsBottomUp(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        this.referenceExpression = (LogicalExpressionImpl) referenceExpression;
        this.comparisonExpression = (LogicalExpressionImpl) comparisonExpression;
        this.referenceVisitData = new TreeNodeVisitDataImpl(referenceExpression.getNodeCount());
        this.referenceExpression.depthFirstVisit(null, this.referenceExpression.getRoot(), this.referenceVisitData, 0);
        this.comparisonVisitData = new TreeNodeVisitDataImpl(comparisonExpression.getNodeCount());
        this.comparisonExpression.depthFirstVisit(null, comparisonExpression.getRoot(), this.comparisonVisitData, 0);
        this.referenceExpressionToMergedNodeIdMap = new int[referenceExpression.getNodeCount()];
        Arrays.fill(this.referenceExpressionToMergedNodeIdMap, -1);
        this.comparisonExpressionToReferenceNodeIdMap = new int[comparisonExpression.getNodeCount()];
        Arrays.fill(this.comparisonExpressionToReferenceNodeIdMap, -1);
        this.optionalIsomorphicSolution = isomorphicAnalysis();

        if (optionalIsomorphicSolution.isPresent()) {
            for (int referenceNodeId = 0; referenceNodeId < this.optionalIsomorphicSolution.get().solution.length; referenceNodeId++) {
                if (this.optionalIsomorphicSolution.get().solution[referenceNodeId] > -1) {
                    this.comparisonExpressionToReferenceNodeIdMap[this.optionalIsomorphicSolution.get().solution[referenceNodeId]]
                            = referenceNodeId;
                }
            }
            this.isomorphicExpression = new LogicalExpressionImpl(this.referenceExpression,
                this.optionalIsomorphicSolution.get().solution);
        } else {
            this.isomorphicExpression = this.referenceExpression;
        }

        this.referenceVisitData.getNodeIdsForDepth(3).forEachKey((nodeId) -> {
            this.referenceRelationshipNodesMap.put(
                    new RelationshipKey(nodeId, this.referenceExpression), nodeId);
            return true;
        });
        this.comparisonVisitData.getNodeIdsForDepth(3).forEachKey((nodeId) -> {
            this.comparisonRelationshipNodesMap.put(
                    new RelationshipKey(nodeId, this.comparisonExpression), nodeId);
            return true;
        });
        computeAdditions();
        computeDeletions();

        final int[] identityMap = new int[this.referenceExpression.getNodeCount()];

        for (int i = 0; i < identityMap.length; i++) {
            identityMap[i] = i;
        }

        this.mergedExpression = new LogicalExpressionImpl(this.referenceExpression,
                identityMap,
                this.referenceExpressionToMergedNodeIdMap);

        // make a node mapping from comparison expression to the merged expression
        final int[] comparisonToMergedMap = new int[comparisonExpression.getNodeCount()];

        Arrays.fill(comparisonToMergedMap, -1);
        if (optionalIsomorphicSolution.isPresent()) {
            for (int referenceNodeId = 0; referenceNodeId < this.optionalIsomorphicSolution.get().solution.length; referenceNodeId++) {
                if (this.optionalIsomorphicSolution.get().solution[referenceNodeId] >= 0) {
                    comparisonToMergedMap[this.optionalIsomorphicSolution.get().solution[referenceNodeId]] = referenceNodeId;
                }
            }
        }

        final boolean debug = true;

        // Add the deletions
        getDeletedRelationshipRoots().forEach((deletionRoot) -> {
            // deleted relationships roots come from the comparison expression.
            OptionalInt predecessorNid = this.comparisonVisitData.getPredecessorNid(deletionRoot.getNodeIndex());
            if (predecessorNid.isPresent()) {
                int comparisonExpressionToReferenceNodeId = this.comparisonExpressionToReferenceNodeIdMap[predecessorNid.getAsInt()];
                if (comparisonExpressionToReferenceNodeId >= 0) {
                    final int rootToAddParentSequence
                            = this.referenceExpressionToMergedNodeIdMap[comparisonExpressionToReferenceNodeId];
                    addFragment(deletionRoot, this.comparisonExpression, rootToAddParentSequence);
                }
            }
        });
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Generate possible solutions.
     *
     * @param incomingPossibleSolutions the incoming set of solutions, to seed
     * the generation for this depth
     * @param possibleSolutionMap The set of possible logicNodes to consider for
     * the next depth of the tree.
     * @return A set of possible solutions
     */
    public Set<IsomorphicSolution> generatePossibleSolutions(Set<IsomorphicSolution> incomingPossibleSolutions,
            Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleSolutionMap) {
        Set<IsomorphicSolution> possibleSolutions = new HashSet<>();

        possibleSolutions.addAll(incomingPossibleSolutions);

        for (final Map.Entry<Integer, SortedSet<IsomorphicSearchBottomUpNode>> entry : possibleSolutionMap.entrySet()) {
            possibleSolutions = generatePossibleSolutionsForNode(entry.getKey(), entry.getValue(), possibleSolutions);
        }

        if (possibleSolutions.isEmpty()) {
            return incomingPossibleSolutions;
        }

        final HashMap<Integer, HashSet<IsomorphicSolution>> scoreSolutionMap = new HashMap<>();
        int maxScore = 0;

        for (final IsomorphicSolution solution : possibleSolutions) {
            if (solution.getScore() >= maxScore) {
                maxScore = solution.getScore();

                HashSet<IsomorphicSolution> maxScoreSet = scoreSolutionMap.get(maxScore);

                if (maxScoreSet == null) {
                    maxScoreSet = new HashSet<>();
                    scoreSolutionMap.put(maxScore, maxScoreSet);
                }

                maxScoreSet.add(solution);
            }
        }
        Set<IsomorphicSolution> solution = scoreSolutionMap.get(maxScore);
        if (solution != null) {
            return solution;
        }
        return new HashSet<>();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Isomorphic Analysis for:")
                .append(Get.conceptDescriptionText(this.referenceExpression.conceptNid))
                .append("\n     ")
                .append(Get.identifierService()
                        .getUuidPrimordialForNid(this.referenceExpression.conceptNid))
                .append("\n\n");
        builder.append("Reference expression:\n\n ");
        builder.append(this.referenceExpression.toString("r"));
        builder.append("\nComparison expression:\n\n ");
        builder.append(this.comparisonExpression.toString("c"));

        if (this.isomorphicExpression != null) {
            builder.append("\nIsomorphic expression:\n\n ");
            builder.append(this.isomorphicExpression.toString("i"));
        }

        if (referenceExpressionToMergedNodeIdMap != null) {
            builder.append("\nReference Expression To MergedNodeId Map:\n\n ");
            builder.append(Arrays.stream(referenceExpressionToMergedNodeIdMap).boxed().collect(Collectors.toList()));
        }
        if (comparisonExpressionToReferenceNodeIdMap != null) {
            builder.append("\nComparison Expression To ReferenceNodeId Map:\n\n ");
            builder.append(Arrays.stream(comparisonExpressionToReferenceNodeIdMap).boxed().collect(Collectors.toList()));
        }

        if (this.optionalIsomorphicSolution.isPresent()) {
            builder.append("\nIsomorphic solution: \n");

            String formatString = "[%2d";
            String nullString = " ∅ ";

            if (this.optionalIsomorphicSolution.get().getSolution().length < 10) {
                formatString = "[%d";
                nullString = " ∅ ";
            }

            if (this.optionalIsomorphicSolution.get().getSolution().length > 99) {
                formatString = "[%3d";
                nullString = " ∅ ";
            }

            for (int i = 0; i < this.optionalIsomorphicSolution.get().getSolution().length; i++) {
                builder.append("  ");
                builder.append(String.format(formatString, i));
                builder.append("r] ➞ ");

                if (this.optionalIsomorphicSolution.get().getSolution()[i] == -1) {
                    builder.append(nullString);
                } else {
                    builder.append(String.format(formatString, this.optionalIsomorphicSolution.get().getSolution()[i]));
                }

                if (this.optionalIsomorphicSolution.get().getSolution()[i] < 0) {
                    builder.append("\n");
                } else if (i != this.optionalIsomorphicSolution.get().getSolution()[i]) {
                    builder.append("c]* ");
                    builder.append(this.referenceExpression.getNode(i)
                            .toString("r"));
                    builder.append("\n");
                } else {
                    builder.append("c]  ");
                    builder.append(this.referenceExpression.getNode(i)
                            .toString("r"));
                    builder.append("\n");
                }
            }

            builder.append("\nAdditions: \n\n");
            getAdditionalNodeRoots().forEach((additionRoot) -> {
                builder.append("  ")
                        .append(additionRoot.fragmentToString("r"));
                builder.append("\n");
            });
            builder.append("\nDeletions: \n\n");
            getDeletedNodeRoots().forEach((deletionRoot) -> {
                builder.append("  ")
                        .append(deletionRoot.fragmentToString("c"));
                builder.append("\n");
            });
            builder.append("\nShared relationship roots: \n\n");
            getSharedRelationshipRoots().forEach((sharedRelRoot) -> {
                builder.append("  ")
                        .append(sharedRelRoot.fragmentToString());
                builder.append("\n");
            });
            builder.append("\nNew relationship roots: \n\n");
            getAddedRelationshipRoots().forEach((addedRelRoot) -> {
                builder.append("  ")
                        .append(addedRelRoot.fragmentToString());
                builder.append("\n");
            });
            builder.append("\nDeleted relationship roots: \n\n");
            getDeletedRelationshipRoots().forEach((deletedRelRoot) -> {
                builder.append("  ")
                        .append(deletedRelRoot.fragmentToString());
                builder.append("\n");
            });
            builder.append("\nMerged expression: \n\n");

            if (this.mergedExpression != null) {
                builder.append(this.mergedExpression.toString("m"));
            } else {
                builder.append("null");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * Adds the fragment.
     *
     * @param rootToAdd the root to add
     * @param originExpression the origin expression
     * @param rootToAddParentSequence the root to add parent sequence
     */
    private void addFragment(LogicNode rootToAdd,
            LogicalExpressionImpl originExpression,
            int rootToAddParentSequence) {
        final LogicNode[] descendents = rootToAdd.getDescendents();
        int mergedExpressionIndex = this.mergedExpression.getNodeCount();
        final int[] additionSolution = new int[originExpression.getNodeCount()];

        Arrays.fill(additionSolution, -1);
        additionSolution[rootToAdd.getNodeIndex()] = mergedExpressionIndex++;

        for (final LogicNode descendent : descendents) {
            additionSolution[descendent.getNodeIndex()] = mergedExpressionIndex++;
        }

        final LogicNode[] addedNodes = this.mergedExpression.addNodes(originExpression,
                additionSolution,
                rootToAdd.getNodeIndex());

        // Need convert rootToAddParentSequence from originExpression nodeId to mergedExpression nodeId.
        this.mergedExpression.getNode(rootToAddParentSequence)
                .addChildren(addedNodes[0]);

        // TODO make sure all children are added.
    }

    /**
     * Compute additions.
     */
    private void computeAdditions() {
        final SequenceSet<?> nodesInSolution = new SequenceSet<>();
        final SequenceSet<?> nodesNotInSolution = new SequenceSet<>();

        if (optionalIsomorphicSolution.isPresent()) {
            for (int i = 0; i < this.optionalIsomorphicSolution.get().getSolution().length; i++) {
                if (this.optionalIsomorphicSolution.get().getSolution()[i] >= 0) {
                    nodesInSolution.add(i);
                } else {
                    nodesNotInSolution.add(i);
                }
            }
        }

        nodesNotInSolution.stream().forEach((additionNode) -> {
            int additionRoot = additionNode;

            OptionalInt predecessorNid = this.referenceVisitData.getPredecessorNid(additionRoot);
            while (predecessorNid.isPresent() && nodesNotInSolution.contains(predecessorNid.getAsInt())) {
                additionRoot = predecessorNid.getAsInt();
                predecessorNid = this.referenceVisitData.getPredecessorNid(additionRoot);
            }

            this.referenceAdditionRoots.add(additionRoot);
        });
    }

    /**
     * Compute deletions.
     */
    private void computeDeletions() {
        final SequenceSet<?> comparisonNodesInSolution = new SequenceSet<>();

        if (optionalIsomorphicSolution.isPresent()) {
            Arrays.stream(this.optionalIsomorphicSolution.get().getSolution()).forEach((nodeId) -> {
                if (nodeId >= 0) {
                    comparisonNodesInSolution.add(nodeId);
                }
            });
        }

        final SequenceSet<?> comparisonNodesNotInSolution = new SequenceSet<>();

        IntStream.range(0, this.comparisonVisitData.getNodesVisited())
                .forEach((nodeId) -> {
                    if (!comparisonNodesInSolution.contains(nodeId)) {
                        comparisonNodesNotInSolution.add(nodeId);
                    }
                });
        comparisonNodesNotInSolution.stream().forEach((deletedNode) -> {
            int deletedRoot = deletedNode;

            OptionalInt predecessorNid = this.comparisonVisitData.getPredecessorNid(deletedRoot);
            while (predecessorNid.isPresent() && comparisonNodesNotInSolution.contains(predecessorNid.getAsInt())) {
                deletedRoot = predecessorNid.getAsInt();
                predecessorNid = this.comparisonVisitData.getPredecessorNid(deletedRoot);
            }

            this.comparisonDeletionRoots.add(deletedRoot);
        });
    }

    /**
     * Generate possible solutions for node.
     *
     * @param solutionNodeId the solution node id
     * @param incomingPossibleNodes the incoming possible nodes
     * @param possibleSolutions the possible solutions
     * @return the set
     */
    private Set<IsomorphicSolution> generatePossibleSolutionsForNode(int solutionNodeId,
            Set<IsomorphicSearchBottomUpNode> incomingPossibleNodes,
            Set<IsomorphicSolution> possibleSolutions) {
        // Using a set to eliminate duplicate solutions.
        final Set<IsomorphicSolution> outgoingPossibleNodes = new HashSet<>();

        possibleSolutions.forEach((incomingPossibleSolution) -> {
            incomingPossibleNodes.forEach((isomorphicSearchNode) -> {
                if (this.comparisonExpression.getNode(isomorphicSearchNode.nodeId)
                        .equals(this.referenceExpression.getNode(solutionNodeId))) {
                    final int[] generatedPossibleSolution = new int[incomingPossibleSolution.getSolution().length];

                    System.arraycopy(incomingPossibleSolution.getSolution(),
                            0,
                            generatedPossibleSolution,
                            0,
                            generatedPossibleSolution.length);
                    generatedPossibleSolution[solutionNodeId] = isomorphicSearchNode.nodeId;

                    final IsomorphicSolution localIsomorphicSolution = new IsomorphicSolution(generatedPossibleSolution,
                            this.referenceVisitData,
                            this.comparisonVisitData);

                    if (localIsomorphicSolution.legal) {
                        outgoingPossibleNodes.add(localIsomorphicSolution);
                    }
                }
            });
        });

        if (outgoingPossibleNodes.isEmpty()) {
            outgoingPossibleNodes.addAll(possibleSolutions);
        }

        return outgoingPossibleNodes;
    }

    /**
     * Isomorphic analysis.
     *
     * @return the isomorphic solution
     */
    // ? score based on number or leafs included, with higher score for smaller number of intermediate logicNodes.
    private Optional<IsomorphicSolution> isomorphicAnalysis() {
        final TreeSet<IsomorphicSearchBottomUpNode> comparisonSearchNodeSet = new TreeSet<>();

        for (int i = 0; i < this.comparisonVisitData.getNodesVisited(); i++) {
            final LogicNode logicNode = this.comparisonExpression.getNode(i);
            final LogicNode[] children = logicNode.getChildren();

            if (children.length == 0) {
                comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(logicNode.getNodeSemantic(),
                        this.comparisonVisitData.getUserNodeSet(CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE, i),
                        -1,
                        i));
            } else {
                for (final LogicNode child : children) {
                    comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(logicNode.getNodeSemantic(),
                            this.comparisonVisitData.getUserNodeSet(CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE, i),
                            child.getNodeIndex(),
                            i));
                }
            }
        }

        final SequenceSet<?> nodesProcessed = new SequenceSet<>();
        final Set<IsomorphicSolution> possibleSolutions = new HashSet<>();
        final int[] seedSolution = new int[this.referenceExpression.getNodeCount()];

        Arrays.fill(seedSolution, -1);
        seedSolution[this.referenceExpression.getRoot().getNodeIndex()] = this.comparisonExpression.getRoot()
                .getNodeIndex();
        nodesProcessed.add(this.referenceExpression.getRoot()
                .getNodeIndex());

        // Test for second level matches... Need to do so to make intermediate logicNodes (necessary set/sufficient set)
        // are included in the solution, even if there are no matching leaf logicNodes.
        for (LogicNode referenceRootChild : this.referenceExpression.getRoot().getChildren()) {
            for (LogicNode comparisonRootChild : this.comparisonExpression.getRoot().getChildren()) {
                if (referenceRootChild.equals(comparisonRootChild)) {
                    seedSolution[referenceRootChild.getNodeIndex()] = comparisonRootChild.getNodeIndex();
                    nodesProcessed.add(referenceRootChild.getNodeIndex());

                    // And node below Necessary/sufficient set logicNodes
                    referenceRootChild.getChildStream().forEach((referenceAndNode) -> {
                        assert referenceAndNode.getNodeSemantic() == NodeSemantic.AND :
                                "Expecting reference AND, Found node semantic instead: "
                                + referenceAndNode.getNodeSemantic();
                        comparisonRootChild.getChildStream().forEach((comparisonAndNode) -> {
                            assert comparisonAndNode.getNodeSemantic() == NodeSemantic.AND :
                                    "Expecting comparison AND, Found node semantic instead: "
                                    + comparisonAndNode.getNodeSemantic();
                            nodesProcessed.add(referenceAndNode.getNodeIndex());
                            seedSolution[referenceAndNode.getNodeIndex()] = comparisonAndNode.getNodeIndex();
                        });
                    });
                }
            }
        }

        possibleSolutions.add(new IsomorphicSolution(seedSolution, this.referenceVisitData, this.comparisonVisitData));

        final Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleMatches = new TreeMap<>();
        OpenIntHashSet nodesToTry = this.referenceVisitData.getLeafNodes();

        while (!nodesToTry.isEmpty()) {
            possibleMatches.clear();

            final OpenIntHashSet nextSetToTry = new OpenIntHashSet();

            nodesToTry.forEachKey((referenceNodeId) -> {
                final OptionalInt predecessorNid = this.referenceVisitData.getPredecessorNid(
                        referenceNodeId);  // only add if the node matches. ?

                if (predecessorNid.isPresent()) {
                    if (!nodesProcessed.contains(predecessorNid.getAsInt())) {
                        this.referenceVisitData.getPredecessorNid(referenceNodeId).ifPresent(nextSetToTry::add);
                        nodesProcessed.add(predecessorNid.getAsInt());
                    }
                }

                final LogicNode referenceLogicNode = this.referenceExpression.getNode(referenceNodeId);

                if (referenceLogicNode.getChildren().length == 0) {
                    final IsomorphicSearchBottomUpNode from
                            = new IsomorphicSearchBottomUpNode(referenceLogicNode.getNodeSemantic(),
                                    this.referenceVisitData.getUserNodeSet(
                                            CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE,
                                            referenceNodeId),
                                    -1,
                                    Integer.MIN_VALUE);
                    final IsomorphicSearchBottomUpNode to
                            = new IsomorphicSearchBottomUpNode(referenceLogicNode.getNodeSemantic(),
                                    this.referenceVisitData.getUserNodeSet(
                                            CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE,
                                            referenceNodeId),
                                    -1,
                                    Integer.MAX_VALUE);
                    final SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode
                            = comparisonSearchNodeSet.subSet(from,
                                    to);

                    if (!searchNodesForReferenceNode.isEmpty()) {
                        if (!possibleMatches.containsKey(referenceNodeId)) {
                            possibleMatches.put(referenceNodeId, new TreeSet<>());
                        }

                        possibleMatches.get(referenceNodeId)
                                .addAll(searchNodesForReferenceNode);
                    }
                } else {
                    for (final LogicNode child : referenceLogicNode.getChildren()) {
                        possibleSolutions.stream().map((possibleSolution) -> {
                            final IsomorphicSearchBottomUpNode from
                                    = new IsomorphicSearchBottomUpNode(
                                            referenceLogicNode.getNodeSemantic(),
                                            this.referenceVisitData.getUserNodeSet(
                                                    CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE,
                                                    referenceNodeId),
                                            possibleSolution.getSolution()[child.getNodeIndex()],
                                            Integer.MIN_VALUE);
                            final IsomorphicSearchBottomUpNode to
                                    = new IsomorphicSearchBottomUpNode(
                                            referenceLogicNode.getNodeSemantic(),
                                            this.referenceVisitData.getUserNodeSet(
                                                    CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE,
                                                    referenceNodeId),
                                            possibleSolution.getSolution()[child.getNodeIndex()],
                                            Integer.MAX_VALUE);
                            final SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode
                                    = comparisonSearchNodeSet.subSet(from,
                                            to);

                            return searchNodesForReferenceNode;
                        }).filter((searchNodesForReferenceNode) -> (!searchNodesForReferenceNode.isEmpty())).forEach((searchNodesForReferenceNode) -> {
                            if (!possibleMatches.containsKey(referenceNodeId)) {
                                possibleMatches.put(referenceNodeId, new TreeSet<>());
                            }

                            possibleMatches.get(referenceNodeId)
                                    .addAll(searchNodesForReferenceNode);
                        });
                    }
                }

                nodesProcessed.add(referenceNodeId);
                return true;
            });

            // Introducing tempPossibleSolutions secondary to limitation with lambdas, requiring a final object...
            final Set<IsomorphicSolution> tempPossibleSolutions = new HashSet<>();

            tempPossibleSolutions.addAll(possibleSolutions);
            possibleSolutions.clear();
            Set<IsomorphicSolution> someSolutions = generatePossibleSolutions(tempPossibleSolutions, possibleMatches);
            if (someSolutions != null) {
                possibleSolutions.addAll(someSolutions);
            }
            nodesToTry = nextSetToTry;
        }

        return possibleSolutions.stream()
                .max((IsomorphicSolution o1,
                        IsomorphicSolution o2) -> Integer.compare(o1.getScore(), o2.getScore()));
    }

    /**
     * Scoring algorithm to determine if it is possible that a
     * localIsomorphicSolution based on the possibleSolution may score >= the
     * current maximum localIsomorphicSolution. Used to trim the search space of
     * unnecessary permutations.
     *
     * @param solution the solution
     * @return the int
     */
    private int scoreSolution(int[] solution) {
        int score = 0;

        for (final int solutionArrayValue : solution) {
            if (solutionArrayValue >= 0) {
                score++;
            }
        }

        return score;
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the added relationship roots.
     *
     * @return the added relationship roots
     */
    @Override
    public final List<LogicNode> getAddedRelationshipRoots() {
        final TreeSet<RelationshipKey> addedRelationshipRoots
                = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());

        addedRelationshipRoots.removeAll(this.comparisonRelationshipNodesMap.keySet());

        List<LogicNode> results = new ArrayList<>();
        for (RelationshipKey relationshipKey : addedRelationshipRoots) {
            results.add(this.referenceExpression.getNode(
                    this.referenceRelationshipNodesMap.get(relationshipKey)));
        }
        return results;
    }

    /**
     * Gets the additional node roots.
     *
     * @return the additional node roots
     */
    @Override
    public List<LogicNode> getAdditionalNodeRoots() {

        List<LogicNode> results = new ArrayList<>();
        for (int rootNodeId : this.referenceAdditionRoots.asArray()) {
            results.add(this.referenceExpression.getNode(rootNodeId));
        }

        return results;
    }

    /**
     * Gets the comparison expression.
     *
     * @return the comparison expression
     */
    @Override
    public LogicalExpressionImpl getComparisonExpression() {
        return this.comparisonExpression;
    }

    /**
     * Gets the deleted node roots.
     *
     * @return the deleted node roots
     */
    @Override
    public List<LogicNode> getDeletedNodeRoots() {
        List<LogicNode> results = new ArrayList<>();
        for (int deletedNodeId : this.comparisonDeletionRoots.asArray()) {
            results.add(this.comparisonExpression.getNode(deletedNodeId));
        }
        return results;
    }

    /**
     * Gets the deleted relationship roots.
     *
     * @return the deleted relationship roots
     */
    @Override
    public final List<LogicNode> getDeletedRelationshipRoots() {
        final TreeSet<RelationshipKey> deletedRelationshipRoots
                = new TreeSet<>(this.comparisonRelationshipNodesMap.keySet());

        deletedRelationshipRoots.removeAll(this.referenceRelationshipNodesMap.keySet());

        List<LogicNode> results = new ArrayList<>();
        for (RelationshipKey deletedNodeId : deletedRelationshipRoots) {
            results.add(this.comparisonExpression.getNode(
                    this.comparisonRelationshipNodesMap.get(deletedNodeId)));
        }
        return results;
    }

    /**
     * Gets the isomorphic expression.
     *
     * @return the isomorphic expression
     */
    @Override
    public LogicalExpression getIsomorphicExpression() {
        return this.isomorphicExpression;
    }

    /**
     * Gets the merged expression.
     *
     * @return the merged expression
     */
    @Override
    public LogicalExpression getMergedExpression() {
        return this.mergedExpression;
    }

    /**
     * Gets the reference expression.
     *
     * @return the reference expression
     */
    @Override
    public LogicalExpressionImpl getReferenceExpression() {
        return this.referenceExpression;
    }

    /**
     * Gets the shared relationship roots.
     *
     * @return the shared relationship roots
     */
    @Override
    public List<LogicNode> getSharedRelationshipRoots() {
        final TreeSet<RelationshipKey> sharedRelationshipRoots
                = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());

        sharedRelationshipRoots.retainAll(this.comparisonRelationshipNodesMap.keySet());

        List<LogicNode> results = new ArrayList<>();
        for (RelationshipKey deletedNodeId : sharedRelationshipRoots) {
            results.add(this.comparisonExpression.getNode(
                    this.comparisonRelationshipNodesMap.get(deletedNodeId)));
        }
        return results;
    }

    @Override
    public boolean equivalent() {
        if (optionalIsomorphicSolution.isPresent()) {
            for (int solutionIndex : optionalIsomorphicSolution.get().solution) {
                if (solutionIndex == -1) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
