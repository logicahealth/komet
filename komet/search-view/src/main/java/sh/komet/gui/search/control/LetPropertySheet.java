package sh.komet.gui.search.control;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.control.*;
import sh.komet.gui.manifold.Manifold;
import tornadofx.control.DateTimePicker;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;

/**
 *
 * @author aks8m
 */

public class LetPropertySheet{

    private PropertySheet propertySheet;
    private ObservableList<PropertySheet.Item> items;
    private Manifold manifoldForDisplay;
    private Manifold manifoldForModification;

    private static final String LANGUAGE = "Language";
    private static final String CLASSIFIER = "Classifier";
    private static final String DESCRIPTION_LOGIC = "Logic";
    private static final String DESCRIPTION_TYPE = "Type";
    private static final String DIALECT = "Dialect";
    private static final String TIME = "Time";
    private static final String MODULE = "Module";
    private static final String PATH = "Path";

    public LetPropertySheet(Manifold manifold){
        this.manifoldForModification = manifold.deepClone();
        this.manifoldForDisplay = manifold;
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

        this.propertySheet.setPropertyEditorFactory(prop -> {
            switch (prop.getName()){
                case PATH:
                    return createCustomChoiceEditor(MetaData.PATH____SOLOR, prop);
                case LANGUAGE:
                    return createCustomChoiceEditor(MetaData.LANGUAGE____SOLOR, prop);
                case CLASSIFIER:
                    return createCustomChoiceEditor(MetaData.DESCRIPTION_LOGIC_CLASSIFIER____SOLOR, prop);
                case DESCRIPTION_LOGIC:
                    return createCustomChoiceEditor(MetaData.DESCRIPTION_LOGIC_PROFILE____SOLOR, prop);
                case MODULE:
                    PropertyEditor<?> multiselectPropertyEditor = new AbstractPropertyEditor<ObservableList<ConceptForControlWrapper>,
                            ListView<ConceptForControlWrapper>>(prop, new ListView<>()) {

                        {
                            PropertySheetItemListViewWrapper listViewWrapper = ((PropertySheetItemListViewWrapper) prop);
                            super.getEditor().setId(UUID.randomUUID().toString());
                            super.getEditor().setItems((ObservableList<ConceptForControlWrapper>) listViewWrapper.getValue());
                            super.getEditor().setPrefHeight(((((ObservableList) listViewWrapper.getValue())
                                    .size() * 26) + 2));
                            listViewWrapper.createCustomListListener(super.getEditor().getSelectionModel().getSelectedItems());
                            super.getEditor().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                        }

                        @Override
                        protected ObservableValue<ObservableList<ConceptForControlWrapper>> getObservableValue() {
                            return getEditor().itemsProperty();
                        }

                        @Override
                        public void setValue(ObservableList<ConceptForControlWrapper> value) {
                            getEditor().setItems(value);
                        }
                    };

                    return multiselectPropertyEditor;
                case DESCRIPTION_TYPE:
                case DIALECT:
                    PropertyEditor<?> preferencePropertyEditor = new AbstractPropertyEditor<ObservableList<ConceptForControlWrapper>,
                            ListView<ConceptForControlWrapper>>(prop, new ListView<>()) {

                        {
                            PropertySheetItemListViewWrapper listViewWrapper = ((PropertySheetItemListViewWrapper) prop);

                            super.getEditor().setId(UUID.randomUUID().toString());
                            super.getEditor().setItems((ObservableList<ConceptForControlWrapper>) listViewWrapper.getValue());
                            super.getEditor().setPrefHeight(((((ObservableList) listViewWrapper.getValue())
                                    .size() * 26) + 2));
                            super.getEditor().setCellFactory(cell -> new CellConceptForDragDropControlWrapper());
                            listViewWrapper.createCustomListListener(super.getEditor().getItems());
                        }

                        @Override
                        protected ObservableValue<ObservableList<ConceptForControlWrapper>> getObservableValue() {
                            return getEditor().itemsProperty();
                        }

                        @Override
                        public void setValue(ObservableList<ConceptForControlWrapper> value) {
                            getEditor().setItems(value);
                        }
                    };

                    return preferencePropertyEditor;
                case TIME:
                    PropertySheetItemDateTimeWrapper dateTimeWrapper = ((PropertySheetItemDateTimeWrapper) prop);
                    PropertyEditor<?> dateTimePropertyEditor = new AbstractPropertyEditor<LocalDateTime, DateTimePicker>
                            (prop, new DateTimePicker()) {

                        {
                            super.getEditor().setDateTimeValue((LocalDateTime)dateTimeWrapper.getValue());
                        }

                        @Override
                        protected ObservableValue<LocalDateTime> getObservableValue() {
                            return getEditor().dateTimeValueProperty();
                        }

                        @Override
                        public void setValue(LocalDateTime value) {
                            getEditor().setDateTimeValue(value);
                        }
                    };

                    return dateTimePropertyEditor;
            }

            return Editors.createTextEditor(prop);
        });
    }


