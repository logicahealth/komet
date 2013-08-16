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
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.query.ClauseComputeType;
import org.ihtsdo.otf.tcc.api.query.LeafClause;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Where;

/**
 * Computes the components that have been modified since the version from the
 * specified ViewCoordinate. Currently only retrieves descriptions that were
 * modified since the specified ViewCoordinate.
 *
 * @author dylangrald
 */
public class ChangedFromPreviousVersion extends LeafClause {

    /**
     * The
     * <code>ViewCoordinate</code> used to specify the previous version.
     */
    ViewCoordinate previousViewCoordinate;
    /**
     * The
     * <code>String</code> Let key that designate the previous
     * <code>ViewCoordinate</code>.
     */
    String previousViewCoordinateKey;
    /**
     * Cached set of incoming components. Used to optimize speed in
     * getQueryMatches method.
     */
    NativeIdSetBI cache = new ConcurrentBitSet();

    /**
     * Creates an instance of a ChangedFromPreviousVersion
     * <code>Clause</code> from the enclosing query and key used in let
     * declarations for a previous
     * <code>ViewCoordinate</code>.
     *
     * @param enclosingQuery
     * @param previousViewCoordinateKey
     */
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
        this.cache = incomingPossibleComponents;
        return incomingPossibleComponents;
    }

    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) throws IOException, ContradictionException {
        for (int nid : conceptVersion.getAllNidsForVersion()) {
            if (this.cache.contains(nid)) {
                ComponentChronicleBI dc = conceptVersion.getComponent(nid);
                if (cache.isMember(dc.getNid())) {
                    ComponentVersionBI v1 = dc.getVersion(getEnclosingQuery().getViewCoordinate());
                    if (v1 != null && dc.getVersion(previousViewCoordinate) != null) {
                        if (!dc.getVersion(previousViewCoordinate).equals(v1)) {
                            getResultsCache().add(dc.getNid());
                        }
                    }
                }
            }
        }
    }

    @Override
    public Where.WhereClause getWhereClause() {
        Where.WhereClause whereClause = new Where.WhereClause();
        whereClause.setSemantic(Where.ClauseSemantic.CHANGED_FROM_PREVIOUS_VERSION);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        whereClause.getLetKeys().add(previousViewCoordinateKey);
        return whereClause;
    }
}
