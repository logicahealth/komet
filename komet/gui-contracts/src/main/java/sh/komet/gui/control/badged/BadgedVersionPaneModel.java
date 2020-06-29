package sh.komet.gui.control.badged;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.scene.control.*;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.controlsfx.control.PropertySheet;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.ImageVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.LoincVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.flags.CountryFlagImages;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.komet.gui.control.ExpandControl;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.control.StampControl;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.control.axiom.ConceptNode;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.control.text.StackLabelText;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

public abstract class BadgedVersionPaneModel {
    public static final int FIRST_COLUMN_WIDTH = 32;
    protected static final int BADGE_WIDTH = 25;
    protected static final String PROPERTY_SHEET_ATTACHMENT = BadgedVersionPaneModel.class.getCanonicalName() + ".PROPERTY_SHEET_ATTACHMENT";

    protected final TilePane editControlTiles = new TilePane();
    protected final TilePane badgeTiles = new TilePane();
    protected final FlowPane badgeFlow = new FlowPane();
    protected final StackLabelText componentText = new StackLabelText();
    private final BorderPane primaryPane = new BorderPane();
    private boolean primaryPaneWrappedForAttachment = false;

    {

        componentText.setWrapText(true);
        BorderPane.setAlignment(componentText, Pos.TOP_LEFT);
        editControlTiles.setPrefTileHeight(BADGE_WIDTH);
        editControlTiles.setPrefTileWidth(BADGE_WIDTH);
        editControlTiles.setPrefColumns(1);
        editControlTiles.setPrefRows(2);
        primaryPane.setLeft(badgeFlow);
        primaryPane.setCenter(componentText);
        primaryPane.setRight(this.editControlTiles);
        VBox.setVgrow(primaryPane, Priority.NEVER);


        double flowWidth = (BADGE_WIDTH + 2) * 3;
        this.badgeFlow.setMaxWidth(flowWidth);
        this.badgeFlow.setMinWidth(flowWidth);
        this.badgeFlow.setPrefWidth(flowWidth);
        this.badgeFlow.setHgap(0);
        this.badgeTiles.setHgap(0);
        this.badgeTiles.setPrefTileHeight(BADGE_WIDTH);
        this.badgeTiles.setPrefTileWidth(BADGE_WIDTH);

    }

    private final VBox outerPane = new VBox(primaryPane);
    protected final Text componentType = new Text();
    protected final MenuButton editControl = new MenuButton("", Iconography.EDIT_PENCIL.getIconographic());
    protected final Menu editMenu = new Menu("Edit", Iconography.REDO.getIconographic());
    protected final Menu attachMenu = new Menu("Attach", Iconography.PAPERCLIP.getIconographic());
    protected final ExpandControl expandControl = new ExpandControl();
    protected final ArrayList<Node> badges = new ArrayList<>();
    protected final ObservableList<ComponentPaneModel> extensionPaneModels = FXCollections.observableArrayList();
    protected final ObservableList<VersionPaneModel> versionPanes = FXCollections.observableArrayList();
    protected final Button redoButton = new Button("", Iconography.REDO.getIconographic());

    {
        redoButton.setOnAction(this::redo);
    }

    private final Button cancelButton = new Button("Cancel");
    private final Button commitButton = new Button("Commit");
    private final Region spacer = new Region();

    {
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
    }

    private final ToolBar editToolBar = new ToolBar(spacer, cancelButton, commitButton);
    private final BorderPane editBorderPane = new BorderPane();

    {
        editBorderPane.setTop(editToolBar);
    }


    private final ObservableCategorizedVersion categorizedVersion;
    private final ViewProperties viewProperties;
    private final HashMap<String, AtomicBoolean> disclosureStateMap;
    private final AtomicBoolean disclosureBoolean;
    private final OpenIntIntHashMap stampOrderHashMap;
    protected final StampControl stampControl = new StampControl();

    protected final SimpleBooleanProperty isConcept = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isContradiction = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isDescription = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isInactive = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isLogicalDefinition = new SimpleBooleanProperty(false);

    private Optional<PropertySheetMenuItem> optionalPropertySheetMenuItem = Optional.empty();


