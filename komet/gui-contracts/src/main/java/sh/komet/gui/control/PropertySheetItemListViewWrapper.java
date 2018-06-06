package sh.komet.gui.control;

import sh.komet.gui.control.concept.ConceptForControlWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;

public class PropertySheetItemListViewWrapper implements PropertySheet.Item {

    private final ObservableIntegerArray observableIntegerArray; //from Coordinate
    private final String name;
    private SimpleListProperty<ConceptForControlWrapper> simpleListProperty;
    private ListChangeListener<ConceptForControlWrapper> listChangeListener;
    private final Manifold manifoldForDisplay;


    public PropertySheetItemListViewWrapper(ObservableIntegerArray observableIntegerArray, String name,
                                            Manifold manifoldForDisplay, int[] conceptList) {
        this.observableIntegerArray = observableIntegerArray;
        this.name = name;
        this.manifoldForDisplay = manifoldForDisplay;
        createListViewObservableList(conceptList);
    }

    private void createListViewObservableList(int[] iArray){

        ObservableList<ConceptForControlWrapper> conceptWrapperList = FXCollections.observableArrayList();
        for(int i = 0; i < iArray.length; i++){
            ConceptForControlWrapper tempWrapper = new ConceptForControlWrapper
                    (manifoldForDisplay, iArray[i]);
            conceptWrapperList.add(tempWrapper);
        }
        this.simpleListProperty = new SimpleListProperty<>(conceptWrapperList);
    }

    public void createCustomListListener(ObservableList<ConceptForControlWrapper> observableList){

        if(name.equals("Dialect") || name.equals("Type")){
            observableList.addListener(this.listChangeListener = c -> {
                for(int i = 0; i < c.getList().size(); i++) {
                   this.observableIntegerArray.set(i, c.getList().get(i).getNid());
                }
            });
        }else if(name.equals("Module")){
            observableList.addListener(this.listChangeListener = c -> {
                this.observableIntegerArray.clear();
                if(c.getList().size() > 0) {
                    int[] iArray = new int[c.getList().size()];
                    for (int i = 0; i < iArray.length; i++) {
                       iArray[i] = c.getList().get(i).getNid();
                    }
                    this.observableIntegerArray.addAll(iArray, 0, iArray.length);
                }
            });
        }
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
