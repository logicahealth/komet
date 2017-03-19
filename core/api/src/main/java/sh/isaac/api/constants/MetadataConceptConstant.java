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



package sh.isaac.api.constants;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

public abstract class MetadataConceptConstant
         implements ConceptSpecification {
   private final List<String> synonyms_    = new ArrayList<>();
   private final List<String> definitions_ = new ArrayList<>();
   private ConceptSpecification parent_ =
      null;  // Optional use - most constants have their parent set by the IsaacMetadataAuxiliary parent/child mechanism
   private final String primaryName_;
   private final UUID   uuid_;

   //~--- constructors --------------------------------------------------------

   protected MetadataConceptConstant(String primaryName, UUID uuid) {
      primaryName_ = primaryName;
      uuid_        = uuid;
   }

   protected MetadataConceptConstant(String primaryName, UUID uuid, String definition) {
      primaryName_ = primaryName;
      uuid_        = uuid;
      addDefinition(definition);
   }

   protected MetadataConceptConstant(String primaryName, UUID uuid, String definition, ConceptSpecification parent) {
      primaryName_ = primaryName;
      uuid_        = uuid;
      addDefinition(definition);
      setParent(parent);
   }

   //~--- methods -------------------------------------------------------------

   protected void addDefinition(String definition) {
      definitions_.add(definition);
   }

   protected void addSynonym(String synonym) {
      synonyms_.add(synonym);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * This method is identical to {@link #getPrimaryName()}
    * @see sh.isaac.api.component.concept.ConceptSpecification#getConceptDescriptionText()
    */
   @Override
   public String getConceptDescriptionText() {
      return primaryName_;
   }

   /**
    * @return The descriptions for this concept (if any). Will not return null.
    */
   public List<String> getDefinitions() {
      return definitions_;
   }

   /**
    * @return The nid for the concept.
    */
   public int getNid() {
      return Get.conceptService()
                .getConcept(getUUID())
                .getNid();
   }

   public ConceptSpecification getParent() {
      return parent_;
   }

   //~--- set methods ---------------------------------------------------------

   protected void setParent(ConceptSpecification parent) {
      parent_ = parent;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return The name for this concept, used to construct the FSN and preferred term.
    * This method is identical to {@link #getConceptDescriptionText()}
    */
   public String getPrimaryName() {
      return primaryName_;
   }

   @Override
   public UUID getPrimordialUuid() {
      return getUUID();
   }

   /**
    * @return The concept sequence for the concept.
    */
   public int getSequence() {
      return Get.conceptService()
                .getConcept(getUUID())
                .getConceptSequence();
   }

   /**
    * @return The alternate synonyms for this concept (if any) - does not
    * include the preferred synonym. Will not return null.
    */
   public List<String> getSynonyms() {
      return synonyms_;
   }

   /**
    * @return The UUID for the concept
    */
   public UUID getUUID() {
      return uuid_;
   }

   @Override
   public List<UUID> getUuidList() {
      return Arrays.asList(new UUID[] { uuid_ });
   }
}

