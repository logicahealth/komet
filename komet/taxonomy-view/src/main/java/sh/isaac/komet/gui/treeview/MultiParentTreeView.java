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



package sh.isaac.komet.gui.treeview;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.concurrent.Task;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;

import com.lmax.disruptor.EventHandler;

import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertAction;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertEvent;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.tree.Tree;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.alert.AlertPanel;
import sh.komet.gui.control.ChoiceBoxControls;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import static sh.isaac.komet.gui.treeview.TreeViewExplorationNodeFactory.MENU_TEXT;
import sh.komet.gui.layout.LayoutAnimator;

import static sh.komet.gui.style.StyleClasses.MULTI_PARENT_TREE_NODE;

//~--- classes ----------------------------------------------------------------

/**
 * A {@link TreeView} for browsing the taxonomy.
 *
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MultiParentTreeView
        extends BorderPane
         implements ExplorationNode {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();
   private final static MultiParentTreeItemDisplayPolicies DEFAULT_DISPLAY_POLICIES =
      new DefaultMultiParentTreeItemDisplayPolicies();
   private static volatile boolean shutdownRequested = false;

   //~--- fields --------------------------------------------------------------

   private final SimpleStringProperty         titleProperty   = new SimpleStringProperty(MENU_TEXT);
   private final SimpleStringProperty         toolTipProperty = new SimpleStringProperty("Multi-parent taxonomy view");
   private final ToolBar                      toolBar         = new ToolBar();
   private MultiParentTreeItemDisplayPolicies displayPolicies = DEFAULT_DISPLAY_POLICIES;
   private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
                                                               Iconography.TAXONOMY_ICON.getIconographic());
   private Optional<UUID>                                      selectedItem            = Optional.empty();
   private final ArrayList<UUID>                               expandedUUIDs           = new ArrayList<>();
   private final BooleanProperty                               displayFQN              = new SimpleBooleanProperty();
   private final SimpleObjectProperty<TaxonomySnapshotService> taxonomySnapshotService = new SimpleObjectProperty<>();
   private final ObservableList<AlertObject>                   alertList = FXCollections.observableArrayList();
   private final GridPane                                      topGridPane             = new GridPane();

   /** added to prevent garbage collection of listener while this node is still active */
   private final EventHandler<AlertEvent>    alertHandler = this::handleAlert;
   private final CreateSnapshotService       createSnapshotService;
   private final Manifold                    manifold;
   private final StackPane                   stackPane;
   private final ProgressIndicator           taxonomyTreeFetchProgress;
   private final MultiParentTreeItem         rootTreeItem;
   private final TreeView<ConceptChronology> treeView;
   private final LayoutAnimator topPaneAnimator = new LayoutAnimator();
   private final LayoutAnimator taxonomyAlertsAnimator = new LayoutAnimator();

   //~--- constructors --------------------------------------------------------

   public MultiParentTreeView(Manifold manifold, ConceptSpecification rootSpec) {
      long startTime = System.currentTimeMillis();

      this.createSnapshotService = new CreateSnapshotService(manifold);
      this.createSnapshotService.setExecutor(Get.executor());
      this.taxonomySnapshotService.bind(createSnapshotService.valueProperty());
      this.taxonomySnapshotService.addListener(this::snapshotReady);
      this.taxonomyTreeFetchProgress = new ProgressIndicator();
      this.taxonomyTreeFetchProgress.setMaxHeight(100.0);
      this.taxonomyTreeFetchProgress.setMaxWidth(100.0);
      this.taxonomyTreeFetchProgress.getStyleClass()
                                    .add("progressIndicator");
      this.taxonomyTreeFetchProgress.progressProperty()
                                    .bind(this.createSnapshotService.progressProperty());
      this.taxonomyTreeFetchProgress.visibleProperty()
                                    .bind(this.createSnapshotService.runningProperty());
      getStyleClass().setAll(MULTI_PARENT_TREE_NODE.toString());
      this.manifold = manifold;
      treeView      = new TreeView<>();
      treeView.getSelectionModel()
              .selectedItemProperty()
              .addListener(
                  (ObservableValue<? extends TreeItem<ConceptChronology>> observable,
                   TreeItem<ConceptChronology> oldValue,
                   TreeItem<ConceptChronology> newValue) -> {
                     if (newValue != null) {
                        manifold.setFocusedConceptChronology(newValue.getValue());
                     }
                  });
      stackPane = new StackPane();
      this.setCenter(stackPane);
      StackPane.setAlignment(taxonomyTreeFetchProgress, Pos.CENTER);
      stackPane.getChildren()
               .add(treeView);
      stackPane.getChildren()
               .add(taxonomyTreeFetchProgress);

      ConceptChronology rootConceptCV = Get.conceptService()
                                           .getConcept(rootSpec);

      rootTreeItem = new MultiParentTreeItem(
          rootConceptCV,
          MultiParentTreeView.this,
          Iconography.TAXONOMY_ROOT_ICON.getIconographic());
      treeView.getSelectionModel()
              .setSelectionMode(SelectionMode.SINGLE);
      treeView.setCellFactory((TreeView<ConceptChronology> p) -> new MultiParentTreeCell(treeView));
      treeView.setRoot(rootTreeItem);

      // put this event handler on the root
      rootTreeItem.addEventHandler(
          TreeItem.<ConceptChronology>branchCollapsedEvent(),
              (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
         // remove grandchildren
                 ((MultiParentTreeItem) t.getSource()).removeGrandchildren();
              });
      rootTreeItem.addEventHandler(
          TreeItem.<ConceptChronology>branchExpandedEvent(),
              (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
                 MultiParentTreeItem sourceTreeItem = (MultiParentTreeItem) t.getSource();

                 Get.executor()
                    .execute(() -> sourceTreeItem.addChildrenConceptsAndGrandchildrenItems());
              });
      this.createSnapshotService.start();
      Alert.addAlertListener(alertHandler);
      alertList.addListener(this::onChanged);
      
      topPaneAnimator.observe(topGridPane);
      this.setTop(topGridPane);
      taxonomyAlertsAnimator.observe(this.getChildren());
      setupTopPane();
      LOG.debug("Tree View construct time: {}", System.currentTimeMillis() - startTime);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Convenience method for other code to add buttons, etc to the tool bar displayed above the tree view
    *
    * @param node
    */
   public void addToToolBar(Node node) {
      toolBar.getItems()
             .add(node);
   }

   /**
    * Tell the tree to stop whatever threading operations it has running, since the application is exiting.
    *
    * @see gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI#shutdown()
    */
   public static void globalShutdownRequested() {
      shutdownRequested = true;
      LOG.info("Global Tree shutdown called!");
   }

   public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) {
      // Do work in background.
      Task<MultiParentTreeItem> task = new Task<MultiParentTreeItem>() {
         @Override
         protected MultiParentTreeItem call()
                  throws Exception {
            // await() init() completion.
            LOG.debug("Looking for concept {} in tree", conceptUUID);

            final ArrayList<UUID> pathToRoot = new ArrayList<>();

            pathToRoot.add(conceptUUID);

            // Walk up taxonomy to origin until no parent found.
            UUID current = conceptUUID;

            while (true) {
               Optional<? extends ConceptChronology> conceptOptional = Get.conceptService()
                                                                          .getOptionalConcept(current);

               if (!conceptOptional.isPresent()) {
                  // Must be a "pending concept".
                  // Not handled yet.
                  return null;
               }

               ConceptChronology concept = conceptOptional.get();

               // Look for an IS_A relationship to origin.
               boolean found = false;

               for (int parent: getTaxonomyTree().getParentSequences(concept.getConceptSequence())) {
                  current = Get.identifierService()
                               .getUuidPrimordialFromConceptId(parent)
                               .get();
                  pathToRoot.add(current);
                  found = true;
                  break;
               }

               // No parent IS_A relationship found, stop looking.
               if (!found) {
                  break;
               }
            }

            LOG.debug("Calculated root path {}", Arrays.toString(pathToRoot.toArray()));

            MultiParentTreeItem currentTreeItem = rootTreeItem;

            // Walk down path from root.
            for (int i = pathToRoot.size() - 1; i >= 0; i--) {
               MultiParentTreeItem child = findChild(currentTreeItem, pathToRoot.get(i));

               if (child == null) {
                  break;
               }

               currentTreeItem = child;
            }

            return currentTreeItem;
         }
         @Override
         protected void succeeded() {
            final MultiParentTreeItem lastItemFound = this.getValue();

            // Expand tree to last item found.
            if (lastItemFound != null) {
               int row = treeView.getRow(lastItemFound);

               treeView.scrollTo(row);
               treeView.getSelectionModel()
                       .clearAndSelect(row);
            }

            // Turn off progress indicator.
            if (workingIndicator != null) {
               workingIndicator.set(false);
            }
         }
         @Override
         protected void failed() {
            Throwable ex = getException();

            if (!wasGlobalShutdownRequested()) {
               LOG.warn("Unexpected error trying to find concept in Tree", ex);

               // Turn off progress indicator.
               if (workingIndicator != null) {
                  workingIndicator.set(false);
               }
            }
         }
      };

      Get.executor()
         .execute(task);
   }

   protected void shutdownInstance() {
      LOG.info("Shutdown taxonomy instance");

      if (rootTreeItem != null) {
         rootTreeItem.clearChildren();  // This recursively cancels any active lookups
      }
   }

   protected static boolean wasGlobalShutdownRequested() {
      return shutdownRequested;
   }

   /**
    * The first call you make to this should pass in the root node.
    *
    * After that you can call it repeatedly to walk down the tree (you need to know the path first) This will handle the
    * waiting for each node to open, before moving on to the next node.
    *
    * This should be called on a background thread.
    *
    * @return the found child, or null, if not found. found child will have already been told to expand and fetch its
    * children.
    * @throws InterruptedException
    */
   private MultiParentTreeItem findChild(final MultiParentTreeItem item,
         final UUID targetChildUUID)
            throws InterruptedException {
      LOG.debug("Looking for {}", targetChildUUID);

      SimpleObjectProperty<MultiParentTreeItem> found = new SimpleObjectProperty<>(null);

      if (item.getValue()
              .getPrimordialUuid()
              .equals(targetChildUUID)) {
         // Found it.
         found.set(item);
      } else {
         item.blockUntilChildrenReady();

         // Iterate through children and look for child with target UUID.
         for (TreeItem<ConceptChronology> child: item.getChildren()) {
            if ((child != null) && (child.getValue() != null) && child.getValue().isIdentifiedBy(targetChildUUID)) {
               // Found it.
               found.set((MultiParentTreeItem) child);
               break;
            }
         }
      }

      if (found.get() != null) {
         found.get()
              .blockUntilChildrenReady();

         CountDownLatch cdl = new CountDownLatch(1);

         Platform.runLater(
             () -> {
                treeView.scrollTo(treeView.getRow(found.get()));
                found.get()
                     .setExpanded(true);
                cdl.countDown();
             });
         cdl.await();
      } else {
         LOG.debug("Find child failed to find {}", targetChildUUID);
      }

      return found.get();
   }

   private void handleAlert(AlertEvent event, long sequence, boolean endOfBatch) {
      AlertObject alertObject = event.getAlertObject();

      if (alertObject.getAlertCategory() == AlertCategory.TAXONOMY) {
         switch (event.getAlertAction()) {
         case ADD:
            Platform.runLater(() -> alertList.add(alertObject));
            break;

         case RETRACT:
            Platform.runLater(() -> alertList.remove(alertObject));
            break;

         default:
            throw new UnsupportedOperationException("Can't handle: " + event.getAlertAction());
         }
      }
   }

   private void onChanged(ListChangeListener.Change<? extends AlertObject> change) {
      setupTopPane();
   }

   private void restoreExpanded() {
      treeView.getSelectionModel()
              .clearSelection();
      Get.executor()
         .execute(
             () -> {
                try {
                   SimpleObjectProperty<MultiParentTreeItem> scrollTo = new SimpleObjectProperty<>();

                   restoreExpanded(rootTreeItem, scrollTo);
                   expandedUUIDs.clear();
                   selectedItem = Optional.empty();

                   if (scrollTo.get() != null) {
                      Platform.runLater(
                          () -> {
                             treeView.scrollTo(treeView.getRow(scrollTo.get()));
                             treeView.getSelectionModel()
                                     .select(scrollTo.get());
                          });
                   }
                } catch (InterruptedException e) {
                   LOG.info("Interrupted while looking restoring expanded items");
                }
             });
   }

   private void restoreExpanded(MultiParentTreeItem item,
                                SimpleObjectProperty<MultiParentTreeItem> scrollTo)
            throws InterruptedException {
      if (expandedUUIDs.contains(item.getConceptUuid())) {
         item.blockUntilChildrenReady();
         Platform.runLater(() -> item.setExpanded(true));

         List<TreeItem<ConceptChronology>> list = new ArrayList<>(item.getChildren());

         for (TreeItem<ConceptChronology> child: list) {
            restoreExpanded((MultiParentTreeItem) child, scrollTo);
         }
      }

      if (selectedItem.isPresent() && selectedItem.get().equals(item.getConceptUuid())) {
         scrollTo.set(item);
      }
   }

   private void saveExpanded() {
      if (rootTreeItem.getChildren()
                      .isEmpty()) {
         // keep the last save
         return;
      }

      TreeItem<ConceptChronology> selected = treeView.getSelectionModel()
                                                     .getSelectedItem();

      selectedItem = Optional.ofNullable((selected == null) ? null
            : selected.getValue()
                      .getPrimordialUuid());
      expandedUUIDs.clear();
      saveExpanded(rootTreeItem);
      LOG.debug("Saved {} expanded nodes", expandedUUIDs.size());
   }

   private void saveExpanded(MultiParentTreeItem item) {
      if (!item.isLeaf() && item.isExpanded()) {
         expandedUUIDs.add(item.getConceptUuid());

         if (!item.isLeaf()) {
            for (TreeItem<ConceptChronology> child: item.getChildren()) {
               saveExpanded((MultiParentTreeItem) child);
            }
         }
      }
   }

   private void setupTopPane() {
      toolBar.getItems()
             .clear();

      ChoiceBox<ConceptSpecification> descriptionTypeChoiceBox = ChoiceBoxControls.getDescriptionTypeForDisplay(
                                                                     manifold);

      toolBar.getItems()
             .add(descriptionTypeChoiceBox);

      ChoiceBox<ConceptSpecification> premiseChoiceBox = ChoiceBoxControls.getTaxonomyPremiseTypes(manifold);

      premiseChoiceBox.valueProperty()
                      .addListener(this::taxonomyPremiseChanged);
      toolBar.getItems()
             .add(premiseChoiceBox);

      // Node child, int columnIndex, int rowIndex, int columnspan, int rowspan,
      // HPos halignment, VPos valignment, Priority hgrow, Priority vgrow
      topPaneAnimator.unobserve(topGridPane.getChildren());
      topGridPane.getChildren()
                 .clear();
      double yStart = topGridPane.getLayoutY();
      int row = 0;

      GridPane.setConstraints(toolBar, 0, row++, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
      topGridPane.getChildren()
                 .add(toolBar);

      for (AlertObject alert: alertList) {
         AlertPanel alertPanel = new AlertPanel(alert);
         alertPanel.layoutYProperty().set(toolBar.getHeight());
         topPaneAnimator.observe(alertPanel);
         

         GridPane.setConstraints(alertPanel, 0, row++, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
         topGridPane.getChildren()
                    .add(alertPanel);
      }

   }

   private void snapshotReady(ObservableValue<? extends TaxonomySnapshotService> observable,
                              TaxonomySnapshotService oldValue,
                              TaxonomySnapshotService newValue) {
      Get.executor()
         .execute(() -> this.rootTreeItem.addChildren());
      restoreExpanded();
   }

   private void taxonomyPremiseChanged(ObservableValue<? extends ConceptSpecification> observable,
         ConceptSpecification oldValue,
         ConceptSpecification newValue) {
      saveExpanded();
      this.manifold.getManifoldCoordinate()
                   .premiseTypeProperty()
                   .set(PremiseType.fromConcept(newValue));
      this.rootTreeItem.clearChildren();
      this.rootTreeItem.resetChildrenCalculators();
      this.alertList.clear();
      this.createSnapshotService.restart();
   }

   //~--- get methods ---------------------------------------------------------

   public static MultiParentTreeItemDisplayPolicies getDefaultDisplayPolicies() {
      return DEFAULT_DISPLAY_POLICIES;
   }

   public MultiParentTreeItemDisplayPolicies getDisplayPolicies() {
      return displayPolicies;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDisplayPolicies(MultiParentTreeItemDisplayPolicies policies) {
      this.displayPolicies = policies;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Manifold getManifold() {
      return this.manifold;
   }

   @Override
   public Node getNode() {
      return this;
   }

   public MultiParentTreeItem getRoot() {
      return rootTreeItem;
   }

   protected Tree getTaxonomyTree() {
      TaxonomySnapshotService service = this.taxonomySnapshotService.get();

      if (service != null) {
         return service.getTaxonomyTree();
      }

      return null;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return titleProperty;
   }

   @Override
   public Optional<Node> getTitleNode() {
      Label titleLabel = new Label();

      titleLabel.graphicProperty()
                .bind(iconProperty);
      titleLabel.textProperty()
                .bind(titleProperty);
      return Optional.of(titleLabel);
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }

   public BorderPane getView() {
      return this;
   }
}

