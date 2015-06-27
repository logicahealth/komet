package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.State;
import java.util.Set;

/**
 * Coordinate to control the retrieval and display of
 * object chronicles by indicating the current position on a path, and allowed modules.
 * 
 *
 * Created by kec on 2/16/15.
 */
public interface StampCoordinate {
    
    StampPrecedence getStampPrecedence();
    
    StampPosition getStampPosition();
    
    int[] getModuleSequences();
    
    Set<State> getAllowedStates();
    
}
