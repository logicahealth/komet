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

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.State;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.VersionImpl;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareLongProperty;
import sh.isaac.model.observable.CommitAwareObjectProperty;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------
/**
 * The Class ObservableVersionImpl.
 *
 * @author kec
 */
public abstract class ObservableVersionImpl
        implements ObservableVersion, CommittableComponent {

   /**
    * The state property.
    */
   ObjectProperty<State> stateProperty;

   /**
    * The time property.
    */
   LongProperty timeProperty;

   /**
    * The author sequence property.
    */
   IntegerProperty authorSequenceProperty;

   /**
    * The module sequence property.
    */
   IntegerProperty moduleSequenceProperty;

   /**
    * The path sequence property.
    */
   IntegerProperty pathSequenceProperty;

   /**
    * The commit state property.
    */
   ObjectProperty<CommitStates> commitStateProperty;

   /**
    * The commit state binding.
    */
   ObjectBinding<CommitStates> commitStateBinding;

   /**
    * The stamp sequence property.
    */
   IntegerProperty stampSequenceProperty;

   /**
    * The stamped version.
    */
   protected VersionImpl stampedVersion;

   /**
    * The chronology.
    */
   protected final ObservableChronology chronology;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new observable version impl.
    *
    * @param stampedVersion the stamped version
    * @param chronology the chronology
    */
   public ObservableVersionImpl(Version stampedVersion, ObservableChronology chronology) {
      this.stampedVersion = (VersionImpl) stampedVersion;
      this.chronology = chronology;
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Author sequence property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty authorSequenceProperty() {
      if (this.authorSequenceProperty == null) {
         this.authorSequenceProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.AUTHOR_SEQUENCE_FOR_VERSION.toExternalString(),
                 getAuthorSequence());
         this.authorSequenceProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersion.setAuthorSequence(newValue.intValue());

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersion.getStampSequence());
                    }
                 });
      }

      return this.authorSequenceProperty;
   }

   /**
    * Cancel.
    */
   public void cancel() {
      if (!isUncommitted()) {
         throw new RuntimeException("Attempt to cancel an already committed version: " + this);
      }

      ((VersionImpl) this.stampedVersion).cancel();
      this.getChronology()
              .getVersionList()
              .remove(this);
      this.stampSequenceProperty()
              .set(-1);
   }

   /**
    * Commit state property.
    *
    * @return the object property
    */
   @Override
   public final ObjectProperty<CommitStates> commitStateProperty() {
      if (this.commitStateProperty == null) {
         this.commitStateBinding = new ObjectBinding<CommitStates>() {
            @Override
            protected CommitStates computeValue() {
               if (ObservableVersionImpl.this.stampedVersion.getStampSequence() == -1) {
                  return CommitStates.CANCELED;
               }
               if (ObservableVersionImpl.this.timeProperty()
                       .get() == Long.MAX_VALUE) {
                  return CommitStates.UNCOMMITTED;
               }

               return CommitStates.COMMITTED;
            }
         };
         this.commitStateProperty = new SimpleObjectProperty(
                 this,
                 ObservableFields.COMMITTED_STATE_FOR_VERSION.toExternalString(),
                 this.commitStateBinding.get());
         this.commitStateProperty.bind(this.commitStateBinding);
      }

      return this.commitStateProperty;
   }

   /**
    * Module sequence property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty moduleSequenceProperty() {
      if (this.moduleSequenceProperty == null) {
         this.moduleSequenceProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.MODULE_SEQUENCE_FOR_VERSION.toExternalString(),
                 getModuleSequence());
         this.moduleSequenceProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersion.setModuleSequence(newValue.intValue());

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersion.getStampSequence());
                    }
                 });
      }

      return this.moduleSequenceProperty;
   }

   /**
    * Path sequence property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty pathSequenceProperty() {
      if (this.pathSequenceProperty == null) {
         this.pathSequenceProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.PATH_SEQUENCE_FOR_VERSION.toExternalString(),
                 getPathSequence());
         this.pathSequenceProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersion.setPathSequence(newValue.intValue());

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersion.getStampSequence());
                    }
                 });
      }

      return this.pathSequenceProperty;
   }

   /**
    * Stamp sequence property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty stampSequenceProperty() {
      if (this.stampSequenceProperty == null) {
         this.stampSequenceProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.STAMP_SEQUENCE_FOR_VERSION.toExternalString(),
                 getStampSequence());
         this.stampSequenceProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    if (newValue.intValue() != this.stampedVersion.getStampSequence()) {
                       throw new RuntimeException(
                               "ERROR: Cannot set stamp value directly. stampedVersion now out of sync with observable");
                    }
                 });
         stateProperty();
         authorSequenceProperty();
         moduleSequenceProperty();
         pathSequenceProperty();
         stateProperty();
         timeProperty();
      }

      return this.stampSequenceProperty;
   }

   /**
    * State property.
    *
    * @return the object property
    */
   @Override
   public final ObjectProperty<State> stateProperty() {
      if (this.stateProperty == null) {
         this.stateProperty = new CommitAwareObjectProperty<>(
                 this,
                 ObservableFields.STATUS_FOR_VERSION.toExternalString(),
                 getState());
         this.stateProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersion.setStatus(newValue);

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersion.getStampSequence());
                    }
                 });
      }

      return this.stateProperty;
   }

   /**
    * Time property.
    *
    * @return the long property
    */
   @Override
   public final LongProperty timeProperty() {
      if (this.timeProperty == null) {
         this.timeProperty = new CommitAwareLongProperty(
                 this,
                 ObservableFields.TIME_FOR_VERSION.toExternalString(),
                 getTime());
         this.timeProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    if (this.stampedVersion.getStampSequence() != -1) {
                       this.stampedVersion.setTime(newValue.longValue());

                       if (this.commitStateBinding != null) {
                          this.commitStateBinding.invalidate();
                       }

                       if (this.stampSequenceProperty != null) {
                          this.stampSequenceProperty.setValue(this.stampedVersion.getStampSequence());
                       }
                    }
                 });
      }

      return this.timeProperty;
   }

   @Override
   public String toString() {
      return "ObservableVersionImpl{" + stampedVersion + '}';
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public String toUserString() {
      return toString();
   }

   /**
    * Update version.
    *
    * @param stampedVersion the stamped version
    */
   public void updateVersion(Version stampedVersion) {
      if (!this.stampedVersion.getClass()
              .equals(stampedVersion.getClass())) {
         throw new IllegalStateException(
                 "versions are not of same class: \n" + this.stampedVersion.getClass().getName() + "\n"
                 + stampedVersion.getClass().getName());
      }

      this.stampedVersion = (VersionImpl) stampedVersion;

      if ((this.stampSequenceProperty != null)
              && (this.stampSequenceProperty.get() != stampedVersion.getStampSequence())) {
         this.stampSequenceProperty.set(stampedVersion.getStampSequence());
      }

      if (this.commitStateBinding != null) {
         this.commitStateBinding.invalidate();
      }

      if ((this.stateProperty != null) && (this.stateProperty.get() != stampedVersion.getState())) {
         this.stateProperty.set(stampedVersion.getState());
      }

      if ((this.authorSequenceProperty != null)
              && (this.authorSequenceProperty.get() != stampedVersion.getAuthorSequence())) {
         this.authorSequenceProperty.set(stampedVersion.getAuthorSequence());
      }

      if ((this.moduleSequenceProperty != null)
              && (this.moduleSequenceProperty.get() != stampedVersion.getModuleSequence())) {
         this.moduleSequenceProperty.set(stampedVersion.getModuleSequence());
      }

      if ((this.pathSequenceProperty != null)
              && (this.pathSequenceProperty.get() != stampedVersion.getPathSequence())) {
         this.pathSequenceProperty.set(stampedVersion.getPathSequence());
      }
      
      if ((this.timeProperty != null)
              && (this.timeProperty.get() != stampedVersion.getTime())) {
         this.timeProperty.set(stampedVersion.getTime());
      }
   }

   protected abstract void updateVersion();

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the author sequence.
    *
    * @return the author sequence
    */
   @Override
   public final int getAuthorSequence() {
      if (this.authorSequenceProperty != null) {
         return this.authorSequenceProperty.get();
      }

      return this.stampedVersion.getAuthorSequence();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the author sequence.
    *
    * @param authorSequence the new author sequence
    */
   @Override
   public void setAuthorSequence(int authorSequence) {
      if (this.authorSequenceProperty != null) {
         this.authorSequenceProperty.set(authorSequence);
      }

      this.stampedVersion.setAuthorSequence(authorSequence);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public ObservableChronology getChronology() {
      return this.chronology;
   }

   /**
    * Gets the commit state.
    *
    * @return the commit state
    */
   @Override
   public final CommitStates getCommitState() {
      if (this.commitStateProperty != null) {
         return this.commitStateProperty.get();
      }

      if (getTime() == Long.MAX_VALUE) {
         return CommitStates.UNCOMMITTED;
      }

      if (getTime() == Long.MIN_VALUE) {
         return CommitStates.CANCELED;
      }

      return CommitStates.COMMITTED;
   }

   /**
    * Gets the module sequence.
    *
    * @return the module sequence
    */
   @Override
   public final int getModuleSequence() {
      if (this.moduleSequenceProperty != null) {
         return this.moduleSequenceProperty.get();
      }

      return this.stampedVersion.getModuleSequence();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the module sequence.
    *
    * @param moduleSequence the new module sequence
    */
   @Override
   public void setModuleSequence(int moduleSequence) {
      if (this.moduleSequenceProperty != null) {
         this.moduleSequenceProperty.set(moduleSequence);
      }

      this.stampedVersion.setModuleSequence(moduleSequence);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return ((VersionImpl) this.stampedVersion).getNid();
   }

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public final int getPathSequence() {
      if (this.pathSequenceProperty != null) {
         return this.pathSequenceProperty.get();
      }

      return this.stampedVersion.getPathSequence();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the path sequence.
    *
    * @param pathSequence the new path sequence
    */
   @Override
   public void setPathSequence(int pathSequence) {
      if (this.pathSequenceProperty != null) {
         this.pathSequenceProperty.set(pathSequence);
      }

      this.stampedVersion.setPathSequence(pathSequence);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      return ((VersionImpl) this.stampedVersion).getPrimordialUuid();
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      return new ArrayList(
              Arrays.asList(
                      new Property[]{
                         stateProperty(), timeProperty(), authorSequenceProperty(), moduleSequenceProperty(), pathSequenceProperty(),
                         commitStateProperty(), stampSequenceProperty(),}));
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   @Override
   public final int getStampSequence() {
      if (this.stampSequenceProperty != null) {
         return this.stampSequenceProperty.get();
      }

      return this.stampedVersion.getStampSequence();
   }

   public VersionImpl getStampedVersion() {
      return stampedVersion;
   }

   /**
    * Gets the state.
    *
    * @return the state
    */
   @Override
   public final State getState() {
      if (this.stateProperty != null) {
         return this.stateProperty.get();
      }

      return this.stampedVersion.getState();
   }

   //~--- set methods ---------------------------------------------------------
   @Override
   public final void setStatus(State state) {
      if (this.stateProperty != null) {
         this.stateProperty.set(state);
      }

      this.stampedVersion.setStatus(state);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public final long getTime() {
      if (this.timeProperty != null) {
         return this.timeProperty.get();
      }

      return this.stampedVersion.getTime();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the time.
    *
    * @param time the new time
    */
   @Override
   public void setTime(long time) {
      if (this.timeProperty != null) {
         this.timeProperty.set(time);
      }

      this.stampedVersion.setTime(time);

      if (this.commitStateBinding != null) {
         this.commitStateBinding.invalidate();
      }
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Checks if uncommitted.
    *
    * @return true, if uncommitted
    */
   @Override
   public boolean isUncommitted() {
      return getCommitState() == CommitStates.UNCOMMITTED;
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return ((VersionImpl) this.stampedVersion).getUuidList();
   }

   /**
    * Gets the version sequence.
    *
    * @return the version sequence
    */
   public short getVersionSequence() {
      return ((VersionImpl) this.stampedVersion).getVersionSequence();
   }

   @Override
   public VersionType getVersionType() {
      return ((VersionImpl) this.stampedVersion).getVersionType();
   }
}
