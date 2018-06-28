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

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.logic.TreeNodeVisitDataWithHash.ScoreRecord;

/**
 *
 * @author kec
 */
public class IsomorphicResultsFromPathHash extends TimedTaskWithProgressTracker<IsomorphicSolution> implements IsomorphicResults {

    private final LogicalExpression referenceExpression;
    private TreeNodeVisitDataWithHash referenceExpressionData;
    private final LogicalExpression comparisonExpression;
    private TreeNodeVisitDataWithHash comparisonExpressionData;

    public IsomorphicResultsFromPathHash(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        this.referenceExpression = referenceExpression;
        this.comparisonExpression = comparisonExpression;
    }

    @Override
    protected IsomorphicSolution call() {
        this.referenceExpressionData = new TreeNodeVisitDataWithHash(referenceExpression.getNodeCount(),
                this.referenceExpression);
        this.referenceExpression.processDepthFirst((consumer, treeNodeVisitData) -> {
        }, referenceExpressionData);
        this.comparisonExpressionData = new TreeNodeVisitDataWithHash(comparisonExpression.getNodeCount(),
                this.comparisonExpression);
        this.comparisonExpression.processDepthFirst((consumer, treeNodeVisitData) -> {
        }, comparisonExpressionData);

        int[] solution = new int[referenceExpression.getNodeCount()];
        Arrays.fill(solution, -1);
        solution[referenceExpression.getRoot().getNodeIndex()] = comparisonExpression.getRoot().getNodeIndex();

        IsomorphicSolution incomingSolution = new IsomorphicSolution(solution, referenceExpressionData, comparisonExpressionData);

        BitSet nodesInSolution = new BitSet(solution.length);
        nodesInSolution.set(referenceExpression.getRoot().getNodeIndex());
        return solve(referenceExpression.getRoot(), comparisonExpression.getRoot(), incomingSolution, nodesInSolution);
    }

