/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.sememe;

import gov.vha.isaac.ochre.api.sememe.SememeType;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.sememe.SememeChronicle;
import gov.vha.isaac.ochre.api.sememe.version.MutableComponentNidSememe;
import gov.vha.isaac.ochre.api.sememe.version.MutableConceptSequenceSememe;
import gov.vha.isaac.ochre.api.sememe.version.MutableConceptSequenceTimeSememe;
import gov.vha.isaac.ochre.api.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.api.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronicleImpl;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.ConceptSequenceSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.ConceptSequenceTimeSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import java.util.UUID;

/**
 *
 * @author kec
 * @param <V>
 */
public class SememeChronicleImpl<V extends SememeVersionImpl> extends ObjectChronicleImpl<V> implements SememeChronicle<V> {
    
    byte sememeTypeToken;
    int assemblageSequence;
    int referencedComponentNid;

    public SememeChronicleImpl(DataBuffer data) {
        super(data);
        sememeTypeToken = data.getByte();
        assemblageSequence = data.getInt();
        referencedComponentNid = data.getInt();
        constructorEnd(data);
    }

    public SememeChronicleImpl(SememeType sememeType, 
            UUID primoridalUuid, 
            int nid, 
            int assemblageSequence, 
            int referencedComponentNid, 
            int containerSequence) {
        super(primoridalUuid, nid, containerSequence);
        this.sememeTypeToken = sememeType.getSememeToken();
        this.assemblageSequence = assemblageSequence;
        this.referencedComponentNid = referencedComponentNid;
    }
    
    @Override
    public void writeChronicleData(DataBuffer data) {
        super.writeChronicleData(data);
        data.putByte(sememeTypeToken);
        data.putInt(assemblageSequence);
        data.putInt(referencedComponentNid);
    }
    
    public SememeType getSememeType() {
        return SememeType.getFromToken(sememeTypeToken);
    }

    @Override
    protected V makeVersion(int stampSequence, DataBuffer db) {
        return (V) createSememe(sememeTypeToken, this, stampSequence, db);
    }

    @Override
    public int getSememeSequence() {
        return getContainerSequence();
    }

    @Override
    public int getAssemblageSequence() {
        return assemblageSequence;
    }

    @Override
    public <M extends V> M createMutableVersion(Class<M> type, State status, EditCoordinate ec) {
        M version = createMutableVersionInternal(type, status, ec);
        addVersion(version);
        return version;
    }

    protected <M extends V> M createMutableVersionInternal(Class<M> type, State status, EditCoordinate ec) throws UnsupportedOperationException {
        switch (getSememeType()) {
            case COMPONENT_NID:
                if (MutableComponentNidSememe.class.isAssignableFrom(type)) {
                    return (M) new ComponentNidSememeImpl((SememeChronicleImpl<ComponentNidSememeImpl>) this,
                            status,
                            Long.MAX_VALUE,
                            ec.getAuthorSequence(),
                            ec.getModuleSequence(), 
                            ec.getPathSequence());
                }
                break;
            case CONCEPT_SEQUENCE:
                if (MutableConceptSequenceSememe.class.isAssignableFrom(type)) {
                    return (M) new ConceptSequenceSememeImpl((SememeChronicleImpl<? extends ConceptSequenceSememeImpl>) this,
                            status,
                            Long.MAX_VALUE,
                            ec.getAuthorSequence(),
                            ec.getModuleSequence(), 
                            ec.getPathSequence());
                }
                break;
            case CONCEPT_SEQUENCE_TIME:
                if (MutableConceptSequenceTimeSememe.class.isAssignableFrom(type)) {
                    return (M) new ConceptSequenceTimeSememeImpl((SememeChronicleImpl<ConceptSequenceTimeSememeImpl>) this,
                            status,
                            Long.MAX_VALUE,
                            ec.getAuthorSequence(),
                            ec.getModuleSequence(), 
                            ec.getPathSequence());
                }
                break;
            case DYNAMIC:
                if (MutableDynamicSememe.class.isAssignableFrom(type)) {
                    return (M) new DynamicSememeImpl((SememeChronicleImpl<DynamicSememeImpl>) this,
                            status,
                            Long.MAX_VALUE,
                            ec.getAuthorSequence(),
                            ec.getModuleSequence(), 
                            ec.getPathSequence());
                }
                break;
            case LOGIC_GRAPH:
                if (MutableLogicGraphSememe.class.isAssignableFrom(type)) {
                    return (M) new LogicGraphSememeImpl((SememeChronicleImpl<LogicGraphSememeImpl>) this,
                            status,
                            Long.MAX_VALUE,
                            ec.getAuthorSequence(),
                            ec.getModuleSequence(), 
                            ec.getPathSequence());
                }
                break;
                
            case MEMBER:
                if (SememeVersion.class.isAssignableFrom(type)) {
                    return (M) new SememeVersionImpl(this,
                            status,
                            Long.MAX_VALUE,
                            ec.getAuthorSequence(),
                            ec.getModuleSequence(), 
                            ec.getPathSequence());
                }
                break;
            default:
                throw new UnsupportedOperationException("Can't handle: " + getSememeType()); 
        }
        throw new UnsupportedOperationException("Chronicle is of type: " +
                getSememeType() + " cannot create version of type: " + type.getCanonicalName());
    }

    @Override
    public int getReferencedComponentNid() {
        return referencedComponentNid;
    }
    

    public static SememeVersionImpl createSememe(byte token, SememeChronicleImpl container, 
            int stampSequence, DataBuffer bb) {
        
        SememeType st = SememeType.getFromToken(token);
        switch (st) {
            case MEMBER:
                return new SememeVersionImpl(container, stampSequence, bb);
            case COMPONENT_NID:
                return new ComponentNidSememeImpl(container, stampSequence, bb);
            case CONCEPT_SEQUENCE:
                return new ConceptSequenceSememeImpl(container, stampSequence, bb);
            case CONCEPT_SEQUENCE_TIME:
                return new ConceptSequenceSememeImpl(container, stampSequence, bb);
            case LOGIC_GRAPH:
                return new LogicGraphSememeImpl(container, stampSequence, bb);
            case DYNAMIC:
                return new DynamicSememeImpl(container, stampSequence, bb);
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }
        
    }
    
}
