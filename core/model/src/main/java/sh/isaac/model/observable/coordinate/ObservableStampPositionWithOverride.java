package sh.isaac.model.observable.coordinate;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.observable.override.LongPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableStampPositionWithOverride
        extends ObservableStampPositionBase {

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable stamp position impl.
     *
     * @param stampPosition the stamp position
     */
    public ObservableStampPositionWithOverride(ObservableStampPosition stampPosition, String coordinateName) {
        super(stampPosition, coordinateName);
        if (stampPosition instanceof ObservableStampPositionWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
    }
    public ObservableStampPositionWithOverride(ObservableStampPosition stampPosition) {
        this(stampPosition, stampPosition.getName());
    }

    @Override
    public void setExceptOverrides(StampPositionImmutable updatedCoordinate) {
        int pathConceptNid = updatedCoordinate.getPathForPositionNid();
        if (pathConceptProperty().isOverridden()) {
            pathConceptNid = pathConceptProperty().get().getNid();
        }
        long time = updatedCoordinate.getTime();
        if (timeProperty().isOverridden()) {
            time = timeProperty().get();
        }
        setValue(StampPositionImmutable.make(time, pathConceptNid));
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> pathConceptProperty() {
        return (ObjectPropertyWithOverride<ConceptSpecification>) super.pathConceptProperty();
    }

    @Override
    public LongPropertyWithOverride timeProperty() {
        return (LongPropertyWithOverride) super.timeProperty();
    }

    protected ObjectProperty<ConceptSpecification> makePathConceptProperty(StampPosition stampPosition) {
        ObservableStampPosition observableStampPosition = (ObservableStampPosition) stampPosition;
        return new ObjectPropertyWithOverride<>(observableStampPosition.pathConceptProperty(), this);
    }

    protected LongProperty makeTimeProperty(StampPosition stampPosition) {
        ObservableStampPosition observableStampPosition = (ObservableStampPosition) stampPosition;
        return new LongPropertyWithOverride(observableStampPosition.timeProperty(), this);
    }

    @Override
    public StampPositionImmutable getOriginalValue() {
        return StampPositionImmutable.make(timeProperty().getOriginalValue().longValue(), pathConceptProperty().getOriginalValue());
    }


    @Override
    protected StampPositionImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampPositionImmutable> observable,
                                                                           StampPositionImmutable oldValue, StampPositionImmutable newValue) {
        if (!this.pathConceptProperty().isOverridden()) {
            this.pathConceptProperty().setValue(newValue.getPathForPositionConcept());
        }
        if (!this.timeProperty().isOverridden()) {
            this.timeProperty().set(newValue.getTime());
        }

        return StampPositionImmutable.make(timeProperty().longValue(), pathConceptProperty().get().getNid());
    }

}
