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
package sh.komet.assemblage.view;

import java.io.IOException;
import java.util.Optional;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.ConceptLabel;
import sh.komet.gui.control.ConceptLabelToolbar;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import static sh.komet.gui.style.StyleClasses.ASSEMBLAGE_DETAIL;

/**
 *
 * @author kec
 */
public class AssemblageViewProvider implements ExplorationNode {

   private final BorderPane assemblageDetailPane = new BorderPane();
   private final Manifold manifold;
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("listing of assemblage members");
   private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty assemblage view");
   private ConceptLabel titleLabel = null;

   public AssemblageViewProvider(Manifold manifold) {
      try {
         this.manifold = manifold;
         this.assemblageDetailPane.getStyleClass().setAll(ASSEMBLAGE_DETAIL.toString());
         this.assemblageDetailPane.setTop(ConceptLabelToolbar.make(manifold));
         manifold.setGroupName(Manifold.UNLINKED_GROUP_NAME);

         FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/sh/komet/assemblage/view/AssemblageDetail.fxml"));
         loader.load();
         AssemblageDetailController assemblageDetailController = loader.getController();
         assemblageDetailController.setManifold(manifold);
         assemblageDetailController.getManifold().focusedConceptChronologyProperty().addListener(this::focusConceptChanged);
         assemblageDetailPane.setCenter(assemblageDetailController.getAssemblageDetailRootPane());
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }

   }
   
   private void focusConceptChanged(ObservableValue<? extends ConceptChronology> observable,
           ConceptChronology oldValue,
           ConceptChronology newValue) {
      if (titleProperty == null) {
         titleProperty.set(manifold.getPreferredDescriptionText(newValue));
      }
      toolTipProperty.set("View of all " + manifold.getPreferredDescriptionText(newValue) + " assemblage members");
   }
   
   @Override
   public Manifold getManifold() {
      return manifold;
   }

   @Override
   public Node getNode() {
      return assemblageDetailPane;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return titleProperty;
   }

   @Override
   public Optional<Node> getTitleNode() {
      if (titleLabel == null) {
         this.titleLabel = new ConceptLabel(manifold, ConceptLabel::setPreferredText);
         this.titleLabel.setGraphic(Iconography.PAPERCLIP.getIconographic());
         this.titleProperty.set("");
      }
      return Optional.of(titleLabel);
   }
}
