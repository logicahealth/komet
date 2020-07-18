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

import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.concept.ConceptForControlWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec 
 */
public class ChoiceBoxControls {
   public static ChoiceBox<ConceptSpecification> getDescriptionTypeForDisplay(ManifoldCoordinate manifoldCoordinate) {
      return makeChoiceBox(manifoldCoordinate, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR, MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR);
   }

   public static ChoiceBox<ConceptSpecification> getTaxonomyPremiseTypes(ManifoldCoordinate manifoldCoordinate) {
      return makeChoiceBox(manifoldCoordinate, MetaData.INFERRED_PREMISE_TYPE____SOLOR, MetaData.STATED_PREMISE_TYPE____SOLOR);
   }
   
   
   public static ChoiceBox<ConceptSpecification> makeChoiceBox(ManifoldCoordinate manifoldCoordinate, ConceptSpecification... choices) {

      ObservableList<ConceptSpecification> choiceList = FXCollections.observableArrayList();
      for (ConceptSpecification choice: choices) {
         choiceList.add(new ConceptForControlWrapper(manifoldCoordinate, choice.getNid()));
      }
      ChoiceBox<ConceptSpecification> choiceBox = new ChoiceBox<>(choiceList);
      choiceBox.setValue(choiceList.get(0));
      return choiceBox;
   }
}
