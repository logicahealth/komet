package sh.isaac.komet.gui.graphview;

import com.lmax.disruptor.EventHandler;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.RefreshListener;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertEvent;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.navigation.EmptyNavigator;
import sh.isaac.api.navigation.Navigator;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateWithOverride;
import sh.komet.gui.alert.AlertPanel;
import sh.komet.gui.clipboard.ClipboardHelper;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.layout.LayoutAnimator;
import sh.komet.gui.util.FxGet;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static sh.komet.gui.style.StyleClasses.MULTI_PARENT_TREE_NODE;

public class MultiParentGraphViewController implements RefreshListener {
    private ObservableManifoldCoordinateWithOverride manifoldCoordinateWithOverride;

    public enum Keys {
        ACTIVITY_FEED
    }

    private static final Logger LOG = LogManager.getLogger();
    private static volatile boolean shutdownRequested = false;

    @FXML
    private BorderPane topBorderPane;

    @FXML
    private GridPane topGridPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private MenuButton navigationMenuButton;

    @FXML
    Menu navigationCoordinateMenu;

    private final ListChangeListener<ConceptSpecification> navigationSelectionListener = this::navigationSelectionChanged;

    private final CheckComboBox<ConceptSpecification> navigationMultiChoiceBox = new CheckComboBox<>();

    //~--- fields --------------------------------------------------------------
    private MultiParentGraphItemDisplayPolicies displayPolicies;
    private Optional<UUID> selectedItem = Optional.empty();
    private final ArrayList<UUID> expandedUUIDs = new ArrayList<>();
    private final ObservableList<AlertObject> alertList = FXCollections.observableArrayList();

    /**
     * added to prevent garbage collection of listener while this node is still
     * active
     */
    private final EventHandler<AlertEvent> alertHandler = this::handleAlert;
    private final LayoutAnimator topPaneAnimator = new LayoutAnimator();
    private final LayoutAnimator alertsAnimator = new LayoutAnimator();
    private final SimpleObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>();
    private final UUID uuid = UUID.randomUUID();

    private IsaacPreferences nodePreferences;

    private MultiParentGraphItemImpl rootTreeItem;
    private TreeView<ConceptChronology> treeView;
    private ViewProperties viewProperties;
    private SimpleObjectProperty<ActivityFeed> activityFeedProperty = new SimpleObjectProperty<>();

    private InvalidationListener manifoldChangedListener = this::manifoldChanged;
    private ChangeListener<Scene> sceneChangedListener = this::sceneChanged;

    private void sceneChanged(ObservableValue<? extends Scene> observableValue, Scene oldScene, Scene newScene) {
        if (newScene == null) {
            shutdownInstance();
            this.topBorderPane.sceneProperty().removeListener(this.sceneChangedListener);
            this.getManifoldCoordinate().removeListener(this.manifoldChangedListener);
        }
    }

    private void manifoldChanged(Observable observable) {
        this.refreshTaxonomy();
    }

    @FXML
    void initialize() {

        this.treeView = new TreeView<>();

        MenuItem generateGraphSource = new MenuItem("Generate graph source");
        generateGraphSource.setOnAction(this::generateJGraphTCode);

        this.treeView.setContextMenu(new ContextMenu(generateGraphSource));

        this.treeView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
        this.treeView.setCellFactory((TreeView<ConceptChronology> p) -> new MultiParentGraphCell(treeView));
        this.treeView.setShowRoot(false);
        this.rootTreeItem = new MultiParentGraphItemImpl(
                MultiParentGraphViewController.this);
        this.treeView.setRoot(rootTreeItem);

        // put this event handler on the root
        rootTreeItem.addEventHandler(
                TreeItem.<ConceptChronology>branchCollapsedEvent(),
                (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
                    ((MultiParentGraphItemImpl) t.getSource()).removeChildren();
                });
        rootTreeItem.addEventHandler(
                TreeItem.<ConceptChronology>branchExpandedEvent(),
                (TreeItem.TreeModificationEvent<ConceptChronology> t) -> {
                    MultiParentGraphItemImpl sourceTreeItem = (MultiParentGraphItemImpl) t.getSource();
                    if (sourceTreeItem.getChildren().isEmpty()) {
                        Get.executor()
                                .execute(() -> sourceTreeItem.addChildren());
                    }
                });
        Alert.addAlertListener(alertHandler);
        alertList.addListener(this::onChanged);


        this.topBorderPane.getStyleClass().setAll(MULTI_PARENT_TREE_NODE.toString());

        this.topBorderPane.setCenter(this.treeView);
        toolBar.getItems().add(this.navigationMultiChoiceBox);
    }

