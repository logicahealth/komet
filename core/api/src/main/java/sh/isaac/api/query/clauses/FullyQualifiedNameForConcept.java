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



package sh.isaac.api.query.clauses;

//~--- JDK imports ------------------------------------------------------------


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.ParentClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;
import sh.isaac.api.component.semantic.version.DescriptionVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Retrieves the fully specified names for a result set of concepts.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class FullyQualifiedNameForConcept
        extends ParentClause {
   /**
    * Instantiates a new fully specified name for concept.
    */
   public FullyQualifiedNameForConcept() {}

   /**
    * Instantiates a new fully specified name for concept.
    *
    * @param enclosingQuery the enclosing query
    * @param child the child
    */
   public FullyQualifiedNameForConcept(Query enclosingQuery, Clause child) {
      super(enclosingQuery, child);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compute components.
    *
    * @param incomingComponents the incoming components
    * @return the nid set
    */
   @Override
   public NidSet computeComponents(NidSet incomingComponents) {
      final LanguageCoordinate languageCoordinate         = getEnclosingQuery().getLanguageCoordinate();
      final StampCoordinate    stampCoordinate            = getEnclosingQuery().getStampCoordinate();
      final NidSet             outgoingFullySpecifiedNids = new NidSet();

      for (final Clause childClause: getChildren()) {
         final NidSet             childPossibleComponentNids =
            childClause.computePossibleComponents(incomingComponents);
         final ConceptSequenceSet conceptSequenceSet         = ConceptSequenceSet.of(childPossibleComponentNids);

         Get.conceptService()
            .getConceptChronologyStream(conceptSequenceSet)
            .forEach((conceptChronology) -> {
                        final LatestVersion<? extends DescriptionVersion> desc =
                           conceptChronology.getFullyQualifiedNameDescription(languageCoordinate, stampCoordinate);

                        if (desc.isPresent()) {
                           outgoingFullySpecifiedNids.add(desc
                                 .get()
                                 .getNid());
                        }
                     });
      }

      return outgoingFullySpecifiedNids;
   }

   /**
    * Compute possible components.
    *
    * @param incomingPossibleComponents the incoming possible components
    * @return the nid set
    */
   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
      return incomingPossibleComponents;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.FULLY_QUALIFIED_NAME_FOR_CONCEPT);

      for (final Clause clause: getChildren()) {
         whereClause.getChildren()
                    .add(clause.getWhereClause());
      }

      return whereClause;
   }
   
      @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.FULLY_QUALIFIED_NAME_FOR_CONCEPT_QUERY_CLAUSE;
   }
   @Override
   public Clause[] getAllowedSubstutitionClauses() {
      return new Clause[] {new ConceptForComponent(), new FullyQualifiedNameForConcept(), new PreferredNameForConcept()};
   }

   @Override
   public Clause[] getAllowedSiblingClauses() {
      return new Clause[0];
   }

}
//~--- JDK imports ------------------------------------------------------------
