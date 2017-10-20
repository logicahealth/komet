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
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import sh.isaac.api.TaxonomySnapshotService;

/**
 *
 * @author aks8m
 */

public class LetPropertySheet {

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
    private static final String AUTHOR = "Author";
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
                            listViewWrapper.addMultiselectListener(super.getEditor().getSelectionModel().getSelectedItems());
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
                            listViewWrapper.addDragAndDropListener();
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
                    PropertySheetItemDateWrapper dateWrapper = ((PropertySheetItemDateWrapper) prop);
                    PropertyEditor<?> dateTimePropertyEditor = new AbstractPropertyEditor<LocalDateTime, DateTimePicker>
                            (prop, new DateTimePicker()) {

                        {
                            super.getEditor().setDateTimeValue((LocalDateTime)dateWrapper.getValue());
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

    private PropertyEditor<?> createCustomChoiceEditor(ConceptSpecification conceptSpecification, PropertySheet.Item prop){
       try {
          Collection<ConceptForControlWrapper> collection = new ArrayList<>();
          ConceptChronology concept = Get.concept(conceptSpecification.getConceptSequence());
          
          TaxonomySnapshotService taxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldForDisplay).get();
          for (int i: taxonomySnapshot.getTaxonomyChildSequences(concept.getNid())) {
             ConceptForControlWrapper propertySheetItemConceptWrapper =
                     new ConceptForControlWrapper(this.manifoldForDisplay, i);
             collection.add(propertySheetItemConceptWrapper);
          }
          
          return Editors.createChoiceEditor(prop, collection);
       } catch (InterruptedException | ExecutionException ex) {
          throw new RuntimeException(ex);
       }
    }

    /**
     * Add to the items Observable list of PropertySheet Items
     */
    private void buildPropertySheetItems() {

        buildAndSetModulesForMultiSelect();

        this.items.add(new PropertySheetItemDateWrapper(TIME, this.manifoldForModification.getStampCoordinate()
                .stampPositionProperty().get().timeProperty()));
        this.items.add(new PropertySheetItemListViewWrapper(
                this.manifoldForModification.getStampCoordinate().moduleSequencesProperty().get(),
                MODULE,
                this.manifoldForModification));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                PATH,
                this.manifoldForModification.getStampCoordinate().stampPositionProperty().get().stampPathSequenceProperty()
        ));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                LANGUAGE,
                this.manifoldForModification.getLanguageCoordinate().languageConceptSequenceProperty()
        ));
        this.items.add(new PropertySheetItemListViewWrapper(
                this.manifoldForModification.getLanguageCoordinate().dialectAssemblagePreferenceListProperty().get(),
                DIALECT,
                this.manifoldForDisplay));
        this.items.add(new PropertySheetItemListViewWrapper(
                this.manifoldForModification.getLanguageCoordinate().descriptionTypePreferenceListProperty().get(),
                DESCRIPTION_TYPE,
                this.manifoldForDisplay));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                CLASSIFIER,
                this.manifoldForModification.getLogicCoordinate().classifierSequenceProperty()
        ));
        this.items.add(new PropertySheetItemConceptWrapper(this.manifoldForDisplay,
                DESCRIPTION_LOGIC,
                this.manifoldForModification.getLogicCoordinate().descriptionLogicProfileSequenceProperty()
        ));
    }

    public PropertySheet getPropertySheet() {
        return propertySheet;
    }

    public Manifold getManifold() {
        return manifoldForModification;
    }

    private void buildAndSetModulesForMultiSelect(){

        if(this.manifoldForModification.getStampCoordinate().moduleSequencesProperty().get().size() == 0) {
           try {
              ArrayList<Integer> moduleNIDs = new ArrayList<>();
              ObservableIntegerArray moduleIntegerArray = FXCollections.observableIntegerArray();
              TaxonomySnapshotService taxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldForDisplay).get();
              for (int i: taxonomySnapshot.getTaxonomyChildSequences(MetaData.MODULE____SOLOR.getNid())) {
                 moduleNIDs.add(i);
              }
              int[] iArray = new int[moduleNIDs.size()];
              for (int i = 0; i < iArray.length; i++) {
                 iArray[i] = moduleNIDs.get(i);
              }
              moduleIntegerArray.addAll(iArray, 0, moduleNIDs.size());
              this.manifoldForModification.getStampCoordinate().moduleSequencesProperty().set(moduleIntegerArray);
           } catch (InterruptedException | ExecutionException ex) {
              throw new RuntimeException(ex);
           }
        }

    }

}
