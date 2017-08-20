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

import javafx.beans.value.ObservableValue;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public final class VersionPanel  extends BadgedVersionPanel {
   
   public VersionPanel(Manifold manifold, ObservableCategorizedVersion categorizedVersion) {
      super(manifold, categorizedVersion);
      this.getStyleClass()
              .add(StyleClasses.VERSION_PANEL.toString());
      this.expandControl.setVisible(false);
   }

   @Override
   protected void expand(ObservableValue<? extends ExpandAction> observable, ExpandAction oldValue, ExpandAction newValue) {
      ; // Nothing to do...
   }
   
}
