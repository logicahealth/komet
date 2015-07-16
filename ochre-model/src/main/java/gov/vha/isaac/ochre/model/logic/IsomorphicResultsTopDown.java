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
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.SequenceSet;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithNids;
import gov.vha.isaac.ochre.model.logic.node.internal.TypedNodeWithNids;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The top-down implementation is having big performance issues. I'll write a 
 * bottom up implementation that relies on leaf nodes, and see how it performs. 
 * The scoring algorithms will need to be significantly improved for this 
 * approach to be viable for graphs with more than 40 nodes. 
 * @author kec
 */
public class IsomorphicResultsTopDown implements IsomorphicResults {

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

    ConceptSequenceSet[] comparisonConceptsBelow;
    ConceptSequenceSet[] referenceConceptsBelow;

    TreeNodeVisitData referenceVisitData;
    TreeNodeVisitData comparisonVisitData;

    public IsomorphicResultsTopDown(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        this.referenceExpression = (LogicalExpressionOchreImpl) referenceExpression;
        this.comparisonExpression = (LogicalExpressionOchreImpl) comparisonExpression;
        this.referenceVisitData = new TreeNodeVisitData(referenceExpression.getNodeCount());
        this.referenceExpression.depthFirstVisit(null, this.referenceExpression.getRoot(), referenceVisitData, 0);
        this.comparisonVisitData = new TreeNodeVisitData(comparisonExpression.getNodeCount());
        this.comparisonExpression.depthFirstVisit(null, comparisonExpression.getRoot(), comparisonVisitData, 0);

        this.comparisonConceptsBelow = new ConceptSequenceSet[comparisonExpression.getNodeCount()];
        setupConceptsBelow(comparisonExpression, this.comparisonConceptsBelow,
                this.comparisonVisitData);

        this.referenceConceptsBelow = new ConceptSequenceSet[referenceExpression.getNodeCount()];
        setupConceptsBelow(referenceExpression, this.referenceConceptsBelow,
                this.referenceVisitData);
        this.isomorphicSolution = isomorphicAnalysis();
        computeAdditions();
        computeDeletions();
    }

