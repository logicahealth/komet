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
package sh.komet.gui.control;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Added to allow a layout container than cannot be resized to get around
 * text flow layout problems. 
 * @author kec
 */
public class FixedSizePane extends Pane {

   public FixedSizePane() {
   }

   public FixedSizePane(Node... children) {
      super(children);
   }

   @Override
   public boolean isResizable() {
      return false;
   }

   @Override
   public void setHeight(double value) {
      super.setHeight(value); 
   }

   @Override
   public void setWidth(double value) {
      super.setWidth(value);
   }
   
   
}
