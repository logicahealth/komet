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
package sh.komet.gui.cell;

//~--- JDK imports ------------------------------------------------------------
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

//~--- non-JDK imports --------------------------------------------------------
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
import sh.isaac.api.chronicle.VersionType;
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

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class TreeTableGeneralCell
        extends KometTreeTableCell<ObservableCategorizedVersion> {

   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------
   private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
   private final Manifold manifold;

   //~--- constructors --------------------------------------------------------
   public TreeTableGeneralCell(Manifold manifold) {
      this.manifold = manifold;
      getStyleClass().add("komet-version-general-cell");
      getStyleClass().add("isaac-version");
   }

   //~--- methods -------------------------------------------------------------
   public void addTextToCell(Text... text) {
      TextFlow textFlow = new TextFlow(text);

      textFlow.setPrefWidth(this.getWidth() - (textFlow.getInsets().getLeft() + textFlow.getInsets().getRight()));
      textFlow.setMaxWidth(this.getWidth());
      textFlow.setLayoutX(1);
      textFlow.setLayoutY(1);

      FixedSizePane fixedSizePane = new FixedSizePane(textFlow);

      this.widthProperty()
              .addListener(
                      new WeakChangeListener<>(
                              (ObservableValue<? extends Number> observable,
                                      Number oldValue,
                                      Number newValue) -> {
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

   @Override
   protected void updateItem(TreeTableRow<ObservableCategorizedVersion> row, ObservableCategorizedVersion version) {
      setWrapText(false);

      SememeVersion sememeVersion = version.unwrap();
      VersionType sememeType = sememeVersion.getChronology()
              .getSememeType();

      this.setGraphic(null);
      this.setContentDisplay(ContentDisplay.TEXT_ONLY);

      Text assemblageNameText = new Text(
              manifold.getPreferredDescriptionText(sememeVersion.getAssemblageSequence()) + "\n");

      assemblageNameText.getStyleClass()
              .add(StyleClasses.ASSEMBLAGE_NAME_TEXT.toString());

      String referencedComponentString = manifold.getPreferredDescriptionText(
              sememeVersion.getReferencedComponentNid());
      Text referencedComponentText = new Text("\n" + referencedComponentString);
      Text referencedComponentTextNoNewLine = new Text(
              manifold.getPreferredDescriptionText(
                      sememeVersion.getReferencedComponentNid()));

      switch (Get.identifierService()
              .getChronologyTypeForNid(sememeVersion.getReferencedComponentNid())) {
         case CONCEPT:
            referencedComponentText.getStyleClass()
                    .add(StyleClasses.CONCEPT_COMPONENT_REFERENCE.toString());
            referencedComponentTextNoNewLine.getStyleClass()
                    .add(StyleClasses.CONCEPT_COMPONENT_REFERENCE.toString());
            break;

         case SEMEME:
            referencedComponentText.getStyleClass()
                    .add(StyleClasses.SEMEME_COMPONENT_REFERENCE.toString());
            referencedComponentTextNoNewLine.getStyleClass()
                    .add(StyleClasses.SEMEME_COMPONENT_REFERENCE.toString());
            break;

         case UNKNOWN_NID:
         default:
            referencedComponentText.getStyleClass()
                    .add(StyleClasses.ERROR_TEXT.toString());
            referencedComponentTextNoNewLine.getStyleClass()
                    .add(StyleClasses.ERROR_TEXT.toString());
      }

      switch (sememeType) {
         case DESCRIPTION:
            DescriptionVersion description = version.unwrap();

            this.setText(null);

            Text text = new Text(description.getText());

            text.wrappingWidthProperty()
                    .bind(getTableColumn().widthProperty()
                            .subtract(5));
            this.setGraphic(text);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            text.getStyleClass()
                    .addAll(this.getStyleClass());
            break;

         case COMPONENT_NID:
            ComponentNidVersion componentNidVersion = version.unwrap();

            switch (Get.identifierService()
                    .getChronologyTypeForNid(componentNidVersion.getComponentNid())) {
               case CONCEPT:
                  Text conceptText = new Text(manifold.getPreferredDescriptionText(componentNidVersion.getComponentNid()));

                  conceptText.getStyleClass()
                          .add(StyleClasses.CONCEPT_TEXT.toString());
                  addTextToCell(assemblageNameText, conceptText, referencedComponentText);
                  break;

               case SEMEME:
                  SememeChronology sememe = Get.assemblageService()
                          .getSememe(componentNidVersion.getComponentNid());
                  LatestVersion<SememeVersion> latest = sememe.getLatestVersion(manifold);

                  if (latest.isPresent()) {
                     Text sememeText = new Text(latest.get().toUserString());

                     sememeText.getStyleClass()
                             .add(StyleClasses.SEMEME_TEXT.toString());
                     addTextToCell(assemblageNameText, sememeText, referencedComponentText);
                  } else {
                     Text sememeText = new Text("No latest version for component");

                     sememeText.getStyleClass()
                             .add(StyleClasses.SEMEME_TEXT.toString());
                     addTextToCell(assemblageNameText, sememeText, referencedComponentText);
                  }

                  break;

               case UNKNOWN_NID:
                  LOG.warn("Unknown nid: " + componentNidVersion);

                  Text unknownText = new Text("Unknown nid: " + componentNidVersion);

                  unknownText.getStyleClass()
                          .add(StyleClasses.ERROR_TEXT.toString());
                  addTextToCell(assemblageNameText, unknownText, referencedComponentText);
                  break;
            }

            break;

         case STRING:
            StringVersion stringVersion = version.unwrap();
            Text stringText = new Text(stringVersion.getString());

            stringText.getStyleClass()
                    .add(StyleClasses.SEMEME_TEXT.toString());
            addTextToCell(assemblageNameText, stringText, referencedComponentText);
            break;

         case LOGIC_GRAPH:
            LogicGraphVersion logicGraphVersion = version.unwrap();
            Text definitionText = new Text("\n" + logicGraphVersion.getLogicalExpression().toSimpleString());

            definitionText.getStyleClass()
                    .add(StyleClasses.DEFINITION_TEXT.toString());
            addTextToCell(assemblageNameText, referencedComponentTextNoNewLine, definitionText);
            break;

         case MEMBER:
            addTextToCell(assemblageNameText, referencedComponentTextNoNewLine);
            break;

         case LONG:
            LongVersion longVersion = version.unwrap();

            // TODO, rely on semantic info from assemblage in the future to
            // eliminate this date hack...
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(longVersion.getLongValue())
                    .atZone(ZoneOffset.UTC);

            if ((zonedDateTime.getYear() > 1900) && (zonedDateTime.getYear() < 2232)) {
               Text dateText = new Text(formatter.format(zonedDateTime));

               dateText.getStyleClass()
                       .add(StyleClasses.DATE_TEXT.toString());
               addTextToCell(assemblageNameText, dateText, referencedComponentText);
            } else {
               Text longText = new Text(Long.toString(longVersion.getLongValue()));

               longText.getStyleClass()
                       .add(StyleClasses.DATE_TEXT.toString());
               addTextToCell(assemblageNameText, longText, referencedComponentText);
            }

            break;

         default:
            Text defaultText = new Text("not implemented for type: " + sememeType);

            defaultText.getStyleClass()
                    .add(StyleClasses.ERROR_TEXT.toString());
            addTextToCell(assemblageNameText, defaultText, referencedComponentText);
      }
   }
}
