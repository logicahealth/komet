/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.graph;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.IntStream;

import org.apache.mahout.math.map.OpenIntObjectHashMap;

/**
 *
 * @author kec
 */
public class SimpleDirectedGraph {

    
    /**
     * map from a child sequence key to an array of parent sequences. 
     */
    final private OpenIntObjectHashMap<int[]> childSequence_ParentSequenceArray_Map;
    /**
     * Map from a parent sequence key to an array of child sequences. 
     */
    final private OpenIntObjectHashMap<int[]> parentSequence_ChildSequenceArray_Map;
    final BitSet conceptSequencesWithParents;
    final BitSet conceptSequencesWithChildren;
    static final int[] EMPTY_INT_ARRAY = new int[0];
    private int maxSequence = -1;

    public SimpleDirectedGraph() {
        childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>();
        parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>();
        conceptSequencesWithParents = new BitSet();
        conceptSequencesWithChildren = new BitSet();
    }

    public SimpleDirectedGraph(int initialSize) {
        childSequence_ParentSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
        parentSequence_ChildSequenceArray_Map = new OpenIntObjectHashMap<>(initialSize);
        conceptSequencesWithParents = new BitSet(initialSize);
        conceptSequencesWithChildren = new BitSet(initialSize);
    }

    public int[] getChildren(int parentSequence) {
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            return parentSequence_ChildSequenceArray_Map.get(parentSequence);
        }
        return EMPTY_INT_ARRAY;
    }

    public int[] getDescendents(int parentSequence) {
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            BitSet descendentSequences = new BitSet();
            getDescendentsRecursive(parentSequence, descendentSequences);
            return descendentSequences.stream().toArray();
        }
        return EMPTY_INT_ARRAY;
    }

    public BitSet getDescendentSet(int parentSequence) {
        BitSet descendentSequences = new BitSet();
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            getDescendentsRecursive(parentSequence, descendentSequences);
            return descendentSequences;
        }
        return descendentSequences;
    }

    private void getDescendentsRecursive(int parentSequence, BitSet descendentSequences) {
        if (parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
            for (int childSequence: parentSequence_ChildSequenceArray_Map.get(parentSequence)) {
                descendentSequences.set(childSequence);
                getDescendentsRecursive(childSequence, descendentSequences);
            }
        }
    }


    public void addChildren(int parentSequence, int[] childSequenceArray) {
        maxSequence = Math.max(parentSequence, maxSequence);
        if (childSequenceArray.length > 0) {
            parentSequence_ChildSequenceArray_Map.put(parentSequence, childSequenceArray);
            maxSequence = Math.max(IntStream.of(childSequenceArray).max().getAsInt(), maxSequence);
            conceptSequencesWithChildren.set(parentSequence);
        }

    }

    public void addParents(int childSequence, int[] parentSequenceArray) {
        maxSequence = Math.max(childSequence, maxSequence);
        if (parentSequenceArray.length > 0) {
            childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
            maxSequence = Math.max(IntStream.of(parentSequenceArray).max().getAsInt(), maxSequence);
            conceptSequencesWithParents.set(childSequence);
        }
    }

    public int[] getParents(int childSequence) {
        if (childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
            return childSequence_ParentSequenceArray_Map.get(childSequence);
        }
        return EMPTY_INT_ARRAY;
    }
    

    public int[] getRootSequences() {
        BitSet rootSet = new BitSet(conceptSequencesWithParents.size());
        rootSet.or(conceptSequencesWithChildren);
        rootSet.andNot(conceptSequencesWithParents);
        return rootSet.stream().toArray();
    }

    public int size() {
        return getGraphNodeSequences().cardinality() + 1;
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public BitSet getGraphNodeSequences() {
        BitSet sizeSet = new BitSet(conceptSequencesWithParents.size());
        sizeSet.or(conceptSequencesWithChildren);
        sizeSet.or(conceptSequencesWithParents);
        return sizeSet;
    }

    public int[] getLeaves() {
        BitSet leavesSet = new BitSet(conceptSequencesWithParents.size());
        leavesSet.or(conceptSequencesWithParents);
        leavesSet.andNot(conceptSequencesWithChildren);
        return leavesSet.stream().toArray();
    }

    public int conceptSequencesWithParentsCount() {
        return conceptSequencesWithParents.cardinality();
    }

    public int conceptSequencesWithChildrenCount() {
        return conceptSequencesWithChildren.cardinality();
    }

    public SimpleDirectedGraphVisitData depthFirstSearch(int startSequence) {
        SimpleDirectedGraphVisitData graphVisitData = new SimpleDirectedGraphVisitData(this);
        dfsVisit(startSequence, graphVisitData, 0);
        return graphVisitData;
    }

    private void dfsVisit(int sequence, GraphVisitData graphVisitData, int depth) {
        graphVisitData.startNodeVisit(sequence, depth);
        boolean leaf = true;
        for (int childSequence : getChildren(sequence)) {
            leaf = false;
            if (graphVisitData.getNodeStatus(childSequence)
                    == NodeStatus.UNDISCOVERED) {
                dfsVisit(childSequence, graphVisitData, depth + 1);
            }
        }
        if (leaf) {
            graphVisitData.setLeafNode(sequence);
        }
        graphVisitData.endNodeVisit(sequence);
    }

    public SimpleDirectedGraphVisitData breadthFirstSearch(int startSequence) {
        SimpleDirectedGraphVisitData graphVisitData = new SimpleDirectedGraphVisitData(this);
        Queue<Integer> bfsQueue = new LinkedList<>();
        graphVisitData.startNodeVisit(startSequence, 0);
        bfsQueue.add(startSequence);
        while (!bfsQueue.isEmpty()) {
            int currentSequence = bfsQueue.remove();
            int currentDistance = graphVisitData.getDistance(currentSequence);
            for (int childSequence : getChildren(currentSequence)) {
                if (graphVisitData.getNodeStatus(childSequence) == NodeStatus.UNDISCOVERED) {
                    graphVisitData.startNodeVisit(childSequence, currentDistance + 1);
                    graphVisitData.setPredecessorSequence(childSequence, currentSequence);
                    bfsQueue.add(childSequence);
                }
            }
            graphVisitData.endNodeVisit(currentSequence);
        }
        return graphVisitData;
    }
}
