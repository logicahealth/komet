/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UUIDUtil;

//~--- interfaces -------------------------------------------------------------

/**
 * Coordinate to control the retrieval and display of
 * object chronicle versions by indicating the current position (represented as time) on a path,
 * and allowed modules.
 * <p\>
 * q: How does the stamp coordinate relate to the stamp sequence?
 * <p\>
 a: A stamp sequence is a sequentially assigned identifier for a unique combination of Status, Time, Author, Module, and Path...
 A stamp coordinate specifies a position on a  path, with a particular set of modules, and allowed state values.

 <p\>
 * Created by kec on 2/16/15.
 *
 */
public interface StampCoordinate
        extends TimeBasedAnalogMaker<StampCoordinate>, StateBasedAnalogMaker<StampCoordinate>, Coordinate {

    /**
     * 
     * @return a content based uuid, such that identical stamp coordinates
     * will have identical uuids, and that different stamp coordinates will 
     * always have different uuids.
     */
    default UUID getStampCoordinateUuid() {
        ArrayList<UUID> uuidList = new ArrayList();
        for (Status status: getAllowedStates()) {
            UUIDUtil.addSortedUuids(uuidList, status.getSpecifyingConcept().getNid());
        }
        UUIDUtil.addSortedUuids(uuidList, getModuleNids().asArray());
        UUIDUtil.addSortedUuids(uuidList, getStampPosition().getStampPath().getPathConceptNid());
        UUIDUtil.addSortedUuids(uuidList, getStampPrecedence().getSpecifyingConcept().getNid());
        for (ConceptSpecification spec: getModulePreferenceOrderForVersions()) {
            UUIDUtil.addSortedUuids(uuidList, spec.getNid());
        }
        StringBuilder b = new StringBuilder();
        b.append(uuidList.toString());
        b.append(getStampPosition().getTime());
        return UUID.nameUUIDFromBytes(b.toString().getBytes());
    }

    /**
    * Determine what states should be included in results based on this
    * stamp coordinate. If current—but inactive—versions are desired,
    * the allowed states must include {@code Status.INACTIVE}
    * @return the set of allowed states for results based on this stamp coordinate.
    */
   EnumSet<Status> getAllowedStates();

   /**
    * An empty array is a wild-card, and should match all modules. If there are
    * one or more module nids specified, only those modules will be included
    * in the results.
    * @return the set of module nids to include in results based on this
    * stamp coordinate.
    */
   NidSet getModuleNids();
   
   /**
    * An empty list is a wild-card, and should match all modules. If there are
    * one or more modules specified, only those modules will be included
    * in the results.
    * @return the set of modules to include in results based on this
    * stamp coordinate.
    */
   Set<ConceptSpecification> getModuleSpecifications();

   /**
    * Gets the module preference list for versions. Used to adjudicate which component to 
    * return when more than one version is available. For example, if two modules
    * have versions the same component, which one do you prefer to return?
    * @return the module preference list for versions. 
    */

   List<ConceptSpecification> getModulePreferenceOrderForVersions();
   /**
    * Gets the stamp position.
    *
    * @return the position (time on a path) that is used to
    * compute what stamped objects versions are the latest with respect to this
    * position.
    */
   StampPosition getStampPosition();

   /**
    * Determine if the stamp coordinate is time based, or path based. Generally
    * path based is recommended.
    * @return the stamp precedence.
    */
   StampPrecedence getStampPrecedence();
   
   
   @Override
   StampCoordinate deepClone();
   
   /**
    * Create a new Stamp Coordinate identical to the this coordinate, but with the modules modified.
    * @param modules the new modules list, or the modules to append.
    * @param add - true, if the modules parameter should be appended to the existing modules, false if the 
    * supplied modules should replace the existing modules
    * @return the new coordinate
    */
    StampCoordinate makeModuleAnalog(Collection<ConceptSpecification> modules, boolean add);

    /**
     * An empty list is a wild-card, and should match all authors. If there are
     * one or more authors specified, only those authors will be included
     * in the results.
     * @return the set of authors to include in results based on this
     * stamp coordinate.
     */
    Set<ConceptSpecification> getAuthorSpecifications();

    /**
     * An empty array is a wild-card, and should match all modules. If there are
     * one or more author nids specified, only those authors will be included
     * in the results.
     * @return the set of author nids to include in results based on this
     * stamp coordinate.
     */
    NidSet getAuthorNids();
}