    @FXML
    void copySelectedConcepts(ActionEvent event) {
        List<IdentifiedObject> identifiedObjects = new ArrayList<>();
        for (TreeItem<ConceptChronology> conceptChronologyTreeItem: this.treeView.getSelectionModel().getSelectedItems()) {
            identifiedObjects.add(conceptChronologyTreeItem.getValue());
        }
        ClipboardHelper.copyToClipboard(identifiedObjects);
    }


    private void savePreferences() {
        // TODO selected graphConfigurationKey should be saved in preferences.
        this.nodePreferences.put(Keys.ACTIVITY_FEED, this.activityFeedProperty.get().getFullyQualifiedActivityFeedName());

    }
    private void updateMenus(Observable observable) {
        menuUpdate();
    }
    private void menuUpdate() {
        this.navigationMultiChoiceBox.getCheckModel().getCheckedItems().removeListener(this.navigationSelectionListener);
        this.navigationMultiChoiceBox.getItems().setAll(FxGet.navigationOptions());
        IndexedCheckModel<ConceptSpecification> checkModel = this.navigationMultiChoiceBox.getCheckModel();
        checkModel.clearChecks();
        ImmutableSet<ConceptSpecification> navConcepts = this.manifoldCoordinateWithOverride.getNavigationCoordinate().getNavigationIdentifierConcepts();
        for (ConceptSpecification navConcept: navConcepts) {
            checkModel.check(checkModel.getItemIndex(navConcept));
        }

        this.navigationCoordinateMenu.getItems().clear();
        FxGet.makeCoordinateDisplayMenu(this.manifoldCoordinateWithOverride,
                this.navigationCoordinateMenu.getItems(),
                this.manifoldCoordinateWithOverride);
        this.navigationMultiChoiceBox.getCheckModel().getCheckedItems().addListener(this.navigationSelectionListener);
    }

