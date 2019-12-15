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
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetConceptSetWrapper implements PropertySheet.Item {

    private final SetProperty<ConceptSpecification> conceptSetProperty;
    private final ListProperty<ConceptSpecification> conceptListProperty;
    private final String name;
    private final ListChangeListener<ConceptSpecification> listChangedListener = c -> this.handleListChange(c);

    public PropertySheetConceptSetWrapper(Manifold manifold, SetProperty<ConceptSpecification> conceptSetProperty) {
        this.conceptSetProperty = conceptSetProperty;
        this.name = manifold.getPreferredDescriptionText(new ConceptProxy(conceptSetProperty.getName()));
        ObservableList<ConceptSpecification> list = FXCollections.observableArrayList();
        list.addAll(conceptSetProperty.getValue());
        list.addListener(this.listChangedListener);
        this.conceptListProperty = new SimpleListProperty<>(conceptSetProperty.getBean(), 
                conceptSetProperty.getName(), list);
    }

    private void handleListChange(ListChangeListener.Change<? extends ConceptSpecification> change) {
        while (change.next()) {
            if (change.wasUpdated()) {
                conceptSetProperty.getValue().clear();
                for (ConceptSpecification spec : change.getList()) {
                    conceptSetProperty.getValue().add(spec);
                }
            } else if (change.wasPermutated()) {
                // nothing to do...
            } else {
                for (ConceptSpecification spec : change.getRemoved()) {
                    conceptSetProperty.getValue().remove(spec);
                }
                for (ConceptSpecification spec : change.getAddedSubList()) {
                    conceptSetProperty.getValue().add(spec);
                }
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
        if (value == null) {
            conceptListProperty.get().clear();
            conceptSetProperty.get().clear();
        } else {
            conceptSetProperty.get().clear();
            conceptListProperty.get().removeListener(this.listChangedListener);
            ObservableList list = (ObservableList) value;
            list.addListener(this.listChangedListener);
            conceptListProperty.set(list);
            conceptSetProperty.get().addAll(list);
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(conceptListProperty);
    }
}
