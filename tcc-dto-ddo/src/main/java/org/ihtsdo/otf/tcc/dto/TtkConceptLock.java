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
package org.ihtsdo.otf.tcc.dto;

import gov.vha.isaac.ochre.api.Get;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for locking, to prevent two threads processing same concept at the same
 * time, while allowing for concurrency. 
 * @author kec
 */
public class TtkConceptLock {
    private static final ReentrantLock[] locks = new ReentrantLock[256];

    static {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    protected static ReentrantLock getLock(int key) {
        return locks[((int) ((byte) key)) - Byte.MIN_VALUE];
    }

    public static ReentrantLock getLock(UUID... uuids) {
        return getLock(Get.identifierService().getNidForUuids(uuids));
    }
    public static ReentrantLock getLock(Collection<UUID> uuids) {
        return getLock(Get.identifierService().getNidForUuids(uuids.toArray(new UUID[uuids.size()])));
    }
}
