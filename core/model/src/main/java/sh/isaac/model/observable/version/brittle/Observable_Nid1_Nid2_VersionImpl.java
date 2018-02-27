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
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Nid2_Version;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Nid2_VersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Observable_Nid1_Nid2_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Nid1_Nid2_Version {
   IntegerProperty nid1Property;
   IntegerProperty nid2Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Nid1_Nid2_VersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   private Observable_Nid1_Nid2_VersionImpl(Observable_Nid1_Nid2_Str3_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setNid1(versionToClone.getNid1());
      setNid2(versionToClone.getNid2());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        Observable_Nid1_Nid2_VersionImpl analog = new Observable_Nid1_Nid2_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

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
                getNid1_Nid2_Version().setNid1(newValue.intValue());
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
                getNid1_Nid2_Version().setNid2(newValue.intValue());
             });
      }

      return this.nid2Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid1() {
      if (this.nid1Property != null) {
         return this.nid1Property.get();
      }

      return getNid1_Nid2_Version().getNid1();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setNid1(int nid) {
       if (this.stampedVersionProperty == null) {
           this.nid1Property();
       }
      if (this.nid1Property != null) {
         this.nid1Property.set(nid);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Nid2_Version().setNid1(nid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private Nid1_Nid2_VersionImpl getNid1_Nid2_Version() {
      return (Nid1_Nid2_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getNid2() {
      if (this.nid2Property != null) {
         return this.nid2Property.get();
      }

      return getNid1_Nid2_Version().getNid2();
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
      getNid1_Nid2_Version().setNid2(nid);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(nid1Property());
      properties.add(nid2Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(nid1Property());
      properties.add(nid2Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Nid1_Nid2_VersionImpl) {
            Observable_Nid1_Nid2_VersionImpl observableAnalog = (Observable_Nid1_Nid2_VersionImpl) analog;
            observableAnalog.setNid1(this.getNid1());
            observableAnalog.setNid2(this.getNid2());
        } else if (analog instanceof Nid1_Nid2_VersionImpl) {
             Nid1_Nid2_VersionImpl simpleAnalog = (Nid1_Nid2_VersionImpl) analog;
            simpleAnalog.setNid1(this.getNid1());
            simpleAnalog.setNid2(this.getNid2());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Nid1_Nid2_VersionImpl newVersion = new Nid1_Nid2_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

    @Override
    protected void updateVersion() {
      if (this.nid1Property != null && 
              this.nid1Property.get() != ((Nid1_Nid2_VersionImpl) this.stampedVersionProperty.get()).getNid1()) {
         this.nid1Property.set(((Nid1_Nid2_VersionImpl) this.stampedVersionProperty.get()).getNid1());
      }
     if (this.nid2Property != null && 
              this.nid2Property.get() != ((Nid1_Nid2_VersionImpl) this.stampedVersionProperty.get()).getNid2()) {
         this.nid2Property.set(((Nid1_Nid2_VersionImpl) this.stampedVersionProperty.get()).getNid2());
      }
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
      Nid1_Nid2_VersionImpl newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      Observable_Nid1_Nid2_VersionImpl newObservableVersion = 
              new Observable_Nid1_Nid2_VersionImpl(newVersion, (ObservableSemanticChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}

