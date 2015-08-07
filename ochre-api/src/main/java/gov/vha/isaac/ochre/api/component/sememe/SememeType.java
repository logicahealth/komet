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
package gov.vha.isaac.ochre.api.component.sememe;

import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableComponentNidSememe;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableDescriptionSememe;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableSememeVersion;

/**
 *
 * @author kec
 */
public enum SememeType {

    MEMBER((byte) 0),
    COMPONENT_NID((byte) 1),
    LONG((byte) 2),
    LOGIC_GRAPH((byte) 4),
    STRING((byte) 5),
    DYNAMIC((byte) 6),
    DESCRIPTION((byte) 7),
    RELATIONSHIP_ADAPTOR((byte) 8);

    final byte sememeToken;

    private SememeType(byte sememeToken) {
        this.sememeToken = sememeToken;
    }

    @SuppressWarnings("rawtypes")
	public Class<? extends SememeVersion> getSememeVersionClass() {
        switch (this) {
            case COMPONENT_NID:
                return (Class<? extends SememeVersion>)ComponentNidSememe.class;
            case DESCRIPTION:
                return (Class<? extends SememeVersion>)DescriptionSememe.class;
            case MEMBER:
                return (Class<? extends SememeVersion>)SememeVersion.class;
            case DYNAMIC:
                return (Class<? extends SememeVersion>)DynamicSememe.class;
            case LOGIC_GRAPH:
                return (Class<? extends SememeVersion>)LogicGraphSememe.class;
            case LONG:
                return (Class<? extends SememeVersion>)LongSememe.class;
            case STRING:
                return (Class<? extends SememeVersion>)StringSememe.class;
            default:
                throw new RuntimeException("Can't handle: " + this);
        }
    }

    @SuppressWarnings("rawtypes")
	public Class<? extends ObservableSememeVersion> getObservableSememeVersionClass() {
        switch (this) {
            case COMPONENT_NID:
                return (Class<? extends ObservableSememeVersion>)ObservableComponentNidSememe.class;
            case DESCRIPTION:
                return (Class<? extends ObservableSememeVersion>)ObservableDescriptionSememe.class;
            case MEMBER:
                return (Class<? extends ObservableSememeVersion>)ObservableSememeVersion.class;
            case DYNAMIC:
            //TODO
            case LOGIC_GRAPH:
            //TODO
            case LONG:
            //TODO
            case STRING:
            //TODO
            default:
                throw new RuntimeException("Can't handle: " + this);
        }
    }

    public byte getSememeToken() {
        return sememeToken;
    }

    public static SememeType getFromToken(byte token) {
        switch (token) {
            case 0:
                return MEMBER;
            case 1:
                return COMPONENT_NID;
            case 2:
                return LONG;
            case 4:
                return LOGIC_GRAPH;
            case 5:
                return STRING;
            case 6:
                return DYNAMIC;
            case 7:
                return DESCRIPTION;
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }
    }
}
