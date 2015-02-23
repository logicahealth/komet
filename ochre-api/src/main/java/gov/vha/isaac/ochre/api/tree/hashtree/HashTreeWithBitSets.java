/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.tree.hashtree;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * A {@code Tree} implemented using a {@code OpenIntObjectHashMap<int[]>}.
 *
 * @author kec
 */
public class HashTreeWithBitSets extends AbstractHashTree {
    final BitSet conceptSequencesWithParents;
    final BitSet conceptSequencesWithChildren;
    final BitSet conceptSequences;

    public HashTreeWithBitSets() {
        conceptSequencesWithParents = new BitSet();
        conceptSequencesWithChildren = new BitSet();
        conceptSequences = new BitSet();
    }

    public HashTreeWithBitSets(int initialSize) {
        conceptSequencesWithParents = new BitSet(initialSize);
        conceptSequencesWithChildren = new BitSet(initialSize);
        conceptSequences = new BitSet(initialSize);
    }


    public void addChildren(int parentSequence, int[] childSequenceArray) {
        maxSequence = Math.max(parentSequence, maxSequence);
        if (childSequenceArray.length > 0) {
            parentSequence_ChildSequenceArray_Map.put(parentSequence, childSequenceArray);
            IntStream.of(childSequenceArray).forEach((int sequence) -> {
                conceptSequences.set(sequence);
            });
            maxSequence = Math.max(IntStream.of(childSequenceArray).max().getAsInt(), maxSequence);
            conceptSequencesWithChildren.set(parentSequence);
        }

    }

    public void addParents(int childSequence, int[] parentSequenceArray) {
        maxSequence = Math.max(childSequence, maxSequence);
        if (parentSequenceArray.length > 0) {
            childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
            IntStream.of(parentSequenceArray).forEach((int sequence) -> {
                conceptSequences.set(sequence);
            });
            maxSequence = Math.max(IntStream.of(parentSequenceArray).max().getAsInt(), maxSequence);
            conceptSequencesWithParents.set(childSequence);
        }
    }


    @Override
    public int[] getRootSequences() {
        BitSet rootSet = new BitSet(conceptSequencesWithParents.size());
        rootSet.or(conceptSequencesWithChildren);
        rootSet.andNot(conceptSequencesWithParents);
        return rootSet.stream().toArray();
    }

    @Override
    public int size() {
        return getNodeSequences().cardinality() + 1;
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public BitSet getNodeSequences() {
        return conceptSequences;
    }

    public int[] getLeafSequences() {
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



}
