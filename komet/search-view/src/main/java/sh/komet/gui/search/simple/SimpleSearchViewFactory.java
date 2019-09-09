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
package sh.komet.gui.search.simple;

import java.io.IOException;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;

/**
 *
 * @author kec
 */

@Service(name = "Simple Search Provider")
@Singleton
public class SimpleSearchViewFactory implements ConceptSearchNodeFactory {
   public static final String MENU_TEXT  = "Search";

   @Override
   public ConceptExplorationNode createNode(Manifold manifold, IsaacPreferences preferencesNode) {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/komet/gui/search/fxml/SimpleSearch.fxml"));
         loader.load();
         SimpleSearchController simpleSearchController = loader.getController();
         simpleSearchController.setManifold(manifold);
         return simpleSearchController;
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public String getMenuText() {
      return MENU_TEXT;
   }

   @Override
   public Node getMenuIcon() {
      return Iconography.SIMPLE_SEARCH.getIconographic();
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public ManifoldGroup[] getDefaultManifoldGroups() {
      return new ManifoldGroup[] {ManifoldGroup.SEARCH};
   }
   

   @Override
   public ConceptSpecification getPanelType() {
      return MetaData.SIMPLE_SEARCH_PANEL____SOLOR;
   }
}
