package gov.vha.isaac.ochre.api.coordinate;

/**
 * Coordinate to manage the retrieval and display of logic information.
 *
 * Created by kec on 2/16/15.
 */
public interface LogicCoordinate {
    /**
     * 
     * @return concept sequence for the assemblage where the stated logical form
     * of concept definition graphs are stored. 
     */
    int getStatedAssemblageSequence();
    /**
     * 
     * @return concept sequence for the assemblage where the inferred logical form
     * of concept definition graphs are stored.
     */
    int getInferredAssemblageSequence();
    /**
     * 
     * @return concept sequence for the description-logic profile for this coordinate. 
     */
    int getDescriptionLogicProfileSequence();
    
    /**
     * 
     * @return concept sequence for the classifier for this coordinate. 
     */
    int getClassifierSequence();
    
}
