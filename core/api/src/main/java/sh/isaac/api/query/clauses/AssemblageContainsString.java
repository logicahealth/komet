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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.bootstrap.TermAux;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 *
 * @author dylangrald
 */
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * .
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class AssemblageContainsString
        extends LeafClause {
   /** The query text. */
   @XmlElement
   String queryText;

   /** The view coordinate key. */
   @XmlElement
   String viewCoordinateKey;

   /** The cache. */
   NidSet cache;

   /** The refset spec key. */
   @XmlElement
   String refsetSpecKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new refset contains string.
    */
   public AssemblageContainsString() {}

   /**
    * Instantiates a new refset contains string.
    *
    * @param enclosingQuery the enclosing query
    * @param refsetSpecKey the refset spec key
    * @param queryText the query text
    * @param viewCoordinateKey the view coordinate key
    */
   public AssemblageContainsString(Query enclosingQuery, String refsetSpecKey, String queryText, String viewCoordinateKey) {
      super(enclosingQuery);
      this.refsetSpecKey     = refsetSpecKey;
      this.queryText         = queryText;
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
      throw new UnsupportedOperationException();

      // TODO FIX BACK UP
//    ManifoldCoordinate manifoldCoordinate = (manifoldCoordinate) this.enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
//    ConceptSpec refsetSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(refsetSpecKey);
//
//    int refsetNid = refsetSpec.getNid();
//    ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(viewCoordinate, refsetNid);
//
//    for (RefexVersionBI<?> rm : conceptVersion.getCurrentRefsetMembers(viewCoordinate)) {
//        switch (rm.getRefexType()) {
//            case CID_STR:
//            case CID_CID_CID_STRING:
//            case CID_CID_STR:
//            case STR:
//                RefexStringVersionBI rsv = (RefexStringVersionBI) rm;
//                if (rsv.getString1().toLowerCase().contains(queryText.toLowerCase())) {
//                    getResultsCache().add(refsetNid);
//                }
//            default:
//            //do nothing
//
//        }
//    }
//
//    return getResultsCache();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the compute phases.
    *
    * @return the compute phases
    */
   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return PRE_ITERATION;
   }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    * @return the query matches
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {}

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.ASSEMBLAGE_CONTAINS_STRING);
      whereClause.getLetKeys()
                 .add(this.refsetSpecKey);
      whereClause.getLetKeys()
                 .add(this.queryText);
      whereClause.getLetKeys()
                 .add(this.viewCoordinateKey);
      return whereClause;
   }
   
      @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.ASSEMBLAGE_CONTAINS_STRING_QUERY_CLAUSE;
   }

}

