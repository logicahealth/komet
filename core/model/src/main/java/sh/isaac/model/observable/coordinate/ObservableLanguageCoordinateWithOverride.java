package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.override.ListPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

import java.util.List;
import java.util.Optional;

public class ObservableLanguageCoordinateWithOverride extends ObservableLanguageCoordinateBase {

    private final ObservableLanguageCoordinate overriddenCoordinate;


    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate, String coordinateName) {
        super(overriddenCoordinate, coordinateName);
        if (overriddenCoordinate instanceof ObservableLanguageCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
        this.overriddenCoordinate = overriddenCoordinate;
    }

    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate) {
        this(overriddenCoordinate, overriddenCoordinate.getName());
    }

    @Override
    public void setExceptOverrides(LanguageCoordinateImmutable updatedCoordinate) {
        if (hasOverrides()) {
            int languageConceptNid = updatedCoordinate.getLanguageConceptNid();
            if (languageConceptProperty().isOverridden()) {
                languageConceptNid = languageConceptProperty().get().getNid();
            }
            int[] modulePreferenceList = updatedCoordinate.getModulePreferenceListForLanguage();
            if (modulePreferenceListForLanguageProperty().isOverridden()) {
                modulePreferenceList = getModulePreferenceListForLanguage();
            }

            int[] descriptionTypePreferenceList = updatedCoordinate.getDescriptionTypePreferenceList();
            if (descriptionTypePreferenceListProperty().isOverridden()) {
                descriptionTypePreferenceList = getModulePreferenceListForLanguage();
            }

            int[] dialectAssemblagePreferenceList = updatedCoordinate.getDialectAssemblagePreferenceList();
            if (dialectAssemblagePreferenceListProperty().isOverridden()) {
                dialectAssemblagePreferenceList = getDialectAssemblagePreferenceList();
            }

            setValue(LanguageCoordinateImmutable.make(languageConceptNid, IntLists.immutable.of(descriptionTypePreferenceList),
                    IntLists.immutable.of(dialectAssemblagePreferenceList), IntLists.immutable.of(modulePreferenceList), updatedCoordinate.getNextPriorityLanguageCoordinate()));

        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    public ListPropertyWithOverride<ConceptSpecification> modulePreferenceListForLanguageProperty() {
        return (ListPropertyWithOverride<ConceptSpecification>) super.modulePreferenceListForLanguageProperty();
    }

    @Override
    public ListPropertyWithOverride<ConceptSpecification> descriptionTypePreferenceListProperty() {
        return (ListPropertyWithOverride<ConceptSpecification>) super.descriptionTypePreferenceListProperty();
    }

    @Override
    public ListPropertyWithOverride<ConceptSpecification> dialectAssemblagePreferenceListProperty() {
        return (ListPropertyWithOverride<ConceptSpecification>) super.dialectAssemblagePreferenceListProperty();
    }

    @Override
    public Optional<? extends LanguageCoordinate> getNextPriorityLanguageCoordinate() {
        return super.getNextPriorityLanguageCoordinate();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> languageConceptProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.languageConceptProperty();
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
                    new ObservableLanguageCoordinateWithOverride(overriddenNextCoordinate));
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

    @Override
    public LanguageCoordinateImmutable getOriginalValue() {
        return LanguageCoordinateImmutable.make(languageConceptProperty().getOriginalValue().getNid(),
                IntLists.immutable.of(descriptionTypePreferenceListProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(dialectAssemblagePreferenceListProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(modulePreferenceListForLanguageProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()),
                getNextPriorityLanguageCoordinate());
    }


    @Override
    protected LanguageCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends LanguageCoordinateImmutable> observable, LanguageCoordinateImmutable oldValue, LanguageCoordinateImmutable newValue) {
        this.languageConceptProperty().setValue(newValue.getLanguageConcept());
        this.dialectAssemblagePreferenceListProperty().setAll(newValue.getDialectAssemblageSpecPreferenceList());
        this.descriptionTypePreferenceListProperty().setAll(newValue.getDescriptionTypeSpecPreferenceList());
        this.modulePreferenceListForLanguageProperty().setAll(newValue.getModuleSpecPreferenceListForLanguage());
        if (newValue.getNextPriorityLanguageCoordinate().isPresent()) {
            if (this.nextPriorityLanguageCoordinateProperty().get() != null) {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                this.nextPriorityLanguageCoordinateProperty().get().setValue(languageCoordinateImmutable);
            } else {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                ObservableLanguageCoordinateImpl observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(languageCoordinateImmutable);
                this.nextPriorityLanguageCoordinateProperty().setValue(observableLanguageCoordinate);
            }
        } else {
            this.nextPriorityLanguageCoordinateProperty().setValue(null);
        }
        return LanguageCoordinateImmutable.make(languageConceptProperty().get().getNid(),
                IntLists.immutable.of(descriptionTypePreferenceListProperty().get().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(dialectAssemblagePreferenceListProperty().get().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(modulePreferenceListForLanguageProperty().get().stream().mapToInt(value -> value.getNid()).toArray()),
                getNextPriorityLanguageCoordinate());
    }

}
