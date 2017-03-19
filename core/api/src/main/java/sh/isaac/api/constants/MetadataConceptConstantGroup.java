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



package sh.isaac.api.constants;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//~--- classes ----------------------------------------------------------------

/**
 * The Class MetadataConceptConstantGroup.
 */
public abstract class MetadataConceptConstantGroup
        extends MetadataConceptConstant {
   /** The children. */
   private final List<MetadataConceptConstant> children = new ArrayList<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new metadata concept constant group.
    *
    * @param fsn the fsn
    * @param uuid the uuid
    */
   protected MetadataConceptConstantGroup(String fsn, UUID uuid) {
      super(fsn, uuid);
   }

   /**
    * Instantiates a new metadata concept constant group.
    *
    * @param fsn the fsn
    * @param uuid the uuid
    * @param definition the definition
    */
   protected MetadataConceptConstantGroup(String fsn, UUID uuid, String definition) {
      super(fsn, uuid, definition);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the child.
    *
    * @param child the child
    */
   protected void addChild(MetadataConceptConstant child) {
      this.children.add(child);
      child.setParent(this);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children.
    *
    * @return The constants that should be created under this constant in the
    * taxonomy (if any). Will not return null.
    */
   public List<MetadataConceptConstant> getChildren() {
      return this.children;
   }
}

