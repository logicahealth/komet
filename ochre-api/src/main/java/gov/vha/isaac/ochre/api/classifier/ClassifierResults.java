/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api.classifier;

import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import java.util.Set;

/**
 *
 * @author kec
 */
public class ClassifierResults {
    ConceptSequenceSet affectedConcepts;
    Set<ConceptSequenceSet> equivalentSets;

    public ClassifierResults(ConceptSequenceSet affectedConcepts, Set<ConceptSequenceSet> equivalentSets) {
        this.affectedConcepts = affectedConcepts;
        this.equivalentSets = equivalentSets;
    }

    public ConceptSequenceSet getAffectedConcepts() {
        return affectedConcepts;
    }

    public Set<ConceptSequenceSet> getEquivalentSets() {
        return equivalentSets;
    }

    @Override
    public String toString() {
        return "ClassifierResults{" + "affectedConcepts=" + affectedConcepts.size() + 
                ", equivalentSets=" + equivalentSets.size() + '}';
    }
    
}
