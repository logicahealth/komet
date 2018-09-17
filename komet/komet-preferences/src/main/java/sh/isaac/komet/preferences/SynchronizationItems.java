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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.INITIALIZED;
import static sh.isaac.komet.preferences.PreferencesTreeItem.Properties.CHILDREN_NODES;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class SynchronizationItems extends ParentPanelPreferences {

    public static final String SYNCHRONIZATION_ITEMS_GROUP_NAME = "Synchronization items";
    private final IsaacPreferences userSyncItemsNode;
    private final IsaacPreferences configSyncItemsNode;

    public SynchronizationItems(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(getEquivalentUserPreferenceNode(preferencesNode), preferencesNode.get(GROUP_NAME, SYNCHRONIZATION_ITEMS_GROUP_NAME),
                manifold, kpc);
        this.configSyncItemsNode = preferencesNode;
        this.userSyncItemsNode = getPreferencesNode();
        revert();
        save();
    }

    static final IsaacPreferences getEquivalentUserPreferenceNode(IsaacPreferences configurationPreferencesNode) {
        try {
            IsaacPreferences userPreferences = FxGet.userNode(ConfigurationPreferences.class).node(SYNCHRONIZATION_ITEMS_GROUP_NAME);
            for (String key : configurationPreferencesNode.keys()) {
                userPreferences.put(key, configurationPreferencesNode.get(key, ""));
            }
            for (IsaacPreferences configChildNode : configurationPreferencesNode.children()) {
                IsaacPreferences userChildNode = userPreferences.node(configChildNode.name());
                for (String key : configChildNode.keys()) {
                    userChildNode.put(key, configChildNode.get(key, ""));
                }
            }
            return userPreferences;
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    void saveFields() throws BackingStoreException {
        // Copy the synchronization items from the user preferences to the 
        // configuration preferences
        configSyncItemsNode.putList(CHILDREN_NODES, userSyncItemsNode.getList(CHILDREN_NODES));
        configSyncItemsNode.putBoolean(INITIALIZED, userSyncItemsNode.getBoolean(INITIALIZED, true));
        
        Set<String> nodesToDelete = new HashSet(Arrays.asList(configSyncItemsNode.childrenNames()));
        nodesToDelete.removeAll(configSyncItemsNode.getList(CHILDREN_NODES));
        for (String childToDelete: nodesToDelete) {
            configSyncItemsNode.node(childToDelete).removeNode();
            userSyncItemsNode.node(childToDelete).removeNode();
        }
        for (IsaacPreferences userChildNode : userSyncItemsNode.children()) {
            IsaacPreferences configChildNode = configSyncItemsNode.node(userChildNode.name());
            for (String key : userSyncItemsNode.keys()) {
                configChildNode.put(key, userChildNode.get(key, ""));
            }
            configChildNode.sync();
        }
        configSyncItemsNode.sync();
        userSyncItemsNode.sync();
    }

    @Override
    final void revertFields() throws BackingStoreException {
        // Revert the synchronization items from the 
        // configuration preferences to the user preferences
        configSyncItemsNode.putList(CHILDREN_NODES, userSyncItemsNode.getList(CHILDREN_NODES));
        configSyncItemsNode.putBoolean(INITIALIZED, userSyncItemsNode.getBoolean(INITIALIZED, false));
        
        Set<String> nodesToDelete = new HashSet(Arrays.asList(configSyncItemsNode.childrenNames()));
        nodesToDelete.removeAll(userSyncItemsNode.getList(CHILDREN_NODES));
        for (String childToDelete: nodesToDelete) {
            configSyncItemsNode.node(childToDelete).removeNode();
            userSyncItemsNode.node(childToDelete).removeNode();
        }
        for (IsaacPreferences userChildNode : userSyncItemsNode.children()) {
            IsaacPreferences configChildNode = configSyncItemsNode.node(userChildNode.name());
            for (String key : userSyncItemsNode.keys()) {
                configChildNode.put(key, userChildNode.get(key, ""));
            }
            configChildNode.sync();
        }
        configSyncItemsNode.sync();
        userSyncItemsNode.sync();
    }

    @Override
    protected Class getChildClass() {
        return SynchronizationItemPanel.class;
    }

}
