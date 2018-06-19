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
import java.util.HashMap;
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

        return solve(referenceExpression.getRoot(), comparisonExpression.getRoot(), incomingSolution);
    }

    private IsomorphicSolution solve(LogicNode referenceNode, LogicNode comparisonNode, IsomorphicSolution incomingSolution) {
        if (referenceNode.getChildren().length == 0) {
            return incomingSolution;
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
                if (semanticMatches.isEmpty()) {
                    return incomingSolution;
                }
                IsomorphicSolution outgoingSolution = null;

                for (ScoreRecord semanticMatch : semanticMatches) {
                    int[] solution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
                    solution[referenceChild.getNodeIndex()] = semanticMatch.node;
                    IsomorphicSolution matchSolution = new IsomorphicSolution(solution, referenceExpressionData, comparisonExpressionData);
                    IsomorphicSolution solveForMatchSolution = solve(referenceChild, comparisonExpression.getNode(solution[referenceChild.getNodeIndex()]), matchSolution);
                    if (outgoingSolution == null || solveForMatchSolution.score > outgoingSolution.score) {
                        outgoingSolution = solveForMatchSolution;
                    }
                }
                return outgoingSolution;

            }
            // add the first match and its children to the solution. 
            ScoreRecord lineageMatch = lineageMatches.get(0);
            int[] solution = Arrays.copyOf(incomingSolution.getSolution(), referenceExpression.getNodeCount());
            solution[referenceChild.getNodeIndex()] = lineageMatch.node;
            IsomorphicSolution matchSolution = new IsomorphicSolution(solution, referenceExpressionData, comparisonExpressionData);
            return solve(referenceChild, comparisonExpression.getNode(solution[referenceChild.getNodeIndex()]), matchSolution);
        }
        for (LogicNode child : referenceNode.getChildren()) {
            UUID referenceChildHash = referenceExpressionData.lineageHash[child.getNodeIndex()];
            List<ScoreRecord> matchesForChild = comparisonChildLineageHashAtLevel.get(referenceChildHash);
            if (matchesForChild == null || matchesForChild.isEmpty()) {
                // find a semantic match
            } else if (matchesForChild.size() == 1) {
                // only match, no need to try multiple possiblities
                // add the match and its children to the solution. 
            } else {
                // find the best match
            }
        }
        return null; // not yet complete.
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
