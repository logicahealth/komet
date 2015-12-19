/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.waitfree;

import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.WaitFreeComparable;

/**
 * {@code WaitFreeMergeSerializer} objects enable wait-free serialization to
 * maps, where the objects are append only (data is added to, but never removed
 * from the object).  
 * This ability to compare original 
 * MD5 checksums with current {@code byte[]} data, enables compare and swap 
 * updates to maps so that they may be updated using wait-free algorithms 
 * (an algorithm where there is guaranteed per-thread progress). Wait-freedom is 
 * the strongest non-blocking guarantee of progress). 
 * @author kec
 * @param <T> 
 */
public interface WaitFreeMergeSerializer<T extends WaitFreeComparable> {
    
    /**
     * 
     * @param d the data output stream to write to. 
     * @param a the object to serialize to the data output stream. 
     */
    void serialize(ByteArrayDataBuffer d, T a);

    /**
     * Support for merging objects when a compare and swap operation fails, 
     * enabling wait-free serialization. 
     * @param a the first object to merge. 
     * @param b the second object to merge. 
     * @param writeSequence  to use as origin of 
     * the merged object. 
     * @return the merged object. 
     */
    T merge (T a, T b, int writeSequence);

    
    /**
     * 
     * @param di the data from which to deserialize the object. 
     * @return 
     */
    T deserialize(ByteArrayDataBuffer di);

}
