package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class PathCoordinateImmutable implements PathCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<PathCoordinateImmutable, PathCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;

    public static final StampFilterImmutable getStampFilter(PathCoordinate pathCoordinate) {
        return StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE,
                StampPositionImmutable.make(Long.MAX_VALUE, pathCoordinate.getPathConceptForCoordinate()),
                IntSets.immutable.empty());
    }

    private final int pathConceptNid;

    private final ImmutableIntSet moduleNids;

    private final UUID pathCoordinateUuid;

    private transient final StampFilterImmutable pathStampFilter;


    private PathCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.pathConceptNid = Integer.MAX_VALUE;
        this.moduleNids = null;
        this.pathCoordinateUuid = null;
        this.pathStampFilter = null;
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
     * @param moduleNids the module nids to include in the version
     * computation.  If empty, all modules are allowed.
     */
    private PathCoordinateImmutable(int pathConceptNid,
                                   ImmutableIntSet moduleNids) {
        this.pathConceptNid = pathConceptNid;
        this.moduleNids = moduleNids;
        this.pathCoordinateUuid = PathCoordinate.super.getPathCoordinateUuid();
        this.pathStampFilter = getStampFilter(this);
    }


    private PathCoordinateImmutable(ByteArrayDataBuffer data) {
        this.pathConceptNid = data.getNid();
        this.moduleNids = IntSets.immutable.of(data.getNidArray());
        this.pathCoordinateUuid = PathCoordinate.super.getPathCoordinateUuid();
        this.pathStampFilter = getStampFilter(this);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.pathConceptNid);
        out.putNidArray(moduleNids.toArray());
    }

    @Unmarshaler
    public static PathCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(in),
                        pathCoordinateImmutable -> pathCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    /**
     * The module set will be empty (include all modules)
     * The module priority list will be empty (no module priority)
     * @param pathConceptNid the concept that identifies this path
     */
    public static PathCoordinateImmutable make(int pathConceptNid) {
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConceptNid, IntSets.immutable.empty()),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }

    /**
     * The module set will be empty (include all modules)
     * The module priority list will be empty (no module priority)
     * @param pathConcept the concept that identifies this path
     */
    public static PathCoordinateImmutable make(ConceptSpecification pathConcept) {
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConcept.getNid(), IntSets.immutable.empty()),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }
    /**
     * The module priority list will be empty (no module priority)
     * @param pathConceptNid the concept that identifies this path
     * @param moduleNids the module nids to include in the version
     * computation.  If empty, all modules are allowed.
     */
    public static PathCoordinateImmutable make(int pathConceptNid,
                                    ImmutableIntSet moduleNids) {
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConceptNid, moduleNids),
                pathCoordinateImmutable -> pathCoordinateImmutable);
    }
    /**
     * Instantiates a new stamp coordinate impl.
     * @param pathConcept the concept that identifies this path
     * @param moduleSpecifications the module nids to include in the version
     * computation.  If empty, all modules are allowed.
     * priority order that should be used if a version computation returns two
     * different versions for different modules.
     */
    public static PathCoordinateImmutable make(ConceptSpecification pathConcept,
                                   Collection<ConceptSpecification> moduleSpecifications) {
        return SINGLETONS.computeIfAbsent(new PathCoordinateImmutable(pathConcept.getNid(),
                IntSets.immutable.ofAll(moduleSpecifications.stream().mapToInt(conceptSpecification -> conceptSpecification.getNid()))),
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

        return this.moduleNids.equals(other.getModuleSpecifications());
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public ImmutableSet<ConceptSpecification> getModuleSpecifications() {
        return moduleNids.collect(nid -> Get.conceptSpecification(nid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 11 * hash + Integer.hashCode(this.pathConceptNid);
        hash = 11 * hash + Objects.hashCode(this.moduleNids);
        return hash;
    }



    @Override
    public PathCoordinateImmutable makeModuleAnalog(Collection<ConceptSpecification> modules) {
        return new PathCoordinateImmutable(this.pathConceptNid,
                IntSets.immutable.ofAll(modules.stream().mapToInt(conceptSpecification -> conceptSpecification.getNid())));
    }


    @Override
    public PathCoordinateImmutable makePathAnalog(ConceptSpecification pathConcept) {
         return new PathCoordinateImmutable(pathConcept.getNid(), this.moduleNids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Path ImmutableCoordinate{").append(Get.conceptDescriptionText(this.pathConceptNid))
                .append(", modules: ");

        if (this.moduleNids.isEmpty()) {
            builder.append("all, ");
        } else {
            builder.append(Get.conceptDescriptionTextList(this.moduleNids.toArray()))
                    .append(", ");
        }
        return builder.toString();
    }

    public String toUserString() {
        return toString();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public ImmutableIntSet getModuleNids() {
        return this.moduleNids;
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
}
