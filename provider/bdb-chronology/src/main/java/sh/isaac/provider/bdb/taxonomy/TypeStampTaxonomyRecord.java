/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.bdb.taxonomy;

import java.util.EnumSet;
import sh.isaac.api.Get;

/**
 * The Class TypeStampTaxonomyRecord.
 */
public class TypeStampTaxonomyRecord {

   /** The type sequence. */
   int typeNid;
   /** The stamp sequence. */
   int stampSequence;
   /** The taxonomy flags. */
   int taxonomyFlags;

   //~--- constructors -----------------------------------------------------

   /**
    * Instantiates a new type stamp taxonomy record.
    *
    * @param typeNid the type sequence
    * @param stampSequence the stamp sequence
    * @param taxonomyFlags the taxonomy flags
    */
   public TypeStampTaxonomyRecord(int typeNid, int stampSequence, int taxonomyFlags) {
      if (typeNid >= 0) {
         throw new IllegalStateException("typeNid must be negative. Found: " + typeNid);
      }
      this.typeNid = typeNid;
      this.stampSequence = stampSequence;
      this.taxonomyFlags = taxonomyFlags;
   }

   //~--- methods ----------------------------------------------------------
   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final TypeStampTaxonomyRecord other = (TypeStampTaxonomyRecord) obj;
      if (this.stampSequence != other.stampSequence) {
         return false;
      }
      if (this.typeNid != other.typeNid) {
         return false;
      }
      return this.taxonomyFlags == other.taxonomyFlags;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 89 * hash + this.typeNid;
      hash = 89 * hash + this.stampSequence;
      hash = 89 * hash + this.taxonomyFlags;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("«");
      sb.append(Get.conceptDescriptionText(this.typeNid));
      sb.append(" <");
      sb.append(this.typeNid);
      sb.append("> ss:");
      sb.append(this.stampSequence);
      sb.append(" ");
      sb.append(Get.stampService().describeStampSequence(this.stampSequence));
      sb.append(" ");
      sb.append(getTaxonomyFlagsAsEnum());
      sb.append("»");
      return sb.toString();
   }

   //~--- get methods ------------------------------------------------------

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   public int getStampSequence() {
      return this.stampSequence;
   }

   /**
    * Gets the taxonomy flags.
    *
    * @return the taxonomy flags
    */
   public int getTaxonomyFlags() {
      return this.taxonomyFlags;
   }

   /**
    * Gets the taxonomy flags as enum.
    *
    * @return the taxonomy flags as enum
    */
   public EnumSet<TaxonomyFlag> getTaxonomyFlagsAsEnum() {
      return TaxonomyFlag.getTaxonomyFlags(this.taxonomyFlags);
   }

   /**
    * Gets the type sequence.
    *
    * @return the type sequence
    */
   public int getTypeNid() {
      return this.typeNid;
   }
   
}
