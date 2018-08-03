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
package sh.isaac.model.observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.observable.version.ObservableComponentNidVersionImpl;
import sh.isaac.model.observable.version.ObservableDescriptionVersionImpl;

/**
 *
 * @author kec
 */
public class ObservableDescriptionDialect implements ObservableVersion {
    SimpleObjectProperty<ObservableDescriptionVersionImpl> descriptionProperty = 
            new SimpleObjectProperty(
                 this,
                 ObservableFields.DESCRIPTION_DIALECT_DESCRIPTION.toExternalString(),
                 null);
    SimpleObjectProperty<ObservableComponentNidVersionImpl> dialectProperty = 
            new SimpleObjectProperty(
                 this,
                 ObservableFields.DESCRIPTION_DIALECT_DIALECT.toExternalString(),
                 null);
    
    public ObservableDescriptionDialect(ObservableDescriptionVersionImpl description,
            ObservableComponentNidVersionImpl dialect) {
        this.descriptionProperty.set(description);
        this.dialectProperty.set(dialect);
    }
    
    public ObservableDescriptionDialect(UUID conceptUuid, int assemblageNid) {
        ObservableDescriptionVersionImpl description = new ObservableDescriptionVersionImpl(UUID.randomUUID(), conceptUuid, assemblageNid);
        ObservableComponentNidVersionImpl dialect = new ObservableComponentNidVersionImpl(UUID.randomUUID(), description.getPrimordialUuid(), TermAux.US_DIALECT_ASSEMBLAGE.getNid());
        this.descriptionProperty.set(description);
        this.dialectProperty.set(dialect);
    }
    
    @Override
    public List<Property<?>> getEditableProperties() {
        List<Property<?>> list = new ArrayList<>();
        list.add(descriptionProperty.get().textProperty());
        list.add(descriptionProperty.get().languageConceptNidProperty());
        list.add(descriptionProperty.get().descriptionTypeConceptNidProperty());
        list.add(descriptionProperty.get().caseSignificanceConceptNidProperty());
        list.add(dialectProperty.get().assemblageNidProperty());
        list.add(dialectProperty.get().componentNidProperty());
        
        return list;
    }    
    
    public ObservableDescriptionVersionImpl getDescription() {
        return descriptionProperty.get();
    }
    public ObservableComponentNidVersionImpl getDialect() {
        return dialectProperty.get();
    }

    @Override
    public IntegerProperty authorNidProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectProperty<CommitStates> commitStateProperty() {
        return descriptionProperty.get().commitStateProperty();
    }

    @Override
    public IntegerProperty moduleNidProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IntegerProperty pathNidProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReadOnlyIntegerProperty stampSequenceProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectProperty<Status> stateProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LongProperty timeProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObservableChronology getChronology() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ReadOnlyProperty<?>> getProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> Optional<T> getUserObject(String objectKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putUserObject(String objectKey, Object object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> Optional<T> removeUserObject(String objectKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Chronology createIndependentChronicle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VersionType getSemanticType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addAdditionalUuids(UUID... uuids) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setStatus(Status state) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAuthorNid(int authorSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setModuleNid(int moduleSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPathNid(int pathSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getAuthorNid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getModuleNid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getPathNid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getStampSequence() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Status getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getTime() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<UUID> getUuidList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CommitStates getCommitState() {
        return descriptionProperty.get().getCommitState();
    }

}
