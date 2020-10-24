package sh.isaac.model.observable.coordinate;

import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.NavigationCoordinate;
import sh.isaac.api.coordinate.NavigationCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableNavigationCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

public class ObservableNavigationCoordinateWithOverride extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate, String coordinateName) {
        super(navigationCoordinate, coordinateName);
        if (navigationCoordinate instanceof ObservableNavigationCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }

    }

    public ObservableNavigationCoordinateWithOverride(ObservableNavigationCoordinate navigationCoordinate) {
        this(navigationCoordinate, navigationCoordinate.getName());
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
        ObservableNavigationCoordinate observableDigraphCoordinate = (ObservableNavigationCoordinate) navigationCoordinate;
        return new SetPropertyWithOverride<>(observableDigraphCoordinate.navigatorIdentifierConceptsProperty(), this);
    }

    @Override
    public NavigationCoordinateImmutable getOriginalValue() {
        return NavigationCoordinateImmutable.make(IntSets.immutable.of(navigatorIdentifierConceptsProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()));
    }


    @Override
    protected NavigationCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends NavigationCoordinateImmutable> observable, NavigationCoordinateImmutable oldValue, NavigationCoordinateImmutable newValue) {
        this.navigatorIdentifierConceptsProperty().setAll(newValue.getNavigationConceptNids()
                .collect(nid -> Get.conceptSpecification(nid)).toSet());
        return NavigationCoordinateImmutable.make(IntSets.immutable.of(navigatorIdentifierConceptsProperty().stream().mapToInt(value -> value.getNid()).toArray()));
    }
}
