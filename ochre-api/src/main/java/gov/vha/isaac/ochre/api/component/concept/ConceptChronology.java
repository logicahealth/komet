/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.relationship.RelationshipVersionAdaptor;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author kec
 * @param <V>
 */
public interface ConceptChronology<V extends ConceptVersion<V>>
    extends ObjectChronology<V> {
    
    /**
     * 
     * @return the sequence of this concept. A contiguously assigned identifier for
     * concepts >= 0;
     */
    int getConceptSequence();
    
    /**
     * Create a mutable version with Long.MAX_VALUE as the time, indicating
     * the version is uncommitted. It is the responsibility of the caller to
     * add the mutable version to the commit manager when changes are complete
     * prior to committing the component. 
     * @param state state of the created mutable version
     * @param ec edit coordinate to provide the author, module, and path for the mutable version
     * @return the mutable version
     */
    V createMutableVersion(State state, EditCoordinate ec);
    
    /**
     * Create a mutable version the specified stampSequence. It is the responsibility of the caller to
     * add persist the chronicle when changes to the mutable version are complete . 
     * @param stampSequence stampSequence that specifies the status, time, author, module, and path of this version.
     * @return the mutable version
     */
     V createMutableVersion(int stampSequence);
    
    /**
     * A test for validating that a concept contains a description. Used
     * to validate concept proxies or concept specs at runtime.
     * @param descriptionText text to match against. 
     * @return true if any version of a description matches this text. 
     */
    boolean containsDescription(String descriptionText);

    /**
     * A test for validating that a concept contains a description as specified by
     * the stampCoordinate. Used
     * to validate concept proxies or concept specs at runtime.
     * @param descriptionText text to match against. 
     * @param stampCoordinate coordinate to determine if description is active. 
     * @return true if any version of a description matches this text. 
     */
    boolean containsDescription(String descriptionText, StampCoordinate stampCoordinate);
        
    List<? extends SememeChronology<? extends DescriptionSememe>> getConceptDescriptionList();
    
    Optional<LatestVersion<DescriptionSememe>> 
        getFullySpecifiedDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate);
    
    Optional<LatestVersion<DescriptionSememe>> 
        getPreferredDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate);

    /**
     * Uses the default logic coordinate. 
     * @return 
     */
    List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> 
        getRelationshipListOriginatingFromConcept();
    List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> 
        getRelationshipListOriginatingFromConcept(LogicCoordinate logicCoordinate);
        
    /**
     * Uses the default logic coordinate. 
     * @return 
     */
    List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> 
        getRelationshipListWithConceptAsDestination();
    List<? extends SememeChronology<? extends RelationshipVersionAdaptor>> 
        getRelationshipListWithConceptAsDestination(LogicCoordinate logicCoordinate);
        
        
    Optional<LatestVersion<LogicGraphSememe>> getLogicalDefinition(StampCoordinate stampCoordinate, 
            PremiseType premiseType, LogicCoordinate logicCoordinate);
    
    /**
     * Return a formatted text report showing chronology of logical definitions
     * for this concept, according to the provided parameters.
     * @param stampCoordinate specifies the ordering and currency of versions. 
     * @param premiseType Stated or inferred premise type
     * @param logicCoordinate specifies the assemblages where the definitions are stored. 
     * @return 
     */
    String getLogicalDefinitionChronologyReport(StampCoordinate stampCoordinate, 
            PremiseType premiseType, LogicCoordinate logicCoordinate);
    /**
     * 
     * @return
     * @deprecated use getNid() instead.
     */
    @Deprecated
    default int getConceptNid() {
        return getNid();
    }

}
