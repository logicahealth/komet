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

    MEMBER((byte) 0, "Member"),
    COMPONENT_NID((byte) 1, "Component Nid"),
    LONG((byte) 2, "Long"),
    LOGIC_GRAPH((byte) 4, "Logic Graph"),
    STRING((byte) 5, "String"),
    DYNAMIC((byte) 6, "Dynamic Sememe"),
    DESCRIPTION((byte) 7, "Description"),
    RELATIONSHIP_ADAPTOR((byte) 8, "Relationship Adapter"),
    UNKNOWN((byte)Byte.MAX_VALUE, "Unknown");

    final byte sememeToken;
    final String niceName_;

    private SememeType(byte sememeToken, String niceName) {
        this.sememeToken = sememeToken;
        this.niceName_ = niceName;
    }

    public Class getSememeVersionClass() {
        switch (this) {
            case COMPONENT_NID:
                return ComponentNidSememe.class;
            case DESCRIPTION:
                return DescriptionSememe.class;
            case MEMBER:
                return SememeVersion.class;
            case DYNAMIC:
                return DynamicSememe.class;
            case LOGIC_GRAPH:
                return LogicGraphSememe.class;
            case LONG:
                return LongSememe.class;
            case STRING:
                return StringSememe.class;
            default:
                throw new RuntimeException("Can't handle: " + this);
        }
    }

    public Class getObservableSememeVersionClass() {
        switch (this) {
            case COMPONENT_NID:
                return ObservableComponentNidSememe.class;
            case DESCRIPTION:
                return ObservableDescriptionSememe.class;
            case MEMBER:
                return ObservableSememeVersion.class;
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
    
    public static SememeType parse(String name)
    {
        for (SememeType ct : values())
        {
            if (ct.name().equals(name) || ct.niceName_.equals(name))
            {
                return ct;
            }
        }
        return UNKNOWN;
    }
}
