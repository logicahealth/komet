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

import java.util.Optional;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class LogicDetailConceptPanel extends LogicDetailPanel {

    private final ConceptNodeWithNids conceptNode;
    private final Optional<LogicalExpression> expressionForThisConcept;

    public LogicDetailConceptPanel(ConceptNodeWithNids conceptNode,
            PremiseType premiseType, LogicalExpression logicalExpression, Manifold manifold) {
        super(premiseType, logicalExpression, manifold);
        this.conceptNode = conceptNode;
        expressionForThisConcept = manifold.getLogicalExpression(conceptNode.getConceptNid(), premiseType);
        this.panel.setText(manifold.getPreferredDescriptionText(conceptNode.getConceptNid()));
        this.panel.setContent(new Label("empty"));
        panel.setExpanded(false);
        panel.expandedProperty().addListener((observable, oldValue, newValue) -> {
            handleOpenClose(newValue);
        });
        computeGraphicForThisConcept();        
        panel.setRightGraphic(Iconography.EDIT_PENCIL.getIconographic());
        setPseudoClasses(panel);
        panel.getStyleClass()
                .add(StyleClasses.DEF_CONCEPT.toString());
    }

    private void handleOpenClose(boolean open) {
        if (open) {


            computeGraphicForThisConcept();
            
            if (expressionForThisConcept.isPresent()) {
                panel.setContent(new LogicDetailRootNode((RootNode) expressionForThisConcept.get().getRoot(), getPremiseType(), expressionForThisConcept.get(), manifold).getPanelNode());
            } else {
                panel.setContent(new Label("No logical definition available"));
            }

        } else {
            computeGraphicForThisConcept();
            panel.setContent(new Label(""));
        }
    }

    private void computeGraphicForThisConcept() {
        if (expressionForThisConcept.isPresent()) {
            panel.setLeftGraphic2(computeGraphic(expressionForThisConcept.get()));
        } else {
            panel.setLeftGraphic2(Iconography.ALERT_CONFIRM.getIconographic());
        }
    }

    public ReadOnlyStringProperty getConceptText() {
        return panel.textProperty();
    }

    @Override
    String getLabelText() {
        return getConceptText().get();
    }

    @Override
    public Node getPanelNode() {

        return panel;
    }

}
