/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.logic;

import org.roaringbitmap.IntConsumer;
import sh.isaac.api.logic.IsomorphicSolution;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.TreeNodeVisitDataWithHash.ScoreRecord;

/**
 *
 * @author kec
 */
public class IsomorphicResultsFromPathHash extends IsomorphicResultsAbstract {

    public IsomorphicResultsFromPathHash(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        super(referenceExpression, comparisonExpression);
    }

    @Override
    public IsomorphicResults call() throws Exception {
        this.referenceVisitData = new TreeNodeVisitDataWithHash(referenceExpression.getNodeCount(),
                this.referenceExpression);
        this.referenceExpression.processDepthFirst((consumer, treeNodeVisitData) -> {
        }, referenceVisitData);
        this.comparisonVisitData = new TreeNodeVisitDataWithHash(comparisonExpression.getNodeCount(),
                this.comparisonExpression);
        this.comparisonExpression.processDepthFirst((consumer, treeNodeVisitData) -> {
        }, comparisonVisitData);

        int[] solution = new int[referenceExpression.getNodeCount()];
        Arrays.fill(solution, -1);
        solution[referenceExpression.getRoot().getNodeIndex()] = comparisonExpression.getRoot().getNodeIndex();

        IsomorphicSolution incomingSolution = new IsomorphicSolution(solution, referenceVisitData, comparisonVisitData);

        BitSet nodesInSolution = new BitSet(solution.length);
        nodesInSolution.set(referenceExpression.getRoot().getNodeIndex());
        this.isomorphicSolution = solve(referenceExpression.getRoot(), comparisonExpression.getRoot(), incomingSolution, nodesInSolution);

        for (int referenceNodeId = 0; referenceNodeId < this.isomorphicSolution.getSolution().length; referenceNodeId++) {
            if (this.isomorphicSolution.getSolution()[referenceNodeId] > -1) {
                this.comparisonExpressionToReferenceNodeIdMap[this.isomorphicSolution.getSolution()[referenceNodeId]]
                        = referenceNodeId;
            }
        }
        this.isomorphicExpression = new LogicalExpressionImpl(this.referenceExpression,
                this.isomorphicSolution.getSolution());

        this.referenceVisitData.getNodeIdsForDepth(3).forEach((IntConsumer) nodeId -> {
            this.referenceRelationshipNodesMap.put(
                    new RelationshipKey(nodeId, this.referenceExpression), nodeId);
        });
        this.comparisonVisitData.getNodeIdsForDepth(3).forEach((IntConsumer) nodeId -> {
            this.comparisonRelationshipNodesMap.put(
                    new RelationshipKey(nodeId, this.comparisonExpression), nodeId);
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

    private IsomorphicSolution solve(LogicNode referenceNode, LogicNode comparisonNode, IsomorphicSolution incomingSolution, BitSet referenceNodesInSolution) {
        if (referenceNode.getChildren().length == 0) {
            return incomingSolution;
        }
        BitSet comparisonNodeChildren = new BitSet();
        for (LogicNode comparisonChild : comparisonNode.getChildren()) {
            comparisonNodeChildren.set(comparisonChild.getNodeIndex());
        }
        int distance = referenceVisitData.getDistance(referenceNode) + 1;
        HashMap<UUID, List<ScoreRecord>> comparisonChildLineageHashAtLevel = ((TreeNodeVisitDataWithHash) comparisonVisitData).getLineageHash_ScoreRecord_Map_ForDistance(distance);
        HashMap<UUID, List<ScoreRecord>> comparisonChildSemanticUuidAtLevel = ((TreeNodeVisitDataWithHash) comparisonVisitData).getNodeSemanticUuid_ScoreRecord_Map_ForDistance(distance);
        if (referenceNode.getChildren().length == 1) {
            LogicNode referenceChild = referenceNode.getChildren()[0];
            UUID referenceChildLineageHash = ((TreeNodeVisitDataWithHash) referenceVisitData).lineageHash[referenceChild.getNodeIndex()];
            List<ScoreRecord> lineageMatches = comparisonChildLineageHashAtLevel.get(referenceChildLineageHash);
            if (lineageMatches == null || lineageMatches.isEmpty()) {
                // find a semantic match...
                List<ScoreRecord> semanticMatches = comparisonChildSemanticUuidAtLevel.get(((TreeNodeVisitDataWithHash) referenceVisitData).nodeSemanticUuid[referenceChild.getNodeIndex()]);
                if (semanticMatches == null || semanticMatches.isEmpty()) {
                    return incomingSolution;
                }
                IsomorphicSolution outgoingSolution = null;

                for (ScoreRecord semanticMatch : semanticMatches) {
                    if (!referenceNodesInSolution.get(referenceChild.getNodeIndex())) {
                        int[] solution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
                        solution[referenceChild.getNodeIndex()] = semanticMatch.nodeIndex;
                        referenceNodesInSolution.set(referenceChild.getNodeIndex());
                        IsomorphicSolution matchSolution = new IsomorphicSolution(solution, referenceVisitData, comparisonVisitData);
                        IsomorphicSolution solveForMatchSolution = solve(referenceChild, comparisonExpression.getNode(solution[referenceChild.getNodeIndex()]), matchSolution, referenceNodesInSolution);
                        if (outgoingSolution == null || solveForMatchSolution.getScore() > outgoingSolution.getScore()) {
                            outgoingSolution = solveForMatchSolution;
                        }
                    }
                }
                if (outgoingSolution == null) {
                    return incomingSolution;
                }
                return outgoingSolution;

            }
            // add the first match and its children to the isomorphicSolutionForSolve. 
            // if the node had not already been used, and if the parent of the node is correct. 
            for (ScoreRecord lineageMatch : lineageMatches) {
                if (!referenceNodesInSolution.get(referenceChild.getNodeIndex())) {
                    if (comparisonNodeChildren.get(lineageMatch.nodeIndex)) {
                        int[] solution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
                        solution[referenceChild.getNodeIndex()] = lineageMatch.nodeIndex;
                        referenceNodesInSolution.set(referenceChild.getNodeIndex());
                        IsomorphicSolution matchSolution = new IsomorphicSolution(solution, referenceVisitData, comparisonVisitData);
                        return solve(referenceChild, comparisonExpression.getNode(solution[referenceChild.getNodeIndex()]), matchSolution, referenceNodesInSolution);
                    }
                }
            }
            return incomingSolution;
        }
        int[] workingSolution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
        // match and eliminate lineage matches first
        for (LogicNode referenceChild : referenceNode.getChildren()) {
            UUID referenceLineageHash = ((TreeNodeVisitDataWithHash) referenceVisitData).lineageHash[referenceChild.getNodeIndex()];
            List<ScoreRecord> lineageMatchesForChild = comparisonChildLineageHashAtLevel.get(referenceLineageHash);
            if (lineageMatchesForChild != null) {
                for (ScoreRecord lineageMatch : lineageMatchesForChild) {
                    if (!referenceNodesInSolution.get(referenceChild.getNodeIndex())) {
                        // verify that this match is a referenceChild of the parent... Only want connected matches. 
                        if (comparisonNodeChildren.get(lineageMatch.nodeIndex)) {
                            referenceNodesInSolution.set(referenceChild.getNodeIndex());
                            workingSolution[referenceChild.getNodeIndex()] = lineageMatch.nodeIndex;
                            IsomorphicSolution matchSolution = new IsomorphicSolution(workingSolution, referenceVisitData, comparisonVisitData);
                            matchSolution = solve(referenceChild, comparisonExpression.getNode(lineageMatch.nodeIndex), matchSolution, referenceNodesInSolution);
                            workingSolution = matchSolution.getSolution();
                        }
                    }
                }
            }
        }
        // then do semantic match for any remaining. 
        for (LogicNode referenceChild : referenceNode.getChildren()) {
            if (!referenceNodesInSolution.get(referenceChild.getNodeIndex())) {
                // find a semantic match
                UUID nodeSemanticUuid = ((TreeNodeVisitDataWithHash) referenceVisitData).nodeSemanticUuid[referenceChild.getNodeIndex()];
                List<ScoreRecord> semanticMatchesForChild = comparisonChildSemanticUuidAtLevel.get(nodeSemanticUuid);
                if (semanticMatchesForChild != null) {
                    IsomorphicSolution bestSolution = new IsomorphicSolution(workingSolution, referenceVisitData, comparisonVisitData);
                    for (ScoreRecord semanticMatch : semanticMatchesForChild) {
                        if (comparisonNodeChildren.get(semanticMatch.nodeIndex)) {
                            BitSet tempReferenceNodesInSolution = (BitSet) referenceNodesInSolution.clone();
                            tempReferenceNodesInSolution.set(referenceChild.getNodeIndex());
                            workingSolution[referenceChild.getNodeIndex()] = semanticMatch.nodeIndex;
                            IsomorphicSolution matchSolution = new IsomorphicSolution(workingSolution, referenceVisitData, comparisonVisitData);
                            if (matchSolution.isLegal()) {
                                matchSolution = solve(referenceChild, comparisonExpression.getNode(semanticMatch.nodeIndex), matchSolution, tempReferenceNodesInSolution);
                                if (matchSolution.getScore() > bestSolution.getScore()) {
                                    bestSolution = matchSolution;
                                    referenceNodesInSolution.or(tempReferenceNodesInSolution);
                                } else {
                                    workingSolution[referenceChild.getNodeIndex()] = -1;
                                }
                                workingSolution = bestSolution.getSolution();
                            }
                        }
                    }
                    workingSolution = bestSolution.getSolution();
                }
            }
        }
        IsomorphicSolution isomorphicSolutionForSolve = new IsomorphicSolution(workingSolution, referenceVisitData, comparisonVisitData);
        for (int referenceNodeId = 0; referenceNodeId < isomorphicSolutionForSolve.getSolution().length; referenceNodeId++) {
            if (isomorphicSolutionForSolve.getSolution()[referenceNodeId] > -1) {
                this.comparisonExpressionToReferenceNodeIdMap[isomorphicSolutionForSolve.getSolution()[referenceNodeId]]
                        = referenceNodeId;
            }
        }

        return isomorphicSolutionForSolve;
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

}
