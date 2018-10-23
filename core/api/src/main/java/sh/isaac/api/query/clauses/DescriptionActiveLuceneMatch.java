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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DescriptionActiveLuceneMatch.
 *
 * @author dylangrald
 */
public class DescriptionActiveLuceneMatch
        extends DescriptionLuceneMatch {

   private String parameterString;
   private ManifoldCoordinate manifoldCoordinate;

   /**
    * Instantiates a new description active lucene match.
    */
   public DescriptionActiveLuceneMatch() {}

   /**
    * Instantiates a new description active lucene match.
    *
    * @param enclosingQuery the enclosing query
    * @param luceneMatchKey the lucene match key
    * @param viewCoordinateKey the view coordinate key
    */
   public DescriptionActiveLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey) {
      super(enclosingQuery, luceneMatchKey, viewCoordinateKey);
   }

   //~--- methods -------------------------------------------------------------


   @Override
   public void setParameterString(String parameterString) {
      this.parameterString = parameterString;
   }

   @Override
   public void setManifoldCoordinate(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }

   /**
    * Compute components.
    *
    * @param incomingComponents the incoming components
    * @return the nid set
    */
   @Override
   public final Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> incomingComponents) {

      getResultsCache().and(incomingComponents.get(this.getAssemblageForIteration()));
      incomingComponents.get(this.getAssemblageForIteration()).stream().forEach((nid) -> {
                                    final Optional<? extends Chronology> chronology =
                                       Get.identifiedObjectService()
                                          .getChronology(nid);

                                    if (chronology.isPresent()) {
                                       if (!chronology.get()
                                             .isLatestVersionActive(this.manifoldCoordinate.getStampCoordinate())) {
                                          getResultsCache().remove(nid);
                                       }
                                    } else {
                                       getResultsCache().remove(nid);
                                    }
                                 });
      HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(incomingComponents);
      resultsMap.put(this.getAssemblageForIteration(), getResultsCache());
      return resultsMap;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the compute phases.
    *
    * @return the compute phases
    */
   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return PRE_AND_POST_ITERATION;
   }
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.DESCRIPTION_ACTIVE_LUCENE_MATCH;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.DESCRIPTION_ACTIVE_LUCENE_MATCH);
      whereClause.getLetKeys()
                 .add(this.luceneMatchKey);
      whereClause.getLetKeys()
                 .add(this.viewCoordinateKey);
      return whereClause;
   }
}

