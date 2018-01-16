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
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public interface DetailNode {
   /**
    * A title as might be used to provide a title in a tab for a DetailNode. 
    * @return the read-only property for the title.  
    */
   ReadOnlyProperty<String> getTitle(); 
   
   /**
    * An optional node to use in addition the title. If the DetailNode wishes to 
    * support drag and drop within a tab or other titled control, use this option. 
    * The optional node may include the text of the current focused concept. If duplicate 
    * display of the concept text may result, make the title property display an empty
    * string if the titleNode has been constructed. 
    * @return a Node to represent the title of this DetailNode. 
    */
   Optional<Node> getTitleNode();
   
   /**
    * Tool tip text to explain this node in more detail that a title would. 
    * @return the read-only property for the tool-tip text.
    */
   ReadOnlyProperty<String> getToolTip(); 
   
   /**
    * 
    * @return true if the detail node should become the selected (frontmost)
    * tab within a TabPane or similar construct when the focused 
    * concept changes. 
    */
   boolean selectInTabOnChange();
   
   /**
    * 
    * @return the Manifold associated with this DetailNode. 
    */
   Manifold getManifold();
   
}
