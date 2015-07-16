/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.chronicle;

import gov.vha.isaac.ochre.api.commit.CommittableComponent;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePosition;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
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
        
    boolean isLatestVersionActive(StampCoordinate coordinate);
    /**
     * 
     * @return a list of all versions of this object chronology. 
     */
    List<? extends V> getVersionList();
    
    /**
     * 
     * @param stampCoordinate used to determine visibility and order of versions
     * @return a list of all visible versions of this object chronology, sorted in
     * ascending order (oldest version first, newest version last). 
     */
    default List<? extends V> getVisibleOrderedVersionList(StampCoordinate stampCoordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(stampCoordinate);
        SortedSet<V> sortedLogicGraphs = new TreeSet<>((V graph1, V graph2) -> {
            RelativePosition relativePosition = calc.fastRelativePosition(graph1, graph2, stampCoordinate.getStampPrecedence());
            switch (relativePosition) {
                case BEFORE:
                    return -1;
                case EQUAL:
                    return 0;
                case AFTER:
                    return 1;
                case UNREACHABLE:
                case CONTRADICTION:
                default:
                    throw new UnsupportedOperationException("Can't handle: " + relativePosition);
            }
        });
        
        sortedLogicGraphs.addAll(getVersionList());
        
        return sortedLogicGraphs.stream().collect(Collectors.toList());
    }
    
    
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
