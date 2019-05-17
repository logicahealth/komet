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
package sh.komet.gui.cell.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

import static sh.komet.gui.style.PseudoClasses.*;

/**
 * Parent class to handle setting of inactive, uncommitted, and uncommitted-with-error pseudo-classes.
 *
 * @author kec
 */
public abstract class KometTableCell extends TableCell<ObservableChronology, ObservableVersion> {


   /**
    * Provides handling of empty/null values, update of PseudoClasses, then calls
    * updateItem(TreeTableRow&lt;ObservableCategorizedVersion&gt; row, C cellValue);.
    *
    * @param cellValue
    * @param empty
    */
   @Override
   protected final void updateItem(ObservableVersion cellValue, boolean empty) {
      super.updateItem(cellValue, empty);
      TableRow<ObservableChronology> row = getTableRow();
      updatePseudoStates(this, cellValue, empty, row);
   }

   private void updatePseudoStates(TableCell cell,
                                   ObservableVersion cellValue, boolean empty,
                                          TableRow<ObservableChronology> row) {
      if (empty || row.getItem() == null) {
         cell.setText(null);
         cell.setGraphic(null);
         cell.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(SUPERCEDED_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(CONTRADICTED_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(CONCEPT_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(OTHER_VERSION_PSEUDO_CLASS, false);

      } else {
         ObservableChronology observableVersion = row.getItem();
         cell.pseudoClassStateChanged(OTHER_VERSION_PSEUDO_CLASS, false);
         cell.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, observableVersion.isUncommitted());
         cell.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !cellValue.getStatus().isActive());
         // TODO: check for superseded.
         cell.pseudoClassStateChanged(SUPERCEDED_PSEUDO_CLASS, false);
         // TODO: check for contradicted.
         cell.pseudoClassStateChanged(CONTRADICTED_PSEUDO_CLASS, false);
         if (observableVersion.isUncommitted()) {
            // TODO check for errors from rules, etc.
            cell.pseudoClassStateChanged(UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS, false);
         } else {
            cell.pseudoClassStateChanged(UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS, false);
         }
         updateItem(row, cellValue);
         if (cellValue instanceof ConceptVersion) {
            cell.pseudoClassStateChanged(CONCEPT_PSEUDO_CLASS, true);
            cell.pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
            cell.pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
         } else {
            cell.pseudoClassStateChanged(CONCEPT_PSEUDO_CLASS, false);
            cell.pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
            cell.pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
            switch (cellValue.getSemanticType()) {
               case DESCRIPTION:
                  cell.pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, true);
                  break;
               case LOGIC_GRAPH:
                  cell.pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, true);
                  break;
               case COMPONENT_NID:
               case DYNAMIC:
               case LONG:
               case MEMBER:
               case STRING:
               case UNKNOWN:
               default:
                  cell.pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
                  cell.pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
                  cell.pseudoClassStateChanged(OTHER_VERSION_PSEUDO_CLASS, true);
            }
         }

      }
   }

   protected abstract void updateItem(TableRow<ObservableChronology> row, ObservableVersion cellValue);
}
