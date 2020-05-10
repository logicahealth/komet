/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 *
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 *
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.komet.fx.stage;

import com.sun.javafx.application.PlatformImpl;
import de.codecentric.centerdevice.MenuToolkit;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.api.MultiException;
//import org.scenicview.ScenicView;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.SystemUtils;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.preferences.RootPreferences;
import sh.isaac.komet.statement.StatementView;
import sh.isaac.komet.statement.StatementViewController;
import sh.isaac.model.statement.ClinicalStatementImpl;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.PersonaChangeListener;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.PersonaChangeListeners;


import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.prefs.BackingStoreException;

//~--- classes ----------------------------------------------------------------
public class MainApp
        extends Application implements PersonaChangeListener {
// TODO add TaskProgressView
// http://dlsc.com/2014/10/13/new-custom-control-taskprogressview/
// http://fxexperience.com/controlsfx/features/   

    protected static final Logger LOG = LogManager.getLogger();
    private static Stage primaryStage;
    public IsaacPreferences configurationPreferences;
    public boolean firstRun = true;
    private boolean setupWindowMenu = true;
    private boolean setupWindowToolkit = true;

    //~--- methods -------------------------------------------------------------

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    // Create drop label for identified components
    // Create walker panel
    // grow & shrink icons for tabs & tab panels...
    // for each tab group, add a + control to create new tabs...
    @Override
    public void start(Stage stage)
            throws Exception {
        MainApp.primaryStage = stage;
        //stage.initStyle(StageStyle.UTILITY);
        // TODO have SvgImageLoaderFactory autoinstall as part of a HK2 service.
        LOG.info("Startup memory info: "
                + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString());

        SvgImageLoaderFactory.install();


        FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("/fxml/SelectDataSource.fxml"));
        BorderPane sourceRoot = sourceLoader.load();
        SelectDataSourceController selectDataSourceController = sourceLoader.getController();
        selectDataSourceController.setMainApp(this);

        stage.initStyle(StageStyle.UTILITY);
        Scene sourceScene = new Scene(sourceRoot, 600, 400);
        stage.setScene(sourceScene);
        stage.getScene()
                .getStylesheets()
                .add(MainApp.class.getResource("/user.css").toString());
        stage.show();
        PersonaChangeListeners.addPersonaChangeListener(this);
    }

    public void replacePrimaryStage(Stage primaryStage) {
        Stage oldStage = MainApp.primaryStage;
        MainApp.primaryStage = primaryStage;
        if (oldStage != null) {
            oldStage.hide();
            oldStage.close();
        }
    }


    protected Parent setupStageMenus(Stage stage, BorderPane root, WindowPreferencesItem windowPreference) throws MultiException {
        BorderPane stageRoot = root;
        // Get the toolkit
        MenuToolkit tk = MenuToolkit.toolkit();  //Note, this only works on Mac....
        MenuBar mb = new MenuBar();
        if (setupWindowMenu) {
            setupWindowMenu = false;
            for (AppMenu ap : AppMenu.values()) {
                if (ap == AppMenu.NEW_WINDOW) {
                    continue;
                }
                if (!FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                    if (ap == AppMenu.EDIT) {
                        continue;
                    }
                    if (ap == AppMenu.FILE) {
                        continue;
                    }
                    if (ap == AppMenu.TOOLS) {
                        continue;
                    }
                }
                mb.getMenus().add(ap.getMenu());

                for (MenuProvider mp : LookupService.get().getAllServices(MenuProvider.class)) {
                    if (mp.getParentMenus().contains(ap)) {
                        for (MenuItem mi : mp.getMenuItems(ap, primaryStage.getOwner())) {
                            ap.getMenu().getItems().add(mi);
                        }
                    }
                }
                ap.getMenu().getItems().sort((MenuItem o1, MenuItem o2) -> {
                    // Separator menu items have null text.
                    String o1Text = o1.getText();
                    if (o1Text == null) {
                        o1Text = "";
                    }
                    String o2Text = o2.getText();
                    if (o2Text == null) {
                        o2Text = "";
                    }
                    return o1Text.compareTo(o2Text);
                });

                switch (ap) {
                    case APP:
                        if (tk != null) {
                            MenuItem aboutItem = new MenuItemWithText("About KOMET...");
                            aboutItem.setOnAction(this::handleAbout);
                            ap.getMenu().getItems().add(aboutItem);
                            ap.getMenu().getItems().add(new SeparatorMenuItem());
                        }
                        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                            MenuItem prefsItem = new MenuItemWithText("KOMET Preferences...");
                            //TODO TEMP to do  something. Need to make it do something better.
                            prefsItem.setOnAction(this::handlePrefs);
                            ap.getMenu().getItems().add(prefsItem);
                            ap.getMenu().getItems().add(new SeparatorMenuItem());
                        }
                        if (tk == null) {
                            MenuItem quitItem = new MenuItemWithText("Quit");
                            quitItem.setOnAction(this::close);
                            ap.getMenu().getItems().add(quitItem);
                        }
                        break;
                    case WINDOW:
                        Menu newWindowMenu = AppMenu.NEW_WINDOW.getMenu();
                        AppMenu.WINDOW.getMenu().getItems().add(newWindowMenu);
                        updateNewWindowMenu();

                        break;
                    case HELP:
                        if (tk == null) {
                            MenuItem aboutItem = new MenuItemWithText("About...");
                            aboutItem.setOnAction(this::handleAbout);
                            ap.getMenu().getItems().add(aboutItem);
                        }

                        break;
                    default:
                        break;
                }
            }
            com.sun.glass.ui.Application.GetApplication().setEventHandler(
                    new com.sun.glass.ui.Application.EventHandler() {
                        @Override
                        public void handleQuitAction(com.sun.glass.ui.Application app, long time) {
                            shutdown();
                        }

                        @Override
                        public boolean handleThemeChanged(String themeName) {
                            return PlatformImpl.setAccessibilityTheme(themeName);
                        }
                    });
        }

        if (tk != null) {
            // this is used on Mac... I don't think its uses anywhere else...
            // Dan notes, code is rather confusing, and I can't test... it was making both an appMenu and a defaultApplicationMenu
            //but not being consistent about things, no idea which was actually being used on mac.
            // TBD: services menu


            if (setupWindowToolkit) {
                setupWindowToolkit = false;
                tk.setForceQuitOnCmdQ(false);
                tk.setApplicationMenu(AppMenu.APP.getMenu());

                AppMenu.APP.getMenu().getItems().addAll(tk.createHideMenuItem(AppMenu.APP.getMenu().getText()), tk.createHideOthersMenuItem(),
                        tk.createUnhideAllMenuItem(), new SeparatorMenuItem(), tk.createQuitMenuItem(AppMenu.APP.getMenu().getText()));

                AppMenu.WINDOW.getMenu().getItems().addAll(new SeparatorMenuItem(),
                        tk.createMinimizeMenuItem(),
                        tk.createZoomMenuItem(), tk.createCycleWindowsItem(),
                        new SeparatorMenuItem(), tk.createBringAllToFrontItem());
                tk.autoAddWindowMenuItems(AppMenu.WINDOW.getMenu());
                tk.setGlobalMenuBar(mb);
            }


        } else {
            //And for everyone else....
            stageRoot = new BorderPane(stageRoot);
            stageRoot.setTop(mb);
            stage.setHeight(stage.getHeight() + 20);
        }

         return stageRoot;
    }

    Set<UUID> personaUuids = new HashSet<>();


    @Override
    public void personaChanged(PersonaItem item, boolean active) {
        List<MenuItem> menuItems = new ArrayList<>(AppMenu.NEW_WINDOW.getMenu().getItems());
        AppMenu.NEW_WINDOW.getMenu().getItems().clear();
        String personaUuidStr = item.getPersonaUuid().toString();

        if (active) {
            if (!personaUuids.contains(item.getPersonaUuid())) {
                personaUuids.add(item.getPersonaUuid());
                MenuItem newKometWindowItem = new MenuItemWithText(item.nameProperty().get());
                newKometWindowItem.setId(personaUuidStr);
                menuItems.add(newKometWindowItem);
                newKometWindowItem.setOnAction(event -> {
                    //FxGet.dialogs().showInformationDialog("Opening new window. ", item.nameProperty().get());
                    this.newViewer(item);
                });
            } else {
                for (MenuItem menuItem: menuItems) {
                    if (menuItem.getId() != null && menuItem.getId().equals(personaUuidStr)) {
                        menuItem.setText(item.nameProperty().get());
                        break;
                    }
                }
            }
        } else {
            int indexToDelete = -1;
            for (int i = 0; i < menuItems.size(); i++) {
                MenuItem menuItem = menuItems.get(i);
                if (menuItem.getId() != null && menuItem.getId().equals(personaUuidStr)) {
                    indexToDelete = i;
                    break;
                }
            }
            menuItems.remove(indexToDelete);
        }

        menuItems.sort((o1, o2) -> {
            return o1.getText().compareTo(o2.getText());
        });
        AppMenu.NEW_WINDOW.getMenu().getItems().addAll(menuItems);
    }

    private void updateNewWindowMenu() {
        Menu newWindowMenu = AppMenu.NEW_WINDOW.getMenu();
        newWindowMenu.getItems().clear();
        List<MenuItem> itemsToAdd = new ArrayList<>();
        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
            // TODO: Go away after personas completely implemented.
            MenuItem newStatementWindowItem = new MenuItemWithText("Statement Window");
            newStatementWindowItem.setOnAction(this::newStatement);
            itemsToAdd.add(newStatementWindowItem);
            for (MenuProvider mp : LookupService.get().getAllServices(MenuProvider.class)) {
                if (mp.getParentMenus().contains(AppMenu.NEW_WINDOW)) {
                    for (MenuItem menuItem : mp.getMenuItems(AppMenu.NEW_WINDOW, primaryStage.getOwner())) {
                        menuItem.getProperties().put(MenuProvider.PARENT_PREFERENCES, FxGet.configurationNode(RootPreferences.class));
                        newWindowMenu.getItems().add(menuItem);
                    }
                }
            }
        }
        // Add personas here...
        for (PersonaItem personaItem: FxGet.kometPreferences().getPersonaPreferences()) {
            MenuItem newKometWindowItem = new MenuItemWithText(personaItem.nameProperty().get());
            newKometWindowItem.setOnAction(event -> {
                this.newViewer(personaItem);
            });
            itemsToAdd.add(newKometWindowItem);
        }

        // TODO: Go away after personas completely implemented.
        MenuItem newKometWindowItem = new MenuItemWithText("Viewer Window");

        itemsToAdd.add(newKometWindowItem);


        itemsToAdd.sort((o1, o2) -> {
                    return o1.getText().compareTo(o2.getText());
                });
        newWindowMenu.getItems().addAll(itemsToAdd);

    }

    private void close(ActionEvent event) {
        event.consume();
        shutdown();
    }

    private void newStatement(ActionEvent event) {
        Manifold statementManifold = FxGet.manifold(ManifoldGroup.CLINICAL_STATEMENT);
        StatementViewController statementController = StatementView.show(statementManifold,
                MenuProvider::handleCloseRequest);

        statementController.setClinicalStatement(new ClinicalStatementImpl(statementManifold));
        statementController.getClinicalStatement().setManifold(statementManifold);
        MenuProvider.WINDOW_COUNT.incrementAndGet();
    }

    private void newViewer(PersonaItem personaItem) {

        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
            BorderPane root = loader.load();
            KometStageController controller = loader.getController();
            WindowPreferencesItem personaWindowPreferences = personaItem.createNewWindowPreferences();

            root.setId(UUID.randomUUID().toString());

            stage.setTitle(personaWindowPreferences.getWindowName().getValue());

            //Menu hackery
            Scene scene;
            if (SystemUtils.isMacOS()) {
                scene = new Scene(root);
            } else {
                scene = new Scene(setupStageMenus(stage, root, personaWindowPreferences));
            }

            stage.setScene(scene);
            controller.setWindowPreferenceItem(personaWindowPreferences, stage);
            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.ico")));
            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.png")));

            // GraphController.setSceneForControllers(scene);
            scene.getStylesheets()
                    .add(FxGet.fxConfiguration().getUserCSSURL().toString());
            scene.getStylesheets()
                    .add(IconographyHelper.getStyleSheetStringUrl());
            FxGet.statusMessageService()
                    .addScene(scene, controller::reportStatus);
            stage.setOnCloseRequest(MenuProvider::handleCloseRequest);
            stage.show();
            MenuProvider.WINDOW_COUNT.incrementAndGet();

        } catch (Exception ex) {
            FxGet.dialogs().showErrorDialog("Error opening new KOMET window.", ex);
        }
    }

    private void handlePrefs(ActionEvent event) {
        Stage prefStage = FxGet.kometPreferences().showPreferences(FxGet.manifold(ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ANY_NODE));
    }

    private void handleAbout(ActionEvent event) {
        event.consume();
        System.out.println("Handle about...");
        //create stage which has set stage style transparent
        final Stage stage = new Stage(StageStyle.TRANSPARENT);

        //create root node of scene, i.e. group
        Group rootGroup = new Group();

        //create scene with set width, height and color
        Scene scene = new Scene(rootGroup, 806, 675, Color.TRANSPARENT);

        //set scene to stage
        stage.setScene(scene);

        //center stage on screen
        stage.centerOnScreen();
        Image image = new Image(MainApp.class.getResourceAsStream("/images/about@2x.png"));
        ImageView aboutView = new ImageView(image);

        aboutView.setFitHeight(675);
        aboutView.setPreserveRatio(true);
        aboutView.setSmooth(true);
        aboutView.setCache(true);
        rootGroup.getChildren().add(aboutView);
        //show the stage
        stage.show();

        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == false) {
                stage.close();
            }
        });
    }

    protected void shutdown() {
        for (Transaction transaction: Get.commitService().getPendingTransactionList()) {
            transaction.cancel();
        }
        Get.applicationStates().remove(ApplicationStates.RUNNING);
        Get.applicationStates().add(ApplicationStates.STOPPING);
        try {
            configurationPreferences.flush();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        Thread shutdownThread = new Thread(() -> {  //Can't use the thread poool for this, because shutdown 
            //system stops the thread pool, which messes up the shutdown sequence.
            LookupService.shutdownSystem();
            Platform.runLater(() -> {
                try {
                    configurationPreferences.flush();
                    Platform.exit();
                    System.exit(0);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
        }, "shutdown-thread");
        shutdownThread.setDaemon(true);
        shutdownThread.start();
    }

}
