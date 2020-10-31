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

import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static sh.komet.gui.control.badged.ComponentPaneModel.compareWithList;
import static sh.komet.gui.style.StyleClasses.ADD_DESCRIPTION_BUTTON;
import static sh.komet.gui.util.FxUtils.setupHeaderPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

//~--- JDK imports ------------------------------------------------------------

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.*;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.komet.gui.control.ExpandControl;
import sh.komet.gui.control.StampControl;
import sh.komet.gui.control.badged.ComponentPaneModel;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.concept.MenuSupplierForFocusConcept;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.provider.concept.builder.ConceptBuilderComponentPanel;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.FxUtils;

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
 * The setConcept(IdentifiedObject component) method starts the layout process.
 *
 * The animateLayout() method controls which components are rendered, in what order.
 *
 * The addChronology(ObservableChronology observableChronology, ParallelTransition parallelTransition) method
 * adds each top level focus.
 *
 *
 * @author kec
 */
public class ConceptDetailPanelNode extends DetailNodeAbstract
implements ChronologyChangeListener, Supplier<List<MenuItem>> {
    public enum ConceptDetailNodeKeys {
        AXIOM_ORDER,
        DETAIL_ORDER,
        DESCRIPTION_TYPE_ORDER,
        CONCEPT_SEMANTICS_ORDER,
        DESCRIPTION_SEMANTIC_ORDER,
        AXIOM_SEMANTIC_ORDER,
        FOCUS_CONCEPT;
    }

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
    private final Label expandControlLabel = new Label("Expand All", expandControl);
    private final Button panelSettings = new Button(null, Iconography.SETTINGS_SLIDERS.getStyledIconographic());
    private final Button conceptFocusSettings = new Button(null, Iconography.SETTINGS_SLIDERS.getStyledIconographic());
    private final Button descriptionFocusSettings = new Button(null, Iconography.SETTINGS_SLIDERS.getStyledIconographic());
    private final Button axiomFocusSettings = new Button(null, Iconography.SETTINGS_SLIDERS.getStyledIconographic());
    private final OpenIntIntHashMap stampOrderHashMap = new OpenIntIntHashMap();
    private final Button addDescriptionButton = new Button("+ Add");
    private final ToggleButton versionGraphToggle = new ToggleButton("", Iconography.SOURCE_BRANCH_1.getIconographic());
    private final ArrayList<Integer> sortedStampSequences = new ArrayList<>();
    private final List<ComponentPaneModel> componentPaneModels = new ArrayList<>();
    private final ScrollPane scrollPane;



    private final ObservableList<ObservableDescriptionDialect> newDescriptions = FXCollections.observableArrayList();

    // Preference items
    private final SimpleEqualityBasedListProperty<ConceptSpecification> detailOrderList = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.DETAIL_ORDER_FOR_DETAILS_PANE.toExternalString(),
            FXCollections.observableArrayList());

    private final SimpleEqualityBasedListProperty<ConceptSpecification> descriptionTypeList = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.DESCRIPTION_TYPE_ORDER_FOR_DETAILS_PANE.toExternalString(),
            FXCollections.observableArrayList());

    private final SimpleEqualityBasedListProperty<ConceptSpecification> axiomSourceList = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.AXIOM_ORDER_FOR_DETAILS_PANE.toExternalString(),
            FXCollections.observableArrayList());

    private final SimpleEqualityBasedListProperty<ConceptSpecification> semanticOrderForConceptDetails = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.SEMANTIC_ORDER_FOR_CONCEPT_DETAILS.toExternalString(),
            FXCollections.observableArrayList());

    private final SimpleEqualityBasedListProperty<ConceptSpecification> semanticOrderForDescriptionDetails = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.SEMANTIC_ORDER_FOR_DESCRIPTION_DETAILS.toExternalString(),
            FXCollections.observableArrayList());

    private final SimpleEqualityBasedListProperty<ConceptSpecification> semanticOrderForAxiomDetails = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.SEMANTIC_ORDER_FOR_AXIOM_DETAILS.toExternalString(),
            FXCollections.observableArrayList());


    private final PropertySheetConceptListWrapper detailsSettingsWrapper;
    private final PropertySheetConceptListWrapper conceptSettingsWrapper;
    private final PropertySheetConceptListWrapper descriptionAttachmentsOrderWrapper;
    private final PropertySheetConceptListWrapper descriptionSettingsWrapper;
    private final PropertySheetConceptListWrapper axiomSettingsWrapper;
    private final PropertySheetConceptListWrapper semanticOrderForAxiomDetailsWrapper;


    //~--- initializers --------------------------------------------------------
    {
        expandControlLabel.setGraphicTextGap(0);
    }

    private static final ConceptSpecification[] defaultDetailOrder = new ConceptSpecification[] {
            MetaData.CONCEPT_FOCUS____SOLOR, MetaData.DESCRIPTION_FOCUS____SOLOR,
            MetaData.AXIOM_FOCUS____SOLOR
    };

    private static final ConceptSpecification[] defaultDescriptionTypeOrder = new ConceptSpecification[] {
            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TermAux.REGULAR_NAME_DESCRIPTION_TYPE, TermAux.DEFINITION_DESCRIPTION_TYPE,
            ObservableFields.WILDCARD_FOR_ORDER
    };

    private static final ConceptSpecification[] defaultAxiomSourceOrder = new ConceptSpecification[] {
            TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE, TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE, ObservableFields.WILDCARD_FOR_ORDER
    };

    private static final ConceptSpecification[] defaultSemanticOrderForConcept = new ConceptSpecification[] {
            TermAux.SNOMED_IDENTIFIER, MetaData.LOINC_ID_ASSEMBLAGE____SOLOR,
            MetaData.RXNORM_CUI____SOLOR,
            ObservableFields.WILDCARD_FOR_ORDER
    };

    private static final ConceptSpecification[] defaultSemanticOrderForDescription = new ConceptSpecification[] {
            MetaData.US_ENGLISH_DIALECT____SOLOR, MetaData.GB_ENGLISH_DIALECT____SOLOR, TermAux.SNOMED_IDENTIFIER, ObservableFields.WILDCARD_FOR_ORDER
    };

    private static final ConceptSpecification[] defaultSemanticOrderForAxiom = new ConceptSpecification[] {
            ObservableFields.WILDCARD_FOR_ORDER
    };

    private final Runnable saveAction = () -> this.savePreferences();

    //~--- constructors --------------------------------------------------------
    public ConceptDetailPanelNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferences) {
        super(viewProperties, activityFeed, preferences, MenuSupplierForFocusConcept.getArray());

        viewProperties.addSaveAction(saveAction);

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

        this.detailOrderList.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DETAIL_ORDER, defaultDetailOrder));
        this.descriptionTypeList.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DESCRIPTION_TYPE_ORDER, defaultDescriptionTypeOrder));
        this.axiomSourceList.setAll(preferences.getConceptList(ConceptDetailNodeKeys.AXIOM_ORDER, defaultAxiomSourceOrder));
        this.semanticOrderForConceptDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, defaultSemanticOrderForConcept));
        this.semanticOrderForDescriptionDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, defaultSemanticOrderForDescription));
        this.semanticOrderForAxiomDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, defaultSemanticOrderForAxiom));


        this.revertPreferences();
        this.savePreferences();

        this.viewProperties.getManifoldCoordinate().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() ->  resetConceptFromFocus());
        });

        detailsSettingsWrapper = new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), detailOrderList);
        detailsSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.DETAIL_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        detailsSettingsWrapper.setAllowDuplicates(false);
        panelSettings.setOnAction(makeChangeSettingsAction(detailsSettingsWrapper));

        conceptSettingsWrapper = new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), semanticOrderForConceptDetails);
        conceptSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.CONCEPT_ATTACHMENT_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        conceptSettingsWrapper.setAllowDuplicates(false);
        conceptFocusSettings.setOnAction(makeChangeSettingsAction(conceptSettingsWrapper));

        descriptionAttachmentsOrderWrapper = new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), semanticOrderForDescriptionDetails);
        descriptionAttachmentsOrderWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.DESCRIPTION_ATTACHMENT_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        descriptionAttachmentsOrderWrapper.setAllowDuplicates(false);

        descriptionSettingsWrapper = new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), descriptionTypeList);
        descriptionSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.DESCRIPTION_TYPE_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        descriptionSettingsWrapper.setAllowDuplicates(false);
        descriptionFocusSettings.setOnAction(makeChangeSettingsAction(descriptionSettingsWrapper, descriptionAttachmentsOrderWrapper));

        axiomSettingsWrapper = new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), axiomSourceList);
        axiomSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.AXIOM_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        axiomSettingsWrapper.setAllowDuplicates(false);

        semanticOrderForAxiomDetailsWrapper = new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), semanticOrderForAxiomDetails);
        semanticOrderForAxiomDetailsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.AXIOM_ATTACHMENT_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        semanticOrderForAxiomDetailsWrapper.setAllowDuplicates(false);
        axiomFocusSettings.setOnAction(makeChangeSettingsAction(axiomSettingsWrapper, semanticOrderForAxiomDetailsWrapper));

        detailOrderList.addListener(this::handleSettingsChange);
        descriptionTypeList.addListener(this::handleSettingsChange);
        axiomSourceList.addListener(this::handleSettingsChange);
        semanticOrderForConceptDetails.addListener(this::handleSettingsChange);
        descriptionTypeList.addListener(this::handleSettingsChange);

        this.axiomSourceList.setAll(preferences.getConceptList(ConceptDetailNodeKeys.AXIOM_ORDER, defaultAxiomSourceOrder));
        this.semanticOrderForConceptDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, defaultSemanticOrderForConcept));
        this.semanticOrderForDescriptionDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, defaultSemanticOrderForDescription));
        this.semanticOrderForAxiomDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, defaultSemanticOrderForAxiom));

    }



    private void handleSettingsChange(ListChangeListener.Change<? extends ConceptSpecification> c) {
        Platform.runLater(() -> {
            resetConceptFromFocus();
            //Platform.runLater(() -> popOver.show(popOver.getOwnerNode(), popOverArrowLocation.getX(), popOverArrowLocation.getY(), Duration.ZERO));
            Get.executor().execute(() -> savePreferences());
        });
    }

    private EventHandler<ActionEvent> makeChangeSettingsAction(PropertySheetConceptListWrapper... listWrapper) {
        EventHandler<ActionEvent> changeSettingsHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                PropertySheet propertySheet = new PropertySheet();
                propertySheet.setMode(PropertySheet.Mode.NAME);
                propertySheet.setSearchBoxVisible(false);
                propertySheet.setModeSwitcherVisible(false);
                propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(viewProperties.getManifoldCoordinate()));
                propertySheet.getItems().addAll(listWrapper);
                PopOver popOver = new PopOver();
                popOver.setContentNode(propertySheet);
                popOver.setCloseButtonEnabled(true);
                popOver.setHeaderAlwaysVisible(false);
                popOver.setTitle("");
                Point2D popOverArrowLocation = FxGet.getMouseLocation();
                popOver.show(ConceptDetailPanelNode.this.getNode(), popOverArrowLocation.getX(), popOverArrowLocation.getY());

                event.consume();
            }
        };
        return changeSettingsHandler;
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.CONCEPT_DETAILS.getIconographic();
    }

    private void revertList(ConceptDetailNodeKeys detailOrder, SimpleEqualityBasedListProperty<ConceptSpecification> detailOrderList) {
        if (this.preferences.hasKey(detailOrder)) {
            detailOrderList.setAll(this.preferences.getConceptList(detailOrder));
        }
    }

    @Override
    public void revertPreferences() {
        this.selectionIndexProperty.setValue(this.preferences.getInt(Keys.ACTIVITY_SELECTION_INDEX, this.selectionIndexProperty.getValue()));
        revertList(ConceptDetailNodeKeys.DETAIL_ORDER, this.detailOrderList);
        revertList(ConceptDetailNodeKeys.DESCRIPTION_TYPE_ORDER, this.descriptionTypeList);
        revertList(ConceptDetailNodeKeys.AXIOM_ORDER, this.axiomSourceList);
        revertList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, this.semanticOrderForConceptDetails);
        revertList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, this.semanticOrderForDescriptionDetails);
        revertList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, this.semanticOrderForAxiomDetails);
        this.preferences.getConceptSpecification(ConceptDetailNodeKeys.FOCUS_CONCEPT).ifPresent(
                conceptSpecification -> this.focusedObjectProperty().setValue(conceptSpecification));
    }

    @Override
    public void savePreferences() {
        Optional<IdentifiedObject> optionalFocus = this.getFocusedObject();
        if (optionalFocus.isPresent()) {
            this.preferences.putInt(Keys.ACTIVITY_SELECTION_INDEX, this.selectionIndexProperty.getValue());
        }
       this.preferences.put(Keys.ACTIVITY_FEED_NAME, this.getActivityFeed().getFullyQualifiedActivityFeedName());

        this.preferences.putConceptList(ConceptDetailNodeKeys.DETAIL_ORDER, this.detailOrderList);
        this.preferences.putConceptList(ConceptDetailNodeKeys.DESCRIPTION_TYPE_ORDER, this.descriptionTypeList);
        this.preferences.putConceptList(ConceptDetailNodeKeys.AXIOM_ORDER, this.axiomSourceList);
        this.preferences.putConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, this.semanticOrderForConceptDetails);
        this.preferences.putConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, this.semanticOrderForDescriptionDetails);
        this.preferences.putConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, this.semanticOrderForAxiomDetails);
        this.getFocusedObject().ifPresentOrElse(identifiedObject -> {
            this.preferences.putConceptSpecification(ConceptDetailNodeKeys.FOCUS_CONCEPT, Get.concept(identifiedObject.getNid()));
        }, () -> this.preferences.remove(ConceptDetailNodeKeys.FOCUS_CONCEPT));


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
        detailsSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.DETAIL_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        conceptSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.CONCEPT_ATTACHMENT_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        descriptionAttachmentsOrderWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.DESCRIPTION_ATTACHMENT_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        descriptionSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.DESCRIPTION_TYPE_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        axiomSettingsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.AXIOM_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
        semanticOrderForAxiomDetailsWrapper.setConstraints(FxGet.activeConceptMembers(TermAux.AXIOM_ATTACHMENT_ORDER_OPTIONS_ASSEMBLAGE,viewProperties.getManifoldCoordinate()));
    }

    private void addCategorizedVersions(CategorizedVersions<ObservableCategorizedVersion> categorizedVersions,
                                        List<ConceptSpecification> semanticOrderForChronology, ParallelTransition parallelTransition) {
        categorizedVersions.getLatestVersion().ifPresent(observableCategorizedVersion -> {
            parallelTransition.getChildren()
                    .add(addComponent(categorizedVersions, semanticOrderForChronology));
        });
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

    private Animation addComponent(CategorizedVersions<ObservableCategorizedVersion> categorizedVersions,
                                   List<ConceptSpecification> semanticOrderForChronology) {
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

        ComponentPaneModel componentPaneModel = new ComponentPaneModel(this.viewProperties, categorizedVersion, semanticOrderForChronology,
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
    
    private void handleCommit(ObservableDescriptionDialect observableDescriptionDialect, ObservableVersion[] versionsToCommit) {
        Transaction transaction = Get.commitService().newTransaction(Optional.of("ConceptDetailPanelNode commit"), ChangeCheckerMode.ACTIVE);
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

        populateVersionBranchGrid();
        componentPanelBox.getChildren().add(toolGrid);

        getFocusedObject().ifPresent(identifiedObject -> {
            ConceptChronology newValue = Get.concept(identifiedObject.getNid());
            if (titleLabel == null) {
                titleProperty.set(this.viewProperties.getPreferredDescriptionText(newValue));
            } else {
                titleProperty.set(this.titleLabel.getText());
            }

            ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                    .getObservableConceptChronology(
                            newValue.getNid());
            animateFocus(observableConceptChronology);
        });
    }

    private void animateFocus(ObservableConceptChronology observableConceptChronology) {

        final ParallelTransition parallelTransition = new ParallelTransition();
        List<CategorizedVersions<ObservableCategorizedVersion>> descriptionSemantics = new ArrayList<>();
        List<CategorizedVersions<ObservableCategorizedVersion>> axiomSemantics = new ArrayList<>();
        List<CategorizedVersions<ObservableCategorizedVersion>> otherSemantics = new ArrayList<>();

        CategorizedVersions<ObservableCategorizedVersion> categorizedConceptVersions = observableConceptChronology.getCategorizedVersions(
                this.viewProperties.getManifoldCoordinate().getViewStampFilter());

        observableConceptChronology.getObservableSemanticList().forEach(observableSemanticChronology -> {
            CategorizedVersions<ObservableCategorizedVersion> categorizedVersions
                    = observableSemanticChronology.getCategorizedVersions(
                    this.viewProperties.getManifoldCoordinate().getViewStampFilter());
            categorizedVersions.getLatestVersion().ifPresent(semanticVersion -> {
                switch (observableSemanticChronology.getVersionType()) {
                    case DESCRIPTION:
                        descriptionSemantics.add(categorizedVersions);
                        break;
                    case LOGIC_GRAPH:
                        axiomSemantics.add(categorizedVersions);
                        break;
                    default:
                        otherSemantics.add(categorizedVersions);
                }
            });
        });

        for (ConceptSpecification focus: this.detailOrderList) {
            if (focus.getNid() == MetaData.CONCEPT_FOCUS____SOLOR.getNid()) {
                AnchorPane conceptHeader = setupHeaderPanel("CONCEPT", null, conceptFocusSettings);
                conceptHeader.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
                parallelTransition.getChildren()
                        .add(addNode(conceptHeader));
                toolTipProperty.set(
                        "concept details for: " + this.viewProperties.getFullyQualifiedDescriptionText(observableConceptChronology));

                addCategorizedVersions(categorizedConceptVersions,
                                        semanticOrderForConceptDetails, parallelTransition);
            } else if (focus.getNid() == MetaData.DESCRIPTION_FOCUS____SOLOR.getNid()) {
                AnchorPane descriptionHeader = setupHeaderPanel("DESCRIPTIONS", addDescriptionButton, descriptionFocusSettings);

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
                // add description versions here...
                filterAndSortDescriptions(descriptionSemantics, descriptionTypeList)
                        .forEach(categorizedVersions -> addCategorizedVersions(categorizedVersions,
                                semanticOrderForDescriptionDetails, parallelTransition));


            } else if (focus.getNid() == MetaData.AXIOM_FOCUS____SOLOR.getNid()) {
                AnchorPane axiomHeader = setupHeaderPanel("AXIOMS", null, axiomFocusSettings);
                axiomHeader.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
                parallelTransition.getChildren()
                        .add(addNode(axiomHeader));
                // add axiom versions here...
                filterAndSortByAssemblage(axiomSemantics, axiomSourceList)
                        .forEach(categorizedVersions ->
                                addCategorizedVersions(categorizedVersions,
                                        semanticOrderForAxiomDetails, parallelTransition));
            } // else if (lineage view) {
             /* TODO finish lineage view
            AnchorPane lineageHeader = setupHeaderPanel("LINEAGE", null);
            parallelTransition.getChildren()
                    .add(addNode(lineageHeader));

            parallelTransition.getChildren()
                    .add(addNode(LineageTree.makeLineageTree(newValue, this.manifoldProperty.get())));
            */
            // }
        }
        parallelTransition.play();
    }
    public static List<CategorizedVersions<ObservableCategorizedVersion>> filterAndSortByAssemblage(List<CategorizedVersions<ObservableCategorizedVersion>> semantics,
                                                                                                    SimpleEqualityBasedListProperty<ConceptSpecification> assemblagePriorityList) {
        // SimpleEqualityBasedListProperty<ConceptSpecification>
        // Delete any versions not active in configuration
        IntList assemblageOrderList = IntLists.immutable.ofAll(assemblagePriorityList.stream().mapToInt(value -> value.getNid()));
        List<CategorizedVersions<ObservableCategorizedVersion>> filteredAndSortedSemantics = new ArrayList<>(semantics.size());
        if (!assemblageOrderList.contains(MetaData.ANY_COMPONENT____SOLOR.getNid())) {
            // need to filter
            IntSet allowedAssemblageSet = IntSets.immutable.ofAll(assemblageOrderList);
            semantics.stream().forEach(observableCategorizedVersion -> {
                if (allowedAssemblageSet.contains(observableCategorizedVersion.getAssemblageNid())) {
                    filteredAndSortedSemantics.add(observableCategorizedVersion);
                }
            });
        } else {
            filteredAndSortedSemantics.addAll(semantics);
        }
        // now need to sort...
        filteredAndSortedSemantics.sort(compareWithList(assemblageOrderList));
        return filteredAndSortedSemantics;
    }


    public static Comparator<CategorizedVersions<ObservableCategorizedVersion>> compareWithList(IntList assemblageOrderList) {
        return (o1, o2) -> {
            int o1index = assemblageOrderList.indexOf(o1.getAssemblageNid());
            int o2index = assemblageOrderList.indexOf(o2.getAssemblageNid());
            if (o1index == o2index) {
                // same assemblage
                return o1.toString().compareTo(o2.toString());
            }
            if (o1index == -1) {
                return 1;
            }
            if (o2index == -1) {
                return -1;
            }
            return (o1index < o2index) ? -1 : 1;
        };
    }


    public static List<CategorizedVersions<ObservableCategorizedVersion>> filterAndSortDescriptions(List<CategorizedVersions<ObservableCategorizedVersion>> descriptionSemantics,
                                                                                                     SimpleEqualityBasedListProperty<ConceptSpecification> typeList) {
        // SimpleEqualityBasedListProperty<ConceptSpecification>
        // Delete any versions not active in configuration
        IntList typeOrderList = IntLists.immutable.ofAll(typeList.stream().mapToInt(value -> value.getNid()));
        List<CategorizedVersions<ObservableCategorizedVersion>> filteredAndSortedDescriptions = new ArrayList<>(descriptionSemantics.size());
        if (!typeOrderList.contains(MetaData.ANY_COMPONENT____SOLOR.getNid())) {
            // need to filter
            IntSet descTypeSet = IntSets.immutable.ofAll(typeOrderList);
            descriptionSemantics.stream().forEach(observableCategorizedVersion -> {
                observableCategorizedVersion.getLatestVersion().ifPresent(observableDescriptionVersion -> {
                    if (descTypeSet.contains(((DescriptionVersion) observableDescriptionVersion.unwrap()).getDescriptionTypeConceptNid())) {
                        filteredAndSortedDescriptions.add(observableCategorizedVersion);
                    }
                });
             });
        } else {
            filteredAndSortedDescriptions.addAll(descriptionSemantics);
        }
        // now need to sort...
        filteredAndSortedDescriptions.sort((o1, o2) -> {
            int o1index = typeOrderList.indexOf(((DescriptionVersion) o1.getLatestVersion().get().unwrap()).getDescriptionTypeConceptNid());
            int o2index = typeOrderList.indexOf(((DescriptionVersion) o2.getLatestVersion().get().unwrap()).getDescriptionTypeConceptNid());
            if (o1index == o2index) {
                // alphabetical by text if types are the same
                return NaturalOrder.compareStrings(((DescriptionVersion) o1.getLatestVersion().get().unwrap()).getText(),
                        ((DescriptionVersion) o2.getLatestVersion().get().unwrap()).getText());
            }
            if (o1index == -1) {
                return 1;
            }
            if (o2index == -1) {
                return -1;
            }
            return (o1index < o2index) ? -1 : 1;
        });
        return filteredAndSortedDescriptions;
    }

    private void newDescription(Event event) {
        Optional<IdentifiedObject> optionalFocus = this.getFocusedObject();
        if (optionalFocus.isPresent()) {
            ObservableDescriptionDialect newDescriptionDialect
                    = new ObservableDescriptionDialect(optionalFocus.get().getPrimordialUuid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
            newDescriptions.add(newDescriptionDialect);
            newDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
            newDescriptionDialect.getDescription().setStatus(Status.ACTIVE, null);
            newDescriptionDialect.getDialect().setStatus(Status.ACTIVE, null);
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


        GridPane.setConstraints(
                panelSettings,
                2,
                0,
                1,
                1,
                HPos.RIGHT,
                VPos.BOTTOM,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(panelSettings);

        panelSettings.setBorder(Border.EMPTY);
        panelSettings.setBackground(FxUtils.makeBackground(Color.TRANSPARENT));

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
        if (component != null) {
            Platform.runLater(() -> {
                Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(component.getNid());
                optionalChronology.ifPresent(chronology -> {
                    if (chronology instanceof ConceptChronology) {
                        setConcept(chronology);
                    } else {
                        SemanticChronology semanticChronology = (SemanticChronology) chronology;
                        Optional<? extends Chronology> optionalReferencedComponent = Get.identifiedObjectService().getChronology(semanticChronology.getReferencedComponentNid());
                        Chronology referencedComponent = optionalReferencedComponent.get();
                        while (!(referencedComponent instanceof ConceptChronology)) {
                            semanticChronology = (SemanticChronology) referencedComponent;
                            optionalReferencedComponent = Get.identifiedObjectService().getChronology(semanticChronology.getReferencedComponentNid());
                            referencedComponent = optionalReferencedComponent.get();
                        }
                        setConcept(referencedComponent);
                    }
                });
            });
        } else {
            setConcept(null);
        }
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
        animateLayout();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public UUID getListenerUuid() {
        return listenerUuid;
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
        viewProperties.removeSaveAction(saveAction);
    }

    @Override
    public boolean canClose() {
        return true;
    }

}
