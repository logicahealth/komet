/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.control.property;

import java.time.LocalDateTime;
import sh.komet.gui.control.concept.PropertySheetItemConceptNidWrapper;
import sh.komet.gui.control.concept.ConceptLabel;
import sh.komet.gui.control.concept.ConceptForControlWrapper;
import sh.komet.gui.control.measure.PropertySheetMeasureWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.model.statement.MeasureImpl;
import sh.isaac.model.statement.ResultImpl;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.control.PropertySheetItemDateTimeWrapper;
import sh.komet.gui.control.PropertySheetItemStringListWrapper;
import sh.komet.gui.control.PropertySheetPasswordWrapper;
import sh.komet.gui.control.PropertySheetStampPrecedenceWrapper;
import sh.komet.gui.control.PropertySheetStatusSetWrapper;
import sh.komet.gui.control.PropertySheetStatusWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.circumstance.CircumstanceEditor;
import sh.komet.gui.control.circumstance.PropertySheetCircumstanceWrapper;
import sh.komet.gui.control.concept.AssemblageListEditor;
import sh.komet.gui.control.concept.ConceptListEditor;
import sh.komet.gui.control.concept.ConceptSpecificationEditor;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptSetWrapper;
import sh.komet.gui.control.concept.PropertySheetItemAssemblageListWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapperEditor;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapperNoSearch;
import sh.komet.gui.control.list.ListEditor;
import sh.komet.gui.control.list.PropertySheetListWrapper;
import sh.komet.gui.control.measure.MeasureEditor;
import sh.komet.gui.control.result.PropertySheetResultWrapper;
import sh.komet.gui.control.result.ResultEditor;
import sh.komet.gui.control.versiontype.PropertySheetItemVersionTypeWrapper;
import sh.komet.gui.manifold.HistoryRecord;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.time.KometDateTimePicker;

/**
 *
 * @author kec
 */
public class PropertyEditorFactory implements Callback<PropertySheet.Item, PropertyEditor<?>> {

    Manifold manifoldForDisplay;

    public PropertyEditorFactory(Manifold manifoldForDisplay) {
        this.manifoldForDisplay = manifoldForDisplay;
    }

