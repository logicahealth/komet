package sh.isaac.model.observable.coordinate;

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
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

