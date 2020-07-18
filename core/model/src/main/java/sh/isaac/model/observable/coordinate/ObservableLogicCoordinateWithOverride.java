package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.LogicCoordinateImmutable;
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
    public void setExceptOverrides(LogicCoordinateImmutable updatedCoordinate) {
        int rootNid = updatedCoordinate.getRootNid();
        if (rootConceptProperty().isOverridden()) {
            rootNid = rootConceptProperty().get().getNid();
        }

        int classifierNid = updatedCoordinate.getClassifierNid();
        if (classifierProperty().isOverridden()) {
            classifierNid = classifierProperty().get().getNid();
        }
        int conceptAssemblageNid = updatedCoordinate.getConceptAssemblageNid();
        if (conceptAssemblageProperty().isOverridden()) {
            conceptAssemblageNid = conceptAssemblageProperty.get().getNid();
        }
        int descriptionLogicProfileNid = updatedCoordinate.getDescriptionLogicProfileNid();
        if (descriptionLogicProfileProperty().isOverridden()) {
            descriptionLogicProfileNid = descriptionLogicProfileProperty().get().getNid();
        }
        int inferredAssemblageNid = updatedCoordinate.getInferredAssemblageNid();
        if (inferredAssemblageProperty().isOverridden()) {
            inferredAssemblageNid = inferredAssemblageProperty().get().getNid();
        }
        int statedAssemblageNid = updatedCoordinate.getStatedAssemblageNid();
        if (statedAssemblageProperty().isOverridden()) {
            statedAssemblageNid = statedAssemblageProperty().get().getNid();
        }
        int digraphIdentityNid = updatedCoordinate.getDigraphIdentityNid();
        if (digraphIdentityProperty().isOverridden()) {
            digraphIdentityNid = digraphIdentityProperty.get().getNid();
        }
        setValue(LogicCoordinateImmutable.make(classifierNid, descriptionLogicProfileNid, inferredAssemblageNid,
                statedAssemblageNid, conceptAssemblageNid, digraphIdentityNid, rootNid));
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> classifierProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.classifierProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> conceptAssemblageProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.conceptAssemblageProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> descriptionLogicProfileProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.descriptionLogicProfileProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> inferredAssemblageProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.inferredAssemblageProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> statedAssemblageProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.statedAssemblageProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> digraphIdentityProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.digraphIdentityProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> rootConceptProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.rootConceptProperty();
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
