package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.Get;
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
            if (descType == Get.identifierService().getConceptSequenceForUuids(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"))) {
                return true;
            }
            break;
        }
        return false;
    }
        
    <T extends DescriptionSememe<T>> Optional<LatestVersion<T>> 
        getFullySpecifiedDescription(
                List<SememeChronology<T>> descriptionList,
                StampCoordinate<? extends StampCoordinate<?>> stampCoordinate);
    
    <T extends DescriptionSememe<T>> Optional<LatestVersion<T>> 
        getPreferredDescription(
                List<SememeChronology<T>> descriptionList,
                StampCoordinate<? extends StampCoordinate<?>> stampCoordinate);
    
    /**
     * Return the description according to the type and dialect preferences 
     * of this {@code LanguageCoordinate}.
     * @param descriptionList descriptions to consider
     * @param stampCoordinate
     * @return an optional description best matching the {@code LanguageCoordinate}
     * constraints. 
     */
    <T extends DescriptionSememe<T>> Optional<LatestVersion<T>> 
        getDescription(
                List<SememeChronology<T>> descriptionList,
                StampCoordinate<?> stampCoordinate);
    
    
}
