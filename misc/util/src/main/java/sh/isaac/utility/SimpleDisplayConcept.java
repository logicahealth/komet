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



package sh.isaac.utility;

//~--- JDK imports ------------------------------------------------------------

import java.util.function.Function;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptVersion;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link SimpleDisplayConcept}
 *
 * A very simple concept container, useful for things like ComboBoxes, or lists
 * where we want to display workbench concepts, and still have a link to the underlying
 * concept (via the nid)
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SimpleDisplayConcept
         implements Comparable<SimpleDisplayConcept> {
   private boolean           uncommitted_ = false;
   protected String          description_;
   private int               nid_;
   private Supplier<Boolean> customLogic_;

   //~--- constructors --------------------------------------------------------

   public SimpleDisplayConcept(ConceptSnapshot c) {
      this(c.getChronology(), null);
   }

   public SimpleDisplayConcept(Integer conceptId) {
      this(conceptId, null);
   }

   public SimpleDisplayConcept(String description) {
      this(description, 0);
   }

   public SimpleDisplayConcept(ConceptChronology<? extends ConceptVersion<?>> c,
                               Function<ConceptChronology<? extends ConceptVersion<?>>, String> descriptionReader) {
      final Function<ConceptChronology<? extends ConceptVersion<?>>, String> dr = ((descriptionReader == null)
                                                                             ? (conceptVersion) -> {
               return ((conceptVersion == null) ? ""
               : Frills.getDescription(conceptVersion.getConceptSequence())
                       .get());
            }
            : descriptionReader);

      this.description_ = dr.apply(c);
      this.nid_         = (c == null) ? 0
                                 : c.getNid();
      this.customLogic_ = null;
   }

   /**
    * @param conceptId nid or sequence
    * @param descriptionReader - optional
    */
   public SimpleDisplayConcept(Integer conceptId,
                               Function<ConceptChronology<? extends ConceptVersion<?>>, String> descriptionReader) {
      this(((conceptId == null) ? null
                                : Get.conceptService()
                                     .getConcept(conceptId)), descriptionReader);
   }

   public SimpleDisplayConcept(String description, int nid) {
      this(description, nid, null);
   }

   /**
    *
    * @param description
    * @param nid
    * @param customLogic - typically used to allow a changeListener to ignore a change.
    * See {@link #shouldIgnoreChange()}
    */
   public SimpleDisplayConcept(String description, int nid, Supplier<Boolean> customLogic) {
      this.description_ = description;
      this.nid_         = nid;
      this.customLogic_ = customLogic;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public SimpleDisplayConcept clone() {
      return new SimpleDisplayConcept(this.description_, this.nid_, this.customLogic_);
   }

   /**
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(SimpleDisplayConcept o) {
      return new SimpleDisplayConceptComparator().compare(this, o);
   }

   /**
    * Return back whatever customLogic supplier was passed in
    */
   public Supplier<Boolean> customLogic() {
      return this.customLogic_;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj instanceof SimpleDisplayConcept) {
         final SimpleDisplayConcept other = (SimpleDisplayConcept) obj;

         return (this.nid_ == other.nid_) && StringUtils.equals(this.description_, other.description_);
      }

      return false;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + ((this.description_ == null) ? 0
            : this.description_.hashCode());
      result = prime * result + this.nid_;
      return result;
   }

   @Override
   public String toString() {
      return this.description_;
   }

   //~--- get methods ---------------------------------------------------------

   public String getDescription() {
      return this.description_;
   }

   public int getNid() {
      return this.nid_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setNid(int nid) {
      this.nid_ = nid;
   }

   //~--- get methods ---------------------------------------------------------

   public boolean isUncommitted() {
      return this.uncommitted_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUncommitted(boolean val) {
      this.uncommitted_ = val;
   }
}

