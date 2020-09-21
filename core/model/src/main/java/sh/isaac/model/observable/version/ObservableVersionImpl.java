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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.VersionImpl;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.commitaware.CommitAwareLongProperty;
import sh.isaac.model.observable.commitaware.CommitAwareObjectProperty;

/**
 * The Class ObservableVersionImpl.
 *
 * @author kec
 */
public abstract class ObservableVersionImpl
        implements ObservableVersion, CommittableComponent {

    protected static final Logger LOG = LogManager.getLogger();

    IntegerProperty nidProperty;

    /**
     * The primordial uuid property.
     */
    ObjectProperty<UUID> primordialUuidProperty;

    /**
     * The state property.
     */
    ObjectProperty<Status> stateProperty;

    /**
     * The time property.
     */
    LongProperty timeProperty;

    /**
     * The author nid property.
     */
    IntegerProperty authorNidProperty;

    /**
     * The module nid property.
     */
    IntegerProperty moduleNidProperty;

    /**
     * The path nid property.
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
    protected ObservableChronologyImpl chronology;

    protected final VersionType versionType;

    /**
     * The assemblage nid property.
     */
    protected IntegerProperty assemblageNidProperty;

    /**
     * limited arg constructor, for making an observable uncoupled for
     * underlying data, for example when creating a new component prior to being
     * committed for the first time.
     *
     * @param versionType
     * @param primordialUuid
     * @param assemblageNid
     */
    public ObservableVersionImpl(VersionType versionType, UUID primordialUuid, int assemblageNid) {
        this.stampedVersionProperty = null;
        this.chronology = null;
        this.versionType = versionType;
        assemblageNidProperty(assemblageNid);
        this.primordialUuidProperty = new SimpleObjectProperty<>(
                this,
                ObservableFields.PRIMORDIAL_UUID_FOR_COMPONENT.toExternalString(),
                primordialUuid);
        getProperties();
    }

    protected ObjectProperty<UUID> primordialUuidProperty() {
        if (this.primordialUuidProperty == null) {
            this.primordialUuidProperty = new SimpleObjectProperty<>(
                    this,
                    ObservableFields.PRIMORDIAL_UUID_FOR_COMPONENT.toExternalString());
            this.primordialUuidProperty.set(chronology.getPrimordialUuid());
        }
        return this.primordialUuidProperty;
    }

    protected IntegerProperty nidProperty() {
        if (this.nidProperty == null) {
            this.nidProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.NATIVE_ID_FOR_COMPONENT.toExternalString(), 
                    Get.nidForUuids(this.getPrimordialUuid()));
        }
        return this.nidProperty;
    }

    /**
     * Instantiates a new observable version.
     *
     * @param stampedVersion the stamped version
     * @param chronology the chronology
     */
    public ObservableVersionImpl(Version stampedVersion, ObservableChronology chronology) {
        if (stampedVersion instanceof VersionImpl) {
            this.stampedVersionProperty = new SimpleObjectProperty<>((VersionImpl) stampedVersion);
        } else {
            this.stampedVersionProperty = null;
        }
        this.chronology = (ObservableChronologyImpl) chronology;
        this.versionType = stampedVersion.getSemanticType();
    }

    protected ObservableVersionImpl(ObservableChronology chronology) {
        this.chronology = (ObservableChronologyImpl) chronology;
        this.stampedVersionProperty = null;
        this.versionType = chronology.getVersionType();
    }

    public void setChronology(ObservableChronology chronology) {
        if (this.chronology != null) {
            throw new IllegalStateException("Chronology is not null. Cannot change.");
        }
        this.chronology = (ObservableChronologyImpl) chronology;
    }

    /**
     * Assemblage sequence property.
     *
     * @return the integer property
     */
    public final IntegerProperty assemblageNidProperty() {
        return assemblageNidProperty(getAssemblageNid());
    }

    private IntegerProperty assemblageNidProperty(int assemblageNid) {
        if (this.assemblageNidProperty == null) {
            this.assemblageNidProperty
                    = new CommitAwareIntegerProperty(this,
                            TermAux.ASSEMBLAGE_NID_FOR_COMPONENT.toExternalString(),
                            assemblageNid);
        }
        return this.assemblageNidProperty;
    }

    @Override
    public final int getAssemblageNid() {
        if (this.assemblageNidProperty != null) {
            return this.assemblageNidProperty.get();
        }
        return this.chronology.getAssemblageNid();
    }

    @Override
    public void addAdditionalUuids(UUID... uuids) {
        ((VersionImpl) this.stampedVersionProperty.get()).addAdditionalUuids(uuids);
    }

    @Override
    public final IntegerProperty authorNidProperty() {
        if (this.stampedVersionProperty == null && this.authorNidProperty == null) {
            this.authorNidProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.AUTHOR_FOR_VERSION.toExternalString(),
                    0);
        }
        if (this.authorNidProperty == null) {
            this.authorNidProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.AUTHOR_FOR_VERSION.toExternalString(),
                    getAuthorNid());
            this.authorNidProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        this.stampedVersionProperty.get().setAuthorNid(newValue.intValue(), null);

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

    @Override
    public final ObjectProperty<CommitStates> commitStateProperty() {
        if (this.stampedVersionProperty == null && this.commitStateProperty == null) {
            this.commitStateProperty = new SimpleObjectProperty<>(
                    this,
                    TermAux.COMMITTED_STATE_FOR_VERSION.toExternalString(),
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
                            .get() == Long.MAX_VALUE || Get.stampService().isUncommitted(ObservableVersionImpl.this.stampSequenceProperty().get())) {
                        return CommitStates.UNCOMMITTED;
                    }

                    return CommitStates.COMMITTED;
                }
            };
            this.timeProperty().addListener((observable) -> {
                this.commitStateBinding.invalidate();
            });
            this.commitStateProperty = new SimpleObjectProperty<>(
                    this,
                    TermAux.COMMITTED_STATE_FOR_VERSION.toExternalString(),
                    this.commitStateBinding.get());
            this.commitStateProperty.bind(this.commitStateBinding);
        }

        return this.commitStateProperty;
    }

    @Override
    public final IntegerProperty moduleNidProperty() {
        if (this.stampedVersionProperty == null && this.moduleNidProperty == null) {
            this.moduleNidProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.MODULE_FOR_VERSION.toExternalString(),
                    0);
        }
        if (this.moduleNidProperty == null) {
            this.moduleNidProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.MODULE_FOR_VERSION.toExternalString(),
                    getModuleNid());
            this.moduleNidProperty.addListener(
                    (observable, oldValue, newValue) -> {  //TODO [KEC] no idea if there is a transaction somewhere that should be fed into this....
                        this.stampedVersionProperty.get().setModuleNid(newValue.intValue(), null);

                        if (this.stampSequenceProperty != null) {
                            this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                        }
                    });
        }

        return this.moduleNidProperty;
    }

    @Override
    public final IntegerProperty pathNidProperty() {
        if (this.stampedVersionProperty == null && this.pathNidProperty == null) {
            this.pathNidProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.PATH_FOR_VERSION.toExternalString(),
                    0);
        }
        if (this.pathNidProperty == null) {
            this.pathNidProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.PATH_FOR_VERSION.toExternalString(),
                    getPathNid());
            this.pathNidProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        this.stampedVersionProperty.get().setPathNid(newValue.intValue(), null);

                        if (this.stampSequenceProperty != null) {
                            this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                        }
                    });
        }

        return this.pathNidProperty;
    }

    @Override
    public final IntegerProperty stampSequenceProperty() {
        if (this.stampedVersionProperty == null) {
            throw new IllegalStateException();
        }
        if (this.stampSequenceProperty == null) {
            this.stampSequenceProperty = new CommitAwareIntegerProperty(
                    this,
                    TermAux.STAMP_SEQUENCE_FOR_VERSION.toExternalString(),
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

    @Override
    public final ObjectProperty<Status> stateProperty() {
        if (this.stampedVersionProperty == null && this.stateProperty == null) {
            this.stateProperty = new CommitAwareObjectProperty<>(
                    this,
                    TermAux.STATUS_FOR_VERSION.toExternalString(),
                    Status.PRIMORDIAL);
        }
        if (this.stateProperty == null) {
            this.stateProperty = new CommitAwareObjectProperty<>(
                    this,
                    TermAux.STATUS_FOR_VERSION.toExternalString(),
                    getStatus());
            this.stateProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        this.stampedVersionProperty.get().setStatus(newValue, null);

                        if (this.stampSequenceProperty != null) {
                            this.stampSequenceProperty.setValue(this.stampedVersionProperty.get().getStampSequence());
                        }
                    });
        }

        return this.stateProperty;
    }

    @Override
    public final LongProperty timeProperty() {
        if (this.stampedVersionProperty == null && this.timeProperty == null) {
            this.timeProperty = new CommitAwareLongProperty(
                    this,
                    TermAux.TIME_FOR_VERSION.toExternalString(),
                    Long.MAX_VALUE);
        }
        if (this.timeProperty == null) {
            this.timeProperty = new CommitAwareLongProperty(
                    this,
                    TermAux.TIME_FOR_VERSION.toExternalString(),
                    getTime());
            this.timeProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        if (this.stampedVersionProperty.get().getStampSequence() != -1) {
                            if (this.stampedVersionProperty.get().getTime() != newValue.longValue()) {
                                this.stampedVersionProperty.get().setTime(newValue.longValue(), null);

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
        if (stampedVersionProperty != null) {
            return "ObservableVersionImpl{" + stampedVersionProperty.get() + '}';
        }
        return "ObservableVersionImpl{ no wrapped version }";
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

        try {
            updateVersion();
        } catch (UnsupportedOperationException e) {
            LOG.warn("\n  ** Could not update to version: " + stampedVersion);
        }
    }

    protected abstract void updateVersion();

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

    public void setAuthorNid(int authorSequence) {
        setAuthorNid(authorSequence, null);
    }
    
    @Override
    public void setAuthorNid(int authorSequence, Transaction t) {
        if (this.stampedVersionProperty == null) {
            this.authorNidProperty();
        }
        if (this.authorNidProperty != null) {
            this.authorNidProperty.set(authorSequence);
        }
        if (this.stampedVersionProperty != null) {
            this.stampedVersionProperty.get().setAuthorNid(authorSequence, t);
        }
    }

    @Override
    public ObservableChronology getChronology() {
        return this.chronology;
    }

    @Override
    public final CommitStates getCommitState() {
        if (this.stampedVersionProperty == null) {
            return CommitStates.UNCOMMITTED;
        }
        if (this.commitStateProperty != null) {
            return this.commitStateProperty.get();
        }
        if (getStampSequence() == -1) {
            return CommitStates.CANCELED;
        }

        if (getTime() == Long.MAX_VALUE || Get.stampService().isUncommitted(ObservableVersionImpl.this.stampSequenceProperty().get())) {
            return CommitStates.UNCOMMITTED;
        }

        if (getTime() == Long.MIN_VALUE) {
            return CommitStates.CANCELED;
        }

        return CommitStates.COMMITTED;
    }

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


    /**
     * @param moduleNidForAnalog
     */
    public void setModuleNid(int moduleNidForAnalog) {
        setModuleNid(moduleNidForAnalog, null);
    }
    
    @Override
    public void setModuleNid(int moduleNid, Transaction t) {
        if (this.stampedVersionProperty == null) {
            this.moduleNidProperty();
        }
        if (this.moduleNidProperty != null) {
            this.moduleNidProperty.set(moduleNid);
        }

        if (this.stampedVersionProperty != null) {
            this.stampedVersionProperty.get().setModuleNid(moduleNid, t);
        }
    }

    @Override
    public int getNid() {
        if (this.primordialUuidProperty != null) {
            return Get.identifierService().assignNid(this.primordialUuidProperty.get());
        }
        if (this.chronology != null) {
            return this.chronology.getNid();
        }
        UUID primordialUuid = getPrimordialUuid();
        if (Get.identifierService().hasUuid(primordialUuid)) {
            return Get.identifierService().getNidForUuids(primordialUuid);
        }
        return Get.identifierService().assignNid(primordialUuid);
    }

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

    public void setPathNid(int pathSequence) {
        setPathNid(pathSequence, null);
    }
    
    @Override
    public void setPathNid(int pathSequence, Transaction t) {
        if (this.stampedVersionProperty == null) {
            this.pathNidProperty();
        }
        if (this.pathNidProperty != null) {
            this.pathNidProperty.set(pathSequence);
        }
        if (this.stampedVersionProperty != null) {
            this.stampedVersionProperty.get().setPathNid(pathSequence, t);
        }
    }

    @Override
    public UUID getPrimordialUuid() {
        if (this.primordialUuidProperty != null) {
            return this.primordialUuidProperty.get();
        }
        return getChronology().getPrimordialUuid();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<ReadOnlyProperty<?>> getProperties() {
        return new ArrayList(
                Arrays.asList(new Property[]{
            primordialUuidProperty(),
            nidProperty(),
            stateProperty(), timeProperty(), authorNidProperty(), moduleNidProperty(), pathNidProperty(),
            commitStateProperty()}));
    }

    protected abstract List<Property<?>> getEditableProperties2();

    @Override
    public final List<Property<?>> getEditableProperties() {
        ArrayList<Property<?>> propertyList = new ArrayList<>();
        propertyList.add(stateProperty());
        propertyList.addAll(getEditableProperties2());
        propertyList.addAll(Arrays.asList(new Property<?>[]{
            moduleNidProperty(), pathNidProperty()
        }));
        return propertyList;
    }

    @Override
    public final int getStampSequence() {
        if (this.stampSequenceProperty != null) {
            return this.stampSequenceProperty.get();
        }
        if (this.stampedVersionProperty != null && this.stampedVersionProperty.get() != null) {
            return this.stampedVersionProperty.get().getStampSequence();
        }
        return Integer.MAX_VALUE;
    }

    public VersionImpl getStampedVersion() {
        return stampedVersionProperty.get();
    }

    @Override
    public final Status getStatus() {
        if (this.stateProperty != null) {
            return this.stateProperty.get();
        }
        if (this.stampedVersionProperty != null) {
            return this.stampedVersionProperty.get().getStatus();
        }
        return Status.PRIMORDIAL;
    }

    public final void setStatus(Status state) {
        setStatus(state, null);
    }
    
    @Override
    public final void setStatus(Status state, Transaction t) {
        if (this.stampedVersionProperty == null) {
            this.stateProperty();
        }
        if (this.stateProperty != null) {
            this.stateProperty.set(state);
        }
        if (this.stampedVersionProperty != null) {
            this.stampedVersionProperty.get().setStatus(state, t);
        }

    }

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


    @Override
    public void setTime(long time, Transaction t) {
        if (this.stampedVersionProperty == null) {
            throw new IllegalStateException();
        }
        if (this.timeProperty != null) {
            this.timeProperty.set(time);
        }

        this.stampedVersionProperty.get().setTime(time, t);

        if (this.commitStateBinding != null) {
            this.commitStateBinding.invalidate();
        }
    }

    @Override
    public boolean isUncommitted() {
        return getCommitState() == CommitStates.UNCOMMITTED;
    }

    @Override
    public List<UUID> getUuidList() {
        if (this.stampedVersionProperty == null || this.stampedVersionProperty.get() == null) {
            return Arrays.asList(this.primordialUuidProperty.get());
        }
        return ((VersionImpl) this.stampedVersionProperty.get()).getUuidList();
    }

    @Override
    public final VersionType getSemanticType() {
        return this.versionType;
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    @Override
    public boolean deepEquals(Object other) {
        if (other instanceof ObservableVersionImpl) {
            ObservableVersionImpl otherObservable = (ObservableVersionImpl) other;
            return this.getStampedVersion().deepEquals(otherObservable.getStampedVersion());
        }
        return this.getStampedVersion().deepEquals(other);
    }
}
