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

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.convert.mojo.turtle.TurtleImportHK2Direct;
import sh.isaac.komet.gui.exporter.ExportView;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.preferences.window.WindowPreferencePanel;
import sh.komet.gui.contract.NodeFactory;
import sh.komet.gui.contract.StatusMessageConsumer;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.contract.preferences.TabSpecification;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.importation.ArtifactImporter;
import sh.komet.gui.importation.ImportView;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.tab.TabWrapper;
import sh.komet.gui.util.FxGet;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.MenuProvider.Keys.WINDOW_PREFERENCE_ABSOLUTE_PATH;

//~--- classes ----------------------------------------------------------------

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


    //~--- fields --------------------------------------------------------------
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
    private WindowPreferencesItem windowPreferencesItem;
    private IsaacPreferences preferencesNode;
    private Stage stage;
    private List<MenuButton> newTabMenuButtons = new ArrayList<>(5);


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

    //~--- methods -------------------------------------------------------------

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
                .remove(FxGet.fxConfiguration().getUserCSSURL().toString());
        vanityBox.getScene()
                .getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        System.out.println("Updated css: " + FxGet.fxConfiguration().getUserCSSURL().toString());
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

        for (ManifoldGroup mg : ManifoldGroup.values()) {
            Manifold manifold = FxGet.manifold(mg);
            manifold.manifoldSelectionProperty().addListener(this::printSelectionDetails);

            manifold.manifoldSelectionProperty()
                    .addListener((ListChangeListener.Change<? extends ComponentProxy> c) -> {
                        StringBuilder buff = new StringBuilder();
                        for (int index = 0; index < c.getList().size(); index++) {
                            buff.append(Get.conceptDescriptionText(c.getList().get(index).getNid()));
                            if (index < c.getList().size() - 1) {
                                buff.append("; ");
                            }
                        }
                        FxGet.statusMessageService()
                                .reportSceneStatus(statusMessage.getScene(),
                                        mg.getGroupName() + " selected: " + buff.toString());
                    });
        }

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
            ImportView.show(FxGet.manifold(ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ANY_NODE));
        });
        items.add(selectiveImport);

        MenuItem selectiveExport = new MenuItemWithText("Selective export");
        selectiveExport.setOnAction(event -> ExportView.show(FxGet.manifold(ManifoldGroup.UNLINKED)));
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
                ClassifierService classifierService = Get.logicService().getClassifierService(FxGet.manifold(ManifoldGroup.SEARCH), editCoordinate);
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
                            Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE);
                            TurtleImportHK2Direct timd = new TurtleImportHK2Direct(transaction);
                            timd.configure(null, beer.toPath(), "0.8", null);
                            timd.convertContent(transaction, update -> {
                            }, (work, totalWork) -> {
                            });
                            transaction.commit("Beer has arrived!");
                            Get.indexDescriptionService().refreshQueryEngine();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Beer has arrived!");
                                alert.setHeaderText("Beer has been imported!");
                                alert.initOwner(topGridPane.getScene().getWindow());
                                alert.setResizable(true);
                                alert.showAndWait();
                            });
                        } catch (Exception e) {
                            LOG.error("Beer failure", e);
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

        ExplorationNode node = factory.createNode(FxGet.manifold(factory.getDefaultManifoldGroups()[0]), tabPreferences);
        tab.setOnCloseRequest(event1 -> {
            if (!node.canClose()) {
                event1.consume();
            }
        });
        tab.setOnClosed(event1 -> {
            node.close();
        });
        ObjectProperty<Node> menuIconProperty = node.getMenuIconProperty();
        tab.setGraphic(menuIconProperty.get());
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
        if (node instanceof DetailNode) {
            node.getManifold().manifoldSelectionProperty().addListener((observable, oldValue, newValue) -> {
                if (((DetailNode) node).selectInTabOnChange()) {
                    tabPane.getSelectionModel().select(tab);
                }
            });
        }
        // TODO Modify skin to look for drag handling methods...
    }

    /**
     * @param windowPreferencesItem preferences of the window.
     */
    public void setWindowPreferenceItem(WindowPreferencesItem windowPreferencesItem, Stage stage) throws BackingStoreException {
        this.windowPreferencesItem = windowPreferencesItem;
        this.preferencesNode = windowPreferencesItem.getPreferenceNode();
        this.stage = stage;
        this.stage.getScene().getProperties().put(WindowPreferencePanel.Keys.WINDOW_UUID_STR, windowPreferencesItem.getPreferenceNode().name());
        this.stage.getScene().getProperties().put(WINDOW_PREFERENCE_ABSOLUTE_PATH, windowPreferencesItem.getPreferenceNode().absolutePath());
        if (windowPreferencesItem.getPersonaItem() != null) {
            PersonaItem personaItem = windowPreferencesItem.getPersonaItem();
            for (int paneIndex = 0; paneIndex < newTabMenuButtons.size(); paneIndex++) {
                newTabMenuButtons.get(paneIndex).getItems().clear();
                ObservableList<TabSpecification> nodeListForPaneIndex = windowPreferencesItem.getNodesList(paneIndex);
                Set<ConceptSpecification> allowedOptions = personaItem.getAllowedOptionsForPane(paneIndex);
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

                if (!windowPreferencesItem.isPaneEnabled(paneIndex)) {
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
                    ExplorationNode en = factory.createNode(FxGet.manifold(factory.getDefaultManifoldGroups()[0]), childNode);
                    Tab tab = new Tab();

                    tab.setGraphic(en.getMenuIconProperty().getValue());
                    en.getMenuIconProperty().addListener((observable, oldValue, newValue) -> tab.setGraphic(newValue));
                    tab.setContent(new BorderPane(en.getNode()));
                    tab.textProperty().bind(en.getTitle());
                    tab.setTooltip(new Tooltip(""));
                    tab.getTooltip().textProperty().bind(en.getToolTip());

                    if (en instanceof DetailNode) {
                        DetailNode dt = (DetailNode) en;
                        //TODO this is broken by design, if more than one tab requests focus on change...
                        dt.getManifold().manifoldSelectionProperty().addListener((observable, oldValue, newValue) -> {
                            if (dt.selectInTabOnChange()) {
                                leftTabPane.getSelectionModel().select(tab);
                            }
                        });
                    }

                    int tabIndex = childNode.getInt(Keys.TAB_PANE_INDEX, 0);
                    int indexInTab = childNode.getInt(Keys.INDEX_IN_TAB_PANE, 0);

                    tabPanes.get(tabIndex).getTabs().add(indexInTab, tab);

                } catch (Exception ex) {
                    FxGet.dialogs().showErrorDialog(ex.getLocalizedMessage(), ex);
                }
            }
        }


        this.stage.xProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferencesItem.xLocationProperty().setValue(newValue);
            windowPreferencesItem.save();
        });

        this.stage.yProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferencesItem.yLocationProperty().setValue(newValue);
            windowPreferencesItem.save();
        });

        this.stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferencesItem.widthProperty().setValue(newValue);
            windowPreferencesItem.save();
        });

        this.stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferencesItem.heightProperty().setValue(newValue);
            windowPreferencesItem.save();
        });

        this.leftTabPane.getSelectionModel().select(this.windowPreferencesItem.leftTabSelectionProperty().get());
        this.centerTabPane.getSelectionModel().select(this.windowPreferencesItem.centerTabSelectionProperty().get());
        this.rightTabPane.getSelectionModel().select(this.windowPreferencesItem.rightTabSelectionProperty().get());
        this.windowPreferencesItem.leftTabSelectionProperty().bind(this.leftTabPane.getSelectionModel().selectedIndexProperty());
        this.windowPreferencesItem.centerTabSelectionProperty().bind(this.centerTabPane.getSelectionModel().selectedIndexProperty());
        this.windowPreferencesItem.rightTabSelectionProperty().bind(this.rightTabPane.getSelectionModel().selectedIndexProperty());

        this.windowSplitPane.setDividerPositions(this.windowPreferencesItem.dividerPositionsProperty().get());
        Platform.runLater(() -> {
            // The initial layout seems to adjust the divider positions. Doing a runLater seems to put the
            // dividers in the right location.
            // Once in the right location, we can then add listeners, so that the initial layout adjustment
            // does not overwrite the saved layout.
            setupDividerPositions();
            setupFocusOwner(this.windowPreferencesItem.isFocusOwner());
        });
    }

    void setupFocusOwner(boolean focusOwner) {
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            this.windowPreferencesItem.setFocusOwner(newValue);
            this.windowPreferencesItem.save();
        });
        if (focusOwner) {
            this.stage.requestFocus();
        }

    }

    void setupDividerPositions() {
        this.windowSplitPane.setDividerPositions(this.windowPreferencesItem.dividerPositionsProperty().get());
        for (SplitPane.Divider divider: this.windowSplitPane.getDividers()) {
            divider.positionProperty().addListener((observable, oldValue, newValue) -> {
                this.windowPreferencesItem.dividerPositionsProperty().setValue(this.windowSplitPane.getDividerPositions());
                this.windowPreferencesItem.save();
            });
        }
    }

    public void saveSettings() throws BackingStoreException {
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

    private void printSelectionDetails(ListChangeListener.Change<? extends ComponentProxy> c) {
        Get.executor().submit(() -> {
            if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                StringBuffer buff = new StringBuffer();
                buff.append("selected (processed in background):\n");
                for (int i = 0; i < c.getList().size(); i++) {
                    ConceptChronology concept = Get.concept(c.getList().get(i).getNid());
                    buff.append(concept.toString());
                    buff.append("\n");
                }

                if (Get.configurationService().isVerboseDebugEnabled()) {
                    System.out.println(buff.toString());
                } else {
                    LOG.debug(buff.toString());
                }
            }
        });
    }

}