    private void navigationSelectionChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        ImmutableSet<ConceptSpecification> newNavigationConcepts = Sets.immutable.withAll(c.getList());
        SetProperty<ConceptSpecification> navigationConcepts = this.manifoldCoordinateWithOverride.getNavigationCoordinate().navigatorIdentifierConceptsProperty();
        navigationConcepts.clear();
        navigationConcepts.addAll(newNavigationConcepts.castToSet());
        refreshTaxonomy();
    }

    public void setProperties(ViewProperties viewProperties, IsaacPreferences nodePreferences) {
        this.nodePreferences = nodePreferences;
        this.viewProperties = viewProperties;
        this.manifoldCoordinateWithOverride = new ObservableManifoldCoordinateWithOverride(this.viewProperties.getManifoldCoordinate());
        this.navigationMultiChoiceBox.setConverter(new StringConverter<ConceptSpecification>() {
            @Override
            public String toString(ConceptSpecification object) {
                return viewProperties.getPreferredDescriptionText(object);
            }

            @Override
            public ConceptSpecification fromString(String string) {
                return null;
            }
        });
        this.menuUpdate();
        FxGet.pathCoordinates().addListener(this::updateMenus);
        this.manifoldCoordinateWithOverride.addListener(this::updateMenus);

        String activityFeedKey = nodePreferences.get(Keys.ACTIVITY_FEED, this.viewProperties.getViewUuid() + ":" + ViewProperties.NAVIGATION);
        this.activityFeedProperty.set(this.viewProperties.getActivityFeed(activityFeedKey));
        this.treeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<ConceptChronology>>) c -> {
            ActivityFeed activityFeed = this.activityFeedProperty.get();
            while (c.next()) {
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //nothing to do...
                    }
                } else if (c.wasUpdated()) {
                    //nothing to do
                } else {
                    for (TreeItem<ConceptChronology> remitem : c.getRemoved()) {
                        activityFeed.feedSelectionProperty().remove(new ComponentProxy(remitem.getValue().toExternalString()));
                    }
                    for (TreeItem<ConceptChronology> additem : c.getAddedSubList()) {
                        activityFeed.feedSelectionProperty().add(new ComponentProxy(additem.getValue().toExternalString()));
                    }
                }
            }
            // Check to make sure lists are equal in size/properly synchronized.
            if (activityFeed.feedSelectionProperty().size() != c.getList().size()) {
                // lists are out of sync, reset with fresh list.
                ComponentProxy[] selectedItems = new ComponentProxy[c.getList().size()];
                for (int i = 0; i < selectedItems.length; i++) {
                    selectedItems[i] = new ComponentProxy(c.getList().get(i).getValue().toExternalString());
                }
                activityFeed.feedSelectionProperty().setAll(selectedItems);
            }
        });

        this.displayPolicies = new DefaultMultiParentGraphItemDisplayPolicies(this.getManifoldCoordinate());



        this.topPaneAnimator.observe(topGridPane);
        this.topBorderPane.setTop(topGridPane);
        this.alertsAnimator.observe(this.topBorderPane.getChildren());
        handleDescriptionTypeChange(null);

        setupTopPane();
        // Not a leak, since the taxonomy service adds a weak reference to the listener.
        Get.taxonomyService().addTaxonomyRefreshListener(this);

        this.topBorderPane.setOnDragOver(this::dragOver);
        this.topBorderPane.setOnDragDropped(this::dragDropped);
        refreshTaxonomy();
        this.getManifoldCoordinate().addListener(this.manifoldChangedListener);
        this.topBorderPane.sceneProperty().addListener(this.sceneChangedListener);
    }



    public ObservableManifoldCoordinate getManifoldCoordinate() {
        return this.manifoldCoordinateWithOverride;
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

    @Override
    public void refresh() {
        Platform.runLater(() -> {
            this.refreshTaxonomy();
        });
    }

    /**
     * Tell the tree to stop whatever threading operations it has running, since
     * the application is exiting.
     *
     */
    public static void globalShutdownRequested() {
        shutdownRequested = true;
        LOG.info("Global Navigator shutdown called!");
    }

    @Override
    public UUID getListenerUuid() {
        return this.uuid;
    }

    public void showConcept(final UUID conceptUUID) {
        // Do work in background.
        ShowConceptInGraphTask task
                = new ShowConceptInGraphTask(this, conceptUUID);

        Get.executor()
                .execute(task);
    }

    protected void shutdownInstance() {
        LOG.info("Shutdown graph view instance");
        this.getManifoldCoordinate().removeListener(this.manifoldChangedListener);
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
     * @param item
     * @param targetChildUUID
     * @return the found child, or null, if not found. found child will have
     * already been told to expand and fetch its children.
     * @throws InterruptedException
     */
    protected MultiParentGraphItemImpl findChild(final MultiParentGraphItemImpl item,
                                                 final UUID targetChildUUID)
            throws InterruptedException {
        LOG.debug("Looking for {}", targetChildUUID);

        SimpleObjectProperty<MultiParentGraphItemImpl> found = new SimpleObjectProperty<>(null);

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
                    found.set((MultiParentGraphItemImpl) child);
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
                                SimpleObjectProperty<MultiParentGraphItemImpl> scrollTo = new SimpleObjectProperty<>();
                                if (scrollTo.get() != null) {

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
                                }
                            } catch (InterruptedException e) {
                                LOG.info("Interrupted while looking restoring expanded items");
                            }
                        });
    }

    private void restoreExpanded(MultiParentGraphItemImpl item,
                                 SimpleObjectProperty<MultiParentGraphItemImpl> scrollTo)
            throws InterruptedException {
        if (expandedUUIDs.contains(item.getConceptUuid())) {
            item.addChildren();
            item.blockUntilChildrenReady();
            Platform.runLater(() -> item.setExpanded(true));

            List<TreeItem<ConceptChronology>> list = new ArrayList<>(item.getChildren());

            for (TreeItem<ConceptChronology> child : list) {
                restoreExpanded((MultiParentGraphItemImpl) child, scrollTo);
            }
        }

        if (selectedItem.isPresent() && selectedItem.get().equals(item.getConceptUuid())) {
            scrollTo.set(item);
        }
    }

    public void expandAndSelect(ArrayList<UUID> expansionPath) {
        boolean foundRoot = false;
        for (TreeItem<ConceptChronology> rootConcept: rootTreeItem.getChildren()) {
            MultiParentGraphItemImpl viewRoot = (MultiParentGraphItemImpl) rootConcept;
            if (viewRoot.getConceptUuid().equals(expansionPath.get(0))) {
                foundRoot = true;
                viewRoot.addChildrenNow();
                viewRoot.setExpanded(true);
                Platform.runLater(new MultiParentGraphViewController.ExpandTask(viewRoot, expansionPath, 1));
            }
        }
        if (!foundRoot) {
            FxGet.statusMessageService().reportStatus("Expansion path for concept ends at: "
                    + Get.conceptDescriptionText(Get.nidForUuids(expansionPath.get(0))));
        }
    }

    private class ExpandTask extends TimedTaskWithProgressTracker<Void> {

        final ArrayList<UUID> expansionPath;
        final int pathIndex;
        final MultiParentGraphItemImpl currentItem;

        public ExpandTask(MultiParentGraphItemImpl currentItem, ArrayList<UUID> expansionPath, int pathIndex) {
            this.currentItem = currentItem;
            this.expansionPath = expansionPath;
            this.pathIndex = pathIndex;
        }

        @Override
        protected Void call() throws Exception {
            treeView.scrollTo(treeView.getRow(currentItem));
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(currentItem);
            if (pathIndex < expansionPath.size()) {
                UUID childUuidToMatch = expansionPath.get(pathIndex);
                for (TreeItem child : currentItem.getChildren()) {
                    MultiParentGraphItemImpl childItem = (MultiParentGraphItemImpl) child;
                    if (childItem.getConceptUuid().equals(childUuidToMatch)) {
                        childItem.addChildrenNow();
                        currentItem.setExpanded(true);
                        Platform.runLater(new MultiParentGraphViewController.ExpandTask(childItem, expansionPath, pathIndex + 1));
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

    private void saveExpanded(MultiParentGraphItemImpl item) {
        if (!item.isLeaf() && item.isExpanded()) {
            expandedUUIDs.add(item.getConceptUuid());

            if (!item.isLeaf()) {
                for (TreeItem<ConceptChronology> child : item.getChildren()) {
                    saveExpanded((MultiParentGraphItemImpl) child);
                }
            }
        }
    }

    private void setupTopPane() {

        // Node child, int columnIndex, int rowIndex, int columnspan, int rowspan,
        // HPos halignment, VPos valignment, Priority hgrow, Priority vgrow
        topPaneAnimator.unobserve(topGridPane.getChildren());
        topGridPane.getChildren().clear();
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

    public final void generateSmartGraphCode(ActionEvent event) {
        TreeItem<ConceptChronology> item  = this.treeView.getSelectionModel().getSelectedItem();
        ConceptChronology concept = item.getValue();
        Navigator navigator = navigatorProperty.get();
        OpenIntHashSet conceptNids = new OpenIntHashSet();
        HashMap<Integer, ArrayList<Edge>> taxonomyLinks = new HashMap<>();
        handleConcept(concept.getNid(), navigator, conceptNids, taxonomyLinks);
        String conceptName = Get.conceptDescriptionText(concept.getNid());
        conceptName = conceptName.replaceAll("\\s+", "_");
        conceptName = conceptName.replaceAll("-", "_");
        StringBuffer buff = new StringBuffer("private Graph<String, String> build_" + conceptName + "() {\n");
        buff.append("\n   Graph<String, String> g = new GraphEdgeList<>();\n\n");
        ManifoldCoordinate m = this.getManifoldCoordinate();
        conceptNids.forEachKey(nid -> {

            buff.append("   g.insertVertex(\"").append(m.getPreferredDescriptionText(nid)).append("\");\n");
            return true;
        });
        buff.append("\n");
        int edgeCount = 1;
        for (Map.Entry<Integer, ArrayList<Edge>> entry: taxonomyLinks.entrySet()) {
            for (Edge link: entry.getValue()) {
                buff.append("   g.insertEdge(\"").append(m.getPreferredDescriptionText(entry.getKey())).append("\", \"")
                        .append(m.getPreferredDescriptionText(link.getDestinationNid())).append("\", \"").append(edgeCount++).append("\");\n");
            }
        }
        buff.append("   return g;\n}\n");
        ClipboardHelper.copyToClipboard(buff);
        LOG.info(event);
    }
    public final void generateJGraphTCode(ActionEvent event) {
        TreeItem<ConceptChronology> item  = this.treeView.getSelectionModel().getSelectedItem();
        ConceptChronology concept = item.getValue();
        Navigator navigator = navigatorProperty.get();
        OpenIntHashSet conceptNids = new OpenIntHashSet();
        HashMap<Integer, ArrayList<Edge>> taxonomyLinks = new HashMap<>();
        handleConcept(concept.getNid(), navigator, conceptNids, taxonomyLinks);
        String conceptName = Get.conceptDescriptionText(concept.getNid());
        conceptName = conceptName.replaceAll("\\s+", "_");
        conceptName = conceptName.replaceAll("-", "_");
        conceptName = conceptName.replace('(', '_');
        conceptName = conceptName.replace(')', '_');
        StringBuffer buff = new StringBuffer("private static Graph<String, DefaultEdge> build_" + conceptName + "() {\n");
        buff.append("\n   Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);\n\n");
        ManifoldCoordinate m = this.getManifoldCoordinate();
        conceptNids.forEachKey(nid -> {

            buff.append("   g.addVertex(\"\\\"").append(m.getPreferredDescriptionText(nid)).append("\\\"\");\n");
            return true;
        });
        buff.append("\n");
        int edgeCount = 1;
        for (Map.Entry<Integer, ArrayList<Edge>> entry: taxonomyLinks.entrySet()) {
            for (Edge link: entry.getValue()) {
                buff.append("   g.addEdge(\"\\\"").append(m.getPreferredDescriptionText(entry.getKey())).append("\\\"\", \"\\\"")
                        .append(m.getPreferredDescriptionText(link.getDestinationNid())).append("\\\"\");\n");
            }
        }
        buff.append("   return g;\n}\n");
        ClipboardHelper.copyToClipboard(buff);
        LOG.info(event);
    }

    private void handleConcept(int conceptNid, Navigator navigator, OpenIntHashSet conceptNids, HashMap<Integer, ArrayList<Edge>> taxonomyLinks) {
        if (!conceptNids.contains(conceptNid)) {
            conceptNids.add(conceptNid);
            ArrayList<Edge> linkList = new ArrayList<>();
            taxonomyLinks.put(conceptNid, linkList);
            for (Edge link: navigator.getParentLinks(conceptNid)) {
                if (link.getTypeNid() == TermAux.IS_A.getNid()) {
                    linkList.add(link);
                }
                handleConcept(link.getDestinationNid(), navigator, conceptNids, taxonomyLinks);
            }
        }
    }


    public final void handleDescriptionTypeChange(ActionEvent event) {
        this.rootTreeItem.invalidate();
        this.treeView.refresh();
    }

    private void refreshTaxonomy() {
        saveExpanded();
        Navigator navigator = new EmptyNavigator(this.getManifoldCoordinate());
        try {
            navigator = Get.navigationService().getNavigator(this.getManifoldCoordinate());
        } catch (IllegalStateException ex) {
            FxGet.dialogs().showErrorDialog("Do you have more that one premise type selected?", ex);
        }
        this.navigatorProperty.set(navigator);
        this.rootTreeItem.clearChildren();
        for (int rootNid: this.navigatorProperty.get().getRootNids()) {
            MultiParentGraphItemImpl graphRoot = new MultiParentGraphItemImpl(
                    Get.conceptService()
                            .getConceptChronology(rootNid),
                    MultiParentGraphViewController.this,
                    TermAux.UNINITIALIZED_COMPONENT_ID.getNid(),
                    Iconography.TAXONOMY_ROOT_ICON.getIconographic());
            this.rootTreeItem.getChildren().add(graphRoot);
        }
        for (TreeItem<ConceptChronology> rootChild: this.rootTreeItem.getChildren()) {
            ((MultiParentGraphItemImpl) rootChild).clearChildren();
            Get.workExecutors().getExecutor().execute(() -> ((MultiParentGraphItemImpl) rootChild).addChildren());
        }

        this.rootTreeItem.invalidate();
        this.alertList.clear();
        restoreExpanded();
    }

    //~--- get methods ---------------------------------------------------------
    public MultiParentGraphItemDisplayPolicies getDisplayPolicies() {
        return displayPolicies;
    }

    //~--- set methods ---------------------------------------------------------
    public void setDisplayPolicies(MultiParentGraphItemDisplayPolicies policies) {
        this.displayPolicies = policies;
    }

    //~--- get methods ---------------------------------------------------------

    public MultiParentGraphItemImpl getRoot() {
        return rootTreeItem;
    }

    protected Navigator getNavigator() {
        return navigatorProperty.get();
    }

    public BorderPane getView() {
        return topBorderPane;
    }

    public TreeView<ConceptChronology> getTreeView() {
        return treeView;
    }


}

