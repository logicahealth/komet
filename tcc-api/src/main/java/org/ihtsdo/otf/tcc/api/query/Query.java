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

/**
 *
 * @author kec
 */
public abstract class Query {
    
    private final HashMap<String, Object> letDeclarations =
            new HashMap<>();

    public HashMap<String, Object> getLetDeclarations() {
        return letDeclarations;
    }
    private NativeIdSetBI forSet;
    private EnumSet<ClauseComputeType> computeTypes =
            EnumSet.noneOf(ClauseComputeType.class);
    private ViewCoordinate viewCoordinate;
    
    public EnumSet<ClauseComputeType> getComputePhases() {
        return computeTypes;
    }
    
    public Query(ViewCoordinate viewCoordinate) {
        this.viewCoordinate = viewCoordinate;
    }
    
    protected abstract NativeIdSetBI For() throws IOException;
    
    protected abstract void Let() throws IOException;
    
    protected abstract Clause Where();
    
    public void let(String key, Object object) throws IOException {
        letDeclarations.put(key, object);
    }
    
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

    public ViewCoordinate getViewCoordinate() {
        return viewCoordinate;
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
                for(Clause c: rootClause.getChildren()){
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
        return new ConceptIsKindOf(this, conceptSpecKey);
    }
    
    protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey) {
        return new DescriptionRegexMatch(this, regexKey);
    }
    
    protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey){
        return new DescriptionActiveRegexMatch(this, regexKey);
    }
    
    protected ConceptForComponent ConceptForComponent(Clause child){
        return new ConceptForComponent(this, child);
    }
    
    protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey){
        return new ConceptIsDescendentOf(this, conceptSpecKey);
    }
    
    protected ComponentIsMemberOfRefset ConceptIsMemberOfRefset(String refsetSpecKey){
        return new ComponentIsMemberOfRefset(this, refsetSpecKey);
    }
    
    protected ConceptIsChildOf ConceptIsChildOf(String refsetSpecKey){
        return new ConceptIsChildOf(this, refsetSpecKey);
    }
    
    protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey){
        return new DescriptionActiveLuceneMatch(this, queryTextKey);
    }
    
    protected DescriptionLuceneMatch DescriptionLuceneMatch(String queryTextKey){
        return new DescriptionLuceneMatch(this, queryTextKey);
    }
    
    protected And And(Clause... clauses) {
        return new And(this, clauses);
    }
    
    protected And Intersection(Clause... clauses) {
        return new And(this, clauses);
    }
    
    public Not Not(Clause clause) {
        return new Not(this, clause);
    }
    
    public NativeIdSetBI getForSet() {
        return forSet;
    }
    
    protected Or Or(Clause... clauses) {
        return new Or(this, clauses);
    }
    
    protected Or Union(Clause... clauses) {
        return new Or(this, clauses);
    }
    
    protected ChangedFromPreviousVersion ChangedFromPreviousVersion(String previousCoordinateKey){
        return new ChangedFromPreviousVersion(this, previousCoordinateKey);
    }
}
