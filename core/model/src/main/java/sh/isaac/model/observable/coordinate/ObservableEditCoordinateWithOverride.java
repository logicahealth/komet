package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableEditCoordinateWithOverride
        extends ObservableEditCoordinateBase {

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable edit coordinate impl.
     *
     * @param editCoordinate the edit coordinate
     */
    public ObservableEditCoordinateWithOverride(ObservableEditCoordinate editCoordinate, String coordinateName) {
        super(editCoordinate, coordinateName);
        if (editCoordinate instanceof ObservableEditCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }

    }

    @Override
    protected EditCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends EditCoordinateImmutable> observable, EditCoordinateImmutable oldValue, EditCoordinateImmutable newValue) {
        if (!this.authorForChangesProperty().isOverridden()) {
            this.authorForChangesProperty().setValue(newValue.getAuthorForChanges());
        }
        if (!this.defaultModuleProperty().isOverridden()) {
            this.defaultModuleProperty().setValue(newValue.getDefaultModule());
        }
        if (!this.destinationModuleProperty().isOverridden()) {
            this.destinationModuleProperty().setValue(newValue.getDestinationModule());
        }
        if (!this.promotionPathProperty().isOverridden()) {
            this.promotionPathProperty().setValue(newValue.getPromotionPath());
        }
        /*
int authorNid, int defaultModuleNid, int promotionPathNid, int destinationModuleNid
         */
        return EditCoordinateImmutable.make(this.authorForChangesProperty().get().getNid(),
                this.defaultModuleProperty().get().getNid(),
                this.promotionPathProperty().get().getNid(),
                this.destinationModuleProperty().get().getNid());
    }

    public ObservableEditCoordinateWithOverride(ObservableEditCoordinate editCoordinate) {
        this(editCoordinate, editCoordinate.getName());
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> authorForChangesProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.authorForChangesProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> defaultModuleProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.defaultModuleProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> promotionPathProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.promotionPathProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> destinationModuleProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.destinationModuleProperty();
    }

    @Override
    public void setExceptOverrides(EditCoordinateImmutable updatedCoordinate) {
        if (hasOverrides()) {
            ConceptSpecification author = updatedCoordinate.getAuthorForChanges();
            if (authorForChangesProperty().isOverridden()) {
                author = authorForChangesProperty().get();
            };
            ConceptSpecification defaultModule = updatedCoordinate.getDefaultModule();
            if (defaultModuleProperty().isOverridden()) {
                defaultModule = defaultModuleProperty().get();
            };
            ConceptSpecification promotionPath = updatedCoordinate.getPromotionPath();
            if (promotionPathProperty().isOverridden()) {
                promotionPath = promotionPathProperty().get();
            };
            ConceptSpecification destinationModule = updatedCoordinate.getDestinationModule();
            if (destinationModuleProperty().isOverridden()) {
                destinationModule = destinationModuleProperty().get();
            };
            setValue(EditCoordinateImmutable.make(author, defaultModule, promotionPath, destinationModule));
        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makePromotionPathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.promotionPathProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDefaultModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.defaultModuleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorForChangesProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.authorForChangesProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDestinationModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.destinationModuleProperty(), this);
    }

    @Override
    public EditCoordinateImmutable getOriginalValue() {
        return EditCoordinateImmutable.make(authorForChangesProperty().getOriginalValue(),
                defaultModuleProperty().getOriginalValue(),
                promotionPathProperty().getOriginalValue(),
                destinationModuleProperty().getOriginalValue());
    }
}

