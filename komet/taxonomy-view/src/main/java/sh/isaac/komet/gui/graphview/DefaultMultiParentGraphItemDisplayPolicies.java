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
import javafx.scene.layout.HBox;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.api.navigation.NavigationRecord;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.style.StyleClasses;

import java.util.EnumSet;

/**
 * DefaultMultiParentGraphItemDisplayPolicies
 *
 * @author kec
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class DefaultMultiParentGraphItemDisplayPolicies implements MultiParentGraphItemDisplayPolicies {

   
   @Override
   public Node computeGraphic(MultiParentGraphItem item, ManifoldCoordinate manifoldCoordinate) {
       NavigationRecord navigationRecord = Get.taxonomyService().getNavigationRecord(item.getConceptNid());

       boolean conceptActive = navigationRecord.getConceptStates(item.getConceptNid(), manifoldCoordinate.getViewStampFilter().toStampFilterImmutable()).contains(Status.ACTIVE);
       if (!conceptActive) {
           return Iconography.DELETE_TRASHCAN.getStyledIconographic();
       }

       ImmutableIntSet navigationConceptNids = manifoldCoordinate.getNavigationCoordinate().getNavigationConceptNids();

       Node navigationGraphic = getNavigationGraphic(item);
       if (navigationConceptNids.size() > 1 && item.getTypeNid() == TermAux.IS_A.getNid() && item.getOptionalParentNid().isPresent()) {
            // could be stated and inferred...
            if (navigationConceptNids.contains(manifoldCoordinate.getLogicCoordinate().getInferredAssemblageNid()) &&
                    navigationConceptNids.contains(manifoldCoordinate.getLogicCoordinate().getStatedAssemblageNid())) {
                // Stated and inferred, need to indicate which.
                HBox combinedGraphic = new HBox(navigationGraphic);
                combinedGraphic.setSpacing(2);


                // Stated
                if (navigationRecord.containsConceptNidViaType(item.getOptionalParentNid().getAsInt(),
                        item.getTypeNid(), manifoldCoordinate, PremiseType.STATED.getFlags())) {
                    Node navigationBadge = Iconography.STATED.getIconographicWithStyleClasses(StyleClasses.NAVIGATION_BADGE.toString());
                    combinedGraphic.getChildren().add(navigationBadge);
                }
                // Inferred
                if (navigationRecord.containsConceptNidViaType(item.getOptionalParentNid().getAsInt(),
                        item.getTypeNid(), manifoldCoordinate, PremiseType.INFERRED.getFlags())) {
                    Node navigationBadge = Iconography.INFERRED.getIconographicWithStyleClasses(StyleClasses.NAVIGATION_BADGE.toString());
                    combinedGraphic.getChildren().add(navigationBadge);
                }
                navigationGraphic = combinedGraphic;
            }
       }
       return navigationGraphic;
   }

    private Node getNavigationGraphic(MultiParentGraphItem item) {
        if (item.isRoot()) {
            // TODO get dynamic icons from Assemblages.
            if (item.getConceptNid() == TermAux.PRIMORDIAL_PATH.getNid()) {
                return Iconography.SOURCE_BRANCH_1.getIconographic();
            } else if (item.getConceptNid() == TermAux.PRIMORDIAL_MODULE.getNid()) {
                return Iconography.LINK_EXTERNAL.getIconographic();
            }
            return Iconography.TAXONOMY_ROOT_ICON.getIconographic();
        }

        if (item.getTypeNid() != TermAux.IS_A.getNid()) {
            // TODO get dynamic icons from Assemblages.
            if (item.getTypeNid() == TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid()) {
                return Iconography.SOURCE_BRANCH_1.getIconographic();
            } else if (item.getTypeNid() == TermAux.DEPENDENCY_MANAGEMENT.getNid()) {
                return Iconography.LINK_EXTERNAL.getIconographic();
            }
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
   public boolean shouldDisplay(MultiParentGraphItem treeItem, ManifoldCoordinate manifoldCoordinate) {
       if (treeItem.isRoot()) {
           return true;
       }
      int conceptNid = treeItem.getConceptNid();
       StampFilterImmutable vertexStampFilter = manifoldCoordinate.getVertexStampFilter().toStampFilterImmutable();
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
