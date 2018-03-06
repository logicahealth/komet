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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
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
    private final Manifold manifold;
    private final VBox setBox = new VBox();
    private final TitledPane panel = new TitledPane("Root", setBox);

    public LogicDetailRootNode(RootNode rootNode,             
            PremiseType premiseType, Manifold manifold) {
        super(premiseType);

        this.rootNode = rootNode;
        this.manifold = manifold;
        this.panel.setText(this.manifold.getPreferredDescriptionText(this.rootNode.getNidForConceptBeingDefined()));
        
        // find the set nodes.
        
       for (LogicNode childNode: rootNode.getChildren()) {
            LogicDetailSetPanel setPanel;
            if (childNode instanceof NecessarySetNode) {
                setPanel = new LogicDetailSetPanel((NecessarySetNode) childNode, getPremiseType(), manifold);
            } else if (childNode instanceof SufficientSetNode) {
                setPanel = new LogicDetailSetPanel((SufficientSetNode) childNode, getPremiseType(), manifold);
            } else {
                throw new IllegalStateException("Can't handle node: " + childNode);
            }
            VBox.setMargin(setPanel.getPanelNode(), new Insets(0));
            setBox.getChildren().add(setPanel.getPanelNode());
        }
    }

    @Override
    String getLabelText() {
        return "root";
    }
    
    @Override
    public Node getPanelNode() {
        return setBox;
    }
}
