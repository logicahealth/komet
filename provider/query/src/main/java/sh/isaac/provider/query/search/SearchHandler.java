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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.index.AmpRestriction;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.provider.query.lucene.indexers.DescriptionIndexer;
import sh.isaac.provider.query.lucene.indexers.SemanticIndexer;

/**
 * Class for wrapping search functionality provided by {@link IndexQueryService} or
 * {@link DescriptionIndexer} or {@link SemanticIndexer} and adds background threading, post filtering,
 * merging on concepts, etc.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author ocarlsen
 */

public class SearchHandler
{
	private static final Logger LOG = LogManager.getLogger();

	//TODO put some of this into a "just find the things" method
	// If search query is an ID, look up concept and add the result.
//	if (UUIDUtil.isUUID(localQuery) || NumericUtils.isLong(localQuery))
//	{
//		throw new UnsupportedOperationException("Search for unknown identifier is not implemented.");
//                     final Optional<? extends ConceptChronology> temp =
//                        Frills.getConceptForUnknownIdentifier(localQuery);
//
//                     if (temp.isPresent()) {
//                        final CompositeSearchResult gsr = new CompositeSearchResult(temp.get(), 2.0f);
//
//                        initialSearchResults.add(gsr);
//                     }

	
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
	 * @param stampForVersionRead - optional - if not supplied, uses the default stamp for the user / system, for any operations that require getting a 
	 *            version (as opposed to a chronology).  This primarily impacts convenience methods inside of {@link CompositeSearchResult}
	 * @param filterOffPathResults - if true, will only return matching components that are present on the provided stampForVersionRead.  Note, 
	 * it is faster to do Author / Module / Path restrictions with a {@link AmpRestriction} during the query - this should only be used to filter out 
	 * based on status or time.
	 * @return A handle to the running search.
	 */
	public static SearchHandle search(final Supplier<List<SearchResult>> searchFunction, final Consumer<SearchHandle> operationToRunWhenSearchComplete, 
			final Integer taskId, final Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter,
			boolean mergeOnConcepts, StampCoordinate stampForVersionRead, boolean filterOffPathResults)
	{
		final SearchHandle searchHandle = new SearchHandle(taskId);

		// Do search in background.
		final Runnable r = () -> {
			try
			{
				// execute the search
				final List<SearchResult> searchResults = searchFunction.get();
				LOG.debug(searchResults.size() + " results from search function");
				
				// sort, filter and merge the results as necessary
				processResults(searchHandle, searchResults, filter, mergeOnConcepts, stampForVersionRead, filterOffPathResults);

				if (operationToRunWhenSearchComplete != null)
				{
					operationToRunWhenSearchComplete.accept(searchHandle);
				}
			}
			catch (final Exception ex)
			{
				LOG.error("Unexpected error during lucene search", ex);
				searchHandle.setError(ex);
			}
		};

		Get.workExecutors().getExecutor().execute(r);
		return searchHandle;
	}
	
	/**
	 * Process results.
	 *
	 * see {@link #search(Supplier, Consumer, Integer, Function, boolean, StampCoordinate)} for details on the parameters.
	 */
	private static void processResults(SearchHandle searchHandle, List<SearchResult> initialResults,
			final Function<List<CompositeSearchResult>, List<CompositeSearchResult>> filter, boolean mergeOnConcepts, StampCoordinate stampForRead, 
			boolean filterOffPathResults)
	{
		
		List<CompositeSearchResult> rawResults = lookupChronologies(initialResults, stampForRead, searchHandle);
		// filter and sort the results
		if (filter != null)
		{
			LOG.debug("Applying SearchResultsFilter " + filter + " to " + rawResults.size() + " search results");
			rawResults = filter.apply(rawResults);
			LOG.debug(rawResults.size() + " results remained after running the filter");
		}

		if (filterOffPathResults)
		{
			LOG.debug("Applying Path filter to " + rawResults.size() + " search results");
			rawResults = new Function<List<CompositeSearchResult>, List<CompositeSearchResult>>()
			{
				@Override
				public List<CompositeSearchResult> apply(List<CompositeSearchResult> t)
				{
					final Iterator<CompositeSearchResult> it = t.iterator();

					while (it.hasNext())
					{
						if (it.next().getMatchingComponentVersions().isEmpty())
						{
							it.remove();
						}
					}

					return t;
				}
			}.apply(rawResults);
			LOG.debug(rawResults.size() + " results remained after running path filter");
		}

		if (mergeOnConcepts)
		{
			final HashMap<Integer, CompositeSearchResult> merged = new HashMap<>();

			for (final CompositeSearchResult csr : rawResults)
			{
				final CompositeSearchResult found = merged.get(csr.getContainingConcept().getNid());

				if (found == null)
				{
					merged.put(csr.getContainingConcept().getNid(), csr);
				}
				else
				{
					found.merge(csr);
				}
			}

			rawResults.clear();
			rawResults.addAll(merged.values());
		}

		Collections.sort(rawResults);
		searchHandle.setResults(rawResults);
	}
	
	private static List<CompositeSearchResult> lookupChronologies(List<SearchResult> searchResults, StampCoordinate stampForRead, SearchHandle searchHandle)
	{
		List<CompositeSearchResult> initialSearchResults = new ArrayList<>(searchResults.size());
		for (SearchResult searchResult : searchResults)
		{
			if (searchHandle.isCancelled())
			{
				break;
			}
			try
			{
				initialSearchResults.add(new CompositeSearchResult(searchResult, stampForRead));
			}
			catch (Exception e)
			{
				LOG.error("Unexpected: ", e);
			}
		}
		return initialSearchResults;
	}
}
