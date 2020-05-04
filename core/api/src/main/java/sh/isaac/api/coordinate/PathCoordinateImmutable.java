package sh.isaac.api.coordinate;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class PathCoordinateImmutable implements PathCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<PathCoordinateImmutable, PathCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 2;

    public static final StampFilterImmutable getStampFilter(PathCoordinate pathCoordinate) {
        return StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE,
                StampPositionImmutable.make(Long.MAX_VALUE, pathCoordinate.getPathConceptForCoordinate()),
                IntSets.immutable.empty());
    }

    private final int pathConceptNid;

    private final ImmutableSet<StampPositionImmutable> pathOrigins;

    private final UUID pathCoordinateUuid;

    private transient final StampFilterImmutable pathStampFilter;


    private PathCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.pathConceptNid = Integer.MAX_VALUE;
        this.pathCoordinateUuid = null;
        this.pathStampFilter = null;
        this.pathOrigins = null;
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
     * @param pathConceptNid the concept that identifies this path
     */
    private PathCoordinateImmutable(int pathConceptNid,
                                    ImmutableSet<StampPositionImmutable> pathOrigins) {
        this.pathConceptNid = pathConceptNid;
        this.pathCoordinateUuid = PathCoordinate.super.getPathCoordinateUuid();
        this.pathStampFilter = getStampFilter(this);
        this.pathOrigins = pathOrigins;
    }


    private PathCoordinateImmutable(ByteArrayDataBuffer data, int objectMarshalVersion) {
        this.pathConceptNid = data.getNid();
        switch (objectMarshalVersion) {
            case 1:
                this.pathOrigins = Sets.immutable.empty();
                break;
            case marshalVersion:
                int setSize = data.getInt();
                MutableSet<StampPositionImmutable> mutableOrigins = Sets.mutable.ofInitialCapacity(setSize);
                for (int i = 0; i < setSize; i++) {
                    mutableOrigins.add(StampPositionImmutable.make(data));
                }
                this.pathOrigins = mutableOrigins.toImmutable();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
        this.pathCoordinateUuid = PathCoordinate.super.getPathCoordinateUuid();
        this.pathStampFilter = getStampFilter(this);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.pathConceptNid);
        out.putInt(this.pathOrigins.size());
        for (StampPositionImmutable pathOrigin: this.pathOrigins) {
            pathOrigin.marshal(out);
        }
    }

    @Unmarshaler
    public static PathCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(in, objectMarshalVersion),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }

    /**
     * @param pathConceptNid the concept that identifies this path
     */
    public static PathCoordinateImmutable make(int pathConceptNid) {
        ImmutableSet<StampPositionImmutable> origins = Get.versionManagmentPathService().getOrigins(pathConceptNid);
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConceptNid,
                        origins),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }

    /**
     * @param pathConcept the concept that identifies this path
     */
    public static PathCoordinateImmutable make(ConceptSpecification pathConcept) {
        ImmutableSet<StampPositionImmutable> origins = Get.versionManagmentPathService().getOrigins(pathConcept.getNid());
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConcept.getNid(),
                        origins),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }
    public static PathCoordinateImmutable make(int pathConceptNid,
                                               ImmutableSet<StampPositionImmutable> origins) {
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConceptNid, origins),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }


    public static PathCoordinateImmutable make(ConceptSpecification pathConcept,
                                               ImmutableSet<StampPositionImmutable> origins) {
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConcept.getNid(), origins),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }

    public UUID getPathCoordinateUuid() {
        return this.pathCoordinateUuid;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof PathCoordinate)) {
            return false;
        }

        final PathCoordinate other = (PathCoordinate) obj;

        if (this.pathConceptNid != other.getPathNidForCoordinate()) {
            return false;
        }
        return this.getPathOrigins().equals(other.getPathOrigins());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 11 * hash + Integer.hashCode(this.pathConceptNid);
        for (StampPositionImmutable origin: this.getPathOrigins().toList()) {
            hash = 11 * hash + origin.hashCode();
        }
        return hash;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Path ImmutableCoordinate{").append(Get.conceptDescriptionText(this.pathConceptNid))
                .append(", Origins: ");

        if (this.pathOrigins.isEmpty()) {
            builder.append("none ");
        } else {
            AtomicInteger count = new AtomicInteger(0);
            for (StampPositionImmutable origin: this.pathOrigins) {
                builder.append(origin);
                if (count.getAndIncrement() < this.pathOrigins.size()) {
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    public String toUserString() {
        return toString();
    }

    @Override
    public ConceptSpecification getPathConceptForCoordinate() {
        return Get.conceptSpecification(this.pathConceptNid);
    }

    @Override
    public int getPathNidForCoordinate() {
        return this.pathConceptNid;
    }

    @Override
    public PathCoordinateImmutable toPathCoordinateImmutable() {
        return this;
    }

    @Override
    public final StampFilterImmutable getStampFilter() {
        return this.pathStampFilter;
    }

    @Override
    public ImmutableSet<StampPositionImmutable> getPathOrigins() {
        return pathOrigins;
    }
}
