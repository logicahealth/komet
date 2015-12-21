/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.observable;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.commit.CommittableComponent;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.observable.ObservableChronology;
import gov.vha.isaac.ochre.api.observable.ObservableChronologyService;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableSememeVersion;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import gov.vha.isaac.ochre.model.ObjectVersionImpl;
import gov.vha.isaac.ochre.model.observable.version.ObservableVersionImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.mahout.math.map.OpenShortObjectHashMap;

/**
 *
 * @author kec
 * @param <OV> ofType of the observable version
 * @param <C>  ofType of the unobservable (base) chronicle
 */
public abstract class ObservableChronologyImpl<
        OV extends ObservableVersionImpl, 
        C extends ObjectChronology<?>>
        implements ObservableChronology<OV>, ChronologyChangeListener, CommittableComponent {
    
    private static final ObservableChronologyService ocs = 
            LookupService.getService(ObservableChronologyService.class);

    
    private ListProperty<? extends OV> versionListProperty;
    private IntegerProperty nidProperty;
    private ObjectProperty<UUID> primordialUuidProperty;
    private ListProperty<UUID> uuidListProperty;
    private ObjectProperty<CommitStates> commitStateProperty;
    private ListProperty<ObservableSememeChronology<? extends ObservableSememeVersion<?>>> sememeListProperty;
    private ObservableList<? extends OV> versionList = null;

    protected C chronicledObjectLocal;

    public ObservableChronologyImpl(C chronicledObjectLocal) {
        this.chronicledObjectLocal = chronicledObjectLocal;
    }
    
    protected final void updateChronicle(C chronicledObjectLocal) {
        C oldChronicle = this.chronicledObjectLocal;
        this.chronicledObjectLocal = chronicledObjectLocal;
        if (versionList != null) {
            OpenShortObjectHashMap<OV> observableVersionMap = 
                    new OpenShortObjectHashMap<>(versionList.size());
            versionList.stream().forEach((ov)-> 
                    observableVersionMap.put(ov.getVersionSequence(), ov));
            
            chronicledObjectLocal.getVersionList().stream().forEach((sv) -> {
                OV observableVersion = observableVersionMap.get(((ObjectVersionImpl)sv).getVersionSequence());
                if (observableVersion == null) {
                    // add new version to list
                    
                } else {
                    
                }
            });
            
            
            // update versions...
            throw new UnsupportedOperationException();
        }
        // else, nothing to do, since no one is looking...
    };
    
    @Override
    public final void handleChange(SememeChronology<? extends SememeVersion<?>> sc) {
        if (this.getNid() == sc.getNid()) {
            updateChronicle((C) sc);
        }
        if (sc.getReferencedComponentNid() == this.getNid()) {
            if (sememeListProperty != null) {
                // check to be sure sememe is in list, if not, add it. 
                if (sememeListProperty.get().stream().noneMatch(
                        (element) -> element.getNid() == sc.getNid())) {
                    sememeListProperty.get().add((ObservableSememeChronology<? extends ObservableSememeVersion<?>>)ocs.getObservableSememeChronology(sc.getNid()));
                }
            }
            // else, nothing to do, since no one is looking...
        }
    }
    @Override
    public final void handleChange(ConceptChronology<? extends StampedVersion> cc) {
        if (this.getNid() == cc.getNid()) {
            updateChronicle((C) cc);
            // update descriptions...
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        //TODO implement handle commit...
    }

    @Override
    public final ObservableList<? extends OV> getVersionList() {
        if (versionListProperty != null) {
            return versionListProperty.get();
        }
        if (versionList == null) {
            versionList = getObservableVersionList();
        }
        return versionList;
    }

    @Override
    public final ListProperty<? extends OV> versionListProperty() {
        if (versionListProperty == null) {
            versionListProperty = new SimpleListProperty<>(this,
                    ObservableFields.VERSION_LIST_FOR_CHRONICLE.toExternalString(),
                    getVersionList());
        }
        return versionListProperty;
    }

    protected abstract ObservableList<? extends OV> getObservableVersionList();

    @Override
    public final IntegerProperty nidProperty() {
        if (nidProperty == null) {
            nidProperty = new CommitAwareIntegerProperty(this,
                    ObservableFields.NATIVE_ID_FOR_CHRONICLE.toExternalString(),
                    getNid());
        }
        return nidProperty;
    }

    @Override
    public final ObjectProperty<UUID> primordialUuidProperty() {
        if (primordialUuidProperty == null) {
            primordialUuidProperty = new CommitAwareObjectProperty<>(this,
                    ObservableFields.PRIMORDIAL_UUID_FOR_CHRONICLE.toExternalString(),
                    getPrimordialUuid());
        }
        return primordialUuidProperty;
    }

    @Override
    public final ListProperty<UUID> uuidListProperty() {
        if (uuidListProperty == null) {
            uuidListProperty = new SimpleListProperty<>(this,
                    ObservableFields.UUID_LIST_FOR_CHRONICLE.toExternalString(),
                    FXCollections.observableList(getUuidList()));
        }
        return uuidListProperty;
    }

    @Override
    public final ObjectProperty<CommitStates> commitStateProperty() {
        if (commitStateProperty == null) {
            ObjectBinding<CommitStates> binding = new ObjectBinding<CommitStates>() {

                @Override
                protected CommitStates computeValue() {
                    if (getVersionList().stream().anyMatch((version) -> 
                            version.getCommitState() == CommitStates.UNCOMMITTED)) {
                        return CommitStates.UNCOMMITTED;
                    }
                    return CommitStates.COMMITTED;
                }
            };

            commitStateProperty = new SimpleObjectProperty(this,
                    ObservableFields.COMMITTED_STATE_FOR_CHRONICLE.toExternalString(),
                    binding.get());
            commitStateProperty.bind(binding);
        }
        return commitStateProperty;
    }

    @Override
    public final ListProperty<ObservableSememeChronology<? extends ObservableSememeVersion<?>>> sememeListProperty() {
        if (sememeListProperty == null) {
            ObservableList<ObservableSememeChronology<? extends ObservableSememeVersion>> sememeList = 
                    FXCollections.emptyObservableList();
            Get.sememeService().getSememeSequencesForComponent(getNid()).stream()
                    .forEach((sememeSequence) -> 
                            sememeList.add(ocs.getObservableSememeChronology(sememeSequence)));
            sememeListProperty = new SimpleListProperty(this,
                    ObservableFields.SEMEME_LIST_FOR_CHRONICLE.toExternalString(),
                    sememeList);
        }
        return sememeListProperty;
    }

    
    @Override
    public final ObservableList<? extends ObservableSememeChronology<? extends ObservableSememeVersion<?>>> getSememeList() {
        return sememeListProperty().get();
    }


    @Override
    public final IntStream getVersionStampSequences() {
        return chronicledObjectLocal.getVersionStampSequences();
    }

    @Override
    public final int getNid() {
        if (nidProperty != null) {
            return nidProperty.get();
        }
        return chronicledObjectLocal.getNid();
    }

    @Override
    public final String toUserString() {
        return chronicledObjectLocal.toUserString();
    }

    @Override
    public final UUID getPrimordialUuid() {
        if (primordialUuidProperty != null) {
            return primordialUuidProperty.get();
        }
        return chronicledObjectLocal.getPrimordialUuid();
    }

    @Override
    public final List<UUID> getUuidList() {
        return chronicledObjectLocal.getUuidList();
    }

    @Override
    public final CommitStates getCommitState() {
        if (commitStateProperty != null) {
            return commitStateProperty.get();
        }
        return chronicledObjectLocal.getCommitState();
    }

    @Override
    public final UUID getListenerUuid() {
        return getPrimordialUuid();
    }

    @Override
    public Optional<LatestVersion<OV>> getLatestVersion(Class<OV> type, StampCoordinate coordinate) {
       RelativePositionCalculator calculator = RelativePositionCalculator.getCalculator(coordinate);
       return calculator.getLatestVersion(this);
    }


    @Override
    public List<? extends ObservableSememeChronology<? extends SememeVersion<?>>> getSememeListFromAssemblage(int assemblageSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <SV extends ObservableSememeVersion> List<? extends ObservableSememeChronology<SV>> getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
