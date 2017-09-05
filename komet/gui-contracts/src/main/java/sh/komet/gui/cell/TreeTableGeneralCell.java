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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeTableRow;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
import sh.komet.gui.control.FixedSizePane;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class TreeTableGeneralCell extends KometTreeTableCell<ObservableCategorizedVersion> {

   private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
      Text assemblageNameText = new Text(manifold.getPreferredDescriptionText(sememeVersion.getAssemblageSequence()) + "\n");
      assemblageNameText.getStyleClass().add(StyleClasses.ASSEMBLAGE_NAME_TEXT.toString());
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
                  Text conceptText = new Text(manifold.getPreferredDescriptionText(componentNidVersion.getComponentNid()));
                  conceptText.getStyleClass().add(StyleClasses.CONCEPT_TEXT.toString());
                  addTextToCell(assemblageNameText, conceptText);
                  break;
               case SEMEME:

                  SememeChronology sememe = Get.assemblageService().getSememe(componentNidVersion.getComponentNid());
                  LatestVersion<SememeVersion> latest = sememe.getLatestVersion(manifold);
                  if (latest.isPresent()) {
                     Text sememeText = new Text(latest.get().toUserString());
                     sememeText.getStyleClass().add(StyleClasses.SEMEME_TEXT.toString());
                     addTextToCell(assemblageNameText, sememeText);
                  } else {
                     Text sememeText = new Text("No latest version for component");
                     sememeText.getStyleClass().add(StyleClasses.SEMEME_TEXT.toString());
                     addTextToCell(assemblageNameText, sememeText);
                  }
                  break;
               case UNKNOWN_NID:
                  LOG.warn("Unknown nid: " + componentNidVersion);
                  Text unknownText = new Text("Unknown nid: " + componentNidVersion);
                  unknownText.getStyleClass().add(StyleClasses.ERROR_TEXT.toString());
                  addTextToCell(assemblageNameText, unknownText);
                  break;
            }
            break;
         case STRING:
            StringVersion stringVersion = version.unwrap();
            Text stringText = new Text(stringVersion.getString());
            stringText.getStyleClass().add(StyleClasses.SEMEME_TEXT.toString());
            addTextToCell(assemblageNameText, stringText);
            break;
         case LOGIC_GRAPH:
            LogicGraphVersion logicGraphVersion = version.unwrap();
            Text definitionText = new Text(logicGraphVersion.getLogicalExpression().toSimpleString());
            definitionText.getStyleClass().add(StyleClasses.DEFINITION_TEXT.toString());
            addTextToCell(assemblageNameText, definitionText);
            break;
         case MEMBER:
            addTextToCell(assemblageNameText);
            break;
         case LONG:
            LongVersion longVersion = version.unwrap();
            //TODO, rely on semantic info from assemblage in the future to 
            // eliminate this date hack...
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(longVersion.getLongValue()).atZone(ZoneOffset.UTC);
            if (zonedDateTime.getYear() > 1900 && zonedDateTime.getYear() < 2232) {
               Text dateText = new Text(formatter.format(zonedDateTime));
               dateText.getStyleClass().add(StyleClasses.DATE_TEXT.toString());
               addTextToCell(assemblageNameText, dateText);
            } else {
               Text longText = new Text(Long.toString(longVersion.getLongValue()));
               longText.getStyleClass().add(StyleClasses.DATE_TEXT.toString());
               addTextToCell(assemblageNameText, longText);
            }
            break;
         default:
            Text defaultText = new Text("not implemented for type: " + sememeType);
            defaultText.getStyleClass().add(StyleClasses.ERROR_TEXT.toString());
            addTextToCell(assemblageNameText, defaultText);
      }
   }

   public void addTextToCell(Text... text) {
      TextFlow textFlow = new TextFlow(text);
      textFlow.setPrefWidth(this.getWidth() - (textFlow.getInsets().getLeft() + textFlow.getInsets().getRight()));
      textFlow.setMaxWidth(this.getWidth());
      textFlow.setLayoutX(1);
      textFlow.setLayoutY(1);

      FixedSizePane fixedSizePane = new FixedSizePane(textFlow);
      this.widthProperty().addListener(new WeakChangeListener<>((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
         double newTextFlowWidth = newValue.doubleValue() - 8;
         double newTextFlowHeight = textFlow.prefHeight(newTextFlowWidth);
         textFlow.setPrefWidth(newTextFlowWidth);
         textFlow.setMaxWidth(newTextFlowWidth);
         textFlow.setPrefHeight(newTextFlowHeight);
         textFlow.setMaxHeight(newTextFlowHeight);

         double newFixedSizeWidth = newTextFlowWidth + 4;
         double newFixedSizeHeight = newTextFlowHeight + 4;
         fixedSizePane.setWidth(newFixedSizeWidth);
         fixedSizePane.setHeight(newFixedSizeHeight);
      }));
      this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      this.setGraphic(fixedSizePane);
   }

}
