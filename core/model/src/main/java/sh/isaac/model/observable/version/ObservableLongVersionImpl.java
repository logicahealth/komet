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



package sh.isaac.model.observable.version;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.MutableLongVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.commitaware.CommitAwareLongProperty;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;

/**
 *
 * @author kec
 */
public class ObservableLongVersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements ObservableLongVersion {
   LongProperty longProperty;

   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableLongVersionImpl(LongVersion version, ObservableSemanticChronology chronology) {
      super(version, chronology);
   }

   public ObservableLongVersionImpl(ObservableLongVersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setLongValue(versionToClone.getLongValue());
   }
    public ObservableLongVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid) {
        super(VersionType.LONG, primordialUuid, referencedComponentUuid, assemblageNid);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        ObservableLongVersionImpl analog = new ObservableLongVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog());
        return (V) analog;
    }

   @Override
   public LongProperty longValueProperty() {
      if (this.stampedVersionProperty == null && this.longProperty == null) {
         this.longProperty = new CommitAwareLongProperty(
             this,
             ObservableFields.LONG_VALUE_FOR_SEMANTIC.toExternalString(),
                 0);
      }
      if (this.longProperty == null) {
         this.longProperty = new CommitAwareLongProperty(
             this,
             ObservableFields.LONG_VALUE_FOR_SEMANTIC.toExternalString(),
             getLongValue());
         this.longProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((LongVersionImpl) this.stampedVersionProperty.get()).setLongValue(newValue.longValue());
             });
      }

      return this.longProperty;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      LongVersion newVersion = getStampedVersion().makeAnalog(stampSequence);
      ObservableLongVersionImpl newObservableVersion = new ObservableLongVersionImpl(newVersion, getChronology());
      getChronology().getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
   }

   @Override
   public String toString() {
      return "ObservableLongVersionImpl{value:" + getLongValue() + '}';
   }

   @Override
   protected void updateVersion() {
      if (this.longProperty != null && this.longProperty.get() != ((LongVersion) this.stampedVersionProperty.get()).getLongValue()) {
         this.longProperty.set(((LongVersion) this.stampedVersionProperty.get()).getLongValue());
      }
   }

   @Override
   public long getLongValue() {
      if (this.longProperty != null) {
         return this.longProperty.get();
      }

      return ((LongVersion) this.stampedVersionProperty.get()).getLongValue();
   }

   @Override
   public final void setLongValue(long longValue) {
       if (this.stampedVersionProperty == null) {
           this.longValueProperty();
       }
      if (this.longProperty != null) {
         this.longProperty.set(longValue);
      }

      if (this.stampedVersionProperty != null) {
         ((MutableLongVersion) this.stampedVersionProperty.get()).setLongValue(longValue);
      }
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(longValueProperty());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(longValueProperty());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableLongVersionImpl) {
            ObservableLongVersionImpl observableAnalog = (ObservableLongVersionImpl) analog;
            observableAnalog.setLongValue(this.getLongValue());
        } else if (analog instanceof LongVersionImpl) {
             LongVersionImpl simpleAnalog = (LongVersionImpl) analog;
             simpleAnalog.setLongValue(this.getLongValue());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        LongVersionImpl newVersion = new LongVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }
}
