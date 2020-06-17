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
package sh.komet.gui.control.property;

import java.util.Optional;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.action.Action;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;

/**
 *
 * @author kec
 */
public class PropertySheetItem implements PropertySheet.Item {
    
    private Object defaultValue;
    
    private final ObservableList<Object> allowedValues = FXCollections.observableArrayList();
    
    private final ObservableList<AlertObject> alerts = FXCollections.observableArrayList();
    
    private final ObservableList<Action> itemActions = FXCollections.observableArrayList();
    
    private final ObservableList<Action> groupActions = FXCollections.observableArrayList();
    
    private final ObservableList<Action> parentActions = FXCollections.observableArrayList();
    
    private final Property theProperty;
    
    private final ViewProperties viewProperties;
    
    private final String name;
    
    private final ConceptSpecification specificationForProperty;

    private EditorType editorType = EditorType.UNSPECIFIED;
    
    private PropertySheetPurpose propertySheetPurpose = PropertySheetPurpose.UNSPECIFIED;
    
    public PropertySheetItem(Property theProperty, ViewProperties viewProperties) {
        this(null, theProperty, viewProperties);
    }

    public PropertySheetItem(Object defaultValue, Property theProperty, ViewProperties viewProperties, PropertySheetPurpose propertySheetPurpose) {
        this(defaultValue, theProperty, viewProperties);
        this.propertySheetPurpose = propertySheetPurpose;
    }
    
    public PropertySheetItem(Object defaultValue, Property theProperty, ViewProperties viewProperties) {
        this.defaultValue = defaultValue;
        this.theProperty = theProperty;
        this.viewProperties = viewProperties;
        this.specificationForProperty = new ConceptProxy(theProperty.getName());
        this.name = viewProperties.getPreferredDescriptionText(this.specificationForProperty);
        if (defaultValue instanceof Boolean) {
            this.editorType = EditorType.BOOLEAN;
        } else if (theProperty instanceof StringProperty) {
            this.editorType = EditorType.TEXT;
        } else if (theProperty instanceof IntegerProperty) {
            this.editorType = EditorType.CONCEPT_SPEC_CHOICE_BOX;
        }
    }

    public ConceptSpecification getSpecification() {
        return specificationForProperty;
    }

    public EditorType getEditorType() {
        return editorType;
    }

    public void setEditorType(EditorType editorType) {
        this.editorType = editorType;
    }

    public ObservableList<Object> getAllowedValues() {
        return allowedValues;
    }

    public ObservableList<AlertObject> getAlerts() {
        return alerts;
    }

    public ObservableList<Action> getItemActions() {
        return itemActions;
    }

    public ObservableList<Action> getGroupActions() {
        return groupActions;
    }

    public ObservableList<Action> getParentActions() {
        return parentActions;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
    }

    /**
     * Sets the default value if the current default is null, or an integer that == 0,
     * or == TermAux.UNINITIALIZED_COMPONENT_ID.getNid()
     * @param defaultValue
     */
    public void setDefaultValueIfUnknown(Object defaultValue) {
        if (this.defaultValue ==  null) {
            this.defaultValue = defaultValue;
            this.setValue(defaultValue);
        } else if (this.defaultValue instanceof Integer) {
            int defaultInt = (Integer) this.defaultValue;
            if (defaultInt == 0 ||
                defaultInt == TermAux.UNINITIALIZED_COMPONENT_ID.getNid()) {
                this.defaultValue = defaultValue;
                this.setValue(defaultValue);
            }
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
      return name;
   }

    @Override
   public String getDescription() {
      return null;
   }

    @Override
    public Object getValue() {
        if (editorType == EditorType.UNSPECIFIED) {
            return getName();
        }
        if (editorType == EditorType.CONCEPT_SPEC_CHOICE_BOX) {
            CommitAwareIntegerProperty intProperty = (CommitAwareIntegerProperty) theProperty;
            if (intProperty.getValue() == 0 || intProperty.getValue() == MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
                return null;
            }
            return Get.concept(intProperty.get());
        }
        
        return theProperty.getValue();
    }

    @Override
    public void setValue(Object value) {
        if (editorType == EditorType.UNSPECIFIED) {
            // not editable, don't set.
        } else {
            if (value instanceof ConceptSpecification) {
                theProperty.setValue(((ConceptSpecification) value).getNid());
            } else {
                theProperty.setValue(value);
            }
                        
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        if (editorType == EditorType.UNSPECIFIED) {
            return null;
        }
        return Optional.of(theProperty);
    }
    @Override
    public boolean isEditable() {
        if (editorType == EditorType.UNSPECIFIED) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "PropertySheetItem{name=" + name + ", editorType=" + editorType + '}';
    }
    
    public PropertySheetPurpose getPropertySheetPurpose() {
        return propertySheetPurpose;
    }

    public void setPropertySheetPurpose(PropertySheetPurpose propertySheetPurpose) {
        this.propertySheetPurpose = propertySheetPurpose;
    }

    
}
