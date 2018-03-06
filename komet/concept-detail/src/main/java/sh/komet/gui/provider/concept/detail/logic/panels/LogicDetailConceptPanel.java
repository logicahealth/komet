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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.NecessarySetNode;
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
    private final Manifold manifold;
    private final TitledPane panel = new TitledPane();
    private final Optional<LogicalExpression> logicalExpression;

    public LogicDetailConceptPanel(ConceptNodeWithNids conceptNode,
            PremiseType premiseType, Manifold manifold) {
        super(premiseType);
        this.conceptNode = conceptNode;
        this.manifold = manifold;
        this.panel.setText(manifold.getPreferredDescriptionText(conceptNode.getConceptNid()));
        this.panel.setContent(new Label("empty"));
        panel.setExpanded(false);
        panel.expandedProperty().addListener((observable, oldValue, newValue) -> {
            handleOpenClose(newValue);
        });
        logicalExpression = manifold.getLogicalExpression(conceptNode.getConceptNid(), getPremiseType());
        panel.setGraphic(computeGraphic());
        setPseudoClasses(panel);
        panel.getStyleClass()
                .add(StyleClasses.DEF_CONCEPT.toString());        
    }

    private void handleOpenClose(boolean open) {
        if (open) {
            
            if (logicalExpression.isPresent()) {
                final VBox setBox = new VBox();
                panel.setGraphic(computeGraphic());
                for (LogicNode childNode : logicalExpression.get().getRoot().getChildren()) {
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
                panel.setContent(setBox);
                
            } 
        } else {
            panel.setGraphic(computeGraphic());
            panel.setContent(new Label(""));
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
   public final Node computeGraphic() {
      boolean sufficient = false;
      int[] parents = Get.taxonomyService().getSnapshot(manifold)
              .getTaxonomyTree().getParentNids(conceptNode.getConceptNid());
      if (parents.length == 0) {
          panel.setCollapsible(false);
      }
      boolean multiParent = parents.length > 1;
        if (logicalExpression.isPresent()) {
            sufficient = logicalExpression.get().contains(NodeSemantic.SUFFICIENT_SET);
        }

      if (parents.length == 0) {
         return Iconography.TAXONOMY_ROOT_ICON.getIconographic();
      } else if (sufficient && multiParent) {
         if (panel.isExpanded()) {
            return Iconography.TAXONOMY_DEFINED_MULTIPARENT_OPEN.getIconographic();
         } else {
            return Iconography.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.getIconographic();
         }
      } else if (!sufficient && multiParent) {
         if (panel.isExpanded()) {
            return Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.getIconographic();
         } else {
            return Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.getIconographic();
         }
      } else if (sufficient && !multiParent) {
         return Iconography.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic();
      }
      return Iconography.TAXONOMY_PRIMITIVE_SINGLE_PARENT.getIconographic();
   }

}
