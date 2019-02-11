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

import java.util.ArrayList;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import sh.isaac.api.util.UUIDUtil;

/**
 * Coordinate to manage the retrieval and display of logic information.
 *
 * Created by kec on 2/16/15.
 */
public interface LogicCoordinate extends Coordinate {
    /**
     * 
     * @return a content based uuid, such that identical logic coordinates
     * will have identical uuids, and that different logic coordinates will 
     * always have different uuids.
     */
    default UUID getLogicCoordinateUuid() {
       ArrayList<UUID> uuidList = new ArrayList();
       UUIDUtil.addSortedUuids(uuidList, getClassifierNid());
       UUIDUtil.addSortedUuids(uuidList, getDescriptionLogicProfileNid());
       UUIDUtil.addSortedUuids(uuidList, getInferredAssemblageNid());
       UUIDUtil.addSortedUuids(uuidList, getStatedAssemblageNid());
       UUIDUtil.addSortedUuids(uuidList, getConceptAssemblageNid());
       return UUID.nameUUIDFromBytes(uuidList.toString().getBytes());
   }
   /**
    * Gets the classifier nid.
    *
    * @return concept nid for the classifier for this coordinate.
    */
   int getClassifierNid();

   /**
    * Gets the description logic profile nid.
    *
    * @return concept nid for the description-logic profile for this coordinate.
    */
   int getDescriptionLogicProfileNid();

   /**
    * Gets the inferred assemblage nid.
    *
    * @return concept nid for the assemblage where the inferred logical form
    * of concept definition graphs are stored.
    */
   int getInferredAssemblageNid();

   /**
    * Gets the stated assemblage nid.
    *
    * @return concept nid for the assemblage where the stated logical form
    * of concept definition graphs are stored.
    */
   int getStatedAssemblageNid();
   

   @Override
   public LogicCoordinate deepClone();
   
   /**
    * 
    * @return the nid for the assemblage within which the concepts to be classified are defined within. 
    */
   int getConceptAssemblageNid();
   
}

