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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronologyImpl;
import gov.vha.isaac.ochre.model.OchreExternalizable;
import gov.vha.isaac.ochre.model.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LongSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;

import java.util.UUID;

/**
 *
 * @author kec
 * @param <V>
 */
public class SememeChronologyImpl<V extends SememeVersionImpl<V>> extends ObjectChronologyImpl<V> 
        implements SememeChronology<V>, OchreExternalizable {

    byte sememeTypeToken = -1;
    int assemblageSequence = -1;
    int referencedComponentNid = Integer.MAX_VALUE;

    public SememeChronologyImpl(ByteArrayDataBuffer data) {
        super(data);
        sememeTypeToken = data.getByte();
        assemblageSequence = data.getConceptSequence();
        referencedComponentNid = data.getNid();
        if (data.isExternalData()) {
            constructorEnd(data);
        }
    }

    public SememeChronologyImpl(SememeType sememeType,
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
    public byte getDataFormatVersion() {
        return 0;
    }

    @Override
    public OchreExternalizableObjectType getOchreObjectType() {
        return OchreExternalizableObjectType.SEMEME;
    }

    @Override
    public void putExternal(ByteArrayDataBuffer out) {
        TODO
        super.putExternal(out); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeChronicleData(ByteArrayDataBuffer data) {
        super.writeChronicleData(data);
        data.putByte(sememeTypeToken);
        data.putConceptSequence(assemblageSequence);
        data.putNid(referencedComponentNid);
    }

    @Override
    public SememeType getSememeType() {
        return SememeType.getFromToken(sememeTypeToken);
    }

    @Override
    protected V makeVersion(int stampSequence, ByteArrayDataBuffer db) {
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
    public <M extends V> M createMutableVersion(Class<M> type, int stampSequence) {
        M version = createMutableVersionInternal(type, stampSequence,
                nextVersionSequence());
        addVersion(version);
        return version;
    }

    @Override
    public <M extends V> M createMutableVersion(Class<M> type, State status, EditCoordinate ec) {
        int stampSequence = Get.commitService().getStampSequence(status, Long.MAX_VALUE,
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
                    return (M) new ComponentNidSememeImpl((SememeChronologyImpl<ComponentNidSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case LONG:
                if (LongSememe.class.isAssignableFrom(type)) {
                    return (M) new LongSememeImpl((SememeChronologyImpl<LongSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case DYNAMIC:
                if (MutableDynamicSememe.class.isAssignableFrom(type)) {
                    return (M) new DynamicSememeImpl((SememeChronologyImpl<DynamicSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;
            case LOGIC_GRAPH:
                if (MutableLogicGraphSememe.class.isAssignableFrom(type)) {
                    return (M) new LogicGraphSememeImpl((SememeChronologyImpl<LogicGraphSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;

            case STRING:
                if (StringSememe.class.isAssignableFrom(type)) {
                    return (M) new StringSememeImpl((SememeChronologyImpl<StringSememeImpl>) this,
                            stampSequence, versionSequence);
                }
                break;

            case MEMBER:
                if (SememeVersion.class.isAssignableFrom(type)) {
                    return (M) new SememeVersionImpl(this,
                            stampSequence, versionSequence);
                }
                break;
            case DESCRIPTION:
                if (DescriptionSememe.class.isAssignableFrom(type)) {
                    return (M) new DescriptionSememeImpl((SememeChronologyImpl<DescriptionSememeImpl>) this,
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

    public static SememeVersionImpl<?> createSememe(byte token, SememeChronologyImpl<?> container,
            int stampSequence, short versionSequence, ByteArrayDataBuffer bb) {

        SememeType st = SememeType.getFromToken(token);
        switch (st) {
            case MEMBER:
                return (SememeVersionImpl<?>)new SememeVersionImpl<>(container, stampSequence, versionSequence);
            case COMPONENT_NID:
                return (SememeVersionImpl<?>)new ComponentNidSememeImpl((SememeChronologyImpl<ComponentNidSememeImpl>)container, stampSequence, versionSequence, bb);
            case LONG:
                return (SememeVersionImpl<?>)new LongSememeImpl((SememeChronologyImpl<LongSememeImpl>)container, stampSequence, versionSequence, bb);
            case LOGIC_GRAPH:
                return (SememeVersionImpl<?>)new LogicGraphSememeImpl((SememeChronologyImpl<LogicGraphSememeImpl>)container, stampSequence, versionSequence, bb);
            case DYNAMIC:
                return (SememeVersionImpl<?>)new DynamicSememeImpl((SememeChronologyImpl<DynamicSememeImpl>)container, stampSequence, versionSequence, bb);
            case STRING:
                return (SememeVersionImpl<?>)new StringSememeImpl((SememeChronologyImpl<StringSememeImpl>)container, stampSequence, versionSequence, bb);
            case DESCRIPTION:
                return (SememeVersionImpl<?>)(new DescriptionSememeImpl((SememeChronologyImpl<DescriptionSememeImpl>)container, stampSequence, versionSequence, bb));
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SememeChronology{")
                .append(SememeType.getFromToken(sememeTypeToken))
                .append("\n assemblage:")
                .append(Get.conceptDescriptionText(assemblageSequence))
                .append(" <")
                .append(assemblageSequence)
                .append(">\n rc:");
        switch (Get.identifierService().getChronologyTypeForNid(referencedComponentNid)) {
            case CONCEPT:
                builder.append("CONCEPT: ").append(Get.conceptDescriptionText(referencedComponentNid));
                break;
            case SEMEME:
                builder.append("SEMEME: ").append(Get.sememeService().getSememe(referencedComponentNid));
                break;
            default:
                builder.append(Get.identifierService().getChronologyTypeForNid(referencedComponentNid)).append(" ").append(referencedComponentNid);
        }
        builder.append(" <")
                .append(referencedComponentNid)
                .append(">\n ");
        super.toString(builder);
        builder.append('}');

        return builder.toString();
    }

}
