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
import java.util.Collection;
import java.util.function.Predicate;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;

/**
 *
 * @author kec
 */
public class IsaacFilteredCollectorManager implements CollectorManager<IsaacFilteredCollectorManager.IsaacFilteredCollector, TopDocs> {

   final Predicate<Integer> filter;
   final int sizeLimit;
   final ArrayList<IsaacFilteredCollector> collectors = new ArrayList<>();

   public IsaacFilteredCollectorManager(Predicate<Integer> filter, int sizeLimit) {
      this.filter = filter;
      this.sizeLimit = sizeLimit;
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
      final TopDocs[] topDocs = new TopDocs[collectors.size()];
      int i = 0;
      int totalSize = 0;
      for (IsaacFilteredCollectorManager.IsaacFilteredCollector collector : collectors) {
         topDocs[i] = collector.topDocs();
         totalSize = totalSize + topDocs[i++].scoreDocs.length;
      }
      return TopDocs.merge(0, Math.min(sizeLimit, totalSize), topDocs, true);
   }

   public class IsaacFilteredCollector implements Collector {
      IsaacFilteredLeafCollector leafCollector;
      
      @Override
      public IsaacFilteredLeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
         leafCollector = new IsaacFilteredLeafCollector(context, filter);
         return leafCollector;
      }

      @Override
      public boolean needsScores() {
         return true;
      }
      
      public TopDocs topDocs() {
         return leafCollector.topDocs();
      }

   }

   public static class IsaacFilteredLeafCollector implements LeafCollector {

      final int docBase;
      final LeafReader reader;
      Scorer scorer;
      final Predicate<Integer> filter;
      final ArrayList<ScoreDoc> results = new ArrayList<>();
      float maxScore = 0;
      int totalHits = 0;

      private IsaacFilteredLeafCollector(final LeafReaderContext context, Predicate<Integer> filter) throws IOException {
         docBase = context.docBase;
         reader = context.reader();
         this.filter = filter;
      }

      @Override
      final public void setScorer(final Scorer scorer) throws IOException {
         this.scorer = scorer;
      }

      @Override
      final public void collect(final int doc) throws IOException {
         final Document document = reader.document(doc);
         final int componentNid = document.getField(LuceneIndexer.FIELD_COMPONENT_NID)
                 .numericValue()
                 .intValue();
         totalHits++;
         if (filter.test(componentNid)) {
            maxScore = Math.max(maxScore, scorer.score());
            results.add(new ScoreDoc(scorer.docID(), scorer.score()));
         }
      }

      public TopDocs topDocs() {

         results.sort((ScoreDoc hitA, ScoreDoc hitB) -> {
            if (hitA.score == hitB.score) {
               if (hitA.doc < hitB.doc) {
                  return -1;
               }
               return 1;
            } else {
               if (hitA.score > hitB.score) {
                  return -1;
               } else if (hitA.score < hitB.score) {
                  return 1;
               }
               return 0;
            }
         });

         ScoreDoc[] resultArray = results.toArray(new ScoreDoc[results.size()]);

 
         return new TopDocs(totalHits, resultArray, maxScore);
      }

   }

   static class IsaacFilteredPriorityQueue extends PriorityQueue<ScoreDoc> {

      public IsaacFilteredPriorityQueue(int maxSize) {
         super(maxSize, false);
      }

      @Override
      protected final boolean lessThan(ScoreDoc hitA, ScoreDoc hitB) {
         if (hitA.score == hitB.score) {
            return hitA.doc > hitB.doc;
         } else {
            return hitA.score < hitB.score;
         }
      }
   }

}
