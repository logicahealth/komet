/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.control.concept;

import sh.komet.gui.control.concept.ConceptForControlWrapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class PropertySheetItemConceptWrapper implements ConceptSpecification, PropertySheet.Item {

   private final Manifold manifoldForDisplay;
   private final String name;
   private final SimpleObjectProperty<ConceptForControlWrapper> observableWrapper;
   private final SimpleObjectProperty<ConceptSpecification> conceptProperty;
   private final NidSet allowedValues = new NidSet();
   
   public PropertySheetItemConceptWrapper(Manifold manifoldForDisplay,
           ObjectProperty<? extends ConceptSpecification> conceptProperty, int... allowedValues) {
      this(manifoldForDisplay, manifoldForDisplay.getPreferredDescriptionText(new ConceptProxy(conceptProperty.getName())), conceptProperty, allowedValues);
   }


   public PropertySheetItemConceptWrapper(Manifold manifoldForDisplay, String name,
           ObjectProperty<? extends ConceptSpecification> conceptProperty, int... allowedValues) {
      this.manifoldForDisplay = manifoldForDisplay;
      this.name = name;
      this.conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptProperty;
      if (allowedValues.length > 0) {
          this.conceptProperty.set(Get.concept(allowedValues[0]));
      }
      this.allowedValues.addAll(allowedValues);
      this.observableWrapper = new SimpleObjectProperty<>(new ConceptForControlWrapper(manifoldForDisplay, conceptProperty.get().getNid()));
   }

   @Override
   public String getFullyQualifiedName() {
      return this.manifoldForDisplay.getFullySpecifiedDescriptionText(conceptProperty.get());
   }

   @Override
   public Optional<String> getRegularName() {
      return Optional.of(manifoldForDisplay.getPreferredDescriptionText(conceptProperty.get()));
   }

   @Override
   public List<UUID> getUuidList() {
      return new ConceptProxy(conceptProperty.getName()).getUuidList();
   }

   @Override
   public Class<?> getType() {
      return ConceptForControlWrapper.class;
   }

   @Override
   public String getCategory() {
      return null;
   }

   public NidSet getAllowedValues() {
      return allowedValues;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getDescription() {
      return "Select the proper concept value for the version you wish to create. ";
   }

   @Override
   public ConceptForControlWrapper getValue() {
      return this.observableWrapper.get();
   }

   @Override
   public void setValue(Object value) {
      try {
         // Concept sequence property may throw a runtime exception if it cannot be changed
         this.conceptProperty.setValue(((ConceptForControlWrapper) value));
         // only change the observableWrapper if no exception is thrown. 
         this.observableWrapper.setValue((ConceptForControlWrapper) value);
      } catch (RuntimeException ex) {
         FxGet.statusMessageService().reportStatus(ex.getMessage());
         this.observableWrapper.setValue(new ConceptForControlWrapper(manifoldForDisplay, this.conceptProperty.get().getNid()));
      }
   }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(this.conceptProperty);
   }
  
   public ConceptSpecification getPropertySpecification() {
      return new ConceptProxy(this.conceptProperty.getName());
   }

   @Override
   public String toString() {
      return "Property sheet item for "
              + manifoldForDisplay.getPreferredDescriptionText(new ConceptProxy(conceptProperty.getName()));
   }
}
