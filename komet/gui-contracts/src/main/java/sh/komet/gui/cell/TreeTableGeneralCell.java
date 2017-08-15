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

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeTableRow;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class TreeTableGeneralCell extends KometTreeTableCell<ObservableCategorizedVersion> {

   private static final Logger LOG = LogManager.getLogger();
   private final Manifold manifold;

   public TreeTableGeneralCell(Manifold manifold) {
      this.manifold = manifold;
      getStyleClass().add("komet-version-general-cell");
      getStyleClass().add("isaac-version");
   }

   @Override
   protected void updateItem(TreeTableRow<ObservableCategorizedVersion> row, ObservableCategorizedVersion version) {
      setWrapText(false);
      SememeVersion sememeVersion = version.unwrap();
      SememeType sememeType = sememeVersion.getChronology().getSememeType();
      this.setGraphic(null);
      this.setContentDisplay(ContentDisplay.TEXT_ONLY);
      switch (sememeType) {
         case DESCRIPTION:
            DescriptionVersion description = version.unwrap();
            this.setText(null);
            Text text = new Text(description.getText());
            text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(5));
            this.setGraphic(text);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            text.getStyleClass().addAll(this.getStyleClass());
            break;
         case COMPONENT_NID:
            ComponentNidVersion componentNidVersion = version.unwrap();

            switch (Get.identifierService().getChronologyTypeForNid(componentNidVersion.getComponentNid())) {
               case CONCEPT:
                  setText(manifold.getPreferredDescriptionText(componentNidVersion.getComponentNid()));
                  break;
               case SEMEME:

                  SememeChronology sememe = Get.sememeService().getSememe(componentNidVersion.getComponentNid());
                  LatestVersion<SememeVersion> latest = sememe.getLatestVersion(manifold);
                  if (latest.isPresent()) {
                     setText(latest.get().toUserString());
                  } else {
                     setText("No latet");
                  }
                  break;
               case UNKNOWN_NID:
                  LOG.warn("Unknown nid: " + componentNidVersion);
                  break;
            }
            break;
         case STRING:
            StringVersion stringVersion = version.unwrap();
            setText(stringVersion.getString());
            break;
         case LOGIC_GRAPH:
            LogicGraphVersion logicGraphVersion = version.unwrap();
            setText(logicGraphVersion.getLogicalExpression().toString());
            break;
         case MEMBER:
            break;
         case LONG:
            LongVersion longVersion = version.unwrap();
            setText(Long.toString(longVersion.getLongValue()));
            break;
         default:
            setText("not implemented for type: " + sememeType);
      }
   }

}
