package gov.vha.isaac.ochre.api.logic;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by kec on 12/9/14.
 */
public interface Node extends Comparable<Node> {

    NodeSemantic getNodeSemantic();

    Node[] getChildren();
    
    default Stream<Node> getChildStream() {
        return Arrays.stream(getChildren());
    };

    byte[] getBytes(DataTarget dataTarget);

    short getNodeIndex();

    void setNodeIndex(short nodeIndex);

    void addChildren(Node... children);
    
    /**
     * Sort the children of this node
     */
    void sort();
    
    /**
     * 
     * @return A string representing the fragment of the expression 
     * rooted in this node. 
     */
    String fragmentToString();
    
    /**
     * Use to when printing out multiple expressions, and you want to differentiate the 
     * identifiers so that they are unique across all the expressions. 
     * @param nodeIdSuffix the identifier suffix for this expression. 
     * @return A string representing the fragment of the expression 
     * rooted in this node. 
     */
    String fragmentToString(String nodeIdSuffix);
    
    /**
     * Use to when printing out multiple expressions, and you want to differentiate the 
     * identifiers so that they are unique across all the expressions. 
     * @param nodeIdSuffix the identifier suffix for this expression. 
     * @return a text representation of this expression. 
     */
    String toString(String nodeIdSuffix);
    
    /**
     * Adds the sequences of the concepts referenced by this node, including the
     * node semantic. Used by isomorphic algorithms to score potential matches.
     * Concepts reference by children of connector nodes should not be included, just
     * concepts associated with the node itself (node semantic concept + type concept, etc). 
     * @param conceptSequenceSet The set to add the concept sequences to. 
     */
    void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet);

}
