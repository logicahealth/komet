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

import java.util.List;
import java.util.prefs.BackingStoreException;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import static sh.isaac.komet.preferences.PreferencesTreeItem.Properties.CHILDREN_NODES;
import static sh.isaac.komet.preferences.SynchronizationItems.SYNCHRONIZATION_ITEMS_GROUP_NAME;

import sh.isaac.komet.preferences.personas.PersonasItems;
import sh.isaac.komet.preferences.window.WindowPreferencePanel;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class RootPreferences extends AbstractPreferences {

    public RootPreferences(IsaacPreferences preferencesNode, Manifold manifold, 
            KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Root"), manifold, 
                kpc);
        if (!initialized()) {
            // Add children nodes and reflection classes for children
            addChild("Configuration", ConfigurationPreferencePanel.class);
            addChild("User", UserPreferencesPanel.class);
            addChild(SYNCHRONIZATION_ITEMS_GROUP_NAME, SynchronizationItems.class);
            addChild("Attachment actions", AttachmentItems.class);
            addChild("Logic actions", LogicItemPanels.class);
            addChild("Taxonomy configurations", TaxonomyItems.class);
            addChild("Window configurations", WindowPreferencePanel.class);
            addChild("Personas", PersonasItems.class);
        }
        List<String> childPreferences = this.preferencesNode.getList(CHILDREN_NODES);
        if (childPreferences.contains("Change sets")) {
            childPreferences.replaceAll((t) -> {
                if (t.equals("Change sets")) {
                    return SYNCHRONIZATION_ITEMS_GROUP_NAME;
                }
                return t; 
            });
            this.preferencesNode.putList(CHILDREN_NODES, childPreferences);
        }
        save();
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        // No additional fields. Nothing to do. 
    }

    @Override
    protected void revertFields() {
        // No additional fields. Nothing to do. 
    }


}