    //~--- initializers --------------------------------------------------------
    {
        isDescription.addListener(this::pseudoStateChanged);
        isInactive.addListener(this::pseudoStateChanged);
        isConcept.addListener(this::pseudoStateChanged);
        isLogicalDefinition.addListener(this::pseudoStateChanged);
        isContradiction.addListener(this::pseudoStateChanged);
        expandControl.expandActionProperty()
                .addListener(this::expand);
        componentType.getStyleClass()
                .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
        componentText.getStyleClass()
                .setAll(StyleClasses.COMPONENT_TEXT.toString());
        //componentText.setWrapText(true);
        editControl.getStyleClass().setAll(StyleClasses.EDIT_COMPONENT_BUTTON.toString());

        cancelButton.getStyleClass()
                .add(StyleClasses.CANCEL_BUTTON.toString());
        cancelButton.setOnAction(this::cancel);
        commitButton.getStyleClass()
                .add(StyleClasses.COMMIT_BUTTON.toString());
        commitButton.setOnAction(this::commit);

//        componentText.setBorder(new Border(new BorderStroke(Color.RED,
//                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

    }

    protected BadgedVersionPaneModel(ViewProperties viewProperties,
                                     ObservableCategorizedVersion categorizedVersion,
                                     OpenIntIntHashMap stampOrderHashMap,
                                     HashMap<String, AtomicBoolean> disclosureStateMap) {
        this.viewProperties = viewProperties;
        this.disclosureStateMap = disclosureStateMap;
        this.stampOrderHashMap = stampOrderHashMap;
        this.categorizedVersion = categorizedVersion;
        this.isInactive.set(categorizedVersion.getStatus() == Status.INACTIVE);
        int stampSequence = categorizedVersion.getStampSequence();
        if (stampOrderHashMap.containsKey(stampSequence)) {
            this.stampControl.setStampedVersion(
                    stampSequence,
                    viewProperties,
                    stampOrderHashMap.get(stampSequence));
        } else {
            this.stampControl.setStampedVersion(
                    stampSequence,
                    viewProperties,
                    -1);
        }
        if (this.isInactive.get()) {
            this.stampControl.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, true);
        }
        this.badges.add(this.stampControl);
        this.editControl.getItems().clear();
        this.attachMenu.getItems().addAll(getAttachmentMenuItems());
        this.editMenu.getItems().addAll(getEditMenuItems());

        if (!this.attachMenu.getItems().isEmpty()) {
            this.editControl.getItems().add(this.attachMenu);
        }
        if (!this.editMenu.getItems().isEmpty()) {
            this.editControl.getItems().add(this.editMenu);
        }
        if (this.editControl.getItems().isEmpty()) {
            this.editControl.setVisible(false);
        }


        ObservableVersion observableVersion = categorizedVersion.getObservableVersion();
        if (observableVersion instanceof DescriptionVersion) {
            this.isDescription.set(true);
            setupDescription((DescriptionVersion) observableVersion);
        } else if (observableVersion instanceof ConceptVersion) {
            this.isConcept.set(true);
            setupConcept((ConceptVersion) observableVersion);
        } else if (observableVersion instanceof LogicGraphVersion) {
            this.isLogicalDefinition.set(true);
            setupLogicDef((LogicGraphVersion) observableVersion);
        } else {
            setupOther(observableVersion);
        }

