/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.chronicle;

import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.commit.IdentifiedStampedVersion;
import sh.isaac.api.coordinate.WriteCoordinate;

/**
 * @author kec
 */
public interface Version extends MutableStampedVersion, IdentifiedStampedVersion {
    /**
     * Gets the chronology.
     *
     * @return the chronology
     */
    Chronology getChronology();

    VersionType getSemanticType();

    /**
     * Calls {@link #makeAnalog(int)} with a stamp created from the values provided by the supplied WriteCoordinate.
     * This version will be passed into the WriteCoordinate for selecting the moduleNid.
     *
     * @param <V>         the mutable version type
     * @param wc   the write coordinate to determine status, time, author, module, and path for the analog.
     * @return the mutable version, with all properties beyond STAMP properties copied from this version.
     */
    default <V extends Version> V makeAnalog(WriteCoordinate wc) {
        final int stampSequence = Get.stampService()
                .getStampSequence(wc.getTransaction().orElse(null),
                        wc.getStatus(),
                        wc.getTime(),
                        wc.getAuthorNid(),
                        wc.getModuleNid(),
                        wc.getPathNid());
        return makeAnalog(stampSequence);
    }

    /**
     * Create a analog version with the specified stamp.
     * It is the responsibility of the caller to directly write the chronology to the store after 
     * making any further changes.  Values for all properties except the STAMP properties will be copied 
     * from this version. 
     *
     * @param <V> the mutable version type
     * @param stampSequence the complete stamp for the mutable version
     * @return the mutable version, with all properties beyond STAMP properties copied from this version.
     */
    <V extends Version> V makeAnalog(int stampSequence);
    
    /**
     * Adds the additional uuids.
     *
     * @param uuids the uuid
     */
    public void addAdditionalUuids(UUID... uuids);

    /**
     * DeepEquals considers all fields, not just the stamp and the assumptions that the commit manager will not allow
     * more one version for a given stamp. This extra consideration is necessary to support uncommitted versions, that
     * may change in a multi-user environment, including that an individual author may make changes on more than one path
     * at a time.
     *
     * @param other the object to compare.
     * @return true if all fields are equal, otherwise false.
     */
    boolean deepEquals(Object other);

}
