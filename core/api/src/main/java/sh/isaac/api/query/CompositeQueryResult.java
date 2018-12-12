/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;

/**
 *
 * @author kec
 */
public interface CompositeQueryResult extends Comparable<CompositeQueryResult> {

    /**
     * Gets the best score. If this result set has been merged on concepts, it
     * will be the best score from the {@link #getMatchingComponents()},
     * otherwise, it will be the score from the initial
     * {@link SearchResult#getScore()}
     *
     * @return the best score
     */
    float getBestScore();

    /**
     * Find the concept most closely linked to the semantic that matched the
     * query.
     *
     * @return the found concept
     */
    ConceptChronology getContainingConcept();

    /**
     * Get the text (per the manifold) for the {@link #getContainingConcept()}
     * concept
     *
     * @return the text
     */
    String getContainingConceptText();

    /**
     * Gets the versions of matching components. Note, this filters on the
     * provided stamp, and if the match is not on the path of the given stamp
     * coords, it will not be returned here.
     *
     * @return the matching components
     */
    List<Version> getMatchingComponentVersions();

    /**
     * Gets the matching components. If merged on concept, this will be size >
     * 1, otherwise, size 1.
     *
     * @return the matching components
     */
    Set<Chronology> getMatchingComponents();
    
    LinkedHashMap<Chronology, Function<Chronology, LatestVersion<Version>>> getExpandedMatchingComponents();

    void merge(CompositeQueryResult other);
    /**
     * A convenience method to get string representation from each of the
     * matching components
     *
     * This uses the coordinates that were passed in at search start, if the
     * matching component isn't available on the given stamp, it won't be
     * included in this list.
     *
     * This will return the items in the same number and order as
     * {@link #getMatchingComponentVersions()}
     *
     * @return the matching strings
     */
    List<String> getMatchingStrings();
    
}
