/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.query.clauses;

import java.util.EnumSet;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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

/**
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ReferencedComponentIsNotMemberOf 
        extends LeafClause {
   /** The cache. */
   NidSet cache;

   /** The concept spec key. */
   @XmlElement
   LetItemKey conceptSpecKey;

   /** the manifold coordinate key. */
   @XmlElement
   LetItemKey stampCoordinateKey;

   /** The assemblage spec key. */
   //
   @XmlElement
   LetItemKey assemblageSpecKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new refset contains concept.
    */
   public ReferencedComponentIsNotMemberOf() {}

   /**
    * Instantiates a new refset contains concept.
    *
    * @param enclosingQuery the enclosing query
    * @param assemblageSpecKey the refset spec key
    * @param conceptSpecKey the concept spec key
    * @param stampCoordinateKey the manifold coordinate key
    */
   public ReferencedComponentIsNotMemberOf(Query enclosingQuery,
                                LetItemKey assemblageSpecKey,
                                LetItemKey conceptSpecKey,
                                LetItemKey stampCoordinateKey) {
      super(enclosingQuery);
      this.assemblageSpecKey     = assemblageSpecKey;
      this.conceptSpecKey    = conceptSpecKey;
      this.stampCoordinateKey = stampCoordinateKey;
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
      throw new UnsupportedOperationException();

      // TODO FIX BACK UP
//    ViewCoordinate viewCoordinate = (ViewCoordinate) this.enclosingQuery.getLetDeclarations().get(stampCoordinateKey);
//    ConceptSpec refsetSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(assemblageSpecKey);
//    ConceptSpec conceptSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(conceptSpecKey);
//
//    int conceptNid = conceptSpec.getNid();
//    int refsetNid = refsetSpec.getNid();
//    ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(viewCoordinate, refsetNid);
//    for (RefexVersionBI<?> rm : conceptVersion.getCurrentRefsetMembers(viewCoordinate)) {
//        if (rm.getReferencedComponentNid() == conceptNid) {
//            getResultsCache().add(refsetNid);
//        }
//    }
//
//    return getResultsCache();
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

    public LetItemKey getConceptSpecKey() {
        return conceptSpecKey;
    }

    public void setConceptSpecKey(LetItemKey conceptSpecKey) {
        this.conceptSpecKey = conceptSpecKey;
    }

    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKey;
    }

    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKey = stampCoordinateKey;
    }

    public LetItemKey getAssemblageSpecKey() {
        return assemblageSpecKey;
    }

    public void setAssemblageSpecKey(LetItemKey assemblageSpecKey) {
        this.assemblageSpecKey = assemblageSpecKey;
    }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      // Nothing to do here...
   }
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.REFERENCED_COMPONENT_IS_NOT_MEMBER_OF;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.REFERENCED_COMPONENT_IS_NOT_MEMBER_OF);
      whereClause.getLetKeys()
                 .add(this.assemblageSpecKey);
      whereClause.getLetKeys()
                 .add(this.conceptSpecKey);
      whereClause.getLetKeys()
                 .add(this.stampCoordinateKey);
      return whereClause;
   }
   
      @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.REFERENCED_COMPONENT_IS_NOT_MEMBER_OF;
   }

}

