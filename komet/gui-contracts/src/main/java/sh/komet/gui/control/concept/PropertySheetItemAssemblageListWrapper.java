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

import java.util.Optional;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class PropertySheetItemAssemblageListWrapper implements PropertySheet.Item {

    private final ListProperty<ConceptSpecification> conceptListProperty;
    private final String name;

    public PropertySheetItemAssemblageListWrapper(ViewProperties viewProperties, ListProperty<ConceptSpecification> conceptListProperty) {
        this.conceptListProperty = conceptListProperty;
        this.name = viewProperties.getPreferredDescriptionText(new ConceptProxy(conceptListProperty.getName()));
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
        return this.name;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ObservableList<ConceptSpecification> getValue() {
        return conceptListProperty.get();
    }

    @Override
    public void setValue(Object value) {
        if (conceptListProperty.get() != value) {
            conceptListProperty.set((ObservableList<ConceptSpecification>) value);
        }
     }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(conceptListProperty);
    }     
}
