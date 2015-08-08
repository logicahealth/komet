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
    
    int getLanguageConceptSequence();
    
    int[] getDialectAssemblagePreferenceList();
    
    int[] getDescriptionTypePreferenceList();
    
        
    Optional<LatestVersion<DescriptionSememe<?>>> 
        getFullySpecifiedDescription(
                List<SememeChronology<DescriptionSememe<?>>> descriptionList,
                StampCoordinate<? extends StampCoordinate<?>> stampCoordinate);
    
     Optional<LatestVersion<DescriptionSememe<?>>> 
        getPreferredDescription(
                List<SememeChronology<DescriptionSememe<?>>> descriptionList,
                StampCoordinate<? extends StampCoordinate<?>> stampCoordinate);
    
    /**
     * Return the description according to the type and dialect preferences 
     * of this {@code LanguageCoordinate}.
     * @param descriptionList descriptions to consider
     * @param stampCoordinate
     * @return an optional description best matching the {@code LanguageCoordinate}
     * constraints. 
     */
     Optional<LatestVersion<DescriptionSememe<?>>> 
        getDescription(
                List<SememeChronology<DescriptionSememe<?>>> descriptionList,
                StampCoordinate<?> stampCoordinate);
    
    
}
