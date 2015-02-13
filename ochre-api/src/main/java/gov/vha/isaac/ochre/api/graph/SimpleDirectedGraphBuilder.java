/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.graph;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

/**
 *
 * @author kec
 */
public class SimpleDirectedGraphBuilder {
    private static final AtomicInteger builderCount = new AtomicInteger();
    final OpenIntObjectHashMap<IntStream.Builder> childSequence_ParentSequenceStream_Map = new OpenIntObjectHashMap<>();
    final OpenIntObjectHashMap<IntStream.Builder> parentSequence_ChildSequenceStream_Map = new OpenIntObjectHashMap<>();
    final int builderId;

    public SimpleDirectedGraphBuilder() {
        this.builderId = builderCount.getAndIncrement();
    }

    public void add(int parent, int child) {
        if (!childSequence_ParentSequenceStream_Map.containsKey(child)) {
            childSequence_ParentSequenceStream_Map.put(child, IntStream.builder());
        }
        childSequence_ParentSequenceStream_Map.get(child).add(parent);
        if (!parentSequence_ChildSequenceStream_Map.containsKey(parent)) {
            parentSequence_ChildSequenceStream_Map.put(parent, IntStream.builder());
        }
        parentSequence_ChildSequenceStream_Map.get(parent).add(child);
    }

    public void combine(SimpleDirectedGraphBuilder another) {
        
        another.childSequence_ParentSequenceStream_Map.forEachPair((int childSequence, IntStream.Builder parentsFromAnother) -> {
            if (childSequence_ParentSequenceStream_Map.containsKey(childSequence)) {
                IntStream.Builder parentsStream = childSequence_ParentSequenceStream_Map.get(childSequence);
                parentsFromAnother.build().forEach((int c) -> parentsStream.add(c));
            } else {
                childSequence_ParentSequenceStream_Map.put(childSequence, parentsFromAnother);
            }
            return true;
        });

        another.parentSequence_ChildSequenceStream_Map.forEachPair((int parentSequence, IntStream.Builder childrenFromAnother) -> {
            if (parentSequence_ChildSequenceStream_Map.containsKey(parentSequence)) {
                IntStream.Builder childrenStream = parentSequence_ChildSequenceStream_Map.get(parentSequence);
                childrenFromAnother.build().forEach((int p) -> childrenStream.add(p));
            } else {
                parentSequence_ChildSequenceStream_Map.put(parentSequence, childrenFromAnother);
            }
            return true;
        });
    }

    public SimpleDirectedGraph getSimpleDirectedGraphGraph() {
        SimpleDirectedGraph graph = new SimpleDirectedGraph(childSequence_ParentSequenceStream_Map.size());
        childSequence_ParentSequenceStream_Map.forEachPair((int childSequence, IntStream.Builder parentSequenceStreamBuilder) -> {
            int[] parentSequenceArray = parentSequenceStreamBuilder.build().distinct().toArray();
            if (parentSequenceArray.length > 0) {
                graph.addParents(childSequence, parentSequenceArray);
            }

            return true;
        });
        parentSequence_ChildSequenceStream_Map.forEachPair((int parentSequence, IntStream.Builder childSequenceStreamBuilder) -> {
            int[] childSequenceArray = childSequenceStreamBuilder.build().distinct().toArray();
            if (childSequenceArray.length > 0) {
                graph.addChildren(parentSequence, childSequenceArray);
            }

            return true;
        });

        return graph;
    }

}
