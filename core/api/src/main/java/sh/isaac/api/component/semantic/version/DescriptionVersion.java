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



package sh.isaac.api.component.semantic.version;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.VertexSortPreferredName;

/**
 * Describes the referenced component in a way appropriate for the type and
 * language specified.
 * The description is annotated to provide support for dialect.
 *
 * @author kec
 */
public interface DescriptionVersion
        extends SemanticVersion {
   /**
    * Gets the case significance concept nid.
    *
    * @return the case significance concept nid
    */
   int getCaseSignificanceConceptNid();

   /**
    * Gets the description type concept nid.
    *
    * @return the description type concept nid
    */
   int getDescriptionTypeConceptNid();
   
   /**
    * A convenience method to get a end-user reasonable label for the description type.
    * 
    * Calls {@link #getDescriptionType()} and returns one of "Fully Qualified Name", "Regular Name" or "Definition",
    * as it matches the type.  If there is an error, and the nid doesn't align to one of these, it falls through to 
    * {@link Get#conceptDescriptionText(int)} 
    * 
    * @return a description type string suitable for an end-user display.
    */
   default String getDescriptionType() {
      int nid = getDescriptionTypeConceptNid();
      //Because the names of these concepts keep arbitrarily changing, provide labels that can be used in a GUI
      //the way users actually want to see them.... nobody wants to see 
      //"Regular name description type (SOLOR)" in a GUI dropdown of description types
      if (nid == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
         return "Fully Qualified Name";
      } else if (nid == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
          return "Regular Name";
      } else if (nid == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
          return "Definition";
      } else {
          return VertexSortPreferredName.getRegularName(nid, Get.defaultCoordinate().getLanguageCoordinate(),
                  Get.defaultCoordinate().getStampFilter());
      }
   }

   /**
    * Gets the language concept nid.
    *
    * @return the language concept nid
    */
   int getLanguageConceptNid();

   /**
    * Gets the text.
    *
    * @return the text
    */
   String getText();

   @Override
   default VersionType getSemanticType() {
      return VersionType.DESCRIPTION;
   }
}

