package sh.komet.gui.search.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.control.ConceptForControlWrapper;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;

public class PropertySheetItemPreferenceWrapper implements PropertySheet.Item {

    private final ObjectProperty<ObservableIntegerArray> observableIntegerArray; //from Coordinate
    private final ObservableList<ConceptForControlWrapper> list = FXCollections.observableArrayList();
    private final SimpleObjectProperty observableWrapper;
    private final String name;

    public PropertySheetItemPreferenceWrapper(ObjectProperty<ObservableIntegerArray> observableIntegerArray, String name, Manifold manifoldForDisplay) {
        this.observableIntegerArray = observableIntegerArray;
        for(int i = 0; i < this.observableIntegerArray.get().size(); i++){
            list.add(new ConceptForControlWrapper(manifoldForDisplay, this.observableIntegerArray.get().get(i)));
        }
        this.name = name;
        observableWrapper = new SimpleObjectProperty(observableIntegerArray.get());
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
        return "Tooltip for the property sheet item we are editing. ";
    }

    @Override
    public Object getValue() {
        return this.observableWrapper.get();
    }

    @Override
    public void setValue(Object value) {
        this.observableWrapper.setValue((ObservableIntegerArray) value);
        this.observableIntegerArray.setValue((ObservableIntegerArray) value);

    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.observableIntegerArray);
    }

    public ObservableList<ConceptForControlWrapper> getList() {
        return list;
    }
}
