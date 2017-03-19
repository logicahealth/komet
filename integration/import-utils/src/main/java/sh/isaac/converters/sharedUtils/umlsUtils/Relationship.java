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



package sh.isaac.converters.sharedUtils.umlsUtils;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.converters.sharedUtils.ConsoleUtil;

//~--- classes ----------------------------------------------------------------

public class Relationship {
   private static HashMap<String, String> preferredNameMap = new HashMap<>();

   //~--- static initializers -------------------------------------------------

   static {
      // I didn't like the names they provide in the UMLS - so I put those in as descriptions, and use these as the preferred terms.
      preferredNameMap.put("PAR",
                           "is parent");  // This is a confusing mess in UMLS - they define it as "has parent" but they really mean "is the parent of"
      preferredNameMap.put("CHD", "is child");
      preferredNameMap.put("SY", "synonym");
      preferredNameMap.put("SIB", "sibling");
      preferredNameMap.put("DEL", "deleted");
      preferredNameMap.put("RB", "broader");
      preferredNameMap.put("RN", "narrower");
      preferredNameMap.put("AQ", "allowed qualifier");
      preferredNameMap.put("RO", "other");
      preferredNameMap.put("RQ", "related, possibly synonymous");
      preferredNameMap.put("XR", "not related");
      preferredNameMap.put("RL", "alike");
      preferredNameMap.put("RU", "related");
      preferredNameMap.put("QB", "qualified by");
   }

   //~--- fields --------------------------------------------------------------

   private HashSet<String> name1SnomedCode = new HashSet<String>();
   private HashSet<String> name2SnomedCode = new HashSet<String>();
   private String          name1;
   private String          description1;
   private String          name2;
   private String          description2;
   private String          name1RelType;
   private String          name2RelType;
   private boolean         isRela_;
   private Boolean         swap;

   //~--- constructors --------------------------------------------------------

   public Relationship(boolean isRela) {
      this.isRela_ = isRela;
   }

   //~--- methods -------------------------------------------------------------

   public void addDescription(String name, String niceName) {
      if (name.equals(name1)) {
         if (description1 != null) {
            throw new RuntimeException("Oops");
         }

         description1 = niceName;
      } else if (name.equals(name2)) {
         if (description2 != null) {
            throw new RuntimeException("Oops");
         }

         description2 = niceName;
      } else if ((name1 == null) && (name2 == null)) {
         if (description1 != null) {
            throw new RuntimeException("Oops");
         }

         name1        = name;
         description1 = niceName;
      } else {
         throw new RuntimeException("Oops");
      }
   }

   public void addRelInverse(String name, String inverseRelName) {
      if ((name1 == null) && (name2 == null)) {
         name1 = name;
         name2 = inverseRelName;
      } else if (name.equals(name1)) {
         if (name2 == null) {
            name2 = inverseRelName;
         } else if (!name2.equals(inverseRelName)) {
            throw new RuntimeException("oops");
         }
      } else if (name.equals(name2)) {
         if (name1 == null) {
            name1 = inverseRelName;
         } else if (!name1.equals(inverseRelName)) {
            throw new RuntimeException("oops");
         }
      } else {
         throw new RuntimeException("oops");
      }
   }

   public void addRelType(String name, String type) {
      if (name.equals(name1)) {
         if (name1RelType == null) {
            name1RelType = type;
         } else if (!name1RelType.equals(type)) {
            throw new RuntimeException("oops");
         }
      } else if (name.equals(name2)) {
         if (name2RelType == null) {
            name2RelType = type;
         } else if (!name2RelType.equals(type)) {
            throw new RuntimeException("oops");
         }
      } else {
         throw new RuntimeException("oops");
      }
   }

   public void addSnomedCode(String name, String code) {
      if (name.equals(name1)) {
         name1SnomedCode.add(code);
      } else if (name.equals(name2)) {
         name2SnomedCode.add(code);
      } else {
         throw new RuntimeException("oops");
      }
   }

   //~--- get methods ---------------------------------------------------------

   public String getAltName() {
      return preferredNameMap.get(getFSNName());
   }

   public String getDescription() {
      return swap ? description2
                  : description1;
   }

   public String getFSNName() {
      return swap ? name2
                  : name1;
   }

   public String getInverseAltName() {
      return (getInverseFSNName() == null) ? null
            : preferredNameMap.get(getInverseFSNName());
   }

   public String getInverseDescription() {
      return swap ? description1
                  : description2;
   }

   public String getInverseFSNName() {
      return swap ? name1
                  : name2;
   }

   public Set<String> getInverseRelSnomedCode() {
      return swap ? name1SnomedCode
                  : name2SnomedCode;
   }

   public String getInverseRelType() {
      return swap ? name1RelType
                  : name2RelType;
   }

   public boolean getIsRela() {
      return isRela_;
   }

