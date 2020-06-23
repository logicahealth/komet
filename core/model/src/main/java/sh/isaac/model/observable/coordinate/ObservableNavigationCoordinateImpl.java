package sh.isaac.model.observable.coordinate;

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
    protected SimpleEqualityBasedSetProperty<ConceptSpecification> makeNavigatorIdentifierConceptsProperty(NavigationCoordinate navigationCoordinate) {
        return new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.DIGRAPH_SPECIFICATION_SET.toExternalString(),
                FXCollections.observableSet(navigationCoordinate.getNavigationConceptNids()
                        .collect(nid -> Get.conceptSpecification(nid)).toSet()));
    }

    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinate(NavigationCoordinate navigationCoordinate) {
        return new ObservableLogicCoordinateImpl(navigationCoordinate.getLogicCoordinate());
    }
}
