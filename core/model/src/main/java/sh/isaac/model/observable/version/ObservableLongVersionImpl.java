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



package sh.isaac.model.observable.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.MutableLongVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.model.observable.CommitAwareLongProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.model.observable.CommitAwareObjectProperty;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableLongVersionImpl
        extends ObservableSemanticVersionImpl
         implements ObservableLongVersion {
   /** The long property. */
   LongProperty longProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableLongVersionImpl(LongVersion version, ObservableSemanticChronology chronology) {
      super(version, chronology);
   }

   public ObservableLongVersionImpl(ObservableLongVersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setLongValue(versionToClone.getLongValue());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableLongVersionImpl analog = new ObservableLongVersionImpl(this, getChronology());
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public LongProperty longValueProperty() {
      if (this.stampedVersionProperty == null && this.longProperty == null) {
         this.longProperty = new CommitAwareLongProperty(
             this,
             ObservableFields.LONG_VALUE_FOR_SEMANTIC.toExternalString(),
                 0);
      }
      if (this.longProperty == null) {
         this.longProperty = new CommitAwareLongProperty(
             this,
             ObservableFields.LONG_VALUE_FOR_SEMANTIC.toExternalString(),
             getLongValue());
         this.longProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((LongVersionImpl) this.stampedVersionProperty.get()).setLongValue(newValue.longValue());
             });
      }

      return this.longProperty;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      LongVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableLongVersionImpl newObservableVersion = new ObservableLongVersionImpl(
                                                           newVersion,
                                                                 (ObservableSemanticChronology) chronology);

      ((ObservableChronologyImpl) chronology).getVersionList()
            .add(newObservableVersion);
      return (V) newObservableVersion;
   }

   @Override
   public String toString() {
      return "ObservableLongVersionImpl{value:" + getLongValue() + '}';
   }

   @Override
   protected void updateVersion() {
      super.updateVersion();
      if (this.longProperty != null && this.longProperty.get() != ((LongVersion) this.stampedVersionProperty.get()).getLongValue()) {
         this.longProperty.set(((LongVersion) this.stampedVersionProperty.get()).getLongValue());
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the long value.
    *
    * @return the case significance concept sequence
    */
   @Override
   public long getLongValue() {
      if (this.longProperty != null) {
         return this.longProperty.get();
      }

      return ((LongVersion) this.stampedVersionProperty.get()).getLongValue();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the long value.
    *
    * @param longValue the new long value
    */
   @Override
   public final void setLongValue(long longValue) {
       if (this.stampedVersionProperty == null) {
           this.longValueProperty();
       }
      if (this.longProperty != null) {
         this.longProperty.set(longValue);
      }

      if (this.stampedVersionProperty != null) {
         ((MutableLongVersion) this.stampedVersionProperty.get()).setLongValue(longValue);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(longValueProperty());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(longValueProperty());
      return properties;
    }
}

