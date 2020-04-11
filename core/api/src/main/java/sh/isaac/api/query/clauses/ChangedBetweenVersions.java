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
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.query.*;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * Computes the components that have been modified between the specified
 * Filter Coordinates.
 *
 * @author dylangrald
 */
public class ChangedBetweenVersions
        extends LeafClause {

   /**
    * The <code>StampCoordinate</code> used to specify version one.
    */
   LetItemKey stampCoordinateOneKey;

   /**
    * The <code>StampCoordinate</code> used to specify version two.
    */
   LetItemKey stampCoordinateTwoKey;

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
   public ChangedBetweenVersions(Query enclosingQuery, LetItemKey stampCoordinateOneKey, LetItemKey stampCoordinateTwoKey) {
      super(enclosingQuery);
      this.stampCoordinateOneKey = stampCoordinateOneKey;
      this.stampCoordinateTwoKey = stampCoordinateTwoKey;
   }

   public LetItemKey getStampCoordinateOneKey() {
      return stampCoordinateOneKey;
   }

    public void setStampCoordinateOneKey(LetItemKey stampCoordinateOneKey) {
        this.stampCoordinateOneKey = stampCoordinateOneKey;
    }

    public LetItemKey getStampCoordinateTwoKey() {
        return stampCoordinateTwoKey;
    }

    //~--- methods -------------------------------------------------------------
    public void setStampCoordinateTwoKey(LetItemKey stampCoordinateTwoKey) {
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
        StampFilter stampFilterOne = (StampFilter) this.enclosingQuery.getLetDeclarations()
                         .get(this.stampCoordinateOneKey);
        StampFilter stampFilterTwo = (StampFilter) this.enclosingQuery.getLetDeclarations()
                         .get(this.stampCoordinateTwoKey);

        NidSet possibleComponents = incomingPossibleComponents.get(getAssemblageForIteration());

        StampSequenceSet allowedStamps = Get.stampService().getStampsBetweenCoordinates(stampFilterOne, stampFilterTwo);

        
        for (int nid: possibleComponents.asArray()) {
            Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(nid);
            if (optionalChronology.isPresent()) {
                Chronology chronology = optionalChronology.get();
                boolean found = false;
                for (Version v: chronology.getVersionList()) {
                    if (allowedStamps.contains(v.getStampSequence())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    possibleComponents.remove(nid);
                }
            } else {
                possibleComponents.remove(nid);
            }
            
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
      return ITERATION;
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
   
}

