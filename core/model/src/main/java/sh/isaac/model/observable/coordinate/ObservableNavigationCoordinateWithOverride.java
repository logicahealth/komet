package sh.isaac.model.observable.coordinate;

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.NavigationCoordinate;
import sh.isaac.api.observable.coordinate.ObservableNavigationCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

public class ObservableNavigationCoordinateWithOverride extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate digraphCoordinate, String coordinateName) {
        super(digraphCoordinate, coordinateName);
    }

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate digraphCoordinate) {
        super(digraphCoordinate, "Navigation coordinate with override");
    }

    @Override
    protected SimpleEqualityBasedSetProperty<ConceptSpecification> makeNavigatorIdentifierConceptsProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinateImpl observableDigraphCoordinate = (ObservableNavigationCoordinateImpl) navigationCoordinate;
        return new SetPropertyWithOverride<>(observableDigraphCoordinate.navigatorIdentifierConceptsProperty(), this);
    }

    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinate(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinateImpl observableDigraphCoordinate = (ObservableNavigationCoordinateImpl) navigationCoordinate;
        return new ObservableLogicCoordinateWithOverride(observableDigraphCoordinate.getLogicCoordinate());
    }
}
