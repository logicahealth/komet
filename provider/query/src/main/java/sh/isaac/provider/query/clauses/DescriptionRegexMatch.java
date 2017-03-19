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

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.provider.query.ClauseComputeType;
import sh.isaac.provider.query.ClauseSemantic;
import sh.isaac.provider.query.LeafClause;
import sh.isaac.provider.query.Query;
import sh.isaac.provider.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * Calculates descriptions that match the specified Java Regular Expression.
 * Very slow when iterating over a large
 * {@link org.ihtsdo.otf.query.implementation.ForCollection} set.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionRegexMatch
        extends LeafClause {
   NidSet cache = new NidSet();
   @XmlElement
   String regexKey;
   @XmlElement
   String viewCoordinateKey;

   //~--- constructors --------------------------------------------------------

   protected DescriptionRegexMatch() {}

   public DescriptionRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey) {
      super(enclosingQuery);
      this.viewCoordinateKey = viewCoordinateKey;
      this.regexKey          = regexKey;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
      this.cache = incomingPossibleComponents;
      return incomingPossibleComponents;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return ITERATION;
   }

   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      final String                                      regex = (String) this.enclosingQuery.getLetDeclarations()
                                                                                 .get(this.regexKey);
      final ConceptChronology<? extends ConceptVersion> conceptChronology = conceptVersion.getChronology();

      conceptChronology.getConceptDescriptionList().forEach((description) -> {
                                   if (this.cache.contains(description.getNid())) {
                                      description.getVersionList().forEach((dv) -> {
                     if (dv.getText()
                           .matches(regex)) {
                        addToResultsCache((dv.getNid()));
                     }
                  });
                                   }
                                });
   }

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

