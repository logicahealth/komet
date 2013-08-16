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
package org.ihtsdo.otf.tcc.api.query;

import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.clauses.ConceptIsKindOf;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.query.clauses.ChangedFromPreviousVersion;
import org.ihtsdo.otf.tcc.api.query.clauses.ConceptForComponent;
import org.ihtsdo.otf.tcc.api.query.clauses.ConceptIsChildOf;
import org.ihtsdo.otf.tcc.api.query.clauses.ConceptIsDescendentOf;
import org.ihtsdo.otf.tcc.api.query.clauses.ComponentIsMemberOfRefset;
import org.ihtsdo.otf.tcc.api.query.clauses.DescriptionActiveLuceneMatch;
import org.ihtsdo.otf.tcc.api.query.clauses.DescriptionActiveRegexMatch;
import org.ihtsdo.otf.tcc.api.query.clauses.DescriptionLuceneMatch;
import org.ihtsdo.otf.tcc.api.query.clauses.DescriptionRegexMatch;
import org.ihtsdo.otf.tcc.api.query.clauses.FullySpecifiedNameForConcept;
import org.ihtsdo.otf.tcc.api.query.clauses.PreferredNameForConcept;
import org.ihtsdo.otf.tcc.api.query.clauses.RefsetLuceneMatch;
import org.ihtsdo.otf.tcc.api.query.clauses.RelType;

/**
 * Executes queries within the terminology hierarchy and returns the nids of the
 * components that match the criterion of query.
 *
 * @author kec
 */
public abstract class Query {

    public String currentViewCoordinateKey = "Current view coordinate";
    private final HashMap<String, Object> letDeclarations =
            new HashMap<>();

    public HashMap<String, Object> getLetDeclarations(){
        return letDeclarations;
    }

    public HashMap<String, Object> getVCLetDeclarations() throws IOException {
        HashMap<String, Object> letVCDeclarations =
                new HashMap<>();
        letVCDeclarations.put(currentViewCoordinateKey, StandardViewCoordinates.getSnomedInferredLatest());
        return letVCDeclarations;
    }
    /**
     * The concepts, stored as nids in a
     * <code>NativeIdSetBI</code>, that are considered in the query.
     */
    private NativeIdSetBI forSet;
    /**
     * The steps requires to compute the query clause.
     */
    private EnumSet<ClauseComputeType> computeTypes =
            EnumSet.noneOf(ClauseComputeType.class);
    /**
     * The
     * <code>ViewCoordinate</code> used in the query.
     */
    private ViewCoordinate viewCoordinate;

    /**
     * Retrieves what type of iterations are required to compute the clause.
     *
     * @return an <code>EnumSet</code> of the compute types required
     */
    public EnumSet<ClauseComputeType> getComputePhases() {
        return computeTypes;
    }

    public Query(ViewCoordinate viewCoordinate) {
        this.viewCoordinate = viewCoordinate;
    }

    /**
     * Determines the set that will be searched in the query.
     *
     * @return the <code>NativeIdSetBI</code> of the set that will be queried
     * @throws IOException
     */
    protected abstract NativeIdSetBI For() throws IOException;

    protected abstract void Let() throws IOException;

    /**
     * Retrieves the root clause of the query.
     *
     * @return root <code>Clause</code> in the query
     */
    protected abstract Clause Where();

    public void let(String key, Object object) throws IOException {
        letDeclarations.put(key, object);
    }

    /**
     * Constructs the query and computes the set of concepts that match the
     * criterion specified in the clauses.
     *
     * @return the <code>NativeIdSetBI</code> of nids that meet the criterion of
     * the query
     * @throws IOException
     * @throws Exception
     */
    public NativeIdSetBI compute() throws IOException, Exception {
        forSet = For();
        Let();
        Clause rootClause = Where();
        NativeIdSetBI possibleComponents =
                rootClause.computePossibleComponents(forSet);
        if (computeTypes.contains(ClauseComputeType.ITERATION)) {
            NativeIdSetBI conceptsToIterateOver =
                    Ts.get().getConceptNidsForComponentNids(possibleComponents);
            Iterator itr = new Iterator(rootClause, conceptsToIterateOver);
            Ts.get().iterateConceptDataInParallel(itr);
        }
        return rootClause.computeComponents(possibleComponents);
    }

    /**
     *
     * @return the <code>ViewCoordinate</code> in the query
     */
    public ViewCoordinate getViewCoordinate() {
        return viewCoordinate;
    }

