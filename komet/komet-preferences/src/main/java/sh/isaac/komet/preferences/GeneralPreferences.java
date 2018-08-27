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

import java.util.prefs.BackingStoreException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.GeneralPreferences.Keys.ENABLE_EDITING;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class GeneralPreferences extends AbstractPreferences {

    public enum Keys {
        ENABLE_EDITING

    }
    private final BooleanProperty enableEdit = new SimpleBooleanProperty(this, ObservableFields.ENABLE_EDIT.toExternalString());

    public GeneralPreferences(IsaacPreferences preferencesNode, Manifold manifold, 
            KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "General"), manifold, 
                kpc);
        this.enableEdit.setValue(preferencesNode.getBoolean(enableEdit.getName(), true));
        revertFields();
        save();
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableEdit));
    }

    @Override
    void saveFields() throws BackingStoreException {
        getPreferencesNode().putBoolean(ENABLE_EDITING, enableEdit.get());
    }

    @Override
    final void revertFields() {
        enableEdit.set(getPreferencesNode().getBoolean(ENABLE_EDITING, true));
    }


}
