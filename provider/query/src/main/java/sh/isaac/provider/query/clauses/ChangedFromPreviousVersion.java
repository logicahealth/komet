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
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.provider.query.ClauseComputeType;
import sh.isaac.provider.query.ClauseSemantic;
import sh.isaac.provider.query.LeafClause;
import sh.isaac.provider.query.Query;
import sh.isaac.provider.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * Computes the components that have been modified since the version specified
 * by the <code>ViewCoordinate</code>. Currently only retrieves descriptions
 * that were modified since the specified <code>ViewCoordinate</code>.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ChangedFromPreviousVersion
        extends LeafClause {
   /**
    * Cached set of incoming components. Used to optimize speed in
    * getQueryMatches method.
    */
   NidSet cache = new NidSet();

   /**
    * The <code>ViewCoordinate</code> used to specify the previous version.
    */
   @XmlElement
   String previousViewCoordinateKey;

   //~--- constructors --------------------------------------------------------

   protected ChangedFromPreviousVersion() {}

   /**
    * Creates an instance of a ChangedFromPreviousVersion <code>Clause</code>
    * from the enclosing query and key used in let declarations for a previous
    * <code>ViewCoordinate</code>.
    *
    * @param enclosingQuery
    * @param previousViewCoordinateKey
    */
   public ChangedFromPreviousVersion(Query enclosingQuery, String previousViewCoordinateKey) {
      super(enclosingQuery);
      this.previousViewCoordinateKey = previousViewCoordinateKey;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
//    System.out.println(incomingPossibleComponents.size());
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
      TaxonomyCoordinate previousViewCoordinate = (TaxonomyCoordinate) enclosingQuery.getLetDeclarations()
                                                                                     .get(previousViewCoordinateKey);

      throw new UnsupportedOperationException();

      // TODO FIX BACK UP
//    for (DescriptionVersionBI desc : conceptVersion.getDescriptionsActive()) {
//        if (desc.getVersion(previousViewCoordinate) != null) {
//            if (!desc.getVersion(previousViewCoordinate).equals(desc.getVersion(ViewCoordinates.getDevelopmentInferredLatestActiveOnly()))) {
//                getResultsCache().add(desc.getConceptNid());
//            }
//        }
//    }
   }

   @Override
   public WhereClause getWhereClause() {
      WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.CHANGED_FROM_PREVIOUS_VERSION);
      whereClause.getLetKeys()
                 .add(previousViewCoordinateKey);
      return whereClause;
   }
}

