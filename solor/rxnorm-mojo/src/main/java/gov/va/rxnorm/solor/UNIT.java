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



package gov.va.rxnorm.solor;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.util.UuidT5Generator;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum UNIT.
 */
public enum UNIT {
   /** The mg. */
   MG("MilliGrams"),

   /** The ml. */
   ML("MilliLiter"),

   /** The percent. */
   PERCENT("Percent", "%"),

   /** The unt. */
   UNT("Units"),

   /** The actuat. */
   ACTUAT("Actuation"),

   /** The hr. */
   HR("Hour"),

   /** The meq. */
   MEQ("MilliEquivalent"),

   /** The cells. */
   CELLS("Cells"),

   /** The bau. */
   BAU("Bioequivalent Allergy Units"),

   /** The mgml. */
   MGML("MilliGrams per MilliLiter", "MG/ML"),

   /** The untml. */
   UNTML("Units per MilliLiter", "UNT/ML"),

   /** The mgmg. */
   MGMG("Milligrams per Milligram", "MG/MG"),

   /** The meqml. */
   MEQML("MilliEquivalent per Milliliter", "MEQ/ML"),

   /** The bauml. */
   BAUML("Bioequivalent Allergy Units per MilliLiter", "BAU/ML"),

   /** The au. */
   AU("Arbitrary Unit"),

   /** The sqcm. */

   // not sure about this
   SQCM("Square Centimeter"),

   /** The mci. */

   // probably
   MCI("millicurie"),

   /** The pnu. */

   // not sure about this
   PNU("Protein nitrogen unit"),

   /** The cellsml. */
   CELLSML("Cells per MilliLiter", "CELLS/ML"),

   /** The auml. */
   AUML("Arbitrary Unit per MilliLiter", "AU/ML"),

   /** The mlml. */
   MLML("MilliLiter per MilliLiter", "ML/ML"),

   /** The mghr. */
   MGHR("MilliGrams per Hour", "MG/HR"),

   /** The mgactuat. */
   MGACTUAT("MilliGrams per Actuation", "MG/ACTUAT"),

   /** The untmg. */
   UNTMG("Units per MilliGram", "UNT/MG"),

   /** The meqmg. */
   MEQMG("MilliEquivalent per MilliGram", "MEQ/MG"),

   /** The untactuat. */
   UNTACTUAT("Units per Actuation", "UNT/ACTUAT"),

   /** The ir. */
   IR("Index of Reactivity", "IR"),

   /** The mciml. */

   // maybe?
   MCIML("milicurie per MilliLiter", "MCI/ML"),

   /** The mgsqcm. */

   // maybe?
   MGSQCM("MilliGram per Square Centimeter", "MG/SQCM"),

   /** The pnuml. */

   // probably
   PNUML("Protein nitrogen unit per MilliLiter", "PNU/ML");

   /** The full name. */
   private final String fullName;

   /** The alt name. */
   private String altName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new unit.
    *
    * @param fsn the fsn
    */
   private UNIT(String fsn) {
      this.fullName = fsn;
   }

   /**
    * Instantiates a new unit.
    *
    * @param fsn the fsn
    * @param altName the alt name
    */
   private UNIT(String fsn, String altName) {
      this.fullName = fsn;
      this.altName  = altName;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Parses the.
    *
    * @param value the value
    * @return the unit
    */
   public static UNIT parse(String value) {
      for (final UNIT u: UNIT.values()) {
         if (u.name().equals(value.trim()) || value.trim().equals(u.altName)) {
            return u;
         }
      }

      throw new RuntimeException("Can't match " + value);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Note that by chance, if this returns a type 3 UUID, then it is an existing SCT concept.
    * If it returns type 5, we invented the UUID, and someone needs to make the concept.
    *
    * @return the concept UUID
    */
   public UUID getConceptUUID() {
      // Note, I would normally define these in the construtor of the enum, as constants, but maven is #@%$#%#@ broken and DIES
      // when it encounters (perfectly valid) code that it can't parse, because it is broken.
      switch (this) {
      case MEQ:
         return UUID.fromString("2def410e-b419-341a-a469-4b97c3e4fe16");

      case HR:
         return UUID.fromString("aca700b1-1500-3c2f-bcc6-87f3121e7913");

      case MG:
         return UUID.fromString("89cb8d09-3a3c-31e6-94ea-05fe8ff17551");

      case ML:
         return UUID.fromString("f48333ed-4449-3a48-b7b1-7d9f0a1df0e6");

      case PERCENT:
         return UUID.fromString("31e4aab3-5b9b-39b7-89a5-52b4738e03a6");

      case UNT:
         return UUID.fromString("17055d89-84e3-3e12-9fb1-1bc4c75a122d");

      // real concepts above here (from sct)
      // concepts that need to be constructed, below here
      // TODO can find constants for the ones above?
      case ACTUAT:
      case CELLS:
      case BAU:
      case MGML:
      case UNTML:
      case AU:
      case BAUML:
      case MCI:
      case MEQML:
      case MGMG:
      case PNU:
      case SQCM:
      case AUML:
      case CELLSML:
      case MGACTUAT:
      case MGHR:
      case MLML:
      case UNTMG:
      case MEQMG:
      case IR:
      case MCIML:
      case MGSQCM:
      case PNUML:
      case UNTACTUAT:
         return UuidT5Generator.get(this.name());

      default:
         throw new RuntimeException("oops");
      }
   }

   /**
    * Gets the full name.
    *
    * @return the full name
    */
   public String getFullName() {
      return this.fullName;
   }

   /**
    * Checks for real SCT concept.
    *
    * @return true, if successful
    */
   public boolean hasRealSCTConcept() {
      if (getConceptUUID().toString()
                          .charAt(14) == '3') {
         return true;
      } else if (getConceptUUID().toString()
                                 .charAt(14) == '5') {
         return false;
      } else {
         throw new RuntimeException("oops");
      }
   }
}

