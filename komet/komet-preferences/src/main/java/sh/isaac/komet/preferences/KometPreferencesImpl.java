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

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.preferences.window.WindowPreferencePanel;
import sh.isaac.komet.preferences.window.WindowsPanel;
import sh.komet.gui.contract.preferences.*;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class KometPreferencesImpl implements KometPreferences, ListChangeListener<TreeItem> {

    private static final Logger LOG = LogManager.getLogger();

    private final ObservableList<AttachmentItem> attachmentItems = FXCollections.observableArrayList();
    private final ObservableList<ConfigurationPreference> configurationPreferences = FXCollections.observableArrayList();
    private final ObservableList<LogicItem> logicItems = FXCollections.observableArrayList();
    private final ObservableList<SynchronizationItem> synchronizationItems = FXCollections.observableArrayList();
    private final ObservableList<TaxonomyItem> taxonomyItems = FXCollections.observableArrayList();
    private final ObservableList<UserPreferenceItems> userPreferenceItems = FXCollections.observableArrayList();
    private final ObservableList<WindowPreferencesItem> windowPreferenceItems = FXCollections.observableArrayList();
    private final ObservableList<PersonaItem> personaPreferences = FXCollections.observableArrayList();
    private WindowsPanel windowsPanel;

    private RootPreferences rootPreferences;

    private KometPreferencesController kpc;
    private Stage preferencesStage;
    private Manifold manifold;

    public KometPreferencesImpl() {

    }

    @Override
    public void resetUserPreferences() {
        try {
            IsaacPreferences userPreferences = FxGet.userNode(ConfigurationPreferencePanel.class);
            clearNodeAndChildren(userPreferences);
        } catch (BackingStoreException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void clearNodeAndChildren(IsaacPreferences node) throws BackingStoreException {
        for (IsaacPreferences child : node.children()) {
            clearNodeAndChildren(child);
        }
        node.clear();
        node.sync();
    }

    @Override
    public void loadPreferences(Manifold manifold) {
        this.manifold = manifold; 
        IsaacPreferences preferences = FxGet.configurationNode(ConfigurationPreferencePanel.class);
        setupPreferencesController(manifold, preferences);
    }

    private void setupPreferencesController(Manifold manifold, IsaacPreferences preferences) {
        if (kpc == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/preferences/KometPreferences.fxml"));
                Parent root = loader.load();
                this.kpc = loader.getController();
                this.kpc.setManifold(manifold);
                Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, manifold, kpc);
                setupRoot(treeRoot);

                root.setId(UUID.randomUUID()
                        .toString());

                this.preferencesStage = new Stage();
                this.preferencesStage.setTitle(FxGet.configurationName() + " preferences");
                FxGet.configurationNameProperty().addListener((observable, oldValue, newValue) -> {
                    this.preferencesStage.setTitle(newValue + " preferences");
                });
                Scene scene = new Scene(root);

                this.preferencesStage.setScene(scene);
                scene.getStylesheets()
                        .add(FxGet.fxConfiguration().getUserCSSURL().toString());
                scene.getStylesheets()
                        .add(IconographyHelper.getStyleSheetStringUrl());
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    private void setupRoot(Optional<PreferencesTreeItem> treeRoot) {
        attachmentItems.clear();
        configurationPreferences.clear();
        logicItems.clear();
        synchronizationItems.clear();
        taxonomyItems.clear();
        userPreferenceItems.clear();
        windowPreferenceItems.clear();
        personaPreferences.clear();
        if (treeRoot.isPresent()) {
            PreferencesTreeItem rootTreeItem = treeRoot.get();
            this.rootPreferences = (RootPreferences) rootTreeItem.getValue();
            recursiveProcess(rootTreeItem);
            this.kpc.setRoot(treeRoot.get());
        }
    }

    private void recursiveProcess(PreferencesTreeItem treeItem) {
        treeItem.getChildren().removeListener(this);
        treeItem.getChildren().addListener(this);
        addPreferenceItem(treeItem.getValue());
        for (TreeItem<PreferenceGroup> child: treeItem.getChildren()) {
            recursiveProcess((PreferencesTreeItem) child);
        }
    }

    @Override
    public void reloadPreferences() {
        Get.preferencesService().reloadConfigurationPreferences();

        IsaacPreferences preferences = FxGet.configurationNode(ConfigurationPreferencePanel.class);
        Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, manifold, kpc);
        setupRoot(treeRoot);
    }

    @Override
    public void onChanged(Change<? extends TreeItem> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //permutate
                }
            } else if (c.wasUpdated()) {
                //update item
            } else {
                for (TreeItem remitem : c.getRemoved()) {
                    remitem.getChildren().removeListener(this);
                    removePreferenceItem(remitem.getValue());
                }
                for (TreeItem additem : c.getAddedSubList()) {
                    additem.getChildren().removeListener(this);
                    additem.getChildren().addListener(this);
                    addPreferenceItem(additem.getValue());
                }
            }
        }

    }

    private void addPreferenceItem(Object item) {
        if (item instanceof AttachmentItem) {
            attachmentItems.add((AttachmentItem) item);
        } else if (item instanceof ConfigurationPreference) {
            configurationPreferences.add((ConfigurationPreference) item);
        } else if (item instanceof LogicItem) {
            logicItems.add((LogicItem) item);
        } else if (item instanceof SynchronizationItem) {
            synchronizationItems.add((SynchronizationItem) item);
        } else if (item instanceof TaxonomyItem) {
            taxonomyItems.add((TaxonomyItem) item);
        } else if (item instanceof UserPreferenceItems) {
            userPreferenceItems.add((UserPreferenceItems) item);
        } else if (item instanceof WindowPreferencesItem) {
            windowPreferenceItems.add((WindowPreferencesItem) item);
        } else if(item instanceof PersonaItem){
            personaPreferences.add((PersonaItem)item);
        } else if (item instanceof WindowsPanel) {
            windowsPanel = (WindowsPanel) item;
        }

    }

    private void removePreferenceItem(Object item) {
        if (item instanceof AttachmentItem) {
            attachmentItems.remove(item);
        } else if (item instanceof ConfigurationPreference) {
            configurationPreferences.remove(item);
        } else if (item instanceof LogicItem) {
            logicItems.remove(item);
        } else if (item instanceof SynchronizationItem) {
            synchronizationItems.remove(item);
        } else if (item instanceof TaxonomyItem) {
            taxonomyItems.remove(item);
        } else if (item instanceof UserPreferenceItems) {
            userPreferenceItems.remove(item);
        } else if (item instanceof WindowPreferencesItem) {
            windowPreferenceItems.remove(item);
        } else if(item instanceof PersonaItem){
            personaPreferences.remove(item);
        }
    }

    @Override
    public Stage showPreferences(Manifold manifold) {
        IsaacPreferences preferences = FxGet.configurationNode(ConfigurationPreferencePanel.class);
        setupPreferencesController(manifold, preferences);
        preferencesStage.show();
        preferencesStage.setAlwaysOnTop(true);
        return preferencesStage;
    }

    @Override
    public void closePreferences() {
        this.preferencesStage.close();
    }

    @Override
    public ObservableList<AttachmentItem> getAttachmentItems() {
        return attachmentItems;
    }

    @Override
    public ObservableList<ConfigurationPreference> getConfigurationPreferences() {
        return configurationPreferences;
    }

    @Override
    public ObservableList<LogicItem> getLogicItems() {
        return logicItems;
    }

    @Override
    public ObservableList<SynchronizationItem> getSynchronizationItems() {
        return synchronizationItems;
    }

    @Override
    public ObservableList<TaxonomyItem> getTaxonomyItems() {
        return taxonomyItems;
    }

    @Override
    public ObservableList<UserPreferenceItems> getUserPreferenceItems() {
        return userPreferenceItems;
    }

    @Override
    public ObservableList<WindowPreferencesItem> getWindowPreferenceItems() {
        return windowPreferenceItems;
    }

    @Override
    public ObservableList<PersonaItem> getPersonaPreferences() {
        return personaPreferences;
    }

    @Override
    public WindowPreferences getWindowParentPreferences() {
        return this.windowsPanel;
    }
}
