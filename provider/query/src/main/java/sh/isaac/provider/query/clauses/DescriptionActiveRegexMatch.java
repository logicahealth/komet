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



package sh.isaac.provider.query.clauses;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.State;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.provider.query.ClauseSemantic;
import sh.isaac.provider.query.Query;
import sh.isaac.provider.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * Calculates the active descriptions that match the specified Java Regular
 * Expression.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionActiveRegexMatch
        extends DescriptionRegexMatch {
   /**
    * Instantiates a new description active regex match.
    */
   protected DescriptionActiveRegexMatch() {}

   /**
    * Instantiates a new description active regex match.
    *
    * @param enclosingQuery the enclosing query
    * @param regexKey the regex key
    * @param viewCoordinateKey the view coordinate key
    */
   public DescriptionActiveRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey) {
      super(enclosingQuery, regexKey, viewCoordinateKey);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    * @return the query matches
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      final String regex = (String) this.enclosingQuery.getLetDeclarations()
                                                       .get(this.regexKey);
      final ConceptChronology<? extends ConceptVersion> conceptChronology = conceptVersion.getChronology();

      conceptChronology.getConceptDescriptionList().stream().forEach((dc) -> {
                                   dc.getVersionList()
                                     .stream()
                                     .filter((dv) -> (dv.getText().matches(regex) && (dv.getState() == State.ACTIVE)))
                                     .forEach((dv) -> {
                  addToResultsCache((dv.getNid()));
               });
                                });
   }

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.DESCRIPTION_ACTIVE_REGEX_MATCH);
      whereClause.getLetKeys()
                 .add(this.regexKey);
      whereClause.getLetKeys()
                 .add(this.viewCoordinateKey);
      return whereClause;
   }
}

