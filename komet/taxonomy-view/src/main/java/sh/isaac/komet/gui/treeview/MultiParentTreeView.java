/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.komet.gui.treeview;

import sh.komet.gui.manifold.Manifold;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mahout.math.Arrays;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ExceptionDialog;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.tree.Tree;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import static sh.komet.gui.style.StyleClasses.MULTI_PARENT_TREE_NODE;

/**
 * A {@link TreeView} for browsing the taxonomy.
 *
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class MultiParentTreeView extends BorderPane implements ExplorationNode {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

    private final static MultiParentTreeItemDisplayPolicies DEFAULT_DISPLAY_POLICIES = new DefaultMultiParentTreeItemDisplayPolicies();

    private static volatile boolean shutdownRequested = false;

    // initializationCountDownLatch begins with count of 2, indicating init() not yet run
    // initializationCountDownLatch count is decremented to 1 during init, indicating that init() started
    // initializationCountDownLatch count is decremented to 0 upon completion of init
    //
    // Calls to init() while count is less than 2 return immediately
    // Methods requiring that init() be completed must run init() if count > 1 and block on await()
    private final CountDownLatch initializationCountDownLatch = new CountDownLatch(2);

    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Multi-parent taxonomy view");
    private final StackPane stackPane;
    private final ToolBar toolBar = new ToolBar();
    private MultiParentTreeItem rootTreeItem;
    private final TreeView<ConceptChronology> treeView;
    private MultiParentTreeItemDisplayPolicies displayPolicies = DEFAULT_DISPLAY_POLICIES;

   public MultiParentTreeItemDisplayPolicies getDisplayPolicies() {
      return displayPolicies;
   }

    private volatile AtomicInteger refreshInProgress = new AtomicInteger(0);

    private Optional<UUID> selectedItem = Optional.empty();
    private final ArrayList<UUID> expandedUUIDs = new ArrayList<>();
    
    private BooleanProperty displayFSN = new SimpleBooleanProperty();
    
    SimpleObjectProperty<Manifold> manifoldProperty = new SimpleObjectProperty<>();
    
    private Tree taxonomyTree = null;
    protected Tree getTaxonomyTree() {
        if (taxonomyTree == null) {
            taxonomyTree = Get.taxonomyService().getTaxonomyTree(manifoldProperty.getValue());
        }
        return taxonomyTree;
    }
 
    
    public MultiParentTreeView(Manifold manifold) {
        long startTime = System.currentTimeMillis();
        getStyleClass().setAll(MULTI_PARENT_TREE_NODE.toString());
        
        this.manifoldProperty.set(manifold);
        treeView = new TreeView<>();
        //treeView.setSkin(new MultiParentTreeViewSkin<>(treeView));
        treeView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TreeItem<ConceptChronology>> observable, 
                 TreeItem<ConceptChronology> oldValue, 
                 TreeItem<ConceptChronology> newValue) -> {
                   if (newValue != null) {
                      manifold.setFocusedConceptChronology(newValue.getValue());
                   }
        });
        
        
         
        Button descriptionType = new Button();
        descriptionType.setPadding(new Insets(2.0));
        Node displayFsn = Iconography.LONG_TEXT.getIconographic();
        Tooltip.install(displayFsn, new Tooltip("Displaying the Fully Specified Name - click to display the Preferred Term"));
        displayFsn.visibleProperty().bind(displayFSN);
        Node displayPreferred = Iconography.SHORT_TEXT.getIconographic();
        displayPreferred.visibleProperty().bind(displayFSN.not());
        Tooltip.install(displayPreferred, new Tooltip("Displaying the Preferred Term - click to display the Fully Specified Name"));
        descriptionType.setGraphic(new StackPane(displayFsn, displayPreferred));
        descriptionType.setOnAction((ActionEvent event) -> {
           displayFSN.set(displayFSN.not().get());
        });
        
        toolBar.getItems().add(descriptionType);
        
        Button taxonomyViewMode = new Button();
        taxonomyViewMode.setPadding(new Insets(2.0));
        Node taxonomyInferred = Iconography.INFERRED_VIEW.getIconographic();
        
        taxonomyInferred.visibleProperty().bind(new BooleanBinding() {
           {
              super.bind(manifold.getManifoldCoordinate().premiseTypeProperty());
           }
           @Override
           protected boolean computeValue() {
              return manifold.getTaxonomyType() == PremiseType.INFERRED;
           }
        });
         
        Tooltip.install(taxonomyInferred, new Tooltip("Displaying the Inferred view- click to display the Inferred then Stated view"));
        Node taxonomyStated = Iconography.STATED_VIEW.getIconographic();
        taxonomyStated.visibleProperty().bind(new BooleanBinding() {
           {
              super.bind(manifold.getManifoldCoordinate().premiseTypeProperty());
           }
           @Override
           protected boolean computeValue() {
              return manifold.getTaxonomyType() == PremiseType.STATED;
           }
        });

        Tooltip.install(taxonomyStated, new Tooltip("Displaying the Stated view- click to display the Inferred view"));
        taxonomyViewMode.setGraphic(new StackPane(taxonomyInferred, taxonomyStated));
        taxonomyViewMode.setOnAction((ActionEvent event) -> {
           ObjectProperty<PremiseType> premiseProperty = manifold.getManifoldCoordinate().premiseTypeProperty();
           premiseProperty.set(premiseProperty.get().next());
        });
        toolBar.getItems().add(taxonomyViewMode);
        
        this.setTop(toolBar);
        
        stackPane = new StackPane();
        this.setCenter(stackPane);
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxHeight(100.0);
        pi.setMaxWidth(100.0);
        pi.getStyleClass().add("progressIndicator");
        StackPane.setAlignment(pi, Pos.CENTER);
        stackPane.getChildren().add(pi);
        LOG.debug("Tree View construct time: {}", System.currentTimeMillis() - startTime);
    }
    
    public MultiParentTreeItem getRoot() {
        return rootTreeItem;
    }
    
    public BorderPane getView()
    {
        if (initializationCountDownLatch.getCount() > 1) {
            LOG.debug("getView() called before initial init() started");
        } else if (initializationCountDownLatch.getCount() > 0) {
            LOG.debug("getView() called before initial init() completed");
        }
        return this;
    }
    
    /**
     * Convenience method for other code to add buttons, etc to the tool bar displayed above
     * the tree view 
     * @param node
     */
    public void addToToolBar(Node node)
    {
        toolBar.getItems().add(node);
    }

    public void refresh() {
        if (refreshInProgress.get() > 0) {
            LOG.debug("Skipping refresh due to in-progress refresh");
            return;
        }
        synchronized (refreshInProgress) {
            //Check again, because first check was before the sync block.
            if (refreshInProgress.get() > 0) {
                LOG.debug("Skipping refresh due to in-progress refresh");
                return;
            }
            refreshInProgress.incrementAndGet();
        }

        if (initializationCountDownLatch.getCount() > 1) {
            // called before initial init() run, so run init()
            init();
        }

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                // Waiting to ensure that init() completed
                initializationCountDownLatch.await();
                return new Object();
            }

            @Override
            protected void succeeded() {
                LOG.debug("Succeeded waiting for init() to complete");

                // record which items are expanded
                saveExpanded();
                
                LOG.debug("Removing existing children...");
                rootTreeItem.clearChildren();
                rootTreeItem.resetChildrenCalculators();
                LOG.debug("Removed existing children.");
                
                LOG.debug("Re-adding children...");
                Get.executor().execute(() -> rootTreeItem.addChildren());
                restoreExpanded();
                
                synchronized (refreshInProgress) {
                    refreshInProgress.decrementAndGet();
                }
            }

            @Override
            protected void failed() {
                synchronized (refreshInProgress) {
                    refreshInProgress.decrementAndGet();
                }
                Throwable ex = getException();
                String title = "Unexpected error waiting for init() to complete";
                String msg = ex.getClass().getName();
                LOG.error(title, ex);
                if (!shutdownRequested)
                {
                   showErrorDialog(ex, title, msg);
                }
            }

           private void showErrorDialog(Throwable ex, String title, String msg) {
              ExceptionDialog errorDialog = new ExceptionDialog(ex);
              errorDialog.initModality(Modality.WINDOW_MODAL);
              errorDialog.initStyle(StageStyle.UNDECORATED);
              errorDialog.setTitle(title);
              errorDialog.setContentText(msg);
              errorDialog.showAndWait();
           }
        };

        Get.executor().execute(task);
    }

    public void init() {
        init(MetaData.ISAAC_ROOT____ISAAC.getPrimordialUuid());
    }

    private synchronized void init(final UUID rootConcept) {
        if (initializationCountDownLatch.getCount() == 0) {
            LOG.debug("Ignoring call to init({}) after previous init() already completed", rootConcept);
            return;
        } else if (initializationCountDownLatch.getCount() <= 1) {
            LOG.debug("Ignoring call to init({}) while initial init() still running", rootConcept);
            return;
        } else if (initializationCountDownLatch.getCount() == 2) {
            initializationCountDownLatch.countDown();
            LOG.debug("Performing initial init({})", rootConcept);
        } else {
            // this should never happen
            throw new RuntimeException("SctTreeView initializationCountDownLatch_ has unexpected count " + initializationCountDownLatch.getCount() + " which is not 0, 1 or 2");
        }

        // Do work in background.
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                LOG.debug("Loading concept {} as the root of a tree view", rootConcept);

                try
                {
                    ConceptChronology rootConceptCV = Get.conceptService().getConcept(rootConcept);
                    rootTreeItem = new MultiParentTreeItem(rootConceptCV, MultiParentTreeView.this, Iconography.TAXONOMY_ROOT_ICON.getIconographic());
                    return null;
                }
                catch (Exception e)
                {
                    LOG.error("Error loading root concept of tree", e);
                    throw e;
                }
            }

            @Override
            protected void succeeded()
            {
                LOG.debug("getConceptVersion() (called by init()) succeeded");

                treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

                treeView.setCellFactory((TreeView<ConceptChronology> p) -> new MultiParentTreeCell(treeView));
                treeView.setRoot(rootTreeItem);
                
                Get.executor().execute(() -> rootTreeItem.addChildren());

                // put this event handler on the root
                rootTreeItem.addEventHandler(TreeItem.<ConceptChronology>branchCollapsedEvent(), (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
                   // remove grandchildren
                   ((MultiParentTreeItem) t.getSource()).removeGrandchildren();
                });

                rootTreeItem.addEventHandler(TreeItem.<ConceptChronology> branchExpandedEvent(), (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
                   // add grandchildren
                   MultiParentTreeItem sourceTreeItem = (MultiParentTreeItem) t.getSource();
                   Get.executor().execute(() -> sourceTreeItem.addChildrenConceptsAndGrandchildrenItems());
                });
                stackPane.getChildren().add(treeView);
                stackPane.getChildren().remove(0);  //remove the progress indicator

                // Final decrement of initializationCountDownLatch to 0,
                // indicating that initial init() is complete
                initializationCountDownLatch.countDown();

            }

            @Override
            protected void failed() {
                if (!shutdownRequested)
                {
                   ExceptionDialog dlg = new ExceptionDialog(getException());
                   dlg.initModality(Modality.WINDOW_MODAL);
                   dlg.initStyle(StageStyle.UTILITY);
                   dlg.showAndWait();
                   
                }
            }
        };

        Get.executor().execute(task);
    }

    public void showConcept(final UUID conceptUUID, final BooleanProperty workingIndicator) {
        if (initializationCountDownLatch.getCount() > 1) {
            // Called before initial init() run, so run init().
            // showConcept Task will internally await() init() completion.

            init();
        }

        // Do work in background.
        Task<MultiParentTreeItem> task = new Task<MultiParentTreeItem>() {

            @Override
            protected MultiParentTreeItem call() throws Exception {
                // await() init() completion.
                initializationCountDownLatch.await();
                
                LOG.debug("Looking for concept {} in tree", conceptUUID);

                final ArrayList<UUID> pathToRoot = new ArrayList<>();
                pathToRoot.add(conceptUUID);

                // Walk up taxonomy to origin until no parent found.
                UUID current = conceptUUID;
                while (true) {

                    Optional<? extends ConceptChronology> conceptOptional = Get.conceptService().getOptionalConcept(current);
                    if (! conceptOptional.isPresent()) {

                        // Must be a "pending concept".
                        // Not handled yet.
                        return null;
                    }

                    ConceptChronology concept = conceptOptional.get();
                    
                    // Look for an IS_A relationship to origin.
                    boolean found = false;
                    for (int parent : getTaxonomyTree().getParentSequences(concept.getConceptSequence())) {
                        current = Get.identifierService().getUuidPrimordialFromConceptId(parent).get();
                        pathToRoot.add(current);
                        
                        found = true;
                        break;
                    }

                    // No parent IS_A relationship found, stop looking.
                    if (! found) {
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
                    treeView.getSelectionModel().clearAndSelect(row);
                }

                // Turn off progress indicator.
                if (workingIndicator != null) {
                    workingIndicator.set(false);
                }
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                if (!wasGlobalShutdownRequested())
                {
                    LOG.warn("Unexpected error trying to find concept in Tree", ex);
    
                    // Turn off progress indicator.
                    if (workingIndicator != null) {
                        workingIndicator.set(false);
                    }
                }
            }
        };

        Get.executor().execute(task);
    }

    /**
     * The first call you make to this should pass in the root node.
     * 
     * After that you can call it repeatedly to walk down the tree (you need to know the path first)
     * This will handle the waiting for each node to open, before moving on to the next node.
     * 
     * This should be called on a background thread.
     *
     * @return the found child, or null, if not found. found child will have
     *         already been told to expand and fetch its children.
     * @throws InterruptedException 
     */
    private MultiParentTreeItem findChild(final MultiParentTreeItem item, final UUID targetChildUUID) throws InterruptedException {
        
        LOG.debug("Looking for {}", targetChildUUID);
        SimpleObjectProperty<MultiParentTreeItem> found = new SimpleObjectProperty<>(null);
        if (item.getValue().getPrimordialUuid().equals(targetChildUUID)) {
            // Found it.
            found.set(item);
        }
        else 
        {
            item.blockUntilChildrenReady();
            // Iterate through children and look for child with target UUID.
            for (TreeItem<ConceptChronology> child : item.getChildren()) {
                if (child != null && child.getValue() != null
                        && child.getValue().getPrimordialUuid().equals(targetChildUUID)) {

                    // Found it.
                    found.set((MultiParentTreeItem) child);
                    break;
                }
            }
        }
        if (found.get() != null)
        {
            found.get().blockUntilChildrenReady();
            CountDownLatch cdl = new CountDownLatch(1);
            Platform.runLater(() ->
            {
                treeView.scrollTo(treeView.getRow(found.get()));
                found.get().setExpanded(true);
                cdl.countDown();
            });
            cdl.await();
        }
        else
        {
            LOG.debug("Find child failed to find {}", targetChildUUID);
        }
        return found.get();
    }

    public void setDisplayPolicies(MultiParentTreeItemDisplayPolicies policies) {
        this.displayPolicies = policies;
    }

    public static MultiParentTreeItemDisplayPolicies getDefaultDisplayPolicies() {
        return DEFAULT_DISPLAY_POLICIES;
    }
    
    /**
     * Tell the tree to stop whatever threading operations it has running,
     * since the application is exiting.
     * @see gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI#shutdown()
     */
    public static void globalShutdownRequested()
    {
        shutdownRequested = true;
        LOG.info("Global Tree shutdown called!");
    }
    
    protected void shutdownInstance()
    {
        LOG.info("Shutdown taxonomy instance");
        synchronized (refreshInProgress) {  //hack way to disable future refresh calls
            refreshInProgress.incrementAndGet();
        }
        if (rootTreeItem != null)
        {
            rootTreeItem.clearChildren();  //This recursively cancels any active lookups
        }
    }
    
    protected static boolean wasGlobalShutdownRequested()
    {
        return shutdownRequested;
    }
    
    private void saveExpanded() {
        if (rootTreeItem.getChildren().isEmpty())
        {
            //keep the last save
            return;
        }
        TreeItem<ConceptChronology> selected = treeView.getSelectionModel().getSelectedItem();
        selectedItem = Optional.ofNullable(selected == null ? null : selected.getValue().getPrimordialUuid());
        expandedUUIDs.clear();
        saveExpanded(rootTreeItem);
        LOG.debug("Saved {} expanded nodes", expandedUUIDs.size());
    }

    private void saveExpanded(MultiParentTreeItem item) {
        if (!item.isLeaf() && item.isExpanded()) {
            expandedUUIDs.add(item.getConceptUuid());
            if (!item.isLeaf()) {
                for (TreeItem<ConceptChronology> child : item.getChildren()) {
                    saveExpanded((MultiParentTreeItem) child);
                }
            }
        }
    }
    
    private void restoreExpanded() {
        treeView.getSelectionModel().clearSelection();
        Get.executor().execute(() -> 
        {
            try
            {
                SimpleObjectProperty<MultiParentTreeItem> scrollTo = new SimpleObjectProperty<>();
                restoreExpanded(rootTreeItem, scrollTo);
                
                expandedUUIDs.clear();
                selectedItem = Optional.empty();
                if (scrollTo.get() != null)
                {
                    Platform.runLater(() -> 
                    {
                        treeView.scrollTo(treeView.getRow(scrollTo.get()));
                        treeView.getSelectionModel().select(scrollTo.get());
                    });
                }
            }
            catch (InterruptedException e)
            {
                LOG.info("Interrupted while looking restoring expanded items");
            }
        });
        
    }
    
    private void restoreExpanded(MultiParentTreeItem item, SimpleObjectProperty<MultiParentTreeItem> scrollTo) throws InterruptedException {
        if (expandedUUIDs.contains(item.getConceptUuid())) {
            item.blockUntilChildrenReady();
            Platform.runLater(() -> item.setExpanded(true));
            List<TreeItem<ConceptChronology>> list = new ArrayList<>(item.getChildren());
            for (TreeItem<ConceptChronology> child : list) {
                restoreExpanded((MultiParentTreeItem) child, scrollTo);
            }
        }
        if (selectedItem.isPresent() && selectedItem.get().equals(item.getConceptUuid())) {
            scrollTo.set(item);
        }
    }

   @Override
   public Manifold getManifold() {
      return this.manifoldProperty.get();
   }

   @Override
   public Node getNode() {
      return this;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }
}
