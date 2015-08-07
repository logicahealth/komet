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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.logic.IsomorphicResults;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.collections.SequenceSet;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author kec
 */
public class IsomorphicResultsBottomUp implements IsomorphicResults {

    LogicalExpressionOchreImpl comparisonExpression;
    LogicalExpressionOchreImpl referenceExpression;

    LogicalExpressionOchreImpl isomorphicExpression;

    /**
     * Nodes that are relationship roots in the referenceExpression.
     */
    private final Map<RelationshipKey, Integer> referenceRelationshipNodesMap = new TreeMap<>();
    /**
     * Nodes that are relationship roots in the comparisonExpression.
     */
    private final Map<RelationshipKey, Integer> comparisonRelationshipNodesMap = new TreeMap<>();

    /*
     isomorphicSolution is a mapping from nodes in the referenceExpression to nodes
     in the comparisonExpression. The index of the isomorphicSolution is the nodeId
     in the referenceExpression, the value of the array at that index is the 
     nodeId in the comparisonExpression: isomorphicSolution[nodeIdInReference] == nodeIdInComparison
     If the nodeIdInComparison == -1, then there is no corresponding node in the
     comparisonExpression as part of the isomorphicSolution. 
     */
    IsomorphicSolution isomorphicSolution;

    SequenceSet<?> comparisonDeletionRoots = new SequenceSet<>();
    SequenceSet<?> referenceAdditionRoots = new SequenceSet<>();

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

        this.isomorphicExpression = new LogicalExpressionOchreImpl(this.referenceExpression,
                this.isomorphicSolution.solution);

