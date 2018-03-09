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
package sh.komet.gui.provider.concept.detail.logic.panels;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class LogicDetailRootNode extends LogicDetailPanel {

    private final RootNode rootNode;
    private final VBox setBox = new VBox();

    public LogicDetailRootNode(RootNode rootNode,
            PremiseType premiseType, LogicalExpression logicalExpression, Manifold manifold) {
        super(premiseType, logicalExpression, manifold);
        this.panel.setContent(setBox);
        panel.expandedProperty().addListener((observable, oldValue, newValue) -> {
            handleOpenClose(newValue);
        });
        setBox.paddingProperty().set(new Insets(0, 0, 0, leftInset));
        this.rootNode = rootNode;
        this.panel.setText(this.manifold.getPreferredDescriptionText(this.rootNode.getNidForConceptBeingDefined()));
        if (premiseType == PremiseType.STATED) {
            this.panel.setLeftGraphic2(Iconography.STATED.getIconographic());
        } else {
            this.panel.setLeftGraphic2(Iconography.INFERRED.getIconographic());
        }

        this.panel.setLeftGraphic1(computeGraphic());

        // find the set nodes.
        for (LogicNode childNode : rootNode.getChildren()) {
            LogicDetailSetPanel setPanel;
            if (childNode instanceof NecessarySetNode) {
                setPanel = new LogicDetailSetPanel((NecessarySetNode) childNode, getPremiseType(), logicalExpression, manifold);
            } else if (childNode instanceof SufficientSetNode) {
                setPanel = new LogicDetailSetPanel((SufficientSetNode) childNode, getPremiseType(), logicalExpression, manifold);
            } else {
                throw new IllegalStateException("Can't handle node: " + childNode);
            }
            VBox.setMargin(setPanel.getPanelNode(), new Insets(0));
            setBox.getChildren().add(setPanel.getPanelNode());
        }
    }

    private void handleOpenClose(boolean open) {
        this.panel.setLeftGraphic1(computeGraphic());
    }

    @Override
    String getLabelText() {
        return "root";
    }

    @Override
    public Node getPanelNode() {
        return panel;
    }
}
