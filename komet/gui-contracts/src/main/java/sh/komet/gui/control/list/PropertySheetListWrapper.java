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
package sh.komet.gui.control.list;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.ConceptProxy;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetListWrapper<T> implements PropertySheet.Item {

    private final SimpleListProperty<T> wrappedProperty;
    private final String name;
    private final Supplier<T> newObjectSupplier;
    private final Function<Manifold,PropertyEditor<T>> newEditorSupplier;

    public PropertySheetListWrapper(Manifold manifold, SimpleListProperty<T> wrappedProperty,
                                    Supplier<T> newObjectSupplier, Function<Manifold,PropertyEditor<T>> newEditorSupplier) {
        this.wrappedProperty = wrappedProperty;
        this.name = manifold.getPreferredDescriptionText(new ConceptProxy(wrappedProperty.getName()));
        this.newObjectSupplier = newObjectSupplier;
        this.newEditorSupplier = newEditorSupplier;
    }

    public Supplier<? extends T> getNewObjectSupplier() {
        return newObjectSupplier;
    }

    public Function<Manifold, PropertyEditor<T>> getNewEditorSupplier() {
        return newEditorSupplier;
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
    public Object getValue() {
        return wrappedProperty.get();
    }

    @Override
    public void setValue(Object value) {
        if (wrappedProperty.get() != value) {
            wrappedProperty.set((ObservableList<T>) value);
        }
     }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(wrappedProperty);
    }     
}
