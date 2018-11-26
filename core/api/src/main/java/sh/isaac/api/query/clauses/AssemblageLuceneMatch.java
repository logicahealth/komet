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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;
//import sh.isaac.provider.query.lucene.indexers.SemanticIndexer;

//~--- classes ----------------------------------------------------------------

/**
 * Retrieves the refset matching the input SNOMED id.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class AssemblageLuceneMatch
        extends LeafClause {
   /** The lucene match key. */
   @XmlElement
   LetItemKey queryStringKey;

   /** the manifold coordinate key. */
   @XmlElement
   LetItemKey manifoldCoordinateKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new assemblage lucene match.
    */
   public AssemblageLuceneMatch() {}

   /**
    * Instantiates a new assemblage lucene match.
    *
    * @param enclosingQuery the enclosing query
    * @param queryStringKey the lucene match key
    * @param manifoldCoordinateKey the manifold coordinate key
    */
   public AssemblageLuceneMatch(Query enclosingQuery, LetItemKey queryStringKey, LetItemKey manifoldCoordinateKey) {
      super(enclosingQuery);
      this.queryStringKey    = queryStringKey;
      this.manifoldCoordinateKey = manifoldCoordinateKey;
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
      this.enclosingQuery.getLetDeclarations()
                         .get(this.manifoldCoordinateKey);
      this.enclosingQuery.getLetDeclarations()
                         .get(this.queryStringKey);

      final NidSet        nids = new NidSet();
      throw new UnsupportedOperationException();
//      final SemanticIndexer si   = LookupService.get()
//                                              .getService(SemanticIndexer.class);
//
//      if (si == null) {
//         throw new IllegalStateException("semanticIndexer is null");
//      }
//
////    List<SearchResult> queryResults = si.query(Long.parseLong(luceneMatch), 1000);
////    queryResults.stream().forEach((s) -> {
////        nids.add(s.nid);
////    });
//      // TODO FIX BACK UP
////    nids.stream().forEach((nid) -> {
////        Optional<? extends ObjectChronology<? extends StampedVersion>> optionalObject
////                = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
////        if (optionalObject.isPresent()) {
////            Optional<? extends LatestVersion<? extends StampedVersion>> optionalVersion = 
////                    optionalObject.get().getLatestVersion(StampedVersion.class, viewCoordinate);
////            if (!optionalVersion.isPresent()) {
////                nids.remove(nid);
////            }
////        } else {
////            nids.remove(nid);
////        }
////    });
//      // Filter the results, based upon the input ViewCoordinate
//      getResultsCache().or(nids);
//      return nids;
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
        return ClauseSemantic.ASSEMBLAGE_LUCENE_MATCH;
    }
   
   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.ASSEMBLAGE_LUCENE_MATCH);
      whereClause.getLetKeys()
                 .add(this.queryStringKey);
      return whereClause;
   }
   
   @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.ASSEMBLAGE_LUCENE_MATCH_QUERY_CLAUSE;
   }
   
}

