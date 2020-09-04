package sh.isaac.api.coordinate;

import java.util.Objects;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

@Service
//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
public final class NavigationCoordinateImmutable implements NavigationCoordinate, ImmutableCoordinate, StaticIsaacCache {

    private static final ConcurrentReferenceHashMap<NavigationCoordinateImmutable, NavigationCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 5;

    private final ImmutableIntSet navigationConceptNids;

    private NavigationCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.navigationConceptNids = null;
     }

    @Override
    public void reset() {
        SINGLETONS.clear();
    }

    /**
     *
     * @param navigationConceptNids
     */
    private NavigationCoordinateImmutable(ImmutableIntSet navigationConceptNids) {
        this.navigationConceptNids = navigationConceptNids;
    }
    public static NavigationCoordinateImmutable make(ImmutableIntSet digraphConceptNids) {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(digraphConceptNids),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeInferred() {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        IntSets.immutable.of(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeStated() {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        IntSets.immutable.of(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeInferred(LogicCoordinate logicCoordinate) {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        IntSets.immutable.of(logicCoordinate.getInferredAssemblageNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeStated(LogicCoordinate logicCoordinate) {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        IntSets.immutable.of(logicCoordinate.getStatedAssemblageNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    private NavigationCoordinateImmutable(ByteArrayDataBuffer in, int objectMarshalVersion) {
        if (objectMarshalVersion < 4) {
            PremiseType.valueOf(in.getUTF());
        }
        if (objectMarshalVersion < 5) {
            // logicCoordinate
            MarshalUtil.unmarshal(in);
        }
        this.navigationConceptNids = IntSets.immutable.of(in.getNidArray());
    }

    public static NavigationCoordinateImmutable make(PremiseType premiseType) {
        if (premiseType == PremiseType.INFERRED) {
            return makeInferred(Coordinates.Logic.ElPlusPlus());
        }
         return makeStated(Coordinates.Logic.ElPlusPlus());
     }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNidArray(this.navigationConceptNids.toArray());
    }

    @Unmarshaler
    public static NavigationCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case 1:
            case 4:
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(in, objectMarshalVersion),
                        digraphCoordinateImmutable -> digraphCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    public ImmutableIntSet getNavigationConceptNids() {
        return this.navigationConceptNids;
    }

    @Override
    public NavigationCoordinateImmutable toNavigationCoordinateImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NavigationCoordinateImmutable)) return false;
        NavigationCoordinateImmutable that = (NavigationCoordinateImmutable) o;
        return getNavigationConceptNids().equals(that.getNavigationConceptNids());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNavigationConceptNids());
    }

    @Override
    public String toString() {
        return "NavigationCoordinateImmutable{" +
                "navigationConcepts=" + navigationConceptNids +
                '}';
    }


}
