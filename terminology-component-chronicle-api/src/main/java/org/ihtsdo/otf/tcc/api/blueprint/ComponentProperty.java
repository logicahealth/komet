/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.api.blueprint;

   /**
     * The Enum ComponentProperty lists the available properties that can be
     * associated with a refex member.
     */
public enum ComponentProperty {

   /**
    * Identifier of the member, either represented as a UUID or a nid.
    */
   COMPONENT_ID, ENCLOSING_CONCEPT_ID,

   /**
    * An identifier to the refex extension to which this member belongs.
    */
   REFEX_EXTENSION_ID,

   /**
    * Identifier of the referenced component&mdash;the component this refex extends&mdash;
    * either represented as a UUID or a nid.
    */
   REFERENCED_COMPONENT_ID,

   /**
    * Identifier of the status concept for this refex version,
    * either represented as a UUID or a nid.
    */
   TIME_IN_MS, AUTHOR_ID, MODULE_ID, PATH_ID,
   
   /**
    * Enumerated status type. 
    */
   STATUS,

   /**
    *
    */
   COMPONENT_EXTENSION_1_ID,

   /**
    *
    */
   COMPONENT_EXTENSION_2_ID,

   /**
    *
    */
   COMPONENT_EXTENSION_3_ID,

      /**
         * The boolean value associated with this refex member.
         */
   BOOLEAN_EXTENSION_1,

       /**
         * The integer value associated with this refex member.
         */
    INTEGER_EXTENSION_1,

       /**
         * The string value associated with this refex member.
         */
    STRING_EXTENSION_1,

        /**
         * The long value associated with this refex member.
         */
   LONG_EXTENSION_1,

       /**
         * The float value associated with this refex member.
         */
   FLOAT_EXTENSION_1,

        /**
         * The array bytearray value associated with this refex member.
         */
   ARRAY_OF_BYTEARRAY
}



 
