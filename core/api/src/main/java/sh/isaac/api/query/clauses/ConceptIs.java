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

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.*;
import sh.isaac.api.query.properties.ConceptClause;

import java.util.EnumSet;
import java.util.Map;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------
/**
 * An identity function that obtains the concept from the input
 * <code>ConceptSpec</code>.
 *
 * @author dylangrald
 */
public class ConceptIs
        extends LeafClause implements ConceptClause {

    /**
     * The concept spec string.
     */
     LetItemKey conceptSpecString;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new concept is.
     */
    public ConceptIs() {
    }

    /**
     * Instantiates a new concept is.
     *
     * @param enclosingQuery the enclosing query
     * @param conceptSpec the concept spec
     */
    public ConceptIs(Query enclosingQuery, LetItemKey conceptSpec) {
        super(enclosingQuery);
        this.conceptSpecString = conceptSpec;
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
        final int conceptNid = ((ConceptSpecification) getLetItem(this.conceptSpecString)).getNid();

        NidSet possibleComponents = incomingPossibleComponents.get(getAssemblageForIteration());

        if (possibleComponents.contains(conceptNid)) {
            possibleComponents.clear();
            possibleComponents.add(conceptNid);
        } else {
            possibleComponents.clear();
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
        return PRE_ITERATION;
    }

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.CONCEPT_IS;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.CONCEPT_IS);
        whereClause.getLetKeys()
                .add(this.conceptSpecString);
        return whereClause;
    }

    @Override
    public LetItemKey getConceptSpecKey() {
        return conceptSpecString;
    }

    @Override
    public void setConceptSpecKey(LetItemKey conceptSpecString) {
        this.conceptSpecString = conceptSpecString;
    }

}
