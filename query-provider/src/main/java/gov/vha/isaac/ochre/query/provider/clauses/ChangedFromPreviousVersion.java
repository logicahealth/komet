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
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.collections.NidSet;
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
 * Computes the components that have been modified since the version specified
 * by the <code>ViewCoordinate</code>. Currently only retrieves descriptions
 * that were modified since the specified <code>ViewCoordinate</code>.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ChangedFromPreviousVersion extends LeafClause {

    /**
     * The <code>ViewCoordinate</code> used to specify the previous version.
     */


    @XmlElement
    String previousViewCoordinateKey;
    /**
     * Cached set of incoming components. Used to optimize speed in
     * getQueryMatches method.
     */
    NidSet cache = new NidSet();

    /**
     * Creates an instance of a ChangedFromPreviousVersion <code>Clause</code>
     * from the enclosing query and key used in let declarations for a previous
     * <code>ViewCoordinate</code>.
     *
     * @param enclosingQuery
     * @param previousViewCoordinateKey
     */
    public ChangedFromPreviousVersion(Query enclosingQuery, String previousViewCoordinateKey) {
        super(enclosingQuery);
        this.previousViewCoordinateKey = previousViewCoordinateKey;
    }

    protected ChangedFromPreviousVersion() {
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return ITERATION;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        System.out.println(incomingPossibleComponents.size());
        this.cache = incomingPossibleComponents;
        return incomingPossibleComponents;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        TaxonomyCoordinate previousViewCoordinate = (TaxonomyCoordinate) enclosingQuery.getLetDeclarations().get(previousViewCoordinateKey);
        throw new UnsupportedOperationException();
        //TODO FIX BACK UP
//        for (DescriptionVersionBI desc : conceptVersion.getDescriptionsActive()) {
//            if (desc.getVersion(previousViewCoordinate) != null) {
//                if (!desc.getVersion(previousViewCoordinate).equals(desc.getVersion(ViewCoordinates.getDevelopmentInferredLatestActiveOnly()))) {
//                    getResultsCache().add(desc.getConceptNid());
//                }
//            }
//        }
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.CHANGED_FROM_PREVIOUS_VERSION);
        whereClause.getLetKeys().add(previousViewCoordinateKey);
        return whereClause;
    }
}
