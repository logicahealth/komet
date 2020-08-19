package sh.isaac.api.coordinate;

import java.util.Objects;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
@Service
public class ManifoldCoordinateImmutable implements ManifoldCoordinate, ImmutableCoordinate, StaticIsaacCache{

    private static final ConcurrentReferenceHashMap<ManifoldCoordinateImmutable, ManifoldCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static final VertexSort DEFAULT_DISPLAY_AND_SORT = VertexSortRegularName.SINGLETON;
    public static final VertexSort DISPLAY_PREFERRED_AND_SORT = VertexSortRegularName.SINGLETON;
    public static final VertexSort DISPLAY_PREFERRED_AND_NO_SORT = VertexSortRegularNameNoSort.SINGLETON;
    public static final VertexSort DISPLAY_FQN_AND_SORT =  VertexSortFullyQualifiedName.SINGLETON;
    public static final VertexSort DISPLAY_FQN_AND_NO_SORT =VertexSortFullyQualifiedNameNoSort.SINGLETON;

    private static final int marshalVersion = 1;

    private final VertexSort vertexSort;
    private final DigraphCoordinateImmutable digraphCoordinateImmutable;
    private final StampFilterImmutable stampFilterImmutable;


    private ManifoldCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.vertexSort = null;
        this.digraphCoordinateImmutable = null;
        this.stampFilterImmutable = null;
    }
    
    @Override
    public void reset() {
        SINGLETONS.clear();
    }

    private ManifoldCoordinateImmutable(VertexSort vertexSort, DigraphCoordinateImmutable digraphCoordinateImmutable, StampFilterImmutable stampFilterImmutable) {
        this.vertexSort = vertexSort;
        this.digraphCoordinateImmutable = digraphCoordinateImmutable;
        this.stampFilterImmutable = stampFilterImmutable;
    }

    private ManifoldCoordinateImmutable(ByteArrayDataBuffer in) {
        this.vertexSort = MarshalUtil.unmarshal(in);
        this.digraphCoordinateImmutable = MarshalUtil.unmarshal(in);
        this.stampFilterImmutable = MarshalUtil.unmarshal(in);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.vertexSort, out);
        MarshalUtil.marshal(this.digraphCoordinateImmutable, out);
        MarshalUtil.marshal(this.stampFilterImmutable, out);
    }

    @Override
    public VertexSort getVertexSort() {
        return vertexSort;
    }

    @Override
    public DigraphCoordinate getDigraph() {
        return this.digraphCoordinateImmutable;
    }

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return getDigraph().getLogicCoordinate();
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return getDigraph().getLanguageCoordinate();
    }

// Using a static method rather than a constructor eliminates the need for
    // a readResolve method, but allows the implementation to decide how
    // to handle special cases.

    @Unmarshaler
    public static ManifoldCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(in),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }
    public static ManifoldCoordinateImmutable make(VertexSort vertexSort,
                                                   DigraphCoordinate digraphCoordinate,
                                                   StampFilter stampFilter) {
         return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(vertexSort,
                         digraphCoordinate.toDigraphImmutable(), stampFilter.toStampFilterImmutable()),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate) {
        DigraphCoordinateImmutable dci = DigraphCoordinateImmutable.makeStated(stampFilter, languageCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(VertexSortFullyQualifiedName.SINGLETON, dci, stampFilter.toStampFilterImmutable()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);


    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        DigraphCoordinateImmutable dci = DigraphCoordinateImmutable.makeStated(stampFilter, languageCoordinate, logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(VertexSortFullyQualifiedName.SINGLETON, dci, stampFilter.toStampFilterImmutable()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);


    }

    public static ManifoldCoordinateImmutable makeInferred(StampFilter stampFilter, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        DigraphCoordinateImmutable dci = DigraphCoordinateImmutable.makeInferred(stampFilter, languageCoordinate, logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(VertexSortFullyQualifiedName.SINGLETON, dci, stampFilter.toStampFilterImmutable()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);


    }

    @Override
    public StampFilterImmutable getStampFilter() {
        return this.stampFilterImmutable;
    }

    @Override
    public ManifoldCoordinateImmutable toManifoldCoordinateImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManifoldCoordinateImmutable)) return false;
        ManifoldCoordinateImmutable that = (ManifoldCoordinateImmutable) o;
        return getVertexSort().equals(that.getVertexSort()) &&
                digraphCoordinateImmutable.equals(that.digraphCoordinateImmutable) &&
                stampFilterImmutable.equals(that.stampFilterImmutable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVertexSort(), digraphCoordinateImmutable, stampFilterImmutable);
    }
}
