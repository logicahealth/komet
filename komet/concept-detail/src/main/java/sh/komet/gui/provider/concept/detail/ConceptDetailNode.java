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



package sh.komet.gui.provider.concept.detail;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import sh.isaac.api.component.concept.ConceptChronology;

import sh.komet.gui.control.ConceptLabelToolbar;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ConceptDetailNode
         implements DetailNode {
   private final BorderPane           conceptDetailPane = new BorderPane();
   private final SimpleStringProperty titleProperty     = new SimpleStringProperty("detail graph");
   private final SimpleStringProperty toolTipProperty   = new SimpleStringProperty("detail graph");
   private final Manifold             conceptDetailManifold;

   //~--- constructors --------------------------------------------------------

   public ConceptDetailNode(Manifold conceptDetailManifold, Consumer<Node> nodeConsumer) {
      try {
         this.conceptDetailManifold = conceptDetailManifold;
         conceptDetailManifold.focusedConceptChronologyProperty()
                 .addListener(
                         (ObservableValue<? extends ConceptChronology> observable,
                                 ConceptChronology oldValue,
                                 ConceptChronology newValue) -> {
                            titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
                            toolTipProperty.set(
                                    "concept details for: " +
                                            this.conceptDetailManifold.getFullySpecifiedDescriptionText(
                                                    newValue));
                         });
         conceptDetailPane.setTop(ConceptLabelToolbar.make(conceptDetailManifold));
         nodeConsumer.accept(conceptDetailPane);
         
         FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/sh/komet/gui/provider/concept/detail/ConceptDetail.fxml"));
         
         loader.load();
         
         ConceptDetailController conceptDetailController = loader.getController();
         
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
   public ReadOnlyProperty<String> getToolTip() {
      return this.toolTipProperty;
   }
}

