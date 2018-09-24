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
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Node;
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
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.convert.mojo.turtle.TurtleImportMojo;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.LoincDirectImporter;
import sh.isaac.solor.direct.LoincExpressionToConcept;
import sh.isaac.solor.direct.LoincExpressionToNavConcepts;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;
import sh.komet.fx.tabpane.DndTabPaneFactory;
import sh.komet.fx.tabpane.DndTabPaneFactory.FeedbackType;
import sh.komet.gui.contract.NodeFactory;
import sh.komet.gui.contract.NodeFactory.PanelPlacement;
import sh.komet.gui.contract.StatusMessageConsumer;
import sh.isaac.komet.gui.exporter.ExportView;
import sh.komet.gui.importation.ImportView;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.tab.TabWrapper;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.drag.drop.TabDragAndDropHandler;
import sh.komet.gui.provider.concept.builder.ConceptBuilderNode;

//~--- classes ----------------------------------------------------------------
/**
 * Root node of scene is given a UUID for unique identification.
 *
 * @author kec
 */
public class KometStageController
        implements StatusMessageConsumer {

    private final Logger LOG = LogManager.getLogger();
    private final HashMap<ManifoldGroup, Manifold> manifolds = new HashMap<>(); 

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

    private final ImageView vanityImage = new ImageView();

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
            manifolds.put(mg, Manifold.make(mg));
        }

        for (Entry<ManifoldGroup, Manifold> manifoldInfo : manifolds.entrySet()) {
            manifoldInfo.getValue().focusedConceptProperty()
                    .addListener(this::printSelectionDetails);
            manifoldInfo.getValue().focusedConceptProperty()
                    .addListener((ObservableValue<? extends IdentifiedObject> observable,
                            IdentifiedObject oldValue,
                            IdentifiedObject newValue) -> {
                        FxGet.statusMessageService()
                                .reportSceneStatus(statusMessage.getScene(),
                                        manifoldInfo.getKey().getGroupName() + " selected: " + (newValue == null ? "" : newValue.toUserString()));
                    });
        }

        leftBorderBox.getChildren()
                .add(createWrappedTabPane(PanelPlacement.LEFT));
        editorLeftPane.getChildren()
                .add(createWrappedTabPane(PanelPlacement.CENTER));
        rightBorderBox.getChildren()
                .add(createWrappedTabPane(PanelPlacement.RIGHT));
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
    }

    private List<MenuItem> getTaskMenuItems() {
        ArrayList<MenuItem> items = new ArrayList<>();

            MenuItem selectiveImport = new MenuItem("Selective import and transform");
            selectiveImport.setOnAction((ActionEvent event) -> {
                ImportView.show(manifolds.get(ManifoldGroup.TAXONOMY));
            });
            items.add(selectiveImport);

            MenuItem selectiveExport = new MenuItem("Selective export");
            selectiveExport.setOnAction(event -> ExportView.show(manifolds.get(ManifoldGroup.UNLINKED)));
            items.add(selectiveExport);
            
        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {

            MenuItem importTransformFull = new MenuItem("Import and transform - FULL");

            importTransformFull.setOnAction((ActionEvent event) -> {
                ImportAndTransformTask itcTask = new ImportAndTransformTask(manifolds.get(ManifoldGroup.TAXONOMY),
                        ImportType.FULL);
                Get.executor().submit(itcTask);

            });

            items.add(importTransformFull);
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
                ClassifierService classifierService = Get.logicService().getClassifierService(manifolds.get(ManifoldGroup.SEARCH), editCoordinate);
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

            MenuItem importLoincRecords = new MenuItem("Import LOINC records");
            importLoincRecords.setOnAction((ActionEvent event) -> {
                LoincDirectImporter importTask = new LoincDirectImporter();
                Get.executor().execute(importTask);
            });
            items.add(importLoincRecords);

            MenuItem addLabNavigationConcepts = new MenuItem("Add lab navigation concepts");
            addLabNavigationConcepts.setOnAction((ActionEvent event) -> {
                LoincExpressionToNavConcepts conversionTask = new LoincExpressionToNavConcepts(manifolds.get(ManifoldGroup.UNLINKED));
                Get.executor().execute(conversionTask);
            });
            items.add(addLabNavigationConcepts);
            
            MenuItem convertLoincExpressions = new MenuItem("Convert LOINC expressions");
            convertLoincExpressions.setOnAction((ActionEvent event) -> {
                LoincExpressionToConcept conversionTask = new LoincExpressionToConcept();
                Get.executor().execute(conversionTask);
            });
            items.add(convertLoincExpressions);
            
            File beer = new File("../../integration/tests/src/test/resources/turtle/bevontology-0.8.ttl");
            if (beer.isFile()) {
                // This should only appear if you are running from eclipse / netbeans....
                MenuItem convertBeer = new MenuItem("Beer me!");
                convertBeer.setOnAction((ActionEvent event) -> {
                    Get.executor().execute(() -> {
                        try {
                            new TurtleImportMojo(null, new FileInputStream(beer), "0.8").processTurtle();
                            Get.indexDescriptionService().refreshQueryEngine();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Beer has arrived!");
                                alert.setHeaderText("Beer has been imported!");
                                alert.initOwner(topGridPane.getScene().getWindow());
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

                    ExplorationNode node = factory.createNode(manifolds.get(factory.getDefaultManifoldGroups()[0]));
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

    @SuppressWarnings("unchecked")
    private Pane createWrappedTabPane(PanelPlacement panelPlacement) {
        Pane pane = DndTabPaneFactory.createDefaultDnDPane(FeedbackType.OUTLINE, true, (tabPane -> setupTabPane(tabPane, panelPlacement)));
        TabPane tabPane = (TabPane) pane.getChildren()
                .get(0);

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

        Pane wrapped = TabWrapper.wrap(pane, menuItems.toArray(new MenuItem[menuItems.size()]));

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

    @SuppressWarnings("unchecked")
    private TabPane setupTabPane(TabPane tabPane, PanelPlacement panelPlacement) {
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        for (NodeFactory<? extends ExplorationNode> nf : Get.services(NodeFactory.class)) {
            if (nf.isEnabled() && panelPlacement == nf.getPanelPlacement()) {
                //Node has requested to be initially placed on the panel we are processing.
                for (ManifoldGroup mg : nf.getDefaultManifoldGroups()) {
                    long start = System.currentTimeMillis();
                    Tab tab = new Tab();
                    ExplorationNode en = nf.createNode(manifolds.get(mg));
                    tab.setGraphic(en.getMenuIcon());
                    tab.setContent(new BorderPane(en.getNode()));
                    tab.textProperty().bind(en.getTitle());
//                   en.getTitleNode().ifPresent((titleNode) -> {
//                       tab.graphicProperty().set(titleNode);
//                       ((Label) titleNode).getGraphic().parentProperty().addListener((observable, oldValue, newValue) -> {
//                        System.out.println("Parent 3 changed: " + newValue);
//                        tab.graphicProperty().set(en.getTitleNode().get());
//                    });
//                   });
                    tab.setTooltip(new Tooltip(""));
                    tab.getTooltip().textProperty().bind(en.getToolTip());

                    if (en instanceof DetailNode) {
                        DetailNode dt = (DetailNode) en;
                        if (!(en instanceof ConceptBuilderNode)) {
                            TabDragAndDropHandler.setupTab(tab, dt);
                        }
                        
                        //TODO this is broken by design, if more than one tab requests focus on change...
                        dt.getManifold().focusedConceptProperty().addListener((observable, oldValue, newValue) -> {
                            if (dt.selectInTabOnChange()) {
                                tabPane.getSelectionModel().select(tab);
                            }
                        });
                    }

                    tabPane.getTabs().add(tab);

                    LOG.debug("Executed {} to the {} with manifold {} in {}ms",
                            nf.getClass().getName(), panelPlacement, mg, System.currentTimeMillis() - start);
                    if ((System.currentTimeMillis() - start) > 250) {
                        LOG.warn("NodeFactory {} is unacceptably slow!  Needs to background thread its work.  Took {}ms",
                                nf.getClass().getName(), System.currentTimeMillis() - start);
                    }
                }
            } else {
                LOG.debug("NodeFactory {} did not request an initial placement", nf.getClass().getName());
            }
        }
        return tabPane;
    }
}
