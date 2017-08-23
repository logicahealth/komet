package sh.komet.gui.search.control;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.Collection;
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
        this.propertySheet.setMode(PropertySheet.Mode.NAME);
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);

        AnchorPane.setBottomAnchor(this.propertySheet, 0.0);
        AnchorPane.setTopAnchor(this.propertySheet, 0.0);
        AnchorPane.setLeftAnchor(this.propertySheet, 0.0);
        AnchorPane.setRightAnchor(this.propertySheet, 0.0);

        this.propertySheet.setPropertyEditorFactory(param -> {
            switch (param.getName()){
                case "Path":
                    return createCustomChoiceEditor(MetaData.PATH____ISAAC, param);
                case "Language":
                    return createCustomChoiceEditor(MetaData.LANGUAGE____ISAAC, param);
                case "Classifier":
                    return createCustomChoiceEditor(MetaData.DESCRIPTION_LOGIC_CLASSIFIER____ISAAC, param);
                case "Description Logic":
                    return createCustomChoiceEditor(MetaData.DESCRIPTION_LOGIC_PROFILE____ISAAC, param);
                case "Description Type":
                    return createCustomChoiceEditor(MetaData.DESCRIPTION_TYPE____ISAAC, param);
                case "Dialect":
                    return createCustomChoiceEditor(MetaData.DIALECT_ASSEMBLAGE____ISAAC, param);
            }

            return Editors.createTextEditor(param);
        });

//        SimpleObjectProperty<Callback<PropertySheet.Item, PropertyEditor<?>>> propertyEditorFactory = new SimpleObjectProperty<>(this, "propertyEditor", new DefaultPropertyEditorFactory());
//        this.propertySheet.setPropertyEditorFactory(getItemPropertyEditorCallback(propertyEditorFactory));
    }

//    private Callback<PropertySheet.Item, PropertyEditor<?>> getItemPropertyEditorCallback(SimpleObjectProperty<Callback<PropertySheet.Item, PropertyEditor<?>>> propertyEditorFactory) {
//        return param -> {
//            PropertyEditor<?> editor = propertyEditorFactory.get().call(param);
//
//            //Add listeners to editor
//            editor.getEditor().getUserData().addListener((observable, oldValue, newValue) -> System.out.println(newValue));
//
//            return editor;
//        };
//    }

    private PropertyEditor<?> createCustomChoiceEditor(ConceptSpecification conceptSpecification, PropertySheet.Item param){
        Collection<Integer> collection = new ArrayList<>();
        ConceptChronology concept = Get.concept(conceptSpecification.getConceptSequence());

        Get.taxonomyService().getAllRelationshipOriginSequences(concept.getNid()).forEach(i -> {
            collection.add(i);
            //System.out.println(Get.concept(i));
        });

        return Editors.createChoiceEditor(param, collection);
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
//        this.items.add(new LetItem(
//                "Inferred",
//                "",
//                "Logic Coordinate",
//                Get.conceptDescriptionText(observableLogicCoordinate.getInferredAssemblageSequence()),
//                observableLogicCoordinate.getClass()));
//        this.items.add(new LetItem(
//                "Stated",
//                "",
//                "Logic Coordinate",
//                Get.conceptDescriptionText(observableLogicCoordinate.getStatedAssemblageSequence()),
//                observableLogicCoordinate.getClass()));

//        LetItem classifierItem = new LetItem(
//                "Classifier",
//                "",
//                "Logic Coordinate",
//                Get.conceptDescriptionText(observableLogicCoordinate.getClassifierSequence()),
//                observableLogicCoordinate.getClass(), observableLogicCoordinate.classifierSequenceProperty());
//
//        this.items.add(classifierItem);
//
//        classifierItem.getObservableValue().get().addListener(new ChangeListener<Object>() {
//            @Override
//            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
//                System.out.println("New:" + newValue.toString());
//            }
//        });
//
//        this.items.add(new LetItem(
//                "Description Logic",
//                "",
//                "Logic Coordinate",
//                Get.conceptDescriptionText(observableLogicCoordinate.getDescriptionLogicProfileSequence()),
//                observableLogicCoordinate.getClass(), observableLogicCoordinate.descriptionLogicProfileSequenceProperty()));
    }

    /**
     *
     * @param observableEditCoordinate
     */
    private void parseEditCoordinate(ObservableEditCoordinate observableEditCoordinate){
//        this.items.add(new LetItem(
//                "Module",
//                "",
//                "Edit Coordinate",
//                Get.conceptDescriptionText(observableEditCoordinate.getModuleSequence()),
//                observableEditCoordinate.getClass()));
//        this.items.add(new LetItem(
//                "Path",
//                "",
//                "Edit Coordinate",
//                Get.conceptDescriptionText(observableEditCoordinate.getPathSequence()),
//                observableEditCoordinate.getClass(), observableEditCoordinate.pathSequenceProperty()));
//        this.items.add(new LetItem(
//                "Author",
//                "",
//                "Edit Coordinate",
//                Get.conceptDescriptionText(observableEditCoordinate.getAuthorSequence()),
//                observableEditCoordinate.getClass()));
    }

    /**
     *
     * @param observableLanguageCoordinate
     */
    private void parseLanguageCoordinate(ObservableLanguageCoordinate observableLanguageCoordinate){
        /**
         * Debug for correct oberservable implementation
         */

        observableLanguageCoordinate.languageConceptSequenceProperty().addListener((observable, oldValue, newValue) ->
                System.out.println("Changed Lang to -> " + this.manifold.getPreferredDescriptionText((int)newValue)));


//        this.items.add(new LetItem(
//                "Language",
//                "",
//                "Language Coordinate",
//                Get.conceptDescriptionText(observableLanguageCoordinate.getLanguageConceptSequence()),
//                observableLanguageCoordinate.getClass(), observableLanguageCoordinate.languageConceptSequenceProperty()));
//        this.items.add(new LetItem(
//                "Description Type",
//                "",
//                "Language Coordinate",
//                Get.conceptDescriptionText(observableLanguageCoordinate.getDescriptionTypePreferenceList()[0]),
//                observableLanguageCoordinate.getClass(), observableLanguageCoordinate.descriptionTypePreferenceListProperty()));
//        this.items.add(new LetItem(
//                "Dialect",
//                "",
//                "Language Coordinate",
//                Get.conceptDescriptionText(observableLanguageCoordinate.getDialectAssemblagePreferenceList()[0]),
//                observableLanguageCoordinate.getClass(), observableLanguageCoordinate.dialectAssemblagePreferenceListProperty()));

        this.items.add(new LetItem(
                        "Language",
                        observableLanguageCoordinate.languageConceptSequenceProperty()
        ));


    }

    public PropertySheet getPropertySheet() {
        return propertySheet;
    }

    public Manifold getManifold() {
        return manifold;
    }

    public ObservableList<PropertySheet.Item> getItems() {
        return items;
    }

    private class LetItem implements PropertySheet.Item{

        private String name;
        private IntegerProperty value;

        public LetItem(String name, IntegerProperty value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return this.value.getClass();
        }

        @Override
        public String getCategory() {
            return "";
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public Object getValue() {
            return this.value.getValue();
        }

        @Override
        public void setValue(Object value) {
            this.value.set((int)value);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.of((ObservableValue)this.value);
        }

    }

    private enum Test{
        one, two, three;
    }

}
