package sh.isaac.api.coordinate;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Objects;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class StampPathImmutable implements StampPath, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<Integer, StampPathImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;

    private final int pathConceptNid;

    private final ImmutableSet<StampPositionImmutable> pathOrigins;

    private StampPathImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.pathConceptNid = Integer.MAX_VALUE;
        this.pathOrigins = null;
    }
    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private StampPathImmutable(int pathConceptNid, ImmutableSet<StampPositionImmutable> pathOrigins) {
        this.pathConceptNid = pathConceptNid;
        this.pathOrigins = pathOrigins;
    }

    private StampPathImmutable(ByteArrayDataBuffer in) {
        this.pathConceptNid = in.getNid();
        int pathOriginsSize = in.getInt();
        MutableSet<StampPositionImmutable> mutableOrigins = Sets.mutable.ofInitialCapacity(pathOriginsSize);
        for (int i = 0; i < pathOriginsSize; i++) {
            mutableOrigins.add(StampPositionImmutable.make(in));
        }
        this.pathOrigins = mutableOrigins.toImmutable();
    }

    public static StampPathImmutable make(ConceptSpecification pathConcept, ImmutableSet<StampPositionImmutable> pathOrigins) {
        return make(pathConcept.getNid(), pathOrigins);
    }

    public static StampPathImmutable make(int pathConceptNid, ImmutableSet<StampPositionImmutable> pathOrigins) {
        return SINGLETONS.computeIfAbsent(pathConceptNid,
                pathNid -> new StampPathImmutable(pathConceptNid, pathOrigins));
    }


    public static StampPathImmutable make(ConceptSpecification pathConcept) {
        return make(pathConcept.getNid());
    }
    public static StampPathImmutable make(int pathConceptNid) {
        return SINGLETONS.computeIfAbsent(pathConceptNid,
                pathNid -> {
                    ImmutableSet<StampPositionImmutable> pathOrigins = Get.versionManagmentPathService().getOrigins(pathNid);
                    return new StampPathImmutable(pathNid, pathOrigins);
                });
    }

    @Unmarshaler
    public static StampPathImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                StampPathImmutable stampPath = new StampPathImmutable(in);
                return SINGLETONS.computeIfAbsent(stampPath.getPathConceptNid(),
                        pathNid -> stampPath);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.pathConceptNid);
        out.putInt(this.pathOrigins.size());
        for (StampPositionImmutable stampPosition: this.pathOrigins) {
            stampPosition.marshal(out);
        }
    }

    public StampPathImmutable toStampPathImmutable() {
        return this;
    }

    @Override
    public int getPathConceptNid() {
        return this.pathConceptNid;
    }

    @Override
    public ImmutableSet<StampPositionImmutable> getPathOrigins() {
        return this.pathOrigins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StampPathImmutable)) return false;
        StampPathImmutable that = (StampPathImmutable) o;
        return getPathConceptNid() == that.getPathConceptNid() &&
                getPathOrigins().equals(that.getPathOrigins());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPathConceptNid(), getPathOrigins());
    }


    public static final StampFilterImmutable getStampFilter(StampPath stampPath) {
        return StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE,
                StampPositionImmutable.make(Long.MAX_VALUE, stampPath.getPathConcept()),
                IntSets.immutable.empty());
    }
}
