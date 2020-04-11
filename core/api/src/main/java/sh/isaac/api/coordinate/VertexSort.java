package sh.isaac.api.coordinate;

import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.UUID;

public interface VertexSort {

    /**
     *
     * @return a unique identifier for this sort method
     */
    UUID getVertexSortUUID();

    String getVertexSortName();

    String getVertexLabel(int vertexConceptNid, LanguageCoordinate languageCoordinate, StampFilter stampFilter);

    default String getVertexLabel(ConceptSpecification vertexConcept, LanguageCoordinate languageCoordinate, StampFilter stampFilter) {
        return getVertexLabel(vertexConcept.getNid(), languageCoordinate, stampFilter);
    }

    /**
     * Sort the vertex concept nids with respect to settings from the
     * digraphCoordinate where appropriate.
     * @param vertexConceptNids
     * @param digraph
     * @return sorted vertexConceptNids
     */
    int[] sortVertexes(int[] vertexConceptNids, DigraphCoordinateImmutable digraph);
}
