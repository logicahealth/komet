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
import java.util.EnumSet;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.WhereClause;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Calculates the set of concepts that are a kind of the specified concept. The
 * calculated set is the union of the input concept and all concepts that lie
 * lie beneath the input concept in the terminology hierarchy.
 *
 * @author kec
 */
@XmlAccessorType(value = XmlAccessType.NONE)
public class ConceptIsKindOf extends LeafClause {

    @XmlElement
    String kindOfSpecKey;
    @XmlElement
    String viewCoordinateKey;

    public ConceptIsKindOf(Query enclosingQuery, String kindOfSpecKey, String viewCoordinateKey) {
        super(enclosingQuery);
        this.kindOfSpecKey = kindOfSpecKey;
        this.viewCoordinateKey = viewCoordinateKey;
    }

    protected ConceptIsKindOf() {
        super();
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        TaxonomyCoordinate tc = (TaxonomyCoordinate) this.enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
        ConceptSpecification kindOfSpec = (ConceptSpecification) enclosingQuery.getLetDeclarations().get(kindOfSpecKey);
        int parentNid = kindOfSpec.getNid();
        ConceptSequenceSet kindOfSequenceSet = Get.taxonomyService().getKindOfSequenceSet(parentNid, tc);
        getResultsCache().or(NidSet.of(kindOfSequenceSet));
        return getResultsCache();
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        // Nothing to do...
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.CONCEPT_IS_KIND_OF);
        whereClause.getLetKeys().add(kindOfSpecKey);
        whereClause.getLetKeys().add(viewCoordinateKey);
        return whereClause;
    }
}
