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

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class TreeTableAuthorTimeCellFactory  
         implements Callback<TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion>,
                             TreeTableAuthorTimeCell> {
   private final Manifold manifold;

   //~--- constructors --------------------------------------------------------

   public TreeTableAuthorTimeCellFactory(Manifold manifold) {
      this.manifold = manifold;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public TreeTableAuthorTimeCell call(TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> param) {
      return new TreeTableAuthorTimeCell(this.manifold);
   }

   //~--- get methods ---------------------------------------------------------

   public ObservableValue<ObservableCategorizedVersion> getCellValue(
                       TreeTableColumn.CellDataFeatures<ObservableCategorizedVersion,
                          ObservableCategorizedVersion> param) {
      return param.getValue()
                  .valueProperty();
   }
   
}
