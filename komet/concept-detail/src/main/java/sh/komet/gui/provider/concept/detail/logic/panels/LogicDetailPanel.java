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
import javafx.scene.Node;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.titled.TitledToolbarPane;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.PseudoClasses;

/**
 *
 * @author kec
 */
public abstract class LogicDetailPanel {
    
    protected double leftInset = 25;
    
    private final PremiseType premiseType;

    protected final TitledToolbarPane panel = new TitledToolbarPane();
    protected final Manifold manifold;
    protected final LogicalExpression logicalExpression;
    
    public LogicDetailPanel(PremiseType premiseType, LogicalExpression logicalExpression, Manifold manifold) {
        this.premiseType = premiseType;
        this.manifold = manifold;
        this.logicalExpression = logicalExpression;
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
    abstract Node getPanelNode();
    
    abstract String getLabelText();
    
    final PremiseType getPremiseType() {
        return this.premiseType;
    }
   public final Node computeGraphic() {
       return computeGraphic(logicalExpression);
   }
   
   public final Node computeGraphic(LogicalExpression expression) {
      int[] parents = Get.taxonomyService().getSnapshot(manifold)
              .getTaxonomyTree().getParentNids(expression.getConceptNid());
      if (parents.length == 0) {
          panel.setCollapsible(false);
      }
      boolean multiParent = parents.length > 1;
      boolean sufficient = expression.contains(NodeSemantic.SUFFICIENT_SET);
 
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
