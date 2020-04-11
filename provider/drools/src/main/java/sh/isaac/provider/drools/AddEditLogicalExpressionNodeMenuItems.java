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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.api.logic.ConcreteDomainOperators;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.LiteralNodeDouble;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.model.logic.node.internal.TypedNodeWithNids;
import sh.komet.gui.CatchThrowableEventHandler;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.control.PropertySheetItemFloatWrapper;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

            return NaturalOrder.compareStrings(o1.getText(), o2.getText());
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
        Action deleteAction = new Action(DELETE, new CatchThrowableEventHandler((ActionEvent event) -> {
            expressionContiningNode.removeNode(nodeToEdit);
            updateExpression();
        }));
        actionItems.add(deleteAction);
    }

    public void addIsaNodeAction() {
        addIsaNodeAction(MetaData.METADATA____SOLOR);
    }

    public void addNecessarySetAction() {
        if (nodeToEdit.getNodeSemantic() != NodeSemantic.DEFINITION_ROOT) {
            throw new IllegalStateException("Node must be of type " + NodeSemantic.DEFINITION_ROOT);
        }
        Action addNecessarySetAction = new Action("Add necessary set", new CatchThrowableEventHandler((ActionEvent event) -> {
            NecessarySetNode necessarySetNode = new NecessarySetNode(expressionContiningNode, new AndNode(expressionContiningNode));
            nodeToEdit.addChildren(necessarySetNode);
            updateExpression();
        }));
        actionItems.add(addNecessarySetAction);
    }

    public void addSufficientSetAction() {
        if (this.nodeToEdit.getNodeSemantic() != NodeSemantic.DEFINITION_ROOT) {
            throw new IllegalStateException("Node must be of type " + NodeSemantic.DEFINITION_ROOT);
        }
        Action addSufficientSetAction = new Action("Add sufficient set", new CatchThrowableEventHandler((ActionEvent event) -> {
            SufficientSetNode sufficientSetNode = new SufficientSetNode(this.expressionContiningNode, new AndNode(expressionContiningNode));
            this.nodeToEdit.addChildren(sufficientSetNode);
            updateExpression();
        }));
        actionItems.add(addSufficientSetAction);
    }

    public void addRoleGroupAction() {
        Action addRoleGroupAction = new Action("Add role group", new CatchThrowableEventHandler((ActionEvent event) -> {
            AndNode andNode = new AndNode(expressionContiningNode);
            RoleNodeSomeWithNids newRoleGroup = expressionContiningNode.SomeRole(MetaData.ROLE_GROUP____SOLOR.getNid(), andNode);
            LogicNode nodeToAddTo = this.nodeToEdit;
            if (nodeToAddTo.getNodeSemantic() != NodeSemantic.AND) {
                if (nodeToAddTo.getNodeSemantic() == NodeSemantic.SUFFICIENT_SET ||
                        nodeToAddTo.getNodeSemantic() == NodeSemantic.NECESSARY_SET) {
                    if (nodeToAddTo.getChildren().length != 1) {
                        throw new IllegalStateException(nodeToAddTo.getNodeSemantic() + " must have an AND node child");
                    }
                    nodeToAddTo = nodeToAddTo.getChildren()[0];
                    if (nodeToAddTo.getNodeSemantic() != NodeSemantic.AND) {
                        throw new IllegalStateException(nodeToAddTo.getNodeSemantic() + " must be an AND node: " + nodeToAddTo);
                    }
                }
            }

            nodeToAddTo.addChildren(newRoleGroup);

            RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(MetaData.ROLE____SOLOR.getNid(), expressionContiningNode.Concept(MetaData.HEALTH_CONCEPT____SOLOR.getNid()));

            andNode.addChildren(newRole);

            updateExpression();
        }));
        actionItems.add(addRoleGroupAction);
    }

    public void addIsaNodeAction(ConceptSpecification spec) {
        addIsaNodeAction(spec.getNid());
    }

    public void addRoleWithRestrictionsAction(ConceptSpecification roleType, ConceptSpecification assemblageWithRestrictions) {
        ActionGroup newRoleGroup = new ActionGroup("Add " + manifold.getPreferredDescriptionText(roleType) + "...");
        NidSet semanticNids = Get.assemblageService().getSemanticNidsFromAssemblage(assemblageWithRestrictions.getNid());
        SemanticSnapshotService<SemanticVersion> snapshot = Get.assemblageService().getSnapshot(SemanticVersion.class, manifold.getStampFilter());
        for (int semanticNid : semanticNids.asArray()) {
            LatestVersion<SemanticVersion> latestMembership = snapshot.getLatestSemanticVersion(semanticNid);
            if (latestMembership.isPresent() && latestMembership.get().isActive()) {
                int restrictionNid = latestMembership.get().getReferencedComponentNid();
                Action newRoleAction = new Action(manifold.getPreferredDescriptionText(restrictionNid), new CatchThrowableEventHandler((ActionEvent event) -> {
                    RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(roleType.getNid(), expressionContiningNode.Concept(restrictionNid));
                    for (LogicNode node : nodeToEdit.getChildren()) {
                        if (node.getNodeSemantic() == NodeSemantic.AND) {
                            node.addChildren(newRole);
                            break;
                        }
                    }
                    updateExpression();
                }));
                newRoleGroup.getActions().add(newRoleAction);
            }
        }
        actionItems.add(newRoleGroup);
    }

    public void changeRoleRestrictionToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.ROLE_SOME) {

            for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change restriction from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
                for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                    Action addIsaAction = new Action("Change role restriction to " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        for (LogicNode node : nodeToEdit.getChildren()) {
                            if (node.getNodeSemantic() == NodeSemantic.CONCEPT) {
                                ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) node;
                                conceptNode.setConceptNid(historyRecord.getNid());
                                updateExpression();
                                break;
                            }
                        }
                    }));
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

    private ActionGroup newActionGroup(String text, Optional<Node> optionalIcon, List<Action> actions) {
        if (optionalIcon.isPresent()) {
            return new ActionGroup(text, optionalIcon.get(), actions);
        }
        return new ActionGroup(text, actions);
    }

    public void changeRoleTypeToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.ROLE_SOME) {

            for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change type from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
                for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                    Action addAction = new Action("Change role type to " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) this.nodeToEdit;
                        roleNode.setTypeConceptNid(historyRecord.getNid());
                        updateExpression();
                    }));
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
    public void changeFeatureTypeToNewSearchSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.FEATURE) {
            Action changeFeatureTypeToSearchSelection = new Action("Change feature type...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeFeatureTypeToNewSearchSelection, "Search for feature type replacement");
            }));
            actionItems.add(changeFeatureTypeToSearchSelection);
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }
    private void changeFeatureTypeToNewSearchSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            FeatureNodeWithNids featureNode = (FeatureNodeWithNids) nodeToEdit;
            featureNode.setTypeConceptNid(this.findSelectedConceptSpecification.get().getNid());
            updateExpression();
        }
    }
    public void changeFeatureUnitsToNewSearchSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.FEATURE) {
            Action changeFeatureUnitsToSearchSelection = new Action("Change feature units...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeFeatureUnitsToNewSearchSelection, "Search for feature units replacement");
            }));
            actionItems.add(changeFeatureUnitsToSearchSelection);
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }
    private void changeFeatureUnitsToNewSearchSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            FeatureNodeWithNids featureNode = (FeatureNodeWithNids) nodeToEdit;
            featureNode.setMeasureSemanticNid(this.findSelectedConceptSpecification.get().getNid());
            updateExpression();
        }
    }
    public void changeFeatureUnitsToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.FEATURE) {

            for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change feature units from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
                for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                    Action addAction = new Action("Change feature units to " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.nodeToEdit;
                        featureNode.setMeasureSemanticNid(historyRecord.getNid());
                        updateExpression();
                    }));
                    actionGroup.getActions().add(addAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.FEATURE");
        }
    }

    public void changeFeatureRelationalOperator() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.FEATURE) {
            List<Action> actions = new ArrayList<>();
            ActionGroup actionGroup = new ActionGroup("Change feature relational operator",  actions);
            for (ConcreteDomainOperators operator: ConcreteDomainOperators.values()) {
                Action addAction = new Action("Change relational operator to " + operator, new CatchThrowableEventHandler((ActionEvent event) -> {
                    FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.nodeToEdit;
                    featureNode.setOperator(operator);
                    updateExpression();
                }));
                actionGroup.getActions().add(addAction);
            }
            actionItems.add(actionGroup);
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.FEATURE");
        }
    }

    public void changeFeatureTypeToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.FEATURE) {

            for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change feature type from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
                for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                    Action addAction = new Action("Change feature type to " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.nodeToEdit;
                        featureNode.setTypeConceptNid(historyRecord.getNid());
                        updateExpression();
                    }));
                    actionGroup.getActions().add(addAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.FEATURE");
        }
    }

    public void changeConceptToRecentSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.CONCEPT) {

            for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change concept from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
                for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                    Action addIsaAction = new Action("Change to " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) this.nodeToEdit;
                        conceptNode.setConceptNid(historyRecord.getNid());
                        updateExpression();
                    }));
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

    public void changeConceptToNewSearchSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.CONCEPT) {
            Action addIsaUsingSearch = new Action("Change concept...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeConceptToFindSelection, "Search for concept replacement");
            }));
            actionItems.add(addIsaUsingSearch);
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.CONCEPT");
        }
    }
    private void changeConceptToFindSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            ConceptNodeWithNids conceptNodeWithNids = (ConceptNodeWithNids) this.nodeToEdit;
            conceptNodeWithNids.setConceptNid(this.findSelectedConceptSpecification.get().getNid());
            updateExpression();
        }
    }
    public void changeRoleRestrictionToSearchSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.ROLE_SOME) {
            Action changeRoleRestrictionToSearchSelection = new Action("Change restriction...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeRoleRestrictionToSearchSelection, "Search for restriction replacement");
            }));
            actionItems.add(changeRoleRestrictionToSearchSelection);
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }
    private void changeRoleRestrictionToSearchSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.CONCEPT) {
                    ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) node;
                    conceptNode.setConceptNid(this.findSelectedConceptSpecification.get().getNid());
                    updateExpression();
                    break;
                }
            }
        }
    }

    private void hidePopover() {
        if (this.popOver != null) {
            this.popOver.hide(Duration.seconds(0.5));
        }
    }

    public void changeRoleTypeToSearchSelection() {
        if (this.nodeToEdit.getNodeSemantic() == NodeSemantic.ROLE_SOME) {
            Action changeRoleTypeToSearchSelection = new Action("Change type...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeRoleTypeToSearchSelection, "Search for type replacement");
            }));
            actionItems.add(changeRoleTypeToSearchSelection);
        } else {
            throw new IllegalStateException(this.nodeToEdit + " getNodeSemantic() == NodeSemantic.ROLE_SOME");
        }
    }

    private void changeRoleTypeToSearchSelection(ActionEvent event) {
        RoleNodeSomeWithNids roleNode = (RoleNodeSomeWithNids) this.nodeToEdit;
        roleNode.setTypeConceptNid(this.findSelectedConceptSpecification.get().getNid());
        updateExpression();
    }

    public void changeFeatureValue() {
        Action changeValue = new Action("Change feature value...", new CatchThrowableEventHandler((ActionEvent event) -> {

            FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.nodeToEdit;
            LiteralNodeDouble floatNode = (LiteralNodeDouble) featureNode.getOnlyChild();
            featureNode.getOnlyChild();
            SimpleFloatProperty floatProperty = new SimpleFloatProperty();
            floatProperty.set((float) floatNode.getLiteralValue());
            floatProperty.addListener((observable, oldValue, newValue) -> {
                floatNode.setLiteralValue(newValue.doubleValue());
                updateExpression();
            });
            PropertySheetItemFloatWrapper floatWrapper = new PropertySheetItemFloatWrapper("Value", floatProperty);
            PropertySheet propertySheet = new PropertySheet();
            propertySheet.getItems().add(floatWrapper);
            propertySheet.setSearchBoxVisible(false);


            PopOver valuePopOver = new PopOver();
            valuePopOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
            valuePopOver.getRoot().getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
            valuePopOver.setCloseButtonEnabled(true);
            valuePopOver.setHeaderAlwaysVisible(false);
            valuePopOver.setTitle("Enter new value");
            valuePopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
            valuePopOver.setContentNode(propertySheet);
            valuePopOver.show(mouseEvent.getPickResult().getIntersectedNode());
        }));
        actionItems.add(changeValue);
    }

    public void addSearchIsa() {
        Action addIsaUsingSearch = new Action("Add is-a...", new CatchThrowableEventHandler((ActionEvent event) -> {
            showFindConceptPopup(this::addIsaFromFindSelection, "Search for new is-a");
        }));
        actionItems.add(addIsaUsingSearch);
    }

    private void showFindConceptPopup(EventHandler<ActionEvent> actionOnSet, String title) {
        this.popOver = new PopOver();

        this.popOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
        this.popOver.getRoot().getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
        this.popOver.setTitle(title);
        this.popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
        ConceptSearchNodeFactory searchNodeFactory = LookupService.getNamedServiceIfPossible(ConceptSearchNodeFactory.class, "Extended Search Provider");
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(Manifold.get(Manifold.ManifoldGroup.UNLINKED), null);
        Node searchNode = searchExplorationNode.getNode();

        this.findSelectedConceptSpecification = searchExplorationNode.selectedConceptSpecification();
        BorderPane searchBorder = new BorderPane(searchNode);
        Button addSelection = new Button("set");
        addSelection.setOnAction(actionOnSet);
        ToolBar popOverToolbar = new ToolBar(addSelection);
        searchBorder.setTop(popOverToolbar);
        searchBorder.setPrefSize(500, 400);
        searchBorder.setMinSize(500, 400);

        this.popOver.setContentNode(searchBorder);
        this.popOver.sizeToScene();
        this.popOver.show((Node) mouseEvent.getSource(), mouseEvent.getSceneX(), mouseEvent.getSceneY(), Duration.seconds(0.5));
        searchExplorationNode.focusOnInput();
    }

    private void addIsaFromFindSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            ConceptNodeWithNids newIsa = expressionContiningNode.Concept(this.findSelectedConceptSpecification.get());
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newIsa);
                    break;
                }
            }
            updateExpression();
        }
    }

    public void addRecentSelectionIsa() {

        // create action group for each 
        for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
            List<Action> actions = new ArrayList<>();

            ActionGroup actionGroup = newActionGroup("Add is-a using " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
            for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                Action addIsaAction = new Action("Add is-a " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                    ConceptNodeWithNids newIsa = expressionContiningNode.Concept(historyRecord.getNid());
                    for (LogicNode node : nodeToEdit.getChildren()) {
                        if (node.getNodeSemantic() == NodeSemantic.AND) {
                            node.addChildren(newIsa);
                            break;
                        }
                    }
                    updateExpression();
                }));
                actionGroup.getActions().add(addIsaAction);
            }
            if (!actionGroup.getActions().isEmpty()) {
                actionItems.add(actionGroup);
            }
        }
    }

    public void addIsaNodeAction(int conceptNid) {
        Action addIsaAction = new Action("Add is-a " + manifold.getPreferredDescriptionText(conceptNid), new CatchThrowableEventHandler((ActionEvent event) -> {
            ConceptNodeWithNids newIsa = expressionContiningNode.Concept(conceptNid);
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newIsa);
                    break;
                }
            }
            updateExpression();
        }));
        actionItems.add(addIsaAction);
    }
    private void addRoleTypeFindSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(this.findSelectedConceptSpecification.get().getNid(),
                    expressionContiningNode.Concept(MetaData.HEALTH_CONCEPT____SOLOR));
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newRole);
                    break;
                }
            }
            updateExpression();
        }
    }

    public void addRoleOfType() {
        StringBuilder builder = new StringBuilder();
        builder.append("Add role type...");
        Action addNewRoleAction = new Action(builder.toString(), new CatchThrowableEventHandler((ActionEvent event) -> {
            showFindConceptPopup(this::addRoleTypeFindSelection, "Select role type concept");
        }));
        actionItems.add(addNewRoleAction);
    }

    public void addGenericRoleAction() {
        addRoleAction(MetaData.ROLE____SOLOR, MetaData.HEALTH_CONCEPT____SOLOR);
    }

    public void addRoleAction(ConceptSpecification typeSpec, ConceptSpecification restrictionSpec) {
        addRoleAction(typeSpec.getNid(), restrictionSpec.getNid());
    }

    public void addRoleTypeFromRecentHistory() {

        // create action group for each
        for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
            List<Action> actions = new ArrayList<>();
            ActionGroup actionGroup = newActionGroup("Add role type from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
            for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                Action roleTypeAction = new Action("Add role type " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                    RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(historyRecord.getNid(),
                            expressionContiningNode.Concept(MetaData.HEALTH_CONCEPT____SOLOR.getNid()));
                    for (LogicNode node : nodeToEdit.getChildren()) {
                        if (node.getNodeSemantic() == NodeSemantic.AND) {
                            node.addChildren(newRole);
                            break;
                        }
                    }
                    updateExpression();
                }));
                actionGroup.getActions().add(roleTypeAction);
            }
            if (!actionGroup.getActions().isEmpty()) {
                actionItems.add(actionGroup);
            }
        }
    }

    public void addRoleAction(int typeNid, int restrictionNid) {
        StringBuilder builder = new StringBuilder();
        builder.append("Add  (");
        builder.append(manifold.getPreferredDescriptionText(typeNid));
        builder.append(")➞[");
        builder.append(manifold.getPreferredDescriptionText(restrictionNid));
        builder.append("]");
        Action addNewRoleAction = new Action(builder.toString(), new CatchThrowableEventHandler((ActionEvent event) -> {
            RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(typeNid, expressionContiningNode.Concept(restrictionNid));
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newRole);
                    break;
                }
            }
            updateExpression();
        }));
        actionItems.add(addNewRoleAction);
    }

    public void addFloatFeatureAction() {
        StringBuilder builder = new StringBuilder();
        builder.append("Add feature...");
        Action addNewRoleAction = new Action(builder.toString(), new CatchThrowableEventHandler((ActionEvent event) -> {
            showFindConceptPopup(this::addFeatureTypeFromSelection, "Select feature type concept");
        }));
        actionItems.add(addNewRoleAction);
    }
    private void addFeatureTypeFromSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {

            FeatureNodeWithNids newFeature = expressionContiningNode.Feature(this.findSelectedConceptSpecification.get().getNid(),
                    MetaData.MEASURE_SEMANTIC____SOLOR.getNid(), ConcreteDomainOperators.EQUALS, expressionContiningNode.DoubleLiteral(0.0));
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newFeature);
                    break;
                }
            }
            updateExpression();
        }
    }


    public void addFloatFeatureAction(ConceptSpecification typeSpec, ConceptSpecification measureSemanticNid, ConcreteDomainOperators operator) {
        addFloatFeatureAction(typeSpec.getNid(), measureSemanticNid.getNid(), operator);
    }

    public void addFeatureTypeFromRecentHistory() {

        // create action group for each
        for (Manifold.ManifoldGroup manifoldGroup : Manifold.ManifoldGroup.values()) {
            List<Action> actions = new ArrayList<>();
            ActionGroup actionGroup = newActionGroup("Add feature type from " + manifoldGroup.getGroupName() + " history", Manifold.getOptionalIconographic(manifoldGroup.getGroupName()), actions);
            for (ComponentProxy historyRecord : Manifold.get(manifoldGroup).getHistoryRecords()) {
                Action roleTypeAction = new Action("Add feature type " + manifold.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                    FeatureNodeWithNids newFeature = expressionContiningNode.Feature(historyRecord.getNid(),
                            MetaData.MEASURE_SEMANTIC____SOLOR.getNid(), ConcreteDomainOperators.EQUALS, expressionContiningNode.DoubleLiteral(0.0));
                    for (LogicNode node : nodeToEdit.getChildren()) {
                        if (node.getNodeSemantic() == NodeSemantic.AND) {
                            node.addChildren(newFeature);
                            break;
                        }
                    }
                    updateExpression();
                }));
                actionGroup.getActions().add(roleTypeAction);
            }
            if (!actionGroup.getActions().isEmpty()) {
                actionItems.add(actionGroup);
            }
        }
    }
    public void addFloatFeatureAction(int typeNid, int measureSemanticNid, ConcreteDomainOperators operator) {
        StringBuilder builder = new StringBuilder();
        builder.append("Add ⒡ ");
        builder.append(manifold.getPreferredDescriptionText(typeNid));
        builder.append(" ");
        builder.append(operator);
        builder.append(" 0.0 ");
        builder.append(manifold.getPreferredDescriptionText(measureSemanticNid));
        Action addFeatureAction = new Action(builder.toString(), new CatchThrowableEventHandler((ActionEvent event) -> {
            FeatureNodeWithNids newRole = expressionContiningNode.Feature(typeNid,
                    measureSemanticNid, operator, expressionContiningNode.DoubleLiteral(0.0));
            for (LogicNode node : nodeToEdit.getChildren()) {
                if (node.getNodeSemantic() == NodeSemantic.AND) {
                    node.addChildren(newRole);
                    break;
                }
            }
            updateExpression();
        }));
        actionItems.add(addFeatureAction);
    }

    private void updateExpression() {
        this.nodeToEdit.getLogicalExpression().setUncommitted();
        this.expressionUpdater.accept(this.expressionContiningNode);
    }

    @Override
    public String toString() {
        return "AddEditLogicalExpressionNodeMenuItems{nodeToEdit: " + nodeToEdit + ", expression: " + expressionContiningNode + '}';
    }

}
