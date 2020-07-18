package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableEditCoordinateWithOverride
        extends ObservableEditCoordinateBase {

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable edit coordinate impl.
     *
     * @param editCoordinate the edit coordinate
     */
    public ObservableEditCoordinateWithOverride(ObservableEditCoordinateBase editCoordinate, String coordinateName) {
        super(editCoordinate, coordinateName);
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> authorProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.authorProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> moduleProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.moduleProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> pathProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.pathProperty();
    }

    @Override
    public void setExceptOverrides(EditCoordinateImmutable updatedCoordinate) {
        if (hasOverrides()) {
            ConceptSpecification author = updatedCoordinate.getAuthor();
            if (authorProperty().isOverridden()) {
                author = authorProperty().get();
            };
            ConceptSpecification module = updatedCoordinate.getModule();
            if (moduleProperty().isOverridden()) {
                module = moduleProperty().get();
            };
            ConceptSpecification path = updatedCoordinate.getPath();
            if (pathProperty().isOverridden()) {
                path = pathProperty().get();
            };
            setValue(EditCoordinateImmutable.make(author, module, path));

        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makePathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.pathProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.moduleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.authorProperty(), this);
    }
}

