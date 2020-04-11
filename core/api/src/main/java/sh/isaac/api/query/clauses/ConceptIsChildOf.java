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

import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.*;
import sh.isaac.api.query.properties.ConceptClause;
import sh.isaac.api.query.properties.ManifoldClause;

import java.util.EnumSet;
import java.util.Map;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * Computes the set of concepts that are a child of the input concept. The set
 * of children of a given concept is the set of all concepts that lie one step
 * below the input concept in the terminology hierarchy. This
 * <code>Clause</code> has an optional parameter for a previous
 * <code>ViewCoordinate</code>, which allows for queries of previous versions.
 *
 * @author dylangrald
 */
public class ConceptIsChildOf
        extends LeafClause implements ConceptClause, ManifoldClause {
   /** The child of spec key. */
   LetItemKey childOfSpecKey;

   /** the manifold coordinate key. */
   LetItemKey manifoldCoordinateKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept is child of.
    */
   public ConceptIsChildOf() {}

   /**
    * Instantiates a new concept is child of.
    *
    * @param enclosingQuery the enclosing query
    * @param kindOfSpecKey the kind of spec key
    * @param manifoldCoordinateKey the manifold coordinate key
    */
   public ConceptIsChildOf(Query enclosingQuery, LetItemKey kindOfSpecKey, LetItemKey manifoldCoordinateKey) {
      super(enclosingQuery);
      this.childOfSpecKey    = kindOfSpecKey;
      this.manifoldCoordinateKey = manifoldCoordinateKey;
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
      final int parentNid         = ((ConceptSpecification) getLetItem(this.childOfSpecKey)).getNid();
      final TaxonomySnapshot kindOfSnapshot = Get.taxonomyService().getSnapshot(getLetItem(manifoldCoordinateKey));
      
      NidSet possibleComponents = incomingPossibleComponents.get(getAssemblageForIteration());
        
      for (int nid: possibleComponents.asArray()) {
          if (!kindOfSnapshot.isChildOf(nid, parentNid)) {
              possibleComponents.remove(nid);
          }
      }
      return incomingPossibleComponents;

   }

   //~--- get methods ---------------------------------------------------------

    public LetItemKey getChildOfSpecKey() {
        return childOfSpecKey;
    }

    public void setChildOfSpecKey(LetItemKey childOfSpecKey) {
        this.childOfSpecKey = childOfSpecKey;
    }

    @Override
    public LetItemKey getConceptSpecKey() {
        return getChildOfSpecKey();
    }

    @Override
    public void setConceptSpecKey(LetItemKey childOfSpecKey) {
        setChildOfSpecKey(childOfSpecKey);
    }

   
   @Override
    public LetItemKey getManifoldCoordinateKey() {
        return manifoldCoordinateKey;
    }

   @Override
    public void setManifoldCoordinateKey(LetItemKey manifoldCoordinateKey) {
        this.manifoldCoordinateKey = manifoldCoordinateKey;
    }


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
        return ClauseSemantic.CONCEPT_IS_CHILD_OF;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.CONCEPT_IS_CHILD_OF);
      whereClause.getLetKeys()
                 .add(this.childOfSpecKey);
      whereClause.getLetKeys()
                 .add(this.manifoldCoordinateKey);
      return whereClause;
   }

}

