package sh.komet.gui.control.property.wrapper;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.model.observable.coordinate.ObservableStampPositionImpl;

import java.util.Optional;

public class PropertySheetPositionWrapper implements PropertySheet.Item {

    private final ObservableStampPositionImpl stampPosition = new ObservableStampPositionImpl(Coordinates.Position.LatestOnDevelopment());
    private final String name;

    public PropertySheetPositionWrapper(String name, SimpleObjectProperty<StampPositionImmutable> positionProperty) {
        if (positionProperty == null) {
            throw new NullPointerException("positionProperty cannot be null");
        }
        this.name = name;
        this.stampPosition.setValue(positionProperty.getValue());
        this.stampPosition.bindBidirectional(positionProperty);
    }
    @Override
    public Class<?> getType() {
        return ObservableSet.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "Specify the position set";
    }

    @Override
    public StampPositionImmutable getValue() {
        return this.stampPosition.getValue();
    }

    @Override
    public void setValue(Object value) {
        this.stampPosition.setValue((StampPositionImmutable) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.stampPosition);
    }

    public ObservableStampPositionImpl getObservableStampPosition() {
        return this.getObservableStampPosition();
    }

}
