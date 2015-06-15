/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import static gov.vha.isaac.ochre.api.ConceptProxy.FIELD_SEPERATOR;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 *
 * @author kec
 */
public enum ObservableFields {

    AUTHOR_SEQUENCE_FOR_EDIT_COORDINATE("author sequence for edit coordinate"),
    MODULE_SEQUENCE_FOR_EDIT_COORDINATE("module sequence for edit coordinate"),
    PATH_SEQUENCE_FOR_EDIT_CORDINATE("path sequence for edit cordinate"),
    LANGUAGE_SEQUENCE_FOR_LANGUAGE_COORDINATE("language sequence for language coordinate"),
    DIALECT_ASSEMBLAGE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE("dialect assemblage sequence preference list for language coordinate"),
    DESCRIPTION_TYPE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE("description type sequence preference list for language coordinate"),
    STATED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE("stated assemblage sequence for logic coordinate"),
    INFERRED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE("inferred assemblage sequence for logic coordinate"),
    DESCRIPTION_LOGIC_PROFILE_SEQUENCE_FOR_LOGIC_COORDINATE("description logic profile sequence for logic coordinate"),
    CLASSIFIER_SEQUENCE_FOR_LOGIC_COORDINATE("classifier sequence for logic coordinate"),
    STAMP_PRECEDENCE_FOR_STAMP_COORDINATE("stamp precedence for stamp coordinate"),
    STAMP_POSITION_FOR_STAMP_COORDINATE("stamp position for stamp coordinate"),
    MODULE_SEQUENCE_ARRAY_FOR_STAMP_COORDINATE("module sequence array for stamp coordinate"),
    PATH_SEQUENCE_FOR_STAMP_PATH("path sequence for stamp path"),
    PATH_ORIGIN_LIST_FOR_STAMP_PATH("path origin list for stamp path"),
    TIME_FOR_STAMP_POSITION("time for stamp position"),
    PATH_SEQUENCE_FOR_STAMP_POSITION("path sequence for stamp position"),
    TAXONOMY_TYPE_FOR_TAXONOMY_COORDINATE("taxonomy type for taxonomy coordinate"),
    STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE("stamp coordinate for taxonomy coordinate"),
    LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE("language coordinate for taxonomy coordinate"),
    STATUS_FOR_VERSION("status for version"),
    TIME_FOR_VERSION("time for version"),
    AUTHOR_SEQUENCE_FOR_VERSION("author sequence for version"),
    MODULE_SEQUENCE_FOR_VERSION("module sequence for version"),
    PATH_SEQUENCE_FOR_VERSION("path sequence for version"),
    COMMITTED_STATE_FOR_VERSION("committed state for version"),
    STAMP_SEQUENCE_FOR_VERSION("stamp sequence for version"),
    CASE_SIGNIFICANCE_CONCEPT_SEQUENCE_FOR_DESCRIPTION("case significance concept sequence for description"),
    LANGUAGE_CONCEPT_SEQUENCE_FOR_DESCRIPTION("language concept sequence for description"),
    TEXT_FOR_DESCRIPTION("text for description"),
    DESCRIPTION_TYPE_FOR_DESCRIPTION("description type for description"),
    VERSION_LIST_FOR_CHRONICLE("version list for chronicle"),
    NATIVE_ID_FOR_CHRONICLE("native id for chronicle"),
    CONCEPT_SEQUENCE_FOR_CHRONICLE("concept sequence for chronicle"),
    SEMEME_SEQUENCE_FOR_CHRONICLE("sememe sequence for chronicle"),
    ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE("assemblage sequence for sememe chronicle"),
    REFERENCED_COMPONENT_NID_FOR_SEMEME_CHRONICLE("referenced component nid for sememe chronicle"),
    PRIMORDIAL_UUID_FOR_CHRONICLE("primordial UUID for chronicle"),
    UUID_LIST_FOR_CHRONICLE("UUID list for chronicle"),
    COMMITTED_STATE_FOR_CHRONICLE("committed state for chronicle"),
    SEMEME_LIST_FOR_CHRONICLE("sememe list for chronicle"),
    DESCRIPTION_LIST_FOR_CONCEPT("description list for concept");

    private static final UUID namespace = UUID.fromString("cbbd1e22-0cac-11e5-a6c0-1697f925ec7b");
    String description;

    ObservableFields(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String toExternalString() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);
        sb.append(FIELD_SEPERATOR).append(getUuid().toString());
        return sb.toString();
    }

    public UUID getUuid() {
        try {
            return UuidT5Generator.get(namespace, name());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
