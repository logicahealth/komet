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
package sh.isaac.provider.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.control.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class AddEditVersionMenuItems {
   final List<MenuItem> menuItems = new ArrayList<>();
   final Manifold manifold;
   final ObservableCategorizedVersion categorizedVersion;
   final Consumer<PropertySheet> propertySheetConsumer;
   
   Map<ConceptSpecification, Property<?>> propertyMap;

   public AddEditVersionMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion, Consumer<PropertySheet> propertySheetConsumer) {
      this.manifold = manifold;
      this.categorizedVersion = categorizedVersion;
      this.propertySheetConsumer = propertySheetConsumer;
   }

   public Manifold getManifold() {
      return manifold;
   }

   public List<MenuItem> getMenuItems() {
      return menuItems;
   }
   
   public List<Property<?>> getProperties() {
      return categorizedVersion.getProperties();
   }

   public VersionType getVersionType() {
      return categorizedVersion.getVersionType();
   }
   
   public Map<ConceptSpecification, Property<?>> getPropertyMap() {
      if (propertyMap == null) {
         propertyMap = categorizedVersion.getPropertyMap();
      }
      return propertyMap;
   }
   
   public PropertySheetItemConceptWrapper getConceptProperty(ConceptSpecification propertyConceptSpecification, String nameForProperty) {
      return new PropertySheetItemConceptWrapper(
              manifold, nameForProperty, (IntegerProperty) getPropertyMap().get(propertyConceptSpecification));
   }
   
   public PropertySheetMenuItem makePropertySheetMenuItem(String menuText) {
      PropertySheetMenuItem propertySheetMenuItem = new PropertySheetMenuItem(manifold);
      MenuItem menuItem = new MenuItem(menuText);
      menuItem.setOnAction((event) -> {
         propertySheetConsumer.accept(propertySheetMenuItem.propertySheet);
      });
      menuItems.add(menuItem);
      return propertySheetMenuItem;
   }
}
