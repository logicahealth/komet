/*
 * Copyright 2017 Your Organisation.
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
package sh.komet.gui.state;

import java.util.function.Supplier;
import javafx.scene.Node;
import sh.isaac.komet.iconography.Iconography;

/**
 *
 * @author kec
 */
 public enum LayoutAlgorithm {
   AspectRatioTreeLayout("tree aspect ratio", Iconography.TREE_ASPECT_RATIO_LAYOUT::getIconographic), 
   ClassicTreeLayout("tree", Iconography.CLASSIC_TREE_LAYOUT::getIconographic), 
   HierarchicalLayout("hierarchical", Iconography.HIERARCHICAL_LAYOUT::getIconographic);
   
   String name;
   Supplier<Node> iconSupplier;
   
   private LayoutAlgorithm(String name, Supplier<Node> iconSupplier) {
      this.name = name;
      this.iconSupplier = iconSupplier;
   }

   @Override
   public String toString() {
      return name;
   }
   
   public Node getNode() {
      return iconSupplier.get();
   }
   
}
