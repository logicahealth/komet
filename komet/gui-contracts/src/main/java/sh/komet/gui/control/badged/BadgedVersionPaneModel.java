package sh.komet.gui.control.badged;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.mahout.math.map.OpenIntIntHashMap;
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
import sh.isaac.api.component.semantic.version.*;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.komet.flags.CountryFlagImages;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.*;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

import java.util.*;

import static sh.komet.gui.style.StyleClasses.ADD_ATTACHMENT;

public abstract class BadgedVersionPaneModel {
    protected static final int BADGE_WIDTH = 25;
    protected static final String PROPERTY_SHEET_ATTACHMENT = BadgedVersionPaneModel.class.getCanonicalName() + ".PROPERTY_SHEET_ATTACHMENT";

    protected final FlowPane editControlFlow = new FlowPane(Orientation.VERTICAL);
    protected final FlowPane badgeFlow = new FlowPane();
    protected final TextArea componentText = new TextArea();
    private final BorderPane primaryPane = new BorderPane();
    {
        primaryPane.setLeft(this.badgeFlow);
        primaryPane.setCenter(this.componentText);
        primaryPane.setRight(this.editControlFlow);
    }
    private final VBox outerPane = new VBox(primaryPane);
    protected final Text componentType = new Text();
    protected final MenuButton editControl = new MenuButton("", Iconography.EDIT_PENCIL.getIconographic());
    protected final MenuButton addAttachmentControl = new MenuButton("", Iconography.combine(Iconography.PLUS, Iconography.PAPERCLIP));
    protected final ExpandControl expandControl = new ExpandControl();
    protected final ArrayList<Node> badges = new ArrayList<>();
    protected final ObservableList<ComponentPaneModel> extensionPaneModels = FXCollections.observableArrayList();
    protected final ObservableList<VersionPaneModel> versionPanes = FXCollections.observableArrayList();
    protected final CheckBox revertCheckBox = new CheckBox();

    private final Button cancelButton = new Button("Cancel");
    private final Button commitButton = new Button("Commit");


    private final ObservableCategorizedVersion categorizedVersion;
    private final Manifold manifold;
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
        componentText.setWrapText(true);
        addAttachmentControl.getStyleClass().setAll(ADD_ATTACHMENT.toString());
        editControl.getStyleClass().setAll(StyleClasses.EDIT_COMPONENT_BUTTON.toString());

        cancelButton.getStyleClass()
                .add(StyleClasses.CANCEL_BUTTON.toString());
        cancelButton.setOnAction(this::cancel);
        commitButton.getStyleClass()
                .add(StyleClasses.COMMIT_BUTTON.toString());
        commitButton.setOnAction(this::commit);
        cancelButton.setVisible(false);
        commitButton.setVisible(false);

    }

    protected BadgedVersionPaneModel(Manifold manifold,
                                     ObservableCategorizedVersion categorizedVersion,
                                     OpenIntIntHashMap stampOrderHashMap) {
        this.manifold = manifold;
        this.stampOrderHashMap = stampOrderHashMap;
        this.categorizedVersion = categorizedVersion;
        this.isInactive.set(categorizedVersion.getStatus() == Status.INACTIVE);
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
        this.badges.add(this.stampControl);
        this.addAttachmentControl.getItems().addAll(getAttachmentMenuItems());
        this.addAttachmentControl.setVisible(!addAttachmentControl.getItems().isEmpty());
        this.editControl.getItems().addAll(getEditMenuItems());
        this.editControl.setVisible(!editControl.getItems().isEmpty());


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
        redoLayout();
    }

    protected abstract boolean isLatestPanel();

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

        //TODO fix back up
        //LogicalExpression expression = logicGraphVersion.getLogicalExpression();
        //this.logicDetailPanel = AxiomView.createWithCommitPanel(expression, premiseType, manifold);
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

    }

    private void setupEditControls() {
        this.editControlFlow.getChildren().clear();
        if (!this.editControl.getItems().isEmpty()) {
            this.editControlFlow.getChildren().add(this.editControl);
        }
        if (!this.addAttachmentControl.getItems().isEmpty()) {
            this.editControlFlow.getChildren().add(this.addAttachmentControl);
        }
    }
    private void setupBadges() {
        this.badgeFlow.getChildren().clear();

        double flowWidth = (BADGE_WIDTH + 2) * 3;
        this.badgeFlow.setMaxWidth(flowWidth);
        this.badgeFlow.setMinWidth(flowWidth);
        this.badgeFlow.setPrefWidth(flowWidth);
        this.badgeFlow.getChildren().add(this.expandControl);
        this.badgeFlow.getChildren().add(this.componentType);
        for (Node badge: badges) {
            badgeFlow.getChildren().add(badge);
        }
    }

    private void cancel(ActionEvent event) {
        if (optionalPropertySheetMenuItem.isPresent()) {
            PropertySheetMenuItem item = optionalPropertySheetMenuItem.get();
            item.cancel();
            cleanupAfterCommitOrCancel(item);
        }
    }

    private void commit(ActionEvent event) {
        if (this.optionalPropertySheetMenuItem.isPresent()) {
            PropertySheetMenuItem item = this.optionalPropertySheetMenuItem.get();
            item.commit();
            cleanupAfterCommitOrCancel(item);
        }
    }

    private void cleanupAfterCommitOrCancel(PropertySheetMenuItem item) {
        Platform.runLater(() -> {
            this.cancelButton.setVisible(false);
            this.commitButton.setVisible(false);
            this.outerPane.getChildren().remove(item.getPropertySheet());
            this.optionalPropertySheetMenuItem = Optional.empty();
            this.outerPane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, false);
            this.editControl.getItems().setAll(getEditMenuItems());
            this.editControl.setVisible(!editControl.getItems().isEmpty());
            redoLayout();
        });
    }
    public final List<MenuItem> getEditMenuItems() {
        return FxGet.rulesDrivenKometService().getEditVersionMenuItems(manifold, this.categorizedVersion, (propertySheetMenuItem) -> {
            addEditingPropertySheet(propertySheetMenuItem);
        });
    }

    protected void addEditingPropertySheet(PropertySheetMenuItem propertySheetMenuItem) {
        ObservableVersion observableVersion = propertySheetMenuItem.getVersionInFlight();
        this.outerPane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, true);
        this.editControl.setVisible(false);
        this.cancelButton.setVisible(true);
        this.commitButton.setVisible(true);
        this.optionalPropertySheetMenuItem = Optional.of(propertySheetMenuItem);
        observableVersion.putUserObject(PROPERTY_SHEET_ATTACHMENT, propertySheetMenuItem);
        propertySheetMenuItem.addCompletionListener((observable, oldValue, newValue) -> {
            observableVersion.removeUserObject(PROPERTY_SHEET_ATTACHMENT);
        });
        redoLayout();
    }

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

        ComponentPaneModel componentPane = new ComponentPaneModel(getManifold(), categorizedVersions.getUncommittedVersions().get(0), stampOrderHashMap);
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
    public Manifold getManifold() {
        return manifold;
    }

    public ObservableCategorizedVersion getCategorizedVersion() {
        return categorizedVersion;
    }
}
