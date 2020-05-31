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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class PropertySheetItemConceptWrapper implements ConceptSpecification, PropertySheet.Item, PreferenceChanged {

    private final ViewProperties viewProperties;
    private final String name;
    private final SimpleObjectProperty<ConceptSpecificationForControlWrapper> observableWrapper;
    private final SimpleObjectProperty<ConceptSpecification> conceptProperty;
    private ObservableList<ConceptSpecification> allowedValues = FXCollections.observableArrayList();
    private final SimpleBooleanProperty allowSearchProperty = new SimpleBooleanProperty(this, "allow search", true);
    private final SimpleBooleanProperty allowHistoryProperty = new SimpleBooleanProperty(this, "allow history", true);
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(this, "changed", false);

    private ConceptSpecification propertySpecification = null;

    public PropertySheetItemConceptWrapper(ViewProperties viewProperties,
                                           ObjectProperty<? extends ConceptSpecification> conceptProperty, int... allowedValues) {
        this(viewProperties, viewProperties.getPreferredDescriptionText(new ConceptProxy(conceptProperty.getName())), conceptProperty, allowedValues);
    }

    public PropertySheetItemConceptWrapper(ViewProperties viewProperties, String name,
                                           ObjectProperty<? extends ConceptSpecification> conceptProperty, int... allowedValues) {
        this.viewProperties = viewProperties;
        this.name = name;
        this.conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptProperty;
        if (allowedValues.length > 0) {
            this.conceptProperty.set(Get.concept(allowedValues[0]));
        }
        for (int allowedNid : allowedValues) {
            this.allowedValues.add(Get.conceptSpecification(allowedNid));
        }
        this.observableWrapper = new SimpleObjectProperty<>(new ConceptSpecificationForControlWrapper(conceptProperty.get(), viewProperties));
        bindProperties();

    }
    
    public PropertySheetItemConceptWrapper(ViewProperties viewProperties, String name,
                                           ObjectProperty<? extends ConceptSpecification> conceptProperty) {
        this(viewProperties, name, conceptProperty, (ConceptSpecification[]) new ConceptSpecification[0]);
    }

    public PropertySheetItemConceptWrapper(ViewProperties viewProperties, String name,
                                           ObjectProperty<? extends ConceptSpecification> conceptProperty, ConceptSpecification... allowedValues) {
        this.viewProperties = viewProperties;
        this.name = name;
        this.conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptProperty;
        if (allowedValues.length > 0) {
            this.conceptProperty.set(allowedValues[0]);
        }
        this.allowedValues.addAll(Arrays.asList(allowedValues));
        this.observableWrapper = new SimpleObjectProperty<>(new ConceptSpecificationForControlWrapper(conceptProperty.get(), viewProperties));
        bindProperties();
    }

    @Override
    public BooleanProperty changedProperty() {
        return changedProperty;
    }

    private void bindProperties() {
        
        this.observableWrapper.addListener((observable, oldValue, newValue) -> {
            setValue(newValue);
            changedProperty.setValue(true);
        });
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
        return this.viewProperties.getFullyQualifiedDescriptionText(conceptProperty.get());
    }

    @Override
    public Optional<String> getRegularName() {
        return Optional.of(viewProperties.getPreferredDescriptionText(conceptProperty.get()));
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
    
    public void setAllowedValues(ObservableList<ConceptSpecification> allowedValues) {
        this.allowedValues = allowedValues;
        this.allowedValues.addListener((ListChangeListener.Change<? extends ConceptSpecification> c) -> {
            changedProperty.setValue(true);
       });
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
    public ConceptSpecificationForControlWrapper getValue() {
        return this.observableWrapper.get();
    }
    
    public void setDefaultValue(Object value) {
        setValue(value);
    }

    @Override
    public void setValue(Object value) {
        ConceptSpecificationForControlWrapper specValue = null;
        if (value instanceof ConceptSpecificationForControlWrapper) {
            specValue = (ConceptSpecificationForControlWrapper) value;
        } else if (value != null) {
            specValue = new ConceptSpecificationForControlWrapper((ConceptSpecification) value, viewProperties);
        }
        try {
            // Concept sequence property may throw a runtime exception if it cannot be changed
            this.conceptProperty.setValue(specValue);
            // only change the observableWrapper if no exception is thrown. 
            this.observableWrapper.setValue(specValue);
        } catch (RuntimeException ex) {
            FxGet.statusMessageService().reportStatus(ex.getMessage());
            this.observableWrapper.setValue(new ConceptSpecificationForControlWrapper(this.conceptProperty.get(), viewProperties));
        }
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
                + viewProperties.getPreferredDescriptionText(new ConceptProxy(getSpecification().toExternalString()));
    }
}
