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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.ConnectorNode;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class LogicDetailSetPanel extends LogicDetailPanel {

    private ConnectorNode setNode;
    private final VBox setBox = new VBox(0);

    public LogicDetailSetPanel(NecessarySetNode setNode, 
                        PremiseType premiseType, LogicalExpression logicalExpression, 
                        Manifold manifold, LogicDetailRootNode rootNodePanel) {
        super(premiseType, setNode, logicalExpression, manifold, rootNodePanel);
        panel.setContent(setBox);
        setBox.paddingProperty().set(new Insets(0, 0, 0, leftInset));
        setup(setNode);
        setPseudoClasses(panel);
        panel.getStyleClass()
                .add(StyleClasses.DEF_NECESSARY_SET.toString());        
        setPseudoClasses(setBox);
        setBox.getStyleClass()
                .add(StyleClasses.DEF_NECESSARY_SET.toString()); 
    }

    public LogicDetailSetPanel(SufficientSetNode setNode,             
                        PremiseType premiseType, LogicalExpression logicalExpression, 
                        Manifold manifold, LogicDetailRootNode rootNodePanel) {
        super(premiseType, setNode, logicalExpression, manifold, rootNodePanel);
        panel.setContent(setBox);
        setBox.paddingProperty().set(new Insets(0, 0, 0, leftInset));
        setup(setNode);
        setPseudoClasses(panel);
        panel.getStyleClass()
                .add(StyleClasses.DEF_SUFFICIENT_SET.toString());        
        setPseudoClasses(setBox);
        setBox.getStyleClass()
                .add(StyleClasses.DEF_SUFFICIENT_SET.toString());        
    }

    private void setup(ConnectorNode setNode) {
        this.setNode = setNode;
        if (setNode instanceof SufficientSetNode) {
            this.panel.setLeftGraphic2(Iconography.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic());
        } else {
            this.panel.setLeftGraphic2(Iconography.TAXONOMY_ROOT_ICON.getIconographic());
        }
        this.panel.setText(manifold.getPreferredDescriptionText(setNode.getNodeSemantic().getConceptNid())
        + ": " 
        + manifold.getPreferredDescriptionText(setNode.getNidForConceptBeingDefined()));

        // process the children of the set
        for (LogicNode childNode : setNode.getChildren()) {
            // There should only be one child node, an AND node. 
            List<LogicDetailPanel> childNodes = new ArrayList<>();
            for (LogicNode andNodeChild : childNode.getChildren()) {
 
                switch (andNodeChild.getNodeSemantic()) {
                    case ROLE_SOME:
                        LogicDetailRolePanel rolePanel = new LogicDetailRolePanel((RoleNodeSomeWithNids) andNodeChild, getPremiseType(), logicalExpression, manifold, rootNodePanel);
                        VBox.setMargin(rolePanel.getPanelNode(), new Insets(0));
                        childNodes.add(rolePanel);
                        break;
                    case CONCEPT:
                        LogicDetailConceptPanel conceptPanel = new LogicDetailConceptPanel((ConceptNodeWithNids) andNodeChild, getPremiseType(), logicalExpression, manifold, rootNodePanel);
                        VBox.setMargin(conceptPanel.getPanelNode(), new Insets(0));
                        childNodes.add(conceptPanel);
                        break;
                    default:
                        throw new IllegalStateException("Can't handle node: " + childNode);
                }
            }
            childNodes.sort(new LogicDetailPanelComparator());
            for (LogicDetailPanel childPanel: childNodes) {
                setBox.getChildren().add(childPanel.getPanelNode());
            }
            
        }

    }

    @Override
    public Node getPanelNode() {
        return panel;
    }

    @Override
    String getLabelText() {
        return panel.getText();
    }

}
