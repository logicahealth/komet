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
package sh.komet.gui.contract;

import org.jvnet.hk2.annotations.Contract;
import javafx.scene.Node;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.util.FxGet;

/**
 * Common methods for node factories
 * @author kec
 * @param <T> The type of node this factory will create.  
 */
@Contract
public interface NodeFactory<T extends ExplorationNode> {
   
   /**
    * Create the node
    * @param manifold the manifold that determines the current coordinates and focus.
    * @param nodePreferences preferences node where the new node can store its preferences
    * @return the created node, not yet added to the scenegraph.  Call {@link ExplorationNode#getNode()} 
    * to get the appropriate node, when ready to add it to the scenegraph.
    */
   T createNode(Manifold manifold, IsaacPreferences nodePreferences);
   
   /**
    * 
    * @return text to display in a menu that invokes this factory
    */
   String getMenuText();
   
   /**
    * 
    * @return icon to display in a menu that invokes this factory
    */
   Node getMenuIcon();

   /**
    * The initial manifold group(s) this view should be linked to.
    * returning more than one manifold group will be seen as a request
    * to create multiple copies of this node on system startup.
    * 
    * The first item in this array is used as the desired manifold group when the node is requested via a menu.
    * 
    * @return the desired manifold group
    */
   ManifoldGroup[] getDefaultManifoldGroups();

   /**
    *
    * @return a ConceptSpecification that defines the type of this panel.
    */
   ConceptSpecification getPanelType();
}
