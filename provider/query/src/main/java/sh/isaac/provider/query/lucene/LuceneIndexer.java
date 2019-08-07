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

package sh.isaac.provider.query.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import javafx.concurrent.Task;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.index.AuthorModulePathRestriction;
import sh.isaac.api.index.ComponentSearchResult;
import sh.isaac.api.index.ConceptSearchResult;
import sh.isaac.api.index.GenerateIndexes;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.IndexStatusListener;
import sh.isaac.api.index.IndexedGenerationCallable;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.api.util.WorkExecutors;

/**
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class LuceneIndexer implements IndexBuilderService
{

	public static final String DEFAULT_LUCENE_FOLDER = "lucene";

	protected static final Logger LOG = LogManager.getLogger();

	private static final CompletableFuture<Long> UNINDEXED_FUTURE = new CompletableFuture<>();
	static
	{
		UNINDEXED_FUTURE.complete(Long.MIN_VALUE);
	}

	// don't need to analyze this - and even though it is an integer, we index it as a string, as that is faster when we are only doing
	// exact matches.
	protected static final String FIELD_SEMANTIC_ASSEMBLAGE_NID = "_semantic_type_sequence_" + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER;

	//don't need to analyze, we only ever put a single char here - "t" - when a description is on a concept that is a part of the metadata tree.
	protected static final String FIELD_CONCEPT_IS_METADATA = "_concept_metadata_marker_" + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER;
	protected static final String FIELD_CONCEPT_IS_METADATA_VALUE = "t";

	// this isn't indexed
	public static final String FIELD_COMPONENT_NID = "_component_nid_";
	private static final String FIELD_INDEXED_MODULE_NID = "_module_content_";
	private static final String FIELD_INDEXED_PATH_NID = "_path_content_";
	private static final String FIELD_INDEXED_AUTHOR_NID = "_author_content_";

	private final Cache<Integer, ScoreDoc> lastDocCache = Caffeine.newBuilder().maximumSize(100).build();

	private File indexFolder = null;

	private final HashMap<String, AtomicInteger> indexedComponentStatistics = new HashMap<>();

	private final Semaphore indexedComponentStatisticsBlock = new Semaphore(1);

	private final ConcurrentHashMap<Integer, IndexedGenerationCallable> componentNidLatch = new ConcurrentHashMap<>();

	private boolean enabled = true;

	private Boolean dbBuildMode = null;

	private DataStoreStartState databaseValidity = DataStoreStartState.NOT_YET_CHECKED;

	private ChronologyChangeListener changeListenerRef;

	protected ExecutorService luceneWriterService;

	protected ExecutorService luceneWriterFutureCheckerService;

	private ControlledRealTimeReopenThread<IndexSearcher> reopenThread;

	private IndexWriter indexWriter;

	private ReferenceManager<IndexSearcher> referenceManager;

	private final String indexName;

	private final ReentrantLock reindexLock = new ReentrantLock();
	
	protected List<WeakReference<IndexStatusListener>> statusListeners = new ArrayList<>();

	/**
	 * Instantiates a new lucene indexer.
	 *
	 * @param indexName the index name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected LuceneIndexer(String indexName)
	{
		this.indexName = indexName;
	}

	private IndexWriterConfig getIndexWriterConfig()
	{
		IndexWriterConfig config = new IndexWriterConfig(new PerFieldAnalyzer());
		config.setRAMBufferSizeMB(256);
		MergePolicy mergePolicy = new LogByteSizeMergePolicy();

		config.setMergePolicy(mergePolicy);
		config.setSimilarity(new ShortTextSimilarity());
		return config;
	}

	/**
	 * Clear index.
	 */
	private final void clearIndex()
	{
		try
		{
			this.indexWriter.deleteAll();
			this.lastDocCache.invalidateAll();;
			//When we wipe the index, write out the data store ID that we know will apply to anything we index going forward
			Files.write(getDataStorePath().resolve(DATASTORE_ID_FILE), Get.assemblageService().getDataStoreId().get().toString().getBytes());
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Clear indexed statistics.
	 */
	private void clearIndexedStatistics()
	{
		this.indexedComponentStatistics.clear();
	}

	/**
	 * Commit writer.
	 */
	private void commitWriter()
	{
		try
		{
			this.indexWriter.commit();
			this.referenceManager.maybeRefreshBlocking();
		}
		catch (final IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refreshQueryEngine()
	{
		try
		{
			this.referenceManager.maybeRefreshBlocking();
		}
		catch (final IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forceMerge()
	{
		try
		{
			this.indexWriter.forceMerge(1);
			this.referenceManager.maybeRefreshBlocking();
		}
		catch (final IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final CompletableFuture<Long> index(Chronology chronicle)
	{
		return index((() -> new AddDocument(chronicle)), (() -> indexChronicle(chronicle)), chronicle.getNid());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long indexNow(Chronology chronicle)
	{
		if (this.enabled && indexChronicle(chronicle))
		{
			return new AddDocument(chronicle).get();
		}
		else
		{
			releaseLatch(chronicle.getNid(), Long.MIN_VALUE);
		}
		return Long.MIN_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ConceptSearchResult> mergeResultsOnConcept(List<SearchResult> searchResult)
	{
		final HashMap<Integer, ConceptSearchResult> merged = new HashMap<>();
		final List<ConceptSearchResult> result = new ArrayList<>();

		searchResult.forEach((sr) -> {
			final OptionalInt conNid = findConcept(sr.getNid());

			if (!conNid.isPresent())
			{
				LOG.error("Failed to find a concept that references nid " + sr.getNid());
			}
			else if (merged.containsKey(conNid.getAsInt()))
			{
				merged.get(conNid.getAsInt()).merge(sr);
			}
			else
			{
				final ConceptSearchResult csr = new ConceptSearchResult(conNid.getAsInt(), sr.getNid(), sr.getScore());

				merged.put(conNid.getAsInt(), csr);
				result.add(csr);
			}
		});

		return result;
	}

	/**
	 * Convenience method to find the nearest concept related to a semantic. Recursively walks referenced components until it finds a concept.
	 *
	 * @param nid the nid
	 * @return the nearest concept nid, or empty, if no concept can be found.
	 */
	public static OptionalInt findConcept(int nid)
	{
		final Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(nid);

		if (c.isPresent())
		{
			if (null == c.get().getIsaacObjectType())
			{
				LOG.warn("Unexpected object type: " + c.get().getIsaacObjectType());
			}
			else
			{
				switch (c.get().getIsaacObjectType())
				{
					case SEMANTIC:
						return findConcept(((SemanticChronology) c.get()).getReferencedComponentNid());

					case CONCEPT:
						return OptionalInt.of(((ConceptChronology) c.get()).getNid());

					default :
						LOG.warn("Unexpected object type: " + c.get().getIsaacObjectType());
						break;
				}
			}
		}

		return OptionalInt.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HashMap<String, Integer> reportIndexedItems()
	{
		final HashMap<String, Integer> result = new HashMap<>();

		this.indexedComponentStatistics.forEach((name, value) -> {
			result.put(name, value.get());
		});
		return result;
	}

	/**
	 * Subclasses must implement this, with their own indexer-specific fields and formats.
	 *
	 * @param chronicle the chronicle
	 * @param doc the doc
	 * @param pathNids - a set of nids that represent the paths that this chonicle lives on. Provided for convenience,
	 *            may be ignored if not required by the implementation.
	 */
	protected abstract void addFields(Chronology chronicle, Document doc, Set<Integer> pathNids);

	/**
	 * Builds the prefix query.
	 *
	 * @param searchString the search string
	 * @param field the field
	 * @param analyzer the analyzer
	 * @return the query
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected Query buildPrefixQuery(String searchString, String field, Analyzer analyzer) throws IOException
	{
		final TokenStream tokenStream;
		final List<String> terms;
		try (StringReader textReader = new StringReader(searchString))
		{
			tokenStream = analyzer.tokenStream(field, textReader);
			tokenStream.reset();
			terms = new ArrayList<>();
			final CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken())
			{
				terms.add(charTermAttribute.toString());
			}
		}
		tokenStream.close();
		analyzer.close();

		final BooleanQuery.Builder bq = new BooleanQuery.Builder();

		if ((terms.size() > 0) && !searchString.endsWith(" "))
		{
			final String last = terms.remove(terms.size() - 1);

			bq.add(new PrefixQuery((new Term(field, last))), Occur.MUST);
		}

		terms.stream().forEach((s) -> {
			bq.add(new TermQuery(new Term(field, s)), Occur.MUST);
		});
		return bq.build();
	}

	/**
	 * Create a query that will match on the specified text using either the WhitespaceAnalyzer or the StandardAnalyzer.
	 * Uses the Lucene Query Parser if prefixSearch is false, otherwise, uses a custom prefix algorithm.
	 * See {@link LuceneIndexer#query(String, boolean, Integer, int, Long)} for details on the prefix search algorithm.
	 *
	 * @param query the query
	 * @param field the field
	 * @param prefixSearch the prefix search
	 * @return the query
	 */
	protected Query buildTokenizedStringQuery(String query, String field, boolean prefixSearch, boolean metadataOnly)
	{
		try
		{
			BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

			if (metadataOnly)
			{
				booleanQueryBuilder.add(new TermQuery(new Term(FIELD_CONCEPT_IS_METADATA, FIELD_CONCEPT_IS_METADATA_VALUE)), Occur.MUST);
			}

			if (prefixSearch)
			{
				BooleanQuery.Builder bqParts = new BooleanQuery.Builder();
				bqParts.add(buildPrefixQuery(query, field, new PerFieldAnalyzer()), Occur.SHOULD);
				bqParts.add(buildPrefixQuery(query, field + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, new PerFieldAnalyzer()), Occur.SHOULD);
				booleanQueryBuilder.add(bqParts.build(), Occur.MUST);
			}
			else
			{
				BooleanQuery.Builder bqParts = new BooleanQuery.Builder();

				final QueryParser qp1 = new QueryParser(field, new PerFieldAnalyzer());

				qp1.setAllowLeadingWildcard(true);
				bqParts.add(qp1.parse(query), Occur.SHOULD);

				final QueryParser qp2 = new QueryParser(field + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, new PerFieldAnalyzer());

				qp2.setAllowLeadingWildcard(true);
				bqParts.add(qp2.parse(query), Occur.SHOULD);
				booleanQueryBuilder.add(bqParts.build(), Occur.MUST);
			}

			final BooleanQuery wrap = new BooleanQuery.Builder().add(booleanQueryBuilder.build(), Occur.MUST).build();
			return wrap;
		}
		catch (IOException | ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return a Query adding the appropriate Stamp criteria for Path and/or Module(s).
	 * 
	 * @param query The query base to add Stamp parameters to
	 * @param stamp The StampCoordinate to further restrict the query
	 * @return
	 */
	protected Query addAmpRestriction(Query query, AuthorModulePathRestriction amp)
	{
		if (amp == null)
		{
			return query;
		}

		BooleanQuery.Builder bq = new BooleanQuery.Builder();

		// Original query
		bq.add(query, Occur.MUST);

		if (amp.getAuthors() != null && !amp.getAuthors().isEmpty())
		{
			BooleanQuery.Builder inner = new BooleanQuery.Builder();

			for (Integer authorNid : amp.getAuthors().asArray())
			{
				inner.add(new TermQuery(new Term(FIELD_INDEXED_AUTHOR_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, authorNid.toString())), Occur.SHOULD);
			}
			bq.add(inner.build(), Occur.MUST);
		}

		if (amp.getModules() != null && !amp.getModules().isEmpty())
		{
			BooleanQuery.Builder inner = new BooleanQuery.Builder();

			for (Integer moduleNid : amp.getModules().asArray())
			{
				inner.add(new TermQuery(new Term(FIELD_INDEXED_MODULE_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, moduleNid.toString())), Occur.SHOULD);
			}
			bq.add(inner.build(), Occur.MUST);
		}

		if (amp.getPaths() != null && !amp.getPaths().isEmpty())
		{
			BooleanQuery.Builder inner = new BooleanQuery.Builder();

			for (Integer pathNid : amp.getPaths().asArray())
			{
				inner.add(new TermQuery(new Term(FIELD_INDEXED_PATH_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, pathNid.toString())), Occur.SHOULD);
			}
			bq.add(inner.build(), Occur.MUST);
		}

		return bq.build();
	}

	/**
	 * Increment indexed item count. Just increments the stats counters.
	 *
	 * @param name the name
	 */
	protected void incrementIndexedItemCount(String name)
	{
		AtomicInteger temp = this.indexedComponentStatistics.get(name);

		if (temp == null)
		{
			try
			{
				this.indexedComponentStatisticsBlock.acquireUninterruptibly();
				temp = this.indexedComponentStatistics.get(name);

				if (temp == null)
				{
					temp = new AtomicInteger(1);
					this.indexedComponentStatistics.put(name, temp);
				}
			}
			finally
			{
				this.indexedComponentStatisticsBlock.release();
			}
		}
		else
		{
			temp.incrementAndGet();
		}
	}

	/**
	 * Allows implementations to specify whether a chronology should be indexed by that particular indexer at all.
	 *
	 * @param chronicle the chronicle to decide if it should be indexed
	 * @return true, if the chronicle should be indexed
	 */
	protected abstract boolean indexChronicle(Chronology chronicle);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> query(String query)
	{
		return query(query, false, null, null, null, null, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> query(String query, Integer sizeLimit)
	{
		return query(query, false, null, null, null, null, sizeLimit, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> query(String query, int[] assemblageConcept, AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit,
			Long targetGeneration)
	{
		return query(query, false, assemblageConcept, null, amp, pageNum, sizeLimit, targetGeneration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> query(String query, boolean prefixSearch, int[] assemblageConcept, AuthorModulePathRestriction amp, Integer pageNum,
			Integer sizeLimit, Long targetGeneration)
	{
		return query(query, prefixSearch, assemblageConcept, null, amp, pageNum, sizeLimit, targetGeneration);
	}

	/**
	 * Release latch.
	 *
	 * @param latchNid the latch nid
	 * @param indexGeneration the index generation
	 */
	private void releaseLatch(int latchNid, long indexGeneration)
	{
		final IndexedGenerationCallable latch = this.componentNidLatch.remove(latchNid);

		if (latch != null)
		{
			latch.setIndexGeneration(indexGeneration);
		}
	}

	/**
	 * Restrict to assemblage(s) of a particular type.
	 *
	 * @param query the query
	 * @param semanticAssemblageNid the semantic assemblage concept
	 * @return the query newly modified query that takes into account the semantic restriction
	 */
	protected Query restrictToSemantic(Query query, int[] semanticAssemblageNid)
	{
		final ArrayList<Integer> nullSafe = new ArrayList<>();

		if (semanticAssemblageNid != null)
		{
			for (final Integer i : semanticAssemblageNid)
			{
				if (i != null)
				{
					nullSafe.add(i);
				}
			}
		}

		if (!nullSafe.isEmpty())
		{
			final BooleanQuery.Builder outerWrapQueryBuilder = new BooleanQuery.Builder();;

			outerWrapQueryBuilder.add(query, Occur.MUST);

			final BooleanQuery.Builder wrapBuilder = new BooleanQuery.Builder();

			// or together the semanticAssemblageNid, but require at least one of them to match.
			nullSafe.forEach((i) -> {
				wrapBuilder.add(new TermQuery(new Term(FIELD_SEMANTIC_ASSEMBLAGE_NID, i.toString())), Occur.SHOULD);
			});

			return outerWrapQueryBuilder.add(wrapBuilder.build(), Occur.MUST).build();
		}
		else
		{
			return query;
		}
	}

	/**
	 * This method does the actual lucene search, collecting the results, merging them on NID and handling paging.
	 *
	 * @param q - the query
	 * @param filter - an optional filter on results - if provided, the filter should expect nids, and can return true, if the nid should be
	 *            allowed in the result, false otherwise. Note that this may cause large performance slowdowns, depending on the implementation
	 *            of your filter. If you are utilizing filters along with paging, ensure that your filter has a proper implementation of hashCode()
	 * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
	 * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
	 * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
	 *            size limit with and passing pageNum is the recommended way of handling large result sets.
	 * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
	 *            is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
	 *            indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
	 *            {@link IndexQueryService#getIndexedGenerationCallable(int)}
	 * 
	 *            Implementation note - this actually returns a list of {@link ComponentSearchResult}
	 * @return the list
	 */
	protected final List<SearchResult> search(Query q, Predicate<Integer> filter, AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit,
			Long targetGeneration)
	{
		return searchInternal(q, filter, amp, pageNum, sizeLimit, targetGeneration, null);
	}

	/**
	 * see {@link #search(Query, Predicate, AmpRestriction, Integer, Integer, Long)} for details on this method.
	 * 
	 * This API variation is simply so a second object can be returned to the caller (via the lastDoc reference)
	 * for special cases.
	 * 
	 * Implementation note - this actually returns a list of {@link ComponentSearchResult}
	 * 
	 * @param lastDoc - the passed reference will be updated with the last ScoreDoc of the search.
	 */
	private final List<SearchResult> searchInternal(Query q, Predicate<Integer> filter, AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit,
			Long targetGeneration, AtomicReference<ScoreDoc> lastDoc)
	{

		IndexSearcher searcher = null;
		try
		{
			searcher = getIndexSearcher(targetGeneration);
			// Include the module and path selelctions
			q = this.addAmpRestriction(q, amp);

			LOG.debug("Running query: {}", q.toString());

			int internalPage = pageNum == null ? 1 : pageNum < 1 ? 1 : pageNum.intValue();
			int internalSize = sizeLimit == null ? 100 : sizeLimit < 1 ? 1 : sizeLimit.intValue();

			// We're only going to return up to what was requested
			List<SearchResult> results = new ArrayList<>(Math.min(500, internalSize)); // Use page size, only up to 500 for our pre-allocation
			HashSet<Integer> includedComponentNids = new HashSet<>();
			boolean complete = false; // i.e., results.size() < sizeLimit

			ScoreDoc after = getAfterScoreDoc(q, filter, internalPage, internalSize, targetGeneration);

			// Note, we cannot just ask lucene for the same page size / result count as we are asked for, because lucene may have multiple versions
			// of a component indexed, which each match the query. However, since we only return nids, not versions, these results get merged.
			// We will ask lucene for a few extra results up front, and then keep going until the requested number of docs are found, or no more
			// matches/results
			while (!complete)
			{

				//We use this API for search even when we don't have a filter, because this also enables parallel searching in the lower levels of lucene.
				TopDocs topDocs = searcher.search(q, new IsaacFilteredCollectorManager(filter, Math.min(500, internalSize), after));

				// If no scoreDocs exist, we're done
				if (topDocs.scoreDocs.length == 0)
				{
					complete = true;
					LOG.debug("Search exhausted after finding only {} results (of {} requested) from query", results.size(), internalSize);
				}
				else
				{
					for (ScoreDoc hit : topDocs.scoreDocs)
					{
						LOG.trace("Hit: {} Score: {}", new Object[] { hit.doc, hit.score });

						// Save the last doc to search after later, if needed
						after = hit;
						Document doc = searcher.doc(hit.doc);
						int componentNid = doc.getField(FIELD_COMPONENT_NID).numericValue().intValue();
						if (includedComponentNids.contains(componentNid))
						{
							continue;
						}
						else
						{
							includedComponentNids.add(componentNid);
							results.add(new ComponentSearchResult(componentNid, hit.score));
							if (results.size() == internalSize)
							{
								complete = true;
								break;
							}
						}
					}
				}
			}
			LOG.debug("Returning {} results from query", results.size());
			if (lastDoc != null)
			{
				lastDoc.set(after);
			}
			if (after != null)
			{
				this.lastDocCache.put(hashQueryForCache(q, filter, internalPage, internalSize), after);
			}
			else
			{
				this.lastDocCache.invalidate(hashQueryForCache(q, filter, internalPage, internalSize));
			}
			return results;

		}
		catch (IOException e)
		{
			LOG.error("Unexpected error during search", e);
			throw new RuntimeException(e);
		}

		finally
		{
			if (searcher != null)
			{
				try
				{
					this.referenceManager.release(searcher);
				}
				catch (IOException e)
				{
					LOG.error("Unexpected error releasing searcher", e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Read the previous page's last doc from the cache (or calculate it, if the cache hit fails)
	 * 
	 * @param q
	 * @param filter
	 * @param pageNum
	 * @param sizeLimit
	 * @param targetGeneration
	 * @return
	 */
	private ScoreDoc getAfterScoreDoc(Query q, Predicate<Integer> filter, int pageNum, int sizeLimit, Long targetGeneration)
	{

		if (pageNum == 1)
		{
			return null;
		}
		else
		{
			ScoreDoc lastDoc = this.lastDocCache.getIfPresent(hashQueryForCache(q, filter, (pageNum - 1), sizeLimit));
			if (lastDoc != null)
			{
				LOG.debug("Cache hit for last doc");
				return lastDoc;
			}
			else
			{
				//The cache doesn't know what the last doc was of the previous page.  Need to run a full search, and 
				//get the last doc hit.  Just do a single page search, as it is likely that none of the previous pages 
				//are in the cache, and its likely cheapest just to get straight to the result we want, rather than paging 
				//our way up.  If, for some reason, we needed to jump over very large result sets, we may need to reevaluate this
				//to do this in multiple-page jumps to keep memory usage down.
				//Note, we don't pass the AuthorModulePathRestriction parameter, as it was already integrated into the query we were passed.
				AtomicReference<ScoreDoc> temp = new AtomicReference<ScoreDoc>();
				searchInternal(q, filter, null, 1, ((pageNum - 1) * sizeLimit), targetGeneration, temp);
				return temp.get();
			}
		}
	}

	/**
	 * Calculate a hash code for a query. Note that a filter needs to have a reasonable hashcode for this to be effective.
	 * 
	 * @param q
	 * @param filter
	 * @param pageNum
	 * @param sizeLimit
	 * @return
	 */
	private int hashQueryForCache(Query q, Predicate<Integer> filter, int pageNum, int sizeLimit)
	{
		return q.hashCode() ^ (filter == null ? 0 : filter.hashCode()) ^ (pageNum - 1) ^ sizeLimit;
	}

	/**
	 * Get the index search, waiting if requested and necessary for a reopen.
	 * 
	 * @param targetGeneration
	 * @return
	 * @throws RuntimeException
	 * @throws IOException
	 */
	private IndexSearcher getIndexSearcher(Long targetGeneration) throws RuntimeException, IOException
	{
		if ((targetGeneration != null) && (targetGeneration != Long.MIN_VALUE))
		{
			if (targetGeneration == Long.MAX_VALUE)
			{
				this.referenceManager.maybeRefreshBlocking();
			}
			else
			{
				try
				{
					this.reopenThread.waitForGeneration(targetGeneration);
				}
				catch (final InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		final IndexSearcher searcher = this.referenceManager.acquire();
		return searcher;
	}

	/**
	 * Index.
	 *
	 * @param documentSupplier the document supplier
	 * @param indexChronicle the index chronicle
	 * @param chronicleNid the chronicle nid
	 * @return the future
	 */
	private CompletableFuture<Long> index(Supplier<AddDocument> documentSupplier, BooleanSupplier indexChronicle, int chronicleNid)
	{
		if (!this.enabled)
		{
			releaseLatch(chronicleNid, Long.MIN_VALUE);
			return UNINDEXED_FUTURE;
		}

		if (indexChronicle.getAsBoolean())
		{
			final CompletableFuture<Long> completableFuture = CompletableFuture.supplyAsync(documentSupplier.get(), luceneWriterService);
			luceneWriterFutureCheckerService.execute(new FutureChecker(completableFuture));
			return completableFuture;
		}
		else
		{
			releaseLatch(chronicleNid, Long.MIN_VALUE);
		}

		return UNINDEXED_FUTURE;
	}

	@PostConstruct
	private void startMe()
	{
		if (!Get.configurationService().getGlobalDatastoreConfiguration().enableLuceneIndexes())
		{
			return;
		}
		LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting " + getIndexerName() + " provider");
		Get.executor().execute(progressTask);

		try
		{
			LOG.info("Starting " + getIndexerName() + " post-construct");
			this.luceneWriterService = LookupService.getService(WorkExecutors.class).getIOExecutor();
			try
			{
				final Path searchFolder = LookupService.getService(ConfigurationService.class).getDataStoreFolderPath().resolve("search");
				final File luceneRootFolder = new File(searchFolder.toFile(), DEFAULT_LUCENE_FOLDER);

				this.luceneWriterFutureCheckerService = Executors.newFixedThreadPool(1, new NamedThreadFactory(indexName + " Lucene future checker", false));

				luceneRootFolder.mkdirs();
				this.indexFolder = new File(luceneRootFolder, indexName);

				if (!this.indexFolder.exists() || this.indexFolder.list().length == 0)
				{
					this.databaseValidity = DataStoreStartState.NO_DATASTORE;
					LOG.info("Index folder missing or empty: " + this.indexFolder.getAbsolutePath());
				}
				else if (this.indexFolder.list().length > 0)
				{
					this.databaseValidity = DataStoreStartState.EXISTING_DATASTORE;

					if (!getDataStorePath().resolve(DATASTORE_ID_FILE).toFile().isFile())
					{
						LOG.warn("Existing index loaded from {}, but no datastore id was present!", getDataStorePath());
						Files.write(getDataStorePath().resolve(DATASTORE_ID_FILE), Get.assemblageService().getDataStoreId().get().toString().getBytes());
					}
				}
				else
				{
					LOG.error("Unexpected logic");
					this.databaseValidity = DataStoreStartState.NO_DATASTORE;
				}

				this.indexFolder.mkdirs();

				boolean reindexRequired = this.databaseValidity == DataStoreStartState.NO_DATASTORE;

				LOG.info("Index: {} " + (reindexRequired ? "" : " data store id {} "), this.indexFolder.getAbsolutePath(), getDataStoreId());

				final Directory indexDirectory = new MMapDirectory(this.indexFolder.toPath()); // switch over to MMapDirectory - in theory - this gives us back some
				// room on the JDK stack, letting the OS directly manage the caching of the index files - and more importantly, gives us a huge
				// performance boost during any operation that tries to do multi-threaded reads of the index (like the SOLOR rules processing) because
				// the default value of SimpleFSDirectory is a huge bottleneck.

				try
				{
					this.indexWriter = new IndexWriter(indexDirectory, getIndexWriterConfig());

					Optional<UUID> temp = getDataStoreId();

					if (temp.isPresent() && !temp.get().equals(Get.assemblageService().getDataStoreId().get()))
					{
						LOG.error("Index ID {} does not match assemblage service ID {}.  Did someone swap indexes?  Reindexing...", temp,
								Get.assemblageService().getDataStoreId());
						throw new IndexFormatTooOldException("Index Mismatch", "Index mismatch");
					}
				}
				catch (IndexFormatTooOldException e)
				{
					//TODO [DAN 1] test we should be able to catch other corrupt index issues here, and also solve by just reindexing... need to test
					LOG.warn("Lucene index format was too old or didn't match the assemblage service in'" + getIndexerName() + "'.  Reindexing!");
					RecursiveDelete.delete(this.indexFolder);
					this.databaseValidity = DataStoreStartState.NO_DATASTORE;
					this.indexFolder.mkdirs();
					this.indexWriter = new IndexWriter(indexDirectory, getIndexWriterConfig());
					reindexRequired = true;
				}

				// In the case of a blank index, we need to kick it to disk, otherwise, the search manager constructor fails.
				this.indexWriter.commit();

				final boolean applyAllDeletes = false;
				final boolean writeAllDeletes = false;

				// To get concurrent search, we have to provide an executor service (and use the IsaacFilteredCollectorManager)
				this.referenceManager = new SearcherManager(this.indexWriter, applyAllDeletes, writeAllDeletes, new SearcherFactory()
				{
					@Override
					public IndexSearcher newSearcher(IndexReader reader, IndexReader previousReader) throws IOException
					{
						return new IndexSearcher(reader, Get.workExecutors().getIOExecutor());
					}

				});

				// [3]: Create the ControlledRealTimeReopenThread that reopens the index periodically taking into
				// account the changes made to the index and tracked by the TrackingIndexWriter instance
				// The index is refreshed every 60sc when nobody is waiting
				// and every 100 millis whenever is someone waiting (see search method)
				// (see http://lucene.apache.org/core/4_3_0/core/org/apache/lucene/search/NRTManagerReopenThread.html)
				this.reopenThread = new ControlledRealTimeReopenThread<>(this.indexWriter, this.referenceManager, 60.00, 0.1);
				this.startReopenThread();

				// Register for commits:
				LOG.info("Registering indexer " + indexName + " for commits");
				this.changeListenerRef = new ChronologyChangeListener()
				{
					@Override
					public void handleCommit(CommitRecord commitRecord)
					{
						if (LuceneIndexer.this.dbBuildMode == null)
						{
							LuceneIndexer.this.dbBuildMode = Get.configurationService().isInDBBuildMode();
						}

						if (LuceneIndexer.this.dbBuildMode)
						{
							LOG.debug("Ignore commit due to db build mode");
							return;
						}

						final int size = commitRecord.getSemanticNidsInCommit().size();

						if (size < 100)
						{
							LOG.info("submitting semantic elements " + commitRecord.getSemanticNidsInCommit().toString() + " to indexer " + getIndexerName()
									+ " due to commit");
						}
						else
						{
							LOG.info("submitting " + size + " semantic elements to indexer " + getIndexerName() + " due to commit");
						}

						ArrayList<Future<Long>> futures = new ArrayList<>();
						commitRecord.getSemanticNidsInCommit().stream().forEach(semanticId -> {
							final SemanticChronology sc = Get.assemblageService().getSemanticChronology(semanticId);

							futures.add(index(sc));
						});
						// wait for all indexing operations to complete
						for (Future<Long> f : futures)
						{
							try
							{
								f.get();
							}
							catch (InterruptedException | ExecutionException e)
							{
								log.error("Unexpected error waiting for index update", e);
							}
						}
						commitWriter();
						LOG.info("Completed index of " + size + " semantics for " + getIndexerName());
					}

					@Override
					public void handleChange(SemanticChronology sc)
					{
						// noop
					}

					@Override
					public void handleChange(ConceptChronology cc)
					{
						// noop
					}

					@Override
					public UUID getListenerUuid()
					{
						return UuidT5Generator.get(getIndexerName());
					}
				};
				Get.commitService().addChangeListener(this.changeListenerRef);

				if (reindexRequired)
				{
					LOG.info("Starting reindex of '" + getIndexerName() + "' due to out-of-date index");
					GenerateIndexes gi = new GenerateIndexes(this);
					LookupService.getService(WorkExecutors.class).getExecutor().execute(gi);
					gi.get();
					LOG.info("Reindex complete");
				}
			}
			catch (InterruptedException | ExecutionException | IOException e)
			{
				LOG.fatal("Error during indexer start", e);
				LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure(indexName, e);
				throw new RuntimeException(e);
			}

			LOG.info("Indexer {} Started", this.indexName);
		}
		finally
		{
			progressTask.finished();
		}
	}

	private void startReopenThread()
	{
		this.reopenThread.setName("Lucene " + this.indexName + " Reopen Thread");
		this.reopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
		this.reopenThread.setDaemon(true);
		this.reopenThread.start();
	}

	@PreDestroy
	protected void stopMe()
	{
		LOG.info("Stopping " + getIndexerName() + " pre-destroy. ");
		if (!Get.configurationService().getGlobalDatastoreConfiguration().enableLuceneIndexes())
		{
			return;
		}
		Get.commitService().removeChangeListener(this.changeListenerRef);
		commitWriter();
		try
		{
			this.reopenThread.close();
			this.referenceManager.close();

			// We don't shutdown the writer service we are using, because it is the core isaac thread pool.
			// waiting for the future checker service is sufficient to ensure that all write operations are complete.
			this.luceneWriterFutureCheckerService.shutdown();
			this.luceneWriterFutureCheckerService.awaitTermination(15, TimeUnit.MINUTES);
			this.indexWriter.close();
		}
		catch (InterruptedException | IOException ex)
		{
			throw new RuntimeException(ex);
		}
		this.databaseValidity = DataStoreStartState.NOT_YET_CHECKED;
		this.lastDocCache.invalidateAll();
		clearIndexedStatistics();
		this.dbBuildMode = null;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Path getDataStorePath()
	{
		if (!Get.configurationService().getGlobalDatastoreConfiguration().enableLuceneIndexes())
		{
			return null;
		}
		return this.indexFolder.toPath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataStoreStartState getDataStoreStartState()
	{
		return this.databaseValidity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexedGenerationCallable getIndexedGenerationCallable(int nid)
	{
		final IndexedGenerationCallable indexedLatch = new IndexedGenerationCallable();
		final IndexedGenerationCallable existingIndexedLatch = this.componentNidLatch.putIfAbsent(nid, indexedLatch);

		if (existingIndexedLatch != null)
		{
			return existingIndexedLatch;
		}

		return indexedLatch;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIndexerName()
	{
		return this.indexName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<Void> sync()
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				commitWriter();
				return null;
			}
		};

		Get.executor().submit(t);
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<UUID> getDataStoreId()
	{
		Path p = getDataStorePath();
		if (p != null)
		{
			p = p.resolve(DATASTORE_ID_FILE);
		}
		try
		{
			if (p != null && p.toFile().isFile())
			{
				return Optional.of(UUID.fromString(new String(Files.readAllBytes(p))));
			}
		}
		catch (IOException e)
		{
			LOG.error("Error reading dataStoreId from {}", p);
		}
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startBatchReindex()
	{
		LOG.info("locking for reindex");
		
		synchronized (statusListeners)
		{
			Iterator<WeakReference<IndexStatusListener>> it = statusListeners.iterator();
			while (it.hasNext())
			{
				IndexStatusListener isl = it.next().get();
				if (isl == null)
				{
					it.remove();
				}
				else
				{
					isl.reindexBegan(this);
				}
			}	
		}
		
		if (!reindexLock.isHeldByCurrentThread())
		{
			reindexLock.lock();
		}
		clearIndexedStatistics();
		clearIndex();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishBatchReindex()
	{
		synchronized (statusListeners)
		{
			Iterator<WeakReference<IndexStatusListener>> it = statusListeners.iterator();
			while (it.hasNext())
			{
				IndexStatusListener isl = it.next().get();
				if (isl == null)
				{
					it.remove();
				}
				else
				{
					isl.reindexCompleted(this);
				}
			}	
		}
		if (reindexLock.isHeldByCurrentThread())
		{
			reindexLock.unlock();
			LOG.info("unlocking after reindex");
		}
	}

	@Override
	public int getIndexMemoryInUse()
	{
		return (int) indexWriter.ramBytesUsed();
	}
	
	

	@Override
	public void registerListener(IndexStatusListener statusListener)
	{
		synchronized (statusListeners)
		{
			statusListeners.add(new WeakReference<IndexStatusListener>(statusListener));
		}
	}

	@Override
	public void unregisterListener(IndexStatusListener statusListener)
	{
		synchronized (statusListeners)
		{
			Iterator<WeakReference<IndexStatusListener>> it = statusListeners.iterator();
			while (it.hasNext())
			{
				IndexStatusListener isl = it.next().get();
				if (isl == null || isl.getId().equals(statusListener.getId()))
				{
					it.remove();
				}
			}	
		}
	}
	
	protected void fireIndexConfigurationChanged()
	{
		synchronized (statusListeners)
		{
			Iterator<WeakReference<IndexStatusListener>> it = statusListeners.iterator();
			while (it.hasNext())
			{
				IndexStatusListener isl = it.next().get();
				if (isl == null)
				{
					it.remove();
				}
				else
				{
					isl.indexConfigurationChanged(this);
				}
			}	
		}
	}

	/**
	 * Class to ensure that any exceptions associated with indexingFutures are properly logged.
	 */
	private static class FutureChecker implements Runnable
	{

		CompletableFuture<Long> future;

		public FutureChecker(CompletableFuture<Long> future)
		{
			this.future = future;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run()
		{
			try
			{
				this.future.get();
			}
			catch (InterruptedException | ExecutionException ex)
			{
				LOG.fatal("Unexpected error in future checker!", ex);
			}
		}
	}

	private class AddDocument implements Supplier<Long>
	{
		Chronology chronicle = null;

		/**
		 * Instantiates a new adds the document.
		 *
		 * @param chronicle the chronicle
		 */
		public AddDocument(Chronology chronicle)
		{
			this.chronicle = chronicle;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long get()
		{
			try
			{
				final Document doc = new Document();
				doc.add(new StoredField(FIELD_COMPONENT_NID, this.chronicle.getNid()));
				Set<Integer> foundPathNids = indexStamp(chronicle, doc);
				addFields(this.chronicle, doc, foundPathNids);
				// Note that the addDocument operation could cause duplicate documents to be
				// added to the index if a new version is added after initial index
				// creation. It does this to avoid the performance penalty of
				// finding and deleting documents prior to inserting a new one.
				//
				// At this point, the number of duplicates should be
				// small, and we are willing to accept a small number of duplicates
				// because the new versions are additive (we don't allow deletion of content)
				// so the search results will be the same. Duplicates can be removed
				// by regenerating the index.
				final long indexGeneration = LuceneIndexer.this.indexWriter.addDocument(doc);

				releaseLatch(getNid(), indexGeneration);
				return indexGeneration;
			}
			catch (IOException ex)
			{
				throw new RuntimeException(ex);
			}
		}

		/**
		 * Add the necessary ids to the index to represent author, module and path
		 * 
		 * @param chron
		 * @param doc
		 * @return the nids of the unique paths found
		 */
		private Set<Integer> indexStamp(Chronology chron, Document doc)
		{
			Set<Integer> uniqPathNid = new HashSet<>();
			Set<Integer> uniqModuleNid = new HashSet<>();
			Set<Integer> uniqAuthorNid = new HashSet<>();

			for (Version sv : chron.getVersionList())
			{
				if (!uniqAuthorNid.contains(sv.getAuthorNid()))
				{
					doc.add(new TextField(FIELD_INDEXED_AUTHOR_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, sv.getAuthorNid() + "", Field.Store.NO));
					incrementIndexedItemCount("Author");
					uniqAuthorNid.add(sv.getAuthorNid());
				}

				if (!uniqModuleNid.contains(sv.getModuleNid()))
				{
					doc.add(new TextField(FIELD_INDEXED_MODULE_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, sv.getModuleNid() + "", Field.Store.NO));
					incrementIndexedItemCount("Module");
					uniqModuleNid.add(sv.getModuleNid());
				}

				if (!uniqPathNid.contains(sv.getPathNid()))
				{
					doc.add(new TextField(FIELD_INDEXED_PATH_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, sv.getPathNid() + "", Field.Store.NO));
					incrementIndexedItemCount("Path");
					uniqPathNid.add(sv.getPathNid());
				}
			}
			return uniqPathNid;
		}

		/**
		 * Gets the nid.
		 *
		 * @return the nid
		 */
		public int getNid()
		{
			return this.chronicle.getNid();
		}
	}
}
