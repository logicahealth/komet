package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.State;
import java.util.Set;

/**
 * Coordinate to control the retrieval and display of
 * object chronicle versions by indicating the current position on a path, and allowed modules.
 * 
 *
 * Created by kec on 2/16/15.
 */
public interface StampCoordinate {
    
    /**
     * Determine if the stamp coordinate is time based, or path based. Generally
     * path based is recommended. 
     * @return the stamp precedence. 
     */
    StampPrecedence getStampPrecedence();
    
    /**
     * 
     * @return the position (time on a path) that is used to 
     * compute what stamped objects versions are the latest with respect to this
     * position.
     */
    StampPosition getStampPosition();
    
    /**
     * An empty array is a wild-card, and should match all modules. If there are
     * one or more module sequences specified, only those modules will be included
     * in the results. 
     * @return the set of module sequences to include in results based on this
     * stamp coordinate. 
     * TODO change this to a concept sequence set, instead of an int[]?
     */
    int[] getModuleSequences();
    
    /**
     * Determine what states should be included in results based on this
     * stamp coordinate. If current—but inactive—versions are desired, 
     * the allowed states must include {@code State.INACTIVE}
     * @return the set of allowed states for results based on this stamp coordinate. 
     */
    Set<State> getAllowedStates();
    /**
     * Analog: A structural derivative that often differs by a single element.
     * @param stampPositionTime the time of the stamp position for the analog
     * @return a new StampCoordinate with the specified stamp position time. 
     */
    StampCoordinate makeAnalog(long stampPositionTime);
    
}
