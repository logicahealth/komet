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
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_Version;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_Version {
   IntegerProperty nid1Property;
   IntegerProperty int2Property;
   StringProperty  str3Property;
   StringProperty  str4Property;
   IntegerProperty nid5Property;
   IntegerProperty nid6Property;

   public Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   public Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setNid1(versionToClone.getNid1());
      setInt2(versionToClone.getInt2());
      setStr3(versionToClone.getStr3());
      setStr4(versionToClone.getStr4());
      setNid5(versionToClone.getNid5());
      setNid6(versionToClone.getNid6());
   }

   @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl analog = new Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(analog));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForChanges());
        return (V) analog;
    }

   @Override
   public IntegerProperty int2Property() {
      if (this.stampedVersionProperty == null  && this.int2Property == null) {
        this.int2Property = new CommitAwareIntegerProperty(this, ObservableFields.INT2.toExternalString(), 
        0);
      }
      if (this.int2Property == null) {
         this.int2Property = new CommitAwareIntegerProperty(this, ObservableFields.INT2.toExternalString(), getInt2());
         this.int2Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setInt2(newValue.intValue());
             });
      }

      return this.int2Property;
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
                getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setNid1(newValue.intValue());
             });
      }

      return this.nid1Property;
   }

   @Override
   public IntegerProperty nid5Property() {
      if (this.stampedVersionProperty == null  && this.nid5Property == null) {
        this.nid5Property = new CommitAwareIntegerProperty(this, ObservableFields.NID5.toExternalString(),
        0);
      }
      if (this.nid5Property == null) {
         this.nid5Property = new CommitAwareIntegerProperty(this, ObservableFields.NID5.toExternalString(), getNid5());
         this.nid5Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setNid5(newValue.intValue());
             });
      }

      return this.nid5Property;
   }

   @Override
   public IntegerProperty nid6Property() {
      if (this.stampedVersionProperty == null  && this.nid6Property == null) {
        this.nid6Property = new CommitAwareIntegerProperty(this, ObservableFields.NID6.toExternalString(),
        0);
      }
      if (this.nid6Property == null) {
         this.nid6Property = new CommitAwareIntegerProperty(this, ObservableFields.NID6.toExternalString(), getNid6());
         this.nid6Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setNid6(newValue.intValue());
             });
      }

      return this.nid6Property;
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
                getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setStr3(newValue);
             });
      }

      return this.str3Property;
   }

   @Override
   public StringProperty str4Property() {
      if (this.stampedVersionProperty == null  && this.str4Property == null) {
        this.str4Property = new CommitAwareStringProperty(this, ObservableFields.STR4.toExternalString(),
        "");
      }
      if (this.str4Property == null) {
         this.str4Property = new CommitAwareStringProperty(this, ObservableFields.STR4.toExternalString(), getStr4());
         this.str4Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setStr4(newValue);
             });
      }

      return this.str4Property;
   }

   @Override
   public int getInt2() {
      if (this.int2Property != null) {
         return this.int2Property.get();
      }

      return getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().getInt2();
   }

   @Override
   public final void setInt2(int nid) {
       if (this.stampedVersionProperty == null) {
           this.int2Property();
       }
      if (this.int2Property != null) {
         this.int2Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setInt2(nid);
      }
   }

   @Override
   public int getNid1() {
      if (this.nid1Property != null) {
         return this.nid1Property.get();
      }

      return getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().getNid1();
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
      getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setNid1(nid);
      }
   }

   private Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl getNid1_Int2_Str3_Str4_Nid5_Nid6_Version() {
      return (Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getNid5() {
      if (this.nid5Property != null) {
         return this.nid5Property.get();
      }

      return getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().getNid5();
   }

   @Override
   public final void setNid5(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid5Property();
       }
      if (this.nid5Property != null) {
         this.nid5Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setNid5(nid);
      }
   }

   @Override
   public int getNid6() {
      if (this.nid6Property != null) {
         return this.nid6Property.get();
      }

      return getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().getNid6();
   }

   @Override
   public final void setNid6(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid6Property();
       }
      if (this.nid6Property != null) {
         this.nid6Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setNid6(nid);
      }
   }

   @Override
   public String getStr3() {
      if (this.str3Property != null) {
         return this.str3Property.get();
      }

      return getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().getStr3();
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
      getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setStr3(value);
      }
   }

   @Override
   public String getStr4() {
      if (this.str4Property != null) {
         return this.str4Property.get();
      }

      return getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().getStr4();
   }

   @Override
   public final void setStr4(String value) {
       if (this.stampedVersionProperty == null) {
           this.str4Property();
       }
      if (this.str4Property != null) {
         this.str4Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Int2_Str3_Str4_Nid5_Nid6_Version().setStr4(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(nid1Property());
      properties.add(int2Property());
      properties.add(str3Property());
      properties.add(str4Property());
      properties.add(nid5Property());
      properties.add(nid6Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(nid1Property());
      properties.add(int2Property());
      properties.add(str3Property());
      properties.add(str4Property());
      properties.add(nid5Property());
      properties.add(nid6Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) {
            Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl observableAnalog = (Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) analog;
            observableAnalog.setNid1(this.getNid1());
            observableAnalog.setInt2(this.getInt2());
            observableAnalog.setStr3(this.getStr3());
            observableAnalog.setStr4(this.getStr4());
            observableAnalog.setNid5(this.getNid5());
            observableAnalog.setNid6(this.getNid6());
        } else if (analog instanceof Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) {
             Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl simpleAnalog = (Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) analog;
            simpleAnalog.setNid1(this.getNid1());
            simpleAnalog.setInt2(this.getInt2());
            simpleAnalog.setStr3(this.getStr3());
            simpleAnalog.setStr4(this.getStr4());
            simpleAnalog.setNid5(this.getNid5());
            simpleAnalog.setNid6(this.getNid6());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl newVersion = new Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.nid1Property != null && 
              this.nid1Property.get() != ((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getNid1()) {
         this.nid1Property.set(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getNid1());
      }
     if (this.int2Property != null && 
              this.int2Property.get() != ((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getInt2()) {
         this.int2Property.set(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getInt2());
      }
     if (this.str3Property != null && 
              !this.str3Property.get().equals(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getStr3())) {
         this.str3Property.set(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getStr3());
      }
     if (this.str4Property != null && 
              !this.str4Property.get().equals(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getStr4())) {
         this.str4Property.set(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getStr4());
      }
     if (this.nid5Property != null && 
              this.nid5Property.get() != ((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getNid5()) {
         this.nid5Property.set(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getNid5());
      }
     if (this.nid6Property != null && 
              this.nid6Property.get() != ((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getNid6()) {
         this.nid6Property.set(((Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl) this.stampedVersionProperty.get()).getNid6());
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Version> V makeAnalog(int stampSequence) {
      Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl newVersion = this.getOptionalStampedVersion().get().makeAnalog(stampSequence);
      Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl newObservableVersion = 
              new Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(newVersion, getChronology());
      getChronology().getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}