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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
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
   private final SimpleObjectProperty<ConceptSpecificationForControlWrapper> observableWrapper;
   private final SimpleObjectProperty<ConceptSpecification> conceptProperty;
   private final List<ConceptSpecification> allowedValues = new ArrayList();
   private boolean allowSearch = true;
   private boolean allowHistory = true;
   
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
      for (int allowedNid: allowedValues) {
          this.allowedValues.add(Get.conceptSpecification(allowedNid));
      }
      this.observableWrapper = new SimpleObjectProperty<>(new ConceptSpecificationForControlWrapper(conceptProperty.get(), manifoldForDisplay));
   }
   
      public PropertySheetItemConceptWrapper(Manifold manifoldForDisplay, String name,
           ObjectProperty<? extends ConceptSpecification> conceptProperty) {
          this(manifoldForDisplay, name, conceptProperty, (ConceptSpecification[]) new ConceptSpecification[0]);
      }

   public PropertySheetItemConceptWrapper(Manifold manifoldForDisplay, String name,
           ObjectProperty<? extends ConceptSpecification> conceptProperty, ConceptSpecification... allowedValues) {
      this.manifoldForDisplay = manifoldForDisplay;
      this.name = name;
      this.conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptProperty;
      if (allowedValues.length > 0) {
          this.conceptProperty.set(Get.concept(allowedValues[0]));
      }
      this.allowedValues.addAll(Arrays.asList(allowedValues));
      this.observableWrapper = new SimpleObjectProperty<>(new ConceptSpecificationForControlWrapper(conceptProperty.get(), manifoldForDisplay));
   }

    public boolean allowSearch() {
        return allowSearch;
    }

    public void setAllowSearch(boolean allowSearch) {
        this.allowSearch = allowSearch;
    }

    public boolean allowHistory() {
        return allowHistory;
    }

    public void setAllowHistory(boolean allowHistory) {
        this.allowHistory = allowHistory;
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
      return ConceptSpecificationForControlWrapper.class;
   }

   @Override
   public String getCategory() {
      return null;
   }

   public List<ConceptSpecification> getAllowedValues() {
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
   public ConceptSpecificationForControlWrapper getValue() {
      return this.observableWrapper.get();
   }

   @Override
   public void setValue(Object value) {
      try {
         // Concept sequence property may throw a runtime exception if it cannot be changed
         this.conceptProperty.setValue(((ConceptSpecificationForControlWrapper) value));
         // only change the observableWrapper if no exception is thrown. 
         this.observableWrapper.setValue((ConceptSpecificationForControlWrapper) value);
      } catch (RuntimeException ex) {
         FxGet.statusMessageService().reportStatus(ex.getMessage());
         this.observableWrapper.setValue(new ConceptSpecificationForControlWrapper(this.conceptProperty.get(), manifoldForDisplay));
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
