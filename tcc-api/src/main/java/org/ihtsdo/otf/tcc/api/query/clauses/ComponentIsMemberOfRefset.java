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
import org.ihtsdo.otf.tcc.api.query.Where;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * TODO: Not supported yet. Calculates the set of concepts that are members of
 * the specified refset.
 *
 * @author dylangrald
 */
public class ComponentIsMemberOfRefset extends LeafClause {

    String refsetSpecKey;
    ConceptSpec refsetSpec;
    NativeIdSetBI cache;

    public ComponentIsMemberOfRefset(Query enclosingQuery, String refsetSpecKey) {
        super(enclosingQuery);
        this.refsetSpecKey = refsetSpecKey;
        this.refsetSpec = (ConceptSpec) enclosingQuery.getLetDeclarations().get(refsetSpecKey);

    }

    /*@Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_AND_POST_ITERATION;
    }

    @Override
    public NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleComponents) throws ValidationException, IOException, ContradictionException {
        int refsetNid = refsetSpec.getNid();
        NativeIdSetItrBI iter = incomingPossibleComponents.getIterator();
        while (iter.next()) {
            List<NidPairForRefex> refsetPairs = Bdb.getRefsetPairs(iter.nid());
            for (NidPairForRefex nidPair : refsetPairs) {
                if (nidPair.getRefexNid() == refsetNid) {
                    getResultsCache().add(nidPair.getMemberNid());
                }
            }

        }
        return getResultsCache();
    }

    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) throws ValidationException, IOException {
    }

    @Override
    public Where.WhereClause getWhereClause() {
        Where.WhereClause whereClause = new Where.WhereClause();
        whereClause.setSemantic(Where.ClauseSemantic.COMPONENT_IS_MEMBER_OF_REFSET);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        whereClause.getLetKeys().add(refsetSpecKey);
        return whereClause;
    }*/

    @Override
    public Where.WhereClause getWhereClause() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleComponents) throws IOException, ValidationException, ContradictionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) throws IOException, ContradictionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
