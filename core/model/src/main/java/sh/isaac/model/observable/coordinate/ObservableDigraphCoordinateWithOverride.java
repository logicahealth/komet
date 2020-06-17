package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.DigraphCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.VertexSort;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

public class ObservableDigraphCoordinateWithOverride extends ObservableDigraphCoordinateBase {

    public ObservableDigraphCoordinateWithOverride(ObservableDigraphCoordinateImpl digraphCoordinate, String coordinateName) {
        super(digraphCoordinate, coordinateName);
    }

    public ObservableDigraphCoordinateWithOverride(ObservableDigraphCoordinateImpl digraphCoordinate) {
        super(digraphCoordinate, "Digraph coordinate");
    }

    @Override
    protected SimpleEqualityBasedSetProperty<ConceptSpecification> makeDigraphIdentifierConceptsProperty(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new SetPropertyWithOverride<>(observableDigraphCoordinate.digraphIdentifierConceptsProperty(), this);
    }

    @Override
    protected SimpleObjectProperty<VertexSort> makeVertexSortProperty(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObjectPropertyWithOverride<>(observableDigraphCoordinate.vertexSortProperty(), this);
    }

    @Override
    protected ObjectProperty<PremiseType> makePremiseTypeProperty(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObjectPropertyWithOverride<>(observableDigraphCoordinate.premiseTypeProperty(), this);
    }

    @Override
    protected ObservableStampFilterBase makeLanguageStampFilterProperty(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObservableStampFilterWithOverride(observableDigraphCoordinate.getLanguageStampFilter());
    }

    @Override
    protected ObservableStampFilterBase makeVertexStampFilterProperty(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObservableStampFilterWithOverride(observableDigraphCoordinate.getVertexStampFilter());
    }

    @Override
    protected ObservableStampFilterBase makeEdgeStampFilterProperty(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObservableStampFilterWithOverride(observableDigraphCoordinate.getEdgeStampFilter());
    }

    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinate(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObservableLogicCoordinateWithOverride(observableDigraphCoordinate.getLogicCoordinate());
    }

    @Override
    protected ObservableLanguageCoordinateBase makeLanguageCoordinate(DigraphCoordinate digraphCoordinate) {
        ObservableDigraphCoordinateImpl observableDigraphCoordinate = (ObservableDigraphCoordinateImpl) digraphCoordinate;
        return new ObservableLanguageCoordinateWithOverride(observableDigraphCoordinate.getLanguageCoordinate());
    }
}
