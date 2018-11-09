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
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;

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
public class ChangedBetweenVersions
        extends LeafClause {
   /**
    * Cached set of incoming components. Used to optimize speed in
    * getQueryMatches method.
    */
   NidSet cache = new NidSet();

   /**
    * The <code>StampCoordinate</code> used to specify version one.
    */
   @XmlElement
   String stampCoordinateOneKey;

   /**
    * The <code>StampCoordinate</code> used to specify version two.
    */
   @XmlElement
   String stampCoordinateTwoKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new changed from previous version.
    */
   public ChangedBetweenVersions() {}

   /**
    * Creates an instance of a ChangedFromPreviousVersion <code>Clause</code>
    * from the enclosing query and key used in let declarations for a previous
    * <code>ViewCoordinate</code>.
    *
    * @param enclosingQuery the enclosing query
    * @param stampCoordinateOneKey the first stamp version to compare
    * @param stampCoordinateTwoKey the second stamp version to compare
    */
   public ChangedBetweenVersions(Query enclosingQuery, String stampCoordinateOneKey, String stampCoordinateTwoKey) {
      super(enclosingQuery);
      this.stampCoordinateOneKey = stampCoordinateOneKey;
      this.stampCoordinateTwoKey = stampCoordinateTwoKey;
   }

   public String getStampCoordinateOneKey() {
      return stampCoordinateOneKey;
   }

    public void setStampCoordinateOneKey(String stampCoordinateOneKey) {
        this.stampCoordinateOneKey = stampCoordinateOneKey;
    }

    public String getStampCoordinateTwoKey() {
        return stampCoordinateTwoKey;
    }

    //~--- methods -------------------------------------------------------------
    public void setStampCoordinateTwoKey(String stampCoordinateTwoKey) {
        this.stampCoordinateTwoKey = stampCoordinateTwoKey;
    }

    /**
     * Compute possible components.
     *
     * @param incomingPossibleComponents the incoming possible components
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
        this.cache = incomingPossibleComponents.get(this.getAssemblageForIteration());
        return incomingPossibleComponents;
    }

   //~--- get methods ---------------------------------------------------------

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
      this.enclosingQuery.getLetDeclarations()
                         .get(this.stampCoordinateOneKey);
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
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.CHANGED_FROM_PREVIOUS_VERSION;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.CHANGED_FROM_PREVIOUS_VERSION);
      whereClause.getLetKeys()
                 .add(this.stampCoordinateOneKey);
      return whereClause;
   }
   
   @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.CHANGED_FROM_PREVIOUS_VERSION_QUERY_CLAUSE;
   }
   
}

