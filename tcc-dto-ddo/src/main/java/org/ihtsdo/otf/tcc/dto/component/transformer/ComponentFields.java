/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.dto.component.transformer;

/**
 *
 * @author kec
 */
public enum ComponentFields {
    // REVISION_FIELDS
        STATUS_UUID,
        TIME,
        AUTHOR_UUID,
        MODULE_UUID,
        PATH_UUID,
    
    // COMPONENT FIELDS
        PRIMORDIAL_UUID,
        ADDITIONAL_UUIDS,
        
    // ATTRIBUTE FIELDS
        ATTRIBUTE_DEFINED,
        
    // DESCRIPTION FIELDS
        DESCRIPTION_ENCLOSING_CONCEPT_UUID, //TODO, LEGACY FIELD, DEPRECATE/REMOVE 
        DESCRIPTION_INITIAL_CASE_SIGNIFICANT, 
        DESCRIPTION_LANGUAGE,
        DESCRIPTION_TEXT,
        DESCRIPTION_TYPE_UUID,
        
   // IDENTIFIER FIELDS
        ID_AUTHORITY_UUID,
        ID_LONG_DENOTATION,
        ID_STRING_DENOTATION,
        ID_UUID_DENOTATION,
        
   // MEDIA FIELDS
        MEDIA_ENCLOSING_CONCEPT_UUID, //TODO, LEGACY FIELD, DEPRECATE/REMOVE 
        MEDIA_DATA,
        MEDIA_FORMAT,
        MEDIA_TEXT_DESCRIPTION,
        MEDIA_TYPE_UUID,
        
   // REFEX FIELDS
        REFEX_ARRAY_OF_BYTEARRAY,
        REFEX_COLLECTION_UUID,
        REFEX_REFERENCED_COMPONENT_UUID,
        REFEX_COMPONENT_1_UUID,
        REFEX_COMPONENT_2_UUID,
        REFEX_COMPONENT_3_UUID,
        REFEX_BOOLEAN1,
        REFEX_INTEGER1,
        REFEX_STRING1,
        REFEX_LONG1,
        REFEX_FLOAT1,
        
   // RELATIONSHIP FIELDS
        RELATIONSHIP_ORIGIN_UUID, //TODO, LEGACY FIELD, DEPRECATE/REMOVE
        RELATIONSHIP_TYPE_UUID,
        RELATIONSHIP_DESTINATION_UUID,
        RELATIONSHIP_CHARACTERISTIC_UUID,
        RELATIONSHIP_REFINABILITY_UUID,
        RELATIONSHIP_GROUP,
        
   // CONCEPT FIELDS
        ANNOTATION_REFEX, 
        ANNOTATION_INDEX_REFEX,
        
    
}
