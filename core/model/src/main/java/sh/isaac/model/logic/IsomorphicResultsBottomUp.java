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
import sh.isaac.api.logic.IsomorphicSolution;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.mahout.math.set.OpenIntHashSet;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.collections.SequenceSet;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.tree.TreeNodeVisitDataImpl;

//~--- classes ----------------------------------------------------------------
/**
 * The Class IsomorphicResultsBottomUp.
 *
 * @author kec
 */
public class IsomorphicResultsBottomUp extends IsomorphicResultsAbstract {

    private static final String CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE = "ConceptsReferencedAtNodeOrAbove";

    /**
     * The isomorphic solution.
     */
    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new isomorphic results bottom up.
     *
     * @param referenceExpression the reference expression
     * @param comparisonExpression the comparison expression
     */
    public IsomorphicResultsBottomUp(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        super(referenceExpression, comparisonExpression);
    }

    @Override
    public IsomorphicResults call() throws Exception {
        this.referenceVisitData = new TreeNodeVisitDataImpl(referenceExpression.getNodeCount());
        this.referenceExpression.depthFirstVisit(null, this.referenceExpression.getRoot(), this.referenceVisitData, 0);
        this.comparisonVisitData = new TreeNodeVisitDataImpl(comparisonExpression.getNodeCount());
        this.comparisonExpression.depthFirstVisit(null, comparisonExpression.getRoot(), this.comparisonVisitData, 0);
        this.referenceExpressionToMergedNodeIdMap = new int[referenceExpression.getNodeCount()];
        Arrays.fill(this.referenceExpressionToMergedNodeIdMap, -1);
        this.comparisonExpressionToReferenceNodeIdMap = new int[comparisonExpression.getNodeCount()];
        Arrays.fill(this.comparisonExpressionToReferenceNodeIdMap, -1);
        this.isomorphicSolution = isomorphicAnalysis();

        for (int referenceNodeId = 0; referenceNodeId < this.isomorphicSolution.getSolution().length; referenceNodeId++) {
            if (this.isomorphicSolution.getSolution()[referenceNodeId] > -1) {
                this.comparisonExpressionToReferenceNodeIdMap[this.isomorphicSolution.getSolution()[referenceNodeId]]
                        = referenceNodeId;
            }
        }
        this.isomorphicExpression = new LogicalExpressionImpl(this.referenceExpression,
                this.isomorphicSolution.getSolution());

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
        for (int referenceNodeId = 0; referenceNodeId < this.isomorphicSolution.getSolution().length; referenceNodeId++) {
            if (this.isomorphicSolution.getSolution()[referenceNodeId] >= 0) {
                comparisonToMergedMap[this.isomorphicSolution.getSolution()[referenceNodeId]] = referenceNodeId;
            }
        }

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
        return this;
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

        for (IsomorphicSolution incomingPossibleSolution : possibleSolutions) {
            for (IsomorphicSearchBottomUpNode isomorphicSearchNode : incomingPossibleNodes) {
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

                    if (localIsomorphicSolution.isLegal()) {
                        outgoingPossibleNodes.add(localIsomorphicSolution);
                    }
                }
            }

        }

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
    private IsomorphicSolution isomorphicAnalysis() {
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

        IsomorphicSolution maxSolution = null;
        for (IsomorphicSolution possibleSolution: possibleSolutions) {
            if (maxSolution == null) {
                maxSolution = possibleSolution;
            } else if (possibleSolution.getScore() > maxSolution.getScore()) {
                maxSolution = possibleSolution;
            }
        }
        return maxSolution;
    }
}
