package sh.isaac.model.observable.coordinate;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;

public class ObservableNavigationCoordinateImpl extends ObservableNavigationCoordinateBase {

    public ObservableNavigationCoordinateImpl(NavigationCoordinateImmutable immutableCoordinate, String coordinateName) {
        super(immutableCoordinate, coordinateName);
    }

    public ObservableNavigationCoordinateImpl(NavigationCoordinateImmutable immutableCoordinate) {
        super(immutableCoordinate, "Navigation coordinate");
    }

    @Override
    public void setExceptOverrides(NavigationCoordinateImmutable updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected SimpleEqualityBasedSetProperty<ConceptSpecification> makeNavigatorIdentifierConceptsProperty(NavigationCoordinate navigationCoordinate) {
        return new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.DIGRAPH_SPECIFICATION_SET.toExternalString(),
                FXCollections.observableSet(navigationCoordinate.getNavigationConceptNids()
                        .collect(nid -> Get.conceptSpecification(nid)).toSet()));
    }

    @Override
    protected NavigationCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends NavigationCoordinateImmutable> observable, NavigationCoordinateImmutable oldValue, NavigationCoordinateImmutable newValue) {
        this.navigatorIdentifierConceptsProperty().setAll(newValue.getNavigationConceptNids()
                .collect(nid -> Get.conceptSpecification(nid)).toSet());
        return newValue;
    }

    @Override
    public NavigationCoordinateImmutable getOriginalValue() {
        return getValue();
    }
}
