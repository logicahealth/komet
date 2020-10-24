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

import javafx.scene.control.TableRow;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.komet.gui.control.property.ViewProperties;

import java.util.function.ToIntFunction;

/**
 *
 * @author kec
 */
public class TableConceptCell extends KometTableCell {
   private final ManifoldCoordinate manifoldCoordinate;
   private final ToIntFunction<ObservableVersion> conceptNidGetter;

   protected TableConceptCell() {
       throw new UnsupportedOperationException(
               "Manifold must be set. No arg constructor not allowed");
   }
   public TableConceptCell(ManifoldCoordinate manifoldCoordinate, ToIntFunction<ObservableVersion> conceptNidGetter) {
      if (manifoldCoordinate == null) {
         throw new IllegalArgumentException("manifold cannot be null");
      }
      this.manifoldCoordinate = manifoldCoordinate;
      this.conceptNidGetter = conceptNidGetter;
      getStyleClass().add("komet-version-concept-cell");
      getStyleClass().add("isaac-version");
   }

   @Override
   protected void updateItem(TableRow<ObservableChronology> row, ObservableVersion cellValue) {
       if (cellValue!= null) {
           setText(manifoldCoordinate.getPreferredDescriptionText(conceptNidGetter.applyAsInt(cellValue)));
       }
         
   }
   
}
