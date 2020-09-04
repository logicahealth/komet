package sh.isaac.api.coordinate;

import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.util.NaturalOrder;

import java.util.UUID;

public class VertexSortNaturalOrder implements VertexSort, Marshalable {
    private static final int marshalVersion = 1;

    private static final UUID VERTEX_SORT_UUID = UUID.fromString("035a8679-0f77-4a2c-80c2-2495a8e2bf14");

    public static final VertexSortNaturalOrder SINGLETON = new VertexSortNaturalOrder();

    private VertexSortNaturalOrder() {
    }

    @Override
    public UUID getVertexSortUUID() {
        return VERTEX_SORT_UUID;
    }

    @Override
    public final int[] sortVertexes(int[] vertexConceptNids, ManifoldCoordinateImmutable manifold) {
        if (vertexConceptNids.length < 2) {
            // nothing to sort, skip creating the objects for sort.
            return vertexConceptNids;
        }
        final LanguageCoordinate languageCoordinate = manifold.getLanguageCoordinate();
        final StampFilter stampFilter = manifold.getViewStampFilter();
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
        if (obj == null) {
            return false;
        }
        return obj.getClass().equals(this.getClass());
    }

    @Override
    public String toString() {
        return getVertexSortName();
    }

    @Override
    public String getVertexSortName() {
        return "Natural sort order";
    }

    @Override
    public String getVertexLabel(int vertexConceptNid, LanguageCoordinate languageCoordinate, StampFilter stampFilter) {
        return languageCoordinate.getDescriptionText(vertexConceptNid, stampFilter).orElse(Get.conceptDescriptionText(vertexConceptNid));
    }

    @Unmarshaler
    public static VertexSortNaturalOrder make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                // Using a static method rather than a constructor eliminates the need for
                // a readResolve method, but allows the implementation to decide how
                // to handle special cases. This is the equivalent of readresolve, since it
                // returns an existing object always.
                return SINGLETON;
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        // No fields...
    }
}
