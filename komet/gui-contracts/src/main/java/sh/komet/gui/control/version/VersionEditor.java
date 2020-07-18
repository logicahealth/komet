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
package sh.komet.gui.control.version;

import javafx.beans.property.Property;
import javafx.scene.Node;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.model.observable.version.ObservableVersionImpl;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 * @param <V>
 */
public class VersionEditor<V extends ObservableVersionImpl> implements PropertyEditor<V>{
    private final PropertySheet propertySheet = new PropertySheet();
    {
        this.propertySheet.setMode(PropertySheet.Mode.NAME);
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);
         
    }
    private V observableVersion;
    private final ManifoldCoordinate manifoldCoordinate;

    public VersionEditor(V observableVersion, ManifoldCoordinate manifoldCoordinate) {
        this.observableVersion = observableVersion;
        this.manifoldCoordinate = manifoldCoordinate;
        this.propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(manifoldCoordinate));
        if (observableVersion != null) {
            setupProperties();
        }
    }

    public VersionEditor(ManifoldCoordinate manifoldCoordinate) {
        this(null, manifoldCoordinate);
    }

    @Override
    public Node getEditor() {
        return this.propertySheet;
    }

    @Override
    public V getValue() {
        return observableVersion;
    }

    @Override
    public void setValue(V value) {
        if (observableVersion != null) {
            //TODO unbind and unlink...
        }
        this.observableVersion = value;
        
        setupProperties();
    }

    private void setupProperties() {
        propertySheet.getItems().clear();
        for (Property<?> property: this.observableVersion.getEditableProperties()) {
            propertySheet.getItems().add(new PropertySheetItem(property.getValue(), property, manifoldCoordinate));
        }
        WindowPreferences windowPreferences = FxGet.windowPreferences(this.propertySheet);

        FxGet.rulesDrivenKometService().populateWrappedProperties(propertySheet.getItems(),
                manifoldCoordinate.toManifoldCoordinateImmutable(),
                windowPreferences.getViewPropertiesForWindow().getEditCoordinate().getValue());
    }
}
