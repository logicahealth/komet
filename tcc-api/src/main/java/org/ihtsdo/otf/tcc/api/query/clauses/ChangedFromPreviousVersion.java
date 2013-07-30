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
package org.ihtsdo.otf.tcc.api.query.clauses;

import java.io.IOException;
import java.util.EnumSet;
import org.ihtsdo.otf.tcc.api.query.ClauseComputeType;
import org.ihtsdo.otf.tcc.api.query.LeafClause;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Where;

/**
 *
 * @author dylangrald
 */
public class ChangedFromPreviousVersion extends LeafClause {

    ViewCoordinate previousViewCoordinate;
    String previousViewCoordinateKey;

    public ChangedFromPreviousVersion(Query enclosingQuery, String previousViewCoordinateKey) {
        super(enclosingQuery);
        this.previousViewCoordinateKey = previousViewCoordinateKey;
        this.previousViewCoordinate = (ViewCoordinate) enclosingQuery.getLetDeclarations().get(previousViewCoordinateKey);
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return ITERATION;
    }

    @Override
    public NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleComponents) throws IOException {
        return incomingPossibleComponents;
    }

    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) throws IOException, ContradictionException {
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsActive()) {
            if (!dv.getVersion(previousViewCoordinate).toString().equals(dv.getChronicle().toString())) {
                getResultsCache().add(dv.getNid());
            }
        }
    }
    
    @Override
    public Where.WhereClause getWhereClause() {
        Where.WhereClause whereClause = new Where.WhereClause();
        whereClause.setSemantic(Where.ClauseSemantic.CHANGED_FROM_PREVIOUS_VERSION);
        for(Clause clause : getChildren()){
            whereClause.getChildren().add(clause.getWhereClause());
        }
        whereClause.getLetKeys().add(previousViewCoordinateKey);
        return whereClause;
    }
}
