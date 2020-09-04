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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.AuthorModulePathRestriction;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.SearchResult;

/**
 *
 * @author kec
 */
@Contract
public interface QueryHandler {

    /**
     *
     * This is really just a convenience wrapper with threading and results conversion on top of the APIs available in {@link DescriptionIndexer}
     *
     * Execute a Query against the description indexes in a background thread, hand back a handle to the search object which will
     * allow you to get the results (when they are ready) and also cancel an in-progress query.
     *
     * If there is a problem with the internal indexes - an error will be logged, and the exception will be re-thrown when the
     * {@link SearchHandle#getResults()} method of the SearchHandle is called.
     *
     * @param searchFunction - A function that will call one of the query(...) methods within an implementation of a {@link IndexQueryService}.
     * @param operationToRunWhenSearchComplete - (optional) Pass the function that you want to have executed when the search is complete and the
     *            results are ready for use. Note that this function will also be executed in the background thread.
     * @param taskId - An optional field that is simply handed back during the callback when results are complete. Useful for matching
     *            requests to this method with callbacks.
     * @param filter - An optional filter than can add or remove items from the tentative result set before it is returned.
     * @param mergeOnConcepts - If true, when multiple semantics attached to the same concept match the search, this will be returned
     *            as a single result representing the concept - with each matching component included, and the score being the best score of any of the
     *            matching items.  When false, you will get one search result per match (per semantic) - so concepts can be returned multiple times.
     * @param manifoldForRead - optional - if not supplied, uses the default for the user / system, for any operations that require getting a
     *            version (as opposed to a chronology).  This primarily impacts convenience methods inside of {@link CompositeSearchResult}
     * @param filterOffPathResults - if true, will only return matching components that are present on the provided stampForVersionRead.  Note,
     * it is faster to do Author / Module / Path restrictions with a {@link AuthorModulePathRestriction} during the query - this should only be used to filter out
     * based on status or time.
     * @return A handle to the running search.
     */
    QueryHandle search(final Supplier<List<SearchResult>> searchFunction, final Consumer<QueryHandle> operationToRunWhenSearchComplete,
                       final Integer taskId, final Function<List<CompositeQueryResult>, List<CompositeQueryResult>> filter,
                       boolean mergeOnConcepts, ManifoldCoordinate manifoldForRead, boolean filterOffPathResults);

    /**
     * @param searchString the string that contains an identifier
     * @param identifierTypes - optional - null, or the identifier types to restrict the search to
     * @param operationToRunWhenSearchComplete - (optional) Pass the function that you want to have executed when the search is complete and the
     *            results are ready for use. Note that this function will also be executed in the background thread.
     * @param taskId - An optional field that is simply handed back during the callback when results are complete. Useful for matching
     *            requests to this method with callbacks.
     * @param postQueryfilter - An optional filter than can add or remove items from the tentative result set before it is returned.
     * @param mergeOnConcepts - If true, when multiple semantics attached to the same concept match the search, this will be returned
     *            as a single result representing the concept - with each matching component included, and the score being the best score of any of the
     *            matching items.  When false, you will get one search result per match (per semantic) - so concepts can be returned multiple times.
     * @param manifoldForRead - optional - if not supplied, uses the default for the user / system, for any operations that require getting a
     *            version (as opposed to a chronology).  This primarily impacts convenience methods inside of {@link CompositeSearchResult}
     * @param filterOffPathResults - if true, will only return matching components that are present on the provided stampForVersionRead.  Note,
     * it is faster to do Author / Module / Path restrictions with a {@link AuthorModulePathRestriction} during the query - this should only be used to filter out
     * based on status or time.
     * @param queryFilter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
     *           will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
     *           true, or false, to have the item excluded.  Not applicable to UUID or nid queries
     * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.  Not applicable to UUID or nid queries
     * @param sizeLimit - restrict to this number of results
     * @return A handle to the running search.
     */
    QueryHandle searchIdentifiers(String searchString, int[] identifierTypes, final Consumer<QueryHandle> operationToRunWhenSearchComplete,
                                  final Integer taskId, final Function<List<CompositeQueryResult>, List<CompositeQueryResult>> postQueryfilter,
                                  boolean mergeOnConcepts, ManifoldCoordinate manifoldForRead, boolean filterOffPathResults,
                                  Predicate<Integer> queryFilter, AuthorModulePathRestriction amp, int sizeLimit);
    
}
