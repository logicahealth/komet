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
package sh.komet.gui.control.axiom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.docbook.DocBook;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.LiteralNodeBoolean;
import sh.isaac.model.logic.node.LiteralNodeDouble;
import sh.isaac.model.logic.node.LiteralNodeInstant;
import sh.isaac.model.logic.node.LiteralNodeInteger;
import sh.isaac.model.logic.node.LiteralNodeString;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.model.observable.ObservableSemanticChronologyImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class AxiomView {

    private static final int INDENT_PIXELS = 25;
    private static final Border CHILD_BOX_BORDER = new Border(
            new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 0))
    );
    private static final Border TOOL_BAR_BORDER = new Border(
            new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 0))
    );
    private static final Border ROOT_BORDER = new Border(
            new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))
    );
    private static final Border INNER_ROOT_BORDER = new Border(
            new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 0, 1, 1))
    );

    private ObservableLogicGraphVersionImpl logicGraphVersion;
    private LogicalExpression expression;
    private final Manifold manifold;
    private final PremiseType premiseType;
    private final AnchorPane anchorPane = new AnchorPane();
    private BorderPane borderPane;

    private AxiomView(LogicalExpression expression, PremiseType premiseType, Manifold manifold) {
        this.expression = expression;
        this.manifold = manifold;
        this.premiseType = premiseType;
    }

    private String getConceptBeingDefinedText(String prefix) {

        if (expression.getConceptBeingDefinedNid() != -1
                && expression.getConceptBeingDefinedNid() != MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            StringBuilder builder = new StringBuilder();
            if (prefix != null) {
                builder.append(prefix);
                builder.append(": ");
            }
            builder.append(manifold.getPreferredDescriptionText(expression.getConceptBeingDefinedNid()));
            return builder.toString();
        } else if (prefix != null) {
            return prefix + ": Concept being defined";
        }
        return "Concept being defined";
    }

    protected final void setPseudoClasses(Node node) {
        switch (premiseType) {
            case INFERRED:
                node.pseudoClassStateChanged(PseudoClasses.STATED_PSEUDO_CLASS, false);
                node.pseudoClassStateChanged(PseudoClasses.INFERRED_PSEUDO_CLASS, true);
                break;
            case STATED:
                node.pseudoClassStateChanged(PseudoClasses.STATED_PSEUDO_CLASS, true);
                node.pseudoClassStateChanged(PseudoClasses.INFERRED_PSEUDO_CLASS, false);
                break;
        }

    }

    private boolean isDefined(int conceptNid) {
        Optional<LogicalExpression> conceptExpression = manifold.getLogicalExpression(conceptNid, premiseType);
        if (!conceptExpression.isPresent()) {
            return false;
        }
        return conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET);
    }

    private boolean isMultiparent(int conceptNid) {
        if (conceptNid == -1
                || conceptNid == MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            return false;
        }
        int[] parents = Get.taxonomyService().getSnapshot(manifold)
                .getTaxonomyTree().getParentNids(conceptNid);
        Optional<LogicalExpression> conceptExpression = manifold.getLogicalExpression(conceptNid, premiseType);
        if (!conceptExpression.isPresent()) {
            return false;
        }
        return parents.length > 1;
    }

    public final Node computeGraphic(int conceptNid, boolean expanded) {

        if (conceptNid == -1
                || conceptNid == MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            return Iconography.ALERT_CONFIRM2.getIconographic();
        }
        int[] parents = Get.taxonomyService().getSnapshot(manifold).getTaxonomyParentConceptNids(conceptNid);
        Optional<LogicalExpression> conceptExpression = manifold.getLogicalExpression(conceptNid, premiseType);
        if (!conceptExpression.isPresent()) {
            return Iconography.ALERT_CONFIRM2.getIconographic();
        }
        boolean multiParent = parents.length > 1;
        boolean sufficient = conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET);

        if (parents.length == 0) {
            return Iconography.TAXONOMY_ROOT_ICON.getIconographic();
        } else if (sufficient && multiParent) {
            if (expanded) {
                return Iconography.TAXONOMY_DEFINED_MULTIPARENT_OPEN.getIconographic();
            } else {
                return Iconography.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.getIconographic();
            }
        } else if (!sufficient && multiParent) {
            if (expanded) {
                return Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.getIconographic();
            } else {
                return Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.getIconographic();
            }
        } else if (sufficient && !multiParent) {
            return Iconography.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic();
        }
        return Iconography.TAXONOMY_PRIMITIVE_SINGLE_PARENT.getIconographic();
    }

    private void addToToolbarNoGrowTopAlign(GridPane rootToolBar, Node node, int column) {
        GridPane.setConstraints(node, column, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
        rootToolBar.getChildren().add(node);
    }

    private void addToToolbarNoGrow(GridPane rootToolBar, Node node, int column) {
        GridPane.setConstraints(node, column, 0, 1, 1, HPos.LEFT, VPos.BASELINE, Priority.NEVER, Priority.NEVER);
        rootToolBar.getChildren().add(node);
    }

    private void addToToolbarGrow(GridPane rootToolBar, Node node, int column) {
        GridPane.setConstraints(node, column, 0, 1, 1, HPos.LEFT, VPos.BASELINE, Priority.ALWAYS, Priority.NEVER);
        rootToolBar.getChildren().add(node);
    }

    public static Node create(ObservableLogicGraphVersionImpl logicGraphVersion, PremiseType premiseType, Manifold manifold) {
        AxiomView axiomView = new AxiomView(logicGraphVersion.getLogicalExpression(), premiseType, manifold);
        axiomView.logicGraphVersion = logicGraphVersion;
        BorderPane axiomBorderPane = axiomView.create((AbstractLogicNode) axiomView.expression.getRoot());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        axiomView.anchorPane.getChildren().setAll(axiomBorderPane);
        return axiomView.anchorPane;
    }

    public static Node createWithCommitPanel(ObservableLogicGraphVersionImpl logicGraphVersion, PremiseType premiseType, Manifold manifold) {
        AxiomView axiomView = new AxiomView(logicGraphVersion.getLogicalExpression(), premiseType, manifold);
        axiomView.logicGraphVersion = logicGraphVersion;
        BorderPane axiomBorderPane = axiomView.create((AbstractLogicNode) axiomView.expression.getRoot());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        axiomView.anchorPane.getChildren().setAll(axiomBorderPane);
        axiomView.borderPane = new BorderPane(axiomView.anchorPane);
        return axiomView.borderPane;
    }

    private BorderPane create(AbstractLogicNode logicNode) {
        ClauseView clauseView = new ClauseView(logicNode);
        return clauseView.rootPane;
    }

    public static AnchorPane create(LogicalExpression expression,
            PremiseType premiseType, Manifold manifold) {
        AxiomView axiomView = new AxiomView(expression, premiseType, manifold);
        BorderPane axiomBorderPane = axiomView.create((AbstractLogicNode) expression.getRoot());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        axiomView.anchorPane.getChildren().setAll(axiomBorderPane);
        return axiomView.anchorPane;
    }

    public static BorderPane createWithCommitPanel(LogicalExpression expression,
            PremiseType premiseType, Manifold manifold) {
        AxiomView axiomView = new AxiomView(expression, premiseType, manifold);
        BorderPane axiomBorderPane = axiomView.create((AbstractLogicNode) expression.getRoot());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        axiomView.anchorPane.getChildren().setAll(axiomBorderPane);
        axiomView.borderPane = new BorderPane(axiomView.anchorPane);
        return axiomView.borderPane;
    }

    private void updateExpressionForAxiomView(LogicalExpression expression) {
        this.expression = expression;
        if (logicGraphVersion != null) {
            logicGraphVersion.setGraphData(expression.getData(DataTarget.INTERNAL));

        }
        BorderPane axiomBorderPane = create((AbstractLogicNode) expression.getRoot());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        anchorPane.getChildren().setAll(axiomBorderPane);
        if (expression.isUncommitted()) {
            if (borderPane != null) {
                ToolBar commitToolbar = new ToolBar();
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                spacer.setMinWidth(Region.USE_PREF_SIZE);
                Button cancel = new Button("Cancel");
                cancel.setOnAction(this::cancelEdit);
                Button commit = new Button("Commit");
                commit.setOnAction(this::commitEdit);
                commitToolbar.getItems().addAll(spacer, cancel, commit);
                borderPane.setTop(commitToolbar);
            }
        } else {
            if (borderPane != null) {
                borderPane.setTop(null);
            }
        }
    }

    private void cancelEdit(Event event) {
        updateExpression();
    }

    private void updateExpression() {
        if (this.expression.getConceptBeingDefinedNid() != -1) {
            Optional<LogicalExpression> committedExpression = manifold.getStatedLogicalExpression(this.expression.getConceptBeingDefinedNid());
            if (committedExpression.isPresent()) {
                updateExpressionForAxiomView(committedExpression.get());
            }
        }
    }

    private void commitEdit(Event event) {

        LatestVersion<LogicGraphVersion> latestVersion = manifold.getStatedLogicGraphVersion(this.expression.getConceptBeingDefinedNid());
        if (latestVersion.isPresent()) {
            LogicGraphVersion version = latestVersion.get();
            ObservableSemanticChronologyImpl observableSemanticChronology = new ObservableSemanticChronologyImpl(version.getChronology());
            ObservableLogicGraphVersionImpl observableVersion = new ObservableLogicGraphVersionImpl(version, observableSemanticChronology);
            ObservableLogicGraphVersionImpl mutableVersion = observableVersion.makeAutonomousAnalog(manifold.getEditCoordinate());
            mutableVersion.setGraphData(this.expression.getData(DataTarget.INTERNAL));
            Get.commitService().commit(manifold.getEditCoordinate(), "Axiom view edit", mutableVersion);
        }
        updateExpression();
    }

    protected class ClauseView {

        protected final AbstractLogicNode logicNode;
        protected final Label titleLabel = new Label();
        protected final BorderPane rootPane = new BorderPane();
        protected final GridPane rootToolBar = new GridPane();
        protected final Button editButton = new Button("", Iconography.EDIT_PENCIL.getIconographic());
        protected final ToggleButton expandButton = new ToggleButton("", Iconography.OPEN.getIconographic());
        protected final List<ClauseView> childClauses = new ArrayList<>();
        protected final SimpleBooleanProperty expanded = new SimpleBooleanProperty(true);
        protected final VBox childBox = new VBox();
        protected PopOver popover;
        Button openConceptButton = new Button("", Iconography.LINK_EXTERNAL.getIconographic());
        TransferMode[] transferMode = null;
        Background originalBackground;
        boolean editable = false;

        public ClauseView(AbstractLogicNode logicNode) {
            this.logicNode = logicNode;
            setPseudoClasses(rootPane);
            rootToolBar.setBorder(TOOL_BAR_BORDER);
            rootToolBar.setPadding(new Insets(2));
            rootPane.setBorder(INNER_ROOT_BORDER);
            rootPane.setPadding(new Insets(3));
            editButton.setPadding(Insets.EMPTY);
            expandButton.setPadding(Insets.EMPTY);
            expandButton.selectedProperty().bindBidirectional(expanded);
            expanded.addListener((observable, oldValue, newValue) -> {
                this.toggleExpansion();
            });
            editButton.setOnMousePressed(this::handleEditClick);

            titleLabel.setOnDragOver(this::handleDragOver);
            titleLabel.setOnDragEntered(this::handleDragEntered);
            titleLabel.setOnDragDetected(this::handleDragDetected);
            titleLabel.setOnDragExited(this::handleDragExited);
            titleLabel.setOnDragDone(this::handleDragDone);

            switch (logicNode.getNodeSemantic()) {
                case CONCEPT: {
                    if (premiseType == PremiseType.STATED) {
                        editable = true;
                    }
                    ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_CONCEPT.toString());
                    titleLabel.setText(manifold.getPreferredDescriptionText(conceptNode.getConceptNid()));
                    titleLabel.setGraphic(computeGraphic(conceptNode.getConceptNid(), false));
                    openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
                    openConceptButton.setOnMouseClicked(this::handleShowConceptNodeClick);

                    int column = 0;
                    addToToolbarNoGrowTopAlign(rootToolBar, openConceptButton, column++);
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    }
                    break;
                }
                case FEATURE: {
                    if (premiseType == PremiseType.STATED) {
                        editable = true;
                    }
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_FEATURE.toString());
                    int column = 0;
                    addToToolbarNoGrow(rootToolBar, expandButton, column++);
                    openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
                    addToToolbarNoGrowTopAlign(rootToolBar, openConceptButton, column++);
                    openConceptButton.setOnMouseClicked(this::handleShowFeatureNodeClick);
                    FeatureNodeWithNids featureNode = (FeatureNodeWithNids) logicNode;
                    StringBuilder builder = new StringBuilder();
                    builder.append("⒡ ");
                    builder.append(manifold.getPreferredDescriptionText(featureNode.getTypeConceptNid()));
                    switch (featureNode.getOperator()) {
                        case EQUALS:
                            builder.append(" = ");
                            break;
                        case GREATER_THAN:
                            builder.append(" > ");
                            break;
                        case GREATER_THAN_EQUALS:
                            builder.append(" ≥ ");
                            break;
                        case LESS_THAN:
                            builder.append(" < ");
                            break;
                        case LESS_THAN_EQUALS:
                            builder.append(" ≤ ");
                            break;
                        default:
                            throw new UnsupportedOperationException("Can't handle: " + featureNode.getOperator());
                    }

                    for (AbstractLogicNode featureChildNode : featureNode.getChildren()) {
                        switch (featureChildNode.getNodeSemantic()) {
                            case LITERAL_BOOLEAN: {
                                LiteralNodeBoolean node = (LiteralNodeBoolean) featureChildNode;
                                builder.append(node.getLiteralValue());
                                break;
                            }
                            case LITERAL_FLOAT: {
                                LiteralNodeDouble node = (LiteralNodeDouble) featureChildNode;
                                builder.append(node.getLiteralValue());
                                break;
                            }
                            case LITERAL_INSTANT: {
                                LiteralNodeInstant node = (LiteralNodeInstant) featureChildNode;
                                builder.append(node.getLiteralValue());
                                break;
                            }
                            case LITERAL_INTEGER: {
                                LiteralNodeInteger node = (LiteralNodeInteger) featureChildNode;
                                builder.append(node.getLiteralValue());
                                node.getChildren();
                                break;
                            }
                            case LITERAL_STRING: {
                                LiteralNodeString node = (LiteralNodeString) featureChildNode;
                                builder.append(node.getLiteralValue());
                                break;
                            }
                        }
                    }
                    builder.append(" ");
                    builder.append(manifold.getPreferredDescriptionText(featureNode.getMeasureSemanticNid()));
                    titleLabel.setText(builder.toString());
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    }
                    break;
                }
                case ROLE_SOME: {
                    int column = 0;
                    RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) logicNode;
                    if (roleNode.getTypeConceptNid() == MetaData.ROLE_GROUP____SOLOR.getNid()) {
                        rootPane.getStyleClass()
                                .add(StyleClasses.DEF_ROLE_GROUP.toString());
                        titleLabel.setGraphic(Iconography.ROLE_GROUP.getIconographic());
                        StringBuilder builder = new StringBuilder();

                        for (LogicNode descendentNode : roleNode.getDescendents()) {
                            if (descendentNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
                                ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) descendentNode;
                                builder.append("[");
                                builder.append(manifold.getPreferredDescriptionText(conceptNode.getConceptNid()));
                                builder.append("] ");
                            }
                        }
                        titleLabel.setText(builder.toString());
                        addToToolbarNoGrow(rootToolBar, expandButton, column++);
                    } else {
                      if (premiseType == PremiseType.STATED) {
                        editable = true;
                      }
                      rootPane.getStyleClass()
                                .add(StyleClasses.DEF_ROLE.toString());
                        StringBuilder builder = new StringBuilder();
                        builder.append("∃ (");
                        builder.append(manifold.getPreferredDescriptionText(roleNode.getTypeConceptNid()));
                        builder.append(")➞[");
                        for (LogicNode descendentNode : roleNode.getDescendents()) {
                            if (descendentNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
                                ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) descendentNode;
                                builder.append(manifold.getPreferredDescriptionText(conceptNode.getConceptNid()));
                            }
                        }
                        builder.append("]");
                        titleLabel.setText(builder.toString());
                        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
                        openConceptButton.setOnMouseClicked(this::handleShowRoleNodeClick);
                        addToToolbarNoGrow(rootToolBar, expandButton, column++);
                        addToToolbarNoGrowTopAlign(rootToolBar, openConceptButton, column++);
                    }

                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    }
                    break;
                }
                case NECESSARY_SET: {
                    NecessarySetNode necessarySet = (NecessarySetNode) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_NECESSARY_SET.toString());
                    titleLabel.setText(getConceptBeingDefinedText(
                            manifold.getPreferredDescriptionText(necessarySet.getNodeSemantic().getConceptNid())
                    ));
                    titleLabel.setGraphic(Iconography.TAXONOMY_ROOT_ICON.getIconographic());
                    int column = 0;
                    addToToolbarNoGrow(rootToolBar, expandButton, column++);
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    }
                    break;
                }
                case SUFFICIENT_SET: {
                    SufficientSetNode sufficientSet = (SufficientSetNode) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_SUFFICIENT_SET.toString());
                    titleLabel.setText(getConceptBeingDefinedText(
                            manifold.getPreferredDescriptionText(sufficientSet.getNodeSemantic().getConceptNid())));
                    titleLabel.setGraphic(Iconography.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic());
                    int column = 0;
                    addToToolbarNoGrow(rootToolBar, expandButton, column++);
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    }
                    break;
                }

                case DEFINITION_ROOT: {
                    RootNode root = (RootNode) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_ROOT.toString());
                    rootPane.setBorder(ROOT_BORDER);
                    titleLabel.setText(getConceptBeingDefinedText(null));
                    titleLabel.setGraphic(computeGraphic(expression.getConceptBeingDefinedNid(), false));
                    titleLabel.setContextMenu(getContextMenu());
                    int column = 0;
                    addToToolbarNoGrow(rootToolBar, expandButton, column++);
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                     Label formLabel = new Label("", Iconography.STATED.getIconographic());
                     formLabel.setTooltip(new Tooltip("Stated form"));
                     addToToolbarNoGrow(rootToolBar, formLabel, column++);
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    } else {
                        Label formLabel = new Label("", Iconography.INFERRED.getIconographic());
                        formLabel.setTooltip(new Tooltip("Inferred form"));
                        addToToolbarNoGrow(rootToolBar, formLabel, column++);
              }
                    break;
                }
                case LITERAL_FLOAT: {
                    LiteralNodeDouble literalNodeFloat = (LiteralNodeDouble) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_LITERAL.toString());
                    titleLabel.setText(Double.toString(literalNodeFloat.getLiteralValue()));
                    titleLabel.setGraphic(Iconography.LITERAL_NUMERIC.getIconographic());
                    int column = 0;
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                     Label formLabel = new Label("", Iconography.STATED.getIconographic());
                     formLabel.setTooltip(new Tooltip("Stated form"));
                     addToToolbarNoGrow(rootToolBar, formLabel, column++);
                    }
                    break;
                }

                case LITERAL_BOOLEAN: {
                    LiteralNodeBoolean literalNode = (LiteralNodeBoolean) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_LITERAL.toString());
                    titleLabel.setText(Boolean.toString(literalNode.getLiteralValue()));
                    titleLabel.setGraphic(Iconography.LITERAL_NUMERIC.getIconographic());
                    int column = 0;
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                     Label formLabel = new Label("", Iconography.STATED.getIconographic());
                     formLabel.setTooltip(new Tooltip("Stated form"));
                     addToToolbarNoGrow(rootToolBar, formLabel, column++);
                    }
                    break;
                }
                case LITERAL_INSTANT: {
                    LiteralNodeInstant literalNode = (LiteralNodeInstant) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_LITERAL.toString());
                    titleLabel.setText(DateTimeUtil.format(literalNode.getLiteralValue()));
                    titleLabel.setGraphic(Iconography.LITERAL_NUMERIC.getIconographic());
                    int column = 0;
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                       Label formLabel = new Label("", Iconography.STATED.getIconographic());
                     formLabel.setTooltip(new Tooltip("Stated form"));
                     addToToolbarNoGrow(rootToolBar, formLabel, column++);
                  }
                    break;
                }
                case LITERAL_INTEGER: {
                    LiteralNodeInteger literalNode = (LiteralNodeInteger) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_LITERAL.toString());
                    titleLabel.setText(Integer.toString(literalNode.getLiteralValue()));
                    titleLabel.setGraphic(Iconography.LITERAL_NUMERIC.getIconographic());
                    int column = 0;
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                     Label formLabel = new Label("", Iconography.STATED.getIconographic());
                     formLabel.setTooltip(new Tooltip("Stated form"));
                     addToToolbarNoGrow(rootToolBar, formLabel, column++);
                    }
                    break;
                }
                case LITERAL_STRING: {
                    LiteralNodeString literalNode = (LiteralNodeString) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_LITERAL.toString());
                    titleLabel.setText(literalNode.getLiteralValue());
                    titleLabel.setGraphic(Iconography.LITERAL_STRING.getIconographic());
                    int column = 0;
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                     Label formLabel = new Label("", Iconography.STATED.getIconographic());
                     formLabel.setTooltip(new Tooltip("Stated form"));
                     addToToolbarNoGrow(rootToolBar, formLabel, column++);
                    }
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Can't handle: " + logicNode);
            }

            rootPane.setPadding(new Insets(2, 0, 0, 0));
            rootPane.setTop(rootToolBar);
            setPseudoClasses(childBox);
            childBox.setBorder(CHILD_BOX_BORDER);
            childBox.setPadding(new Insets(0, 0, 0, INDENT_PIXELS));
            for (AbstractLogicNode childNode : logicNode.getChildren()) {
                if (childNode.getNodeSemantic() == NodeSemantic.AND) {
                    for (AbstractLogicNode andChildNode : childNode.getChildren()) {
                        ClauseView andChildClause = new ClauseView(andChildNode);
                        childClauses.add(andChildClause);
                    }
                } else {
                    ClauseView childClause = new ClauseView(childNode);
                    childClauses.add(childClause);
                }
            }

            childClauses.sort(new AxiomComparator());
            if (logicNode.getNodeSemantic() == NodeSemantic.ROLE_SOME
                    || logicNode.getNodeSemantic() == NodeSemantic.FEATURE) {
                expanded.set(false);
            } else {
                for (ClauseView childClause : childClauses) {
                    childBox.getChildren().add(childClause.rootPane);
                }
            }
            rootPane.setCenter(childBox);
            rootPane.setUserData(logicNode);
        }

        private void handleDragDetected(MouseEvent event) {
            System.out.println("Drag detected: " + event);

            DragImageMaker dragImageMaker = new DragImageMaker(titleLabel);
            Dragboard db = titleLabel.startDragAndDrop(TransferMode.COPY);

            db.setDragView(dragImageMaker.getDragImage());

            int conceptNid;
            switch (logicNode.getNodeSemantic()) {
                case CONCEPT:
                    ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) logicNode;
                    conceptNid = conceptNode.getConceptNid();
                    break;
                case SUFFICIENT_SET:
                case NECESSARY_SET:
                    conceptNid = logicNode.getNodeSemantic().getConceptNid();
                    break;
                case DEFINITION_ROOT:
                    conceptNid = logicNode.getNidForConceptBeingDefined();
                    break;
                case ROLE_SOME:
                    RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) logicNode;
                    conceptNid = roleNode.getTypeConceptNid();
                    break;
                case FEATURE:
                    FeatureNodeWithNids featureNode = (FeatureNodeWithNids) logicNode;
                    conceptNid = featureNode.getTypeConceptNid();
                    break;
                default:
                    conceptNid = logicNode.getNidForConceptBeingDefined();
            }

            IsaacClipboard content = new IsaacClipboard(Get.concept(conceptNid));
            db.setContent(content);
            event.consume();
        }

        private void handleDragDone(DragEvent event) {
            System.out.println("Dragging done: " + event);
            titleLabel.setBackground(originalBackground);
            this.transferMode = null;
        }

        private void handleDragEntered(DragEvent event) {
            if (editable) {
                System.out.println("Dragging entered: " + event);
                this.originalBackground = titleLabel.getBackground();

                Color backgroundColor;
                Set<DataFormat> contentTypes = event.getDragboard()
                        .getContentTypes();

                if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.CONCEPT_TYPES)) {
                    backgroundColor = Color.AQUA;
                    this.transferMode = TransferMode.COPY_OR_MOVE;
                } else if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.DESCRIPTION_TYPES)) {
                    backgroundColor = Color.OLIVEDRAB;
                    this.transferMode = TransferMode.COPY_OR_MOVE;
                } else {
                    backgroundColor = Color.RED;
                    this.transferMode = null;
                }

                BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);

                titleLabel.setBackground(new Background(fill));
            }
        }

        private void handleDragExited(DragEvent event) {
            System.out.println("Dragging exited: " + event);
            titleLabel.setBackground(originalBackground);
            this.transferMode = null;
        }

        private void handleDragOver(DragEvent event) {
            // System.out.println("Dragging over: " + event );
            if (this.transferMode != null) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        }

        private void toggleExpansion() {
            if (expanded.get()) {
                expandButton.setGraphic(Iconography.OPEN.getIconographic());
                for (ClauseView childClause : childClauses) {
                    childBox.getChildren().add(childClause.rootPane);
                }
            } else {
                expandButton.setGraphic(Iconography.CLOSE.getIconographic());
                childBox.getChildren().clear();
            }
        }

        protected final void handleEditClick(MouseEvent mouseEvent) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem doNothing = new MenuItem("");
            contextMenu.getItems().addAll(doNothing);

            List<Action> actionItems
                    = FxGet.rulesDrivenKometService().getEditLogicalExpressionNodeMenuItems(
                            manifold,
                            logicNode,
                            AxiomView.this.expression, this::updateExpressionForClauseView);

            if (!actionItems.isEmpty()) {
                contextMenu.getItems().add(new SeparatorMenuItem());
                for (Action action : actionItems) {
                    if (action instanceof ActionGroup) {
                        ActionGroup actionGroup = (ActionGroup) action;
                        Menu menu = ActionUtils.createMenu(action);
                        //menu.setGraphic(actionGroup.getGraphic());
                        for (Action actionInGroup : actionGroup.getActions()) {
                            if (actionInGroup == ActionUtils.ACTION_SEPARATOR) {
                                menu.getItems().add(new SeparatorMenuItem());
                            } else {
                                menu.getItems().add(ActionUtils.createMenuItem(actionInGroup));
                            }

                        }
                        contextMenu.getItems().add(menu);
                    } else {
                        if (action == ActionUtils.ACTION_SEPARATOR) {
                            contextMenu.getItems().add(new SeparatorMenuItem());
                        } else {
                            contextMenu.getItems().add(ActionUtils.createMenuItem(action));
                        }
                    }
                }
            }

            mouseEvent.consume();
            contextMenu.show(editButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }

        private void updateExpressionForClauseView(LogicalExpression expression) {
            updateExpressionForAxiomView(expression);
        }

        private void handleShowRoleNodeClick(MouseEvent mouseEvent) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) logicNode;
                showPopup(roleNode.getTypeConceptNid(), mouseEvent);
            }
        }

        private void handleShowFeatureNodeClick(MouseEvent mouseEvent) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                FeatureNodeWithNids featureNid = (FeatureNodeWithNids) logicNode;
                showPopup(featureNid.getTypeConceptNid(), mouseEvent);
            }
        }

        private void showPopup(int conceptNid, MouseEvent mouseEvent) {
            Optional<LogicalExpression> expression = manifold.getLogicalExpression(conceptNid, premiseType);
            if (expression.isPresent()) {
                popover = new PopOver();
                popover.setContentNode(AxiomView.createWithCommitPanel(expression.get(),
                        premiseType,
                        manifold));
                popover.setCloseButtonEnabled(true);
                popover.setHeaderAlwaysVisible(false);
                popover.setTitle("");
                popover.show(openConceptButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                mouseEvent.consume();
            }
        }

        private void handleShowConceptNodeClick(MouseEvent mouseEvent) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) logicNode;
                showPopup(conceptNode.getConceptNid(), mouseEvent);
            }
        }

        private void addSvg(StringBuilder builder, int depth, double xOffset, double yOffset) {

            Bounds rootBounds = rootPane.localToScreen(rootPane.getBoundsInLocal());
            String leftStroke = "stroke: #c3cdd3;";
            int textOffset = 5;
            int preTextIconWidth = 0;
            if (rootBounds != null) {
                double leftWidth = 1;
                String nodeText = titleLabel.getText();
                double bottomInset = 0;
                double childOffset = 0;
                switch (logicNode.getNodeSemantic()) {

                    case DEFINITION_ROOT:
                        preTextIconWidth = 23;
                        if (premiseType == PremiseType.STATED) {
                            builder.append("<use xlink:href=\"#stated\" x=\"");
                            builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset - 1) * 33);
                            builder.append("\" y=\"");
                            builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);

                            builder.append("\" style=\"fill: black; stroke: black; \"");

                            builder.append(" transform=\" scale(.03) \"/>");
                        } else {
                            builder.append("<use xlink:href=\"#inferred\" x=\"");
                            builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 100);
                            builder.append("\" y=\"");
                            builder.append((yOffset + rootBounds.getMinY() + textOffset + 4) * 100);

                            builder.append("\" style=\"fill: black; stroke: black; \"");

                            builder.append(" transform=\" scale(.01) \"/>");
                        }

                        break;
                    case NECESSARY_SET:
                        leftStroke = "stroke: #FF4E08;";
                        leftWidth = 4;
                        nodeText = "Necessary set";
                        bottomInset = 4;
                        preTextIconWidth = 20;
                        builder.append("<use xlink:href=\"#hexagon\" x=\"");
                        builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                        builder.append("\" y=\"");
                        builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);

                        builder.append("\" style=\"fill: white; stroke: #FF4E08; stroke-width: 50.0;\"");

                        builder.append(" transform=\" scale(.03) \"/>");
                        break;
                    case SUFFICIENT_SET:
                        leftStroke = "stroke: #5ec200;";
                        leftWidth = 4;
                        nodeText = "Sufficient set";
                        bottomInset = 4;
                        preTextIconWidth = 20;
                        builder.append("<use xlink:href=\"#circle\" x=\"");
                        builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                        builder.append("\" y=\"");
                        builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);

                        builder.append("\" style=\"fill: white; stroke: #5ec200; stroke-width: 50.0;\"");

                        builder.append(" transform=\" scale(.03) \"/>");
                        break;
                    case CONCEPT:
                        leftStroke = "stroke: #c3cdd3;";
                        leftWidth = 4;
                        bottomInset = 5;
                        preTextIconWidth = 20;

                        ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) logicNode;
                        boolean defined = isDefined(conceptNode.getConceptNid());
                        boolean multiParent = isMultiparent(conceptNode.getConceptNid());
                        if (defined) {
                            if (multiParent) {
                                builder.append("<use xlink:href=\"#arrow-circle\" ");
                                builder.append(" style=\"fill: #5ec200; stroke: #5ec200; \"");
                            } else {
                                builder.append("<use xlink:href=\"#circle\" ");
                                builder.append(" style=\"fill: #5ec200; stroke: #5ec200; \"");
                            }
                        } else {
                            if (multiParent) {
                                builder.append("<use xlink:href=\"#arrow-hexagon\" ");
                                builder.append(" style=\"fill: #FF4E08; stroke: #FF4E08; \"");
                            } else {
                                builder.append("<use xlink:href=\"#hexagon\" ");
                                builder.append(" style=\"fill: #FF4E08; stroke: #FF4E08; \"");
                            }
                        }

                        builder.append(" x=\"");
                        builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                        builder.append("\" y=\"");
                        builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);

                        builder.append("\" transform=\"scale(.03) \"/>");
                        break;
                    case ROLE_SOME:
                        RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) logicNode;
                        if (roleNode.getTypeConceptNid() == MetaData.ROLE_GROUP____SOLOR.getNid()) {
                            leftStroke = "stroke: #009bff;";
                            nodeText = "Role group";
                            bottomInset = 5;
                            childOffset = 3;
                            preTextIconWidth = 20;
                            builder.append("<use xlink:href=\"#role-group\" x=\"");
                            builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 25);
                            builder.append("\" y=\"");
                            builder.append((yOffset + rootBounds.getMinY() + textOffset) * 25);

                            builder.append("\" style=\"fill: black; stroke: black;\"");

                            builder.append(" transform=\" scale(.04) \"/>\n");

                        } else {
                            leftStroke = "stroke: #ff9100;";
                            bottomInset = 5;

                            StringBuilder roleStrBuilder = new StringBuilder();
                            roleStrBuilder.append("∃ (");

                            boolean roleDefined = isDefined(roleNode.getTypeConceptNid());
                            boolean roleMultiParent = isMultiparent(roleNode.getTypeConceptNid());
                            if (roleDefined) {
                                if (roleMultiParent) {
                                    roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF060; </tspan>\n<tspan dy=\"-1.5\" />");
                                } else {
                                    roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF12F; </tspan>\n<tspan dy=\"-1.5\" />");
                                }
                            } else {
                                if (roleMultiParent) {
                                    roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF061; </tspan>\n<tspan dy=\"-1.5\" />");
                                } else {
                                    roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF2D8; </tspan>\n<tspan dy=\"-1.5\" />");
                                }
                            }

                            roleStrBuilder.append(manifold.getPreferredDescriptionText(roleNode.getTypeConceptNid()));
                            roleStrBuilder.append(")➞[");

                            for (LogicNode descendentNode : roleNode.getDescendents()) {
                                if (descendentNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
                                    ConceptNodeWithNids roleRestrictionNode = (ConceptNodeWithNids) descendentNode;
                                    roleDefined = isDefined(roleRestrictionNode.getConceptNid());
                                    roleMultiParent = isMultiparent(roleRestrictionNode.getConceptNid());
                                    if (roleDefined) {
                                        if (roleMultiParent) {
                                            roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF060; </tspan>\n<tspan dy=\"-1.5\" />");
                                        } else {
                                            roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF12F; </tspan>\n<tspan dy=\"-1.5\" />");
                                        }
                                    } else {
                                        if (roleMultiParent) {
                                            roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF061; </tspan>\n<tspan dy=\"-1.5\" />");
                                        } else {
                                            roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF2D8; </tspan>\n<tspan dy=\"-1.5\" />");
                                        }
                                    }

                                    roleStrBuilder.append(manifold.getPreferredDescriptionText(roleRestrictionNode.getConceptNid()));
                                }
                            }
                            roleStrBuilder.append("]");
                            nodeText = roleStrBuilder.toString();

                        }

                        leftWidth = 4;
                        break;

                    default:
                }

                double topWidth = 1;
                double halfTopWidth = topWidth / 2;

                double bottomWidth = 1;
                double halfBottomWidth = bottomWidth / 2;

                double halfLeftWidth = leftWidth / 2;

                int rightLineExtra;
                if (depth == 0) {
                    rightLineExtra = 0;
                } else {
                    rightLineExtra = 1;
                }
                // Top
                addLine(builder, xOffset + rootBounds.getMinX(), yOffset + rootBounds.getMinY(),
                        xOffset + rootBounds.getMaxX() + rightLineExtra, yOffset + rootBounds.getMinY(), topWidth, "stroke: #c3cdd3;");

                if (depth == 0) {
                    // Right
                    addLine(builder, xOffset + rootBounds.getMaxX(), yOffset + rootBounds.getMinY(),
                            xOffset + rootBounds.getMaxX(), yOffset + rootBounds.getMaxY() - bottomInset, 1, "stroke: #c3cdd3;");
                }

                // Bottom
                addLine(builder, xOffset + rootBounds.getMaxX() + rightLineExtra, yOffset + rootBounds.getMaxY() - bottomInset,
                        xOffset + rootBounds.getMinX(), yOffset + rootBounds.getMaxY() - bottomInset, bottomWidth, "stroke: #c3cdd3;");

                // Left
                addLine(builder, xOffset + rootBounds.getMinX() + halfLeftWidth, yOffset + rootBounds.getMaxY() + halfTopWidth - bottomInset,
                        xOffset + rootBounds.getMinX() + halfLeftWidth, yOffset + rootBounds.getMinY() - halfBottomWidth, leftWidth, leftStroke);

                // Text
                addText(builder, xOffset + rootBounds.getMinX() + leftWidth + textOffset + preTextIconWidth,
                        yOffset + rootBounds.getMinY() + textOffset + 9,
                        nodeText,
                        "font-size: 9pt; font-family: Open Sans Condensed Light, Symbol, Material Design Icons; baseline-shift: sub;");

                for (ClauseView child : childClauses) {
                    child.addSvg(builder, depth + 1, xOffset, yOffset - childOffset);
                }
            }

        }

        private void addText(StringBuilder builder, double x, double y, String text, String style) {
            text = text.replace("➞", "<tspan style=\"font-family: Symbol;\">→</tspan>\n<tspan style=\"" + style + "\"/>\n");

            addText(builder, (int) x, (int) y, text, style);
        }

        private void addText(StringBuilder builder, int x, int y, String text, String style) {
            builder.append("    <text x=\"");
            builder.append(x);
            builder.append("\" y=\"");
            builder.append(y);
            builder.append("\" style=\"");
            builder.append(style);
            builder.append("\">");
            builder.append(text);
            builder.append("</text>\n");
        }

        private void addLine(StringBuilder builder, double x1, double y1, double x2, double y2, double width, String style) {
            builder.append("<line x1=\"");
            builder.append(x1);
            builder.append("\" y1=\"");
            builder.append(y1);
            builder.append("\" x2=\"");
            builder.append(x2);
            builder.append("\" y2=\"");
            builder.append(y2);
            builder.append("\" style=\"");
            builder.append(style);
            builder.append(" stroke-width: ");
            builder.append(width);
            builder.append(";\"/>\n");
        }

        private ContextMenu getContextMenu() {
            MenuItem svgItem = new MenuItem("Make concept svg");
            svgItem.setOnAction(this::makeSvg);
            MenuItem inlineSvgItem = new MenuItem("Make inline svg");
            inlineSvgItem.setOnAction(this::makeInlineSvg);
            MenuItem mediaObjectSvgItem = new MenuItem("Make media object svg");
            mediaObjectSvgItem.setOnAction(this::makeMediaObjectSvg);
            MenuItem glossaryEntryItem = new MenuItem("Make glossary entry");
            glossaryEntryItem.setOnAction(this::makeGlossaryEntry);
            MenuItem javaExpressionItem = new MenuItem("Make java expression");
            javaExpressionItem.setOnAction(this::makeJavaExpression);
            return new ContextMenu(svgItem, inlineSvgItem, mediaObjectSvgItem, 
                    glossaryEntryItem, javaExpressionItem);
        }
        
        private void makeJavaExpression(Event event) {
            putOnClipboard(AxiomView.this.expression.toBuilder());
            
        }

        private void makeMediaObjectSvg(Event event) {
            StringBuilder builder = new StringBuilder();
            builder.append("<mediaobject>\n");
            builder.append("       <imageobject>\n");
            builder.append("            <imagedata>\n");
            makeSvg(builder);
            builder.append("\n          </imagedata>");
            builder.append("\n     </imageobject>");
            builder.append("\n</mediaobject>");

            putOnClipboard(builder.toString());
        }

        private void makeGlossaryEntry(Event event) {
            StringBuilder builder = new StringBuilder();
            builder.append("<inlinemediaobject>\n");
            builder.append("                <imageobject>\n");
            builder.append("                    <imagedata>\n");
            makeSvg(builder);
            builder.append("\n                    </imagedata>");
            builder.append("\n                </imageobject>");
            builder.append("\n</inlinemediaobject>");

            putOnClipboard(DocBook.getGlossentry(expression.getConceptBeingDefinedNid(), manifold, builder.toString()));
        }

        private void makeInlineSvg(Event event) {
            StringBuilder builder = new StringBuilder();
            builder.append("<inlinemediaobject>\n");
            builder.append("                <imageobject>\n");
            builder.append("                    <imagedata>\n");
            makeSvg(builder);
            builder.append("\n                    </imagedata>");
            builder.append("\n                </imageobject>");
            builder.append("\n</inlinemediaobject>");

            putOnClipboard(builder.toString());
        }

        private void makeSvg(Event event) {

            StringBuilder builder = makeSvg(new StringBuilder());

            putOnClipboard(builder.toString());

        }

        private void putOnClipboard(String string) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(string);
            clipboard.setContent(content);
        }

        private StringBuilder makeSvg(StringBuilder builder) {
            Bounds rootBoundsInScreen = rootPane.localToScreen(borderPane.getBoundsInLocal());
            builder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"");
            builder.append(rootBoundsInScreen.getWidth() + 5);
            builder.append("px\" height=\"");
            builder.append(rootBoundsInScreen.getHeight() + 5);
            builder.append("px\">\n");
            builder.append("   <defs>\n");

            builder.append("<path id=\"arrow-circle\" d=\"M149,234h64v85h86v-85h64L256,127L149,234z M407,385c40-40,60.7-90.3,62-151c-1.3-60.7-22-111-62-151 ");
            builder.append("s-90.3-60.7-151-62c-60.7,1.3-111,22-151,62s-60.7,90.3-62,151c1.3,60.7,22,111,62,151s90.3,60.7,151,62 ");
            builder.append("C316.7,445.7,367,425,407,385z M135,355c-32-32-48.7-72.3-50-121c1.3-48.7,18-89,50-121s72.3-48.7,121-50c48.7,1.3,89,18,121,50 ");
            builder.append("s48.7,72.3,50,121c-1.3,48.7-18,89-50,121s-72.3,48.7-121,50C207.3,403.7,167,387,135,355z\"/>\n");
            builder.append("<path id=\"arrow-hexagon\" d=\"M133,227h64v85h86v-85h64L240,120L133,227z M432,131c-0.7-8.7-4.3-15-11-19L252,18c-3.3-2.7-7.3-4-12-4 ");
            builder.append("c-4.7,0-8.7,1.3-12,4L59,112c-6.7,4-10.3,10.3-11,19v192c0.7,8.7,4.3,15,11,19l169,94c3.3,2.7,7.3,4,12,4c4.7,0,8.7-1.3,12-4l169-94 ");
            builder.append("c6.7-4,10.3-10.3,11-19V131z M389,144v166l-149,84L91,310V144l149-84L389,144z\"/>\n");
            builder.append("<path  id=\"circle\" d=\"M256 21q-73 2 -121 50t-50 121q2 73 50 121t121 50q73 -2 121 -50t50 -121q-2 -73 -50 -121t-121 -50zM256 405q-91 -2 -151 -62t-62 -151q2 -91 62 -151t151 -62q91 2 151 62t62 151q-2 91 -62 151t-151 62z\"/> \n");
            builder.append("<path  id=\"hexagon\" d=\"M448 96q-1 -13 -11 -19l-169 -94q-5 -4 -12 -4t-12 4l-169 94q-10 6 -11 19v192q1 13 11 19l169 94q5 4 12 4t12 -4l169 -94q10 -6 11 -19v-192z\"/> \n");
            builder.append("<path  id=\"role-group\" d=\"M107 245l53 -96h-107zM64 363h85v-86h-85v86zM107 21q18 1 30 13t12 30t-12 30t-30 12t-30.5 -12t-12.5 -30t12.5 -30t30.5 -13zM192 341v-42h256v42h-256zM192 43h256v42h-256v-42zM192 171h256v42h-256v-42z\"/> \n");
            builder.append("<path id=\"stated\" d=\"M252.5,37c38.7,0,75,6.7,109,20c34,12.7,61,30.3,81,53c19.3,22.7,29,47,29,73s-9.7,50.3-29,73\n"
                    + "	c-20,22.7-47,40.3-81,53c-34,13.3-70.3,20-109,20c-11.3,0-23.7-0.7-37-2l-16-2l-13,11c-23.3,20.7-49.3,37-78,49\n"
                    + "	c8.7-15.3,15.3-31.7,20-49l7-28l-24-14c-25.3-14-44.7-30.7-58-50s-20-39.7-20-61c0-26,9.7-50.3,29-73c20-22.7,47-40.3,81-53\n"
                    + "	C177.5,43.7,213.8,37,252.5,37L252.5,37z M508.5,183c0-33.3-11.3-64-34-92c-23.3-28-54.3-50-93-66c-39.3-16.7-82.3-25-129-25\n"
                    + "	s-89.7,8.3-129,25c-38.7,16-69.7,38-93,66c-22.7,28-34,58.7-34,92c0,28.7,8.7,55.3,26,80c17.3,25.3,41,46.3,71,63\n"
                    + "	c-2.7,8-5.3,15.3-8,22s-5.3,12.3-8,17c-6,9.3-9,14-9,14l-9,12l-10,10c-4,5.3-6.7,8.7-8,10c-0.7,0-1.7,1-3,3l-2,2l0,0l-1,3\n"
                    + "	c-1.3,1.3-2,2.3-2,3v2c-0.7,2-0.7,3.3,0,4l0,0c0.7,3.3,2,6,4,8c2.7,2,5.3,3,8,3h2c12-1.3,22.7-3.3,32-6c50-12.7,94-35.7,132-69\n"
                    + "	c14,1.3,27.7,2,41,2c46.7,0,89.7-8.3,129-25c38.7-16,69.7-38,93-66C497.2,247,508.5,216.3,508.5,183L508.5,183z\"/>\n");
            builder.append("    <path id=\"inferred\" \n"
                    + "d=\"M896 640q0 106 -75 181t-181 75t-181 -75t-75 -181t75 -181t181 -75t181 75t75 181zM1664 128q0 52 -38 90t-90 38t-90 -38t-38 -90q0 -53 37.5 -90.5t90.5 -37.5t90.5 37.5t37.5 90.5zM1664 1152q0 52 -38 90t-90 38t-90 -38t-38 -90q0 -53 37.5 -90.5t90.5 -37.5\n"
                    + "t90.5 37.5t37.5 90.5zM1280 731v-185q0 -10 -7 -19.5t-16 -10.5l-155 -24q-11 -35 -32 -76q34 -48 90 -115q7 -11 7 -20q0 -12 -7 -19q-23 -30 -82.5 -89.5t-78.5 -59.5q-11 0 -21 7l-115 90q-37 -19 -77 -31q-11 -108 -23 -155q-7 -24 -30 -24h-186q-11 0 -20 7.5t-10 17.5\n"
                    + "l-23 153q-34 10 -75 31l-118 -89q-7 -7 -20 -7q-11 0 -21 8q-144 133 -144 160q0 9 7 19q10 14 41 53t47 61q-23 44 -35 82l-152 24q-10 1 -17 9.5t-7 19.5v185q0 10 7 19.5t16 10.5l155 24q11 35 32 76q-34 48 -90 115q-7 11 -7 20q0 12 7 20q22 30 82 89t79 59q11 0 21 -7\n"
                    + "l115 -90q34 18 77 32q11 108 23 154q7 24 30 24h186q11 0 20 -7.5t10 -17.5l23 -153q34 -10 75 -31l118 89q8 7 20 7q11 0 21 -8q144 -133 144 -160q0 -8 -7 -19q-12 -16 -42 -54t-45 -60q23 -48 34 -82l152 -23q10 -2 17 -10.5t7 -19.5zM1920 198v-140q0 -16 -149 -31\n"
                    + "q-12 -27 -30 -52q51 -113 51 -138q0 -4 -4 -7q-122 -71 -124 -71q-8 0 -46 47t-52 68q-20 -2 -30 -2t-30 2q-14 -21 -52 -68t-46 -47q-2 0 -124 71q-4 3 -4 7q0 25 51 138q-18 25 -30 52q-149 15 -149 31v140q0 16 149 31q13 29 30 52q-51 113 -51 138q0 4 4 7q4 2 35 20\n"
                    + "t59 34t30 16q8 0 46 -46.5t52 -67.5q20 2 30 2t30 -2q51 71 92 112l6 2q4 0 124 -70q4 -3 4 -7q0 -25 -51 -138q17 -23 30 -52q149 -15 149 -31zM1920 1222v-140q0 -16 -149 -31q-12 -27 -30 -52q51 -113 51 -138q0 -4 -4 -7q-122 -71 -124 -71q-8 0 -46 47t-52 68\n"
                    + "q-20 -2 -30 -2t-30 2q-14 -21 -52 -68t-46 -47q-2 0 -124 71q-4 3 -4 7q0 25 51 138q-18 25 -30 52q-149 15 -149 31v140q0 16 149 31q13 29 30 52q-51 113 -51 138q0 4 4 7q4 2 35 20t59 34t30 16q8 0 46 -46.5t52 -67.5q20 2 30 2t30 -2q51 71 92 112l6 2q4 0 124 -70\n"
                    + "q4 -3 4 -7q0 -25 -51 -138q17 -23 30 -52q149 -15 149 -31z\" />");
            builder.append("");
            builder.append("");
            builder.append("");
            builder.append("");
            builder.append("");

            builder.append("   </defs>\n");

            builder.append("    <g alignment-baseline=\"baseline\"></g>\n");
            addSvg(builder, 0, -rootBoundsInScreen.getMinX(), -rootBoundsInScreen.getMinY());
            builder.append("</svg>\n");
            return builder;
        }
    }

}
