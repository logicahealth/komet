/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.provider.concept.detail.treetable;

import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.DetailNodeFactory;
import sh.komet.gui.contract.DetailType;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;

/**
 *
 * @author kec
 */
@Service(name = "Concept Detail Provider")
@Singleton
public class ConceptDetailTreeTableProviderFactory implements DetailNodeFactory {

   @Override
   public DetailType getSupportedType() {
      return DetailType.Concept;
   }

   @Override
   public DetailNode createNode(Manifold manifold, IsaacPreferences preferencesNode) {
      return new ConceptDetailTreeTableNode(manifold);
   }
   @Override
   public String getMenuText() {
      return "Concept Details Table"; 
   }

   @Override
   public Node getMenuIcon() {
      // FontAwesomeIcon.COLUMNS
      // FontAwesomeIcon.TABLE
      // FontAwesomeIcon.LIST
      // Icons525.MENU
      // Icons525.ARCHIVE rotate 90°?
      // MaterialDesignIcon.VIEW_DAY
      // MaterialIcon.VIEW_WEEK rotate 90°
      // FontAwesomeIcon.NAVICON
      return Iconography.CONCEPT_TABLE.getIconographic();
   }
     
   /** 
    * {@inheritDoc}
    */
   @Override
   public ManifoldGroup[] getDefaultManifoldGroups() {
      return new ManifoldGroup[] {ManifoldGroup.TAXONOMY};
   }

   @Override
   public ConceptSpecification getPanelType() {
      return MetaData.CONCEPT_DETAILS_TREE_TABLE____SOLOR;
   }
}
