package sh.isaac.api.coordinate;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.util.UUIDUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface StampFilter extends StampFilterTemplate, TimeBasedAnalogMaker<StampFilter>,
        StateBasedAnalogMaker<StampFilter> {

    /**
     * @return a content based uuid, such that identical stamp coordinates
     * will have identical uuids, and that different stamp coordinates will
     * always have different uuids.
     */

    default UUID getStampFilterUuid() {
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (Status status: getAllowedStates().toEnumSet()) {
            UUIDUtil.addSortedUuids(uuidList, status.getSpecifyingConcept().getNid());
        }
        UUIDUtil.addSortedUuids(uuidList, getStampPosition().getPathForPositionNid());
        UUIDUtil.addSortedUuids(uuidList, getModuleNids().toArray());
        UUIDUtil.addSortedUuids(uuidList, getModulePriorityOrder().toArray());
        StringBuilder b = new StringBuilder();
        b.append(uuidList.toString());
        b.append(getStampPosition().getTime());
        return UUID.nameUUIDFromBytes(b.toString().getBytes());
    }

    int getPathNidForFilter();

    default ConceptSpecification getPathConceptForFilter() {
        return Get.conceptSpecification(getPathNidForFilter());
    }

    /**
     * Create a new Filter ImmutableCoordinate identical to the this coordinate, but with the modules modified.
     * @param modules the new modules list.
     * supplied modules should replace the existing modules
     * @return the new path coordinate
     */
    StampFilter makeModuleAnalog(Collection<ConceptSpecification> modules);

    /**
     * Create a new Filter ImmutableCoordinate identical to the this coordinate, but with the path for position replaced.
     * @param pathForPosition the new path for position
     * @return the new path coordinate
     */
    StampFilter makePathAnalog(ConceptSpecification pathForPosition);

    /**
     * Gets the stamp position.
     *
     * @return the position (time on a path) that is used to
     * compute what stamped objects versions are the latest with respect to this
     * position.
     */
    StampPosition getStampPosition();

    /**
     * @return multi-line string output suitable for presentation to user, as opposed to use in debugging.
     */

    default String toUserString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("   allowed states: ");
        builder.append(this.getAllowedStates().toUserString());

        builder.append("\n   position: ")
                .append(this.getStampPosition().toUserString())
                .append("\n   modules: ");

        if (this.getModuleNids().isEmpty()) {
            builder.append("all ");
        } else {
            builder.append(Get.conceptDescriptionTextList(this.getModuleNids().toArray()))
                    .append(" ");
        }

        builder.append("\n   excluded modules: ");

        if (this.getExcludedModuleNids().isEmpty()) {
            builder.append("none ");
        } else {
            builder.append(Get.conceptDescriptionTextList(this.getExcludedModuleNids().toArray()))
                    .append(" ");
        }

        builder.append("\n   module priorities: ");
        if (this.getModulePriorityOrder().isEmpty()) {
            builder.append("none ");
        } else {
            builder.append(Get.conceptDescriptionTextList(this.getModulePriorityOrder().toArray()))
                    .append(" ");
        }

        return builder.toString();
    }

    default StampFilterImmutable toStampFilterImmutable() {
        return StampFilterImmutable.make(getAllowedStates(),
                getStampPosition());
    }

    default long getTime() {
        return getStampPosition().getTime();
    }

    RelativePositionCalculator getRelativePositionCalculator();

    default LatestVersion<Version> latestConceptVersion(int conceptNid) {
        try {
            return Get.concept(conceptNid).getLatestVersion(this);
        } catch (NoSuchElementException e) {
            return new LatestVersion<>();
        }
    }

}
