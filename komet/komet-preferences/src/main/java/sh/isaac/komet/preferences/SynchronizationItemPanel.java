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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javax.naming.AuthenticationException;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.MergeFailure;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_LOCAL_FOLDER;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_PASSWORD;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_URL;
import static sh.isaac.komet.preferences.SynchronizationItemPanel.Keys.GIT_USER_NAME;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.provider.sync.git.SyncServiceGIT;
import sh.komet.gui.control.PropertySheetItemStringListWrapper;
import sh.komet.gui.control.PropertySheetPasswordWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class SynchronizationItemPanel extends AbstractPreferences {
    enum Keys {
        ITEM_NAME,
        GIT_USER_NAME,
        GIT_PASSWORD,
        GIT_URL,
        GIT_LOCAL_FOLDER
    }
// ObservableFields.STATEMENT_NARRATIVE.toExternalString()
    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.SYNCHRONICATION_ITEM_NAME____SOLOR.toExternalString());
    private final StringProperty gitUserName = new SimpleStringProperty(this, ObservableFields.GIT_USER_NAME.toExternalString());
    private final StringProperty gitPassword = new SimpleStringProperty(this, ObservableFields.GIT_PASSWORD.toExternalString());
    private final StringProperty gitUrl = new SimpleStringProperty(this, ObservableFields.GIT_URL.toExternalString());
    private final StringProperty localFolder = new SimpleStringProperty(this, ObservableFields.GIT_LOCAL_FOLDER.toExternalString());

    public SynchronizationItemPanel(IsaacPreferences preferencesNode, Manifold manifold, 
            KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Change sets"), 
                manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        getItemList().add(new PropertySheetTextWrapper(manifold, gitUserName));
        getItemList().add(new PropertySheetPasswordWrapper(manifold, gitPassword));
        getItemList().add(new PropertySheetTextWrapper(manifold, gitUrl));
        getItemList().add(new PropertySheetItemStringListWrapper(manifold, localFolder, 
                Arrays.asList(new String[] {"data/isaac.data/changesets", "data/isaac.data/preferences"})));
    }

    @Override
    void saveFields() throws BackingStoreException {
        getPreferencesNode().put(AttachmentActionPanel.Keys.ITEM_NAME, nameProperty.get());
        getPreferencesNode().put(GIT_USER_NAME, gitUserName.get());
        getPreferencesNode().putPassword(GIT_PASSWORD, gitPassword.get().toCharArray());
        getPreferencesNode().put(GIT_URL, gitUrl.get());
        getPreferencesNode().put(GIT_LOCAL_FOLDER, localFolder.get());
        
    }

    @Override
    final void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(AttachmentActionPanel.Keys.ITEM_NAME, getGroupName()));
        gitUserName.set(getPreferencesNode().get(GIT_USER_NAME, "username"));
        gitPassword.set(new String(getPreferencesNode().getPassword(GIT_PASSWORD, "password".toCharArray())));
        gitUrl.set(getPreferencesNode().get(GIT_URL, "url"));
        localFolder.set(getPreferencesNode().get(GIT_LOCAL_FOLDER, "data/isaac.data/changesets"));
    }

    @Override
    public Node getTopPanel(Manifold manifold) {
        Button initializeButton = new Button("Initialize");
        initializeButton.setOnAction((event) -> {
            try {
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                syncService.setRootLocation(new File(localFolder.get()));
                syncService.linkAndFetchFromRemote(gitUrl.get(), gitUserName.get(), gitPassword.get().toCharArray());
            } catch (IllegalArgumentException | IOException | AuthenticationException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        });

        Button pushButton = new Button("Push");
        pushButton.setOnAction((event) -> {
            try {
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                syncService.setRootLocation(new File(localFolder.get()));
                syncService.updateCommitAndPush("Push commit", gitUserName.get(), 
                        gitPassword.get().toCharArray(), MergeFailOption.KEEP_LOCAL);
            } catch (IllegalArgumentException | IOException | AuthenticationException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (MergeFailure ex) {
                Logger.getLogger(SynchronizationItemPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Button pullButton = new Button("Pull");
        pullButton.setOnAction((event) -> {
            try {
                SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                syncService.setRootLocation(new File(localFolder.get()));
                syncService.updateFromRemote(gitUserName.get(), 
                        gitPassword.get().toCharArray(), MergeFailOption.KEEP_LOCAL);
            } catch (IllegalArgumentException | IOException | AuthenticationException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (MergeFailure ex) {
                Logger.getLogger(SynchronizationItemPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        ToolBar topBar = new ToolBar(initializeButton, pushButton, pullButton);
        return topBar;
    }
    @Override
    public boolean showDelete() {
        return true;
    }

}
