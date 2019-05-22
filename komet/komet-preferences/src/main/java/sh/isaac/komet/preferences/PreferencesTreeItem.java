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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PreferencesTreeItem extends TreeItem<PreferenceGroup> {
     private static final Logger LOG = LogManager.getLogger();



  public enum Properties {
        PROPERTY_SHEET_CLASS,
        CHILDREN_NODES
        
    } 
    
    IsaacPreferences preferences;
    final KometPreferencesController controller;
    
    private PreferencesTreeItem(PreferenceGroup value,
            IsaacPreferences preferences, Manifold manifold, KometPreferencesController controller) {
        super(value);
        this.preferences = preferences;
        this.controller = controller;
        List<String> propertySheetChildren = preferences.getList(Properties.CHILDREN_NODES);
        for (String child: propertySheetChildren) {
            Optional<PreferencesTreeItem> childTreeItem = from(preferences.node(child), manifold, controller);
            if (childTreeItem.isPresent()) {
                getChildren().add(childTreeItem.get());
                childTreeItem.get().getValue().setTreeItem(childTreeItem.get());
            }
        }
    }
    
    public void select() {
        this.controller.getPreferenceTree().getSelectionModel().select(this);
    }
    
    public void removeChild(String uuid) {
        List<String> propertySheetChildren = preferences.getList(Properties.CHILDREN_NODES);
        propertySheetChildren.remove(uuid);
        preferences.putList(Properties.CHILDREN_NODES, propertySheetChildren);

    }
    
    public static Optional<PreferencesTreeItem> from(IsaacPreferences preferences, 
            Manifold manifold, KometPreferencesController controller)  {
        Optional<String> optionalPropertySheetClass = preferences.get(Properties.PROPERTY_SHEET_CLASS);
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
                    propertySheetClassName = "sh.isaac.komet.preferences.window.WindowPreferences";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.WindowPreferencePanel")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.window.WindowPreferencePanel";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.WindowTabPanePreferencesPanel")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.window.WindowTabPanePreferencesPanel";
                } else if (propertySheetClassName.equals("sh.isaac.komet.preferences.LogicItems")) {
                    propertySheetClassName = "sh.isaac.komet.preferences.LogicItemPanels";
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
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        } else {
            preferences.put(Properties.PROPERTY_SHEET_CLASS, RootPreferences.class.getName());
            return from(preferences, manifold, controller);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return getValue().getGroupName();
    }
    
    
}
