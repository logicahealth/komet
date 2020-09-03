package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.LogicCoordinateImmutable;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

public class ObservableLogicCoordinateImpl extends ObservableLogicCoordinateBase  {

    public ObservableLogicCoordinateImpl(LogicCoordinate logicCoordinate, String coordinateName) {
        super(logicCoordinate, coordinateName);
    }

    public ObservableLogicCoordinateImpl(LogicCoordinate logicCoordinate) {
        super(logicCoordinate, "Logic coordinate");
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeClassifierProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.CLASSIFIER_NID_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getClassifier());
    }

    @Override
    public void setExceptOverrides(LogicCoordinateImmutable updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeConceptAssemblageProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getConceptAssemblage());
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeDescriptionLogicProfileProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getDescriptionLogicProfile());
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeInferredAssemblageProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getInferredAssemblage());
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeStatedAssemblageProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getStatedAssemblage());
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeDigraphIdentityProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.DIGRAPH_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getDigraphIdentity());
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makeRootConceptProperty(LogicCoordinate logicCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.ROOT_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinate.getRoot());
    }

    @Override
    protected LogicCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends LogicCoordinateImmutable> observable, LogicCoordinateImmutable oldValue, LogicCoordinateImmutable newValue) {
        this.classifierProperty().setValue(newValue.getClassifier());
        this.conceptAssemblageProperty().setValue(newValue.getConceptAssemblage());
        this.descriptionLogicProfileProperty().setValue(newValue.getDescriptionLogicProfile());
        this.inferredAssemblageProperty().setValue(newValue.getInferredAssemblage());
        this.statedAssemblageProperty().setValue(newValue.getStatedAssemblage());
        this.digraphIdentityProperty().setValue(newValue.getDigraphIdentity());
        this.rootConceptProperty().setValue(newValue.getRoot());
        return newValue;
    }

    @Override
    public LogicCoordinateImmutable getOriginalValue() {
        return getValue();
    }
}
