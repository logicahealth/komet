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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Str2_Version;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Str2_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Nid1_Str2_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Nid1_Str2_Version {
   IntegerProperty nid1Property;
   StringProperty  str2Property;

   public Observable_Nid1_Str2_VersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   private Observable_Nid1_Str2_VersionImpl(Observable_Nid1_Str2_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setNid1(versionToClone.getNid1());
      setStr2(versionToClone.getStr2());
   }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        Observable_Nid1_Str2_VersionImpl analog = new Observable_Nid1_Str2_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog());
        return (V) analog;
    }

   @Override
   public IntegerProperty nid1Property() {
      if (this.stampedVersionProperty == null  && this.nid1Property == null) {
        this.nid1Property = new CommitAwareIntegerProperty(this, ObservableFields.NID1.toExternalString(),
        0);
      }
      if (this.nid1Property == null) {
         this.nid1Property = new CommitAwareIntegerProperty(this, ObservableFields.NID1.toExternalString(), getNid1());
         this.nid1Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Str2_Version().setNid1(newValue.intValue());
             });
      }

      return this.nid1Property;
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
                getNid1_Str2_Version().setStr2(newValue);
             });
      }

      return this.str2Property;
   }

   @Override
   public int getNid1() {
      if (this.nid1Property != null) {
         return this.nid1Property.get();
      }

      return getNid1_Str2_Version().getNid1();
   }

   @Override
   public final void setNid1(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid1Property();
       }
      if (this.nid1Property != null) {
         this.nid1Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Str2_Version().setNid1(nid);
      }
   }

   private Nid1_Str2_VersionImpl getNid1_Str2_Version() {
      return (Nid1_Str2_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public String getStr2() {
      if (this.str2Property != null) {
         return this.str2Property.get();
      }

      return getNid1_Str2_Version().getStr2();
   }

   @Override
   public final void setStr2(String value) {
       if (this.stampedVersionProperty == null) {
           this.str2Property();
       }
      if (this.str2Property != null) {
         this.str2Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Str2_Version().setStr2(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(nid1Property());
      properties.add(str2Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      
      properties.add(nid1Property());
      properties.add(str2Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Nid1_Str2_VersionImpl) {
            Observable_Nid1_Str2_VersionImpl observableAnalog = (Observable_Nid1_Str2_VersionImpl) analog;
            observableAnalog.setNid1(this.getNid1());
            observableAnalog.setStr2(this.getStr2());
        } else if (analog instanceof Nid1_Str2_VersionImpl) {
             Nid1_Str2_VersionImpl simpleAnalog = (Nid1_Str2_VersionImpl) analog;
             simpleAnalog.setNid1(this.getNid1());
             simpleAnalog.setStr2(this.getStr2());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Nid1_Str2_VersionImpl newVersion = new Nid1_Str2_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.nid1Property != null && 
              this.nid1Property.get() != ((Nid1_Str2_VersionImpl) this.stampedVersionProperty.get()).getNid1()) {
         this.nid1Property.set(((Nid1_Str2_VersionImpl) this.stampedVersionProperty.get()).getNid1());
      }
     if (this.str2Property != null && 
              !this.str2Property.get().equals(((Nid1_Str2_VersionImpl) this.stampedVersionProperty.get()).getStr2())) {
         this.str2Property.set(((Nid1_Str2_VersionImpl) this.stampedVersionProperty.get()).getStr2());
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Version> V makeAnalog(int stampSequence) {
      Nid1_Str2_VersionImpl newVersion = getOptionalStampedVersion().get().makeAnalog(stampSequence);
      Observable_Nid1_Str2_VersionImpl newObservableVersion = new Observable_Nid1_Str2_VersionImpl(newVersion, getChronology());
      getChronology().getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}

