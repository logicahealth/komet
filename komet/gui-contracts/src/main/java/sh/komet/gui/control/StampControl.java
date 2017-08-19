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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.identity.StampedVersion;

/**
 *
 * @author kec
 */
public class StampControl extends Label {

   public StampControl() {
   }

   public StampControl(String text) {
      super(text);
   }

   public StampControl(String text, Node graphic) {
      super(text, graphic);
   }
   public void setStampedVersion(StampedVersion stampedVersion, ManifoldCoordinate manifoldCoordinate) {
      String toolTipText = Get.stampService().describeStampSequenceForTooltip(stampedVersion.getStampSequence(), manifoldCoordinate);
      Tooltip stampTip = new Tooltip(toolTipText);
      this.setTooltip(stampTip);
   }
}
