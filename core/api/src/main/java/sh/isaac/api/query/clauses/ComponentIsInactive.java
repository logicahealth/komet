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

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.query.*;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author kec
 */
public class ComponentIsInactive extends LeafClause {

    /**
     * the manifold coordinate key.
     */
    LetItemKey stampFilterKey;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new component is active clause.
     */
    public ComponentIsInactive() {
    }

    /**
     * Instantiates a new component is active clause.
     *
     * @param enclosingQuery the enclosing query
     * @param stampFilterKey the manifold coordinate key
     */
    public ComponentIsInactive(Query enclosingQuery, LetItemKey stampFilterKey) {
        super(enclosingQuery);
        this.stampFilterKey = stampFilterKey;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public final Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> incomingComponents) {
        StampFilter stampFilter = getLetItem(stampFilterKey);
        
        NidSet possibleComponents = incomingComponents.get(getAssemblageForIteration());
        
        for (int nid: possibleComponents.asArray()) {
            final Optional<? extends Chronology> chronology
                    = Get.identifiedObjectService()
                            .getChronology(nid);
            if (chronology.isPresent()) {
                if (chronology.get()
                        .isLatestVersionActive(stampFilter)) {
                    possibleComponents.remove(nid);
                }
            } else {
                possibleComponents.remove(nid);
            }
        }
  
        return incomingComponents;
    }

    /**
     * Compute possible components.
     *
     * @param incomingPossibleComponents the incoming possible components
     * @return the nid set
     */
    @Override
    public final Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
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
        return ClauseSemantic.COMPONENT_IS_INACTIVE;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.COMPONENT_IS_INACTIVE);
        return whereClause;
    }

    public LetItemKey getStampFilterKey() {
        return stampFilterKey;
    }

    public void setStampFilterKey(LetItemKey stampFilterKey) {
        this.stampFilterKey = stampFilterKey;
    }
}
