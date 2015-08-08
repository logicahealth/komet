/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.tree.hashtree;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.SequenceSet;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 * A {@code Tree} implemented using a {@code OpenIntObjectHashMap<int[]>}.
 *
 * @author kec
 */
public class HashTreeWithBitSets extends AbstractHashTree {
    final ConceptSequenceSet conceptSequencesWithParents;
    final ConceptSequenceSet conceptSequencesWithChildren;
    final ConceptSequenceSet conceptSequences;

    public HashTreeWithBitSets() {
        conceptSequencesWithParents = new ConceptSequenceSet();
        conceptSequencesWithChildren = new ConceptSequenceSet();
        conceptSequences = new ConceptSequenceSet();
    }

    public HashTreeWithBitSets(int initialSize) {
        conceptSequencesWithParents = new ConceptSequenceSet();
        conceptSequencesWithChildren = new ConceptSequenceSet();
        conceptSequences = new ConceptSequenceSet();
    }


    public void addChildren(int parentSequence, int[] childSequenceArray) {
        maxSequence = Math.max(parentSequence, maxSequence);
        if (childSequenceArray.length > 0) {
            if (!parentSequence_ChildSequenceArray_Map.containsKey(parentSequence)) {
                parentSequence_ChildSequenceArray_Map.put(parentSequence, childSequenceArray);
            } else {
                OpenIntHashSet combinedSet = new OpenIntHashSet();
                Arrays.stream(parentSequence_ChildSequenceArray_Map.get(parentSequence)).forEach((sequence) -> combinedSet.add(sequence));
                Arrays.stream(childSequenceArray).forEach((sequence) -> combinedSet.add(sequence));
                parentSequence_ChildSequenceArray_Map.put(parentSequence,combinedSet.keys().elements());
            }
            
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
            if (!childSequence_ParentSequenceArray_Map.containsKey(childSequence)) {
                childSequence_ParentSequenceArray_Map.put(childSequence, parentSequenceArray);
            } else {
                OpenIntHashSet combinedSet = new OpenIntHashSet();
                Arrays.stream(childSequence_ParentSequenceArray_Map.get(childSequence)).forEach((sequence) -> combinedSet.add(sequence));
                Arrays.stream(parentSequenceArray).forEach((sequence) -> combinedSet.add(sequence));
                childSequence_ParentSequenceArray_Map.put(childSequence,combinedSet.keys().elements());
            }

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
        return getRootSequenceStream().toArray();
    }

    @Override
    public IntStream getRootSequenceStream() {
        SequenceSet rootSet = new SequenceSet<>();
        rootSet.or(conceptSequencesWithChildren);
        rootSet.andNot(conceptSequencesWithParents);
        return rootSet.stream();
    }

    @Override
    public int size() {
        return getNodeSequences().size() + 1;
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public SequenceSet<?> getNodeSequences() {
        return conceptSequences;
    }

    public IntStream getLeafSequences() {
        SequenceSet leavesSet = new SequenceSet<>();
        leavesSet.or(conceptSequencesWithParents);
        leavesSet.andNot(conceptSequencesWithChildren);
        return leavesSet.stream();
    }

    public int conceptSequencesWithParentsCount() {
        return conceptSequencesWithParents.size();
    }

    public int conceptSequencesWithChildrenCount() {
        return conceptSequencesWithChildren.size();
    }
}
