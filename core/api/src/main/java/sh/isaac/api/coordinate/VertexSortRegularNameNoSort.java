package sh.isaac.api.coordinate;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import java.util.UUID;

import static sh.isaac.api.coordinate.VertexSortRegularName.getRegularName;

public class VertexSortRegularNameNoSort implements VertexSort, Marshalable {

    private static final int marshalVersion = 1;

    private static final UUID VERTEX_SORT_UUID = UUID.fromString("25b8e3b4-6efc-11ea-bc55-0242ac130003");

    public static final VertexSortRegularNameNoSort SINGLETON = new VertexSortRegularNameNoSort();

    private VertexSortRegularNameNoSort() {
    }

    @Override
    public UUID getVertexSortUUID() {
        return VERTEX_SORT_UUID;
    }

    @Override
    public String getVertexSortName() {
        return "Regular name, no sort";
    }

    @Override
    public String getVertexLabel(int vertexConceptNid, LanguageCoordinate languageCoordinate, StampFilter stampFilter) {
        return getRegularName(vertexConceptNid, languageCoordinate, stampFilter);
    }

    @Override
    public int[] sortVertexes(int[] vertexConceptNids, DigraphCoordinateImmutable digraph) {
        return vertexConceptNids;
    }

    @Unmarshaler
    public static VertexSortRegularNameNoSort make(ByteArrayDataBuffer in) {
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
