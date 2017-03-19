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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.provider.query.ClauseComputeType;
import sh.isaac.provider.query.ClauseSemantic;
import sh.isaac.provider.query.Query;
import sh.isaac.provider.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 *
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionActiveLuceneMatch
        extends DescriptionLuceneMatch {
   protected DescriptionActiveLuceneMatch() {}

   public DescriptionActiveLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey) {
      super(enclosingQuery, luceneMatchKey, viewCoordinateKey);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public final NidSet computeComponents(NidSet incomingComponents) {
      TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) this.enclosingQuery.getLetDeclarations()
                                                                                      .get(viewCoordinateKey);

      getResultsCache().and(incomingComponents);
      incomingComponents.stream().forEach((nid) -> {
                                    Optional<? extends ObjectChronology<? extends StampedVersion>> chronology =
                                       Get.identifiedObjectService()
                                          .getIdentifiedObjectChronology(nid);

                                    if (chronology.isPresent()) {
                                       if (!chronology.get()
                                             .isLatestVersionActive(taxonomyCoordinate.getStampCoordinate())) {
                                          getResultsCache().remove(nid);
                                       }
                                    } else {
                                       getResultsCache().remove(nid);
                                    }
                                 });
      return getResultsCache();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return PRE_AND_POST_ITERATION;
   }

   @Override
   public WhereClause getWhereClause() {
      WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.DESCRIPTION_ACTIVE_LUCENE_MATCH);
      whereClause.getLetKeys()
                 .add(luceneMatchKey);
      whereClause.getLetKeys()
                 .add(viewCoordinateKey);
      return whereClause;
   }
}

