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
import javafx.beans.property.StringProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Str1_Str2_Version;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_VersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Observable_Str1_Str2_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Str1_Str2_Version {
   StringProperty str1Property;
   StringProperty str2Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Str1_Str2_VersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   private Observable_Str1_Str2_VersionImpl(Observable_Str1_Str2_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setStr1(versionToClone.getStr1());
      setStr2(versionToClone.getStr2());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        Observable_Str1_Str2_VersionImpl analog = new Observable_Str1_Str2_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }
   //~--- methods -------------------------------------------------------------

   @Override
   public StringProperty str1Property() {
      if (this.stampedVersionProperty == null  && this.str1Property == null) {
        this.str1Property = new CommitAwareStringProperty(this, ObservableFields.STR1.toExternalString(),
        "");
      }
      if (this.str1Property == null) {
         this.str1Property = new CommitAwareStringProperty(this, ObservableFields.STR1.toExternalString(), getStr1());
         this.str1Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Version().setStr1(newValue);
             });
      }

      return this.str1Property;
   }

   @Override
   public StringProperty str2Property() {
      if (this.stampedVersionProperty == null  && this.str2Property == null) {
        this.str2Property = new CommitAwareStringProperty(this, ObservableFields.STR2.toExternalString(),
        "");
      }
      if (this.str2Property == null) {
         this.str2Property = new CommitAwareStringProperty(this, ObservableFields.STR2.toExternalString(), getStr2());
         this.str2Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Version().setStr2(newValue);
             });
      }

      return this.str2Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr1() {
      if (this.str1Property != null) {
         return this.str1Property.get();
      }

      return getStr1_Str2_Version().getStr1();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr1(String value) {
       if (this.stampedVersionProperty == null) {
           this.str1Property();
       }
      if (this.str1Property != null) {
         this.str1Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Str2_Version().setStr1(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Str1_Str2_VersionImpl getStr1_Str2_Version() {
      return (Str1_Str2_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public String getStr2() {
      if (this.str2Property != null) {
         return this.str2Property.get();
      }

      return getStr1_Str2_Version().getStr2();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr2(String value) {
       if (this.stampedVersionProperty == null) {
           this.str2Property();
       }
      if (this.str2Property != null) {
         this.str2Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Str2_Version().setStr2(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(str1Property());
      properties.add(str2Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(str1Property());
      properties.add(str2Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Str1_Str2_VersionImpl) {
            Observable_Str1_Str2_VersionImpl observableAnalog = (Observable_Str1_Str2_VersionImpl) analog;
            observableAnalog.setStr1(this.getStr1());
            observableAnalog.setStr2(this.getStr2());
        } else if (analog instanceof Str1_Str2_VersionImpl) {
             Str1_Str2_VersionImpl simpleAnalog = (Str1_Str2_VersionImpl) analog;
            simpleAnalog.setStr1(this.getStr1());
            simpleAnalog.setStr2(this.getStr2());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Str1_Str2_VersionImpl newVersion = new Str1_Str2_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.str1Property != null && !this.str1Property.get().equals(((Str1_Str2_VersionImpl) this.stampedVersionProperty.get()).getStr1())) {
         this.str1Property.set(((Str1_Str2_VersionImpl) this.stampedVersionProperty.get()).getStr1());
      }
      if (this.str2Property != null && !this.str2Property.get().equals(((Str1_Str2_VersionImpl) this.stampedVersionProperty.get()).getStr2())) {
         this.str2Property.set(((Str1_Str2_VersionImpl) this.stampedVersionProperty.get()).getStr2());
      }
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
      Str1_Str2_VersionImpl newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      Observable_Str1_Str2_VersionImpl newObservableVersion = 
              new Observable_Str1_Str2_VersionImpl(newVersion, (ObservableSemanticChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
    
    
}

