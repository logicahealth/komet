/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.tree;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.SequenceSet;

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class TreeNodeVisitData {

    protected final IntArrayList distanceList;
    protected final IntArrayList discoveryTimeList;
    protected final IntArrayList finishTimeList;
    protected final IntArrayList predecessorSequenceList;
    protected final IntArrayList siblingGroupSequenceList;
    private SequenceSet<?> visitStarted = new SequenceSet<>();
    private SequenceSet<?> visitEnded = new SequenceSet<>();
    private SequenceSet<?> leafNodes = new SequenceSet<>();
    private OpenIntHashSet[] conceptsReferencedAtNodeOrAbove;

    private int maxDepth = 0;
    private int time = 0;
    private int nodesVisited = 0;
    private final int graphSize;

    public TreeNodeVisitData(int graphSize) {

        this.graphSize = graphSize;
        this.visitStarted = new SequenceSet<>();
        this.visitEnded = new SequenceSet<>();
        this.leafNodes = new SequenceSet<>();
        this.distanceList = new IntArrayList(new int[graphSize]);
        this.discoveryTimeList = new IntArrayList(new int[graphSize]);
        this.finishTimeList = new IntArrayList(new int[graphSize]);
        this.siblingGroupSequenceList = new IntArrayList(new int[graphSize]);
        this.predecessorSequenceList = new IntArrayList(new int[graphSize]);
        this.predecessorSequenceList.fillFromToWith(0, graphSize - 1, -1);
    }

    public int getTime() {
        return time;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getNodesVisited() {
        return nodesVisited;
    }

    public void setLeafNode(int sequence) {
        leafNodes.add(sequence);
    }

    public OpenIntHashSet getConceptsReferencedAtNodeOrAbove(int nodeSequence) {
        if (nodeSequence >= 0) {
            // lazy creation to save memory since not all tree traversals want to 
            // use this capability. 
            if (conceptsReferencedAtNodeOrAbove == null) {
                conceptsReferencedAtNodeOrAbove = new OpenIntHashSet[graphSize];
            }
            if (conceptsReferencedAtNodeOrAbove[nodeSequence] == null) {
                conceptsReferencedAtNodeOrAbove[nodeSequence] = new OpenIntHashSet(graphSize);
            }
            return conceptsReferencedAtNodeOrAbove[nodeSequence];
        }
        return new OpenIntHashSet(graphSize);
    }

    public void setConceptsReferencedAtNodeOrAbove(int nodeSequence, OpenIntHashSet conceptSet) {
        if (nodeSequence >= 0) {
            // lazy creation to save memory since not all tree traversals want to
            // use this capability.
            if (conceptsReferencedAtNodeOrAbove == null) {
                conceptsReferencedAtNodeOrAbove = new OpenIntHashSet[graphSize];
            }
            conceptsReferencedAtNodeOrAbove[nodeSequence] = conceptSet;
        }
    }

    public void setConceptsReferencedAtNodeOrAbove(int nodeSequence, ConceptSequenceSet conceptSet) {
        if (nodeSequence >= 0) {
            // lazy creation to save memory since not all tree traversals want to
            // use this capability.
            if (conceptsReferencedAtNodeOrAbove == null) {
                conceptsReferencedAtNodeOrAbove = new OpenIntHashSet[graphSize];
            }
            conceptsReferencedAtNodeOrAbove[nodeSequence] = conceptSet.asOpenIntHashSet();
        }
    }

    public int getPredecessorSequence(int sequence) {
        return predecessorSequenceList.getQuick(sequence);
    }

    public void setPredecessorSequence(int sequence, int predecessorSequence) {
        if (sequence >= predecessorSequenceList.size()) {
            predecessorSequenceList.setSize(sequence + 1);
        }
        predecessorSequenceList.set(sequence, predecessorSequence);
    }

    public int getDistance(int sequence) {
        return distanceList.getQuick(sequence);
    }

    public void setDistance(int sequence, int distance) {
        if (sequence >= distanceList.size()) {
            distanceList.setSize(sequence + 1);
        }
        distanceList.set(sequence, distance);
        maxDepth = Math.max(maxDepth, distance);
    }

    public int getDiscoveryTime(int sequence) {
        return discoveryTimeList.getQuick(sequence);
    }

    private void setDiscoveryTime(int sequence, int discoveryTime) {
        if (sequence >= discoveryTimeList.size()) {
            discoveryTimeList.setSize(sequence + 1);
        }
        discoveryTimeList.set(sequence, discoveryTime);
    }

    public int getFinishTime(int sequence) {
        return finishTimeList.getQuick(sequence);
    }

    private void setFinishTime(int sequence, int finishTime) {
        if (sequence >= finishTimeList.size()) {
            finishTimeList.setSize(sequence + 1);
        }
        finishTimeList.set(sequence, finishTime);
    }

    public NodeStatus getNodeStatus(int nodeSequence) {
        if (!visitStarted.contains(nodeSequence)) {
            return NodeStatus.UNDISCOVERED;
        }
        if (visitEnded.contains(nodeSequence)) {
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
                visitEnded.add(nodeSequence);
                break;
            case PROCESSING:
                visitStarted.add(nodeSequence);
                break;
            case UNDISCOVERED:
                throw new UnsupportedOperationException("Can't reset to undiscovered");
            default:
                throw new UnsupportedOperationException("no support for: " + nodeStatus);
        }
    }

    public SequenceSet<?> getLeafNodes() {
        return leafNodes;
    }

    public SequenceSet<?> getIntermediateNodes() {
        SequenceSet intermediateNodes = new SequenceSet<>();
        intermediateNodes.or(visitEnded);
        intermediateNodes.andNot(leafNodes);

        return intermediateNodes;
    }

    public SequenceSet<?> getNodeIdsForDepth(int depth) {
        SequenceSet<?> nodeIdsForDepth = new SequenceSet<>();
        for (int i = 0; i < distanceList.size(); i++) {
            if (distanceList.get(i) == depth) {
                nodeIdsForDepth.add(i);
            }
        }
        return nodeIdsForDepth;
    }

    public int getSiblingGroupForSequence(int sequence) {
        return siblingGroupSequenceList.get(sequence);
    }

    public void setSiblingGroupForSequence(int sequence, int value) {
        siblingGroupSequenceList.set(sequence, value);
    }

}
