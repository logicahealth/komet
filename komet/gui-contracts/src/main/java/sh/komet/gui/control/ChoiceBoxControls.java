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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec 
 */
public class ChoiceBoxControls {
   public static ChoiceBox<ConceptSpecification> getDescriptionTypeForDisplay(Manifold manifold) {      
      return makeChoiceBox(manifold, MetaData.REGULAR_NAME____SOLOR, MetaData.FULLY_QUALIFIED_NAME____SOLOR);
   }

   public static ChoiceBox<ConceptSpecification> getTaxonomyPremiseTypes(Manifold manifold) {
      return makeChoiceBox(manifold, MetaData.INFERRED_PREMISE_TYPE____SOLOR, MetaData.STATED_PREMISE_TYPE____SOLOR);
   }
   
   
   public static ChoiceBox<ConceptSpecification> makeChoiceBox(Manifold manifold, ConceptSpecification... choices) {

      ObservableList<ConceptSpecification> choiceList = FXCollections.observableArrayList();
      for (ConceptSpecification choice: choices) {
         choiceList.add(new ConceptForControlWrapper(manifold, choice.getNid()));
      }
      ChoiceBox<ConceptSpecification> choiceBox = new ChoiceBox<>(choiceList);
      choiceBox.setValue(choiceList.get(0));
      return choiceBox;
   }
}
