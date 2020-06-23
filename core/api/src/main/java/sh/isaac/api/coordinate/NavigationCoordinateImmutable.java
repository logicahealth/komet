package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
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
public final class NavigationCoordinateImmutable implements NavigationCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<NavigationCoordinateImmutable, NavigationCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 4;

    private final LogicCoordinateImmutable logicCoordinate;
    private final ImmutableIntSet digraphConceptNids;

    private NavigationCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.logicCoordinate = null;
        this.digraphConceptNids = null;
     }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    /**
     *
     * @param logicCoordinate
     * @param digraphConceptNids
     */
    private NavigationCoordinateImmutable(LogicCoordinateImmutable logicCoordinate,
                                          ImmutableIntSet digraphConceptNids) {
        this.logicCoordinate = logicCoordinate;
        this.digraphConceptNids = digraphConceptNids;
    }
    public static NavigationCoordinateImmutable make(LogicCoordinateImmutable logicCoordinate,
                                                     ImmutableIntSet digraphConceptNids) {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(logicCoordinate, digraphConceptNids),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeInferred() {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        Coordinates.Logic.ElPlusPlus(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeStated() {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        Coordinates.Logic.ElPlusPlus(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeInferred(LogicCoordinate logicCoordinate) {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        logicCoordinate.toLogicCoordinateImmutable(), IntSets.immutable.of(logicCoordinate.getInferredAssemblageNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static NavigationCoordinateImmutable makeStated(LogicCoordinate logicCoordinate) {
        return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(
                        logicCoordinate.toLogicCoordinateImmutable(), IntSets.immutable.of(logicCoordinate.getStatedAssemblageNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    private NavigationCoordinateImmutable(ByteArrayDataBuffer in, int objectMarshalVersion) {
        if (objectMarshalVersion < 4) {
            PremiseType.valueOf(in.getUTF());
        }
        this.logicCoordinate  = MarshalUtil.unmarshal(in);
        this.digraphConceptNids = IntSets.immutable.of(in.getNidArray());
    }

    public static NavigationCoordinateImmutable make(PremiseType premiseType,
                                                     LogicCoordinateImmutable logicCoordinate) {
        if (premiseType == PremiseType.INFERRED) {
            return makeInferred(logicCoordinate);
        }
         return makeStated(logicCoordinate);
     }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.logicCoordinate, out);
        out.putNidArray(this.digraphConceptNids.toArray());
    }

    @Unmarshaler
    public static NavigationCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case 1:
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new NavigationCoordinateImmutable(in, objectMarshalVersion),
                        digraphCoordinateImmutable -> digraphCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    public LogicCoordinateImmutable getLogicCoordinate() {
        return this.logicCoordinate;
    }

    @Override
    public ImmutableIntSet getNavigationConceptNids() {
        return this.digraphConceptNids;
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
        return getLogicCoordinate().equals(that.getLogicCoordinate()) &&
                getNavigationConceptNids().equals(that.getNavigationConceptNids());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLogicCoordinate(), getNavigationConceptNids());
    }

}
