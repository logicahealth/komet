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
package sh.komet.gui.provider.concept.detail.treetable;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.State;

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.ConceptLabel;

import sh.komet.gui.control.ConceptLabelToolbar;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptDetailTreeTableNode
        implements DetailNode {

   private final BorderPane conceptDetailPane = new BorderPane();
   private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty");
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("empty");
   private final Manifold conceptDetailManifold;
   private ConceptLabel titleLabel = null;

   //~--- constructors --------------------------------------------------------
   public ConceptDetailTreeTableNode(Manifold conceptDetailManifold, Consumer<Node> nodeConsumer) {
      try {
         this.conceptDetailManifold = conceptDetailManifold;
         this.conceptDetailManifold.getStampCoordinate().allowedStatesProperty().add(State.INACTIVE);
         conceptDetailManifold.focusedConceptProperty()
                 .addListener(
                         (ObservableValue<? extends ConceptSpecification> observable,
                                 ConceptSpecification oldValue,
                                 ConceptSpecification newValue) -> {
                            if (titleLabel == null) {
                               if (newValue == null) {
                                  titleProperty.set("empty");
                                  toolTipProperty.set(
                                          "concept details for: empty");
                               } else {
                                  titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
                                  toolTipProperty.set(
                                          "concept details for: "
                                          + this.conceptDetailManifold.getFullySpecifiedDescriptionText(
                                                  newValue));
                               }
                            }

                         });
         conceptDetailPane.setTop(ConceptLabelToolbar.make(conceptDetailManifold));
         conceptDetailPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
         if (nodeConsumer != null) {
            nodeConsumer.accept(conceptDetailPane);
         }

         FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/sh/komet/gui/provider/concept/detail/ConceptDetail.fxml"));

         loader.load();

         ConceptDetailTreeTableController conceptDetailController = loader.getController();

         conceptDetailController.setManifold(conceptDetailManifold);
         conceptDetailPane.setCenter(conceptDetailController.getConceptDetailRootPane());
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public ReadOnlyProperty<String> getTitle() {
      return this.titleProperty;
   }

   @Override
   public Optional<Node> getTitleNode() {
      if (titleLabel == null) {
         this.titleLabel = new ConceptLabel(conceptDetailManifold, ConceptLabel::setPreferredText);
         this.titleLabel.setGraphic(Iconography.CONCEPT_TABLE.getIconographic());
         this.titleProperty.set("");
      }
      return Optional.of(titleLabel);
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return this.toolTipProperty;
   }
}
