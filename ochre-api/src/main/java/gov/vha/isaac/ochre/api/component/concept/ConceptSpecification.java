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
package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.IdentifiedObject;

/**
 *
 * @author kec
 */
public interface ConceptSpecification extends IdentifiedObject {

    public static final String FIELD_SEPARATOR = "⦙";

    /**
     *
     * @return the sequence of the specified concept. Sequences are contiguously
     * assigned identifiers >= 0;
     */
    default int getConceptSequence() {
        return Get.identifierService().getConceptSequenceForUuids(getUuidList());
    }

    /**
     *
     * @return a text description for the specified concept.
     */
    String getConceptDescriptionText();

    /**
     * 
     * @return A string to specify a concept externally, including a description, followed by a FIELD_SEPARATOR, and the Uuids for this concept, each UUID also separated by a FIELD_SEPARATOR. 
     */
    default String toExternalString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getConceptDescriptionText());
        getUuidList().stream().forEach((uuid) -> {
            sb.append(FIELD_SEPARATOR).append(uuid.toString());
        });

        return sb.toString();

    }

}
