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

/**
 *
 * @author kec
 */
public enum SememeType {

    MEMBER((byte) 0),
    COMPONENT_NID((byte) 1),
    CONCEPT_SEQUENCE((byte) 2),
    CONCEPT_SEQUENCE_TIME((byte) 3),
    LOGIC_GRAPH((byte) 4),
    STRING((byte) 5),
    DYNAMIC((byte) 6);

    final byte sememeToken;

    private SememeType(byte sememeToken) {
        this.sememeToken = sememeToken;
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
                return CONCEPT_SEQUENCE;
            case 3:
                return CONCEPT_SEQUENCE_TIME;
            case 4:
                return LOGIC_GRAPH;
            case 5:
                return STRING;
            case 6:
                return DYNAMIC;
            default:
                throw new UnsupportedOperationException("Can't handle: " + token);
        }
    }
}
