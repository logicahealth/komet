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
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Str1_Nid2_Nid3_Nid4_Version;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Nid2_Nid3_Nid4_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Str1_Nid2_Nid3_Nid4_VersionImpl 
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Str1_Nid2_Nid3_Nid4_Version {
   StringProperty  str1Property;
   IntegerProperty nid2Property;
   IntegerProperty nid3Property;
   IntegerProperty nid4Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Str1_Nid2_Nid3_Nid4_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }
   private Observable_Str1_Nid2_Nid3_Nid4_VersionImpl(Observable_Str1_Nid2_Nid3_Nid4_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setStr1(versionToClone.getStr1());
      setNid2(versionToClone.getNid2());
      setNid3(versionToClone.getNid3());
      setNid4(versionToClone.getNid4());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        Observable_Str1_Nid2_Nid3_Nid4_VersionImpl analog = new Observable_Str1_Nid2_Nid3_Nid4_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog(this));
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
                getStr1_Nid2_Nid3_Nid4_Version().setNid3(newValue.intValue());
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
                getStr1_Nid2_Nid3_Nid4_Version().setNid4(newValue.intValue());
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
                getStr1_Nid2_Nid3_Nid4_Version().setStr1(newValue);
             });
      }

      return this.str1Property;
   }

   @Override
   public IntegerProperty nid2Property() {
      if (this.stampedVersionProperty == null  && this.nid2Property == null) {
        this.nid2Property = new CommitAwareIntegerProperty(this, ObservableFields.NID2.toExternalString(),
        0);
      }
      if (this.nid2Property == null) {
         this.nid2Property = new CommitAwareIntegerProperty(this, ObservableFields.NID2.toExternalString(), getNid4());
         this.nid2Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Nid2_Nid3_Nid4_Version().setNid4(newValue.intValue());
             });
      }

      return this.nid2Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid3() {
      if (this.nid3Property != null) {
         return this.nid3Property.get();
      }

      return getStr1_Nid2_Nid3_Nid4_Version().getNid3();
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
      getStr1_Nid2_Nid3_Nid4_Version().setNid3(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid4() {
      if (this.nid4Property != null) {
         return this.nid4Property.get();
      }

      return getStr1_Nid2_Nid3_Nid4_Version().getNid4();
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
      getStr1_Nid2_Nid3_Nid4_Version().setNid4(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr1() {
      if (this.str1Property != null) {
         return this.str1Property.get();
      }

      return getStr1_Nid2_Nid3_Nid4_Version().getStr1();
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
      getStr1_Nid2_Nid3_Nid4_Version().setStr1(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Str1_Nid2_Nid3_Nid4_VersionImpl getStr1_Nid2_Nid3_Nid4_Version() {
      return (Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getNid2() {
      if (this.nid2Property != null) {
         return this.nid2Property.get();
      }

      return getStr1_Nid2_Nid3_Nid4_Version().getNid2();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setNid2(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid2Property();
       }
      if (this.nid2Property != null) {
         this.nid2Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getStr1_Nid2_Nid3_Nid4_Version().setNid2(nid);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(str1Property());
      properties.add(nid2Property());
      properties.add(nid3Property());
      properties.add(nid4Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();

      properties.add(str1Property());
      properties.add(nid2Property());
      properties.add(nid3Property());
      properties.add(nid4Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Str1_Nid2_Nid3_Nid4_VersionImpl) {
            Observable_Str1_Nid2_Nid3_Nid4_VersionImpl observableAnalog = (Observable_Str1_Nid2_Nid3_Nid4_VersionImpl) analog;
            observableAnalog.setStr1(this.getStr1());
            observableAnalog.setNid2(this.getNid2());
            observableAnalog.setNid3(this.getNid3());
            observableAnalog.setNid4(this.getNid4());
        } else if (analog instanceof Str1_Nid2_Nid3_Nid4_VersionImpl) {
             Str1_Nid2_Nid3_Nid4_VersionImpl simpleAnalog = (Str1_Nid2_Nid3_Nid4_VersionImpl) analog;
            simpleAnalog.setStr1(this.getStr1());
            simpleAnalog.setNid2(this.getNid2());
            simpleAnalog.setNid3(this.getNid3());
            simpleAnalog.setNid4(this.getNid4());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Str1_Nid2_Nid3_Nid4_VersionImpl newVersion = new Str1_Nid2_Nid3_Nid4_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.str1Property != null && 
              !this.str1Property.get().equals(((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getStr1())) {
         this.str1Property.set(((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getStr1());
      }
      if (this.nid2Property != null && 
              this.nid2Property.get() != ((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getNid2()) {
         this.nid2Property.set(((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getNid2());
      }
      if (this.nid3Property != null && 
              this.nid3Property.get() != ((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getNid3()) {
         this.nid3Property.set(((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getNid3());
      }
      if (this.nid4Property != null && 
              this.nid4Property.get() != ((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getNid4()) {
         this.nid4Property.set(((Str1_Nid2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get()).getNid4());
      }
    }

    @Override
    public <V extends Version> V setupAnalog(int stampSequence) {
        Str1_Nid2_Nid3_Nid4_VersionImpl newVersion = getStampedVersion().setupAnalog(stampSequence);
        Observable_Str1_Nid2_Nid3_Nid4_VersionImpl newObservableVersion = new Observable_Str1_Nid2_Nid3_Nid4_VersionImpl(
                newVersion,
                getChronology());
        chronology.getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }

}

