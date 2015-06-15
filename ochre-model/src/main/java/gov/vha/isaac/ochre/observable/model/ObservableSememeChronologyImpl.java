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
package gov.vha.isaac.ochre.observable.model;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.observable.model.version.ObservableDescriptionImpl;
import gov.vha.isaac.ochre.observable.model.version.ObservableSememeVersionImpl;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author kec
 * @param <OV>
 * @param <C>
 */
public class ObservableSememeChronologyImpl<
        OV extends ObservableSememeVersionImpl, 
        C extends SememeChronology<?>>
    extends ObservableChronologyImpl<OV, C> 

    implements ObservableSememeChronology<OV> {
 
    private IntegerProperty sememeSequenceProperty;
    private IntegerProperty assemblageSequenceProperty;
    private IntegerProperty referencedComponentNidProperty;

    public ObservableSememeChronologyImpl(C chronicledObjectLocal) {
        super(chronicledObjectLocal);
    }

    @Override
    protected ObservableList<? extends OV> getObservableVersionList() {
        ObservableList<OV> observableList = FXCollections.observableArrayList();
        chronicledObjectLocal.getVersionList().stream().forEach((sememeVersion) -> {
            
            observableList.add(wrapInObservable(sememeVersion));
        });
        return observableList;
    }


    @Override
    public int getSememeSequence() {
        if (sememeSequenceProperty != null) {
            return sememeSequenceProperty.get();
        }
        return chronicledObjectLocal.getSememeSequence();
    }

    @Override
    public IntegerProperty sememeSequenceProperty() {
        if (sememeSequenceProperty == null) {
            sememeSequenceProperty = new CommitAwareIntegerProperty(this,
                    ObservableFields.SEMEME_SEQUENCE_FOR_CHRONICLE.toExternalString(),
                    getSememeSequence());
        }
        return sememeSequenceProperty;
    }

    @Override
    public int getAssemblageSequence() {
        if (assemblageSequenceProperty != null) {
            return assemblageSequenceProperty.get();
        }
        return chronicledObjectLocal.getAssemblageSequence();
    }
    @Override
    public IntegerProperty assemblageSequenceProperty() {
        if (assemblageSequenceProperty == null) {
            assemblageSequenceProperty = new CommitAwareIntegerProperty(this,
                    ObservableFields.ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE.toExternalString(),
                    getAssemblageSequence());
        }
        return assemblageSequenceProperty;
    }
    
    @Override
    public int getReferencedComponentNid() {
        if (referencedComponentNidProperty != null) {
            return referencedComponentNidProperty.get();
        }
        return chronicledObjectLocal.getReferencedComponentNid();
    }

    @Override
    public IntegerProperty referencedComponentNidProperty() {
        if (referencedComponentNidProperty == null) {
            referencedComponentNidProperty = new CommitAwareIntegerProperty(this,
                    ObservableFields.REFERENCED_COMPONENT_NID_FOR_SEMEME_CHRONICLE.toExternalString(),
                    getReferencedComponentNid());
        }
        return referencedComponentNidProperty;
    }
    
    private <M extends OV> Class getSvForOv(Class<M> type) {
        if (type.isAssignableFrom(ObservableDescriptionImpl.class)) {
            return DescriptionSememe.class;
        }
        throw new UnsupportedOperationException("Can't convert " + type);
    }

    private OV wrapInObservable(SememeVersion sememeVersion) {
        if (DescriptionSememe.class.isAssignableFrom(sememeVersion.getClass())) {
            return (OV) new ObservableDescriptionImpl((DescriptionSememeImpl) sememeVersion, 
                    (ObservableSememeChronology) this);
        }
        throw new UnsupportedOperationException("Can't convert " + sememeVersion);
    }

    @Override
    public <M extends OV> M createMutableVersion(Class<M> type, State status, EditCoordinate ec) {
        return (M) wrapInObservable(chronicledObjectLocal.createMutableVersion(getSvForOv(type), status, ec));
    }

    @Override
    public <M extends OV> M createMutableVersion(Class<M> type, int stampSequence) {
        return (M) wrapInObservable(chronicledObjectLocal.createMutableVersion(getSvForOv(type), stampSequence));
    }

    @Override
    public SememeType getSememeType() {
        return chronicledObjectLocal.getSememeType();
    }
}
