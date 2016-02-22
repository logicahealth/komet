package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordinate to manage the retrieval and display of language and dialect information.
 *
 * Created by kec on 2/16/15.
 */
public interface LanguageCoordinate {
    
    int getLanguageConceptSequence();
    
    int[] getDialectAssemblagePreferenceList();
    
    int[] getDescriptionTypePreferenceList();
    
    /**
     * Convenience method - returns true if FSN is at the top of the description list.
     * @return
     */
    public default boolean isFSNPreferred() {
        for (int descType : getDescriptionTypePreferenceList()) {
            if (descType == Get.identifierService().getConceptSequenceForUuids(TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getPrimordialUuid())) {
                return true;
            }
            break;
        }
        return false;
    }
        
    Optional<LatestVersion<DescriptionSememe<?>>> 
        getFullySpecifiedDescription(List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, StampCoordinate stampCoordinate);
    
    Optional<LatestVersion<DescriptionSememe<?>>> 
        getPreferredDescription(List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, StampCoordinate stampCoordinate);
    
    /**
     * Return the description according to the type and dialect preferences 
     * of this {@code LanguageCoordinate}.
     * @param descriptionList descriptions to consider
     * @param stampCoordinate
     * @return an optional description best matching the {@code LanguageCoordinate}
     * constraints. 
     */
    Optional<LatestVersion<DescriptionSememe<?>>> getDescription(
            List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, StampCoordinate stampCoordinate);
    
}
