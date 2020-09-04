package sh.isaac.model.observable.coordinate;

import javafx.beans.property.SetProperty;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.NavigationCoordinate;
import sh.isaac.api.coordinate.NavigationCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableNavigationCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

public class ObservableNavigationCoordinateWithOverride extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate, String coordinateName) {
        super(navigationCoordinate, coordinateName);
    }

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate) {
        super(navigationCoordinate, navigationCoordinate.getName());
    }

    @Override
    public SetPropertyWithOverride<ConceptSpecification> navigatorIdentifierConceptsProperty() {
        return (SetPropertyWithOverride<ConceptSpecification>) super.navigatorIdentifierConceptsProperty();
    }

    @Override
    public void setExceptOverrides(NavigationCoordinateImmutable updatedCoordinate) {
        if (navigatorIdentifierConceptsProperty().isOverridden()) {
            this.setValue(NavigationCoordinateImmutable.make(getNavigationConceptNids()));
        } else {
            this.setValue(updatedCoordinate);
        }
    }

    @Override
    protected SimpleEqualityBasedSetProperty<ConceptSpecification> makeNavigatorIdentifierConceptsProperty(NavigationCoordinate navigationCoordinate) {
        ObservableNavigationCoordinateImpl observableDigraphCoordinate = (ObservableNavigationCoordinateImpl) navigationCoordinate;
        return new SetPropertyWithOverride<>(observableDigraphCoordinate.navigatorIdentifierConceptsProperty(), this);
    }
}
