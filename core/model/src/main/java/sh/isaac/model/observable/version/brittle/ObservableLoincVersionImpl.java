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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.ObservableLoincVersion;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.LoincVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableLoincVersionImpl
        extends ObservableAbstractSemanticVersionImpl
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

   public ObservableLoincVersionImpl(ObservableLoincVersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setLoincNum(versionToClone.getLoincNum());
      setComponent(versionToClone.getComponent());
      setProperty(versionToClone.getProperty());
      setTimeAspect(versionToClone.getTimeAspect());
      setSystem(versionToClone.getSystem());
      setScaleType(versionToClone.getScaleType());
      setMethodType(versionToClone.getMethodType());
      setLoincStatus(versionToClone.getLoincStatus());
      setShortName(versionToClone.getShortName());
      setLongCommonName(versionToClone.getLongCommonName());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableLoincVersionImpl analog = new ObservableLoincVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

   @Override
   public StringProperty componentProperty() {
      if (this.stampedVersionProperty == null  && this.componentProperty == null) {
          componentProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_COMPONENT.toExternalString(), "");
      }
      if (componentProperty == null) {
          componentProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_COMPONENT.toExternalString(), getComponent());
      }
      return componentProperty;
   }

   @Override
   public StringProperty loincNumProperty() {
      if (this.stampedVersionProperty == null  && this.loincNumProperty == null) {
          loincNumProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_NUMBER.toExternalString(), "");
      }
      if (loincNumProperty == null) {
          loincNumProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_NUMBER.toExternalString(), getComponent());
      }
      return loincNumProperty;
   }

   @Override
   public StringProperty loincStatusProperty() {
      if (this.stampedVersionProperty == null  && this.propertyProperty == null) {
          propertyProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_PROPERTY.toExternalString(), "");
      }
      if (propertyProperty == null) {
          propertyProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_PROPERTY.toExternalString(), getComponent());
      }
      return propertyProperty;
   }

   @Override
   public StringProperty longCommonNameProperty() {
      if (this.stampedVersionProperty == null  && this.longCommonNameProperty == null) {
          longCommonNameProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_LONG_COMMON_NAME.toExternalString(), "");
      }
      if (longCommonNameProperty == null) {
          longCommonNameProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_LONG_COMMON_NAME.toExternalString(), getComponent());
      }
      return longCommonNameProperty;
   }

   @Override
   public StringProperty methodTypeProperty() {
      if (this.stampedVersionProperty == null  && this.methodTypeProperty == null) {
          methodTypeProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_METHOD_TYPE.toExternalString(), "");
      }
      if (methodTypeProperty == null) {
          methodTypeProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_METHOD_TYPE.toExternalString(), getComponent());
      }
      return methodTypeProperty;
   }

   @Override
   public StringProperty propertyProperty() {
      if (this.stampedVersionProperty == null  && this.propertyProperty == null) {
          propertyProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_PROPERTY.toExternalString(), "");
      }
      if (propertyProperty == null) {
          propertyProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_PROPERTY.toExternalString(), getComponent());
      }
      return propertyProperty;
   }

   @Override
   public StringProperty scaleTypeProperty() {
      if (this.stampedVersionProperty == null  && this.scaleTypeProperty == null) {
          scaleTypeProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_SCALE_TYPE.toExternalString(), "");
      }
      if (scaleTypeProperty == null) {
          scaleTypeProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_SCALE_TYPE.toExternalString(), getComponent());
      }
      return scaleTypeProperty;
   }

   @Override
   public StringProperty shortNameProperty() {
      if (this.stampedVersionProperty == null  && this.shortNameProperty == null) {
          shortNameProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_SHORT_NAME.toExternalString(), "");
      }
      if (shortNameProperty == null) {
          shortNameProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_SHORT_NAME.toExternalString(), getComponent());
      }
      return shortNameProperty;
   }

   @Override
   public StringProperty systemProperty() {
      if (this.stampedVersionProperty == null  && this.systemProperty == null) {
          systemProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_SYSTEM.toExternalString(), "");
      }
     if (systemProperty == null) {
          systemProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_SYSTEM.toExternalString(), getComponent());
      }
      return systemProperty;
    }

   @Override
   public StringProperty timeAspectProperty() {
      if (this.stampedVersionProperty == null  && this.timeAspectProperty == null) {
          timeAspectProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_TIME_ASPECT.toExternalString(), "");
      }
     if (timeAspectProperty == null) {
          timeAspectProperty = new SimpleStringProperty(this, 
                  ObservableFields.LOINC_TIME_ASPECT.toExternalString(), getComponent());
      }
      return timeAspectProperty;
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
   public final void setComponent(String value) {
       if (this.stampedVersionProperty == null) {
           this.componentProperty();
       }
      if (this.componentProperty != null) {
         this.componentProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setComponent(value);
      }
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
   public final void setLoincNum(String value) {
       if (this.stampedVersionProperty == null) {
           this.loincNumProperty();
       }
      if (this.loincNumProperty != null) {
         this.loincNumProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setLoincNum(value);
      }
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
   public final void setLoincStatus(String value) {
       if (this.stampedVersionProperty == null) {
           this.loincStatusProperty();
       }
      if (this.loincStatusProperty != null) {
         this.loincStatusProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setLoincStatus(value);
      }
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
   public final void setLongCommonName(String value) {
       if (this.stampedVersionProperty == null) {
           this.longCommonNameProperty();
       }
      if (this.longCommonNameProperty != null) {
         this.longCommonNameProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setLongCommonName(value);
      }
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
   public final void setMethodType(String value) {
       if (this.stampedVersionProperty == null) {
           this.methodTypeProperty();
       }
      if (this.methodTypeProperty != null) {
         this.methodTypeProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setMethodType(value);
      }
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
   public final void setProperty(String value) {
       if (this.stampedVersionProperty == null) {
           this.propertyProperty();
       }
      if (this.propertyProperty != null) {
         this.propertyProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setProperty(value);
      }
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
   public final void setScaleType(String value) {
       if (this.stampedVersionProperty == null) {
           this.scaleTypeProperty();
       }
      if (this.scaleTypeProperty != null) {
         this.scaleTypeProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setScaleType(value);
      }
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
   public final void setShortName(String value) {
       if (this.stampedVersionProperty == null) {
           this.shortNameProperty();
       }
      if (this.shortNameProperty != null) {
         this.shortNameProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setShortName(value);
      }
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
   public final void setSystem(String value) {
       if (this.stampedVersionProperty == null) {
           this.systemProperty();
       }
      if (this.systemProperty != null) {
         this.systemProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setSystem(value);
      }
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
   public final void setTimeAspect(String value) {
       if (this.stampedVersionProperty == null) {
           this.timeAspectProperty();
       }
      if (this.timeAspectProperty != null) {
         this.timeAspectProperty.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getLoincVersion().setTimeAspect(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(componentProperty());
      properties.add(loincNumProperty());
      properties.add(loincStatusProperty());
      properties.add(longCommonNameProperty());
      properties.add(methodTypeProperty());
      properties.add(propertyProperty());
      properties.add(scaleTypeProperty());
      properties.add(shortNameProperty());
      properties.add(systemProperty());
      properties.add(timeAspectProperty());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(componentProperty());
      properties.add(loincNumProperty());
      properties.add(loincStatusProperty());
      properties.add(longCommonNameProperty());
      properties.add(methodTypeProperty());
      properties.add(propertyProperty());
      properties.add(scaleTypeProperty());
      properties.add(shortNameProperty());
      properties.add(systemProperty());
      properties.add(timeAspectProperty());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableLoincVersionImpl) {
            ObservableLoincVersionImpl observableAnalog = (ObservableLoincVersionImpl) analog;
            observableAnalog.setComponent(this.getComponent());
            observableAnalog.setLoincNum(this.getLoincNum());
            observableAnalog.setLoincStatus(this.getLoincStatus());
            observableAnalog.setLongCommonName(this.getLongCommonName());
            observableAnalog.setMethodType(this.getMethodType());
            observableAnalog.setProperty(this.getProperty());
            observableAnalog.setScaleType(this.getScaleType());
            observableAnalog.setShortName(this.getShortName());
            observableAnalog.setSystem(this.getSystem());
            observableAnalog.setTimeAspect(this.getTimeAspect());
        } else if (analog instanceof LoincVersionImpl) {
            LoincVersionImpl simpleAnalog = (LoincVersionImpl) analog;
            simpleAnalog.setComponent(this.getComponent());
            simpleAnalog.setLoincNum(this.getLoincNum());
            simpleAnalog.setLoincStatus(this.getLoincStatus());
            simpleAnalog.setLongCommonName(this.getLongCommonName());
            simpleAnalog.setMethodType(this.getMethodType());
            simpleAnalog.setProperty(this.getProperty());
            simpleAnalog.setScaleType(this.getScaleType());
            simpleAnalog.setShortName(this.getShortName());
            simpleAnalog.setSystem(this.getSystem());
            simpleAnalog.setTimeAspect(this.getTimeAspect());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        LoincVersionImpl newVersion = new LoincVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.componentProperty != null && 
              !this.componentProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getComponent())) {
         this.componentProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getComponent());
      }
      if (this.loincNumProperty != null && 
              !this.loincNumProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getLoincNum())) {
         this.loincNumProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getLoincNum());
      }
      if (this.loincStatusProperty != null && 
              !this.loincStatusProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getLoincStatus())) {
         this.loincStatusProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getLoincStatus());
      }
      if (this.longCommonNameProperty != null && 
              !this.longCommonNameProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getLongCommonName())) {
         this.longCommonNameProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getLongCommonName());
      }
      if (this.methodTypeProperty != null && 
              !this.methodTypeProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getMethodType())) {
         this.methodTypeProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getMethodType());
      }
      if (this.propertyProperty != null && 
              !this.propertyProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getProperty())) {
         this.propertyProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getProperty());
      }
      if (this.scaleTypeProperty != null && 
              !this.scaleTypeProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getScaleType())) {
         this.scaleTypeProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getScaleType());
      }
      if (this.shortNameProperty != null && 
              !this.shortNameProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getShortName())) {
         this.shortNameProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getShortName());
      }
      if (this.systemProperty != null && 
              !this.systemProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getSystem())) {
         this.systemProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getSystem());
      }
      if (this.timeAspectProperty != null && 
              !this.timeAspectProperty.get().equals(((LoincVersionImpl) this.stampedVersionProperty.get()).getTimeAspect())) {
         this.timeAspectProperty.set(((LoincVersionImpl) this.stampedVersionProperty.get()).getTimeAspect());
      }
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
      LoincVersionImpl newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableLoincVersionImpl newObservableVersion = 
              new ObservableLoincVersionImpl(newVersion, (ObservableSemanticChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}

