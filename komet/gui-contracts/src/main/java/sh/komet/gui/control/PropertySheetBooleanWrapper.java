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
package sh.komet.gui.control;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class PropertySheetBooleanWrapper implements PropertySheet.Item {

   private final String name;
   private final BooleanProperty booleanProperty;

   public PropertySheetBooleanWrapper(String name, BooleanProperty booleanProperty) {
      this.name = name;
      this.booleanProperty = booleanProperty;
   }

   public PropertySheetBooleanWrapper(ManifoldCoordinate manifoldCoordinate,
                                      BooleanProperty booleanProperty) {
      this(manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(booleanProperty.getName())),
              booleanProperty);
   }

   @Override
   public Class<?> getType() {
      return Boolean.class;
   }

   @Override
   public String getCategory() {
      return null;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getDescription() {
      return "Enter the boolean value";
   }

   @Override
   public Boolean getValue() {
      return booleanProperty.get();
   }

   @Override
   public void setValue(Object value) {
      booleanProperty.setValue((Boolean) value);
   }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(booleanProperty);
   }
   
}
