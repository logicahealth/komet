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
package sh.isaac.provider.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.model.logic.ConcreteDomainOperators;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.model.logic.node.internal.TypedNodeWithNids;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.manifold.HistoryRecord;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class AddEditLogicalExpressionNodeMenuItems {

    private static final String DELETE = "Delete";

    final List<Action> actionItems = new ArrayList<>();
    final Manifold manifold;
    final LogicNode nodeToEdit;
    final LogicalExpressionImpl expressionContiningNode;
    final Consumer<LogicalExpression> expressionUpdater;
    private PopOver popOver;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;
    private MouseEvent mouseEvent;

    public AddEditLogicalExpressionNodeMenuItems(Manifold manifold,
            LogicNode nodeToEdit,
            LogicalExpression expressionContiningNode,
            Consumer<LogicalExpression> expressionUpdater,
            MouseEvent mouseEvent) {
        this.manifold = manifold;
        this.nodeToEdit = nodeToEdit;
        this.expressionContiningNode = (LogicalExpressionImpl) expressionContiningNode;
        this.expressionUpdater = expressionUpdater;
        this.mouseEvent = mouseEvent;
    }

    public void sortActionItems() {
        AtomicBoolean deleteFound = new AtomicBoolean(false);
        actionItems.sort((o1, o2) -> {
            if (o1.getText().equalsIgnoreCase(DELETE)) {
                deleteFound.set(true);
                return 1;
            }
            if (o2.getText().equalsIgnoreCase(DELETE)) {
                deleteFound.set(true);
                return -1;
            }

            return o1.getText().toLowerCase().compareTo(o2.getText().toLowerCase());
        });

        if (deleteFound.get()) {
            for (int i = 0; i < actionItems.size(); i++) {
                if (actionItems.get(i).getText().equalsIgnoreCase(DELETE)) {
                    actionItems.add(i, ActionUtils.ACTION_SEPARATOR);
                    break;
                }
            }
        }

    }

    public void addGenericRoleAction() {
        addRoleAction(MetaData.ROLE____SOLOR, MetaData.HEALTH_CONCEPT____SOLOR);
    }

    public List<Action> getActionItems() {
        return actionItems;
    }

    public LogicNode getNodeToEdit() {
        return nodeToEdit;
    }

    public int getNodeToEditTypeConceptNid() {
        if (nodeToEdit instanceof TypedNodeWithNids) {
            TypedNodeWithNids typedNode = (TypedNodeWithNids) nodeToEdit;
            return typedNode.getTypeConceptNid();
        }
        return Integer.MAX_VALUE;
    }

    public NodeSemantic getNodeSemantic() {
        return nodeToEdit.getNodeSemantic();
    }

    public boolean containsNodeSemantic(NodeSemantic nodeSemantic) {
        return expressionContiningNode.contains(nodeSemantic);
    }

    public LogicalExpression getExpressionContiningNode() {
        return expressionContiningNode;
    }

    public void addDeleteNodeAction() {
        Action deleteAction = new Action(DELETE, (ActionEvent event) -> {
            this.expressionUpdater.accept(expressionContiningNode.removeNode(nodeToEdit));
        });
        actionItems.add(deleteAction);
    }

    public void addIsaNodeAction() {
        addIsaNodeAction(MetaData.METADATA____SOLOR);
    }

    public void addNecessarySetAction() {
        if (nodeToEdit.getNodeSemantic() != NodeSemantic.DEFINITION_ROOT) {
            throw new IllegalStateException("Node must be of type " + NodeSemantic.DEFINITION_ROOT);
        }
        Action addNecessarySetAction = new Action("Add necessary set", (ActionEvent event) -> {
            NecessarySetNode necessarySetNode = new NecessarySetNode(expressionContiningNode, new AndNode(expressionContiningNode));
            nodeToEdit.addChildren(necessarySetNode);
            this.expressionUpdater.accept(expressionContiningNode);
        });
        actionItems.add(addNecessarySetAction);
    }

    public void addSufficientSetAction() {
        if (this.nodeToEdit.getNodeSemantic() != NodeSemantic.DEFINITION_ROOT) {
            throw new IllegalStateException("Node must be of type " + NodeSemantic.DEFINITION_ROOT);
        }
        Action addSufficientSetAction = new Action("Add sufficient set", (ActionEvent event) -> {
            SufficientSetNode sufficientSetNode = new SufficientSetNode(this.expressionContiningNode, new AndNode(expressionContiningNode));
            this.nodeToEdit.addChildren(sufficientSetNode);
            this.expressionUpdater.accept(expressionContiningNode);
        });
        actionItems.add(addSufficientSetAction);
    }

    public void addRoleGroupAction() {
        Action addSufficientSetAction = new Action("Add role group", (ActionEvent event) -> {
            AndNode andNode = new AndNode(expressionContiningNode);
            RoleNodeSomeWithNids newRoleGroup = expressionContiningNode.SomeRole(MetaData.ROLE_GROUP____SOLOR.getNid(), andNode);
            this.nodeToEdit.addChildren(newRoleGroup);

            RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(MetaData.ROLE____SOLOR.getNid(), expressionContiningNode.Concept(MetaData.HEALTH_CONCEPT____SOLOR.getNid()));

            andNode.addChildren(newRole);

            this.expressionUpdater.accept(expressionContiningNode);
        });
        actionItems.add(addSufficientSetAction);
    }

    public void addIsaNodeAction(ConceptSpecification spec) {
        addIsaNodeAction(spec.getNid());
    }

    public void addRoleWithRestrictionsAction(ConceptSpecification roleType, ConceptSpecification assemblageWithRestrictions) {
        ActionGroup newRoleGroup = new ActionGroup("Add " + manifold.getPreferredDescriptionText(roleType) + "...");
        NidSet semanticNids = Get.assemblageService().getSemanticNidsFromAssemblage(assemblageWithRestrictions.getNid());
        SemanticSnapshotService<SemanticVersion> snapshot = Get.assemblageService().getSnapshot(SemanticVersion.class, manifold);
        for (int semanticNid : semanticNids.asArray()) {
            LatestVersion<SemanticVersion> latestMembership = snapshot.getLatestSemanticVersion(semanticNid);
            if (latestMembership.isPresent() && latestMembership.get().isActive()) {
                int restrictionNid = latestMembership.get().getReferencedComponentNid();
                Action newRoleAction = new Action(manifold.getPreferredDescriptionText(restrictionNid), (ActionEvent event) -> {
                    RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(roleType.getNid(), expressionContiningNode.Concept(restrictionNid));
                    for (LogicNode node : nodeToEdit.getChildren()) {
                        if (node.getNodeSemantic() == NodeSemantic.AND) {
                            node.addChildren(newRole);
                            break;
                        }
                    }
                    this.expressionUpdater.accept(expressionContiningNode);
                });
                newRoleGroup.getActions().add(newRoleAction);
            }
        }
        actionItems.add(newRoleGroup);
    }

    public void changeRoleRestrictionToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.ROLE_SOME) {

            for (String groupName : Manifold.getGroupNames()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = new ActionGroup("Change restriction using " + groupName + " history", Manifold.getIconographic(groupName), actions);
                for (HistoryRecord historyRecord : Manifold.getGroupHistory(groupName)) {
                    Action addIsaAction = new Action("Change role restriction to " + manifold.getPreferredDescriptionText(historyRecord.getComponentId()), (ActionEvent event) -> {
                        for (LogicNode node : nodeToEdit.getChildren()) {
                            if (node.getNodeSemantic() == NodeSemantic.CONCEPT) {
                                ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) node;
                                conceptNode.setConceptNid(historyRecord.getComponentId());
                                this.expressionUpdater.accept(expressionContiningNode);
                                break;
                            }
                        }
                    });
                    actionGroup.getActions().add(addIsaAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }

    public void changeRoleTypeToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.ROLE_SOME) {

            for (String groupName : Manifold.getGroupNames()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = new ActionGroup("Change type using " + groupName + " history", Manifold.getIconographic(groupName), actions);
                for (HistoryRecord historyRecord : Manifold.getGroupHistory(groupName)) {
                    Action addAction = new Action("Change role type to " + manifold.getPreferredDescriptionText(historyRecord.getComponentId()), (ActionEvent event) -> {
                        RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) this.nodeToEdit;
                        roleNode.setTypeConceptNid(historyRecord.getComponentId());
                        this.expressionUpdater.accept(expressionContiningNode);
                    });
                    actionGroup.getActions().add(addAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }

    public void changeFeatureTypeToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.FEATURE) {

            for (String groupName : Manifold.getGroupNames()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = new ActionGroup("Change type using " + groupName + " history", Manifold.getIconographic(groupName), actions);
                for (HistoryRecord historyRecord : Manifold.getGroupHistory(groupName)) {
                    Action addAction = new Action("Change feature type to " + manifold.getPreferredDescriptionText(historyRecord.getComponentId()), (ActionEvent event) -> {
                        FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.nodeToEdit;
                        featureNode.setTypeConceptNid(historyRecord.getComponentId());
                        this.expressionUpdater.accept(expressionContiningNode);
                    });
                    actionGroup.getActions().add(addAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }

    public void changeConceptToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.CONCEPT) {

            for (String groupName : Manifold.getGroupNames()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = new ActionGroup("Change concept using " + groupName + " history", Manifold.getIconographic(groupName), actions);
                for (HistoryRecord historyRecord : Manifold.getGroupHistory(groupName)) {
                    Action addIsaAction = new Action("Change to " + manifold.getPreferredDescriptionText(historyRecord.getComponentId()), (ActionEvent event) -> {
                        ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) this.nodeToEdit;
                        conceptNode.setConceptNid(historyRecord.getComponentId());
                        this.expressionUpdater.accept(expressionContiningNode);
                    });
                    actionGroup.getActions().add(addIsaAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.CONCEPT");
        }
    }

    public void addSearchIsa() {
        Action addIsaUsingSearch = new Action("Add is-a using new search selection...", (ActionEvent event) -> {
            showFindIsaPopup();
        });
        actionItems.add(addIsaUsingSearch);
    }

    private void showFindIsaPopup() {
        this.popOver = new PopOver();
        this.popOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
        this.popOver.getRoot().getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
        this.popOver.setCloseButtonEnabled(true);
        this.popOver.setHeaderAlwaysVisible(false);
        this.popOver.setTitle("");
        this.popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
        ConceptSearchNodeFactory searchNodeFactory = Get.service(ConceptSearchNodeFactory.class);
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(manifold, null);
        Node searchNode = searchExplorationNode.getNode();
        this.findSelectedConceptSpecification = searchExplorationNode.selectedConceptSpecification();
        BorderPane searchBorder = new BorderPane(searchNode);
        Button addSelection = new Button("set");
        addSelection.setOnAction(this::setToFindSelection);
        ToolBar popOverToolbar = new ToolBar(addSelection);
        searchBorder.setTop(popOverToolbar);
        searchBorder.setPrefSize(500, 400);
        searchBorder.setMinSize(500, 400);
        this.popOver.setContentNode(searchBorder);
        this.popOver.show(mouseEvent.getPickResult().getIntersectedNode());
        searchExplorationNode.focusOnInput();
    }

    private void setToFindSelection(ActionEvent event) {
        if (this.popOver != null) {
            this.popOver.hide(Duration.ZERO);
        }
        if (this.findSelectedConceptSpecification.get() != null) {
            ConceptNodeWithNids newIsa = expressionContiningNode.Concept(this.findSelectedConceptSpecification.get());
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newIsa);
                    break;
                }
            }
            this.expressionUpdater.accept(expressionContiningNode);
        }
    }

    public void addRecentSelectionIsa() {

        // create action group for each 
        for (String groupName : Manifold.getGroupNames()) {
            List<Action> actions = new ArrayList<>();
            ActionGroup actionGroup = new ActionGroup("Add is-a using " + groupName + " history", Manifold.getIconographic(groupName), actions);
            for (HistoryRecord historyRecord : Manifold.getGroupHistory(groupName)) {
                Action addIsaAction = new Action("Add is-a " + manifold.getPreferredDescriptionText(historyRecord.getComponentId()), (ActionEvent event) -> {
                    ConceptNodeWithNids newIsa = expressionContiningNode.Concept(historyRecord.getComponentId());
                    for (LogicNode node : nodeToEdit.getChildren()) {
                        if (node.getNodeSemantic() == NodeSemantic.AND) {
                            node.addChildren(newIsa);
                            break;
                        }
                    }
                    this.expressionUpdater.accept(expressionContiningNode);
                });
                actionGroup.getActions().add(addIsaAction);
            }
            if (!actionGroup.getActions().isEmpty()) {
                actionItems.add(actionGroup);
            }
        }
    }

    public void addIsaNodeAction(int conceptNid) {
        Action addIsaAction = new Action("Add is-a " + manifold.getPreferredDescriptionText(conceptNid), (ActionEvent event) -> {
            ConceptNodeWithNids newIsa = expressionContiningNode.Concept(conceptNid);
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newIsa);
                    break;
                }
            }
            this.expressionUpdater.accept(expressionContiningNode);
        });
        actionItems.add(addIsaAction);
    }

    public void addRoleAction(ConceptSpecification typeSpec, ConceptSpecification restrictionSpec) {
        addRoleAction(typeSpec.getNid(), restrictionSpec.getNid());
    }

    public void addRoleAction(int typeNid, int restrictionNid) {
        StringBuilder builder = new StringBuilder();
        builder.append("Add  (");
        builder.append(manifold.getPreferredDescriptionText(typeNid));
        builder.append(")➞[");
        builder.append(manifold.getPreferredDescriptionText(restrictionNid));
        builder.append("]");
        Action addNewRoleAction = new Action(builder.toString(), (ActionEvent event) -> {
            RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(typeNid, expressionContiningNode.Concept(restrictionNid));
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newRole);
                    break;
                }
            }
            this.expressionUpdater.accept(expressionContiningNode);
        });
        actionItems.add(addNewRoleAction);
    }

    public void addFloatFeatureAction(ConceptSpecification typeSpec, ConceptSpecification measureSemanticNid, ConcreteDomainOperators operator) {
        addFloatFeatureAction(typeSpec.getNid(), measureSemanticNid.getNid(), operator);
    }

    public void addFloatFeatureAction(int typeNid, int measureSemanticNid, ConcreteDomainOperators operator) {
        StringBuilder builder = new StringBuilder();
        builder.append("Add ⒡ ");
        builder.append(manifold.getPreferredDescriptionText(typeNid));
        builder.append(" ");
        builder.append(operator);
        builder.append(" 0.0 ");
        builder.append(manifold.getPreferredDescriptionText(measureSemanticNid));
        Action addFeatureAction = new Action(builder.toString(), (ActionEvent event) -> {
            FeatureNodeWithNids newRole = expressionContiningNode.Feature(typeNid,
                    measureSemanticNid, operator, expressionContiningNode.DoubleLiteral(0.0));
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newRole);
                    break;
                }
            }
            this.expressionUpdater.accept(expressionContiningNode);
        });
        actionItems.add(addFeatureAction);
    }

    @Override
    public String toString() {
        return "AddEditLogicalExpressionNodeMenuItems{nodeToEdit: " + nodeToEdit + ", expression: " + expressionContiningNode + '}';
    }

}
