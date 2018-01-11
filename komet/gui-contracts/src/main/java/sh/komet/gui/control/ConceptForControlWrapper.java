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



package sh.komet.gui.control;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;

import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ConceptForControlWrapper
         implements ConceptSpecification {
   private final Manifold manifold;
   private final int      conceptNid;

   //~--- constructors --------------------------------------------------------

   public ConceptForControlWrapper(Manifold manifold, int conceptNid) {
      if (conceptNid == Integer.MAX_VALUE) {
         throw new IllegalStateException("Integer.MAX_VALUE for concept sequence.");
      }
      this.manifold        = manifold;
      this.conceptNid = conceptNid;

      //TODO HACK for resolving issue with ListView items toString() not calling service to early
      System.out.println("Created ConceptForControlWrapper: " + this.toString());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      Optional<String> description = getRegularName();

      if (description.isPresent()) {
         return description.get();
      }

      return "No description for: " + conceptNid;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid() {
      return conceptNid;
   }

   @Override
   public String getFullyQualifiedName() {
      return this.manifold.getFullySpecifiedDescriptionText(this.conceptNid);
   }

   @Override
   public Optional<String> getRegularName() {
      return Optional.of(this.manifold.getPreferredDescriptionText(this.conceptNid));
   }

   @Override
   public List<UUID> getUuidList() {
      return Get.concept(conceptNid)
                .getUuidList();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ConceptSpecification) {
         return this.conceptNid == ((ConceptSpecification) obj).getNid();
      }
      return false;
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.getNid());
   }
}

