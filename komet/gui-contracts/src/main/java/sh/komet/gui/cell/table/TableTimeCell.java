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
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author kec
 */
public class TableTimeCell extends KometTableCell {
   private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

   public TableTimeCell() {
      getStyleClass().add("komet-version-time-cell");
      getStyleClass().add("isaac-version");
   }

   @Override
   protected void updateItem(TableRow<ObservableChronology> row, ObservableVersion cellValue) {
         setText(formatter.format(Instant.ofEpochMilli(cellValue.getTime()).atZone(ZoneOffset.UTC)));
   }
   
}