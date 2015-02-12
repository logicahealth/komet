package gov.vha.isaac.ochre.api.graph;

import java.util.OptionalDouble;
import java.util.stream.IntStream;

/**
 * Created by kec on 12/14/14.
 */
public class SimpleDirectedGraphVisitData extends GraphVisitData {

    private final SimpleDirectedGraph graph;

    public SimpleDirectedGraphVisitData(SimpleDirectedGraph graph) {
        super(graph.getMaxSequence() + 1);
        this.graph = graph;
    }


    public OptionalDouble getAverageDepth() {
        IntStream.Builder averageStreamBuilder = IntStream.builder();
        graph.getGraphNodeSequences().stream().forEach((int sequence) -> {
            averageStreamBuilder.add(distanceList.get(sequence));
        });
        return averageStreamBuilder.build().average();
    }

    @Override
    public String toString() {
        return "GraphVisitData{" + "time=" + getTime() + ", nodesVisited=" + getNodesVisited() + ", leafNodes=" +
                getLeafNodes().cardinality() + ", intermediateNodes=" + getIntermediateNodes().cardinality() + '}';
    }

}