    private IsomorphicSolution solve(LogicNode referenceNode, LogicNode comparisonNode, IsomorphicSolution incomingSolution, BitSet referenceNodesInSolution) {
        if (referenceNode.getChildren().length == 0) {
            return incomingSolution;
        }
        if (referenceNode.getNodeIndex() == 144 || referenceNode.getNodeIndex() == 150) {
            System.out.println("Found node " + referenceNode.getNodeIndex());
        }
        BitSet comparisonNodeChildren = new BitSet();
        for (LogicNode comparisonChild : comparisonNode.getChildren()) {
            comparisonNodeChildren.set(comparisonChild.getNodeIndex());
        }
        int distance = referenceExpressionData.getDistance(referenceNode) + 1;
        HashMap<UUID, List<ScoreRecord>> comparisonChildLineageHashAtLevel = comparisonExpressionData.getLineageHash_ScoreRecord_Map_ForDistance(distance);
        HashMap<UUID, List<ScoreRecord>> comparisonChildSemanticUuidAtLevel = comparisonExpressionData.getNodeSemanticUuid_ScoreRecord_Map_ForDistance(distance);
        if (referenceNode.getChildren().length == 1) {
            LogicNode referenceChild = referenceNode.getChildren()[0];
            UUID referenceChildLineageHash = referenceExpressionData.lineageHash[referenceChild.getNodeIndex()];
            List<ScoreRecord> lineageMatches = comparisonChildLineageHashAtLevel.get(referenceChildLineageHash);
            if (lineageMatches == null || lineageMatches.isEmpty()) {
                // find a semantic match...
                List<ScoreRecord> semanticMatches = comparisonChildSemanticUuidAtLevel.get(referenceExpressionData.nodeSemanticUuid[referenceChild.getNodeIndex()]);
                if (semanticMatches == null || semanticMatches.isEmpty()) {
                    return incomingSolution;
                }
                IsomorphicSolution outgoingSolution = null;

                for (ScoreRecord semanticMatch : semanticMatches) {
                    if (!referenceNodesInSolution.get(semanticMatch.nodeIndex)) {
                        int[] solution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
                        solution[referenceChild.getNodeIndex()] = semanticMatch.nodeIndex;
                        referenceNodesInSolution.set(semanticMatch.nodeIndex);
                        IsomorphicSolution matchSolution = new IsomorphicSolution(solution, referenceExpressionData, comparisonExpressionData);
                        IsomorphicSolution solveForMatchSolution = solve(referenceChild, comparisonExpression.getNode(solution[referenceChild.getNodeIndex()]), matchSolution, referenceNodesInSolution);
                        if (outgoingSolution == null || solveForMatchSolution.score > outgoingSolution.score) {
                            outgoingSolution = solveForMatchSolution;
                        }
                    }
                }
                if (outgoingSolution == null) {
                    return incomingSolution;
                }
                return outgoingSolution;

            }
            // add the first match and its children to the solution. 
            // if the node had not already been used, and if the parent of the node is correct. 
            for (ScoreRecord lineageMatch : lineageMatches) {
                if (!referenceNodesInSolution.get(lineageMatch.nodeIndex)) {
                    if (comparisonNodeChildren.get(lineageMatch.nodeIndex)) {
                        int[] solution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
                        solution[referenceChild.getNodeIndex()] = lineageMatch.nodeIndex;
                        referenceNodesInSolution.set(lineageMatch.nodeIndex);
                        IsomorphicSolution matchSolution = new IsomorphicSolution(solution, referenceExpressionData, comparisonExpressionData);
                        return solve(referenceChild, comparisonExpression.getNode(solution[referenceChild.getNodeIndex()]), matchSolution, referenceNodesInSolution);
                    }
                }
            }
            return incomingSolution;
        }
        int[] workingSolution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
        // match and eliminate lineage matches first
        for (LogicNode referenceChild : referenceNode.getChildren()) {
        if (referenceChild.getNodeIndex() == 144) {
            System.out.println("Found node b " + referenceChild.getNodeIndex());
        }
            
            UUID referenceLineageHash = referenceExpressionData.lineageHash[referenceChild.getNodeIndex()];
            List<ScoreRecord> lineageMatchesForChild = comparisonChildLineageHashAtLevel.get(referenceLineageHash);
            if (lineageMatchesForChild != null) {
                Iterator<ScoreRecord> lineageMatchIterator = lineageMatchesForChild.iterator();
                while (lineageMatchIterator.hasNext()) {
                    ScoreRecord lineageMatch = lineageMatchIterator.next();
                    // verify that this match is a referenceChild of the parent... Only want connected matches. 
                    if (comparisonNodeChildren.get(lineageMatch.nodeIndex)) {
                        // All matches are the same, since the lineage matches. Take the first one. 
                        // remove from future consideration.  
                        lineageMatchIterator.remove();
                        referenceNodesInSolution.set(referenceChild.getNodeIndex());
                        workingSolution[referenceChild.getNodeIndex()] = lineageMatch.nodeIndex;
                        IsomorphicSolution matchSolution = new IsomorphicSolution(workingSolution, referenceExpressionData, comparisonExpressionData);
                        matchSolution = solve(referenceChild, comparisonExpression.getNode(lineageMatch.nodeIndex), matchSolution, referenceNodesInSolution);
                        workingSolution = matchSolution.getSolution();
                    }
                }
            }
        }
        // then do semantic match for any remaining. 
        for (LogicNode referenceChild : referenceNode.getChildren()) {
        if (referenceChild.getNodeIndex() == 144) {
            System.out.println("Found node c " + referenceChild.getNodeIndex());
        }
            if (!referenceNodesInSolution.get(referenceChild.getNodeIndex())) {
                // find a semantic match
                UUID nodeSemanticUuid = referenceExpressionData.nodeSemanticUuid[referenceChild.getNodeIndex()];
                List<ScoreRecord> semanticMatchesForChild = comparisonChildSemanticUuidAtLevel.get(nodeSemanticUuid);
                if (semanticMatchesForChild != null) {
                    Iterator<ScoreRecord> semanticMatchIterator = semanticMatchesForChild.iterator();
                    while (semanticMatchIterator.hasNext()) {
                        ScoreRecord semanticMatch = semanticMatchIterator.next();
                        if (comparisonNodeChildren.get(semanticMatch.nodeIndex)) {
                            // TODO replace greedy algorithm with best score algorithm
                            semanticMatchIterator.remove();
                            referenceNodesInSolution.set(referenceChild.getNodeIndex());
                            workingSolution[referenceChild.getNodeIndex()] = semanticMatch.nodeIndex;
                            IsomorphicSolution matchSolution = new IsomorphicSolution(workingSolution, referenceExpressionData, comparisonExpressionData);
                            matchSolution = solve(referenceChild, comparisonExpression.getNode(semanticMatch.nodeIndex), matchSolution, referenceNodesInSolution);
                            workingSolution = matchSolution.getSolution();
                        }
                    }
                }
            }
        }
        return new IsomorphicSolution(workingSolution, referenceExpressionData, comparisonExpressionData);
    }

    @Override
    public List<LogicNode> getAddedRelationshipRoots() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LogicNode> getAdditionalNodeRoots() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LogicalExpression getComparisonExpression() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LogicNode> getDeletedNodeRoots() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LogicNode> getDeletedRelationshipRoots() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LogicalExpression getIsomorphicExpression() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LogicalExpression getMergedExpression() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LogicalExpression getReferenceExpression() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<LogicNode> getSharedRelationshipRoots() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equivalent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
