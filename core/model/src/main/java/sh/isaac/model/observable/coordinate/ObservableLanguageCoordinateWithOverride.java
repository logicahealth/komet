package sh.isaac.model.observable.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.override.ListPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

public class ObservableLanguageCoordinateWithOverride extends ObservableLanguageCoordinateBase {

    private final ObservableLanguageCoordinate overriddenCoordinate;


    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate, String coordinateName) {
        super(overriddenCoordinate, coordinateName);
        this.overriddenCoordinate = overriddenCoordinate;
    }

    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate) {
        super(overriddenCoordinate, "Language coordinate with override");
        this.overriddenCoordinate = overriddenCoordinate;
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeLanguageProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.languageConceptProperty(), this);
    }

    protected SimpleEqualityBasedObjectProperty<ObservableLanguageCoordinate> makeNextPriorityLanguageCoordinateProperty(LanguageCoordinate languageCoordinate) {
        if (languageCoordinate.getNextPriorityLanguageCoordinate().isPresent()) {
            ObservableLanguageCoordinate overriddenNextCoordinate = (ObservableLanguageCoordinate) languageCoordinate.getNextPriorityLanguageCoordinate().get();
            return new SimpleEqualityBasedObjectProperty<>(this,
                    ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                    new ObservableLanguageCoordinateWithOverride(overriddenNextCoordinate, "Overridden language coordinate"));
        }
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                null);
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptSpecification> makeDialectAssemblagePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.dialectAssemblagePreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptSpecification> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.descriptionTypePreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptSpecification> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.modulePreferenceListForLanguageProperty(), this);
    }

    @Override
    public void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
        this.dialectAssemblagePreferenceListProperty().setAll(Get.conceptList(dialectAssemblagePreferenceList));
    }

    @Override
    public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        this.descriptionTypePreferenceListProperty().setAll(Get.conceptList(descriptionTypePreferenceList));
    }
}
