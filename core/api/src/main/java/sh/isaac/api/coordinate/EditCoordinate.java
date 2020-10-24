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

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UUIDUtil;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Edits occur on the manifold coordinate path when developing.
 *
 * Module is unchanged when developing. A default module is used for any new content.
 *
 * When modularizing, a destination module is provided, and the change will be written to the
 * manifold coordinate path.
 *
 * When promoting, the module will be unchanged, and the promotion path will be where a copy of
 * content on the manifold coordinate path written.
 *
 * @author kec
 */
public interface EditCoordinate {

   default UUID getEditCoordinateUuid() {
      ArrayList<UUID> uuidList = new ArrayList<>();
      UUIDUtil.addSortedUuids(uuidList, getAuthorNidForChanges());
      UUIDUtil.addSortedUuids(uuidList, getDefaultModuleNid());
      UUIDUtil.addSortedUuids(uuidList, getDestinationModuleNid());
      UUIDUtil.addSortedUuids(uuidList, getPromotionPathNid());
      StringBuilder b = new StringBuilder();
      b.append(uuidList.toString());
      return UUID.nameUUIDFromBytes(b.toString().getBytes());
   }
   /**
    * Gets the author nid.
    *
    * @return the author nid
    */
   int getAuthorNidForChanges();
   
   default ConceptSpecification getAuthorForChanges() {
       return Get.conceptSpecification(getAuthorNidForChanges());
   }

   /**
    * The default module is the module for new content when developing. Modifications to existing
    * content retain their module.
    * @return
    */
   int getDefaultModuleNid();

   /**
    * The default module is the module for new content when developing. Modifications to existing
    * content retain their module.
    * @return
    */
   default ConceptSpecification getDefaultModule() {
      return Get.conceptSpecification(getDefaultModuleNid());
   }

   /**
    * The destination module is the module that existing content is moved to when Modularizing
    * @return the nid of the destination module concept
    */
   int getDestinationModuleNid();

   /**
    * The destination module is the module that existing content is moved to when Modularizing
    * @return the destination module concept
    */
   default ConceptSpecification getDestinationModule() {
      return Get.conceptSpecification(getDestinationModuleNid());
   }

   EditCoordinateImmutable toEditCoordinateImmutable();

   /**
    * The promotion path is the path that existing content is moved to when Promoting
    * @return the nid of the promotion concept
    */
   int getPromotionPathNid();

   /**
    * The promotion path is the path that existing content is moved to when Promoting
    * @return the promotion concept
    */
   default ConceptSpecification getPromotionPath() {
      return Get.conceptSpecification(getPromotionPathNid());
   }

   default String toUserString() {
      StringBuilder sb = new StringBuilder();
      sb.append("author: ").append(Get.conceptDescriptionText(getAuthorNidForChanges())).append("\n");
      sb.append("default module: ").append(Get.conceptDescriptionText(getDefaultModuleNid())).append("\n");
      sb.append("destination module: ").append(Get.conceptDescriptionText(getDestinationModuleNid())).append("\n");
      sb.append("promotion path: ").append(Get.conceptDescriptionText(getPromotionPathNid())).append("\n");
      return sb.toString();
   }
   
}

