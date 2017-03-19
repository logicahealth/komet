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

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * ISAAC module level code can implement this class, and annotate with HK2 as a service,
 * in order to have their constants automatically generated into the DB by the mojo by the
 * {@link ExportTaxonomy} mojo.
 */
@Contract
public interface ModuleProvidedConstants {
   /**
    * When providing concepts for this method, any top-level concept returned should have specified a parent
    * via a setParent(..) call.  Otherwise, it will be attached to the ISAAC root concept.
    *
    * Concepts that are nested under a {@link MetadataConceptConstantGroup} will be created relative to the concept
    * created at the top of the group (which should have setParent(...) specified)
    *
    * DO NOT make a reference to the LookupService in a variable this is statically defined - this will break the
    * HK2 init routine!
    *
    * @return the constants to create
    */
   public MetadataConceptConstant[] getConstantsToCreate();
}

