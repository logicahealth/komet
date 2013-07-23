package org.ihtsdo.otf.tcc.api.contradiction;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import java.io.Serializable;
import java.util.List;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

/**
 * Interface for classes that resolve contradictions - i.e. the scenario where two
 * different paths have different data for a given component.
 * <p>
 * Given a component's versions and an optional time point (latest is assumed
 * without a time point), implementations of this interface will calculate the
 * "contradiction resolved view" of the component at that time point.
 */

public interface ContradictionManagerBI extends Serializable {
    /**
     * Method to get the display name of this conflict resolution strategy.
     * Note that this is intended to be something meaningful to an end user
     * attempting to choose a conflict resolution strategy.
     * 
     * @return The display name of this conflict resolution strategy
     */
    String getDisplayName();

    /**
     * Method to get a description of this conflict resolution strategy.
     * Note that this is intended to be something meaningful to an end user
     * attempting to choose a conflict resolution strategy. This content
     * may contain XHTML markup for readability.
     * 
     * @return The display name of this conflict resolution strategy
     */
    String getDescription();

    /**
     * Resolves the supplied versions, which may be from more than one entity,
     * to a conflict resolved latest state.
     * <p>
     * Best case this will resolve to one tuple, however this will depend upon
     * the data and the resolution strategy in use.
     * <p>
     * Note that the input list of tuples will not be modified by this method.
     * 
     * @param tuples
     * @return tuples resolved as per the resolution strategy
     */
    <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions);

    /**
     * Resolves the supplied parts to a conflict resolved latest state.
     * <p>
     * Best case this will resolve to one part, however this will depend upon
     * the data and the resolution strategy in use.
     * <p>
     * Note that the input list of parts will not be modified by this method.
     * <p>
     * <strong> NB This method requires that all the parts are from the same
     * entity! If they are not there is no way for this method to determine that
     * and resolution will take place assuming they are all from the same
     * entity. </strong>
     * 
     * @param parts
     * @return parts resolved as per the resolution strategy
     */
    <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2);
  
}
