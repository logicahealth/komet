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
package sh.komet.gui.control;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;

//~--- non-JDK imports --------------------------------------------------------
import javafx.application.Platform;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import sh.isaac.api.Get;

import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public final class ComponentPanel
        extends Pane {

   private final int rowHeight = 25;
   private final int badgeWidth = 25;
   GridPane gridpane = new GridPane();
   Text componentType = new Text();
   Text componentText = new Text();
   Node editControl = Iconography.EDIT_PENCIL.getIconographic();
   ArrayList<Node> badges = new ArrayList<>();
   Node commitNode = Iconography.CIRCLE_A.getIconographic();
   ExpandControl expandControl = new ExpandControl();
   int columns = 10;
   int wrappingWidth = 300;
   private final SimpleBooleanProperty isDescription = new SimpleBooleanProperty(false);
   private final SimpleBooleanProperty isConcept = new SimpleBooleanProperty(false);
   private final SimpleBooleanProperty isInactive = new SimpleBooleanProperty(false);
   private final SimpleBooleanProperty isLogicalDefinition = new SimpleBooleanProperty(false);
   private final Manifold manifold;
   private final CategorizedVersions<ObservableCategorizedVersion> categorizedVersions;
   private final ObservableCategorizedVersion version;

   //~--- initializers --------------------------------------------------------
   {
      isDescription.addListener(this::pseudoStateChanged);
      isInactive.addListener(this::pseudoStateChanged);
      isConcept.addListener(this::pseudoStateChanged);
      isLogicalDefinition.addListener(this::pseudoStateChanged);
   }

   //~--- constructors --------------------------------------------------------
   public ComponentPanel(Manifold manifold, CategorizedVersions<ObservableCategorizedVersion> categorizedVersions) {
      this.manifold = manifold;
      this.categorizedVersions = categorizedVersions;
      if (categorizedVersions.getLatestVersion().isAbsent()) {
         throw new IllegalStateException("Must have a latest version: " + categorizedVersions);
      }
      this.version = categorizedVersions.getLatestVersion()
              .get();
      isInactive.set(this.version.getState() == State.INACTIVE);
      expandControl.expandActionProperty().addListener(this::expand);

      // gridpane.gridLinesVisibleProperty().set(true);
      this.getStyleClass()
              .add(StyleClasses.COMPONENT_PANEL.toString());
      this.getChildren()
              .add(gridpane);

      componentType.getStyleClass()
              .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
      componentText.getStyleClass()
              .add(StyleClasses.COMPONENT_TEXT.toString());
      componentText.setWrappingWidth(wrappingWidth);
      componentText.boundsInLocalProperty()
              .addListener(this::textLayoutChanged);

      ObservableVersion observableVersion = this.version.getObservableVersion();
      isInactive.set(observableVersion.getState() != State.ACTIVE);
      if (observableVersion instanceof DescriptionVersion) {
         isDescription.set(true);
         setupDescription((DescriptionVersion) observableVersion);
      } else if (observableVersion instanceof ConceptVersion) {
         isConcept.set(true);
         setupConcept((ConceptVersion) observableVersion);
      } else if (observableVersion instanceof LogicGraphVersion) {
         isLogicalDefinition.set(true);
         setupDef((LogicGraphVersion) observableVersion);
      } else {
         setupOther(observableVersion);
      }

      this.widthProperty()
              .addListener(this::widthChanged);
   }

   //~--- methods -------------------------------------------------------------
   private void expand(ObservableValue<? extends ExpandAction> observable, ExpandAction oldValue, ExpandAction newValue) {
      //TODO add add show/hide actions. 
   }

   private void pseudoStateChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      if (observable == isDescription) {
         this.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, newValue);
      } else if (observable == isInactive) {
         this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, newValue);
      } else if (observable == isConcept) {
         this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, newValue);
      } else if (observable == isLogicalDefinition) {
         this.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, newValue);
      }
   }

   private void resetComponentTextHeight(int newRows) {
      gridpane.getChildren()
              .remove(expandControl);
      GridPane.setConstraints(expandControl, 0, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren()
              .add(expandControl);  // next is 1
      gridpane.getChildren()
              .remove(componentType);
      GridPane.setConstraints(componentType, 1, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren()
              .add(componentType);  // next is 3

      gridpane.getChildren()
              .remove(commitNode);
      GridPane.setConstraints(commitNode, columns, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren()
              .add(commitNode);
      gridpane.getChildren()
              .remove(editControl);
      GridPane.setConstraints(editControl, columns, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren()
              .add(editControl);
      componentText.getLayoutBounds()
              .getHeight();
      gridpane.getChildren()
              .remove(componentText);

      GridPane.setConstraints(
              componentText,
              3,
              0,
              columns - 4,
              (int) newRows,
              HPos.LEFT,
              VPos.TOP,
              Priority.ALWAYS,
              Priority.ALWAYS);
      gridpane.getChildren()
              .add(componentText);
      gridpane.getRowConstraints()
              .clear();
      gridpane.getRowConstraints()
              .add(new RowConstraints(rowHeight));  // add row zero...

      for (int i = 0; i < badges.size();) {
         for (int row = 1; i < badges.size(); row++) {
            gridpane.getRowConstraints()
                    .add(new RowConstraints(rowHeight));

            if (row + 1 <= newRows) {
               for (int column = 1; (column < 3) && (i < badges.size()); column++) {
                  setupBadge(badges.get(i++), column, row);
               }
            } else {
               for (int column = 1; (column < columns) && (i < badges.size()); column++) {
                  setupBadge(badges.get(i++), column, row);
               }
            }
         }
      }
   }

   private void setupBadge(Node badge, int column, int row) {
      gridpane.getChildren()
              .remove(badge);
      GridPane.setConstraints(
              badge,
              column,
              row,
              1,
              1,
              HPos.CENTER,
              VPos.CENTER,
              Priority.NEVER,
              Priority.NEVER,
              new Insets(2));
      gridpane.getChildren()
              .add(badge);

      if (!badge.getStyleClass()
              .contains(StyleClasses.COMPONENT_BADGE.toString())) {
         badge.getStyleClass()
                 .add(StyleClasses.COMPONENT_BADGE.toString());
      }
   }


   private void setupOther(Version version) {
      if (version instanceof SememeVersion) {
         SememeVersion sememeVersion = (SememeVersion) version;
         SememeType sememeType = sememeVersion.getChronology().getSememeType();
         componentType.setText(sememeType.toString());
         switch (sememeType) {
            case STRING:
               componentType.setText("EXT");
               componentText.setText(manifold.getPreferredDescriptionText(sememeVersion.getAssemblageSequence()) + "\n" +
                       ((StringVersion) sememeVersion).getString());
               break;
            case COMPONENT_NID:
               componentType.setText("REF");
               int nid = ((ComponentNidVersion) sememeVersion).getComponentNid();
               switch (Get.identifierService().getChronologyTypeForNid(nid)) {
                  case CONCEPT:
                     componentText.setText(manifold.getFullySpecifiedDescriptionText(nid));
                     break;
                  case SEMEME:
                     SememeChronology sc = Get.sememeService().getSememe(nid);
                     componentText.setText("References: " + sc.getSememeType().toString());
                     break;
                  case UNKNOWN_NID:
                  default:
                     componentText.setText(Get.identifierService().getChronologyTypeForNid(nid).toString());
               }
               break;
            case LOGIC_GRAPH:
               componentText.setText(((LogicGraphVersion) sememeVersion).getLogicalExpression().toString());
               break;
            case LONG:
               componentText.setText(Long.toString(((LongVersion) sememeVersion).getLongValue()));
               break;
            case MEMBER:
               componentText.setText("Member");
               break;
            case DYNAMIC:
            case UNKNOWN:
            case DESCRIPTION:
            default:
               throw new UnsupportedOperationException("Can't handle: " + sememeType);

         }
      } else {
         componentText.setText(version.getClass().getSimpleName());
      }

   }

   private void textLayoutChanged(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
      double doubleRows = newBounds.getHeight() / rowHeight;
      int rows = (int) doubleRows + 1;

      resetComponentTextHeight(rows);
      setupColumns();
   }

   private void widthChanged(ObservableValue<? extends Number> observableWidth, Number oldWidth, Number newWidth) {
      int newTextWidth = (int) (newWidth.intValue() - (4 * badgeWidth));

      setupColumns();

      if (newTextWidth > 0) {
         int oldTextWidth = (int) componentText.getWrappingWidth();

         if (newTextWidth != oldTextWidth) {
            this.wrappingWidth = newTextWidth;
            Platform.runLater(
                    () -> {
                       componentText.setWrappingWidth(wrappingWidth);
                    });
         }
      }
   }

   public void setupColumns() {
      if (this.getParent() != null) {
         int width = (int) getParent().getBoundsInLocal().getWidth();
         this.columns = (width / badgeWidth) - 1;
         gridpane.getColumnConstraints().clear();
         for (int i = 0; i < this.columns; i++) {
            gridpane.getColumnConstraints().add(new ColumnConstraints(badgeWidth));
         }
      }
   }
   private void setupConcept(ConceptVersion conceptVersion) {
      componentType.setText("Concept");
      componentText.setText("");
   }

   private void setupDescription(DescriptionVersion description) {
      componentText.setText(description.getText());

      int descriptionType = description.getDescriptionTypeConceptSequence();

      if (descriptionType == TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()) {
         componentType.setText("FSN");
      } else if (descriptionType == TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence()) {
         componentType.setText("SYN");
      } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
         componentType.setText("DEF");
      } else {
         componentType.setText(manifold.getPreferredDescriptionText(descriptionType));
      }
      if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_CASE_SENSITIVE.getConceptSequence()) {
         badges.add(Iconography.CASE_SENSITIVE.getIconographic());
      } else if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getConceptSequence()) {
         // TODO get iconographic for initial character sensitive
         badges.add(Iconography.CASE_SENSITIVE.getIconographic());
      } else if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence()) {
         badges.add(Iconography.CASE_SENSITIVE_NOT.getIconographic());
      }

   }

   private void setupDef(LogicGraphVersion logicGraphVersion) {
      componentType.setText("DEF");
      componentText.setText(logicGraphVersion.getLogicalExpression().toString());
      if (manifold.getLogicCoordinate().getInferredAssemblageSequence() == logicGraphVersion.getAssemblageSequence()) {
         badges.add(Iconography.SETTINGS_GEAR.getIconographic());
      } else if (manifold.getLogicCoordinate().getStatedAssemblageSequence() == logicGraphVersion.getAssemblageSequence()) {
         badges.add(Iconography.ICON_EXPORT.getIconographic());
      }
   }
}