    public PropertySheet getPropertySheet() {
        return propertySheet;
    }

    public Manifold getManifold() {
        return manifoldForModification;
    }


    private PropertyEditor<?> createCustomChoiceEditor(ConceptSpecification conceptSpecification, PropertySheet.Item prop){
       try {
          Collection<ConceptForControlWrapper> collection = new ArrayList<>();
          ConceptChronology concept = Get.concept(conceptSpecification.getNid());
          
          TaxonomySnapshotService taxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldForDisplay).get();
          for (int i: taxonomySnapshot.getTaxonomyChildNids(concept.getNid())) {
             ConceptForControlWrapper propertySheetItemConceptWrapper =
                     new ConceptForControlWrapper(this.manifoldForDisplay, i);
             collection.add(propertySheetItemConceptWrapper);
          }
          
          
          return Editors.createChoiceEditor(prop, collection);
       } catch (InterruptedException | ExecutionException ex) {
          throw new UnsupportedOperationException(ex);
       }
    }

    /**
     * Add to the items Observable list of PropertySheet Items
     */
    private void buildPropertySheetItems() {

        buildListOfAllModules();

        this.items.add(new PropertySheetItemDateTimeWrapper(TIME, this.manifoldForModification.getStampCoordinate()
                .stampPositionProperty().get().timeProperty()));
        this.items.add(new PropertySheetItemListViewWrapper(
                this.manifoldForModification.getStampCoordinate().moduleNidProperty().get(),
                MODULE,
                this.manifoldForModification, buildListOfAllModules()));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                PATH,
                this.manifoldForModification.getStampCoordinate().stampPositionProperty().get().stampPathNidProperty()
        ));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                LANGUAGE,
                this.manifoldForModification.getLanguageCoordinate().languageConceptNidProperty()
        ));
        this.items.add(new PropertySheetItemListViewWrapper(
                this.manifoldForModification.getLanguageCoordinate().dialectAssemblagePreferenceListProperty().get(),
                DIALECT,
                this.manifoldForDisplay,
                this.manifoldForModification.getLanguageCoordinate().dialectAssemblagePreferenceListProperty().get().toArray(null)));
        this.items.add(new PropertySheetItemListViewWrapper(
                this.manifoldForModification.getLanguageCoordinate().descriptionTypePreferenceListProperty().get(),
                DESCRIPTION_TYPE,
                this.manifoldForDisplay,
                this.manifoldForModification.getLanguageCoordinate().descriptionTypePreferenceListProperty().get().toArray(null)));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                CLASSIFIER,
                this.manifoldForModification.getLogicCoordinate().classifierNidProperty()
        ));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                DESCRIPTION_LOGIC,
                this.manifoldForModification.getLogicCoordinate().descriptionLogicProfileNidProperty()
        ));

    }

    private int[] buildListOfAllModules(){
       try {
          int[] arrayOfModules;
          ObservableIntegerArray manifoldModules = this.manifoldForDisplay.getManifoldCoordinate().getStampCoordinate()
                  .moduleNidProperty().get();
          
          if (manifoldModules.size() == 0) {
             ArrayList<Integer> moduleNIDs = new ArrayList<>();
             ObservableIntegerArray moduleIntegerArray = FXCollections.observableIntegerArray();
             
             TaxonomySnapshotService taxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldForDisplay).get();
             for (int i : taxonomySnapshot.getTaxonomyChildNids(MetaData.MODULE____SOLOR.getNid())) {
                moduleNIDs.add(i);
             }
             arrayOfModules = new int[moduleNIDs.size()];
             for (int i = 0; i < arrayOfModules.length; i++) {
                arrayOfModules[i] = moduleNIDs.get(i);
             }
          } else {
             arrayOfModules = manifoldModules.toArray(null);
          }
          return arrayOfModules;
       } catch (InterruptedException | ExecutionException ex) {
          throw new RuntimeException(ex);
       }
    }

}
