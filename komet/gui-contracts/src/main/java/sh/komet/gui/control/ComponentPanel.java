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
package sh.komet.gui.control;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ComponentPanel extends Pane {

   private final Manifold manifold;
   private final DescriptionVersion component;
   
   private double rowHeight = 25;
   private double badgeWidth = 25;
   private enum DisclosureState {
      CLICK_TO_CLOSE, CLICK_TO_OPEN
   };

   GridPane gridpane = new GridPane();
   Button expandControl = new Button("", Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic());
   DisclosureState disclosureState = DisclosureState.CLICK_TO_OPEN;
   Text componentType = new Text();
   Text componentText = new Text();
   Node editControl = Iconography.EDIT_PENCIL.getIconographic();
   ArrayList<Node> badges = new ArrayList<>();
   Node commitNode = Iconography.CIRCLE_A.getIconographic();
   int columns = 10;

   public ComponentPanel(Manifold manifold, DescriptionVersion component) {
      this.manifold = manifold;
      this.component = component;
      expandControl.setOnAction(this::expand);
      this.getChildren().add(gridpane);
      Node[] badgeList = new Node[]{
         Iconography.CASE_SENSITIVE.getIconographic(),
         Iconography.ICON_CLASSIFIER1.getIconographic(),
         Iconography.LOOK_EYE.getIconographic(),
         Iconography.CLASSIC_TREE_LAYOUT.getIconographic(),
         Iconography.CIRCLE_Z.getIconographic(),
         Iconography.CIRCLE_X.getIconographic(),
         Iconography.CIRCLE_C.getIconographic(),
         Iconography.CIRCLE_V.getIconographic(),
         Iconography.CIRCLE_B.getIconographic(),
         Iconography.CIRCLE_N.getIconographic(),
         Iconography.CIRCLE_M.getIconographic(),
         Iconography.CIRCLE_P.getIconographic(),
      };
      badges.addAll(Arrays.asList(badgeList));
      GridPane.setConstraints(expandControl, 0, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER, new Insets(2));
      gridpane.getChildren().add(expandControl); // next is 1
      GridPane.setConstraints(componentType, 1, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER, new Insets(2));
      gridpane.getChildren().add(componentType); // next is 3
      GridPane.setConstraints(componentText, 3, 0, 7, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS, new Insets(2));
      gridpane.getChildren().add(componentText); // next is 10
      GridPane.setConstraints(editControl, 10, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER, new Insets(2));
      gridpane.getChildren().add(editControl); // next is 10
      // next row
      // TODO wrap to next row if to many... 
      for (int i = 0; i < badges.size(); i++) {
         Node badge = badges.get(i);
         GridPane.setConstraints(badge, i, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(2));
         gridpane.getChildren().add(badge);
      }
      GridPane.setConstraints(commitNode, 10, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(2));
      gridpane.add(commitNode, 10, 1, 1, 1); // next is 10

      componentType.setText("SYN");
      componentText.setText("By default the alignment of a child");

      componentText.setWrappingWidth(300);
      componentText.layoutBoundsProperty().addListener(this::textLayoutChanged);
   }

   private void expand(ActionEvent event) {
      switch (disclosureState) {
         case CLICK_TO_CLOSE:
            disclosureState = DisclosureState.CLICK_TO_OPEN;
            expandControl.setGraphic(Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic());
            break;
         case CLICK_TO_OPEN:
            disclosureState = DisclosureState.CLICK_TO_CLOSE;
            expandControl.setGraphic(Iconography.TAXONOMY_CLICK_TO_CLOSE.getIconographic());
            componentText.setText(componentText.getText() + " " + componentText.getText());
            break;

         default:

      }
   }

   private void textLayoutChanged(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
      System.out.println("layout changed to: " + newBounds);
      Font font = componentText.getFont();
      double size = font.getSize();
      double lines = newBounds.getHeight() / size;
      System.out.println("layout lines to: " + lines + " (font size = " + size + ")");
      resetComponentTextHeight(lines);
      this.columns = (int) ((newBounds.getWidth() - (3 * badgeWidth))/ badgeWidth);
      
   }

   private void resetComponentTextHeight(double newLines) {
      componentText.getLayoutBounds().getHeight();
      Double size = componentText.getFont().getSize();
      GridPane.setConstraints(componentText, 3, 0, 7, (int) newLines, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS, new Insets(2));
      gridpane.getRowConstraints().clear();
      gridpane.getRowConstraints().add(new RowConstraints(rowHeight)); // add row zero...
      for (int i = 0; i < badges.size();) {
         for (int row = 1; i < badges.size(); row++) {
            gridpane.getRowConstraints().add(new RowConstraints(rowHeight));
            if (row + 1 < newLines) {
               for (int column = 0; column < 3 && i < badges.size(); column++) {
                  setupBadge(badges.get(i++), column, row);
               }
            } else {
               for (int column = 0; column < columns && i < badges.size(); column++) {
                  setupBadge(badges.get(i++), column, row);
               }
            }
         }
      }
   }

   private void setupBadge(Node badge, int column, int row) {
      gridpane.getChildren().remove(badge);
      GridPane.setConstraints(badge, column, row, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(2));
      gridpane.getChildren().add(badge);
   }

}

/*
public static void setConstraints(Node child,
                                  int columnIndex,
                                  int rowIndex,
                                  int columnspan,
                                  int rowspan,
                                  HPos halignment,
                                  VPos valignment,
                                  Priority hgrow,
                                  Priority vgrow,
                                  Insets margin)
Sets the grid position, spans, alignment, grow priorities, and margin for the child when contained in a gridpane.
Parameters:
child - the child node of a gridpane
columnIndex - the column index position for the child
rowIndex - the row index position for the child
columnspan - the number of columns the child should span
rowspan - the number of rows the child should span
halignment - the horizontal alignment of the child
valignment - the vertical alignment of the child
hgrow - the horizontal grow priority of the child
vgrow - the vertical grow priority of the child
margin - the margin of space around the child
*/
