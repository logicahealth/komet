/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.tree.hashtree;

import gov.vha.isaac.ochre.collections.SequenceSet;
import java.util.stream.IntStream;

/**
 * A {@code Tree} implemented using a {@code OpenIntObjectHashMap<int[]>}.
 *
 * @author kec
 */
public class HashTreeWithBitSets extends AbstractHashTree {
    final SequenceSet conceptSequencesWithParents;
    final SequenceSet conceptSequencesWithChildren;
    final SequenceSet conceptSequences;

    public HashTreeWithBitSets() {
        conceptSequencesWithParents = new SequenceSet();
        conceptSequencesWithChildren = new SequenceSet();
        conceptSequences = new SequenceSet();
    }

    public HashTreeWithBitSets(int initialSize) {
        conceptSequencesWithParents = new SequenceSet();
        conceptSequencesWithChildren = new SequenceSet();
        conceptSequences = new SequenceSet();
    }


    public void addChildren(int parentSequence, int[] childSequenceArray) {
        maxSequence = Math.max(parentSequence, maxSequence);
        if (childSequenceArray.length > 0) {
            parentSequence_ChildSequenceArray_Map.put(parentSequence, childSequenceArray);
            IntStream.of(childSequenceArray).forEach((int sequence) -> {
                conceptSequences.add(sequence);
            });
            maxSequence = Math.max(IntStream.of(childSequenceArray).max().getAsInt(), maxSequence);
            conceptSequencesWithChildren.add(parentSequence);
        }

    }

    public void addParents(int childSequence, int[] parentSequenceArray) {
        maxSequence = Math.max(childSequence, maxSequence);
        if (parentSequenceArray.length > 0) {
            childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
            IntStream.of(parentSequenceArray).forEach((int sequence) -> {
                conceptSequences.add(sequence);
            });
            maxSequence = Math.max(IntStream.of(parentSequenceArray).max().getAsInt(), maxSequence);
            conceptSequencesWithParents.add(childSequence);
        }
    }


    @Override
    public int[] getRootSequences() {
        SequenceSet rootSet = new SequenceSet();
        rootSet.or(conceptSequencesWithChildren);
        rootSet.andNot(conceptSequencesWithParents);
        return rootSet.stream().toArray();
    }

    @Override
    public int size() {
        return getNodeSequences().size() + 1;
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public SequenceSet getNodeSequences() {
        return conceptSequences;
    }

    public int[] getLeafSequences() {
        SequenceSet leavesSet = new SequenceSet();
        leavesSet.or(conceptSequencesWithParents);
        leavesSet.andNot(conceptSequencesWithChildren);
        return leavesSet.stream().toArray();
    }

    public int conceptSequencesWithParentsCount() {
        return conceptSequencesWithParents.size();
    }

    public int conceptSequencesWithChildrenCount() {
        return conceptSequencesWithChildren.size();
    }
}
