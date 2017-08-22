package sh.komet.gui.search.control;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;

import java.time.LocalDate;
import java.util.Optional;

public class LetPropertySheet {

    private PropertySheet propertySheet;
    private ObservableList<PropertySheet.Item> items;

    public LetPropertySheet(){
        items = FXCollections.observableArrayList();
        buildPropertySheetItems();
        this.propertySheet = new PropertySheet(this.items);
    }

    private void buildPropertySheetItems(){
        items.add(new Item("String:", "String Example", "Category 1", "Sample String Value"));
        items.add(new Item("Date:", "Date Example", "Category 1", LocalDate.now()));
        items.add(new Item("Boolean:", "Boolean Example", "Category 2", true));
        items.add(new Item("Enum:", "Enum Example", "Category 2", Days.Monday));
    }

    public PropertySheet getPropertySheet() {
        return propertySheet;
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
