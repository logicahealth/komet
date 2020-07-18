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

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.coordinate.StampPrecedence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author kec
 */
public class PropertySheetStampPrecedenceWrapper implements PropertySheet.Item {

   private final String name;
   private final ObjectProperty<StampPrecedence> precedenceProperty;

   public PropertySheetStampPrecedenceWrapper(String name, ObjectProperty<StampPrecedence> precedenceProperty) {
      this.name = name;
      this.precedenceProperty = precedenceProperty;
   }

   @Override
   public Class<?> getType() {
      return StampPrecedence.class;
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
      return "Select the status for the version you wish to create";
   }

   @Override
   public StampPrecedence getValue() {
      return precedenceProperty.get();
   }

   @Override
   public void setValue(Object value) {
      precedenceProperty.setValue((StampPrecedence) value);
   }
    public List<StampPrecedence> getAllowedValues() {
       StampPrecedence[] allowedValues = {
           StampPrecedence.PATH,
           StampPrecedence.TIME
       }; 
       return Arrays.asList(allowedValues);
    }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(precedenceProperty);
   }
   
}
