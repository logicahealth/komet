package sh.isaac.api.navigation;

import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Consumer;

public interface TypeStampNavigationRecords {
    /**
     * Convert to long.
     *
     * @param typeNid       the type nid
     * @param stampSequence the stamp sequence
     * @param taxonomyFlags the taxonomy flags
     * @return the long
     */
    static int[] convertToArray(int typeNid, int stampSequence, int taxonomyFlags) {
        return new int[]{typeNid, stampSequence, taxonomyFlags};
    }

    Collection<? extends TypeStampNavigationRecord> values();

    /**
     * @return the type, stamp, taxonomy flag records, with no size prefix.
     */
    int[] toArray();

    /**
     * Contains concept nid via type and the status of the latest stamp is within tne
     * allowed states of the stamp coordinate used by the relative position calculator.
     *
     * @param typeNid  the type nid
     * @param flags    the flags
     * @param computer the computer
     * @return true, if successful
     */
    boolean containsConceptNidViaTypeWithAllowedStatus(int typeNid, int[] flags, RelativePositionCalculator computer);

    int[] latestStampsForConceptNidViaTypeWithAllowedStatus(int typeNid, int[] flags, RelativePositionCalculator computer);

    /**
     * @param typeNid
     * @param flags
     * @param computer
     * @return An EnumSet<Status>. If there is a contradiction, then more than one status is returned. If there
     * is no contradiction, then only a single status is in the EnumSet.
     */
    EnumSet<Status> getConceptStates(int typeNid, int[] flags, RelativePositionCalculator computer);

    /**
     * Contains concept nid via type.
     *
     * @param typeNid  the type nid
     * @param tc       the tc
     * @param computer the computer
     * @return true, if successful
     */
    boolean containsConceptNidViaTypeWithAllowedStatus(int typeNid, ManifoldCoordinate tc, RelativePositionCalculator computer);

    /**
     * Contains concept nid via type.
     *
     * @param typeNidSet the type nid set
     * @param flags      the flags
     * @param computer   the computer
     * @return true, if successful
     */
    boolean containsConceptNidViaTypeWithAllowedStatus(NidSet typeNidSet, int[] flags, RelativePositionCalculator computer);

    /**
     * Contains concept nid via type.
     *
     * @param typeNidSet the type nid set
     * @param tc         the tc
     * @param computer   the computer
     * @return true, if successful
     */
    boolean containsConceptNidViaTypeWithAllowedStatus(NidSet typeNidSet,
                                                       ManifoldCoordinate tc,
                                                       RelativePositionCalculator computer);

    /**
     * Contains stamp of type with flags.
     *
     * @param typeNid Integer.MAX_VALUE is a wildcard and will match all types.
     * @param flags   the flags
     * @return true if found.
     */
    boolean containsStampOfTypeWithFlags(int typeNid, int[] flags);

    /**
     * Contains stamp of type with flags.
     *
     * @param typeNidSet An empty set is a wildcard and will match all types.
     * @param flags      the flags
     * @return true if found.
     */
    boolean containsStampOfTypeWithFlags(NidSet typeNidSet, int[] flags);

    void forEach(Consumer<? super TypeStampNavigationRecord> procedure);

    /**
     * Length.
     *
     * @return the number of integers this stampNid record will occupy when
     * packed.
     */
    int length();

    /**
     * Checks if present.
     *
     * @param typeNidSet the type nid set
     * @param flags      the flags
     * @return true, if present
     */
    boolean isPresent(NidSet typeNidSet, int[] flags);

    /**
     * Gets the stamps of type with flags.
     *
     * @param typeNid the type nid
     * @param flags   the flags
     * @return the stamps of type with flags
     */
    int[] getStampsOfTypeWithFlags(int typeNid, int[] flags);

    /**
     * Gets the stamps of type with flags.
     *
     * @param typeNidSet the type nid set
     * @param flags      the flags
     * @return the stamps of type with flags
     */
    int[] getStampsOfTypeWithFlags(NidSet typeNidSet, int[] flags);
}
