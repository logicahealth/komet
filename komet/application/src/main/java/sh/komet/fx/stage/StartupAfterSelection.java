package sh.komet.fx.stage;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.preferences.ConfigurationPreferencePanel;
import sh.isaac.komet.preferences.PreferenceGroup;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.WindowPreferenceItems;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.io.IOException;
import java.util.UUID;

public class StartupAfterSelection implements Runnable {
    private final MainApp mainApp;
    private KometPreferences kometPreferences;
    private final boolean reimportMetadata;

    public StartupAfterSelection(MainApp mainApp, boolean reimportMetadata) {
        this.mainApp = mainApp;
        this.reimportMetadata = reimportMetadata;
    }

    @Override
    public void run()  {

        try {

            Platform.runLater(() -> {
                try {
                    FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("/fxml/StartupScreen.fxml"));
                    BorderPane sourceRoot = sourceLoader.load();
                    StartupScreenController startupSceneController = sourceLoader.getController();
                    Stage stage = new Stage(StageStyle.UTILITY);
                    stage.setResizable(false);
                    Scene sourceScene = new Scene(sourceRoot, 965, 495);
                    stage.setScene(sourceScene);
                    stage.getScene()
                            .getStylesheets()
                            .add(MainApp.class.getResource("/user.css").toString());
                    stage.show();
                    mainApp.replacePrimaryStage(stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });



            LookupService.startupPreferenceProvider();

            mainApp.configurationPreferences = FxGet.configurationNode(ConfigurationPreferencePanel.class);

            if (mainApp.configurationPreferences.getBoolean(PreferenceGroup.Keys.INITIALIZED, false)) {
                mainApp.firstRun = false;
            }

            Get.configurationService().setSingleUserMode(true);  //TODO eventually, this needs to be replaced with a proper user identifier
            Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
            Get.configurationService().getGlobalDatastoreConfiguration().setMemoryConfiguration(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);
            LookupService.startupIsaac();

            if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                System.out.println("Beta features enabled");
            }


            if (reimportMetadata) {
                Get.metadataService().reimportMetadata();
            }
            // open one new stage with defaults
            // Create a node for stage preferences
            Platform.runLater(new OpenWindows());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     private class OpenWindows implements Runnable {

            @Override
            public void run() {
                try {
                    kometPreferences = FxGet.kometPreferences();
                    kometPreferences.loadPreferences(FxGet.getManifold(Manifold.ManifoldGroup.TAXONOMY));


                    if (Get.metadataService()
                            .wasMetadataImported()) {
                        final StampCoordinate stampCoordinate = Get.coordinateFactory()
                                .createDevelopmentLatestStampCoordinate();
                        final LogicCoordinate logicCoordinate = Get.coordinateFactory()
                                .createStandardElProfileLogicCoordinate();
                        final EditCoordinate editCoordinate = Get.coordinateFactory()
                                .createClassifierSolorOverlayEditCoordinate();
                        final ClassifierService logicService = Get.logicService()
                                .getClassifierService(
                                        stampCoordinate,
                                        logicCoordinate,
                                        editCoordinate);
                        final Task<ClassifierResults> classifyTask = logicService.classify();
                        final ClassifierResults classifierResults = classifyTask.get();
                    }

                    // To update metadata if new metadata is available after database was built.

                    kometPreferences.reloadPreferences();
                    boolean replacePrimaryStage = true;
                    for (WindowPreferenceItems windowPreference : kometPreferences.getWindowPreferences()) {
                        try {
                            UUID stageUuid = UUID.randomUUID();
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
                            BorderPane root = loader.load();
                            KometStageController controller = loader.getController();
                            controller.setPreferencesNode(windowPreference.getPreferenceNode());
                            root.setId(stageUuid.toString());
                            Stage stage = new Stage(StageStyle.UNIFIED);
                            stage.setTitle(FxGet.getConfigurationName());
                            Scene scene = new Scene(mainApp.setupStageMenus(stage, root));
                            stage.setScene(scene);
                            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.ico")));
                            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.png")));

                            windowPreference.getWindowName().addListener((observable, oldValue, newValue) -> {
                                stage.setTitle(newValue);
                            });
                            stage.setTitle(windowPreference.getWindowName().getValue());
                            // GraphController.setSceneForControllers(scene);
                            scene.getStylesheets()
                                    .add(FxGet.fxConfiguration().getUserCSSURL().toString());
                            scene.getStylesheets()
                                    .add(Iconography.getStyleSheetStringUrl());
                            FxGet.statusMessageService()
                                    .addScene(scene, controller::reportStatus);
                            stage.setOnCloseRequest(MenuProvider::handleCloseRequest);
                            if (replacePrimaryStage) {
                                replacePrimaryStage = false;
                                mainApp.replacePrimaryStage(stage);
                            }
                            stage.show();
                            mainApp.configurationPreferences.sync();
    //            ScenicView.show(scene);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

}
