package sh.komet.fx.stage;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
//import org.scenicview.ScenicView;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.preferences.ConfigurationPreferencePanel;
import sh.isaac.komet.preferences.UserPreferencesPanel;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.util.FxGet;

import java.io.IOException;
import java.util.UUID;

public class StartupAfterSelection extends TimedTaskWithProgressTracker<Void> {
    private final MainApp mainApp;
    private KometPreferences kometPreferences;
    private final boolean reimportMetadata;

    public StartupAfterSelection(MainApp mainApp, boolean reimportMetadata) {
        this.mainApp = mainApp;
        this.reimportMetadata = reimportMetadata;
        this.updateTitle("Setting up user interface");
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

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
                    stage.getScene()
                            .getStylesheets()
                            .add(Iconography.class.getResource("/sh/isaac/komet/iconography/Iconography.css").toString());
                    stage.show();
                    mainApp.replacePrimaryStage(stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


            this.updateMessage("Starting preference service");
            LookupService.startupPreferenceProvider();

            mainApp.configurationPreferences = FxGet.configurationNode(ConfigurationPreferencePanel.class);

            if (mainApp.configurationPreferences.getBoolean(PreferenceGroup.Keys.INITIALIZED, false)) {
                mainApp.firstRun = false;
            }

            Get.configurationService().setSingleUserMode(true);  //TODO eventually, this needs to be replaced with a proper user identifier
            Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
            Get.configurationService().getGlobalDatastoreConfiguration().setMemoryConfiguration(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);
            this.updateMessage("Starting Solor services");
            LookupService.startupIsaac();
            UserPreferencesPanel.login();

            if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                System.out.println("Beta features enabled");
            }


            if (reimportMetadata) {
                TimedTaskWithProgressTracker<Void> tt = new TimedTaskWithProgressTracker<Void>() {
                    {
                        this.updateTitle("Updating metadata");
                    }
                    @Override
                    protected Void call() throws Exception {
                        Get.activeTasks().add(this);
                        try {
                            Get.metadataService().reimportMetadata();
                            return null;
                        } finally {
                            Get.activeTasks().remove(this);
                        }
                    }
                };
                Get.executor().submit(tt);
            }
            // open one new stage with defaults
            // Create a node for stage preferences
            Platform.runLater(new OpenWindows());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Get.activeTasks().remove(this);
        }
        return null;
    }

    private class OpenWindows extends TimedTaskWithProgressTracker<Void> {

        public OpenWindows() {
            super();
            this.updateTitle("Opening windows");
            Get.activeTasks().add(this);
        }

        @Override
        protected Void call() throws Exception {
            try {
                FxGet.load();
                kometPreferences = FxGet.kometPreferences();
                kometPreferences.loadPreferences();


                if (Get.metadataService()
                        .wasMetadataImported()) {
                    final ClassifierService logicService = Get.logicService()
                            .getClassifierService(Coordinates.Manifold.DevelopmentInferredRegularNameSort());
                    final Task<ClassifierResults> classifyTask = logicService.classify();
                    final ClassifierResults classifierResults = classifyTask.get();
                }

                // To update metadata if new metadata is available after database was built.

                kometPreferences.reloadPreferences();
                boolean replacePrimaryStage = true;
                for (WindowPreferences windowPreference : kometPreferences.getWindowPreferenceItems()) {
                    LOG.info("Opening " + windowPreference.getWindowName().get());
                    this.updateMessage("Opening " + windowPreference.getWindowName().get());
                    try {
                        UUID stageUuid = windowPreference.getWindowUuid();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
                        BorderPane root = loader.load();
                        KometStageController controller = loader.getController();
                        root.setId(stageUuid.toString());
                        Stage stage = new Stage(StageStyle.UNIFIED);
                        stage.getProperties().put(FxGet.PROPERTY_KEYS.WINDOW_PREFERENCES, windowPreference);
                        Scene scene = new Scene(mainApp.setupStageMenus(stage, root, windowPreference));

                        stage.setScene(scene);
                        stage.setX(windowPreference.xLocationProperty().doubleValue());
                        stage.setY(windowPreference.yLocationProperty().doubleValue());
                        stage.setWidth(windowPreference.widthProperty().doubleValue());
                        stage.setHeight(windowPreference.heightProperty().doubleValue());
                        stage.setTitle(FxGet.configurationName());
                        controller.setWindowPreferenceItem(windowPreference, stage);


                        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.ico")));
                        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.png")));

                        stage.setTitle(windowPreference.getWindowName().getValue() + ": " + Get.dataStore().getDataStorePath().toFile().getName());
                        // GraphController.setSceneForControllers(scene);
                        scene.getStylesheets()
                                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
                        scene.getStylesheets()
                                .add(IconographyHelper.getStyleSheetStringUrl());
                        FxGet.statusMessageService()
                                .addScene(scene, controller::reportStatus);
                        stage.setOnCloseRequest(MenuProvider::handleCloseRequest);
                        if (replacePrimaryStage) {
                            replacePrimaryStage = false;
                            mainApp.replacePrimaryStage(stage);
                        }
                        stage.show();
                        //ScenicView.show(stage.getScene());

                        MenuProvider.WINDOW_COUNT.incrementAndGet();
                        mainApp.configurationPreferences.sync();
                        //            ScenicView.show(scene);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Get.activeTasks().remove(this);
            }
            return null;
        }
    }

}
