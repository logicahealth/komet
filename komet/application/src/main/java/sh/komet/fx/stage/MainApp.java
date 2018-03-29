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

import java.lang.management.ManagementFactory;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.codecentric.centerdevice.MenuToolkit;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.statement.StatementView;
import sh.isaac.komet.statement.StatementViewController;
import sh.isaac.model.statement.ClinicalStatementImpl;
import sh.isaac.solor.rf2.direct.Rf2DirectImporter;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------
public class MainApp
        extends Application {
// TODO add TaskProgressView
// http://dlsc.com/2014/10/13/new-custom-control-taskprogressview/
// http://fxexperience.com/controlsfx/features/   

    public static final String SPLASH_IMAGE = "prism-splash.png";
    protected static final Logger LOG = LogManager.getLogger();
    private static Stage primaryStage;

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
        LookupService.startupPreferenceProvider();
        
        Get.configurationService().setSingleUserMode(true);  //TODO eventually, this needs to be replaced with a proper user identifier
        Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
        Get.configurationService().getGlobalDatastoreConfiguration().setMemoryConfiguration(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY); 
        
        //TODO this will likely go away, at some point...
//        Rf2DirectImporter.importDynamic = true;
        
        LookupService.startupIsaac();
        
        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
            System.out.println("Beta features enabled");
        }
        
        //TODO We aren't yet making use of semantic indexes, so no reason to build them.  Disable for performance reasons.
        //However, once the index-config-per-assemblage framework is fixed, this should be removed, and the indexers will
        //be configured at the assemblage level.
        LookupService.getService(IndexBuilderService.class, "semantic index").setEnabled(false);

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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
        Parent root = loader.load();
        KometStageController controller = loader.getController();

        root.setId(UUID.randomUUID()
                .toString());


        stage.setTitle("Viewer");

        // SNAPSHOT
        // Chronology
        // Reflector
        //
        // Logic, Language, Dialect, Chronology,
        // LILAC Reflector (LOGIC,
        // COLLD Reflector: Chronology of Logic, Language, and Dialect : COLLAD
        // COLLDAE Chronology of Logic, Langugage, Dialect, and Extension
        // CHILLDE
        // Knowledge, Language, Dialect, Chronology
        // KOLDAC
        // Get the toolkit
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        if (menuToolkit != null) {
            // Create the default Application menu
            Menu defaultApplicationMenu = menuToolkit.createDefaultApplicationMenu("SOLOR Viewer");
            MenuItem aboutItem = defaultApplicationMenu.getItems().get(0);
            aboutItem.setOnAction(this::handleAbout);
            MenuItem quitItem = defaultApplicationMenu.getItems().get(defaultApplicationMenu.getItems().size() - 1);
            quitItem.setOnAction(this::close);
            menuToolkit.setApplicationMenu(defaultApplicationMenu);
        } else {
            BorderPane wrappingPane = new BorderPane(root);
            Menu defaultApplicationMenu = new Menu("Viewer");
            MenuItem aboutItem = new MenuItem("About...");
            defaultApplicationMenu.getItems().add(aboutItem);
            defaultApplicationMenu.getItems().add(new SeparatorMenuItem());
            aboutItem.setOnAction(this::handleAbout);
            MenuItem quitItem = new MenuItem("Quit");
            defaultApplicationMenu.getItems().add(quitItem);
            quitItem.setOnAction(this::close);
            wrappingPane.setTop(new MenuBar(defaultApplicationMenu));
            root = wrappingPane;
            stage.setHeight(stage.getHeight() + 20);
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // GraphController.setSceneForControllers(scene);
        scene.getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        scene.getStylesheets()
                .add(Iconography.getStyleSheetStringUrl());
        FxGet.statusMessageService()
                .addScene(scene, controller::reportStatus);
        stage.show();
        stage.setOnCloseRequest(this::handleShutdown);

        // ScenicView.show(scene);
        Manifold statementManifold = Manifold.make(Manifold.CLINICAL_STATEMENT_GROUP_NAME);
        StatementViewController statementController = StatementView.show(statementManifold);
        statementController.setClinicalStatement(new ClinicalStatementImpl(statementManifold));
        statementController.getClinicalStatement().setManifold(statementManifold);
        
    }

    private void close(ActionEvent event) {
        event.consume();
        shutdown();
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

    private void handleShutdown(WindowEvent e) {
        // need this to all happen on a non event thread...
        e.consume();
        shutdown();
    }

    protected void shutdown() {
        Get.applicationStates().remove(ApplicationStates.RUNNING);
        Get.applicationStates().add(ApplicationStates.STOPPING);
        Get.executor().execute(() -> {
            LookupService.syncAll();  //This should be unnecessary....
            Platform.runLater(() -> {
                try {
                    stop();
                    LookupService.shutdownSystem();
                    Platform.exit();
                    System.exit(0);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
        });
    }
}
