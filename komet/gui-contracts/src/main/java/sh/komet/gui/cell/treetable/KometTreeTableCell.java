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

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableRow;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import static sh.komet.gui.style.PseudoClasses.*;
import sh.isaac.api.component.semantic.version.SemanticVersion;

/**
 * Parent class to handle setting of inactive, uncommitted, and uncommitted-with-error pseudo-classes.
 *
 * @author kec
 * @param <C> The value type for the cell
 */
public abstract class KometTreeTableCell<C> extends TreeTableCell<ObservableCategorizedVersion, C> {


   /**
    * Provides handling of empty/null values, update of PseudoClasses, then calls
    * updateItem(TreeTableRow&lt;ObservableCategorizedVersion&gt; row, C cellValue);.
    *
    * @param cellValue
    * @param empty
    */
   @Override
   protected final void updateItem(C cellValue, boolean empty) {
      super.updateItem(cellValue, empty);
      TreeTableRow<ObservableCategorizedVersion> row = getTreeTableRow();
      if (empty || row.getItem() == null) {
         setText(null);
         setGraphic(null);
         pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);
         pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, false);
         pseudoClassStateChanged(UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS, false);
         pseudoClassStateChanged(SUPERCEDED_PSEUDO_CLASS, false);
         pseudoClassStateChanged(CONTRADICTED_PSEUDO_CLASS, false);
         pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
         pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
         pseudoClassStateChanged(CONCEPT_PSEUDO_CLASS, false);
         pseudoClassStateChanged(OTHER_VERSION_PSEUDO_CLASS, false);

      } else {
         ObservableCategorizedVersion observableVersion = row.getItem();
         pseudoClassStateChanged(OTHER_VERSION_PSEUDO_CLASS, false);
         pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, observableVersion.isUncommitted());
         pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !observableVersion.getStatus().isActive());
         // TODO: check for superceded. 
         pseudoClassStateChanged(SUPERCEDED_PSEUDO_CLASS, false);
         // TODO: check for contradicted.
         pseudoClassStateChanged(CONTRADICTED_PSEUDO_CLASS, false);
         if (observableVersion.isUncommitted()) {
            // TODO check for errors from rules, etc. 
            pseudoClassStateChanged(UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS, false);
         } else {
            pseudoClassStateChanged(UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS, false);
         }
         updateItem(row, cellValue);
         Version version = observableVersion.unwrap();
         if (version instanceof ConceptVersion) {
            pseudoClassStateChanged(CONCEPT_PSEUDO_CLASS, true);
            pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
            pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
         } else {
            pseudoClassStateChanged(CONCEPT_PSEUDO_CLASS, false);
                  pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
                  pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
            SemanticVersion semanticVersion = observableVersion.unwrap();
            switch (semanticVersion.getChronology().getVersionType()) {
               case DESCRIPTION:
                  pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, true);
                  break;
               case LOGIC_GRAPH:
                  pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, true);
                  break;
               case COMPONENT_NID:
               case DYNAMIC:
               case LONG:
               case MEMBER:
               case STRING:
               case UNKNOWN:
               default:
                  pseudoClassStateChanged(LOGICAL_DEFINITION_PSEUDO_CLASS, false);
                  pseudoClassStateChanged(DESCRIPTION_PSEUDO_CLASS, false);
                  pseudoClassStateChanged(OTHER_VERSION_PSEUDO_CLASS, true);
            }
         }

      }
   }

   protected abstract void updateItem(TreeTableRow<ObservableCategorizedVersion> row, C cellValue);
}
