package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import java.util.List;
import java.util.Set;

/**
 * Coordinate to control the retrieval and display of
 * object chronicle versions by indicating the current position (represented as time) on a path, 
 * and allowed modules.
 * <p\>
 * q: How does the stamp coordinate relate to the stamp sequence?
 * <p\>
 * a: A stamp sequence is a sequentially assigned identifier for a unique combination of State, Time, Author, Module, and Path...
 * A stamp coordinate specifies a position on a  path, with a particular set of modules, and allowed state values.
 * 
 *<p\>
 * Created by kec on 2/16/15.
 * @param <T>
 *  * TODO can remove generic type on StampCoordinate once ViewCoordinate is eliminated. 

 */
public interface StampCoordinate<T extends StampCoordinate> extends TimeBasedAnalogMaker<T>, StateBasedAnalogMaker<T> {
    
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
     */
    ConceptSequenceSet getModuleSequences();
    
    /**
     * 
     * @return A list of {@code ConceptSpecification} that represent the modules to include in version computations
     * based on this stamp coordinate.
     */
    default List<ConceptSpecification> getModuleSpecificationList() {
        return getModuleSequences().toConceptSpecificationList() ;
     }
    
    /**
     * Determine what states should be included in results based on this
     * stamp coordinate. If current—but inactive—versions are desired, 
     * the allowed states must include {@code State.INACTIVE}
     * @return the set of allowed states for results based on this stamp coordinate. 
     */
    Set<State> getAllowedStates();
}
