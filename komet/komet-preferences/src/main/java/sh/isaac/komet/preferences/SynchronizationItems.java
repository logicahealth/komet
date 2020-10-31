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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferenceNodeType;

import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.*;

/**
 *
 * @author kec
 */
public class SynchronizationItems extends ParentPanel {

    public static final String SYNCHRONIZATION_ITEMS_GROUP_NAME = "Synchronization items";
    private final IsaacPreferences userSyncItemsNode;
    private final IsaacPreferences configSyncItemsNode;

    public SynchronizationItems(IsaacPreferences preferencesNode, ViewProperties viewProperties,
                                KometPreferencesController kpc) {
        super(getEquivalentUserPreferenceNode(preferencesNode), preferencesNode.get(GROUP_NAME, SYNCHRONIZATION_ITEMS_GROUP_NAME),
                viewProperties, kpc);
        this.configSyncItemsNode = preferencesNode;
        this.userSyncItemsNode = getPreferencesNode();
        revert();
        save();
    }

    private static IsaacPreferences getEquivalentUserPreferenceNode(IsaacPreferences configurationPreferences) {
        try {
            if (configurationPreferences.getNodeType() == PreferenceNodeType.CONFIGURATION) {
                IsaacPreferences userPreferences = FxGet.kometUserRootNode().node(SYNCHRONIZATION_ITEMS_GROUP_NAME);
                // for version upgrade forward compatibility...
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_USER_NAME");
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_URL");
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_PASSWORD");
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_LOCAL_FOLDER");
                
                configurationPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_USER_NAME");
                configurationPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_URL");
                configurationPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_PASSWORD");
                configurationPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_LOCAL_FOLDER");
                
                userPreferences.putBoolean(INITIALIZED, true);
                userPreferences.put(PROPERTY_SHEET_CLASS, SynchronizationItems.class.getName());
                configurationPreferences.put(PROPERTY_SHEET_CLASS, SynchronizationItems.class.getName());
                
                List<String> userChildren = userPreferences.getList(CHILDREN_NODES);
                List<String> configChildren = configurationPreferences.getList(CHILDREN_NODES);
                for (String configChild: configChildren) {
                    if (!userChildren.contains(configChild)) {
                        userChildren.add(configChild);
                    }
                }
                userPreferences.putList(CHILDREN_NODES, userChildren);
                
                for (IsaacPreferences configChildNode : configurationPreferences.children()) {
                    IsaacPreferences userChildNode = userPreferences.node(configChildNode.name());
                    for (String key : configChildNode.keys()) {
                        userChildNode.put(key, configChildNode.get(key, ""));
                    }
                }
                return userPreferences;
            } else {
                return configurationPreferences;
            }
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        // Copy the synchronization items from the user preferences to the 
        // configuration preferences
        configSyncItemsNode.putList(CHILDREN_NODES, userSyncItemsNode.getList(CHILDREN_NODES));
        configSyncItemsNode.putBoolean(INITIALIZED, userSyncItemsNode.getBoolean(INITIALIZED, true));
        Optional<PreferenceNodeType> optionalNodeType = userSyncItemsNode.getEnum(PreferenceNodeType.class);
        if (optionalNodeType.isPresent()) {
            configSyncItemsNode.putEnum(optionalNodeType.get());
        } else {
            configSyncItemsNode.putEnum(PreferenceNodeType.USER);
        }

        Set<String> nodesToDelete = new HashSet(Arrays.asList(configSyncItemsNode.childrenNames()));
        nodesToDelete.removeAll(configSyncItemsNode.getList(CHILDREN_NODES));
        for (String childToDelete : nodesToDelete) {
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
    final protected void revertFields() throws BackingStoreException {
        // Revert the synchronization items from the 
        // configuration preferences to the user preferences
        configSyncItemsNode.putList(CHILDREN_NODES, userSyncItemsNode.getList(CHILDREN_NODES));
        configSyncItemsNode.putBoolean(INITIALIZED, userSyncItemsNode.getBoolean(INITIALIZED, false));
        Optional<PreferenceNodeType> optionalNodeType = userSyncItemsNode.getEnum(PreferenceNodeType.class);
        if (optionalNodeType.isPresent()) {
            configSyncItemsNode.putEnum(optionalNodeType.get());
        } else {
            configSyncItemsNode.putEnum(PreferenceNodeType.USER);
        }

        Set<String> nodesToDelete = new HashSet(Arrays.asList(configSyncItemsNode.childrenNames()));
        nodesToDelete.removeAll(userSyncItemsNode.getList(CHILDREN_NODES));
        for (String childToDelete : nodesToDelete) {
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
