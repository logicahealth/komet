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

/**
 *
 * @author dylangrald
 */
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.collections.NidSet;
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
 * .
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class RefsetContainsString extends LeafClause {


    @XmlElement
    String queryText;
    @XmlElement
    String viewCoordinateKey;

    NidSet cache;

    @XmlElement
    String refsetSpecKey;

    public RefsetContainsString(Query enclosingQuery, String refsetSpecKey, String queryText, String viewCoordinateKey) {
        super(enclosingQuery);
        this.refsetSpecKey = refsetSpecKey;
        this.queryText = queryText;
        this.viewCoordinateKey = viewCoordinateKey;

    }
    protected RefsetContainsString() {
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
//
//        int refsetNid = refsetSpec.getNid();
//        ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(viewCoordinate, refsetNid);
//
//        for (RefexVersionBI<?> rm : conceptVersion.getCurrentRefsetMembers(viewCoordinate)) {
//            switch (rm.getRefexType()) {
//                case CID_STR:
//                case CID_CID_CID_STRING:
//                case CID_CID_STR:
//                case STR:
//                    RefexStringVersionBI rsv = (RefexStringVersionBI) rm;
//                    if (rsv.getString1().toLowerCase().contains(queryText.toLowerCase())) {
//                        getResultsCache().add(refsetNid);
//                    }
//                default:
//                //do nothing
//
//            }
//        }
//
//        return getResultsCache();
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {

    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.REFSET_CONTAINS_STRING);
        whereClause.getLetKeys().add(refsetSpecKey);
        whereClause.getLetKeys().add(queryText);
        whereClause.getLetKeys().add(viewCoordinateKey);
        return whereClause;
    }
}
