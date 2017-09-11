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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class PropertySheetMenuItem {
   PropertySheet propertySheet = new PropertySheet();
   
   List<PropertySpec> propertiesToEdit = new ArrayList<>();
      
   Map<ConceptSpecification, Property<?>> propertyMap;
   ObservableVersion observableVersion;
   boolean executeOnClone;
   Manifold manifold;
   public PropertySheetMenuItem(Manifold manifold, ObservableCategorizedVersion categorizedVersion, 
           boolean executeOnClone) {
      this.manifold = manifold;
      this.observableVersion = categorizedVersion;
      this.executeOnClone = executeOnClone;
      this.propertySheet.setPropertyEditorFactory(new IsaacPropertyEditorFactory(manifold));
      this.propertySheet.setMode(PropertySheet.Mode.NAME);
      this.propertySheet.setSearchBoxVisible(false);
      this.propertySheet.setModeSwitcherVisible(false);
   }
   
   public void addPropertyToEdit(String nameOnPropertySheet, ConceptSpecification propertySpecification,
           PropertyEditorType propertyEditorType) {
      this.propertiesToEdit.add(new PropertySpec(nameOnPropertySheet, propertySpecification, propertyEditorType));
   }  
   
   public void prepareToExecute() {
      if (executeOnClone) {
         this.observableVersion = this.observableVersion.makeAnalog(manifold.getEditCoordinate());
      }
      FxGet.rulesDrivenKometService().populatePropertySheetEditors(this);
   }
   
   
   public List<Item> getPropertySheetItems() {
      List<Item> items = new ArrayList<>();
      propertiesToEdit.forEach((propertySpec) -> {
         switch (propertySpec.propertyEditorType) {
            case CONCEPT:
               
               items.add(addItem(getConceptProperty(propertySpec.propertyConceptSpecification,
                       propertySpec.nameOnPropertySheet)));
               break;
               default:
                  throw new RuntimeException("Can't handle: " + propertySpec);
         }
      });
      return items;
   }
   
   private Item addItem(Item item) {
      propertySheet.getItems().add(item);
      return item;
   }
   
   public PropertySheet getPropertySheet() {
      return propertySheet;
   }
      
   public PropertySheetItemConceptWrapper getConceptProperty(ConceptSpecification propertyConceptSpecification, String nameForProperty) {
      return new PropertySheetItemConceptWrapper(
              manifold, nameForProperty, (IntegerProperty) getPropertyMap().get(propertyConceptSpecification));
   }
   
   
   public Map<ConceptSpecification, Property<?>> getPropertyMap() {
      if (propertyMap == null) {
         propertyMap = observableVersion.getPropertyMap();
      }
      return propertyMap;
   }

   private static class PropertySpec {
      final String nameOnPropertySheet;
      final ConceptSpecification propertyConceptSpecification;
      final PropertyEditorType propertyEditorType;

      public PropertySpec(String propertySheetName, ConceptSpecification propertyConceptSpecification, PropertyEditorType propertyEditorType) {
         this.nameOnPropertySheet = propertySheetName;
         this.propertyConceptSpecification = propertyConceptSpecification;
         this.propertyEditorType = propertyEditorType;
      }

      @Override
      public int hashCode() {
         int hash = 5;
         hash = 79 * hash + Objects.hashCode(this.nameOnPropertySheet);
         hash = 79 * hash + Objects.hashCode(this.propertyEditorType);
         return hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }
         final PropertySpec other = (PropertySpec) obj;
         if (!Objects.equals(this.nameOnPropertySheet, other.nameOnPropertySheet)) {
            return false;
         }
         return this.propertyEditorType == other.propertyEditorType;
      }

      @Override
      public String toString() {
         return "PropertySpec{" + "propertySheetName=" + nameOnPropertySheet + ", propertyConceptSpecification=" + propertyConceptSpecification + ", propertyEditorType=" + propertyEditorType + '}';
      }
      
      
   }
   
}
