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
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

/**
 *
 * @author kec
 */
public class UuidFactory {

    /**
     * Gets the uuid of a component from the specified alternate identifier of the same component.
     *
     * @param authorityUuid the uuid representing the authoring associated with the alternate id
     * @param altId a string representation of the alternate id
     * @return the uuid of the specified component
     */
    public static UUID getUuidFromAlternateId(UUID authorityUuid, String altId) {
        try {
            if (authorityUuid.equals(TermAux.SNOMED_IDENTIFIER.getUuids()[0])) {
                return UuidT3Generator.fromSNOMED(altId);
            }
            return UuidT5Generator.get(authorityUuid, altId);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
