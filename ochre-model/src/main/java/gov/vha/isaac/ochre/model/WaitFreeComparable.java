/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model;

/**
 * 
 * {@code WaitFreeComparable} objects can compare the write sequence of the 
 * original {@code byte[]} data from which they where deserialized, with  
 * the current write sequence in a map of objects. 
 * This ability to compare original 
 * write sequence with current write sequence, enables compare and swap 
 * updates to maps so that they may be updated using wait-free algorithms 
 * (an algorithm where there is guaranteed per-thread progress). Wait-freedom is 
 * the strongest non-blocking guarantee of progress). 
 * 
 * Also see http://minborgsjavapot.blogspot.com/2014/12/java-8-byo-super-efficient-maps.html
 * for discussion of using maps with known keyspace such as we do here...
 * @author kec
 */
public interface WaitFreeComparable {

    /**
     * 
     * @return the write sequence from which this object was created. 
     */
    int getWriteSequence();
    
    /**
     * 
     * @param sequence the write sequence for which this object is to be written. 
     */
    void setWriteSequence(int sequence);
    
}
