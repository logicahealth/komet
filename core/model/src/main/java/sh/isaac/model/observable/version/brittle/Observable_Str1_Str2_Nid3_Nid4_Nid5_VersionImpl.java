/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.model.observable.version.brittle;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Str1_Str2_Nid3_Nid4_Nid5_Version;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableSemanticVersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl 
        extends ObservableSemanticVersionImpl
         implements Observable_Str1_Str2_Nid3_Nid4_Nid5_Version {
   StringProperty  str1Property;
   StringProperty  str2Property;
   IntegerProperty nid3Property;
   IntegerProperty nid4Property;
  IntegerProperty nid5Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }
   private Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setStr1(versionToClone.getStr1());
      setStr2(versionToClone.getStr2());
      setNid3(versionToClone.getNid3());
      setNid4(versionToClone.getNid4());
      setNid5(versionToClone.getNid5());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl analog = new Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(this, getChronology());
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty nid3Property() {
      if (this.stampedVersionProperty == null  && this.nid3Property == null) {
        this.nid3Property = new CommitAwareIntegerProperty(this, ObservableFields.NID3.toExternalString(),
        0);
      }
      if (this.nid3Property == null) {
         this.nid3Property = new CommitAwareIntegerProperty(this, ObservableFields.NID3.toExternalString(), getNid3());
         this.nid3Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Nid5_Version().setNid3(newValue.intValue());
             });
      }

      return this.nid3Property;
   }

   @Override
   public IntegerProperty nid4Property() {
      if (this.stampedVersionProperty == null  && this.nid4Property == null) {
        this.nid4Property = new CommitAwareIntegerProperty(this, ObservableFields.NID4.toExternalString(),
        0);
      }
      if (this.nid4Property == null) {
         this.nid4Property = new CommitAwareIntegerProperty(this, ObservableFields.NID4.toExternalString(), getNid4());
         this.nid4Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Nid5_Version().setNid4(newValue.intValue());
             });
      }

      return this.nid4Property;
   }


   @Override
   public IntegerProperty nid5Property() {
      if (this.stampedVersionProperty == null  && this.nid5Property == null) {
        this.nid5Property = new CommitAwareIntegerProperty(this, ObservableFields.NID5.toExternalString(),
        0);
      }
      if (this.nid5Property == null) {
         this.nid5Property = new CommitAwareIntegerProperty(this, ObservableFields.NID5.toExternalString(), getNid4());
         this.nid5Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Nid5_Version().setNid5(newValue.intValue());
             });
      }

      return this.nid4Property;
   }
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
                getStr1_Str2_Nid3_Nid4_Nid5_Version().setStr1(newValue);
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
                getStr1_Str2_Nid3_Nid4_Nid5_Version().setStr2(newValue);
             });
      }

      return this.str2Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid3() {
      if (this.nid3Property != null) {
         return this.nid3Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Nid5_Version().getNid3();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setNid3(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid3Property();
       }
      if (this.nid3Property != null) {
         this.nid3Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Str2_Nid3_Nid4_Nid5_Version().setNid3(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid4() {
      if (this.nid4Property != null) {
         return this.nid4Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Nid5_Version().getNid4();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setNid4(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid4Property();
       }
      if (this.nid4Property != null) {
         this.nid4Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Str2_Nid3_Nid4_Nid5_Version().setNid4(nid);
      }
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
      getStr1_Str2_Nid3_Nid4_Nid5_Version().setNid5(nid);
      }
   }
   @Override
   public int getNid5() {
      if (this.nid5Property != null) {
         return this.nid5Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Nid5_Version().getNid5();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr1() {
      if (this.str1Property != null) {
         return this.str1Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Nid5_Version().getStr1();
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
      getStr1_Str2_Nid3_Nid4_Nid5_Version().setStr1(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Str1_Str2_Nid3_Nid4_Nid5_VersionImpl getStr1_Str2_Nid3_Nid4_Nid5_Version() {
      return (Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public String getStr2() {
      if (this.str2Property != null) {
         return this.str2Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Nid5_Version().getStr2();
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
      getStr1_Str2_Nid3_Nid4_Nid5_Version().setStr2(value);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(str1Property());
      properties.add(str2Property());
      properties.add(nid3Property());
      properties.add(nid4Property());
      properties.add(nid5Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(str1Property());
      properties.add(str2Property());
      properties.add(nid3Property());
      properties.add(nid4Property());
      properties.add(nid5Property());
      return properties;
    }
}

