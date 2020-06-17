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
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Str1_Str2_Nid3_Nid4_Nid5_Version;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl 
        extends ObservableAbstractSemanticVersionImpl
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
        copyLocalFields(analog);
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

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) {
            Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl observableAnalog = (Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) analog;
            observableAnalog.setStr1(this.getStr1());
            observableAnalog.setStr2(this.getStr2());
            observableAnalog.setNid3(this.getNid3());
            observableAnalog.setNid4(this.getNid4());
            observableAnalog.setNid5(this.getNid5());
        } else if (analog instanceof Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) {
             Str1_Str2_Nid3_Nid4_Nid5_VersionImpl simpleAnalog = (Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) analog;
            simpleAnalog.setStr1(this.getStr1());
            simpleAnalog.setStr2(this.getStr2());
            simpleAnalog.setNid3(this.getNid3());
            simpleAnalog.setNid4(this.getNid4());
            simpleAnalog.setNid5(this.getNid5());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Str1_Str2_Nid3_Nid4_Nid5_VersionImpl newVersion = new Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.str1Property != null && 
              !this.str1Property.get().equals(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getStr1())) {
         this.str1Property.set(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getStr1());
      }
      if (this.str2Property != null && 
              !this.str2Property.get().equals(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getStr2())) {
         this.str2Property.set(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getStr2());
      }
      if (this.nid3Property != null && 
              this.nid3Property.get() != ((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getNid3()) {
         this.nid3Property.set(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getNid3());
      }
      if (this.nid4Property != null && 
              this.nid4Property.get() != ((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getNid4()) {
         this.nid4Property.set(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getNid4());
      }
      if (this.nid5Property != null && 
              this.nid5Property.get() != ((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getNid5()) {
         this.nid5Property.set(((Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) this.stampedVersionProperty.get()).getNid5());
      }
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
        Str1_Str2_Nid3_Nid4_Nid5_VersionImpl newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
        return setupAnalog(newVersion);
    }

    @Override
    public <V extends Version> V makeAnalog(Transaction transaction, int authorNid) {
        Str1_Str2_Nid3_Nid4_Nid5_VersionImpl newVersion = this.stampedVersionProperty.get().makeAnalog(transaction, authorNid);
        return setupAnalog(newVersion);
    }

    private <V extends Version> V setupAnalog(Str1_Str2_Nid3_Nid4_Nid5_VersionImpl newVersion) {
        Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl newObservableVersion =
                new Observable_Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(newVersion, (ObservableSemanticChronology) chronology);
        ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
        return (V) newObservableVersion;
    }
}

