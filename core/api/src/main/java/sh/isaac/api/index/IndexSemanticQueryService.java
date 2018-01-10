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

package sh.isaac.api.index;

import java.util.List;
import java.util.function.Predicate;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.chronicle.VersionType;

/**
 * An extended query interface that supports very specific querying of semantics - especially with dynamic and/or multi-column semantic data
 * types.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IndexSemanticQueryService extends IndexQueryService {

   /**
    * Search numeric semantic data columns for values in between the provided parameters. You must provide both lower and upper limits, with
    * data types that match the data type in the assemblage.
    * 
    * @param queryDataLower - optional - defaults to appropriate MIN_VALUE of the queryDataUpper data type. If not provided queryDataUpper must
    *           be provided.
    * @param queryDataLowerInclusive - true to have the search be inclusive of the queryDataLower, false to be exclusive.
    * @param queryDataUpper - optional - defaults to appropriate MAX_VALUE of the queryDataLower data type. If not provided queryDataLower must
    *           be provided.
    * @param queryDataUpperInclusive - true to have the search be inclusive of the queryDataLower, false to be exclusive.
    * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
    *           content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
    *           {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
    * @param searchColumns - optional limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
    *           assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
    * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
    *           will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
    *           true, or false, to have the item excluded. 
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
    *           size limit with and passing pageNum is the recommended way of handling large result sets.
    * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
    *           is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    *           indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
    *           {@link IndexQueryService#getIndexedGenerationCallable(int)}
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches. Note that scores are pointless for range matches - they will all be the same.
    */
   public List<SearchResult> queryNumericRange(Number queryDataLower,
         boolean queryDataLowerInclusive,
         Number queryDataUpper,
         boolean queryDataUpperInclusive,
         Integer[] assemblageConcepts,
         Integer[] searchColumns,
         Predicate<Integer> filter,
         AmpRestriction amp,
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
    *           content in this index. This could be set to {@link MetaData#EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR} for example, to limit
    *           a search to content in those particular assemblages.
    * @param searchColumns - optional - limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
    *           assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
    * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
    *           will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
    *           true, or false, to have the item excluded.
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
    *           size limit with and passing pageNum is the recommended way of handling large result sets.
    * @param targetGeneration optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there is
    *           no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    *           indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
    *           {@link IndexQueryService#getIndexedGenerationCallable(int)}
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches. Note that scores are pointless for exact id matches - they will all be the same.
    */
   public List<SearchResult> queryNidReference(int nid,
         Integer[] assemblageConcepts,
         Integer[] searchColumns,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);

   /**
    * Search for matches to the specified queryData, across all semantic types which carry data.
    * 
    * @param queryData - The query data object. This must be a typed DynamicData object, such as {@link DynamicString}. The provided data type
    *           should match the type of data contained in the assemblage being searched for the desired column(s).
    * 
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
    *           to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
    *           string will be required to be found in the result.
    *
    *           Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
    *           following it, that word will be required as a complete term. If the last word of the query does not have a space character
    *           following it, that word will be required as a prefix match only.
    *
    *           For example: The query "family test" will return results that contain 'Family Testudinidae' The query "family test " will not
    *           match on 'Testudinidae', so that will be excluded.
    * 
    * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
    *           content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
    *           {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
    * @param searchColumns - optional - limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
    *           assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
    *           size limit with and passing pageNum is the recommended way of handling large result sets.
    * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
    *           is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    *           indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
    *           {@link IndexQueryService#getIndexedGenerationCallable(int)}
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches.
    */
   // TODO [DAN] fix this limitation on the column restriction...
   public List<SearchResult> queryData(DynamicData queryData,
         boolean prefixSearch,
         Integer[] assemblageConcepts,
         Integer[] searchColumns,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);

   /**
    * Search for matches to the specified queryData, across all semantic types which carry data.
    * 
    * Same as {@link #queryData(DynamicData, boolean, Integer[], Integer[], StampCoordinate, Integer, Integer, Long)} but adds the ability to
    * specify a custom filter to be evaluated along with the query.
    * 
    * @param queryData - The query data object. This must be a typed DynamicData object, such as {@link DynamicString}. The provided data type
    *           should match the type of data contained in the assemblage being searched for the desired column(s).
    * 
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
    *           to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
    *           string will be required to be found in the result.
    *
    *           Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
    *           following it, that word will be required as a complete term. If the last word of the query does not have a space character
    *           following it, that word will be required as a prefix match only.
    *
    *           For example: The query "family test" will return results that contain 'Family Testudinidae' The query "family test " will not
    *           match on 'Testudinidae', so that will be excluded.
    * 
    * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
    *           content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
    *           {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
    * @param searchColumns - optional - limit the search to the specified columns of attached data. May ONLY be provided if ONE and only one
    *           assemblageConcept is provided. May not be provided if 0 or more than 1 assemblageConcept values are provided.
    * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
    *           will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
    *           true, or false, to have the item excluded.
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
    *           size limit with and passing pageNum is the recommended way of handling large result sets.
    * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
    *           is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    *           indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
    *           {@link IndexQueryService#getIndexedGenerationCallable(int)}
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches.
    */
   public List<SearchResult> queryData(DynamicData queryData,
         boolean prefixSearch,
         Integer[] assemblageConcepts,
         Integer[] searchColumns,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);

   /**
    * Search for matches to the specified queryData, across all semantic types which carry data.
    * 
    * Same as {@link #queryData(DynamicData, boolean, Integer[], Integer[], StampCoordinate, Integer, Integer, Long)} but takes in the query as
    * a string for simplicity, and passes nulls for other optional parameters.
    * 
    * @param queryString - The query data string, with will be wrapped into a {@link DynamicString}.
    * 
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
    *           to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
    *           string will be required to be found in the result.
    *
    *           Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
    *           following it, that word will be required as a complete term. If the last word of the query does not have a space character
    *           following it, that word will be required as a prefix match only.
    *
    *           For example: The query "family test" will return results that contain 'Family Testudinidae' The query "family test " will not
    *           match on 'Testudinidae', so that will be excluded.
    * 
    * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
    *           content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
    *           {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
    *           size limit with and passing pageNum is the recommended way of handling large result sets.
    * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
    *           is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    *           indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
    *           {@link IndexQueryService#getIndexedGenerationCallable(int)}
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches.
    */
   public List<SearchResult> queryData(String queryString,
         boolean prefixSearch,
         Integer[] assemblageConcepts,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);
}
