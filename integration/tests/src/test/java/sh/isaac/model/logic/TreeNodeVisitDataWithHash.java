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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.tree.TreeNodeVisitDataImpl;

/**
 *
 * @author kec
 */
public class TreeNodeVisitDataWithHash extends TreeNodeVisitDataImpl {
    
    final UUID[] lineageHash;
    final int[] nodesInHash;
    final UUID[] nodeSemanticUuid;
    final LogicalExpression expression;
    final List<HashMap<UUID, List<ScoreRecord>>> level_NodeSemanticUuid_ScoreRecord = new ArrayList<>();
    final List<HashMap<UUID, List<ScoreRecord>>> level_ChildHash_ScoreRecord = new ArrayList<>();
    
    public TreeNodeVisitDataWithHash(int graphSize, LogicalExpression expression) {
        super(graphSize);
        this.lineageHash = new UUID[graphSize];
        this.nodesInHash = new int[graphSize];
        this.nodeSemanticUuid = new UUID[graphSize];
        this.expression = expression;
    }
    
    UUID digest(ArrayList<UUID> hashParts) throws NoSuchAlgorithmException {
        hashParts.sort((o1, o2) -> {
            return o1.compareTo(o2);
        });
        return UuidT5Generator.get(hashParts.toString());
    }

    @Override
    public void endNodeVisit(int nodeSequence) {
        try {
            super.endNodeVisit(nodeSequence);
            int depth = getDistance(nodeSequence);
            while (depth >= level_NodeSemanticUuid_ScoreRecord.size()) {
                level_NodeSemanticUuid_ScoreRecord.add(new HashMap<>());
                level_ChildHash_ScoreRecord.add(new HashMap<>());
            }
            
            AbstractLogicNode node = (AbstractLogicNode) this.expression.getNode(nodeSequence);
            LogicNode[] children = node.getChildren();
            ArrayList<UUID> hashParts = new ArrayList<>(children.length + 1);
            hashParts.add(node.getNodeUuid());
            int nodesForHash = 1;
            OptionalInt predecessorSequence = getPredecessorNid(nodeSequence);
            if (predecessorSequence.isPresent()) {
                hashParts.add(expression.getNode(predecessorSequence.getAsInt()).getNodeUuid());
            }
            getPredecessorNid(nodeSequence);
            for (LogicNode child: children) {
                hashParts.add(lineageHash[child.getNodeIndex()]);
                nodesForHash = nodesForHash + nodesInHash[child.getNodeIndex()];
            }
            lineageHash[nodeSequence] = digest(hashParts);
            nodesInHash[nodeSequence] = nodesForHash;
            nodeSemanticUuid[nodeSequence] = node.getNodeUuid();
            
            ScoreRecord scoreRecord = new ScoreRecord(nodeSequence, lineageHash[nodeSequence], nodesInHash[nodeSequence], nodeSemanticUuid[nodeSequence]);
            
            HashMap<UUID, List<ScoreRecord>> nodeSemanticMapForLevel = level_NodeSemanticUuid_ScoreRecord.get(depth);
            if (!nodeSemanticMapForLevel.containsKey(nodeSemanticUuid[nodeSequence])) {
                nodeSemanticMapForLevel.put(nodeSemanticUuid[nodeSequence], new ArrayList<>());
            } 
            nodeSemanticMapForLevel.get(nodeSemanticUuid[nodeSequence]).add(scoreRecord);
            
            HashMap<UUID, List<ScoreRecord>> childHashMapForLevel = level_ChildHash_ScoreRecord.get(depth);
            if (!childHashMapForLevel.containsKey(lineageHash[nodeSequence])) {
                childHashMapForLevel.put(lineageHash[nodeSequence], new ArrayList<>());
            }
            childHashMapForLevel.get(lineageHash[nodeSequence]).add(scoreRecord);
        } catch (NoSuchAlgorithmException ex) {
           throw new RuntimeException(ex);
        }
    }
    
    public HashMap<UUID, List<ScoreRecord>> getNodeSemanticUuid_ScoreRecord_Map_ForDistance(int depth) {
        return level_NodeSemanticUuid_ScoreRecord.get(depth);
    }
    
    public HashMap<UUID, List<ScoreRecord>> getLineageHash_ScoreRecord_Map_ForDistance(int depth) {
        return level_ChildHash_ScoreRecord.get(depth);
    }
    
    public ScoreRecord getScore(int nodeSequence) {
        return new ScoreRecord(nodeSequence, lineageHash[nodeSequence], nodesInHash[nodeSequence], nodeSemanticUuid[nodeSequence]);
    }
    
    public SortedSet<ScoreRecord> getScores() {
        TreeSet<ScoreRecord> scores = new TreeSet<>();
        for (int i = 0; i < lineageHash.length; i++) {
            scores.add(new ScoreRecord(i, lineageHash[i], nodesInHash[i], nodeSemanticUuid[i]));
        }
        return scores;
    }
    
    public static class ScoreRecord implements Comparable<ScoreRecord> {
        final int nodeIndex;
        final UUID nodeChildHash;
        final int nodesInRecord;
        final UUID nodeSemanticUuid;

        public ScoreRecord(int node, UUID nodeChildHash, int nodesInRecord, UUID nodeSemanticUuid) {
            this.nodeIndex = node;
            this.nodeChildHash = nodeChildHash;
            this.nodesInRecord = nodesInRecord;
            this.nodeSemanticUuid = nodeSemanticUuid;
        }

        @Override
        public int compareTo(ScoreRecord o) {
            if (this.nodesInRecord != o.nodesInRecord) {
                return Integer.compare(o.nodesInRecord, this.nodesInRecord);
            }
            if (!this.nodeSemanticUuid.equals(o.nodeSemanticUuid)) {
                return o.nodeSemanticUuid.compareTo(this.nodeSemanticUuid);
            }
            if (!this.nodeChildHash.equals(o.nodeChildHash)) {
                return o.nodeChildHash.compareTo(this.nodeChildHash);
            }
            return 0;
        }

        @Override
        public String toString() {
            return "ScoreRecord{" + "node=" + nodeIndex + ", uuid=" + nodeChildHash + 
                    ", nodesInRecord=" + nodesInRecord + 
                    ", nodeSemanticUuid=" + nodeSemanticUuid + '}';
        }
        
        
    }

    public LogicalExpression getExpression() {
        return expression;
    }
    
    
}
