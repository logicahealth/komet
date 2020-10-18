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
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Int2_Version;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.version.ObservableAbstractSemanticVersionImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Int2_VersionImpl;

/**
 *
 * @author kec
 */
public class Observable_Nid1_Int2_VersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements Observable_Nid1_Int2_Version {
   IntegerProperty nid1Property;
   IntegerProperty int2Property;

   public Observable_Nid1_Int2_VersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   public Observable_Nid1_Int2_VersionImpl(Observable_Nid1_Int2_VersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setNid1(versionToClone.getNid1());
      setInt2(versionToClone.getInt2());
   }
    public Observable_Nid1_Int2_VersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid) {
        super(VersionType.Nid1_Int2, primordialUuid, referencedComponentUuid, assemblageNid);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        Observable_Nid1_Int2_VersionImpl analog = new Observable_Nid1_Int2_VersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog());
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
                getNid1_Int2_Version().setInt2(newValue.intValue());
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
                getNid1_Int2_Version().setNid1(newValue.intValue());
             });
      }

      return this.nid1Property;
   }

   @Override
   public int getInt2() {
      if (this.int2Property != null) {
         return this.int2Property.get();
      }

      return getNid1_Int2_Version().getInt2();
   }

   @Override
   public final void setInt2(int value) {
       if (this.stampedVersionProperty == null) {
           this.int2Property();
       }
      if (this.int2Property != null) {
         this.int2Property.set(value);
      }

      if (this.stampedVersionProperty != null) {
      getNid1_Int2_Version().setInt2(value);
      }
   }

   @Override
   public int getNid1() {
      if (this.nid1Property != null) {
         return this.nid1Property.get();
      }

      return getNid1_Int2_Version().getNid1();
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
      getNid1_Int2_Version().setNid1(nid);
      }
   }

   private Nid1_Int2_VersionImpl getNid1_Int2_Version() {
      return (Nid1_Int2_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(nid1Property());
      properties.add(int2Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(nid1Property());
      properties.add(int2Property());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof Observable_Nid1_Int2_VersionImpl) {
            Observable_Nid1_Int2_VersionImpl observableAnalog = (Observable_Nid1_Int2_VersionImpl) analog;
            observableAnalog.setNid1(this.getNid1());
            observableAnalog.setInt2(this.getInt2());
        } else if (analog instanceof Nid1_Int2_VersionImpl) {
             Nid1_Int2_VersionImpl simpleAnalog = (Nid1_Int2_VersionImpl) analog;
            simpleAnalog.setNid1(this.getNid1());
            simpleAnalog.setInt2(this.getInt2());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        Nid1_Int2_VersionImpl newVersion = new Nid1_Int2_VersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

     @Override
    protected void updateVersion() {
      if (this.nid1Property != null && 
              this.nid1Property.get() != ((Nid1_Int2_VersionImpl) this.stampedVersionProperty.get()).getNid1()) {
         this.nid1Property.set(((Nid1_Int2_VersionImpl) this.stampedVersionProperty.get()).getNid1());
      }
      if (this.int2Property != null &&
              this.int2Property.get() != ((Nid1_Int2_VersionImpl) this.stampedVersionProperty.get()).getInt2()) {
         this.int2Property.set(((Nid1_Int2_VersionImpl) this.stampedVersionProperty.get()).getInt2());
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Version> V makeAnalog(int stampSequence) {
      Nid1_Int2_VersionImpl newVersion = getOptionalStampedVersion().get().makeAnalog(stampSequence);
      Observable_Nid1_Int2_VersionImpl newObservableVersion = new Observable_Nid1_Int2_VersionImpl(newVersion, getChronology());
      getChronology().getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
    }
}

