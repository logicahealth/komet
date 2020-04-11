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

package sh.isaac.komet.gui.graphview;

import javafx.scene.Node;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

import java.util.EnumSet;

/**
 * DefaultMultiParentGraphItemDisplayPolicies
 *
 * @author kec
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DefaultMultiParentGraphItemDisplayPolicies implements MultiParentGraphItemDisplayPolicies {
   private final Manifold manifold;

   public DefaultMultiParentGraphItemDisplayPolicies(Manifold manifold) {
      this.manifold = manifold;
   }
   
   
   @Override
   public Node computeGraphic(MultiParentGraphItem item) {
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
   public boolean shouldDisplay(MultiParentGraphItem treeItem) {
      int conceptNid = treeItem.getConceptNid();
       StampFilterImmutable vertexStampFilter = manifold.getVertexStampFilter().toStampFilterImmutable();
       StatusSet allowedStates = vertexStampFilter.getAllowedStates();
       EnumSet<Status> states = Get.conceptActiveService().getConceptStates(conceptNid, vertexStampFilter);
       for (Status state: states) {
           if (allowedStates.contains(state)) {
               return true;
           }
       }
       return false;
   }
   
   
}
