package sh.isaac.api.index;

import java.util.List;
import java.util.function.Predicate;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.coordinate.StampCoordinate;

@Contract
public interface SemanticIndexer extends IndexService {

   /**
    * @param queryDataLower
    * @param queryDataLowerInclusive
    * @param queryDataUpper
    * @param queryDataUpperInclusive
    * @param sememeConceptSequence (optional) limit the search to the specified assemblage
    * @param searchColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
    * ONE and only one sememeConceptSequence is provided.  May not be provided if 0 or more than 1 sememeConceptSequence values are provided.
    * @param sizeLimit
    * @param targetGeneration (optional) wait for an index to build, or null to not wait
    * @return
    */
   List<SearchResult> queryNumericRange(DynamicData queryDataLower, boolean queryDataLowerInclusive,
         DynamicData queryDataUpper, boolean queryDataUpperInclusive, Integer[] sememeConceptSequence,
         Integer[] searchColumns, int sizeLimit, Long targetGeneration, StampCoordinate stamp);

   /**
    * A convenience method.
    * 
    * Search DynamicSememeData columns, treating them as text - and handling the search in the same mechanism as if this were a
    * call to the method {@link LuceneIndexer#query(String, boolean, Integer, int, long)}
    * 
    * Calls the method {@link #query(DynamicSememeDataBI, Integer, boolean, Integer[], int, long) with a null parameter for
    * the searchColumns, and wraps the queryString into a DynamicSememeString.
    * 
    * @param queryString
    * @param assemblageNid
    * @param prefixSearch
    * @param sizeLimit
    * @param targetGeneration
    * @return
    */
   List<SearchResult> query(String queryString, boolean prefixSearch, Integer[] sememeConceptSequence, int sizeLimit,
         Long targetGeneration, StampCoordinate stamp);

   /**
    * 
    * @param queryData - The query data object (string, int, etc)
    * @param sememeConceptSequence (optional) limit the search to the specified assemblage
    * @param searchColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
    * ONE and only one sememeConceptSequence is provided.  May not be provided if 0 or more than 1 sememeConceptSequence values are provided.
    * @param prefixSearch see {@link LuceneIndexer#query(String, boolean, ComponentProperty, int, Long)} for a description.  Only applicable
    * when the queryData type is string.  Ignored for all other data types.
    * @param sizeLimit
    * @param targetGeneration (optional) wait for an index to build, or null to not wait
    * @return
    */
   //TODO fix this limitation on the column restriction...
   List<SearchResult> query(DynamicData queryData, boolean prefixSearch, Integer[] sememeConceptSequence,
         Integer[] searchColumns, int sizeLimit, Long targetGeneration, StampCoordinate stamp);

   /**
    * @param queryData - The query data object (string, int, etc)
    * @param sememeConceptSequence (optional) limit the search to the specified assemblage
    * @param searchColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
    * ONE and only one sememeConceptSequence is provided.  May not be provided if 0 or more than 1 sememeConceptSequence values are provided.
    * @param prefixSearch see {@link LuceneIndexer#query(String, boolean, ComponentProperty, int, Long)} for a description.  Only applicable
    * when the queryData type is string.  Ignored for all other data types.
    * @param sizeLimit
    * @param targetGeneration (optional) wait for an index to build, or null to not wait
    * @param filter - allows application of exclusionary criteria to the returned result
    * 
    * @return
    */
   //TODO fix this limitation on the column restriction...
   List<SearchResult> query(DynamicData queryData, boolean prefixSearch, Integer[] sememeConceptSequence,
         Integer[] searchColumns, int sizeLimit, Long targetGeneration, Predicate<Integer> filter, StampCoordinate stamp);

   /**
    * Search for matches to the specified nid. Note that in the current implementation, you will only find matches to sememes
    * of type {@link SememeType#COMPONENT_NID} or {@link SememeType#LOGIC_GRAPH}.
    * 
    * This only supports nids, not sequences.
    * 
    * If searching a component nid sememe, this will only match on the attached component nid value.  It will not match 
    * on the assemblage concept, nor the referenced component nid.  Those can be found directly via standard sememe APIs.
    * If searching a logic graph sememe, it will find a match in any concept that is involved in the graph, except for the 
    * root concept.
    * 
    * @param nid the id reference to search for
    * @param semeneConceptSequence optional - The concept seqeuence of the sememe that you wish to search within. If null,
    * searches all indexed content. This would be set to the concept sequence like {@link MetaData#EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE}
    * @param searchColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
    * ONE and only one sememeConceptSequence is provided.  May not be provided if 0 or more than 1 sememeConceptSequence values are provided.
    * @param sizeLimit The maximum size of the result list.
    * @param targetGeneration target generation that must be included in the search or Long.MIN_VALUE if there is no need
    * to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in-progress
    * indexing operations are completed - and then use the latest index.
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that
    * match relative to other matches. Note that scores are pointless for exact id matches - they will all be the same.
    */
   List<SearchResult> query(int nid, Integer[] sememeConceptSequence, Integer[] searchColumns, int sizeLimit,
         Long targetGeneration, StampCoordinate stamp);

}
