/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.provider.concept.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.version.ObservableComponentNidVersionImpl;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableDescriptionVersionImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.isaac.model.observable.version.brittle.Observable_Nid1_Int2_VersionImpl;
import sh.komet.gui.control.concept.NewConceptVersionEditor;
import sh.komet.gui.control.description.dialect.DescriptionDialectEditor;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.style.PseudoClasses;
import static sh.komet.gui.style.PseudoClasses.UNCOMMITTED_PSEUDO_CLASS;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ConceptBuilderComponentPanel
        extends Pane {

    private static final Logger LOG = LogManager.getLogger();

    public static final int FIRST_COLUMN_WIDTH = 32;

    //~--- fields --------------------------------------------------------------
    protected final int badgeWidth = 25;
    protected final ArrayList<Node> badges = new ArrayList<>();
    protected int columns = 10;
    protected Node logicDetailTree = null;
    protected Node editorPane = null;
    protected final Text componentText = new Text();
    protected final Text componentType = new Text();
    protected final GridPane gridpane = new GridPane();
    protected final SimpleBooleanProperty isConcept = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isDescription = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isLogicalDefinition = new SimpleBooleanProperty(false);
    protected final int rowHeight = 25;
    protected int wrappingWidth = 300;
    protected final CheckBox revertCheckBox = new CheckBox();
    private final ObservableVersion observableVersion;
    private final ViewProperties viewProperties;
    protected int rows;
    protected final boolean independentCommit;
    protected NewConceptVersionEditor conceptEditor = null;
    protected final StringProperty conceptText;

    //~--- initializers --------------------------------------------------------
    {
        isDescription.addListener(this::pseudoStateChanged);
        isLogicalDefinition.addListener(this::pseudoStateChanged);
        isConcept.addListener(this::pseudoStateChanged);
    }

    //~--- constructors --------------------------------------------------------
    public ConceptBuilderComponentPanel(ViewProperties viewProperties,
                                        ObservableVersion observableVersion,
                                        boolean independentCommit,
                                        StringProperty conceptText) {
        this.conceptText = conceptText;
        this.independentCommit = independentCommit;
        this.viewProperties = viewProperties;
        this.observableVersion = observableVersion;
        this.getChildren()
                .add(gridpane);
        componentType.getStyleClass()
                .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
        componentText.getStyleClass()
                .add(StyleClasses.COMPONENT_TEXT.toString());
        componentText.setWrappingWidth(wrappingWidth);
        componentText.layoutBoundsProperty()
                .addListener(this::textLayoutChanged);
        this.widthProperty()
                .addListener(this::widthChanged);

        if (observableVersion instanceof ObservableDescriptionDialect) {
            isDescription.set(true);
            setupDescription((ObservableDescriptionDialect) observableVersion);
        } else if (observableVersion instanceof LogicGraphVersion) {
            isLogicalDefinition.set(true);
            setupEl((ObservableLogicGraphVersionImpl) observableVersion);
        } else if (observableVersion instanceof ObservableConceptVersionImpl) {
            isConcept.set(true);
            setupConcept((ObservableConceptVersionImpl) observableVersion);
        }

        // gridpane.gridLinesVisibleProperty().set(true);
        this.getStyleClass()
                .add(StyleClasses.COMPONENT_PANEL.toString());
        this.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, true);

    }

    //~--- methods -------------------------------------------------------------
    protected final void setupConcept(ObservableConceptVersionImpl conceptVersion) {
        componentType.setText(" CON");
        componentText.setText(
                "\n" + conceptVersion.getStatus() + " in " + getViewProperties().getPreferredDescriptionText(
                conceptVersion.getModuleNid()) + " on " + getViewProperties().getPreferredDescriptionText(
                conceptVersion.getPathNid()));
        conceptEditor = new NewConceptVersionEditor(viewProperties.getManifoldCoordinate());
        conceptEditor.setValue(conceptVersion);
        this.editorPane = conceptEditor.getEditor();
    }

    protected final void setupEl(ObservableLogicGraphVersionImpl logicGraphVersion) {
        PremiseType premiseType = PremiseType.STATED;
        Label statedLabel = new Label("", Iconography.STATED.getIconographic());
        statedLabel.setTooltip(new Tooltip("Stated form"));
        badges.add(statedLabel);
        componentType.setText(" EL++");

        this.logicDetailTree = AxiomView.create(logicGraphVersion, premiseType, viewProperties.getManifoldCoordinate());
        this.editorPane = this.logicDetailTree;
    }

    protected final void setupDescription(ObservableDescriptionDialect descriptionDialect) {
        DescriptionDialectEditor editor = new DescriptionDialectEditor(this.viewProperties.getManifoldCoordinate());
        editor.setValue(descriptionDialect);
        this.editorPane = editor.getEditor();
        componentText.setText(descriptionDialect.getDescription().getText());

        int descriptionType = descriptionDialect.getDescription().getDescriptionTypeConceptNid();

        setComponentDescriptionType(descriptionType);
        descriptionDialect.getDescription().descriptionTypeConceptNidProperty().addListener((observable, oldValue, newValue) -> {
            setComponentDescriptionType(newValue.intValue());
        });

    }

    private void setComponentDescriptionType(int descriptionType) throws NoSuchElementException {
        if (descriptionType == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" FQN");
        } else if (descriptionType == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" NĀM");
        } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            componentType.setText(" DEF");
        } else {
            componentType.setText(getViewProperties().getPreferredDescriptionText(descriptionType));
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
        } else if (observable == isLogicalDefinition) {
            this.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, newValue);
        } else if (observable == isConcept) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, newValue);
        }
    }
    private EventHandler<ActionEvent> commitHandler;
    private EventHandler<ActionEvent> cancelHandler;

    public void setCommitHandler(EventHandler<ActionEvent> value) {
        this.commitHandler = value;
    }

    public void setCancelHandler(EventHandler<ActionEvent> value) {
        this.cancelHandler = value;
    }

    private void cancelEdit(ActionEvent event) {
        if (this.cancelHandler != null) {
            this.cancelHandler.handle(event);
        }
    }

    public ObservableVersion[] getVersionsToCommit() throws IllegalStateException {
        List<ObservableVersion> versionsToCommit = new ArrayList<>();
        if (this.observableVersion instanceof ObservableConceptVersionImpl) {
            if (conceptEditor != null) {
                if (conceptEditor.conceptIsAssemblage()) {
                    // add the semantic type to assemblage
                    ConceptSpecification semanticType = conceptEditor.getSemanticTypeForAssemblage();
                    ObservableComponentNidVersionImpl semanticTypeSemanticVersion
                            = new ObservableComponentNidVersionImpl(Get.newUuidWithAssignment(),
                                    this.observableVersion.getPrimordialUuid(),
                                    TermAux.SEMANTIC_TYPE.getNid());
                    semanticTypeSemanticVersion.setComponentNid(semanticType.getNid());
                    semanticTypeSemanticVersion.setStatus(Status.ACTIVE);
                    semanticTypeSemanticVersion.setAuthorNid(this.observableVersion.getAuthorNid());
                    semanticTypeSemanticVersion.setModuleNid(this.observableVersion.getModuleNid());
                    semanticTypeSemanticVersion.setPathNid(this.observableVersion.getPathNid());
                    versionsToCommit.add(semanticTypeSemanticVersion);
                    if (semanticType.getNid() != TermAux.MEMBERSHIP_SEMANTIC.getNid()) {
                        // create concepts for each field

                        // create the concept
                        ObservableConceptVersionImpl fieldConcept
                                = new ObservableConceptVersionImpl(Get.newUuidWithAssignment(), MetaData.SOLOR_CONCEPT_ASSEMBLAGE____SOLOR.getNid());
                        fieldConcept.setStatus(Status.ACTIVE);
                        fieldConcept.setAuthorNid(this.observableVersion.getAuthorNid());
                        fieldConcept.setModuleNid(this.observableVersion.getModuleNid());
                        fieldConcept.setPathNid(this.observableVersion.getPathNid());
                        versionsToCommit.add(fieldConcept);

                        // Add a definition for the concept...
                        // parent is: TermAux.SEMANTIC_FIELD_CONCEPTS
                        final LogicalExpressionBuilderService expressionBuilderService
                                = LookupService.getService(LogicalExpressionBuilderService.class);
                        final LogicalExpressionBuilder defBuilder = expressionBuilderService.getLogicalExpressionBuilder();
                        NecessarySet(And(ConceptAssertion(TermAux.SEMANTIC_FIELD_CONCEPTS, defBuilder)));
                        final LogicalExpression logicalExpression = defBuilder.build();
                        ObservableLogicGraphVersionImpl fieldStatedDef = new ObservableLogicGraphVersionImpl(fieldConcept.getPrimordialUuid(), 
                                TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid());
                        fieldStatedDef.setGraphData(logicalExpression.getData(DataTarget.INTERNAL));
                        fieldStatedDef.setStatus(Status.ACTIVE);
                        fieldStatedDef.setAuthorNid(this.observableVersion.getAuthorNid());
                        fieldStatedDef.setModuleNid(this.observableVersion.getModuleNid());
                        fieldStatedDef.setPathNid(this.observableVersion.getPathNid());
                        versionsToCommit.add(fieldStatedDef);

                        // add the descriptions
                        String fieldName = conceptEditor.getFieldNameForSemantic();
                        addDescriptionAndDialect(fieldConcept, fieldName, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR, versionsToCommit);

                        String fsnFieldName = fieldName + " field of " + conceptText.get();
                        addDescriptionAndDialect(fieldConcept, fsnFieldName, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR, versionsToCommit);

                        // Add concept for field to proper refset. 
                        Observable_Nid1_Int2_VersionImpl fieldAssociation = new Observable_Nid1_Int2_VersionImpl(Get.newUuidWithAssignment(),
                                this.observableVersion.getPrimordialUuid(),
                                TermAux.ASSEMBLAGE_SEMANTIC_FIELDS.getNid());
                        fieldAssociation.setNid1(fieldConcept.getNid());
                        fieldAssociation.setInt2(0);
                        fieldAssociation.setStatus(Status.ACTIVE);
                        fieldAssociation.setAuthorNid(this.observableVersion.getAuthorNid());
                        fieldAssociation.setModuleNid(this.observableVersion.getModuleNid());
                        fieldAssociation.setPathNid(this.observableVersion.getPathNid());
                        versionsToCommit.add(fieldAssociation);
                    }
                }
            }
            return versionsToCommit.toArray(new ObservableVersion[versionsToCommit.size()]);
        }
        if (this.observableVersion instanceof ObservableDescriptionDialect) {
            ObservableDescriptionDialect descDialect = (ObservableDescriptionDialect) this.observableVersion;
            if (descDialect.getDescription().getText() != null
                    &! descDialect.getDescription().getText().isBlank()) {
                if (descDialect.getDescription().getText().length() > 1) {
                    versionsToCommit.add(descDialect.getDescription());
                    versionsToCommit.add(descDialect.getDialect());
                } else {
                    FxGet.dialogs().showInformationDialog("Editorial error", "Not adding 1 character description: " + descDialect.getDescription().getText());
                }
            }
            return versionsToCommit.toArray(new ObservableVersion[versionsToCommit.size()]);
        }
        throw new UnsupportedOperationException("Can't handle getVersionsToCommit for: "
                + this.observableVersion.getClass().getName());

    }

    protected void addDescriptionAndDialect(ObservableConceptVersionImpl concept, String description,
            ConceptSpecification descriptionType, List<ObservableVersion> versionsToCommit) throws NoSuchElementException {
        // create a name
        ObservableDescriptionVersionImpl preferredName = new ObservableDescriptionVersionImpl(Get.newUuidWithAssignment(),
                concept.getPrimordialUuid(),
                MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        preferredName.setText(description);
        preferredName.setCaseSignificanceConceptNid(MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getNid());
        preferredName.setDescriptionTypeConceptNid(descriptionType.getNid());
        preferredName.setLanguageConceptNid(MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        preferredName.setStatus(Status.ACTIVE);
        preferredName.setAuthorNid(this.observableVersion.getAuthorNid());
        preferredName.setModuleNid(this.observableVersion.getModuleNid());
        preferredName.setPathNid(this.observableVersion.getPathNid());
        versionsToCommit.add(preferredName);

        ObservableComponentNidVersionImpl dialect = new ObservableComponentNidVersionImpl(Get.newUuidWithAssignment(),
                preferredName.getPrimordialUuid(),
                TermAux.US_DIALECT_ASSEMBLAGE.getNid());
        dialect.setStatus(Status.ACTIVE);
        dialect.setAuthorNid(this.observableVersion.getAuthorNid());
        dialect.setModuleNid(this.observableVersion.getModuleNid());
        dialect.setPathNid(this.observableVersion.getPathNid());
        dialect.setComponentNid(MetaData.PREFERRED____SOLOR.getNid());
        versionsToCommit.add(dialect);
    }

    private void commitEdit(ActionEvent event) {
        if (this.commitHandler != null) {
            this.commitHandler.handle(event);
        }
    }

    private void redoLayout() {
        if (getParent() != null) {
            getParent().applyCss();
            getParent().layout();
        }
        gridpane.getChildren().clear();
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
        if (componentText.getWrappingWidth() != wrappingWidth) {
            componentText.setWrappingWidth(wrappingWidth);
            // will call redoLayout, so should not continue to layout...
        } else {

            gridpane.getChildren()
                    .remove(componentType);
            GridPane.setConstraints(componentType, 0, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
            gridpane.getChildren()
                    .add(componentType);  // next is 3

            int gridRow = 0;

            if (editorPane != null) {
                gridpane.getChildren()
                        .remove(componentText);
                gridpane.getChildren()
                        .remove(editorPane);

                if (this.independentCommit) {
                    Button cancelButton = new Button("Cancel");
                    cancelButton.setOnAction(this::cancelEdit);
                    cancelButton.getStyleClass()
                            .setAll(StyleClasses.CANCEL_BUTTON.toString());
                    Button commitButton = new Button("Commit");
                    commitButton.setOnAction(this::commitEdit);
                    commitButton.getStyleClass()
                            .setAll(StyleClasses.COMMIT_BUTTON.toString());
                    GridPane.setConstraints(commitButton,
                            columns - 3,
                            gridRow,
                            3,
                            1,
                            HPos.RIGHT,
                            VPos.TOP,
                            Priority.NEVER,
                            Priority.NEVER,
                            new Insets(5, 1, 3, 1));
                    gridpane.getChildren()
                            .add(commitButton);
                    GridPane.setConstraints(cancelButton,
                            columns - 6,
                            gridRow++,
                            3,
                            1,
                            HPos.RIGHT,
                            VPos.TOP,
                            Priority.NEVER,
                            Priority.NEVER,
                            new Insets(5, 1, 3, 1));
                    gridpane.getChildren()
                            .add(cancelButton);
                }

                GridPane.setConstraints(editorPane,
                        3,
                        gridRow++,
                        columns - 4,
                        5,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.ALWAYS,
                        Priority.NEVER,
                        new Insets(5, 1, 3, 1));
                gridpane.getChildren()
                        .add(editorPane);
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

    @Override
    public final ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public int getColumns() {
        return columns;
    }

    /**
     * @return the manifold
     */
    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public int getRows() {
        return rows;
    }
}
