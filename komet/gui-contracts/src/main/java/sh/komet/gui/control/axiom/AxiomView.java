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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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

        if (expression.getConceptNid() != -1
                && expression.getConceptNid() != MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            StringBuilder builder = new StringBuilder();
            if (prefix != null) {
                builder.append(prefix);
                builder.append(": ");
            }
            builder.append(manifold.getPreferredDescriptionText(expression.getConceptNid()));
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

    public final Node computeGraphic(int conceptNid, boolean expanded) {

        if (conceptNid == -1
                || conceptNid == MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            return Iconography.ALERT_CONFIRM2.getIconographic();
        }
        int[] parents = Get.taxonomyService().getSnapshot(manifold)
                .getTaxonomyTree().getParentNids(conceptNid);
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
        if (this.expression.getConceptNid() != -1) {
            Optional<LogicalExpression> committedExpression = manifold.getStatedLogicalExpression(this.expression.getConceptNid());
            if (committedExpression.isPresent()) {
                updateExpressionForAxiomView(committedExpression.get());
            }
        }
    }

    private void commitEdit(Event event) {

        LatestVersion<LogicGraphVersion> latestVersion = manifold.getStatedLogicGraphVersion(this.expression.getConceptNid());
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

            switch (logicNode.getNodeSemantic()) {
                case CONCEPT: {
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
                    titleLabel.setGraphic(computeGraphic(expression.getConceptNid(), false));
                    titleLabel.setContextMenu(getContextMenu());
                    int column = 0;
                    addToToolbarNoGrow(rootToolBar, expandButton, column++);
                    addToToolbarGrow(rootToolBar, titleLabel, column++);
                    if (premiseType == PremiseType.STATED) {
                        addToToolbarNoGrow(rootToolBar, Iconography.STATED.getIconographic(), column++);
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
                    } else {
                        addToToolbarNoGrow(rootToolBar, Iconography.INFERRED.getIconographic(), column++);
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
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
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
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
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
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
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
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
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
                        addToToolbarNoGrow(rootToolBar, editButton, column++);
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
            if (rootBounds != null) {
                int textOffset = 5;
                double topWidth = 1;
                double halfTopWidth = topWidth / 2;

                double bottomWidth = 1;
                double halfBottomWidth = bottomWidth / 2;

                double leftWidth = 10;
                double halfLeftWidth = leftWidth / 2;

                int rightLineExtra;
                if (depth == 0) {
                    rightLineExtra = 0;
                } else {
                    rightLineExtra = 1;
                }

                // Top
                addLine(builder, xOffset + rootBounds.getMinX(), yOffset + rootBounds.getMinY(),
                        xOffset + rootBounds.getMaxX() + rightLineExtra, yOffset + rootBounds.getMinY(), topWidth, "stroke: black;");

                if (depth == 0) {
                    // Right
                    addLine(builder, xOffset + rootBounds.getMaxX(), yOffset + rootBounds.getMinY(),
                            xOffset + rootBounds.getMaxX(), yOffset + rootBounds.getMaxY(), 1, "stroke: black;");
                }

                // Bottom
                addLine(builder, xOffset + rootBounds.getMaxX() + rightLineExtra, yOffset + rootBounds.getMaxY(),
                        xOffset + rootBounds.getMinX(), yOffset + rootBounds.getMaxY(), bottomWidth, "stroke: black;");

                // Left
                addLine(builder, xOffset + rootBounds.getMinX() + halfLeftWidth, yOffset + rootBounds.getMaxY() + halfTopWidth,
                        xOffset + rootBounds.getMinX() + halfLeftWidth, yOffset + rootBounds.getMinY() - halfBottomWidth, leftWidth, "stroke: red;");

                // Text
                addText(builder, xOffset + rootBounds.getMinX() + leftWidth + textOffset, yOffset + rootBounds.getMinY() + textOffset + 5, titleLabel.getText(),
                        "font-size: 9pt; font-family: Open Sans Light, Symbol; baseline-shift: sub;");

                for (ClauseView child : childClauses) {
                    child.addSvg(builder, depth + 1, xOffset, yOffset);
                }
            }

        }

        private void addText(StringBuilder builder, double x, double y, String text, String style) {
            text = text.replace("➞", "<tspan style=\"font-family: Symbol;\">→</tspan><tspan style=\"" + style + "\"/>");
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
            return new ContextMenu(svgItem);
        }

        private void makeSvg(StringBuilder builder, int depth, double xOffset, double yOffset) {
            addSvg(builder, depth + 1, xOffset, yOffset);

        }

        private void makeSvg(Event event) {
            StringBuilder builder = new StringBuilder();
            //builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            //builder.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");

            Bounds rootBoundsInScreen = rootPane.localToScreen(borderPane.getBoundsInLocal());
            builder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"");
            builder.append(rootBoundsInScreen.getWidth() + 5);
            builder.append("px\" height=\"");
            builder.append(rootBoundsInScreen.getHeight() + 5);
            builder.append("px\">\n");
            builder.append("    <g alignment-baseline=\"baseline\"></g>\n");

            addSvg(builder, 0, -rootBoundsInScreen.getMinX(), -rootBoundsInScreen.getMinY());

            builder.append("</svg>\n");

            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(builder.toString());
            clipboard.setContent(content);

        }
    }

}
