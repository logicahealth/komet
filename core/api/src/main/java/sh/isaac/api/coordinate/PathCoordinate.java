package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UUIDUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public interface PathCoordinate {

    /**
     *
     * @return the concept representing the path for this coordinate.
     */
    ConceptSpecification getPathConceptForCoordinate();

    int getPathNidForCoordinate();

    /**
     *
     * @return a content based uuid, such that identical stamp coordinates
     * will have identical uuids, and that different stamp coordinates will
     * always have different uuids.
     */
    default UUID getPathCoordinateUuid() {
        ArrayList<UUID> uuidList = new ArrayList<>();
        UUIDUtil.addSortedUuids(uuidList, getPathConceptForCoordinate().getNid());
        UUIDUtil.addSortedUuids(uuidList, getModuleNids().toArray());
        StringBuilder b = new StringBuilder();
        b.append(uuidList.toString());
        return UUID.nameUUIDFromBytes(b.toString().getBytes());
    }

    /**
     * An empty array is a wild-card, and should match all modules. If there are
     * one or more module nids specified, only those modules will be included
     * in the results.
     * @return an unmodifiable set of module nids to include in results based on this
     * stamp coordinate.
     */
    ImmutableIntSet getModuleNids();

    /**
     * An empty list is a wild-card, and should match all modules. If there are
     * one or more modules specified, only those modules will be included
     * in the results.
     * @return an unmodifiable set of modules to include in results based on this
     * stamp coordinate.
     */
    ImmutableSet<ConceptSpecification> getModuleSpecifications();


    /**
     * Create a new Filter ImmutableCoordinate identical to the this coordinate, but with the modules modified.
     * @param modules the new modules list.
     * supplied modules should replace the existing modules
     * @return the new path coordinate
     */
    PathCoordinate makeModuleAnalog(Collection<ConceptSpecification> modules);

    /**
     * Create a new Filter ImmutableCoordinate identical to the this coordinate, but with the path for position replaced.
     * @param pathForPosition the new path for position
     * @return the new path coordinate
     */
    PathCoordinate makePathAnalog(ConceptSpecification pathForPosition);

    PathCoordinateImmutable toPathCoordinateImmutable();
    /**
     *
     * @return a StampFilterImmutable representing the latest on this path, with no author constraints.
     */
    default StampFilterImmutable getStampFilter() {
        return PathCoordinateImmutable.getStampFilter(this);
    }

    String toUserString();

    default ConceptSpecification getPathForCoordinate() {
        return Get.conceptSpecification(getPathNidForCoordinate());
    }
}
