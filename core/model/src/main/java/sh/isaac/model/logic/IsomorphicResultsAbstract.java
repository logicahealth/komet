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

import sh.isaac.api.logic.IsomorphicSolution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import sh.isaac.api.Get;
import sh.isaac.api.collections.SequenceSet;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.tree.TreeNodeVisitDataImpl;

/**
 *
 * @author kec
 */
public abstract class IsomorphicResultsAbstract 
        implements IsomorphicResults, Callable<IsomorphicResults> {

    protected final LogicalExpressionImpl referenceExpression;
    protected TreeNodeVisitDataImpl referenceVisitData;
    protected final LogicalExpressionImpl comparisonExpression;
    protected TreeNodeVisitDataImpl comparisonVisitData;
    protected IsomorphicSolution isomorphicSolution;
    /**
     * The isomorphic expression.
     */
    protected LogicalExpressionImpl isomorphicExpression;

    /**
     * The merged expression.
     */
    protected LogicalExpressionImpl mergedExpression;

    /**
     * Nodes that are relationship roots in the referenceExpression.
     */
    protected final Map<RelationshipKey, Integer> referenceRelationshipNodesMap = new TreeMap<>();

    /**
     * Nodes that are relationship roots in the comparisonExpression.
     */
    protected final Map<RelationshipKey, Integer> comparisonRelationshipNodesMap = new TreeMap<>();
    /**
     * The comparison deletion roots.
     */
    protected SequenceSet<?> comparisonDeletionRoots = new SequenceSet<>();

    /**
     * The reference addition roots.
     */
    protected SequenceSet<?> referenceAdditionRoots = new SequenceSet<>();
    /**
     * The reference expression to merged node id map.
     */
    protected int[] referenceExpressionToMergedNodeIdMap;

    /**
     * The comparison expression to reference node id map.
     */
    protected int[] comparisonExpressionToReferenceNodeIdMap;

    public IsomorphicResultsAbstract(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        this.referenceExpression = (LogicalExpressionImpl) referenceExpression;
        this.comparisonExpression = (LogicalExpressionImpl) comparisonExpression;
        this.referenceExpressionToMergedNodeIdMap = new int[referenceExpression.getNodeCount()];
        Arrays.fill(this.referenceExpressionToMergedNodeIdMap, -1);
        this.comparisonExpressionToReferenceNodeIdMap = new int[comparisonExpression.getNodeCount()];
        Arrays.fill(this.comparisonExpressionToReferenceNodeIdMap, -1);
    }

    /**
     * Compute additions.
     */
    protected final void computeAdditions() {
        final SequenceSet<?> nodesInSolution = new SequenceSet<>();
        final SequenceSet<?> nodesNotInSolution = new SequenceSet<>();

        for (int i = 0; i < isomorphicSolution.getSolution().length; i++) {
            if (this.isomorphicSolution.getSolution()[i] >= 0) {
                nodesInSolution.add(i);
            } else {
                nodesNotInSolution.add(i);
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
    protected final void computeDeletions() {
        final SequenceSet<?> comparisonNodesInSolution = new SequenceSet<>();

        Arrays.stream(isomorphicSolution.getSolution()).forEach((nodeId) -> {
            if (nodeId >= 0) {
                comparisonNodesInSolution.add(nodeId);
            }
        });

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

    @Override
    public final boolean equivalent() {
        if (referenceVisitData.getGraphSize() == comparisonVisitData.getGraphSize()) {
            if (!referenceAdditionRoots.isEmpty()) {
                return false;
            }
            if (!comparisonDeletionRoots.isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public final List<LogicNode> getAddedRelationshipRoots() {
        final TreeSet<RelationshipKey> addedRelationshipRoots = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());
        addedRelationshipRoots.removeAll(this.comparisonRelationshipNodesMap.keySet());
        List<LogicNode> results = new ArrayList<>();
        for (RelationshipKey relationshipKey : addedRelationshipRoots) {
            results.add(this.referenceExpression.getNode(this.referenceRelationshipNodesMap.get(relationshipKey)));
        }
        return results;
    }

    @Override
    public final List<LogicNode> getAdditionalNodeRoots() {
        List<LogicNode> results = new ArrayList<>();
        for (int rootNodeId : this.referenceAdditionRoots.asArray()) {
            results.add(this.referenceExpression.getNode(rootNodeId));
        }
        return results;
    }

    @Override
    public final LogicalExpression getComparisonExpression() {
        return this.comparisonExpression;
    }

    @Override
    public final List<LogicNode> getDeletedNodeRoots() {
        List<LogicNode> results = new ArrayList<>();
        for (int deletedNodeId : this.comparisonDeletionRoots.asArray()) {
            results.add(this.comparisonExpression.getNode(deletedNodeId));
        }
        return results;
    }

    @Override
    public final List<LogicNode> getDeletedRelationshipRoots() {
        final TreeSet<RelationshipKey> deletedRelationshipRoots = new TreeSet<>(this.comparisonRelationshipNodesMap.keySet());
        deletedRelationshipRoots.removeAll(this.referenceRelationshipNodesMap.keySet());
        List<LogicNode> results = new ArrayList<>();
        for (RelationshipKey deletedNodeId : deletedRelationshipRoots) {
            results.add(this.comparisonExpression.getNode(this.comparisonRelationshipNodesMap.get(deletedNodeId)));
        }
        return results;
    }

    @Override
    public final LogicalExpression getIsomorphicExpression() {
        return this.isomorphicExpression;
    }

    public final IsomorphicSolution getIsomorphicSolution() {
        return this.isomorphicSolution;
    }

    @Override
    public final LogicalExpression getMergedExpression() {
       return this.mergedExpression;
     }

    @Override
    public final LogicalExpression getReferenceExpression() {
        return this.referenceExpression;
    }

    @Override
    public final List<LogicNode> getSharedRelationshipRoots() {
        final TreeSet<RelationshipKey> sharedRelationshipRoots = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());
        sharedRelationshipRoots.retainAll(this.comparisonRelationshipNodesMap.keySet());
        List<LogicNode> results = new ArrayList<>();
        for (RelationshipKey deletedNodeId : sharedRelationshipRoots) {
            results.add(this.comparisonExpression.getNode(this.comparisonRelationshipNodesMap.get(deletedNodeId)));
        }
        return results;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Path Hash Isomorphic Analysis for:").append(Get.conceptDescriptionText(this.referenceExpression.getConceptBeingDefinedNid())).append("\n     ").append(Get.identifierService().getUuidPrimordialStringForNid(this.referenceExpression.getConceptBeingDefinedNid())).append("\n\n");
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
            builder.append("[");
            for (int i = 0; i < referenceExpressionToMergedNodeIdMap.length; i++) {
                builder.append(i).append("r:");
                builder.append(referenceExpressionToMergedNodeIdMap[i]).append("m");
                if (i < referenceExpressionToMergedNodeIdMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
        }
        if (comparisonExpressionToReferenceNodeIdMap != null) {
            builder.append("\nReference Expression To ComparisonNodeId Map:\n\n ");
            int[] referenceExpressionToComparisonNodeIdMap = new int[referenceExpressionToMergedNodeIdMap.length];
            Arrays.fill(referenceExpressionToComparisonNodeIdMap, -1);
            for (int i = 0; i < comparisonExpressionToReferenceNodeIdMap.length; i++) {
                if (comparisonExpressionToReferenceNodeIdMap[i] >= 0) {
                    referenceExpressionToComparisonNodeIdMap[comparisonExpressionToReferenceNodeIdMap[i]] = i;
                }
            }
            builder.append("[");
            for (int i = 0; i < referenceExpressionToComparisonNodeIdMap.length; i++) {
                builder.append(i).append("r:");
                builder.append(referenceExpressionToComparisonNodeIdMap[i]).append("c");
                if (i < referenceExpressionToComparisonNodeIdMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
            builder.append("\nComparison Expression To ReferenceNodeId Map:\n\n ");
            builder.append("[");
            for (int i = 0; i < comparisonExpressionToReferenceNodeIdMap.length; i++) {
                builder.append(i).append("c:");
                builder.append(comparisonExpressionToReferenceNodeIdMap[i]).append("r");
                if (i < comparisonExpressionToReferenceNodeIdMap.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]\n");
        }
        if (this.isomorphicSolution != null) {
            builder.append("\nIsomorphic solution: \n");
            String formatString = "[%2d";
            String nullString = " \u2205 ";
            if (this.isomorphicSolution.getSolution().length < 10) {
                formatString = "[%d";
                nullString = " \u2205 ";
            }
            if (this.isomorphicSolution.getSolution().length > 99) {
                formatString = "[%3d";
                nullString = " \u2205 ";
            }
            for (int i = 0; i < this.isomorphicSolution.getSolution().length; i++) {
                builder.append("  ");
                builder.append(String.format(formatString, i));
                builder.append("r] \u279e ");
                if (this.isomorphicSolution.getSolution()[i] == -1) {
                    builder.append(nullString);
                } else {
                    builder.append(String.format(formatString, this.isomorphicSolution.getSolution()[i]));
                }
                if (this.isomorphicSolution.getSolution()[i] < 0) {
                    builder.append("\n");
                } else if (i != this.isomorphicSolution.getSolution()[i]) {
                    builder.append("c]* ");
                    builder.append(this.referenceExpression.getNode(i).toString("r"));
                    builder.append("\n");
                } else {
                    builder.append("c]  ");
                    builder.append(this.referenceExpression.getNode(i).toString("r"));
                    builder.append("\n");
                }
            }
            builder.append("\nAdditions: \n\n");
            getAdditionalNodeRoots().forEach((LogicNode additionRoot) -> {
                builder.append("  ").append(additionRoot.fragmentToString("r"));
                builder.append("\n");
            });
            builder.append("\nDeletions: \n\n");
            getDeletedNodeRoots().forEach((LogicNode deletionRoot) -> {
                builder.append("  ").append(deletionRoot.fragmentToString("c"));
                builder.append("\n");
            });
            builder.append("\nShared relationship roots: \n\n");
            getSharedRelationshipRoots().forEach((LogicNode sharedRelRoot) -> {
                builder.append("  ").append(sharedRelRoot.fragmentToString());
                builder.append("\n");
            });
            builder.append("\nNew relationship roots: \n\n");
            getAddedRelationshipRoots().forEach((LogicNode addedRelRoot) -> {
                builder.append("  ").append(addedRelRoot.fragmentToString());
                builder.append("\n");
            });
            builder.append("\nDeleted relationship roots: \n\n");
            getDeletedRelationshipRoots().forEach((LogicNode deletedRelRoot) -> {
                builder.append("  ").append(deletedRelRoot.fragmentToString());
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
    
}
