/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.tree;

import java.util.BitSet;

import org.apache.mahout.math.list.IntArrayList;

/**
 *
 * @author kec
 */
public class TreeNodeVisitData {


    protected IntArrayList distanceList = new IntArrayList();
    protected IntArrayList discoveryTimeList = new IntArrayList();
    protected IntArrayList finishTimeList = new IntArrayList();
    protected IntArrayList predecessorSequenceList = new IntArrayList();
    private BitSet visitStarted = new BitSet();
    private BitSet visitEnded = new BitSet();
    private BitSet leafNodes = new BitSet();
    
    private int maxDepth = 0;
    private int time = 0;

    public int getTime() {
        return time;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
    
    public int getNodesVisited() {
        return nodesVisited;
    }

    private int nodesVisited = 0;

    public void setLeafNode(int sequence) {
        leafNodes.set(sequence);
    }


    public TreeNodeVisitData(int graphSize) {

        visitStarted = new BitSet(graphSize);
        visitEnded = new BitSet(graphSize);
        leafNodes = new BitSet(graphSize);
        distanceList = new IntArrayList(new int[graphSize]);
        discoveryTimeList = new IntArrayList(new int[graphSize]);
        finishTimeList = new IntArrayList(new int[graphSize]);
        predecessorSequenceList = new IntArrayList(new int[graphSize]);
    }

    public int getPredecessorSequence(int sequence) {
        return predecessorSequenceList.getQuick(sequence);
    }

    public void setPredecessorSequence(int sequence, int predecessorSequence) {
        if (sequence >= predecessorSequenceList.size()) {
            predecessorSequenceList.setSize(sequence+1);
        }
        predecessorSequenceList.set(sequence, predecessorSequence);
    }

    public int getDistance(int sequence) {
        return distanceList.getQuick(sequence);
    }

    public void setDistance(int sequence, int distance) {
        if (sequence >= distanceList.size()) {
            distanceList.setSize(sequence+1);
        }
        distanceList.set(sequence, distance);
        maxDepth = Math.max(maxDepth, distance);
    }

    public int getDiscoveryTime(int sequence) {
        return discoveryTimeList.getQuick(sequence);
    }

    private void setDiscoveryTime(int sequence, int discoveryTime) {
        if (sequence >= discoveryTimeList.size()) {
            discoveryTimeList.setSize(sequence+1);
        }
        discoveryTimeList.set(sequence, discoveryTime);
    }

    public int getFinishTime(int sequence) {
        return finishTimeList.getQuick(sequence);
    }

    private void setFinishTime(int sequence, int finishTime) {
        if (sequence >= finishTimeList.size()) {
            finishTimeList.setSize(sequence+1);
        }
        finishTimeList.set(sequence, finishTime);
    }

    public NodeStatus getNodeStatus(int nodeSequence) {
        if (!visitStarted.get(nodeSequence)) {
            return NodeStatus.UNDISCOVERED;
        }
        if (visitEnded.get(nodeSequence)) {
            return NodeStatus.FINISHED;
        }
        return NodeStatus.PROCESSING;
    }

    public void startNodeVisit(int nodeSequence, int depth) {
        setNodeStatus(nodeSequence, NodeStatus.PROCESSING);
        setDiscoveryTime(nodeSequence, time++);
        setDistance(nodeSequence, depth);
        nodesVisited++;
    }

    public void endNodeVisit(int nodeSequence) {
        setNodeStatus(nodeSequence, NodeStatus.FINISHED);
        setFinishTime(nodeSequence, time++);
    }

    public void setNodeStatus(int nodeSequence, NodeStatus nodeStatus) {
        switch (nodeStatus) {
            case FINISHED:
                visitEnded.set(nodeSequence);
            case PROCESSING:

                visitStarted.set(nodeSequence);
                break;
            case UNDISCOVERED:
                throw new UnsupportedOperationException("Can't reset to undiscovered");
            default:
                throw new UnsupportedOperationException("no support for: " + nodeStatus);
        }
    }


    public BitSet getLeafNodes() {
        return leafNodes;
    }

    public BitSet getIntermediateNodes() {
        BitSet intermediateNodes = new BitSet();
        intermediateNodes.or(visitEnded);
        intermediateNodes.andNot(leafNodes);

        return intermediateNodes;
    }

}
