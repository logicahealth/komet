package sh.komet.gui.control;

import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.coordinate.StampPositionImmutable;

import java.util.Optional;

public class PropertySheetPositionListWrapper implements PropertySheet.Item {

    private final ListProperty<StampPositionImmutable> positionListProperty;
    private final String name;

    public PropertySheetPositionListWrapper(String name, ListProperty<StampPositionImmutable> positionListProperty) {
        this.positionListProperty = positionListProperty;
        this.name = name;
    }

    @Override
    public Class<?> getType() {
        return null;
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
        return "Set of positions";
    }

    @Override
    public ObservableList<StampPositionImmutable> getValue() {
        return positionListProperty.get();
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            positionListProperty.get().clear();
        } else {
            ObservableList list = (ObservableList) value;
            positionListProperty.set(list);
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(positionListProperty);
    }
}
