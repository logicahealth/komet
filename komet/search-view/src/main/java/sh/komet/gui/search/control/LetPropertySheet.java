package sh.komet.gui.search.control;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.manifold.Manifold;

import java.time.LocalDate;
import java.util.Optional;

/**
 *
 * @author aks8m
 */

public class LetPropertySheet {

    private PropertySheet propertySheet;
    private ObservableList<PropertySheet.Item> items;
    private Manifold manifold;

    public LetPropertySheet(Manifold manifold){
        this.manifold = manifold;
        items = FXCollections.observableArrayList();
        buildPropertySheetItems();
        this.propertySheet = new PropertySheet(this.items);
        AnchorPane.setBottomAnchor(this.propertySheet, 0.0);
        AnchorPane.setTopAnchor(this.propertySheet, 0.0);
        AnchorPane.setLeftAnchor(this.propertySheet, 0.0);
        AnchorPane.setRightAnchor(this.propertySheet, 0.0);
    }

    /**
     * Purpose: Add to the items Observable list of PropertySheet Items
     */
    private void buildPropertySheetItems(){
        items.add(new Item("String:", "String Example", "Category 1", "Sample String Value"));
        items.add(new Item("Date:", "Date Example", "Category 1", LocalDate.now()));
        items.add(new Item("Boolean:", "Boolean Example", "Category 2", true));
        items.add(new Item("Enum:", "Enum Example", "Category 2", Days.Monday));
    }

    public PropertySheet getPropertySheet() {
        return propertySheet;
    }

    public ObservableList<PropertySheet.Item> getItems() {
        return items;
    }

    private class Item implements PropertySheet.Item{

        private String name;
        private String description;
        private String category;
        private Object value;

        public Item(String name, String description, String category, Object value) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return this.value.getClass();
        }

        @Override
        public String getCategory() {
            return this.category;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Object getValue() {
            return this.value;
        }

        @Override
        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.empty();
        }
    }

    private enum Days{
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday;
    }
}
