package sh.isaac.api.navigation;

import sh.isaac.api.coordinate.TaxonomyFlag;

import java.util.EnumSet;

public interface TypeStampNavigationRecord {
    long getTypeStampKey();

    /**
     * Gets the stamp sequence.
     *
     * @return the stamp sequence
     */
    int getStampSequence();

    /**
     * Gets the taxonomy flags.
     *
     * @return the taxonomy flags
     */
    int getTaxonomyFlags();

    /**
     * Gets the taxonomy flags as enum.  Do NOT modify the contents of the returned enumset!
     *
     * @return the taxonomy flags as enum
     */
    EnumSet<TaxonomyFlag> getTaxonomyFlagsAsEnum();

    /**
     * Gets the type sequence.
     *
     * @return the type sequence
     */
    int getTypeNid();
}
