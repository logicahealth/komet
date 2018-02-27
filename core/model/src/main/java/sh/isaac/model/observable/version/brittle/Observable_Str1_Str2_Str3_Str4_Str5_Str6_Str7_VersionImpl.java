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
import sh.isaac.api.observable.semantic.version.brittle.Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_VersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version {
   StringProperty str1Property;
   StringProperty str2Property;
   StringProperty str3Property;
   StringProperty str4Property;
   StringProperty str5Property;
   StringProperty str6Property;
   StringProperty str7Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }
   private Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setStr1(versionToClone.getStr1());
      setStr2(versionToClone.getStr2());
      setStr3(versionToClone.getStr3());
      setStr4(versionToClone.getStr4());
      setStr5(versionToClone.getStr5());
      setStr6(versionToClone.getStr6());
      setStr7(versionToClone.getStr7());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl analog = new Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(this, getChronology());
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
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr1(newValue);
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
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr2(newValue);
             });
      }

      return this.str2Property;
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
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr3(newValue);
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
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr4(newValue);
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
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr5(newValue);
             });
      }

      return this.str5Property;
   }

   @Override
   public StringProperty str6Property() {
      if (this.stampedVersionProperty == null  && this.str6Property == null) {
        this.str6Property = new CommitAwareStringProperty(this, ObservableFields.STR6.toExternalString(),
        "");
      }
      if (this.str6Property == null) {
         this.str6Property = new CommitAwareStringProperty(this, ObservableFields.STR6.toExternalString(), getStr6());
         this.str6Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr6(newValue);
             });
      }

      return this.str6Property;
   }

   @Override
   public StringProperty str7Property() {
      if (this.stampedVersionProperty == null  && this.str7Property == null) {
        this.str7Property = new CommitAwareStringProperty(this, ObservableFields.STR7.toExternalString(),
        "");
      }
      if (this.str7Property == null) {
         this.str7Property = new CommitAwareStringProperty(this, ObservableFields.STR7.toExternalString(), getStr7());
         this.str7Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr7(newValue);
             });
      }

      return this.str7Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr1() {
      if (this.str1Property != null) {
         return this.str1Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr1();
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
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr1(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version() {
      return (Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public String getStr2() {
      if (this.str2Property != null) {
         return this.str2Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr2();
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
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr2(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr3() {
      if (this.str3Property != null) {
         return this.str3Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr3();
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
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr3(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr4() {
      if (this.str4Property != null) {
         return this.str4Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr4();
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
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr4(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr5() {
      if (this.str5Property != null) {
         return this.str5Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr5();
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
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr5(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr6() {
      if (this.str6Property != null) {
         return this.str6Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr6();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr6(String value) {
       if (this.stampedVersionProperty == null) {
           this.str6Property();
       }
      if (this.str6Property != null) {
         this.str6Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr6(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr7() {
      if (this.str7Property != null) {
         return this.str7Property.get();
      }

      return getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().getStr7();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setStr7(String value) {
       if (this.stampedVersionProperty == null) {
           this.str7Property();
       }
      if (this.str7Property != null) {
         this.str7Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Str2_Str3_Str4_Str5_Str6_Str7_Version().setStr7(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(str1Property());
      properties.add(str2Property());
      properties.add(str3Property());
      properties.add(str4Property());
      properties.add(str5Property());
      properties.add(str6Property());
      properties.add(str7Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(str1Property());
      properties.add(str2Property());
      properties.add(str3Property());
      properties.add(str4Property());
      properties.add(str5Property());
      properties.add(str6Property());
      properties.add(str7Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) {
            Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl observableAnalog = (Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) analog;
            observableAnalog.setStr1(this.getStr1());
            observableAnalog.setStr2(this.getStr2());
            observableAnalog.setStr3(this.getStr3());
            observableAnalog.setStr4(this.getStr4());
            observableAnalog.setStr5(this.getStr5());
            observableAnalog.setStr6(this.getStr6());
            observableAnalog.setStr7(this.getStr7());
        } else if (analog instanceof Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) {
             Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl simpleAnalog = (Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) analog;
            simpleAnalog.setStr1(this.getStr1());
            simpleAnalog.setStr2(this.getStr2());
            simpleAnalog.setStr3(this.getStr3());
            simpleAnalog.setStr4(this.getStr4());
            simpleAnalog.setStr5(this.getStr5());
            simpleAnalog.setStr6(this.getStr6());
            simpleAnalog.setStr7(this.getStr7());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
    
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl newVersion = new Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.str1Property != null && !this.str1Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr1())) {
         this.str1Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr1());
      }
      if (this.str2Property != null && !this.str2Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr2())) {
         this.str2Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr2());
      }
      if (this.str3Property != null && !this.str3Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr3())) {
         this.str3Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr3());
      }
      if (this.str4Property != null && !this.str4Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr4())) {
         this.str4Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr4());
      }
      if (this.str5Property != null && !this.str5Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr5())) {
         this.str5Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr5());
      }
      if (this.str6Property != null && !this.str6Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr6())) {
         this.str6Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr6());
      }
      if (this.str7Property != null && !this.str7Property.get().equals(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr7())) {
         this.str7Property.set(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) this.stampedVersionProperty.get()).getStr7());
      }
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
      Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl newObservableVersion = 
              new Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(newVersion, (ObservableSemanticChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}

