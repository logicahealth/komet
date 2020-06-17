package sh.isaac.api.coordinate;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import java.util.Objects;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public class ManifoldCoordinateImmutable implements ManifoldCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<ManifoldCoordinateImmutable, ManifoldCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 2;

    private final DigraphCoordinateImmutable digraphCoordinateImmutable;


    private ManifoldCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.digraphCoordinateImmutable = null;
    }
    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private ManifoldCoordinateImmutable(DigraphCoordinateImmutable digraphCoordinateImmutable) {
        this.digraphCoordinateImmutable = digraphCoordinateImmutable;
    }

    private ManifoldCoordinateImmutable(ByteArrayDataBuffer in, int objectMarshalVersion) {
        switch (objectMarshalVersion) {
            case 1:
                MarshalUtil.unmarshal(in); // this.vertexSort =
                this.digraphCoordinateImmutable = MarshalUtil.unmarshal(in);
                MarshalUtil.unmarshal(in); // this.stampFilterImmutable =
                break;
            case marshalVersion:
                this.digraphCoordinateImmutable = MarshalUtil.unmarshal(in);
                break;
            default:
                throw new IllegalStateException("Can't handle marshalVersion: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.digraphCoordinateImmutable, out);
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
            case 1:
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(in, objectMarshalVersion),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }
    public static ManifoldCoordinateImmutable make(DigraphCoordinate digraphCoordinate) {
         return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(digraphCoordinate.toDigraphImmutable()),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate) {
        DigraphCoordinateImmutable dci = DigraphCoordinateImmutable.makeStated(stampFilter, languageCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(dci),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        DigraphCoordinateImmutable dci = DigraphCoordinateImmutable.makeStated(stampFilter, languageCoordinate, logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(dci),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeInferred(StampFilter stampFilter, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        DigraphCoordinateImmutable dci = DigraphCoordinateImmutable.makeInferred(stampFilter, languageCoordinate, logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(dci),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
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
        return digraphCoordinateImmutable.equals(that.digraphCoordinateImmutable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(digraphCoordinateImmutable);
    }
}
