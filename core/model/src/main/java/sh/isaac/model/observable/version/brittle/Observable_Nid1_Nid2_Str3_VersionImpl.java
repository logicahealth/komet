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
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Nid2_Str3_Version;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Nid2_Str3_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Nid1_Nid2_Str3_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Nid1_Nid2_Str3_Version {
   IntegerProperty nid1Property;
   IntegerProperty nid2Property;
   StringProperty  str3Property;

   public Observable_Nid1_Nid2_Str3_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   private Observable_Nid1_Nid2_Str3_VersionImpl(Observable_Nid1_Nid2_Str3_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setNid1(versionToClone.getNid1());
      setNid2(versionToClone.getNid2());
      setStr3(versionToClone.getStr3());
   }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        Observable_Nid1_Nid2_Str3_VersionImpl analog = new Observable_Nid1_Nid2_Str3_VersionImpl(this, getChronology());
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
                getNid1_Nid2_Str3_Version().setNid1(newValue.intValue());
             });
      }

      return this.nid1Property;
   }

   @Override
   public IntegerProperty nid2Property() {
      if (this.stampedVersionProperty == null  && this.nid2Property == null) {
        this.nid2Property = new CommitAwareIntegerProperty(this, ObservableFields.NID2.toExternalString(),
        0);
      }
      if (this.nid2Property == null) {
         this.nid2Property = new CommitAwareIntegerProperty(this, ObservableFields.NID2.toExternalString(), getNid2());
         this.nid2Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Nid2_Str3_Version().setNid2(newValue.intValue());
             });
      }

      return this.nid2Property;
   }

   @Override
   public StringProperty str3Property() {
      if (this.stampedVersionProperty == null  && this.str3Property == null) {
        this.str3Property = new CommitAwareStringProperty(this, ObservableFields.STR3.toExternalString(),
        "");
      }
      if (this.str3Property == null) {
         this.str3Property = new CommitAwareStringProperty(this, ObservableFields.STR3.toExternalString(), getStr3());
         this.str3Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Nid2_Str3_Version().setStr3(newValue);
             });
      }

      return this.str3Property;
   }

   @Override
   public int getNid1() {
      if (this.nid1Property != null) {
         return this.nid1Property.get();
      }

      return getNid1_Nid2_Str3_Version().getNid1();
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
      getNid1_Nid2_Str3_Version().setNid1(nid);
      }
   }

   private Nid1_Nid2_Str3_VersionImpl getNid1_Nid2_Str3_Version() {
      return (Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getNid2() {
      if (this.nid2Property != null) {
         return this.nid2Property.get();
      }

      return getNid1_Nid2_Str3_Version().getNid2();
   }

   @Override
   public final void setNid2(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid2Property();
       }
      if (this.nid2Property != null) {
         this.nid2Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Nid2_Str3_Version().setNid2(nid);
      }
   }

   @Override
   public String getStr3() {
      if (this.str3Property != null) {
         return this.str3Property.get();
      }

      return getNid1_Nid2_Str3_Version().getStr3();
   }

   @Override
   public final void setStr3(String value) {
       if (this.stampedVersionProperty == null) {
           this.str3Property();
       }
      if (this.str3Property != null) {
         this.str3Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Nid2_Str3_Version().setStr3(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(nid1Property());
      properties.add(nid2Property());
      properties.add(str3Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(nid1Property());
      properties.add(nid2Property());
      properties.add(str3Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Nid1_Nid2_Str3_VersionImpl) {
            Observable_Nid1_Nid2_Str3_VersionImpl observableAnalog = (Observable_Nid1_Nid2_Str3_VersionImpl) analog;
            observableAnalog.setNid1(this.getNid1());
            observableAnalog.setNid2(this.getNid2());
            observableAnalog.setStr3(this.getStr3());
        } else if (analog instanceof Nid1_Nid2_Str3_VersionImpl) {
             Nid1_Nid2_Str3_VersionImpl simpleAnalog = (Nid1_Nid2_Str3_VersionImpl) analog;
            simpleAnalog.setNid1(this.getNid1());
            simpleAnalog.setNid2(this.getNid2());
            simpleAnalog.setStr3(this.getStr3());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Nid1_Nid2_Str3_VersionImpl newVersion = new Nid1_Nid2_Str3_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.nid1Property != null && 
              this.nid1Property.get() != ((Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get()).getNid1()) {
         this.nid1Property.set(((Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get()).getNid1());
      }
     if (this.nid2Property != null && 
              this.nid2Property.get() != ((Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get()).getNid2()) {
         this.nid2Property.set(((Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get()).getNid2());
      }
     if (this.str3Property != null && 
              !this.str3Property.get().equals(((Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get()).getStr3())) {
         this.str3Property.set(((Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get()).getStr3());
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Version> V makeAnalog(int stampSequence) {
      Observable_Nid1_Nid2_Str3_VersionImpl newVersion = getStampedVersion().makeAnalog(stampSequence);
      Observable_Nid1_Nid2_Str3_VersionImpl newObservableVersion = new Observable_Nid1_Nid2_Str3_VersionImpl(newVersion, getChronology());
      getChronology().getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}
