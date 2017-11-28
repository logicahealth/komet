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



package sh.komet.gui.search;

//~--- non-JDK imports --------------------------------------------------------

import java.util.Optional;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.scene.Node;
import javafx.scene.control.Label;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class SimpleSearchExplorationNode
         implements ExplorationNode {
   private final SimpleStringProperty titleProperty = new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
   private final SimpleStringProperty titleNodeProperty = new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
   private AnchorPane anchorPane = new AnchorPane();
   private GridPane node = new GridPane();
   SimpleStringProperty               toolTipText   = new SimpleStringProperty("Simple Search Panel");
   private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
                                                               Iconography.SIMPLE_SEARCH.getIconographic());
   final Manifold manifold;

   //~--- constructors --------------------------------------------------------

   public SimpleSearchExplorationNode(Manifold manifold) {
      this.manifold = manifold;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Optional<Node> getTitleNode() {
      Label titleLabel = new Label();
      titleLabel.graphicProperty().bind(iconProperty);
      titleLabel.textProperty().bind(titleNodeProperty);
      titleProperty.set("");
      return Optional.of(titleLabel);
   }
 
   @Override
   public Manifold getManifold() {
      return manifold;
   }

   @Override
   public Node getNode() {

      node.add(new Label("Simple Search:"), 0, 0);
      node.add(new TextField(), 1, 0);

      AnchorPane.setBottomAnchor(this.node, 10.0);
      AnchorPane.setTopAnchor(this.node, 10.0);
      AnchorPane.setLeftAnchor(this.node, 10.0);
      AnchorPane.setRightAnchor(this.node, 10.0);

      anchorPane.getChildren().add(node);


      return node;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return titleProperty;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipText;
   }
}

