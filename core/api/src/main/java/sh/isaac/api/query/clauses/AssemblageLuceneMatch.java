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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
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
   String luceneMatchKey;

   /** The view coordinate key. */
   @XmlElement
   String viewCoordinateKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new refset lucene match.
    */
   public AssemblageLuceneMatch() {}

   /**
    * Instantiates a new refset lucene match.
    *
    * @param enclosingQuery the enclosing query
    * @param luceneMatchKey the lucene match key
    * @param viewCoordinateKey the view coordinate key
    */
   public AssemblageLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey) {
      super(enclosingQuery);
      this.luceneMatchKey    = luceneMatchKey;
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
      this.enclosingQuery.getLetDeclarations()
                         .get(this.viewCoordinateKey);
      this.enclosingQuery.getLetDeclarations()
                         .get(this.luceneMatchKey);

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

      whereClause.setSemantic(ClauseSemantic.ASSEMBLAGE_LUCENE_MATCH);
      whereClause.getLetKeys()
                 .add(this.luceneMatchKey);
      return whereClause;
   }
   
   @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.ASSEMBLAGE_LUCENE_MATCH_QUERY_CLAUSE;
   }
   
}

