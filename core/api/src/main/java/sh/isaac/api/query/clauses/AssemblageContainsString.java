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
import java.util.Map;

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
import sh.isaac.api.query.LetItemKey;
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
   LetItemKey queryTextKey;

   /** The stamp coordinate key. */
   @XmlElement
   LetItemKey stampCoordinateKey = Query.DEFAULT_MANIFOLD_COORDINATE_KEY;

   /** The refset spec key. */
   @XmlElement
   LetItemKey assemblageSpecKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new refset contains string.
    */
   public AssemblageContainsString() {}

   /**
    * Instantiates a new refset contains string.
    *
    * @param enclosingQuery the enclosing query
    * @param assemblageSpecKey the refset spec key
    * @param queryTextKey the query text
    * @param stampCoordinateKey the manifold coordinate key
    */
   public AssemblageContainsString(Query enclosingQuery, LetItemKey assemblageSpecKey, LetItemKey queryTextKey, LetItemKey stampCoordinateKey) {
      super(enclosingQuery);
      this.assemblageSpecKey     = assemblageSpecKey;
      this.queryTextKey         = queryTextKey;
      this.stampCoordinateKey = stampCoordinateKey;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compute possible components.
    *
    * @param incomingPossibleComponents the incoming possible components
    * @return the nid set
    */
   @Override
   public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
      throw new UnsupportedOperationException();

      // TODO FIX BACK UP
//    ManifoldCoordinate manifoldCoordinate = (manifoldCoordinate) this.enclosingQuery.getLetDeclarations().get(stampCoordinateKey);
//    ConceptSpec refsetSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(assemblageSpecKey);
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
//                if (rsv.getString1().toLowerCase().contains(queryTextKey.toLowerCase())) {
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
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {}

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.ASSEMBLAGE_CONTAINS_STRING;
    }
   
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
                 .add(this.assemblageSpecKey);
      whereClause.getLetKeys()
                 .add(this.queryTextKey);
      whereClause.getLetKeys()
                 .add(this.stampCoordinateKey);
      return whereClause;
   }
   
      @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.ASSEMBLAGE_CONTAINS_STRING_QUERY_CLAUSE;
   }

    public LetItemKey getQueryTextKey() {
        return queryTextKey;
    }

    public void setQueryTextKey(LetItemKey queryTextKey) {
        this.queryTextKey = queryTextKey;
    }

    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKey;
    }

    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKey = stampCoordinateKey;
    }

    public LetItemKey getAssemblageSpecKey() {
        return assemblageSpecKey;
    }

    public void setAssemblageSpecKey(LetItemKey assemblageSpecKey) {
        this.assemblageSpecKey = assemblageSpecKey;
    }

}

