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
package gov.vha.isaac.ochre.model.logic;

import gov.vha.isaac.ochre.api.logic.IsomorphicResults;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.collections.SequenceSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author kec
 */
public class IsomorphicResultsBottomUp implements IsomorphicResults {

    LogicalExpressionOchreImpl comparisonExpression;
    LogicalExpressionOchreImpl referenceExpression;
    LogicalExpressionOchreImpl commonExpression;

    /**
     * Nodes in the comparisonExpression that are not in the common expression.
     */
    private final SequenceSet additionNodeIdSet = new SequenceSet();
    /**
     * Nodes in the comparison expression that are not in the reference
     * expression.
     */
    private final SequenceSet deletionNodeIdSet = new SequenceSet();
    /**
     * Nodes in the referenceExpression that are in in both the reference
     * expression and in the common expression.
     */
    private final SequenceSet commonNodeIdSet = new SequenceSet();
    /*
     isomorphicSolution is a mapping from nodes in the referenceExpression to nodes
     in the comparisonExpression. The index of the isomorphicSolution is the nodeId
     in the referenceExpression, the value of the array at that index is the 
     nodeId in the comparisonExpression: isomorphicSolution[nodeIdInReference] == nodeIdInComparison
     If the nodeIdInComparison == -1, then there is no corresponding node in the
     comparisonExpression as part of the isomorphicSolution. 
     */

    int[] isomorphicSolution;

    SequenceSet comparisonDeletionRoots = new SequenceSet();
    SequenceSet referenceAdditionRoots = new SequenceSet();

    TreeNodeVisitData referenceVisitData;
    TreeNodeVisitData comparisonVisitData;

    public IsomorphicResultsBottomUp(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        this.referenceExpression = (LogicalExpressionOchreImpl) referenceExpression;
        this.comparisonExpression = (LogicalExpressionOchreImpl) comparisonExpression;
        this.referenceVisitData = new TreeNodeVisitData(referenceExpression.getNodeCount());
        this.referenceExpression.depthFirstVisit(null, this.referenceExpression.getRoot(), referenceVisitData, 0);
        this.comparisonVisitData = new TreeNodeVisitData(comparisonExpression.getNodeCount());
        this.comparisonExpression.depthFirstVisit(null, comparisonExpression.getRoot(), comparisonVisitData, 0);

        this.isomorphicSolution = isomorphicAnalysis();
        computeAdditions();
        computeDeletions();
    }

    private void computeAdditions() {
        SequenceSet nodesInSolution = new SequenceSet();
        SequenceSet nodesNotInSolution = new SequenceSet();
        for (int i = 0; i < isomorphicSolution.length; i++) {
            if (isomorphicSolution[i] >= 0) {
                nodesInSolution.add(i);
            } else {
                nodesNotInSolution.add(i);
            }
        }
        nodesNotInSolution.stream().forEach((additionNode) -> {
            int additionRoot = additionNode;
            while (nodesNotInSolution.contains(referenceVisitData.getPredecessorSequence(additionRoot))) {
                additionRoot = referenceVisitData.getPredecessorSequence(additionRoot);
            }
            referenceAdditionRoots.add(additionRoot);
        });
    }

    private void computeDeletions() {
        SequenceSet comparisonNodesInSolution = new SequenceSet();
        Arrays.stream(isomorphicSolution).forEach((nodeId) -> {
            if (nodeId >= 0) {
                comparisonNodesInSolution.add(nodeId);
            }
        });
        SequenceSet comparisonNodesNotInSolution = new SequenceSet();
        IntStream.range(0, comparisonVisitData.getNodesVisited())
                .forEach((nodeId) -> {
                    if (!comparisonNodesInSolution.contains(nodeId)) {
                        comparisonNodesNotInSolution.add(nodeId);
                    }
                });
        comparisonNodesNotInSolution.stream().forEach((deletedNode) -> {
            int deletedRoot = deletedNode;
            while (comparisonNodesNotInSolution.contains(comparisonVisitData.getPredecessorSequence(deletedRoot))) {
                deletedRoot = comparisonVisitData.getPredecessorSequence(deletedRoot);
            }
            comparisonDeletionRoots.add(deletedRoot);
        });
    }

