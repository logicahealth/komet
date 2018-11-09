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

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * Allows the user specify a search for circular relationships. Also allows the user to limit the identification
 * of circular relationships to specify types, and also to allow subsumption on the relationship type.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class RelationshipIsCircular
        extends LeafClause {
   /** The rel type key. */
   @XmlElement
   String relTypeKey;

   /** The view coordinate key. */
   @XmlElement
   String viewCoordinateKey;

   /** The rel type subsumption key. */
   @XmlElement
   String relTypeSubsumptionKey;

   /** The rel type set. */
   NidSet relTypeSet;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new relationship is circular.
    */
   public RelationshipIsCircular() {}

   /**
    * Instantiates a new relationship is circular.
    *
    * @param enclosingQuery the enclosing query
    * @param relTypeKey the rel type key
    * @param viewCoordinateKey the view coordinate key
    * @param relTypeSubsumptionKey the rel type subsumption key
    */
   public RelationshipIsCircular(Query enclosingQuery,
                                 String relTypeKey,
                                 String viewCoordinateKey,
                                 String relTypeSubsumptionKey) {
      super(enclosingQuery);
      this.relTypeKey            = relTypeKey;
      this.viewCoordinateKey     = viewCoordinateKey;
      this.relTypeSubsumptionKey = relTypeSubsumptionKey;
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
//    System.out.println("Let declerations: " + enclosingQuery.getLetDeclarations());
      final ManifoldCoordinate manifoldCoordinate = (ManifoldCoordinate) this.enclosingQuery.getLetDeclarations()
                                                                                            .get(this.viewCoordinateKey);
      final ConceptSpecification relType = (ConceptSpecification) this.enclosingQuery.getLetDeclarations()
                                                                                     .get(this.relTypeKey);
      Boolean relTypeSubsumption = (Boolean) this.enclosingQuery.getLetDeclarations()
                                                                .get(this.relTypeSubsumptionKey);

      // The default is to set relTypeSubsumption and destinationSubsumption to true.
      if (relTypeSubsumption == null) {
         relTypeSubsumption = true;
      }

      this.relTypeSet = new NidSet();
      this.relTypeSet.add(relType.getNid());

      if (relTypeSubsumption) {
         this.relTypeSet.or(Get.taxonomyService().getSnapshot(manifoldCoordinate).getKindOfConceptNidSet(relType.getNid()));
      }

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
      return PRE_ITERATION_AND_ITERATION;
   }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      throw new UnsupportedOperationException();

      /*
       * ManifoldCoordinate manifoldCoordinate = (ManifoldCoordinate) enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
       * Get.taxonomyService().getAllRelationshipDestinationSequencesOfType(
       *       conceptVersion.getChronology().getConceptSequence(), relTypeSet, viewCoordinate)
       *       .forEach((destinationSequence) -> {
       *           if (destinationSet.contains(destinationSequence)) {
       *               getResultsCache().add(conceptVersion.getChronology().getNid());
       *           }
       *       });
       */
   }
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.RELATIONSHIP_IS_CIRCULAR;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.RELATIONSHIP_IS_CIRCULAR);
      whereClause.getLetKeys()
                 .add(this.relTypeKey);
      whereClause.getLetKeys()
                 .add(this.viewCoordinateKey);
      whereClause.getLetKeys()
                 .add(this.relTypeSubsumptionKey);

//    System.out.println("Where clause size: " + whereClause.getLetKeys().size());
      return whereClause;
   }
   
   @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.RELATIONSHIP_IS_CIRCULAR_QUERY_CLAUSE;
   }
   
}

