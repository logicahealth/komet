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
 * {@link SimpleDisplayConcept}
 *
 * A very simple concept container, useful for things like ComboBoxes, or lists
 * where we want to display workbench concepts, and still have a link to the underlying
 * concept (via the nid).
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SimpleDisplayConcept
         implements Comparable<SimpleDisplayConcept> {
   /** The uncommitted. */
   private boolean uncommitted = false;

   /** The description. */
   protected String description;

   /** The nid. */
   private int nid;

   /** The custom logic. */
   private Supplier<Boolean> customLogic;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new simple display concept.
    *
    * @param c the c
    */
   public SimpleDisplayConcept(ConceptSnapshot c) {
      this(c.getChronology(), null);
   }

   /**
    * Instantiates a new simple display concept.
    *
    * @param conceptId the concept id
    */
   public SimpleDisplayConcept(Integer conceptId) {
      this(conceptId, null);
   }

   /**
    * Instantiates a new simple display concept.
    *
    * @param description the description
    */
   public SimpleDisplayConcept(String description) {
      this(description, 0);
   }

   /**
    * Instantiates a new simple display concept.
    *
    * @param c the c
    * @param descriptionReader the description reader
    */
   public SimpleDisplayConcept(ConceptChronology<? extends ConceptVersion<?>> c,
                               Function<ConceptChronology<? extends ConceptVersion<?>>, String> descriptionReader) {
      final Function<ConceptChronology<? extends ConceptVersion<?>>, String> dr = ((descriptionReader == null)
                                                                                   ? (conceptVersion) -> {
               return ((conceptVersion == null) ? ""
               : Frills.getDescription(conceptVersion.getConceptSequence())
                       .get());
            }
            : descriptionReader);

      this.description = dr.apply(c);
      this.nid         = (c == null) ? 0
                                      : c.getNid();
      this.customLogic = null;
   }

   /**
    * Instantiates a new simple display concept.
    *
    * @param conceptId nid or sequence
    * @param descriptionReader - optional
    */
   public SimpleDisplayConcept(Integer conceptId,
                               Function<ConceptChronology<? extends ConceptVersion<?>>, String> descriptionReader) {
      this(((conceptId == null) ? null
                                : Get.conceptService()
                                     .getConcept(conceptId)), descriptionReader);
   }

   /**
    * Instantiates a new simple display concept.
    *
    * @param description the description
    * @param nid the nid
    */
   public SimpleDisplayConcept(String description, int nid) {
      this(description, nid, null);
   }

   /**
    * Instantiates a new simple display concept.
    *
    * @param description the description
    * @param nid the nid
    * @param customLogic - typically used to allow a changeListener to ignore a change.
    * See {@link #shouldIgnoreChange()}
    */
   public SimpleDisplayConcept(String description, int nid, Supplier<Boolean> customLogic) {
      this.description = description;
      this.nid         = nid;
      this.customLogic = customLogic;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Clone.
    *
    * @return the simple display concept
    */
   @Override
   public SimpleDisplayConcept clone() {
      return new SimpleDisplayConcept(this.description, this.nid, this.customLogic);
   }

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(SimpleDisplayConcept o) {
      return new SimpleDisplayConceptComparator().compare(this, o);
   }

   /**
    * Return back whatever customLogic supplier was passed in.
    *
    * @return the supplier
    */
   public Supplier<Boolean> customLogic() {
      return this.customLogic;
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj instanceof SimpleDisplayConcept) {
         final SimpleDisplayConcept other = (SimpleDisplayConcept) obj;

         return (this.nid == other.nid) && StringUtils.equals(this.description, other.description);
      }

      return false;
   }

   /**
    * Hash code.
    *
    * @return the int
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + ((this.description == null) ? 0
            : this.description.hashCode());
      result = prime * result + this.nid;
      return result;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.description;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description.
    *
    * @return the description
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   public int getNid() {
      return this.nid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the nid.
    *
    * @param nid the new nid
    */
   public void setNid(int nid) {
      this.nid = nid;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if uncommitted.
    *
    * @return true, if uncommitted
    */
   public boolean isUncommitted() {
      return this.uncommitted;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the uncommitted.
    *
    * @param val the new uncommitted
    */
   public void setUncommitted(boolean val) {
      this.uncommitted = val;
   }
}

