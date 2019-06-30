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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.prefs.BackingStoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.convert.mojo.turtle.TurtleImportMojoDirect;
import sh.isaac.komet.gui.treeview.TreeViewExplorationNodeFactory;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.LoincDirectImporter;
import sh.isaac.solor.direct.LoincExpressionToConcept;
import sh.isaac.solor.direct.LoincExpressionToNavConcepts;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;
import sh.komet.gui.contract.NodeFactory;
import sh.komet.gui.contract.NodeFactory.PanelPlacement;
import sh.komet.gui.contract.StatusMessageConsumer;
import sh.isaac.komet.gui.exporter.ExportView;
import sh.komet.gui.contract.preferences.WindowPreferenceItems;
import sh.komet.gui.importation.ArtifactImporter;
import sh.komet.gui.importation.ImportView;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.provider.concept.detail.panel.ConceptDetailPanelProviderFactory;
import sh.komet.gui.search.extended.ExtendedSearchViewFactory;
import sh.komet.gui.search.simple.SimpleSearchViewFactory;
import sh.komet.gui.tab.TabWrapper;
import sh.komet.gui.util.FxGet;
import sh.komet.progress.view.TaskProgressNodeFactory;

import static sh.komet.gui.provider.concept.detail.panel.ConceptDetailPanelNode.Keys.MANIFOLD_GROUP_NAME;

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
        INDEX_IN_TAB_PANE
    }


    //~--- fields --------------------------------------------------------------
    ArrayList<TabPane> tabPanes = new ArrayList<>();
    @FXML  // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML  // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML                                                                          // fx:id="bottomBorderBox"
    private HBox bottomBorderBox;                  // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="verticalEditorSplitPane"
    private SplitPane verticalEditorSplitPane;          // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="editorButtonBar"
    private ButtonBar editorButtonBar;                  // Value injected by FXMLLoader
    @FXML  // fx:id="editorCenterHorizontalSplitPane"
    private SplitPane editorCenterHorizontalSplitPane;  // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="editorLeftPane"
    private HBox editorLeftPane;                   // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="leftBorderBox"
    private HBox leftBorderBox;                    // Value injected by FXMLLoader
    @FXML                                                                          // fx:id="rightBorderBox"
    private HBox rightBorderBox;                   // Value injected by FXMLLoader
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
    private WindowPreferenceItems windowPreferenceItems;
    private IsaacPreferences preferencesNode;


    private final ImageView vanityImage = new ImageView();

    private TabPane leftTabPane   = new TabPane();
    private TabPane centerTabPane = new TabPane();
    private TabPane rightTabPane  = new TabPane();

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

    @FXML  // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert bottomBorderBox != null :
                "fx:id=\"bottomBorderBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert verticalEditorSplitPane != null :
                "fx:id=\"verticalEditorSplitPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editorButtonBar != null :
                "fx:id=\"editorButtonBar\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editorCenterHorizontalSplitPane != null :
                "fx:id=\"editorCenterHorizontalSplitPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editorLeftPane != null :
                "fx:id=\"editorLeftPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert editToolBar != null :
                "fx:id=\"editToolBar\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert leftBorderBox != null :
                "fx:id=\"leftBorderBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert rightBorderBox != null :
                "fx:id=\"rightBorderBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert statusMessage != null :
                "fx:id=\"statusMessage\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert vanityBox != null : "fx:id=\"vanityBox\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert topGridPane != null :
                "fx:id=\"topGridPane\" was not injected: check your FXML file 'KometStageScene.fxml'.";
        assert classifierMenuButton != null :
                "fx:id=\"classifierMenuButton\" was not injected: check your FXML file 'KometStageScene.fxml'.";

        for (ManifoldGroup mg : ManifoldGroup.values()) {
            Manifold manifold = FxGet.getManifold(mg);
            manifold.focusedConceptProperty()
                    .addListener(this::printSelectionDetails);
            manifold.focusedConceptProperty()
                    .addListener((ObservableValue<? extends IdentifiedObject> observable,
                                  IdentifiedObject oldValue,
                                  IdentifiedObject newValue) -> {
                        FxGet.statusMessageService()
                                .reportSceneStatus(statusMessage.getScene(),
                                        mg.getGroupName() + " selected: " + (newValue == null ? "" : newValue.toUserString()));
                    });
        }

        leftBorderBox.getChildren()
                .add(createWrappedTabPane(PanelPlacement.LEFT, this.leftTabPane));
        editorLeftPane.getChildren()
                .add(createWrappedTabPane(PanelPlacement.CENTER, this.centerTabPane));
        rightBorderBox.getChildren()
                .add(createWrappedTabPane(PanelPlacement.RIGHT, this.rightTabPane));
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

            MenuItem selectiveImport = new MenuItem("Selective import and transform");
            selectiveImport.setOnAction((ActionEvent event) -> {
                ImportView.show(FxGet.getManifold(ManifoldGroup.TAXONOMY));
            });
            items.add(selectiveImport);

            MenuItem selectiveExport = new MenuItem("Selective export");
            selectiveExport.setOnAction(event -> ExportView.show(FxGet.getManifold(ManifoldGroup.UNLINKED)));
            items.add(selectiveExport);
            
        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {

            MenuItem importTransformFull = new MenuItem("Import and transform - FULL");

            importTransformFull.setOnAction((ActionEvent event) -> {
                ImportAndTransformTask itcTask = new ImportAndTransformTask(FxGet.getManifold(ManifoldGroup.TAXONOMY),
                        ImportType.FULL);
                Get.executor().submit(itcTask);

            });

            items.add(importTransformFull);
            
            MenuItem artifactImport = new MenuItem("Artifact Import");
            artifactImport.setOnAction((ActionEvent event) -> {
                ArtifactImporter.startArtifactImport(topGridPane.getScene().getWindow());
            });
            items.add(artifactImport);
        }

        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {

            MenuItem importSourcesFull = new MenuItem("Import terminology content - FULL");
            importSourcesFull.setOnAction((ActionEvent event) -> {
                DirectImporter importerFull = new DirectImporter(ImportType.FULL);
                Get.executor().submit(importerFull);
            });
            items.add(importSourcesFull);
            MenuItem importSourcesSnapshot = new MenuItem("Import terminology content - ACTIVE");
            importSourcesSnapshot.setOnAction((ActionEvent event) -> {
                DirectImporter importerSnapshot = new DirectImporter(ImportType.ACTIVE_ONLY);
                Get.executor().submit(importerSnapshot);
            });
            items.add(importSourcesSnapshot);
            MenuItem transformSourcesFull = new MenuItem("Transform RF2 to EL++ - FULL");
            transformSourcesFull.setOnAction((ActionEvent event) -> {
                Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(ImportType.FULL);
                Get.executor().submit(transformer);
            });
            items.add(transformSourcesFull);
            MenuItem transformSourcesActiveOnly = new MenuItem("Transform RF2 to EL++ - ACTIVE");
            transformSourcesActiveOnly.setOnAction((ActionEvent event) -> {
                Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(ImportType.ACTIVE_ONLY);
                Get.executor().submit(transformer);
            });
            items.add(transformSourcesActiveOnly);
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

            MenuItem completeClassify = new MenuItem("Complete classify");
            completeClassify.setOnAction((ActionEvent event) -> {
                //TODO change how we get the edit coordinate. 
                EditCoordinate editCoordinate = Get.coordinateFactory().createDefaultUserSolorOverlayEditCoordinate();
                ClassifierService classifierService = Get.logicService().getClassifierService(FxGet.getManifold(ManifoldGroup.SEARCH), editCoordinate);
                classifierService.classify();
            });
            items.add(completeClassify);

            MenuItem completeReindex = new MenuItem("Complete reindex");
            completeReindex.setOnAction((ActionEvent event) -> {
                Get.startIndexTask();
            });
            items.add(completeReindex);

            MenuItem recomputeTaxonomy = new MenuItem("Recompute taxonomy");
            recomputeTaxonomy.setOnAction((ActionEvent event) -> {
                Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            });
            items.add(recomputeTaxonomy);

            File beer = new File("../../integration/tests/src/test/resources/turtle/bevontology-0.8.ttl");
            if (beer.isFile()) {
                // This should only appear if you are running from eclipse / netbeans....
                MenuItem convertBeer = new MenuItem("Beer me!");
                convertBeer.setOnAction((ActionEvent event) -> {
                    Get.executor().execute(() -> {
                        try {
                            TurtleImportMojoDirect timd = new TurtleImportMojoDirect();
                            timd.configure(null, beer.toPath(),"0.8", null);
                            Transaction transaction = Get.commitService().newTransaction(ChangeCheckerMode.ACTIVE);
                            timd.convertContent(transaction, update -> {}, (work, totalWork) -> {});
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
                        }
                        catch (Exception e) {
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
        MenuItem tabFactoryMenuItem = new MenuItem(factory.getMenuText(), factory.getMenuIcon());
        tabFactoryMenuItem.setOnAction(
                (event) -> {
                    Tab tab = new Tab(factory.getMenuText());
                    tab.setTooltip(new Tooltip(""));

                    IsaacPreferences newPreferences = this.preferencesNode.node(UUID.randomUUID().toString());
                    ExplorationNode node = factory.createNode(FxGet.getManifold(factory.getDefaultManifoldGroups()[0]), newPreferences);
                    tab.setOnCloseRequest(event1 -> {
                        if (!node.canClose()) {
                            event1.consume();
                        }
                    });
                    tab.setOnClosed(event1 -> {
                        node.close();
                    });
                    Node menuIcon = node.getMenuIcon();
                    menuIcon.parentProperty().addListener((observable, oldValue, newValue) -> {
                        System.out.println("Parent changed: " + newValue);
                    });
                    tab.setGraphic(menuIcon);
                    tab.graphicProperty().addListener((observable, oldValue, newValue) -> {
                        System.out.println("Graphic changed: " + newValue);
                    });

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
                        node.getManifold().focusedConceptProperty().addListener((observable, oldValue, newValue) -> {
                            if (((DetailNode) node).selectInTabOnChange()) {
                                tabPane.getSelectionModel().select(tab);
                            }
                        });
                    }
                    // Modify skin to look for drag handling methods... 
                });
        menuItems.add(tabFactoryMenuItem);

    }
    /**
     *
     * @param preferencesNode preferences of the window.
     */
    public void setPreferencesNode(IsaacPreferences preferencesNode) throws BackingStoreException {

        this.preferencesNode = preferencesNode;
        if (this.preferencesNode.children().length == 0) {
            // New window, add default configuration
            IsaacPreferences treeViewPreferences = preferencesNode.node(UUID.randomUUID().toString());
            treeViewPreferences.put(Keys.FACTORY_CLASS, TreeViewExplorationNodeFactory.class.getName());
            treeViewPreferences.putInt(Keys.TAB_PANE_INDEX, 0);
            treeViewPreferences.putInt(Keys.INDEX_IN_TAB_PANE, 0);

            IsaacPreferences conceptViewOnePreferences = preferencesNode.node(UUID.randomUUID().toString());
            conceptViewOnePreferences.put(Keys.FACTORY_CLASS, ConceptDetailPanelProviderFactory.class.getName());
            conceptViewOnePreferences.putInt(Keys.TAB_PANE_INDEX, 1);
            conceptViewOnePreferences.putInt(Keys.INDEX_IN_TAB_PANE, 0);
            conceptViewOnePreferences.put(MANIFOLD_GROUP_NAME, ManifoldGroup.TAXONOMY.getGroupName());

            IsaacPreferences conceptViewTwoPreferences = preferencesNode.node(UUID.randomUUID().toString());
            conceptViewTwoPreferences.put(Keys.FACTORY_CLASS, ConceptDetailPanelProviderFactory.class.getName());
            conceptViewTwoPreferences.putInt(Keys.TAB_PANE_INDEX, 1);
            conceptViewTwoPreferences.putInt(Keys.INDEX_IN_TAB_PANE, 1);
            conceptViewTwoPreferences.put(MANIFOLD_GROUP_NAME, ManifoldGroup.SEARCH.getGroupName());

            IsaacPreferences progressViewPreferences = preferencesNode.node(UUID.randomUUID().toString());
            progressViewPreferences.put(Keys.FACTORY_CLASS, TaskProgressNodeFactory.class.getName());
            progressViewPreferences.putInt(Keys.TAB_PANE_INDEX, 2);
            progressViewPreferences.putInt(Keys.INDEX_IN_TAB_PANE, 0);

            IsaacPreferences simpleSearchViewPreferences = preferencesNode.node(UUID.randomUUID().toString());
            simpleSearchViewPreferences.put(Keys.FACTORY_CLASS, SimpleSearchViewFactory.class.getName());
            simpleSearchViewPreferences.putInt(Keys.TAB_PANE_INDEX, 2);
            simpleSearchViewPreferences.putInt(Keys.INDEX_IN_TAB_PANE, 1);

            IsaacPreferences extendedSearchViewPreferences = preferencesNode.node(UUID.randomUUID().toString());
            extendedSearchViewPreferences.put(Keys.FACTORY_CLASS, ExtendedSearchViewFactory.class.getName());
            extendedSearchViewPreferences.putInt(Keys.TAB_PANE_INDEX, 2);
            extendedSearchViewPreferences.putInt(Keys.INDEX_IN_TAB_PANE, 2);

        }
        IsaacPreferences[] children = this.preferencesNode.children();
        Arrays.sort(children, (o1, o2) -> {
            if (o1.getInt(Keys.TAB_PANE_INDEX, 0) != o2.getInt(Keys.TAB_PANE_INDEX, 0)) {
                return Integer.compare(o1.getInt(Keys.TAB_PANE_INDEX, 0), o2.getInt(Keys.TAB_PANE_INDEX, 0));
            }
            return Integer.compare(o1.getInt(Keys.INDEX_IN_TAB_PANE, 0), o2.getInt(Keys.INDEX_IN_TAB_PANE, 0));
        });
        for (IsaacPreferences childNode: children) {
            Optional<String> optionalFactoryClass = childNode.get(Keys.FACTORY_CLASS);
            if (optionalFactoryClass.isPresent()) {
                try {
                    String factoryClassName = optionalFactoryClass.get();
                    Class factoryClass = Class.forName(factoryClassName);
                    NodeFactory factory = (NodeFactory) factoryClass.getDeclaredConstructor().newInstance();
                    ExplorationNode en = factory.createNode(FxGet.getManifold(factory.getDefaultManifoldGroups()[0]), childNode);
                    Tab tab = new Tab();
                    tab.setGraphic(en.getMenuIcon());
                    tab.setContent(new BorderPane(en.getNode()));
                    tab.textProperty().bind(en.getTitle());
                    tab.setTooltip(new Tooltip(""));
                    tab.getTooltip().textProperty().bind(en.getToolTip());

                    if (en instanceof DetailNode) {
                        DetailNode dt = (DetailNode) en;
                        //TODO this is broken by design, if more than one tab requests focus on change...
                        dt.getManifold().focusedConceptProperty().addListener((observable, oldValue, newValue) -> {
                            if (dt.selectInTabOnChange()) {
                                leftTabPane.getSelectionModel().select(tab);
                            }
                        });
                    }

                    int tabIndex = childNode.getInt(Keys.TAB_PANE_INDEX, 0);
                    int indexInTab = childNode.getInt(Keys.INDEX_IN_TAB_PANE, 0);

                    tabPanes.get(tabIndex).getTabs().add(indexInTab, tab);

                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }
    public void saveSettings() throws BackingStoreException {
        preferencesNode.sync();
    }

//
//    private void addTabFromSpecification(WindowPreferenceItems windowPreferenceItems, TabPane tabPane, List<ConceptSpecification> tabSpecs) {
//        for (ConceptSpecification tabSpec: tabSpecs) {
//            //Todo create a KOMET coordinate for the gui...
//            Optional<NodeFactory<ExplorationNode>> optionalFactory = FxGet.nodeFactory(tabSpec);
//            if (optionalFactory.isPresent()) {
//                long start = System.currentTimeMillis();
//                NodeFactory<ExplorationNode> factory = optionalFactory.get();
//
//                ExplorationNode en = factory.createNode(FxGet.getManifold(factory.getDefaultManifoldGroups()[0]).deepClone());
//                Tab tab = new Tab();
//                tab.setGraphic(en.getMenuIcon());
//                tab.setContent(new BorderPane(en.getNode()));
//                tab.textProperty().bind(en.getTitle());
//                tab.setTooltip(new Tooltip(""));
//                tab.getTooltip().textProperty().bind(en.getToolTip());
//
//                if (en instanceof DetailNode) {
//                    DetailNode dt = (DetailNode) en;
//                    if (!(en instanceof ConceptBuilderNode)) {
//                        //TabDragAndDropHandler.setupTab(tab, dt);
//                    }
//
//                    //TODO this is broken by design, if more than one tab requests focus on change...
//                    dt.getManifold().focusedConceptProperty().addListener((observable, oldValue, newValue) -> {
//                        if (dt.selectInTabOnChange()) {
//                            tabPane.getSelectionModel().select(tab);
//                        }
//                    });
//                }
//
//                tabPane.getTabs().add(tab);
//
//                if ((System.currentTimeMillis() - start) > 250) {
//                    LOG.warn("NodeFactory {} is slow!  Needs to background thread its work.  Took {}ms",
//                            factory.getClass().getName(), System.currentTimeMillis() - start);
//                }
//            }
//        }
//    }

    @SuppressWarnings("unchecked")
    private Pane createWrappedTabPane(PanelPlacement panelPlacement, TabPane tabPane) {
        tabPanes.add(tabPane);

        ArrayList<MenuItem> menuItems = new ArrayList<>();

        Get.services(NodeFactory.class)
                .forEach(
                        (factory) -> {
                            if (factory.isEnabled()) {
                                addTabFactory(factory, tabPane, menuItems);
                            }
                        });
        menuItems.sort(
                (o1, o2) -> {
                    return o1.getText()
                            .compareTo(o2.getText());
                });

        Pane wrapped = TabWrapper.wrap(tabPane, menuItems.toArray(new MenuItem[menuItems.size()]));

        HBox.setHgrow(wrapped, Priority.ALWAYS);
        return wrapped;
    }

    private void printSelectionDetails(ObservableValue<? extends IdentifiedObject> observable,
            IdentifiedObject oldValue,
            IdentifiedObject newValue) {
        Get.executor().submit(() -> {
            if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                StringBuffer buff = new StringBuffer();
                buff.append("selected (processed in background):\n");
                if (newValue instanceof ConceptChronology) {
                    ConceptChronology concept = (ConceptChronology) newValue;
                    buff.append(concept.toString());
                }
                if (Get.configurationService().isVerboseDebugEnabled()) {
                   System.out.println(buff.toString());
                }
                else {
                   LOG.debug(buff.toString());
                }
            }
        });
    }

}