    @Override
    public PropertyEditor<?> call(PropertySheet.Item propertySheetItem) {
        if (propertySheetItem instanceof PropertySheetItemConceptNidWrapper) {
            return createCustomChoiceEditor((PropertySheetItemConceptNidWrapper) propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetItemConceptConstraintWrapper) {
            return new PropertySheetItemConceptWrapperEditor(manifoldForDisplay);
        } else if (propertySheetItem instanceof PropertySheetItemConceptWrapper) {
            return new ConceptSpecificationEditor((PropertySheetItemConceptWrapper) propertySheetItem, manifoldForDisplay);
        } else if (propertySheetItem instanceof PropertySheetItemConceptWrapperNoSearch) {
            PropertySheetItemConceptWrapperNoSearch propertySheetItemNoSearch = 
                    (PropertySheetItemConceptWrapperNoSearch) propertySheetItem;
            return propertySheetItemNoSearch.getEditor();
        } else if (propertySheetItem instanceof PropertySheetStatusWrapper) {
            return Editors.createChoiceEditor(propertySheetItem, Status.makeActiveAndInactiveSet());
        } else if (propertySheetItem instanceof PropertySheetTextWrapper) {
            return Editors.createTextEditor(propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetItemVersionTypeWrapper) {
            return Editors.createChoiceEditor(propertySheetItem, Arrays.asList(VersionType.values()));
        } else if (propertySheetItem instanceof PropertySheetItemStringListWrapper) {
            PropertySheetItemStringListWrapper wrappedItem = (PropertySheetItemStringListWrapper) propertySheetItem;
            return Editors.createChoiceEditor(propertySheetItem, wrappedItem.getAllowedValues());
        } else if (propertySheetItem instanceof PropertySheetMeasureWrapper) {
            PropertySheetMeasureWrapper measureWrapper = (PropertySheetMeasureWrapper) propertySheetItem;
            MeasureEditor measureEditor = new MeasureEditor(manifoldForDisplay);
            measureEditor.setValue((MeasureImpl) measureWrapper.getValue());
            return measureEditor;
        } else if (propertySheetItem instanceof PropertySheetCircumstanceWrapper) {
            PropertySheetCircumstanceWrapper circumstanceWrapper = (PropertySheetCircumstanceWrapper) propertySheetItem;
            return new CircumstanceEditor(circumstanceWrapper.getObservableValue().get(), manifoldForDisplay);
        } else if (propertySheetItem instanceof PropertySheetResultWrapper) {
            PropertySheetResultWrapper resultWrapper = (PropertySheetResultWrapper) propertySheetItem;
            return new ResultEditor((ObservableValue<ResultImpl>) resultWrapper.getObservableValue().get(), manifoldForDisplay);
        } else if (propertySheetItem instanceof PropertySheetListWrapper) {
            PropertySheetListWrapper listWrapper = (PropertySheetListWrapper) propertySheetItem;
            ListEditor listEditor = new ListEditor(this.manifoldForDisplay, listWrapper.getNewObjectSupplier(), listWrapper.getNewEditorSupplier());
            listEditor.setValue((ObservableList) listWrapper.getValue());
            return listEditor;
        } else if (propertySheetItem instanceof PropertySheetItem) {
            return setupPropertySheetItem((PropertySheetItem) propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetBooleanWrapper) {
            return Editors.createCheckEditor(propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetPasswordWrapper) {
            return createPasswordEditor(propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetConceptListWrapper) {
            return createConceptListEditor((PropertySheetConceptListWrapper) propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetItemAssemblageListWrapper) {
            return createAssemblageListEditor((PropertySheetItemAssemblageListWrapper) propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetConceptSetWrapper) {
            return createConceptListEditor((PropertySheetConceptSetWrapper) propertySheetItem);
        } else if (propertySheetItem instanceof PropertySheetStatusSetWrapper) {
            return Editors.createChoiceEditor(propertySheetItem,
                    ((PropertySheetStatusSetWrapper) propertySheetItem).getAllowedValues());
        } else if (propertySheetItem instanceof PropertySheetStampPrecedenceWrapper) {
            return Editors.createChoiceEditor(propertySheetItem,
                    ((PropertySheetStampPrecedenceWrapper) propertySheetItem).getAllowedValues());
        } else if (propertySheetItem instanceof PropertySheetItemDateTimeWrapper) {
            PropertySheetItemDateTimeWrapper dateTimeWrapper = (PropertySheetItemDateTimeWrapper) propertySheetItem;
            PropertyEditor<?> dateTimePropertyEditor = new AbstractPropertyEditor<LocalDateTime, KometDateTimePicker>(propertySheetItem, new KometDateTimePicker()) {

                {
                    super.getEditor().setDateTimeValue(dateTimeWrapper.getValue());
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
        throw new UnsupportedOperationException("Not supported yet: " + propertySheetItem.getClass().getName());
    }

    private static PropertyEditor<?> createPasswordEditor(PropertySheet.Item property) {

        return new AbstractPropertyEditor<String, PasswordField>(property, new PasswordField()) {

            {
                enableAutoSelectAll(getEditor());
            }

            @Override
            protected StringProperty getObservableValue() {
                return getEditor().textProperty();
            }

            @Override
            public void setValue(String value) {
                getEditor().setText(value);
            }
        };
    }

    private static void enableAutoSelectAll(final TextInputControl control) {
        control.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    control.selectAll();
                });
            }
        });
    }

    private PropertyEditor<?> setupPropertySheetItem(PropertySheetItem item) throws UnsupportedOperationException, NoSuchElementException {
        switch (item.getEditorType()) {
            case CONCEPT_SPEC_CHOICE_BOX: {
                Collection<ConceptForControlWrapper> collection = new ArrayList<>();
                for (Object allowedValue : item.getAllowedValues()) {
                    ConceptSpecification allowedConcept = (ConceptSpecification) allowedValue;
                    collection.add(new ConceptForControlWrapper(manifoldForDisplay, allowedConcept.getNid()));
                }
                PropertyEditor editor = Editors.createChoiceEditor(item, collection);
                ComboBox editorControl = (ComboBox) editor.getEditor();
                editorControl.setMaxWidth(Double.MAX_VALUE);
                Object defaultValue = item.getDefaultValue();
                ConceptSpecification defaultConcept;
                if (defaultValue instanceof ConceptSpecification) {
                    defaultConcept = (ConceptSpecification) defaultValue;
                } else {
                    defaultConcept = Get.conceptSpecification((Integer) defaultValue);
                }
                Object currentValue = item.getValue();

                if (currentValue == null) {
                    editor.setValue(new ConceptForControlWrapper(manifoldForDisplay, defaultConcept.getNid()));
                } else {
                    ConceptSpecification currentConcept;
                    if (currentValue instanceof ConceptSpecification) {
                        currentConcept = (ConceptSpecification) currentValue;
                    } else {
                        currentConcept = Get.conceptSpecification((Integer) currentValue);
                    }
                    editor.setValue(new ConceptForControlWrapper(manifoldForDisplay, currentConcept.getNid()));
                }
                return editor;
            }
            case OBJECT_CHOICE_BOX: {
                PropertyEditor editor = Editors.createChoiceEditor(item, item.getAllowedValues());
                ComboBox editorControl = (ComboBox) editor.getEditor();
                editorControl.setMaxWidth(Double.MAX_VALUE);
                if (item.getValue() == null) {
                    editor.setValue(item.getDefaultValue());
                } else {
                    editor.setValue(item.getValue());
                }
                return editor;
            }

            case TEXT: {
                PropertyEditor editor = Editors.createTextEditor(item);
                TextField editorControl = (TextField) editor.getEditor();
                editorControl.setText((String) item.getValue());
                editorControl.setMaxWidth(Double.MAX_VALUE);
                return editor;
            }
            case BOOLEAN: {
                PropertyEditor editor = Editors.createCheckEditor(item);
                CheckBox checkBox = (CheckBox) editor.getEditor();
                checkBox.setText(item.getName());
                return editor;
            }
            case UNSPECIFIED:
            default:
                PropertyEditor editor = Editors.createTextEditor(item);
                TextField editorControl = (TextField) editor.getEditor();
                editorControl.setText(item.getValue().toString());
                editorControl.setMaxWidth(Double.MAX_VALUE);
                return editor;
        }
    }

    private PropertyEditor<?> createCustomChoiceEditor(PropertySheetItemConceptNidWrapper propertySheetItem) {
        if (!propertySheetItem.getAllowedValues().isEmpty()) {
            Collection<ConceptForControlWrapper> collection = new ArrayList<>();
            propertySheetItem.getAllowedValues().stream().forEach((nid) -> collection.add(new ConceptForControlWrapper(manifoldForDisplay, nid)));
            return Editors.createChoiceEditor(propertySheetItem, collection);
        } else {
            ConceptLabel conceptLabel = new ConceptLabel(manifoldForDisplay, ConceptLabel::setPreferredText, (label) -> {
                List<MenuItem> labelMenu = new ArrayList<>();

                for (String manifoldGroup : Manifold.getGroupNames()) {
                    Menu manifoldHistory = new Menu(manifoldGroup);
                    labelMenu.add(manifoldHistory);
                    Collection<HistoryRecord> groupHistory = Manifold.getGroupHistory(manifoldGroup);
                    for (HistoryRecord record : groupHistory) {
                        MenuItem conceptItem = new MenuItem(
                                manifoldForDisplay.getPreferredDescriptionText(record.getComponentId())
                        );
                        conceptItem.setOnAction((ActionEvent event) -> {
                            label.setValue(record.getComponentId());
                        });
                        manifoldHistory.getItems().add(conceptItem);
                    }
                }
                return labelMenu;
            });

            return conceptLabel;
        }
    }

    private PropertyEditor<?> createConceptListEditor(PropertySheetConceptListWrapper propertySheetConceptListWrapper) {
        ConceptListEditor editor = new ConceptListEditor(manifoldForDisplay);
        editor.setValue(propertySheetConceptListWrapper.getValue());
        return editor;
    }

    private PropertyEditor<?> createAssemblageListEditor(PropertySheetItemAssemblageListWrapper propertySheetConceptListWrapper) {
        AssemblageListEditor editor = new AssemblageListEditor(manifoldForDisplay);
        editor.setValue(propertySheetConceptListWrapper.getValue());
        return editor;
    }

    private PropertyEditor<?> createConceptListEditor(PropertySheetConceptSetWrapper propertySheetConceptSetWrapper) {
        ConceptListEditor editor = new ConceptListEditor(manifoldForDisplay);
        editor.setValue(propertySheetConceptSetWrapper.getValue());
        return editor;
    }
}
