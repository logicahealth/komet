package sh.komet.gui.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.control.ConceptForControlWrapper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertySheetItemPreferenceWrapper implements PropertySheet.Item {

    private final ObservableIntegerArray observableIntegerArray; //from Coordinate
    private final ObservableList<ConceptForControlWrapper> list = FXCollections.observableArrayList();
    private final ObservableValue<ObservableList<ConceptForControlWrapper>> observableWrapper;
    private final String name;



    public PropertySheetItemPreferenceWrapper(ObservableIntegerArray observableIntegerArray, String name, Manifold manifoldForDisplay) {
        this.observableIntegerArray = observableIntegerArray;
        for(int i = 0; i < this.observableIntegerArray.size(); i++){
            list.add(new ConceptForControlWrapper(manifoldForDisplay, this.observableIntegerArray.get(i)));
        }
        this.name = name;
        observableWrapper = new SimpleObjectProperty(this.list);


        this.list.addListener((ListChangeListener<ConceptForControlWrapper>) c -> {
            for(int i = 0; i < c.getList().size(); i++){
                this.observableIntegerArray.set(i, c.getList().get(i).getConceptSequence());
            }
        });

    }

    public ObservableList<ConceptForControlWrapper> getObservableWrapper() {
        return this.observableWrapper.getValue();
    }

    public ObservableValue<ObservableList<ConceptForControlWrapper>> observableWrapperProperty() {
        return observableWrapper;
    }

    public ObservableList<ConceptForControlWrapper> getList() {
        return list;
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
    public ObservableList<ConceptForControlWrapper> getValue() {
        return this.observableWrapper.getValue();
    }

    @Override
    public void setValue(Object value) {
        ((SimpleObjectProperty) this.observableWrapper).setValue(value);

    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.observableWrapper);
    }
}
