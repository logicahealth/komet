package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableLogicCoordinateWithOverride extends ObservableLogicCoordinateBase {

    public ObservableLogicCoordinateWithOverride(ObservableLogicCoordinate logicCoordinate, String coordinateName) {
        super(logicCoordinate, coordinateName);
    }

    public ObservableLogicCoordinateWithOverride(ObservableLogicCoordinate logicCoordinate) {
        super(logicCoordinate, logicCoordinate.getName());
    }


    @Override
    protected ObjectProperty<ConceptSpecification> makeClassifierProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.classifierProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeConceptAssemblageProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.conceptAssemblageProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeDescriptionLogicProfileProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.descriptionLogicProfileProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeInferredAssemblageProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.inferredAssemblageProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeStatedAssemblageProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.statedAssemblageProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeDigraphIdentityProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.digraphIdentityProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeRootConceptProperty(LogicCoordinate logicCoordinate) {
        ObservableLogicCoordinate overriddenCoordinate = (ObservableLogicCoordinate) logicCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.rootConceptProperty(), this);
    }
}
