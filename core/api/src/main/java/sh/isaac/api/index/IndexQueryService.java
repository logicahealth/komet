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

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * The contract interface for basic querying of individual indexes.  Note that individual index implementations
 * will likely extend this with more specific query abilities.
 * 
 * @author aimeefurber
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IndexQueryService {
   
   /**
    * Simple query utilizing defaults suitable for most, across all indexed content in this index
    *
    * @param query The query to apply.
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches.
    */
   public List<SearchResult> query(String query);

   /**
    * Simple query utilizing defaults suitable for most, across all indexed content in this index
    *
    * @param query The query to apply.  
    *           {@link https://lucene.apache.org/core/7_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description}
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Defaults to a system
    *           default size.
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches.
    */
   public List<SearchResult> query(String query,
         Integer sizeLimit);

   /**
    * Simple query still utilizing defaults suitable for most, but allowing additional optional restrictions.
    *
    * @param query - The query to apply
    *           {@link https://lucene.apache.org/core/7_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description}
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
   public List<SearchResult> query(String query,
         int[] assemblageConcepts,
         AuthorModulePathRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);
   /**
    * Simple query still utilizing defaults suitable for most, but allowing additional optional restrictions, and also enabling switching into
    * prefixSearch mode.
    *
    * @param query The query to apply.  In cases where prefixSearch is false, see 
    *           {@link https://lucene.apache.org/core/7_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description}
    *           The query parser is not used in cases there prefixSearch is true.
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
    *           to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
    *           string will be required to be found in the result.
    *
    *           Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
    *           following it, that word will be required as a complete term. If the last word of the query does not have a space character
    *           following it, that word will be required as a prefix match only.
    *
    *           For example: 
    *           The query "family test" will return results that contain 'Family Testudinidae' 
    *           The query "family test " will not match on 'Testudinidae', so that will be excluded.
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
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcepts,
         AuthorModulePathRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);
   
   /**
    * Query with many optional restrictions, and also enabling switching into prefixSearch mode and supplying arbitrary additional query
    * criteria via a predicate.
    *
    * @param query The query to apply.  In cases where prefixSearch is false, see 
    *           {@link https://lucene.apache.org/core/7_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description}
    *           The query parser is not used in cases there prefixSearch is true.
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
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcepts,
         Predicate<Integer> filter,
         AuthorModulePathRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);

   /**
    * Gets the indexed generation callable.
    *
    * @param nid for the component that the caller wished to wait until it's
    * document is added to the index.
    * @return a {@code IndexedGenerationCallable} object that will block until this
    * indexer has added the document to the index. The {@code call()} method
    * on the object will return the index generation that contains the document,
    * which can be used in search calls to make sure the generation is available
    * to the searcher.
    */
   public IndexedGenerationCallable getIndexedGenerationCallable(int nid);
   
   /**
    * After content is indexed, it may be up to 60 seconds until it is visible in queries, unless the user queries with a target 
    * generation.
    * 
    * Alternatively, call this after indexing, to force a reopen now.
    */
   void refreshQueryEngine();

   /**
    * Gets the indexer name.
    *
    * @return the name of this indexer.
    */
   public String getIndexerName();
}

