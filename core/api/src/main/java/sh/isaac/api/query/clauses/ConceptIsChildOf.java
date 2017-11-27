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

import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * Computes the set of concepts that are a child of the input concept. The set
 * of children of a given concept is the set of all concepts that lie one step
 * below the input concept in the terminology hierarchy. This
 * <code>Clause</code> has an optional parameter for a previous
 * <code>ViewCoordinate</code>, which allows for queries of previous versions.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ConceptIsChildOf
        extends LeafClause {
   /** The child of spec key. */
   @XmlElement
   String childOfSpecKey;

   /** The view coordinate key. */
   @XmlElement
   String viewCoordinateKey;

   private ConceptSpecification childOfSpecification;
   private ManifoldCoordinate manifoldCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept is child of.
    */
   public ConceptIsChildOf() {}

   /**
    * Instantiates a new concept is child of.
    *
    * @param enclosingQuery the enclosing query
    * @param kindOfSpecKey the kind of spec key
    * @param viewCoordinateKey the view coordinate key
    */
   public ConceptIsChildOf(Query enclosingQuery, String kindOfSpecKey, String viewCoordinateKey) {
      super(enclosingQuery);
      this.childOfSpecKey    = kindOfSpecKey;
      this.viewCoordinateKey = viewCoordinateKey;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compute possible components.
    *
    * @param incomingPossibleComponents the incoming possible components
    * @return the nid set
    */
   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
      final int parentNid = this.childOfSpecification.getNid();
      final NidSet childrenOfSequenceSet =
              NidSet.of(
                      Get.taxonomyService().getSnapshot(this.manifoldCoordinate).getTaxonomyChildNids(parentNid));
      getResultsCache().or(childrenOfSequenceSet);
      return getResultsCache();
   }

   //~--- get methods ---------------------------------------------------------


   public void setChildOfSpecification(ConceptSpecification childOfSpecification) {
      this.childOfSpecification = childOfSpecification;
   }

   public void setManifoldCoordinate(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }

   /**
    * Gets the compute phases.
    *
    * @return the compute phases
    */
   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return PRE_AND_POST_ITERATION;
   }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    * @return the query matches
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      // Nothing to do...
   }

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.CONCEPT_IS_CHILD_OF);
      whereClause.getLetKeys()
                 .add(this.childOfSpecKey);
      whereClause.getLetKeys()
                 .add(this.viewCoordinateKey);
      return whereClause;
   }
   
      @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.CONCEPT_IS_CHILD_OF_QUERY_CLAUSE;
   }

}

