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
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.AbstractLogicNode;
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
            Get.commitService().commit(mutableVersion, manifold.getEditCoordinate(), "Axiom view graph edit");
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
                    } else {
                        rootPane.getStyleClass()
                                .add(StyleClasses.DEF_ROLE.toString());
                        StringBuilder builder = new StringBuilder();
                        builder.append(" (");
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
                        addToToolbarNoGrowTopAlign(rootToolBar, openConceptButton, column++);
                    }

                    addToToolbarNoGrow(rootToolBar, expandButton, column++);
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
                case FEATURE: {
                    FeatureNodeWithNids featureNode = (FeatureNodeWithNids) logicNode;
                    rootPane.getStyleClass()
                            .add(StyleClasses.DEF_FEATURE.toString());
                    throw new UnsupportedOperationException();
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
            if (logicNode.getNodeSemantic() == NodeSemantic.ROLE_SOME) {
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
    }

}
