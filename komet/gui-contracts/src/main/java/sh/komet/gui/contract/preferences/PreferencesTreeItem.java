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
package sh.komet.gui.contract.preferences;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.manifold.Manifold;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.CHILDREN_NODES;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.PROPERTY_SHEET_CLASS;

/**
 *
 * @author kec
 */
public class PreferencesTreeItem extends TreeItem<PreferenceGroup>  {
     private static final Logger LOG = LogManager.getLogger();

    IsaacPreferences preferences;
    final KometPreferencesController controller;
    
    private PreferencesTreeItem(PreferenceGroup value,
                                IsaacPreferences preferences, Manifold manifold, KometPreferencesController controller) {
        super(value);
        this.preferences = preferences;
        this.controller = controller;
        List<String> propertySheetChildren = preferences.getList(CHILDREN_NODES);
        for (String child: propertySheetChildren) {
            Optional<PreferencesTreeItem> childTreeItem = from(preferences.node(child), manifold, controller);
            if (childTreeItem.isPresent()) {
                getChildren().add(childTreeItem.get());
                childTreeItem.get().getValue().setTreeItem(childTreeItem.get());
            }
        }
    }

    public IsaacPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(IsaacPreferences preferences) {
        this.preferences = preferences;
    }

    public KometPreferencesController getController() {
        return controller;
    }

    public void select() {
        this.controller.getPreferenceTree().getSelectionModel().select(this);
    }
    
    public void removeChild(String uuid) {
        PreferenceGroup.removeChild(preferences, uuid);
    }

    @Override
    public String toString() {
        return getValue().getGroupName();
    }


    static public Optional<PreferencesTreeItem> from(IsaacPreferences preferences,
                                                     Manifold manifold, KometPreferencesController controller)  {
        Optional<String> optionalPropertySheetClass = preferences.get(PROPERTY_SHEET_CLASS);
        if (optionalPropertySheetClass.isPresent()) {
            try {
                String propertySheetClassName = optionalPropertySheetClass.get();
                // These are for upgrade from obsolete classes.
                if (propertySheetClassName.equals("sh.isaac.komet.preferences.GeneralPreferences")) {
                    propertySheetClassName = "sh.komet.gui.contract.preferences.ConfigurationPreferences";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.ChangeSetPreferences")) {
                    propertySheetClassName = "sh.komet.gui.contract.preferences.SynchronizationItems";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.AttachmentActionPreferences")) {
                    propertySheetClassName = "sh.komet.gui.contract.preferences.AttachmentItems";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.LogicActionPreferences")) {
                    propertySheetClassName = "sh.komet.gui.contract.preferences.LogicItems";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.WindowPreferences")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.window.WindowsPanel";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.WindowTabPanePreferencesPanel")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.window.WindowTabPanePreferencesPanel";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.LogicItems")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.LogicItemPanels";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.window.WindowPreferences")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.window.WindowPreferencePanel";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.personas.PersonasItemPanel")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.personas.PersonaItemPanel";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.personas.PersonasItems")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.personas.PersonasPanel";
                }

                Class preferencesSheetClass = Class.forName(propertySheetClassName);
                Constructor<PreferenceGroup> c = preferencesSheetClass.getConstructor(
                        IsaacPreferences.class,
                        Manifold.class,
                        KometPreferencesController.class);
                PreferenceGroup preferencesSheet = c.newInstance(preferences, manifold, controller);
                PreferencesTreeItem preferencesTreeItem = new PreferencesTreeItem(preferencesSheet, preferences,
                        manifold, controller);
                preferencesSheet.setTreeItem(preferencesTreeItem);
                return Optional.of(preferencesTreeItem);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                PreferencesTreeItem.LOG.error("PropertySheetClass: " + optionalPropertySheetClass + " " + ex.getLocalizedMessage(), ex);
            }
        } else {
            preferences.put(PROPERTY_SHEET_CLASS, "sh.isaac.komet.preferences.RootPreferences");
            return from(preferences, manifold, controller);
        }
        return Optional.empty();
    }
}
