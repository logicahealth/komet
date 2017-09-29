package sh.komet.gui.control;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.scene.control.ListView;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;

public class PropertySheetItemListViewWrapper implements PropertySheet.Item {

    private final ObservableIntegerArray observableIntegerArray; //from Coordinate
    private final String name;
    private final SimpleListProperty<ConceptForControlWrapper> simpleListProperty;
    private ListChangeListener<ConceptForControlWrapper> listChangeListener;


    public PropertySheetItemListViewWrapper(ObservableIntegerArray observableIntegerArray, String name,
                                            Manifold manifoldForDisplay) {
        this.observableIntegerArray = observableIntegerArray;
        this.name = name;

        ObservableList<ConceptForControlWrapper> conceptWrapperList = FXCollections.observableArrayList();
        for(int i = 0; i < this.observableIntegerArray.size(); i++){
            ConceptForControlWrapper tempWrapper = new ConceptForControlWrapper
                    (manifoldForDisplay, this.observableIntegerArray.get(i));
            conceptWrapperList.add(tempWrapper);
        }
        this.simpleListProperty = new SimpleListProperty<>(conceptWrapperList);
    }

    public void addDragAndDropListener(){
        this.simpleListProperty.addListener(this.listChangeListener = c -> {
            for(int i = 0; i < c.getList().size(); i++)
                this.observableIntegerArray.set(i, c.getList().get(i).getConceptSequence());
        });
    }

    public void addMultiselectListener(ObservableList<ConceptForControlWrapper> observableList){
        observableList.addListener(this.listChangeListener = c -> {
            System.out.println("Selected: " + c.getList().toString());
            this.observableIntegerArray.clear();
            if(c.getList().size() > 0) {
                int[] iArray = new int[c.getList().size()];
                for (int i = 0; i < iArray.length; i++)
                    iArray[i] = c.getList().get(i).getConceptSequence();
                this.observableIntegerArray.addAll(iArray, 0, iArray.length);
            }
        });
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
        return this.simpleListProperty.getValue();
    }

    @Override
    public void setValue(Object value) {
        //TODO: Currently this isn't fired from ListView when ObservableListChanges in PropterySheet
        /*for(int i = 0; i < c.getList().size(); i++)
            this.observableIntegerArray.set(i, c.getList().get(i).getConceptSequence());*/
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.simpleListProperty);
    }
}
