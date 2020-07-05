package sh.isaac.api.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;

public interface ObservableNavigationCoordinate extends ObservableCoordinate<NavigationCoordinateImmutable>, NavigationCoordinateProxy {

    SetProperty<ConceptSpecification> navigatorIdentifierConceptsProperty();


    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                navigatorIdentifierConceptsProperty(),
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[] {};
    }

    @Override
    ObservableLogicCoordinate getLogicCoordinate();
    ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty();

    default void setPremiseType(PremiseType premiseType) {
        switch (premiseType) {
            case STATED:
                navigatorIdentifierConceptsProperty().clear();
                navigatorIdentifierConceptsProperty().add(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE);
                break;
            case INFERRED:
                navigatorIdentifierConceptsProperty().clear();
                navigatorIdentifierConceptsProperty().add(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE);
                break;
        }
    }

}