    protected RefsetLuceneMatch RefsetLuceneMatch(String luceneMatch) {
        return new RefsetLuceneMatch(this, luceneMatch);
    }

    private class Iterator implements ProcessUnfetchedConceptDataBI {

        NativeIdSetBI conceptsToIterate;
        Clause rootClause;

        public Iterator(Clause rootClause, NativeIdSetBI conceptsToIterate) {
            this.rootClause = rootClause;
            this.conceptsToIterate = conceptsToIterate;
        }

        @Override
        public boolean allowCancel() {
            return true;
        }

        @Override
        public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
            if (conceptsToIterate.contains(cNid)) {
                ConceptVersionBI concept = fetcher.fetch(viewCoordinate);
                for (Clause c : rootClause.getChildren()) {
                    c.getQueryMatches(concept);
                }
            }
        }

        @Override
        public NativeIdSetBI getNidSet() throws IOException {
            return conceptsToIterate;
        }

        @Override
        public String getTitle() {
            return "Query Iterator";
        }

        @Override
        public boolean continueWork() {
            return true;
        }
    }

    protected ConceptIsKindOf ConceptIsKindOf(String conceptSpecKey) {
        return new ConceptIsKindOf(this, conceptSpecKey, this.currentViewCoordinateKey);
    }

    protected ConceptIsKindOf ConceptIsKindOf(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIsKindOf(this, conceptSpecKey, viewCoordinateKey);
    }

    protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey) {
        return new DescriptionRegexMatch(this, regexKey);
    }

    protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey) {
        return new DescriptionActiveRegexMatch(this, regexKey);
    }

    protected ConceptForComponent ConceptForComponent(Clause child) {
        return new ConceptForComponent(this, child);
    }

    protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey) {
        return new ConceptIsDescendentOf(this, conceptSpecKey, this.currentViewCoordinateKey);
    }

    protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIsDescendentOf(this, conceptSpecKey, viewCoordinateKey);
    }

    protected ComponentIsMemberOfRefset ConceptIsMemberOfRefset(String refsetSpecKey) {
        return new ComponentIsMemberOfRefset(this, refsetSpecKey);
    }

    protected ConceptIsChildOf ConceptIsChildOf(String conceptSpecKey) {
        return new ConceptIsChildOf(this, conceptSpecKey, this.currentViewCoordinateKey);
    }

    protected ConceptIsChildOf ConceptIsChildOf(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIsChildOf(this, conceptSpecKey, viewCoordinateKey);
    }

    protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey) {
        return new DescriptionActiveLuceneMatch(this, queryTextKey);
    }

    protected DescriptionLuceneMatch DescriptionLuceneMatch(String queryTextKey) {
        return new DescriptionLuceneMatch(this, queryTextKey);
    }

    protected And And(Clause... clauses) {
        return new And(this, clauses);
    }

    protected RelType RelType(String conceptSpecKey) {
        return new RelType(this, conceptSpecKey, this.currentViewCoordinateKey);
    }

    protected RelType RelType(String conceptSpecKey, String viewCoordinateKey) {
        return new RelType(this, conceptSpecKey, viewCoordinateKey);
    }

    protected PreferredNameForConcept PreferredNameForConcept(Clause clause) {
        return new PreferredNameForConcept(this, clause);
    }

    protected And Intersection(Clause... clauses) {
        return new And(this, clauses);
    }

    protected FullySpecifiedNameForConcept FullySpecifiedNameForConcept(Clause clause) {
        return new FullySpecifiedNameForConcept(this, clause);
    }

    public Not Not(Clause clause) {
        return new Not(this, clause);
    }

    /**
     * Getter for the For set.
     *
     * @return the <code>NativeIdSetBI</code> of the concepts that will be
     * searched in the query
     */
    public NativeIdSetBI getForSet() {
        return forSet;
    }

    protected Or Or(Clause... clauses) {
        return new Or(this, clauses);
    }

    protected Or Union(Clause... clauses) {
        return new Or(this, clauses);
    }

    protected ChangedFromPreviousVersion ChangedFromPreviousVersion(String previousCoordinateKey) {
        return new ChangedFromPreviousVersion(this, previousCoordinateKey);
    }

    protected Xor Xor(Clause... clauses) {
        return new Xor(this, clauses);
    }
}
