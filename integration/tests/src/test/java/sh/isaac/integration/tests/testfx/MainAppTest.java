package sh.isaac.integration.tests.testfx;

import com.sun.javafx.scene.control.VirtualScrollBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import org.testfx.api.FxToolkit;
import org.testfx.robot.ClickRobot;
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
import sh.isaac.komet.gui.treeview.MultiParentTreeCell;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.preferences.ConfigurationPreferencePanel;
import sh.komet.fx.stage.KometStageController;
import sh.komet.fx.stage.MainApp;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.WindowPreferenceItems;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
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
        //Click Plus Button in Center Pane
        HBox editorPane = lookup("#editorLeftPane").query();
        StackPane FxInterface_CenterStackPane = (StackPane) editorPane.getChildren().get(0);
        Node plusButton = FxInterface_CenterStackPane.getChildren().get(1);
        clickOn(plusButton);
        sleep(500);
        clickOn(plusButton);
        sleep(500);

        //Digging through Panes to find the taxonomy button for the Center Pane
        TabPane FxInterface_CenterTabPane = (TabPane) FxInterface_CenterStackPane.getChildren().get(0);
        BorderPane brdrpn = (BorderPane) FxInterface_CenterTabPane.getTabs().get(0).getContent();
        GridPane grdpn = (GridPane) ((BorderPane) brdrpn.getChildren().get(0)).getChildren().get(0);
        MenuButton linkAndSearchButton = (MenuButton) grdpn.getChildren().get(0);

        //Clicking the link and search button and then the taxonomy button
        clickOn(linkAndSearchButton);
        sleep(500);
        Node taxonomyButton = lookup("taxonomy").query();
        clickOn(taxonomyButton);
        sleep(500);

        //Locating root of tree structure and scroll bar
        MultiParentTreeCell originNode = lookup("SOLOR concept").query();
        VirtualScrollBar treeViewScrollBar = (VirtualScrollBar) originNode.getParent().getParent().getParent().getChildrenUnmodifiable().get(2);
        clickOn(originNode.getChildrenUnmodifiable().get(0));
        sleep(500);

        //Expanding tree items
        clickTreeItem("Metadata", treeViewScrollBar);
        clickTreeItem("Node operator", treeViewScrollBar);
        clickTreeItem("Role", treeViewScrollBar);
        clickTreeItem("This item does not exist and will print an error message to console", treeViewScrollBar);

//        //Open Import window
//        MenuButton FxInterface_classifierMenu = lookup("#classifierMenuButton").query();
//        clickOn(FxInterface_classifierMenu).clickOn("Selective import and transform");
//        sleep(1000);
//
//        //Add Item to Import
//        Button FxInterface_addButton = lookup("#addButton").queryButton();
//        clickOn(FxInterface_addButton);

        sleep(5000);
    }

    private void clickTreeItem(String nodeText, VirtualScrollBar treeViewScrollBar) {
        MultiParentTreeCell nodeFromTextQuery = (isFoundInScrollPane(nodeText, treeViewScrollBar) ? lookup(nodeText).query() : null);
        if (nodeFromTextQuery == null) {
            System.out.println("ERROR: No node of name \"" + nodeText + "\" found.");
            return;
        }
        Node nodeToClick = nodeFromTextQuery.getChildrenUnmodifiable().get(0);
        clickOn(nodeToClick);
        sleep(500);
    }

    private boolean isFoundInScrollPane (String node, VirtualScrollBar scrollBar) {
        scrollBar.setValue(0);
        while (lookup(node).tryQuery().isEmpty() && scrollBar.getValue() != 1) {
            scroll(5, VerticalDirection.UP);
        }
        scroll(5, VerticalDirection.UP);
        if (lookup(node).tryQuery().isEmpty()) {
            scrollBar.setValue(0);
            return false;
        } else {
            System.out.println("NODE: " + node);
            return true;
        }
    }

}
