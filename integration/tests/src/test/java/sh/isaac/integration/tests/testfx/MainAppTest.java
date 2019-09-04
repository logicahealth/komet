package sh.isaac.integration.tests.testfx;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.jena.base.Sys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.preferences.ConfigurationPreferencePanel;
import sh.komet.fx.stage.KometStageController;
import sh.komet.fx.stage.MainApp;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.WindowPreferenceItems;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Ignore
public class MainAppTest extends ApplicationTest {

    private final String DATABASE_PATH_STRING = "target/data";
    private MainApp app = new MainApp();
    private KometPreferences kometPreferences;
    public IsaacPreferences configurationPreferences;
    protected static final Logger LOG = LogManager.getLogger();
    private KometStageController controller;


    @Override
    public void init() throws Exception {
        super.init();
        Files.walk(Paths.get(this.DATABASE_PATH_STRING)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

        System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, this.DATABASE_PATH_STRING);
        LookupService.startupPreferenceProvider();
        this.app.configurationPreferences = FxGet.configurationNode(ConfigurationPreferencePanel.class);
        this.app.firstRun = true;

        Get.configurationService().setSingleUserMode(true);  //TODO eventually, this needs to be replaced with a proper user identifier
        Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
        Get.configurationService().getGlobalDatastoreConfiguration().setMemoryConfiguration(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);
        LookupService.startupIsaac();
        Get.metadataService().reimportMetadata();

        kometPreferences = FxGet.kometPreferences();

        Platform.runLater(() -> kometPreferences.loadPreferences(FxGet.getManifold(Manifold.ManifoldGroup.TAXONOMY)));


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

        kometPreferences.reloadPreferences();
    }

    @Override
    public void start (Stage stage) throws Exception {

        for (WindowPreferenceItems windowPreference : kometPreferences.getWindowPreferences()) {
            try {
                UUID stageUuid = UUID.randomUUID();
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/KometStageScene.fxml"));
                BorderPane root = loader.load();
//                KometStageController controller = loader.getController();
                this.controller = loader.getController();
                controller.setPreferencesNode(windowPreference.getPreferenceNode());
                root.setId(stageUuid.toString());
                stage.setTitle(FxGet.getConfigurationName());
                Scene scene = new Scene(this.app.setupStageMenus(stage, root));
                stage.setScene(scene);
                stage.initStyle(StageStyle.DECORATED);
                stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.ico")));
                stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.png")));

                windowPreference.getWindowName().addListener((observable, oldValue, newValue) -> stage.setTitle(newValue));
                stage.setTitle(windowPreference.getWindowName().getValue());
                scene.getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
                scene.getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
                FxGet.statusMessageService().addScene(scene, controller::reportStatus);
                stage.setOnCloseRequest(MenuProvider::handleCloseRequest);
                stage.show();
                this.app.configurationPreferences.sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void tearDown () throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Test
    public void testImport() {
        MenuButton classifierMenu = lookup("#classifierMenuButton").query();
        List<MenuItem> classifierMenuItemsDisplayed = classifierMenu.getItems();
        List<MenuItem> classifierMenuItemsFromController = controller.getClassifierMenuItems();
        assert classifierMenuItemsDisplayed.size() == classifierMenuItemsFromController.size();
        clickMenuItems("#classifierMenuButton", classifierMenuItemsFromController, classifierMenuItemsDisplayed);
        sleep(100000);
    }

    public boolean clickMenuItems(String menuId, List<MenuItem> itemsProvided, List<MenuItem> itemsDisplayed) {
        if (itemsProvided.size() == itemsDisplayed.size()) {
            for (int i=0; i<itemsProvided.size(); i++) {
                System.out.println(itemsProvided.get(i).getText());
                clickOn(menuId).clickOn(itemsProvided.get(i).getText());
                sleep(1000);
            }
            return true;
        }
        else {
            System.out.println("ERROR: Number of items Provided and Displayed are not the same.");
            return false;
        }
    }
}
