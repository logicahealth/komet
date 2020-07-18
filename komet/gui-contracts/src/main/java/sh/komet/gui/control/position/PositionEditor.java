package sh.komet.gui.control.position;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.model.observable.coordinate.ObservableStampPositionImpl;
import sh.komet.gui.control.property.ViewProperties;
import tornadofx.control.DateTimePicker;

import java.time.ZoneId;

public class PositionEditor implements PropertyEditor<StampPositionImmutable> {

    private final ObservableStampPositionImpl position;
    private final Label positionEditor = new Label("Position editor");
    private final DateTimePicker originDateTimePicker = new DateTimePicker();
    private final ManifoldCoordinate manifoldCoordinate;

    public PositionEditor(ManifoldCoordinate manifoldCoordinate, ObservableStampPositionImpl position) {
        if (position == null) {
            throw new NullPointerException("position cannot be null.");
        }
        this.position = position;
        this.manifoldCoordinate = manifoldCoordinate;
        this.originDateTimePicker.dateTimeValueProperty().addListener((observable, oldValue, newValue) -> {
            position.timeProperty().setValue(newValue.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        });
    }


    @Override
    public Node getEditor() {
        return positionEditor;
    }

    @Override
    public StampPositionImmutable getValue() {
        return position.getValue();
    }

    @Override
    public void setValue(StampPositionImmutable position) {
        this.position.setValue(position);
    }

}
