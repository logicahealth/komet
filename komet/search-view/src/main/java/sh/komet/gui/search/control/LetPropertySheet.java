package sh.komet.gui.search.control;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.Get;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.komet.gui.manifold.Manifold;

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
        this.propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);
        AnchorPane.setBottomAnchor(this.propertySheet, 0.0);
        AnchorPane.setTopAnchor(this.propertySheet, 0.0);
        AnchorPane.setLeftAnchor(this.propertySheet, 0.0);
        AnchorPane.setRightAnchor(this.propertySheet, 0.0);
    }

    /**
     * Add to the items Observable list of PropertySheet Items
     */
    private void buildPropertySheetItems(){
        parseStampCoordinate(this.manifold.getStampCoordinate());
        parseLogicCoordinate(this.manifold.getLogicCoordinate());
        parseEditCoordinate(this.manifold.getEditCoordinate());
        parseLanguageCoordinate(this.manifold.getLanguageCoordinate());
    }

    /**
     *
     * @param observableStampCoordinate
     */
    private void parseStampCoordinate(ObservableStampCoordinate observableStampCoordinate){


    }

    /**
     *
     * @param observableLogicCoordinate
     */
    private void parseLogicCoordinate(ObservableLogicCoordinate observableLogicCoordinate){
        this.items.add(new LetItem(
                "Inferred",
                "",
                "Logic Coordinate",
                Get.conceptDescriptionText(observableLogicCoordinate.getInferredAssemblageSequence()),
                observableLogicCoordinate.getClass()));
        this.items.add(new LetItem(
                "Stated",
                "",
                "Logic Coordinate",
                Get.conceptDescriptionText(observableLogicCoordinate.getStatedAssemblageSequence()),
                observableLogicCoordinate.getClass()));
        this.items.add(new LetItem(
                "Classifier",
                "",
                "Logic Coordinate",
                Get.conceptDescriptionText(observableLogicCoordinate.getClassifierSequence()),
                observableLogicCoordinate.getClass()));

        this.items.add(new LetItem(
                "Description Logic",
                "",
                "Logic Coordinate",
                Get.conceptDescriptionText(observableLogicCoordinate.getDescriptionLogicProfileSequence()),
                observableLogicCoordinate.getClass()));
    }

    /**
     *
     * @param observableEditCoordinate
     */
    private void parseEditCoordinate(ObservableEditCoordinate observableEditCoordinate){
        this.items.add(new LetItem(
                "Module",
                "",
                "Edit Coordinate",
                Get.conceptDescriptionText(observableEditCoordinate.getModuleSequence()),
                observableEditCoordinate.getClass()));
        this.items.add(new LetItem(
                "Path",
                "",
                "Edit Coordinate",
                Get.conceptDescriptionText(observableEditCoordinate.getPathSequence()),
                observableEditCoordinate.getClass()));
        this.items.add(new LetItem(
                "Author",
                "",
                "Edit Coordinate",
                Get.conceptDescriptionText(observableEditCoordinate.getAuthorSequence()),
                observableEditCoordinate.getClass()));
    }

    /**
     *
     * @param observableLanguageCoordinate
     */
    private void parseLanguageCoordinate(ObservableLanguageCoordinate observableLanguageCoordinate){
        this.items.add(new LetItem(
                "Language",
                "",
                "Language Coordinate",
                Get.conceptDescriptionText(observableLanguageCoordinate.getLanguageConceptSequence()),
                observableLanguageCoordinate.getClass()));
        this.items.add(new LetItem(
                "Description Type Preference",
                "",
                "Language Coordinate",
                observableLanguageCoordinate.getDescriptionTypePreferenceList(),
                observableLanguageCoordinate.getClass()));
        this.items.add(new LetItem(
                "Dialect",
                "",
                "Language Coordinate",
                observableLanguageCoordinate.getDialectAssemblagePreferenceList(),
                observableLanguageCoordinate.getClass()));
    }

    public PropertySheet getPropertySheet() {
        return propertySheet;
    }

    public ObservableList<PropertySheet.Item> getItems() {
        return items;
    }

    private class LetItem implements PropertySheet.Item{

        private String name;
        private String description;
        private String category;
        private Object value;
        private Class<?> type;

        public LetItem(String name, String description, String category, Object value, Class<?> type) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.value = value;
            this.type = type;
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

}
