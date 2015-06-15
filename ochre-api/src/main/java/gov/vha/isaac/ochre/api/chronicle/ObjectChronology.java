/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.chronicle;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.CommittableComponent;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 *
 * @author kec
* @param <V> the Version type this chronicled object contains. 
 */
public interface ObjectChronology<V extends StampedVersion> 
    extends IdentifiedObjectLocal, CommittableComponent {
    
    Optional<LatestVersion<V>> 
        getLatestVersion(Class<V> type, StampCoordinate coordinate);
        
    default Optional<LatestVersion<V>> 
        getLatestActiveVersion(Class<V> type, StampCoordinate coordinate) {
            Optional<LatestVersion<V>> latest = getLatestVersion(type, coordinate);
            if (latest.isPresent() && latest.get().value.getState() == State.ACTIVE) {
                return latest;
            }
            return Optional.empty();
        }
    /**
     * 
     * @return a list of all versions of this object chronology. 
     */
    List<? extends V> getVersionList();
    
    /**
     * 
     * @return the version stamps for all the versions of this object chronology. 
     */
    IntStream getVersionStampSequences();
    
    /**
     * 
     * @return a list of sememes, where this object is the referenced component. 
     */
    List<? extends SememeChronology<? extends SememeVersion>> 
        getSememeList();
    
    List<? extends SememeChronology<? extends SememeVersion>> 
        getSememeListFromAssemblage(int assemblageSequence);

    <SV extends SememeVersion> List<? extends SememeChronology<SV>> 
        getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type);

    /**
     * 
     * @return a list of all versions of this object chronology. 
     * @deprecated use getVersionList() instead. 
     */
    @Deprecated 
    default List<? extends V> getVersions() {
        return getVersionList();
    }  
}
