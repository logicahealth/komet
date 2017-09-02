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
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import org.apache.mahout.math.map.OpenIntIntHashMap;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
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

import static sh.komet.gui.style.StyleClasses.ADD_ATTACHMENT;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public abstract class BadgedVersionPanel
        extends Pane {
   public static final int FIRST_COLUMN_WIDTH = 32;

   //~--- fields --------------------------------------------------------------

   protected final int                            badgeWidth          = 25;
   protected final ArrayList<Node>                badges              = new ArrayList<>();
   protected int                                  columns             = 10;
   protected final Text                           componentText       = new Text();
   protected final Text                           componentType       = new Text();
   protected final MenuButton                     editControl         = new MenuButton("", Iconography.EDIT_PENCIL.getIconographic());
   protected final MenuButton addAttachmentControl = new MenuButton("", Iconography.combine(Iconography.PLUS, Iconography.PAPERCLIP));
   protected final ExpandControl                  expandControl       = new ExpandControl();
   protected final GridPane                       gridpane            = new GridPane();
   protected final SimpleBooleanProperty          isConcept           = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty          isContradiction     = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty          isDescription       = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty          isInactive          = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty          isLogicalDefinition = new SimpleBooleanProperty(false);
   protected final int                            rowHeight           = 25;
   protected final StampControl                   stampControl        = new StampControl();
   protected int                                  wrappingWidth       = 300;
   protected final ObservableList<ComponentPanel> extensionPanels     = FXCollections.observableArrayList();
   protected final ObservableList<VersionPanel>   versionPanels       = FXCollections.observableArrayList();
   protected final CheckBox                       revertCheckBox      = new CheckBox();
   private final ObservableCategorizedVersion     categorizedVersion;
   private final Manifold                         manifold;
   protected int                                  rows;

   //~--- initializers --------------------------------------------------------

   {
      isDescription.addListener(this::pseudoStateChanged);
      isInactive.addListener(this::pseudoStateChanged);
      isConcept.addListener(this::pseudoStateChanged);
      isLogicalDefinition.addListener(this::pseudoStateChanged);
      isContradiction.addListener(this::pseudoStateChanged);
   }

   //~--- constructors --------------------------------------------------------

   public BadgedVersionPanel(Manifold manifold,
                             ObservableCategorizedVersion categorizedVersion,
                             OpenIntIntHashMap stampOrderHashMap) {
      this.manifold           = manifold;
      this.categorizedVersion = categorizedVersion;
      isInactive.set(categorizedVersion.getState() == State.INACTIVE);
      expandControl.expandActionProperty()
                   .addListener(this::expand);
      this.getChildren()
          .add(gridpane);
      componentType.getStyleClass()
                   .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
      componentText.getStyleClass()
                   .add(StyleClasses.COMPONENT_TEXT.toString());
      componentText.setWrappingWidth(wrappingWidth);
      componentText.boundsInLocalProperty()
                   .addListener(this::textLayoutChanged);
      isInactive.set(this.categorizedVersion.getState() != State.ACTIVE);
      this.stampControl.setStampedVersion(
          categorizedVersion.getStampSequence(),
          manifold,
          stampOrderHashMap.get(categorizedVersion.getStampSequence()));
      badges.add(this.stampControl);
      this.widthProperty()
          .addListener(this::widthChanged);

      ObservableVersion observableVersion = categorizedVersion.getObservableVersion();

      addAttachmentControl.getStyleClass()
                      .setAll(ADD_ATTACHMENT.toString());
      addAttachmentControl.getItems().addAll(getAttachmentMenuItems());
      editControl.getStyleClass()
                      .setAll(StyleClasses.EDIT_COMPONENT_BUTTON.toString());
      editControl.getItems().addAll(getEditMenuItems());

      if (observableVersion instanceof DescriptionVersion) {
         isDescription.set(true);
         setupDescription((DescriptionVersion) observableVersion, isLatestPanel());
      } else if (observableVersion instanceof ConceptVersion) {
         isConcept.set(true);
         setupConcept((ConceptVersion) observableVersion, isLatestPanel());
      } else if (observableVersion instanceof LogicGraphVersion) {
         isLogicalDefinition.set(true);
         setupDef((LogicGraphVersion) observableVersion, isLatestPanel());
      } else {
         setupOther(observableVersion, isLatestPanel());
      }
   }

   //~--- methods -------------------------------------------------------------
   
   public List<MenuItem> getAttachmentMenuItems() {
      return FxGet.rulesDrivenKometService().getAttachmentMenuItems(manifold, this);
   }

   public List<MenuItem> getEditMenuItems() {
      return FxGet.rulesDrivenKometService().getEditMenuItems(manifold, this);
   }

   public void doExpandAllAction(ExpandAction action) {
      expandControl.setExpandAction(action);
      extensionPanels.forEach((panel) -> panel.doExpandAllAction(action));
   }

   protected abstract void addExtras();

   protected final void expand(ObservableValue<? extends ExpandAction> observable,
                               ExpandAction oldValue,
                               ExpandAction newValue) {
      redoLayout();
   }

   protected void setupConcept(ConceptVersion conceptVersion, boolean latest) {
      if (latest) {
         componentType.setText("Concept");
         componentText.setText(
             "\n" + conceptVersion.getState() + " in " + getManifold().getPreferredDescriptionText(
                 conceptVersion.getModuleSequence()) + " on " + getManifold().getPreferredDescriptionText(
                     conceptVersion.getPathSequence()));
      } else {
         componentType.setText("");
         componentText.setText(
             conceptVersion.getState() + " in " + getManifold().getPreferredDescriptionText(
                 conceptVersion.getModuleSequence()) + " on " + getManifold().getPreferredDescriptionText(
                     conceptVersion.getPathSequence()));
      }
   }

   protected void setupDef(LogicGraphVersion logicGraphVersion, boolean latest) {
      if (latest) {
         componentType.setText("DEF");

         if (getManifold().getLogicCoordinate()
                          .getInferredAssemblageSequence() == logicGraphVersion.getAssemblageSequence()) {
            badges.add(Iconography.SETTINGS_GEAR.getIconographic());
         } else if (getManifold().getLogicCoordinate()
                                 .getStatedAssemblageSequence() == logicGraphVersion.getAssemblageSequence()) {
            badges.add(Iconography.ICON_EXPORT.getIconographic());
         }
      } else {
         componentType.setText("");
      }

      componentText.setText(logicGraphVersion.getLogicalExpression()
            .toSimpleString());
   }

   protected void setupDescription(DescriptionVersion description, boolean latest) {
      componentText.setText(description.getText());

      if (latest) {
         int descriptionType = description.getDescriptionTypeConceptSequence();

         if (descriptionType == TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()) {
            componentType.setText("FSN");
         } else if (descriptionType == TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence()) {
            componentType.setText("SYN");
         } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
            componentType.setText("DEF");
         } else {
            componentType.setText(getManifold().getPreferredDescriptionText(descriptionType));
         }
      } else {
         componentType.setText("");
      }

      if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_CASE_SENSITIVE.getConceptSequence()) {
         badges.add(Iconography.CASE_SENSITIVE.getIconographic());
      } else if (description.getCaseSignificanceConceptSequence() ==
                 TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getConceptSequence()) {
         // TODO get iconographic for initial character sensitive
         badges.add(Iconography.CASE_SENSITIVE.getIconographic());
      } else if (description.getCaseSignificanceConceptSequence() ==
                 TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence()) {
         badges.add(Iconography.CASE_SENSITIVE_NOT.getIconographic());
      }
   }

   protected void setupOther(Version version, boolean latest) {
      if (version instanceof SememeVersion) {
         SememeVersion sememeVersion = (SememeVersion) version;
         SememeType    sememeType    = sememeVersion.getChronology()
                                                    .getSememeType();

         componentType.setText(sememeType.toString());

         switch (sememeType) {
         case STRING:
            if (latest) {
               componentType.setText("STR");
            } else {
               componentType.setText("");
            }

            componentText.setText(
                getManifold().getPreferredDescriptionText(
                    sememeVersion.getAssemblageSequence()) + "\n" + ((StringVersion) sememeVersion).getString());
            break;

         case COMPONENT_NID:
            if (latest) {
               componentType.setText("REF");
            } else {
               componentType.setText("");
            }

            int nid = ((ComponentNidVersion) sememeVersion).getComponentNid();

            switch (Get.identifierService()
                       .getChronologyTypeForNid(nid)) {
            case CONCEPT:
               componentText.setText(
                   getManifold().getPreferredDescriptionText(
                       sememeVersion.getAssemblageSequence()) + "\n" + getManifold().getPreferredDescriptionText(nid));
               break;

            case SEMEME:
               SememeChronology sc = Get.sememeService()
                                        .getSememe(nid);

               componentText.setText(
                   getManifold().getPreferredDescriptionText(
                       sememeVersion.getAssemblageSequence()) + "\nReferences: " + sc.getSememeType().toString());
               break;

            case UNKNOWN_NID:
            default:
               componentText.setText(
                   getManifold().getPreferredDescriptionText(
                       sememeVersion.getAssemblageSequence()) + "\nReferences:" +
                       Get.identifierService().getChronologyTypeForNid(
                           nid).toString());
            }

            break;

         case LOGIC_GRAPH:
            if (latest) {
               componentType.setText("DEF");
            } else {
               componentType.setText("");
            }

            componentText.setText(((LogicGraphVersion) sememeVersion).getLogicalExpression()
                  .toString());
            break;

         case LONG:
            if (latest) {
               componentType.setText("INT");
            } else {
               componentType.setText("");
            }

            componentText.setText(Long.toString(((LongVersion) sememeVersion).getLongValue()));
            break;

         case MEMBER:
            componentText.setText(
                getManifold().getPreferredDescriptionText(sememeVersion.getAssemblageSequence()) + "\nMember");
            break;

         case DYNAMIC:
         case UNKNOWN:
         case DESCRIPTION:
         default:
            throw new UnsupportedOperationException("Can't handle: " + sememeType);
         }
      } else {
         componentText.setText(version.getClass()
                                      .getSimpleName());
      }
   }

   protected void textLayoutChanged(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
      redoLayout();
   }

   protected void widthChanged(ObservableValue<? extends Number> observableWidth, Number oldWidth, Number newWidth) {
      redoLayout();
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
      } else if (observable == isContradiction) {
         this.pseudoClassStateChanged(PseudoClasses.CONTRADICTED_PSEUDO_CLASS, newValue);
      }
   }

   private void redoLayout() {
      double doubleRows = componentText.boundsInLocalProperty()
                                       .get()
                                       .getHeight() / rowHeight;
      int    rowsOfText = (int) doubleRows + 1;

      gridpane.setMinWidth(layoutBoundsProperty().get()
            .getWidth());
      gridpane.setPrefWidth(layoutBoundsProperty().get()
            .getWidth());
      gridpane.setMaxWidth(layoutBoundsProperty().get()
            .getWidth());
      setupColumns();
      wrappingWidth = (int) (layoutBoundsProperty().get()
            .getWidth() - (5 * badgeWidth));
      componentText.setWrappingWidth(wrappingWidth);
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
              .remove(addAttachmentControl);
      GridPane.setConstraints(addAttachmentControl,
          columns,
          1,
          2,
          1,
          HPos.RIGHT,
          VPos.CENTER,
          Priority.SOMETIMES,
          Priority.NEVER,
          new Insets(0, 4, 1, 0));
      gridpane.getChildren()
              .add(addAttachmentControl);
      gridpane.getChildren()
              .remove(editControl);
      GridPane.setConstraints(
          editControl,
          columns,
          0,
          2,
          1,
          HPos.RIGHT,
          VPos.TOP,
          Priority.SOMETIMES,
          Priority.NEVER,
          new Insets(1, 4, 0, 0));
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
          (int) rowsOfText,
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

      boolean firstBadgeAdded = false;

      for (int i = 0; i < badges.size(); ) {
         for (int row = 1; i < badges.size(); row++) {
            this.rows = row;
            gridpane.getRowConstraints()
                    .add(new RowConstraints(rowHeight));

            if (row + 1 <= rowsOfText) {
               for (int column = 0; (column < 3) && (i < badges.size()); column++) {
                  if (firstBadgeAdded && (column == 0)) {
                     column          = 1;
                     firstBadgeAdded = true;
                  }

                  setupBadge(badges.get(i++), column, row);
               }
            } else {
               for (int column = 0; (column < columns) && (i < badges.size()); column++) {
                  if (firstBadgeAdded && (column == 0)) {
                     column          = 1;
                     firstBadgeAdded = true;
                  }

                  setupBadge(badges.get(i++), column, row);
               }
            }
         }
      }

      addExtras();
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

   private void setupColumns() {
      if (this.getParent() != null) {
         this.columns = (int) (getLayoutBounds().getWidth() / badgeWidth) - 1;

         if (this.columns < 6) {
            this.columns = 6;
         }

         gridpane.getColumnConstraints()
                 .clear();

         for (int i = 0; i < this.columns; i++) {
            if (i == 0) {
               gridpane.getColumnConstraints()
                       .add(new ColumnConstraints(FIRST_COLUMN_WIDTH));
            } else {
               gridpane.getColumnConstraints()
                       .add(new ColumnConstraints(badgeWidth));
            }
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the categorizedVersion
    */
   public final ObservableCategorizedVersion getCategorizedVersion() {
      return categorizedVersion;
   }

   @Override
   public final ObservableList<Node> getChildren() {
      return super.getChildren();
   }

   public int getColumns() {
      return columns;
   }

   protected abstract boolean isLatestPanel();

   /**
    * @return the manifold
    */
   public Manifold getManifold() {
      return manifold;
   }

   public int getRows() {
      return rows;
   }
}

