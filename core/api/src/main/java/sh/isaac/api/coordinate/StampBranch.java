package sh.isaac.api.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;

import java.time.Instant;

public interface StampBranch
        extends Comparable<StampBranch> {

    /**
     * Gets the time.
     *
     * @return the time
     */
    long getBranchOriginTime();

    /**
     * Gets the time as instant.
     *
     * @return the time as instant
     */
    default Instant getTimeAsInstant() {
        return Instant.ofEpochMilli(this.getBranchOriginTime());
    }


    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    default int compareTo(StampBranch o) {
        final int comparison = Long.compare(this.getBranchOriginTime(), o.getBranchOriginTime());

        if (comparison != 0) {
            return comparison;
        }

        return Integer.compare(this.getPathOfBranchNid(), o.getPathOfBranchNid());
    }


    int getPathOfBranchNid();

    /**
     * Gets the stamp path concept nid.
     *
     * @return the stamp path concept nid
     */
    default ConceptSpecification getPathOfBranchConcept() {
        return Get.conceptSpecification(getPathOfBranchNid());
    }

    StampBranchImmutable toStampBranchImmutable();

    default String toUserString() {
        final StringBuilder sb = new StringBuilder();


        if (this.getBranchOriginTime() == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.getBranchOriginTime() == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" on '")
                .append(Get.conceptDescriptionText(this.getPathOfBranchNid())).append("'");
        return sb.toString();
    }
}
