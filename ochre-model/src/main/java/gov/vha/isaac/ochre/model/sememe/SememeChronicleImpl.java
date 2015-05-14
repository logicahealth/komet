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

import gov.vha.isaac.ochre.api.IdentifiedObjectService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableConceptSequenceSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableConceptSequenceTimeSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronicleImpl;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.ConceptSequenceSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.ConceptSequenceTimeSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;
import java.util.UUID;

/**
 *
 * @author kec
 * @param <V>
 */
public class SememeChronicleImpl<V extends SememeVersionImpl> extends ObjectChronicleImpl<V> implements SememeChronology<V> {

    private static IdentifiedObjectService identifiedObjectService;

    private static IdentifiedObjectService getIdentifiedObjectService() {
        if (identifiedObjectService == null) {
            identifiedObjectService = LookupService.getService(IdentifiedObjectService.class);
        }
        return identifiedObjectService;
    }

    byte sememeTypeToken = -1;
    int assemblageSequence = -1;
    int referencedComponentNid = Integer.MAX_VALUE;

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

    @Override
    public SememeType getSememeType() {
        return SememeType.getFromToken(sememeTypeToken);
    }

    @Override
    protected V makeVersion(int stampSequence, DataBuffer db) {
        return (V) createSememe(sememeTypeToken, this, stampSequence, 
                db.getShort(), db);
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
    public <M extends V> M createMutableStampedVersion(Class<M> type, int stampSequence) {
        M version = createMutableVersionInternal(type, stampSequence, 
                nextVersionSequence());
        addVersion(version);
        return version;
    }

    @Override
    public <M extends V> M createMutableUncommittedVersion(Class<M> type, State status, EditCoordinate ec) {
        int stampSequence = getCommitService().getStamp(status, Long.MAX_VALUE,
                ec.getAuthorSequence(), ec.getModuleSequence(), ec.getPathSequence());
        M version = createMutableVersionInternal(type, stampSequence, 
                nextVersionSequence());
        addVersion(version);
        return version;
    }

    protected <M extends V> M createMutableVersionInternal(Class<M> type, int stampSequence, short versionSequence) throws UnsupportedOperationException {
        switch (getSememeType()) {
            case COMPONENT_NID:
                if (MutableComponentNidSememe.class.isAssignableFrom(type)) {
                    return (M) new ComponentNidSememeImpl((SememeChronicleImpl<ComponentNidSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case CONCEPT_SEQUENCE:
                if (MutableConceptSequenceSememe.class.isAssignableFrom(type)) {
                    return (M) new ConceptSequenceSememeImpl((SememeChronicleImpl<? extends ConceptSequenceSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case CONCEPT_SEQUENCE_TIME:
                if (MutableConceptSequenceTimeSememe.class.isAssignableFrom(type)) {
                    return (M) new ConceptSequenceTimeSememeImpl((SememeChronicleImpl<ConceptSequenceTimeSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case DYNAMIC:
                if (MutableDynamicSememe.class.isAssignableFrom(type)) {
                    return (M) new DynamicSememeImpl((SememeChronicleImpl<DynamicSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case LOGIC_GRAPH:
                if (MutableLogicGraphSememe.class.isAssignableFrom(type)) {
                    return (M) new LogicGraphSememeImpl((SememeChronicleImpl<LogicGraphSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;

            case STRING:
                if (StringSememe.class.isAssignableFrom(type)) {
                    return (M) new StringSememeImpl((SememeChronicleImpl<StringSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;

            case MEMBER:
                if (SememeVersion.class.isAssignableFrom(type)) {
                    return (M) new SememeVersionImpl(this,
                            stampSequence, versionSequence);
                }
                break;
            default:
                throw new UnsupportedOperationException("Can't handle: " + getSememeType());
        }
        throw new UnsupportedOperationException("Chronicle is of type: "
                + getSememeType() + " cannot create version of type: " + type.getCanonicalName());
    }

    @Override
    public int getReferencedComponentNid() {
        return referencedComponentNid;
    }

    public static SememeVersionImpl createSememe(byte token, SememeChronicleImpl container,
            int stampSequence, short versionSequence, DataBuffer bb) {

        SememeType st = SememeType.getFromToken(token);
        switch (st) {
            case MEMBER:
                return new SememeVersionImpl(container, stampSequence, versionSequence);
            case COMPONENT_NID:
                return new ComponentNidSememeImpl(container, stampSequence, versionSequence, bb);
            case CONCEPT_SEQUENCE:
                return new ConceptSequenceSememeImpl(container, stampSequence, versionSequence, bb);
            case CONCEPT_SEQUENCE_TIME:
                return new ConceptSequenceSememeImpl(container, stampSequence, versionSequence, bb);
            case LOGIC_GRAPH:
                return new LogicGraphSememeImpl(container, stampSequence, versionSequence, bb);
            case DYNAMIC:
                return new DynamicSememeImpl(container, stampSequence, versionSequence, bb);
            case STRING:
                return new StringSememeImpl(container, stampSequence, versionSequence, bb);
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SememeChronicleImpl{sememeType=")
                .append(SememeType.getFromToken(sememeTypeToken))
                .append(", assemblageSequence=")
                .append(assemblageSequence)
                .append(" (")
                .append(getIdentifiedObjectService().informAboutObject(assemblageSequence))
                .append("), referencedComponentNid=")
                .append(referencedComponentNid)
                .append(" (")
                .append(getIdentifiedObjectService().informAboutObject(referencedComponentNid))
                .append(")\n ");
        super.toString(builder);
        builder.append('}');

        return builder.toString();
    }

}
