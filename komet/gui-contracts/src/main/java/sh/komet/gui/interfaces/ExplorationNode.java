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
package sh.komet.gui.interfaces;

import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import org.jvnet.hk2.annotations.Contract;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import sh.komet.gui.manifold.Manifold;

/**
 * ExplorationNode: A node that enables traversal (a taxonomy view) of content
 * or search of content for the purpose of discovery.
 * A ExplorationNode provides an entry point to navigate to find details to look at, as opposed to a
 * DetailNode which displays the details selected via an exploration node.
 * 
 * @author kec
 */
@Contract
public interface ExplorationNode {
   /**
    * A title as might be used to provide a title in a tab for a PanelNode.
    * 
    * @return the read-only property for the title.
    */
   ReadOnlyProperty<String> getTitle();

   /**
    * An optional node to use in addition the title. If the PanelNode wishes to
    * support drag and drop within a tab or other titled control, use this option.
    * The optional node may include the text of the current focused concept. If duplicate
    * display of the concept text may result, make the title property display an empty
    * string if the titleNode has been constructed.
    * 
    * @return a Node to represent the title of this DetailNode.
    */
   Optional<Node> getTitleNode();

   /**
    * Tool tip text to explain this node in more detail than a title would.
    * 
    * @return the read-only property for the tool-tip text.
    */
   ReadOnlyProperty<String> getToolTip();

   /**
    * 
    * @return the Manifold associated with this DetailNode.
    */
   Manifold getManifold();

   /**
    * @return The node to be displayed
    */
   Node getNode();
   
   
   ObjectProperty<Node> getMenuIconProperty();

   /**
    * When called, the node should release all resources, as it is closed.
    */
   void close();

   /**
    * Indicate if a node should not close for any reason (uncommitted work, etc).
    */
   boolean canClose();

   /**
    * Save preferences for this node to the preferences provider.
    */
   void savePreferences();
}
