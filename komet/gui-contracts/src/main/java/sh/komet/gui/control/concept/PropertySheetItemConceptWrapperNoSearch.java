/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.control.concept;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptSpecificationWithLabel;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetItemConceptWrapperNoSearch implements ConceptSpecification, PropertySheet.Item, PreferenceChanged {

    private final Manifold manifoldForDisplay;
    private final String name;
    private final SimpleObjectProperty<ConceptSpecification> conceptProperty;
    private final ObservableList<ConceptSpecification> allowedValues;
    private final SimpleBooleanProperty allowSearchProperty = new SimpleBooleanProperty(this, "allow search", true);
    private final SimpleBooleanProperty allowHistoryProperty = new SimpleBooleanProperty(this, "allow history", true);
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(this, "changed", false);

    private ConceptSpecification propertySpecification = null;

    public PropertySheetItemConceptWrapperNoSearch(Manifold manifoldForDisplay,
            ObjectProperty<? extends ConceptSpecification> conceptProperty, ObservableList<ConceptSpecification> allowedValues) {
        this(manifoldForDisplay, manifoldForDisplay.getPreferredDescriptionText(new ConceptProxy(conceptProperty.getName())), conceptProperty, allowedValues);
    }

    public PropertySheetItemConceptWrapperNoSearch(Manifold manifoldForDisplay, String name,
            ObjectProperty<? extends ConceptSpecification> conceptProperty, ObservableList<ConceptSpecification> allowedValues) {
        this.manifoldForDisplay = manifoldForDisplay;
        this.name = name;
        this.allowedValues = allowedValues;
        this.conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptProperty;
        if (allowedValues.size() > 0) {
            this.conceptProperty.set(allowedValues.get(0));
        }
        bindProperties();

    }
    
    public PropertyEditor<ConceptSpecification> getEditor() {
        
        ComboBox<ConceptSpecification> conceptCombo = new ComboBox(allowedValues);
        conceptCombo.setButtonCell(new ListCell<ConceptSpecification>() {
            @Override
            protected void updateItem(ConceptSpecification item, boolean empty) {
                super.updateItem(item, empty); 
                if (!empty) {
                    if (item instanceof ConceptSpecificationWithLabel) {
                        this.setText(((ConceptSpecificationWithLabel) item).toString());
                    } else {
                        this.setText(manifoldForDisplay.getPreferredDescriptionText(item));
                    }
                } else {
                    this.setText("");
                }
            }
        });
        
        conceptCombo.setCellFactory(c-> new ListCell<ConceptSpecification>() {
            @Override
            protected void updateItem(ConceptSpecification item, boolean empty) {
                super.updateItem(item, empty); 
                if (!empty) {
                    if (item instanceof ConceptSpecificationWithLabel) {
                        this.setText(((ConceptSpecificationWithLabel) item).toString());
                    } else {
                        this.setText(manifoldForDisplay.getPreferredDescriptionText(item));
                    }
                } else {
                    this.setText("");
                }
            }
        });
        return new AbstractPropertyEditor<ConceptSpecification, ComboBox<ConceptSpecification>>(
                this, conceptCombo, false) {
            
            @Override protected ObservableValue<ConceptSpecification> getObservableValue() {
                return getEditor().getSelectionModel().selectedItemProperty();
            }

            @Override public void setValue(ConceptSpecification value) {
                getEditor().getSelectionModel().select(value);
            }
        };
        
    }
    
 
    @Override
    public BooleanProperty changedProperty() {
        return changedProperty;
    }

    private void bindProperties() {
        
        this.conceptProperty.addListener((observable, oldValue, newValue) -> {
            setValue(newValue);
            changedProperty.setValue(true);
        });
        this.allowHistoryProperty.addListener((observable, oldValue, newValue) -> {
            changedProperty.setValue(true);
        });
        this.allowSearchProperty.addListener((observable, oldValue, newValue) -> {
            changedProperty.setValue(true);
        });
        this.allowedValues.addListener(new WeakListChangeListener<>((ListChangeListener.Change<? extends ConceptSpecification> c) -> {
            changedProperty.setValue(true);
        }));
    }

    public boolean allowSearch() {
        return allowSearchProperty.get();
    }

    public void setAllowSearch(boolean allowSearch) {
        this.allowSearchProperty.set(allowSearch);
    }

    public SimpleBooleanProperty allowSearchProperty() {
        return allowSearchProperty;
    }

    public SimpleBooleanProperty allowHistoryProperty() {
        return allowHistoryProperty;
    }

    public boolean allowHistory() {
        return allowHistoryProperty.get();
    }

    public void setAllowHistory(boolean allowHistory) {
        this.allowHistoryProperty.set(allowHistory);
    }

    @Override
    public String getFullyQualifiedName() {
        return this.manifoldForDisplay.getFullySpecifiedDescriptionText(conceptProperty.get());
    }

    @Override
    public Optional<String> getRegularName() {
        return Optional.of(manifoldForDisplay.getPreferredDescriptionText(conceptProperty.get()));
    }

    @Override
    public List<UUID> getUuidList() {
        return new ConceptProxy(conceptProperty.getName()).getUuidList();
    }

    @Override
    public Class<?> getType() {
        return ConceptSpecificationForControlWrapper.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    public ObservableList<ConceptSpecification> getAllowedValues() {
        return allowedValues;
    }
    

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "Select the proper concept value for the version you wish to create. ";
    }

    @Override
    public ConceptSpecification getValue() {
        return this.conceptProperty.get();
    }
    
    public void setDefaultValue(Object value) {
        setValue(value);
    }

    @Override
    public void setValue(Object value) {
        this.conceptProperty.setValue((ConceptSpecification) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.conceptProperty);
    }

    public ConceptSpecification getSpecification() {
        if (this.propertySpecification != null) {
            return this.propertySpecification;
        }
        return new ConceptProxy(this.conceptProperty.getName());
    }

    public void setSpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification = propertySpecification;
    }

    @Override
    public String toString() {
        return "Property sheet item for "
                + manifoldForDisplay.getPreferredDescriptionText(new ConceptProxy(getSpecification().toExternalString()));
    }
}
