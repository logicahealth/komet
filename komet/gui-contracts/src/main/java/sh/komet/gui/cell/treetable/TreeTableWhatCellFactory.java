/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.komet.gui.cell.treetable;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableCell;

import javafx.scene.control.TreeTableColumn;

import javafx.util.Callback;

import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableCategorizedVersion;

import sh.komet.gui.control.property.ViewProperties;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class TreeTableWhatCellFactory
         implements Callback<TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion>,
                             TreeTableCell<ObservableCategorizedVersion, ObservableCategorizedVersion>> {
   private final ManifoldCoordinate manifold;

   //~--- constructors --------------------------------------------------------

   public TreeTableWhatCellFactory(ManifoldCoordinate manifold) {
      this.manifold = manifold;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public TreeTableCell<ObservableCategorizedVersion, ObservableCategorizedVersion> call(TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> param) {
      return new TreeTableWhatCell(this.manifold);
   }

   //~--- get methods ---------------------------------------------------------

   public ObservableValue<ObservableCategorizedVersion> getCellValue(
                       TreeTableColumn.CellDataFeatures<ObservableCategorizedVersion,
                          ObservableCategorizedVersion> param) {
      return param.getValue()
                  .valueProperty();
   }
}

