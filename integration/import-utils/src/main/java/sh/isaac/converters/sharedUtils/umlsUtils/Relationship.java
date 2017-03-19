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

/**
 * The Class Relationship.
 */
public class Relationship {
   
   /** The preferred name map. */
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

   /** The name 1 snomed code. */
   private final HashSet<String> name1SnomedCode = new HashSet<String>();
   
   /** The name 2 snomed code. */
   private final HashSet<String> name2SnomedCode = new HashSet<String>();
   
   /** The name 1. */
   private String          name1;
   
   /** The description 1. */
   private String          description1;
   
   /** The name 2. */
   private String          name2;
   
   /** The description 2. */
   private String          description2;
   
   /** The name 1 rel type. */
   private String          name1RelType;
   
   /** The name 2 rel type. */
   private String          name2RelType;
   
   /** The is rela. */
   private final boolean         isRela_;
   
   /** The swap. */
   private Boolean         swap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new relationship.
    *
    * @param isRela the is rela
    */
   public Relationship(boolean isRela) {
      this.isRela_ = isRela;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the description.
    *
    * @param name the name
    * @param niceName the nice name
    */
   public void addDescription(String name, String niceName) {
      if (name.equals(this.name1)) {
         if (this.description1 != null) {
            throw new RuntimeException("Oops");
         }

         this.description1 = niceName;
      } else if (name.equals(this.name2)) {
         if (this.description2 != null) {
            throw new RuntimeException("Oops");
         }

         this.description2 = niceName;
      } else if ((this.name1 == null) && (this.name2 == null)) {
         if (this.description1 != null) {
            throw new RuntimeException("Oops");
         }

         this.name1        = name;
         this.description1 = niceName;
      } else {
         throw new RuntimeException("Oops");
      }
   }

   /**
    * Adds the rel inverse.
    *
    * @param name the name
    * @param inverseRelName the inverse rel name
    */
   public void addRelInverse(String name, String inverseRelName) {
      if ((this.name1 == null) && (this.name2 == null)) {
         this.name1 = name;
         this.name2 = inverseRelName;
      } else if (name.equals(this.name1)) {
         if (this.name2 == null) {
            this.name2 = inverseRelName;
         } else if (!this.name2.equals(inverseRelName)) {
            throw new RuntimeException("oops");
         }
      } else if (name.equals(this.name2)) {
         if (this.name1 == null) {
            this.name1 = inverseRelName;
         } else if (!this.name1.equals(inverseRelName)) {
            throw new RuntimeException("oops");
         }
      } else {
         throw new RuntimeException("oops");
      }
   }

   /**
    * Adds the rel type.
    *
    * @param name the name
    * @param type the type
    */
   public void addRelType(String name, String type) {
      if (name.equals(this.name1)) {
         if (this.name1RelType == null) {
            this.name1RelType = type;
         } else if (!this.name1RelType.equals(type)) {
            throw new RuntimeException("oops");
         }
      } else if (name.equals(this.name2)) {
         if (this.name2RelType == null) {
            this.name2RelType = type;
         } else if (!this.name2RelType.equals(type)) {
            throw new RuntimeException("oops");
         }
      } else {
         throw new RuntimeException("oops");
      }
   }

   /**
    * Adds the snomed code.
    *
    * @param name the name
    * @param code the code
    */
   public void addSnomedCode(String name, String code) {
      if (name.equals(this.name1)) {
         this.name1SnomedCode.add(code);
      } else if (name.equals(this.name2)) {
         this.name2SnomedCode.add(code);
      } else {
         throw new RuntimeException("oops");
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the alt name.
    *
    * @return the alt name
    */
   public String getAltName() {
      return preferredNameMap.get(getFSNName());
   }

   /**
    * Gets the description.
    *
    * @return the description
    */
   public String getDescription() {
      return this.swap ? this.description2
                  : this.description1;
   }

   /**
    * Gets the FSN name.
    *
    * @return the FSN name
    */
   public String getFSNName() {
      return this.swap ? this.name2
                  : this.name1;
   }

   /**
    * Gets the inverse alt name.
    *
    * @return the inverse alt name
    */
   public String getInverseAltName() {
      return (getInverseFSNName() == null) ? null
            : preferredNameMap.get(getInverseFSNName());
   }

   /**
    * Gets the inverse description.
    *
    * @return the inverse description
    */
   public String getInverseDescription() {
      return this.swap ? this.description1
                  : this.description2;
   }

   /**
    * Gets the inverse FSN name.
    *
    * @return the inverse FSN name
    */
   public String getInverseFSNName() {
      return this.swap ? this.name1
                  : this.name2;
   }

   /**
    * Gets the inverse rel snomed code.
    *
    * @return the inverse rel snomed code
    */
   public Set<String> getInverseRelSnomedCode() {
      return this.swap ? this.name1SnomedCode
                  : this.name2SnomedCode;
   }

   /**
    * Gets the inverse rel type.
    *
    * @return the inverse rel type
    */
   public String getInverseRelType() {
      return this.swap ? this.name1RelType
                  : this.name2RelType;
   }

   /**
    * Gets the checks if rela.
    *
    * @return the checks if rela
    */
   public boolean getIsRela() {
      return this.isRela_;
   }

   /**
    * Gets the rel snomed code.
    *
    * @return the rel snomed code
    */
   public Set<String> getRelSnomedCode() {
      return this.swap ? this.name2SnomedCode
                  : this.name1SnomedCode;
   }

   /**
    * Gets the rel type.
    *
    * @return the rel type
    */
   public String getRelType() {
      return this.swap ? this.name2RelType
                  : this.name1RelType;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set swap.
    *
    * @param c the c
    * @param tablePrefix the table prefix
    * @throws SQLException the SQL exception
    */
   public void setSwap(Connection c, String tablePrefix)
            throws SQLException {
      if (this.swap != null) {
         throw new RuntimeException("Swap already set!");
      }

      if ((this.name1 == null) && (this.name2 != null)) {
         this.swap = true;
      } else if ((this.name2 == null) && (this.name1 != null)) {
         this.swap = false;
      } else if (this.name1.equals(this.name2)) {
         this.swap = false;
      } else if (this.name1.equals("RN") || this.name2.equals("RN"))    // narrower as primary
      {
         this.swap = this.name2.equals("RN");
      } else if (this.name1.equals("AQ") || this.name2.equals("AQ"))    // allowed qualifier as primary
      {
         this.swap = this.name2.equals("AQ");
      } else if (this.name1.equals("CHD") || this.name2.equals("CHD"))  // is child as primary
      {
         this.swap = this.name2.equals("CHD");
      } else {
         // Use the primary assignments above, to figure out the more detailed assignments (where possible)
         final Statement s  = c.createStatement();
         final ResultSet rs = s.executeQuery("Select distinct REL from " + tablePrefix + "REL where RELA='" + this.name1 + "'");

         while (rs.next()) {
            if (rs.getString("REL")
                  .equals("RO")) {
               // ignore these - they sometimes occur in tandem with a directional one below
               continue;
            }

            if (this.name1.equals("mapped_from")) {
               // This one is all over the board in UMLS, sometimes tied to RB, sometimes RN, or a whole bunch of other types.
               // Just let the code below handle it.
               break;
            }

            final String rel = rs.getString("REL");

            if (this.swap != null) {
               // this is a bug? in umls - has_part and inverse_isa appears with both PAR and RB rels - but we set the swap the same for each, so ignore the second one.
               // inverse_isa also uses RQ, but just ignore that too.
               if ((this.name1.equals("inverse_isa") || this.name1.equals("has_part")) &&
                     (rel.equals("PAR") || rel.equals("RB") || rel.equals("RQ"))) {
                  continue;
               } else {
                  throw new RuntimeException("too many results on rela " + this.name1);
               }
            }

            if (new HashSet<String>(Arrays.asList(new String[] {
               "RB", "RN", "QB", "AQ", "PAR", "CHD"
            })).contains(rel)) {
               if (rel.equals("RN") || rel.equals("AQ") || rel.equals("CHD")) {
                  this.swap = false;
               } else {
                  this.swap = true;
               }
            }
         }

         rs.close();
         s.close();

         // TODO utilize MRREL DIR column - see if that helps.  Also talk to Brian, see if there is better code for this.
         if (this.swap == null) {
            if (this.name1.startsWith("inverse_") || this.name2.startsWith("inverse_"))       // inverse_ things as secondary
            {
               this.swap = this.name1.startsWith("inverse_");
            } else if (this.name1.startsWith("has_") || this.name2.startsWith("has_"))        // has_ things as primary
            {
               this.swap = this.name2.startsWith("has_");
            } else if (this.name1.startsWith("may_be") || this.name2.startsWith("may_be"))    // may_be X as primary
            {
               this.swap = this.name2.startsWith("may_be");
            } else if (this.name1.contains("_from") || this.name2.contains("_from"))          // X_from as primary
            {
               this.swap = this.name2.contains("_from");
            } else if (this.name1.contains("_by") || this.name2.contains("_by"))              // X_by as primary
            {
               this.swap = this.name2.contains("_by");
            } else if (this.name1.contains("_in_") || this.name2.contains("_in_"))            // X_in_ as primary
            {
               this.swap = this.name2.contains("_in_");
            } else if (this.name1.endsWith("_in") || this.name2.endsWith("_in"))              // X_in as primary
            {
               this.swap = this.name2.endsWith("_in");
            } else if (this.name1.contains("_is") || this.name2.contains("_is"))              // X_is as primary
            {
               this.swap = this.name2.contains("_is");
            } else if (this.name1.startsWith("is_") || this.name2.startsWith("is_"))          // is_ as primary
            {
               this.swap = this.name2.startsWith("is_");
            } else if (this.name1.contains("_has") || this.name2.contains("_has"))            // X_has as secondary
            {
               this.swap = this.name1.contains("_has");
            } else if (this.name1.equals("larger_than") || this.name2.equals("larger_than"))  // swap smaller_than to primary
            {
               this.swap = this.name1.equals("larger_than");
            } else if (this.name1.equals("due_to") || this.name2.equals("due_to"))            // due_to as primary, cause_of secondary
            {
               this.swap = this.name2.equals("due_to");
            } else if (this.name1.equals("occurs_after") ||
                       this.name2.equals("occurs_after"))                                // occurs_after as primary, occurs_before secondary
                       {
               this.swap = this.name2.equals("occurs_after");
            }
         }
      }

      if (this.swap == null) {
         ConsoleUtil.println("No rel direction preference specified for " + this.name1 + "/" + this.name2 + " - using " + this.name1 +
                             " as primary");
         this.swap = false;
      }
   }
}