   public Set<String> getRelSnomedCode() {
      return swap ? name2SnomedCode
                  : name1SnomedCode;
   }

   public String getRelType() {
      return swap ? name2RelType
                  : name1RelType;
   }

   //~--- set methods ---------------------------------------------------------

   public void setSwap(Connection c, String tablePrefix)
            throws SQLException {
      if (swap != null) {
         throw new RuntimeException("Swap already set!");
      }

      if ((name1 == null) && (name2 != null)) {
         swap = true;
      } else if ((name2 == null) && (name1 != null)) {
         swap = false;
      } else if (name1.equals(name2)) {
         swap = false;
      } else if (name1.equals("RN") || name2.equals("RN"))    // narrower as primary
      {
         swap = name2.equals("RN");
      } else if (name1.equals("AQ") || name2.equals("AQ"))    // allowed qualifier as primary
      {
         swap = name2.equals("AQ");
      } else if (name1.equals("CHD") || name2.equals("CHD"))  // is child as primary
      {
         swap = name2.equals("CHD");
      } else {
         // Use the primary assignments above, to figure out the more detailed assignments (where possible)
         Statement s  = c.createStatement();
         ResultSet rs = s.executeQuery("Select distinct REL from " + tablePrefix + "REL where RELA='" + name1 + "'");

         while (rs.next()) {
            if (rs.getString("REL")
                  .equals("RO")) {
               // ignore these - they sometimes occur in tandem with a directional one below
               continue;
            }

            if (name1.equals("mapped_from")) {
               // This one is all over the board in UMLS, sometimes tied to RB, sometimes RN, or a whole bunch of other types.
               // Just let the code below handle it.
               break;
            }

            String rel = rs.getString("REL");

            if (swap != null) {
               // this is a bug? in umls - has_part and inverse_isa appears with both PAR and RB rels - but we set the swap the same for each, so ignore the second one.
               // inverse_isa also uses RQ, but just ignore that too.
               if ((name1.equals("inverse_isa") || name1.equals("has_part")) &&
                     (rel.equals("PAR") || rel.equals("RB") || rel.equals("RQ"))) {
                  continue;
               } else {
                  throw new RuntimeException("too many results on rela " + name1);
               }
            }

            if (new HashSet<String>(Arrays.asList(new String[] {
               "RB", "RN", "QB", "AQ", "PAR", "CHD"
            })).contains(rel)) {
               if (rel.equals("RN") || rel.equals("AQ") || rel.equals("CHD")) {
                  swap = false;
               } else {
                  swap = true;
               }
            }
         }

         rs.close();
         s.close();

         // TODO utilize MRREL DIR column - see if that helps.  Also talk to Brian, see if there is better code for this.
         if (swap == null) {
            if (name1.startsWith("inverse_") || name2.startsWith("inverse_"))       // inverse_ things as secondary
            {
               swap = name1.startsWith("inverse_");
            } else if (name1.startsWith("has_") || name2.startsWith("has_"))        // has_ things as primary
            {
               swap = name2.startsWith("has_");
            } else if (name1.startsWith("may_be") || name2.startsWith("may_be"))    // may_be X as primary
            {
               swap = name2.startsWith("may_be");
            } else if (name1.contains("_from") || name2.contains("_from"))          // X_from as primary
            {
               swap = name2.contains("_from");
            } else if (name1.contains("_by") || name2.contains("_by"))              // X_by as primary
            {
               swap = name2.contains("_by");
            } else if (name1.contains("_in_") || name2.contains("_in_"))            // X_in_ as primary
            {
               swap = name2.contains("_in_");
            } else if (name1.endsWith("_in") || name2.endsWith("_in"))              // X_in as primary
            {
               swap = name2.endsWith("_in");
            } else if (name1.contains("_is") || name2.contains("_is"))              // X_is as primary
            {
               swap = name2.contains("_is");
            } else if (name1.startsWith("is_") || name2.startsWith("is_"))          // is_ as primary
            {
               swap = name2.startsWith("is_");
            } else if (name1.contains("_has") || name2.contains("_has"))            // X_has as secondary
            {
               swap = name1.contains("_has");
            } else if (name1.equals("larger_than") || name2.equals("larger_than"))  // swap smaller_than to primary
            {
               swap = name1.equals("larger_than");
            } else if (name1.equals("due_to") || name2.equals("due_to"))            // due_to as primary, cause_of secondary
            {
               swap = name2.equals("due_to");
            } else if (name1.equals("occurs_after") ||
                       name2.equals("occurs_after"))                                // occurs_after as primary, occurs_before secondary
                       {
               swap = name2.equals("occurs_after");
            }
         }
      }

      if (swap == null) {
         ConsoleUtil.println("No rel direction preference specified for " + name1 + "/" + name2 + " - using " + name1 +
                             " as primary");
         swap = false;
      }
   }
}

