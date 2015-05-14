/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.chronicle;

import gov.vha.isaac.ochre.api.commit.CommittableComponent;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author kec
 * @param <V> the Version type this chronicled object contains. 
 */
public interface ObjectChronology<V extends StampedVersion> 
    extends IdentifiedObjectLocal, CommittableComponent {
    
    List<? extends V> getVersionList();
    
    IntStream getVersionStampSequences();
    
    List<? extends SememeChronology<? extends SememeVersion>> getSememeList();
    
    @Deprecated
    default public List<? extends V> getVersions() {
        return getVersionList();
    }
    
}
