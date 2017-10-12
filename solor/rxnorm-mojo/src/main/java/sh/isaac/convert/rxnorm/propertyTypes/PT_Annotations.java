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



package sh.isaac.convert.rxnorm.propertyTypes;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;

//~--- classes ----------------------------------------------------------------

/**
 * Properties from the DTS ndf load which are treated as alternate IDs within the workbench.
 * @author Daniel Armbrust
 */
public class PT_Annotations
        extends BPT_Annotations {
   /**
    * Instantiates a new p T annotations.
    */
   public PT_Annotations() {
      super("RRF");
      indexByAltNames();
      addProperty("Unique identifier for atom ", "RXAUI", "(RxNorm Atom Id)");
      addProperty("Source asserted atom identifier", "SAUI", null);
      addProperty("Source asserted concept identifier", "SCUI", null);
      addProperty("Source Vocabulary",
                  "SAB",
                  null,
                  false,
                  -1,
                  new DynamicColumnInfo[] { new DynamicColumnInfo(null,
                        0,
                        DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getUUID(),
                        DynamicDataType.UUID,
                        null,
                        true,
                        null,
                        null,
                        false) });

      // TODO merge this on metadata code?  Would mean changing from dynamic refex to string type...
      addProperty("Code",
                  "CODE",
                  "\"Most useful\" source asserted identifier (if the source vocabulary has more than one identifier)" +
                  ", or a RxNorm-generated source entry identifier (if the source vocabulary has none.)");
      addProperty("Suppress",
                  "SUPPRESS",
                  null,
                  false,
                  -1,
                  new DynamicColumnInfo[] { new DynamicColumnInfo(null,
                        0,
                        DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getUUID(),
                        DynamicDataType.UUID,
                        null,
                        true,
                        null,
                        null,
                        true) });
      addProperty("Term Type Class", "tty_class", null);
      addProperty("STYPE",
                  null,
                  "The name of the column in RXNCONSO.RRF or RXNREL.RRF that contains the identifier to which the attribute is attached, e.g., CUI, AUI.",
                  false,
                  1,
                  new DynamicColumnInfo[] { new DynamicColumnInfo(null,
                        0,
                        DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getUUID(),
                        DynamicDataType.UUID,
                        null,
                        true,
                        null,
                        null,
                        true) });
      addProperty("STYPE1",
                  null,
                  "The name of the column in RXNCONSO.RRF that contains the identifier used for the first concept or first atom in source of the relationship (e.g., 'AUI' or 'CUI')");
      addProperty("STYPE2",
                  null,
                  "The name of the column in RXNCONSO.RRF that contains the identifier used for the second concept or second atom in the source of the relationship (e.g., 'AUI' or 'CUI')");
      addProperty("Source Asserted Attribute Identifier",
                  "SATUI",
                  "Source asserted attribute identifier (optional - present if it exists)");
      addProperty("Semantic Type tree number", "STN", null);
      addProperty("Semantic Type",
                  "STY",
                  null,
                  false,
                  -1,
                  new DynamicColumnInfo[] { new DynamicColumnInfo(null,
                        0,
                        DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getUUID(),
                        DynamicDataType.UUID,
                        null,
                        true,
                        null,
                        null,
                        true) });
      addProperty("Content View Flag",
                  "CVF",
                  "Bit field used to flag rows included in Content View.");  // note - this is undocumented in RxNorm - used on the STY table - description_ comes from UMLS
      addProperty("URI");
      addProperty("RG", null, "Machine generated and unverified indicator");
      addProperty("Generic rel type",
                  null,
                  "Generic rel type for this relationship",
                  false,
                  -1,
                  new DynamicColumnInfo[] { new DynamicColumnInfo(null,
                        0,
                        DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getUUID(),
                        DynamicDataType.UUID,
                        null,
                        true,
                        null,
                        null,
                        true) });

      // Things that used to be IDs, below this point
      addProperty("RXCUI", "RxNorm Concept ID", "RxNorm Unique identifier for concept");
      addProperty("TUI", "RxNorm Semantic Type ID", "Unique identifier of Semantic Type");
      addProperty("RUI", "RxNorm Relationship ID", "Unique identifier for Relationship");
      addProperty("ATUI", "RxNorm Attribute ID", "Unique identifier for attribute");
   }
}

