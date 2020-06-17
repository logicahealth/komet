package sh.isaac.api.observable.coordinate;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;

public interface ObservableDigraphCoordinate extends ObservableCoordinate<DigraphCoordinateImmutable>, DigraphCoordinateProxy {

    SetProperty<ConceptSpecification> digraphIdentifierConceptsProperty();
    ObjectProperty<PremiseType> premiseTypeProperty();

    @Override
    ObservableStampFilter getVertexStampFilter();
    ObjectProperty<StampFilterImmutable> vertexStampFilterProperty();

    @Override
    ObservableStampFilter getEdgeStampFilter();
    ObjectProperty<StampFilterImmutable> edgeStampFilterProperty();

    @Override
    ObservableStampFilter getLanguageStampFilter();
    ObjectProperty<StampFilterImmutable> languageStampFilterProperty();

    @Override
    ObservableLanguageCoordinate getLanguageCoordinate();
    ObjectProperty<LanguageCoordinateImmutable> languageCoordinateProperty();

    @Override
    ObservableLogicCoordinate getLogicCoordinate();
    ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty();

    /**
     *
     * @return the vertexSort property.
     */
    ObjectProperty<VertexSort> vertexSortProperty();

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                digraphIdentifierConceptsProperty(),
                premiseTypeProperty()
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[] {
                getEdgeStampFilter(),
                getLanguageStampFilter(),
                getVertexStampFilter(),
                getLanguageCoordinate(),
                getLogicCoordinate()};
    }

}
