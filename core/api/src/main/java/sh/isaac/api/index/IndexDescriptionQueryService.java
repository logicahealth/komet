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
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.ArrayUtil;

/**
 * An extended query interface that supports very specific querying of semantics - especially with dynamic and/or multi-column semantic data
 * types.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IndexDescriptionQueryService extends IndexQueryService {

	/**
    * An extended query option from the Description Indexer, which allows the following additional criteria:
 
 - specifying that search results should be from the metadata
 - specifying the description type to search (FQN, Definition, Regular)
 - specifying the extended description type (description type assemblage concepts from non-snomed terminologies)
 
 Everything else is the same as @see #query(String, boolean, int[], Predicate, AuthorModulePathRestriction, Integer, Integer, Long)
    *
    * @param query The query to apply.  In the description Indexer implementations, the indexer has special rules for handling 
    *           semantic tags - which will disallow the use of the lucene query parser "grouping" feature, if the group appears
    *           to be a semantic tag (if it appears at the end of the string)
    *           {@link https://lucene.apache.org/core/7_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description}
    *           
    *           The Lucene query indexer also supports handling regular expressions - submit your query surrounding by forward slashes to indicate
    *           a regular expression:  /dat[^a].*./
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
    *           content in this index. This could be set to {@link MetaData#ENGLISH_DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
    *           {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
    * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
    *           will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
    *           true, or false, to have the item excluded.
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param metadataOnly - Only search descriptions on concepts which have a commit on the module {@link MetaData#CORE_METADATA_MODULE____SOLOR}
    *      when true, otherwise, search all descriptions.  Note that when metadataOnly is set to true, it will return results that are metadata on SOME
           stamp, not necessarily the passed in AuthorModulePathRestriction.  If you only want results that are metadata on your current coordinate, 
           you will have to post-filter the result. 
    * @param descriptionTypes - optional - if specified, will only match descriptions of the specified type(s).
    * @param extendedDescriptionTypes - optional - if specified, will only match descriptions with an extension semantic of the specified type(s)
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
         boolean metadataOnly,
         int[] descriptionTypes,
         int[] extendedDescriptionTypes,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration);
   
   /**
    * See {@link #query(String, boolean, int[], Predicate, AuthorModulePathRestriction, boolean, int[], int[], Integer, Integer, Long)}
    */
   default List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcepts,
         Predicate<Integer> filter,
         AuthorModulePathRestriction amp,
         boolean metadataOnly,
         ConceptSpecification[] descriptionTypes,
         ConceptSpecification[] extendedDescriptionTypes,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
       
       return query(query,
         prefixSearch,
         assemblageConcepts,
         filter,
         amp,
         metadataOnly,
         ArrayUtil.toNidArray(descriptionTypes),
         ArrayUtil.toNidArray(extendedDescriptionTypes),
         pageNum,
         sizeLimit,
         targetGeneration);
   }
}
