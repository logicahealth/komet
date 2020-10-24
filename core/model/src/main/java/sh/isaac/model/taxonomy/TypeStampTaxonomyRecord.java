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
package sh.isaac.model.taxonomy;

import java.util.EnumSet;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.TaxonomyFlag;
import sh.isaac.api.navigation.TypeStampNavigationRecord;

/**
 * The Class TypeStampTaxonomyRecord.
 */
public class TypeStampTaxonomyRecord implements sh.isaac.api.navigation.TypeStampNavigationRecord {

   /** The type sequence. */
   int typeNid;
   /** The stamp sequence. */
   int stamp;
   /** The taxonomy flags. */
   int taxonomyFlagBits;

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
      this.stamp = stampSequence;
      this.taxonomyFlagBits = taxonomyFlags;
   }
   
   @Override
   public long getTypeStampKey() {
       return (long)this.typeNid << 32 | this.stamp & 0xFFFFFFFFL;
   }
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
      if (this.stamp != other.stamp) {
         return false;
      }
      if (this.typeNid != other.typeNid) {
         return false;
      }
      return this.taxonomyFlagBits == other.taxonomyFlagBits;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 89 * hash + this.typeNid;
      hash = 89 * hash + this.stamp;
      hash = 89 * hash + this.taxonomyFlagBits;
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
      sb.append(this.stamp);
      sb.append(" ");
      sb.append(Get.stampService().describeStampSequence(this.stamp));
      sb.append(" ");
      sb.append(getTaxonomyFlagsAsEnum());
      sb.append("»");
      return sb.toString();
   }

   @Override
   public int getStampSequence() {
      return this.stamp;
   }

   @Override
   public int getTaxonomyFlags() {
      return this.taxonomyFlagBits;
   }

   @Override
   public EnumSet<TaxonomyFlag> getTaxonomyFlagsAsEnum() {
      return TaxonomyFlag.getTaxonomyFlags(this.taxonomyFlagBits);
   }

   @Override
   public int getTypeNid() {
      return this.typeNid;
   }
   
   public boolean merge(TypeStampNavigationRecord another) {
       if (this.typeNid == another.getTypeNid() &&
               this.stamp ==  another.getStampSequence()) {
           this.taxonomyFlagBits = this.taxonomyFlagBits | another.getTaxonomyFlags();
           return true;
       }
       return false;
   }
}
