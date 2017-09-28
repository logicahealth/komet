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



package sh.isaac.convert.loinc.standard.propertyTypes;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.MetaData;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;

//~--- classes ----------------------------------------------------------------

/**
 * Fields to treat as attributes.
 *
 * @author Daniel Armbrust
 */
public class PT_Annotations
        extends BPT_Annotations {
   /**
    * Instantiates a new p T annotations.
    *
    * @param skipList the skip list
    */
   public PT_Annotations(List<String> skipList) {
      super("LOINC");
      super.skipList = skipList;
      addProperty("DT_LAST_CH", 0, 1);                   // replaced with DATE_LAST_CHANGED in 2.38
      addProperty("DATE_LAST_CHANGED", 2, 6);            // replaced with VersionLastChanged in 2.54
      addProperty("CHNG_TYPE");
      addProperty("COMMENTS", 0, 5);                     // deleted in 2.52
      addProperty("ANSWERLIST", 0, 1, true);             // deleted in 2.38
      addProperty("SCOPE", 0, 1, true);                  // deleted in 2.38
      addProperty("IPCC_UNITS", 0, 1, true);             // deleted in 2.38
      addProperty("REFERENCE", 0, 1);                    // deleted in 2.38
      addProperty("MOLAR_MASS", 0, 5);                   // deleted in 2.52
      addProperty("CLASSTYPE");
      addProperty("FORMULA");
      addProperty("SPECIES");
      addProperty("EXMPL_ANSWERS");
      addProperty("CODE_TABLE", 0, 5);                   // deleted in 2.52
      addProperty("SETROOT", 0, 1);                      // deleted in 2.38
      addProperty("PANELELEMENTS", 0, 1, true);          // deleted in 2.38
      addProperty("SURVEY_QUEST_TEXT");
      addProperty("SURVEY_QUEST_SRC");
      addProperty("UNITSREQUIRED");
      addProperty("SUBMITTED_UNITS");
      addProperty("ORDER_OBS");
      addProperty("CDISC_COMMON_TESTS");
      addProperty("HL7_FIELD_SUBFIELD_ID");
      addProperty("EXTERNAL_COPYRIGHT_NOTICE");
      addProperty("EXAMPLE_UNITS");
      addProperty("INPC_PERCENTAGE", 0, 1);              // deleted in 2.38
      addProperty("HL7_V2_DATATYPE", 0, 5);              // deleted in 2.52
      addProperty("HL7_V3_DATATYPE", 0, 5);              // deleted in 2.52
      addProperty("CURATED_RANGE_AND_UNITS", 0, 5);      // deleted in 2.52
      addProperty("DOCUMENT_SECTION");
      addProperty("DEFINITION_DESCRIPTION_HELP", 0, 1);  // deleted in 2.38
      addProperty("EXAMPLE_UCUM_UNITS");
      addProperty("EXAMPLE_SI_UCUM_UNITS");
      addProperty("STATUS_REASON");
      addProperty("STATUS_TEXT");
      addProperty("CHANGE_REASON_PUBLIC");
      addProperty("COMMON_TEST_RANK");
      addProperty("COMMON_ORDER_RANK", 2, 0);            // added in 2.38
      addProperty("STATUS");
      addProperty("COMMON_SI_TEST_RANK",
                  3,
                  0);  // added in 2.40 (or maybe 2.39, 2.39 is untested - they failed to document it)
      addProperty("HL7_ATTACHMENT_STRUCTURE", 4, 0);  // added in 2.42
      addProperty("NAACCR_ID",
                  0,
                  4);  // Moved from ID - turned out it wasn't unique (see loinc_num 42040-6 and 39807-3)  //deleted in 2.52
      addProperty("EXTERNAL_COPYRIGHT_LINK", 5);  // added in 2.50
      addProperty("UnitsAndRange", 6);            // added in 2.52
      addProperty("PanelType", 6);                // added in 2.52
      addProperty("AskAtOrderEntry", 6);          // added in 2.52
      addProperty("AssociatedObservations", 6);   // added in 2.52
      addProperty("VersionLastChanged", 7);       // added in 2.54

      // moved these two out of the descriptions
      addProperty("RELAT_NMS", 0, 1, true);  // deleted in 2.38
      addProperty("RELATEDNAMES2");

      // from multiaxial
      addProperty("SEQUENCE");
      addProperty("IMMEDIATE_PARENT");
      addProperty("PATH_TO_ROOT");

      // From Source_Organization
      addProperty("COPYRIGHT");
      addProperty("TERMS_OF_USE");
      addProperty("URL");

      // From Map_TO
      addProperty("COMMENT");

      // Things that used to be IDs below here
      addProperty(new Property(this, MetaData.LOINC_NUM____SOLOR));

      // Abbrev Codes used by axis and class
      addProperty("ABBREVIATION");

      // From multi-axial class
      addProperty("CODE");
   }
}

