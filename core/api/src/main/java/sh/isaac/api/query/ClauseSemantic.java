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



package sh.isaac.api.query;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 * Enumeration for semantics of query clauses.
 *
 * @author kec
 */
public enum ClauseSemantic {
   /**
    * Logical And connective clause.
    */
   AND(TermAux.AND_QUERY_CLAUSE),

   /** The and not. */
   AND_NOT(TermAux.AND_NOT_QUERY_CLAUSE),

   /**
    * Logical OR connective clause.
    */
   OR(TermAux.OR_QUERY_CLAUSE),

   /**
    * Logical NOT unary operator.
    */
   NOT(TermAux.NOT_QUERY_CLAUSE),

   /**
    * Logical XOR, used to determine what changed in membership between two
    * versions.
    */
   XOR(TermAux.XOR_QUERY_CLAUSE),

   /**
    * Test to see if component has changed from the previous version.
    */
   CHANGED_FROM_PREVIOUS_VERSION(TermAux.CHANGED_FROM_PREVIOUS_VERSION_QUERY_CLAUSE),

   /**
    * Test to see if the concept is a logical child of another.
    */
   CONCEPT_IS_CHILD_OF(TermAux.CONCEPT_IS_CHILD_OF_QUERY_CLAUSE),

   /**
    * Test to see if there is a concept matching the input concept spec.
    */
   CONCEPT_IS(TermAux.CONCEPT_IS_QUERY_CLAUSE),

   /**
    * Test to see if the concept is a logical descendent of another.
    */
   CONCEPT_IS_DESCENDENT_OF(TermAux.CONCEPT_IS_DESCENDENT_OF_QUERY_CLAUSE),

   /**
    * Test to see if the concept is a logical kind of another.
    */
   CONCEPT_IS_KIND_OF(TermAux.CONCEPT_IS_KIND_OF_QUERY_CLAUSE),

   /**
    * Test to see if a component is a member of an ASSEMBLAGE.
    */
   ASSEMBLAGE_CONTAINS_COMPONENT(TermAux.ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE),

   /**
    * Test to see if an active description on a concept matches a Lucene query
    * criterion.
    */
   DESCRIPTION_ACTIVE_LUCENE_MATCH(TermAux.DESCRIPTION_LUCENE_ACTIVE_ONLY_MATCH_QUERY_CLAUSE),

   /**
    * Test to see if an active description on a concept matches matches a regex
    * expression.
    */
   DESCRIPTION_ACTIVE_REGEX_MATCH(TermAux.DESCRIPTION_REGEX_ACTIVE_ONLY_MATCH_QUERY_CLAUSE),

   /**
    * Test to see if any description (active or inactive) on a concept matches
    * a Lucene query criterion.
    */
   DESCRIPTION_LUCENE_MATCH(TermAux.DESCRIPTION_LUCENE_MATCH_QUERY_CLAUSE),

   /**
    * Test to see if any description (active or inactive) on a concept matches
    * matches a regex expression.
    */
   DESCRIPTION_REGEX_MATCH(TermAux.DESCRIPTION_REGEX_MATCH_QUERY_CLAUSE),

   /**
    * Test to see if a relationship matches a specified relationship type.
    */
   REL_TYPE(TermAux.REL_TYPE_QUERY_CLAUSE),

   /**
    * Test to see if a relationship matches a specified relationship type and
    * relationship destination restriction.
    */
   REL_RESTRICTION(TermAux.REL_RESTRICTION_QUERY_CLAUSE),
   
   /**
    * Join two assemblages in the result set when criterion are met 
    */
   JOIN(TermAux.JOIN_QUERY_CLAUSE),
   
   COMPONENT_IS_ACTIVE(TermAux.ACTIVE_QUERY_CLAUSE),
   
   COMPONENT_IS_INACTIVE(TermAux.INACTIVE_QUERY_CLAUSE),
   
   REFERENCED_COMPONENT_IS_ACTIVE(TermAux.REFERENCED_COMPONENT_IS_ACTIVE),

   REFERENCED_COMPONENT_IS_INACTIVE(TermAux.REFERENCED_COMPONENT_IS_INACTIVE),
   
   REFERENCED_COMPONENT_IS(TermAux.REFERENCED_COMPONENT_IS),
   
   REFERENCED_COMPONENT_IS_MEMBER_OF(TermAux.REFERENCED_COMPONENT_IS_MEMBER_OF),
   
   REFERENCED_COMPONENT_IS_NOT_MEMBER_OF(TermAux.REFERENCED_COMPONENT_IS_NOT_MEMBER_OF),
   //
   COMPONENT_IS_MEMBER_OF(TermAux.COMPONENT_IS_MEMBER_OF),
   
   COMPONENT_IS_NOT_MEMBER_OF(TermAux.COMPONENT_IS_NOT_MEMBER_OF),
   
   REFERENCED_COMPONENT_IS_KIND_OF(TermAux.REFERENCED_COMPONENT_IS_KIND_OF),
   
   REFERENCED_COMPONENT_IS_NOT_KIND_OF(TermAux.REFERENCED_COMPONENT_IS_NOT_KIND_OF),
   
   SEMANTIC_CONTAINS_TEXT(TermAux.ASSEMBLAGE_CONTAINS_STRING_QUERY_CLAUSE),

   CONCEPT_HAS_TAXONOMY_DISTANCE_FROM(TermAux.CONCEPT_HAS_TAXONOMY_DISTANCE_FROM);
   
   final ConceptSpecification clauseConcept;

    private ClauseSemantic(ConceptSpecification clauseConcept) {
        this.clauseConcept = clauseConcept;
    }

    public ConceptSpecification getClauseConcept() {
        return clauseConcept;
    }
   
   
   
}

