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

import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.EnumSet;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <code>LeafClause</code> that returns the nid of the input refset if a kind of
 * the input concept is a member of the refset and returns an empty set if a
 * kind of the input concept is not a member of the refset.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class RefsetContainsKindOfConcept extends LeafClause {

    @XmlElement
    String refsetSpecKey;
    @XmlElement
    String conceptSpecKey;
    @XmlElement
    String viewCoordinateKey;

    public RefsetContainsKindOfConcept(Query enclosingQuery, String refsetSpecKey, String conceptSpecKey, String viewCoordinateKey) {
        super(enclosingQuery);
        this.refsetSpecKey = refsetSpecKey;
        this.conceptSpecKey = conceptSpecKey;
        this.viewCoordinateKey = viewCoordinateKey;

    }
    protected RefsetContainsKindOfConcept() {
    }
    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.REFSET_CONTAINS_KIND_OF_CONCEPT);
        whereClause.getLetKeys().add(refsetSpecKey);
        whereClause.getLetKeys().add(conceptSpecKey);
        whereClause.getLetKeys().add(viewCoordinateKey);
        return whereClause;

    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        throw new UnsupportedOperationException();
        //TODO FIX BACK UP
//        TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) this.enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
//        ConceptSpec refsetSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(refsetSpecKey);
//        ConceptSpec conceptSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(conceptSpecKey);
//
//
//        int parentNid = conceptSpec.getNid();
//        NidSet kindOfSet = Ts.get().isKindOfSet(parentNid, viewCoordinate);
//        int refsetNid = refsetSpec.getNid();
//        ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(viewCoordinate, refsetNid);
//        for (RefexVersionBI<?> rm : conceptVersion.getCurrentRefsetMembers(viewCoordinate)) {
//            if (kindOfSet.contains(rm.getReferencedComponentNid())) {
//                getResultsCache().add(refsetNid);
//            }
//        }
//
//        return getResultsCache();
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        //Nothing to do here
    }
}
