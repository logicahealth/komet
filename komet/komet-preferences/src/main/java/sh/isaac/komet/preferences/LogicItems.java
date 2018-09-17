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
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class LogicItems extends ParentPanelPreferences {

    
    public LogicItems(IsaacPreferences preferencesNode, Manifold manifold, 
            KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Logic actions"), 
                manifold, kpc);
        revertFields();
        save();
    }

    @Override
    void saveFields() throws BackingStoreException {
        // nothing to save
    }

    @Override
    final void revertFields() {
        // nothing to revert
    }

    @Override
    protected Class getChildClass() {
        return LogicActionPanel.class;
    }
}
