package sh.komet.fx.stage;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
//import org.scenicview.ScenicView;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.preferences.UserPreferencesPanel;
import sh.isaac.model.builder.ConceptBuilderImpl;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.util.FxGet;

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

            mainApp.configurationPreferences = FxGet.kometConfigurationRootNode();

            if (mainApp.configurationPreferences.getBoolean(PreferenceGroup.Keys.INITIALIZED, false)) {
                mainApp.firstRun = false;
            }

            Get.configurationService().setSingleUserMode(true);  //TODO eventually, this needs to be replaced with a proper user identifier
            Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
            Get.configurationService().getGlobalDatastoreConfiguration().setMemoryConfiguration(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);
            this.updateMessage("Starting Solor services");
            LookupService.startupIsaac();
            
            addUsers();
            
            UserPreferencesPanel.login();

            if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                LOG.info("Beta features enabled");
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

    /**
     * Add users for Komet GUI
     * TODO replace with some external mechanism to provide user info?  Or a login / self register system in the GUI
     * TODO these "terminology" authors should not be here at all, they should each be created by the terminology loader itself...
     */
    private void addUsers() {
        String[] users = new String[] {"Keith Campbell", "Deloitte User", "Bootstrap administrator", "Clinvar author", "UMLS author", 
                "LOINC author", "LIVD author", "CVX author", "SNOMED author", "RxNorm author", "HL7 author", "CDC author", "NLM author",
                "NCI author", "VA author", "DOD author", "FEHRM author", "Logica author", "Susan Castillo", "Penni Hernandez", "Ioana Singureanu"};
        
        Transaction t = Get.commitService().newTransaction("create users");
        WriteCoordinate writeCoordinate = new WriteCoordinateImpl(MetaData.USER____SOLOR.getNid(), MetaData.USERS_MODULE____SOLOR.getNid(), 
                MetaData.PRIMORDIAL_PATH____SOLOR.getNid(), t);
        
        try {
            int created = 0;
            for (String user : users) {
                ConceptBuilder cb = new ConceptBuilderImpl(user, ConceptProxy.METADATA_SEMANTIC_TAG, null, TermAux.ENGLISH_LANGUAGE, 
                        TermAux.US_DIALECT_ASSEMBLAGE, Coordinates.Logic.ElPlusPlus(), TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
                cb.setT5UuidNested(UuidT5Generator.PATH_ID_FROM_FS_DESC);
                UUID conceptId = cb.getPrimordialUuid();
                
                if (!Get.identifierService().hasUuid(conceptId)) {
                    
                    LogicalExpressionBuilder defBuilder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
                    NecessarySet(And(ConceptAssertion(MetaData.USER____SOLOR.getNid(), defBuilder)));
                    LogicalExpression logicalExpression = defBuilder.build();
                    
                    cb.addLogicalExpression(logicalExpression);
                    cb.buildAndWrite(writeCoordinate).get();
                    created++;
                }
            }
            
            if (created > 0) {
                t.commit().get();
            }
            else {
                t.cancel().get();
            }
            LOG.info("Created {} users", created);
        }
        catch (NoSuchElementException | IllegalArgumentException | IllegalStateException | InterruptedException | ExecutionException e) {
            LOG.error("Unexpected problem adding missing users!", e);
        }
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
