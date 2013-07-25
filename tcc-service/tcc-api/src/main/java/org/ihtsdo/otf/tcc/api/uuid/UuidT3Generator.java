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
package org.ihtsdo.otf.tcc.api.uuid;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;


/**
 * The Class UuidT3Generator generates a type 3 UUID object. A type 3 UUID is
 * name based and uses MD5 hashing to create the uuid from the given name. This
 * generator should only be used for SNOMED Ids, all other users should use
 * <code>UuidT5Generator</code>
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Universally_unique_identifier">http://en.wikipedia.org/wiki/Universally_unique_identifier</a>
 */
public class UuidT3Generator {

     /**
     * The the encoding string.
     */
    public static final String encoding = "8859_1";

    /**
     * Generates a type 3 UUID from the given string representing a SNOMED id.
     *
     * @param id a String representation of a SNOMED id
     * @return the generated uuid
     */
    public static UUID fromSNOMED(String id) {
        String name = "org.snomed." + id;
        try {
            return UUID.nameUUIDFromBytes(name.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a type 3 UUID from the given SNOMED id.
     *
     * @param id the SNOMED id
     * @return the generated uuid
     */
    public static UUID fromSNOMED(long id) {
        return fromSNOMED(Long.toString(id));
    }

    /**
     * Generates a type 3 UUID from the given SNOMED id.
     *
     * @param id the SNOMED id
     * @return the generated uuid
     */
    public static UUID fromSNOMED(Long id) {
        return fromSNOMED(id.toString());
    }

}
