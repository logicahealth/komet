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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetItemConceptConstraintWrapper implements PropertySheet.Item {

    public enum Keys {
        CONSTRAINT_LIST
    };

    SimpleObjectProperty<PropertySheetItemConceptWrapper> constraint 
            = new SimpleObjectProperty<>(this, ObservableFields.CONCEPT_CONSTRAINTS.toExternalString());
    Manifold manifold;
    String name;

    public PropertySheetItemConceptConstraintWrapper(PropertySheetItemConceptWrapper conceptWrapper, Manifold manifold, String name) {
        this.manifold = manifold;
        this.constraint.setValue(conceptWrapper);
        this.name = name;
    }
            
    @Override
    public Class<?> getType() {
        return PropertySheetItemConceptWrapper.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "Select the constraints to use when editing this concept. ";
    }

    @Override
    public PropertySheetItemConceptWrapper getValue() {
        return constraint.get();
    }

    @Override
    public void setValue(Object value) {
        this.constraint.setValue((PropertySheetItemConceptWrapper) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.constraint);
    }
    
    public ConceptSpecification getFieldConcept() {
        PropertySheetItemConceptWrapper conceptWrapper = constraint.get();
        SimpleObjectProperty<ConceptSpecification> conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptWrapper.getObservableValue().get();
        return new ConceptProxy(conceptProperty.getName());
    }
    
    public void addSecondaryProperties() {
        
    }

    public void writeToPreferences(IsaacPreferences node) {
        ArrayList<String> constraintList = new ArrayList<>();
        constraintList.add(Boolean.toString(constraint.get().allowHistory()));
        constraintList.add(Boolean.toString(constraint.get().allowSearch()));
        constraintList.add(constraint.get().getValue().toExternalString());
        constraintList.add(Integer.toString(constraint.get().getAllowedValues().size()));
        for (ConceptSpecification allowedValue: constraint.get().getAllowedValues()) {
            constraintList.add(allowedValue.toExternalString());
        }
        node.putList(Keys.CONSTRAINT_LIST, constraintList);
    }
   
    public void readFromPreferences(IsaacPreferences node) {
        List<String> defaultList = new ArrayList<>();
        defaultList.add(Boolean.toString(true)); // allowHistory
        defaultList.add(Boolean.toString(true)); // allowSearch
        defaultList.add(TermAux.UNINITIALIZED_COMPONENT_ID.toExternalString()); //Default value
        defaultList.add(Integer.toString(1));
        defaultList.add(TermAux.UNINITIALIZED_COMPONENT_ID.toExternalString()); //Allowed value
        
        List<String> constraintList = node.getList(Keys.CONSTRAINT_LIST, defaultList);
        PropertySheetItemConceptWrapper conceptWrapper = constraint.get();
        conceptWrapper.setAllowHistory(Boolean.getBoolean(constraintList.get(0)));
        conceptWrapper.setAllowSearch(Boolean.getBoolean(constraintList.get(1)));
        conceptWrapper.setValue(new ConceptProxy(constraintList.get(2)));
        int allowedValueCount = Integer.parseInt(constraintList.get(3));
        conceptWrapper.getAllowedValues().clear();
        for (int i = 4; i < allowedValueCount + 4; i++) {
            conceptWrapper.getAllowedValues().add(new ConceptProxy(constraintList.get(i)));
        }
    }

    @Override
    public String toString() {
        return "PropertySheetItemConceptConstraintWrapper{" + "constraint=" + constraint.getName() + ", name=" + name + '}';
    }
   
}
