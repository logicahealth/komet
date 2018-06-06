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
package sh.komet.gui.cell.treetable;

import javafx.scene.control.TreeTableRow;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.component.semantic.version.SemanticVersion;

/**
 *
 * @author kec
 */
public class TreeTableWhatCell extends KometTreeTableCell<ObservableCategorizedVersion> {
   private final Manifold manifold;

   public TreeTableWhatCell(Manifold manifold) {
      this.manifold = manifold;
      getStyleClass().add("komet-version-what-cell");
      getStyleClass().add("isaac-version");
   }

   @Override
   protected void updateItem(TreeTableRow<ObservableCategorizedVersion> row, ObservableCategorizedVersion cellValue) {
        SemanticVersion semanticVersion = cellValue.unwrap();
        switch (semanticVersion.getChronology().getVersionType()) {
           case DESCRIPTION:
              DescriptionVersion description = cellValue.unwrap();
              int descriptionType = description.getDescriptionTypeConceptNid();
              if (descriptionType == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
                 setText("FQN");
              } else if (descriptionType == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
                 setText("NĀM");
              } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
                 setText("DEF");
              } else if (descriptionType == MetaData.ABBREVIATION_DESCRIPTION_TYPE____SOLOR.getNid()) {
                 setText("ABR");
              } else {
                 setText(manifold.getPreferredDescriptionText(descriptionType));
              } 
              
              break;
           default: 
              if (semanticVersion.getNid() == MetaData.PATH____SOLOR.getNid()) {
                 setText("PATH");
              } else {
                 setText(semanticVersion.getChronology().getVersionType().getWhatName());
              }
           
        }
   }
   
}