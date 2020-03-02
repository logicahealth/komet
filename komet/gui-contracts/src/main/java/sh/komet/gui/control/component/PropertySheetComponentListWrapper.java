package sh.komet.gui.control.component;

import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.ConceptProxy;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;

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

/**
 *
 * @author kec
 */
public class PropertySheetComponentListWrapper implements PropertySheet.Item {

    private final ListProperty<ComponentProxy> componentListProperty;
    private final String name;

    public PropertySheetComponentListWrapper(Manifold manifold, ListProperty<ComponentProxy> componentListProperty) {
        this.componentListProperty = componentListProperty;
        this.name = manifold.getPreferredDescriptionText(new ConceptProxy(componentListProperty.getName()));
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
    public ObservableList<ComponentProxy> getValue() {
        return componentListProperty.get();
    }

    @Override
    public void setValue(Object value) {
        if (componentListProperty.get() != value) {
            componentListProperty.set((ObservableList<ComponentProxy>) value);
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(componentListProperty);
    }
}
