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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
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
   ObjectProperty<Status> stateProperty;

   /**
    * The time property.
    */
   LongProperty timeProperty;

   /**
    * The author sequence property.
    */
   IntegerProperty authorNidProperty;

   /**
    * The module sequence property.
    */
   IntegerProperty moduleNidProperty;

   /**
    * The path sequence property.
    */
   IntegerProperty pathNidProperty;

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
   
   private HashMap<String, Object> userObjectMap;

   /**
    * The stamped version.
    */
   protected final SimpleObjectProperty<VersionImpl> stampedVersionProperty;

   /**
    * The chronology.
    */
   protected final ObservableChronology chronology;
   
   protected final VersionType versionType;

   //~--- constructors --------------------------------------------------------

  /**
    * limited arg constructor, for making an observable uncoupled for underlying data, 
    * for example when creating a new component prior to being committed for 
    * the first time. 
     * @param versionType
    */
   public ObservableVersionImpl(VersionType versionType) {
       this.stampedVersionProperty = null;
       this.chronology = null;
       this.versionType = versionType;
       getProperties();
   }

    /**
     * Instantiates a new observable version.
     *
     * @param stampedVersion the stamped version
     * @param chronology the chronology
     */
    public ObservableVersionImpl(Version stampedVersion, ObservableChronology chronology) {
        this.stampedVersionProperty = new SimpleObjectProperty<>((VersionImpl) stampedVersion);
        this.chronology = chronology;
        this.versionType = stampedVersion.getSemanticType();
    }

   protected ObservableVersionImpl(ObservableChronology chronology) {
      this.chronology = chronology;
      this.stampedVersionProperty = null;
      this.versionType = chronology.getVersionType();
   }

   //~--- methods -------------------------------------------------------------
   
   @Override
   public void addAdditionalUuids(UUID... uuids)
   {
      ((VersionImpl) this.stampedVersionProperty.get()).addAdditionalUuids(uuids);
   }
   
   /**
    * Author nid property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty authorNidProperty() {
      if (this.stampedVersionProperty == null && this.authorNidProperty == null) {
        this.authorNidProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.AUTHOR_NID_FOR_VERSION.toExternalString(),
                 0);
      }
      if (this.authorNidProperty == null) {
         this.authorNidProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.AUTHOR_NID_FOR_VERSION.toExternalString(),
                 getAuthorNid());
         this.authorNidProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersionProperty.get().setAuthorNid(newValue.intValue());

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                    }
                 });
      }

      return this.authorNidProperty;
   }

   /**
    * Cancel.
    */
   public void cancel() {
      if (!isUncommitted()) {
         throw new RuntimeException("Attempt to cancel an already committed version: " + this);
      }

      this.stampedVersionProperty.get().cancel();
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
      if (this.stampedVersionProperty == null && this.commitStateProperty == null) {
         this.commitStateProperty = new SimpleObjectProperty(
                 this,
                 ObservableFields.COMMITTED_STATE_FOR_VERSION.toExternalString(),
                 CommitStates.UNCOMMITTED);
      }
      if (this.commitStateProperty == null) {
         this.commitStateBinding = new ObjectBinding<CommitStates>() {
            @Override
            protected CommitStates computeValue() {
               if (ObservableVersionImpl.this.stampedVersionProperty.get().getStampSequence() == -1) {
                  return CommitStates.CANCELED;
               }
               if (ObservableVersionImpl.this.timeProperty()
                       .get() == Long.MAX_VALUE) {
                  return CommitStates.UNCOMMITTED;
               }

               return CommitStates.COMMITTED;
            }
         };
         this.timeProperty().addListener((observable) -> {
            this.commitStateBinding.invalidate();
         });
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
   public final IntegerProperty moduleNidProperty() {
      if (this.stampedVersionProperty == null && this.moduleNidProperty == null) {
         this.moduleNidProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.MODULE_NID_FOR_VERSION.toExternalString(),
                 0);
      }
      if (this.moduleNidProperty == null) {
         this.moduleNidProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.MODULE_NID_FOR_VERSION.toExternalString(),
                 getModuleNid());
         this.moduleNidProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersionProperty.get().setModuleNid(newValue.intValue());

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                    }
                 });
      }

      return this.moduleNidProperty;
   }

   /**
    * Path nid property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty pathNidProperty() {
      if (this.stampedVersionProperty == null && this.pathNidProperty == null) {
         this.pathNidProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.PATH_NID_FOR_VERSION.toExternalString(),
                 0);
      }
      if (this.pathNidProperty == null) {
         this.pathNidProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.PATH_NID_FOR_VERSION.toExternalString(),
                 getPathNid());
         this.pathNidProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersionProperty.get().setPathNid(newValue.intValue());

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                    }
                 });
      }

      return this.pathNidProperty;
   }

   /**
    * Stamp sequence property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty stampSequenceProperty() {
      if (this.stampedVersionProperty == null) {
         throw new IllegalStateException();
      }
      if (this.stampSequenceProperty == null) {
         this.stampSequenceProperty = new CommitAwareIntegerProperty(
                 this,
                 ObservableFields.STAMP_SEQUENCE_FOR_VERSION.toExternalString(),
                 getStampSequence());
         this.stampSequenceProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    if (newValue.intValue() != this.stampedVersionProperty.get().getStampSequence()) {
                       throw new RuntimeException(
                               "ERROR: Cannot set stamp value directly. stampedVersion now out of sync with observable");
                    }
                 });
         stateProperty();
         authorNidProperty();
         moduleNidProperty();
         pathNidProperty();
         stateProperty();
         timeProperty();
      }

      return this.stampSequenceProperty;
   }

   /**
    * Status property.
    *
    * @return the object property
    */
   @Override
   public final ObjectProperty<Status> stateProperty() {
      if (this.stampedVersionProperty == null && this.stateProperty == null) {
         this.stateProperty = new CommitAwareObjectProperty<>(
                 this,
                 ObservableFields.STATUS_FOR_VERSION.toExternalString(),
                 Status.PRIMORDIAL);
      }
      if (this.stateProperty == null) {
         this.stateProperty = new CommitAwareObjectProperty<>(
                 this,
                 ObservableFields.STATUS_FOR_VERSION.toExternalString(),
                 getStatus());
         this.stateProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    this.stampedVersionProperty.get().setStatus(newValue);

                    if (this.stampSequenceProperty != null) {
                       this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
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
      if (this.stampedVersionProperty == null && this.timeProperty == null) {
         this.timeProperty = new CommitAwareLongProperty(
                 this,
                 ObservableFields.TIME_FOR_VERSION.toExternalString(),
                 Long.MAX_VALUE);
      }
      if (this.timeProperty == null) {
         this.timeProperty = new CommitAwareLongProperty(
                 this,
                 ObservableFields.TIME_FOR_VERSION.toExternalString(),
                 getTime());
         this.timeProperty.addListener(
                 (observable, oldValue, newValue) -> {
                    if (this.stampedVersionProperty.get().getStampSequence() != -1) {
                       if (this.stampedVersionProperty.get().getTime() != newValue.longValue()) {
                          this.stampedVersionProperty.get().setTime(newValue.longValue());

                          if (this.commitStateBinding != null) {
                             this.commitStateBinding.invalidate();
                          }

                          if (this.stampSequenceProperty != null) {
                             this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                          }
                       }
                    }
                 });
      }

      return this.timeProperty;
   }

   @Override
   public String toString() {
      return "ObservableVersionImpl{" + stampedVersionProperty.get() + '}';
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
   public final void updateVersion(Version stampedVersion) {
      if (!this.stampedVersionProperty.get().getClass()
              .equals(stampedVersion.getClass())) {
         throw new IllegalStateException(
                 "versions are not of same class: \n" + this.stampedVersionProperty.get().getClass().getName() + "\n"
                 + stampedVersion.getClass().getName());
      }

      this.stampedVersionProperty.set((VersionImpl) stampedVersion);

      if ((this.stampSequenceProperty != null)
              && (this.stampSequenceProperty.get() != stampedVersion.getStampSequence())) {
         this.stampSequenceProperty.set(stampedVersion.getStampSequence());
      }

      if (this.commitStateBinding != null) {
         this.commitStateBinding.invalidate();
      }

      if ((this.stateProperty != null) && (this.stateProperty.get() != stampedVersion.getStatus())) {
         this.stateProperty.set(stampedVersion.getStatus());
      }

      if ((this.authorNidProperty != null)
              && (this.authorNidProperty.get() != stampedVersion.getAuthorNid())) {
         this.authorNidProperty.set(stampedVersion.getAuthorNid());
      }

      if ((this.moduleNidProperty != null)
              && (this.moduleNidProperty.get() != stampedVersion.getModuleNid())) {
         this.moduleNidProperty.set(stampedVersion.getModuleNid());
      }

      if ((this.pathNidProperty != null)
              && (this.pathNidProperty.get() != stampedVersion.getPathNid())) {
         this.pathNidProperty.set(stampedVersion.getPathNid());
      }

      if ((this.timeProperty != null)
              && (this.timeProperty.get() != stampedVersion.getTime())) {
         this.timeProperty.set(stampedVersion.getTime());
      }
      
      updateVersion();
   }

   protected abstract void updateVersion();

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the author sequence.
    *
    * @return the author sequence
    */
   @Override
   public final int getAuthorNid() {
      if (this.authorNidProperty != null) {
         return this.authorNidProperty.get();
      }

      if (this.stampedVersionProperty != null) {
        return this.stampedVersionProperty.get().getAuthorNid();
      }
      throw new IllegalStateException();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the author sequence.
    *
    * @param authorSequence the new author sequence
    */
   @Override
   public void setAuthorNid(int authorSequence) {
       if (this.stampedVersionProperty == null) {
           this.authorNidProperty();
       }
      if (this.authorNidProperty != null) {
         this.authorNidProperty.set(authorSequence);
      }
      if (this.stampedVersionProperty != null) {
        this.stampedVersionProperty.get().setAuthorNid(authorSequence);
      }
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
      if (this.stampedVersionProperty == null) {
          return CommitStates.UNCOMMITTED;
      }
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
   public final int getModuleNid() {
      if (this.moduleNidProperty != null) {
         return this.moduleNidProperty.get();
      }

      if (this.stampedVersionProperty != null) {
        return this.stampedVersionProperty.get().getModuleNid();
      }
      return TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the module sequence.
    *
    * @param moduleNid the new module sequence
    */
   @Override
   public void setModuleNid(int moduleNid) {
       if (this.stampedVersionProperty == null) {
           this.moduleNidProperty();
       }
      if (this.moduleNidProperty != null) {
         this.moduleNidProperty.set(moduleNid);
      }

      if (this.stampedVersionProperty != null) {
        this.stampedVersionProperty.get().setModuleNid(moduleNid);
      }
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return getChronology().getNid();
   }

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public final int getPathNid() {
      if (this.pathNidProperty != null) {
         return this.pathNidProperty.get();
      }
      if (this.stampedVersionProperty != null) {
        return stampedVersionProperty.get().getPathNid();
      }
      return TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the path sequence.
    *
    * @param pathSequence the new path sequence
    */
   @Override
   public void setPathNid(int pathSequence) {
       if (this.stampedVersionProperty == null) {
           this.pathNidProperty();
       }
      if (this.pathNidProperty != null) {
         this.pathNidProperty.set(pathSequence);
      }
      if (this.stampedVersionProperty != null) {
        this.stampedVersionProperty.get().setPathNid(pathSequence);
      }
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
       
      return getChronology().getPrimordialUuid();
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      return new ArrayList(
              Arrays.asList(new Property[]{
                         stateProperty(), timeProperty(), authorNidProperty(), moduleNidProperty(), pathNidProperty(),
                         commitStateProperty()}));
   }

    protected abstract List<Property<?>> getEditableProperties2();
    
    @Override
    public final List<Property<?>> getEditableProperties() {
        ArrayList<Property<?>> propertyList = new ArrayList();
        propertyList.add(stateProperty());
        propertyList.addAll(getEditableProperties2());
        propertyList.addAll(Arrays.asList(new Property<?>[]{
                    moduleNidProperty(), pathNidProperty()
                }));
      return propertyList;
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

      return this.stampedVersionProperty.get().getStampSequence();
   }

   public VersionImpl getStampedVersion() {
      return stampedVersionProperty.get();
   }

   /**
    * Gets the state.
    *
    * @return the state
    */
   @Override
   public final Status getStatus() {
      if (this.stateProperty != null) {
         return this.stateProperty.get();
      }
      if (this.stampedVersionProperty != null ) {
        return this.stampedVersionProperty.get().getStatus();
      }
      return Status.PRIMORDIAL;
   }

   //~--- set methods ---------------------------------------------------------
   @Override
   public final void setStatus(Status state) {
       if (this.stampedVersionProperty == null) {
           this.stateProperty();
       }
      if (this.stateProperty != null) {
         this.stateProperty.set(state);
      }
      if (this.stampedVersionProperty != null) {
          this.stampedVersionProperty.get().setStatus(state);
      }
      
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
      if (this.stampedVersionProperty != null) {
        return this.stampedVersionProperty.get().getTime();
      }
      return Long.MAX_VALUE;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the time.
    *
    * @param time the new time
    */
   @Override
   public void setTime(long time) {
       if (this.stampedVersionProperty == null) {
           throw new IllegalStateException();
       }
      if (this.timeProperty != null) {
         this.timeProperty.set(time);
      }

      this.stampedVersionProperty.get().setTime(time);

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
      return ((VersionImpl) this.stampedVersionProperty.get()).getUuidList();
   }

   @Override
   public final VersionType getSemanticType() {
      return this.versionType;
   }

   @Override
   public <T> Optional<T> getUserObject(String objectKey) {
      if (userObjectMap == null) {
         return Optional.empty();
      }
      return Optional.ofNullable((T) userObjectMap.get(objectKey));
   }

   @Override
   public void putUserObject(String objectKey, Object object) {
      if (userObjectMap == null) {
         userObjectMap = new HashMap<>();
      }
      userObjectMap.put(objectKey, object);
   }

   @Override
   public <T> Optional<T> removeUserObject(String objectKey) {
      T value = null;
      if (userObjectMap != null) {
         value = (T) userObjectMap.remove(objectKey);
         if (userObjectMap.isEmpty()) {
            userObjectMap = null;
         }
      }
      return Optional.ofNullable(value);
   }
   
   
}
