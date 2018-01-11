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
import java.util.List;
import java.util.UUID;

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 * The Class MetadataConceptConstant.
 */
public abstract class MetadataConceptConstant
         extends ConceptProxy {
   /** The synonyms. */
   private final List<String> synonyms = new ArrayList<>();

   /** The definitions. */
   private final List<String> definitions = new ArrayList<>();

   /** The parent. */
   private ConceptSpecification parent =
      null;  // Optional use - most constants have their parent set by the IsaacMetadataAuxiliary parent/child mechanism

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new metadata concept constant.
    *
    * @param primaryName the primary name
    * @param uuid the uuid
    */
   protected MetadataConceptConstant(String primaryName, UUID uuid) {
      super(primaryName, uuid);
   }

   /**
    * Instantiates a new metadata concept constant.
    *
    * @param primaryName the primary name
    * @param uuid the uuid
    * @param definition the definition
    */
   protected MetadataConceptConstant(String primaryName, UUID uuid, String definition) {
   	super(primaryName, uuid);
      addDefinition(definition);
   }

   /**
    * Instantiates a new metadata concept constant.
    *
    * @param primaryName the primary name
    * @param uuid the uuid
    * @param definition the definition
    * @param parent the parent
    */
   protected MetadataConceptConstant(String primaryName, UUID uuid, String definition, ConceptSpecification parent) {
   	super(primaryName, uuid);
      addDefinition(definition);
      setParent(parent);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the definition.
    *
    * @param definition the definition
    */
   protected void addDefinition(String definition) {
      this.definitions.add(definition);
   }

   /**
    * Adds the synonym.
    *
    * @param synonym the synonym
    */
   protected void addSynonym(String synonym) {
      this.synonyms.add(synonym);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the definitions.
    *
    * @return The descriptions for this concept (if any). Will not return null.
    */
   public List<String> getDefinitions() {
      return this.definitions;
   }

   /**
    * Gets the parent.
    *
    * @return the parent
    */
   public ConceptSpecification getParent() {
      return this.parent;
   }
   

   /**
    * Gets the synonyms.
    *
    * @return The alternate synonyms for this concept (if any) - does not
    * include the preferred synonym. Will not return null.
    */
   public List<String> getSynonyms() {
      return this.synonyms;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the parent.
    *
    * @param parent the new parent
    */
   protected void setParent(ConceptSpecification parent) {
      this.parent = parent;
   }
}

