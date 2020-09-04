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

import static sh.komet.gui.contract.MenuProvider.Keys.WINDOW_PREFERENCE_ABSOLUTE_PATH;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.convert.mojo.turtle.TurtleImportHK2Direct;
import sh.isaac.komet.gui.exporter.ExportView;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.preferences.window.WindowPreferencePanel;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.NodeFactory;
import sh.komet.gui.contract.StatusMessageConsumer;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.contract.preferences.TabSpecification;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.importation.ArtifactImporter;
import sh.komet.gui.importation.ImportView;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.tab.TabWrapper;
import sh.komet.gui.control.manifold.CoordinateMenuFactory;
import sh.komet.gui.util.FxGet;


/**
 * Root node of scene is given a UUID for unique identification.
 *
 * @author kec
 */
public class KometStageController
        implements StatusMessageConsumer {

    private static final Logger LOG = LogManager.getLogger();

    public enum Keys {
        FACTORY_CLASS,
        TAB_PANE_INDEX,
        INDEX_IN_TAB_PANE,
    }

    private static final HashMap<ConceptSpecification, NodeFactory> NODE_FACTORY_MAP = new HashMap<>();

    {
        for (NodeFactory factory : Get.services(NodeFactory.class)) {
            NODE_FACTORY_MAP.put(factory.getPanelType(), factory);
        }
    }


    ArrayList<TabPane> tabPanes = new ArrayList<>();
    @FXML  // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML  // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML                                                                          // fx:id="bottomBorderBox"
    private HBox bottomBorderBox;                  // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="editorButtonBar"
    private ButtonBar editorButtonBar;                  // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="windowSplitPane"
    private SplitPane windowSplitPane;       // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="leftHBox"
    private HBox leftHBox;                    // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="centerHBox"
    private HBox centerHBox;                   // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="rightHBox"
    private HBox rightHBox;                   // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="editToolBar"
    private ToolBar editToolBar;                      // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="statusMessage"
    private Label statusMessage;                    // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="vanityBox"
    private Button vanityBox;                        // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="topGridPane"
    private GridPane topGridPane;                      // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="classifierMenuButton"
    private MenuButton classifierMenuButton;             // Value injected by FXMLLoader

    @FXML
    private Label pathLabel;

    @FXML
    private Menu windowCoordinates;

    @FXML
    private MenuButton viewPropertiesButton;


    private ChangeListener<Boolean> focusChangeListener = this::handleFocusEvents;

    private WindowPreferences windowPreferences;
    private IsaacPreferences preferencesNode;
    private Stage stage;
    private List<MenuButton> newTabMenuButtons = new ArrayList<>(5);
    private ViewProperties viewProperties;


    private final ImageView vanityImage = new ImageView();

    private TabPane leftTabPane = new TabPane();
    private TabPane centerTabPane = new TabPane();
    private TabPane rightTabPane = new TabPane();

    private TabPane getTabPaneFromIndex(int index) {
        switch (index) {
            case 0:
                return leftTabPane;
            case 1:
                return centerTabPane;
            case 2:
                return rightTabPane;
            default:
                throw new ArrayIndexOutOfBoundsException("Tab pane index is: " + index);
        }
    }

    /**
     * When the button action event is triggered, refresh the user CSS file.
     *
     * @param event the action event.
     */
    @FXML
    public void handleRefreshUserCss(ActionEvent event) {
        // "Feature" to make css editing/testing easy in the dev environment. 
        vanityBox.getScene()
                .getStylesheets()
                .clear();
        vanityBox.getScene()
                .getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        LOG.debug("Updated css: " + FxGet.fxConfiguration().getUserCSSURL().toString());
        vanityBox.getScene()
                .getStylesheets()
                .add(FxGet.fxConfiguration().getIconographyCSSURL().toString());
        LOG.debug("Updated Iconography css: " + FxGet.fxConfiguration().getIconographyCSSURL().toString());
    }

    @Override
    public void reportStatus(String status) {
        Platform.runLater(() -> {
            statusMessage.setText(status);
        });
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert bottomBorderBox != null :
                "fx:id=\"bottomBorderBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editorButtonBar != null :
                "fx:id=\"editorButtonBar\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert centerHBox != null :
                "fx:id=\"centerHBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editToolBar != null :
                "fx:id=\"editToolBar\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert leftHBox != null :
                "fx:id=\"leftHBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert rightHBox != null :
                "fx:id=\"rightHBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert statusMessage != null :
                "fx:id=\"statusMessage\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert vanityBox != null : "fx:id=\"vanityBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert topGridPane != null :
                "fx:id=\"topGridPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert classifierMenuButton != null :
                "fx:id=\"classifierMenuButton\" was not injected: check your FXML file 'KometStageScene.fxml'.";


        leftHBox.getChildren()
                .add(createWrappedTabPane(this.newTabMenuButtons, this.leftTabPane));
        centerHBox.getChildren()
                .add(createWrappedTabPane(this.newTabMenuButtons, this.centerTabPane));
        rightHBox.getChildren()
                .add(createWrappedTabPane(this.newTabMenuButtons, this.rightTabPane));
        classifierMenuButton.setGraphic(Iconography.ICON_CLASSIFIER1.getIconographic());
        classifierMenuButton.getItems().clear();
        classifierMenuButton.getItems().addAll(getTaskMenuItems());

        Image image = new Image(KometStageController.class.getResourceAsStream("/images/viewer-logo-b@2.png"));
        vanityImage.setImage(image);
        vanityImage.setFitHeight(36);
        vanityImage.setPreserveRatio(true);
        vanityImage.setSmooth(true);
        vanityImage.setCache(true);
        vanityBox.setGraphic(vanityImage);
        this.leftTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        this.centerTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        this.rightTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
    }

    private List<MenuItem> getTaskMenuItems() {
        ArrayList<MenuItem> items = new ArrayList<>();

        MenuItem selectiveImport = new MenuItemWithText("Selective import and transform");
        selectiveImport.setOnAction((ActionEvent event) -> {
            ImportView.show(this.viewProperties);
        });
        items.add(selectiveImport);

        MenuItem selectiveExport = new MenuItemWithText("Selective export");
        selectiveExport.setOnAction(event -> ExportView.show(this.viewProperties));
        items.add(selectiveExport);

        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
            MenuItem artifactImport = new MenuItemWithText("Artifact Import");
            artifactImport.setOnAction((ActionEvent event) -> {
                ArtifactImporter.startArtifactImport(topGridPane.getScene().getWindow());
            });
            items.add(artifactImport);
        }


//      MenuItem setLowMemConfigAndQuit = new MenuItem("Set to low memory configuration, erase database, and quit");
//      setLowMemConfigAndQuit.setOnAction((ActionEvent event) -> {
//         ChangeDatabaseMemoryConfigurationAndQuit task = 
//                 new ChangeDatabaseMemoryConfigurationAndQuit(MemoryConfiguration.ALL_CHRONICLES_MANAGED_BY_DB);
//         task.run();
//      });
//      items.add(setLowMemConfigAndQuit);
//
//      MenuItem setHighMemConfigAndQuit = new MenuItem("Set to high memory configuration, erase database, and quit");
//      setHighMemConfigAndQuit.setOnAction((ActionEvent event) -> {
//         ChangeDatabaseMemoryConfigurationAndQuit task = 
//                 new ChangeDatabaseMemoryConfigurationAndQuit(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);
//         task.run();
//      });
//      items.add(setHighMemConfigAndQuit);
//      
        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {

            MenuItem completeClassify = new MenuItemWithText("Complete classify");
            completeClassify.setOnAction((ActionEvent event) -> {
                //TODO change how we get the edit coordinate. 
                EditCoordinate editCoordinate = Get.coordinateFactory().createDefaultUserSolorOverlayEditCoordinate();
                ClassifierService classifierService = Get.logicService().getClassifierService(this.viewProperties.getManifoldCoordinate().toManifoldCoordinateImmutable());
                classifierService.classify();
            });
            items.add(completeClassify);

            MenuItem completeReindex = new MenuItemWithText("Complete reindex");
            completeReindex.setOnAction((ActionEvent event) -> {
                Get.startIndexTask();
            });
            items.add(completeReindex);

            MenuItem recomputeTaxonomy = new MenuItemWithText("Recompute taxonomy");
            recomputeTaxonomy.setOnAction((ActionEvent event) -> {
                Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            });
            items.add(recomputeTaxonomy);

            File beer = new File("../../integration/tests/src/test/resources/turtle/bevontology-0.8.ttl");
            if (beer.isFile()) {
                // This should only appear if you are running from eclipse / netbeans....
                MenuItem convertBeer = new MenuItemWithText("Beer me!");
                convertBeer.setOnAction((ActionEvent event) -> {
                    Get.executor().execute(() -> {
                        try {
                            Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE, false);
                            TurtleImportHK2Direct timd = Get.service(TurtleImportHK2Direct.class);
                            timd.configure(null, beer.toPath(), "0.8", null, transaction);
                            timd.convertContent(update -> {}, (work, totalWork) -> {});
                            Optional<CommitRecord> cr = transaction.commit("Beer has arrived!").get();  //TODO [DAN] this is broken, it isn't returning a commit record
                            LOG.error("commit record empty? {}", cr);
                            //if (cr.isPresent()) {
	                            Get.indexDescriptionService().refreshQueryEngine();
	                            Platform.runLater(() -> {
	                                Alert alert = new Alert(AlertType.INFORMATION);
	                                alert.setTitle("Beer has arrived!");
	                                alert.setHeaderText("Beer has been imported!");
	                                alert.initOwner(topGridPane.getScene().getWindow());
	                                alert.setResizable(true);
	                                alert.showAndWait();
	                            });
                            //}
                        } catch (Exception e) {
                            LOG.error("Beer failure", e);
                            Platform.runLater(() -> {
                                Alert alert = new Alert(AlertType.ERROR);
                                alert.setTitle("Party Foul!");
                                alert.setHeaderText("Something went wrong loading beer!");
                                alert.initOwner(topGridPane.getScene().getWindow());
                                alert.setResizable(true);
                                alert.showAndWait();
                            });
                        }
                    });
                });
                items.add(convertBeer);
            }
        }
        return items;
    }

    private void addTabFactory(NodeFactory<? extends ExplorationNode> factory, TabPane tabPane, ArrayList<MenuItem> menuItems) {
        MenuItem tabFactoryMenuItem = new MenuItemWithText(factory.getMenuText(), factory.getMenuIcon());
        tabFactoryMenuItem.setOnAction(
                (event) -> {
                    IsaacPreferences newPreferences = this.preferencesNode.node(UUID.randomUUID().toString());
                    addTabFromFactory(factory, newPreferences, tabPane);
                });
        menuItems.add(tabFactoryMenuItem);
    }

    private void addTabFromFactory(NodeFactory<? extends ExplorationNode> factory, IsaacPreferences tabPreferences, TabPane tabPane) {
        Tab tab = new Tab(factory.getMenuText());
        tab.setTooltip(new Tooltip(""));

        ExplorationNode node = factory.createNode(this.viewProperties,
                this.viewProperties.getActivityFeed(factory.getDefaultActivityFeed()[0]),
                tabPreferences);
        tab.setOnCloseRequest(event1 -> {
            if (!node.canClose()) {
                event1.consume();
            }
        });
        tab.setOnClosed(event1 -> {
            node.close();
        });
        ObjectProperty<Node> menuIconProperty = node.getMenuIconProperty();
        tab.setGraphic(node.getTitleNode().orElse(menuIconProperty.get()));
        menuIconProperty.addListener((observable, oldValue, newValue) -> tab.setGraphic(newValue));

        BorderPane borderPaneForTab = new BorderPane(node.getNode());
        tab.textProperty()
                .bind(node.getTitle());
        tab.getTooltip()
                .textProperty()
                .bind(node.getToolTip());
        tab.setContent(borderPaneForTab);
        tabPane.getTabs()
                .add(tab);
        tabPane.getSelectionModel().select(tab);
        node.setNodeSelectionMethod(() -> tabPane.getSelectionModel().select(tab));
        // TODO Modify skin to look for drag handling methods...
    }

    private void updateMenus(Observable observable) {
        this.windowCoordinates.getItems().clear();
        CoordinateMenuFactory.makeCoordinateDisplayMenu(this.viewProperties.getManifoldCoordinate(),
                this.windowCoordinates.getItems(), this.viewProperties.getManifoldCoordinate());
    }

    private String makePathLabelString() {
        return this.viewProperties.getManifoldCoordinate().getCurrentActivity().toUserString() +
                " on " + this.viewProperties.getManifoldCoordinate().getPathString();
    }

    /**
     * @param windowPreferences preferences of the window.
     */
    public void setWindowPreferenceItem(WindowPreferences windowPreferences, Stage stage) throws BackingStoreException {
        this.windowPreferences = windowPreferences;
        this.viewProperties = windowPreferences.getViewPropertiesForWindow();

        this.pathLabel.setText(makePathLabelString());

        this.preferencesNode = windowPreferences.getPreferenceNode();
        this.stage = stage;
        this.stage.getProperties().put(FxGet.PROPERTY_KEYS.WINDOW_PREFERENCES, windowPreferences);

        String windowName = windowPreferences.getWindowName().getValue();
        String dataStoreName = Get.dataStore().getDataStorePath().toString();
        this.stage.titleProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Title changed to: " + newValue);
        });
        this.stage.setTitle(windowName + ": " + Get.dataStore().getDataStorePath().toFile().getName());
        LOG.info("Setting title to: " + windowName + ": " + dataStoreName);

        this.stage.getScene().getProperties().put(WindowPreferencePanel.Keys.WINDOW_UUID_STR, windowPreferences.getPreferenceNode().name());
        this.stage.getScene().getProperties().put(WINDOW_PREFERENCE_ABSOLUTE_PATH, windowPreferences.getPreferenceNode().absolutePath());

        for (ActivityFeed activityFeed : viewProperties.getActivityFeeds()) {
            activityFeed.feedSelectionProperty().addListener(this::printSelectionDetails);

            activityFeed.feedSelectionProperty()
                    .addListener((ListChangeListener.Change<? extends IdentifiedObject> c) -> {
                        StringBuilder buff = new StringBuilder();
                        if (c.getList().size() > 3) {
                            buff.append("Selected list of size: " + c.getList().size());
                        } else {
                            for (int index = 0; index < c.getList().size(); index++) {
                                buff.append(Get.conceptDescriptionText(c.getList().get(index).getNid()));
                                if (index < c.getList().size() - 1) {
                                    buff.append("; ");
                                }
                            }
                        }
                        FxGet.statusMessageService()
                                .reportSceneStatus(statusMessage.getScene(),
                                        activityFeed.getFeedName() + " selected: " + buff.toString());
                    });
        }

        if (windowPreferences.getPersonaItem() != null) {
            PersonaItem personaItem = windowPreferences.getPersonaItem();
            for (int paneIndex = 0; paneIndex < newTabMenuButtons.size(); paneIndex++) {
                newTabMenuButtons.get(paneIndex).getItems().clear();
                ObservableList<TabSpecification> nodeListForPaneIndex = windowPreferences.getNodesList(paneIndex);
                //Ignore all of these silly hard-coded persona settings when we are debug mode, since they are completely mis-designed for modular plugin GUI components
                Set<ConceptSpecification> allowedOptions = FxGet.fxConfiguration().isShowBetaFeaturesEnabled() ? new HashSet<>() : personaItem.getAllowedOptionsForPane(paneIndex);
                if (allowedOptions.isEmpty()) {
                    allowedOptions.addAll(NODE_FACTORY_MAP.keySet());
                }
                ArrayList<MenuItem> menuItems = new ArrayList<>();
                for (ConceptSpecification allowedOption : allowedOptions) {
                    if (NODE_FACTORY_MAP.containsKey(allowedOption)) {
                        NodeFactory factory = NODE_FACTORY_MAP.get(allowedOption);
                        addTabFactory(factory, getTabPaneFromIndex(paneIndex), menuItems);
                    } else {
                        FxGet.dialogs().showErrorDialog("Creation from Persona Error",
                                "Can't create menu item for: " + Get.conceptDescriptionText(allowedOption),
                                "Available factories:  " + NODE_FACTORY_MAP.keySet());
                    }
                }
                menuItems.sort((o1, o2) -> NaturalOrder.compareStrings(o1.getText(), o2.getText()));
                newTabMenuButtons.get(paneIndex).getItems().addAll(menuItems);
                // add the actual pane items...
                for (TabSpecification paneItem : nodeListForPaneIndex) {
                    if (NODE_FACTORY_MAP.containsKey(paneItem.tabSpecification)) {
                        NodeFactory factory = NODE_FACTORY_MAP.get(paneItem.tabSpecification);
                        IsaacPreferences existingPreferences = this.preferencesNode.node(paneItem.preferencesNodeName.toString());
                        addTabFromFactory(factory, existingPreferences, getTabPaneFromIndex(paneIndex));
                    } else {
                        FxGet.dialogs().showErrorDialog("Restore pane error",
                                "Can't create item for: " + paneItem,
                                "Available factories:  \n" + NODE_FACTORY_MAP.keySet());
                    }
                }

                if (!windowPreferences.isPaneEnabled(paneIndex)) {
                    switch (paneIndex) {
                        case 0:
                            this.windowSplitPane.getItems().remove(this.leftHBox);
                            break;
                        case 1:
                            this.windowSplitPane.getItems().remove(this.centerHBox);
                            break;
                        case 2:
                            this.windowSplitPane.getItems().remove(this.rightHBox);
                            break;
                        default:
                            FxGet.dialogs().showErrorDialog("Pane index error",
                                    "Pane index is out of bounds.",
                                    "paneIndex: " + paneIndex);
                    }
                }
            }
        }
        IsaacPreferences[] children = this.preferencesNode.children();
        Arrays.sort(children, (o1, o2) -> {
            if (o1.getInt(Keys.TAB_PANE_INDEX, 0) != o2.getInt(Keys.TAB_PANE_INDEX, 0)) {
                return Integer.compare(o1.getInt(Keys.TAB_PANE_INDEX, 0), o2.getInt(Keys.TAB_PANE_INDEX, 0));
            }
            return Integer.compare(o1.getInt(Keys.INDEX_IN_TAB_PANE, 0), o2.getInt(Keys.INDEX_IN_TAB_PANE, 0));
        });
        for (IsaacPreferences childNode : children) {
            Optional<String> optionalFactoryClass = childNode.get(Keys.FACTORY_CLASS);
            if (optionalFactoryClass.isPresent()) {
                try {
                    String factoryClassName = optionalFactoryClass.get();
                    Class factoryClass = Class.forName(factoryClassName);
                    NodeFactory factory = (NodeFactory) factoryClass.getDeclaredConstructor().newInstance();
                    ExplorationNode en = factory.createNode(this.viewProperties,
                            this.viewProperties.getActivityFeed(factory.getDefaultActivityFeed()[0]), childNode);
                    Tab tab = new Tab();

                    tab.setGraphic(en.getMenuIconProperty().getValue());
                    en.getMenuIconProperty().addListener((observable, oldValue, newValue) -> tab.setGraphic(newValue));
                    tab.setContent(new BorderPane(en.getNode()));
                    tab.textProperty().bind(en.getTitle());
                    tab.setTooltip(new Tooltip(""));
                    tab.getTooltip().textProperty().bind(en.getToolTip());

                    int tabIndex = childNode.getInt(Keys.TAB_PANE_INDEX, 0);
                    int indexInTab = childNode.getInt(Keys.INDEX_IN_TAB_PANE, 0);
                    TabPane tabPane = tabPanes.get(tabIndex);
                    tabPane.getTabs().add(indexInTab, tab);
                    en.setNodeSelectionMethod(() -> tabPane.getSelectionModel().select(tab));

                } catch (Exception ex) {
                    FxGet.dialogs().showErrorDialog(ex.getLocalizedMessage(), ex);
                }
            }
        }


        this.stage.xProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferences.xLocationProperty().setValue(newValue);
            windowPreferences.save();
        });

        this.stage.yProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferences.yLocationProperty().setValue(newValue);
            windowPreferences.save();
        });

        this.stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferences.widthProperty().setValue(newValue);
            windowPreferences.save();
        });

        this.stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferences.heightProperty().setValue(newValue);
            windowPreferences.save();
        });

        this.leftTabPane.getSelectionModel().select(this.windowPreferences.leftTabSelectionProperty().get());
        this.centerTabPane.getSelectionModel().select(this.windowPreferences.centerTabSelectionProperty().get());
        this.rightTabPane.getSelectionModel().select(this.windowPreferences.rightTabSelectionProperty().get());
        this.windowPreferences.leftTabSelectionProperty().bind(this.leftTabPane.getSelectionModel().selectedIndexProperty());
        this.windowPreferences.centerTabSelectionProperty().bind(this.centerTabPane.getSelectionModel().selectedIndexProperty());
        this.windowPreferences.rightTabSelectionProperty().bind(this.rightTabPane.getSelectionModel().selectedIndexProperty());

        this.windowSplitPane.setDividerPositions(this.windowPreferences.dividerPositionsProperty().get());

        this.updateMenus(null);


        this.viewProperties.getManifoldCoordinate().addListener((observable, oldValue, newValue) -> {
            this.pathLabel.setText(makePathLabelString());
            this.updateMenus(observable);
        });
        FxGet.pathCoordinates().addListener(this::updateMenus);
        Platform.runLater(() -> {
            // The initial layout seems to adjust the divider positions. Doing a runLater seems to put the
            // dividers in the right location.
            // Once in the right location, we can then add listeners, so that the initial layout adjustment
            // does not overwrite the saved layout.
            setupDividerPositions();
            setupFocusOwner(this.windowPreferences.isFocusOwner());
            stage.setOnCloseRequest(this::handleCloseRequest);

        });
    }

    void handleCloseRequest(WindowEvent event) {
        stage.focusedProperty().removeListener(this.focusChangeListener);
        MenuProvider.handleCloseRequest(event);
    }

    void handleFocusEvents(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        KometStageController.this.windowPreferences.setFocusOwner(newValue);
        KometStageController.this.windowPreferences.save();
    }

    void setupFocusOwner(boolean focusOwner) {
        stage.focusedProperty().addListener(this.focusChangeListener);
        if (focusOwner) {
            this.stage.requestFocus();
        }

    }

    void setupDividerPositions() {
        this.windowSplitPane.setDividerPositions(this.windowPreferences.dividerPositionsProperty().get());
        for (SplitPane.Divider divider: this.windowSplitPane.getDividers()) {
            divider.positionProperty().addListener((observable, oldValue, newValue) -> {
                this.windowPreferences.dividerPositionsProperty().setValue(this.windowSplitPane.getDividerPositions());
                this.windowPreferences.save();
            });
        }
    }

    public void saveSettings() throws BackingStoreException {
        FxGet.sync();
        preferencesNode.sync();

    }

    @SuppressWarnings("unchecked")
    private TabWrapper createWrappedTabPane(List<MenuButton> newTabMenuButtons, TabPane tabPane) {
        tabPanes.add(tabPane);

        ArrayList<MenuItem> menuItems = new ArrayList<>();

        Get.services(NodeFactory.class)
                .forEach(
                        (factory) -> {
                            addTabFactory(factory, tabPane, menuItems);
                        });
        menuItems.sort((o1, o2) -> NaturalOrder.compareStrings(o1.getText(), o2.getText()));

        TabWrapper wrapped = TabWrapper.wrap(tabPane, menuItems.toArray(new MenuItem[menuItems.size()]));
        newTabMenuButtons.add(wrapped.getAddTabMenuButton());
        HBox.setHgrow(wrapped, Priority.ALWAYS);
        return wrapped;
    }

    private void printSelectionDetails(ListChangeListener.Change<? extends IdentifiedObject> c) {
        Get.executor().submit(() -> {
            if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                StringBuffer buff = new StringBuffer();
                if (c.getList().size() > 3) {
                    buff.append("selected list of size (processed in background): " +
                            c.getList().size() + "\n");
                } else {
                    buff.append("selected (processed in background):\n");
                    for (int i = 0; i < c.getList().size(); i++) {
                        Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(c.getList().get(i).getNid());
                        optionalChronology.ifPresent(chronology -> {
                            buff.append(chronology.toString());
                            buff.append("\n");
                        });
                    }
                }

                if (Get.configurationService().isVerboseDebugEnabled()) {
                    LOG.info(() -> buff.toString());
                } else {
                    LOG.trace(() -> buff.toString());
                }
            }
        });
    }

}
