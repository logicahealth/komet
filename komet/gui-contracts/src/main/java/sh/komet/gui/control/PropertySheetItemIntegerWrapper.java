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
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetItemIntegerWrapper implements PropertySheet.Item {

   private final String name;
   private final IntegerProperty integerProperty;
   private ConceptSpecification propertySpecification = null;

   public PropertySheetItemIntegerWrapper(String name, IntegerProperty integerProperty) {
       if (integerProperty == null) {
           throw new NullPointerException("Integer property cannot be null");
       }
      this.name = name;
      this.integerProperty = integerProperty;
   }

   public PropertySheetItemIntegerWrapper(Manifold manifold,
                                          IntegerProperty integerProperty) {
      this(manifold.getPreferredDescriptionText(new ConceptProxy(integerProperty.getName())),
              integerProperty);
   }
    public ConceptSpecification getSpecification() {
        if (this.propertySpecification != null) {
            return this.propertySpecification;
        }
        return new ConceptProxy(this.integerProperty.getName());
    }

    public void setSpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification = propertySpecification;
    }


   @Override
   public Class<?> getType() {
      return Integer.class;
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
      return "Enter the text for the version you wish to create";
   }

   @Override
   public Integer getValue() {
      return integerProperty.get();
   }

   @Override
   public void setValue(Object value) {
      integerProperty.setValue((Integer) value);
   }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(integerProperty);
   }
   
}
