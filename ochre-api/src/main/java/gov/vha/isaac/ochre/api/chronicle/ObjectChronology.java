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
import gov.vha.isaac.ochre.api.dag.Graph;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
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
    extends OchreExternalizable, CommittableComponent {
    
    Optional<LatestVersion<V>> 
        getLatestVersion(Class<V> type, StampCoordinate coordinate);    
        
    boolean isLatestVersionActive(StampCoordinate coordinate);
    /**
     * 
     * @return a list of all versions of this object chronology, with no order guarantee. . 
     */
    List<? extends V> getVersionList();
    
    /**
     * 
     * @return Get a graph representation of the versions of this object chronology, where the root of the 
     * graph is the original version of this component on a path, and the children are in sequential order, taking path 
     * precedence into account. When a component version may have subsequent changes on more than one path,
     * which will result in more that one child node for that version. 
     * If a chronology has disconnected versions on multiple paths, multiple graphs will be created and returned. 
     * A version may be included in more than one graph if disconnected original versions are subsequently 
     * merged onto commonly visible downstream paths. 
     * 
     */
    default List<Graph<? extends V>> getVersionGraphList() {
        throw new UnsupportedOperationException();
    }
    
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
    List<? extends SememeChronology<? extends SememeVersion<?>>> 
        getSememeList();
    
    List<? extends SememeChronology<? extends SememeVersion<?>>> 
        getSememeListFromAssemblage(int assemblageSequence);

    <SV extends SememeVersion> List<? extends SememeChronology<SV>> 
        getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type);
}
