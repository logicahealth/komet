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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableComponentNidVersion;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.sememe.version.ComponentNidVersionImpl;

/**
 *
 * @author kec
 */
public class ObservableComponentNidVersionImpl 
        extends ObservableSememeVersionImpl 
        implements ObservableComponentNidVersion {
   /** The component nid property. */
   IntegerProperty componentNidProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableComponentNidVersionImpl(ComponentNidVersion version,
                                    ObservableSememeChronology chronology) {
      super(version, 
              chronology);
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      ComponentNidVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableComponentNidVersionImpl newObservableVersion = 
              new ObservableComponentNidVersionImpl(newVersion, (ObservableSememeChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty componentNidProperty() {
      if (this.componentNidProperty == null) {
         this.componentNidProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.COMPONENT_NID_FOR_SEMEME.toExternalString(),
               getComponentNid());
         this.componentNidProperty.addListener((observable, oldValue, newValue) -> {
            ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).setComponentNid(newValue.intValue());
         });
      }

      return this.componentNidProperty;
   }


   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the component nid.
    *
    * @return the case significance concept sequence
    */
   @Override
   public int getComponentNid() {
      super.updateVersion();
      if (this.componentNidProperty != null) {
         return this.componentNidProperty.get();
      }

      return ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).getComponentNid();
   }

   @Override
   protected void updateVersion() {
      if (this.componentNidProperty != null && this.componentNidProperty.get() != ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).getComponentNid()) {
         this.componentNidProperty.set(((ComponentNidVersionImpl) this.stampedVersionProperty.get()).getComponentNid());
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param componentNid the new case significance concept sequence
    */
   @Override
   public void setComponentNid(int componentNid) {
      if (this.componentNidProperty != null) {
         this.componentNidProperty.set(componentNid);
      }
      ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).setComponentNid(componentNid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String toString() {
      return "ObservableComponentNidVersionImpl{component:" + Get.conceptDescriptionText(getComponentNid()) + '}';
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();
      properties.add(componentNidProperty());
      return properties;
   }  
}
   
