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

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.geometry.HPos;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lmax.disruptor.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import sh.isaac.api.Get;
import sh.isaac.api.RefreshListener;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertEvent;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.alert.AlertPanel;
import sh.komet.gui.control.ChoiceBoxControls;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import static sh.isaac.komet.gui.treeview.TreeViewExplorationNodeFactory.MENU_TEXT;
import sh.komet.gui.control.OnOffToggleSwitch;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.layout.LayoutAnimator;

import static sh.komet.gui.style.StyleClasses.MULTI_PARENT_TREE_NODE;
import sh.komet.gui.util.FxGet;

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
        implements ExplorationNode, RefreshListener {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();
    private static volatile boolean shutdownRequested = false;

    //~--- fields --------------------------------------------------------------
    private final SimpleStringProperty titleProperty = new SimpleStringProperty(MENU_TEXT);
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Multi-parent taxonomy view");
    private final OnOffToggleSwitch historySwitch = new OnOffToggleSwitch();
    private final ToolBar toolBar = new ToolBar();
    private MultiParentTreeItemDisplayPolicies displayPolicies;
    private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
            Iconography.TAXONOMY_ICON.getIconographic());
    private Optional<UUID> selectedItem = Optional.empty();
    private final ArrayList<UUID> expandedUUIDs = new ArrayList<>();
    private final ObservableList<AlertObject> alertList = FXCollections.observableArrayList();
    private final GridPane topGridPane = new GridPane();

    /**
     * added to prevent garbage collection of listener while this node is still
     * active
     */
    private final EventHandler<AlertEvent> alertHandler = this::handleAlert;
    private final Manifold manifold;
    private final MultiParentTreeItemImpl rootTreeItem;
    private final TreeView<ConceptChronology> treeView;
    private final LayoutAnimator topPaneAnimator = new LayoutAnimator();
    private final LayoutAnimator taxonomyAlertsAnimator = new LayoutAnimator();
    private final ChoiceBox<ConceptSpecification> descriptionTypeChoiceBox;
    private final ChoiceBox<ConceptSpecification> premiseChoiceBox;
    private final SimpleObjectProperty<TaxonomySnapshotService> taxonomySnapshotProperty = new SimpleObjectProperty<>();
    private final UUID uuid = UUID.randomUUID();
    private final Label titleLabel = new Label();

    //~--- constructors --------------------------------------------------------
    public MultiParentTreeView(Manifold manifold, ConceptSpecification rootSpec) {
        long startTime = System.currentTimeMillis();
        this.manifold = manifold.deepClone();
        this.manifold.getStampCoordinate().allowedStatesProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Allowed states changed to: " + newValue);
        });
        historySwitch.setSelected(false);
        updateManifoldHistoryStates();
        historySwitch.selectedProperty()
                .addListener(this::setShowHistory);

        this.displayPolicies = new DefaultMultiParentTreeItemDisplayPolicies(this.manifold);
        this.taxonomySnapshotProperty.set(Get.taxonomyService().getSnapshot(this.manifold));
        getStyleClass().setAll(MULTI_PARENT_TREE_NODE.toString());
        treeView = new TreeView<>();
        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (ObservableValue<? extends TreeItem<ConceptChronology>> observable,
                                TreeItem<ConceptChronology> oldValue,
                                TreeItem<ConceptChronology> newValue) -> {
                            if (newValue != null) {
                                this.manifold.setFocusedConceptChronology(newValue.getValue());
                            }
                        });
        this.setCenter(treeView);

        ConceptChronology rootConceptCV = Get.conceptService()
                .getConceptChronology(rootSpec);

        rootTreeItem = new MultiParentTreeItemImpl(
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
                    ((MultiParentTreeItemImpl) t.getSource()).removeChildren();
                });
        rootTreeItem.addEventHandler(
                TreeItem.<ConceptChronology>branchExpandedEvent(),
                (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
                    MultiParentTreeItemImpl sourceTreeItem = (MultiParentTreeItemImpl) t.getSource();

                    Get.executor()
                            .execute(() -> sourceTreeItem.addChildren());
                });
        Alert.addAlertListener(alertHandler);
        alertList.addListener(this::onChanged);

        topPaneAnimator.observe(topGridPane);
        this.setTop(topGridPane);
        taxonomyAlertsAnimator.observe(this.getChildren());
        descriptionTypeChoiceBox = ChoiceBoxControls.getDescriptionTypeForDisplay(
                this.manifold);
        descriptionTypeChoiceBox.addEventHandler(ActionEvent.ACTION, this::handleDescriptionTypeChange);
        handleDescriptionTypeChange(null);
        this.premiseChoiceBox = ChoiceBoxControls.getTaxonomyPremiseTypes(this.manifold);
        this.premiseChoiceBox.valueProperty()
                .addListener(this::taxonomyPremiseChanged);

        setupTopPane();
        // Not a leak, since the taxonomy service adds a weak reference to the listener. 
        Get.taxonomyService().addTaxonomyRefreshListener(this);
        this.titleLabel.graphicProperty()
                .bind(iconProperty);
        this.titleLabel.textProperty()
                .bind(titleProperty);

        this.setOnDragOver(this::dragOver);
        this.setOnDragDropped(this::dragDropped);

        LOG.debug("Tree View construct time: {}", System.currentTimeMillis() - startTime);
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.TAXONOMY_ICON.getIconographic();
    }

    private void dragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT)) {
            ConceptChronology conceptChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT);
            showConcept(conceptChronology.getPrimordialUuid());
            success = true;
        } else if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT_VERSION)) {
            ConceptVersion conceptVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT_VERSION);

            showConcept(conceptVersion.getPrimordialUuid());
            success = true;
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION)) {
            SemanticChronology semanticChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION);
            UUID primordial = Get.identifierService()
                    .getUuidPrimordialForNid(semanticChronology.getReferencedComponentNid());
            showConcept(primordial);
            success = true;

        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION_VERSION)) {
            DescriptionVersion descriptionVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION_VERSION);
            UUID primordial = Get.identifierService()
                    .getUuidPrimordialForNid(descriptionVersion.getReferencedComponentNid());
            showConcept(primordial);
            success = true;
        }
        /* let the source know if the droped item was successfully 
                 * transferred and used */
        event.setDropCompleted(success);

        event.consume();
    }

    private void dragOver(DragEvent event) {

        /* accept it only if it is  not dragged from the same node */
        if (event.getGestureSource() != this) {
            /* allow for both copying */
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    //~--- methods -------------------------------------------------------------
    private void setShowHistory(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        updateManifoldHistoryStates();
        refresh();
    }

    private void updateManifoldHistoryStates() {
        if (historySwitch.isSelected()) {
            this.manifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .clear();
            this.manifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .addAll(Status.makeActiveAndInactiveSet());
        } else {
            this.manifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .clear();
            this.manifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .addAll(Status.makeActiveOnlySet());
        }
    }

    @Override
    public void refresh() {
        Platform.runLater(() -> {
            this.refreshTaxonomy();
        });
    }

    /**
     * Convenience method for other code to add buttons, etc to the tool bar
     * displayed above the tree view
     *
     * @param node
     */
    public void addToToolBar(Node node) {
        toolBar.getItems()
                .add(node);
    }

    /**
     * Tell the tree to stop whatever threading operations it has running, since
     * the application is exiting.
     *
     * @see
     * gov.va.isaac.interfaces.utility.ShutdownBroadcastListenerI#shutdown()
     */
    public static void globalShutdownRequested() {
        shutdownRequested = true;
        LOG.info("Global Tree shutdown called!");
    }

    @Override
    public UUID getListenerUuid() {
        return this.uuid;
    }

    public void showConcept(final UUID conceptUUID) {
        // Do work in background.
        ShowConceptInTaxonomyTask task
                = new ShowConceptInTaxonomyTask(this, conceptUUID);

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
     * After that you can call it repeatedly to walk down the tree (you need to
     * know the path first) This will handle the waiting for each node to open,
     * before moving on to the next node.
     *
     * This should be called on a background thread.
     *
     * @return the found child, or null, if not found. found child will have
     * already been told to expand and fetch its children.
     * @throws InterruptedException
     */
    protected MultiParentTreeItemImpl findChild(final MultiParentTreeItemImpl item,
            final UUID targetChildUUID)
            throws InterruptedException {
        LOG.debug("Looking for {}", targetChildUUID);

        SimpleObjectProperty<MultiParentTreeItemImpl> found = new SimpleObjectProperty<>(null);

        if (item.getValue()
                .getPrimordialUuid()
                .equals(targetChildUUID)) {
            // Found it.
            found.set(item);
        } else {
            item.blockUntilChildrenReady();

            // Iterate through children and look for child with target UUID.
            for (TreeItem<ConceptChronology> child : item.getChildren()) {
                if ((child != null) && (child.getValue() != null) && child.getValue().isIdentifiedBy(targetChildUUID)) {
                    // Found it.
                    found.set((MultiParentTreeItemImpl) child);
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
                                SimpleObjectProperty<MultiParentTreeItemImpl> scrollTo = new SimpleObjectProperty<>();

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

    private void restoreExpanded(MultiParentTreeItemImpl item,
            SimpleObjectProperty<MultiParentTreeItemImpl> scrollTo)
            throws InterruptedException {
        if (expandedUUIDs.contains(item.getConceptUuid())) {
            item.addChildren();
            item.blockUntilChildrenReady();
            Platform.runLater(() -> item.setExpanded(true));

            List<TreeItem<ConceptChronology>> list = new ArrayList<>(item.getChildren());

            for (TreeItem<ConceptChronology> child : list) {
                restoreExpanded((MultiParentTreeItemImpl) child, scrollTo);
            }
        }

        if (selectedItem.isPresent() && selectedItem.get().equals(item.getConceptUuid())) {
            scrollTo.set(item);
        }
    }

    public void expandAndSelect(ArrayList<UUID> expansionPath) {
        MultiParentTreeItemImpl currentItem = rootTreeItem;
        if (currentItem.getConceptUuid().equals(expansionPath.get(0))) {
            currentItem.addChildrenNow();
            currentItem.setExpanded(true);
            Platform.runLater(new ExpandTask(currentItem, expansionPath, 1));
        } else {
            FxGet.statusMessageService().reportStatus("Expansion path for concept does not end at root. ");
        }
    }

    private class ExpandTask extends TimedTaskWithProgressTracker<Void> {

        final ArrayList<UUID> expansionPath;
        final int pathIndex;
        final MultiParentTreeItemImpl currentItem;

        public ExpandTask(MultiParentTreeItemImpl currentItem, ArrayList<UUID> expansionPath, int pathIndex) {
            this.currentItem = currentItem;
            this.expansionPath = expansionPath;
            this.pathIndex = pathIndex;
        }

        @Override
        protected Void call() throws Exception {
            treeView.scrollTo(treeView.getRow(currentItem));
            treeView.getSelectionModel().select(currentItem);
            if (pathIndex < expansionPath.size()) {
                UUID childUuidToMatch = expansionPath.get(pathIndex);
                for (TreeItem child : currentItem.getChildren()) {
                    MultiParentTreeItemImpl childItem = (MultiParentTreeItemImpl) child;
                    if (childItem.getConceptUuid().equals(childUuidToMatch)) {
                        childItem.addChildrenNow();
                        currentItem.setExpanded(true);
                        Platform.runLater(new ExpandTask(childItem, expansionPath, pathIndex + 1));
                        break;
                    }
                }
            } 
            return null;
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

    private void saveExpanded(MultiParentTreeItemImpl item) {
        if (!item.isLeaf() && item.isExpanded()) {
            expandedUUIDs.add(item.getConceptUuid());

            if (!item.isLeaf()) {
                for (TreeItem<ConceptChronology> child : item.getChildren()) {
                    saveExpanded((MultiParentTreeItemImpl) child);
                }
            }
        }
    }

    private void setupTopPane() {
        toolBar.getItems()
                .clear();

        toolBar.getItems()
                .add(descriptionTypeChoiceBox);

        toolBar.getItems()
                .add(premiseChoiceBox);

        Label historySwitchWithLabel = new Label("History", historySwitch);
        historySwitchWithLabel.setContentDisplay(ContentDisplay.RIGHT);
        toolBar.getItems()
                .add(historySwitchWithLabel);

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

        for (AlertObject alert : alertList) {
            AlertPanel alertPanel = new AlertPanel(alert);
            alertPanel.layoutYProperty().set(toolBar.getHeight());
            topPaneAnimator.observe(alertPanel);

            GridPane.setConstraints(alertPanel, 0, row++, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
            topGridPane.getChildren()
                    .add(alertPanel);
        }

    }
    
    public int getPreferredDescriptionType() {
        return this.manifold.getLanguageCoordinate().descriptionTypePreferenceListProperty().get().get(0);
    }

    public final void handleDescriptionTypeChange(ActionEvent event) {
        ConceptSpecification selectedDescriptionType = this.descriptionTypeChoiceBox.getSelectionModel().getSelectedItem();
        List<ConceptSpecification> items = this.descriptionTypeChoiceBox.getItems();
        int[] descriptionTypes = new int[items.size()];
        int descriptionIndex = 0;
        descriptionTypes[descriptionIndex++] = selectedDescriptionType.getNid();
        for (ConceptSpecification spec : items) {
            if (spec != selectedDescriptionType) {
                descriptionTypes[descriptionIndex++] = spec.getNid();
            }
        }
        this.manifold.getLanguageCoordinate().descriptionTypePreferenceListProperty().get().setAll(descriptionTypes);
        this.rootTreeItem.invalidate();
        this.treeView.refresh();
    }

    private void taxonomyPremiseChanged(ObservableValue<? extends ConceptSpecification> observable,
            ConceptSpecification oldValue,
            ConceptSpecification newValue) {
        refreshTaxonomy();
    }

    private void refreshTaxonomy() {
        saveExpanded();
        PremiseType newPremiseType = PremiseType.fromConcept(this.premiseChoiceBox.getValue());
        this.manifold.getManifoldCoordinate()
                .taxonomyPremiseTypeProperty()
                .set(newPremiseType);
        taxonomySnapshotProperty.set(Get.taxonomyService().getSnapshot(manifold));
        this.rootTreeItem.clearChildren();
        Get.workExecutors().getExecutor().execute(() -> this.rootTreeItem.addChildren());
        this.rootTreeItem.invalidate();
        this.alertList.clear();
        restoreExpanded();
    }

    //~--- get methods ---------------------------------------------------------
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

    public MultiParentTreeItemImpl getRoot() {
        return rootTreeItem;
    }

    protected TaxonomySnapshotService getTaxonomySnapshot() {
        return taxonomySnapshotProperty.get();
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.of(titleLabel);
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    public BorderPane getView() {
        return this;
    }

    public TreeView<ConceptChronology> getTreeView() {
        return treeView;
    }
}
