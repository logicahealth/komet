/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package gov.vha.isaac.ochre.query.provider.clauses;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.io.IOException;
import java.util.EnumSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;

/**
 * Computes the set of concepts that are a child of the input concept. The set
 * of children of a given concept is the set of all concepts that lie one step
 * below the input concept in the terminology hierarchy. This
 * <code>Clause</code> has an optional parameter for a previous
 * <code>ViewCoordinate</code>, which allows for queries of previous versions.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ConceptIsChildOf extends LeafClause {

    @XmlElement
    String childOfSpecKey;
    @XmlElement
    String viewCoordinateKey;

    public ConceptIsChildOf(Query enclosingQuery, String kindOfSpecKey, String viewCoordinateKey) {
        super(enclosingQuery);
        this.childOfSpecKey = kindOfSpecKey;
        this.viewCoordinateKey = viewCoordinateKey;
    }

    protected ConceptIsChildOf() {
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) this.enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
        ConceptSpecification childOfSpec = (ConceptSpecification) enclosingQuery.getLetDeclarations().get(childOfSpecKey);
        int parentNid = childOfSpec.getNid();
        ConceptSequenceSet childrenOfSequenceSet = Get.taxonomyService().getChildOfSequenceSet(parentNid, taxonomyCoordinate);
        getResultsCache().or(NidSet.of(childrenOfSequenceSet));
        return getResultsCache();
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_AND_POST_ITERATION;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        // Nothing to do...
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.CONCEPT_IS_CHILD_OF);
        whereClause.getLetKeys().add(childOfSpecKey);
        whereClause.getLetKeys().add(viewCoordinateKey);
        return whereClause;
    }
}
