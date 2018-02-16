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
import sh.isaac.api.commit.IdentifiedStampedVersion;
import sh.isaac.api.coordinate.EditCoordinate;

/**
 *
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
    * Create a analog version with Long.MAX_VALUE as the time, indicating
    * the version is uncommitted. It is the responsibility of the caller to
    * add the mutable version to the commit manager when changes are complete
    * prior to committing the component. Values for all properties except author,
    * time, and path (which are provided by the edit coordinate) will be copied 
    * from this version. 
    *
    * @param <V> the mutable version type
    * @param ec edit coordinate to provide the author, module, and path for the mutable version
    * @return the mutable version
    */
   <V extends Version> V makeAnalog(EditCoordinate ec);
   
   /**
    * Adds the additional uuids.
    *
    * @param uuids the uuid
    */
   public void addAdditionalUuids(UUID ...uuids);

}
