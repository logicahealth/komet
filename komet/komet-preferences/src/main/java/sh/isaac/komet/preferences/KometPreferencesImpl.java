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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.KometPreferences;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class KometPreferencesImpl implements KometPreferences {

    private static final Logger LOG = LogManager.getLogger();

    private KometPreferencesController kpc;
    private Stage preferencesStage;
    private Manifold manifold;

    public KometPreferencesImpl() {

    }

    @Override
    public void resetUserPreferences() {
        try {
            IsaacPreferences userPreferences = FxGet.userNode(ConfigurationPreferences.class);
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
        IsaacPreferences preferences = FxGet.configurationNode(ConfigurationPreferences.class);
        if (kpc == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/preferences/KometPreferences.fxml"));
                Parent root = loader.load();
                this.kpc = loader.getController();
                this.kpc.setManifold(manifold);
                Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, manifold, kpc);
                if (treeRoot.isPresent()) {
                    this.kpc.setRoot(treeRoot.get());
                }

                root.setId(UUID.randomUUID()
                        .toString());

                this.preferencesStage = new Stage();
                this.preferencesStage.setTitle(FxGet.getConfigurationName() + " preferences");
                FxGet.configurationNameProperty().addListener((observable, oldValue, newValue) -> {
                    this.preferencesStage.setTitle(newValue + " preferences");
                });
                Scene scene = new Scene(root);

                this.preferencesStage.setScene(scene);
                scene.getStylesheets()
                        .add(FxGet.fxConfiguration().getUserCSSURL().toString());
                scene.getStylesheets()
                        .add(Iconography.getStyleSheetStringUrl());
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void reloadPreferences() {
        Get.preferencesService().reloadConfigurationPreferences();
        IsaacPreferences preferences = FxGet.configurationNode(ConfigurationPreferences.class);
        Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, manifold, kpc);
        if (treeRoot.isPresent()) {
            this.kpc.setRoot(treeRoot.get());
        }
    }

    @Override
    public void showPreferences(Manifold manifold) {
        IsaacPreferences preferences = FxGet.configurationNode(ConfigurationPreferences.class);
        if (kpc == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/preferences/KometPreferences.fxml"));
                Parent root = loader.load();
                this.kpc = loader.getController();
                this.kpc.setManifold(manifold);
                Optional<PreferencesTreeItem> treeRoot = PreferencesTreeItem.from(preferences, manifold, kpc);
                if (treeRoot.isPresent()) {
                    this.kpc.setRoot(treeRoot.get());
                }

                root.setId(UUID.randomUUID()
                        .toString());

                this.preferencesStage = new Stage();
                this.preferencesStage.setTitle("KOMET Preferences");
                Scene scene = new Scene(root);

                this.preferencesStage.setScene(scene);
                scene.getStylesheets()
                        .add(FxGet.fxConfiguration().getUserCSSURL().toString());
                scene.getStylesheets()
                        .add(Iconography.getStyleSheetStringUrl());
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
        preferencesStage.show();
        preferencesStage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == false) {
                this.kpc = null;
                this.preferencesStage = null;
            }
        });
        preferencesStage.setAlwaysOnTop(true);
    }

    @Override
    public void closePreferences() {
        this.preferencesStage.close();
        this.kpc = null;
        this.preferencesStage = null;
    }
}
