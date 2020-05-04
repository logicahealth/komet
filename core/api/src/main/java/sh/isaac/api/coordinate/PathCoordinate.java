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
        StringBuilder b = new StringBuilder();
        b.append(uuidList.toString());
        return UUID.nameUUIDFromBytes(b.toString().getBytes());
    }


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

    ImmutableSet<StampPositionImmutable> getPathOrigins();


}
