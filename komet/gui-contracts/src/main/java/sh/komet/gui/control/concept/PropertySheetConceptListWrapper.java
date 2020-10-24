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
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class PropertySheetConceptListWrapper implements PropertySheet.Item {

    private final ListProperty<ConceptSpecification> conceptListProperty;
    private final String name;
    private SimpleEqualityBasedListProperty<ConceptSpecification> allowedValuesListProperty;
    private SimpleBooleanProperty allowDuplicates = new SimpleBooleanProperty(false);

    public PropertySheetConceptListWrapper(ManifoldCoordinate manifoldCoordinate, ListProperty<ConceptSpecification> conceptListProperty) {
        if (manifoldCoordinate == null) {
            throw new NullPointerException("Manifold cannot be null");
        }
        if (conceptListProperty == null) {
            throw new NullPointerException("conceptListProperty cannot be null");
        }
        this.conceptListProperty = conceptListProperty;
        this.name = manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(conceptListProperty.getName()));
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

    public void setConstraints(ConceptSpecification[] constraints) {
        setConstraints(Arrays.asList(constraints));
    }

    public void setConstraints(List<? extends ConceptSpecification> constraints) {
        setConstraints(FXCollections.observableArrayList(constraints));
    }

    public void setConstraints(ObservableList<? extends ConceptSpecification> constraints) {
        this.allowedValuesListProperty = new SimpleEqualityBasedListProperty(constraints);
    }


    public Optional<SimpleEqualityBasedListProperty<ConceptSpecification>> getConstraints() {
        return Optional.ofNullable(this.allowedValuesListProperty);
     }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(conceptListProperty);
    }

    public boolean allowDuplicates() {
        return allowDuplicates.get();
    }

    public SimpleBooleanProperty allowDuplicatesProperty() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates.set(allowDuplicates);
    }
}
