/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sh.isaac.komet.gui.treeview;

import java.util.EnumSet;
import javafx.scene.Node;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

/**
 * DefaultMultiParentTreeItemDisplayPolicies
 *
 * @author kec
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DefaultMultiParentTreeItemDisplayPolicies implements MultiParentTreeItemDisplayPolicies {
   private final Manifold manifold;

   public DefaultMultiParentTreeItemDisplayPolicies(Manifold manifold) {
      this.manifold = manifold;
   }
   
   
   @Override
   public Node computeGraphic(MultiParentTreeItem item) {
        if (item.isRoot()) {
            return Iconography.TAXONOMY_ROOT_ICON.getIconographic();
        } 
       
       if (item.getTypeNid() != TermAux.IS_A.getNid()) {
          return Iconography.ALERT_CONFIRM.getIconographic();
       } 
       
       if (item.isDefined() && (item.isMultiParent() || item.getMultiParentDepth() > 0)) {
         if (item.isSecondaryParentOpened()) {
            return Iconography.TAXONOMY_DEFINED_MULTIPARENT_OPEN.getIconographic();
         } else {
            return Iconography.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.getIconographic();
         }
      } else if (!item.isDefined() && (item.isMultiParent() || item.getMultiParentDepth() > 0)) {
         if (item.isSecondaryParentOpened()) {
            return Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.getIconographic();
         } else {
            return Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.getIconographic();
         }
      } else if (item.isDefined() && !item.isMultiParent()) {
         return Iconography.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic();
      }
      return Iconography.TAXONOMY_PRIMITIVE_SINGLE_PARENT.getIconographic();
   }

   @Override
   public boolean shouldDisplay(MultiParentTreeItem treeItem) {
      int conceptNid = treeItem.getConceptNid();
      String description = Get.conceptDescriptionText(conceptNid);
      EnumSet<Status> allowedStates = manifold.getAllowedStates();
      EnumSet<Status> states = Get.conceptActiveService().getConceptStates(conceptNid, manifold);
      for (Status state: states) {
         if (allowedStates.contains(state)) {
            return true;
         }
      }
      return false;
   }
   
   
}
