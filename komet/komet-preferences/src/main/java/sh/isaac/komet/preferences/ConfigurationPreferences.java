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
package sh.isaac.komet.preferences;

import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.ConfigurationPreferences.Keys.ENABLE_EDITING;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ConfigurationPreferences extends AbstractPreferences {

    public enum Keys {
        ENABLE_EDITING,
        CONFIGURATION_NAME,
        DATASTORE_LOCATION
    }
    
    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.CONFIGURATION_NAME____SOLOR.toExternalString());
    private final BooleanProperty enableEdit = new SimpleBooleanProperty(this, ObservableFields.ENABLE_EDIT.toExternalString());
    private final SimpleStringProperty datastoreLocationProperty
            = new SimpleStringProperty(this, MetaData.DATASTORE_LOCATION____SOLOR.toExternalString());

    public ConfigurationPreferences(IsaacPreferences preferencesNode, Manifold manifold, 
            KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Configuration"), manifold, 
                kpc);
        nameProperty.set(groupNameProperty().get());
        this.enableEdit.setValue(preferencesNode.getBoolean(enableEdit.getName(), true));
        revertFields();
        save();
        FxGet.setConfigurationName(nameProperty.get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            FxGet.setConfigurationName(newValue);
        });
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableEdit));
        getItemList().add(new PropertySheetTextWrapper(manifold, datastoreLocationProperty));
    }

    @Override
    void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.DATASTORE_LOCATION, datastoreLocationProperty.get());
        getPreferencesNode().put(Keys.CONFIGURATION_NAME, nameProperty.get());
        getPreferencesNode().putBoolean(ENABLE_EDITING, enableEdit.get());
    }

    @Override
    final void revertFields() {
        ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
        Path folderPath = configurationService.getDataStoreFolderPath();
        this.datastoreLocationProperty.set(getPreferencesNode().get(Keys.DATASTORE_LOCATION, folderPath.toString()));
        this.nameProperty.set(getPreferencesNode().get(Keys.CONFIGURATION_NAME, getGroupName()));
        enableEdit.set(getPreferencesNode().getBoolean(ENABLE_EDITING, true));
    }


}
