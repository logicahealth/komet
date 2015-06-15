package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import java.util.List;
import java.util.Optional;

/**
 * Coordinate to manage the retrieval and display of language and dialect information.
 *
 * Created by kec on 2/16/15.
 */
public interface LanguageCoordinate {
    
    int getLanugageConceptSequence();
    
    int[] getDialectAssemblagePreferenceList();
    
    int[] getDescriptionTypePreferenceList();
    
        
    Optional<LatestVersion<DescriptionSememe>> 
        getFullySpecifiedDescription(
                List<SememeChronology<DescriptionSememe>> descriptionList,
                StampCoordinate stampCoordinate);
    
     Optional<LatestVersion<DescriptionSememe>> 
        getPreferredDescription(
                List<SememeChronology<DescriptionSememe>> descriptionList,
                StampCoordinate stampCoordinate);
    
    
}
