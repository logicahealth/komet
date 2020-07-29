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

    public ObservableEditCoordinateWithOverride(ObservableEditCoordinateBase editCoordinate) {
        super(editCoordinate, editCoordinate.getName());
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> authorForChangesProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.authorForChangesProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> defaultModuleProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.defaultModuleProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> promotionPathProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.promotionPathProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> destinationModuleProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.destinationModuleProperty();
    }

    @Override
    public void setExceptOverrides(EditCoordinateImmutable updatedCoordinate) {
        if (hasOverrides()) {
            ConceptSpecification author = updatedCoordinate.getAuthorForChanges();
            if (authorForChangesProperty().isOverridden()) {
                author = authorForChangesProperty().get();
            };
            ConceptSpecification defaultModule = updatedCoordinate.getDefaultModule();
            if (defaultModuleProperty().isOverridden()) {
                defaultModule = defaultModuleProperty().get();
            };
            ConceptSpecification promotionPath = updatedCoordinate.getPromotionPath();
            if (promotionPathProperty().isOverridden()) {
                promotionPath = promotionPathProperty().get();
            };
            ConceptSpecification destinationModule = updatedCoordinate.getDestinationModule();
            if (destinationModuleProperty().isOverridden()) {
                destinationModule = destinationModuleProperty().get();
            };
            setValue(EditCoordinateImmutable.make(author, defaultModule, promotionPath, destinationModule));
        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makePromotionPathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.promotionPathProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDefaultModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.defaultModuleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorForChangesProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.authorForChangesProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDestinationModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.destinationModuleProperty(), this);
    }
}

