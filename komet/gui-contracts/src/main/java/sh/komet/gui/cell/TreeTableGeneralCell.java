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
package sh.komet.gui.cell;

import javafx.scene.control.TreeTableCell;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class TreeTableGeneralCell extends TreeTableCell<ObservableCategorizedVersion, ObservableCategorizedVersion> {
   private final Manifold manifold;

   public TreeTableGeneralCell(Manifold manifold) {
      this.manifold = manifold;
   }

   @Override
   protected void updateItem(ObservableCategorizedVersion version, boolean empty) {
     super.updateItem(version, empty);

     if (empty || version == null) {
         setText(null);
         setGraphic(null);
     } else {
        SememeVersion sememeVersion = version.unwrap();
        SememeType sememeType = sememeVersion.getChronology().getSememeType();
        switch (sememeType) {
           case DESCRIPTION:
              DescriptionVersion description = version.unwrap();
              setText(description.getText());
              break;
           default: 
           setText("not implemented for type: " + sememeType);
        }
         
     }
   }
   
}