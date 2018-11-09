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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * Calculates descriptions that match the specified Java Regular Expression.
 * Very slow when iterating over a large sets.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionRegexMatch
        extends LeafClause {
   /** The cache. */
   NidSet cache = new NidSet();

   /** The regex key. */
   @XmlElement
   String regexKey;

   /** The view coordinate key. */
   @XmlElement
   String viewCoordinateKey;

   private String parameterString;
   private ManifoldCoordinate manifoldCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new description regex match.
    */
   public DescriptionRegexMatch() {}

   /**
    * Instantiates a new description regex match.
    *
    * @param enclosingQuery the enclosing query
    * @param regexKey the regex key
    * @param viewCoordinateKey the view coordinate key
    */
   public DescriptionRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey) {
      super(enclosingQuery);
      this.viewCoordinateKey = viewCoordinateKey;
      this.regexKey          = regexKey;
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
      this.cache = incomingPossibleComponents.get(this.getAssemblageForIteration());
      HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(incomingPossibleComponents);
      resultsMap.put(this.getAssemblageForIteration(), this.cache);
      return resultsMap;
   }

   //~--- get methods ---------------------------------------------------------


   public void setParameterString(String parameterString) {
      this.parameterString = parameterString;
   }

   public void setManifoldCoordinate(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }

   @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.DESCRIPTION_REGEX_MATCH_QUERY_CLAUSE;
   }

   /**
    * Gets the compute phases.
    *
    * @return the compute phases
    */
   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return ITERATION;
   }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {

      final ConceptChronology conceptChronology = conceptVersion.getChronology();

      conceptChronology.getConceptDescriptionList()
                       .forEach(
                           (description) -> {
                              if (this.cache.contains(description.getNid())) {
                                 description.getVersionList()
                                            .forEach(
                                                  (dv) -> {
                     if (((DescriptionVersion) dv).getText()
                           .matches(this.parameterString)) {
                        addToResultsCache((dv.getNid()));
                     }
                  });
                              }
                           });
   }
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.DESCRIPTION_REGEX_MATCH;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.DESCRIPTION_REGEX_MATCH);
      whereClause.getLetKeys()
                 .add(this.regexKey);
      whereClause.getLetKeys()
                 .add(this.viewCoordinateKey);
      return whereClause;
   }
}

