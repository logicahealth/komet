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

import java.util.List;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 * The Interface EditCoordinate.
 *
 * @author kec
 */
public interface EditCoordinate extends Coordinate {
   /**
    * Gets the author nid.
    *
    * @return the author nid
    */
   int getAuthorNid();
   
   default ConceptSpecification getAuthor() {
       return Get.conceptSpecification(getAuthorNid());
   }

   /**
    * Gets the module nid.
    *
    * @return the module nid
    */
   int getModuleNid();
   
   default ConceptSpecification getModule() {
       return Get.conceptSpecification(getModuleNid());
   }
   
   List<ConceptSpecification> getModuleOptions();
   
   void setModuleOptions(List<ConceptSpecification> options);

   /**
    * Gets the path nid.
    *
    * @return the path nid
    */
   int getPathNid();

   default ConceptSpecification getPath() {
       return Get.conceptSpecification(getPathNid());
   }

   List<ConceptSpecification> getPathOptions();

   void setPathOptions(List<ConceptSpecification> options);
   
   @Override
   EditCoordinate deepClone();

   default String toUserString() {
      StringBuilder sb = new StringBuilder();
      sb.append("author: ").append(Get.conceptDescriptionText(getAuthorNid())).append("\n");
      sb.append("module: ").append(Get.conceptDescriptionText(getModuleNid())).append("\n");
      sb.append("path: ").append(Get.conceptDescriptionText(getPathNid())).append("\n");
      return sb.toString();
   }
   
}

