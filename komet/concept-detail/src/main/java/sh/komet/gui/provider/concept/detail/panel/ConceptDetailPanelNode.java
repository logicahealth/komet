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
package sh.komet.gui.provider.concept.detail.panel;

//~--- JDK imports ------------------------------------------------------------

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.*;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.komet.gui.control.ExpandControl;
import sh.komet.gui.control.StampControl;
import sh.komet.gui.control.badged.ComponentPaneModel;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.control.toggle.OnOffToggleSwitch;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.provider.concept.builder.ConceptBuilderComponentPanel;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.style.StyleClasses.ADD_DESCRIPTION_BUTTON;
import static sh.komet.gui.util.FxUtils.setupHeaderPanel;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptDetailPanelNode extends DetailNodeAbstract
implements ChronologyChangeListener, Supplier<List<MenuItem>> {

    private static final Logger LOG = LogManager.getLogger();

    private static final int TRANSITION_OFF_TIME = 250;
    private static final int TRANSITION_ON_TIME = 300;

    //~--- fields --------------------------------------------------------------
    private final HashMap<String, AtomicBoolean> disclosureStateMap = new HashMap<>();
    private final UUID listenerUuid = UUID.randomUUID();
    {
        titleProperty.setValue("empty");
        toolTipProperty.setValue("empty");
        menuIconProperty.setValue(Iconography.CONCEPT_DETAILS.getIconographic());
    }

    private final VBox componentPanelBox = new VBox(8);
    private final GridPane versionBranchGrid = new GridPane();
    private final GridPane toolGrid = new GridPane();
    private final ExpandControl expandControl = new ExpandControl();
    private final OnOffToggleSwitch historySwitch = new OnOffToggleSwitch();
    private final Label expandControlLabel = new Label("Expand All", expandControl);
    private final OpenIntIntHashMap stampOrderHashMap = new OpenIntIntHashMap();
    private final Button addDescriptionButton = new Button("+ Add");
    private final ToggleButton versionGraphToggle = new ToggleButton("", Iconography.SOURCE_BRANCH_1.getIconographic());
    private final ArrayList<Integer> sortedStampSequences = new ArrayList<>();
    private final List<ComponentPaneModel> componentPaneModels = new ArrayList<>();
    private final ScrollPane scrollPane;


    private final ObservableList<ObservableDescriptionDialect> newDescriptions = FXCollections.observableArrayList();

    //~--- initializers --------------------------------------------------------
    {
        expandControlLabel.setGraphicTextGap(0);
    }

    //~--- constructors --------------------------------------------------------
    public ConceptDetailPanelNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferences) {
        super(viewProperties, activityFeed, preferences);

        this.historySwitch.setSelected(false); // add to pref...
        updateManifoldHistoryStates();
        this.detailPane.getStyleClass()
                .add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        this.scrollPane = new ScrollPane(componentPanelBox);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.detailPane.setCenter(this.scrollPane);
        this.versionBranchGrid.add(versionGraphToggle, 0, 0);
        this.versionGraphToggle.getStyleClass()
                .setAll(StyleClasses.VERSION_GRAPH_TOGGLE.toString());
        this.versionGraphToggle.selectedProperty()
                .addListener(this::toggleVersionGraph);
        this.detailPane.setLeft(versionBranchGrid);
        this.componentPanelBox.getStyleClass()
                .add(StyleClasses.COMPONENT_DETAIL_BACKGROUND.toString());
        this.componentPanelBox.setFillWidth(true);
        setupToolGrid();
        this.historySwitch.selectedProperty()
                .addListener(this::setShowHistory);

        this.expandControl.expandActionProperty()
                .addListener(this::expandAllAction);

        // commit service uses weak change listener references, so this method call is not a leak.
        Get.commitService().addChangeListener(this);

        Optional<IdentifiedObject> optionalFocus = this.activityFeedProperty.get().getOptionalFocusedComponent(selectionIndexProperty.get());
        if (optionalFocus.isPresent()) {
            titleProperty.set(this.viewProperties.getPreferredDescriptionText(optionalFocus.get().getNid()));
        } else {
            titleProperty.set(ConceptLabelWithDragAndDrop.EMPTY_TEXT);
        }

        this.savePreferences();
        Platform.runLater(() ->  resetConceptFromFocus());

    }


    @Override
    public Node getMenuIconGraphic() {
        return Iconography.CONCEPT_DETAILS.getIconographic();
    }

    @Override
    public void savePreferences() {
        Optional<IdentifiedObject> optionalFocus = this.getFocusedObject();
        if (optionalFocus.isPresent()) {
            this.preferences.putInt(Keys.ACTIVITY_SELECTION_INDEX, this.selectionIndexProperty.getValue());
        }
       this.preferences.put(Keys.ACTIVITY_FEED_NAME, this.getActivityFeed().getFullyQualifiedActivityFeedName());
        try {
            this.preferences.sync();
        } catch (BackingStoreException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void handleChange(ConceptChronology cc) {
        // ignore uncommitted changes...
    }

    @Override
    public Node getNode() {
        return this.detailPane;
    }

    @Override
    public void handleChange(SemanticChronology sc) {
        // ignore uncommitted changes...
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        Optional<IdentifiedObject> optionalFocus = this.getFocusedObject();
        if (optionalFocus.isPresent()) {
            ConceptChronology focusedConcept = Get.concept(optionalFocus.get().getNid());
            ImmutableIntSet recursiveSemantics = focusedConcept.getRecursiveSemanticNids();

            final Runnable runnable = () -> {
                resetConceptFromFocus();
            };
            if (commitRecord.getConceptsInCommit()
                    .contains(optionalFocus.get().getNid())) {
                Platform.runLater(runnable);
            } else {
                MutableIntSet semanticSet = recursiveSemantics.toSet();
                semanticSet.retainAll(commitRecord.getSemanticNidsInCommit().asArray());
                if (!semanticSet.isEmpty()) {
                    Platform.runLater(runnable);
                }
            }
        }
    }

    private void addChronology(ObservableChronology observableChronology, ParallelTransition parallelTransition) {
        if (ComponentPaneModel.isSemanticTypeSupported(observableChronology.getVersionType())) {
            CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
                    = observableChronology.getCategorizedVersions(
                            this.viewProperties.getManifoldCoordinate().getStampFilter());

            if (oscCategorizedVersions.getLatestVersion()
                    .isPresent()) {
                parallelTransition.getChildren()
                        .add(addComponent(oscCategorizedVersions));
            }
        }
    }

    private Animation addComponent(ConceptBuilderComponentPanel panel) {
        return this.addComponent(panel, new Insets(1, 5, 1, 5));
    }

    private Animation addComponent(ConceptBuilderComponentPanel panel, Insets insets) {

        panel.setOpacity(0);
        VBox.setMargin(panel, insets);
        VBox.setVgrow(panel, Priority.NEVER);
        componentPanelBox.getChildren()
                .add(panel);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), panel);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private Animation addComponent(CategorizedVersions<ObservableCategorizedVersion> categorizedVersions) {
        ObservableCategorizedVersion categorizedVersion;

        if (categorizedVersions.getLatestVersion()
                .isPresent()) {
            categorizedVersion = categorizedVersions.getLatestVersion()
                    .get();
        } else if (!categorizedVersions.getUncommittedVersions()
                .isEmpty()) {
            categorizedVersion = categorizedVersions.getUncommittedVersions()
                    .get(0);
        } else {
            throw new IllegalStateException(
                    "Categorized version has no latest version or uncommitted version: \n" + categorizedVersions);
        }

        ComponentPaneModel componentPaneModel = new ComponentPaneModel(this.viewProperties, categorizedVersion,
                stampOrderHashMap, disclosureStateMap);

        componentPaneModels.add(componentPaneModel);
        componentPaneModel.getBadgedPane().setOpacity(0);
        VBox.setMargin(componentPaneModel.getBadgedPane(), new Insets(1, 5, 1, 5));
        VBox.setVgrow(componentPaneModel.getBadgedPane(), Priority.NEVER);
        componentPanelBox.getChildren()
                .add(componentPaneModel.getBadgedPane());

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), componentPaneModel.getBadgedPane());

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private Animation addNode(Node headerNode) {
        headerNode.setOpacity(0);
        VBox.setMargin(headerNode, new Insets(1, 5, 1, 5));
        componentPanelBox.getChildren()
                .add(headerNode);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), headerNode);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }
    
    private void handleCommit(ObservableDescriptionDialect observableDescriptionDialect, 
            ObservableVersion[] versionsToCommit) {
        Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE);
        CommitTask commitTask = transaction.commitObservableVersions("", versionsToCommit);
        Get.executor().execute(() -> {
            try {
                Optional<CommitRecord> commitRecord = commitTask.get();
                //completeCommit(commitTask, commitRecord);
            } catch (InterruptedException | ExecutionException ex) {
                FxGet.dialogs().showErrorDialog("Error during commit", ex);
            }
        });
    }
    private void animateLayout() {
        componentPanelBox.getChildren().clear();

        AtomicBoolean axiomHeaderAdded = new AtomicBoolean(false);
        populateVersionBranchGrid();
        componentPanelBox.getChildren().add(toolGrid);

        Optional<IdentifiedObject> focusedConceptSpec = this.getFocusedObject();

        if (focusedConceptSpec.isPresent()) {
            ConceptChronology newValue = Get.concept(focusedConceptSpec.get().getNid());
            if (titleLabel == null) {
                titleProperty.set(this.viewProperties.getPreferredDescriptionText(newValue));
            } else {
                titleProperty.set(this.titleLabel.getText());
            }

            toolTipProperty.set(
                    "concept details for: " + this.viewProperties.getFullyQualifiedDescriptionText(newValue));

            ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                    .getObservableConceptChronology(
                            newValue.getNid());
            final ParallelTransition parallelTransition = new ParallelTransition();

            addChronology(observableConceptChronology, parallelTransition);

            /* TODO finish lineage view
            AnchorPane lineageHeader = setupHeaderPanel("LINEAGE", null);
            parallelTransition.getChildren()
                    .add(addNode(lineageHeader));

            parallelTransition.getChildren()
                    .add(addNode(LineageTree.makeLineageTree(newValue, this.manifoldProperty.get())));
            */
            AnchorPane descriptionHeader = setupHeaderPanel("DESCRIPTIONS", addDescriptionButton);

            addDescriptionButton.getStyleClass()
                    .setAll(ADD_DESCRIPTION_BUTTON.toString());

            addDescriptionButton.setOnAction(this::newDescription);
            descriptionHeader.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
            parallelTransition.getChildren()
                    .add(addNode(descriptionHeader));

            Iterator<ObservableDescriptionDialect> iter = newDescriptions.iterator();
            while (iter.hasNext()) {
                ObservableDescriptionDialect descDialect = iter.next();
                if (descDialect.getCommitState() == CommitStates.UNCOMMITTED) {
                    ConceptBuilderComponentPanel descPanel = new ConceptBuilderComponentPanel(this.viewProperties,
                            descDialect, true, null);
                    parallelTransition.getChildren().add(addComponent(descPanel));
                    descPanel.setCommitHandler((event) -> {
                        newDescriptions.remove(descDialect);
                        this.handleCommit(descDialect, descPanel.getVersionsToCommit());
                        clearComponents();
                    });
                    descPanel.setCancelHandler((event) -> {
                        newDescriptions.remove(descDialect);
                        clearComponents();
                    });
                    
                } else {
                    iter.remove();
                }
            }
            // Sort them...
            observableConceptChronology.getObservableSemanticList()
                    .filtered((semanticChronology) -> {
                        switch (semanticChronology.getVersionType()) {
                            case DESCRIPTION:
                            case LOGIC_GRAPH:
                                if (historySwitch.isSelected()) {
                                    return true;
                                } else {
                                    LatestVersion<SemanticVersion> latest
                                            = semanticChronology.getLatestVersion(
                                            this.viewProperties.getManifoldCoordinate().getStampFilter());

                                    if (latest.isPresent()) {
                                        return latest.get()
                                                .getStatus() == Status.ACTIVE;
                                    }
                                }
                            default:
                                return false;
                        }
                    })
                    .sorted(
                            (o1, o2) -> {
                                switch (o1.getVersionType()) {
                                    case DESCRIPTION:
                                        if (o2.getVersionType() == VersionType.DESCRIPTION) {
                                            DescriptionVersion dv1 = (DescriptionVersion) o1.getVersionList()
                                                    .get(0);
                                            DescriptionVersion dv2 = (DescriptionVersion) o2.getVersionList()
                                                    .get(0);

                                            if (dv1.getDescriptionTypeConceptNid()
                                            == dv2.getDescriptionTypeConceptNid()) {
                                                return 0;
                                            }

                                            if (dv1.getDescriptionTypeConceptNid()
                                            == MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid()) {
                                                return -1;
                                            }

                                            return 1;
                                        }

                                        return -1;

                                    case LOGIC_GRAPH:
                                        if (o2.getVersionType() == VersionType.LOGIC_GRAPH) {
                                            if (o1.getAssemblageNid() == o2.getAssemblageNid()) {
                                                return 0;
                                            }

                                            if (o1.getAssemblageNid()
                                            == this.viewProperties.getManifoldCoordinate().getLogicCoordinate().getInferredAssemblageNid()) {
                                                return -1;
                                            }

                                            return 1;
                                        }

                                        return 1;
                                }

                                return 0;  // others already filtered out...
                            })
                    .forEach(
                            (osc) -> {
                                if (osc.getVersionType() == VersionType.LOGIC_GRAPH && !axiomHeaderAdded.get()) {
                                    axiomHeaderAdded.set(true);
                                    AnchorPane axiomHeader = setupHeaderPanel("AXIOMS", null);
                                    axiomHeader.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
                                    parallelTransition.getChildren()
                                            .add(addNode(axiomHeader));
                                }
                                addChronology(osc, parallelTransition);
                            });
            parallelTransition.play();
        }
    }

    private void newDescription(Event event) {
        Optional<IdentifiedObject> optionalFocus = this.getFocusedObject();
        if (optionalFocus.isPresent()) {
            ObservableDescriptionDialect newDescriptionDialect
                    = new ObservableDescriptionDialect(optionalFocus.get().getPrimordialUuid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
            newDescriptions.add(newDescriptionDialect);
            newDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
            newDescriptionDialect.getDescription().setStatus(Status.ACTIVE);
            newDescriptionDialect.getDialect().setStatus(Status.ACTIVE);
            clearComponents();
        }
    }

    private void clearComponents() {
        final ParallelTransition parallelTransition = new ParallelTransition();

        componentPanelBox.getChildren()
                .forEach(
                        (child) -> {
                            if (toolGrid != child) {
                                FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_OFF_TIME), child);

                                ft.setFromValue(1.0);
                                ft.setToValue(0.0);
                                parallelTransition.getChildren()
                                        .add(ft);
                            }
                        });
        versionBranchGrid.getChildren()
                .forEach(
                        (child) -> {
                            if (versionGraphToggle != child) {
                                FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_OFF_TIME), child);

                                ft.setFromValue(1.0);
                                ft.setToValue(0.0);
                                parallelTransition.getChildren()
                                        .add(ft);
                            }
                        });
        //parallelTransition.setOnFinished(this::clearAnimationComplete);
        parallelTransition.play();
    }

    private void expandAllAction(ObservableValue<? extends ExpandAction> observable,
            ExpandAction oldValue,
            ExpandAction newValue) {
        componentPaneModels.forEach((componentPaneModel) -> componentPaneModel.doExpandAllAction(newValue));
    }

    private void populateVersionBranchGrid() {
        versionBranchGrid.getChildren()
                .clear();
        versionBranchGrid.add(versionGraphToggle, 0, 0);

        if (versionGraphToggle.isSelected()) {
            for (int stampOrder = 0; stampOrder < sortedStampSequences.size(); stampOrder++) {
                StampControl stampControl = new StampControl();
                int stampSequence = sortedStampSequences.get(stampOrder);
                stampControl.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, !Get.stampService().isStampActive(stampSequence));

                stampControl.setStampedVersion(stampSequence, this.viewProperties, stampOrder + 1);
                versionBranchGrid.add(stampControl, 0, stampOrder + 2);
            }
        }
    }

    private void setupToolGrid() {
        GridPane.setConstraints(
                expandControlLabel,
                0,
                0,
                1,
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(expandControlLabel);

        Pane spacer = new Pane();

        GridPane.setConstraints(
                spacer,
                1,
                0,
                1,
                1,
                HPos.CENTER,
                VPos.CENTER,
                Priority.ALWAYS,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(spacer);

        Label historySwitchWithLabel = new Label("History", historySwitch);

        historySwitchWithLabel.setContentDisplay(ContentDisplay.RIGHT);
        GridPane.setConstraints(
                historySwitchWithLabel,
                2,
                0,
                1,
                1,
                HPos.RIGHT,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(historySwitchWithLabel);
        componentPanelBox.getChildren()
                .add(toolGrid);
    }

    private void toggleVersionGraph(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resetConceptFromFocus();
    }

    private void resetConceptFromFocus() {
        if (getFocusedObject().isPresent()) {
            setConcept(Get.concept(getFocusedObject().get().getNid()));
        } else {
            setConcept((ConceptSpecification) null);
        }
    }

    private void updateManifoldHistoryStates() {
        if (historySwitch.isSelected()) {
//            this.manifoldProperty.get().getStampCoordinate()
//                    .allowedStatesProperty()
//                    .clear();
//            this.manifoldProperty.get().getStampCoordinate()
//                    .allowedStatesProperty()
//                    .addAll(Status.makeActiveAndInactiveSet());
        } else {
//            this.manifoldProperty.get().getStampCoordinate()
//                    .allowedStatesProperty()
//                    .clear();
//            this.manifoldProperty.get().getStampCoordinate()
//                    .allowedStatesProperty()
//                    .addAll(Status.makeActiveOnlySet());
        }
    }

    private void updateStampControls(Chronology chronology, PrefetchTask prefetchTask) {
        if (chronology == null) {
            return;
        }
        for (int stampSequence : chronology.getVersionStampSequences()) {
            stampOrderHashMap.put(stampSequence, 0);
        }
        Get.assemblageService().getSemanticNidsForComponent(chronology.getNid()).forEach(nid -> {
            updateStampControls(prefetchTask.getChronology(nid), prefetchTask);
        });
    }

    //~--- set methods ---------------------------------------------------------


    @Override
    public void updateFocusedObject(IdentifiedObject component) {
        Platform.runLater(() -> setConcept(component));
    }

    private void setConcept(IdentifiedObject component) {
        Optional<PrefetchTask> optionalPrefetchTask = Optional.empty();
        if (component != null) {
            PrefetchTask prefetchTask = new PrefetchTask(component.getNid());
            Get.executor().submit(prefetchTask);
            optionalPrefetchTask = Optional.of(prefetchTask);
        }

        clearComponents();

        this.stampOrderHashMap.clear();
        this.componentPaneModels.clear();

        if (optionalPrefetchTask.isPresent()) {
            ConceptChronology newValue;
            if (component instanceof ConceptChronology) {
                newValue = (ConceptChronology) component;
            } else {
                newValue = Get.concept(component.getNid());
            }
            updateStampControls(newValue, optionalPrefetchTask.get());
        }

        IntArrayList stampSequences = this.stampOrderHashMap.keys();
        this.sortedStampSequences.clear();
        this.sortedStampSequences.addAll(stampSequences.toList());

        StampService stampService = Get.stampService();

        this.sortedStampSequences.sort(
                (o1, o2) -> {
                    return stampService.getInstantForStamp(o2)
                            .compareTo(stampService.getInstantForStamp(o1));
                });

        final AtomicInteger stampOrder = new AtomicInteger();

        this.sortedStampSequences.forEach((stampSequence) -> {
            this.stampOrderHashMap.put(stampSequence, stampOrder.incrementAndGet());
        });
        populateVersionBranchGrid();
        updateManifoldHistoryStates();
        animateLayout();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public UUID getListenerUuid() {
        return listenerUuid;
    }

    //~--- set methods ---------------------------------------------------------
    private void setShowHistory(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resetConceptFromFocus();
    }

    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        // No extra menu items added yet. 
        return assemblageMenuList;
    }

    @Override
    public boolean selectInTabOnChange() {
        return this.conceptLabelToolbar.getFocusTabOnConceptChange().get();
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }

}
