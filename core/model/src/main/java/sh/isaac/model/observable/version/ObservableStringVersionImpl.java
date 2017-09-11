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
package sh.isaac.model.observable.version;

import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.version.MutableStringVersion;
import sh.isaac.api.component.sememe.version.StringVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableStringVersion;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class ObservableStringVersionImpl 
        extends ObservableSememeVersionImpl
         implements ObservableStringVersion {

   /** The string property. */
   StringProperty stringProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable description impl.
    *
    * @param stampedVersion the stamped version
    * @param chronology the chronology
    */
   public ObservableStringVersionImpl(StringVersion stampedVersion,
                                    ObservableSememeChronology chronology) {
      super(stampedVersion, 
              chronology);
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      StringVersion newVersion = this.stampedVersion.makeAnalog(ec);
      ObservableStringVersionImpl newObservableVersion = 
              new ObservableStringVersionImpl(newVersion, (ObservableSememeChronology) chronology);
      ((ObservableChronologyImpl) chronology).getObservableVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
   }


   //~--- methods -------------------------------------------------------------

   /**
    * string property.
    *
    * @return the string property
    */
   @Override
   public StringProperty stringProperty() {
      if (this.stringProperty == null) {
         this.stringProperty = new CommitAwareStringProperty(this,
               ObservableFields.STRING_VALUE_FOR_SEMEME.toExternalString(),
               getString());
      }

      return this.stringProperty;
   }

   @Override
   public List<Property<?>> getProperties() {
      List<Property<?>> properties = super.getProperties();
      properties.add(stringProperty());
      return properties;
   }  

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the string.
    *
    * @return the string
    */
   @Override
   public String getString() {
      if (this.stringProperty != null) {
         return this.stringProperty.get();
      }

      return ((StringVersion) this.stampedVersion).getString();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the string.
    *
    * @param string the new string
    */
   @Override
   public void setString(String string) {
      if (this.stringProperty != null) {
         this.stringProperty.set(string);
      }

      ((MutableStringVersion) this.stampedVersion).setString(string);
   }

   @Override
   public String toString() {
      return "ObservableStringVersionImpl{" + getString() + '}';
   }
   
   
}

