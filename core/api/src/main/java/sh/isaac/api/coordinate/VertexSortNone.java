package sh.isaac.api.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import java.util.UUID;

public class VertexSortNone implements VertexSort, Marshalable {

    private static final int marshalVersion = 1;

    private static final UUID VERTEX_SORT_UUID = UUID.fromString("9e21329f-da07-4a15-8664-7a08ebdad987");

    public static final VertexSortNone SINGLETON = new VertexSortNone();

    private VertexSortNone() {
    }

    @Override
    public UUID getVertexSortUUID() {
        return VERTEX_SORT_UUID;
    }

    @Override
    public String getVertexSortName() {
        return "No sort order";
    }

    @Override
    public String getVertexLabel(int vertexConceptNid, LanguageCoordinate languageCoordinate, StampFilter stampFilter) {
        return languageCoordinate.getDescriptionText(vertexConceptNid, stampFilter).orElse(Get.conceptDescriptionText(vertexConceptNid));
    }

    @Override
    public int[] sortVertexes(int[] vertexConceptNids, ManifoldCoordinateImmutable manifold) {
        return vertexConceptNids;
    }

    @Unmarshaler
    public static VertexSortNone make(ByteArrayDataBuffer in) {
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
