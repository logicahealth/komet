/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.chronicle;

import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author kec
 * @param <V> the Version type this chronicled object contains. 
 */
public interface ChronicledObjectLocal<V extends StampedVersion> extends IdentifiedObjectLocal {
    
    List<? extends V> getVersions();
    
    IntStream getVersionStampSequences();
    
}