    @Override
    public Stream<Node> getAdditionRoots() {
        return referenceAdditionRoots.stream().mapToObj((nodeId) -> referenceExpression.getNode(nodeId));
    }

    @Override
    public Stream<Node> getDeletionRoots() {
        return comparisonDeletionRoots.stream().mapToObj((nodeId) -> comparisonExpression.getNode(nodeId));
    }

    @Override
    public LogicalExpression getCommon() {
        return commonExpression;
    }

    // ? score based on number or leafs included, with higher score for smaller number of intermediate nodes. 
    private int[] isomorphicAnalysis() {

        TreeSet<IsomorphicSearchBottomUpNode> comparisonSearchNodeSet = new TreeSet<>();

        for (int i = 0; i < comparisonVisitData.getNodesVisited(); i++) {
            Node node = comparisonExpression.getNode(i);
            Node[] children = node.getChildren();
            if (children.length == 0) {
                comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(
                        node.getNodeSemantic(),
                        comparisonVisitData.getConceptsReferencedAtNodeOrAbove(i),
                        -1,
                        i));
            } else {
                for (Node child : children) {
                    comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(
                            node.getNodeSemantic(),
                            comparisonVisitData.getConceptsReferencedAtNodeOrAbove(i),
                            child.getNodeIndex(),
                            i));
                }

            }
        }

        Set<int[]> possibleSolutions = new TreeSet<>(new IntArrayComparator());
        int[] seedSolution = new int[referenceExpression.getNodeCount()];
        Arrays.fill(seedSolution, -1);
        seedSolution[referenceExpression.getRoot().getNodeIndex()]
                = comparisonExpression.getRoot().getNodeIndex();
        possibleSolutions.add(seedSolution);

        Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleMatches = new TreeMap<>();
        SequenceSet nodesToTry = referenceVisitData.getLeafNodes();

        while (!nodesToTry.isEmpty()) {
            SequenceSet nextSetToTry = new SequenceSet();
            nodesToTry.stream().forEach((referenceNodeId) -> {
                int predecessorSequence = referenceVisitData.getPredecessorSequence(referenceNodeId);
                if (predecessorSequence >= 0) {
                    nextSetToTry.add(referenceVisitData.getPredecessorSequence(referenceNodeId));
                }
                Node refLeafNode = referenceExpression.getNode(referenceNodeId);
                IsomorphicSearchBottomUpNode from = new IsomorphicSearchBottomUpNode(
                        refLeafNode.getNodeSemantic(),
                        referenceVisitData.getConceptsReferencedAtNodeOrAbove(referenceNodeId),
                        referenceNodeId,
                        Integer.MIN_VALUE);

                IsomorphicSearchBottomUpNode to = new IsomorphicSearchBottomUpNode(
                        refLeafNode.getNodeSemantic(),
                        referenceVisitData.getConceptsReferencedAtNodeOrAbove(referenceNodeId),
                        referenceNodeId,
                        Integer.MAX_VALUE);

                SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode
                        = comparisonSearchNodeSet.subSet(from, to);
                possibleMatches.put(referenceNodeId, searchNodesForReferenceNode);

            });
            possibleSolutions = generatePossibleSolutions(possibleSolutions,
                    possibleMatches);

            nodesToTry = nextSetToTry;
        }

        return possibleSolutions.stream().max((int[] o1, int[] o2)
                -> Integer.compare(scoreSolution(o1), scoreSolution(o2))).get();
    }

    /**
     *
     * @param incomingPossibleSolutions the incoming set of solutions, to seed
     * the generation for this depth
     * @param possibleSolutionMap The set of possible nodes to consider for the
     * next depth of the tree.
     * @return A set of possible solutions
     *
     */
    public Set<int[]> generatePossibleSolutions(Set<int[]> incomingPossibleSolutions,
            Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleSolutionMap) {

        Set<int[]> possibleSolutions = new TreeSet<>(new IntArrayComparator());
        possibleSolutions.addAll(incomingPossibleSolutions);

        for (Map.Entry<Integer, SortedSet<IsomorphicSearchBottomUpNode>> entry : possibleSolutionMap.entrySet()) {
            possibleSolutions = generatePossibleSolutions(
                    entry.getKey(),
                    entry.getValue(),
                    possibleSolutions);
        }
        if (possibleSolutions.isEmpty()) {
            possibleSolutions.addAll(incomingPossibleSolutions);
        }
        return possibleSolutions;
    }

    private Set<int[]> generatePossibleSolutions(int solutionNodeId,
            Set<IsomorphicSearchBottomUpNode> incomingPossibleNodes,
            Set<int[]> possibleSolutions) {

        // Using a set to eliminate duplicate solutions. 
        Set<int[]> outgoingPossibleNodes = new TreeSet<>(new IntArrayComparator());

        possibleSolutions.forEach((incomingPossibleSolution) -> {
            incomingPossibleNodes.forEach((isomorphicSearchNode) -> {
                if (comparisonExpression.getNode(isomorphicSearchNode.nodeId)
                        .equals(referenceExpression.getNode(solutionNodeId))) {
                    int[] generatedPossibleSolution = new int[incomingPossibleSolution.length];
                    System.arraycopy(incomingPossibleSolution, 0,
                            generatedPossibleSolution, 0, generatedPossibleSolution.length);
                    generatedPossibleSolution[solutionNodeId] = isomorphicSearchNode.nodeId;
                    outgoingPossibleNodes.add(generatedPossibleSolution);
                }
            });
        });
        if (outgoingPossibleNodes.isEmpty()) {
            outgoingPossibleNodes.addAll(possibleSolutions);
        }
        return outgoingPossibleNodes;
    }

    /**
     * Scoring algorithm to determine if it is possible that a
     * isomorphicSolution based on the possibleSolution may score >= the current
     * maximum isomorphicSolution. Used to trim the search space of unnecessary
     * permutations.
     */
    private int scoreSolution(int[] solution) {
        int score = 0;
        for (int solutionArrayValue : solution) {
            if (solutionArrayValue >= 0) {
                score++;
            }
        }
        return score;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Isomorphic Analysis:\n");
        builder.append("Reference expression:\n ");
        builder.append(referenceExpression.toString("r"));
        builder.append("\nComparison expression:\n ");
        builder.append(comparisonExpression.toString("c"));
        builder.append("Isomprphic solution: \n");
        String formatString = "[%2d";
        String nullString = " ∅ ";
        if (isomorphicSolution.length < 10) {
            formatString = "[%d";
            nullString = " ∅ ";
        }
        if (isomorphicSolution.length > 99) {
            formatString = "[%3d";
            nullString = " ∅ ";
        }
        for (int i = 0; i < isomorphicSolution.length; i++) {
            builder.append("  ");
            builder.append(String.format(formatString, i));
            builder.append("r] ➞ ");
            if (isomorphicSolution[i] == -1) {
                builder.append(nullString);
            } else {
                builder.append(String.format(formatString, isomorphicSolution[i]));
            }
            if (isomorphicSolution[i] < 0) {
                builder.append("\n");
            } else if (i != isomorphicSolution[i]) {
                builder.append("c]* ");
                builder.append(referenceExpression.getNode(i).toString("r"));
                builder.append("\n");
            } else {
                builder.append("c]  ");
                builder.append(referenceExpression.getNode(i).toString("r"));
                builder.append("\n");
            }
        }

        builder.append("\nAdditions: \n");
        getAdditionRoots().forEach((additionRoot) -> {
            builder.append(additionRoot.fragmentToString("r"));
            builder.append("\n");
        });
        builder.append("\nDeletions: \n");
        getDeletionRoots().forEach((deletionRoot) -> {
            builder.append(deletionRoot.fragmentToString("c"));
            builder.append("\n");
        });
        return builder.toString();
    }

}
