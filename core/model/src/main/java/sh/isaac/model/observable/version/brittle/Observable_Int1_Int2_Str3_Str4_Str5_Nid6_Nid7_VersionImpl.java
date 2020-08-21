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
import sh.isaac.api.observable.semantic.version.brittle.Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version {
   IntegerProperty int1Property;
   IntegerProperty int2Property;
   StringProperty  str3Property;
   StringProperty  str4Property;
   StringProperty  str5Property;
   IntegerProperty nid6Property;
   IntegerProperty nid7Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   private Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setInt1(versionToClone.getInt1());
      setInt2(versionToClone.getInt2());
      setStr3(versionToClone.getStr3());
      setStr4(versionToClone.getStr4());
      setStr5(versionToClone.getStr5());
      setNid6(versionToClone.getNid6());
      setNid7(versionToClone.getNid7());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl analog = new Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty int1Property() {
      if (this.stampedVersionProperty == null  && this.int1Property == null) {
        this.int1Property = new CommitAwareIntegerProperty(this, ObservableFields.INT1.toExternalString(), 
        0);
      }
      if (this.int1Property == null) {
         this.int1Property = new CommitAwareIntegerProperty(this, ObservableFields.INT1.toExternalString(), getInt1());
         this.int1Property.addListener(
             (observable, oldValue, newValue) -> {
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setInt1(newValue.intValue());
             });
      }

      return this.int1Property;
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
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setInt2(newValue.intValue());
             });
      }

      return this.int2Property;
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
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setNid6(newValue.intValue());
             });
      }

      return this.nid6Property;
   }

   @Override
   public IntegerProperty nid7Property() {
      if (this.stampedVersionProperty == null  && this.nid7Property == null) {
        this.nid7Property = new CommitAwareIntegerProperty(this, ObservableFields.NID7.toExternalString(), 
        0);
      }
      if (this.nid7Property == null) {
         this.nid7Property = new CommitAwareIntegerProperty(this, ObservableFields.NID7.toExternalString(), getNid7());
         this.nid7Property.addListener(
             (observable, oldValue, newValue) -> {
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setNid7(newValue.intValue());
             });
      }

      return this.nid7Property;
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
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setStr3(newValue);
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
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setStr4(newValue);
             });
      }

      return this.str4Property;
   }

   @Override
   public StringProperty str5Property() {
      if (this.stampedVersionProperty == null  && this.str5Property == null) {
        this.str5Property = new CommitAwareStringProperty(this, ObservableFields.STR5.toExternalString(), 
        "");
      }
      if (this.str5Property == null) {
         this.str5Property = new CommitAwareStringProperty(this, ObservableFields.STR5.toExternalString(), getStr5());
         this.str5Property.addListener(
             (observable, oldValue, newValue) -> {
                getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setStr5(newValue);
             });
      }

      return this.str5Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getInt1() {
      if (this.int1Property != null) {
         return this.int1Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getInt1();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setInt1(int nid) {
       if (this.stampedVersionProperty == null) {
           this.int1Property();
       }
      if (this.int1Property != null) {
         this.int1Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setInt1(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version() {
      return (Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getInt2() {
      if (this.int2Property != null) {
         return this.int2Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getInt2();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setInt2(int nid) {
       if (this.stampedVersionProperty == null) {
           this.int2Property();
       }
      if (this.int2Property != null) {
         this.int2Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setInt2(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid6() {
      if (this.nid6Property != null) {
         return this.nid6Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getNid6();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setNid6(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid6Property();
       }
      if (this.nid6Property != null) {
         this.nid6Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setNid6(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid7() {
      if (this.nid7Property != null) {
         return this.nid7Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getNid7();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setNid7(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid7Property();
       }
      if (this.nid7Property != null) {
         this.nid7Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setNid7(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr3() {
      if (this.str3Property != null) {
         return this.str3Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getStr3();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr3(String value) {
       if (this.stampedVersionProperty == null) {
           this.str3Property();
       }
      if (this.str3Property != null) {
         this.str3Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setStr3(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr4() {
      if (this.str4Property != null) {
         return this.str4Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getStr4();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr4(String value) {
       if (this.stampedVersionProperty == null) {
           this.str4Property();
       }
      if (this.str4Property != null) {
         this.str4Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setStr4(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr5() {
      if (this.str5Property != null) {
         return this.str5Property.get();
      }

      return getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().getStr5();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr5(String value) {
       if (this.stampedVersionProperty == null) {
           this.str5Property();
       }
      if (this.str5Property != null) {
         this.str5Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
        getInt1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version().setStr5(value);
      }
   }


   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(int1Property());
      properties.add(int2Property());
      properties.add(nid6Property());
      properties.add(nid7Property());
      properties.add(str3Property());
      properties.add(str4Property());
      properties.add(str5Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(int1Property());
      properties.add(int2Property());
      properties.add(nid6Property());
      properties.add(nid7Property());
      properties.add(str3Property());
      properties.add(str4Property());
      properties.add(str5Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) {
            Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl observableAnalog = (Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) analog;
            observableAnalog.setInt1(this.getInt1());
            observableAnalog.setInt2(this.getInt2());
            observableAnalog.setStr3(this.getStr3());
            observableAnalog.setStr4(this.getStr4());
            observableAnalog.setStr5(this.getStr5());
            observableAnalog.setNid6(this.getNid6());
            observableAnalog.setNid7(this.getNid7());
        } else if (analog instanceof Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) {
             Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl simpleAnalog = (Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) analog;
            simpleAnalog.setInt1(this.getInt1());
            simpleAnalog.setInt2(this.getInt2());
            simpleAnalog.setStr3(this.getStr3());
            simpleAnalog.setStr4(this.getStr4());
            simpleAnalog.setStr5(this.getStr5());
            simpleAnalog.setNid6(this.getNid6());
            simpleAnalog.setNid7(this.getNid7());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl newVersion = new Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.int1Property != null && 
              this.int1Property.get() != ((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getInt1()) {
         this.int1Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getInt1());
      }
      if (this.int2Property != null && 
              this.int2Property.get() != ((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getInt2()) {
         this.int2Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getInt2());
      }
      if (this.str3Property != null && 
              !this.str3Property.get().equals(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getStr3())) {
         this.str3Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getStr3());
      }
      if (this.str4Property != null && 
              !this.str4Property.get().equals(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getStr4())) {
         this.str4Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getStr4());
      }
      if (this.str5Property != null && 
              !this.str5Property.get().equals(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getStr5())) {
         this.str5Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getStr5());
      }
      if (this.nid6Property != null && 
              this.nid6Property.get() != ((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getNid6()) {
         this.nid6Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getNid6());
      }
      if (this.nid7Property != null && 
              this.nid7Property.get() != ((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getNid7()) {
         this.nid7Property.set(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) this.stampedVersionProperty.get()).getNid7());
      }
    }

    @Override
    public <V extends Version> V setupAnalog(int stampSequence) {
        Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl newVersion = getStampedVersion().setupAnalog(stampSequence);
        Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl newObservableVersion = new Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(
                newVersion,
                getChronology());
        chronology.getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }

}

