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



package sh.isaac.model.observable.version.brittle;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.StringProperty;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.ObservableLoincVersion;
import sh.isaac.model.observable.version.ObservableSemanticVersionImpl;
import sh.isaac.model.semantic.version.brittle.LoincVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableLoincVersionImpl
        extends ObservableSemanticVersionImpl
         implements ObservableLoincVersion {
   StringProperty loincNumProperty;
   StringProperty componentProperty;
   StringProperty propertyProperty;
   StringProperty timeAspectProperty;
   StringProperty systemProperty;
   StringProperty scaleTypeProperty;
   StringProperty methodTypeProperty;
   StringProperty loincStatusProperty;
   StringProperty shortNameProperty;
   StringProperty longCommonNameProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableLoincVersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public StringProperty componentProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty loincNumProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty loincStatusProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty longCommonNameProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty methodTypeProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty propertyProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty scaleTypeProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty shortNameProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty systemProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public StringProperty timeAspectProperty() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getComponent() {
      if (this.componentProperty != null) {
         return this.componentProperty.get();
      }

      return getLoincVersion().getComponent();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setComponent(String value) {
      if (this.componentProperty != null) {
         this.componentProperty.set(value);
      }

      getLoincVersion().setComponent(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getLoincNum() {
      if (this.loincNumProperty != null) {
         return this.loincNumProperty.get();
      }

      return getLoincVersion().getLoincNum();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLoincNum(String value) {
      if (this.loincNumProperty != null) {
         this.loincNumProperty.set(value);
      }

      getLoincVersion().setLoincNum(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getLoincStatus() {
      if (this.loincStatusProperty != null) {
         return this.loincStatusProperty.get();
      }

      return getLoincVersion().getLoincStatus();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLoincStatus(String value) {
      if (this.loincStatusProperty != null) {
         this.loincStatusProperty.set(value);
      }

      getLoincVersion().setLoincStatus(value);
   }

   //~--- get methods ---------------------------------------------------------

   private LoincVersionImpl getLoincVersion() {
      return (LoincVersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public String getLongCommonName() {
      if (this.longCommonNameProperty != null) {
         return this.longCommonNameProperty.get();
      }

      return getLoincVersion().getLongCommonName();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLongCommonName(String value) {
      if (this.longCommonNameProperty != null) {
         this.longCommonNameProperty.set(value);
      }

      getLoincVersion().setLongCommonName(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getMethodType() {
      if (this.methodTypeProperty != null) {
         return this.methodTypeProperty.get();
      }

      return getLoincVersion().getMethodType();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setMethodType(String value) {
      if (this.methodTypeProperty != null) {
         this.methodTypeProperty.set(value);
      }

      getLoincVersion().setMethodType(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getProperty() {
      if (this.propertyProperty != null) {
         return this.propertyProperty.get();
      }

      return getLoincVersion().getProperty();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setProperty(String value) {
      if (this.propertyProperty != null) {
         this.propertyProperty.set(value);
      }

      getLoincVersion().setProperty(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getScaleType() {
      if (this.scaleTypeProperty != null) {
         return this.scaleTypeProperty.get();
      }

      return getLoincVersion().getScaleType();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setScaleType(String value) {
      if (this.scaleTypeProperty != null) {
         this.scaleTypeProperty.set(value);
      }

      getLoincVersion().setScaleType(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getShortName() {
      if (this.shortNameProperty != null) {
         return this.shortNameProperty.get();
      }

      return getLoincVersion().getShortName();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setShortName(String value) {
      if (this.shortNameProperty != null) {
         this.shortNameProperty.set(value);
      }

      getLoincVersion().setShortName(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getSystem() {
      if (this.systemProperty != null) {
         return this.systemProperty.get();
      }

      return getLoincVersion().getSystem();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setSystem(String value) {
      if (this.systemProperty != null) {
         this.systemProperty.set(value);
      }

      getLoincVersion().setSystem(value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getTimeAspect() {
      if (this.timeAspectProperty != null) {
         return this.timeAspectProperty.get();
      }

      return getLoincVersion().getTimeAspect();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTimeAspect(String value) {
      if (this.timeAspectProperty != null) {
         this.timeAspectProperty.set(value);
      }

      getLoincVersion().setTimeAspect(value);
   }
}

