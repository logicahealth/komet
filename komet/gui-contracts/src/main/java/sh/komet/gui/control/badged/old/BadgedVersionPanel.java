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
package sh.komet.gui.control.badged.old;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import javafx.application.Platform;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.controlsfx.control.PropertySheet;
import sh.isaac.MetaData;

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.control.*;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

import static sh.komet.gui.style.StyleClasses.ADD_ATTACHMENT;
import sh.komet.gui.util.FxGet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.komet.flags.CountryFlagImages;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.control.textarea.TextAreaReadOnly;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public abstract class BadgedVersionPanel
        extends Pane {

    public static final int FIRST_COLUMN_WIDTH = 32;

    protected static final String PROPERTY_SHEET_ATTACHMENT = BadgedVersionPanel.class.getCanonicalName() + ".PROPERTY_SHEET_ATTACHMENT";

    //~--- fields --------------------------------------------------------------
    protected final int badgeWidth = 25;
    protected final ArrayList<Node> badges = new ArrayList<>();
    protected int columns = 10;
    protected Node logicDetailPanel = null;
    protected final TextAreaReadOnly componentText = new TextAreaReadOnly();
    protected final Text componentType = new Text();
    protected final MenuButton editControl = new MenuButton("", Iconography.EDIT_PENCIL.getIconographic());
    protected final MenuButton addAttachmentControl = new MenuButton("", Iconography.combine(Iconography.PLUS, Iconography.PAPERCLIP));
    protected final ExpandControl expandControl = new ExpandControl();
    protected final GridPane gridpane = new GridPane();
    protected final SimpleBooleanProperty isConcept = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isContradiction = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isDescription = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isInactive = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isLogicalDefinition = new SimpleBooleanProperty(false);
    protected final int rowHeight = 25;
    protected final StampControl stampControl = new StampControl();
    protected int wrappingWidth = 300;
    protected final ObservableList<ComponentPane> extensionPanels = FXCollections.observableArrayList();
    protected final ObservableList<VersionPanel> versionPanels = FXCollections.observableArrayList();
    protected final CheckBox revertCheckBox = new CheckBox();
    private final ObservableCategorizedVersion categorizedVersion;
    private final Manifold manifold;
    protected int rows;
    private Optional<PropertySheetMenuItem> optionalPropertySheetMenuItem = Optional.empty();
    private final Button cancelButton = new Button("Cancel");
    private final Button commitButton = new Button("Commit");
    private final OpenIntIntHashMap stampOrderHashMap;

    //~--- initializers --------------------------------------------------------
    {
        isDescription.addListener(this::pseudoStateChanged);
        isInactive.addListener(this::pseudoStateChanged);
        isConcept.addListener(this::pseudoStateChanged);
        isLogicalDefinition.addListener(this::pseudoStateChanged);
        isContradiction.addListener(this::pseudoStateChanged);
    }

    //~--- constructors --------------------------------------------------------
    public BadgedVersionPanel(Manifold manifold,
            ObservableCategorizedVersion categorizedVersion,
            OpenIntIntHashMap stampOrderHashMap) {
        this.manifold = manifold;
        this.stampOrderHashMap = stampOrderHashMap;
        this.categorizedVersion = categorizedVersion;
        isInactive.set(categorizedVersion.getStatus() == Status.INACTIVE);
        expandControl.expandActionProperty()
                .addListener(this::expand);
        this.getChildren()
                .add(gridpane);
        componentType.getStyleClass()
                .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
        componentText.getStyleClass()
                .setAll(StyleClasses.COMPONENT_TEXT.toString());
        componentText.setWrapText(true);;
        componentText.layoutBoundsProperty()
                .addListener(this::textLayoutChanged);
        componentText.layoutBoundsProperty().addListener(this::debugTextLayoutListener);
        isInactive.set(this.categorizedVersion.getStatus() != Status.ACTIVE);
        int stampSequence = -1;
        if (!categorizedVersion.isUncommitted()) {
            stampSequence = categorizedVersion.getStampSequence();
        }
        if (stampOrderHashMap.containsKey(stampSequence)) {
            this.stampControl.setStampedVersion(
                    stampSequence,
                    manifold,
                    stampOrderHashMap.get(stampSequence));
        } else {
            this.stampControl.setStampedVersion(
                    stampSequence,
                    manifold,
                    -1);
        }
        badges.add(this.stampControl);
        this.widthProperty()
                .addListener(this::widthChanged);

        ObservableVersion observableVersion = categorizedVersion.getObservableVersion();

        addAttachmentControl.getStyleClass()
                .setAll(ADD_ATTACHMENT.toString());
        addAttachmentControl.getItems().addAll(getAttachmentMenuItems());
        addAttachmentControl.setVisible(!addAttachmentControl.getItems().isEmpty());
        editControl.getStyleClass()
                .setAll(StyleClasses.EDIT_COMPONENT_BUTTON.toString());
        editControl.getItems().addAll(getEditMenuItems());
        editControl.setVisible(!editControl.getItems().isEmpty());

        cancelButton.getStyleClass()
                .add(StyleClasses.CANCEL_BUTTON.toString());
        cancelButton.setOnAction(this::cancel);
        commitButton.getStyleClass()
                .add(StyleClasses.COMMIT_BUTTON.toString());
        commitButton.setOnAction(this::commit);
        cancelButton.setVisible(false);
        commitButton.setVisible(false);

        if (observableVersion instanceof DescriptionVersion) {
            isDescription.set(true);
            setupDescription((DescriptionVersion) observableVersion);
        } else if (observableVersion instanceof ConceptVersion) {
            isConcept.set(true);
            setupConcept((ConceptVersion) observableVersion);
        } else if (observableVersion instanceof LogicGraphVersion) {
            isLogicalDefinition.set(true);
            setupLogicDef((LogicGraphVersion) observableVersion);
        } else {
            setupOther(observableVersion);
        }
    }

    private void cancel(ActionEvent event) {
        System.out.println("cancel");
        if (optionalPropertySheetMenuItem.isPresent()) {
            PropertySheetMenuItem item = optionalPropertySheetMenuItem.get();
            item.cancel();
            cleanupAfterCommitOrCancel(item);
        }
    }

    private void commit(ActionEvent event) {
        System.out.println("commit");
        if (optionalPropertySheetMenuItem.isPresent()) {
            PropertySheetMenuItem item = optionalPropertySheetMenuItem.get();
            item.commit();
            cleanupAfterCommitOrCancel(item);
        }
    }

    private void cleanupAfterCommitOrCancel(PropertySheetMenuItem item) {
        Platform.runLater(() -> {
            cancelButton.setVisible(false);
            commitButton.setVisible(false);
            gridpane.getChildren().remove(item.getPropertySheet());
            optionalPropertySheetMenuItem = Optional.empty();
            pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, false);
            editControl.getItems().setAll(getEditMenuItems());
            editControl.setVisible(!editControl.getItems().isEmpty());
            redoLayout();
        });
    }

    public void debugTextLayoutListener(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
        if (this.getParent() != null && componentText.getText().startsWith("SNOMED CT has been")) {
            System.out.println("SCT has been layout: " + newBounds + "\n panel bounds: " + this.getLayoutBounds());
            if (newBounds.getHeight() >= this.getLayoutBounds().getHeight()) {
                this.setMinHeight(newBounds.getHeight());
                this.setPrefHeight(newBounds.getHeight());
                this.setHeight(newBounds.getHeight());
                Platform.runLater(() -> this.getParent().requestLayout());
                System.out.println("Requested layout ");
            }
        }
    }
    //~--- methods -------------------------------------------------------------

    public final List<MenuItem> getAttachmentMenuItems() {
        return FxGet.rulesDrivenKometService().getAddAttachmentMenuItems(manifold, this.categorizedVersion,
                (propertySheetMenuItem, assemblageSpecification) -> {
                    addNewAttachmentPropertySheet(propertySheetMenuItem, assemblageSpecification);
                });
    }

    protected void addNewAttachmentPropertySheet(PropertySheetMenuItem propertySheetMenuItem,
            ConceptSpecification assemblageSpecification) {

        ObservableVersion observableVersion = propertySheetMenuItem.getVersionInFlight();
        observableVersion.putUserObject(PROPERTY_SHEET_ATTACHMENT, propertySheetMenuItem);
        CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = observableVersion.getChronology().getCategorizedVersions(manifold);

        ComponentPane newPanel = new ComponentPane(getManifold(), categorizedVersions.getUncommittedVersions().get(0), stampOrderHashMap);
        extensionPanels.add(newPanel);
        this.expandControl.setExpandAction(ExpandAction.SHOW_CHILDREN);
        propertySheetMenuItem.addCompletionListener((observable, oldValue, newValue) -> {
            observableVersion.removeUserObject(PROPERTY_SHEET_ATTACHMENT);
        });
        redoLayout();
    }

    public final List<MenuItem> getEditMenuItems() {
        return FxGet.rulesDrivenKometService().getEditVersionMenuItems(manifold, this.categorizedVersion, (propertySheetMenuItem) -> {
            addEditingPropertySheet(propertySheetMenuItem);
        });
    }

    protected void addEditingPropertySheet(PropertySheetMenuItem propertySheetMenuItem) {
        ObservableVersion observableVersion = propertySheetMenuItem.getVersionInFlight();
        pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, true);
        editControl.setVisible(false);
        cancelButton.setVisible(true);
        commitButton.setVisible(true);
        this.optionalPropertySheetMenuItem = Optional.of(propertySheetMenuItem);
        observableVersion.putUserObject(PROPERTY_SHEET_ATTACHMENT, propertySheetMenuItem);
        propertySheetMenuItem.addCompletionListener((observable, oldValue, newValue) -> {
            observableVersion.removeUserObject(PROPERTY_SHEET_ATTACHMENT);
        });
        redoLayout();
    }

    public void doExpandAllAction(ExpandAction action) {
        expandControl.setExpandAction(action);
        extensionPanels.forEach((panel) -> panel.doExpandAllAction(action));
    }

    protected abstract void addExtras();

    protected final void expand(ObservableValue<? extends ExpandAction> observable,
            ExpandAction oldValue,
            ExpandAction newValue) {
        redoLayout();
    }

    protected final void setupConcept(ConceptVersion conceptVersion) {
        if (isLatestPanel()) {
            componentType.setText("CON");
            componentText.setText(
                    "\n" + conceptVersion.getStatus() + " in " + getManifold().getPreferredDescriptionText(
                    conceptVersion.getModuleNid()) + " on " + getManifold().getPreferredDescriptionText(
                    conceptVersion.getPathNid()));
        } else {
            componentType.setText("");
            componentText.setText(
                    conceptVersion.getStatus() + " in " + getManifold().getPreferredDescriptionText(
                    conceptVersion.getModuleNid()) + " on " + getManifold().getPreferredDescriptionText(
                    conceptVersion.getPathNid()));
        }
    }

    protected final void setupLogicDef(LogicGraphVersion logicGraphVersion) {
        PremiseType premiseType = PremiseType.STATED;
        if (isLatestPanel()) {
            componentType.setText("EL++");

            if (getManifold().getLogicCoordinate()
                    .getInferredAssemblageNid() == logicGraphVersion.getAssemblageNid()) {
                premiseType = PremiseType.INFERRED;
                Label formLabel = new Label("", Iconography.INFERRED.getIconographic());
                formLabel.setTooltip(new Tooltip("Inferred form"));
                badges.add(formLabel);
            } else if (getManifold().getLogicCoordinate()
                    .getStatedAssemblageNid() == logicGraphVersion.getAssemblageNid()) {
                premiseType = PremiseType.STATED;
                Label formLabel = new Label("", Iconography.STATED.getIconographic());
                formLabel.setTooltip(new Tooltip("Stated form"));
                badges.add(formLabel);
            }
        } else {
            componentType.setText("");
        }

        LogicalExpression expression = logicGraphVersion.getLogicalExpression();
        this.logicDetailPanel = AxiomView.createWithCommitPanel(expression, premiseType, manifold);
    }

    private void setComponentDescriptionType(int descriptionType) throws NoSuchElementException {
        if (descriptionType == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" FQN");
        } else if (descriptionType == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" NĀM");
        } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" DEF");
        } else {
            componentType.setText(getManifold().getPreferredDescriptionText(descriptionType));
        }
    }

    protected final void setupDescription(DescriptionVersion description) {
        componentText.setText(description.getText());

        if (isLatestPanel()) {
            int descriptionType = description.getDescriptionTypeConceptNid();

            setComponentDescriptionType(descriptionType);
            if (description instanceof ObservableDescriptionVersion) {
                ((ObservableDescriptionVersion)description).descriptionTypeConceptNidProperty().addListener((observable, oldValue, newValue) -> {
                    setComponentDescriptionType(newValue.intValue());
                });
            }
        } else {
            componentType.setText("");
        }

        Tooltip tooltip = new Tooltip(manifold.getPreferredDescriptionText(description.getCaseSignificanceConceptNid()));

        if (description.getCaseSignificanceConceptNid() == TermAux.DESCRIPTION_CASE_SENSITIVE.getNid()) {
            Node icon = Iconography.CASE_SENSITIVE.getIconographic();
            Tooltip.install(icon, tooltip);
            badges.add(icon);
        } else if (description.getCaseSignificanceConceptNid()
                == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid()) {
            // TODO get iconographic for initial character sensitive
            Node icon = Iconography.CASE_SENSITIVE.getIconographic();
            Tooltip.install(icon, tooltip);
            badges.add(icon);
        } else if (description.getCaseSignificanceConceptNid()
                == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid()) {
            Node icon = Iconography.CASE_SENSITIVE_NOT.getIconographic();
            Tooltip.install(icon, tooltip);
            badges.add(icon);
        }

        addAcceptabilityBadge(description, MetaData.US_ENGLISH_DIALECT____SOLOR.getNid(), CountryFlagImages.USA.createImageView(16));
        addAcceptabilityBadge(description, MetaData.GB_ENGLISH_DIALECT____SOLOR.getNid(), CountryFlagImages.UK.createImageView(16));
    }

    private void addAcceptabilityBadge(DescriptionVersion description, int dialogAssembblageNid, ImageView countryBadge) throws NoSuchElementException {
        OptionalInt optAcceptabilityNid = manifold.getAcceptabilityNid(description.getNid(), dialogAssembblageNid, manifold);
        if (optAcceptabilityNid.isPresent()) {
            int acceptabilityNid = optAcceptabilityNid.getAsInt();
            if (acceptabilityNid == MetaData.PREFERRED____SOLOR.getNid()) {
                StringBuilder toolTipText = new StringBuilder();
                toolTipText.append(manifold.getPreferredDescriptionText(acceptabilityNid));
                toolTipText.append(" in ");
                toolTipText.append(manifold.getPreferredDescriptionText(dialogAssembblageNid));
                Tooltip acceptabilityTooltip = new Tooltip(toolTipText.toString());
                Tooltip.install(countryBadge, acceptabilityTooltip);
                badges.add(countryBadge);
            }
        }
    }

    protected final void setupOther(Version version) {

        if (version instanceof SemanticVersion) {
            SemanticVersion semanticVersion = (SemanticVersion) version;
            VersionType semanticType = semanticVersion.getChronology()
                    .getVersionType();

            componentType.setText(semanticType.toString());

            switch (semanticType) {
                case STRING:
                    if (isLatestPanel()) {
                        componentType.setText("STR");
                    } else {
                        componentType.setText("");
                    }

                    componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\n" + ((StringVersion) semanticVersion).getString());
                    break;

                case COMPONENT_NID: {
                    if (isLatestPanel()) {
                        componentType.setText("REF");
                    } else {
                        componentType.setText("");
                    }

                    int nid = ((ComponentNidVersion) semanticVersion).getComponentNid();

                    switch (Get.identifierService().getObjectTypeForComponent(nid)) {
                        case CONCEPT:
                            componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\n" + getManifold().getPreferredDescriptionText(nid));
                            break;

                        case SEMANTIC:
                            SemanticChronology sc = Get.assemblageService()
                                    .getSemanticChronology(nid);

                            componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\nReferences: " + sc.getVersionType().toString());
                            break;

                        case UNKNOWN:
                        default:
                            componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\nReferences:"
                                    + Get.identifierService().getObjectTypeForComponent(
                                            nid).toString());
                    }

                    break;
                }

                case Nid1_Int2:
                    if (isLatestPanel()) {
                        componentType.setText("INT-REF");
                    } else {
                        componentType.setText("");
                    }

                    int nid = ((Nid1_Int2_Version) semanticVersion).getNid1();
                    int intValue = ((Nid1_Int2_Version) semanticVersion).getInt2();

                    switch (Get.identifierService().getObjectTypeForComponent(nid)) {
                        case CONCEPT:
                            componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) 
                                    + "\n" + intValue + ": " + getManifold().getPreferredDescriptionText(nid));
                            break;

                        case SEMANTIC:
                            SemanticChronology sc = Get.assemblageService()
                                    .getSemanticChronology(nid);

                            componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) 
                                    + "\n" + intValue + ": References: " + sc.getVersionType().toString());
                            break;

                        case UNKNOWN:
                        default:
                            componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) 
                                    + "\n" + intValue + ": References:"
                                    + Get.identifierService().getObjectTypeForComponent(
                                            nid).toString());
                    }

                    break;

                case LOGIC_GRAPH:
                    if (isLatestPanel()) {
                        componentType.setText("DEF");
                    } else {
                        componentType.setText("");
                    }

                    componentText.setText(((LogicGraphVersion) semanticVersion).getLogicalExpression()
                            .toString());
                    break;

                case LONG:
                    if (isLatestPanel()) {
                        componentType.setText("INT");
                    } else {
                        componentType.setText("");
                    }

                    componentText.setText(Long.toString(((LongVersion) semanticVersion).getLongValue()));
                    break;

                case MEMBER:
                    if (isLatestPanel()) {
                        componentType.setText("MBR");
                    } else {
                        componentType.setText("");
                    }
                    componentText.setText(getManifold().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\nMember");
                    break;

                case RF2_RELATIONSHIP:
                case DYNAMIC:
                case UNKNOWN:
                case DESCRIPTION:
                default:
                    throw new UnsupportedOperationException("al Can't handle: " + semanticType);
            }
        } else {
            componentText.setText(version.getClass()
                    .getSimpleName());
        }
    }

    protected void textLayoutChanged(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
        redoLayout();
    }

    protected void widthChanged(ObservableValue<? extends Number> observableWidth, Number oldWidth, Number newWidth) {
        redoLayout();
    }

    private void pseudoStateChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (observable == isDescription) {
            this.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, newValue);
        } else if (observable == isInactive) {
            this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, newValue);
        } else if (observable == isConcept) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, newValue);
        } else if (observable == isLogicalDefinition) {
            this.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, newValue);
        } else if (observable == isContradiction) {
            this.pseudoClassStateChanged(PseudoClasses.CONTRADICTED_PSEUDO_CLASS, newValue);
        }
    }

    private void redoLayout() {
        if (getParent() != null) {
            getParent().applyCss();
            getParent().layout();
        }
        double doubleRows = componentText.boundsInLocalProperty()
                .get()
                .getHeight() / rowHeight;
        int rowsOfText = (int) doubleRows + 1;
        gridpane.getRowConstraints()
                .clear();

        gridpane.setMinWidth(layoutBoundsProperty().get()
                .getWidth());
        gridpane.setPrefWidth(layoutBoundsProperty().get()
                .getWidth());
        gridpane.setMaxWidth(layoutBoundsProperty().get()
                .getWidth());
        setupColumns();
        wrappingWidth = (int) (layoutBoundsProperty().get()
                .getWidth() - (5 * badgeWidth));




        componentText.setMinWidth(wrappingWidth);
        componentText.setPrefWidth(wrappingWidth);
        componentText.setMaxWidth(wrappingWidth);

        double height = componentText.computeTextHeight(wrappingWidth);
        if (componentText.getWidth() != wrappingWidth
                || componentText.getHeight() != height) {
            componentText.setPrefSize(wrappingWidth, height);
            componentText.setMinSize(wrappingWidth, height);
            componentText.setMaxSize(wrappingWidth, height);
            componentText.resize(wrappingWidth, height);
            // will call redoLayout, so should not continue to layout...
        } else {

            gridpane.getChildren()
                    .remove(expandControl);
            GridPane.setConstraints(expandControl, 0, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER);
            gridpane.getChildren()
                    .add(expandControl);  // next is 1
            gridpane.getChildren()
                    .remove(componentType);
            GridPane.setConstraints(componentType, 1, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
            gridpane.getChildren()
                    .add(componentType);  // next is 3
            gridpane.getChildren()
                    .remove(addAttachmentControl);
            GridPane.setConstraints(addAttachmentControl,
                    columns,
                    1,
                    2,
                    1,
                    HPos.RIGHT,
                    VPos.CENTER,
                    Priority.SOMETIMES,
                    Priority.NEVER,
                    new Insets(0, 4, 1, 0));
            gridpane.getChildren()
                    .add(addAttachmentControl);
// edit control         
            gridpane.getChildren()
                    .remove(editControl);
            GridPane.setConstraints(
                    editControl,
                    columns,
                    0,
                    2,
                    1,
                    HPos.RIGHT,
                    VPos.TOP,
                    Priority.SOMETIMES,
                    Priority.NEVER,
                    new Insets(1, 4, 0, 0));
            gridpane.getChildren()
                    .add(editControl);
// commitButton         

            gridpane.getChildren()
                    .remove(commitButton);
            GridPane.setConstraints(
                    commitButton,
                    columns - 3,
                    0,
                    4,
                    1,
                    HPos.RIGHT,
                    VPos.TOP,
                    Priority.SOMETIMES,
                    Priority.NEVER,
                    new Insets(1, 4, 0, 0));
            gridpane.getChildren()
                    .add(commitButton);

//         
// cancelButton         
            gridpane.getChildren()
                    .remove(cancelButton);
            GridPane.setConstraints(
                    cancelButton,
                    columns - 6,
                    0,
                    3,
                    1,
                    HPos.RIGHT,
                    VPos.TOP,
                    Priority.SOMETIMES,
                    Priority.NEVER,
                    new Insets(1, 4, 0, 0));
            gridpane.getChildren()
                    .add(cancelButton);

//         
            int gridRow = 0;
            if (optionalPropertySheetMenuItem.isPresent()) {
                PropertySheetMenuItem propertySheetMenuItem = optionalPropertySheetMenuItem.get();
                PropertySheet propertySheet = propertySheetMenuItem.getPropertySheet();
                gridpane.getChildren()
                        .remove(propertySheet);
                gridRow = 1;
                gridpane.getRowConstraints()
                        .add(new RowConstraints(rowHeight));  // add row zero...
                RowConstraints propertyRowConstraints = new RowConstraints();
                propertyRowConstraints.setVgrow(Priority.NEVER);

                gridpane.getRowConstraints()
                        .add(propertyRowConstraints);  // add row one...

                GridPane.setConstraints(
                        propertySheet,
                        0,
                        gridRow++,
                        columns - 1,
                        1,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.ALWAYS,
                        Priority.NEVER);

                gridpane.getChildren()
                        .add(propertySheet);
            }

            if (logicDetailPanel != null) {
                gridpane.getChildren()
                        .remove(componentText);
                gridpane.getChildren()
                        .remove(logicDetailPanel);

                GridPane.setConstraints(
                        logicDetailPanel,
                        3,
                        gridRow++,
                        columns - 4,
                        5,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.ALWAYS,
                        Priority.NEVER);
                gridpane.getChildren()
                        .add(logicDetailPanel);
            } else {
                componentText.getLayoutBounds()
                        .getHeight();
                gridpane.getChildren()
                        .remove(componentText);
                GridPane.setConstraints(
                        componentText,
                        3,
                        gridRow++,
                        columns - 4,
                        (int) rowsOfText,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.ALWAYS,
                        Priority.NEVER);
                gridpane.getChildren()
                        .add(componentText);
                gridpane.getRowConstraints()
                        .add(new RowConstraints(rowHeight));
            }

            boolean firstBadgeAdded = false;

            for (int i = 0; i < badges.size();) {
                for (int row = gridRow; i < badges.size(); row++) {
                    this.rows = row;
                    gridpane.getRowConstraints()
                            .add(new RowConstraints(rowHeight));

                    if (row + 1 <= rowsOfText) {
                        for (int column = 0; (column < 3) && (i < badges.size()); column++) {
                            if (firstBadgeAdded && (column == 0)) {
                                column = 1;
                                firstBadgeAdded = true;
                            }

                            setupBadge(badges.get(i++), column, row);
                        }
                    } else {
                        for (int column = 0; (column < columns) && (i < badges.size()); column++) {
                            if (firstBadgeAdded && (column == 0)) {
                                column = 1;
                                firstBadgeAdded = true;
                            }

                            setupBadge(badges.get(i++), column, row);
                        }
                    }
                }
            }

            addExtras();
        }

    }

    private void setupBadge(Node badge, int column, int row) {
        gridpane.getChildren()
                .remove(badge);
        GridPane.setConstraints(
                badge,
                column,
                row,
                1,
                1,
                HPos.CENTER,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        gridpane.getChildren()
                .add(badge);

        if (!badge.getStyleClass()
                .contains(StyleClasses.COMPONENT_BADGE.toString())) {
            badge.getStyleClass()
                    .add(StyleClasses.COMPONENT_BADGE.toString());
        }
    }

    private void setupColumns() {
        if (this.getParent() != null) {
            this.columns = (int) (getLayoutBounds().getWidth() / badgeWidth) - 1;

            if (this.columns < 6) {
                this.columns = 6;
            }

            gridpane.getColumnConstraints()
                    .clear();

            for (int i = 0; i < this.columns; i++) {
                if (i == 0) {
                    gridpane.getColumnConstraints()
                            .add(new ColumnConstraints(FIRST_COLUMN_WIDTH));
                } else {
                    gridpane.getColumnConstraints()
                            .add(new ColumnConstraints(badgeWidth));
                }
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * @return the uncommittedVersion
     */
    public final ObservableCategorizedVersion getCategorizedVersion() {
        return categorizedVersion;
    }

    @Override
    public final ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public int getColumns() {
        return columns;
    }

    protected abstract boolean isLatestPanel();

    /**
     * @return the manifold
     */
    public Manifold getManifold() {
        return manifold;
    }

    public int getRows() {
        return rows;
    }
}
