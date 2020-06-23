package sh.isaac.api.coordinate;

import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.util.Hashcode;
import sh.isaac.api.util.NaturalOrder;


public abstract class VertexSortAbstract implements VertexSort {


    @Override
    public final int[] sortVertexes(int[] vertexConceptNids, ManifoldCoordinateImmutable manifold) {
        if (vertexConceptNids.length < 2) {
            // nothing to sort, skip creating the objects for sort.
            return vertexConceptNids;
        }
        final LanguageCoordinate languageCoordinate = manifold.getLanguageCoordinate();
        final StampFilter stampFilter = manifold.getLanguageStampFilter();
        return IntLists.immutable.of(vertexConceptNids).primitiveStream().mapToObj(vertexConceptNid ->
                new VertexItem(vertexConceptNid, getVertexLabel(vertexConceptNid, languageCoordinate, stampFilter)))
                .sorted().mapToInt(value -> value.nid).toArray();
    }

    private static class VertexItem implements Comparable<VertexItem> {
        private final int nid;
        private final String description;

        public VertexItem(int nid, String description) {
            this.nid = nid;
            this.description = description;
        }

        @Override
        public int compareTo(VertexItem o) {
            return NaturalOrder.compareStrings(this.description, o.description);
        }
    }

    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(this.getClass());
    }

    @Override
    public String toString() {
        return getVertexSortName();
    }

}
