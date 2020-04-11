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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import sh.isaac.MetaData;
import sh.isaac.api.ChangeSetLoadService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferenceNodeType;
import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.MergeFailure;
import sh.isaac.api.util.DirectoryUtil;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_LOCAL_FOLDER;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_PASSWORD;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_URL;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_USER_NAME;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.ITEM_ACTIVE;
import static sh.isaac.komet.preferences.SynchronizationItems.SYNCHRONIZATION_ITEMS_GROUP_NAME;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.provider.sync.git.SyncServiceGIT;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.SynchronizationItem;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.control.PropertySheetItemStringListWrapper;
import sh.komet.gui.control.PropertySheetPasswordWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class SynchronizationItemPanel extends AbstractPreferences implements SynchronizationItem {
    enum Keys {
        ITEM_NAME,
        ITEM_ACTIVE,
        GIT_USER_NAME,
        GIT_PASSWORD,
        GIT_URL,
        GIT_LOCAL_FOLDER
    }
// ObservableFields.STATEMENT_NARRATIVE.toExternalString()
    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.ITEM_NAME____SOLOR.toExternalString());
    private final SimpleBooleanProperty activeProperty = new SimpleBooleanProperty(this, MetaData.ITEM_ACTIVE____SOLOR.toExternalString(), false);
    private final StringProperty gitUserName = new SimpleStringProperty(this, ObservableFields.GIT_USER_NAME.toExternalString());
    private final StringProperty gitPassword = new SimpleStringProperty(this, ObservableFields.GIT_PASSWORD.toExternalString());
    private final StringProperty gitUrl = new SimpleStringProperty(this, ObservableFields.GIT_URL.toExternalString());
    private final StringProperty localFolder = new SimpleStringProperty(this, ObservableFields.GIT_LOCAL_FOLDER.toExternalString());
    private final StringProperty localFolderAbsolutePath = new SimpleStringProperty(this, ObservableFields.GIT_LOCAL_FOLDER.toExternalString());
    private final String[] folderOptions;
    Button initializeButton = new Button("Initialize");
    {
        initializeButton.setOnAction((event) -> {
            try {
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                DirectoryUtil.cleanDirectory(localFolderAbsolutePath.get());
                syncService.setRootLocation(new File(localFolderAbsolutePath.get()));
                syncService.linkAndFetchFromRemote(gitUrl.get(), gitUserName.get(), gitPassword.get().toCharArray());
                setupSyncButtons();
                if (localFolderAbsolutePath.get().endsWith("preferences")) {
                    File from = new File(localFolderAbsolutePath.get() + "/sh/isaac/komet/preferences/Change sets");
                    File to = new File(localFolderAbsolutePath.get() + "/sh/isaac/komet/preferences/" + SYNCHRONIZATION_ITEMS_GROUP_NAME);
                    DirectoryUtil.moveDirectory(from.toPath(), to.toPath());
                    FxGet.kometPreferences().reloadPreferences();
                } else if (localFolderAbsolutePath.get().endsWith("changesets")) {
                    LOG.info("Reading all synchronized changeset files");
                    int loaded = LookupService.get().getService(ChangeSetLoadService.class).readChangesetFiles();
                    LOG.info("Read {} changeset files", loaded);
                    FxGet.statusMessageService().reportStatus("Read "+ loaded + " changeset files");
                }
            } catch (IllegalArgumentException | IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        });
    }
    Button pushButton = new Button("Push");
    {
        pushButton.setOnAction((event) -> {
            try {
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                syncService.setRootLocation(new File(localFolderAbsolutePath.get()));
                syncService.updateCommitAndPush("Push commit", gitUserName.get(), 
                        gitPassword.get().toCharArray(), MergeFailOption.KEEP_LOCAL);
                setupSyncButtons();
            } catch (IllegalArgumentException | IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (MergeFailure ex) {
                Logger.getLogger(SynchronizationItemPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    Button pullButton = new Button("Pull");
    {
        pullButton.setOnAction((event) -> {
            try {
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                syncService.setRootLocation(new File(localFolderAbsolutePath.get()));
                syncService.updateFromRemote(gitUserName.get(), 
                        gitPassword.get().toCharArray(), MergeFailOption.KEEP_LOCAL);
                setupSyncButtons();
                if (localFolder.get().endsWith("preferences")) { 
                    FxGet.kometPreferences().reloadPreferences();
                } else if (localFolder.get().endsWith("changesets")) {
                    LOG.info("Reading all synchronized changeset files");
                    int loaded = LookupService.get().getService(ChangeSetLoadService.class).readChangesetFiles();
                    LOG.info("Read {} changeset files", loaded);
                    FxGet.statusMessageService().reportStatus("Read "+ loaded + " changeset files");
                }
            } catch (IllegalArgumentException | IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (MergeFailure ex) {
                Logger.getLogger(SynchronizationItemPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public SynchronizationItemPanel(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(getEquivalentUserPreferenceNode(preferencesNode), preferencesNode.get(GROUP_NAME, "Change sets"), 
                manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        folderOptions = new String[] {"changesets", "preferences"};
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, activeProperty));
        getItemList().add(new PropertySheetTextWrapper(manifold, gitUserName));
        getItemList().add(new PropertySheetPasswordWrapper(manifold, gitPassword));
        getItemList().add(new PropertySheetTextWrapper(manifold, gitUrl));
        getItemList().add(new PropertySheetItemStringListWrapper(manifold, localFolder, 
                Arrays.asList(folderOptions)));
        setupSyncButtons();
        localFolder.addListener((observable, oldValue, newValue) -> {
            setupSyncButtons();
            setAbsolutePath(newValue);
        });
        
    }

    private void setAbsolutePath(String newValue) {
        Path absolutePath = Paths.get(Get.configurationService().getDataStoreFolderPath().toString(), newValue);
        localFolderAbsolutePath.setValue(absolutePath.toAbsolutePath().toString());
    }

    private void setupSyncButtons() {
        if (activeProperty.get() && new File(localFolderAbsolutePath.get()).exists()) {
            if (new File(localFolderAbsolutePath.get(), ".git").exists()) {
                initializeButton.setDisable(true);
                pushButton.setDisable(false);
                pullButton.setDisable(false);
            } else {
                initializeButton.setDisable(false);
                pushButton.setDisable(true);
                pullButton.setDisable(true);
            }
        } else {
            initializeButton.setDisable(true);
            pushButton.setDisable(true);
            pullButton.setDisable(true);
        }
    }
    
    private static IsaacPreferences getEquivalentUserPreferenceNode(IsaacPreferences configurationPreferencesNode) {
        try {
            if (configurationPreferencesNode.getNodeType() == PreferenceNodeType.CONFIGURATION) {
                IsaacPreferences userPreferences = FxGet.userNode(ConfigurationPreferencePanel.class).node(configurationPreferencesNode.absolutePath());
                
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_USER_NAME");
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_LOCAL_FOLDER");
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_URL");
                userPreferences.remove("85526abf-c427-3db0-b001-b4223427becf.Keys.GIT_PASSWORD");
                
                for (String key : configurationPreferencesNode.keys()) {
                    if (!key.endsWith(ITEM_ACTIVE.name())) {
                        userPreferences.put(key, configurationPreferencesNode.get(key, ""));
                    }
                    
                }
                return userPreferences;
            } else {
                return configurationPreferencesNode;
            }
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(AttachmentActionPanel.Keys.ITEM_NAME, nameProperty.get());
        IsaacPreferences configurationNode = FxGet.configurationNode(ConfigurationPreferencePanel.class).node(getPreferencesNode().absolutePath());
        configurationNode.putBoolean(ITEM_ACTIVE, activeProperty.get());
        getPreferencesNode().put(GIT_USER_NAME, gitUserName.get());
        getPreferencesNode().putPassword(GIT_PASSWORD, gitPassword.get().toCharArray());
        getPreferencesNode().put(GIT_URL, gitUrl.get());
        getPreferencesNode().put(GIT_LOCAL_FOLDER, localFolder.get());
        setAbsolutePath(localFolder.get());
        setupSyncButtons();
    }

    @Override
    final protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(AttachmentActionPanel.Keys.ITEM_NAME, getGroupName()));
        IsaacPreferences configurationNode = FxGet.configurationNode(ConfigurationPreferencePanel.class).node(getPreferencesNode().absolutePath());
        activeProperty.set(configurationNode.getBoolean(ITEM_ACTIVE, false));
        gitUserName.set(getPreferencesNode().get(GIT_USER_NAME, "username"));
        gitPassword.set(new String(getPreferencesNode().getPassword(GIT_PASSWORD, "password".toCharArray())));
        gitUrl.set(getPreferencesNode().get(GIT_URL, "https://bitbucket.org/account/repo.git"));
        localFolder.set(getPreferencesNode().get(GIT_LOCAL_FOLDER, folderOptions[0]));
        setAbsolutePath(localFolder.get());
        setupSyncButtons();
    }

    @Override
    public Node getTopPanel(Manifold manifold) {


        ToolBar topBar = new ToolBar(initializeButton, pushButton, pullButton);
        return topBar;
    }
    @Override
    public boolean showDelete() {
        return true;
    }

}
