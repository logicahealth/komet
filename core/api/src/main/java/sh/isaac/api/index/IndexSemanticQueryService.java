/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.api.index;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.jvnet.hk2.annotations.Contract;
import javafx.concurrent.Task;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;

/**
 * An extended query interface that supports very specific querying of semantics - especially with dynamic and/or multi-column semantic data
 * types.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IndexSemanticQueryService extends IndexQueryService
{

	final Set<DynamicDataType> unsupportedDataTypes = new HashSet<>(
			Arrays.asList(new DynamicDataType[] { DynamicDataType.BYTEARRAY, DynamicDataType.POLYMORPHIC, DynamicDataType.UNKNOWN }));

	/**
	 * Search numeric semantic data columns for values in between the provided parameters. You must provide both lower and upper limits, with
	 * data types that match the data type in the assemblage.
	 * 
	 * @param queryDataLower - optional - defaults to appropriate MIN_VALUE of the queryDataUpper data type. If not provided queryDataUpper must
	 *            be provided.
	 * @param queryDataLowerInclusive - true to have the search be inclusive of the queryDataLower, false to be exclusive.
	 * @param queryDataUpper - optional - defaults to appropriate MAX_VALUE of the queryDataLower data type. If not provided queryDataLower must
	 *            be provided.
	 * @param queryDataUpperInclusive - true to have the search be inclusive of the queryDataLower, false to be exclusive.
	 * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
	 *            content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
	 *            {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
	 * @param searchColumns - optional limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
	 *            assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
	 * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
	 *            will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
	 *            true, or false, to have the item excluded.
	 * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
	 * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
	 * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
	 *            size limit with and passing pageNum is the recommended way of handling large result sets.
	 * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
	 *            is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
	 *            indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
	 *            {@link IndexQueryService#getIndexedGenerationCallable(int)}
	 * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
	 *         matches. Note that scores are pointless for range matches - they will all be the same.
	 */
	public List<SearchResult> queryNumericRange(Number queryDataLower,
			boolean queryDataLowerInclusive,
			Number queryDataUpper,
			boolean queryDataUpperInclusive,
			int[] assemblageConcepts,
			int[] searchColumns,
			Predicate<Integer> filter,
			AuthorModulePathRestriction amp,
			Integer pageNum,
			Integer sizeLimit,
			Long targetGeneration);

	/**
	 * Search for matches to the specified nid in semantic instances. Implementations should at least support matching in semantics of the types
	 * {@link VersionType#COMPONENT_NID}, {@link VersionType#LOGIC_GRAPH} and {@link VersionType#DYNAMIC} where a data column in the item is of
	 * type {@link DynamicNid}
	 * 
	 * This should not match on the assemblage concept, nor the referenced component nid.
	 * 
	 * Those can be found directly via standard semantic APIs.
	 * 
	 * If searching a logic graph semantic, it will find a match in any concept that is involved in the graph, except for the root concept.
	 * 
	 * @param nid the id reference to search for
	 * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
	 *            content in this index. This could be set to {@link MetaData#EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR} for example, to limit
	 *            a search to content in those particular assemblages.
	 * @param searchColumns - optional - limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
	 *            assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
	 * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
	 *            will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
	 *            true, or false, to have the item excluded.
	 * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
	 * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
	 * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
	 *            size limit with and passing pageNum is the recommended way of handling large result sets.
	 * @param targetGeneration optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there is
	 *            no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
	 *            indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
	 *            {@link IndexQueryService#getIndexedGenerationCallable(int)}
	 * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
	 *         matches. Note that scores are pointless for exact id matches - they will all be the same.
	 */
	public List<SearchResult> queryNidReference(int nid,
			int[] assemblageConcepts,
			int[] searchColumns,
			Predicate<Integer> filter,
			AuthorModulePathRestriction amp,
			Integer pageNum,
			Integer sizeLimit,
			Long targetGeneration);

	/**
	 * Search for matches to the specified queryData, across all semantic types which carry data.
	 * 
	 * @param queryData - The query data object. This must be a typed DynamicData object, such as {@link DynamicString}. The provided data type
	 *            should match the type of data contained in the assemblage being searched for the desired column(s).
	 * 
	 * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
	 *            to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
	 *            string will be required to be found in the result.
	 *
	 *            Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
	 *            following it, that word will be required as a complete term. If the last word of the query does not have a space character
	 *            following it, that word will be required as a prefix match only.
	 *
	 *            For example: The query "family test" will return results that contain 'Family Testudinidae' The query "family test " will not
	 *            match on 'Testudinidae', so that will be excluded.
	 * 
	 * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
	 *            content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
	 *            {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
	 * @param searchColumns - optional - limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
	 *            assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
	 * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
	 *            will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
	 *            true, or false, to have the item excluded.
	 * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
	 * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
	 * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
	 *            size limit with and passing pageNum is the recommended way of handling large result sets.
	 * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
	 *            is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
	 *            indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
	 *            {@link IndexQueryService#getIndexedGenerationCallable(int)}
	 * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
	 *         matches.
	 */
	public List<SearchResult> queryData(DynamicData queryData,
			boolean prefixSearch,
			int[] assemblageConcepts,
			int[] searchColumns,
			Predicate<Integer> filter,
			AuthorModulePathRestriction amp,
			Integer pageNum,
			Integer sizeLimit,
			Long targetGeneration);

	/**
	 * Search for matches to the specified queryData, across all semantic types which carry data.
	 * 
	 * Same as {@link #queryData(DynamicData, boolean, int[], int[], Predicate, AmpRestriction, Integer, Integer, Long)} but takes in the query as
	 * a string for simplicity, and passes nulls for other optional parameters that aren't requested here
	 * 
	 * @param queryString - The query data string, with will be wrapped into a {@link DynamicString}.
	 * 
	 * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
	 *            to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
	 *            string will be required to be found in the result.
	 *
	 *            Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
	 *            following it, that word will be required as a complete term. If the last word of the query does not have a space character
	 *            following it, that word will be required as a prefix match only.
	 *
	 *            For example: The query "family test" will return results that contain 'Family Testudinidae' The query "family test " will not
	 *            match on 'Testudinidae', so that will be excluded.
	 * 
	 * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
	 *            content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
	 *            {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
	 * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
	 *            will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
	 *            true, or false, to have the item excluded.
	 * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
	 * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
	 * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
	 *            size limit with and passing pageNum is the recommended way of handling large result sets.
	 * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
	 *            is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
	 *            indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
	 *            {@link IndexQueryService#getIndexedGenerationCallable(int)}
	 * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
	 *         matches.
	 */
	public List<SearchResult> queryData(String queryString,
				boolean prefixSearch,
				int[] assemblageConcepts,
				Predicate<Integer> filter,
				AuthorModulePathRestriction amp,
				Integer pageNum,
				Integer sizeLimit,
				Long targetGeneration);
	/**
	 * @return the data types that this semantic indexer doesn't support. The default implementation specifies that we don't support the bytearray
	 *			type, nor the unknown placeholder, or polymorphic, which shouldn't exist at runtime with real data.
	 * 
	 *			If an implementation supports less (or more) that these types, they must override this method.
	 */
	public default Set<DynamicDataType> getUnsupportedDataTypes()
	{
		return unsupportedDataTypes;
	}

	/**
	 * For a given assemblage, mark certain columns as data that should NOT be indexed.
	 * By default, all columns (of supported data types) are indexed for each assemblage.
	 *
	 * This feature serves to improve performance / reduce storage space for certain data which never needs to be
	 * searched.
	 *
	 * Calling this method replaces the current excluded column list if any, and triggers a complete reindex.
	 *
	 * @param assemblageConceptNid The semantic assemblage to configure
	 * @param columnsToExclude - which columns to not index (0 indexed column identifiers). Pass null or an empty array
	 *            to return to the default of indexing all indexable columns for this semantic.
	 * @return a task handle to the background reindexing operation that happens after this is called.
	 */
	public Task<Void> setColumnsToExclude(int assemblageConceptNid, Integer[] columnsToExclude);

	/**
	 * For a given semantic assemblage, return the (0 indexed) column identifiers of the columns that should be indexed.
	 * 
	 * This takes into account any columns that were excluded by a call to {@link #setColumnsToExclude(int, Integer[])}
	 * 
	 * If no columns have been excluded, it will return a result indicating that all columns should be indexed, with the exception
	 * of columns that contain data types that aren't indexable.
	 * 
	 * @param assemblageConceptNid - The semantic (dynamic or static) to request the indexing information of
	 * @return an array of (0 indexed) column identifiers of the columns that should be indexed.
	 */
	public Integer[] getColumnsToIndex(int assemblageConceptNid);
}
