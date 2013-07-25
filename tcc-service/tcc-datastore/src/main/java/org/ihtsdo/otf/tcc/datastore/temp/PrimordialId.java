/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.datastore.temp;

/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public enum PrimordialId {
    UNASSIGNED_ID(0, UUID.fromString("1c423bfd-147a-11db-ac5d-0800200c9a66")), 
    AUTHORITY_ID(1, UUID.randomUUID()), 
    ACE_AUXILIARY_ID(2, UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")), 
    CURRENT_ID(3, UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")), 
    ACE_AUX_ENCODING_ID(4, UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66")), 
    FULLY_SPECIFIED_DESCRIPTION_TYPE_ID(5, UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")), 
    XHTML_DEF_ID(6, UUID.fromString("5e1fe941-8faf-11db-b606-0800200c9a66")), 
    IS_A_REL_ID(7, UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66")), 
    DEFINING_CHARACTERISTIC_ID(8, UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66")), 
    NOT_REFINABLE_ID(9, UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66")), 
    PREFERED_TERM_ID(10, UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44")), 
    EXTENSION_TABLE_ID(11, UUID.fromString("0cbed8ca-650d-11dc-8314-0800200c9a66")), 
    STATED_CHARACTERISTIC_ID(12, UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d")), 
    INFERRED_CHARACTERISTIC_ID(13, UUID.fromString("d8fb4fb0-18c3-3352-9431-4919193f85bc"));
    

    private int sequenceRelativeId;
    private Collection<UUID> uids;

    private PrimordialId(int sequenceRelativeId, UUID uid) {
        this(sequenceRelativeId, Arrays.asList(new UUID[] { uid }));
    }

    private PrimordialId(int sequenceRelativeId, Collection<UUID> uids) {
        this.sequenceRelativeId = sequenceRelativeId;
        this.uids = uids;
    }

    public int getNativeId() {
        return Integer.MIN_VALUE + sequenceRelativeId;
    }

    public Collection<UUID> getUids() {
        return uids;
    }
}
