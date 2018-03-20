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
package sh.komet.gui.control.logic;

import java.util.Optional;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.observable.ObservableSemanticChronologyImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.control.titled.TitledToolbarPane;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class LogicDetailRootNode extends LogicDetailPanel {

    private RootNode rootNode;

    private final VBox setBox = new VBox();

    private final BorderPane rootBorderPane = new BorderPane();

    public LogicDetailRootNode(RootNode rootNode,
            PremiseType premiseType, LogicalExpression logicalExpression,
            Manifold manifold) {
        super(premiseType, rootNode, logicalExpression, manifold);
        this.panel.setContent(setBox);

        rootBorderPane.setCenter(panel);
        panel.expandedProperty().addListener((observable, oldValue, newValue) -> {
            handleOpenClose(newValue);
        });
        setBox.paddingProperty().set(new Insets(0, 0, 0, leftInset));
        this.rootNode = rootNode;

        if (premiseType == PremiseType.STATED) {
            this.panel.setLeftGraphic2(Iconography.STATED.getIconographic());
        } else {
            this.panel.setLeftGraphic2(Iconography.INFERRED.getIconographic());
        }
        if (rootNode.getChildren().length == 0) {
            this.panel.setText("Concept being created");
            this.panel.setLeftGraphic1(Iconography.ALERT_CONFIRM2.getIconographic());
            this.panel.setLeftGraphic2(null);
        } else {
            this.panel.setText(this.manifold.getPreferredDescriptionText(this.rootNode.getNidForConceptBeingDefined()));
            this.panel.setLeftGraphic1(computeGraphic());
        }

        updateChildPanels();
    }

    public void updateStatedExpression(LogicalExpression expression) {
        this.logicalExpression = expression;
        this.rootNode = (RootNode) expression.getRoot();
        updateChildPanels();
    }

    private void updateChildPanels() throws IllegalStateException {
        if (logicalExpression.isUncommitted()) {
            ToolBar commitToolbar = new ToolBar();
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMinWidth(Region.USE_PREF_SIZE);
            Button cancel = new Button("Cancel");
            cancel.setOnAction(this::cancelEdit);
            Button commit = new Button("Commit");
            commit.setOnAction(this::commitEdit);
            commitToolbar.getItems().addAll(spacer, cancel, commit);
            rootBorderPane.setTop(commitToolbar);
        } else {
            rootBorderPane.setTop(null);
        }
        // find the set nodes.
        setBox.getChildren().clear();
        for (LogicNode childNode : rootNode.getChildren()) {
            LogicDetailSetPanel setPanel;
            if (childNode instanceof NecessarySetNode) {
                setPanel = new LogicDetailSetPanel((NecessarySetNode) childNode, getPremiseType(), logicalExpression, manifold, this);
            } else if (childNode instanceof SufficientSetNode) {
                setPanel = new LogicDetailSetPanel((SufficientSetNode) childNode, getPremiseType(), logicalExpression, manifold, this);
            } else {
                throw new IllegalStateException("Can't handle node: " + childNode);
            }
            VBox.setMargin(setPanel.getPanelNode(), new Insets(0));
            setBox.getChildren().add(setPanel.getPanelNode());
        }
    }

    private void handleOpenClose(boolean open) {
        if (logicalExpression.getConceptNid() != -1) {
            this.panel.setLeftGraphic1(computeGraphic());
        }
    }

    @Override
    String getLabelText() {
        return "root";
    }

    @Override
    public Node getPanelNode() {
        return rootBorderPane;
    }

    public TitledToolbarPane getTitledToolbar() {
        this.rootBorderPane.setCenter(null);
        return panel;
    }
    public Node getSetsPanelNode() {

        this.panel.setContent(null);
        this.rootBorderPane.setCenter(setBox);
        return this.rootBorderPane;
    }

    private void cancelEdit(Event event) {
        updateExpression();
    }

    private void updateExpression() {
        if (this.logicalExpression.getConceptNid() != -1) {
            Optional<LogicalExpression> expression = manifold.getStatedLogicalExpression(this.logicalExpression.getConceptNid());
            if (expression.isPresent()) {
                this.rootNode = (RootNode) expression.get().getRoot();
                updateLogicalExpression(expression.get());
                updateChildPanels();
            }
        }
    }

    private void commitEdit(Event event) {

        LatestVersion<LogicGraphVersion> latestVersion = manifold.getStatedLogicGraphVersion(this.logicalExpression.getConceptNid());
        if (latestVersion.isPresent()) {
            LogicGraphVersion version = latestVersion.get();
            ObservableSemanticChronologyImpl observableSemanticChronology = new ObservableSemanticChronologyImpl(version.getChronology());
            ObservableLogicGraphVersionImpl observableVersion = new ObservableLogicGraphVersionImpl(version, observableSemanticChronology);
            ObservableLogicGraphVersionImpl mutableVersion = observableVersion.makeAutonomousAnalog(manifold.getEditCoordinate());
            mutableVersion.setGraphData(this.logicalExpression.getData(DataTarget.INTERNAL));
            Get.commitService().commit(mutableVersion, manifold.getEditCoordinate(), "Lambda graph edit");
        }
        updateExpression();
    }

}