    private void setupConceptsBelow(LogicalExpression expression,
            ConceptSequenceSet[] conceptsBelow,
            TreeNodeVisitData visitData) {

        for (int i = 0; i < conceptsBelow.length; i++) {
            conceptsBelow[i] = new ConceptSequenceSet();
        }
        visitData.getLeafNodes().stream().forEach((leafNodeId) -> {
            ConceptSequenceSet conceptsOnPath = new ConceptSequenceSet();
            int nodeId = leafNodeId;
            while (nodeId > 0) {
                Node node = expression.getNode(nodeId);
                switch (node.getNodeSemantic()) {
                    case CONCEPT:
                        ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) node;
                        conceptsOnPath.add(conceptNode.getConceptNid());
                        break;
                    case FEATURE:
                    case ROLE_ALL:
                    case ROLE_SOME:
                        TypedNodeWithNids typedNode = (TypedNodeWithNids) node;
                        conceptsOnPath.add(typedNode.getTypeConceptNid());
                        break;
                }
                conceptsBelow[nodeId].or(conceptsOnPath);
                nodeId = visitData.getPredecessorSequence(nodeId);
            }
        });
    }

    public final void computeAdditions() {
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

    public final void computeDeletions() {
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

    private int[] isomorphicAnalysis() {

        TreeSet<IsomorphicSearchTopDownNode> comparisonSearchNodeSet = new TreeSet<>();

        for (int i = 0; i < comparisonVisitData.getNodesVisited(); i++) {
            comparisonSearchNodeSet.add(new IsomorphicSearchTopDownNode(
                    comparisonVisitData.getDistance(i),
                    comparisonVisitData.getPredecessorSequence(i),
                    comparisonExpression.nodes.get(i).getNodeSemantic(), i));
        }

        int[] seedSolution = new int[referenceExpression.getNodeCount()];
        Arrays.fill(seedSolution, -1);
        seedSolution[referenceExpression.getRoot().getNodeIndex()]
                = comparisonExpression.getRoot().getNodeIndex();
        int currentMaxScore = 0;
        int maxDepth = referenceVisitData.getMaxDepth();
        Set<int[]> possibleSolutions = new TreeSet<>(new IntArrayComparator());
        possibleSolutions.add(seedSolution);

        for (int depth = 1; depth <= maxDepth; depth++) {
            currentMaxScore = scoreSolution(possibleSolutions.stream().max((int[] o1, int[] o2)
                    -> Integer.compare(scoreSolution(o1), scoreSolution(o2))).get());
            SequenceSet nodesForDepth = referenceVisitData.getNodeIdsForDepth(depth);
            HashMap<Integer, SortedSet<IsomorphicSearchTopDownNode>> possibleSolutionMap = new HashMap<>();
            for (int referenceNodeId : nodesForDepth.asArray()) {
                // Find what the parent was mapped to... Need to combine sets
                // from each possible parent...
                int referenceNodeParentId = referenceVisitData.getPredecessorSequence(referenceNodeId);
                SequenceSet comparisonNodePossibleParentSet = new SequenceSet();
                for (int[] possibleSolution : possibleSolutions) {
                    if (possibleSolution[referenceNodeParentId] >= 0) {
                        comparisonNodePossibleParentSet.add(possibleSolution[referenceNodeParentId]);
                    }
                }
                SortedSet<IsomorphicSearchTopDownNode> searchNodesForReferenceNode = new TreeSet<>();
                for (int comparisonNodePossibleParentId : comparisonNodePossibleParentSet.asArray()) {
                    IsomorphicSearchTopDownNode from = new IsomorphicSearchTopDownNode(
                            depth,
                            comparisonNodePossibleParentId,
                            referenceExpression.nodes.get(referenceNodeId).getNodeSemantic(), Integer.MIN_VALUE);

                    IsomorphicSearchTopDownNode to = new IsomorphicSearchTopDownNode(
                            depth,
                            comparisonNodePossibleParentId,
                            referenceExpression.nodes.get(referenceNodeId).getNodeSemantic(), Integer.MAX_VALUE);

                    searchNodesForReferenceNode.addAll(comparisonSearchNodeSet.subSet(from, to));
                }
                possibleSolutionMap.put(referenceNodeId, searchNodesForReferenceNode);
            }

            possibleSolutions = generatePossibleSolutions(possibleSolutions,
                    possibleSolutionMap,
                    currentMaxScore, depth);
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
     * @param currentMaxScore THe current best score for a isomorphicSolution
     * going into this depth
     * @param depth The current depth (in a breadth-first search sense) of
     * isomorphicSolution construction.
     * @return A set of possible solutions
     *
     */
    public Set<int[]> generatePossibleSolutions(Set<int[]> incomingPossibleSolutions,
            HashMap<Integer, SortedSet<IsomorphicSearchTopDownNode>> possibleSolutionMap,
            int currentMaxScore, int depth) {

        Set<int[]> possibleSolutions = new TreeSet<>(new IntArrayComparator());
        possibleSolutions.addAll(incomingPossibleSolutions);
        SequenceSet solutionNodesForDepth = SequenceSet.of(possibleSolutionMap.keySet().stream().mapToInt((nodeId) -> (int) nodeId));

        for (Map.Entry<Integer, SortedSet<IsomorphicSearchTopDownNode>> entry : possibleSolutionMap.entrySet()) {
            possibleSolutions = generatePossibleSolutions(
                    entry.getKey(),
                    entry.getValue(),
                    possibleSolutions,
                    solutionNodesForDepth,
                    currentMaxScore,
                    depth);
        }
        if (possibleSolutions.isEmpty()) {
            possibleSolutions.addAll(incomingPossibleSolutions);
        }
        return possibleSolutions;
    }

    private Set<int[]> generatePossibleSolutions(int solutionNodeId,
            Set<IsomorphicSearchTopDownNode> incomingPossibleNodes,
            Set<int[]> possibleSolutions,
            SequenceSet solutionNodesForDepth, int currentMaxScore, int depth) {

        int[] maxScoreArray = new int[]{currentMaxScore};
        // Using a set to eliminate duplicate solutions. 
        Set<int[]> outgoingPossibleNodes = new TreeSet<>(new IntArrayComparator());

        possibleSolutions.forEach((incomingPossibleSolution) -> {
            incomingPossibleNodes.forEach((isomorphicSearchNode) -> {
                // A rule: a comparison expression node cannot appear twice in a isomorphicSolution...
                if (solutionNodesForDepth.stream().allMatch((solutionNodeForDepth)
                        -> incomingPossibleSolution[solutionNodeForDepth] != isomorphicSearchNode.nodeId)) {

                    OptionalDouble average = comparisonConceptsBelow[isomorphicSearchNode.nodeId]
                            .stream().mapToDouble((conceptSequenceBelow) -> {
                                if (referenceConceptsBelow[solutionNodeId].contains(conceptSequenceBelow)) {
                                    return 1d;
                                }
                                return 0d;
                            }).average();
                    if (average.getAsDouble() > 0.5 && comparisonExpression.getNode(isomorphicSearchNode.nodeId)
                            .equals(referenceExpression.getNode(solutionNodeId))) {
                        int[] generatedPossibleSolution = new int[incomingPossibleSolution.length];
                        System.arraycopy(incomingPossibleSolution, 0,
                                generatedPossibleSolution, 0, generatedPossibleSolution.length);
                        generatedPossibleSolution[solutionNodeId] = isomorphicSearchNode.nodeId;
                        int score = scoreSolution(generatedPossibleSolution);
                        maxScoreArray[0] = Math.max(maxScoreArray[0], score);
                        if (score == maxScoreArray[0]) {
                            outgoingPossibleNodes.add(generatedPossibleSolution);
                        }
                    }
                }
            });
        });
        if (outgoingPossibleNodes.isEmpty()) {
            outgoingPossibleNodes.addAll(possibleSolutions);
        }
        return outgoingPossibleNodes.stream().filter((generatedPossibleSolution)
                -> scoreSolution(generatedPossibleSolution) >= maxScoreArray[0])
                .collect(Collectors.toCollection(() -> new TreeSet<>(new IntArrayComparator())));
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