        this.referenceVisitData.getNodeIdsForDepth(3).stream().forEach((nodeId) -> {
            referenceRelationshipNodesMap.put(new RelationshipKey(nodeId, this.referenceExpression), nodeId);
        });
        this.comparisonVisitData.getNodeIdsForDepth(3).stream().forEach((nodeId) -> {
            comparisonRelationshipNodesMap.put(new RelationshipKey(nodeId, this.comparisonExpression), nodeId);
        });
        computeAdditions();
        computeDeletions();
    }

    @Override
    public Stream<Node> getDeletedRelationshipRoots() {
        TreeSet<RelationshipKey> deletedRelationshipRoots = new TreeSet<>(comparisonRelationshipNodesMap.keySet());
        deletedRelationshipRoots.removeAll(referenceRelationshipNodesMap.keySet());
        return deletedRelationshipRoots.stream().map((RelationshipKey key) -> comparisonExpression.getNode(comparisonRelationshipNodesMap.get(key)));
    }

    @Override
    public Stream<Node> getAddedRelationshipRoots() {
        TreeSet<RelationshipKey> addedRelationshipRoots = new TreeSet<>(referenceRelationshipNodesMap.keySet());
        addedRelationshipRoots.removeAll(comparisonRelationshipNodesMap.keySet());
        return addedRelationshipRoots.stream().map((RelationshipKey key) -> referenceExpression.getNode(referenceRelationshipNodesMap.get(key)));
    }

    @Override
    public Stream<Node> getSharedRelationshipRoots() {
        TreeSet<RelationshipKey> sharedRelationshipRoots = new TreeSet<>(referenceRelationshipNodesMap.keySet());
        sharedRelationshipRoots.retainAll(comparisonRelationshipNodesMap.keySet());
        return sharedRelationshipRoots.stream().map((RelationshipKey key) -> referenceExpression.getNode(referenceRelationshipNodesMap.get(key)));
    }

    @Override
    public LogicalExpressionOchreImpl getComparisonExpression() {
        return comparisonExpression;
    }

    @Override
    public LogicalExpressionOchreImpl getReferenceExpression() {
        return referenceExpression;
    }
    
    private void computeAdditions() {
        SequenceSet<?> nodesInSolution = new SequenceSet<>();
        SequenceSet<?> nodesNotInSolution = new SequenceSet<>();
        for (int i = 0; i < isomorphicSolution.getSolution().length; i++) {
            if (isomorphicSolution.getSolution()[i] >= 0) {
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
        SequenceSet<?> comparisonNodesInSolution = new SequenceSet<>();
        Arrays.stream(isomorphicSolution.getSolution()).forEach((nodeId) -> {
            if (nodeId >= 0) {
                comparisonNodesInSolution.add(nodeId);
            }
        });
        SequenceSet<?> comparisonNodesNotInSolution = new SequenceSet<>();
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
    public Stream<Node> getAdditionalNodeRoots() {
        return referenceAdditionRoots.stream().mapToObj((nodeId) -> referenceExpression.getNode(nodeId));
    }

    @Override
    public Stream<Node> getDeletedNodeRoots() {
        return comparisonDeletionRoots.stream().mapToObj((nodeId) -> comparisonExpression.getNode(nodeId));
    }

    @Override
    public LogicalExpression getIsomorphicExpression() {
        return isomorphicExpression;
    }

    // ? score based on number or leafs included, with higher score for smaller number of intermediate nodes. 
    private IsomorphicSolution isomorphicAnalysis() {

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

        SequenceSet<?> nodesProcessed = new SequenceSet<>();
        Set<IsomorphicSolution> possibleSolutions = new HashSet<>();
        int[] seedSolution = new int[referenceExpression.getNodeCount()];
        Arrays.fill(seedSolution, -1);
        seedSolution[referenceExpression.getRoot().getNodeIndex()]
                = comparisonExpression.getRoot().getNodeIndex();
        nodesProcessed.add(referenceExpression.getRoot().getNodeIndex());
        // Test for second level matches... Need to do so to make intermediate nodes (necessary set/sufficient set)
        // are included in the solution, even if there are no matching leaf nodes. 

        referenceExpression.getRoot().getChildStream().forEach((referenceRootChild) -> {
            comparisonExpression.getRoot().getChildStream().forEach((comparisonRootChild) -> {
                // Necessary/sufficient set nodes. 
                if (referenceRootChild.equals(comparisonRootChild)) {
                    seedSolution[referenceRootChild.getNodeIndex()] = comparisonRootChild.getNodeIndex();
                    nodesProcessed.add(referenceRootChild.getNodeIndex());
                    // And node below Necessary/sufficient set nodes
                    referenceRootChild.getChildStream().forEach((referenceAndNode) -> {
                        assert referenceAndNode.getNodeSemantic() == NodeSemantic.AND : "Expecting reference AND, Found node semantic instead: " + referenceAndNode.getNodeSemantic();
                        comparisonRootChild.getChildStream().forEach((comparisonAndNode) -> {
                            assert comparisonAndNode.getNodeSemantic() == NodeSemantic.AND : "Expecting comparison AND, Found node semantic instead: " + comparisonAndNode.getNodeSemantic();
                            nodesProcessed.add(referenceAndNode.getNodeIndex());
                            seedSolution[referenceAndNode.getNodeIndex()] = comparisonAndNode.getNodeIndex();
                        });
                    });
                }
            });
        });

        possibleSolutions.add(new IsomorphicSolution(seedSolution, referenceVisitData, comparisonVisitData));

        Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleMatches = new TreeMap<>();
        SequenceSet<?> nodesToTry = referenceVisitData.getLeafNodes();

        while (!nodesToTry.isEmpty()) {
            possibleMatches.clear();
            SequenceSet<?> nextSetToTry = new SequenceSet<>();



            nodesToTry.stream().forEach((referenceNodeId) -> {
                int predecessorSequence = referenceVisitData.getPredecessorSequence(referenceNodeId); // only add if the node matches. ?
                if (predecessorSequence >= 0) {
                    if (!nodesProcessed.contains(predecessorSequence)) {
                        nextSetToTry.add(referenceVisitData.getPredecessorSequence(referenceNodeId));
                        nodesProcessed.add(predecessorSequence);
                    }
                }
                Node referenceNode = referenceExpression.getNode(referenceNodeId);
                if (referenceNode.getChildren().length == 0) {
                    IsomorphicSearchBottomUpNode from = new IsomorphicSearchBottomUpNode(
                            referenceNode.getNodeSemantic(),
                            referenceVisitData.getConceptsReferencedAtNodeOrAbove(referenceNodeId),
                            -1,
                            Integer.MIN_VALUE);

                    IsomorphicSearchBottomUpNode to = new IsomorphicSearchBottomUpNode(
                            referenceNode.getNodeSemantic(),
                            referenceVisitData.getConceptsReferencedAtNodeOrAbove(referenceNodeId),
                            -1,
                            Integer.MAX_VALUE);

                    SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode
                            = comparisonSearchNodeSet.subSet(from, to);

                    if (!searchNodesForReferenceNode.isEmpty()) {
                        if (!possibleMatches.containsKey(referenceNodeId)) {
                            possibleMatches.put(referenceNodeId, new TreeSet<>());
                        }
                        possibleMatches.get(referenceNodeId).addAll(searchNodesForReferenceNode);
                    }
                } else {
                    for (Node child : referenceNode.getChildren()) {
                        possibleSolutions.stream().map((possibleSolution) -> {
                            IsomorphicSearchBottomUpNode from = new IsomorphicSearchBottomUpNode(
                                    referenceNode.getNodeSemantic(),
                                    referenceVisitData.getConceptsReferencedAtNodeOrAbove(referenceNodeId),
                                    possibleSolution.getSolution()[child.getNodeIndex()],
                                    Integer.MIN_VALUE);
                            IsomorphicSearchBottomUpNode to = new IsomorphicSearchBottomUpNode(
                                    referenceNode.getNodeSemantic(),
                                    referenceVisitData.getConceptsReferencedAtNodeOrAbove(referenceNodeId),
                                    possibleSolution.getSolution()[child.getNodeIndex()],
                                    Integer.MAX_VALUE);
                            SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode
                                    = comparisonSearchNodeSet.subSet(from, to);
                            return searchNodesForReferenceNode;
                        }).filter((searchNodesForReferenceNode) -> (!searchNodesForReferenceNode.isEmpty())).forEach((searchNodesForReferenceNode) -> {
                            if (!possibleMatches.containsKey(referenceNodeId)) {
                                possibleMatches.put(referenceNodeId, new TreeSet<>());
                            }
                            possibleMatches.get(referenceNodeId).addAll(searchNodesForReferenceNode);
                        });
                    }
                }
                nodesProcessed.add(referenceNodeId);

            });

            // Introducing tempPossibleSolutions secondary to limitation with lambdas, requiring a final object...
            Set<IsomorphicSolution> tempPossibleSolutions = new HashSet<>();
            tempPossibleSolutions.addAll(possibleSolutions);
            possibleSolutions.clear();
            possibleSolutions.addAll(generatePossibleSolutions(tempPossibleSolutions,
                    possibleMatches));

            nodesToTry = nextSetToTry;
        }

        return possibleSolutions.stream().max((IsomorphicSolution o1, IsomorphicSolution o2)
                -> Integer.compare(o1.getScore(), o2.getScore())).get();
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
    public Set<IsomorphicSolution> generatePossibleSolutions(Set<IsomorphicSolution> incomingPossibleSolutions,
                                                                    Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleSolutionMap) {

        Set<IsomorphicSolution> possibleSolutions = new HashSet<>();
        possibleSolutions.addAll(incomingPossibleSolutions);

        for (Map.Entry<Integer, SortedSet<IsomorphicSearchBottomUpNode>> entry : possibleSolutionMap.entrySet()) {
            possibleSolutions = generatePossibleSolutionsForNode(
                    entry.getKey(),
                    entry.getValue(),
                    possibleSolutions);
        }
        if (possibleSolutions.isEmpty()) {
            return incomingPossibleSolutions;
        }

        HashMap<Integer, HashSet<IsomorphicSolution>> scoreSolutionMap = new HashMap<>();
        int maxScore = 0;
        for (IsomorphicSolution solution: possibleSolutions) {
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

        return scoreSolutionMap.get(maxScore);
    }

    private Set<IsomorphicSolution> generatePossibleSolutionsForNode(int solutionNodeId,
                                                                     Set<IsomorphicSearchBottomUpNode> incomingPossibleNodes,
                                                                     Set<IsomorphicSolution> possibleSolutions) {

        // Using a set to eliminate duplicate solutions. 
        Set<IsomorphicSolution> outgoingPossibleNodes = new HashSet<>();

        possibleSolutions.forEach((incomingPossibleSolution) -> {
            incomingPossibleNodes.forEach((isomorphicSearchNode) -> {
                if (comparisonExpression.getNode(isomorphicSearchNode.nodeId)
                        .equals(referenceExpression.getNode(solutionNodeId))) {
                    int[] generatedPossibleSolution = new int[incomingPossibleSolution.getSolution().length];
                    System.arraycopy(incomingPossibleSolution.getSolution(), 0,
                            generatedPossibleSolution, 0, generatedPossibleSolution.length);
                    generatedPossibleSolution[solutionNodeId] = isomorphicSearchNode.nodeId;
                    IsomorphicSolution isomorphicSolution = new IsomorphicSolution(generatedPossibleSolution, referenceVisitData, comparisonVisitData);
                    if (isomorphicSolution.legal) {
                        outgoingPossibleNodes.add(isomorphicSolution);
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
        builder.append("Isomorphic Analysis for:" + Get.conceptDescriptionText(referenceExpression.conceptSequence) +
                "\n     " +
                Get.identifierService().getUuidPrimordialFromConceptSequence(referenceExpression.conceptSequence) +
                "\n\n");
        builder.append("Reference expression:\n\n ");
        builder.append(referenceExpression.toString("r"));
        builder.append("\nComparison expression:\n\n ");
        builder.append(comparisonExpression.toString("c"));
        if (isomorphicExpression != null) {
            builder.append("\nIsomorphic expression:\n\n ");
            builder.append(isomorphicExpression.toString("i"));
        }

        if (isomorphicSolution != null) {
            builder.append("\nIsomorphic solution: \n");
            String formatString = "[%2d";
            String nullString = " ∅ ";
            if (isomorphicSolution.getSolution().length < 10) {
                formatString = "[%d";
                nullString = " ∅ ";
            }
            if (isomorphicSolution.getSolution().length > 99) {
                formatString = "[%3d";
                nullString = " ∅ ";
            }
            for (int i = 0; i < isomorphicSolution.getSolution().length; i++) {
                builder.append("  ");
                builder.append(String.format(formatString, i));
                builder.append("r] ➞ ");
                if (isomorphicSolution.getSolution()[i] == -1) {
                    builder.append(nullString);
                } else {
                    builder.append(String.format(formatString, isomorphicSolution.getSolution()[i]));
                }
                if (isomorphicSolution.getSolution()[i] < 0) {
                    builder.append("\n");
                } else if (i != isomorphicSolution.getSolution()[i]) {
                    builder.append("c]* ");
                    builder.append(referenceExpression.getNode(i).toString("r"));
                    builder.append("\n");
                } else {
                    builder.append("c]  ");
                    builder.append(referenceExpression.getNode(i).toString("r"));
                    builder.append("\n");
                }
            }

            builder.append("\nAdditions: \n\n");
            getAdditionalNodeRoots().forEach((additionRoot) -> {
                builder.append("  ").append(additionRoot.fragmentToString("r"));
                builder.append("\n");
            });
            builder.append("\nDeletions: \n\n");
            getDeletedNodeRoots().forEach((deletionRoot) -> {
                builder.append("  ").append(deletionRoot.fragmentToString("c"));
                builder.append("\n");
            });
            
             builder.append("\nShared relationship roots: \n\n");
             getSharedRelationshipRoots().forEach((sharedRelRoot) -> {
                 builder.append("  ").append(sharedRelRoot.fragmentToString());
                builder.append("\n");
             });
             
             builder.append("\nNew relationship roots: \n\n");
             getAddedRelationshipRoots().forEach((addedRelRoot) -> {
                 builder.append("  ").append(addedRelRoot.fragmentToString());
                builder.append("\n");
             });
             builder.append("\nDeleted relationship roots: \n\n");
              getDeletedRelationshipRoots().forEach((deletedRelRoot) -> {
                 builder.append("  ").append(deletedRelRoot.fragmentToString());
                builder.append("\n");
             });
          
        }
        return builder.toString();
    }

}
