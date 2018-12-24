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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;

import javafx.application.Platform;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.event.Event;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import org.controlsfx.control.ToggleSwitch;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.control.badged.ComponentPaneModel;
import sh.komet.gui.control.badged.old.ComponentPane;
import sh.komet.gui.control.concept.ManifoldLinkedConceptLabel;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.control.ExpandControl;
import sh.komet.gui.control.StampControl;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

import static sh.komet.gui.style.StyleClasses.ADD_DESCRIPTION_BUTTON;
import static sh.komet.gui.util.FxUtils.setupHeaderPanel;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.komet.gui.provider.concept.builder.ConceptBuilderComponentPanel;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptDetailPanelNode
        implements DetailNode, ChronologyChangeListener, Supplier<List<MenuItem>> {

    private static final Logger LOG = LogManager.getLogger();

    private static final int TRANSITION_OFF_TIME = 250;
    private static final int TRANSITION_ON_TIME = 750;

    //~--- fields --------------------------------------------------------------
    private final UUID listenerUuid = UUID.randomUUID();
    private final BorderPane conceptDetailPane = new BorderPane();
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("empty");
    private final VBox componentPanelBox = new VBox(8);
    private final GridPane versionBrancheGrid = new GridPane();
    private final GridPane toolGrid = new GridPane();
    private final ExpandControl expandControl = new ExpandControl();
    private final ToggleSwitch historySwitch = new ToggleSwitch();
    private final Label expandControlLabel = new Label("Expand All", expandControl);
    private final OpenIntIntHashMap stampOrderHashMap = new OpenIntIntHashMap();
    private final Button addDescriptionButton = new Button("+ Add");
    private final ToggleButton versionGraphToggle = new ToggleButton("", Iconography.SOURCE_BRANCH_1.getIconographic());
    private final ArrayList<Integer> sortedStampSequences = new ArrayList<>();
    private final List<ComponentPaneModel> componentPaneModels = new ArrayList<>();
    private ManifoldLinkedConceptLabel titleLabel = null;
    private final Manifold conceptDetailManifold;
    private final ScrollPane scrollPane;
    private final ConceptLabelToolbar conceptLabelToolbar;

    private final ObservableList<ObservableDescriptionDialect> newDescriptions = FXCollections.observableArrayList();

    //~--- initializers --------------------------------------------------------
    {
        expandControlLabel.setGraphicTextGap(0);
    }

    //~--- constructors --------------------------------------------------------
    public ConceptDetailPanelNode(Manifold conceptDetailManifold) {
        this.conceptDetailManifold = conceptDetailManifold;
        this.historySwitch.setSelected(false);
        updateManifoldHistoryStates();
        conceptDetailManifold.focusedConceptProperty()
                .addListener(this::setConcept);
        this.conceptLabelToolbar = ConceptLabelToolbar.make(conceptDetailManifold, this, Optional.of(true));
        this.conceptDetailPane.setTop(this.conceptLabelToolbar.getToolbarNode());
        this.conceptDetailPane.getStyleClass()
                .add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        this.scrollPane = new ScrollPane(componentPanelBox);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.conceptDetailPane.setCenter(this.scrollPane);
        this.versionBrancheGrid.add(versionGraphToggle, 0, 0);
        this.versionGraphToggle.getStyleClass()
                .setAll(StyleClasses.VERSION_GRAPH_TOGGLE.toString());
        this.versionGraphToggle.selectedProperty()
                .addListener(this::toggleVersionGraph);
        this.conceptDetailPane.setLeft(versionBrancheGrid);
        this.componentPanelBox.getStyleClass()
                .add(StyleClasses.COMPONENT_DETAIL_BACKGROUND.toString());
        this.componentPanelBox.setFillWidth(true);
        setupToolGrid();
        this.historySwitch.selectedProperty()
                .addListener(this::setShowHistory);

        this.expandControl.expandActionProperty()
                .addListener(this::expandAllAction);

        // commit service uses weak change listener references, so this method call is not a leak.
        Get.commitService()
                .addChangeListener(this);
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.CONCEPT_DETAILS.getIconographic();
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void handleChange(ConceptChronology cc) {
        // ignore uncommitted changes...
    }

    @Override
    public Node getNode() {
        return this.conceptDetailPane;
    }

    @Override
    public void handleChange(SemanticChronology sc) {
        // ignore uncommitted changes...
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        if (conceptDetailManifold.getFocusedConcept() != null && conceptDetailManifold.getFocusedConcept().isPresent()) {
            ConceptSpecification focusedConceptSpec = conceptDetailManifold.getFocusedConcept().get();
            ConceptChronology focusedConcept = Get.concept(focusedConceptSpec);
            NidSet recursiveSemantics = focusedConcept.getRecursiveSemanticNids();

            if (commitRecord.getConceptsInCommit()
                    .contains(conceptDetailManifold.getFocusedConcept().get()
                            .getNid())) {
                Platform.runLater(
                        () -> {
                            setConcept(
                                    conceptDetailManifold.focusedConceptProperty(),
                                    null,
                                    conceptDetailManifold.focusedConceptProperty()
                                            .get());
                        });
            } else if (!recursiveSemantics.and(commitRecord.getSemanticNidsInCommit())
                    .isEmpty()) {
                Platform.runLater(
                        () -> {
                            setConcept(
                                    conceptDetailManifold.focusedConceptProperty(),
                                    null,
                                    conceptDetailManifold.focusedConceptProperty()
                                            .get());
                        });
            }
        }
    }

    private void addChronology(ObservableChronology observableChronology, ParallelTransition parallelTransition) {
        if (ComponentPane.isSemanticTypeSupported(observableChronology.getVersionType())) {
            CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
                    = observableChronology.getCategorizedVersions(
                            this.conceptDetailManifold);

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

        ComponentPaneModel componentPaneModel = new ComponentPaneModel(conceptDetailManifold, categorizedVersion, stampOrderHashMap);

        componentPaneModels.add(componentPaneModel);
        componentPaneModel.getBadgedPane().setOpacity(0);
        VBox.setMargin(componentPaneModel.getBadgedPane(), new Insets(1, 5, 1, 5));
        componentPanelBox.getChildren()
                .add(componentPaneModel.getBadgedPane());

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), componentPaneModel.getBadgedPane());

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private Animation addNode(AnchorPane descriptionHeader) {
        descriptionHeader.setOpacity(0);
        VBox.setMargin(descriptionHeader, new Insets(1, 5, 1, 5));
        componentPanelBox.getChildren()
                .add(descriptionHeader);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), descriptionHeader);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }
    
    private void handleCommit(ObservableDescriptionDialect observableDescriptionDialect, 
            ObservableVersion[] versionsToCommit) {
       
        CommitTask commitTask = Get.commitService().commit(FxGet.editCoordinate(), "", versionsToCommit);
        Get.executor().execute(() -> {
            try {
                Optional<CommitRecord> commitRecord = commitTask.get();
                //completeCommit(commitTask, commitRecord);
            } catch (InterruptedException | ExecutionException ex) {
                FxGet.dialogs().showErrorDialog("Error during commit", ex);
            }
        });
    }
    private void clearAnimationComplete(ActionEvent completeEvent) {
        AtomicBoolean axiomHeaderAdded = new AtomicBoolean(false);
        populateVersionBranchGrid();
        componentPanelBox.getChildren()
                .clear();
        componentPanelBox.getChildren()
                .add(toolGrid);

        Optional<ConceptSpecification> focusedConceptSpec = this.conceptDetailManifold.getFocusedConcept();

        if (focusedConceptSpec.isPresent()) {
            ConceptChronology newValue = Get.concept(focusedConceptSpec.get());
            if (titleLabel == null) {
                titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
            }

            toolTipProperty.set(
                    "concept details for: " + this.conceptDetailManifold.getFullySpecifiedDescriptionText(newValue));

            ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                    .getObservableConceptChronology(
                            newValue.getNid());
            final ParallelTransition parallelTransition = new ParallelTransition();

            addChronology(observableConceptChronology, parallelTransition);

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
                    ConceptBuilderComponentPanel descPanel = new ConceptBuilderComponentPanel(conceptDetailManifold, descDialect, true, null);
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
                                                    conceptDetailManifold);

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
                                            == conceptDetailManifold.getInferredAssemblageNid()) {
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
        if (conceptDetailManifold.getFocusedConcept().isPresent()) {
            ObservableDescriptionDialect newDescriptionDialect
                    = new ObservableDescriptionDialect(conceptDetailManifold.getFocusedConcept().get().getPrimordialUuid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
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
        versionBrancheGrid.getChildren()
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
        parallelTransition.setOnFinished(this::clearAnimationComplete);
        parallelTransition.play();
    }

    private void expandAllAction(ObservableValue<? extends ExpandAction> observable,
            ExpandAction oldValue,
            ExpandAction newValue) {
        componentPaneModels.forEach((componentPaneModel) -> componentPaneModel.doExpandAllAction(newValue));
    }

    private void populateVersionBranchGrid() {
        versionBrancheGrid.getChildren()
                .clear();
        versionBrancheGrid.add(versionGraphToggle, 0, 0);

        if (versionGraphToggle.isSelected()) {
            for (int stampOrder = 0; stampOrder < sortedStampSequences.size(); stampOrder++) {
                StampControl stampControl = new StampControl();

                stampControl.setStampedVersion(sortedStampSequences.get(stampOrder), conceptDetailManifold, stampOrder + 1);
                versionBrancheGrid.add(stampControl, 0, stampOrder + 2);
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
        setConcept(
                conceptDetailManifold.focusedConceptProperty(),
                null,
                conceptDetailManifold.focusedConceptProperty()
                        .get());
    }

    private void updateManifoldHistoryStates() {
        if (historySwitch.isSelected()) {
            this.conceptDetailManifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .clear();
            this.conceptDetailManifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .addAll(Status.makeActiveAndInactiveSet());
        } else {
            this.conceptDetailManifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .clear();
            this.conceptDetailManifold.getStampCoordinate()
                    .allowedStatesProperty()
                    .addAll(Status.makeActiveOnlySet());
        }
    }

    private void updateStampControls(Chronology chronology) {
        if (chronology == null) {
            return;
        }
        for (int stampSequence : chronology.getVersionStampSequences()) {
            if (historySwitch.isSelected()) {
                stampOrderHashMap.put(stampSequence, 0);
            } else {
                if (Get.stampService()
                        .getStatusForStamp(stampSequence) == Status.ACTIVE) {
                    stampOrderHashMap.put(stampSequence, 0);
                }
            }
        }

        chronology.getSemanticChronologyList()
                .forEach(
                        (extension) -> {
                            updateStampControls(extension);
                        });
    }

    //~--- set methods ---------------------------------------------------------
    private void setConcept(ObservableValue<? extends ConceptSpecification> observable,
            ConceptSpecification oldSpec,
            ConceptSpecification newSpec) {
        ConceptChronology newValue = Get.concept(newSpec);

        stampOrderHashMap.clear();
        updateStampControls(newValue);
        componentPaneModels.clear();

        IntArrayList stampSequences = stampOrderHashMap.keys();
        sortedStampSequences.clear();
        sortedStampSequences.addAll(stampSequences.toList());

        StampService stampService = Get.stampService();

        sortedStampSequences.sort(
                (o1, o2) -> {
                    return stampService.getInstantForStamp(o2)
                            .compareTo(stampService.getInstantForStamp(o1));
                });

        final AtomicInteger stampOrder = new AtomicInteger();

        sortedStampSequences.forEach((stampSequence) -> {
            if (historySwitch.isSelected()) {
                stampOrderHashMap.put(stampSequence, stampOrder.incrementAndGet());
            } else {
                if (Get.stampService()
                        .getStatusForStamp(stampSequence) == Status.ACTIVE) {
                    stampOrderHashMap.put(stampSequence, stampOrder.incrementAndGet());
                }
            }
        });
        populateVersionBranchGrid();
        updateManifoldHistoryStates();
        clearComponents();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public UUID getListenerUuid() {
        return listenerUuid;
    }

    //~--- set methods ---------------------------------------------------------
    private void setShowHistory(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        setConcept(
                conceptDetailManifold.focusedConceptProperty(),
                null,
                conceptDetailManifold.focusedConceptProperty()
                        .get());
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public ReadOnlyProperty<String> getTitle() {
        return this.titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        if (titleLabel == null) {
            this.titleLabel = new ManifoldLinkedConceptLabel(conceptDetailManifold, ManifoldLinkedConceptLabel::setPreferredText, this);
            this.titleLabel.setGraphic(Iconography.CONCEPT_DETAILS.getIconographic());
            this.titleProperty.set("");
        }

        return Optional.of(titleLabel);
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return this.toolTipProperty;
    }

    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        // No extra menu items added yet. 
        return assemblageMenuList;
    }

    @Override
    public Manifold getManifold() {
        return this.conceptDetailManifold;
    }

    @Override
    public boolean selectInTabOnChange() {
        return this.conceptLabelToolbar.getFocusTabOnConceptChange().get();
    }

}
