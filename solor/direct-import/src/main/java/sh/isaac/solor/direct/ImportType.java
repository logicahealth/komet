/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.solor.direct;

/**
 *
 * @author kec
 */
public enum ImportType {
    FULL, SNAPSHOT, ACTIVE_ONLY, DELTA;

    /**
     * @param stringValue
     * @return the Matching import type, or a runtime exception if unmatchable
     */
    public static ImportType parseFromString(String stringValue) {
        if (stringValue.toLowerCase().equals(FULL.name().toLowerCase())) {
            return FULL;
        }
        else if (stringValue.toLowerCase().equals(SNAPSHOT.name().toLowerCase())) {
            return SNAPSHOT;
        }
        //Provide lots of parse options, because this is a classifier a user can hand enter when configuring the conversion...
        else if (stringValue.toLowerCase().equals(ACTIVE_ONLY.name().toLowerCase()) 
                || stringValue.toLowerCase().equals("active only")
                || stringValue.toLowerCase().equals("snapshot active only")
                || stringValue.toLowerCase().equals("snapshot_active_only")
                || stringValue.toLowerCase().equals("snapshot-active-only")) {
            return ACTIVE_ONLY;
        }
        else if (stringValue.toLowerCase().equals(DELTA.name().toLowerCase())) {
            return DELTA;
        }
        throw new IllegalArgumentException("No match for import type " + stringValue); 
    }
}
