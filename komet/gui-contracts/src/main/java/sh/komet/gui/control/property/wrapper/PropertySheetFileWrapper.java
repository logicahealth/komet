package sh.komet.gui.control.property.wrapper;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.coordinate.ManifoldCoordinate;

import java.util.Optional;

public class PropertySheetFileWrapper implements PropertySheet.Item {
    public enum FileOperation { READ, WRITE, OVERWRITE };

    private final String name;
    private final StringProperty textProperty;
    private final FileOperation fileOperation;

    public PropertySheetFileWrapper(String name, StringProperty textProperty, FileOperation fileOperation) {
        if (textProperty == null) {
            throw new NullPointerException("textProperty cannot be null");
        }
        this.name = name;
        this.textProperty = textProperty;
        this.fileOperation = fileOperation;
    }

    public PropertySheetFileWrapper(ManifoldCoordinate manifoldCoordinate,
                                    StringProperty textProperty,
                                    FileOperation fileOperation) {
        this(manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(textProperty.getName())),
                textProperty, fileOperation);
    }

    public FileOperation getFileOperation() {
        return fileOperation;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Select a file for " + fileOperation;
    }

    @Override
    public String getValue() {
        return textProperty.get();
    }

    @Override
    public void setValue(Object value) {
        textProperty.setValue((String) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(textProperty);
    }

}
