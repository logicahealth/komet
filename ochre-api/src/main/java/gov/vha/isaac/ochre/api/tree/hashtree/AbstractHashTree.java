/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.tree.hashtree;

import gov.vha.isaac.ochre.api.tree.NodeStatus;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

/**
 *
 * @author kec
 */
public abstract class AbstractHashTree implements Tree {

    static final int[] EMPTY_INT_ARRAY = new int[0];
    /**
     * map from a child sequence key to an array of parent sequences.
     */
    protected final OpenIntObjectHashMap<int[]> childSequence_ParentSequenceArray_Map;
    /**
     * Maximum sequence number in this tree.
     */
    protected int maxSequence = -1;
    /**
     * Map from a parent sequence key to an array of child sequences.
     */
    protected final OpenIntObjectHashMap<int[]> parentSequence_ChildSequenceArray_Map;

    public AbstractHashTree() {
        childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>();
        parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>();
    }

    public AbstractHashTree(int initialSize) {
        childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
        parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
    }

    @Override
    public final TreeNodeVisitData breadthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer) {
        TreeNodeVisitData nodeVisitData = new TreeNodeVisitData(maxSequence);
        Queue<Integer> bfsQueue = new LinkedList<>();
        nodeVisitData.startNodeVisit(startSequence, 0);
        bfsQueue.add(startSequence);
        while (!bfsQueue.isEmpty()) {
            int currentSequence = bfsQueue.remove();
            int currentDistance = nodeVisitData.getDistance(currentSequence);
            int[] childSequences = getChildrenSequences(currentSequence);
            if (childSequences.length == 0) {
                nodeVisitData.setLeafNode(currentSequence);
            }
            consumer.accept(nodeVisitData, currentSequence);
            for (int childSequence : childSequences) {
                nodeVisitData.setLeafNode(currentSequence);
                consumer.accept(nodeVisitData, currentSequence);
                if (nodeVisitData.getNodeStatus(childSequence) == NodeStatus.UNDISCOVERED) {
                    nodeVisitData.startNodeVisit(childSequence, currentDistance + 1);
                    nodeVisitData.setPredecessorSequence(childSequence, currentSequence);
                    bfsQueue.add(childSequence);
                }
            }
            nodeVisitData.endNodeVisit(currentSequence);
        }
        return nodeVisitData;
    }

    @Override
    public final TreeNodeVisitData depthFirstProcess(int startSequence, ObjIntConsumer<TreeNodeVisitData> consumer) {
        TreeNodeVisitData graphVisitData = new TreeNodeVisitData(maxSequence);
        dfsVisit(startSequence, consumer, graphVisitData, 0);
        return graphVisitData;
    }

    protected void dfsVisit(int sequence, ObjIntConsumer<TreeNodeVisitData> consumer, TreeNodeVisitData nodeVisitData, int depth) {
        nodeVisitData.startNodeVisit(sequence, depth);
        int[] childSequences = getChildrenSequences(sequence);
        if (childSequences.length == 0) {
            nodeVisitData.setLeafNode(sequence);
        }
        consumer.accept(nodeVisitData, sequence);
        for (int childSequence : childSequences) {
            if (nodeVisitData.getNodeStatus(childSequence) == NodeStatus.UNDISCOVERED) {
                dfsVisit(childSequence, consumer, nodeVisitData, depth + 1);
            }
        }
        nodeVisitData.endNodeVisit(sequence);
    }

    @Override
    public final Tree createAncestorTree(int childSequence) {
        SimpleHashTree tree = new SimpleHashTree();
        addParentsAsChildren(tree, childSequence, getParentSequences(childSequence));
        return tree;
    }

    private void addParentsAsChildren(SimpleHashTree tree, int childSequence, int[] parentSequences) {
        IntStream.of(parentSequences).forEach((parentSequence) -> {
            tree.addChild(childSequence, parentSequence);
            addParentsAsChildren(tree, parentSequence, getParentSequences(parentSequence));
        });

    }

    @Override
    public final int[] getParentSequences(int childSequence) {
        if (childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
            return childSequence_ParentSequenceArray_Map.get(childSequence);
        }
        return new int[0];
    }

    @Override
    public IntStream getParentSequenceStream(int childSequence) {
        if (childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
            return IntStream.of(childSequence_ParentSequenceArray_Map.get(childSequence));
        }
        return IntStream.empty();
    }

    @Override
    public final int[] getChildrenSequences(int parentSequence) {
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            return parentSequence_ChildSequenceArray_Map.get(parentSequence);
        }
        return new int[0];
   }

    @Override
    public IntStream getChildrenSequenceStream(int parentSequence) {
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            return IntStream.of(parentSequence_ChildSequenceArray_Map.get(parentSequence));
        }
        return IntStream.empty();
    }

    @Override
    public final ConceptSequenceSet getDescendentSequenceSet(int parentSequence) {
        ConceptSequenceSet descendentSequences = new ConceptSequenceSet();
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            getDescendentsRecursive(parentSequence, descendentSequences);
            return descendentSequences;
        }
        return descendentSequences;
    }

    private void getDescendentsRecursive(int parentSequence, ConceptSequenceSet descendentSequences) {
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            for (int childSequence : parentSequence_ChildSequenceArray_Map.get(parentSequence)) {
                descendentSequences.add(childSequence);
                getDescendentsRecursive(childSequence, descendentSequences);
            }
        }
    }

}
