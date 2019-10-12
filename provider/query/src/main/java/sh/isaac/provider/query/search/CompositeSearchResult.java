/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.provider.query.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.query.CompositeQueryResult;

/**
 * A class that returns richer search results than {@link SearchResult} -
 * turning the nids into chronologies, and allowing for merging results on a
 * parent concept.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class CompositeSearchResult implements CompositeQueryResult {

    private static final Logger LOG = LogManager.getLogger();

    //The first item, is the match for this result.  If more than one it represents a merged result, and containingConcept should be populated.
    //initially, only keys are populated.  Version (value) are only populated when requested.
    private LinkedHashMap<Chronology, Function<Chronology, LatestVersion<Version>>> matchingComponents = new LinkedHashMap<>();

    private ConceptChronology containingConcept = null;

    // best score, rather than score, as multiple matches may go into a CompositeSearchResult
    private float bestScore;

    private ManifoldCoordinate manifoldCoord;

    /**
     * Instantiates a new composite search result.
     *
     * @param searchResult - the lucene search result to base this on
     * @param mc - optional - the stamp coordinate to use for operations that
     * require a stamp to get a version. Uses the user default, if not provided.
     */
    public CompositeSearchResult(SearchResult searchResult, ManifoldCoordinate mc) {
        this.bestScore = searchResult.getScore();
        this.manifoldCoord = (mc == null ? Get.configurationService().getUserConfiguration(null).getManifoldCoordinate() : mc);

        // Get the match object.
        Optional<? extends Chronology> chron = Get.identifiedObjectService().getChronology(searchResult.getNid());

        if (chron.isPresent()) {
            matchingComponents.put(chron.get(), new Function<Chronology, LatestVersion<Version>>() {
                LatestVersion<Version> cachedValue = null;

                @Override
                public LatestVersion<Version> apply(Chronology t) {
                    if (cachedValue == null) {
                        cachedValue = t.getLatestVersion(CompositeSearchResult.this.manifoldCoord);
                    }
                    return cachedValue;
                }
            });
        } else {
            throw new RuntimeException("No chronology available for nid " + searchResult.getNid());
        }
    }

    /**
     * Find the concept most closely linked to the semantic that matched the
     * query.
     *
     * @return the found concept
     */
    @Override
    public ConceptChronology getContainingConcept() {
        if (containingConcept == null) {
            locateContainingConcept(matchingComponents.keySet().iterator().next());
        }
        return containingConcept;
    }

    /**
     * Get the text (per the manifold) for the {@link #getContainingConcept()}
     * concept
     *
     * @return the text
     */
    @Override
    public String getContainingConceptText() {
        ConceptChronology cc = getContainingConcept();
        return Get.conceptService().getSnapshot(manifoldCoord).conceptDescriptionText(cc.getNid());
    }

    /**
     * recursive method to find and populate the nearest concept
     *
     * @param chronology - the starting point
     */
    private void locateContainingConcept(Chronology chronology) {
        switch (chronology.getIsaacObjectType()) {
            case CONCEPT:
                containingConcept = (ConceptChronology) chronology;
                break;
            case SEMANTIC:
                //recurse
                try {
                    locateContainingConcept(Get.identifiedObjectService().getChronology(((SemanticChronology) chronology).getReferencedComponentNid()).get());
                } catch (NoSuchElementException ex) {
                    containingConcept = Get.concept(TermAux.UNINITIALIZED_COMPONENT_ID);
                    LOG.error("Can't find containing concept for: " + chronology,ex);
                }
                break;
            //Things below here should never come back from a nid from the indexer
            case STAMP:
            case STAMP_ALIAS:
            case STAMP_COMMENT:
            case UNKNOWN:
            default:
                throw new RuntimeException("Oops, can't handle: " + chronology.getIsaacObjectType());
        }
    }

    /**
     * Gets the best score. If this result set has been merged on concepts, it
     * will be the best score from the {@link #getMatchingComponents()},
     * otherwise, it will be the score from the initial
     * {@link SearchResult#getScore()}
     *
     * @return the best score
     */
    @Override
    public float getBestScore() {
        return this.bestScore;
    }

    /**
     * Gets the matching components. If merged on concept, this will be size >
     * 1, otherwise, size 1.
     *
     * @return the matching components
     */
    @Override
    public Set<Chronology> getMatchingComponents() {
        return this.matchingComponents.keySet();
    }

    @Override
    public LinkedHashMap<Chronology, Function<Chronology, LatestVersion<Version>>> getExpandedMatchingComponents() {
        return this.matchingComponents;
    }
    
    /**
     * Gets the versions of matching components. Note, this filters on the
     * provided stamp, and if the match is not on the path of the given stamp
     * coords, it will not be returned here.
     *
     * @return the matching components
     */
    @Override
    public List<Version> getMatchingComponentVersions() {
        ArrayList<Version> matchingVersions = new ArrayList<>();
        for (Entry<Chronology, Function<Chronology, LatestVersion<Version>>> vp : matchingComponents.entrySet()) {
            LatestVersion<Version> version = vp.getValue().apply(vp.getKey());
            if (version.isPresent()) {
                matchingVersions.add(version.get());
            }
        }
        return matchingVersions;
    }

    /**
     * Merge.
     *
     * @param other the other one to merge in
     */
    public void merge(CompositeQueryResult other) {
        if (this.getContainingConcept().getNid() != other.getContainingConcept().getNid()) {
            throw new RuntimeException("Unmergeable!");
        }

        if (other.getBestScore() > this.bestScore) {
            this.bestScore = other.getBestScore();
        }

        this.matchingComponents.putAll(other.getExpandedMatchingComponents());
    } 

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("CompositeSearchResult [containingConcept=");
        builder.append(getContainingConcept());
        builder.append(", bestScore=");
        builder.append(this.bestScore);
        builder.append(", MatchingComponents=[");

        for (Entry<Chronology, Function<Chronology, LatestVersion<Version>>> entry : matchingComponents.entrySet()) {
            builder.append(entry.getKey());
            builder.append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append("]]");
        return builder.toString();
    }

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
    @Override
    public List<String> getMatchingStrings() {
        final ArrayList<String> strings = new ArrayList<>();

        for (Version version : getMatchingComponentVersions()) {
            switch (version.getSemanticType()) {
                case CONCEPT:
                    //This should only happen when we are injecting results for UUID lookups, etc.  Not from a lucene search.
                    strings.add("Concept: " + version.getPrimordialUuid());
                    break;
                case COMPONENT_NID:
                    strings.add("Component Nid" + version.getNid());
                    break;
                case DESCRIPTION:
                    strings.add(((DescriptionVersion) version).getText());
                    break;
                case DYNAMIC:
                    strings.add(((DynamicVersion) version).dataToString());
                    break;
                case LOGIC_GRAPH:
                    strings.add(((LogicGraphVersion) version).getLogicalExpression().toSimpleString());
                    break;
                case LONG:
                    strings.add(((LongVersion) version).getLongValue() + "");
                    break;
                case STRING:
                    strings.add(((StringVersion) version).getString());
                    break;
                case RF2_RELATIONSHIP:
                    strings.add(((Rf2Relationship) version).toString());
                    break;
                case MEASURE_CONSTRAINTS:
                    strings.add("Measure Constraint with nid of " + version.getNid());
                    break;
                case LOINC_RECORD:
                case Nid1_Int2:
                case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                case Nid1_Nid2:
                case Nid1_Nid2_Int3:
                case Nid1_Nid2_Str3:
                case Nid1_Str2:
                case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                case Str1_Nid2_Nid3_Nid4:
                case Str1_Str2:
                case Str1_Str2_Nid3_Nid4:
                case Str1_Str2_Nid3_Nid4_Nid5:
                case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                    strings.add("Brittle semantic with nid of " + version.getNid());
                case UNKNOWN:
                case MEMBER:
                default:
                    LOG.error("Unexpected type {} in search results", version);
                    break;
            }
        }
        return strings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(CompositeQueryResult o) {
        int c = Float.compare(this.bestScore, o.getBestScore()) * -1;
        if (c != 0) {
            return c;
        }

        c = Integer.compare(this.matchingComponents.size(), o.getMatchingComponents().size()) * -1;
        if (c != 0) {
            return c;
        }

        //sort on the text of the first (best) matching component
        List<String> myStrings = this.getMatchingStrings();
        List<String> theirStrings = o.getMatchingStrings();

        if (myStrings.size() > 0 && theirStrings.size() > 0) {
            return myStrings.get(0).compareTo(theirStrings.get(0));
        } else if (myStrings.size() > 0) {
            return 1;
        } else if (theirStrings.size() > 0) {
            return -1;
        }
        return 0;
    }
}
