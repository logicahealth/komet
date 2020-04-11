package sh.isaac.api.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;

public interface ObservableDigraphCoordinate extends ObservableCoordinate<DigraphCoordinateImmutable>, DigraphCoordinateProxy {

    SetProperty<ConceptSpecification> digraphIdentifierConceptsProperty();

    ObjectProperty<StampFilterImmutable> vertexStampFilterProperty();

    @Override
    ObservableStampFilter getVertexStampFilter();

    ObjectProperty<StampFilterImmutable> edgeStampFilterProperty();

    @Override
    ObservableStampFilter getEdgeStampFilter();

    ObjectProperty<StampFilterImmutable> languageStampFilterProperty();

    @Override
    ObservableStampFilter getLanguageStampFilter();

    ObjectProperty<PremiseType> premiseTypeProperty();

    ObjectProperty<LanguageCoordinateImmutable> languageCoordinateProperty();
    ObservableLanguageCoordinate getLanguageCoordinate();

    ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty();
    ObservableLogicCoordinate getLogicCoordinate();

}
