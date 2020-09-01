/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.isaac.provider.query.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreCachingWrappingScorer;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHits;
import javafx.util.Pair;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsaacFilteredCollectorManager implements CollectorManager<IsaacFilteredCollectorManager.IsaacFilteredCollector, TopDocs> {

	final Predicate<Integer> filter;
	final int sizeLimit;
	final PreviousResult after;
	final ArrayList<IsaacFilteredCollector> collectors = new ArrayList<>();
	
	protected static final Logger LOG = LogManager.getLogger();

	/**
	 * 
	 * @param filter - optional - arbitrary filtering
	 * @param sizeLimit - max number of results to return
	 * @param after - optional - specify to begin on a future page of results.  Should be the last ScoreDoc from a previous query, and all doc IDs already 
	 *     returned on a lower page.
	 */
	public IsaacFilteredCollectorManager(Predicate<Integer> filter, int sizeLimit, PreviousResult after) {
		LOG.trace("Filter init filter: {}, sizeLimit: {}, after: {}", filter == null ? null : filter.hashCode(), sizeLimit, after);
		this.filter = filter;
		this.sizeLimit = sizeLimit;
		this.after = after;
	}
	
	TopDocs getTopDocs() throws IOException {
		return reduce(collectors);
	}

	@Override
	public IsaacFilteredCollector newCollector() throws IOException {
		IsaacFilteredCollector collector =  new IsaacFilteredCollector();
		collectors.add(collector);
		return collector;
	}

	@Override
	public TopDocs reduce(Collection<IsaacFilteredCollectorManager.IsaacFilteredCollector> collectors) throws IOException {
		//We may have duplicates in our index, if a single semantic version is indexed multiple times, by accident, or, 
		//if a semantic has multiple versions that each match our score.
		//Since we return results based on the semantic nid, not the version, we only want to return each nid once, with 
		//the best score.  So we need to go through our preliminary results, and remove any duplicates.
		//Note, that each searcher already removes some duplicate nids, (especially cases where things were indexed more than once)
		//But will not remove duplicate nids that have differing scores for different versions of a semantic.  Must handle here
		//after we have sorted scores per searcher
		HashMap<Integer, HashSet<Integer>> itemsToRemove = new HashMap<>(); //collector id / docs to skip
		HashMap<Integer, Pair<Float, Runnable>> nidsBeingReturned = new HashMap<>(); //semantic nid / {score,function that will add itself into itemsToRemove}
		HashMap<Integer, Integer> nidsToDocId = new HashMap<>(); //semantic nid / docId
		
		int collectorId = 0;
		for (IsaacFilteredCollectorManager.IsaacFilteredCollector collector : collectors) {
			itemsToRemove.put(collectorId, new HashSet<>());
			
			for (int docPosition = 0; docPosition < collector.getTopDocs().scoreDocs.length; docPosition++)
			{
				final int collectorIdFinal = collectorId;
				final int docPositionFinal = docPosition;

				int componentNid = collector.docToNid.get(collector.getTopDocs().scoreDocs[docPosition].doc);
				float score = collector.getTopDocs().scoreDocs[docPosition].score;
				if (after != null && (after.includedDocIds.contains(collector.getTopDocs().scoreDocs[docPosition].doc) || after.includedNids.contains(componentNid))) {
					//Ignore this one
					itemsToRemove.get(collectorId).add(docPosition);
					LOG.trace("Ignore nid " + componentNid + " from doc " + collector.getTopDocs().scoreDocs[docPosition].doc + " for being on previous page");
				}
				else if (nidsBeingReturned.containsKey(componentNid)) {
					Pair<Float, Runnable> info = nidsBeingReturned.get(componentNid);
					if (info.getKey() < score) {
						//The one we already saw has a lower score than us... need to mark that one for removal and keep this one
						info.getValue().run();  //This marks the existing one for removal
						nidsBeingReturned.put(componentNid, new Pair<>(score, () -> itemsToRemove.get(collectorIdFinal).add(docPositionFinal)));
					}
					else if (info.getKey() == score) {
						//The one we already saw has an identical score.  Keep the lower doc ID, to keep us sane / working on the next paged query.
						if (nidsToDocId.get(componentNid) < (collector.getTopDocs().scoreDocs[docPosition].doc))
						{
							//Ignore this one
							itemsToRemove.get(collectorId).add(docPosition);
						}
						else
						{
							info.getValue().run();  //This marks the existing one for removal
							nidsBeingReturned.put(componentNid, new Pair<>(score, () -> itemsToRemove.get(collectorIdFinal).add(docPositionFinal)));
							nidsToDocId.put(componentNid, collector.getTopDocs().scoreDocs[docPosition].doc);
						}
					}
					else {
						//Ignore this one
						itemsToRemove.get(collectorId).add(docPosition);
					}
				 }
				 else {
					nidsBeingReturned.put(componentNid, new Pair<>(score, () -> itemsToRemove.get(collectorIdFinal).add(docPositionFinal)));
					nidsToDocId.put(componentNid, collector.getTopDocs().scoreDocs[docPosition].doc);
				}
			}
			collectorId++;
		}

		//We should now have a proper set of itemsToRemove we can use to filter our getTopDocs arrays
		
		collectorId = 0;
		final TopDocs[] topDocs = new TopDocs[collectors.size()];
		
		for (IsaacFilteredCollectorManager.IsaacFilteredCollector collector : collectors) {
			ArrayList<ScoreDoc> scoreDocsToKeep = new ArrayList<>(collector.getTopDocs().scoreDocs.length);
			for (int docPos = 0; docPos < collector.getTopDocs().scoreDocs.length; docPos++) {
				if (!itemsToRemove.get(collectorId).contains(docPos)) {
					scoreDocsToKeep.add(collector.getTopDocs().scoreDocs[docPos]);
					LOG.trace("Keeping document {} with score {} in the results", 
							collector.getTopDocs().scoreDocs[docPos].doc, collector.getTopDocs().scoreDocs[docPos].score);
				}
				else {
					LOG.trace("Skipping document {} with score {} as it duplicates a nid already in the results", 
							collector.getTopDocs().scoreDocs[docPos].doc, collector.getTopDocs().scoreDocs[docPos].score);
				}
			}
			
			TopDocs td = new TopDocs(new TotalHits((collector.getTopDocs().totalHits.value - (itemsToRemove.get(collectorId).size())), collector.getTopDocs().totalHits.relation), 
					scoreDocsToKeep.toArray(new ScoreDoc[scoreDocsToKeep.size()]));
			topDocs[collectorId] = td;
			collectorId++;
		}
		TopDocs result = TopDocs.merge(sizeLimit, topDocs);
		return result;
	}

	public class IsaacFilteredCollector implements Collector {
		private TopScoreDocCollector collector;
		HashMap<Integer, Integer> docToNid = new HashMap<>();
		HashMap<Integer, Float> nidToScore = new HashMap<>();
		private TopDocs topDocs;
		
		protected IsaacFilteredCollector() {
			//We need to add extra results here, because if most of the searching gets done by a single thread, it will stop collecting results when it sees
			//that the scores are too low.  But if it collected docs that duplicate nids, when we remove the duplicate nid / docs above, we will end up short
			//of the number of results we should have.  The final result limit is done above, in the reduce merge.
			int addOn = (Math.min(sizeLimit * 2, 500));
			collector = TopScoreDocCollector.create(sizeLimit + addOn, after == null ? null : after.lowestScoreDoc, sizeLimit +  addOn);
		}
		
		protected TopDocs getTopDocs() {
			if (topDocs == null) {
				topDocs = collector.topDocs();
				//Done at this point
				nidToScore = null;
				collector = null;
			}
			return topDocs;
		}
		
		@Override
		public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException
		{
			return new LeafCollector()
			{
				private Scorable scorer;
				LeafCollector lf = collector.getLeafCollector(context);
		
				@Override
				public void setScorer(Scorable scorer) throws IOException
				{
					this.scorer = new ScoreCachingWrappingScorer(scorer);
					lf.setScorer(this.scorer);
				}
		
				@Override
				public void collect(int doc) throws IOException
				{
					boolean filterPass = false;
					final Document document = context.reader().document(doc);
					final int componentNid = document.getField(LuceneIndexer.FIELD_COMPONENT_NID).numericValue().intValue();
					
					if (after != null && (after.includedDocIds.contains(doc) || after.includedNids.contains(componentNid))) {
						LOG.trace("skipping filter eval of doc {} and collect for item before page 1 {}", doc, componentNid);
						return;
					}
					else if (nidToScore.containsKey(componentNid) && nidToScore.get(componentNid) >= scorer.score()) {
						//Note that we can't "uncollect" one with a worse score we have already collected, so just collect
						//again here, and we will deal with the dupe nid above, in reduce
						LOG.trace("skipping filter eval of doc {} and collect for item that is already collected {}", doc, componentNid);
						return;
					}
					
					if (filter != null)
					{
						if (Get.identifierService().getObjectTypeForComponent(componentNid) == IsaacObjectType.SEMANTIC) {
							try {
								filterPass = filter.test(componentNid);
							} catch (NoSuchElementException | IllegalStateException e) {
								StringBuilder b = new StringBuilder();
								b.append(e.getLocalizedMessage()).append("\n");
								b.append("Filtering document: ").append(document).append("\n");
								b.append("Evaluating: ").append(componentNid).append(" uuids: ")
										.append(Arrays.toString(Get.identifierService().getUuidArrayForNid(componentNid)));
								LOG.error(b.toString());
							}
						} else {
							LOG.warn("Concept nid in search results: " + componentNid + " " + document);
						}
					}
					else
					{
						filterPass = true;
					}
					
					if (filterPass)
					{
						LOG.trace("Collect on " + (doc + context.docBase) + " with collector " + IsaacFilteredCollector.this.hashCode() + " for " + componentNid);
						lf.collect(doc);
						docToNid.put((doc + context.docBase), componentNid);
						nidToScore.put(componentNid, scorer.score());
					}
				}
			};
		}

		@Override
		public ScoreMode scoreMode()
		{
			return collector.scoreMode();
		}
	}
	
	protected class PreviousResult
	{
		ScoreDoc lowestScoreDoc;
		HashSet<Integer> includedDocIds;
		HashSet<Integer> includedNids;
		
		protected PreviousResult(ScoreDoc lowestScore, HashSet<Integer> includedDocIds, HashSet<Integer> includedNids)
		{
			this.lowestScoreDoc = lowestScore;
			this.includedDocIds = includedDocIds;
			this.includedNids = includedNids;
		}
	}
}
