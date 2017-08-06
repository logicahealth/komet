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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TreeTableCell;
import sh.isaac.api.observable.ObservableCategorizedVersion;

/**
 *
 * @author kec
 */
public class TreeTableTimeCell  extends TreeTableCell<ObservableCategorizedVersion, Long> {
   private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

   @Override
   protected void updateItem(Long time, boolean empty) {
     super.updateItem(time, empty);
     if (empty || time == null) {
         setText(null);
         setGraphic(null);
     } else {
         setText(formatter.format(Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC)));
     }
   }
   
}