        this.disclosureBoolean = this.disclosureStateMap.computeIfAbsent(componentType.getText(), (key) -> new AtomicBoolean());
        if (this.disclosureBoolean.get()) {

            Platform.runLater(() -> {
                this.expandControl.setExpandAction(ExpandAction.SHOW_CHILDREN);
            });
        }
        this.expandControl.expandActionProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case HIDE_CHILDREN:
                    this.disclosureBoolean.set(false);
                    break;
                case SHOW_CHILDREN:
                    this.disclosureBoolean.set(true);
                    break;
            }
        });


        redoLayout();
    }

    public HashMap<String, AtomicBoolean> getDisclosureStateMap() {
        return disclosureStateMap;
    }

    protected void wrapAttachmentPane() {
        if (!primaryPaneWrappedForAttachment) {
            BorderPane attachmentWrapperPane = new BorderPane();
            this.outerPane.getChildren().clear();
            this.outerPane.getChildren().add(attachmentWrapperPane);
            attachmentWrapperPane.setCenter(this.primaryPane);
            Node paperClip = Iconography.PAPERCLIP.getIconographic();
            BorderPane.setAlignment(paperClip, Pos.TOP_LEFT);

            TilePane clipPane = new TilePane();
            clipPane.setPrefTileWidth(BADGE_WIDTH);
            clipPane.setPrefTileWidth(BADGE_WIDTH);
            TilePane.setMargin(paperClip, new Insets(7, 10, 0, 5));
            clipPane.getChildren().add(paperClip);


            attachmentWrapperPane.setLeft(clipPane);
            this.primaryPaneWrappedForAttachment = true;
        }

    }

    protected abstract boolean isLatestPanel();

    protected final void setupConcept(ConceptVersion conceptVersion) {
        if (isLatestPanel()) {
            componentType.setText("CON");
            componentText.setText(
                    "\n" + conceptVersion.getStatus() + " in " + getManifoldCoordinate().getPreferredDescriptionText(
                            conceptVersion.getModuleNid()) + " on " + getManifoldCoordinate().getPreferredDescriptionText(
                            conceptVersion.getPathNid()));
        } else {
            componentType.setText("");
            componentText.setText(
                    conceptVersion.getStatus() + " in " + getManifoldCoordinate().getPreferredDescriptionText(
                            conceptVersion.getModuleNid()) + " on " + getManifoldCoordinate().getPreferredDescriptionText(
                            conceptVersion.getPathNid()));
        }
    }


    protected final void setupLogicDef(LogicGraphVersion logicGraphVersion) {
        PremiseType premiseType = PremiseType.STATED;
        if (isLatestPanel()) {
            componentType.setText("EL++");

            if (getManifoldCoordinate().getLogicCoordinate()
                    .getInferredAssemblageNid() == logicGraphVersion.getAssemblageNid()) {
                premiseType = PremiseType.INFERRED;
                Label formLabel = new Label("", Iconography.INFERRED.getIconographic());
                formLabel.setTooltip(new Tooltip("Inferred form"));
                badges.add(formLabel);
            } else if (getManifoldCoordinate().getLogicCoordinate()
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
        Pane logicDetailPanel = AxiomView.createWithCommitPanel(expression, premiseType, viewProperties);
        BorderPane.setAlignment(logicDetailPanel, Pos.TOP_LEFT);
        primaryPane.setCenter(logicDetailPanel);
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

                    componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\n" + ((StringVersion) semanticVersion).getString());
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
                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()));
                            componentText.setImage(new ConceptNode(nid, viewProperties));
                            componentText.setImageLocation(ContentDisplay.BOTTOM);
                            break;

                        case SEMANTIC:
                            SemanticChronology sc = Get.assemblageService()
                                    .getSemanticChronology(nid);

                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\nReferences: " + sc.getVersionType().toString());
                            break;

                        case UNKNOWN:
                        default:
                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\nReferences:"
                                    + Get.identifierService().getObjectTypeForComponent(
                                    nid).toString());
                    }

                    break;
                }

                case Nid1_Int2: {
                    if (isLatestPanel()) {
                        componentType.setText("INT-REF");
                    } else {
                        componentType.setText("");
                    }

                    int nid = ((Nid1_Int2_Version) semanticVersion).getNid1();
                    int intValue = ((Nid1_Int2_Version) semanticVersion).getInt2();

                    switch (Get.identifierService().getObjectTypeForComponent(nid)) {
                        case CONCEPT:
                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid())
                                    + "\n" + intValue);
                            componentText.setImage(new ConceptNode(nid, viewProperties));
                            componentText.setImageLocation(ContentDisplay.BOTTOM);
                            break;

                        case SEMANTIC:
                            SemanticChronology sc = Get.assemblageService()
                                    .getSemanticChronology(nid);

                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid())
                                    + "\n" + intValue + ": References: " + sc.getVersionType().toString());
                            break;

                        case UNKNOWN:
                        default:
                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid())
                                    + "\n" + intValue + ": References:"
                                    + Get.identifierService().getObjectTypeForComponent(
                                    nid).toString());
                    }
                }
                    break;

                case Nid1_Long2: {
                    if (isLatestPanel()) {
                        componentType.setText("REF-LONG");
                    } else {
                        componentType.setText("");
                    }

                    int nid = ((Nid1_Long2_Version) semanticVersion).getNid1();
                    long longValue = ((Nid1_Long2_Version) semanticVersion).getLong2();

                    switch (Get.identifierService().getObjectTypeForComponent(nid)) {
                        case CONCEPT:
                            String longText;
                            if (semanticVersion.getAssemblageNid() == MetaData.PATH_ORIGINS_ASSEMBLAGE____SOLOR.getNid() ||
                                    semanticVersion.getAssemblageNid() == MetaData.DEPENDENCY_MANAGEMENT_ASSEMBLAGE____SOLOR.getNid()) {
                                longText = DateTimeUtil.format(longValue);
                            } else {
                                longText = Long.toString(longValue);
                            }
                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid())
                                    + "\n" + getManifoldCoordinate().getPreferredDescriptionText(nid)
                                    + "\n" + longText);
                            componentText.setImage(new ConceptNode(nid, viewProperties));
                            componentText.setImageLocation(ContentDisplay.BOTTOM);
                            break;

                        case SEMANTIC:
                            SemanticChronology sc = Get.assemblageService()
                                    .getSemanticChronology(nid);

                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid())
                                    + "\n" + getManifoldCoordinate().getPreferredDescriptionText(nid)
                                    + "\n" + longValue + ": References: " + sc.getVersionType().toString());
                            break;

                        case UNKNOWN:
                        default:
                            componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid())
                                    + "\n" + getManifoldCoordinate().getPreferredDescriptionText(nid)
                                    + "\n" + longValue + ": References:"
                                    + Get.identifierService().getObjectTypeForComponent(
                                    nid).toString());
                    }
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
                    componentText.setText(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\nMember");
                    break;

                case LOINC_RECORD: {
                    if (isLatestPanel()) {
                        componentType.setText("LNC");
                    } else {
                        componentType.setText("");
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()));
                    LoincVersion lv = (LoincVersion) semanticVersion;
                    sb.append("\nstatus: ");
                    sb.append(lv.getLoincStatus());
                    sb.append("\nlcn: ");
                    sb.append(lv.getLongCommonName());
                    sb.append("\nshort name: ");
                    sb.append(lv.getShortName());
                    sb.append("\ncomponent: ");
                    sb.append(lv.getComponent());
                    sb.append("\nmethod: ");
                    sb.append(lv.getMethodType());
                    sb.append("\nproperty: ");
                    sb.append(lv.getProperty());
                    sb.append("\nscale: ");
                    sb.append(lv.getScaleType());
                    sb.append("\nsystem: ");
                    sb.append(lv.getSystem());
                    sb.append("\ntiming: ");
                    sb.append(lv.getTimeAspect());
                    componentText.setText(sb.toString());

                }
                break;

                case DYNAMIC:
                    if (isLatestPanel()) {
                        componentType.setText("DYN");
                    } else {
                        componentType.setText("");
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(getManifoldCoordinate().getPreferredDescriptionText(semanticVersion.getAssemblageNid()));
                    DynamicVersion dv = (DynamicVersion) semanticVersion;
                    DynamicUsageDescription dud = DynamicUsageDescriptionImpl.read(version.getAssemblageNid());
                    for (DynamicColumnInfo dci : dud.getColumnInfo()) {
                        sb.append("\n");
                        sb.append(dci.getColumnName());
                        sb.append(": ");
                        sb.append(dv.getData()[dci.getColumnOrder()] == null ? "" : dv.getData()[dci.getColumnOrder()].dataToString());
                    }
                    componentText.setText(sb.toString());
                    break;

                case IMAGE:
                    ImageVersion iv = (ImageVersion) semanticVersion;
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(iv.getImageData());
                    ImageView view = new ImageView(new Image(imageStream));
                    componentText.setImage(view);
                    break;

                case RF2_RELATIONSHIP:
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

    private void pseudoStateChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (observable == isDescription) {
            this.outerPane.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, newValue);
        } else if (observable == isInactive) {
            this.outerPane.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, newValue);
        } else if (observable == isConcept) {
            this.outerPane.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, newValue);
        } else if (observable == isLogicalDefinition) {
            this.outerPane.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, newValue);
        } else if (observable == isContradiction) {
            this.outerPane.pseudoClassStateChanged(PseudoClasses.CONTRADICTED_PSEUDO_CLASS, newValue);
        }
    }

    protected final void setupDescription(DescriptionVersion description) {
        componentText.setText(description.getText());

        if (isLatestPanel()) {
            int descriptionType = description.getDescriptionTypeConceptNid();

            setComponentDescriptionType(descriptionType);
            if (description instanceof ObservableDescriptionVersion) {
                ((ObservableDescriptionVersion) description).descriptionTypeConceptNidProperty().addListener((observable, oldValue, newValue) -> {
                    setComponentDescriptionType(newValue.intValue());
                });
            }
        } else {
            componentType.setText("");
        }

        Tooltip tooltip = new Tooltip(viewProperties.getPreferredDescriptionText(description.getCaseSignificanceConceptNid()));

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

    private void setComponentDescriptionType(int descriptionType) throws NoSuchElementException {
        if (descriptionType == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" FQN");
        } else if (descriptionType == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" NĀM");
        } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" DEF");
        } else {
            componentType.setText(getManifoldCoordinate().getPreferredDescriptionText(descriptionType));
        }
    }

    private void addAcceptabilityBadge(DescriptionVersion description, int dialogAssembblageNid, ImageView countryBadge) throws NoSuchElementException {
        OptionalInt optAcceptabilityNid = viewProperties.getManifoldCoordinate().getAcceptabilityNid(description.getNid(), dialogAssembblageNid);
        if (optAcceptabilityNid.isPresent()) {
            int acceptabilityNid = optAcceptabilityNid.getAsInt();
            if (acceptabilityNid == MetaData.PREFERRED____SOLOR.getNid()) {
                StringBuilder toolTipText = new StringBuilder();
                toolTipText.append(viewProperties.getPreferredDescriptionText(acceptabilityNid));
                toolTipText.append(" in ");
                toolTipText.append(viewProperties.getPreferredDescriptionText(dialogAssembblageNid));
                Tooltip acceptabilityTooltip = new Tooltip(toolTipText.toString());
                Tooltip.install(countryBadge, acceptabilityTooltip);
                badges.add(countryBadge);
            }
        }
    }

    public void doExpandAllAction(ExpandAction action) {
        expandControl.setExpandAction(action);
        extensionPaneModels.forEach((panel) -> panel.doExpandAllAction(action));
    }

    protected abstract void addExtras();

    public Pane getBadgedPane() {
        return this.outerPane;
    }

    protected final void expand(ObservableValue<? extends ExpandAction> observable,
                                ExpandAction oldValue,
                                ExpandAction newValue) {
        redoLayout();
    }

    private void redoLayout() {
        setupBadges();
        setupEditControls();
        addExtras();

    }

    private void setupEditControls() {
        this.editControlTiles.getChildren().clear();
        if (!this.editControl.getItems().isEmpty()) {
            this.editControlTiles.getChildren().add(this.editControl);
        }
    }

    private void setupBadges() {
        this.badgeFlow.getChildren().clear();
        this.badgeTiles.getChildren().clear();

        this.badgeFlow.getChildren().add(this.expandControl);
        this.badgeFlow.getChildren().add(this.componentType);
        this.badgeFlow.getChildren().add(this.badgeTiles);
        badgeTiles.setPrefColumns(3);
        badgeTiles.setPrefRows(badges.size() / 3 + 1);

        for (Node badge : badges) {
            badgeTiles.getChildren().add(badge);
        }
    }

    private void redo(ActionEvent event) {
        List<MenuItem> menuItems = getEditMenuItems();
        if (!menuItems.isEmpty()) {
            MenuItem itemToExecute = menuItems.get(0);
            itemToExecute.getOnAction().handle(event);
        }
    }

    private void cancel(ActionEvent event) {
        primaryPane.setTop(null);
        if (optionalPropertySheetMenuItem.isPresent()) {
            PropertySheetMenuItem item = optionalPropertySheetMenuItem.get();
            item.cancel();
            cleanupAfterCommitOrCancel(item);
        }
    }

    private void commit(ActionEvent event) {
        primaryPane.setTop(null);
        if (this.optionalPropertySheetMenuItem.isPresent()) {
            PropertySheetMenuItem item = this.optionalPropertySheetMenuItem.get();
            item.commit();
            cleanupAfterCommitOrCancel(item);
        }
    }

    private void cleanupAfterCommitOrCancel(PropertySheetMenuItem item) {
        Platform.runLater(() -> {
            this.primaryPane.setTop(null);
            this.optionalPropertySheetMenuItem = Optional.empty();
            this.editBorderPane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, false);
            this.editToolBar.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, false);

            this.editControl.getItems().clear();
            this.attachMenu.getItems().setAll(getAttachmentMenuItems());
            if (!this.attachMenu.getItems().isEmpty()) {
                this.editControl.getItems().add(this.attachMenu);
            }
            this.editMenu.getItems().setAll(getEditMenuItems());
            if (!this.editMenu.getItems().isEmpty()) {
                this.editControl.getItems().add(this.editMenu);
            }
            this.editControl.setVisible(!this.editControl.getItems().isEmpty());
            if (this instanceof VersionPaneModel) {
                this.redoButton.setVisible(true);
            }
            redoLayout();
        });
    }

    public final List<MenuItem> getEditMenuItems() {
        return FxGet.rulesDrivenKometService().getEditVersionMenuItems(viewProperties, this.categorizedVersion, (propertySheetMenuItem) -> {
            addEditingPropertySheet(propertySheetMenuItem);
        });
    }

    protected void addEditingPropertySheet(PropertySheetMenuItem propertySheetMenuItem) {
        PropertySheet propertySheet = propertySheetMenuItem.getPropertySheet();
        propertySheet.getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
        this.primaryPane.setTop(this.editBorderPane);
        ObservableVersion observableVersion = propertySheetMenuItem.getVersionInFlight();
        this.editBorderPane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, true);
        this.editToolBar.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, true);
        propertySheet.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, true);
        this.editControl.setVisible(false);
        this.redoButton.setVisible(false);
        this.optionalPropertySheetMenuItem = Optional.of(propertySheetMenuItem);
        this.editBorderPane.setCenter(propertySheet);

        observableVersion.putUserObject(PROPERTY_SHEET_ATTACHMENT, propertySheetMenuItem);
        propertySheetMenuItem.addCompletionListener((observable, oldValue, newValue) -> {
            observableVersion.removeUserObject(PROPERTY_SHEET_ATTACHMENT);
        });
        redoLayout();
    }

    public final List<MenuItem> getAttachmentMenuItems() {
        return FxGet.rulesDrivenKometService().getAddAttachmentMenuItems(viewProperties, this.categorizedVersion,
                (propertySheetMenuItem, assemblageSpecification) -> {
                    addNewAttachmentPropertySheet(propertySheetMenuItem, assemblageSpecification);
                });
    }

    protected void addNewAttachmentPropertySheet(PropertySheetMenuItem propertySheetMenuItem,
                                                 ConceptSpecification assemblageSpecification) {

        ObservableVersion observableVersion = propertySheetMenuItem.getVersionInFlight();
        observableVersion.putUserObject(PROPERTY_SHEET_ATTACHMENT, propertySheetMenuItem);
        CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = observableVersion.getChronology().getCategorizedVersions(viewProperties.getManifoldCoordinate().getVertexStampFilter());

        ComponentPaneModel componentPane = new ComponentPaneModel(getViewProperties(), categorizedVersions.getUncommittedVersions().get(0), stampOrderHashMap, getDisclosureStateMap());
        extensionPaneModels.add(componentPane);
        this.expandControl.setExpandAction(ExpandAction.SHOW_CHILDREN);
        propertySheetMenuItem.addCompletionListener((observable, oldValue, newValue) -> {
            observableVersion.removeUserObject(PROPERTY_SHEET_ATTACHMENT);
        });
        redoLayout();
    }

    /**
     * @return the manifold
     */
    public ManifoldCoordinate getManifoldCoordinate() {
        return viewProperties.getManifoldCoordinate();
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public ObservableCategorizedVersion getCategorizedVersion() {
        return categorizedVersion;
    }
}
