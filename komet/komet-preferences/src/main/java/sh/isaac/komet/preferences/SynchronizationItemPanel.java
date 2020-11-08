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

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import sh.isaac.MetaData;
import sh.isaac.api.ChangeSetLoadService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferenceNodeType;
import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.MergeFailure;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
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
import sh.komet.gui.control.property.wrapper.PropertySheetBooleanWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetItemStringListWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetPasswordWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetTextWrapper;
import sh.komet.gui.control.property.ViewProperties;
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
            InitializeTask initializeTask = new InitializeTask();
            Get.executor().submit(initializeTask);
        });
    }
    Button pushButton = new Button("Push");
    {
        pushButton.setOnAction((event) -> {
            PushTask pushTask = new PushTask(true);
            Get.executor().submit(pushTask);
        });
    }
    Button pullButton = new Button("Pull");
    {
        pullButton.setOnAction((event) -> {
            PullTask pullTask = new PullTask(true);
            Get.executor().submit(pullTask);
        });
    }
    private final MenuItem pushMenuItem = new MenuItem("Push " + nameProperty.getValueSafe());
    private final MenuItem pullMenuItem = new MenuItem("Pull " + nameProperty.getValueSafe());
    {
        pushMenuItem.setOnAction((event) -> {
            PushTask pushTask = new PushTask(false);
            Get.executor().submit(pushTask);
        });
        pullMenuItem.setOnAction((event) -> {
            PullTask pullTask = new PullTask(false);
            Get.executor().submit(pullTask);
        });
    }

    public SynchronizationItemPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties,
                                    KometPreferencesController kpc) {
        super(getEquivalentUserPreferenceNode(preferencesNode), preferencesNode.get(GROUP_NAME, "Change sets"),
                viewProperties, kpc);
        nameProperty.set(groupNameProperty().get());
        pushMenuItem.setText("Push " + nameProperty.getValueSafe());
        pullMenuItem.setText("Pull " + nameProperty.getValueSafe());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            pushMenuItem.setText("Push " + newValue);
            pullMenuItem.setText("Pull " + newValue);
        });
        folderOptions = new String[] {"changesets", "preferences"};
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), nameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(viewProperties.getManifoldCoordinate(), activeProperty));
        getItemList().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), gitUserName));
        getItemList().add(new PropertySheetPasswordWrapper(viewProperties, gitPassword));
        getItemList().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), gitUrl));
        getItemList().add(new PropertySheetItemStringListWrapper(viewProperties, localFolder,
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
                FxGet.synchronizationMenuItems().add(pullMenuItem);
                FxGet.synchronizationMenuItems().add(pushMenuItem);
            } else {
                initializeButton.setDisable(false);
                pushButton.setDisable(true);
                pullButton.setDisable(true);
                FxGet.synchronizationMenuItems().remove(pullMenuItem);
                FxGet.synchronizationMenuItems().remove(pushMenuItem);
            }
        } else {
            initializeButton.setDisable(true);
            pushButton.setDisable(true);
            pullButton.setDisable(true);
            FxGet.synchronizationMenuItems().remove(pullMenuItem);
            FxGet.synchronizationMenuItems().remove(pushMenuItem);
        }
    }
    
    private static IsaacPreferences getEquivalentUserPreferenceNode(IsaacPreferences configurationPreferencesNode) {
        try {
            if (configurationPreferencesNode.getNodeType() == PreferenceNodeType.CONFIGURATION) {
                IsaacPreferences userPreferences = FxGet.kometUserRootNode().node(configurationPreferencesNode.absolutePath());
                
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
        IsaacPreferences configurationNode = FxGet.kometConfigurationRootNode().node(getPreferencesNode().absolutePath());
        configurationNode.putBoolean(ITEM_ACTIVE, activeProperty.get());
        getPreferencesNode().put(GIT_USER_NAME, gitUserName.get());
        getPreferencesNode().putPassword(GIT_PASSWORD, gitPassword.get().toCharArray());
        getPreferencesNode().put(GIT_URL, gitUrl.get());
        getPreferencesNode().put(GIT_LOCAL_FOLDER, localFolder.get());
        setAbsolutePath(localFolder.get());
        setupSyncButtons();
        if (!initializeButton.isDisabled()) {
            initializeButton.fire();
        }
    }

    @Override
    final protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(AttachmentActionPanel.Keys.ITEM_NAME, getGroupName()));
        IsaacPreferences configurationNode = FxGet.kometConfigurationRootNode().node(getPreferencesNode().absolutePath());
        activeProperty.set(configurationNode.getBoolean(ITEM_ACTIVE, false));
        gitUserName.set(getPreferencesNode().get(GIT_USER_NAME, "username"));
        gitPassword.set(new String(getPreferencesNode().getPassword(GIT_PASSWORD, "password".toCharArray())));
        gitUrl.set(getPreferencesNode().get(GIT_URL, "https://bitbucket.org/account/repo.git"));
        localFolder.set(getPreferencesNode().get(GIT_LOCAL_FOLDER, folderOptions[0]));
        setAbsolutePath(localFolder.get());
        setupSyncButtons();
    }

    @Override
    public Node getTopPanel(ViewProperties viewProperties) {


        ToolBar topBar = new ToolBar(initializeButton, pushButton, pullButton);
        return topBar;
    }
    @Override
    public boolean showDelete() {
        return true;
    }

    private class PullTask extends TimedTaskWithProgressTracker<Void> {
        boolean updateButtons;
        public PullTask(boolean updateButtons) {
            this.updateButtons = updateButtons;
            updateTitle("Pulling " + getGroupName() +
                    " files from server");
            Get.activeTasks().add(this);
            addToTotalWork(3);
        }

        @Override
        protected Void call() throws Exception {
            try {
                this.updateMessage("Getting synchronization service");
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                this.completedUnitOfWork();
                syncService.setRootLocation(new File(localFolderAbsolutePath.get()));
                this.updateMessage("Pulling from remote server");
                syncService.updateFromRemote(gitUserName.get(),
                        gitPassword.get().toCharArray(), MergeFailOption.KEEP_LOCAL);
                this.completedUnitOfWork();
                if (updateButtons) {
                    Platform.runLater(() -> setupSyncButtons());
                }
                if (localFolder.get().endsWith("preferences")) {
                    this.updateMessage("Updating preferences");
                    FxGet.kometPreferences().reloadPreferences();
                    this.completedUnitOfWork();
                } else if (localFolder.get().endsWith("changesets")) {
                    this.updateMessage("Reading all synchronized changeset files");
                    LOG.info("Reading all synchronized changeset files");
                    int loaded = LookupService.get().getService(ChangeSetLoadService.class).readChangesetFiles();
                    this.updateMessage("Read " + loaded +
                            " changeset files");
                    LOG.info("Read {} changeset files", loaded);
                    FxGet.statusMessageService().reportStatus("Read " + loaded + " changeset files");
                    this.completedUnitOfWork();
                }
                return null;
            } catch (IllegalArgumentException | IOException ex) {
                FxGet.dialogs().showErrorDialog(ex);
                return null;
            } catch (MergeFailure ex) {
                FxGet.dialogs().showErrorDialog(ex);
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }
    private class PushTask extends TimedTaskWithProgressTracker<Void> {
        boolean updateButtons;
        public PushTask(boolean updateButtons) {
            this.updateButtons = updateButtons;
            updateTitle("Pushing " + getGroupName() +
                    " files to server");
            Get.activeTasks().add(this);
            addToTotalWork(3);
        }

        @Override
        protected Void call() throws Exception {
            try {
                this.updateMessage("Getting synchronization service");
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                this.completedUnitOfWork();
                syncService.setRootLocation(new File(localFolderAbsolutePath.get()));
                this.updateMessage("Adding untracked files");
                syncService.addUntrackedFiles();
                this.completedUnitOfWork();
                this.updateMessage("Pushing files");
                syncService.updateCommitAndPush("Push commit", gitUserName.get(),
                        gitPassword.get().toCharArray(), MergeFailOption.KEEP_LOCAL);
                this.completedUnitOfWork();
                if (this.updateButtons) {
                    Platform.runLater(() -> setupSyncButtons());
               }
                return null;
            } catch (IllegalArgumentException | IOException ex) {
                FxGet.dialogs().showErrorDialog(ex);
                return null;
            } catch (MergeFailure ex) {
                FxGet.dialogs().showErrorDialog(ex);
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }

    private class InitializeTask extends TimedTaskWithProgressTracker<Void> {
        public InitializeTask() {
            updateTitle("Initializing " + getGroupName() +
                    " synchronization");
            Get.activeTasks().add(this);
            addToTotalWork(3);
        }

        @Override
        protected Void call() throws Exception {
            try {
                this.updateMessage("Getting synchronization service");
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                this.completedUnitOfWork();

                //Cleaning the directory causes change sets to disappear for the entire session. Clean is not necessary
                //in most cases.
                //DirectoryUtil.cleanDirectory(localFolderAbsolutePath.get());

                syncService.setRootLocation(new File(localFolderAbsolutePath.get()));
                this.updateMessage("Linking and fetching files from server ");
                syncService.linkAndFetchFromRemote(gitUrl.get(), gitUserName.get(), gitPassword.get().toCharArray());
                Platform.runLater(() -> setupSyncButtons());
                this.completedUnitOfWork();

                if (localFolderAbsolutePath.get().endsWith("preferences")) {
                    this.updateMessage("Reloading preferences ");
                    FxGet.kometPreferences().reloadPreferences();
                    this.completedUnitOfWork();
                } else if (localFolderAbsolutePath.get().endsWith("changesets")) {
                    this.updateMessage("Reading all synchronized changeset files ");
                    LOG.info("Reading all synchronized changeset files");
                    int loaded = LookupService.get().getService(ChangeSetLoadService.class).readChangesetFiles();
                    this.updateMessage("Read " + loaded +
                            " changeset files");
                    LOG.info("Read {} changeset files", loaded);
                    FxGet.statusMessageService().reportStatus("Read "+ loaded + " changeset files");
                    this.completedUnitOfWork();
                }
                return null;
            } catch (IllegalArgumentException | IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }

}
